package com.coredumped.project.iq

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.annotation.RawRes
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
// import androidx.compose.foundation.layout.width // Not directly used in the final version of this file
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
// import androidx.compose.material3.IconButtonDefaults // Not directly used by active components
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
// import androidx.compose.ui.res.stringResource // Not used as "Is it correct?" was removed
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
// import androidx.compose.ui.unit.sp // Used by Button text, ensure it's imported if needed elsewhere
import androidx.navigation.NavController
import com.coredumped.project.R // Ensure this R file import is correct
import kotlin.math.min
import kotlin.math.max // Explicitly import max for clarity if not auto-imported
import kotlin.random.Random

// Color definitions
private val LightGreen = Color(0xFFA5D6A7)
private val DarkGreen = Color(0xFF388E3C)
private val DarkRed = Color(0xFFD32F2F)
// private val LightRed = Color(0xFFEF9A9A) // Not used in the final button logic
// private val LightGray = Color(0xFFE0E0E0) // Not used in the final button logic


private const val maxProblems = 10
private const val TAG = "PreschoolMathsScreen"

enum class ProblemType {
    COUNTING,
    GREATER_THAN,
    LESS_THAN
}

data class Problem(
    val questionText: String,
    val options: List<String>,
    val correctAnswerValue: String,
    val problemType: ProblemType,
    val itemsToCount: Int? = null
)

class Maths0Game {
    private var score = 0
    var currentProblemInternal: Problem? by mutableStateOf(null)
        private set

    // Public getter for non-mutable observation if needed elsewhere, though direct state observation is used in Screen
    val currentProblem: Problem?
        get() = currentProblemInternal

    fun generateProblem(howManyStarsText: String, whichIsMoreText: String, whichIsLessText: String): Problem {
        val problemType = ProblemType.values().random()
        Log.d(TAG, "Generating problem of type: $problemType")

        val newProblem: Problem = when (problemType) {
            ProblemType.COUNTING -> {
                val count = Random.nextInt(1, 6) // Count 1 to 5 items
                val correctAnswer = count.toString()
                val distractors = mutableSetOf<String>()
                // Generate 2 or 3 distractors
                while (distractors.size < Random.nextInt(2, 4)) {
                    val distractorNum = Random.nextInt(1, 8) // numbers from 1 to 7 for options
                    if (distractorNum != count) {
                        distractors.add(distractorNum.toString())
                    }
                }
                val currentOptions = (distractors + correctAnswer).toList().shuffled()
                Problem(
                    questionText = howManyStarsText,
                    options = currentOptions,
                    correctAnswerValue = correctAnswer,
                    problemType = ProblemType.COUNTING,
                    itemsToCount = count
                )
            }
            ProblemType.GREATER_THAN, ProblemType.LESS_THAN -> {
                var num1 = Random.nextInt(1, 10)
                var num2 = Random.nextInt(1, 10)
                while (num2 == num1) {
                    num2 = Random.nextInt(1, 10)
                }
                val question = if (problemType == ProblemType.GREATER_THAN) whichIsMoreText else whichIsLessText
                val correctAnswer = if (problemType == ProblemType.GREATER_THAN) maxOf(num1, num2) else minOf(num1, num2)
                val currentOptions = listOf(num1.toString(), num2.toString()).shuffled()
                Problem(
                    questionText = question,
                    options = currentOptions,
                    correctAnswerValue = correctAnswer.toString(),
                    problemType = problemType
                )
            }
        }
        currentProblemInternal = newProblem
        Log.d(TAG, "New problem: ${newProblem.questionText}, Options: ${newProblem.options}, Correct: ${newProblem.correctAnswerValue}")
        return newProblem
    }

    fun checkAnswer(userSelectedOption: String): Boolean {
        val problem = currentProblemInternal ?: return false
        val isCorrect = userSelectedOption == problem.correctAnswerValue
        if (isCorrect) {
            score++
        }
        Log.d(TAG, "User selected: $userSelectedOption. Correct: $isCorrect. Current score: $score")
        return isCorrect
    }

    fun getScore(): Int = score

    fun resetGame() {
        score = 0
        currentProblemInternal = null
        Log.d(TAG, "Game reset.")
    }
}

private object SoundPlayer0 {
    private var mediaPlayer: MediaPlayer? = null
    fun playSound(context: Context, @RawRes soundResourceId: Int) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(context, soundResourceId)?.apply {
            setOnCompletionListener { mp ->
                mp.release()
                mediaPlayer = null
            }
            start()
        }
    }
    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}

@Composable
fun Maths0Screen(navController: NavController) {
    val game = remember { Maths0Game() }
    var currentProblemState by remember { mutableStateOf(game.currentProblemInternal) }
    var score by remember { mutableIntStateOf(game.getScore()) }
    var isAnswerSubmitted by remember { mutableStateOf(false) }
    var selectedUserOption by remember { mutableStateOf<String?>(null) }
    var totalProblemsAttempted by remember { mutableIntStateOf(0) }
    var showResultsDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val howManyStarsText = stringResource(R.string.how_many_stars)
    val whichIsMoreText = stringResource(R.string.which_is_more)
    val whichIsLessText = stringResource(R.string.which_is_less)

    LaunchedEffect(Unit) {
        currentProblemState = game.generateProblem(howManyStarsText, whichIsMoreText, whichIsLessText)
        Log.d(TAG, "Screen initialized. First problem: ${currentProblemState?.questionText}")
    }

    fun handleNextProblem() {
        // Condition to move to next or show results
        if (totalProblemsAttempted < maxProblems -1 ) { // -1 because we increment *before* showing the next problem
            currentProblemState = game.generateProblem(howManyStarsText, whichIsMoreText, whichIsLessText)
            isAnswerSubmitted = false
            selectedUserOption = null
            totalProblemsAttempted++ // Increment here for the next problem being shown
            Log.d(TAG, "Next problem. Attempted: $totalProblemsAttempted. Problem: ${currentProblemState?.questionText}")
        } else {
            if (!showResultsDialog) { // Ensure this only happens once
                totalProblemsAttempted++ // Account for the last problem being completed
                showResultsDialog = true
                Log.d(TAG, "Max problems reached. Attempted: $totalProblemsAttempted. Showing results.")
            }
        }
    }

    fun submitAndCheckAnswer(userOption: String) {
        if (isAnswerSubmitted) return

        selectedUserOption = userOption
        val problem = currentProblemState ?: return
        val isCorrect = game.checkAnswer(userOption)
        score = game.getScore()
        isAnswerSubmitted = true

        // Ensure you have R.raw.correct_sound and R.raw.wrong_sound in your res/raw folder
        val soundToPlay = if (isCorrect) R.raw.correct else R.raw.wrong
        SoundPlayer0.playSound(context, soundToPlay)

        Log.d(TAG, "User option '$userOption' submitted. Correct: $isCorrect. Score: $score.")

        // If it's the last problem, submitting the answer should lead to results, not require another next click
        if (totalProblemsAttempted >= maxProblems -1 && isAnswerSubmitted) { // -1 because totalProblemsAttempted increments on next
            if (!showResultsDialog) { // Ensure this only happens once
                // totalProblemsAttempted++ // Already accounted for if this is the path
                showResultsDialog = true
                Log.d(TAG, "Last problem submitted. Attempted: ${totalProblemsAttempted + 1}. Showing results.")
            }
        }
    }

    if (showResultsDialog) {
        ResultsDialog0(
            score = score,
            totalAttempted = maxProblems, // Always show maxProblems as total, as we iterate up to it
            onDismiss = {
                showResultsDialog = false
                game.resetGame()
                navController.popBackStack()
            }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .imePadding() // Handles keyboard padding if any text inputs were present
    ) {
        // Main content column
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top info row (Score, Question Count)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Displaying current question number / total questions
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
                    style = MaterialTheme.typography.titleMedium
                )
                Text(text = "Score: $score", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Problem display area
            if (currentProblemState == null && !showResultsDialog) { // Show loading only if not showing results
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                    Text("Loading question...", modifier = Modifier.padding(top = 8.dp))
                }
            } else if (currentProblemState != null){
                val problem = currentProblemState!! // Safe due to null check
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = problem.questionText,
                        style = MaterialTheme.typography.displaySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 20.dp)
                    )

                    // Display items for counting
                    if (problem.problemType == ProblemType.COUNTING && problem.itemsToCount != null) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(vertical = 16.dp)
                                .heightIn(min = 60.dp) // Ensure some space
                        ) {
                            val displayCount = min(problem.itemsToCount, 10) // Max 10 items visual
                            repeat(displayCount) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = "countable item",
                                    modifier = Modifier
                                        .size(36.dp)
                                        .padding(horizontal = 3.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Options display
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        problem.options.forEach { option ->
                            val isSelected = option == selectedUserOption
                            val isCorrectAnswer = option == problem.correctAnswerValue

                            val buttonBackgroundColor = when {
                                isAnswerSubmitted && isCorrectAnswer -> DarkGreen
                                isAnswerSubmitted && isSelected && !isCorrectAnswer -> DarkRed
                                else -> MaterialTheme.colorScheme.secondaryContainer
                            }
                            val textColor = if (isAnswerSubmitted && (isSelected || isCorrectAnswer)) Color.White else MaterialTheme.colorScheme.onSecondaryContainer

                            Button(
                                onClick = {
                                    if (!isAnswerSubmitted) {
                                        submitAndCheckAnswer(option)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 6.dp)
                                    .height(70.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = buttonBackgroundColor,
                                    contentColor = textColor,
                                    disabledContainerColor = buttonBackgroundColor.copy(alpha = 0.6f), // For when enabled = false
                                    disabledContentColor = textColor.copy(alpha = 0.7f)
                                ),
                                enabled = !isAnswerSubmitted // Disable after one selection
                            ) {
                                Text(option, style = MaterialTheme.typography.headlineMedium)
                            }
                        }
                    }
                }
            } else {
                // Fallback for when currentProblemState is null but not loading (e.g., after results dialog if not navigating away)
                Spacer(modifier = Modifier.weight(1f)) // Keep the layout structure
            }
        }

        // Side column for Next and Back
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 8.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // "Next Question" button
            if (isAnswerSubmitted && totalProblemsAttempted < maxProblems -1 && !showResultsDialog) { // Show only if more problems and not showing results
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
        }
    }
}


@Composable
fun ResultsDialog0(
    score: Int,
    totalAttempted: Int, // Should reflect maxProblems
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
                    // Display score out of total problems in the round
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
