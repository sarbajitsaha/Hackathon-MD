package com.coredumped.project

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height // Import for explicit height
import androidx.compose.foundation.layout.width  // Import for explicit width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun HomeScreen(navController: NavController) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2), // 2 columns
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 32.dp), // Increased vertical padding for the grid
        contentPadding = PaddingValues(all = 16.dp), // Increased padding around items
        verticalArrangement = Arrangement.spacedBy(16.dp), // Add space between rows
        horizontalArrangement = Arrangement.spacedBy(16.dp) // Add space between columns
    ) {
        item {
            LargeButton(
                text = "Daily Activity",
                onClick = { navController.navigate("daily_activity") },
                color = Color(0xFF4CAF50)
            )
        }
        item {
            LargeButton(
                text = "IQ",
                onClick = { navController.navigate("iq") },
                color = Color(0xFF2196F3)
            )
        }
        item {
            LargeButton(
                text = "Learning",
                onClick = { navController.navigate("learning") },
                color = Color(0xFFFFC107)
            )
        }
        item {
            LargeButton(
                text = "Calm",
                onClick = { navController.navigate("calm") },
                color = Color(0xFF9C27B0)
            )
        }
    }
}

@Composable
fun LargeButton(
    text: String,
    onClick: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier // Allow passing additional modifiers if needed
) {
    Button(
        onClick = onClick,
        modifier = modifier // Use the passed modifier
            .fillMaxSize() // Fill the available space given by the grid cell
        // .height(150.dp) // You could set a minimum or fixed height if fillMaxSize isn't enough
        // .widthIn(min = 120.dp) // Ensure a minimum width
        ,
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Text(
            text = text,
            fontSize = 22.sp, // Slightly reduced to fit potentially more text if needed
            textAlign = TextAlign.Center,
            lineHeight = 28.sp // Adjust line height for better readability if text wraps
        )
    }
}
