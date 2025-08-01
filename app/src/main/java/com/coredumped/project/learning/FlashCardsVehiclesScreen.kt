package com.coredumped.project.learning

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.coredumped.project.R
import kotlin.math.min

// Reusing the same background colors, or define a new list if desired
val vehicleBackgroundColors = listOf(
    Color(0xFFE3F2FD), // Light Blue
    Color(0xFFFCE4EC), // Light Pink
    Color(0xFFFFF3E0), // Light Orange
    Color(0xFFE8F5E9), // Light Green
    Color(0xFFF9FBE7), // Light Yellow
    Color(0xFFEDE7F6)  // Light Purple
)

data class VehicleFlashCardItem(
    val name: String,
    @DrawableRes val imageResId: Int
)

// TODO: Replace with your actual vehicle drawables
val vehicleFlashCards = listOf(
    VehicleFlashCardItem("car", R.drawable.car),
    VehicleFlashCardItem("bus", R.drawable.bus),
    VehicleFlashCardItem("bicycle", R.drawable.cycle),
    VehicleFlashCardItem("bike", R.drawable.bike),
    VehicleFlashCardItem("train", R.drawable.train),
    VehicleFlashCardItem("auto", R.drawable.auto)
)

@Composable
fun FlashCardsVehiclesScreen(navController: NavController) {
    var currentCardIndex by remember { mutableIntStateOf(0) }
    val currentCard = vehicleFlashCards[currentCardIndex]
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp

    val dynamicBackgroundColor by animateColorAsState(
        targetValue = vehicleBackgroundColors[currentCardIndex % vehicleBackgroundColors.size],
        animationSpec = tween(durationMillis = 500), label = "vehicle_background_color_animation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(dynamicBackgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = 16.dp,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Flashcard Content
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = currentCard.imageResId),
                    contentDescription = stringResource(id = getVehicleLabelRes(currentCard.name)),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .fillMaxHeight(0.8f),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(8.dp)) // Adjust spacer if needed

                Text(
                    text = stringResource(id = getVehicleLabelRes(currentCard.name)),
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.3f),
                            offset = androidx.compose.ui.geometry.Offset(2f, 2f),
                            blurRadius = 4f
                        )
                    )
                )
            }

            // Next and Previous Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val buttonSize = 72.dp
                val iconSize = 48.dp

                Box(
                    modifier = Modifier
                        .size(buttonSize)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable(
                            enabled = vehicleFlashCards.isNotEmpty(),
                            onClick = {
                                if (currentCardIndex > 0) {
                                    currentCardIndex--
                                } else {
                                    currentCardIndex = vehicleFlashCards.size - 1
                                }
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "Previous Card",
                        modifier = Modifier.size(iconSize),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .size(buttonSize)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable(
                            enabled = vehicleFlashCards.isNotEmpty(),
                            onClick = {
                                if (currentCardIndex < vehicleFlashCards.size - 1) {
                                    currentCardIndex++
                                } else {
                                    currentCardIndex = 0
                                }
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.ArrowForward,
                        contentDescription = "Next Card",
                        modifier = Modifier.size(iconSize),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        // Styled Back Button
        val backButtonSize = min(64f, screenWidth * 0.12f).dp
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
                modifier = Modifier.size(min(32f, screenWidth * 0.06f).dp)
            )
        }
    }
}

// TODO: Create these string resources in your strings.xml
private fun getVehicleLabelRes(text: String): Int {
    return when (text.lowercase()) {
        "car" -> R.string.flashcards_car
        "bus" -> R.string.flashcards_bus
        "bicycle" -> R.string.flashcards_bicycle
        "bike" -> R.string.flashcards_bike
        "train" -> R.string.flashcards_train
        "auto" -> R.string.flashcards_auto
        else -> R.string.test
    }
}

@Preview(showBackground = true)
@Composable
fun FlashCardsVehiclesScreenPreview() {
    MaterialTheme {
        FlashCardsVehiclesScreen(navController = rememberNavController())
    }
}
