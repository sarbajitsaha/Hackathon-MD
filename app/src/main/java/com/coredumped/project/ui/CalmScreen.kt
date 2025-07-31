package com.coredumped.project.ui

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.coredumped.project.R
import kotlin.math.min as mathMin

val TAG = "CalmScreen"

@Composable
fun CalmScreen(navController: NavController) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val screenHeight = configuration.screenHeightDp

    // Calculate responsive grid cells based on screen width

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.homescreen),
            contentDescription = "Background Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        val items = listOf(
            CategoryDataCalm(
                text = "Fluid",
                imageResId = R.drawable.fluid,
                color = Color.Blue,
                route = "fluid_simulation"
            ),
            CategoryDataCalm(
                text = "Audio",
                imageResId = R.drawable.calm_audio,
                color = Color.Green,
                route = "calm_audio"
            ),
            CategoryDataCalm(
                text = "Video",
                imageResId = R.drawable.calm_video,
                color = Color.Blue,
                route = "" // Empty route means disabled
            ),
            CategoryDataCalm(
                text = "Bubbles",
                imageResId = R.drawable.bubbles,
                color = Color.Cyan,
                route = "pop_bubble"
            )
        )

        // Single Row layout for horizontal arrangement
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { category ->
                CategoryItemCalm(
                    text = category.text,
                    imageResId = category.imageResId,
                    color = category.color,
                    onClick = { navController.navigate(category.route) },
                    itemCount = items.size
                )
            }
        }

        // Back button with responsive size
        val backButtonSize = mathMin(64f, screenWidth * 0.12f).dp

        Box(
            modifier = Modifier
                .padding(16.dp)
                .size(backButtonSize)
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
                .clickable { navController.popBackStack() }
                .align(Alignment.TopStart),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(mathMin(32f, screenWidth * 0.06f).dp)
            )
        }
    }
}

// Data class for category information
data class CategoryDataCalm(
    val text: String,
    val imageResId: Int,
    val color: Color,
    val route: String
)

@Composable
fun CategoryItemCalm(
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
            text = stringResource(id = getLabelResCalm(text)),
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

private fun getLabelResCalm(text: String): Int {
    Log.d(TAG, "$text getLabelCalm")
    return when (text) {
        "Fluid" -> R.string.calm_fluid
        "Audio" -> R.string.calm_audio
        "Video" -> R.string.calm_video
        "Bubbles" -> R.string.calm_bubbles
        else -> R.string.test // Fallback
    }
}