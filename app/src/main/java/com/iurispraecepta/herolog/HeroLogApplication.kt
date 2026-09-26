package com.iurispraecepta.herolog

import android.app.Application
import androidx.room.Room
import com.iurispraecepta.herolog.data.database.HeroLogDatabase
import com.iurispraecepta.herolog.data.database.MIGRATION_1_2
import com.iurispraecepta.herolog.data.database.MIGRATION_2_3
import com.iurispraecepta.herolog.data.repository.CharacterRepository
import com.iurispraecepta.herolog.data.repository.FocusSessionRepository
import com.iurispraecepta.herolog.data.repository.PendingRewardRepository

class HeroLogApplication : Application() {
    val database: HeroLogDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            HeroLogDatabase::class.java,
            "herolog.db"
        ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build()
    }

    val characterRepository: CharacterRepository by lazy {
        CharacterRepository(database.characterStateDao())
    }

    val focusSessionRepository: FocusSessionRepository by lazy {
        FocusSessionRepository(database.activeFocusSessionDao())
    }

    val pendingRewardRepository: PendingRewardRepository by lazy {
        PendingRewardRepository(database.pendingRewardCelebrationDao())
    }
}
