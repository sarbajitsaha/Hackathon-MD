package com.coredumped.project.features.learning.time

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material3.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.coredumped.project.R
import com.coredumped.project.features.learning.time.data.DailyRoutineItem
import com.coredumped.project.features.learning.time.data.TimeLearningData
import com.coredumped.project.features.learning.time.data.TimePeriod
import com.coredumped.project.features.learning.time.components.LearningBackButton

@Composable
fun MyDayScreen(navController: NavController) {
    val configuration = LocalConfiguration.current
    val items = remember { TimeLearningData.items }
    var index by remember { mutableIntStateOf(0) }
    var selectedPeriod by remember { mutableStateOf(items.firstOrNull()?.period ?: TimePeriod.Morning) }
    val current = items.getOrNull(index)

    Box(modifier = Modifier.fillMaxSize()) {
        // Background
        Image(
            painter = painterResource(id = R.drawable.homescreen),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Main content
        val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = if (isLandscape) 16.dp else 80.dp,
                    start = if (isLandscape) 80.dp else 20.dp,
                    end = 20.dp,
                    bottom = 20.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main card with animation
            AnimatedContent(
                targetState = index,
                transitionSpec = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ) togetherWith slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                },
                label = "card_transition",
                modifier = Modifier.weight(1f)
            ) { currentIndex ->
                items.getOrNull(currentIndex)?.let { item ->
                    MyDayCard(
                        item = item,
                        selectedPeriod = selectedPeriod,
                        onPeriodSelected = { selectedPeriod = it },
                        onPlayAudio = {
                            // Placeholder: audio playback hook
                        },
                        onNext = {
                            index = (index + 1) % items.size
                            selectedPeriod = items[(index + 1) % items.size].period
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Back button
        LearningBackButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.align(Alignment.TopStart)
        )
    }
}

@Composable
private fun MyDayCard(
    item: DailyRoutineItem,
    selectedPeriod: TimePeriod,
    onPeriodSelected: (TimePeriod) -> Unit,
    onPlayAudio: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.7f))
    ) {
        val configuration = LocalConfiguration.current
        val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

        if (isLandscape) {
            // Landscape Layout: Row with two columns
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Column: Image, Name, Description
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Activity Icon
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .shadow(4.dp, RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = item.activityImageRes),
                            contentDescription = item.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = item.name,
                        fontSize = 28.sp, // Slightly smaller
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF37474F),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    item.voiceoverText?.let { text ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF5F5F5))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = text,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF546E7A),
                                textAlign = TextAlign.Center,
                                lineHeight = 24.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Right Column: Selection and Buttons
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "When do you do this?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF37474F),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Compact Grid for Periods
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TimePeriodChip(
                                period = TimePeriod.Morning,
                                icon = R.drawable.morning,
                                isSelected = selectedPeriod == TimePeriod.Morning,
                                onClick = { onPeriodSelected(TimePeriod.Morning) },
                                modifier = Modifier.weight(1f)
                            )
                            TimePeriodChip(
                                period = TimePeriod.Afternoon,
                                icon = R.drawable.afternoon,
                                isSelected = selectedPeriod == TimePeriod.Afternoon,
                                onClick = { onPeriodSelected(TimePeriod.Afternoon) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TimePeriodChip(
                                period = TimePeriod.Evening,
                                icon = R.drawable.evening,
                                isSelected = selectedPeriod == TimePeriod.Evening,
                                onClick = { onPeriodSelected(TimePeriod.Evening) },
                                modifier = Modifier.weight(1f)
                            )
                            TimePeriodChip(
                                period = TimePeriod.Night,
                                icon = R.drawable.night,
                                isSelected = selectedPeriod == TimePeriod.Night,
                                onClick = { onPeriodSelected(TimePeriod.Night) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onPlayAudio,
                            modifier = Modifier.weight(1f).height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, "Play", tint = Color.White, modifier = Modifier.size(24.dp))
                        }

                        Button(
                            onClick = onNext,
                            modifier = Modifier.weight(1f).height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                             Text("Next", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                             Spacer(modifier = Modifier.width(4.dp))
                             Icon(Icons.Default.NavigateNext, "Next", tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        } else {
            // Portrait Layout (Original)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Activity Icon
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .shadow(4.dp, RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.5f))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = item.activityImageRes),
                        contentDescription = item.name,
                        modifier = Modifier.fillMaxSize().padding(8.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                // Activity Name
                Text(
                    text = item.name,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF37474F),
                    textAlign = TextAlign.Center
                )

                // Description
                item.voiceoverText?.let { text ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF5F5F5))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = text,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF546E7A),
                            textAlign = TextAlign.Center,
                            lineHeight = 26.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Time Periods Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "When do you do this?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF37474F),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    // Time Period Options
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TimePeriodChip(
                            period = TimePeriod.Morning,
                            icon = R.drawable.morning,
                            isSelected = selectedPeriod == TimePeriod.Morning,
                            onClick = { onPeriodSelected(TimePeriod.Morning) },
                            modifier = Modifier.weight(1f)
                        )
                        TimePeriodChip(
                            period = TimePeriod.Afternoon,
                            icon = R.drawable.afternoon,
                            isSelected = selectedPeriod == TimePeriod.Afternoon,
                            onClick = { onPeriodSelected(TimePeriod.Afternoon) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TimePeriodChip(
                            period = TimePeriod.Evening,
                            icon = R.drawable.evening,
                            isSelected = selectedPeriod == TimePeriod.Evening,
                            onClick = { onPeriodSelected(TimePeriod.Evening) },
                            modifier = Modifier.weight(1f)
                        )
                        TimePeriodChip(
                            period = TimePeriod.Night,
                            icon = R.drawable.night,
                            isSelected = selectedPeriod == TimePeriod.Night,
                            onClick = { onPeriodSelected(TimePeriod.Night) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Removed Spacer(modifier = Modifier.weight(1f)) to allow scrolling
                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onPlayAudio,
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E88E5)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 8.dp
                        )
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Play Audio",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Button(
                        onClick = onNext,
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFB300)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 8.dp
                        )
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Next",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.NavigateNext,
                                contentDescription = "Next",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimePeriodChip(
    period: TimePeriod,
    @DrawableRes icon: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (label, bgColor, borderColor) = when (period) {
        TimePeriod.Morning -> Triple(
            "Morning",
            if (isSelected) Color(0xFFFFE082) else Color(0xFFFFF9E6),
            if (isSelected) Color(0xFFFFB300) else Color(0xFFE0E0E0)
        )
        TimePeriod.Afternoon -> Triple(
            "Afternoon",
            if (isSelected) Color(0xFFFFCC80) else Color(0xFFFFF5E8),
            if (isSelected) Color(0xFFFF6F00) else Color(0xFFE0E0E0)
        )
        TimePeriod.Evening -> Triple(
            "Evening",
            if (isSelected) Color(0xFF90CAF9) else Color(0xFFE8F4FF),
            if (isSelected) Color(0xFF1E88E5) else Color(0xFFE0E0E0)
        )
        TimePeriod.Night -> Triple(
            "Night",
            if (isSelected) Color(0xFF9FA8DA) else Color(0xFFEDE7F6),
            if (isSelected) Color(0xFF5C6BC0) else Color(0xFFE0E0E0)
        )
    }

    Card(
        modifier = modifier
            .height(90.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(
            width = if (isSelected) 3.dp else 1.dp,
            color = borderColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color(0xFF37474F) else Color(0xFF78909C),
                textAlign = TextAlign.Center
            )
        }
    }
}
