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
import androidx.compose.material.icons.filled.DoneAll
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.coredumped.project.R
import kotlin.math.min
import kotlin.random.Random

// Removed sp import as it's not directly used in the provided snippet for modification.
// It might be used elsewhere in your actual file.
// Define Light and Darker Green/Red colors
val LightGreen = Color(0xFFA5D6A7) // A lighter green
val DarkGreen = Color(0xFF388E3C)  // A darker, more saturated green
val LightRed = Color(0xFFEF9A9A)   // A lighter red
val DarkRed = Color(0xFFD32F2F)    // A darker, more saturated red
val LightGray = Color(0xFFE0E0E0)   // A light gray for error highlighting

val maxProblems = 10;

class SimpleMathsGame {

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

            else -> {
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
            while (wrongAnswerOffset == 0) {
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

@Composable
fun SimpleMathsScreen(navController: NavController) {
    val game = remember { SimpleMathsGame() }

    var currentProblemState by remember { mutableStateOf<SimpleMathsGame.Problem?>(null) }
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
        currentProblemState = game.generateProblem()
        isAnswerSubmitted = false
        selectedUserResponse = null
        totalProblemsAttempted++
        if (totalProblemsAttempted == maxProblems) {
            showResultsDialog = true
        }
        Log.d(
            TAG,
            "Next problem requested: ${currentProblemState?.equation}, Expected: ${currentProblemState?.correctAnswerString}"
        )
    }

    fun handleSubmitResponse(userResponse: String) {
        if (isAnswerSubmitted) return

        selectedUserResponse = userResponse
        val isCorrect = game.checkAnswer(userResponse)
        score = game.getScore()
        isAnswerSubmitted = true
        Log.d(
            TAG,
            "User response '$userResponse' submitted. Correct: $isCorrect. Score: $score. Total Attempted: $totalProblemsAttempted"
        )
    }

    if (showResultsDialog) {
        ResultsDialog(
            score = score,
            totalAttempted = totalProblemsAttempted,
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
            .imePadding()
    ) {
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
                Text(text = stringResource(id = R.string.is_it_correct), style = MaterialTheme.typography.titleLarge)
                val currentQuestionDisplayNumber = min(totalProblemsAttempted + 1, maxProblems)

                Text(
                    // UPDATED TEXT FORMAT
                    text = if (totalProblemsAttempted >= maxProblems && isAnswerSubmitted) {
                        "Question $maxProblems / $maxProblems" // Show max/max if last question is submitted
                    } else {
                        "Question $currentQuestionDisplayNumber / $maxProblems"
                    },
                    style = MaterialTheme.typography.titleMedium
                )

                Text(text = "Score: $score", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (currentProblemState == null) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                    Text("Loading equation...", modifier = Modifier.padding(top = 8.dp))
                }
            } else {
                val problem = currentProblemState!!
                Text(
                    text = problem.equation,
                    style = MaterialTheme.typography.displayMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                        .weight(1f)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val correctButtonSelected = selectedUserResponse == "Correct"
                    val incorrectButtonSelected = selectedUserResponse == "Incorrect"

                    ChoiceIconButton(
                        onClick = { handleSubmitResponse("Correct") },
                        icon = Icons.Filled.Check,
                        contentDescription = "Correct",
                        isSelected = correctButtonSelected,
                        isCorrectChoice = problem.isEquationCorrect,
                        isAnswerSubmitted = isAnswerSubmitted,
                        baseColorLight = LightGreen,
                        baseColorDark = DarkGreen,
                        errorColorDark = LightGray
                    )

                    Spacer(modifier = Modifier.width(24.dp))

                    ChoiceIconButton(
                        onClick = { handleSubmitResponse("Incorrect") },
                        icon = Icons.Filled.Close,
                        contentDescription = "Incorrect",
                        isSelected = incorrectButtonSelected,
                        isCorrectChoice = !problem.isEquationCorrect,
                        isAnswerSubmitted = isAnswerSubmitted,
                        baseColorLight = LightRed,
                        baseColorDark = DarkRed,
                        errorColorDark = LightGray
                    )
                }
                Spacer(modifier = Modifier.weight(0.5f))
            }
        }

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
                        if (currentProblemState != null && selectedUserResponse != null && !isAnswerSubmitted) {
                            handleSubmitResponse(selectedUserResponse!!)
                        }
                    }
                },
                modifier = Modifier
                    .size(64.dp)
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
                    ),
                enabled = isAnswerSubmitted || (currentProblemState != null && selectedUserResponse != null)
            ) {
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = if (isAnswerSubmitted) "Next Equation" else "Submit Answer",
                    modifier = Modifier.size(32.dp),
                    tint = if (isAnswerSubmitted) MaterialTheme.colorScheme.onSecondaryContainer
                    else MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .size(64.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer
                    )
                    .clickable { navController.popBackStack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun ChoiceIconButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    isSelected: Boolean,
    isCorrectChoice: Boolean,
    isAnswerSubmitted: Boolean,
    baseColorLight: Color,
    baseColorDark: Color,
    errorColorDark: Color, // Color to show if this button was selected but was the wrong choice
    modifier: Modifier = Modifier
) {
    val containerColor = when {
        isAnswerSubmitted && isCorrectChoice -> baseColorDark
        isAnswerSubmitted && !isCorrectChoice -> errorColorDark
        else -> baseColorLight // Default to light color
    }

    val iconColor = Color.White // Always white
    val iconSize = 48.dp

    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(iconSize * 1.5f)
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .background(containerColor), // Applied background here
        enabled = !isAnswerSubmitted,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = Color.Transparent, // Make default IconButton container transparent, background is handled by Modifier
            contentColor = iconColor, // Use the always white color
            disabledContainerColor = Color.Transparent, // Background handled by modifier
            disabledContentColor = iconColor.copy(alpha = if (isAnswerSubmitted && (isSelected || isCorrectChoice)) 1f else 0.7f) // Keep white, slightly transparent if not prominent
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(iconSize)
            // tint is overridden by IconButtonDefaults.iconButtonColors contentColor
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