package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Generates a gentle, 100% offline warm romantic music-box / harp arpeggio loop
 * using Android's AudioTrack so no network or external media dependencies are needed.
 */
class AmbientMusicPlayer {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var playJob: Job? = null
    @Volatile
    private var isPlaying = false

    fun setEnabled(enabled: Boolean) {
        if (enabled && !isPlaying) {
            start()
        } else if (!enabled && isPlaying) {
            stop()
        }
    }

    private fun start() {
        if (isPlaying) return
        isPlaying = true
        playJob = scope.launch {
            val sampleRate = 22050
            // Warm romantic progression in F Major / D Minor (Hz)
            val progression = listOf(
                349.23, 440.00, 523.25, 659.25, // Fmaj7
                293.66, 349.23, 440.00, 523.25, // Dm7
                233.08, 293.66, 349.23, 440.00, // Bbmaj7
                261.63, 329.63, 392.00, 523.25  // C
            )
            val noteDurationSec = 0.85
            val samplesPerNote = (sampleRate * noteDurationSec).toInt()
            val minBuf = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(samplesPerNote * 2)

            val track = try {
                AudioTrack.Builder()
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
                    .setBufferSizeInBytes(minBuf)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
            } catch (_: Exception) {
                isPlaying = false
                return@launch
            }

            try {
                track.play()
                val buffer = ShortArray(samplesPerNote)
                var noteIndex = 0
                while (isActive && isPlaying) {
                    val freq = progression[noteIndex % progression.size]
                    for (i in 0 until samplesPerNote) {
                        val t = i.toDouble() / sampleRate
                        val attack = (t / 0.04).coerceAtMost(1.0)
                        val decay = exp(-3.0 * t)
                        val fundamental = sin(2.0 * PI * freq * t)
                        val warmOctave = 0.25 * sin(2.0 * PI * (freq * 2.0) * t)
                        val sample = (fundamental + warmOctave) * attack * decay * 0.16
                        buffer[i] = (sample * Short.MAX_VALUE).toInt()
                            .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                    }
                    track.write(buffer, 0, buffer.size)
                    noteIndex++
                }
            } catch (_: Exception) {
                // Gracefully stop if audio hardware is unavailable (e.g. headless test)
            } finally {
                try {
                    track.pause()
                    track.flush()
                    track.release()
                } catch (_: Exception) {
                }
            }
        }
    }

    fun stop() {
        isPlaying = false
        playJob?.cancel()
        playJob = null
    }
}
