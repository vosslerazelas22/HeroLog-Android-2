package com.iurispraecepta.herolog.ui.kingdom

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.iurispraecepta.herolog.model.LogEntry
import com.iurispraecepta.herolog.ui.theme.Amber100
import com.iurispraecepta.herolog.ui.theme.Amber400
import com.iurispraecepta.herolog.ui.theme.Amber500
import com.iurispraecepta.herolog.ui.theme.JetBrainsMono
import com.iurispraecepta.herolog.ui.theme.Stone950

private val Champagne400 = Color(0xFFE5C158)
private val Champagne500 = Color(0xFFD4AF37)
private val Amber200 = Color(0xFFFDE68A)

/**
 * Porte de `{activeTab === 'logs' && (...)}` em `App.tsx` (linha ~3517) — a aba "REGISTRO DE
 * ATIVIDADES" do módulo Reino.
 *
 * ⚠️ ABERTO PRA CONFIRMAÇÃO — ícone e cor: a fonte usa `lucide-react`'s `Scroll` (ícone de
 * pergaminho) tingido de `text-champagne-500`. Este arquivo usa `Icons.Filled.Menu` como
 * aproximação temporária (Material Icons não tem "scroll") e `Amber500` no lugar de
 * `champagne-500`, porque `Color.kt` neste commit não tinha nenhum token `Champagne*` definido
 * quando este arquivo foi escrito — só `Stone*`/`Amber*`/`Purple*`/`Pink*`. Se o rebrand
 * amber→champagne (risco já registrado no Bloco D / GuideScreen) tiver adicionado esses tokens
 * em outro commit paralelo, trocar `Amber500` por `Champagne500` aqui e escolher um ícone de
 * pergaminho melhor (`Icons.Filled.Article` ou `Icons.AutoMirrored.Filled.Article` são mais
 * próximos visualmente que `Menu` — `Menu` foi usado só como placeholder óbvio de "precisa trocar").
 *
 * A fonte não pagina nem trunca a exibição — mostra `logs.map(...)` inteiro dentro de uma `div`
 * com `overflow-y-auto` e altura fixa de 420px. O cap de 51 entradas acontece na escrita
 * (`addSystemLog`, `prev.slice(0, 50)`), não na leitura; esta tela é só leitura passiva da lista
 * que o ViewModel já entrega pronta e ordenada (mais recente primeiro).
 *
 * @param logs Lista já ordenada mais-recente-primeiro, como entregue pelo
 *   `StateFlow<List<LogEntry>>` do `HeroLogViewModel` (ver instruções de patch — este StateFlow
 *   ainda precisa ser adicionado ao ViewModel, este arquivo assume que ele existirá com esse nome
 *   de tipo).
 */
@Composable
fun LogsScreen(logs: List<LogEntry>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        // Header -- porte do wrapper compartilhado (título centralizado + ícone + borda inferior)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
                .border(width = 0.dp, color = Color.Transparent), // divisor real fica no Box abaixo
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Article,
                contentDescription = null,
                tint = Champagne500,
                modifier = Modifier.height(16.dp)
            )
            Text(
                text = "REGISTRO DE ATIVIDADES",
                color = Champagne400,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                letterSpacing = 0.05.em,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Painel de log -- porte de bg-stone-950/90 ... h-[420px] overflow-y-auto
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
                .background(Stone950.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                .border(1.dp, Amber500.copy(alpha = 0.10f), RoundedCornerShape(8.dp))
        ) {
            if (logs.isEmpty()) {
                Text(
                    text = "Nenhum sussurro celestial registrado até o momento...",
                    color = Amber100.copy(alpha = 0.30f),
                    fontStyle = FontStyle.Italic,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(logs, key = { it.id }) { log ->
                        Row(modifier = Modifier.padding(bottom = 8.dp)) {
                            Text(
                                text = "[${log.time}]",
                                color = Amber400.copy(alpha = 0.5f),
                                fontSize = 11.sp,
                                fontFamily = JetBrainsMono,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = log.text,
                                color = if (log.highlighted) Amber200 else Amber100.copy(alpha = 0.40f),
                                fontWeight = if (log.highlighted) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp,
                                fontFamily = JetBrainsMono
                            )
                        }
                    }
                }
            }
        }
    }
}
