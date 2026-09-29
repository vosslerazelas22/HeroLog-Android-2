package com.iurispraecepta.herolog

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.iurispraecepta.herolog.data.database.MIGRATION_3_4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Teste da migration 3→4 (plano-focus-completion-flow, decisão 1 + gap historyId):
 * executa o SQL real da migration sobre um schema v3 e confere colunas, defaults e
 * preservação das linhas pré-existentes.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PendingRewardMigrationTest {

    private fun createV3Database(): SupportSQLiteDatabase {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(null)
            .callback(object : SupportSQLiteOpenHelper.Callback(3) {
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
    fun migration_endpoints_are3To4() {
        assertEquals(3, MIGRATION_3_4.startVersion)
        assertEquals(4, MIGRATION_3_4.endVersion)
    }

    @Test
    fun migrate3To4_addsFourColumnsWithDefaultsAndPreservesRows() {
        val db = createV3Database()
        db.execSQL(
            "INSERT INTO `pending_reward_celebrations` " +
                "(`id`, `sessionCompletedAt`, `skillName`, `durationMinutes`, `xpGained`, " +
                "`goldGained`, `lootedItems`, `droppedTitle`, `leveledUp`, `previousLevel`, " +
                "`newLevel`, `achievementsUnlocked`, `consumed`) VALUES " +
                "('s1', 1000, 'Kotlin', 25, 60, 90, '[]', NULL, 0, NULL, NULL, '[]', 0)"
        )

        MIGRATION_3_4.migrate(db)

        val columns = columnNames(db)
        assertTrue(columns.contains("streakAfter"))
        assertTrue(columns.contains("showStreakCelebration"))
        assertTrue(columns.contains("pauseCount"))
        assertTrue(columns.contains("historyId"))

        db.query(
            "SELECT `skillName`, `xpGained`, `streakAfter`, `showStreakCelebration`, " +
                "`pauseCount`, `historyId` FROM `pending_reward_celebrations` WHERE `id` = 's1'"
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("Kotlin", cursor.getString(cursor.getColumnIndex("skillName")))
            assertEquals(60, cursor.getInt(cursor.getColumnIndex("xpGained")))
            assertEquals(0, cursor.getInt(cursor.getColumnIndex("streakAfter")))
            assertEquals(0, cursor.getInt(cursor.getColumnIndex("showStreakCelebration")))
            assertEquals(0, cursor.getInt(cursor.getColumnIndex("pauseCount")))
            assertTrue(cursor.isNull(cursor.getColumnIndex("historyId")))
        }

        // Linha nova com os 4 campos grava e lê de volta.
        db.execSQL(
            "INSERT INTO `pending_reward_celebrations` " +
                "(`id`, `sessionCompletedAt`, `skillName`, `durationMinutes`, `xpGained`, " +
                "`goldGained`, `lootedItems`, `droppedTitle`, `leveledUp`, `previousLevel`, " +
                "`newLevel`, `achievementsUnlocked`, `consumed`, `streakAfter`, " +
                "`showStreakCelebration`, `pauseCount`, `historyId`) VALUES " +
                "('s2', 2000, 'Kotlin', 25, 60, 90, '[]', NULL, 0, NULL, NULL, '[]', 0, " +
                "5, 1, 2, 'h-1')"
        )
        db.query(
            "SELECT `streakAfter`, `showStreakCelebration`, `pauseCount`, `historyId` " +
                "FROM `pending_reward_celebrations` WHERE `id` = 's2'"
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(5, cursor.getInt(cursor.getColumnIndex("streakAfter")))
            assertEquals(1, cursor.getInt(cursor.getColumnIndex("showStreakCelebration")))
            assertEquals(2, cursor.getInt(cursor.getColumnIndex("pauseCount")))
            assertEquals("h-1", cursor.getString(cursor.getColumnIndex("historyId")))
        }
        db.close()
    }

    @Test
    fun migrate3To4_newColumnsAcceptNullHistoryId() {
        val db = createV3Database()

        MIGRATION_3_4.migrate(db)

        // historyId é TEXT nullable — insert sem ela não quebra.
        db.execSQL(
            "INSERT INTO `pending_reward_celebrations` " +
                "(`id`, `sessionCompletedAt`, `skillName`, `durationMinutes`, `xpGained`, " +
                "`goldGained`, `lootedItems`, `droppedTitle`, `leveledUp`, `previousLevel`, " +
                "`newLevel`, `achievementsUnlocked`, `consumed`) VALUES " +
                "('s9', 9000, 'Kotlin', 25, 60, 90, '[]', NULL, 0, NULL, NULL, '[]', 0)"
        )
        db.query(
            "SELECT `historyId` FROM `pending_reward_celebrations` WHERE `id` = 's9'"
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertNull(cursor.getString(cursor.getColumnIndex("historyId")))
        }
        db.close()
    }
}
