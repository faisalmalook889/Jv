package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.JarvisDatabase
import com.example.data.local.JarvisRepository
import com.example.data.local.entities.CommandLogEntity
import com.example.data.local.entities.JarvisMemoryEntity
import com.example.data.local.entities.JarvisNoteEntity
import com.example.data.local.entities.JarvisReminderEntity
import com.example.data.model.InstalledAppInfo
import com.example.data.model.JarvisExecutionPlan
import com.example.data.model.JarvisStepItem
import com.example.data.model.SystemTelemetry
import com.example.service.GeminiJarvisEngine
import com.example.service.PhoneAutomationEngine
import com.example.service.VoiceAssistantEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class JarvisViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = JarvisRepository(JarvisDatabase.getInstance(application).jarvisDao())
    val automationEngine = PhoneAutomationEngine(application)
    private val geminiEngine = GeminiJarvisEngine()

    private val _telemetry = MutableStateFlow(automationEngine.getSystemTelemetry())
    val telemetry: StateFlow<SystemTelemetry> = _telemetry.asStateFlow()

    private val _installedApps = MutableStateFlow<List<InstalledAppInfo>>(emptyList())
    val installedApps: StateFlow<List<InstalledAppInfo>> = _installedApps.asStateFlow()

    // Active conversation
    private val _conversationHistory = MutableStateFlow<List<Pair<String, String>>>(listOf(
        Pair("JARVIS", "System online, sir. All core protocols, voice telemetry, and Android phone automation modules are initialized and standing by.")
    ))
    val conversationHistory: StateFlow<List<Pair<String, String>>> = _conversationHistory.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _currentPlan = MutableStateFlow<JarvisExecutionPlan?>(null)
    val currentPlan: StateFlow<JarvisExecutionPlan?> = _currentPlan.asStateFlow()

    private val _activeSteps = MutableStateFlow<List<JarvisStepItem>>(emptyList())
    val activeSteps: StateFlow<List<JarvisStepItem>> = _activeSteps.asStateFlow()

    // Security Dialog
    private val _pendingSecurityPrompt = MutableStateFlow<String?>(null)
    val pendingSecurityPrompt: StateFlow<String?> = _pendingSecurityPrompt.asStateFlow()
    private var pendingSecurityAction: (() -> Unit)? = null

    // Room DB streams
    val memoryList: StateFlow<List<JarvisMemoryEntity>> = repository.allMemory.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val notesList: StateFlow<List<JarvisNoteEntity>> = repository.allNotes.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val remindersList: StateFlow<List<JarvisReminderEntity>> = repository.allReminders.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val commandLogs: StateFlow<List<CommandLogEntity>> = repository.recentLogs.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // Voice Engine
    val voiceEngine = VoiceAssistantEngine(application) { recognizedText ->
        executeVoiceCommand(recognizedText)
    }

    val isListening: StateFlow<Boolean> = voiceEngine.isListening
    val isSpeaking: StateFlow<Boolean> = voiceEngine.isSpeaking
    val liveRmsDb: StateFlow<Float> = voiceEngine.liveRmsDb
    val partialTranscript: StateFlow<String> = voiceEngine.partialText

    init {
        refreshInstalledApps()
        startTelemetryPoller()
    }

    private fun startTelemetryPoller() {
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                _telemetry.value = automationEngine.getSystemTelemetry()
                delay(3000)
            }
        }
    }

    fun refreshInstalledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            _installedApps.value = automationEngine.getInstalledApps()
        }
    }

    fun toggleVoiceListening() {
        if (voiceEngine.isListening.value) {
            voiceEngine.stopListening()
        } else {
            voiceEngine.startListening()
        }
    }

    fun executeVoiceCommand(command: String) {
        if (command.isBlank() || _isProcessing.value) return

        val startTime = System.currentTimeMillis()
        _isProcessing.value = true

        // Append to local conversation
        val updatedHistory = _conversationHistory.value.toMutableList().apply {
            add(Pair("User", command))
        }
        _conversationHistory.value = updatedHistory

        viewModelScope.launch {
            try {
                val knownMemories = memoryList.value.map { Pair(it.key, it.value) }
                val plan = withContext(Dispatchers.IO) {
                    geminiEngine.processCommand(
                        command = command,
                        conversationHistory = updatedHistory,
                        telemetry = _telemetry.value,
                        knownMemories = knownMemories
                    )
                }

                _currentPlan.value = plan
                _activeSteps.value = plan.steps

                // Check security prompt
                if (plan.needsConfirmation && plan.confirmationPrompt != null) {
                    _pendingSecurityPrompt.value = plan.confirmationPrompt
                    pendingSecurityAction = {
                        runExecutionSteps(plan, command, startTime)
                    }
                    _isProcessing.value = false
                    return@launch
                }

                runExecutionSteps(plan, command, startTime)
            } catch (e: Exception) {
                val errorMsg = "System encounter: ${e.message ?: "Unable to complete request."}"
                appendAssistantReply(errorMsg)
                voiceEngine.speak(errorMsg)
                _isProcessing.value = false
            }
        }
    }

    private fun runExecutionSteps(plan: JarvisExecutionPlan, rawCommand: String, startTime: Long) {
        viewModelScope.launch {
            // Speak verbal response
            appendAssistantReply(plan.spokenResponse)
            voiceEngine.speak(plan.spokenResponse)

            // Save learned memories
            plan.memoryLearned?.forEach { mem ->
                repository.saveMemory(mem.key, mem.value, mem.category)
            }

            val stepsList = plan.steps.map { it.copy() }.toMutableList()
            _activeSteps.value = stepsList

            // Sequential multi-step execution loop
            var allSucceeded = true
            for (i in stepsList.indices) {
                val step = stepsList[i]
                step.status = "RUNNING"
                _activeSteps.value = stepsList.toList()
                delay(300) // Visual pacing

                val success = executeSingleStep(step)
                step.status = if (success) "COMPLETED" else "FAILED"
                _activeSteps.value = stepsList.toList()
                if (!success) allSucceeded = false
                delay(200)
            }

            val duration = System.currentTimeMillis() - startTime
            repository.logExecution(
                rawCommand = rawCommand,
                reply = plan.spokenResponse,
                stepCount = plan.steps.size,
                status = if (allSucceeded) "SUCCESS" else "PARTIAL",
                latencyMs = duration
            )

            _telemetry.value = automationEngine.getSystemTelemetry()
            _isProcessing.value = false
        }
    }

    private suspend fun executeSingleStep(step: JarvisStepItem): Boolean = withContext(Dispatchers.Main) {
        try {
            when (step.actionType) {
                "OPEN_APP" -> automationEngine.openApp(step.target)
                "SEARCH_APP" -> automationEngine.searchInApp(step.target, step.payload)
                "YOUTUBE" -> automationEngine.playYouTube(step.payload)
                "VOLUME" -> {
                    val percent = step.payload.toIntOrNull() ?: 70
                    automationEngine.setVolume(percent)
                    true
                }
                "FLASHLIGHT" -> {
                    val enable = if (step.payload.contains("false") || step.payload.contains("off")) false
                    else if (step.payload.contains("true") || step.payload.contains("on")) true
                    else null
                    automationEngine.toggleFlashlight(enable)
                    true
                }
                "SETTINGS" -> automationEngine.openSettings(step.target)
                "WHATSAPP" -> automationEngine.sendWhatsApp(step.target, step.payload)
                "EMAIL" -> automationEngine.sendEmail(step.target, step.extraData.ifEmpty { "JARVIS Dispatch" }, step.payload)
                "WEB_SEARCH" -> {
                    if (step.payload.startsWith("http://") || step.payload.startsWith("https://")) {
                        automationEngine.openUrl(step.payload)
                    } else {
                        automationEngine.searchWeb(step.payload)
                    }
                }
                "NOTE" -> {
                    repository.saveNote(step.target.ifEmpty { "JARVIS Note" }, step.payload)
                    true
                }
                "REMINDER" -> {
                    repository.saveReminder(step.target.ifEmpty { "Task Reminder" }, step.payload.ifEmpty { "Today" })
                    true
                }
                "CALCULATE" -> {
                    repository.saveNote("Calculation Result", "${step.description}\n\n${step.payload}", "math")
                    true
                }
                "CODE_GEN" -> {
                    repository.saveNote(step.target.ifEmpty { "Generated Code" }, step.payload, "code")
                    true
                }
                "SYSTEM_LOCK" -> {
                    automationEngine.openSettings("accessibility")
                    true
                }
                "RESTART_PROMPT" -> {
                    automationEngine.triggerHapticFeedback(100)
                    true
                }
                else -> {
                    if (step.payload.isNotEmpty()) {
                        automationEngine.searchWeb(step.payload)
                    } else {
                        true
                    }
                }
            }
        } catch (e: Exception) {
            false
        }
    }

    fun confirmSecurityAction() {
        _pendingSecurityPrompt.value = null
        pendingSecurityAction?.invoke()
        pendingSecurityAction = null
    }

    fun dismissSecurityAction() {
        _pendingSecurityPrompt.value = null
        pendingSecurityAction = null
        _isProcessing.value = false
        val abortMsg = "Security protocol aborted by user."
        appendAssistantReply(abortMsg)
        voiceEngine.speak(abortMsg)
    }

    private fun appendAssistantReply(reply: String) {
        val updatedHistory = _conversationHistory.value.toMutableList().apply {
            add(Pair("JARVIS", reply))
        }
        _conversationHistory.value = updatedHistory
    }

    fun toggleTorchDirect() {
        automationEngine.toggleFlashlight()
        _telemetry.value = automationEngine.getSystemTelemetry()
    }

    fun setVolumeDirect(percent: Int) {
        automationEngine.setVolume(percent)
        _telemetry.value = automationEngine.getSystemTelemetry()
    }

    // CRUD helpers for Productivity Tab
    fun addMemory(key: String, value: String, category: String = "preference") {
        viewModelScope.launch { repository.saveMemory(key, value, category) }
    }

    fun deleteMemory(id: Long) {
        viewModelScope.launch { repository.deleteMemory(id) }
    }

    fun addNote(title: String, content: String, category: String = "note") {
        viewModelScope.launch { repository.saveNote(title, content, category) }
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch { repository.deleteNote(id) }
    }

    fun addReminder(title: String, scheduledTime: String) {
        viewModelScope.launch { repository.saveReminder(title, scheduledTime) }
    }

    fun toggleReminder(reminder: JarvisReminderEntity) {
        viewModelScope.launch { repository.toggleReminder(reminder) }
    }

    fun deleteReminder(id: Long) {
        viewModelScope.launch { repository.deleteReminder(id) }
    }

    fun clearCommandLogs() {
        viewModelScope.launch { repository.clearAllLogs() }
    }

    override fun onCleared() {
        super.onCleared()
        voiceEngine.destroy()
    }
}
