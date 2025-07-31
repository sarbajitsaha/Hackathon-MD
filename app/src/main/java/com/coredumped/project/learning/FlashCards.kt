package com.coredumped.project.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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

// Data class for category information
data class CategoryItemFlashcards(
    val text: String,
    val imageResId: Int,
    val color: Color,
    val route: String
)

@Composable
fun FlashcardsScreen(navController: NavController) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val screenHeight = configuration.screenHeightDp

    val categories = listOf(
        CategoryItemFlashcards(
            text = "Fruits",
            imageResId = R.drawable.mango,
            color = Color.Blue,
            route = "" // Empty route means disabled
        ),
        CategoryItemFlashcards(
            text = "Animals",
            imageResId = R.drawable.tiger,
            color = Color.Green,
            route = "" // Empty route means disabled
        ),
        CategoryItemFlashcards(
            text = "Vehicles",
            imageResId = R.drawable.bus,
            color = Color.Blue,
            route = "" // Empty route means disabled
        ),
        CategoryItemFlashcards(
            text = "Object",
            imageResId = R.drawable.book,
            color = Color.Cyan,
            route = "" // Empty route means disabled
        )
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.homescreen),
            contentDescription = "Background Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Single horizontal row with responsive spacing
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            categories.forEach { category ->
                CategoryItemLearning(
                    text = category.text,
                    imageResId = category.imageResId,
                    color = category.color,
                    onClick = {
                        if (category.route.isNotEmpty()) {
                            navController.navigate(category.route)
                        }
                    },
                    itemCount = categories.size
                )
            }
        }

        // Responsive back button
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

@Composable
fun CategoryItemFlashcards(
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
    val fontSize = mathMin(22f, availableWidthPerItem * 0.15f).sp

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

// Helper unchanged
private fun getLabelRes(text: String): Int {
    return when (text) {
        "Brush" -> R.string.activity_brush
        "HandWash" -> R.string.activity_handwash
        "Girl" -> R.string.activity_girl
        "Boy" -> R.string.activity_boy
        else -> R.string.test // Fallback
    }
}