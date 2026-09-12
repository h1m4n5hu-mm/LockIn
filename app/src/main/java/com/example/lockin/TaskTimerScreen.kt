package com.example.lockin

import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.ceil

@Composable
internal fun TaskTimerScreen(
    taskStateHolder: TaskStateHolder,
    onFinished: () -> Unit
) {
    val activeTask = taskStateHolder.tasks.firstOrNull { it.id == taskStateHolder.activeTaskId }
    if (activeTask == null) {
        LaunchedEffect(Unit) { onFinished() }
        return
    }

    var currentTimeMillis by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }

    LaunchedEffect(taskStateHolder.isTimerRunning, taskStateHolder.isTimerPaused) {
        while (taskStateHolder.isTimerRunning && !taskStateHolder.isTimerPaused) {
            currentTimeMillis = SystemClock.elapsedRealtime()
            delay(250L)
        }
        currentTimeMillis = SystemClock.elapsedRealtime()
    }

    val allocatedMillis = activeTask.durationMinutes * 60_000L
    val elapsedMillis = taskStateHolder.currentElapsedMillis(currentTimeMillis)
    val overtimeMillis = (elapsedMillis - allocatedMillis).coerceAtLeast(0L)
    val isOvertime = elapsedMillis > allocatedMillis
    val displayedSeconds = if (isOvertime) {
        overtimeMillis / 1_000L
    } else {
        ceil((allocatedMillis - elapsedMillis).coerceAtLeast(0L) / 1_000.0).toLong()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Task timer",
                color = AccentColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Text(
                text = activeTask.name,
                color = PrimaryTextColor,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 18.dp)
            )
            Text(
                text = if (isOvertime) "Overtime" else "Stay focused. You've got this.",
                color = if (isOvertime) AccentColor else SecondaryTextColor,
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 10.dp)
            )
            Text(
                text = formatTimerSeconds(displayedSeconds, isOvertime),
                color = if (isOvertime) Color(0xFFFFC078) else AccentColor,
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 48.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        if (taskStateHolder.isTimerPaused) {
                            taskStateHolder.resumeTimer()
                        } else {
                            taskStateHolder.pauseTimer()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CardColor,
                        contentColor = PrimaryTextColor
                    )
                ) {
                    Text(
                        text = if (taskStateHolder.isTimerPaused) "Resume" else "Pause",
                        fontWeight = FontWeight.Bold
                    )
                }
                Button(
                    onClick = {
                        taskStateHolder.completeActiveTask()
                        onFinished()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentColor,
                        contentColor = BackgroundColor
                    )
                ) {
                    Text(text = "Complete", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun formatTimerSeconds(seconds: Long, overtime: Boolean): String {
    val minutes = seconds / 60L
    val remainingSeconds = seconds % 60L
    val prefix = if (overtime) "+" else ""
    return "$prefix%02d:%02d".format(minutes, remainingSeconds)
}
