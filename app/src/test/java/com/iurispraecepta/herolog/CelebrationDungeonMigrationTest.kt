package com.iurispraecepta.herolog

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.iurispraecepta.herolog.data.database.MIGRATION_4_5
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Teste da migration 4→5 (OQ-2, spec-tema-por-modo-foco): adiciona
 * `dungeonSessionsCompleted` (INTEGER NOT NULL DEFAULT 0) preservando linhas.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CelebrationDungeonMigrationTest {

    private fun createV4Database(): SupportSQLiteDatabase {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(null)
            .callback(object : SupportSQLiteOpenHelper.Callback(4) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                        CREATE TABLE `pending_reward_celebrations` (
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
                            `streakAfter` INTEGER NOT NULL DEFAULT 0,
                            `showStreakCelebration` INTEGER NOT NULL DEFAULT 0,
                            `pauseCount` INTEGER NOT NULL DEFAULT 0,
                            `historyId` TEXT,
                            PRIMARY KEY(`id`)
                        )
                        """.trimIndent()
                    )
                }

                override fun onUpgrade(
                    db: SupportSQLiteDatabase,
                    oldVersion: Int,
                    newVersion: Int
                ) {
                }
            })
            .build()
        return FrameworkSQLiteOpenHelperFactory().create(config).writableDatabase
    }

    private fun columnNames(db: SupportSQLiteDatabase): List<String> {
        val names = mutableListOf<String>()
        db.query("PRAGMA table_info(`pending_reward_celebrations`)").use { cursor ->
            val nameIdx = cursor.getColumnIndex("name")
            while (cursor.moveToNext()) {
                names.add(cursor.getString(nameIdx))
            }
        }
        return names
    }

    @Test
    fun migration_endpoints_are4To5() {
        assertEquals(4, MIGRATION_4_5.startVersion)
        assertEquals(5, MIGRATION_4_5.endVersion)
    }

    @Test
    fun migrate4To5_addsDungeonColumnWithDefaultZeroAndPreservesRows() {
        val db = createV4Database()
        db.execSQL(
            "INSERT INTO `pending_reward_celebrations` " +
                "(`id`, `sessionCompletedAt`, `skillName`, `durationMinutes`, `xpGained`, " +
                "`goldGained`, `lootedItems`, `droppedTitle`, `leveledUp`, `previousLevel`, " +
                "`newLevel`, `achievementsUnlocked`, `consumed`, `streakAfter`, " +
                "`showStreakCelebration`, `pauseCount`, `historyId`) VALUES " +
                "('s1', 1000, 'Kotlin', 25, 60, 90, '[]', NULL, 0, NULL, NULL, '[]', 0, " +
                "5, 1, 2, 'h-1')"
        )

        MIGRATION_4_5.migrate(db)

        assertTrue(columnNames(db).contains("dungeonSessionsCompleted"))

        // Linha pré-existente preservada, coluna nova com default 0.
        db.query(
            "SELECT `skillName`, `streakAfter`, `dungeonSessionsCompleted` " +
                "FROM `pending_reward_celebrations` WHERE `id` = 's1'"
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("Kotlin", cursor.getString(cursor.getColumnIndex("skillName")))
            assertEquals(5, cursor.getInt(cursor.getColumnIndex("streakAfter")))
            assertEquals(0, cursor.getInt(cursor.getColumnIndex("dungeonSessionsCompleted")))
        }

        // Linha nova com o índice da 4ª sessão grava e lê de volta.
        db.execSQL(
            "INSERT INTO `pending_reward_celebrations` " +
                "(`id`, `sessionCompletedAt`, `skillName`, `durationMinutes`, `xpGained`, " +
                "`goldGained`, `lootedItems`, `droppedTitle`, `leveledUp`, `previousLevel`, " +
                "`newLevel`, `achievementsUnlocked`, `consumed`, `dungeonSessionsCompleted`) VALUES " +
                "('s2', 2000, 'Kotlin', 25, 60, 2590, '[]', NULL, 0, NULL, NULL, '[]', 0, 4)"
        )
        db.query(
            "SELECT `dungeonSessionsCompleted` FROM `pending_reward_celebrations` WHERE `id` = 's2'"
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(4, cursor.getInt(cursor.getColumnIndex("dungeonSessionsCompleted")))
        }
        db.close()
    }
}
