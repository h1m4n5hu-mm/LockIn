package com.example.lockin

import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class TaskStateHolder {
    val tasks = mutableStateListOf<Task>()

    var activeTaskId by mutableStateOf<String?>(null)
        private set

    var isTimerRunning by mutableStateOf(false)
        private set

    var isTimerPaused by mutableStateOf(false)
        private set

    private var timerStartedAtMillis by mutableStateOf<Long?>(null)
    private var elapsedBeforePauseMillis by mutableStateOf(0L)

    fun addTask(name: String, durationMinutes: Int) {
        tasks.add(Task(name = name, durationMinutes = durationMinutes))
    }

    fun toggleTaskCompleted(taskId: String) {
        val index = tasks.indexOfFirst { it.id == taskId }
        if (index >= 0) {
            val task = tasks[index]
            tasks[index] = task.copy(completed = !task.completed)
        }
    }

    fun deleteTask(taskId: String) {
        tasks.removeAll { it.id == taskId }
        if (activeTaskId == taskId) {
            resetTimer()
        }
    }

    fun startTimer(taskId: String) {
        activeTaskId = taskId
        elapsedBeforePauseMillis = 0L
        timerStartedAtMillis = SystemClock.elapsedRealtime()
        isTimerRunning = true
        isTimerPaused = false
    }

    fun pauseTimer() {
        if (!isTimerRunning || isTimerPaused) return
        elapsedBeforePauseMillis = currentElapsedMillis()
        timerStartedAtMillis = null
        isTimerPaused = true
    }

    fun resumeTimer() {
        if (!isTimerRunning || !isTimerPaused) return
        timerStartedAtMillis = SystemClock.elapsedRealtime()
        isTimerPaused = false
    }

    fun currentElapsedMillis(nowMillis: Long = SystemClock.elapsedRealtime()): Long {
        val runningElapsed = if (isTimerRunning && !isTimerPaused) {
            nowMillis - (timerStartedAtMillis ?: nowMillis)
        } else {
            0L
        }
        return elapsedBeforePauseMillis + runningElapsed
    }

    fun completeActiveTask() {
        val taskId = activeTaskId ?: return
        val index = tasks.indexOfFirst { it.id == taskId }
        if (index >= 0) {
            tasks[index] = tasks[index].copy(completed = true)
        }
        resetTimer()
    }

    private fun resetTimer() {
        activeTaskId = null
        isTimerRunning = false
        isTimerPaused = false
        timerStartedAtMillis = null
        elapsedBeforePauseMillis = 0L
    }
}
