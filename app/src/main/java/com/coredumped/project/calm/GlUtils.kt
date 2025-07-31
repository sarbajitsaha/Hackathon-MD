package com.coredumped.project.calm

import android.opengl.GLES30
import android.util.Log

object GlUtils {

    fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES30.glCreateShader(type)
        GLES30.glShaderSource(shader, shaderCode)
        GLES30.glCompileShader(shader)

        // Add error checking
        val compileStatus = IntArray(1)
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compileStatus, 0)
        if (compileStatus[0] == 0) {
            val error = "Could not compile shader $type:"
            Log.e("GlUtils", error)
            Log.e("GlUtils", GLES30.glGetShaderInfoLog(shader))
            GLES30.glDeleteShader(shader)
            throw RuntimeException("$error\n${GLES30.glGetShaderInfoLog(shader)}")
        }
        return shader
    }

    fun createProgram(vertexShaderCode: String, fragmentShaderCode: String): Int {
        val vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentShaderCode)

        val program = GLES30.glCreateProgram()
        GLES30.glAttachShader(program, vertexShader)
        GLES30.glAttachShader(program, fragmentShader)
        // Bind the attribute location for 'aPosition'. This is important.
        GLES30.glBindAttribLocation(program, 0, "aPosition")
        GLES30.glLinkProgram(program)

        // Add error checking
        val linkStatus = IntArray(1)
        GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] == 0) {
            val error = "Could not link program: "
            Log.e("GlUtils", error)
            Log.e("GlUtils", GLES30.glGetProgramInfoLog(program))
            GLES30.glDeleteProgram(program)
            throw RuntimeException("$error\n${GLES30.glGetProgramInfoLog(program)}")
        }
        return program
    }
}