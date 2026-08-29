package com.iurispraecepta.herolog.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.iurispraecepta.herolog.data.dao.ActiveFocusSessionDao
import com.iurispraecepta.herolog.data.dao.CharacterStateDao
import com.iurispraecepta.herolog.data.entity.ActiveFocusSessionEntity
import com.iurispraecepta.herolog.data.entity.CharacterStateEntity

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

@Database(entities = [CharacterStateEntity::class, ActiveFocusSessionEntity::class], version = 2, exportSchema = true)
abstract class HeroLogDatabase : RoomDatabase() {
    abstract fun characterStateDao(): CharacterStateDao
    abstract fun activeFocusSessionDao(): ActiveFocusSessionDao
}

