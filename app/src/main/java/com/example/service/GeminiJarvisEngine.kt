package com.example.service

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.JarvisExecutionPlan
import com.example.data.model.JarvisMemoryItem
import com.example.data.model.JarvisStepItem
import com.example.data.model.SystemTelemetry
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiJarvisEngine {

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val planAdapter = moshi.adapter(JarvisExecutionPlan::class.java)

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val MODEL_NAME = "gemini-3.5-flash"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

        private val SYSTEM_PROMPT = """
            You are JARVIS, an autonomous real-time Android AI Assistant modeled after Iron Man's JARVIS.
            Your primary goal is to completely automate the user's Android smartphone through natural voice commands while maintaining speed, accuracy, security, and a premium experience.
            
            Core Identity:
            - You are NOT a chatbot. You are an autonomous Android Operating AI.
            - You think before acting and break complex multi-step tasks into ordered sub-steps.
            - Whenever the user gives a command, execute it automatically rather than explaining how to do it.
            - Voice tone: Crisp, intelligent, confident, concise, British-courteous (e.g., "Right away, sir.", "Executing protocols now.", "Volume adjusted to 70% and launching YouTube search for Arijit Singh.").
            
            Multi-Step Execution capability:
            If user says "Open YouTube, search Arijit Singh songs, play the latest song and increase volume to 70%":
            You must output multiple sequential steps in the 'steps' array.
            
            Supported Action Types for steps:
            - "VOLUME": adjust volume. target = stream name ("media", "ring"), payload = percent value (e.g., "70").
            - "FLASHLIGHT": toggle torch. payload = "true" or "false" or "toggle".
            - "OPEN_APP": launch app. target = app name (e.g., "whatsapp", "youtube", "chrome", "camera", "spotify", "instagram", "settings", "calculator", "notes", "clock", "calendar", "gallery").
            - "SEARCH_APP": search within app. target = app name ("youtube", "spotify", "maps", "play store"), payload = search query.
            - "YOUTUBE": search/play on YouTube. payload = query (e.g. "Arijit Singh latest songs").
            - "WHATSAPP": send message. target = contact or phone number, payload = message body.
            - "EMAIL": compose email. target = recipient email or name, payload = email body, extraData = subject line.
            - "WEB_SEARCH": search Google or open web page. payload = query or url.
            - "SETTINGS": open system settings. target = "wifi", "bluetooth", "sound", "display", "battery", "apps", "dnd", "date", "accessibility".
            - "NOTE": save note / document / code. target = title, payload = content.
            - "REMINDER": create reminder. target = title, payload = scheduled time.
            - "CALCULATE": solve math. payload = solution & steps.
            - "CODE_GEN": generate code / script. target = language, payload = code snippet.
            - "SYSTEM_LOCK": lock screen request.
            - "RESTART_PROMPT": prompt confirmation for restart / shutdown / dangerous action.
            
            Output MUST be valid strictly formatted JSON matching this exact structure:
            {
              "spokenResponse": "Short verbal response from JARVIS",
              "thoughtSummary": "Brief reasoning / protocol summary",
              "steps": [
                {
                  "stepNumber": 1,
                  "actionType": "VOLUME",
                  "description": "Set media volume to 70%",
                  "target": "media",
                  "payload": "70",
                  "extraData": ""
                },
                {
                  "stepNumber": 2,
                  "actionType": "YOUTUBE",
                  "description": "Search and play Arijit Singh songs on YouTube",
                  "target": "youtube",
                  "payload": "Arijit Singh latest songs",
                  "extraData": ""
                }
              ],
              "memoryLearned": [
                {
                  "key": "favorite_artist",
                  "value": "Arijit Singh",
                  "category": "preference"
                }
              ],
              "needsConfirmation": false,
              "confirmationPrompt": null
            }
            
            For risky actions (formatting device, deleting files, sending funds, factory reset, restarting device), set "needsConfirmation": true and provide "confirmationPrompt".
            Do NOT include markdown formatting or backticks around the json if possible, or provide standard json object.
        """.trimIndent()
    }

    suspend fun processCommand(
        command: String,
        conversationHistory: List<Pair<String, String>> = emptyList(),
        telemetry: SystemTelemetry? = null,
        knownMemories: List<Pair<String, String>> = emptyList()
    ): JarvisExecutionPlan = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.d("GeminiJarvis", "API key missing or placeholder. Running Offline Autonomous NLP Engine.")
            return@withContext offlineParseCommand(command, telemetry)
        }

        try {
            val jsonContext = buildContextJson(command, conversationHistory, telemetry, knownMemories)
            val requestUrl = "$BASE_URL?key=$apiKey"

            val body = jsonContext.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(requestUrl)
                .post(body)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val respBody = response.body?.string()

            if (!response.isSuccessful || respBody == null) {
                Log.w("GeminiJarvis", "API call unsuccessful (${response.code}). Falling back to Offline Engine.")
                return@withContext offlineParseCommand(command, telemetry)
            }

            val parsedPlan = parseGeminiResponse(respBody)
            parsedPlan ?: offlineParseCommand(command, telemetry)
        } catch (e: Exception) {
            Log.e("GeminiJarvis", "Gemini call exception: ${e.message}. Using offline fallback.")
            offlineParseCommand(command, telemetry)
        }
    }

    private fun buildContextJson(
        command: String,
        history: List<Pair<String, String>>,
        telemetry: SystemTelemetry?,
        memories: List<Pair<String, String>>
    ): JSONObject {
        val root = JSONObject()

        // System Instruction
        val sysInstructionObj = JSONObject()
        val sysParts = JSONArray()
        sysParts.put(JSONObject().put("text", SYSTEM_PROMPT))
        sysInstructionObj.put("parts", sysParts)
        root.put("systemInstruction", sysInstructionObj)

        // Contents
        val contentsArray = JSONArray()

        // Add context info
        val contextInfo = StringBuilder()
        if (telemetry != null) {
            contextInfo.append("System Telemetry: Battery ${telemetry.batteryPercent}%, RAM ${telemetry.ramUsedMb}/${telemetry.ramTotalMb}MB, Flashlight ${if (telemetry.isTorchOn) "ON" else "OFF"}, Volume ${telemetry.volumePercent}%.\n")
        }
        if (memories.isNotEmpty()) {
            contextInfo.append("Known User Preferences/Memory:\n")
            memories.take(10).forEach { (k, v) -> contextInfo.append("- $k: $v\n") }
        }

        // Add history
        history.takeLast(6).forEach { (role, text) ->
            val roleName = if (role.lowercase() == "user") "user" else "model"
            val contentObj = JSONObject()
            contentObj.put("role", roleName)
            val parts = JSONArray()
            parts.put(JSONObject().put("text", text))
            contentObj.put("parts", parts)
            contentsArray.put(contentObj)
        }

        // Current command
        val currentContent = JSONObject()
        currentContent.put("role", "user")
        val currentParts = JSONArray()
        val promptText = if (contextInfo.isNotEmpty()) "$contextInfo\nUser Command: $command" else command
        currentParts.put(JSONObject().put("text", promptText))
        currentContent.put("parts", currentParts)
        contentsArray.put(currentContent)

        root.put("contents", contentsArray)

        // Generation Config
        val genConfig = JSONObject()
        genConfig.put("temperature", 0.2)
        genConfig.put("topP", 0.95)
        root.put("generationConfig", genConfig)

        return root
    }

    private fun parseGeminiResponse(rawJson: String): JarvisExecutionPlan? {
        try {
            val jsonRoot = JSONObject(rawJson)
            val candidates = jsonRoot.optJSONArray("candidates") ?: return null
            val firstCandidate = candidates.optJSONObject(0) ?: return null
            val content = firstCandidate.optJSONObject("content") ?: return null
            val parts = content.optJSONArray("parts") ?: return null
            val text = parts.optJSONObject(0)?.optString("text") ?: return null

            // Clean json text if wrapped in ```json ... ```
            var cleanText = text.trim()
            if (cleanText.startsWith("```json")) {
                cleanText = cleanText.removePrefix("```json").trim()
            }
            if (cleanText.startsWith("```")) {
                cleanText = cleanText.removePrefix("```").trim()
            }
            if (cleanText.endsWith("```")) {
                cleanText = cleanText.removeSuffix("```").trim()
            }

            return planAdapter.fromJson(cleanText)
        } catch (e: Exception) {
            Log.e("GeminiJarvis", "Failed to parse json plan: ${e.message}")
            return null
        }
    }

    /**
     * Autonomous Offline NLP Command Engine
     * Decomposes complex multi-sentence or chained voice commands into ordered steps with 0ms latency!
     */
    fun offlineParseCommand(rawCommand: String, telemetry: SystemTelemetry?): JarvisExecutionPlan {
        val lower = rawCommand.lowercase().trim()
        val steps = mutableListOf<JarvisStepItem>()
        var spokenResponse = "Protocols executed, sir."
        var thought = "Decomposed offline voice command stream."
        val learnedMemory = mutableListOf<JarvisMemoryItem>()
        var needsConfirmation = false
        var confirmationPrompt: String? = null

        // Split chained commands by "and", "then", ",", "."
        val subClauses = lower.split(Regex("(?:\\band\\b|,|\\bthen\\b|\\.)"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        var stepIdx = 1

        for (clause in subClauses) {
            when {
                // 1. Volume command
                clause.contains("volume") || clause.contains("sound") -> {
                    val numberMatch = Regex("(\\d+)").find(clause)
                    val targetVol = if (numberMatch != null) {
                        numberMatch.groupValues[1].toIntOrNull()?.coerceIn(0, 100) ?: 70
                    } else if (clause.contains("up") || clause.contains("increase") || clause.contains("max") || clause.contains("full")) {
                        ((telemetry?.volumePercent ?: 50) + 25).coerceAtMost(100)
                    } else if (clause.contains("down") || clause.contains("decrease") || clause.contains("mute") || clause.contains("silent") || clause.contains("low")) {
                        if (clause.contains("mute") || clause.contains("silent")) 0 else ((telemetry?.volumePercent ?: 50) - 25).coerceAtLeast(0)
                    } else {
                        70
                    }
                    steps.add(
                        JarvisStepItem(
                            stepNumber = stepIdx++,
                            actionType = "VOLUME",
                            description = "Adjust audio volume to $targetVol%",
                            target = "media",
                            payload = targetVol.toString()
                        )
                    )
                }

                // 2. Flashlight / Torch
                clause.contains("flashlight") || clause.contains("torch") -> {
                    val enable = if (clause.contains("off") || clause.contains("disable") || clause.contains("stop")) "false" else "true"
                    steps.add(
                        JarvisStepItem(
                            stepNumber = stepIdx++,
                            actionType = "FLASHLIGHT",
                            description = if (enable == "true") "Activate flashlight" else "Deactivate flashlight",
                            payload = enable
                        )
                    )
                }

                // 3. YouTube search / song playback
                clause.contains("youtube") || clause.contains("song") || clause.contains("play ") || clause.contains("music") -> {
                    var songQuery = clause
                        .replace("open youtube", "")
                        .replace("search on youtube", "")
                        .replace("search in youtube", "")
                        .replace("search youtube for", "")
                        .replace("search youtube", "")
                        .replace("play the latest song", "")
                        .replace("play latest song", "")
                        .replace("play songs by", "")
                        .replace("play song", "")
                        .replace("play", "")
                        .replace("songs", "")
                        .replace("for", "")
                        .trim()

                    if (songQuery.isBlank()) {
                        songQuery = "Top trending songs"
                    }
                    steps.add(
                        JarvisStepItem(
                            stepNumber = stepIdx++,
                            actionType = "YOUTUBE",
                            description = "Search & Play '$songQuery' on YouTube",
                            target = "youtube",
                            payload = songQuery
                        )
                    )
                    learnedMemory.add(JarvisMemoryItem("favorite_search", songQuery, "favorite_song"))
                }

                // 4. WhatsApp
                clause.contains("whatsapp") -> {
                    val messageMatch = Regex("saying\\s+(.+)|message\\s+(.+)|to\\s+([a-zA-Z0-9+]+)\\s*(?:saying|message)?\\s*(.*)").find(clause)
                    val recipient = messageMatch?.groups?.get(3)?.value?.trim() ?: ""
                    val msg = messageMatch?.groups?.get(1)?.value ?: messageMatch?.groups?.get(2)?.value ?: messageMatch?.groups?.get(4)?.value ?: "Hello, this is JARVIS automation."

                    steps.add(
                        JarvisStepItem(
                            stepNumber = stepIdx++,
                            actionType = "WHATSAPP",
                            description = "Compose WhatsApp message" + if (recipient.isNotEmpty()) " to $recipient" else "",
                            target = recipient,
                            payload = msg.trim()
                        )
                    )
                }

                // 5. Email
                clause.contains("email") || clause.contains("mail") -> {
                    val toMatch = Regex("to\\s+([a-zA-Z0-9@._-]+)").find(clause)
                    val recipient = toMatch?.groupValues?.get(1) ?: ""
                    steps.add(
                        JarvisStepItem(
                            stepNumber = stepIdx++,
                            actionType = "EMAIL",
                            description = "Compose email message" + if (recipient.isNotEmpty()) " to $recipient" else "",
                            target = recipient,
                            payload = "Dear Sir,\n\nI am writing to provide an immediate update. All requested protocols are online.\n\nBest regards,\nJARVIS System",
                            extraData = "Status Update"
                        )
                    )
                }

                // 6. Settings toggles
                clause.contains("settings") || clause.contains("wifi") || clause.contains("bluetooth") || clause.contains("display") || clause.contains("battery") -> {
                    val settingName = when {
                        clause.contains("wifi") -> "wifi"
                        clause.contains("bluetooth") -> "bluetooth"
                        clause.contains("display") || clause.contains("brightness") -> "display"
                        clause.contains("battery") -> "battery"
                        clause.contains("sound") -> "sound"
                        clause.contains("apps") -> "apps"
                        else -> "general"
                    }
                    steps.add(
                        JarvisStepItem(
                            stepNumber = stepIdx++,
                            actionType = "SETTINGS",
                            description = "Open $settingName settings",
                            target = settingName
                        )
                    )
                }

                // 7. Math calculation
                clause.contains("calculate") || clause.contains("solve") || clause.contains("+") || clause.contains("*") || clause.contains("times") || clause.contains("divided") -> {
                    steps.add(
                        JarvisStepItem(
                            stepNumber = stepIdx++,
                            actionType = "CALCULATE",
                            description = "Perform mathematical calculation",
                            payload = clause
                        )
                    )
                }

                // 8. Note / Document creation
                clause.contains("note") || clause.contains("write") || clause.contains("draft") -> {
                    val noteText = clause.replace("take a note", "").replace("write note", "").replace("note that", "").replace("note", "").trim()
                    steps.add(
                        JarvisStepItem(
                            stepNumber = stepIdx++,
                            actionType = "NOTE",
                            description = "Create and save note",
                            target = "JARVIS Note",
                            payload = if (noteText.isNotEmpty()) noteText else "Automated note created via JARVIS voice protocol."
                        )
                    )
                }

                // 9. Reminder / Schedule
                clause.contains("reminder") || clause.contains("remind me") || clause.contains("schedule") -> {
                    val remText = clause.replace("remind me to", "").replace("reminder for", "").replace("schedule", "").trim()
                    steps.add(
                        JarvisStepItem(
                            stepNumber = stepIdx++,
                            actionType = "REMINDER",
                            description = "Create reminder: $remText",
                            target = if (remText.isNotEmpty()) remText else "Scheduled Task",
                            payload = "Today, 5:00 PM"
                        )
                    )
                }

                // 10. Web search
                clause.contains("search") || clause.contains("google") || clause.contains("who is") || clause.contains("what is") || clause.contains("news") -> {
                    val query = clause.replace("search for", "").replace("search google for", "").replace("search", "").replace("google", "").trim()
                    steps.add(
                        JarvisStepItem(
                            stepNumber = stepIdx++,
                            actionType = "WEB_SEARCH",
                            description = "Search Google for '$query'",
                            payload = if (query.isNotEmpty()) query else "Latest news headlines"
                        )
                    )
                }

                // 11. App launcher
                clause.contains("open") || clause.contains("launch") || clause.contains("start") -> {
                    val appName = clause
                        .replace("open", "")
                        .replace("launch", "")
                        .replace("start", "")
                        .replace("the", "")
                        .replace("app", "")
                        .trim()

                    if (appName.isNotEmpty()) {
                        steps.add(
                            JarvisStepItem(
                                stepNumber = stepIdx++,
                                actionType = "OPEN_APP",
                                description = "Launch $appName",
                                target = appName
                            )
                        )
                    }
                }

                // 12. Remember fact / preference
                clause.contains("remember") || clause.contains("my favorite") || clause.contains("prefer") -> {
                    val fact = clause.replace("remember that", "").replace("remember", "").trim()
                    learnedMemory.add(JarvisMemoryItem("user_preference", fact, "preference"))
                    steps.add(
                        JarvisStepItem(
                            stepNumber = stepIdx++,
                            actionType = "NOTE",
                            description = "Record preference into memory core",
                            target = "User Memory",
                            payload = fact
                        )
                    )
                }

                // 13. Risky commands (Restart/Shutdown/Wipe)
                clause.contains("restart") || clause.contains("shutdown") || clause.contains("power off") || clause.contains("format") || clause.contains("delete all") -> {
                    needsConfirmation = true
                    confirmationPrompt = "Security Confirmation Required: Are you sure you wish to initiate device power/system operation?"
                    steps.add(
                        JarvisStepItem(
                            stepNumber = stepIdx++,
                            actionType = "RESTART_PROMPT",
                            description = "System Security Confirmation Protocol",
                            payload = clause
                        )
                    )
                }

                // Default general action
                else -> {
                    if (steps.isEmpty()) {
                        steps.add(
                            JarvisStepItem(
                                stepNumber = stepIdx++,
                                actionType = "WEB_SEARCH",
                                description = "Search query: $clause",
                                payload = clause
                            )
                        )
                    }
                }
            }
        }

        if (steps.isEmpty()) {
            steps.add(
                JarvisStepItem(
                    stepNumber = 1,
                    actionType = "WEB_SEARCH",
                    description = "Search '$rawCommand'",
                    payload = rawCommand
                )
            )
        }

        spokenResponse = when {
            steps.any { it.actionType == "YOUTUBE" && steps.any { it.actionType == "VOLUME" } } ->
                "Right away, sir. Adjusting volume and launching YouTube."
            steps.any { it.actionType == "YOUTUBE" } ->
                "Playing media on YouTube now, sir."
            steps.any { it.actionType == "WHATSAPP" } ->
                "Preparing WhatsApp communication dispatch."
            steps.any { it.actionType == "FLASHLIGHT" } ->
                "Flashlight protocol toggled."
            steps.any { it.actionType == "VOLUME" } ->
                "Volume level adjusted as requested."
            steps.any { it.actionType == "OPEN_APP" } ->
                "Launching ${steps.first { it.actionType == "OPEN_APP" }.target} now."
            steps.any { it.actionType == "EMAIL" } ->
                "Opening email composer with prefilled draft."
            steps.any { it.actionType == "NOTE" } ->
                "Recorded and committed to memory bank, sir."
            steps.any { it.actionType == "REMINDER" } ->
                "Reminder scheduled successfully."
            else ->
                "Protocols initiated for: $rawCommand."
        }

        thought = "Decomposed input into ${steps.size} executable step(s) with automated sequence handler."

        return JarvisExecutionPlan(
            spokenResponse = spokenResponse,
            thoughtSummary = thought,
            steps = steps,
            memoryLearned = if (learnedMemory.isNotEmpty()) learnedMemory else null,
            needsConfirmation = needsConfirmation,
            confirmationPrompt = confirmationPrompt
        )
    }
}
