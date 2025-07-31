package com.coredumped.project

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Set immersive mode to hide status and navigation bars
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars()) // Hide status and navigation bars
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE // Allow swipe to show bars temporarily

        // Ensure immersive mode persists after user interaction
        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                // Re-apply immersive mode if system bars become visible
                controller.hide(WindowInsetsCompat.Type.systemBars())
            }
        }
        //enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()  // Add this
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-apply immersive mode when activity resumes
        WindowInsetsControllerCompat(window, window.decorView).hide(WindowInsetsCompat.Type.systemBars())
    }
}


@Preview(
    showSystemUi = true,
    device = "spec:width=411dp,height=891dp,dpi=420,isRound=false,chinSize=0dp,orientation=landscape",
    showBackground = true, locale = "en", name = "HomeScreen English"
)

@Preview(
    showSystemUi = true,
    device = "spec:width=411dp,height=891dp,dpi=420,isRound=false,chinSize=0dp,orientation=landscape",
    showBackground = true, locale = "bn", name = "HomeScreen Bengali"
)
@Composable
fun HomeScreenPreview() {
    // Mock NavController for preview
    val mockNavController = rememberNavController()
    HomeScreen(navController = mockNavController)
}

@Preview(
    showSystemUi = true,
    device = "spec:width=411dp,height=891dp,dpi=420,isRound=false,chinSize=0dp,orientation=landscape",
    showBackground = true, locale = "en", name = "CalmScreen English"
)

@Preview(
    showSystemUi = true,
    device = "spec:width=411dp,height=891dp,dpi=420,isRound=false,chinSize=0dp,orientation=landscape",
    showBackground = true, locale = "bn", name = "CalmScreen Bengali"
)
@Composable
fun CalmScreenPreview() {
    // Mock NavController for preview
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