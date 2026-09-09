package com.iurispraecepta.herolog.ui.sfx

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.iurispraecepta.herolog.R

class SfxManager internal constructor(private val soundPool: SoundPool?) {

    private val soundIds = mutableMapOf<SfxType, Int>()

    var isMuted: Boolean by mutableStateOf(false)
        internal set

    fun toggleMute() {
        isMuted = !isMuted
    }

    fun play(type: SfxType) {
        if (isMuted) return
        val pool = soundPool ?: return
        val id = soundIds[type] ?: return
        pool.play(id, 1.0f, 1.0f, 1, 0, 1.0f)
    }

    fun playFocusBell() = play(SfxType.FOCUS_BELL)
    fun playLevelUp() = play(SfxType.LEVEL_UP)
    fun playCoins() = play(SfxType.COINS)
    fun playDeath() = play(SfxType.DEATH)
    fun playWildernessWarning() = play(SfxType.WILDERNESS_WARNING)
    fun playClick() = play(SfxType.CLICK)

    fun release() {
        soundPool?.release()
    }

    companion object {
        fun create(context: Context): SfxManager {
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val pool = SoundPool.Builder()
                .setMaxStreams(6)
                .setAudioAttributes(attrs)
                .build()

            val manager = SfxManager(pool)
            manager.soundIds[SfxType.FOCUS_BELL] = pool.load(context, R.raw.sfx_focus_bell, 1)
            manager.soundIds[SfxType.LEVEL_UP] = pool.load(context, R.raw.sfx_level_up, 1)
            manager.soundIds[SfxType.COINS] = pool.load(context, R.raw.sfx_coins, 1)
            manager.soundIds[SfxType.DEATH] = pool.load(context, R.raw.sfx_death, 1)
            manager.soundIds[SfxType.WILDERNESS_WARNING] = pool.load(context, R.raw.sfx_wilderness_warning, 1)
            manager.soundIds[SfxType.CLICK] = pool.load(context, R.raw.sfx_click, 1)
            return manager
        }

        fun noOp() = SfxManager(null)
    }
}

enum class SfxType {
    FOCUS_BELL,
    LEVEL_UP,
    COINS,
    DEATH,
    WILDERNESS_WARNING,
    CLICK
}

@Composable
fun rememberSfxManager(): SfxManager {
    val context = LocalContext.current
    val manager = remember { SfxManager.create(context.applicationContext) }
    DisposableEffect(Unit) {
        onDispose { manager.release() }
    }
    return manager
}

val LocalSfxManager = staticCompositionLocalOf<SfxManager> {
    error("No SfxManager provided")
}
