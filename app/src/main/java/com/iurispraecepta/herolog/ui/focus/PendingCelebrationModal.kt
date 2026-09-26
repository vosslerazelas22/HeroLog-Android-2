package com.iurispraecepta.herolog.ui.focus

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iurispraecepta.herolog.data.JsonConfig
import com.iurispraecepta.herolog.logic.achievements.Achievement
import com.iurispraecepta.herolog.logic.achievements.AchievementCatalog
import com.iurispraecepta.herolog.logic.achievements.AchievementCatalog.ACHIEVEMENTS_LIST
import com.iurispraecepta.herolog.logic.focus.AggregatedCelebrationSummary
import com.iurispraecepta.herolog.logic.focus.DroppedTitle
import com.iurispraecepta.herolog.logic.focus.FocusRewardsCalculation
import com.iurispraecepta.herolog.logic.focus.LootItem
import com.iurispraecepta.herolog.data.entity.PendingRewardCelebrationEntity
import com.iurispraecepta.herolog.model.Rarity
import com.iurispraecepta.herolog.ui.components.HeroLogModal
import com.iurispraecepta.herolog.ui.theme.Amber500
import com.iurispraecepta.herolog.ui.theme.Champagne400
import com.iurispraecepta.herolog.ui.theme.Cinzel
import com.iurispraecepta.herolog.ui.theme.JetBrainsMono
import com.iurispraecepta.herolog.ui.theme.QuestPanel
import com.iurispraecepta.herolog.ui.theme.Stone950

/**
 * Modal de celebração pendente (spec-008, T11).
 *
 * Roteamento:
 * - sessionCount == 1: reusa FocusCompletionFlow existente (single session)
 * - sessionCount >= 2: MultipleSessionCelebration (agregado)
 *
 * O botão "Continuar" / "Receber Recompensas" chama onDismiss que consome
 * a fila atomicamente via viewModel.confirmPendingCelebration().
 */
@Composable
fun PendingCelebrationModal(
    summary: AggregatedCelebrationSummary,
    onDismiss: () -> Unit,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier
) {
    if (summary.sessionCount == 1) {
        // Single session: reusa FocusCompletionFlow existente
        val session = summary.sessions.first()
        val rewardsCalculation = rebuildRewardsCalculation(session)
        val skillName = session.skillName
        val pauseCount = 0 // entity não guarda pauseCount; 0 é seguro
        val streak = 0 // será calculado pelo Flow se shouldShowStreakCelebration=true
        val shouldShowStreakCelebration = false // conservative default

        // Skills tags from the skill
        val skillTags = emptyList<String>() // entity não guarda tags; safe default

        // Achievements: decode IDs -> Achievement objects
        val newAchievements = runCatching {
            JsonConfig.default.decodeFromString<List<String>>(session.achievementsUnlocked)
        }.getOrDefault(emptyList()).mapNotNull { id ->
            ACHIEVEMENTS_LIST.firstOrNull { it.id == id }
        }

        HeroLogModal(
            isOpen = true,
            onClose = onDismiss,
            title = "Sessão Concluída",
            variant = com.iurispraecepta.herolog.ui.components.ModalVariant.Amber
        ) {
            FocusCompletionFlow(
                rewardsCalculation = rewardsCalculation,
                pauseCount = pauseCount,
                streak = streak,
                shouldShowStreakCelebration = shouldShowStreakCelebration,
                skillTags = skillTags,
                newAchievements = newAchievements,
                initialNotes = "",
                onConfirm = { _: String, _: String -> onDismiss() },
                insideModal = true,
                modifier = Modifier.fillMaxSize()
            )
        }
    } else {
        // Multiple sessions: aggregated view
        HeroLogModal(
            isOpen = true,
            onClose = onDismiss,
            title = "${summary.sessionCount} Sessões Concluídas",
            variant = com.iurispraecepta.herolog.ui.components.ModalVariant.Amber
        ) {
            MultipleSessionCelebration(
                summary = summary,
                onDismiss = onDismiss
            )
        }
    }
}

/**
 * Reconstrói FocusRewardsCalculation a partir de PendingRewardCelebrationEntity.
 * Usa os valores persistidos (sem recalcular) — anti-reroll.
 */
private fun rebuildRewardsCalculation(entity: PendingRewardCelebrationEntity): FocusRewardsCalculation {
    val lootedItems = runCatching { JsonConfig.default.decodeFromString<List<LootItem>>(entity.lootedItems) }
        .getOrDefault(emptyList())

    val droppedTitle = entity.droppedTitle?.let { 
        runCatching { JsonConfig.default.decodeFromString<DroppedTitle>(it) }.getOrNull() 
    }

    return FocusRewardsCalculation(
        skillIdx = 0, // não persistido; 0 é placeholder
        skillName = entity.skillName,
        xpEarned = entity.xpGained,
        goldEarned = entity.goldGained,
        durationMins = entity.durationMinutes,
        dungeonClearGoldBonus = 0, // entity não guarda; 0 conservador
        hasUsedDoubleLoot = false,
        hasUsedFocusElixir = false,
        hasUsedRuneFortune = false,
        hasUsedCrystalClarity = false,
        usedEquipmentIndicesAndCharges = emptyList(),
        lootedItems = lootedItems,
        droppedTitle = droppedTitle,
        isWildernessChecked = false,
        isDungeonMode = false,
        comboBonusPercent = 0
    )
}