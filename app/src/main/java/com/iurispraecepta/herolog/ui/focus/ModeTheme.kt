package com.iurispraecepta.herolog.ui.focus

import androidx.compose.ui.graphics.Color
import com.iurispraecepta.herolog.ui.theme.Champagne400
import com.iurispraecepta.herolog.ui.theme.Champagne500
import com.iurispraecepta.herolog.ui.theme.Purple300
import com.iurispraecepta.herolog.ui.theme.Purple400
import com.iurispraecepta.herolog.ui.theme.Purple500
import com.iurispraecepta.herolog.ui.theme.Rose400
import com.iurispraecepta.herolog.ui.theme.Rose500

/**
 * Tema da Câmara de Foco derivado de [RaidMode] (FR-001/FR-002).
 *
 * - `accent`: título do header + ícone do botão Modo.
 * - `accentDim`: ícone do timer no header + bordas.
 * - `statusMain`: texto principal do status do modo.
 *
 * Todos os valores reaproveitam tokens já existentes (sem hex novo).
 * PADRAO usa os mesmos tokens do header de hoje (FR-005: sem diff visual).
 * Accent da Terra Selvagem = Rose400 `FB7185` (D10): igual ao orb A/C
 * (FocusOrb.tsx da fonte real) e às notificações; substitui o `F87171`
 * do status e do ícone do Modo.
 */
data class ModeTheme(
    val accent: Color,
    val accentDim: Color,
    val statusMain: Color,
)

fun RaidMode.theme(): ModeTheme = when (this) {
    RaidMode.PADRAO -> ModeTheme(
        accent = Champagne400,
        accentDim = Champagne500,
        statusMain = Champagne400,
    )
    RaidMode.MASMORRA -> ModeTheme(
        accent = Purple400,
        accentDim = Purple500,
        statusMain = Purple300,
    )
    RaidMode.SELVAGEM -> ModeTheme(
        accent = Rose400,
        accentDim = Rose500,
        statusMain = Rose400,
    )
}
