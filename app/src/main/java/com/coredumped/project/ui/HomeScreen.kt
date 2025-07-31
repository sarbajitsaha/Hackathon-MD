package com.coredumped.project.ui

import android.app.Activity
import android.content.Context
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
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.coredumped.project.R

@Composable
fun HomeScreen(navController: NavController) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.homescreen),
            contentDescription = null,
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
            CategoryItem(
                text = "Daily Activity",
                imageResId = R.drawable.brush,
                color = Color(0xFF4CAF50),
                onClick = { navController.navigate("daily_activity") }
            )
            CategoryItem(
                text = "Learning",
                imageResId = R.drawable.learning,
                color = Color(0xFFFFC107),
                onClick = { navController.navigate("learning") }
            )
            CategoryItem(
                text = "IQ",
                imageResId = R.drawable.iq,
                color = Color(0xFF2196F3),
                onClick = { navController.navigate("iq") }
            )
            CategoryItem(
                text = "Calm",
                imageResId = R.drawable.calm,
                color = Color(0xFF9C27B0),
                onClick = { navController.navigate("calm") }
            )
        }

        val context = LocalContext.current
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
                .clickable { context.findActivity()?.finish() }
                .align(Alignment.TopStart),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

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
        "Daily Activity" -> R.string.category_daily_activity
        "Learning" -> R.string.category_learning
        "IQ" -> R.string.category_iq
        "Calm" -> R.string.category_calm
        else -> R.string.test // Fallback
    }
}