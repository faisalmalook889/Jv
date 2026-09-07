package com.example.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceAssistantEngine(
    private val context: Context,
    private val onSpeechResult: (String) -> Unit
) : RecognitionListener, TextToSpeech.OnInitListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _liveRmsDb = MutableStateFlow(0f)
    val liveRmsDb: StateFlow<Float> = _liveRmsDb.asStateFlow()

    private val _partialText = MutableStateFlow("")
    val partialText: StateFlow<String> = _partialText.asStateFlow()

    var isTtsEnabled: Boolean = true
    var voicePitch: Float = 0.95f
    var voiceRate: Float = 1.08f

    init {
        initSpeechRecognizer()
        initTts()
    }

    private fun initSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(this@VoiceAssistantEngine)
            }
        }
    }

    private fun initTts() {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsInitialized = true
            tts?.let { engine ->
                val result = engine.setLanguage(Locale.UK)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    engine.setLanguage(Locale.US)
                }
                engine.setPitch(voicePitch)
                engine.setSpeechRate(voiceRate)
                engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                    }
                })
            }
        }
    }

    fun startListening() {
        if (speechRecognizer == null) {
            initSpeechRecognizer()
        }
        stopSpeaking()
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        }
        try {
            speechRecognizer?.startListening(intent)
            _isListening.value = true
            _partialText.value = ""
        } catch (e: Exception) {
            Log.e("VoiceAssistant", "Failed to start listening: ${e.message}")
            _isListening.value = false
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            _isListening.value = false
            _liveRmsDb.value = 0f
        } catch (e: Exception) {
            Log.e("VoiceAssistant", "Failed to stop listening: ${e.message}")
        }
    }

    fun speak(text: String) {
        if (!isTtsEnabled || !isTtsInitialized || text.isBlank()) return
        stopListening()
        tts?.setPitch(voicePitch)
        tts?.setSpeechRate(voiceRate)
        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "JARVIS_REPLY_${System.currentTimeMillis()}")
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "JARVIS_REPLY")
    }

    fun stopSpeaking() {
        if (isTtsInitialized) {
            tts?.stop()
            _isSpeaking.value = false
        }
    }

    fun destroy() {
        speechRecognizer?.destroy()
        tts?.stop()
        tts?.shutdown()
    }

    // RecognitionListener callbacks
    override fun onReadyForSpeech(params: Bundle?) {
        _isListening.value = true
    }

    override fun onBeginningOfSpeech() {}

    override fun onRmsChanged(rmsdB: Float) {
        _liveRmsDb.value = rmsdB.coerceIn(0f, 15f)
    }

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {
        _isListening.value = false
        _liveRmsDb.value = 0f
    }

    override fun onError(error: Int) {
        _isListening.value = false
        _liveRmsDb.value = 0f
        Log.w("VoiceAssistant", "Speech recognizer error: $error")
    }

    override fun onResults(results: Bundle?) {
        _isListening.value = false
        _liveRmsDb.value = 0f
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val recognized = matches?.firstOrNull() ?: ""
        if (recognized.isNotBlank()) {
            _partialText.value = recognized
            onSpeechResult(recognized)
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val partial = matches?.firstOrNull() ?: ""
        if (partial.isNotBlank()) {
            _partialText.value = partial
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}
}
