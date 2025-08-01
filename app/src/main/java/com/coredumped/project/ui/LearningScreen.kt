package com.coredumped.project.ui

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.coredumped.project.BackgroundMusic
import com.coredumped.project.R
import kotlin.math.min as mathMin

// Data class for category information
data class CategoryDataLearning(
    val text: String,
    val imageResId: Int,
    val color: Color, // This color is not used in CategoryItemLearning currently
    val route: String
)

const val ITEMS_PER_ROW = 4

@Composable
fun LearningScreen(navController: NavController) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    // val screenHeight = configuration.screenHeightDp.dp // Not directly used for item sizing anymore

    BackgroundMusic(R.raw.background_music) // Ensure this music file exists

    val allCategories = listOf(
        CategoryDataLearning("Fruits", R.drawable.mango, Color.Blue, "flashcards_fruits"),
        CategoryDataLearning("Animals", R.drawable.tiger, Color.Green, "flashcards_animals"),
        CategoryDataLearning("Vehicles", R.drawable.bus, Color.Red, "flashcards_vehicles"),
        CategoryDataLearning("Stationery", R.drawable.stationery, Color.Yellow, "flashcards_stationary"),
        CategoryDataLearning("Alphabets", R.drawable.alpha, Color.Cyan, "flashcards_alphabets"),
        CategoryDataLearning("Numbers", R.drawable.numbers, Color.Magenta, "flashcards_numbers"), // Example new // Example new
        CategoryDataLearning("Days", R.drawable.days, Color.LightGray, "flashcards_days"),   // Example new
        CategoryDataLearning("Shapes", R.drawable.basicshapes, Color.Black, "flashcards_shapes"),       // Example new
        // Add more categories as needed
    )

    // Chunk categories into rows of 'ITEMS_PER_ROW'
    val chunkedCategories = allCategories.chunked(ITEMS_PER_ROW)

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.homescreen),
            contentDescription = "Background Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 80.dp) // Space for the back button
                .verticalScroll(rememberScrollState()) // Allow vertical scrolling for multiple rows
        ) {
            chunkedCategories.forEach { rowCategories ->
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(rowCategories) { category ->
                        // Calculate item width to fit ITEMS_PER_ROW items
                        // Subtract padding between items and outer padding for LazyRow
                        val totalHorizontalPadding = (ITEMS_PER_ROW + 1) * 8f
                        val itemWidth = (screenWidth - totalHorizontalPadding.dp) / ITEMS_PER_ROW

                        CategoryItemLearning(
                            modifier = Modifier.width(itemWidth), // Apply calculated width
                            text = category.text,
                            imageResId = category.imageResId,
                            onClick = {
                                if (category.route.isNotEmpty()) {
                                    navController.navigate(category.route)
                                }
                            }
                        )
                    }
                }
            }
        }

        // Responsive back button
        val backButtonSize = mathMin(64f, configuration.screenWidthDp * 0.12f).dp
        Box(
            modifier = Modifier
                .padding(start = 16.dp, top = 16.dp) // Adjusted padding
                .size(backButtonSize)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFFF9500), Color(0xFFFF2D55), Color(0xFF5856D6))
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
                modifier = Modifier.size(mathMin(32f, configuration.screenWidthDp * 0.06f).dp)
            )
        }
    }
}

@Composable
fun CategoryItemLearning(
    modifier: Modifier = Modifier, // Accept a modifier
    text: String,
    imageResId: Int,
    onClick: () -> Unit
) {
    // val configuration = LocalConfiguration.current // Not needed here if width is passed
    // val screenWidth = configuration.screenWidthDp // Not needed here

    // Font size can be made adaptive or fixed
    val fontSize = 16.sp // Example: fixed font size, adjust as needed
    // Or, calculate based on the itemWidth passed in modifier if more dynamic sizing is needed

    Column(
        modifier = modifier // Use the passed modifier (which includes width)
            .fillMaxHeight(0.35f) // Limit height relative to screen or fixed Dp
            .padding(vertical = 4.dp) // Keep internal padding
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.8f)) // Slightly more opaque
            .clickable(onClick = onClick)
            .padding(8.dp), // Padding for content inside the card
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = imageResId),
            contentDescription = text, // Good practice for accessibility
            modifier = Modifier
                .fillMaxWidth(0.8f) // Image takes 80% of card width
                .aspectRatio(1f)    // Keep image square
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(id = getLabelRes(text)), // Make sure getLabelRes handles all texts
            fontWeight = FontWeight.Bold,
            fontSize = fontSize,
            color = Color.Black,
            textAlign = TextAlign.Center,
            maxLines = 2, // Allow up to 2 lines for text
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp)
        )
    }
}

// Helper to get string resource ID from category text key
// Ensure all category texts in CategoryDataLearning have a corresponding entry here
private fun getLabelRes(text: String): Int {
    return when (text) {
        "Fruits" -> R.string.flashcards_fruits
        "Animals" -> R.string.flashcards_animals
        "Vehicles" -> R.string.flashcards_vehicles
        "Stationery" -> R.string.flashcards_stationery
        "Alphabets" -> R.string.flashcards_alphabets
        "Numbers" -> R.string.flashcards_numbers // Add if you have this string
        "Shapes" -> R.string.flashcards_shapes   // Add if you have this string
        "Days" -> R.string.flashcards_days     // Add if you have this string
        // Add more mappings as needed
        else -> R.string.test // Fallback for undefined categories
    }
}
