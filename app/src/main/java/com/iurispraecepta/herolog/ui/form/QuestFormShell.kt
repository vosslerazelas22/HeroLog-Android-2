package com.iurispraecepta.herolog.ui.form

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iurispraecepta.herolog.ui.theme.Inter
import com.iurispraecepta.herolog.ui.theme.Stone400
import com.iurispraecepta.herolog.ui.theme.Stone800
import com.iurispraecepta.herolog.ui.theme.Stone900

private val Amber300 = Color(0xFFFCD34D)
private val Amber200 = Color(0xFFFDE68A)
private val Amber400 = Color(0xFFE5C158)

/**
 * Shell compartilhado para formulários de create/edit de Habits, Dailies e Todos.
 * Resolve FR-001 (tres regioes: header do modal, corpo rolavel, rodape fixo com divisor),
 * FR-004 (imePadding via HeroLogModal), FR-005/FR-007 (dirty state + requestClose unico).
 *
 * Uso: invocar dentro do content slot do HeroLogModal com contentPadding = PaddingValues(0.dp).
 *
 * @param isDirty se true, o formulario tem mudancas nao salvas (snapshot != current)
 * @param showDiscard se true, exibe a confirmacao inline "Descartar alteracoes?" no rodape
 * @param isEditing se true, esta em modo de edicao (mostra botao Excluir)
 * @param onDiscard confirmar descarte (Descartar) — reset + fecha
 * @param onContinueEditing fechar apenas a confirmacao (Continuar editando)
 * @param onRequestClose unica rota de fechamento (X/backdrop/Voltar/Cancelar)
 * @param onConfirmDelete confirmar exclusao (Sim na confirmacao)
 * @param deleteConfirmState estado da confirmacao de exclusao
 * @param onDeleteConfirmChange callback para alterar estado de confirmacao de exclusao
 * @param onSubmit callback ao submeter (Criar/Salvar)
 * @param body slot do corpo rolavel do formulario
 */
@Composable
fun QuestFormShell(
    isDirty: Boolean,
    showDiscard: Boolean,
    isEditing: Boolean,
    onDiscard: () -> Unit,
    onContinueEditing: () -> Unit,
    onRequestClose: () -> Unit,
    onConfirmDelete: () -> Unit,
    deleteConfirmState: DeleteConfirmState,
    onDeleteConfirmChange: (DeleteConfirmState) -> Unit,
    onSubmit: () -> Unit,
    submitEnabled: Boolean,
    submitLabel: String,
    body: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Corpo rolavel (sem scroll proprio — o HeroLogModal scrolla)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            content = body
        )

        // Divisor sutil separando corpo do rodape
        HorizontalDivider(
            color = Stone800,
            thickness = 1.dp
        )

        // Rodape de acoes (fixo, nao rola)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lado esquerdo: Excluir (somente em edicao)
            if (isEditing) {
                when (deleteConfirmState) {
                    DeleteConfirmState.None -> {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0x1ADC2626))
                                .border(1.dp, Color(0x4DDC2626), RoundedCornerShape(6.dp))
                                .clickable { onDeleteConfirmChange(DeleteConfirmState.Confirming) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                // Paridade React: botoes do rodape usam classe CSS `uppercase`
                                text = "Excluir".uppercase(),
                                color = Color(0xFFF87171),
                                fontFamily = Inter,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    DeleteConfirmState.Confirming -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Excluir?".uppercase(),
                                color = Color(0xFFF87171),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFDC2626))
                                    .clickable {
                                        onDeleteConfirmChange(DeleteConfirmState.None)
                                        onConfirmDelete()
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Sim".uppercase(),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Stone800)
                                    .clickable { onDeleteConfirmChange(DeleteConfirmState.None) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Não".uppercase(),
                                    color = Stone400,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }

            // Lado direito: Cancelar/Descarte + Submit
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isDirty && showDiscard) {
                    // Confirmacao de descarte inline
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Descartar?".uppercase(),
                            color = Amber300,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0x33F59E0B))
                                .border(1.dp, Amber400, RoundedCornerShape(4.dp))
                                .clickable { onDiscard() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                                Text(
                                    text = "Sim".uppercase(),
                                    color = Amber200,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Stone800)
                                    .clickable { onContinueEditing() }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Não".uppercase(),
                                color = Stone400,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    // Cancelar — unica rota de fechamento
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x331C1917))
                            .border(1.dp, Color(0x3344403C), RoundedCornerShape(6.dp))
                            .clickable { onRequestClose() }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Cancelar".uppercase(),
                            color = Stone400,
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Submit (Criar / Salvar)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (submitEnabled) Color(0x33F59E0B) else Color(0x1AF59E0B))
                        .border(
                            1.dp,
                            if (submitEnabled) Color(0x66FBBF24) else Color(0x33FBBF24),
                            RoundedCornerShape(6.dp)
                        )
                        .clickable(enabled = submitEnabled) { onSubmit() }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = submitLabel.uppercase(),
                        color = if (submitEnabled) Amber300 else Amber300.copy(alpha = 0.4f),
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

enum class DeleteConfirmState {
    None,
    Confirming
}