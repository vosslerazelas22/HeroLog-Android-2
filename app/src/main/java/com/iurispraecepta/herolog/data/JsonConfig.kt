package com.iurispraecepta.herolog.data

import kotlinx.serialization.json.Json

/**
 * Configurações de `Json` compartilhadas pelo app.
 *
 * Centraliza a criação de instâncias de `Json` pra que `CharacterRepository`,
 * `FocusSessionRepository` e os fluxos de export/import de save usem configs
 * consistentes — antes desta consolidação, os 2 repositories tinham configs
 * idênticas mas duplicadas localmente, e o export/import teria que criar a sua
 * própria (risco de divergência silenciosa com o que está no banco).
 *
 * - [default]: o `Json` usado pra serializar pro Room — não prettifica pra
 *   economizar espaço no blob, ignora chaves desconhecidas (import resiliente).
 * - [pretty]: usado pelo export pra gerar arquivo legível. Liga `encodeDefaults`
 *   pra que o JSON tenha todos os campos do `CharacterState` (mesmo os com
 *   valor default), deixando o import robusto a campos novos futuros.
 */
object JsonConfig {

    val default: Json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    val pretty: Json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        encodeDefaults = true
    }
}
