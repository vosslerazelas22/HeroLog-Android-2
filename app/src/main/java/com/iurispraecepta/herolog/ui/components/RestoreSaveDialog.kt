// app/src/main/java/com/iurispraecepta/herolog/ui/components/RestoreSaveDialog.kt
package com.iurispraecepta.herolog.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iurispraecepta.herolog.logic.SaveImportOutcome

private val Amber100Half = Color(0xFFFEF3C7)
private val Amber400 = Color(0xFFFBBF24)
private val Amber500 = Color(0xFFF59E0B)
private val Stone900 = Color(0xFF1C1917)
private val Emerald400 = Color(0xFF34D399)
private val Red400 = Color(0xFFF87171)

/**
 * Porte do modal "Restaurar Código Rúnico" (`App.tsx`, `isImportTextOpen`/`pastedSaveText`/
 * `handleImportSaveFromText`) — a via de "código de salvamento" colado do painel de Ajustes
 * Gerais real. **Recorte deliberado**: só esta sub-tela foi portada, não o modal "Ajustes
 * Gerais" inteiro (que também tem edição de nome do herói, nome do dispositivo, exportar/
 * importar arquivo `.json`, copiar pra área de transferência, purgar campanha e logout —
 * nenhum desses tem equivalente Supabase/conta no Android, que não tem sistema de login por
 * decisão de escopo já registrada no `PARIDADE.md`). Fica pra depois se algum dia fizer sentido.
 *
 * Textos e títulos copiados literalmente de `App.tsx` (linhas ~4238-4280 e ~1911-1935):
 * título "Restaurar Código Rúnico", subtítulo, placeholder, "Confirmar Runas"/"Cancelar", e as
 * duas mensagens de resultado ("📜 Gravação Restaurada!"/"⚠️ Runas Corrompidas" — usando o texto
 * específico da variante "texto colado", não o da variante "arquivo", que difere ligeiramente).
 *
 * A validação leve da fonte (`typeof parsed.charClass === 'string' && typeof parsed.gold ===
 * 'number'`) não foi replicada à parte — `SaveMigrationLogic.normalizeGameState` já falha com
 * `InvalidJson` nesses casos (campos obrigatórios do `CharacterState`), tornando o guard
 * redundante no Android.
 *
 * Reusa `HeroLogModal` (Bloco 3, componente já existente) em vez de um `Dialog` cru, pelo mesmo
 * motivo de qualquer outro modal do projeto.
 */
@Composable
fun RestoreSaveDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (rawJson: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var pastedText by remember(isOpen) { mutableStateOf("") }

    HeroLogModal(
        isOpen = isOpen,
        onClose = onDismiss,
        title = "Restaurar Código Rúnico"
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "🔮", fontSize = 30.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Cole abaixo o conjunto completo de runas do seu progresso de campanha " +
                    "exportado anteriormente:",
                fontFamily = FontFamily.Serif,
                fontSize = 11.sp,
                color = Amber100Half.copy(alpha = 0.70f),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = pastedText,
            onValueChange = { pastedText = it },
            placeholder = { Text(text = "Cole aqui o longo código de progresso...", fontSize = 11.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 128.dp),
            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Amber400,
                unfocusedBorderColor = Amber500.copy(alpha = 0.30f),
                focusedTextColor = Amber100Half,
                unfocusedTextColor = Amber100Half
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    onConfirm(pastedText)
                    onDismiss()
                },
                enabled = pastedText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Amber500,
                    contentColor = Stone900,
                    disabledContainerColor = Stone900,
                    disabledContentColor = Amber100Half.copy(alpha = 0.30f)
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Confirmar Runas", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Black, fontSize = 12.sp)
            }
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Cancelar", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Black, fontSize = 12.sp)
            }
        }
    }
}

/**
 * Porte do `customDialog` de resultado (`isConfirm: false`) só pras duas mensagens deste fluxo
 * de import — não o sistema genérico de diálogo da fonte (`setCustomDialog`), que é usado em
 * dezenas de outros lugares do app e está fora do escopo deste recorte.
 */
@Composable
fun SaveImportResultDialog(
    result: SaveImportOutcome?,
    onDismiss: () -> Unit
) {
    if (result == null) return

    HeroLogModal(
        isOpen = true,
        onClose = onDismiss,
        title = if (result is SaveImportOutcome.Restored) "📜 Gravação Restaurada!" else "⚠️ Runas Corrompidas"
    ) {
        Text(
            text = when (result) {
                is SaveImportOutcome.Restored ->
                    "Os pergaminhos lendários de ${result.charName} foram aplicados às runas com sucesso!"
                is SaveImportOutcome.Failed ->
                    "O texto inserido não pôde ser decifrado pelo Santuário. Certifique-se de " +
                        "colar o código de progresso completo e sem alterações."
            },
            fontFamily = FontFamily.Serif,
            fontSize = 12.sp,
            color = if (result is SaveImportOutcome.Restored) Emerald400 else Red400,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(containerColor = Amber500, contentColor = Stone900),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "OK", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Black, fontSize = 12.sp)
        }
    }
}

/** Resultado da tentativa de import, já traduzido pra o que a UI precisa mostrar (definido em
 * `com.iurispraecepta.herolog.logic.SaveMigrationLogic.kt`, não aqui — ver patch em anexo). */
