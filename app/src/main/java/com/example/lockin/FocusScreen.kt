package com.example.lockin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
internal fun FocusScreen(onScreenSelected: (AppScreen) -> Unit) {
    var selectedDuration by remember { mutableStateOf(25) }
    var customDuration by remember { mutableStateOf("") }
    var selectedApps by remember { mutableStateOf(setOf<String>()) }
    var isSessionActive by remember { mutableStateOf(false) }
    var remainingSeconds by remember { mutableStateOf(0) }

    LaunchedEffect(isSessionActive) {
        if (isSessionActive) {
            while (remainingSeconds > 0) {
                delay(1000L)
                remainingSeconds -= 1
            }
            isSessionActive = false
        }
    }

    if (isSessionActive) {
        FocusSessionScreen(
            remainingSeconds = remainingSeconds,
            onEndFocus = {
                isSessionActive = false
                remainingSeconds = 0
            }
        )
        return
    }

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
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Focus",
                        color = PrimaryTextColor,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Protect your time. Eliminate distractions.",
                        color = SecondaryTextColor,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                }

                DurationSection(
                    selectedDuration = selectedDuration,
                    customDuration = customDuration,
                    onDurationSelected = {
                        selectedDuration = it
                        customDuration = ""
                    },
                    onCustomDurationChanged = {
                        customDuration = it.filter(Char::isDigit).take(3)
                        customDuration.toIntOrNull()?.let { minutes ->
                            if (minutes > 0) selectedDuration = minutes
                        }
                    }
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Apps to block",
                        color = PrimaryTextColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    listOf("Instagram", "YouTube", "TikTok", "WhatsApp", "Chrome").forEach { appName ->
                        AppToggleRow(
                            name = appName,
                            checked = appName in selectedApps,
                            onCheckedChange = {
                                selectedApps = if (it) selectedApps + appName else selectedApps - appName
                            }
                        )
                    }
                }

                Button(
                    onClick = {
                        remainingSeconds = selectedDuration.coerceAtLeast(1) * 60
                        isSessionActive = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentColor,
                        contentColor = BackgroundColor
                    )
                ) {
                    Text(text = "Start Focus", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
            BottomNavigation(
                selectedScreen = AppScreen.FOCUS,
                onScreenSelected = onScreenSelected
            )
        }
    }
}

@Composable
private fun DurationSection(
    selectedDuration: Int,
    customDuration: String,
    onDurationSelected: (Int) -> Unit,
    onCustomDurationChanged: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Focus duration",
            color = PrimaryTextColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(25, 45, 60, 90).forEach { duration ->
                Button(
                    onClick = { onDurationSelected(duration) },
                    modifier = Modifier.weight(1f),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedDuration == duration && customDuration.isEmpty()) AccentColor else CardColor,
                        contentColor = if (selectedDuration == duration && customDuration.isEmpty()) BackgroundColor else PrimaryTextColor
                    )
                ) {
                    Text(text = "$duration min", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        OutlinedTextField(
            value = customDuration,
            onValueChange = onCustomDurationChanged,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Custom duration (minutes)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentColor,
                unfocusedBorderColor = DividerColor,
                focusedLabelColor = AccentColor,
                unfocusedLabelColor = SecondaryTextColor,
                focusedTextColor = PrimaryTextColor,
                unfocusedTextColor = PrimaryTextColor,
                cursorColor = AccentColor
            )
        )
    }
}

@Composable
private fun AppToggleRow(name: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardColor, RoundedCornerShape(16.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            color = PrimaryTextColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = AccentColor,
                checkmarkColor = BackgroundColor,
                uncheckedColor = SecondaryTextColor
            )
        )
    }
}

@Composable
private fun FocusSessionScreen(remainingSeconds: Int, onEndFocus: () -> Unit) {
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60

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
                text = "You're locked in.",
                color = PrimaryTextColor,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Stay focused. You've got this.",
                color = SecondaryTextColor,
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 10.dp)
            )
            Text(
                text = "%02d:%02d".format(minutes, seconds),
                color = AccentColor,
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 48.dp)
            )
            Button(
                onClick = onEndFocus,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CardColor,
                    contentColor = PrimaryTextColor
                )
            ) {
                Text(text = "End Focus", fontWeight = FontWeight.Bold)
            }
        }
    }
}