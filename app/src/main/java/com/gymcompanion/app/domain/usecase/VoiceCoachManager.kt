package com.gymcompanion.app.domain.usecase

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoiceCoachManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale("es", "ES"))
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("VoiceCoach", "Spanish language not supported, using default")
                    tts?.setLanguage(Locale.getDefault())
                }
                isInitialized = true
                Log.d("VoiceCoach", "TTS initialized successfully")
            } else {
                Log.e("VoiceCoach", "TTS initialization failed")
            }
        }
    }

    fun speak(text: String) {
        if (isInitialized) {
            tts?.speak(text, TextToSpeech.QUEUE_ADD, null, null)
        } else {
            Log.w("VoiceCoach", "TTS not initialized yet")
        }
    }

    fun announceSetComplete() {
        val phrases = listOf(
            "¡Excelente! Serie completada.",
            "¡Bien hecho! Serie terminada.",
            "¡Perfecto! Siguiente serie."
        )
        speak(phrases.random())
    }

    fun announceRestTimer(seconds: Int) {
        speak("Descansa $seconds segundos")
    }

    fun announceRestComplete() {
        speak("¡Siguiente serie!")
    }

    fun announceMotivation() {
        val phrases = listOf(
            "¡Vamos! Tú puedes.",
            "¡Dale todo!",
            "¡Última serie, a por todas!",
            "¡Sigue así, campeón!",
            "¡No te rindas!"
        )
        speak(phrases.random())
    }

    fun announceWorkoutComplete() {
        speak("¡Entrenamiento completado! Excelente trabajo.")
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
