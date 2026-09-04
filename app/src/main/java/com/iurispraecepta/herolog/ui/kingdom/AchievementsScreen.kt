package com.iurispraecepta.herolog.ui.kingdom

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.iurispraecepta.herolog.logic.achievements.Achievement
import com.iurispraecepta.herolog.logic.achievements.AchievementCatalog
import com.iurispraecepta.herolog.logic.achievements.AchievementLogic
import com.iurispraecepta.herolog.model.CharacterState
import com.iurispraecepta.herolog.ui.theme.Amber100
import com.iurispraecepta.herolog.ui.theme.Amber500
import com.iurispraecepta.herolog.ui.theme.Cinzel
import com.iurispraecepta.herolog.ui.theme.Stone900
import com.iurispraecepta.herolog.ui.theme.Stone950

private val Champagne400 = Color(0xFFE5C158)
private val Champagne500 = Color(0xFFD4AF37)
private val Stone600 = Color(0xFF57534E)

/**
 * Porte fiel de AchievementsTab.tsx (src/modules/kingdom/AchievementsTab.tsx, 106L) + o
 * cabeçalho de painel do wrapper em App.tsx (activeTab === 'achievements').
 *
 * Sem lógica nova — o catálogo `ACHIEVEMENTS_LIST` (8 itens) e `AchievementLogic.isUnlocked`
 * já foram portados e validados no Bloco 16 (`AchievementCatalog.kt`/`AchievementLogic.kt`),
 * conferidos aqui como confirmação cruzada de que batem exatamente com a fonte atual — não é
 * achado novo.
 *
 * Nota sobre ícones: `Award` (usado no header "CONQUISTAS" do wrapper) aproximado como
 * `WorkspacePremium` (novo, ainda não confirmado por build real). `Lock` e `Sparkles→AutoAwesome`
 * (já usado no Bloco D) exatos/já estabelecidos.
 */
@Composable
fun AchievementsScreen(state: CharacterState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Stone950)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // ═══ CABEÇALHO DO PAINEL (vem do wrapper em App.tsx) ═══
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.WorkspacePremium,
                contentDescription = null,
                tint = Champagne500,
                modifier = Modifier.height(16.dp).width(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "CONQUISTAS",
                fontFamily = Cinzel,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                letterSpacing = 0.10.em,
                color = Champagne400
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            items(AchievementCatalog.ACHIEVEMENTS_LIST) { achievement ->
                AchievementCard(
                    achievement = achievement,
                    isUnlocked = AchievementLogic.isUnlocked(achievement, state)
                )
            }
        }
    }
}

@Composable
private fun AchievementCard(achievement: Achievement, isUnlocked: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isUnlocked) Amber500.copy(alpha = 0.03f) else Stone950.copy(alpha = 0.10f))
            .border(
                1.dp,
                if (isUnlocked) Amber500.copy(alpha = 0.30f) else Stone900,
                RoundedCornerShape(8.dp)
            )
            .alpha(if (isUnlocked) 1f else 0.40f)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(Stone950.copy(alpha = 0.40f))
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isUnlocked) {
                Text(text = achievement.icon, fontSize = 28.sp)
            } else {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    tint = Stone600,
                    modifier = Modifier.height(24.dp).width(24.dp)
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = achievement.name,
                    fontFamily = Cinzel,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 0.025.em,
                    color = Amber100.copy(alpha = 0.90f)
                )
                if (isUnlocked) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Champagne500.copy(alpha = 0.10f))
                            .border(1.dp, Champagne500.copy(alpha = 0.20f), CircleShape)
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = Champagne400,
                            modifier = Modifier.height(9.dp).width(9.dp)
                        )
                        Text(
                            text = "DESBLOQUEADO",
                            fontFamily = Cinzel,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            letterSpacing = 0.05.em,
                            color = Champagne400
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = achievement.desc,
                fontFamily = Cinzel,
                fontSize = 12.sp,
                color = Amber100.copy(alpha = 0.40f)
            )
        }
    }
}
