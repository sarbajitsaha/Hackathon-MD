package com.coredumped.project.features.learning.time

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.coredumped.project.R
import com.coredumped.project.features.learning.time.data.TimeLearningData
import com.coredumped.project.features.learning.time.data.TimePeriod
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TimeDetectiveScreen(navController: NavController) {
    val configuration = LocalConfiguration.current
    val allItems = remember { TimeLearningData.items.shuffled() }
    var index by remember { mutableIntStateOf(0) }
    var showCorrect by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<TimePeriod?>(null) }
    val scope = rememberCoroutineScope()

    val current = allItems.getOrNull(index)

    Box(modifier = Modifier.fillMaxSize()) {
        // Background
        Image(
            painter = painterResource(id = R.drawable.homescreen),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        if (current == null) {
            // Finished
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Great job!",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            LaunchedEffect(Unit) {
                delay(1500)
                navController.popBackStack()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 80.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Clue area
                Card(
                    modifier = Modifier.fillMaxWidth().weight(0.4f, fill = false),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = current.activityImageRes),
                            contentDescription = current.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "When do we ${current.name}?",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2x2 options
                val options = listOf(TimePeriod.Morning, TimePeriod.Afternoon, TimePeriod.Evening, TimePeriod.Night)
                for (row in 0 until 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        for (col in 0 until 2) {
                            val period = options[row * 2 + col]
                            val isCorrect = period == current.period
                            val isSelected = selected == period
                            val scale by animateFloatAsState(
                                targetValue = if (showCorrect && isCorrect) 1.08f else 1f,
                                animationSpec = tween(300),
                                label = "scale"
                            )
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(140.dp)
                                    .scale(scale)
                                    .clickable {
                                        if (isCorrect) {
                                            showCorrect = true
                                            selected = period
                                            scope.launch {
                                                delay(800)
                                                showCorrect = false
                                                selected = null
                                                index++
                                            }
                                        } else {
                                            // gentle feedback: temporarily disable color emphasis
                                            selected = period
                                            scope.launch {
                                                delay(400)
                                                selected = null
                                            }
                                        }
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = when {
                                        isCorrect && showCorrect -> Color(0xFFC8E6C9) // greenish
                                        isSelected -> Color(0xFFE0E0E0)
                                        else -> Color.White
                                    }
                                ),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = current.timeIconRes),
                                        contentDescription = period.name,
                                        modifier = Modifier.size(56.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = when (period) {
                                            TimePeriod.Morning -> "Morning"
                                            TimePeriod.Afternoon -> "Afternoon"
                                            TimePeriod.Evening -> "Evening"
                                            TimePeriod.Night -> "Night"
                                        },
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                    if (row == 0) Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        // Back button
        val backButtonSize = (configuration.screenWidthDp * 0.12f).coerceAtMost(64f).dp
        Box(
            modifier = Modifier
                .padding(start = 16.dp, top = 16.dp)
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
                modifier = Modifier.size((configuration.screenWidthDp * 0.06f).coerceAtMost(32f).dp)
            )
        }
    }
}
