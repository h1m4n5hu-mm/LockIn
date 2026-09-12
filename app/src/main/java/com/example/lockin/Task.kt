package com.example.lockin

import java.util.UUID

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val durationMinutes: Int,
    val completed: Boolean = false
)
