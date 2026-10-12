package com.iurispraecepta.herolog.data.repository

import com.iurispraecepta.herolog.data.JsonConfig
import com.iurispraecepta.herolog.data.dao.CharacterStateDao
import com.iurispraecepta.herolog.data.entity.CharacterStateEntity
import com.iurispraecepta.herolog.logic.SaveMigrationLogic
import com.iurispraecepta.herolog.model.CharacterState
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class CharacterRepository(
    private val characterStateDao: CharacterStateDao,
    private val json: Json = JsonConfig.default
) {
    suspend fun getCharacterState(): CharacterState? {
        val entity = characterStateDao.getState() ?: return null
        return runCatching {
            // Migração de saves antigos de hábito (SPEC-Rebranding item 4) no único
            // ponto de decodificação do blob: sem Migration Room, porque o payload é
            // JSON com ignoreUnknownKeys (ver SaveMigrationLogic.migrateHabitDates).
            SaveMigrationLogic.migrateHabitDates(
                json.decodeFromString<CharacterState>(entity.jsonPayload)
            )
        }.getOrNull()
    }

    suspend fun saveCharacterState(state: CharacterState) {
        val jsonPayload = json.encodeToString(state)
        val entity = CharacterStateEntity(
            id = 0,
            jsonPayload = jsonPayload,
            updatedAt = System.currentTimeMillis()
        )
        characterStateDao.saveState(entity)
    }

    suspend fun clearCharacterState() {
        characterStateDao.clearState()
    }
}
