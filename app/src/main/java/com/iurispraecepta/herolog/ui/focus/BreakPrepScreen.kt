package com.iurispraecepta.herolog.ui.focus

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iurispraecepta.herolog.ui.theme.Amber500
import com.iurispraecepta.herolog.ui.theme.Stone950

@Composable
fun BreakPrepScreen(
    shortBreakMinutes: Int,
    longBreakMinutes: Int,
    selectedBreakMinutes: Int,
    isDungeonMode: Boolean,
    onSelectDuration: (Int) -> Unit,
    onStartBreak: () -> Unit,
    onSkipBreak: () -> Unit,
    modifier: Modifier = Modifier
) {
    val emeraldPrimary = Color(0xFF10B981)
    val emeraldLight = Color(0xFF34D399)
    val emeraldDark = Color(0xFF059669)
    val tealDark = Color(0xFF0D9488)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Stone950)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, emeraldPrimary, RoundedCornerShape(20.dp))
                .testTag("break_prep_card"),
            color = Color(0xCC0C0A09), // stone-950/70
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "MISSÃO CONCLUÍDA.",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = emeraldLight,
                    letterSpacing = 1.2.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Escolha a duração da pausa.",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                if (!isDungeonMode) {
                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        BreakDurationOption(
                            minutes = shortBreakMinutes,
                            label = "(curto)",
                            isSelected = selectedBreakMinutes == shortBreakMinutes,
                            onClick = { onSelectDuration(shortBreakMinutes) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("break_duration_short")
                        )

                        BreakDurationOption(
                            minutes = longBreakMinutes,
                            label = "(longo)",
                            isSelected = selectedBreakMinutes == longBreakMinutes,
                            onClick = { onSelectDuration(longBreakMinutes) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("break_duration_long")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(emeraldDark, tealDark)
                            )
                        )
                        .clickable { onStartBreak() }
                        .testTag("start_break_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "☕ FAZER UMA PAUSA",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = onSkipBreak,
                    modifier = Modifier.testTag("skip_break_button")
                ) {
                    Text(
                        text = "⏩ CONTINUAR SEM PAUSA",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Amber500
                    )
                }
            }
        }
    }
}

@Composable
private fun BreakDurationOption(
    minutes: Int,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val emeraldPrimary = Color(0xFF10B981)
    val emeraldLight = Color(0xFF34D399)

    val backgroundColor = if (isSelected) Color(0x3310B981) else Color(0xFF1C1917)
    val borderColor = if (isSelected) emeraldPrimary else Color(0xFF444444)
    val textColor = if (isSelected) emeraldLight else Color(0xFFD4D4D8)
    val labelColor = if (isSelected) emeraldLight.copy(alpha = 0.8f) else Color(0xFFA8A29E)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 14.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$minutes MIN",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = textColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = labelColor
            )
        }
    }
}
