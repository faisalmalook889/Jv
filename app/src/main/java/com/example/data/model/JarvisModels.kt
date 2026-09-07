package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class JarvisExecutionPlan(
    val spokenResponse: String,
    val thoughtSummary: String,
    val steps: List<JarvisStepItem> = emptyList(),
    val memoryLearned: List<JarvisMemoryItem>? = null,
    val needsConfirmation: Boolean = false,
    val confirmationPrompt: String? = null
)

@JsonClass(generateAdapter = true)
data class JarvisStepItem(
    val stepNumber: Int,
    val actionType: String, // "OPEN_APP", "SEARCH_APP", "VOLUME", "FLASHLIGHT", "SETTINGS", "WHATSAPP", "EMAIL", "YOUTUBE", "WEB_SEARCH", "NOTE", "REMINDER", "CALCULATE", "CODE_GEN", "SYSTEM_LOCK", "RESTART_PROMPT"
    val description: String,
    val target: String = "",
    val payload: String = "",
    val extraData: String = "",
    var status: String = "PENDING" // "PENDING", "RUNNING", "COMPLETED", "FAILED"
)

@JsonClass(generateAdapter = true)
data class JarvisMemoryItem(
    val key: String,
    val value: String,
    val category: String = "preference"
)

data class InstalledAppInfo(
    val appName: String,
    val packageName: String,
    val category: String = "App",
    val isSystemApp: Boolean = false
)

data class SystemTelemetry(
    val batteryPercent: Int = 85,
    val isCharging: Boolean = false,
    val ramUsedMb: Long = 2048,
    val ramTotalMb: Long = 4096,
    val storageFreeGb: Double = 32.5,
    val isTorchOn: Boolean = false,
    val volumePercent: Int = 60,
    val speechEngineReady: Boolean = true,
    val geminiReady: Boolean = true,
    val activeProtocols: Int = 8
)
