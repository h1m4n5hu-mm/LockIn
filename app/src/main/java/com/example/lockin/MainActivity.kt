package com.example.lockin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import com.example.lockin.ui.theme.LockInTheme
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LockInTheme {
                LockInApp()
            }
        }
    }
}

@Composable
fun LockInApp() {
    var currentScreen by remember { mutableStateOf(AppScreen.WELCOME) }
    val taskStateHolder = remember { TaskStateHolder() }

    BackHandler(enabled = currentScreen != AppScreen.WELCOME && currentScreen != AppScreen.HOME) {
        currentScreen = AppScreen.HOME
    }

    when (currentScreen) {
        AppScreen.WELCOME -> WelcomeScreen(onGetStarted = { currentScreen = AppScreen.HOME })
        AppScreen.HOME -> HomeScreen(
            taskStateHolder = taskStateHolder,
            onScreenSelected = { currentScreen = it },
            onTaskSelected = {
                taskStateHolder.startTimer(it)
                currentScreen = AppScreen.TASK_TIMER
            }
        )
        AppScreen.TASK_TIMER -> TaskTimerScreen(
            taskStateHolder = taskStateHolder,
            onFinished = { currentScreen = AppScreen.HOME }
        )
        AppScreen.FOCUS -> FocusScreen(onScreenSelected = { currentScreen = it })
        AppScreen.STATS -> PlaceholderScreen(
            title = "Your Progress",
            onScreenSelected = { currentScreen = it },
            selectedScreen = AppScreen.STATS
        )
        AppScreen.SETTINGS -> PlaceholderScreen(
            title = "Settings",
            onScreenSelected = { currentScreen = it },
            selectedScreen = AppScreen.SETTINGS
        )
    }
}

@Composable
fun WelcomeScreen(onGetStarted: () -> Unit = {}) {
    val backgroundColor = Color(0xFF0B0D0C)
    val primaryTextColor = Color(0xFFF5F7F5)
    val secondaryTextColor = Color(0xFFA8B0AA)
    val accentColor = Color(0xFFB7F397)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 24.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "LOCKIN",
            color = accentColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )

        Column(
            modifier = Modifier.padding(vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                text = "Focus on what matters.",
                color = primaryTextColor,
                fontSize = 42.sp,
                lineHeight = 48.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Build discipline. Protect your time. Become consistent.",
                color = secondaryTextColor,
                fontSize = 17.sp,
                lineHeight = 25.sp
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Button(
                onClick = onGetStarted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    contentColor = backgroundColor
                )
            ) {
                Text(
                    text = "Get Started",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "Your time. Your focus. Your choice.",
                color = secondaryTextColor,
                fontSize = 12.sp
            )
        }
    }
}

internal val BackgroundColor = Color(0xFF0B0D0C)
internal val CardColor = Color(0xFF151916)
internal val PrimaryTextColor = Color(0xFFF5F7F5)
internal val SecondaryTextColor = Color(0xFFA8B0AA)
internal val AccentColor = Color(0xFFB7F397)
internal val DividerColor = Color(0xFF29302A)

@Composable
private fun HomeScreen(
    taskStateHolder: TaskStateHolder,
    onScreenSelected: (AppScreen) -> Unit,
    onTaskSelected: (String) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var isAddTaskVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = "LOCKIN",
                    color = AccentColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Good morning",
                        color = PrimaryTextColor,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Friday, September 11",
                        color = SecondaryTextColor,
                        fontSize = 14.sp
                    )
                }

                ProgressCard(tasks = taskStateHolder.tasks)

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Today's Tasks",
                            color = PrimaryTextColor,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = { isAddTaskVisible = !isAddTaskVisible },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentColor,
                                contentColor = BackgroundColor
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(text = if (isAddTaskVisible) "Close" else "Add task", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (isAddTaskVisible) {
                        AddTaskForm(
                            onTaskAdded = { name, duration ->
                                taskStateHolder.addTask(name, duration)
                                isAddTaskVisible = false
                            }
                        )
                    }

                    if (taskStateHolder.tasks.isEmpty()) {
                        Text(
                            text = "No tasks yet. Add one to start your day.",
                            color = SecondaryTextColor,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        taskStateHolder.tasks.forEach { task ->
                            TaskRow(
                                task = task,
                                onCheckedChange = { taskStateHolder.toggleTaskCompleted(task.id) },
                                onDelete = { taskStateHolder.deleteTask(task.id) },
                                onPlay = { onTaskSelected(task.id) }
                            )
                        }
                    }
                }

                FocusCard(
                    onStartFocus = {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Focus mode will be available soon.")
                        }
                    }
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        title = "7 Day Streak",
                        value = "0 days",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Screen Time",
                        value = "Not available yet",
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
            BottomNavigation(
                selectedScreen = AppScreen.HOME,
                onScreenSelected = onScreenSelected
            )
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 76.dp, start = 20.dp, end = 20.dp)
        )
    }
}

@Composable
private fun ProgressCard(tasks: List<Task>) {
    val completedTasks = tasks.count { it.completed }
    val progress = if (tasks.isEmpty()) 0f else completedTasks.toFloat() / tasks.size
    val progressPercent = (progress * 100).roundToInt()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardColor, RoundedCornerShape(20.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Today's Progress",
                color = PrimaryTextColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(text = "$progressPercent%", color = AccentColor, fontWeight = FontWeight.Bold)
        }
        Text(text = "$completedTasks / ${tasks.size} tasks completed", color = SecondaryTextColor, fontSize = 14.sp)
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = AccentColor,
            trackColor = DividerColor
        )
    }
}

@Composable
private fun TaskRow(
    task: Task,
    onCheckedChange: () -> Unit,
    onDelete: () -> Unit,
    onPlay: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardColor, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = task.completed,
            onCheckedChange = { onCheckedChange() },
            colors = CheckboxDefaults.colors(
                checkedColor = AccentColor,
                checkmarkColor = BackgroundColor,
                uncheckedColor = SecondaryTextColor
            )
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = task.name,
                color = if (task.completed) SecondaryTextColor else PrimaryTextColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(text = "${task.durationMinutes} min", color = SecondaryTextColor, fontSize = 12.sp)
        }
        Button(
            onClick = onPlay,
            enabled = !task.completed,
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentColor,
                contentColor = BackgroundColor,
                disabledContainerColor = DividerColor,
                disabledContentColor = SecondaryTextColor
            ),
            shape = RoundedCornerShape(10.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(text = "Play", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Button(
            onClick = onDelete,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = SecondaryTextColor),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Text(text = "Delete", fontSize = 11.sp)
        }
    }
}

@Composable
private fun AddTaskForm(onTaskAdded: (String, Int) -> Unit) {
    var name by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardColor, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it; errorMessage = null },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Task name") },
            colors = taskTextFieldColors()
        )
        OutlinedTextField(
            value = duration,
            onValueChange = { duration = it.filter(Char::isDigit).take(4); errorMessage = null },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Duration (minutes)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = taskTextFieldColors()
        )
        if (errorMessage != null) {
            Text(text = errorMessage!!, color = Color(0xFFFF9D9D), fontSize = 12.sp)
        }
        Button(
            onClick = {
                val durationMinutes = duration.toIntOrNull()
                when {
                    name.isBlank() -> errorMessage = "Enter a task name."
                    durationMinutes == null || durationMinutes <= 0 -> errorMessage = "Duration must be greater than 0."
                    else -> onTaskAdded(name.trim(), durationMinutes)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = AccentColor, contentColor = BackgroundColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "Add task", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun taskTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AccentColor,
    unfocusedBorderColor = DividerColor,
    focusedLabelColor = AccentColor,
    unfocusedLabelColor = SecondaryTextColor,
    focusedTextColor = PrimaryTextColor,
    unfocusedTextColor = PrimaryTextColor,
    cursorColor = AccentColor
)

@Composable
private fun FocusCard(onStartFocus: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AccentColor, RoundedCornerShape(20.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(text = "Ready to focus?", color = BackgroundColor, fontSize = 21.sp, fontWeight = FontWeight.Bold)
        Text(text = "Protect your time. Get in the zone.", color = Color(0xFF34412F), fontSize = 14.sp)
        Button(
            onClick = onStartFocus,
            colors = ButtonDefaults.buttonColors(containerColor = BackgroundColor, contentColor = PrimaryTextColor),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text(text = "Start Focus", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(CardColor, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(text = title, color = SecondaryTextColor, fontSize = 12.sp)
        Text(text = value, color = PrimaryTextColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PlaceholderScreen(
    title: String,
    selectedScreen: AppScreen,
    onScreenSelected: (AppScreen) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = title, color = PrimaryTextColor, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text(text = "Coming soon", color = SecondaryTextColor, fontSize = 16.sp, modifier = Modifier.padding(top = 10.dp))
        }
        BottomNavigation(
            selectedScreen = selectedScreen,
            onScreenSelected = onScreenSelected,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
internal fun BottomNavigation(
    selectedScreen: AppScreen,
    onScreenSelected: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        Triple(AppScreen.HOME, "H", "Home"),
        Triple(AppScreen.FOCUS, "F", "Focus"),
        Triple(AppScreen.STATS, "S", "Stats"),
        Triple(AppScreen.SETTINGS, "⚙", "Settings")
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(CardColor)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items.forEach { (screen, icon, label) ->
            val isSelected = screen == selectedScreen
            Column(
                modifier = Modifier
                    .clickable { onScreenSelected(screen) }
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = icon,
                    color = if (isSelected) AccentColor else SecondaryTextColor,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = label,
                    color = if (isSelected) PrimaryTextColor else SecondaryTextColor,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    LockInTheme {
        WelcomeScreen()
    }
}