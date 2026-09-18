package com.iurispraecepta.herolog.ui.focus

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.R as LucideR
import com.iurispraecepta.herolog.model.PomodoroSettings
import com.iurispraecepta.herolog.ui.components.HeroLogModal
import com.iurispraecepta.herolog.ui.components.ModalVariant
import com.iurispraecepta.herolog.ui.theme.Champagne300
import com.iurispraecepta.herolog.ui.theme.Champagne400
import com.iurispraecepta.herolog.ui.theme.Champagne500
import com.iurispraecepta.herolog.ui.theme.Cinzel
import com.iurispraecepta.herolog.ui.theme.Emerald400
import com.iurispraecepta.herolog.ui.theme.Inter
import com.iurispraecepta.herolog.ui.theme.JetBrainsMono
import com.iurispraecepta.herolog.ui.theme.Red400
import com.iurispraecepta.herolog.ui.theme.Red500
import com.iurispraecepta.herolog.ui.theme.Stone100
import com.iurispraecepta.herolog.ui.theme.Stone400
import com.iurispraecepta.herolog.ui.theme.Stone500
import com.iurispraecepta.herolog.ui.theme.Stone600
import com.iurispraecepta.herolog.ui.theme.Stone800
import com.iurispraecepta.herolog.ui.theme.Stone900
import com.iurispraecepta.herolog.ui.theme.Stone950
import com.iurispraecepta.herolog.ui.theme.Zinc300

private val PRESETS = listOf(25, 50, 90)

@Composable
fun TimerSettingsModal(
    isOpen: Boolean,
    onClose: () -> Unit,
    pomodoroSettings: PomodoroSettings,
    isRunning: Boolean,
    isBreakActive: Boolean,
    onSavePresetDuration: (Int) -> Unit,
    onSaveCustomSettings: (focus: Int, shortBreak: Int, longBreak: Int) -> Unit,
    onToggleAutoStartBreak: () -> Unit,
    onToggleAutoStartFocus: () -> Unit,
) {
    val isCustomActiveInitial = pomodoroSettings.focusDuration !in PRESETS
    var isCustomTime by remember(isOpen, pomodoroSettings) { mutableStateOf(isCustomActiveInitial) }
    var customFocusInput by remember(isOpen, pomodoroSettings) {
        mutableStateOf(pomodoroSettings.focusDuration.toString())
    }
    var customShortBreakInput by remember(isOpen, pomodoroSettings) {
        mutableStateOf(pomodoroSettings.shortBreakDuration.toString())
    }
    var customLongBreakInput by remember(isOpen, pomodoroSettings) {
        mutableStateOf(pomodoroSettings.longBreakDuration.toString())
    }

    val isCustomActive = isCustomTime || (pomodoroSettings.focusDuration !in PRESETS)
    val isControlsDisabled = isRunning || isBreakActive

    val focusInt = customFocusInput.toIntOrNull()
    val shortInt = customShortBreakInput.toIntOrNull()
    val longInt = customLongBreakInput.toIntOrNull()

    val isFocusValid = focusInt != null && focusInt in 1..180
    val isShortValid = shortInt != null && shortInt in 1..60
    val isLongValid = longInt != null && longInt in 1..60
    val isCustomValid = isFocusValid && isShortValid && isLongValid

    // R1 — React mantém o bloco sempre visível e só anima opacity (opacity-100 ↔
    // opacity-40, transition-all duration-300). Sem AnimatedVisibility (sem colapso).
    val customBlockAlpha by animateFloatAsState(
        targetValue = if (isCustomActive) 1f else 0.4f,
        animationSpec = tween(durationMillis = 300),
        label = "customBlockAlpha"
    )

    HeroLogModal(
        isOpen = isOpen,
        onClose = onClose,
        title = "⚙️ Ajustes do Timer",
        variant = ModalVariant.Amber
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Presets de Duração
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "PRESETS DE DURAÇÃO",
                    style = TextStyle(
                        fontFamily = Cinzel,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 0.6.sp,
                        color = Champagne500
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PRESETS.forEach { preset ->
                        val isActive = !isCustomActive && pomodoroSettings.focusDuration == preset
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (isActive) Champagne500.copy(alpha = 0.1f)
                                    else Stone900.copy(alpha = 0.4f)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isActive) Champagne400 else Color.White.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .clickable(
                                    enabled = !isControlsDisabled,
                                    onClick = {
                                        // R8 — React reseta isCustomTime ao clicar num preset.
                                        isCustomTime = false
                                        onSavePresetDuration(preset)
                                        onClose()
                                    }
                                )
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$preset MIN",
                                style = TextStyle(
                                    fontFamily = Cinzel,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp,
                                    letterSpacing = 0.6.sp,
                                    color = when {
                                        isControlsDisabled -> Zinc300.copy(alpha = 0.6f)
                                        isActive -> Champagne300
                                        else -> Zinc300.copy(alpha = 0.6f)
                                    }
                                )
                            )
                        }
                    }
                }
            }

            // 2. Duração Personalizada (pt-2 border-t border-white/10 — R2)
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.1f))
                )
                Spacer(modifier = Modifier.size(8.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Toggle row — card próprio (bg-stone-900/20 p-2.5 rounded
                    // border-white/10 — R5)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(Stone900.copy(alpha = 0.2f))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "DURAÇÃO PERSONALIZADA",
                                style = TextStyle(
                                    fontFamily = Cinzel,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Stone100.copy(alpha = 0.9f)
                                )
                            )
                            Text(
                                text = "Define tempos customizados para foco e pausas",
                                style = TextStyle(
                                    fontFamily = Inter,
                                    fontSize = 9.sp,
                                    lineHeight = 11.sp,
                                    color = Zinc300.copy(alpha = 0.5f)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.size(16.dp))

                        HeroLogToggleIcon(
                            checked = isCustomActive,
                            enabled = !isControlsDisabled,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    isCustomTime = true
                                } else {
                                    isCustomTime = false
                                    onSavePresetDuration(25)
                                }
                            }
                        )
                    }

                    // Bloco de campos — sempre visível, só escurece (R1);
                    // bg-stone-950/60 border-white/10 rounded-lg p-3 space-y-4 (R4)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Stone950.copy(alpha = 0.6f))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .alpha(customBlockAlpha),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Foco (1-180 min)
                                CustomDurationInputField(
                                    label = "FOCO",
                                    limitHint = "1-180 min",
                                    value = customFocusInput,
                                    onValueChange = { customFocusInput = it.filter { c -> c.isDigit() } },
                                    isValid = isFocusValid,
                                    enabled = !isControlsDisabled && isCustomActive,
                                    modifier = Modifier.weight(1f)
                                )

                                // Pausa Curta (1-60 min)
                                CustomDurationInputField(
                                    label = "PAUSA CURTA",
                                    limitHint = "1-60 min",
                                    value = customShortBreakInput,
                                    onValueChange = { customShortBreakInput = it.filter { c -> c.isDigit() } },
                                    isValid = isShortValid,
                                    enabled = !isControlsDisabled && isCustomActive,
                                    modifier = Modifier.weight(1f)
                                )

                                // Pausa Longa (1-60 min)
                                CustomDurationInputField(
                                    label = "PAUSA LONGA",
                                    limitHint = "1-60 min",
                                    value = customLongBreakInput,
                                    onValueChange = { customLongBreakInput = it.filter { c -> c.isDigit() } },
                                    isValid = isLongValid,
                                    enabled = !isControlsDisabled && isCustomActive,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            if (!isCustomValid && isCustomActive) {
                                Text(
                                    text = "Por favor, insira valores dentro dos limites indicados.",
                                    style = TextStyle(
                                        fontFamily = Cinzel,
                                        fontSize = 10.sp,
                                        color = Red400,
                                        textAlign = TextAlign.Center
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Button(
                                onClick = {
                                    if (isCustomValid && focusInt != null && shortInt != null && longInt != null) {
                                        onSaveCustomSettings(focusInt, shortInt, longInt)
                                        onClose()
                                    }
                                },
                                enabled = isCustomValid && isCustomActive && !isControlsDisabled,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .border(
                                        1.dp,
                                        Champagne500.copy(alpha = 0.3f),
                                        RoundedCornerShape(4.dp)
                                    ),
                                shape = RoundedCornerShape(4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Champagne500.copy(alpha = 0.1f),
                                    contentColor = Champagne300,
                                    disabledContainerColor = Stone800,
                                    disabledContentColor = Stone400.copy(alpha = 0.5f)
                                )
                            ) {
                                Text(
                                    text = "SALVAR PERSONALIZADO",
                                    style = TextStyle(
                                        fontFamily = Cinzel,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        letterSpacing = 1.2.sp,
                                        color = Champagne300
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // 3. Opções Adicionais (pt-2 border-t border-white/10 — R2)
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.1f))
                )
                Spacer(modifier = Modifier.size(8.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "OPÇÕES ADICIONAIS",
                        style = TextStyle(
                            fontFamily = Cinzel,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 0.6.sp,
                            color = Champagne500
                        )
                    )

                    // Toggle 1: Início automático de descanso
                    OptionToggleRow(
                        title = "AUTO-INICIAR DESCANSO",
                        description = "Inicia o descanso automaticamente ao fim da sessão de foco",
                        checked = pomodoroSettings.autoStartBreak,
                        onToggle = onToggleAutoStartBreak
                    )

                    // Toggle 2: Início automático de foco
                    OptionToggleRow(
                        title = "AUTO-INICIAR FOCO",
                        description = "Inicia a próxima sessão de foco automaticamente ao fim do descanso",
                        checked = pomodoroSettings.autoStartFocus,
                        onToggle = onToggleAutoStartFocus
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomDurationInputField(
    label: String,
    limitHint: String,
    value: String,
    onValueChange: (String) -> Unit,
    isValid: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    // R3 — card próprio: bg-stone-900/40 border-stone-800 p-2 rounded,
    // conteúdo centralizado (flex-col items-center text-center).
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Stone900.copy(alpha = 0.4f))
            .border(1.dp, Stone800, RoundedCornerShape(4.dp))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontFamily = Cinzel,
                fontWeight = FontWeight.Normal,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                color = Zinc300.copy(alpha = 0.5f)
            )
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = TextStyle(
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = Champagne300
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Stone900,
                unfocusedContainerColor = Stone900,
                disabledContainerColor = Stone900.copy(alpha = 0.5f),
                focusedBorderColor = if (isValid) Champagne500 else Red500,
                unfocusedBorderColor = if (isValid) Color.White.copy(alpha = 0.1f)
                else Red500.copy(alpha = 0.5f),
                disabledBorderColor = Color.White.copy(alpha = 0.05f),
                cursorColor = Emerald400
            ),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = limitHint,
            style = TextStyle(
                fontSize = 8.sp,
                textAlign = TextAlign.Center,
                color = Stone500,
                fontFamily = JetBrainsMono
            )
        )
    }
}

@Composable
private fun OptionToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(Stone900.copy(alpha = 0.2f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
            .clickable(
                role = Role.Switch,
                onClick = onToggle
            )
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = Cinzel,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Stone100.copy(alpha = 0.9f)
                )
            )
            Text(
                text = description,
                style = TextStyle(
                    fontFamily = Inter,
                    fontSize = 9.sp,
                    lineHeight = 11.sp,
                    color = Zinc300.copy(alpha = 0.5f)
                )
            )
        }

        Spacer(modifier = Modifier.size(16.dp))

        HeroLogToggleIcon(
            checked = checked,
            enabled = true,
            onCheckedChange = { onToggle() }
        )
    }
}

@Composable
private fun HeroLogToggleIcon(
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Icon(
        painter = painterResource(
            id = if (checked) LucideR.drawable.lucide_ic_toggle_right
            else LucideR.drawable.lucide_ic_toggle_left
        ),
        contentDescription = null,
        tint = if (checked) Emerald400 else Stone600,
        modifier = Modifier
            .size(32.dp)
            .clickable(
                enabled = enabled,
                role = Role.Switch,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onCheckedChange(!checked) }
            )
    )
}