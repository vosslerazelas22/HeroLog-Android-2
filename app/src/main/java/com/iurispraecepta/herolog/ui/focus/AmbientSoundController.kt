package com.iurispraecepta.herolog.ui.focus

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

private const val PREFS_NAME = "herolog_ambient_sound"
private const val KEY_TRACK = "track"
private const val KEY_VOLUME = "volume"

class AmbientSoundController(private val context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private var mediaPlayer: MediaPlayer? = null
    private var currentPlayingTrackId: String? = null

    var selectedTrack: String? by mutableStateOf(prefs.getString(KEY_TRACK, null))
        private set
    private var _volume by mutableIntStateOf(prefs.getInt(KEY_VOLUME, 50))
    val volume: Int get() = _volume

    fun setVolume(v: Int) {
        val clamped = v.coerceIn(0, 100)
        _volume = clamped
        prefs.edit().putInt(KEY_VOLUME, clamped).apply()
        mediaPlayer?.setVolume(clamped / 100f, clamped / 100f)
    }

    // Réplica do toggle (mesmo id = desliga) da fonte real.
    fun selectTrack(id: String?) {
        val next = if (selectedTrack == id) null else id
        selectedTrack = next
        if (next != null) {
            prefs.edit().putString(KEY_TRACK, next).apply()
        } else {
            prefs.edit().remove(KEY_TRACK).apply()
        }
    }

    // Chamado a cada mudança de selectedTrack OU de isWorkSessionActive
    fun sync(isWorkSessionActive: Boolean) {
        val trackId = selectedTrack
        if (trackId == null) {
            mediaPlayer?.pause()
            return
        }

        val track = AMBIENT_SOUNDS.find { it.id == trackId } ?: return

        if (currentPlayingTrackId != trackId) {
            mediaPlayer?.release()
            mediaPlayer = null
            currentPlayingTrackId = null
        }

        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, track.rawRes)?.apply {
                isLooping = true
                setVolume(volume / 100f, volume / 100f)
            }
            currentPlayingTrackId = trackId
        }

        if (isWorkSessionActive) {
            mediaPlayer?.start()
        } else {
            mediaPlayer?.pause()
        }
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
        currentPlayingTrackId = null
    }
}

@Composable
fun rememberAmbientSoundController(): AmbientSoundController {
    val context = LocalContext.current
    val controller = remember { AmbientSoundController(context.applicationContext) }
    DisposableEffect(Unit) {
        onDispose { controller.release() }
    }
    return controller
}
