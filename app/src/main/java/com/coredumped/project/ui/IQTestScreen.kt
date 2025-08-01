package com.coredumped.project.iq

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlin.random.Random // For shuffling questions if needed

// --- Data Structures and Game Logic for IQ Test ---

class IQTestGame {
    private var score = 0
    private var currentProblemInternal: Problem? = null
    private var allQuestions: List<Problem> = emptyList()
    private var currentQuestionIndex = -1

    val currentProblem: Problem?
        get() = currentProblemInternal

    // IQ Problem data class
    data class Problem(
        val id: Int,
        val questionText: String,
        val options: List<String>,
        val correctAnswer: String
    )

    init {
        loadQuestions()
        allQuestions = allQuestions.shuffled(Random) // Shuffle questions once on init
    }

    private fun loadQuestions() {
        allQuestions = listOf(
            Problem(
                id = 1,
                questionText = "Which number logically follows this series?\n4, 6, 9, 6, 14, 6, ...",
                options = listOf("6", "17", "19", "21"),
                correctAnswer = "19"
            ),
            Problem(
                id = 2,
                questionText = "Book is to Reading as Fork is to:",
                options = listOf("Drawing", "Writing", "Stirring", "Eating"),
                correctAnswer = "Eating"
            ),
            Problem(
                id = 3,
                questionText = "What is the missing number in the sequence?\n3, 7, 16, 35, ?, 153",
                options = listOf("70", "74", "78", "82"),
                correctAnswer = "74"
            ),
            Problem(
                id = 4,
                questionText = "Which one of the five is least like the other four? (Select the odd one out)",
                options = listOf("Dog", "Mouse", "Lion", "Snake"),
                correctAnswer = "Snake"
            ),
            Problem(
                id = 5,
                questionText = "A man walks 5 km South, then 4 km West, then 5 km North. How far is he from his starting point?",
                options = listOf("0 km", "4 km", "5 km", "9 km"),
                correctAnswer = "4 km"
            ),
            Problem(
                id = 6,
                questionText = "If all Bloops are Razzies and all Razzies are Lazzies, are all Bloops definitely Lazzies?",
                options = listOf("Yes", "No", "Maybe", "Not enough info"),
                correctAnswer = "Yes"
            ),
            Problem(
                id = 7,
                questionText = "Which letter does not belong in this series: A, C, F, J, O, ?",
                options = listOf("S", "T", "U", "V"),
                correctAnswer = "U"
            )
        )
    }

    fun generateNextQuestion(): Problem? {
        currentQuestionIndex++
        return if (currentQuestionIndex < allQuestions.size) {
            currentProblemInternal = allQuestions[currentQuestionIndex]
            currentProblemInternal
        } else {
            currentProblemInternal = null
            null
        }
    }

    fun checkAnswer(selectedOption: String): Boolean {
        val isCorrect = selectedOption == currentProblemInternal?.correctAnswer
        if (isCorrect) {
            score++
        }
        return isCorrect
    }

    fun getScore(): Int = score
    fun getTotalQuestions(): Int = allQuestions.size

    fun resetGame() {
        score = 0
        currentQuestionIndex = -1
        currentProblemInternal = null
        allQuestions = allQuestions.shuffled(Random)
    }
}

private const val IQ_TAG = "IQTestScreen"

@Composable
fun IQTestScreen(navController: NavController) {
    val game = remember { IQTestGame() }

    var currentProblemState by remember { mutableStateOf<IQTestGame.Problem?>(null) }
    var score by remember { mutableIntStateOf(game.getScore()) }
    var isAnswerSubmitted by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var currentQuestionNumber by remember { mutableIntStateOf(0) }

    var showResultsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        currentProblemState = game.generateNextQuestion()
        if (currentProblemState != null) { // Only set question number if a question is loaded
            currentQuestionNumber = 1
        }
        Log.d(IQ_TAG, "IQ Test Screen initialized. First problem: ${currentProblemState?.questionText}")
    }

    fun handleNextQuestion() {
        currentProblemState = game.generateNextQuestion()
        if (currentProblemState != null) {
            isAnswerSubmitted = false
            selectedOption = null
            currentQuestionNumber++
            Log.d(IQ_TAG, "Next question requested: ${currentProblemState?.questionText}")
        } else {
            showResultsDialog = true
            Log.d(IQ_TAG, "End of IQ Test. Showing results.")
        }
    }

    fun handleSubmitOption(option: String) {
        if (isAnswerSubmitted || currentProblemState == null) return

        selectedOption = option
        val isCorrect = game.checkAnswer(option)
        score = game.getScore()
        isAnswerSubmitted = true
        Log.d(IQ_TAG, "Option '$option' submitted. Correct: $isCorrect. Score: $score.")
    }

    // Removed triggerConfetti function
    // Removed LaunchedEffect for showResultsDialog triggering confetti

    // The main layout is now directly a Row, not nested in a Box for confetti
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
                Text(text = "IQ Challenge", style = MaterialTheme.typography.titleLarge)
                if (currentProblemState != null || showResultsDialog) { // Show count if questions loaded or results are up
                    Text(
                        text = "Q: $currentQuestionNumber/${game.getTotalQuestions()}",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            Text(text = "Score: $score", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.End))

            Spacer(modifier = Modifier.height(16.dp))

            if (currentProblemState == null && !showResultsDialog) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    if (game.getTotalQuestions() == 0 && currentQuestionNumber == 0) { // Initial state, no questions yet
                        Text("Loading Test...", style = MaterialTheme.typography.bodyLarge)
                        CircularProgressIndicator(modifier = Modifier.padding(top=8.dp))

                    } else if (game.getTotalQuestions() == 0){
                        Text("No questions loaded.", style = MaterialTheme.typography.bodyLarge)
                    }
                    else { // Should ideally not be hit if generateNextQuestion is called in LaunchedEffect
                        CircularProgressIndicator()
                        Text("Loading Question...", modifier = Modifier.padding(top = 8.dp))
                    }
                }
            } else if (currentProblemState != null) {
                val problem = currentProblemState!!

                Text(
                    text = problem.questionText,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .weight(0.4f)
                )

                val optionsToShow = problem.options.take(4)
                if (optionsToShow.size == 4) {
                    Column(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            IQOptionButton(optionsToShow[0], problem, selectedOption, isAnswerSubmitted, { handleSubmitOption(optionsToShow[0]) }, Modifier.weight(1f))
                            Spacer(modifier = Modifier.width(10.dp))
                            IQOptionButton(optionsToShow[1], problem, selectedOption, isAnswerSubmitted, { handleSubmitOption(optionsToShow[1]) }, Modifier.weight(1f))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            IQOptionButton(optionsToShow[2], problem, selectedOption, isAnswerSubmitted, { handleSubmitOption(optionsToShow[2]) }, Modifier.weight(1f))
                            Spacer(modifier = Modifier.width(10.dp))
                            IQOptionButton(optionsToShow[3], problem, selectedOption, isAnswerSubmitted, { handleSubmitOption(optionsToShow[3]) }, Modifier.weight(1f))
                        }
                    }
                } else {
                    optionsToShow.forEach { option ->
                        IQOptionButton(option, problem, selectedOption, isAnswerSubmitted, { handleSubmitOption(option) })
                    }
                }
                Spacer(modifier = Modifier.weight(0.2f))
            } else {
                Spacer(modifier = Modifier.weight(1f)) // Placeholder when results dialog is shown
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
                        handleNextQuestion()
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
                enabled = (isAnswerSubmitted || selectedOption != null) && currentProblemState != null
            ) {
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = when {
                        isAnswerSubmitted -> "Next Question"
                        selectedOption != null -> "Submit Answer"
                        else -> "Select an Option"
                    },
                    modifier = Modifier.size(32.dp),
                    tint = if (isAnswerSubmitted) MaterialTheme.colorScheme.onSecondaryContainer
                    else MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            IconButton(
                onClick = {
                    showResultsDialog = true
                },
                modifier = Modifier
                    .size(56.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF2E7D32), Color(0xFF4CAF50))
                        )
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.DoneAll,
                    contentDescription = "Finish Test & Show Results",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }


    // Results Dialog (remains the same, but no confetti will be triggered)
    if (showResultsDialog) {
        IQResultsDialog(
            score = score,
            totalQuestions = game.getTotalQuestions(),
            onDismiss = {
                showResultsDialog = false
                game.resetGame()
                navController.popBackStack()
            }
        )
    }
}

@Composable
fun IQOptionButton(
    text: String,
    problem: IQTestGame.Problem,
    selectedOption: String?,
    isAnswerSubmitted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCorrect = text == problem.correctAnswer
    val isSelected = text == selectedOption

    Button(
        onClick = onClick,
        modifier = modifier
            .height(IntrinsicSize.Min)
            .aspectRatio(2.8f / 1f)
            .padding(vertical = 4.dp),
        enabled = !isAnswerSubmitted,
        colors = ButtonDefaults.buttonColors(
            containerColor = when {
                isAnswerSubmitted && isSelected && isCorrect -> Color(0xFF4CAF50)
                isAnswerSubmitted && isSelected && !isCorrect -> Color(0xFFF44336)
                isAnswerSubmitted && !isSelected && isCorrect -> Color(0xFFA5D6A7)
                isAnswerSubmitted && !isSelected && !isCorrect -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            },
            contentColor = when {
                isAnswerSubmitted && isSelected -> Color.White
                isAnswerSubmitted && !isSelected && isCorrect -> MaterialTheme.colorScheme.onSurfaceVariant
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            disabledContainerColor = when {
                isSelected && isCorrect -> Color(0xFF4CAF50)
                isSelected && !isCorrect -> Color(0xFFF44336)
                !isSelected && isCorrect -> Color(0xFFA5D6A7)
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            },
            disabledContentColor = when {
                isSelected -> Color.White
                !isSelected && isCorrect -> MaterialTheme.colorScheme.onSurfaceVariant
                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            }
        ),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )
    }
}

@Composable
fun IQResultsDialog(
    score: Int,
    totalQuestions: Int,
    onDismiss: () -> Unit
) {
    val percentage = if (totalQuestions > 0) (score.toFloat() / totalQuestions.toFloat() * 100).toInt() else 0
    val resultMessage = when {
        percentage >= 90 -> "Exceptional! A true mastermind!"
        percentage >= 75 -> "Excellent! Very impressive score."
        percentage >= 60 -> "Great job! Well above average."
        percentage >= 40 -> "Good effort! Solid performance."
        else -> "Keep practicing to sharpen your skills!"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Test Results",
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
                    "You scored:",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$score / $totalQuestions",
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "($percentage%)",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = resultMessage,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
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
