package com.coredumped.project.features.learning.time

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.coredumped.project.features.learning.data.CategoryDataLearning
import com.coredumped.project.features.learning.time.components.LearningBackButton
import kotlin.math.min

@Composable
fun TimeLearningMenuScreen(navController: NavController) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    val timeCategories = listOf(
        CategoryDataLearning("Visual Timer", R.drawable.visual_timer, Color(0xFFE53935), "visual_timer"),
        CategoryDataLearning("My Day", R.drawable.myday, Color(0xFF1E88E5), "my_day"),
        CategoryDataLearning("Time Detective", R.drawable.time_detective, Color(0xFFFFB300), "time_detective"),
        CategoryDataLearning("Analog Clock", R.drawable.analog_clock, Color(0xFF43A047), "analog_clock")
    )

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
                .padding(top = 80.dp, start = 16.dp, end = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Single Row layout with tighter sizing similar to Home
            androidx.compose.foundation.layout.Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                timeCategories.forEach { category ->
                    TimeItem(
                        text = category.text,
                        imageResId = category.imageResId,
                        onClick = {
                            if (category.route.isNotEmpty()) {
                                navController.navigate(category.route)
                            }
                        },
                        itemCount = timeCategories.size
                    )
                }
            }
        }

        LearningBackButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.align(Alignment.TopStart)
        )
    }
}

@Composable
fun TimeItem(
    text: String,
    imageResId: Int,
    onClick: () -> Unit,
    itemCount: Int = 4
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    // Make cards smaller and consistent with Home style
    val targetItemWidthDp = (screenWidth * 0.22f).dp
    val fontSize = 14.sp

    Column(
        modifier = Modifier
            .width(targetItemWidthDp)
            .padding(6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.25f))
            .clickable(onClick = onClick)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Image takes maximum possible space
        Image(
            painter = painterResource(id = imageResId),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Text with adaptive size and overflow handling
        Text(
            text = stringResource(id = getTimeLabelRes(text)),
            fontWeight = FontWeight.Bold,
            fontSize = fontSize,
            color = Color.Black,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        )
    }
}

private fun getTimeLabelRes(text: String): Int {
    return when (text) {
        "Visual Timer" -> R.string.time_visual_timer
        "My Day" -> R.string.time_my_day
        "Time Detective" -> R.string.time_detective
        "Analog Clock" -> R.string.time_analog_clock
        else -> R.string.test
    }
}

