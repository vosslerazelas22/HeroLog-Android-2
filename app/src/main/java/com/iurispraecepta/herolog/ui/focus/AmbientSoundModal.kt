package com.iurispraecepta.herolog.ui.focus

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.R
import com.iurispraecepta.herolog.ui.components.HeroLogModal
import com.iurispraecepta.herolog.ui.components.ModalVariant
import com.iurispraecepta.herolog.ui.theme.Amber400
import com.iurispraecepta.herolog.ui.theme.Amber500
import com.iurispraecepta.herolog.ui.theme.Champagne400
import com.iurispraecepta.herolog.ui.theme.Champagne500
import com.iurispraecepta.herolog.ui.theme.Emerald400
import com.iurispraecepta.herolog.ui.theme.Stone500
import com.iurispraecepta.herolog.ui.theme.Stone600
import com.iurispraecepta.herolog.ui.theme.Stone700
import com.iurispraecepta.herolog.ui.theme.Stone800
import com.iurispraecepta.herolog.ui.theme.Stone900
import com.iurispraecepta.herolog.ui.theme.Zinc300

@Composable
fun AmbientSoundModal(
    isOpen: Boolean,
    onClose: () -> Unit,
    selectedTrack: String?,
    volume: Int,
    onSelectTrack: (String?) -> Unit,
    onSetVolume: (Int) -> Unit,
    tracks: List<AmbientTrack> = AMBIENT_SOUNDS
) {
    HeroLogModal(
        isOpen = isOpen,
        onClose = onClose,
        title = "Sons Ambiente",
        variant = ModalVariant.Amber
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Volume Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Stone900)
                    .border(1.dp, Stone800, RoundedCornerShape(8.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(
                                if (volume == 0) R.drawable.lucide_ic_volume_x else R.drawable.lucide_ic_volume_2
                            ),
                            contentDescription = null,
                            tint = if (volume == 0) Stone500 else Champagne400,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Volume do Eco",
                            color = Zinc300.copy(alpha = 0.8f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Champagne500.copy(alpha = 0.1f))
                            .border(1.dp, Champagne500.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "$volume%",
                            color = Champagne400.copy(alpha = 0.9f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Slider(
                    value = volume.toFloat(),
                    onValueChange = { onSetVolume(it.toInt()) },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(
                        thumbColor = Amber400,
                        activeTrackColor = Amber500,
                        inactiveTrackColor = Stone700
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Lista de Trilhas
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tracks.forEach { track ->
                    val isSelected = selectedTrack == track.id
                    AmbientTrackRow(
                        track = track,
                        isSelected = isSelected,
                        onClick = { onSelectTrack(if (isSelected) null else track.id) }
                    )
                }
            }

            // Rodapé informativo
            Text(
                text = "Os sussurros do santuário reverberam somente enquanto sua mente estiver focada de fato (durante sessões de foco ativas, não pausadas).",
                color = Stone500,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun AmbientTrackRow(
    track: AmbientTrack,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) Champagne500.copy(alpha = 0.3f) else Stone800
    val bgColor = if (isSelected) Champagne500.copy(alpha = 0.05f) else Stone900.copy(alpha = 0.5f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = track.icone,
                fontSize = 20.sp
            )
            Column {
                Text(
                    text = track.nome,
                    color = Zinc300.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black
                )
                if (isSelected) {
                    Text(
                        text = "Ativo e em Sintonia",
                        color = Emerald400.copy(alpha = 0.8f),
                        fontSize = 11.sp
                    )
                }
            }
        }

        Icon(
            painter = painterResource(
                if (isSelected) R.drawable.lucide_ic_toggle_right else R.drawable.lucide_ic_toggle_left
            ),
            contentDescription = null,
            tint = if (isSelected) Emerald400 else Stone600
        )
    }
}
