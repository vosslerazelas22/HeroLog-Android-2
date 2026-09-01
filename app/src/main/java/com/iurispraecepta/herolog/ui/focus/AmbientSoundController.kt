package com.iurispraecepta.herolog.ui.focus

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
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
    private var pendingPlayOnPrepared = false

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
            // Troca de trilha em tempo real: reutiliza o MediaPlayer via setDataSource + prepareAsync
            // (paridade com React: audio.src = newSrc; audio.load())
            val uri = Uri.parse("android.resource://${context.packageName}/${track.rawRes}")
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer().apply {
                    setOnPreparedListener { mp ->
                        mp.isLooping = true
                        mp.setVolume(volume / 100f, volume / 100f)
                        if (pendingPlayOnPrepared) {
                            mp.start()
                            pendingPlayOnPrepared = false
                        }
                    }
                    setOnErrorListener { _, _, _ ->
                        pendingPlayOnPrepared = false
                        true
                    }
                }
            }
            try {
                mediaPlayer?.reset()
                mediaPlayer?.setDataSource(context, uri)
                currentPlayingTrackId = trackId
                if (isWorkSessionActive) {
                    pendingPlayOnPrepared = true
                    mediaPlayer?.prepareAsync()
                } else {
                    mediaPlayer?.prepareAsync()
                }
            } catch (e: Exception) {
                // Em caso de erro, limpa e tenta recriar na próxima sync
                mediaPlayer?.release()
                mediaPlayer = null
                currentPlayingTrackId = null
            }
        } else {
            // Mesma trilha: só controla play/pause conforme isWorkSessionActive
            if (isWorkSessionActive) {
                mediaPlayer?.start()
            } else {
                mediaPlayer?.pause()
            }
        }
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
        currentPlayingTrackId = null
        pendingPlayOnPrepared = false
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
