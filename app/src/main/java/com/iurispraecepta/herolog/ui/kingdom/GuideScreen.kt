package com.iurispraecepta.herolog.ui.kingdom

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.iurispraecepta.herolog.ui.theme.Amber100
import com.iurispraecepta.herolog.ui.theme.Amber400
import com.iurispraecepta.herolog.ui.theme.Amber500
import com.iurispraecepta.herolog.ui.theme.Stone400
import com.iurispraecepta.herolog.ui.theme.Stone900
import com.iurispraecepta.herolog.ui.theme.Stone950

// ─── Cores sem equivalente nomeado no tema global (mesmo padrão de HistoryScreen.kt/TodosScreen.kt) ───
private val Champagne400 = Color(0xFFE5C158)
private val Champagne500 = Color(0xFFD4AF37)
private val Amber300 = Color(0xFFFCD34D)
private val Stone300 = Color(0xFFD6D3D1)
private val Purple200 = Color(0xFFE9D5FF)
private val Purple300 = Color(0xFFD8B4FE)
private val Purple400 = Color(0xFFC084FC)
private val Purple500 = Color(0xFFA855F7)
private val Purple950 = Color(0xFF3B0764)
private val Red200 = Color(0xFFFECACA)
private val Red300 = Color(0xFFFCA5A5)
private val Red400 = Color(0xFFF87171)
private val Red500 = Color(0xFFEF4444)
private val Red950 = Color(0xFF450A0A)
private val Rose400 = Color(0xFFFB7185)
private val Rose500 = Color(0xFFF43F5E)

/**
 * Porte fiel de GuideTab.tsx (src/modules/kingdom/GuideTab.tsx, 183L) + o cabeçalho
 * de painel definido no wrapper de App.tsx (activeTab === 'guide').
 *
 * Conteúdo 100% estático — nenhuma prop, nenhum state. Único cuidado de porte foi
 * fidelidade literal do texto (fonte extensa em português com termos de RPG).
 *
 * Nota sobre ícones: nenhum ícone lucide-react usado aqui (Swords, Skull, Compass,
 * Shield, Coins, RotateCcw, Sparkles, Flame) tem equivalente 1:1 no Material Icons
 * clássico. Mapeamento por aproximação semântica, mesmo padrão já documentado em
 * HeroLogBottomNav.kt:
 *   Sparkles → AutoAwesome | Swords → SportsMartialArts | Shield → Shield (exato)
 *   Compass → Explore | Coins → MonetizationOn | RotateCcw → Restore
 *   Flame → LocalFireDepartment | Skull → Warning (mesma escolha já usada em
 *   IncursionModeModal.kt pro mesmo conceito de "Modo Selvagem")
 * `SportsMartialArts` e `Restore` ainda não confirmados por build real neste bloco —
 * pendente de Bruno rodar assembleDebug (mesmo processo do Castle/Checklist no Bloco
 * de navegação).
 *
 * Nota sobre `border-stone-850` (card "Modo Padrão" na fonte): `stone-850` não existe
 * na paleta padrão do Tailwind nem está definido em `index.css` (`@theme` só define
 * quest-* / ice-* / champagne-*) — classe inválida na fonte real, renderiza sem cor de
 * borda customizada no app React. Aproximado aqui como Stone900 (quase imperceptível,
 * mesmo efeito prático do bug de origem). Não é um achado que precise de correção na
 * fonte, só uma decisão de porte para uma classe que já não fazia nada no React.
 */
@Composable
fun GuideScreen(modifier: Modifier = Modifier) {
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
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                contentDescription = null,
                tint = Champagne400,
                modifier = Modifier.height(18.dp).width(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "TUTORIAL",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                letterSpacing = 0.12.em,
                color = Champagne400
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Seção 1: Como Funciona o Jogo? ──
            GuideSectionCard(
                backgroundColor = Amber500.copy(alpha = 0.02f),
                borderColor = Amber500.copy(alpha = 0.10f)
            ) {
                GuideSectionHeader(icon = Icons.Filled.AutoAwesome, iconTint = Amber500, title = "Como Funciona o Jogo?")
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "HeroLog funde a famosa técnica Pomodoro com elementos clássicos de RPG de Fantasia Escura. Cada minuto dedicado ao estudo desenvolve suas verdadeiras habilidades, gera fortunas de Ouro do Reino (GP) e concede experiência (XP) ao seu nível de combate heróico.",
                    fontFamily = FontFamily.Serif,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = Amber100.copy(alpha = 0.80f)
                )
            }

            // ── Seção 2: Escolha sua Classe e Atributos ──
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                GuideSectionHeader(icon = Icons.Filled.SportsMartialArts, iconTint = Amber500, title = "Escolha sua Classe e Atributos:")
                BulletItem(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = Amber100, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold)) {
                            append("Mago (Mage):")
                        }
                        append(" O arquétipo do intelecto puro. Concede ")
                        withStyle(SpanStyle(color = Amber400, fontWeight = FontWeight.Bold)) {
                            append("+20% de bônus de XP")
                        }
                        append(" fixo ao concluir qualquer Missão de Foco.")
                    }
                )
                BulletItem(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = Amber100, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold)) {
                            append("Guerreiro (Warrior):")
                        }
                        append(" Robustez e disciplina firme. Concede ")
                        withStyle(SpanStyle(color = Amber400, fontWeight = FontWeight.Bold)) {
                            append("+20% de bônus de Ouro (GP)")
                        }
                        append(" fixo em todas as aventuras.")
                    }
                )
                BulletItem(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = Amber100, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold)) {
                            append("Patrulheiro (Ranger):")
                        }
                        append(" Velocidade e rastro de precisão. Concede ")
                        withStyle(SpanStyle(color = Amber400, fontWeight = FontWeight.Bold)) {
                            append("+15% de bônus de Série de Ofício")
                        }
                        append(", reduzindo as penalidades por atraso na consistência.")
                    }
                )
            }

            // ── Seção 3: Modos de Incursão de Foco ──
            GuideSectionCard(
                backgroundColor = Stone950.copy(alpha = 0.40f),
                borderColor = Amber500.copy(alpha = 0.15f)
            ) {
                GuideSectionHeader(icon = Icons.Filled.Explore, iconTint = Amber500, title = "Modos de Incursão de Foco")
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "O HeroLog oferece três modos de foco (incursões) adaptados para diferentes níveis de compromisso e tolerância a riscos. Ajuste seu modo no painel de preparação antes de iniciar uma sessão:",
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = Amber100.copy(alpha = 0.70f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Modo Padrão
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Stone900.copy(alpha = 0.30f))
                            .border(1.dp, Stone900, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Icon(Icons.Filled.Shield, contentDescription = null, tint = Stone400, modifier = Modifier.height(14.dp).width(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "MODO PADRÃO (NEUTRO)",
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                letterSpacing = 0.08.em,
                                color = Stone300
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(color = Stone300)) { append("Regras: ") }
                                append("Sem regras especiais ou restrições. É o caminho seguro e flexível.\n")
                                withStyle(SpanStyle(color = Stone300)) { append("Recompensas: ") }
                                append("Multiplicadores padrão (neutro), sem bônus adicionais.")
                            },
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = Amber100.copy(alpha = 0.65f)
                        )
                    }

                    // Modo Masmorra
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Purple950.copy(alpha = 0.08f))
                            .border(1.dp, Purple500.copy(alpha = 0.20f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Icon(Icons.Filled.SportsMartialArts, contentDescription = null, tint = Purple400, modifier = Modifier.height(14.dp).width(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "MODO MASMORRA (DUNGEON)",
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                letterSpacing = 0.08.em,
                                color = Purple400
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(SpanStyle(color = Purple300, fontWeight = FontWeight.Bold)) { append("Regras da Jornada: ") }
                                    append("Exige o compromisso absoluto de realizar ")
                                    withStyle(SpanStyle(color = Purple200, fontWeight = FontWeight.Bold)) { append("4 sessões consecutivas") }
                                    append(" de foco sem abandonar a jornada.")
                                },
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                color = Amber100.copy(alpha = 0.65f)
                            )
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(SpanStyle(color = Purple300, fontWeight = FontWeight.Bold)) { append("Recompensas Magnas: ") }
                                    append("Receba ")
                                    withStyle(SpanStyle(color = Purple200, fontWeight = FontWeight.Bold)) { append("+50% de XP por minuto") }
                                    append(" em cada sessão, rolos de saque quadruplicados (")
                                    withStyle(SpanStyle(color = Purple200, fontWeight = FontWeight.Bold)) { append("Quad Loot") }
                                    append("), ")
                                    withStyle(SpanStyle(color = Purple200, fontWeight = FontWeight.Bold)) { append("40% de chance de saque Lendário") }
                                    append(" e um bônus monumental de ")
                                    withStyle(SpanStyle(color = Purple200, fontWeight = FontWeight.Bold)) { append("+2.500 GP") }
                                    append(" ao concluir as 4 sessões.")
                                },
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                color = Amber100.copy(alpha = 0.65f)
                            )
                            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                Icon(Icons.Filled.Restore, contentDescription = null, tint = Purple300.copy(alpha = 0.80f), modifier = Modifier.height(11.dp).width(11.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Tempo de recarga de 2 horas após a conclusão. Não acumulável com o Modo Selvagem.",
                                    fontFamily = FontFamily.Serif,
                                    fontStyle = FontStyle.Italic,
                                    fontSize = 10.sp,
                                    color = Purple300.copy(alpha = 0.80f)
                                )
                            }
                        }
                    }

                    // Modo Selvagem
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Red950.copy(alpha = 0.08f))
                            .border(1.dp, Red500.copy(alpha = 0.20f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Icon(Icons.Filled.Warning, contentDescription = null, tint = Red400, modifier = Modifier.height(14.dp).width(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "MODO SELVAGEM (WILDERNESS)",
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                letterSpacing = 0.08.em,
                                color = Red400
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(color = Red300, fontWeight = FontWeight.Bold)) { append("Regras da Jornada: ") }
                                append("Voto cognitivo severo. Qualquer tentativa de minimizar ou trocar de aba do navegador convoca a morte e falha o bônus automaticamente (perda instantânea da recompensa da sessão).\n")
                                withStyle(SpanStyle(color = Red300, fontWeight = FontWeight.Bold)) { append("Recompensas Magnas: ") }
                                append("Sobreviventes ganham ")
                                withStyle(SpanStyle(color = Red200, fontWeight = FontWeight.Bold)) { append("+25% de XP & GP extras") }
                                append(" no fechamento do foco.")
                            },
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = Amber100.copy(alpha = 0.65f)
                        )
                    }
                }

                // Tabela Comparativa Lado a Lado
                Spacer(modifier = Modifier.height(14.dp))
                run {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Amber500.copy(alpha = 0.10f))
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Icon(Icons.Filled.MonetizationOn, contentDescription = null, tint = Amber400, modifier = Modifier.height(11.dp).width(11.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "TABELA RÁPIDA (RISCO VS. RECOMPENSA)",
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Black,
                                fontSize = 9.5.sp,
                                letterSpacing = 0.10.em,
                                color = Amber400
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(Stone950.copy(alpha = 0.40f))
                                .border(1.dp, Stone900, RoundedCornerShape(4.dp))
                        ) {
                            // Cabeçalho da tabela
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Stone900.copy(alpha = 0.60f))
                                    .padding(vertical = 8.dp, horizontal = 10.dp)
                            ) {
                                TableHeaderCell("Modo", Modifier.weight(0.8f))
                                TableHeaderCell("Compromisso", Modifier.weight(1f))
                                TableHeaderCell("Bônus Ativo", Modifier.weight(1.3f))
                                TableHeaderCell("Penalidade de Falha", Modifier.weight(1.2f))
                            }
                            androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Stone900))
                            // Linha Padrão
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 10.dp)) {
                                TableCell("Padrão", Stone300, FontWeight.SemiBold, Modifier.weight(0.8f))
                                TableCell("Flexível / Livre", Amber100.copy(alpha = 0.65f), FontWeight.Normal, Modifier.weight(1f))
                                TableCell("Nenhum", Stone400, FontWeight.Normal, Modifier.weight(1.3f))
                                TableCell("Sem punição extra", Amber100.copy(alpha = 0.65f), FontWeight.Normal, Modifier.weight(1.2f))
                            }
                            androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Stone900))
                            // Linha Masmorra
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Purple950.copy(alpha = 0.15f))
                                    .padding(vertical = 8.dp, horizontal = 10.dp)
                            ) {
                                TableCell("Masmorra", Purple400, FontWeight.SemiBold, Modifier.weight(0.8f), FontFamily.Serif)
                                TableCell("Alto (4 sessões)", Amber100.copy(alpha = 0.65f), FontWeight.Normal, Modifier.weight(1f))
                                TableCell("+50% XP, Quad Loot, +2500 GP", Purple300, FontWeight.Medium, Modifier.weight(1.3f))
                                TableCell("Perda de progresso do combo", Red400.copy(alpha = 0.80f), FontWeight.Normal, Modifier.weight(1.2f))
                            }
                            androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Stone900))
                            // Linha Selvagem
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Red950.copy(alpha = 0.15f))
                                    .padding(vertical = 8.dp, horizontal = 10.dp)
                            ) {
                                TableCell("Selvagem", Red400, FontWeight.SemiBold, Modifier.weight(0.8f), FontFamily.Serif)
                                TableCell("Extremo (Foco Único)", Amber100.copy(alpha = 0.65f), FontWeight.Normal, Modifier.weight(1f))
                                TableCell("+25% XP & GP extras", Red300, FontWeight.Medium, Modifier.weight(1.3f))
                                TableCell("Morte (Perda dos bônus e ganhos)", Red400, FontWeight.Normal, Modifier.weight(1.2f))
                            }
                        }

                        // Guia de Decisão
                        Spacer(modifier = Modifier.height(14.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Amber500.copy(alpha = 0.01f))
                                .border(1.dp, Amber500.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "ORIENTAÇÃO: QUANDO ESCOLHER CADA MODO?",
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Black,
                                fontSize = 9.5.sp,
                                letterSpacing = 0.08.em,
                                color = Amber300
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                BulletItem(
                                    text = buildAnnotatedString {
                                        withStyle(SpanStyle(color = Stone300, fontWeight = FontWeight.Bold)) { append("Escolha o Modo Padrão: ") }
                                        append("Quando precisar de foco maleável, onde interrupções externas, trocas de aba rápidas ou pausas imprevistas podem ocorrer.")
                                    },
                                    fontSize = 10.5.sp,
                                    lineHeight = 15.sp,
                                    textColor = Amber100.copy(alpha = 0.60f),
                                    indent = 16.dp
                                )
                                BulletItem(
                                    text = buildAnnotatedString {
                                        withStyle(SpanStyle(color = Purple300, fontWeight = FontWeight.Bold)) { append("Escolha o Modo Masmorra: ") }
                                        append("Para maratonas intensas e estruturadas de estudo (ex: blocos de 2 horas), potencializando a experiência heróica e a conquista de tesouros lendários de alto valor.")
                                    },
                                    fontSize = 10.5.sp,
                                    lineHeight = 15.sp,
                                    textColor = Amber100.copy(alpha = 0.60f),
                                    indent = 16.dp
                                )
                                BulletItem(
                                    text = buildAnnotatedString {
                                        withStyle(SpanStyle(color = Red300, fontWeight = FontWeight.Bold)) { append("Escolha o Modo Selvagem: ") }
                                        append("Quando precisar de disciplina absoluta contra distrações digitais, usando o perigo iminente de perder o progresso como âncora de atenção inquebrável.")
                                    },
                                    fontSize = 10.5.sp,
                                    lineHeight = 15.sp,
                                    textColor = Amber100.copy(alpha = 0.60f),
                                    indent = 16.dp
                                )
                            }
                        }
                    }
                }
            }

            // ── Seção 4: Terra Selvagem (destaque vermelho) ──
            GuideSectionCard(
                backgroundColor = Red500.copy(alpha = 0.02f),
                borderColor = Red500.copy(alpha = 0.10f)
            ) {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Icon(Icons.Filled.Warning, contentDescription = null, tint = Rose500, modifier = Modifier.height(16.dp).width(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "💀 Terra Selvagem (Modo Wilderness):",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Rose400
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "A Wilderness é uma região de altíssimo perigo cognitivo. Se você ativá-lo antes de iniciar a Missão de Foco:",
                    fontFamily = FontFamily.Serif,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = Amber100.copy(alpha = 0.70f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    BulletItem(
                        text = buildAnnotatedString { append("Qualquer tentativa de sair do navegador ou trocar de aba invoca a sombra da morte.") },
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        textColor = Amber100.copy(alpha = 0.50f)
                    )
                    BulletItem(
                        text = buildAnnotatedString {
                            append("Você escutará o bater rápido de seu próprio coração e receberá um alerta crítico com ")
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("3 segundos de período de carência") }
                            append(" para retornar antes da punição total.")
                        },
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        textColor = Amber100.copy(alpha = 0.50f)
                    )
                    BulletItem(
                        text = buildAnnotatedString {
                            append("A morte causa perda instantâneo de toda a recompensa acumulada na sessão atual. No entanto, os fortes que sobrevivem à incursão ganham um multiplicador glorioso de ")
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("+25% extras de Ouro e XP") }
                            append(".")
                        },
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        textColor = Amber100.copy(alpha = 0.50f)
                    )
                }
            }

            // ── Seção 5: Combo de consistência (streak) ──
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                GuideSectionHeader(icon = Icons.Filled.LocalFireDepartment, iconTint = Amber500, title = "Combo de consistência (streak):")
                Text(
                    text = buildAnnotatedString {
                        append("Sessões completas sequencialmente geram um combo multiplicador acumulado de ")
                        withStyle(SpanStyle(color = Amber400, fontWeight = FontWeight.Bold)) { append("+5% bônus de XP por foco extra consecutivo") }
                        append(", até o teto místico de +50% no total. Não desista e mantenha a chama viva!")
                    },
                    fontFamily = FontFamily.Serif,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = Amber100.copy(alpha = 0.60f)
                )
            }
        }
    }
}

@Composable
private fun GuideSectionCard(
    backgroundColor: Color,
    borderColor: Color,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(16.dp),
        content = content
    )
}

@Composable
private fun GuideSectionHeader(icon: ImageVector, iconTint: Color, title: String) {
    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.height(16.dp).width(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = Amber300
        )
    }
}

@Composable
private fun BulletItem(
    text: androidx.compose.ui.text.AnnotatedString,
    fontSize: androidx.compose.ui.unit.TextUnit = 12.sp,
    lineHeight: androidx.compose.ui.unit.TextUnit = 17.sp,
    textColor: Color = Amber100.copy(alpha = 0.60f),
    indent: androidx.compose.ui.unit.Dp = 20.dp
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.width(indent))
        Text(text = "•", color = textColor, fontSize = fontSize)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontFamily = FontFamily.Serif,
            fontSize = fontSize,
            lineHeight = lineHeight,
            color = textColor,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TableHeaderCell(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        color = Amber300
    )
}

@Composable
private fun TableCell(
    text: String,
    color: Color,
    weight: FontWeight,
    modifier: Modifier = Modifier,
    fontFamily: FontFamily? = null
) {
    Text(
        text = text,
        modifier = modifier,
        fontFamily = fontFamily,
        fontWeight = weight,
        fontSize = 10.sp,
        color = color
    )
}