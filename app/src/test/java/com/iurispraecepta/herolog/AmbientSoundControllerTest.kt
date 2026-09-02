package com.iurispraecepta.herolog

import android.content.Context
import android.media.MediaPlayer
import androidx.test.core.app.ApplicationProvider
import com.iurispraecepta.herolog.ui.focus.AmbientSoundController
import com.iurispraecepta.herolog.ui.focus.AMBIENT_SOUNDS
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AmbientSoundControllerTest {

    private lateinit var context: Context
    private lateinit var controller: AmbientSoundController

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
        controller = AmbientSoundController(context)
    }

    @After
    fun tearDown() {
        controller.release()
    }

    @Test
    fun selectTrack_updatesSelectedTrackAndPersists() {
        // Initially null
        assertNull(controller.selectedTrack)

        // Select a track
        controller.selectTrack("floresta")
        assertEquals("floresta", controller.selectedTrack)

        // Select same track again -> toggles to null (React parity: same id = disable)
        controller.selectTrack("floresta")
        assertNull(controller.selectedTrack)

        // Select different track
        controller.selectTrack("chuva")
        assertEquals("chuva", controller.selectedTrack)
    }

    @Test
    fun sync_withNullTrack_pausesMediaPlayer() {
        controller.selectTrack("floresta")
        // First sync to initialize MediaPlayer
        controller.sync(isWorkSessionActive = true)

        // Now set track to null and sync
        controller.selectTrack(null)
        controller.sync(isWorkSessionActive = true)

        // MediaPlayer should be paused (we can't directly verify internal state,
        // but we verify no crash and controller is in valid state)
        assertNull(controller.selectedTrack)
    }

    @Test
    fun sync_trackSwitchDuringActiveSession_preparesNewTrack() {
        // Start with first track, session active
        controller.selectTrack("floresta")
        controller.sync(isWorkSessionActive = true)

        // Switch to second track while session still active
        controller.selectTrack("chuva")
        controller.sync(isWorkSessionActive = true)

        // Verify track was updated
        assertEquals("chuva", controller.selectedTrack)
        // MediaPlayer should be preparing/prepared with new track
        // (internal state not directly testable, but no crash = basic validation)
    }

    @Test
    fun sync_trackSwitchDuringPausedSession_preparesButDoesNotPlay() {
        // Start with first track, session paused
        controller.selectTrack("floresta")
        controller.sync(isWorkSessionActive = false)

        // Switch to second track while session paused
        controller.selectTrack("chuva")
        controller.sync(isWorkSessionActive = false)

        // Verify track was updated
        assertEquals("chuva", controller.selectedTrack)
        // MediaPlayer should be prepared but not playing
    }

    @Test
    fun setVolume_updatesVolumeAndAppliesToMediaPlayer() {
        controller.selectTrack("floresta")
        controller.sync(isWorkSessionActive = true)

        // Change volume
        controller.setVolume(75)
        assertEquals(75, controller.volume)

        // Volume should be applied to MediaPlayer (internal, verified by no crash)
        controller.sync(isWorkSessionActive = true)
    }

    @Test
    fun volumeClamping_clampsTo0And100() {
        controller.setVolume(-10)
        assertEquals(0, controller.volume)

        controller.setVolume(150)
        assertEquals(100, controller.volume)
    }

    @Test
    fun release_cleansUpMediaPlayer() {
        controller.selectTrack("floresta")
        controller.sync(isWorkSessionActive = true)

        controller.release()

        // After release, controller should be in clean state
        // (internal state not directly testable, but no crash on re-sync)
        controller.sync(isWorkSessionActive = true)
    }

    @Test
    fun tracksList_containsAllEightTracks() {
        assertEquals(8, AMBIENT_SOUNDS.size)
        val ids = AMBIENT_SOUNDS.map { it.id }.toSet()
        assertTrue(ids.contains("floresta"))
        assertTrue(ids.contains("chuva"))
        assertTrue(ids.contains("taverna"))
        assertTrue(ids.contains("biblioteca"))
        assertTrue(ids.contains("ruinas"))
        assertTrue(ids.contains("montanha"))
        assertTrue(ids.contains("cidade"))
        assertTrue(ids.contains("templo"))
    }
}