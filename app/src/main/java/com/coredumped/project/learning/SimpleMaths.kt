package com.coredumped.project.learning


import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlin.random.Random
import androidx.compose.foundation.text.KeyboardActions // For handling keyboard actions like "Done"
import androidx.compose.foundation.text.KeyboardOptions // For configuring keyboard type (e.g., Number)
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button // Material 3 Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator // For loading indicator
import androidx.compose.material3.ExperimentalMaterial3Api // Opt-in for some Material 3 APIs
import androidx.compose.material3.IconButton // For icon buttons (like the back arrow in TopAppBar)
import androidx.compose.material3.MaterialTheme // For accessing theme colors and typography
import androidx.compose.material3.OutlinedTextField // Material 3 Outlined Text Field
import androidx.compose.material3.Scaffold // For standard screen structure (app bar, content area)
import androidx.compose.material3.Text // Material 3 Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar // Material 3 Top App Bar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults // For TopAppBar default colors
import androidx.compose.ui.platform.LocalFocusManager // To control keyboard focus
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction // For specifying keyboard action (e.g., Done)
import androidx.compose.ui.text.input.KeyboardType // For specifying keyboard input type (e.g., NumberPassword)
import androidx.compose.ui.text.style.TextAlign // For text alignment
import androidx.compose.ui.unit.sp // For specifying text size in scalable pixels


class SimpleMathsGame {

    private var score = 0
    private var currentProblemInternal: Problem? = null
    val currentProblem: Problem?
        get() = currentProblemInternal

    // Updated Problem data class for MCQ
    data class Problem(
        val question: String,
        val options: List<String>, // Options as strings
        val correctAnswer: String // The correct answer string from the options
    )

    fun generateProblem(): Problem {
        val num1 = Random.nextInt(1, 21)
        val num2 = Random.nextInt(1, 21)
        val answer = num1 + num2
        val question = "$num1 + $num2 = ?"

        val options = mutableListOf<String>()
        options.add(answer.toString()) // Add the correct answer

        // Generate plausible distractors
        while (options.size < 4) { // Assuming 4 options
            val distractorOffset = Random.nextInt(-5, 6) // Generate numbers around the answer
            if (distractorOffset == 0) continue // Avoid offset of 0 if it results in the same answer again
            val distractor = answer + distractorOffset
            if (distractor >= 0 && !options.contains(distractor.toString())) { // Ensure non-negative and unique
                options.add(distractor.toString())
            }
        }

        options.shuffle() // Shuffle the options so the correct answer isn't always in the same place

        currentProblemInternal = Problem(question, options, answer.toString())
        return currentProblemInternal!!
    }

    fun checkAnswer(selectedOption: String): Boolean {
        val isCorrect = selectedOption == currentProblemInternal?.correctAnswer
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

// Logging tag
private const val TAG = "SimpleMathsScreen"


@Composable
fun SimpleMathsScreen(navController: NavController) {
    val game = remember { SimpleMathsGame() }

    var currentProblemState by remember { mutableStateOf<SimpleMathsGame.Problem?>(null) }
    var score by remember { mutableIntStateOf(game.getScore()) }
    var isAnswerSubmitted by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf<String?>(null) }

    // New state variables for results dialog
    var totalProblemsAttempted by remember { mutableIntStateOf(0) }
    var showResultsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        currentProblemState = game.generateProblem()
        // Do not increment totalProblemsAttempted here, only on submission
        Log.d(TAG, "MCQ Screen initialized. First problem: ${currentProblemState?.question}")
    }

    fun handleNextProblem() {
        currentProblemState = game.generateProblem()
        isAnswerSubmitted = false
        selectedOption = null
        // Do not increment totalProblemsAttempted here for "Next", only on actual submission
        Log.d(TAG, "Next problem requested: ${currentProblemState?.question}")
    }

    fun handleSubmitOption(option: String) {
        if (isAnswerSubmitted) return

        selectedOption = option
        val isCorrect = game.checkAnswer(option)
        score = game.getScore()
        isAnswerSubmitted = true
        totalProblemsAttempted++ // Increment when an answer is submitted
        Log.d(TAG, "Option '$option' submitted. Correct: $isCorrect. Score: $score. Total Attempted: $totalProblemsAttempted")
    }

    if (showResultsDialog) {
        ResultsDialog(
            score = score,
            totalAttempted = totalProblemsAttempted,
            onDismiss = {
                showResultsDialog = false
                navController.popBackStack() // Navigate back after dismissing dialog
            }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        // Content Area (Column on the left)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Simple Maths", style = MaterialTheme.typography.titleLarge)
                Text(text = "Score: $score", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (currentProblemState == null) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                    Text("Loading problem...", modifier = Modifier.padding(top = 8.dp))
                }
            } else {
                val problem = currentProblemState!!

                Text(
                    text = problem.question,
                    style = MaterialTheme.typography.displaySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.weight(0.5f))

                if (problem.options.size == 4) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            OptionButton(problem.options[0], problem, selectedOption, isAnswerSubmitted, { handleSubmitOption(problem.options[0]) }, Modifier.weight(1f))
                            Spacer(modifier = Modifier.width(10.dp))
                            OptionButton(problem.options[1], problem, selectedOption, isAnswerSubmitted, { handleSubmitOption(problem.options[1]) }, Modifier.weight(1f))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            OptionButton(problem.options[2], problem, selectedOption, isAnswerSubmitted, { handleSubmitOption(problem.options[2]) }, Modifier.weight(1f))
                            Spacer(modifier = Modifier.width(10.dp))
                            OptionButton(problem.options[3], problem, selectedOption, isAnswerSubmitted, { handleSubmitOption(problem.options[3]) }, Modifier.weight(1f))
                        }
                    }
                } else {
                    problem.options.forEach { option ->
                        OptionButton(option, problem, selectedOption, isAnswerSubmitted, { handleSubmitOption(option) })
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        // Button Area (Right Sidebar)
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 8.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            IconButton(
                onClick = {
                    if (isAnswerSubmitted) {
                        handleNextProblem()
                    } else {
                        if (currentProblemState != null && selectedOption != null && !isAnswerSubmitted) {
                            handleSubmitOption(selectedOption!!)
                        }
                    }
                },
                modifier = Modifier
                    .size(64.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(
                        color = if (isAnswerSubmitted) MaterialTheme.colorScheme.secondaryContainer
                        else MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (selectedOption != null) 1f else 0.5f)
                    ),
                enabled = isAnswerSubmitted || (currentProblemState != null && selectedOption != null)
            ) {
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = if (isAnswerSubmitted) "Next Problem" else "Submit Answer",
                    modifier = Modifier.size(32.dp),
                    tint = if (isAnswerSubmitted) MaterialTheme.colorScheme.onSecondaryContainer
                    else MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            // This is the button you wanted to change
            IconButton(
                onClick = {
                    // Show the results dialog instead of navigating back directly
                    showResultsDialog = true
                },
                modifier = Modifier
                    .size(56.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF0D47A1), Color(0xFF1976D2))
                        )
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.DoneAll, // Changed back to DoneAll as requested
                    contentDescription = "Show Results",   // Updated content description
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun OptionButton(
    text: String,
    problem: SimpleMathsGame.Problem,
    selectedOption: String?,
    isAnswerSubmitted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCorrect = text == problem.correctAnswer
    val isSelected = text == selectedOption

    // Log.d(TAG, "OptionButton ('$text'): isSelected=$isSelected, isCorrect=$isCorrect, isAnswerSubmitted=$isAnswerSubmitted, currentCorrectAnswer='${problem.correctAnswer}', currentSelectedProp='$selectedOption'")

    Button(
        onClick = onClick,
        modifier = modifier
            .height(IntrinsicSize.Min)
            .aspectRatio(2.5f / 1f)
            .padding(vertical = 4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = when {
                isAnswerSubmitted && isSelected && isCorrect -> Color(0xFF4CAF50)
                isAnswerSubmitted && isSelected && !isCorrect -> Color(0xFFF44336)
                isAnswerSubmitted && !isSelected && isCorrect -> Color(0xFFA5D6A7)
                isAnswerSubmitted && !isSelected && !isCorrect -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            },
            contentColor = when {
                isAnswerSubmitted && isSelected && (isCorrect || !isCorrect) -> Color.White // Simplified for selected states
                isAnswerSubmitted && !isSelected && isCorrect -> MaterialTheme.colorScheme.onSurfaceVariant // Or a dark green
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        ),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Text(text = text, fontSize = 16.sp, textAlign = TextAlign.Center)
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
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    "You got",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$score / $totalAttempted",
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "correct answers.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
}