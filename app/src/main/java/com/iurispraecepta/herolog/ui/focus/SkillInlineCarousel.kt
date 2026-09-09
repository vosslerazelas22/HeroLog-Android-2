package com.iurispraecepta.herolog.ui.focus

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.R
import com.iurispraecepta.herolog.logic.SkillCarouselLogic
import com.iurispraecepta.herolog.model.Skill
import com.iurispraecepta.herolog.ui.sfx.LocalSfxManager

/**
 * Port fiel de `src/components/SkillInlineCarousel.tsx` (React).
 *
 * Troca rápida de skill ativa direto no fluxo de Foco, sem abrir modal — distinto de
 * `SkillSelectorModal` (gerenciamento via modal). Ver `PARIDADE.md` seção 3 (Módulo Foco).
 *
 * Lógica de índice/swipe delegada a [SkillCarouselLogic] (testada isoladamente, 14 testes).
 *
 * **Divergências deliberadas da fonte**, mesmo padrão já registrado em outros componentes de Foco:
 * - SFX (`sound.playClick()`) adicionado via `LocalSfxManager`.
 * - Atalho de teclado (←/→) N/A — plataforma touch, sem teclado físico por padrão.
 * - Cores hardcoded localmente (`#e5c158`/`#1c1917` etc), mesmo padrão do `AppHeader.kt` —
 *   `Color.kt` ainda não tem token `Champagne*` neste commit.
 */
@Composable
fun SkillInlineCarousel(
    skills: List<Skill>,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit,
    disabled: Boolean = false,
    onOpenSkillsManager: (() -> Unit)? = null,
) {
    val champagne = Color(0xFFE5C158)
    val cardBg = Color(0xFF1C1917)
    val sfxManager = LocalSfxManager.current

    if (skills.isEmpty()) {
        OutlinedButton(
            onClick = {
                sfxManager?.playClick()
                onOpenSkillsManager?.invoke()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.lucide_ic_plus),
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = champagne
            )
            Text(
                text = "Adicione sua primeira Habilidade de Foco",
                color = champagne,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 6.dp),
            )
        }
        return
    }

    val activeIdx = SkillCarouselLogic.clampIndex(selectedIndex, skills.size)
    val currentSkill = skills[activeIdx]
    val canNavigate = SkillCarouselLogic.canNavigate(skills.size) && !disabled

    var dragTotalX by remember { mutableFloatStateOf(0f) }
    var dragTotalY by remember { mutableFloatStateOf(0f) }
    // direção só afeta a animação de entrada/saída, não a lógica de índice
    var direction by remember { mutableFloatStateOf(0f) }

    fun goPrev() {
        if (!canNavigate) return
        direction = -1f
        onSelectIndex(SkillCarouselLogic.prevIndex(activeIdx, skills.size))
    }

    fun goNext() {
        if (!canNavigate) return
        direction = 1f
        onSelectIndex(SkillCarouselLogic.nextIndex(activeIdx, skills.size))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(canNavigate) {
                if (!canNavigate) return@pointerInput
                detectHorizontalDragGestures(
                    onDragStart = { dragTotalX = 0f },
                    onHorizontalDrag = { change, amount ->
                        change.consume()
                        dragTotalX += amount
                    },
                    onDragEnd = {
                        if (SkillCarouselLogic.isSwipe(dragTotalX, dragTotalY)) {
                            if (dragTotalX < 0) goNext() else goPrev()
                        }
                        dragTotalX = 0f
                    },
                )
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(1.5.dp),
    ) {
        IconButton(
            onClick = {
                sfxManager?.playClick()
                goPrev()
            },
            enabled = canNavigate,
            modifier = Modifier.size(28.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.lucide_ic_chevron_left),
                contentDescription = "Habilidade anterior",
                tint = if (canNavigate) champagne.copy(alpha = 0.6f) else Color(0xFF52525B),
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .clickable(enabled = !disabled) { onOpenSkillsManager?.invoke() }
                .padding(vertical = 4.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AnimatedContent(
                targetState = currentSkill,
                transitionSpec = {
                    val dir = direction
                    (slideInHorizontally(tween(180)) { w -> (if (dir > 0) w else -w) / 4 } + fadeIn(tween(180)))
                        .togetherWith(
                            slideOutHorizontally(tween(180)) { w -> (if (dir > 0) -w else w) / 4 } + fadeOut(tween(180))
                        )
                },
                label = "skillCarouselContent",
            ) { skill ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(skill.emoji ?: "🎯", fontSize = 16.sp)
                    Text(
                        text = skill.name,
                        color = Color(0xFFF4F4F5),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                    )
                    Text(
                        text = "Nv. ${skill.level}" + ((skill.prestige ?: 0).let { p -> if (p > 0) " " + "★".repeat(p) else "" }),
                        color = champagne.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                    )
                }
            }

            if (skills.size > 1) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 3.dp),
                ) {
                    skills.forEachIndexed { idx, skill ->
                        val isActive = idx == activeIdx
                        Row(
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable(enabled = !disabled && !isActive) {
                                    direction = if (idx > activeIdx) 1f else -1f
                                    onSelectIndex(idx)
                                }
                                .background(if (isActive) Color(0xFFD4D4D8) else Color(0xFF52525B).copy(alpha = 0.7f))
                                .size(width = if (isActive) 10.dp else 4.dp, height = 4.dp)
                        ) {}
                    }
                }
            }
        }

        IconButton(
            onClick = {
                sfxManager?.playClick()
                goNext()
            },
            enabled = canNavigate,
            modifier = Modifier.size(28.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.lucide_ic_chevron_right),
                contentDescription = "Próxima habilidade",
                tint = if (canNavigate) champagne.copy(alpha = 0.6f) else Color(0xFF52525B),
            )
        }
    }
}
