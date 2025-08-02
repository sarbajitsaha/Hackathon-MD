package com.coredumped.project.calm

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import androidx.navigation.NavController
import com.coredumped.project.R
import com.coredumped.project.component.VideoPlayerComponent

private const val TAG = "CalmVideoScreen"

@Composable
fun CalmVideoScreen(navController: NavController) {
    val context = LocalContext.current
    var currentVideo by remember { mutableStateOf<VideoData?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.homescreen),
            contentDescription = stringResource(R.string.calm_background_desc),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Grid layout for video items (visible only when no video is selected)
        if (currentVideo == null) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Nature Videos Header
                item(span = { GridItemSpan(4) }, key = "nature_video_header") {
                    CategoryHeader(text = stringResource(R.string.category_nature_videos))
                }

                // Nature Video Items with updated resource names
                val natureVideos = listOf(
                    VideoData(R.drawable.calm_waves, R.string.video_ocean, "video_ocean"),
                    VideoData(R.drawable.calm_waterfall, R.string.video_waterfall, "video_waterfall"),
                    VideoData(R.drawable.calm_forest, R.string.video_forest, "video_forest")
                )

                items(natureVideos, key = { it.key }) { videoData ->
                    VideoItem(
                        videoData = videoData,
                        onClick = { selectedVideo ->
                            currentVideo = selectedVideo
                        }
                    )
                }
            }
        }

        // Back button (always visible)
        Box(
            modifier = Modifier
                .padding(16.dp)
                .size(64.dp)
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
                contentDescription = stringResource(R.string.back_button),
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }

        // Full-screen video player overlay
        if (currentVideo != null) {
            val videoName = context.resources.getResourceEntryName(currentVideo!!.thumbnailResId)
            val rawVideoId = context.resources.getIdentifier(videoName, "raw", context.packageName)

            if (rawVideoId != 0) {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)) {
                    VideoPlayerComponent(
                        modifier = Modifier.fillMaxSize(),
                        videoResourceId = rawVideoId,
                        autoPlay = true,
                        onPlayerReady = { player ->
                            player.playWhenReady = true // Ensure playback starts
                            player.repeatMode = Player.REPEAT_MODE_ONE // Loop the video
                        },
                        onRelease = {
                            // The component handles its own release.
                        }
                    )
                    // Custom close button to exit the video player
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .clickable { currentVideo = null }, // Set state to null to close
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.close_button),
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            } else {
                // Handle error case where video file is not found
                LaunchedEffect(currentVideo) {
                    Log.e(TAG, "Raw video resource for '${videoName}' not found.")
                    currentVideo = null // Go back to the grid
                }
            }
        }
    }
}

data class VideoData(
    val thumbnailResId: Int,
    val textResId: Int,
    val key: String
)

@Composable
fun CategoryHeader(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(44.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Color.White.copy(alpha = 0.65f))
        )

        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp),
            color = Color.Black,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun VideoItem(videoData: VideoData, onClick: (VideoData) -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Card(
                modifier = Modifier
                    .size(110.dp)
                    .clickable { onClick(videoData) },
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.3f)),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Image(
                    painter = painterResource(id = videoData.thumbnailResId),
                    contentDescription = stringResource(id = videoData.textResId),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.7f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = stringResource(id = videoData.textResId),
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}