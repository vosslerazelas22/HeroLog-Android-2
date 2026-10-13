package com.iurispraecepta.herolog.ui.focus

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import com.iurispraecepta.herolog.logic.focus.AggregatedCelebrationSummary
import com.iurispraecepta.herolog.model.Skill

/**
 * Decide se dispensar a celebração deve tirar o usuário do FocusModeScreen.
 *
 * Sai do focus mode quando ele está ativo, EXCETO durante um descanso em
 * andamento: o descanso roda no Service (BREAK_RUNNING) e a tela de descanso
 * deve permanecer após o dismiss da celebração (Issue #39, correção de descanso).
 * Função pura — testada em [FocusCelebrationOverlayBehaviorTest].
 */
fun shouldExitFocusOnDismiss(isFocusMode: Boolean, isBreakActive: Boolean): Boolean =
    isFocusMode && !isBreakActive

/**
 * Overlay fullscreen da celebração pendente (spec-008 T11, Issue #39).
 *
 * Espelha o React (`FocusCompletionFlow` em `App.tsx:3624`, `fixed inset-0 z-[10000]`):
 * renderizado como irmão do FocusModeScreen/Scaffold — cobre os dois. Tudo aqui
 * (Box + BackHandler + Host) só é composto quando [summary] não é nulo.
 *
 * - Bloqueio de input: o `pointerInput` do Box consome toques em áreas vazias para
 *   não vazarem para o que está por baixo; filhos clicáveis seguem funcionando.
 * - BackHandler: dispensa pelo MESMO caminho de crédito do botão — AGGREGATED via
 *   [onConfirmAggregated]; SINGLE via [onConfirmSingle] com as notas/tag digitadas
 *   (estado em progresso hoisted de [PendingCelebrationHost], iniciando em "" como
 *   o `initialNotes` do FocusCompletionFlow).
 * - [exitFocusModeOnDismiss] vem de [shouldExitFocusOnDismiss] na chamada.
 */
@Composable
fun PendingCelebrationOverlay(
    summary: AggregatedCelebrationSummary?,
    exitFocusModeOnDismiss: Boolean,
    onExitFocusMode: () -> Unit,
    onConfirmSingle: (historyId: String?, notes: String, tag: String) -> Unit,
    onConfirmAggregated: () -> Unit,
    skills: List<Skill>,
    modifier: Modifier = Modifier
) {
    if (summary == null) return

    // Notas/tag em progresso no modo SINGLE: espelham o estado interno do
    // FocusCompletionFlow (initialNotes = "" via PendingCelebrationHost) para que
    // o BackHandler confirme sem perder o digitado. Chaveados no historyId da
    // primeira sessão: uma troca de celebração sem passar por summary == null
    // (ex.: fila crescendo via refreshPendingCelebration em handleServiceCompleted)
    // não pode vazar notas de A para B.
    val firstHistoryId = summary.sessions.firstOrNull()?.historyId
    var inProgressNotes by remember(firstHistoryId) { mutableStateOf("") }
    var inProgressTag by remember(firstHistoryId) { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                // Consome toques em áreas vazias do overlay para não vazar pro
                // Scaffold/FocusModeScreen. Filhos clicáveis continuam normais.
                detectTapGestures { }
            }
    ) {
        // BackHandler: impede que o botão voltar do sistema navegue/feche o que
        // está por baixo; confirma a celebração (não descarta sem creditar — o
        // crédito foi aplicado em FocusUseCase.applyRewards antes da fila).
        BackHandler(enabled = true) {
            if (exitFocusModeOnDismiss) onExitFocusMode()
            if (selectCelebrationMode(summary.sessionCount) == CelebrationMode.SINGLE) {
                onConfirmSingle(summary.sessions.first().historyId, inProgressNotes, inProgressTag)
            } else {
                onConfirmAggregated()
            }
        }
        PendingCelebrationHost(
            summary = summary,
            onConfirmSingle = { historyId, notes, tag ->
                if (exitFocusModeOnDismiss) onExitFocusMode()
                onConfirmSingle(historyId, notes, tag)
            },
            onDismissAggregated = {
                if (exitFocusModeOnDismiss) onExitFocusMode()
                onConfirmAggregated()
            },
            skills = skills,
            onNotesChange = { inProgressNotes = it },
            onTagChange = { inProgressTag = it },
            modifier = Modifier.fillMaxSize()
        )
    }
}
