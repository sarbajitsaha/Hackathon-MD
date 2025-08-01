package com.coredumped.project.learning

// import android.content.Context // No longer needed
// import android.media.MediaPlayer // No longer needed
// import android.util.Log // Keep if other logs exist, remove if only for MediaPlayer
import androidx.annotation.DrawableRes
// import androidx.annotation.RawRes // No longer needed as audioResId is removed from data class
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
// import androidx.compose.runtime.DisposableEffect // No longer needed for MediaPlayer
// import androidx.compose.runtime.LaunchedEffect // No longer needed for MediaPlayer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
// import androidx.compose.runtime.mutableStateOf // No longer needed for MediaPlayer
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
// import androidx.compose.ui.platform.LocalContext // No longer needed for MediaPlayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.coredumped.project.R
// import kotlinx.coroutines.delay // No longer needed for MediaPlayer
import kotlin.math.min

// private const val TAG = "FlashCardsFoods" // Can be removed if no other logging

// Define a list of background colors for the flashcards
val flashcardBackgroundColors = listOf(
    Color(0xFFFFF3E0), // Light Orange
    Color(0xFFE3F2FD), // Light Blue
    Color(0xFFE8F5E9), // Light Green
    Color(0xFFFCE4EC), // Light Pink
    Color(0xFFF9FBE7), // Light Yellow
    Color(0xFFEDE7F6)  // Light Purple
)

// Data class for a single flashcard item (audioResId removed)
data class FoodFlashCardItem(
    val name: String,
    @DrawableRes val imageResId: Int
    // @RawRes val audioResId: Int, // Removed
)

// Sample data - audioResId removed from items
val foodFlashCards = listOf(
    FoodFlashCardItem("apple", R.drawable.apple),
    FoodFlashCardItem("banana", R.drawable.banana),
    FoodFlashCardItem("mango", R.drawable.mango),
    FoodFlashCardItem("ice-cream", R.drawable.icecream),
//    FoodFlashCardItem("grapes", R.drawable.grapes),
//    FoodFlashCardItem("strawberry", R.drawable.strawberry)
)

@Composable
fun FlashCardsFoodsScreen(navController: NavController) {
    var currentCardIndex by remember { mutableIntStateOf(0) }
    val currentCard = foodFlashCards[currentCardIndex]
    // val context = LocalContext.current // No longer needed for MediaPlayer
    // var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) } // No longer needed

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp

    val dynamicBackgroundColor by animateColorAsState(
        targetValue = flashcardBackgroundColors[currentCardIndex % flashcardBackgroundColors.size],
        animationSpec = tween(durationMillis = 500), label = "background_color_animation"
    )

    // Removed playSound function
    // Removed LaunchedEffect for playing sound
    // Removed DisposableEffect for releasing MediaPlayer

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(dynamicBackgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = 80.dp,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = currentCard.imageResId),
                    contentDescription = currentCard.name,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .fillMaxHeight(0.7f),
                    contentScale = ContentScale.Fit
                )

                Text(
                    text = stringResource(id = getLabelRes(currentCard.name)), // Assuming you want to keep string resources for names
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
                            enabled = foodFlashCards.isNotEmpty(),
                            onClick = {
                                if (currentCardIndex > 0) {
                                    currentCardIndex--
                                } else {
                                    currentCardIndex = foodFlashCards.size - 1
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
                            enabled = foodFlashCards.isNotEmpty(),
                            onClick = {
                                if (currentCardIndex < foodFlashCards.size - 1) {
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

@Preview(showBackground = true)
@Composable
fun FlashCardsFoodsScreenPreview() {
    MaterialTheme {
        FlashCardsFoodsScreen(navController = rememberNavController())
    }
}

// Keep this function if you are using it to get string resources for food names
private fun getLabelRes(text: String): Int {
    return when (text) {
        "apple" -> R.string.flashcards_apple
        "banana" -> R.string.flashcards_banana
        "mango" -> R.string.flashcards_mango
        "ice-cream" -> R.string.flashcards_ice_cream
//        "grapes" -> R.string.flashcards_grapes // Assuming you have this
//        "strawberry" -> R.string.flashcards_strawberry // Assuming you have this
        else -> R.string.test // Fallback
    }
}
