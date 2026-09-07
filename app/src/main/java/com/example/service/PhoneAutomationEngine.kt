package com.example.service

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.util.Log
import com.example.data.model.InstalledAppInfo
import com.example.data.model.SystemTelemetry
import java.io.File
import java.net.URLEncoder

class PhoneAutomationEngine(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private var isTorchActive = false
    private var torchCameraId: String? = null

    init {
        initCameraTorch()
    }

    private fun initCameraTorch() {
        try {
            cameraManager?.let { manager ->
                for (id in manager.cameraIdList) {
                    val characteristics = manager.getCameraCharacteristics(id)
                    val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                    val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                    if (hasFlash && facing == CameraCharacteristics.LENS_FACING_BACK) {
                        torchCameraId = id
                        break
                    }
                }
                if (torchCameraId == null && manager.cameraIdList.isNotEmpty()) {
                    torchCameraId = manager.cameraIdList[0]
                }
            }
        } catch (e: Exception) {
            Log.e("PhoneAutomation", "Torch init error: ${e.message}")
        }
    }

    fun triggerHapticFeedback(durationMs: Long = 40) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (e: Exception) {
            Log.e("PhoneAutomation", "Vibration failed: ${e.message}")
        }
    }

    fun getInstalledApps(): List<InstalledAppInfo> {
        val apps = mutableListOf<InstalledAppInfo>()
        val pm = context.packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        for (app in packages) {
            // Check if app has launch intent
            val launchIntent = pm.getLaunchIntentForPackage(app.packageName)
            if (launchIntent != null) {
                val label = pm.getApplicationLabel(app).toString()
                val isSystem = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                apps.add(
                    InstalledAppInfo(
                        appName = label,
                        packageName = app.packageName,
                        category = if (isSystem) "System" else "User",
                        isSystemApp = isSystem
                    )
                )
            }
        }
        return apps.sortedBy { it.appName.lowercase() }
    }

    fun openApp(target: String): Boolean {
        triggerHapticFeedback()
        val pm = context.packageManager
        val query = target.trim().lowercase()

        // 1. Direct package match
        val directIntent = pm.getLaunchIntentForPackage(target)
        if (directIntent != null) {
            directIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(directIntent)
            return true
        }

        // 2. Well-known standard apps mappings
        val knownPackages = mapOf(
            "whatsapp" to "com.whatsapp",
            "youtube" to "com.google.android.youtube",
            "chrome" to "com.android.chrome",
            "browser" to "com.android.chrome",
            "google" to "com.google.android.googlequicksearchbox",
            "maps" to "com.google.android.apps.maps",
            "google maps" to "com.google.android.apps.maps",
            "gmail" to "com.google.android.gm",
            "email" to "com.google.android.gm",
            "mail" to "com.google.android.gm",
            "spotify" to "com.spotify.music",
            "music" to "com.spotify.music",
            "instagram" to "com.instagram.android",
            "facebook" to "com.facebook.katana",
            "telegram" to "org.telegram.messenger",
            "discord" to "com.discord",
            "snapchat" to "com.snapchat.android",
            "netflix" to "com.netflix.mediaclient",
            "camera" to "camera_intent",
            "gallery" to "gallery_intent",
            "photos" to "com.google.android.apps.photos",
            "calculator" to "calc_intent",
            "clock" to "clock_intent",
            "calendar" to "calendar_intent",
            "settings" to "settings_intent",
            "play store" to "com.android.vending",
            "drive" to "com.google.android.apps.docs",
            "notes" to "notes_intent"
        )

        val mapped = knownPackages[query]
        if (mapped != null) {
            when (mapped) {
                "camera_intent" -> {
                    val intent = Intent(android.provider.MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    if (intent.resolveActivity(pm) != null) {
                        context.startActivity(intent)
                        return true
                    }
                }
                "settings_intent" -> {
                    val intent = Intent(Settings.ACTION_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                    context.startActivity(intent)
                    return true
                }
                "calendar_intent" -> {
                    val intent = Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_APP_CALENDAR)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    if (intent.resolveActivity(pm) != null) {
                        context.startActivity(intent)
                        return true
                    }
                }
                "gallery_intent" -> {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        type = "image/*"
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    if (intent.resolveActivity(pm) != null) {
                        context.startActivity(intent)
                        return true
                    }
                }
                else -> {
                    val pkgIntent = pm.getLaunchIntentForPackage(mapped)
                    if (pkgIntent != null) {
                        pkgIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(pkgIntent)
                        return true
                    }
                }
            }
        }

        // 3. Search among installed apps by name match
        val installed = getInstalledApps()
        val matchedApp = installed.firstOrNull { it.appName.lowercase() == query }
            ?: installed.firstOrNull { it.appName.lowercase().contains(query) }
            ?: installed.firstOrNull { query.contains(it.appName.lowercase()) }

        if (matchedApp != null) {
            val intent = pm.getLaunchIntentForPackage(matchedApp.packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return true
            }
        }

        // 4. Fallback: Search in Google Play Store
        try {
            val storeIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=$query")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (storeIntent.resolveActivity(pm) != null) {
                context.startActivity(storeIntent)
                return true
            }
        } catch (e: Exception) {
            Log.e("PhoneAutomation", "Play Store fallback error: ${e.message}")
        }

        return false
    }

    fun searchInApp(appName: String, query: String): Boolean {
        triggerHapticFeedback()
        val app = appName.lowercase().trim()
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val pm = context.packageManager

        when {
            app.contains("youtube") -> {
                return playYouTube(query)
            }
            app.contains("spotify") || app.contains("music") -> {
                val spotifyUri = Uri.parse("spotify:search:$encodedQuery")
                val intent = Intent(Intent.ACTION_VIEW, spotifyUri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (intent.resolveActivity(pm) != null) {
                    context.startActivity(intent)
                    return true
                }
            }
            app.contains("maps") || app.contains("location") || app.contains("directions") -> {
                val mapUri = Uri.parse("geo:0,0?q=$encodedQuery")
                val intent = Intent(Intent.ACTION_VIEW, mapUri).apply {
                    setPackage("com.google.android.apps.maps")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (intent.resolveActivity(pm) != null) {
                    context.startActivity(intent)
                    return true
                } else {
                    val genericMap = Intent(Intent.ACTION_VIEW, mapUri).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(genericMap)
                    return true
                }
            }
            app.contains("play store") || app.contains("market") -> {
                val storeIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=$encodedQuery")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(storeIntent)
                return true
            }
            else -> {
                return searchWeb(query)
            }
        }
        return false
    }

    fun playYouTube(query: String): Boolean {
        triggerHapticFeedback()
        val pm = context.packageManager
        val encoded = URLEncoder.encode(query, "UTF-8")
        try {
            val appIntent = Intent(Intent.ACTION_SEARCH).apply {
                setPackage("com.google.android.youtube")
                putExtra("query", query)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (appIntent.resolveActivity(pm) != null) {
                context.startActivity(appIntent)
                return true
            }
        } catch (e: Exception) {
            Log.e("PhoneAutomation", "YouTube app intent failed: ${e.message}")
        }

        // Web fallback
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=$encoded")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(webIntent)
        return true
    }

    fun searchWeb(query: String): Boolean {
        triggerHapticFeedback()
        val encoded = URLEncoder.encode(query, "UTF-8")
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=$encoded")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(webIntent)
        return true
    }

    fun openUrl(url: String): Boolean {
        triggerHapticFeedback()
        var targetUrl = url.trim()
        if (!targetUrl.startsWith("http://") && !targetUrl.startsWith("https://")) {
            targetUrl = "https://$targetUrl"
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return true
    }

    fun sendWhatsApp(phoneOrContact: String, message: String): Boolean {
        triggerHapticFeedback()
        val encodedMsg = URLEncoder.encode(message, "UTF-8")
        val cleanPhone = phoneOrContact.replace(Regex("[^0-9+]"), "")

        if (cleanPhone.isNotEmpty()) {
            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedMsg")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.whatsapp")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                return true
            } else {
                // Try generic browser link
                val browserIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(browserIntent)
                return true
            }
        } else {
            // General share intent to WhatsApp
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                setPackage("com.whatsapp")
                putExtra(Intent.EXTRA_TEXT, message)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (sendIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(sendIntent)
                return true
            } else {
                // Open WhatsApp app directly
                return openApp("whatsapp")
            }
        }
    }

    fun sendEmail(recipient: String, subject: String, body: String): Boolean {
        triggerHapticFeedback()
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            if (recipient.isNotBlank()) {
                putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
            }
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
            return true
        }
        return false
    }

    fun setVolume(percent: Int, streamType: Int = AudioManager.STREAM_MUSIC): Int {
        triggerHapticFeedback()
        audioManager?.let { am ->
            val max = am.getStreamMaxVolume(streamType)
            val clamped = percent.coerceIn(0, 100)
            val targetLevel = (clamped * max) / 100
            am.setStreamVolume(streamType, targetLevel, AudioManager.FLAG_SHOW_UI)
            return (targetLevel * 100) / max
        }
        return percent
    }

    fun getVolumePercent(streamType: Int = AudioManager.STREAM_MUSIC): Int {
        audioManager?.let { am ->
            val current = am.getStreamVolume(streamType)
            val max = am.getStreamMaxVolume(streamType)
            if (max > 0) return (current * 100) / max
        }
        return 50
    }

    fun toggleFlashlight(enable: Boolean? = null): Boolean {
        triggerHapticFeedback()
        val targetState = enable ?: !isTorchActive
        val camId = torchCameraId ?: return false
        return try {
            cameraManager?.setTorchMode(camId, targetState)
            isTorchActive = targetState
            isTorchActive
        } catch (e: Exception) {
            Log.e("PhoneAutomation", "Flashlight toggle failed: ${e.message}")
            false
        }
    }

    fun isTorchOn(): Boolean = isTorchActive

    fun openSettings(settingType: String): Boolean {
        triggerHapticFeedback()
        val action = when (settingType.lowercase().trim()) {
            "wifi" -> Settings.ACTION_WIFI_SETTINGS
            "bluetooth" -> Settings.ACTION_BLUETOOTH_SETTINGS
            "sound", "volume", "audio" -> Settings.ACTION_SOUND_SETTINGS
            "display", "brightness" -> Settings.ACTION_DISPLAY_SETTINGS
            "battery", "power" -> Settings.ACTION_BATTERY_SAVER_SETTINGS
            "apps", "applications" -> Settings.ACTION_APPLICATION_SETTINGS
            "dnd", "do_not_disturb" -> Settings.ACTION_ZEN_MODE_PRIORITY_SETTINGS
            "date", "time" -> Settings.ACTION_DATE_SETTINGS
            "accessibility" -> Settings.ACTION_ACCESSIBILITY_SETTINGS
            "location", "gps" -> Settings.ACTION_LOCATION_SOURCE_SETTINGS
            "hotspot", "tethering" -> Settings.ACTION_WIRELESS_SETTINGS
            else -> Settings.ACTION_SETTINGS
        }
        return try {
            val intent = Intent(action).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            val fallback = Intent(Settings.ACTION_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            context.startActivity(fallback)
            true
        }
    }

    fun getSystemTelemetry(): SystemTelemetry {
        // Battery
        val batteryStatusIntent = context.registerReceiver(null, android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryStatusIntent?.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1) ?: 85
        val scale = batteryStatusIntent?.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1) ?: 100
        val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale) else 85
        val status = batteryStatusIntent?.getIntExtra(android.os.BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == android.os.BatteryManager.BATTERY_STATUS_CHARGING || status == android.os.BatteryManager.BATTERY_STATUS_FULL

        // RAM info
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? android.app.ActivityManager
        val memInfo = android.app.ActivityManager.MemoryInfo()
        actManager?.getMemoryInfo(memInfo)
        val totalMb = memInfo.totalMem / (1024 * 1024)
        val availMb = memInfo.availMem / (1024 * 1024)
        val usedMb = (totalMb - availMb).coerceAtLeast(0)

        // Storage
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val availableBlocks = stat.availableBlocksLong
        val freeGb = (availableBlocks * blockSize).toDouble() / (1024 * 1024 * 1024)

        return SystemTelemetry(
            batteryPercent = batteryPct,
            isCharging = isCharging,
            ramUsedMb = usedMb,
            ramTotalMb = totalMb,
            storageFreeGb = String.format("%.1f", freeGb).toDoubleOrNull() ?: 32.0,
            isTorchOn = isTorchActive,
            volumePercent = getVolumePercent(),
            speechEngineReady = true,
            geminiReady = true,
            activeProtocols = 12
        )
    }
}
