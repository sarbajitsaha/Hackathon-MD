package com.coredumped.project.iq

import android.util.Log
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
// import androidx.compose.ui.res.stringResource // Will be removed as per request
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.coredumped.project.R
import kotlin.math.min
import kotlin.random.Random
import android.content.Context
import android.media.MediaPlayer
import androidx.annotation.RawRes
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource

// Define Light and Darker Green/Red colors
private val LightGreen = Color(0xFFA5D6A7) // A lighter green
private val DarkGreen = Color(0xFF388E3C)  // A darker, more saturated green
private val LightRed = Color(0xFFEF9A9A)   // A lighter red
private val DarkRed = Color(0xFFD32F2F)    // A darker, more saturated red
private val LightGray = Color(0xFFE0E0E0)   // A light gray for error highlighting

private const val maxProblems = 10;

class Maths1Game {

    private var score = 0
    private var currentProblemInternal: Problem? = null
    val currentProblem: Problem?
        get() = currentProblemInternal

    data class Problem(
        val equation: String,
        val isEquationCorrect: Boolean,
        val correctAnswerString: String
    )

    fun generateProblem(): Problem {
        val num1 = Random.nextInt(1, 21)
        val num2 = Random.nextInt(1, 11)
        val operationType = Random.nextInt(0, 2)

        val actualAnswer: Int
        val operationSymbol: String

        when (operationType) {
            0 -> {
                actualAnswer = num1 + num2
                operationSymbol = "+"
            }

            1 -> {
                actualAnswer = num1 - num2
                operationSymbol = "-"
            }

            else -> { // Default case, can be addition or another logic
                actualAnswer = num1 + num2
                operationSymbol = "+"
            }
        }

        val displayEquationCorrectly = Random.nextBoolean()
        val displayedResult: Int
        val finalEquationString: String
        val isProblemActuallyCorrect: Boolean
        val expectedUserResponse: String

        if (displayEquationCorrectly) {
            displayedResult = actualAnswer
            isProblemActuallyCorrect = true
            expectedUserResponse = "Correct"
        } else {
            var wrongAnswerOffset = Random.nextInt(-5, 6)
            while (wrongAnswerOffset == 0) { // Ensure offset is not zero
                wrongAnswerOffset = Random.nextInt(-5, 6)
            }
            displayedResult = actualAnswer + wrongAnswerOffset
            isProblemActuallyCorrect = false
            expectedUserResponse = "Incorrect"
        }

        finalEquationString = "$num1 $operationSymbol $num2 = $displayedResult"
        currentProblemInternal =
            Problem(finalEquationString, isProblemActuallyCorrect, expectedUserResponse)
        return currentProblemInternal!!
    }

    fun checkAnswer(userResponse: String): Boolean {
        val isCorrect = userResponse == currentProblemInternal?.correctAnswerString
        if (isCorrect) {
            score++
        }
        return isCorrect
    }

    fun getScore(): Int = score

    fun resetGame() {
        score = 0
        currentProblemInternal = null
    }
}

private const val TAG = "SimpleMathsScreen"

private object SoundPlayer {

    private var mediaPlayer: MediaPlayer? = null

    fun playSound(context: Context, @RawRes soundResourceId: Int) {
        mediaPlayer?.release()
        mediaPlayer = null

        mediaPlayer = MediaPlayer.create(context, soundResourceId)
        mediaPlayer?.setOnCompletionListener {
            it.release()
            mediaPlayer = null
        }
        mediaPlayer?.start()
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}


@Composable
fun Maths1Screen(navController: NavController) {
    val game = remember { Maths1Game() }

    var currentProblemState by remember { mutableStateOf<Maths1Game.Problem?>(null) }
    var score by remember { mutableIntStateOf(game.getScore()) }
    var isAnswerSubmitted by remember { mutableStateOf(false) }
    var selectedUserResponse by remember { mutableStateOf<String?>(null) }

    var totalProblemsAttempted by remember { mutableIntStateOf(0) }
    var showResultsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        currentProblemState = game.generateProblem()
        Log.d(
            TAG,
            "Yes/No Maths Screen initialized. First problem: ${currentProblemState?.equation}, Expected: ${currentProblemState?.correctAnswerString}"
        )
    }

    fun handleNextProblem() {
        // Reset for next problem only if not exceeding max problems
        if (totalProblemsAttempted < maxProblems -1) { // -1 because totalProblemsAttempted is 0-indexed for attempts made
            currentProblemState = game.generateProblem()
            isAnswerSubmitted = false
            selectedUserResponse = null
            totalProblemsAttempted++
            Log.d(
                TAG,
                "Next problem requested: ${currentProblemState?.equation}, Expected: ${currentProblemState?.correctAnswerString}. Attempted: $totalProblemsAttempted"
            )
        } else {
            // This is after the last problem has been attempted and next is clicked (or should be handled by submit)
            if (!showResultsDialog) {
                totalProblemsAttempted++ // To reflect maxProblems were attempted
                showResultsDialog = true
                Log.d(TAG, "Max problems reached. Total attempted: $totalProblemsAttempted. Showing results.")
            }
        }
    }

    fun handleSubmitResponse(userResponse: String) {
        if (isAnswerSubmitted) return

        selectedUserResponse = userResponse
        val isCorrect = game.checkAnswer(userResponse)
        score = game.getScore()
        isAnswerSubmitted = true // Mark as submitted
        Log.d(
            TAG,
            "User response '$userResponse' submitted. Correct: $isCorrect. Score: $score. Total Attempted before this: $totalProblemsAttempted"
        )

        // Check if this submission completes the game
        if (totalProblemsAttempted == maxProblems - 1 && isAnswerSubmitted) { // If it was the last problem
            if (!showResultsDialog) {
                // We don't increment totalProblemsAttempted here as handleNextProblem or initial load logic manages it
                // We just show the dialog because the game is over
                showResultsDialog = true
                Log.d(TAG, "Last problem submitted. Total attempted will be $maxProblems. Showing results.")
            }
        }
    }


    if (showResultsDialog) {
        ResultsDialog(
            score = score,
            totalAttempted = maxProblems, // Show total as maxProblems for consistency
            onDismiss = {
                showResultsDialog = false
                game.resetGame() // Reset game logic
                navController.popBackStack()
            }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        // Main content column
        Column(
            modifier = Modifier
                .weight(1f) // Give most space to this column
                .fillMaxHeight()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top info row (Back Button, Question Count, Score)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Group Question Count and Score to manage spacing with SpaceBetween
                val currentQuestionDisplayNumber = min(totalProblemsAttempted + 1, maxProblems)
                Box(
                    modifier = Modifier
                        .padding(24.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable { navController.popBackStack() }, // Set state to null to close
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.close_button),
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "Question $currentQuestionDisplayNumber / $maxProblems",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Text(text = "Score: $score", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (currentProblemState == null && !showResultsDialog) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                    Text("Loading equation...", modifier = Modifier.padding(top = 8.dp))
                }
            } else if (currentProblemState != null) {
                val problem = currentProblemState!!
                // Equation Text
                Text(
                    text = problem.equation,
                    style = MaterialTheme.typography.displayMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                        .weight(1f) // Ensure it takes available vertical space
                )

                // Choice Buttons Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val context = LocalContext.current // Get context for sound player

                    ChoiceIconButton(
                        onClick = {
                            // Determine sound based on if the equation presented IS correct,
                            // not if the user's choice "Correct" is the right answer to the problem
                            val soundToPlay = if (problem.isEquationCorrect) R.raw.correct else R.raw.wrong
                            handleSubmitResponse("Correct")
                            SoundPlayer.playSound(context, soundToPlay)
                        },
                        icon = Icons.Filled.Check,
                        contentDescription = "Correct",
                        isSelected = selectedUserResponse == "Correct",
                        isCorrectChoice = problem.correctAnswerString == "Correct", // The actual correct response for this problem
                        isAnswerSubmitted = isAnswerSubmitted,
                        baseColorLight = LightGreen,
                        baseColorDark = DarkGreen,
                        errorColorDark = LightGray // This is the color if this button was chosen and it was WRONG
                    )

                    Spacer(modifier = Modifier.width(24.dp))

                    ChoiceIconButton(
                        onClick = {
                            // Determine sound based on if the equation presented IS INCORRECT,
                            // not if the user's choice "Incorrect" is the right answer to the problem
                            val soundToPlay = if (!problem.isEquationCorrect) R.raw.correct else R.raw.wrong
                            handleSubmitResponse("Incorrect")
                            SoundPlayer.playSound(context, soundToPlay)
                        },
                        icon = Icons.Filled.Close,
                        contentDescription = "Incorrect",
                        isSelected = selectedUserResponse == "Incorrect",
                        isCorrectChoice = problem.correctAnswerString == "Incorrect", // The actual correct response for this problem
                        isAnswerSubmitted = isAnswerSubmitted,
                        baseColorLight = LightRed,
                        baseColorDark = DarkRed,
                        errorColorDark = LightGray // This is the color if this button was chosen and it was WRONG
                    )
                }
                Spacer(modifier = Modifier.weight(0.5f)) // Pushes content up a bit
            } else {
                // Fallback for when currentProblemState is null but not loading (e.g., after results dialog if not navigating away)
                Spacer(modifier = Modifier.weight(1f)) // Keep the layout structure
            }
        }

        // Side column - NOW ONLY FOR "Next Question" button
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 8.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // "Next Question" button
            // Show if an answer has been submitted, AND it's not the last problem (or about to be the last problem shown)
            // AND the results dialog isn't already up
            if (isAnswerSubmitted && totalProblemsAttempted < maxProblems -1  && !showResultsDialog) {
                IconButton(
                    onClick = { handleNextProblem() },
                    modifier = Modifier
                        .size(64.dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(colors = listOf(Color(0xFF4CAF50), Color(0xFF8BC34A)))
                        )
                ) {
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = "Next Question",
                        modifier = Modifier.size(32.dp),
                        tint = Color.White
                    )
                }
            } else {
                // Placeholder to keep layout consistent if Next button is not shown
                Spacer(modifier = Modifier.size(64.dp))
            }
            // The back button that was here is now removed.
        }
    }
}

@Composable
fun ChoiceIconButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    isSelected: Boolean, // Is this specific button currently selected by the user?
    isCorrectChoice: Boolean, // Is choosing this button the correct action for the current problem?
    isAnswerSubmitted: Boolean, // Has any answer been submitted for the current problem?
    baseColorLight: Color, // Default color when not submitted
    baseColorDark: Color, // Color if this was selected AND it was correct
    errorColorDark: Color, // Color if this was selected AND it was incorrect (or for unselected correct answer sometimes)
    modifier: Modifier = Modifier
) {
    // Determine the background color based on submission state and correctness
    val containerColor = when {
        isAnswerSubmitted && isCorrectChoice -> baseColorDark // Selected and Correct: Dark (e.g., Dark Green)
        isAnswerSubmitted && !isCorrectChoice -> errorColorDark  // Selected and Incorrect: Error (e.g., Dark Red or Gray)
        else -> baseColorLight // Default state or before submission
    }

    val iconColor = Color.White // Keep icon color consistent for visibility on varied backgrounds
    val iconSize = 48.dp

    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(iconSize * 1.5f) // Button size
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .background(containerColor), // Apply the determined background color
        enabled = !isAnswerSubmitted, // Disable button after an answer is submitted
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = Color.Transparent, // IconButton's own container is transparent; background is handled by Modifier
            contentColor = iconColor,
            disabledContainerColor = Color.Transparent, // Respect the Modifier.background
            disabledContentColor = iconColor.copy(alpha = if (isAnswerSubmitted && isSelected) 1f else 0.7f) // Icon slightly dimmer if disabled but not selected
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(iconSize)
            // Tint is handled by IconButtonDefaults contentColor
        )
    }
}

@Composable
fun ResultsDialog(
    score: Int,
    totalAttempted: Int,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Game Over!",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Your Score:",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$score / $totalAttempted",
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("OK") }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
