package com.example.api

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.*
import kotlin.math.sin

object SoundEngine {
    private const val TAG = "SoundEngine"
    private var audioTrack: AudioTrack? = null
    private var isPlaying = false
    private var soundJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var baseFreq = 110.0 
    private var breathFreq = 0.2 
    private var beatTempo = 0.0 

    fun start(tempo: String, outcome: String) {
        stop()
        isPlaying = true

        baseFreq = when (outcome.lowercase()) {
            "comfort", "stress relief", "calmness", "emotional support" -> 130.81 // comforting C3 drone
            "focus", "inspiration" -> 146.83 // focused D3 tone
            else -> 98.0 // deep motivational G2 rumble
        }

        updateTempoParams(tempo)

        soundJob = scope.launch {
            try {
                synthesizeAudio()
            } catch (e: Exception) {
                Log.e(TAG, "Audio synthesis error", e)
            }
        }
    }

    fun updateTempo(tempo: String) {
        updateTempoParams(tempo)
    }

    private fun updateTempoParams(tempo: String) {
        breathFreq = when (tempo.lowercase()) {
            "slow" -> 0.125 // Slow wave (~8s full cycle)
            "moderate" -> 0.2 // Moderate wave (~5s cycle)
            else -> 0.35 // Quick energizing wave (~3s cycle)
        }
        beatTempo = when (tempo.lowercase()) {
            "slow" -> 0.0 // Drone only
            "moderate" -> 1.2 // Gentle pulsing tempo (72 BPM)
            else -> 2.4 // Fast driving drums/pulse (144 BPM)
        }
    }

    private fun synthesizeAudio() {
        val sampleRate = 44100
        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val bufferSize = if (minBufferSize > 0) minBufferSize else 4096

        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack = track
        track.play()

        val shortBuffer = ShortArray(bufferSize)
        var phase = 0.0
        var breathPhase = 0.0
        var beatCount = 0L

        while (isPlaying) {
            for (i in shortBuffer.indices) {
                // Drone tone oscillator
                val drone = sin(phase)
                phase += 2.0 * Math.PI * baseFreq / sampleRate
                if (phase > 2.0 * Math.PI) phase -= 2.0 * Math.PI

                // Harmonious fifth tone overlay for depth and color
                val harmony = sin(phase * 1.5) * 0.35

                // Breathing wave volume modulator (0.1 to 1.0)
                val breathMod = 0.1 + 0.9 * ((sin(breathPhase) + 1.0) / 2.0)
                breathPhase += 2.0 * Math.PI * breathFreq / sampleRate
                if (breathPhase > 2.0 * Math.PI) breathPhase -= 2.0 * Math.PI

                // Percussive rhythm clicks for active comeback vibes
                var beatClick = 0.0
                if (beatTempo > 0.0) {
                    val samplesPerBeat = (sampleRate / beatTempo).toLong()
                    if (samplesPerBeat > 0) {
                        val positionInBeat = beatCount % samplesPerBeat
                        if (positionInBeat < 3000) { // First 3000 samples decay
                            val decay = 1.0 - (positionInBeat / 3000.0)
                            // Synthesize regional-like kick/percussion beat
                            beatClick = sin(2.0 * Math.PI * 55.0 * positionInBeat / sampleRate) * decay * 0.4
                        }
                    }
                    beatCount++
                }

                // Mix oscillators and clamp safely
                val mixed = ((drone + harmony) * 0.25 * breathMod) + beatClick
                val finalVal = mixed * 32767.0
                shortBuffer[i] = finalVal.coerceIn(-32768.0, 32767.0).toInt().toShort()
            }
            track.write(shortBuffer, 0, shortBuffer.size)
        }
    }

    fun stop() {
        isPlaying = false
        soundJob?.cancel()
        soundJob = null
        try {
            audioTrack?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            // ignore
        }
        audioTrack = null
    }
}
