package com.coredumped.project

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
// Add if needed: import androidx.compose.material.icons.Icons; import androidx.compose.material.icons.filled.Waves; etc. (use built-in icons)

@Composable
fun CalmScreen(navController: NavController) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalArrangement = Arrangement.Center
    ) {
        item {
            Button(
                onClick = { navController.navigate("fluid_simulation") },
                modifier = Modifier.padding(8.dp).fillMaxSize(),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
            ) {
                Text("Fluid", fontSize = 20.sp, textAlign = TextAlign.Center)  // Placeholder; replace with icon later
            }
        }
        item {
            Button(
                onClick = { navController.navigate("calming_audio") },
                modifier = Modifier.padding(8.dp).fillMaxSize(),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
            ) {
                Text("Audio", fontSize = 20.sp, textAlign = TextAlign.Center)
            }
        }
        item {
            Button(
                onClick = { navController.navigate("calming_video") },
                modifier = Modifier.padding(8.dp).fillMaxSize(),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
            ) {
                Text("Video", fontSize = 20.sp, textAlign = TextAlign.Center)
            }
        }
        item {
            Button(
                onClick = { navController.navigate("bubble_pop") },
                modifier = Modifier.padding(8.dp).fillMaxSize(),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan)
            ) {
                Text("Bubbles", fontSize = 20.sp, textAlign = TextAlign.Center)
            }
        }
    }
}