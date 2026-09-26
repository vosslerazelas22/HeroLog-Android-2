package com.iurispraecepta.herolog.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.iurispraecepta.herolog.data.dao.ActiveFocusSessionDao
import com.iurispraecepta.herolog.data.dao.CharacterStateDao
import com.iurispraecepta.herolog.data.dao.PendingRewardCelebrationDao
import com.iurispraecepta.herolog.data.entity.ActiveFocusSessionEntity
import com.iurispraecepta.herolog.data.entity.CharacterStateEntity
import com.iurispraecepta.herolog.data.entity.PendingRewardCelebrationEntity

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `active_focus_session` (
                `id` INTEGER NOT NULL,
                `jsonPayload` TEXT NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `pending_reward_celebrations` (
                `id` TEXT NOT NULL,
                `sessionCompletedAt` INTEGER NOT NULL,
                `skillName` TEXT NOT NULL,
                `durationMinutes` INTEGER NOT NULL,
                `xpGained` INTEGER NOT NULL,
                `goldGained` INTEGER NOT NULL,
                `lootedItems` TEXT NOT NULL,
                `droppedTitle` TEXT,
                `leveledUp` INTEGER NOT NULL,
                `previousLevel` INTEGER,
                `newLevel` INTEGER,
                `achievementsUnlocked` TEXT NOT NULL,
                `consumed` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
    }
}

@Database(
    entities = [CharacterStateEntity::class, ActiveFocusSessionEntity::class, PendingRewardCelebrationEntity::class],
    version = 3,
    exportSchema = true
)
abstract class HeroLogDatabase : RoomDatabase() {
    abstract fun characterStateDao(): CharacterStateDao
    abstract fun activeFocusSessionDao(): ActiveFocusSessionDao
    abstract fun pendingRewardCelebrationDao(): PendingRewardCelebrationDao
}

