package com.coredumped.project

import android.content.Context
import android.os.Bundle
import android.os.LocaleList
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.rememberNavController
import com.coredumped.project.ui.CalmScreen
import com.coredumped.project.ui.HomeScreen
import com.coredumped.project.ui.LearningScreen
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge() // Handles edge-to-edge display, including setDecorFitsSystemWindows(false)

        // Set up immersive mode
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        // Persist immersive mode on visibility changes
        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                controller.hide(WindowInsetsCompat.Type.systemBars())
            }
        }

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-apply immersive mode on resume
        WindowInsetsControllerCompat(window, window.decorView).hide(WindowInsetsCompat.Type.systemBars())
    }

    override fun attachBaseContext(base: Context) {
        val prefs = base.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val lang = prefs.getString("preferred_language", null)
        if (lang != null) {
            val locale = Locale(lang)
            val localeList = LocaleList(locale)
            val config = base.resources.configuration
            config.setLocales(localeList)
            super.attachBaseContext(base.createConfigurationContext(config))
        } else {
            super.attachBaseContext(base)
        }
    }
}

@Preview(
    showSystemUi = true,
    device = "spec:width=411dp,height=891dp,dpi=420,isRound=false,chinSize=0dp,orientation=landscape",
    showBackground = true,
    locale = "en",
    name = "HomeScreen English"
)
@Preview(
    showSystemUi = true,
    device = "spec:width=411dp,height=891dp,dpi=420,isRound=false,chinSize=0dp,orientation=landscape",
    showBackground = true,
    locale = "bn",
    name = "HomeScreen Bengali"
)
@Composable
fun HomeScreenPreview() {
    val mockNavController = rememberNavController()
    HomeScreen(navController = mockNavController)
}

@Preview(
    showSystemUi = true,
    device = "spec:width=411dp,height=891dp,dpi=420,isRound=false,chinSize=0dp,orientation=landscape",
    showBackground = true,
    locale = "en",
    name = "CalmScreen English"
)
@Preview(
    showSystemUi = true,
    device = "spec:width=411dp,height=891dp,dpi=420,isRound=false,chinSize=0dp,orientation=landscape",
    showBackground = true,
    locale = "bn",
    name = "CalmScreen Bengali"
)

@Composable
fun CalmScreenPreview() {
    val mockNavController = rememberNavController()
    CalmScreen(navController = mockNavController)
}

@Preview(
    showSystemUi = true,
    device = "spec:width=411dp,height=891dp,dpi=420,isRound=false,chinSize=0dp,orientation=landscape",
    showBackground = true, locale = "en", name = "LearningScreen English"
)

@Preview(
    showSystemUi = true,
    device = "spec:width=411dp,height=891dp,dpi=420,isRound=false,chinSize=0dp,orientation=landscape",
    showBackground = true, locale = "bn", name = "LearningScreen Bengali"
)
@Composable
fun LearningScreenPreview() {
    // Mock NavController for preview
    val mockNavController = rememberNavController()
    LearningScreen(navController = mockNavController)
}