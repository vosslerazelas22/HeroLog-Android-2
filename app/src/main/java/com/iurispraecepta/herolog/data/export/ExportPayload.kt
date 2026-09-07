package com.iurispraecepta.herolog.data.export

import com.iurispraecepta.herolog.logic.focus.PersistedFocusSession
import com.iurispraecepta.herolog.model.CharacterState
import kotlinx.serialization.Serializable

/**
 * Wrapper versionado do save exportado.
 *
 * Inspirado em como o app React trata o save em `localStorage`: ele guarda
 * o `CharacterState` cru, mas este app precisa carregar a sessão de foco
 * ativa junto — daí o `activeFocus` opcional. O [version] dá espaço pra
 * migrations futuras (se um dia o `CharacterState` tiver mudanças
 * incompatíveis, basta bumpar e tratar em [GameStateImporter]).
 *
 * `exportedAt` é ISO-8601 (`Instant.toString()`) — registro de auditoria,
 * não usado pelo import.
 *
 * Mantido em `data.export` (não `model`) porque é específico do feature de
 * backup/restore do Android, não faz parte do estado de jogo persistido em
 * Room.
 */
@Serializable
data class ExportPayload(
    val version: Int = EXPORT_VERSION,
    val exportedAt: String,
    val character: CharacterState,
    val activeFocus: PersistedFocusSession? = null
) {
    companion object {
        const val EXPORT_VERSION: Int = 1
    }
}
