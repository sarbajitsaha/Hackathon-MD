package com.coredumped.project.ui

import android.app.Activity
import android.content.Context
import android.provider.Settings.Global.getString
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.coredumped.project.BackgroundMusic
import com.coredumped.project.R
import kotlin.math.min as mathMin


@Composable
fun HomeScreen(navController: NavController) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp

    BackgroundMusic(R.raw.background_music)

    val categories = listOf(
        CategoryData(
            text = "Daily Activity",
            imageResId = R.drawable.brush,
            color = Color(0xFF4CAF50),
            route = "daily_activity"
        ),
        CategoryData(
            text = "Learning",
            imageResId = R.drawable.learning,
            color = Color(0xFFFFC107),
            route = "learning"
        ),
        CategoryData(
            text = "IQ",
            imageResId = R.drawable.iq,
            color = Color(0xFF2196F3),
            route = "iq"
        ),
        CategoryData(
            text = "Calm",
            imageResId = R.drawable.calm,
            color = Color(0xFF9C27B0),
            route = "calm"
        )
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Background image
        Image(
            painter = painterResource(id = R.drawable.homescreen),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Text(
            // FIX: Correctly load the string resource using the stringResource() function.
            text = stringResource(id = R.string.app_name),
            style = TextStyle(
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,

                // STYLE 1: Set the text color to a bright, fluorescent color.
                color = Color.Black, // The bright "core" of the neon light

                // STYLE 2: Create a "glow" effect using a shadow of a vibrant color.
                // This makes the text appear to emit light.
                shadow = Shadow(
                    color = Color(0xFF00BFFF), // A vibrant "Electric Blue" for the glow
                    offset = Offset.Zero,      // Center the glow perfectly behind the text
                    blurRadius = 20f           // A large blur radius creates the soft, fuzzy glow
                )
            ),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 20.dp) // Position it neatly between the top buttons
        )

        // Single Row layout for horizontal arrangement
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            categories.forEach { category ->
                CategoryItem(
                    text = category.text,
                    imageResId = category.imageResId,
                    color = category.color,
                    onClick = { navController.navigate(category.route) },
                    itemCount = categories.size
                )
            }
        }

        // Close button
        val context = LocalContext.current
        val buttonSize = mathMin(64f, screenWidth * 0.12f).dp
        val iconSize = mathMin(32f, screenWidth * 0.06f).dp

        Box(
            modifier = Modifier
                .padding(16.dp)
                .size(buttonSize)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFF9500),
                            Color(0xFFFF2D55),
                            Color(0xFF5856D6)
                        )
                    )
                )
                .clickable { context.findActivity()?.finish() }
                .align(Alignment.TopStart),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(iconSize)
            )
        }

        // Settings button
        Box(
            modifier = Modifier
                .padding(16.dp)
                .size(buttonSize)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFF9500),
                            Color(0xFFFF2D55),
                            Color(0xFF5856D6)
                        )
                    )
                )
                .clickable { navController.navigate("settings") }
                .align(Alignment.TopEnd),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

// Data class for category information
data class CategoryData(
    val text: String,
    val imageResId: Int,
    val color: Color,
    val route: String
)

// Utility function unchanged
fun Context.findActivity(): Activity? {
    var currentContext = this
    while (currentContext is android.content.ContextWrapper) {
        if (currentContext is Activity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return null
}

@Composable
fun CategoryItem(
    text: String,
    imageResId: Int,
    color: Color,
    onClick: () -> Unit,
    itemCount: Int = 4 // Default to 4 items
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp

    // Calculate available width per item (accounting for padding)
    val availableWidthPerItem = (screenWidth - (16 * (itemCount + 1))) / itemCount

    // Calculate responsive font size based on available width
    val fontSize = mathMin(18f, availableWidthPerItem * 0.15f).sp

    Column(
        modifier = Modifier
            .width((availableWidthPerItem).dp)
            .padding(4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.3f))
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Image takes maximum possible space
        Image(
            painter = painterResource(id = imageResId),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .aspectRatio(1f) // Keep image square
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Text with adaptive size and overflow handling
        Text(
            text = stringResource(id = getLabelRes(text)),
            fontWeight = FontWeight.Bold,
            fontSize = fontSize,
            color = Color.Black,
            textAlign = TextAlign.Center,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp)
        )
    }
}

// Helper function unchanged
private fun getLabelRes(text: String): Int {
    return when (text) {
        "Daily Activity" -> R.string.category_daily_activity
        "Learning" -> R.string.category_learning
        "IQ" -> R.string.category_iq
        "Calm" -> R.string.category_calm
        else -> R.string.test // Fallback
    }
}