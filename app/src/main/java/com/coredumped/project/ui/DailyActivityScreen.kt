package com.coredumped.project.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.coredumped.project.R

@Composable
fun DailyActivityScreen(navController: NavController) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.homescreen),
            contentDescription = "Background Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Single horizontal row for 1x4 layout (4 icons horizontally)
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly, // Even spacing to reduce wasted space
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryItemDA(
                text = "Brush",
                imageResId = R.drawable.brush,
                color = Color.Blue,
                onClick = { navController.navigate("brush") }
            )
            CategoryItemDA(
                text = "HandWash",
                imageResId = R.drawable.handwash,
                color = Color.Green,
                onClick = { navController.navigate("handwash") }
            )
            CategoryItemDA(
                text = "Girl",
                imageResId = R.drawable.girl,
                color = Color.Blue,
                onClick = { /* Disabled until route is implemented */ }
            )
            CategoryItemDA(
                text = "Boy",
                imageResId = R.drawable.boy,
                color = Color.Cyan,
                onClick = { /* Disabled until route is implemented */ }
            )
        }

        // Colorful back button in the top left corner
        Box(
            modifier = Modifier
                .padding(16.dp)
                .size(64.dp)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFF9500),  // Orange
                            Color(0xFFFF2D55),  // Pink
                            Color(0xFF5856D6)   // Purple
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
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun CategoryItemDA(
    text: String, // Used to map to string res for label
    imageResId: Int,
    color: Color, // Unused in display; consider removing if not needed
    onClick: () -> Unit
) {
    // Use Column to stack Image and Text vertically
    Column(
        modifier = Modifier
            .size(190.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.3f)) // Translucent background
            .clickable(onClick = onClick)
            .padding(8.dp), // Inner padding for spacing
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = imageResId),
            contentDescription = null,
            modifier = Modifier
                .size(140.dp) // Adjust size to fit text below
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(10.dp)) // Space between image and text
        Text(
            text = stringResource(id = getLabelRes(text)), // Bilingual label
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color.Black // Visible on translucent bg
            )
        )
    }
}

// Helper to map text to string resource IDs for labels (add to strings.xml for English/Bengali)
private fun getLabelRes(text: String): Int {
    return when (text) {
        "Brush" -> R.string.activity_brush
        "HandWash" -> R.string.activity_handwash
        "Girl" -> R.string.activity_girl
        "Boy" -> R.string.activity_boy
        else -> R.string.test // Fallback
    }
}