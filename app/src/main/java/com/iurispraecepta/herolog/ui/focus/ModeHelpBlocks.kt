package com.iurispraecepta.herolog.ui.focus

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iurispraecepta.herolog.ui.components.ModalVariant

// Cores extraídas do ModeDescriptionModal (paridade com ModeDescriptionModal.tsx
// da fonte real) para reuso no acordeão inline do IncursionModeModal (FR-017).
private val Stone950 = Color(0xFF0C0A09)
private val Stone300 = Color(0xFFD6D3D1)

private val Amber500 = Color(0xFFF59E0B)
private val Amber400 = Color(0xFFFBBF24)
private val Amber950 = Color(0xFF451A03)

private val Purple600 = Color(0xFF9333EA)
private val Purple500 = Color(0xFFA855F7)
private val Purple400 = Color(0xFFC084FC)
private val Purple950 = Color(0xFF3B0764)

private val Red600 = Color(0xFFDC2626)
private val Red500 = Color(0xFFEF4444)
private val Red400 = Color(0xFFF87171)
private val Red950 = Color(0xFF450A0A)

private data class HelpVariantColors(
    val cardBorder: Color,
    val cardBg: Color,
    val labelColor: Color,
)

private fun helpColorsFor(variant: ModalVariant): HelpVariantColors = when (variant) {
    ModalVariant.Amber -> HelpVariantColors(
        Amber500.copy(alpha = 0.25f),
        Amber950.copy(alpha = 0.20f),
        Amber400
    )
    ModalVariant.Purple -> HelpVariantColors(
        Purple500.copy(alpha = 0.25f),
        Purple950.copy(alpha = 0.25f),
        Purple400
    )
    ModalVariant.Red -> HelpVariantColors(
        Red500.copy(alpha = 0.25f),
        Red950.copy(alpha = 0.25f),
        Red400
    )
}

fun helpVariantFor(mode: RaidMode): ModalVariant = when (mode) {
    RaidMode.PADRAO -> ModalVariant.Amber
    RaidMode.MASMORRA -> ModalVariant.Purple
    RaidMode.SELVAGEM -> ModalVariant.Red
}

/**
 * Renderizador de blocos de ajuda extraído do ModeDescriptionModal (FR-017):
 * os mesmos cards (borda/bg/label) sem o chrome de modal, para uso inline
 * no acordeão de cada card do IncursionModeModal.
 */
@Composable
fun ModeHelpBlocks(
    blocks: List<RaidModeHelpBlock>,
    variant: ModalVariant,
    modifier: Modifier = Modifier,
) {
    val colors = helpColorsFor(variant)
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        blocks.forEach { block ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Stone950)
                    .background(colors.cardBg)
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = block.label.uppercase(),
                        style = TextStyle(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = colors.labelColor,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = block.text,
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = Stone300
                        )
                    )
                }
            }
        }
    }
}
