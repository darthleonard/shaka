package com.darthleonard.shaka.core.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import kotlin.math.PI
import kotlin.math.sin

/**
 * Generates an audio beep using AudioTrack with PCM 16-bit sine wave.
 * Includes a gentle fade-in and fade-out envelope to eliminate speaker clicking.
 */
class BeepGenerator(
    private val sampleRate: Int = 44100,
    private val toneFrequencyHz: Double = 1000.0,
    private val durationMs: Int = 50
) {
    private var audioTrack: AudioTrack? = null
    private var pcmBuffer: ShortArray? = null
    private var isInitialized = false

    @Synchronized
    fun initialize(volume: Float = 1.0f) {
        if (isInitialized && audioTrack != null) return

        try {
            val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
            val generatedBuffer = ShortArray(numSamples)
            val fadeSamples = (sampleRate * 0.005).toInt() // 5ms fade envelope

            for (i in 0 until numSamples) {
                val time = i.toDouble() / sampleRate
                val rawSine = sin(2.0 * PI * toneFrequencyHz * time)

                // Envelope calculation
                val envelope = when {
                    i < fadeSamples -> i.toDouble() / fadeSamples
                    i > numSamples - fadeSamples -> (numSamples - i).toDouble() / fadeSamples
                    else -> 1.0
                }

                val sample = (rawSine * envelope * Short.MAX_VALUE * 0.85).toInt()
                generatedBuffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            pcmBuffer = generatedBuffer

            val bufferSizeInBytes = numSamples * 2 // 16-bit = 2 bytes per sample

            val track = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSizeInBytes)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                AudioTrack(
                    AudioManager.STREAM_NOTIFICATION,
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSizeInBytes,
                    AudioTrack.MODE_STATIC
                )
            }

            track.write(generatedBuffer, 0, numSamples)
            setVolumeLocked(track, volume)
            audioTrack = track
            isInitialized = true
        } catch (e: Exception) {
            release()
            throw e
        }
    }

    @Synchronized
    fun setVolume(volume: Float) {
        audioTrack?.let { setVolumeLocked(it, volume) }
    }

    private fun setVolumeLocked(track: AudioTrack, volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            track.setVolume(clamped)
        } else {
            @Suppress("DEPRECATION")
            track.setStereoVolume(clamped, clamped)
        }
    }

    @Synchronized
    fun playBeep() {
        val track = audioTrack ?: return
        try {
            if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                track.stop()
            }
            track.reloadStaticData()
            track.play()
        } catch (_: Exception) {
            // Audio errors handled gracefully
        }
    }

    @Synchronized
    fun stop() {
        try {
            audioTrack?.let {
                if (it.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    it.stop()
                }
            }
        } catch (_: Exception) {
        }
    }

    @Synchronized
    fun release() {
        try {
            audioTrack?.let {
                if (it.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    it.stop()
                }
                it.release()
            }
        } catch (_: Exception) {
        } finally {
            audioTrack = null
            pcmBuffer = null
            isInitialized = false
        }
    }
}

