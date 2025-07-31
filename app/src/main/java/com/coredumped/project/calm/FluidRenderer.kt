package com.coredumped.project.calm

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.util.Log
import android.view.MotionEvent
import com.coredumped.project.calm.ADVECTION_FRAGMENT_SHADER
import com.coredumped.project.calm.BASE_VERTEX_SHADER
import com.coredumped.project.calm.CLEAR_FRAGMENT_SHADER
import com.coredumped.project.calm.COLOR_FRAGMENT_SHADER
import com.coredumped.project.calm.COPY_FRAGMENT_SHADER
import com.coredumped.project.calm.CURL_FRAGMENT_SHADER
import com.coredumped.project.calm.DIVERGENCE_FRAGMENT_SHADER
import com.coredumped.project.calm.DISPLAY_FRAGMENT_SHADER
import com.coredumped.project.calm.GRADIENT_SUBTRACT_FRAGMENT_SHADER
import com.coredumped.project.calm.PRESSURE_FRAGMENT_SHADER
import com.coredumped.project.calm.SPLAT_FRAGMENT_SHADER
import com.coredumped.project.calm.VORTICITY_FRAGMENT_SHADER
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.concurrent.ConcurrentLinkedQueue
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.floor
import kotlin.random.Random

class FluidRenderer(private val context: Context) : GLSurfaceView.Renderer {

    //region Helper Classes
    private data class FBO(
        val fboId: Int,
        val textureId: Int,
        var width: Int,
        var height: Int,
        val texelSizeX: Float,
        val texelSizeY: Float
    ) {
        fun attach(id: Int): Int {
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0 + id)
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId)
            return id
        }
    }

    private class DoubleFBO(var read: FBO, var write: FBO) {
        val width get() = read.width
        val height get() = read.height
        val texelSizeX get() = read.texelSizeX
        val texelSizeY get() = read.texelSizeY
        fun swap() {
            val temp = read
            read = write
            write = temp
        }
    }

    private data class Splat(val x: Float, val y: Float, val dx: Float, val dy: Float, val color: FloatArray)

    private data class Pointer(
        var id: Int = -1,
        var texcoordX: Float = 0f,
        var texcoordY: Float = 0f,
        var prevTexcoordX: Float = 0f,
        var prevTexcoordY: Float = 0f,
        var deltaX: Float = 0f,
        var deltaY: Float = 0f,
        var down: Boolean = false,
        var moved: Boolean = false,
        var color: FloatArray = floatArrayOf(0.15f, 0.15f, 0.15f)
    )
    //endregion

    //region Config
    private val config = mapOf(
        "SIM_RESOLUTION" to 128,
        "DYE_RESOLUTION" to 1024,
        "DENSITY_DISSIPATION" to 1.0f,
        "VELOCITY_DISSIPATION" to 0.2f,
        "PRESSURE" to 0.8f,
        "PRESSURE_ITERATIONS" to 20,
        "CURL" to 30f,
        "SPLAT_RADIUS" to 0.25f,
        "SPLAT_FORCE" to 6000f,
        "SHADING" to true,
        "COLORFUL" to true,
        "BACK_COLOR" to floatArrayOf(0f, 0f, 0f)
    )
    //endregion

    //region GL State
    private var quadVBO: Int = 0
    private var quadIBO: Int = 0

    private var copyProgram = 0
    private var clearProgram = 0
    private var colorProgram = 0
    private var displayProgram = 0
    private var splatProgram = 0
    private var advectionProgram = 0
    private var divergenceProgram = 0
    private var curlProgram = 0
    private var vorticityProgram = 0
    private var pressureProgram = 0
    private var gradientSubtractProgram = 0

    private var dye: DoubleFBO? = null
    private var velocity: DoubleFBO? = null
    private var divergence: FBO? = null
    private var curl: FBO? = null
    private var pressure: DoubleFBO? = null

    private var viewWidth: Int = 0
    private var viewHeight: Int = 0
    private var lastTime = System.currentTimeMillis()
    private val pointers = List(5) { Pointer() } // Support up to 5 touch points
    //endregion

    private val splatQueue = ConcurrentLinkedQueue<Splat>()
    private var prevX: Float = 0f
    private var prevY: Float = 0f

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        fun createProgramWithKeywords(vertexShader: String, fragmentShader: String, keywords: String): Int {
            val versionDirective = "#version"
            val versionIndex = fragmentShader.indexOf(versionDirective)

            // Find the end of the line that contains #version
            val endOfVersionLine = fragmentShader.indexOf('\n', versionIndex)

            // Insert the keywords on the line immediately after #version
            val finalFragmentShader = StringBuilder(fragmentShader).insert(endOfVersionLine + 1, keywords).toString()

            return GlUtils.createProgram(vertexShader, finalFragmentShader)
        }

        // Create programs
        copyProgram = GlUtils.createProgram(BASE_VERTEX_SHADER, COPY_FRAGMENT_SHADER)
        clearProgram = GlUtils.createProgram(BASE_VERTEX_SHADER, CLEAR_FRAGMENT_SHADER)
        colorProgram = GlUtils.createProgram(BASE_VERTEX_SHADER, COLOR_FRAGMENT_SHADER)
        splatProgram = GlUtils.createProgram(BASE_VERTEX_SHADER, SPLAT_FRAGMENT_SHADER)
        advectionProgram = GlUtils.createProgram(BASE_VERTEX_SHADER, ADVECTION_FRAGMENT_SHADER)
        divergenceProgram = GlUtils.createProgram(BASE_VERTEX_SHADER, DIVERGENCE_FRAGMENT_SHADER)
        curlProgram = GlUtils.createProgram(BASE_VERTEX_SHADER, CURL_FRAGMENT_SHADER)
        vorticityProgram = GlUtils.createProgram(BASE_VERTEX_SHADER, VORTICITY_FRAGMENT_SHADER)
        pressureProgram = GlUtils.createProgram(BASE_VERTEX_SHADER, PRESSURE_FRAGMENT_SHADER)
        gradientSubtractProgram = GlUtils.createProgram(BASE_VERTEX_SHADER, GRADIENT_SUBTRACT_FRAGMENT_SHADER)

        val displayKeywords = if (this.config["SHADING"] as Boolean) "#define SHADING\n" else ""
        displayProgram = createProgramWithKeywords(BASE_VERTEX_SHADER, DISPLAY_FRAGMENT_SHADER, displayKeywords)

        setupQuad()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        viewWidth = width
        viewHeight = height
        GLES30.glViewport(0, 0, width, height)

        // **FIX**: Initialize FBOs here, where width and height are known.
        // The check prevents re-initialization on every surface change event (e.g., on-resume).
        if (dye == null) {
            initFramebuffers()
            multipleSplats(10) // Initial splats
        }
    }

    override fun onDrawFrame(gl: GL10?) {
        Log.d("FluidRenderer", "onDrawFrame called")
        GLES30.glClearColor(1.0f, 0.0f, 0.0f, 1.0f)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        return;
        // Guard against drawing before initialization is complete
        if (dye == null) return

        val now = System.currentTimeMillis()
        var dt = (now - lastTime) / 1000.0f
        dt = dt.coerceAtMost(0.016666f)
        lastTime = now

        val color = floatArrayOf(0.5f, 0.0f, 0.0f)
        splat(0.5f, 0.5f, 1000f, 0f, color)

        // **FIX**: Apply inputs from the queue on the GL thread
        applyInputs()

        step(dt)
        render()
    }

    private fun applyInputs() {
        while(splatQueue.isNotEmpty()) {
            val s : Splat = splatQueue.poll() ?: continue
            splat(s.x, s.y, s.dx, s.dy, s.color)
        }
    }


    private fun step(dt: Float) {
        GLES30.glDisable(GLES30.GL_BLEND)

        // Curl
        GLES30.glUseProgram(curlProgram)
        setUniforms(curlProgram, "uVelocity" to velocity!!.read.attach(0), "texelSize" to floatArrayOf(velocity!!.texelSizeX, velocity!!.texelSizeY))
        blit(curl)

        // Vorticity
        GLES30.glUseProgram(vorticityProgram)
        setUniforms(vorticityProgram,
            "uVelocity" to velocity!!.read.attach(0),
            "uCurl" to curl!!.attach(1),
            "curl" to config["CURL"] as Float,
            "dt" to dt,
            "texelSize" to floatArrayOf(velocity!!.texelSizeX, velocity!!.texelSizeY)
        )
        blit(velocity!!.write)
        velocity!!.swap()

        // Divergence
        GLES30.glUseProgram(divergenceProgram)
        setUniforms(divergenceProgram, "uVelocity" to velocity!!.read.attach(0), "texelSize" to floatArrayOf(velocity!!.texelSizeX, velocity!!.texelSizeY))
        blit(divergence)

        // Clear Pressure
        GLES30.glUseProgram(clearProgram)
        GLES30.glUniform1i(GLES30.glGetUniformLocation(clearProgram, "uTexture"), pressure!!.read.attach(0))
        GLES30.glUniform1f(GLES30.glGetUniformLocation(clearProgram, "value"), config["PRESSURE"] as Float)
        blit(pressure!!.write)
        pressure!!.swap()

        // Pressure Iterations
        GLES30.glUseProgram(pressureProgram)
        GLES30.glUniform1i(GLES30.glGetUniformLocation(pressureProgram, "uDivergence"), divergence!!.attach(0))
        for (i in 0 until (config["PRESSURE_ITERATIONS"] as Int)) {
            GLES30.glUniform1i(GLES30.glGetUniformLocation(pressureProgram, "uPressure"), pressure!!.read.attach(1))
            blit(pressure!!.write)
            pressure!!.swap()
        }

        // Gradient Subtract
        GLES30.glUseProgram(gradientSubtractProgram)
        setUniforms(gradientSubtractProgram,
            "uPressure" to pressure!!.read.attach(0),
            "uVelocity" to velocity!!.read.attach(1),
            "texelSize" to floatArrayOf(velocity!!.texelSizeX, velocity!!.texelSizeY)
        )
        blit(velocity!!.write)
        velocity!!.swap()

        // Advection
        GLES30.glUseProgram(advectionProgram)
        GLES30.glUniform1f(GLES30.glGetUniformLocation(advectionProgram, "dt"), dt)
        GLES30.glUniform2f(GLES30.glGetUniformLocation(advectionProgram, "texelSize"), velocity!!.texelSizeX, velocity!!.texelSizeY)

        // Advect Velocity
        GLES30.glUniform1f(GLES30.glGetUniformLocation(advectionProgram, "dissipation"), config["VELOCITY_DISSIPATION"] as Float)
        GLES30.glUniform1i(GLES30.glGetUniformLocation(advectionProgram, "uVelocity"), velocity!!.read.attach(0))
        GLES30.glUniform1i(GLES30.glGetUniformLocation(advectionProgram, "uSource"), velocity!!.read.attach(0))
        blit(velocity!!.write)
        velocity!!.swap()

        // Advect Dye
        GLES30.glUniform1f(GLES30.glGetUniformLocation(advectionProgram, "dissipation"), config["DENSITY_DISSIPATION"] as Float)
        GLES30.glUniform2f(GLES30.glGetUniformLocation(advectionProgram, "dyeTexelSize"), dye!!.texelSizeX, dye!!.texelSizeY)
        GLES30.glUniform1i(GLES30.glGetUniformLocation(advectionProgram, "uVelocity"), velocity!!.read.attach(0))
        GLES30.glUniform1i(GLES30.glGetUniformLocation(advectionProgram, "uSource"), dye!!.read.attach(1))
        blit(dye!!.write)
        dye!!.swap()
    }

    private fun render() {
        val backColor = config["BACK_COLOR"] as FloatArray
        GLES30.glClearColor(backColor[0], backColor[1], backColor[2], 1.0f)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

        // Draw final dye texture
        GLES30.glEnable(GLES30.GL_BLEND)
        GLES30.glBlendFunc(GLES30.GL_ONE, GLES30.GL_ONE_MINUS_SRC_ALPHA)

        GLES30.glUseProgram(displayProgram)
        setUniforms(displayProgram,
            "uTexture" to dye!!.read.attach(0),
            "texelSize" to floatArrayOf(1f / viewWidth, 1f / viewHeight)
        )
        blit(null)

        GLES30.glDisable(GLES30.GL_BLEND)
    }

    fun handleTouchEvent(event: MotionEvent) {
        if (viewWidth == 0 || viewHeight == 0) {
            return
        }

        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                prevX = x
                prevY = y
            }
            MotionEvent.ACTION_MOVE -> {
                val texcoordX = x / viewWidth
                val texcoordY = 1.0f - y / viewHeight

                var deltaX = x - prevX
                var deltaY = y - prevY
                prevX = x
                prevY = y

                deltaX = correctDeltaX(deltaX)
                deltaY = correctDeltaY(deltaY)

                val force = config["SPLAT_FORCE"] as Float
                val dx = deltaX * force
                val dy = deltaY * force * -1.0f // Invert Y-axis for motion

                val color = if(config["COLORFUL"] as Boolean) generateColor() else floatArrayOf(0.15f, 0.15f, 0.15f)

                // **FIX**: Add event to the queue instead of calling splat() directly
                splatQueue.add(Splat(texcoordX, texcoordY, dx, dy, color))
            }
        }
    }

    private fun splatPointer(p: Pointer) {
        val dx = p.deltaX * (config["SPLAT_FORCE"] as Float)
        val dy = p.deltaY * (config["SPLAT_FORCE"] as Float)
        splat(p.texcoordX, p.texcoordY, dx, dy, p.color)
    }

    private fun splat(x: Float, y: Float, dx: Float, dy: Float, color: FloatArray) {
        GLES30.glUseProgram(splatProgram)
        setUniforms(splatProgram,
            "uTarget" to velocity!!.read.attach(0),
            "aspectRatio" to viewWidth.toFloat() / viewHeight.toFloat(),
            "point" to floatArrayOf(x, y),
            "color" to floatArrayOf(dx, dy, 0f),
            "radius" to correctRadius(config["SPLAT_RADIUS"] as Float / 100f)
        )
        blit(velocity!!.write)
        velocity!!.swap()

        GLES30.glUniform1i(GLES30.glGetUniformLocation(splatProgram, "uTarget"), dye!!.read.attach(0))
        GLES30.glUniform3fv(GLES30.glGetUniformLocation(splatProgram, "color"), 1, color, 0)
        blit(dye!!.write)
        dye!!.swap()
    }

    private fun multipleSplats(amount: Int) {
        for (i in 0 until amount) {
            val color = generateColor()
            color[0] *= 10.0f
            color[1] *= 10.0f
            color[2] *= 10.0f
            val x = Random.nextFloat()
            val y = Random.nextFloat()
            val dx = 1000 * (Random.nextFloat() - 0.5f)
            val dy = 1000 * (Random.nextFloat() - 0.5f)

            // **FIX**: Add initial splats to queue
            splatQueue.add(Splat(x, y, dx, dy, color))
        }
    }

    private fun updatePointerDown(pointer: Pointer, id: Int, x: Float, y: Float) {
        pointer.id = id
        pointer.down = true
        pointer.moved = false
        // **FIX**: Use viewWidth and viewHeight class properties
        pointer.texcoordX = x / viewWidth
        pointer.texcoordY = 1.0f - y / viewHeight
        pointer.prevTexcoordX = pointer.texcoordX
        pointer.prevTexcoordY = pointer.texcoordY
        pointer.deltaX = 0f
        pointer.deltaY = 0f
        if (config["COLORFUL"] as Boolean) {
            pointer.color = generateColor()
        }
    }

    private fun updatePointerMove(pointer: Pointer, x: Float, y: Float) {
        pointer.prevTexcoordX = pointer.texcoordX
        pointer.prevTexcoordY = pointer.texcoordY
        // **FIX**: Use viewWidth and viewHeight class properties
        pointer.texcoordX = x / viewWidth
        pointer.texcoordY = 1.0f - y / viewHeight
        pointer.deltaX = correctDeltaX(pointer.texcoordX - pointer.prevTexcoordX)
        pointer.deltaY = correctDeltaY(pointer.texcoordY - pointer.prevTexcoordY)
        pointer.moved = true
    }

    private fun updatePointerUp(pointer: Pointer) {
        pointer.down = false
        pointer.id = -1
    }
    //endregion

    //region Helpers
    // In FluidRenderer.kt

    private fun initFramebuffers() {
        val simRes = getResolution(config["SIM_RESOLUTION"] as Int)
        val dyeRes = getResolution(config["DYE_RESOLUTION"] as Int)

        // **FIX**: Changed GLES30.GL_LINEAR to GLES30.GL_NEAREST for guaranteed support
        dye = createDoubleFBO(dyeRes.first, dyeRes.second, GLES30.GL_RGBA16F, GLES30.GL_RGBA, GLES30.GL_HALF_FLOAT, GLES30.GL_NEAREST)
        velocity = createDoubleFBO(simRes.first, simRes.second, GLES30.GL_RG16F, GLES30.GL_RG, GLES30.GL_HALF_FLOAT, GLES30.GL_NEAREST)

        // These already use GL_NEAREST, so they are fine
        divergence = createFBO(simRes.first, simRes.second, GLES30.GL_R16F, GLES30.GL_RED, GLES30.GL_HALF_FLOAT, GLES30.GL_NEAREST)
        curl = createFBO(simRes.first, simRes.second, GLES30.GL_R16F, GLES30.GL_RED, GLES30.GL_HALF_FLOAT, GLES30.GL_NEAREST)
        pressure = createDoubleFBO(simRes.first, simRes.second, GLES30.GL_R16F, GLES30.GL_RED, GLES30.GL_HALF_FLOAT, GLES30.GL_NEAREST)
    }

    private fun createFBO(w: Int, h: Int, internalFormat: Int, format: Int, type: Int, param: Int): FBO {
        val fbo = IntArray(1)
        GLES30.glGenFramebuffers(1, fbo, 0)
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fbo[0])

        val texture = IntArray(1)
        GLES30.glGenTextures(1, texture, 0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture[0])
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, param)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, param)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, internalFormat, w, h, 0, format, type, null)
        GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D, texture[0], 0)

        GLES30.glViewport(0, 0, w, h)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

        val status = GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER)
        if (status != GLES30.GL_FRAMEBUFFER_COMPLETE) {
            throw RuntimeException("FBO is not complete: $status")
        }

        return FBO(fbo[0], texture[0], w, h, 1f / w, 1f / h)
    }

    private fun createDoubleFBO(w: Int, h: Int, internalFormat: Int, format: Int, type: Int, param: Int): DoubleFBO {
        val fbo1 = createFBO(w, h, internalFormat, format, type, param)
        val fbo2 = createFBO(w, h, internalFormat, format, type, param)
        return DoubleFBO(fbo1, fbo2)
    }

    private fun blit(target: FBO?) {
        if (quadVBO == 0 || quadIBO == 0) {
            Log.e("FluidRenderer", "Attempting to draw without a valid quad VBO/IBO.")
            return
        }
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, target?.fboId ?: 0)
        if (target != null) {
            GLES30.glViewport(0, 0, target.width, target.height)
        } else {
            GLES30.glViewport(0, 0, viewWidth, viewHeight)
        }

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, quadVBO)
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(0, 2, GLES30.GL_FLOAT, false, 0, 0)
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, quadIBO)
        GLES30.glDrawElements(GLES30.GL_TRIANGLES, 6, GLES30.GL_UNSIGNED_SHORT, 0)
        GLES30.glDisableVertexAttribArray(0)
    }

    private fun setupQuad() {
        val vertices = floatArrayOf(-1f, -1f, -1f, 1f, 1f, 1f, 1f, -1f)
        val indices = shortArrayOf(0, 1, 2, 0, 2, 3)

        val vbo = IntArray(1)
        GLES30.glGenBuffers(1, vbo, 0)
        quadVBO = vbo[0]
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, quadVBO)
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, vertices.size * 4,
            FloatBuffer.wrap(vertices), GLES30.GL_STATIC_DRAW)

        val ibo = IntArray(1)
        GLES30.glGenBuffers(1, ibo, 0)
        quadIBO = ibo[0]
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, quadIBO)
        GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER, indices.size * 2,
            ByteBuffer.allocateDirect(indices.size * 2).order(ByteOrder.nativeOrder()).asShortBuffer().put(indices).position(0), GLES30.GL_STATIC_DRAW)
    }

    private fun getResolution(resolution: Int): Pair<Int, Int> {
        val aspectRatio = viewWidth.toFloat() / viewHeight.toFloat()
        val scaledResolution = if (aspectRatio < 1) resolution else (resolution * aspectRatio).toInt()
        val scaledResolutionY = if (aspectRatio < 1) (resolution / aspectRatio).toInt() else resolution

        return if (viewWidth > viewHeight) Pair(scaledResolution, scaledResolutionY) else Pair(scaledResolutionY, scaledResolution)
    }

    private fun generateColor(): FloatArray {
        val c = HSVtoRGB(Random.nextFloat(), 1.0f, 1.0f)
        return floatArrayOf(c[0] * 0.15f, c[1] * 0.15f, c[2] * 0.15f)
    }

    private fun HSVtoRGB(h: Float, s: Float, v: Float): FloatArray {
        val i = floor(h * 6).toInt()
        val f = h * 6 - i
        val p = v * (1 - s)
        val q = v * (1 - f * s)
        val t = v * (1 - (1 - f) * s)
        return when (i % 6) {
            0 -> floatArrayOf(v, t, p)
            1 -> floatArrayOf(q, v, p)
            2 -> floatArrayOf(p, v, t)
            3 -> floatArrayOf(p, q, v)
            4 -> floatArrayOf(t, p, v)
            else -> floatArrayOf(v, p, q)
        }
    }

    private fun correctRadius(radius: Float): Float {
        val aspectRatio = viewWidth.toFloat() / viewHeight.toFloat()
        if (aspectRatio > 1) return radius * aspectRatio
        return radius
    }

    private fun correctDeltaX(delta: Float): Float {
        val aspectRatio = viewWidth.toFloat() / viewHeight.toFloat()
        if (aspectRatio < 1) return delta * aspectRatio
        return delta
    }

    private fun correctDeltaY(delta: Float): Float {
        val aspectRatio = viewWidth.toFloat() / viewHeight.toFloat()
        if (aspectRatio > 1) return delta / aspectRatio
        return delta
    }

    private fun setUniforms(program: Int, vararg pairs: Pair<String, Any>) {
        for ((name, value) in pairs) {
            val location = GLES30.glGetUniformLocation(program, name)
            when (value) {
                is Int -> GLES30.glUniform1i(location, value)
                is Float -> GLES30.glUniform1f(location, value)
                is FloatArray -> when (value.size) {
                    2 -> GLES30.glUniform2fv(location, 1, value, 0)
                    3 -> GLES30.glUniform3fv(location, 1, value, 0)
                    4 -> GLES30.glUniform4fv(location, 1, value, 0)
                }
            }
        }
    }
    //endregion
}