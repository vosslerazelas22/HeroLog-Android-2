package com.iurispraecepta.herolog.logic.character

import com.iurispraecepta.herolog.model.CharacterState

/**
 * Porte de `useLevelUp` (`src/modules/character/useLevelUp.ts`) — hook que detecta subidas de
 * nível de combate e de skills comparando o `gameState` anterior com o atual, pra disparar o
 * popup de "LEVEL UP!"/"MAESTRIA APRIMORADA".
 *
 * Auditado direto contra `useLevelUp.ts` real (clone do repo, HEAD `9fa5380` — mesmo hash já
 * conferido pro `useGameState.ts` durante a auditoria do import de save; sem drift).
 *
 * **Diferença estrutural deliberada da fonte**: a fonte usa `useRef` pra manter
 * `lastKnownCombatLevelRef`/`lastKnownSkillLevelsRef` persistentes entre renders e um `useEffect`
 * reagindo a mudanças de `gameState`. No Android não há "render" equivalente — em vez disso, esta
 * função pura compara o `CharacterState` anterior (valor de `_characterState` antes da chamada a
 * `saveCharacterState`) contra o novo estado sendo salvo, no próprio funil único de mutação.
 * Funcionalmente equivalente: mesma condição de disparo (`novo > antigo && antigo > 0`), mesma
 * correspondência de skill por `name` (não por índice/id — igual à fonte).
 *
 * **Skill nova (sem nível anterior conhecido)**: nunca dispara level up na primeira aparição —
 * mesmo comportamento da fonte (`prevSkillLvl !== undefined` guard).
 *
 * **Supressão durante import/reset**: a fonte usa `isImportingRef` pra pular a detecção logo após
 * um `importGameState`/`resetGameState`/restauração de save. Esta função pura não decide isso
 * sozinha — quem decide se compara ou não é o chamador (`HeroLogViewModel`).
 *
 * **`charClass` do evento `Combat`** (campo existente em `CombatLevelUpType` na fonte, mas nunca
 * lido no JSX do modal de level-up — só `charName` aparece no texto renderizado): a fonte usa
 * `gameState.charClass || 'Guerreiro'`, fallback hardcoded em português que na prática nunca
 * dispara (`charClass` real da fonte é sempre `'Mage'|'Warrior'|'Ranger'`, nunca falsy — campo
 * morto/bug latente da fonte). Como no Android `charClass` é enum não-nulo, mapeamos direto pro
 * nome serializado (`CharClass.name`), preservando o campo pra paridade de tipo mesmo sem uso
 * visual atual. **Sinalizado pra confirmação do Bruno**: manter assim ou remover, já que é campo
 * morto também na fonte?
 *
 * @param previousState estado antes da mutação sendo aplicada.
 * @param newState estado depois da mutação sendo aplicada.
 * @return eventos detectados, na ordem: combate primeiro, depois skills na ordem de
 *   `newState.skills`.
 */
object LevelUpLogic {

    fun detectLevelUps(previousState: CharacterState, newState: CharacterState): List<LevelUpEvent> {
        val events = mutableListOf<LevelUpEvent>()

        val prevCombat = previousState.combatLevel
        val currentCombat = newState.combatLevel
        if (currentCombat > prevCombat && prevCombat > 0) {
            events.add(
                LevelUpEvent.Combat(
                    oldLevel = prevCombat,
                    newLevel = currentCombat,
                    charName = newState.charName.ifBlank { "Aventureiro" },
                    charClass = newState.charClass.name
                )
            )
        }

        val prevSkillLevels = previousState.skills.associate { it.name to it.level }
        newState.skills.forEach { skill ->
            val prevLevel = prevSkillLevels[skill.name]
            if (prevLevel != null && skill.level > prevLevel && prevLevel > 0) {
                events.add(
                    LevelUpEvent.Skill(
                        skillName = skill.name,
                        emoji = skill.emoji ?: "\uD83C\uDFAF",
                        oldLevel = prevLevel,
                        newLevel = skill.level
                    )
                )
            }
        }

        return events
    }
}

/** Porte de `LevelUpModalType` (`src/types.ts`) — união discriminada de eventos de level up. */
sealed class LevelUpEvent {
    data class Combat(
        val oldLevel: Int,
        val newLevel: Int,
        val charName: String,
        val charClass: String
    ) : LevelUpEvent()

    data class Skill(
        val skillName: String,
        val emoji: String,
        val oldLevel: Int,
        val newLevel: Int
    ) : LevelUpEvent()
}
