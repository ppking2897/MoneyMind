package com.bianca.moneymind.presentation.chat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale

/**
 * Helper class for speech recognition
 */
class SpeechRecognitionHelper(
    private val context: Context,
    private val onResult: (String) -> Unit,
    private val onError: (String) -> Unit,
    private val onListening: (Boolean) -> Unit
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false

    fun isAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    fun startListening() {
        if (isListening) return

        if (!isAvailable()) {
            onError("語音辨識功能不可用")
            return
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(createRecognitionListener())
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.TRADITIONAL_CHINESE.toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        isListening = true
        onListening(true)
        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        if (!isListening) return

        isListening = false
        onListening(false)
        speechRecognizer?.stopListening()
    }

    fun destroy() {
        stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    private fun createRecognitionListener() = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            // Ready to receive speech
        }

        override fun onBeginningOfSpeech() {
            // User started speaking
        }

        override fun onRmsChanged(rmsdB: Float) {
            // Sound level changed
        }

        override fun onBufferReceived(buffer: ByteArray?) {
            // Buffer received
        }

        override fun onEndOfSpeech() {
            isListening = false
            onListening(false)
        }

        override fun onError(error: Int) {
            isListening = false
            onListening(false)

            val errorMessage = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "音訊錄製錯誤"
                SpeechRecognizer.ERROR_CLIENT -> "客戶端錯誤"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "權限不足，請允許麥克風權限"
                SpeechRecognizer.ERROR_NETWORK -> "網路錯誤"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "網路超時"
                SpeechRecognizer.ERROR_NO_MATCH -> "無法辨識，請再試一次"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "辨識服務忙碌中"
                SpeechRecognizer.ERROR_SERVER -> "伺服器錯誤"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "沒有偵測到語音"
                else -> "語音辨識錯誤"
            }
            onError(errorMessage)
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val recognizedText = matches?.firstOrNull()

            if (!recognizedText.isNullOrBlank()) {
                onResult(recognizedText)
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            // Partial results - can be used for real-time feedback
        }

        override fun onEvent(eventType: Int, params: Bundle?) {
            // Reserved for future use
        }
    }
}
