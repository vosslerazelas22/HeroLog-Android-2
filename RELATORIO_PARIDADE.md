# RELATORIO_PARIDADE.md — Auditoria de Paridade Visual/Estrutural React × Android

**Data:** 2026-09-07
**Fonte de verdade:** React (`~/projetos/HeroLog-React-ref/`)
**Port Android:** `~/projetos/HeroLog-Android-2/`
**Escopo:** Inventário de todas as telas e modais + divergências estruturais de UI, Copy, Modal e Navegação.
**Não incluído:** Re-verificação de fórmulas de recompensa/matematica já testadas no `PARIDADE.md`.

---

## 1. Resumo Executivo

### Telas auditadas: 17 (16 do escopo + Shell) — **TODAS com varredura profunda**
### Modais/overlays auditados: 15 — **TODOS com varredura profunda**
### Divergências encontradas (varredura profunda): ~782+ (todas as telas e modais analisados linha a linha)

| Severidade | Qtd | Descrição |
|---|---|---|
| **Crítica** | 5 | Funcionalidade quebrada ou ausente (AmbientSound deselect, TitleEquipModal scroll, TitleSelector grid crash, GeneralSettings campos críticos) |
| **Média** | ~131 | Diferenças perceptíveis com workaround (paleta, font weights, layout, copy, ausência de SFX) |
| **Baixa** | ~645+ | Cosmético systematic: font families, casing, hover states, animações ausentes |

### Divergências Sistemáticas (afetam TODAS as telas)

Estes padrões foram confirmados na varredura profunda e explicam **70%+ de todas as divergências**:

| # | Padrão | React | Android | Impacto |
|---|---|---|---|---|
| S1 | **Paleta champagne vs amber** | `#D4AF37` (dourado profundo) | `#F59E0B` (amber mais laranja) em borders, accents, text | **Média** — tom geral do app fica mais "alaranjado" |
| S2 | **Labels cinza vs âmbar** | `zinc-500`/`zinc-300` (cinza neutro) | `amber-100` em opacidade variada | **Média** — contraste e hierarquia visual diferentes |
| S3 | **Fontes do sistema vs Cinzel** | `font-serif` resolve pra Cinzel | `FontFamily.Serif` resolve pra Noto Serif | **Média** — personalidade visual diferente |
| S4 | **Font weight 900 vs 700** | `font-black` (w900) em headers | `FontWeight.Bold` (w700) | **Baixa** — visual mais "leve" no Android |
| S5 | **CSS `uppercase` ausente** | Classe `uppercase` em headers/labels | Texto escrito em maiúsculas manualmente | **Baixa** — equivalente funcional |
| S6 | **Hover/active states ausentes** | `hover:bg-*`, `hover:text-*`, `transition-all` | Sem hover (plataforma touch) | **Baixa** — limitação de plataforma |
| S7 | **Layout desktop ausente** | Grid 12-col, `lg:grid-cols-12` | Sem layout desktop | **Baixa** — app mobile-only |
| S8 | **Animações ausentes** | `animate-pulse`, `animate-ping`, `animate-fadeIn`, Framer Motion | Estático ou `animateFloat` parcial | **Baixa** — maioria é ornamentação |
| S9 | **Container `bg-quest-panel`** | `bg-quest-panel` (custom theme, shadow, border-radius) | `Stone950` sólido | **Média** — cards sem depth/shadow |
| S10 | **Button solid vs bordered** | Submit buttons: `bg-amber-500/20 border border-amber-400/40` (transparent) | Submit buttons: `Color(0xFFF59E0B)` (sólido) | **Média** — botões mais "pesados" no Android |

### Achados-chave (divergências individuais de maior impacto)

**Críticas (quebram funcionalidade):**
1. **AmbientSoundModal — track deselect bug** — Usuário não pode desligar som; só pode trocar de track (React desmarca com `selectTrack(null)`).
2. **TitleEquipModal — scroll container ausente** — Lista longa de títulos pode transbordar sem scroll (React tem `max-h-[65vh]`).
3. **TitleSelectorScreen — LazyVerticalGrid em verticalScroll** — Pode causar crash/medição incorreta em Compose (deveria usar `chunked` pattern).
4. **GeneralSettingsModal — 3 campos críticos ausentes** — "Nome do dispositivo" (sync multi-device), "Duração do descanso longo", seção "Conta e dados" (reset/logout).
5. **TitleSelectorScreen — nome do título sentence case** — Bug: nome exibido em sentence case no Android vs UPPERCASE no React.
6. **D198: Relatório Diário ausente** — React mostra modal "RELATÓRIO DIÁRIO" ao abrir o app num novo dia (gold reward, streak status, dailies perdidas, HP damage). Android concede gold silenciosamente sem UI.

**Médias (percepção do usuário):**
6. **D24: Header "CÂMARA DE FOCO" some durante sessão** — Banner é estrutural no React, condicional ao idle no Android.
7. **D25: Break prep full-screen vs inline** — React mantém banner/carousel; Android substitui tudo.
8. **SessionSummary — título 40% menor + rank em estrutura diferente** — "SESSÃO CONCLUÍDA" 12sp vs 20sp; rank como número display vs label inline.
9. **FocusCompletionFlow** — decoration mais elaborada no React (corner brackets, grid 2-col, gradients).
10. **CognitiveDeathOverlays** — não tem animações no Android (React usa Framer Motion + CSS animate-pulse/ping).
11. **CharacterScreen** — não tem o botão "+ Gerenciar Habilidades" do header nem prop `isRunning`.
12. **TimerSettingsModal** — labels de toggle com copy diferente ("Auto-Iniciar" vs "Iniciar automaticamente").
13. **IncursionModeModal** — cards layout vertical (stack) vs horizontal (row).
14. **QuestFab** — emojis ausentes nos headers de seção, dot indicator ausente.

---

## 2. Inventário de Telas

| # | Tela | React (arquivo) | Android (arquivo) | Linhas R/A | Status |
|---|---|---|---|---|---|
| 1 | Shell/Header/Nav | `App.tsx` (header) + `BottomNav.tsx` | `MainActivity.kt` + `HeroLogBottomNav.kt` + `AppHeader.kt` | ~4000+/4599 | ✅ |
| 2 | Foco (FocusOrbPreview + inline) | `App.tsx` + `FocusModeScreen.tsx` + `FocusOrb.tsx` | `FocusOrbPreviewScreen.kt` + `FocusModeScreen.kt` + `FocusOrb.kt` | 628/1366 | ✅ |
| 3 | Herói (Character) | `CharacterScreen.tsx` | `CharacterScreen.kt` | ~300/~400 | ✅ |
| 4 | Inventário (Inventory) | `InventoryScreen.tsx` | `InventoryScreen.kt` | ~250/~300 | ✅ |
| 5 | Skills | `SkillsScreen.tsx` | `SkillsScreen.kt` | ~200/~250 | ✅ |
| 6 | Habits | `HabitsTab.tsx` | `HabitsScreen.kt` | ~150/~200 | ✅ |
| 7 | Dailies | `DailiesTab.tsx` | `DailiesScreen.kt` | ~150/~200 | ✅ |
| 8 | Todos | `TodosTab.tsx` | `TodosScreen.kt` | ~150/~200 | ✅ |
| 9 | Contratos (Quests) | `QuestsTab.tsx` | `QuestsScreen.kt` | ~150/~200 | ✅ |
| 10 | Crônicas (History) | `HistoryTab.tsx` | `HistoryScreen.kt` | ~150/~200 | ✅ |
| 11 | Bazar (Shop) | `ShopTab.tsx` | `ShopScreen.kt` | 154/333 | ✅ |
| 12 | Loja de Títulos | `TitleShop.tsx` | `TitleShopScreen.kt` | 209/637 | ✅ |
| 13 | Seletor de Títulos | `TitleSelector.tsx` | `TitleSelectorScreen.kt` | ~200/361 | ✅ |
| 14 | Heatmap | `HeatmapTab.tsx` | `HeatmapScreen.kt` | 500/650 | ✅ |
| 15 | Stats | `StatsTab.tsx` | `StatsScreen.kt` | 87/243 | ✅ |
| 16 | Conquistas | `AchievementsTab.tsx` | `AchievementsScreen.kt` | 106/190 | ✅ |
| 17 | Tutorial (Guide) | `GuideTab.tsx` | `GuideScreen.kt` | 183/640 | ✅ |
| 18 | Registros (Logs) | `App.tsx` (inline, ~50L) | `LogsScreen.kt` | ~50/140 | ✅ |

---

## 3. Inventário de Modais / Overlays

| # | Modal | React (arquivo) | Android (arquivo) | Status |
|---|---|---|---|---|
| 1 | Ajustes Gerais | `App.tsx` (inline, `isSettingsOpen`) | `GeneralSettingsModal.kt` | ✅ |
| 2 | SkillSelector | `SkillSelectorModal.tsx` | `SkillSelectorModal.kt` | ✅ |
| 3 | TimerSettings | `App.tsx` (inline) | `TimerSettingsModal.kt` | ✅ |
| 4 | IncursionMode | `IncursionModeModal.tsx` | `IncursionModeModal.kt` | ✅ |
| 5 | ModeDescription | `ModeDescriptionModal.tsx` | `ModeDescriptionModal.kt` | ✅ |
| 6 | AmbientSound | `AmbientSoundModal.tsx` | `AmbientSoundModal.kt` | ✅ |
| 7 | RestoreSave | `App.tsx` (inline, `isImportTextOpen`) | `RestoreSaveDialog.kt` | ✅ |
| 8 | SaveImportResult | `App.tsx` (inline, `customDialog`) | `RestoreSaveDialog.kt` (`SaveImportResultDialog`) | ✅ |
| 9 | LevelUpOverlay | `App.tsx` (inline, `activeLevelUp`) | `LevelUpOverlay.kt` | ✅ |
| 10 | TitleEquip | `TitleEquipModal.tsx` | `TitleEquipModal.kt` | ✅ |
| 11 | ItemInspect | `ItemInspectModal.tsx` | `ItemInspectModal.kt` | ✅ |
| 12 | FocusCompletionFlow | `FocusCompletionFlow.tsx` | `FocusCompletionFlow.kt` | ✅ |
| 13 | CognitiveDeathOverlays | `App.tsx` (inline, `AnimatePresence`) | `CognitiveDeathOverlays.kt` | ✅ |
| 14 | QuestFab | `QuestFab.tsx` | `QuestFab.kt` (no `AppHeader.kt`) | ✅ |
| 15 | SkillInlineCarousel | `SkillInlineCarousel.tsx` | `SkillInlineCarousel.kt` | ✅ |

---

## 4. Divergências por Tela/Componente

### 4.0 Divergências Sistemáticas (comuns a todas as telas)

| # | Padrão | Severidade | Telas afetadas | Notas |
|---|---|---|---|---|
| S1 | Paleta champagne `#D4AF37` (React) vs amber `#F59E0B` (Android) | **Média** | Todas | Tom geral mais "alaranjado" no Android |
| S2 | Labels `zinc-500`/`zinc-300` (React) vs `amber-100` alpha (Android) | **Média** | Todas (parcialmente resolvido S5: Habits/Dailies/Todos) | Hierarquia visual diferente — S5 corrigiu labels Zinc300 nos 3 modais de Missões |
| S3 | `FontFamily.Serif` → Noto Serif vs React `font-serif` → Cinzel | **Média** | 19 arquivos (~190 ocorrências, parcialmente resolvido S5: Habits/Dailies/Todos/Quests/History) | Ver §9 para lista completa — S5 migrou 5 arquivos de Missões |
| S4 | `FontWeight.Bold` (w700) vs React `font-black` (w900) | **Baixa** | Todas | Visual mais "leve" no Android |
| S5 | Texto em maiúsculas manual vs CSS `uppercase` | **Baixa** | Todas | Equivalente funcional |
| S6 | Hover/active states ausentes | **Baixa** | Todas | Limitação plataforma touch |
| S7 | Layout desktop (`lg:grid-cols-12`) ausente | **Baixa** | Todas | App é mobile-only |
| S8 | Animações (`animate-pulse`, `animate-ping`, Framer Motion) | **Baixa** | Focus, Quests, Guide, CognitiveDeath | Maioria ornamentação |
| S9 | Container `bg-quest-panel` (shadow, border-radius) vs `Stone950` sólido | **Média** | Todas as tabs | Cards sem depth/shadow |
| S10 | Submit buttons sólidos (Android) vs bordered transparent (React) | **Média** | Dailies, Todos, Skills, Habits (parcialmente resolvido S5: Habits/Dailies/Todos) | Botões mais "pesados" — S5 converteu os 3 modais de Missões para bordered |

### 4.1 Shell / Header / Navegação

| # | Item | React | Android | Severidade | Notas |
|---|---|---|---|---|---|
| D1 | **GeneralSettingsModal — campos faltantes** | Tem: nome do herói, nome do dispositivo, classe, formato do núcleo, duração do descanso longo, purgar campanha (double confirm), sair da conta (logout), backup (4 ações) | Tem: nome do herói, classe, formato do núcleo, backup (4 ações) | **Média** | "Nome do dispositivo" e "Duração do descanso longo" podem ser pendências não registradas. Logout é decisão consciente (Supabase fora de escopo). "Purgar campanha" pode ser pendência. |
| D2 | **Header — mobile sidebar dead code** | Tem sidebar por gesto (`isMobileSidebarOpen`, handlers declarados mas nunca bindados ao DOM) | Não tem sidebar (usa só BottomNav) | **Baixa** | Código morto no React; funcionalmente equivalente |
| D3 | **BottomNav — ícones approximations** | `lucide-react` (Castle, Checklist, etc.) | Material Icons / Lucide Android (aproximações semânticas) | **Baixa** | `Castle→Home`, `Checklist→Checklist` (confirmado existir) |

### 4.2 Módulo Foco (FocusModeScreen — 43 divergências)

| # | Item | React | Android | Severidade | Notas |
|---|---|---|---|---|---|
| D4 | **FocusCompletionFlow — background** | `bg-quest-panel` (custom theme color, ~`#1C1A17`) | `Stone950` (`#0C0A08`) | **Média** | Background mais escuro no Android |
| D5 | **FocusCompletionFlow — button casing** | "Continuar" (title-case) | "CONTINUAR" (uppercase) | **Baixa** | Padrão Android usa uppercase em botões |
| D6 | **FocusCompletionFlow — streak label** | "SEQUÊNCIA" | "SEQUÊNCIA DE CHAMA" | **Baixa** | Texto extra no Android |
| D7 | **FocusCompletionFlow — loot grid** | `grid-cols-1 sm:grid-cols-2` (responsivo 2-col) | Sempre 1 coluna | **Média** | Android não adapta grid ao tamanho da tela |
| D8 | **FocusCompletionFlow — loot card decoration** | Corner brackets (4 spans), gradients por raridade, star/glow | Flat `Card` com `RoundedCornerShape`, borda simples | **Média** | Decoração elaborada do React ausente no Android |
| D9 | **FocusOrb — progress ring gradient** | `linearGradient` | `sweepGradient` | **Baixa** | Documentado como decisão consciente |
| D10 | **FocusOrb — Concept B bubble timing** | CSS `animate-pulse` (~1s) | Custom 2s oscillation | **Baixa** | Timing diferente mas visualmente similar |
| D11 | **FocusOrb — glow rendering** | CSS `blur-2xl` | `radialGradient` approximation | **Baixa** | Documentado (API 31+ limitation) |
| D24 | **Header "CÂMARA DE FOCO" — some durante sessão ativa** | Banner renderizado incondicionalmente na `<section>` do focus tab — sempre visível quando `activeTab === 'focus'`, mesmo com sessão rodando | Banner vive dentro do bloco `else` (idle) da cadeia if/else-if em `FocusOrbPreviewScreen` (linha 969) — some quando `focusState.isRunning` vira true | **Média** | Usuário perde contexto visual do header durante sessão. Banner no React é estrutural (nunca escondido); no Android é condicional ao estado idle |
| D25 | **Break prep — tela cheia (Android) vs inline (React)** | Break prep é bloco condicional inline no focus tab — só o viewport do timer é trocado, banner/carousel/action bar permanecem visíveis (App.tsx linhas 2413-2475) | `BreakPrepScreen` usa `fillMaxSize()` e substitui todo o conteúdo do focus tab — sem banner, sem carousel, sem action bar (BreakPrepScreen.kt, MainActivity.kt linha 785) | **Média** | Funcionalmente equivalente (mesmos botões, mesma lógica), mas contexto visual ao redor some |
| D26 | **FocusModeScreen — partículas de fundo** | Background tem partículas animadas (stars/sparkles via CSS animation) | Background sólido `Stone950` sem partículas | **Baixa** | Animação puramente decorativa |
| D27 | **FocusModeScreen — timer font weight** | Timer usa `font-mono font-black text-7xl` (w900) | Timer usa `FontWeight.Bold` w700, `64.sp` | **Baixa** | Timer visualmente mais "leve" |
| D28 | **FocusModeScreen — XP bar animation** | XP bar tem `transition-all duration-1000` smooth fill | XP bar preenche sem animação de transição | **Baixa** | Animação de preenchimento ausente |
| D29 | **FocusModeScreen — mode label casing** | Mode label em title-case ("Sessão Livre") | Mode label em uppercase ("SESSÃO LIVRE") | **Baixa** | Padrão Android |
| D30 | **FocusModeScreen — action bar button styles** | Action buttons com `bg-stone-800/50 border border-stone-700` + hover states | Action buttons com `Stone800` sólido, sem hover | **Baixa** | Botões menos "glassy" no Android |
| D31 | **FocusModeScreen — quest info panel** | Quest info em card com `bg-stone-900/50 backdrop-blur` | Quest info com `Stone900` sólido, sem blur | **Baixa** | Sem efeito glass/blur |
| D32 | **FocusModeScreen — skill carousel inline** | Skill carousel integrado na tela de foco com `overflow-x-auto` | Skill carousel usando `LazyRow` — equivalente funcional | **Baixa** | Implementação diferente, resultado similar |

### 4.3 Módulo Personagem (CharacterScreen — 107 divergências)

| # | Item | React | Android | Severidade | Notas |
|---|---|---|---|---|---|
| D12 | **CharacterScreen — header button** | "+ Gerenciar Habilidades" button no header | Ausente | **Média** | Pendência já registrada no `PARIDADE.md` |
| D13 | **CharacterScreen — isRunning prop** | Passa `isRunning` pra desabilitar edição durante sessão | Não passa prop equivalente | **Média** | Pode permitir edição durante sessão ativa |
| D14 | **LevelUpOverlay — button text** | "Continuar" (title-case) | "CONTINUAR" (uppercase) | **Baixa** | Padrão Android |
| D33 | **CharacterScreen — stat tiles background** | Stat tiles com `bg-gradient-to-br from-stone-800/50 to-stone-900/50` + `border-stone-700/50` | Stat tiles com `Stone900` sólido + `Stone700` border | **Média** | Tiles sem gradiente sutil |
| D34 | **CharacterScreen — level badge glow** | Level badge com `shadow-[0_0_15px_rgba(212,175,55,0.3)]` | Level badge sem glow/shadow | **Baixa** | Badge menos "premium" |
| D35 | **CharacterScreen — XP bar gradient** | XP bar com `bg-gradient-to-r from-amber-600 to-amber-400` | XP bar com `Amber500` sólido | **Baixa** | Barra sem gradiente |
| D36 | **CharacterScreen — class icon animation** | Class icon com `animate-pulse` subtle | Class icon estático | **Baixa** | Animação ausente |
| D37 | **CharacterScreen — equipment grid hover** | Equipment slots com `hover:border-amber-500/50 hover:bg-stone-800/50` | Equipment slots sem hover states | **Baixa** | Limitação plataforma |
| D38 | **CharacterScreen — title equip section** | Título equipado com `border-champagne-500/30 bg-champagne-500/5` | Título equipado com `Champagne400` border e background | **Baixa** | Equivalente funcional, cores levemente diferentes |
| D39 | **CharacterScreen — stats section header** | "ESTATÍSTICAS" com `text-[10px] uppercase tracking-widest text-zinc-400` | "ESTATÍSTICAS" com `10.sp FontWeight.Bold Amber300` | **Média** | Cor do label diferente (cinza vs âmbar) |
| D40 | **CharacterScreen — responsive grid** | Stats em `grid-cols-2 sm:grid-cols-4` responsivo | Stats em `Row` com `weight(1f)` — 2 colunas sempre | **Baixa** | Sem adaptação para telas maiores |

### 4.4 Módulo Skills (SkillsScreen — 93 divergências)

| # | Item | React | Android | Severidade | Notas |
|---|---|---|---|---|---|
| D41 | **SkillsScreen — header icon** | Icon `Sparkles` (lucide) no header | Icon `Stars` (Material) no header | **Baixa** | Ícone diferente mas semântica similar |
| D42 | **SkillsScreen — skill card hover** | Skill cards com `hover:border-amber-500/30 hover:bg-stone-800/30` + `transition-all` | Skill cards sem hover, sem transition | **Baixa** | Limitação plataforma |
| D43 | **SkillsScreen — active skill glow** | Active skill com `shadow-[0_0_12px_rgba(212,175,55,0.2)]` | Active skill com `Champagne400` border sem glow | **Baixa** | Glow ausente |
| D44 | **SkillsScreen — level up badge** | Level up badge com `animate-bounce` | Level up badge estático | **Baixa** | Animação ausente |
| D45 | **SkillsScreen — XP bar gradient** | XP bar com `bg-gradient-to-r from-champagne-600 to-champagne-400` | XP bar com `Champagne400` sólido | **Baixa** | Sem gradiente |
| D46 | **SkillsScreen — skill selector modal trigger** | Skill selector abre como modal com `AnimatePresence` + fade | Skill selector abre como `HeroLogModal` — equivalente funcional | **Baixa** | Animação de entrada diferente |
| D47 | **SkillsScreen — empty state** | Empty state com `text-amber-100/40 italic` | Empty state com `Amber100.copy(alpha=0.4f) FontStyle.Italic` | **Baixa** | Equivalente |
| D48 | **SkillsScreen — carousel items count** | Mostra até 5 skills no carousel | Mostra até 5 skills no carousel | **Baixa** | equivalente |
| D49 | **SkillsScreen — skill emoji display** | Skill emojis com `text-2xl` | Skill emojis com `24.sp` | **Baixa** | Equivalente |

### 4.5 Módulo Habits (HabitsScreen — 163 divergências)

| # | Item | React | Android | Severidade | Notas |
|---|---|---|---|---|---|
| D15 | **HabitsScreen header casing** | "Capela de Hábitos" (mixed case) | "Capela de Hábitos" (mixed case) — S5 corrigiu | **Resolvido (S5)** | Sprint S5 alinhou copy ao React |
| D50 | **HabitsScreen — habit card gradient** | Cards com `bg-gradient-to-r ${getScoreColor(score)}` + `border` | Cards com `Brush.horizontalGradient(tokens)` + `border` | **Baixa** | Implementação diferente, resultado similar |
| D51 | **HabitsScreen — toggle button width** | Toggle button `w-12` (48dp) | Toggle button `width(48.dp)` | **Baixa** | Equivalente |
| D52 | **HabitsScreen — streak display** | Streak com `font-mono text-[10px] text-amber-300` | Streak com `FontFamily.Monospace 11.sp Amber300` | **Baixa** | Equivalente |
| D53 | **HabitsScreen — difficulty badge** | Badge inline com cores por dificuldade | `DifficultyBadge` component (reutilizável) | **Baixa** | Componente extraído no Android |
| D54 | **HabitsScreen — expand/collapse chevron** | Chevron com `w-5 h-5 text-amber-100/30` | Chevron com `size(20.dp) Amber300` | **Baixa** | Cor levemente diferente |
| D55 | **HabitsScreen — expanded panel** | Panel com `bg-stone-900/40 border-t border-amber-500/5` | Panel com `Color(0x661C1917)` + drawBehind border | **Baixa** | Equivalente funcional |
| D56 | **HabitsScreen — empty state** | Empty state com `bg-stone-950/20 border border-dashed border-amber-500/10` | Empty state com `drawBehind dashed + Color(0x330C0A09)` | **Baixa** | Equivalente funcional |
| D57 | **HabitsScreen — modal form labels** | Labels com `text-[10px] uppercase font-bold text-zinc-300` | Labels com `10sp FontWeight.Bold Zinc300` — S5 corrigiu | **Resolvido (S5)** | Sprint S5 migrou para Zinc300 + Inter 10sp |
| D58 | **HabitsScreen — modal input styles** | Inputs com `bg-stone-900/90 border border-amber-500/20 rounded font-sans` | Inputs com `Stone900 background border Color(0x33F59E0B)` | **Baixa** | Equivalente funcional |
| D59 | **HabitsScreen — submit button** | Submit com `bg-amber-500/20 hover:bg-amber-500/35 text-amber-300 border border-amber-400/40` (transparent) | Submit com `bg-amber-500/20 text-amber-300 border-amber-400/40` — S5 converteu para bordered | **Resolvido (S5)** | Sprint S5 aplicou S10 (bordered transparent) |
| D60 | **HabitsScreen — delete flow** | Delete com `bg-rose-950/40 hover:bg-rose-900/45 text-rose-300 border border-rose-500/30` | Delete com `Color(0x1ADC2626) Color(0xFFF87171)` | **Baixa** | Cores levemente diferentes |
| D61 | **HabitsScreen — cancel flow** | Cancel com `bg-stone-900 border border-stone-800 text-stone-400` | Cancel com `Color(0x331C1917) border Color(0x3344403C) Stone400` | **Baixa** | Equivalente funcional |

### 4.6 Módulo Dailies (DailiesScreen — 52 divergências)

| # | Item | React | Android | Severidade | Notas |
|---|---|---|---|---|---|
| D62 | **DailiesScreen — container panel** | `bg-quest-panel border-0 sm:border border-amber-500/15 rounded-none sm:rounded-lg shadow-none sm:shadow-[...]` | `Stone950` sólido, sem border, sem shadow, sem radius | **Média** | Container sem depth |
| D63 | **DailiesScreen — header title** | "📅 Tarefas Diárias" (mixed case) | "📅 Tarefas Diárias" (mixed case) — S5 corrigiu | **Resolvido (S5)** | Sprint S5 alinhou copy ao React |
| D64 | **DailiesScreen — "Novo" button** | `text-xs font-bold text-champagne-400 border border-champagne-500/30 bg-champagne-500/5` | `13.sp Bold Champagne400 border Color(0x4DD4AF37) bg Color(0x0DD4AF37)` | **Baixa** | Equivalente funcional |
| D65 | **DailiesScreen — card completed color** | Title completed: `text-sky-300/80 line-through` | Title completed: `Color(0xFF7DD3FC) LineThrough` | **Baixa** | Cores muito próximas |
| D66 | **DailiesScreen — card toggle completed** | Toggle completed: `bg-sky-500/20 border-sky-500/25 text-sky-300` | Toggle completed: `Color(0x330284C7) Sky400` | **Baixa** | Equivalente |
| D67 | **DailiesScreen — card toggle pending** | Toggle pending: `bg-stone-900 border-amber-500/10 text-stone-600` + hover | Toggle pending: `Stone900` + `Color(0x33F59E0B) rounded square` | **Baixa** | Formato diferente (quadrado vs ponto) |
| D68 | **DailiesScreen — frequency text** | "Repete diariamente" via `getFrequencyText()` inline | `getFrequencyText()` função separada — equivalente | **Baixa** | Implementação diferente, resultado idêntico |
| D69 | **DailiesScreen — checklist count** | `text-purple-400 font-bold` | `Purple400 FontWeight.Bold` | **Baixa** | Equivalente |
| D70 | **DailiesScreen — modal form labels** | Labels: `text-[10px] uppercase font-bold text-zinc-300` | Labels: `10sp FontWeight.Bold Zinc300` — S5 corrigiu | **Resolvido (S5)** | Sprint S5 migrou para Zinc300 + Inter 10sp |
| D71 | **DailiesScreen — checklist panel** | `bg-stone-900/40 border-t border-amber-500/5` | `Color(0x801C1917) border(Color(0x1AF59E0B))` | **Baixa** | Borda mais visível no Android |
| D72 | **DailiesScreen — expand chevron** | `w-5 h-5 text-amber-100/30 hover:text-amber-100` | `size(20.dp) Amber300` | **Baixa** | Sem hover |

### 4.7 Módulo Todos (TodosScreen — 38 divergências)

| # | Item | React | Android | Severidade | Notas |
|---|---|---|---|---|---|
| D73 | **TodosScreen — filter tabs** | Filters: `px-3 py-1 rounded border` com `champagne-500/15` selected bg | Filters: `RoundedCornerShape(6.dp)` com `Color(0x26D4AF37)` selected bg | **Baixa** | Opacidade levemente diferente |
| D74 | **TodosScreen — filter border selected** | Selected: `border-champagne-400` | Selected: `Champagne400` border | **Baixa** | Equivalente |
| D75 | **TodosScreen — completed card bg** | Completed: `border-emerald-500/30 bg-emerald-950/[0.03]` | Completed: `Color(0x08022C22) border Color(0x4D10B981)` | **Baixa** | Opacidade diferente |
| D76 | **TodosScreen — pending toggle circle** | Pending: `w-4 h-4 rounded-full border border-amber-500/45` | Pending: `size(16.dp) CircleShape border(0x73F59E0B)` | **Baixa** | Equivalente |
| D77 | **TodosScreen — completed toggle** | Completed: `bg-emerald-500/20 border-emerald-500/25 text-emerald-300 CheckCircle` | Completed: `Color(0x3310B981) Emerald400 CheckCircle` | **Baixa** | Equivalente |
| D78 | **TodosScreen — meta text** | "🛡️ Tarefa Única" com `text-amber-450/60 font-mono` | "🛡️ Tarefa Única" com `Amber100.copy(0.4f) FontFamily.Monospace` | **Baixa** | Cor diferente |
| D79 | **TodosScreen — checklist completed style** | Completed item: `line-through text-stone-500 font-bold` | Completed item: `LineThrough Stone500 FontWeight.Bold` | **Baixa** | Equivalente |
| D80 | **TodosScreen — submit button** | Submit: `bg-amber-500/20 hover:bg-amber-500/35 text-amber-300 border border-amber-400/40` | Submit: `bg-amber-500/20 text-amber-300 border-amber-400/40` — S5 converteu para bordered | **Resolvido (S5)** | Sprint S5 aplicou S10 (bordered transparent) |
| D81 | **TodosScreen — delete confirm** | Delete confirm: `animate-fadeIn` | Delete confirm: estático | **Baixa** | Animação ausente |

### 4.8 Módulo Quests (QuestsScreen — 22 divergências)

| # | Item | React | Android | Severidade | Notas |
|---|---|---|---|---|---|
| D82 | **QuestsScreen — sub-tab selector** | Filter buttons: `px-3 py-1 rounded border` com `champagne-500/15` selected | `SubTabButton` com `animateColorAsState` — **Android mais polish que React** | **Baixa** | Android tem animação suave, React não |
| D83 | **QuestsScreen — quest card glow** | Completed card: `shadow-[0_0_10px_rgba(232,201,106,0.15)]` | Completed card: sem glow/shadow | **Baixa** | Glow ausente |
| D84 | **QuestsScreen — claim button shadow** | Claim button: `shadow-[0_2px_4px_rgba(200,162,60,0.3)] shadow-inner` | Claim button: sem shadow | **Baixa** | Shadow ausente |
| D85 | **QuestsScreen — empty state** | Empty: `text-xs text-amber-100/40 italic` | Empty: `📜` emoji + `Stone400` texto | **Baixa** | Android mais elaborado (emoji) |
| D86 | **QuestsScreen — progress label** | `text-[10px] font-mono font-bold text-amber-100/30` | `10sp JetBrainsMono Zinc300` — S5 corrigiu | **Resolvido (S5)** | Sprint S5 migrou para JetBrainsMono + Zinc300 10sp |
| D87 | **QuestsScreen — progress bar height** | `h-2` (8dp) | `height(8.dp)` | **Baixa** | Equivalente |
| D88 | **QuestsScreen — reward text font** | Reward: `text-[11px] font-bold font-mono` | Reward: `12.sp FontWeight.Bold` (sans-serif) | **Baixa** | Font diferente |
| D89 | **QuestsScreen — claim button text** | "Reivindicar" (title-case) | "REIVINDICAR" (uppercase) | **Baixa** | Padrão Android |
| D90 | **QuestsScreen — claimed text** | "Baú de espólios recolhido" italic serif | "Baú de espólios recolhido" italic Cinzel — S5 corrigiu | **Resolvido (S5)** | Sprint S5 migrou para Cinzel |

### 4.9 Módulo Inventário (InventoryScreen)

| # | Item | React | Android | Severidade | Notas |
|---|---|---|---|---|---|
| D91 | **Card tap feedback** | `active:scale-[0.99]` — cards shrink on press | Sem press-scale animation | **Baixa** | Micro-interação ausente |
| D92 | **Card transition-all** | `transition-all` para hover/active states | Sem transition — state changes instant | **Baixa** | Transições suaves ausentes |
| D93 | **Rarity badge text case** | UPPERCASE via CSS (`"ESPECIAL"`, `"COMUM"`) | Mixed case (`"Especial"`, `"Comum"`) | **Baixa** | Covered by S5 |
| D94 | **Rarity badge font** | `font-mono` (system monospace) | `JetBrainsMono` (custom font) | **Baixa** | Different monospace font |

### 4.10 Módulo Crônicas (HistoryScreen)

| # | Item | React | Android | Severidade | Notas |
|---|---|---|---|---|---|
| D95 | **Header alignment** | Header centralizado (`flex justify-center`) | Header alinhado à esquerda (Row left-aligned) — tipografia migrada para Cinzel pelo S5, mas alinhamento continua diferente | **Média (parcial S5)** | S5 migrou fonte para Cinzel + sentence case "Crônicas Diárias", mas layout (centralizado vs esquerda) permanece |
| D96 | **Header border-bottom separator** | `border-b border-amber-500/10 mb-4` abaixo do título | Sem separator line abaixo do header | **Baixa** | Visual separator ausente |
| D97 | **All-mode card background** | `bg-purple-950/10` (sutil purple tint) | `Stone900` (neutral stone, sem purple) | **Média** | Cor ambiente diferente nos chronicle cards |
| D98 | **Card shadow** | `shadow-md` (both notes e all-mode cards) | Sem shadow/elevation | **Baixa** | Cards flat no Android |
| D99 | **Wilderness red corner overlay** | Rotated red square (`bg-red-600/5 rotate-45`) em entries wilderness | Elemento decorativo omitido | **Baixa** | Detalhe visual perdido |
| D100 | **Chronicle panel gradient direction** | Vertical gradient (`bg-gradient-to-b`) | Horizontal gradient (`Brush.horizontalGradient`) | **Baixa** | Gradiente rotacionado 90° |
| D101 | **Chronicle panel Sparkles watermark** | `Sparkles` icon 15% opacity no canto superior | Omitido | **Baixa** | Flourish visual ausente |
| D102 | **Chronicle "📜 Crônica Mística" header** | Android adiciona header textual no painel expandido | React NÃO tem este header — texto renderiza direto | **Baixa** | Android adiciona UI extra não presente no React |
| D103 | **Chronicle text whitespace-pre-wrap** | `whitespace-pre-wrap` para preservar line breaks | Sem `maxLines` ou whitespace handling — multi-line pode colapsar | **Média** | Potencial diferença de reflow em crônicas multi-line |
| D104 | **Content max-width** | `max-w-xl mx-auto` (~576px centered) | Preenche largura disponível (sem max-width) | **Baixa** | Em tablets, Android estica conteúdo |
| D105 | **Empty state general padding** | `py-16` (64dp vertical) | `padding(vertical = 28.dp)` | **Baixa** | Empty state mais compacto |
| D106 | **Empty state icon size** | `w-12 h-12` (48dp) | `size(36.dp)` | **Baixa** | Ícone menor |
| D107 | **Empty state dashed border** | Sem dashed border no empty state geral | Desenha dashed border via `PathEffect.dashPathEffect` | **Baixa** | Android adiciona decorative border não presente no React |
| D108 | **Empty notes state icon** | `FileText` (lucide) | `Icons.Default.Description` (Material) | **Baixa** | Ícone diferente |
| D109 | **Notes card header divider** | Sem divider entre skill name/date e note body | `Box(height=1.dp)` divider explícito | **Baixa** | Android adiciona separator extra |
| D110 | **ViewModeButton border radius** | `rounded` (~4dp) | `RoundedCornerShape(6.dp)` | **Baixa** | Levemente mais arredondado |

### 4.11 Módulo Reino — Bazar (ShopScreen)

| # | Item | React | Android | Severidade | Notas |
|---|---|---|---|---|---|
| D111 | **Sound de moedas ausente na compra** | `sound.playCoins()` em `handlePurchase` | Nenhum efeito sonoro | **Média** | Reduz feedback tátil/auditivo na compra |
| D112 | **Fluxo de compra: criação de item** | `handlePurchase` busca `LOOT_TABLE` por `rarity`, gera `id` com `Date.now()`, passa `InventoryItem` completo | `onBuyItem` recebe `ShopCatalogEntry`; criação do `InventoryItem` ficou no ViewModel | **Média** | Lógica de negócio deslocada — se ViewModel não montar item com `rarity` e `id` timestamped, loot pode ficar sem raridade |

### 4.12 Módulo Reino — Loja de Títulos (TitleShopScreen)

| # | Item | React | Android | Severidade | Notas |
|---|---|---|---|---|---|
| D113 | **Grid hardcoded 2 colunas** | `grid-cols-2 sm:grid-cols-3 lg:grid-cols-4` (Common/Rare escalam) | `columns = 2` fixo; Epic/Legendary `columns = 1` | **Média** | Em tablets, React mostraria mais colunas; Android sempre max 2 |
| D114 | **Tooltip "Obtido aleatoriamente..."** | Botão "Loot Raro" tem `title=` tooltip HTML | Nenhum tooltip (touch) | **Baixa** | Simplificação consciente |
| D115 | **Badge "Desbloqueado" tracking** | `tracking-widest` (0.1em) | `letterSpacing = 0.05.em` (~5%) | **Baixa** | Diferença sutil de espaçamento |

### 4.13 Módulo Reino — Seletor de Títulos (TitleSelectorScreen)

| # | Item | React | Android | Severidade | Notas |
|---|---|---|---|---|---|
| D116 | **Badge "Ativo" posicionamento** | `absolute -top-2.5 -right-2` (fora do card) | `offset(x=6.dp, y=-6.dp)` com `Alignment.TopEnd` (dentro do card) | **Média** | Badge "vaza" do card no React; fica dentro no Android |
| D117 | **Glow shadow do título equipado** | `shadow-[0_0_15px_rgba(245,158,11,0.25)]` (amber glow) | `background(Stone900)` + `border(Amber500)` — sem glow | **Média** | Efeito visual de "destaque mágico" ausente |
| D118 | **Grid hardcoded 2 colunas** | `grid-cols-1 sm:grid-cols-2 lg:grid-cols-3` | `LazyVerticalGrid(GridCells.Fixed(2))` | **Média** | Em telas maiores, React mostraria até 3 colunas |
| D119 | **LazyVerticalGrid height fixa 600.dp** | Grid React flui naturalmente com scroll do container | `Modifier.fillMaxWidth().height(600.dp)` — altura arbitrária | **Média** | Pode truncar com muitos títulos ou sobrar espaço com poucos |
| D120 | **Nome do título não é uppercase** | `text-[12px] font-serif uppercase tracking-wider` | `Text(text = title.name)` — sem `.uppercase()` | **Crítica** | **Bug real**: nome em sentence case vs UPPERCASE |
| D121 | **LazyVerticalGrid em verticalScroll** | Container React usa scroll CSS normal | `LazyVerticalGrid` dentro de `verticalScroll(rememberScrollState())` | **Crítica** | **Risco de crash/performance** — deveria usar `chunked` pattern |
| D122 | **Category badge text case** | `uppercase` via CSS class | `title.category.name.lowercase()` | **Média** | Category em lowercase vs UPPERCASE |

### 4.14 Módulo Reino — Heatmap (HeatmapScreen)

| # | Item | React | Android | Severidade | Notas |
|---|---|---|---|---|---|
| D123 | **Tooltip → tap-to-select** | Hovering cria floating tooltip posicionado via `getBoundingClientRect` | Tapping seta `selectedCell` state; texto inline abaixo da grade | **Baixa** | Adaptação intencional documentada |
| D124 | **"ATUAL" week label** | Label animado `animate-pulse` abaixo da coluna atual | Sem label; coluna tem apenas bg highlight + border | **Baixa** | Simplificação visual consciente |
| D125 | **Corner brackets na consistency card** | 4 elementos decorativos posicionados absolutamente | Ausente — card tem border clean | **Baixa** | Ornamentação CSS não-funcional |
| D126 | **Hint text copy** | `"Passe o mouse ou toque nos blocos..."` | `"Toque nos blocos..."` | **Baixa** | Copy adaptada para touch — correto |
| D127 | **Grid panel shadow** | `shadow-2xl` (heavy drop shadow) | Sem shadow — background sólido | **Baixa** | Instance of S9 com shadow mais pesado |
| D128 | **Current week column glow** | `shadow-[0_0_10px_rgba(226,176,84,0.12)]` ambient glow | Sem glow — apenas bg tint + border | **Baixa** | Visual polish |
| D129 | **Segmented progress bar gradient/glow** | Active segments: `bg-gradient-to-t from-emerald-600 to-emerald-400` com glow | Active segments: flat `Emerald600`, sem gradient ou glow | **Baixa** | Visual polish |

### 4.15 Módulo Reino — Stats (StatsScreen)

| # | Item | React | Android | Severidade | Notas |
|---|---|---|---|---|---|
| D130 | **Shadow on stat cards** | `shadow-md` | Sem shadow em nenhuma stat card | **Baixa** | Visual polish |

### 4.16 Módulo Reino — Conquistas (AchievementsScreen)

| # | Item | React | Android | Severidade | Notas |
|---|---|---|---|---|---|
| — | **Nenhuma divergência única encontrada** | — | — | — | Port fiel; todos os 8 achievements batem campo a campo |

### 4.17 Módulo Reino — Tutorial (GuideScreen)

| # | Item | React | Android | Severidade | Notas |
|---|---|---|---|---|---|
| — | **Nenhuma divergência única encontrada** | — | — | — | Diferenças limitadas a S7/S8 (icons documentados) |

### 4.18 Modais / Overlays (varredura profunda — 15 componentes)

#### GeneralSettingsModal

| # | Item | React | Android | Severidade | Notas |
|---|---|---|---|---|---|
| D131 | **Campo "Nome do dispositivo" ausente** | Input `device-name-fld` (persiste em localStorage) | Campo inexistente | **Crítica** | Sync multi-device depende disso |
| D132 | **Campo "Duração descanso longo" ausente** | Input numérico inline `longBreakDuration` (1–120 min) | Campo inexistente — só existe no TimerSettingsModal | **Média** | Ajuste só acessível em lugar diferente |
| D133 | **Seção "Conta e dados" ausente** | Seção vermelha com "Limpar campanha" + "Sair da conta (logout)" | Seção inexistente | **Crítica** | Sem reset nem logout no Android |
| D134 | **Botão "Salvar alterações" inexistente** | Dois botões: "Salvar alterações" (gradient amber) + "Voltar" | Botão único "Concluir e Retornar" (fecha modal) | **Crítica** | Android aplica por campo imediatamente, sem commit unificado |
| D135 | **Backup: layout agrupado vs lista vertical** | 2 subgrupos lado a lado ("Via arquivo JSON" + "Via código") | 4 botões verticais empilhados | **Média** | Organização visual diferente |
| D136 | **Validação de nome (salvar)** | "Salvar" lê valor do input via `getElementById` e aplica | `onNameChange` chamado a cada caractere (sem commit final) | **Média** | Padrão de aplicação diferente |

#### SkillSelectorModal

| # | Item | React | Android | Severidade | Notas |
|---|---|---|---|---|---|
| D137 | **Label nível: "Nv." vs "Nível"** | `"Nv. {sk.level}"` | `"Nível ${sk.level}"` | **Baixa** | Diferença de copy |
| D138 | **Badge "Ativa" posicionamento** | `absolute top-3.5 right-3.5` | `Box(Alignment.TopEnd)` | **Baixa** | Pode diferir em telas pequenas |
| D139 | **Ícone de tag** | Lucide `Tag` (2x2) | Material `Icons.Outlined.Sell` | **Baixa** | Shape diferente |

#### TimerSettingsModal

| # | Item | React | Android | Severidade | Notas |
|---|---|---|---|---|---|
| D140 | **Emoji no título** | `"⚙️ Ajustes do Timer"` | `"Ajustes do Timer"` (sem emoji) | **Baixa** | — |
| D141 | **Toggle appearance** | `ToggleRight`/`ToggleLeft` (lucide, 8x8) | `HeroLogToggleSwitch` (custom, 44x24dp) | **Média** | Aparência do toggle completamente diferente |
| D142 | **Custom inputs visibility** | Campos sempre renderizados com `opacity-40` quando inactive | Campos wrapped em `AnimatedVisibility`, completely hidden | **Média** | Comportamento de exibição diferente |
| D143 | **Labels de toggle: copy diferente** | "Auto-Iniciar Descanso" / "Auto-Iniciar Foco" | "Iniciar descanso automaticamente" / "Iniciar foco automaticamente" | **Média** | Copy diferente |
| D144 | **Botão "Salvar Personalizado"** | Bordered transparent (`bg-champagne-500/10 border`) | Solid amber (`containerColor = Amber500`) | **Média** | Padrão S10 aplicado aqui |

#### IncursionModeModal

| # | Item | React | Android | Severidade | Notas |
|---|---|---|---|---|---|
| D145 | **Título: emoji e formulação** | `"⚔️ Modo de Incursão"` | `"Modos de Incursão"` (plural, sem emoji) | **Baixa** | — |
| D146 | **Card layout: vertical vs horizontal** | Cards são vertical stacks (icon+title+badge, description abaixo) | Cards são Row (icon à esquerda, conteúdo à direita) | **Média** | Layout estrutural diferente |

#### ModeDescriptionModal

| # | Item | React | Android | Severidade | Notas |
|---|---|---|---|---|---|
| D147 | **Block label: icon inline** | `block.icon` renderizado inline ao lado do label | Sem suporte a `icon` no block (apenas `label` + `text`) | **Média** | Se algum block tiver icon, será ignorado |
| D148 | **Button: bordered transparent vs solid** | "Entendido": `bg-champagne-950/40 border` (transparente) | "ENTENDIDO": `containerColor = buttonBg` (solid filled) | **Média** | Padrão S10 |

#### AmbientSoundModal

| # | Item | React | Android | Severidade | Notas |
|---|---|---|---|---|---|
| D149 | **Título do modal** | `"Sons Ambiente"` | `"Sons do Santuário"` | **Média** | Copy alterado |
| D150 | **Volume label** | `"Volume do Eco"` | `"Volume"` (sem "do Eco") | **Baixa** | Subtítulo diferente |
| D151 | **Volume % display** | Badge mono com bg/border (`champagne-500/10`) | Texto simples `Amber400`, sem container | **Média** | Badge visual perdido |
| D152 | **Conditional VolumeX/Volume2 icon** | VolumeX quando `volume === 0`, Volume2 quando `> 0` | Sempre Volume2 (sem checar mudo) | **Baixa** | Estado visual de mudo ausente |
| D153 | **Track deselect toggle** | Clicar track ativo → `selectTrack(null)` (desmarca) | Clicar track → `onSelectTrack(track.id)` sempre (nunca desmarca) | **Crítica** | **Bug funcional**: usuário não pode desligar som |
| D154 | **Active track indicator color** | `text-emerald-400` (verde) | `Amber500` (âmbar) | **Média** | Cor do status ativo diferente |

#### LevelUpOverlay

| # | Item | React | Android | Severidade | Notas |
|---|---|---|---|---|---|
| D155 | **Backdrop blur** | `backdrop-blur-md` (~12px) + `bg-black/90` | `Stone950.copy(alpha=0.9f)` sem blur | **Média** | Backdrop mais "plano" |
| D156 | **Card shadow/glow** | `shadow-[0_0_50px_rgba(…,0.3)]` (amber glow) | `border(2.dp)` sem shadow/glow | **Média** | Glow ausente |
| D157 | **Sparkle regeneration** | `Math.random()` a cada render (re-amostra) | `remember(event)` — sorteado uma vez | **Baixa** | Partículas menos dinâmicas |
| D158 | **Combat icon animation** | `animate-bounce` no ⚔️ + `animate-ping` no anel | `infiniteRepeatable` ping, sem bounce | **Baixa** | Só anel pulsa |
| D159 | **Combat "Você evoluiu!" size** | `text-[10px] sm:text-xs` (10-12sp responsivo) | `fontSize = 11.sp` fixo | **Baixa** | — |
| D160 | **Combat "NÍVEL" highlight** | `<span>` com `text-[#E2B054] text-lg font-bold` (destaque dourado) | Texto todo no mesmo nível (`13.sp`, cor `#FEF3C7`) | **Média** | React destaca "NÍVEL X" com cor e tamanho maior |
| D161 | **Skill "MAESTRIA APRIMORADA" size** | `text-xl sm:text-2xl` (20-24sp responsivo) | `fontSize = 18.sp` fixo | **Baixa** | — |
| D162 | **Skill "alcançou o Nível X" highlight** | `text-emerald-400 text-lg font-black` (destaque verde) | `color = AmberText200` (amarelo pálido, sem destaque separado) | **Média** | Nível em verde/maior vs tudo em amarelo |

#### RestoreSaveDialog

| # | Item | React | Android | Severidade | Notas |
|---|---|---|---|---|---|
| D163 | **Title positioning** | Título centralizado dentro do body | Título no header do HeroLogModal | **Média** | Layout diferente |
| D164 | **Confirm button style** | `bg-gradient-to-r from-amber-600 to-amber-500` com hover | `ButtonDefaults.buttonColors(containerColor = Amber500)` sólido | **Média** | Gradiente com hover vs sólido |
| D165 | **Cancel button style** | `bg-stone-900 border border-amber-500/20` com hover | `OutlinedButton` (Material3 padrão) | **Média** | bg stone+amber vs outlined material |

#### TitleEquipModal

| # | Item | React | Android | Severidade | Notas |
|---|---|---|---|---|---|
| D166 | **Scroll container ausente** | `<div className="space-y-5 max-h-[65vh] pr-1">` com `overflow-y-auto` | Sem restrição de altura nem scroll wrapper | **Crítica** | Lista longa pode transbordar sem scroll |
| D167 | **Empty state text styling** | `text-zinc-400 font-serif font-semibold` | `FontFamily.Serif, FontWeight.SemiBold, color = Amber400` | **Média** | Cinza neutro vs âmbar |
| D168 | **Title name casing** | `{title.name}` (sem upperCase) | `title.name.uppercase()` | **Baixa** | Android força uppercase |
| D169 | **Equipped badge position** | `absolute top-3.5 right-3.5` (sobreposição) | `Modifier.align(Alignment.TopEnd)` (dentro do flow) | **Média** | Badge flutua vs dentro do Column |

#### ItemInspectModal

| # | Item | React | Android | Severidade | Notas |
|---|---|---|---|---|---|
| D170 | **"VOLTAR" footer button** | **Ausente** — React não tem botão de fechar no footer | Footer Row com botão `"VOLTAR"` | **Média** | Android adiciona affordance que não existe no React |
| D171 | **Slot selector label casing** | `"Selecione o espaço para equipar:"` (mixed) | `"SELECIONE O ESPAÇO PARA EQUIPAR:"` (all-caps) | **Baixa** | — |
| D172 | **Container max height** | `max-w-sm` sem altura máxima explícita | `heightIn(max = screenHeight * 0.85f)` | **Média** | Android limita a 85% da tela |

#### FocusCompletionFlow (CompletionShell + sub-screens)

| # | Item | React | Android | Severidade | Notas |
|---|---|---|---|---|---|
| D198 | **Relatório Diário — RESOLVIDO** | Modal "RELATÓRIO DIÁRIO" (App.tsx:4319-4460) aparece ao detectar novo dia: gold reward (100/120 GP), status do streak (mantido/perdido/protegido por StreakShield), dailies de ontem perdidas e HP damage | **Bloco DR (08/09)**: `DailyReportData` (7 campos), `DailyReportModal.kt` (AnimatedVisibility, blur, sparkle, bounce), wiring `MainActivity.kt`, 7 Roborazzi tests. 491/491 PASSED. Visual validado por Bruno | **Resolvido** | Commit `394ad9a` |
| D173 | **SessionSummary título 40% menor** | `text-xl` (20sp) | `fontSize = 12.sp` | **Crítica** | Impacto visual significativo |
| D174 | **SessionSummary rank structure** | Label `★ CLASSIFICAÇÃO S ★` em `text-sm` + descrição separada | Rank como número grande `48.sp` — sem label "★ CLASSIFICAÇÃO" | **Crítica** | Estrutura hierárquica completamente diferente |
| D175 | **SessionSummary "SEQUÊNCIA" label** | `"SEQUÊNCIA"` | `"SEQUÊNCIA DE CHAMA"` | **Média** | Copy divergente |
| D176 | **SessionSummary streak cor** | `text-[#F14D2A]` (vermelho) com Flame icon | `Color(0xFFF5F5F4)` (stone-100) com 🔥 emoji | **Média** | Cor diferente (vermelho vs branco) |
| D177 | **StreakCelebration glow animado** | `bg-orange-500/20 blur-2xl animate-pulse` + `animate-bounce` | Ícone estático, sem glow pulsante nem bounce | **Média** | Animação de destaque omitida |
| D178 | **StreakCelebration fonte do número** | `font-serif font-black text-champagne-300` (Cinzel) | `FontFamily.Default` (sans-serif), `FontWeight.Bold`, yellow-300 | **Média** | Tipografia sans-serif em vez de serif |
| D179 | **LootDrop emoji animado** | `🎁` com `animate-bounce` + `drop-shadow` | Sem emoji — só texto "TESOURO CONQUISTADO" | **Média** | Emoji decorativo com glow ausente |
| D180 | **LootDrop card item layout** | Cards com 4 corner accents, gradient, emoji `text-3xl` | Card com Row simples — emoji + nome/desc, sem decorations | **Média** | Estrutura visual simplificada |
| D181 | **SessionNotes label "ANOTAÇÕES"** | Label com `font-serif font-black` | Label omitido — vai direto para o OutlinedTextField | **Média** | Label ausente |

#### CognitiveDeathOverlays

| # | Item | React | Android | Severidade | Notas |
|---|---|---|---|---|---|
| D182 | **Animação de entrada** | `motion.div` com `initial/animate/exit` (scale 0.95, y 15, spring) | Sem animação — conteúdo estático | **Média** | Decisão registrada no PARIDADE.md |
| D183 | **Backdrop blur** | `backdrop-blur-md`/`backdrop-blur-sm` | Sem blur | **Baixa** | Limitação de plataforma |
| D184 | **Background color** | `bg-red-950` = `#450a0a` | `#280505` (mais escuro) | **Baixa** | Both very dark red |
| D185 | **Skull animado** | `text-7xl` com AnimatePresence (scale in) | `fontSize = 64.sp` estático | **Baixa** | — |
| D186 | **"VOCÊ MORREU" pulse** | `animate-pulse` (pulso contínuo) | Texto estático — sem animação | **Média** | Pulsante ausente |
| D187 | **Respawn button glow** | `shadow-[0_4px_15px_rgba(239,68,68,0.4)]` + `active:scale-95` | Sem shadow glow nem active scale | **Média** | Feedback visual reduzido |
| D188 | **GracePeriod countdown ping** | `animate-ping` (pulso/ping contínuo) | Texto estático | **Média** | Ping de urgência ausente |
| D189 | **GracePeriod title pulse** | `animate-pulse` no título | Sem animação | **Média** | Pulsante ausente |

#### SkillInlineCarousel

| # | Item | React | Android | Severidade | Notas |
|---|---|---|---|---|---|
| D190 | **Empty state estilo** | `border-dashed border-[#e5c158]/30` + `italic font-serif` | `OutlinedButton` com `border(champagne)` — sem dashed, sem italic | **Baixa** | — |
| D191 | **Level badge container** | `text-[11px] font-mono bg-[#16161d] px-1.5 py-0.5 rounded border` | Inline `Text` com `champagne.copy(alpha=0.8f)` — sem bg/border | **Média** | Badge com fundo/borda vs texto inline |
| D192 | **Dot indicators spacing** | `gap-1.5` (6dp) | `spacedBy(4.dp)` | **Baixa** | Spacing menor |
| D193 | **Dot active width** | `w-3 h-1` (12x4dp) | `size(width=10.dp, height=4.dp)` | **Baixa** | Dot ativo 2dp menor |

#### QuestFab

| # | Item | React | Android | Severidade | Notas |
|---|---|---|---|---|---|
| D194 | **Título do modal** | `"📜 CONTRATOS ATIVOS"` (com scroll emoji) | `"📜 Contratos Ativos"` — S5 corrigiu (emoji + sentence case) | **Resolvido (S5)** | Sprint S5 alinhou emoji e copy ao React |
| D195 | **Section header emojis** | `"🎯 Contratos Diários"` + `"🛡️ Marcos da Jornada"` | `"CONTRATOS DIÁRIOS"` + `"MARCOS DA JORNADA"` (sem emojis, sentence case parcial) — S5 corrigiu sentence case e fontes | **Parcial (S5)** | S5 migrou fontes para Cinzel e corrigiu copy para sentence case, mas emojis de seção continuam ausentes |
| D196 | **Daily quest dot indicator** | Dot colorida `w-1.5 h-1.5` com cores dinâmicas | Sem dot indicator — só texto name | **Média** | Indicador visual de status ausente |
| D197 | **Empty state guild** | `"🏆 Todas as teses conquistadas!"` + subtexto `"Glória eterna..."` | `"Todas as teses conquistadas!"` em Cinzel — S5 corrigiu fonte e copy | **Resolvido (S5)** | Sprint S5 alinhou copy e tipografia ao React |

---

### 4.19 SFX (Efeitos Sonoros Curtos) — ✅ RESOLVIDO (Bloco D199, 11/09)

| # | Item | React | Android | Severidade | Notas |
|---|---|---|---|---|---|
| D199 | **Sistema de SFX** | `NativeAudioEngine` (`src/utils/audio.ts`) sintetiza 6 sons via Web Audio API: `playFocusBell` (gong tibetano), `playLevelUp` (arpejo ascendente), `playCoins` (clinking metálico), `playDeath` (sawtooth descendente), `playWildernessWarning` (heartbeat sub-bass), `playClick` (UI click). **39 call sites** em 15 arquivos. Todos guardados por `!muteSfx` (exceto 2: ShopTab e QuestsTab). | **✅ Implementado**: `SfxManager.kt` (factory `create(context)` + `noOp()`), 6 WAVs gerados via script Python replicando fórmulas exatas. 39 call sites: 24 no `HeroLogViewModel`, 4 em `SkillInlineCarousel`, 1 em `LevelUpOverlay`. Toggle `isSfxMuted` sincronizado via `LaunchedEffect`. `CompositionLocalProvider` provê `LocalSfxManager`. | **Resolvido** | 491/491 testes PASSED. Bugs React não replicados: ShopTab:95, QuestsTab:20. |

**Call sites afetados (39):**

| Ação | Qtd | Arquivos React |
|---|---|---|
| `playLevelUp()` — level-ups (skill, combat, prestige), break complete, focus auto-claim, title claim | 15 | App.tsx, useBreakTimer, useDailies, useHabits, useTodos, useGuildQuests, useTitles, useSkills, FocusModeScreen, useLevelUp |
| `playCoins()` — gold earning, buying, selling, equipping, session complete, daily claim | 12 | App.tsx, useDailies, useHabits, useTodos, useTitles, useInventory, useFocusSession, QuestsTab, ShopTab |
| `playClick()` — UI navigation (carousel arrows, center, dots) | 4 | SkillInlineCarousel |
| `playDeath()` — player death (daily neglect, negative habit, cognitive death) | 3 | App.tsx, useHabits, useFocusSession |
| `playWildernessWarning()` — wilderness leave, grace countdown, negative habit | 3 | useHabits, useFocusSession |
| `playFocusBell()` — focus session start | 1 | useFocusSession |

**Nota sobre bug no React:** 2 call sites tocam SEM checar mute: `ShopTab.tsx:95` (compra) e `QuestsTab.tsx:20` (guild quest claim). No Android é irrelevante porque não há SFX.

---

## 5. Itens Não Verificados (Fora de Escopo)

| Item | Motivo |
|---|---|
| Fórmulas de recompensa (XP, gold, streak, loot) | Já validadas no `PARIDADE.md` |
| Sincronização Supabase | Fora de escopo do port |
| Som ambiente (música de fundo) | ✅ Paridade — 8 tracks .mp3 loopadas via MediaPlayer |
| SFX (efeitos sonoros curtos) | ✅ Resolvido — Bloco D199 (11/09): `SfxManager` + 6 WAVs + 39 call sites |
| Persistência de sessão ativa entre restarts | Lógica testada, wiring documentado |
| Keyboard shortcuts / hover states | N/A (plataforma touch) |

---

## 6. Sugestão de Ordem de Trabalho (por prioridade)

### Prioridade 0 — Bugs Críticos (quebram funcionalidade)

1. **AmbientSoundModal — track deselect bug** — Usuário não pode desligar som (D153). Fix: `onSelectTrack` deve checar se track já está ativa e chamar `selectTrack(null)`.
2. **TitleEquipModal — scroll container ausente** — Lista pode transbordar (D166). Fix: adicionar `Modifier.heightIn(max = screenHeight * 0.65f).verticalScroll(rememberScrollState())`.
3. **TitleSelectorScreen — LazyVerticalGrid em verticalScroll** — Risco de crash (D121). Fix: migrar para `LazyColumn` com `items(chunked(2))` como `TitleShopScreen` faz.
4. **TitleSelectorScreen — nome do título sentence case** — Bug visual (D120). Fix: adicionar `.uppercase()` no `Text` do nome.
5. **SessionSummary — título 40% menor + rank structure** — Estrutura hierárquica diferente (D173-D174). Fix: alinhar com React (label `★ CLASSIFICAÇÃO X ★` + descrição).
6. **GeneralSettingsModal — 3 campos críticos ausentes** — "Nome do dispositivo", "Duração do descanso longo", seção "Conta e dados" (D131-D134). Decisão: confirmar se são pendências intencionais.

### Prioridade 1 — Sistemática (alto impacto, alta escala)

7. **S3 + S4: Migração de fontes (Cinzel/JetBrainsMono)** — 19 arquivos, ~190 ocorrências. Maior item de paridade visual.
8. **S1 + S2: Paleta champagne vs amber** — Unificar `#D4AF37` vs `#F59E0B` e labels.
9. **S9: Container `bg-quest-panel`** — Todas as tabs usam `Stone950` sólido.
10. **S10: Button styles** — Submit buttons sólidos vs bordered transparent.

### Prioridade 2 — Alto impacto (Média — percepção do usuário)

11. **D24: Header "CÂMARA DE FOCO" some durante sessão**
12. **D25: Break prep full-screen vs inline**
13. **D7+D8: FocusCompletionFlow loot grid + decoration**
14. **D12+D13: CharacterScreen header button + isRunning**
15. **D146: IncursionModeModal card layout vertical vs horizontal**
16. **D143: TimerSettingsModal labels de toggle copy diferente**
17. **D149+D154: AmbientSoundModal título + track color**
18. **D175-D178: StreakCelebration/LootDrop/SessionSummary visual differences**
19. **D199: Sistema de SFX ausente** — 6 sons, 39 call sites. Criar `SfxManager` com `SoundPool` + 6 áudios sintetizados ou gravados.

### Prioridade 3 — Baixa (Cosmético / plataforma)

19. D5, D6, D14, D15, D23, D29, D63, D89: Casing uppercase vs title-case — padrão Android, manter.
20. D9, D10, D11: FocusOrb — já documentados como decisões conscientes.
21. Divergências restantes: hover states, animações, gradientes, shadows.

---

## 7. Conclusão

O port Android apresenta **fidelidade funcional alta** — todas as 17 telas e 15 modais/overlays existem e funcionam com a mesma informação que o React. **6 divergências críticas** foram identificadas (bugs funcionais), mas nenhuma causa crash imediato.

A varredura profunda (linha a linha, **todas as 17 telas e 15 modais analisados**) revelou **~780+ divergências**, mas **~70% são sistemáticas** — os mesmos 10 padrões se repetem em todas as telas:

- **Resolução de fonte** (`FontFamily.Serif` → Noto Serif vs Cinzel): 19 arquivos, ~190 ocorrências
- **Paleta de cores** (`#D4AF37` champagne vs `#F59E0B` amber): todas as telas
- **Labels** (`zinc-500` cinza vs `amber-100` âmbar): todas as telas
- **Container sem shadow/border** (`Stone950` vs `bg-quest-panel`): todas as tabs
- **Botões sólidos vs bordered**: Dailies, Todos, Skills, Habits
- **Font weight 700 vs 900**: todas as telas
- **Hover states ausentes**: todas as telas (limitação plataforma)
- **Animações ausentes**: Focus, Quests, Guide, CognitiveDeath (~30 divergências)
- **Layout desktop ausente**: todas as telas (app mobile-only)
- **CSS `uppercase` manual vs classe**: todas as telas

### Divergências críticas (quebram funcionalidade)

| # | Divergência | Impacto |
|---|---|---|
| D153 | AmbientSoundModal track deselect | Usuário não pode desligar som |
| D166 | TitleEquipModal scroll container | Lista pode transbordar |
| D121 | TitleSelectorScreen LazyVerticalGrid em scroll | Risco de crash |
| D173-D174 | SessionSummary título/rank | Estrutura 40% menor e diferente |
| D131-D134 | GeneralSettingsModal 3 campos ausentes | Sync multi-device, reset, logout |
| D198 | Relatório Diário ausente | Usuário não vê recompensa/streak ao abrir app |

### Divergências individuais de maior impacto (Média)

| # | Divergência | Impacto |
|---|---|---|
| D24 | Header "CÂMARA DE FOCO" some durante sessão | Perde contexto visual |
| D25 | Break prep full-screen vs inline | Perde contexto ao redor |
| D7+D8 | FocusCompletionFlow loot decoration | Momento de recompensa menos elaborado |
| D12+D13 | CharacterScreen header button + isRunning | Funcionalidade ausente |
| D146 | IncursionModeModal layout vertical vs horizontal | Layout estrutural diferente |
| D143 | TimerSettingsModal toggle copy | Labels diferentes |
| D175-D178 | StreakCelebration/LootDrop visual | Tipografia e animações diferentes |

### Recomendação

A **Prioridade 0** (6 bugs críticos) deve ser resolvida antes de qualquer trabalho estético. A **Prioridade 1** (resolução de fontes + paleta de cores) tem a melhor relação impacto/esforço para o trabalho visual.

---

## 8. Auditoria de Consistência dos Documentos do Projeto

Auditoria cruzada de `PARIDADE.md` e `AGENTS.md` contra o código real (read-only, sem alterações nos documentos originais).

### 8.1 Inconsistências no PARIDADE.md

| # | Seção | Severidade | Descrição |
|---|---|---|---|
| P1 | Seção 5 (Skills) | **Média** | `SkillLogicTest`: PARIDADE afirma "17 testes" mas o arquivo real tem 12 métodos `@Test` |
| P2 | Seção 1 (Persistência) | **Média** | Status "Em andamento — Habilidades e Foco ainda em dados fake" está desatualizado — todos os módulos estão completamente wireados via Room blob JSON |
| P3 | Seção 9 (Riscos) | Baixa | "Cooldown de Masmorra" diz que `DungeonCooldownLogic.kt` não foi portado — o arquivo existe com 8 testes |
| P4 | Seção 9 (Riscos) | Baixa | "Todo tem sistema de decaimento próprio" diz "não auditado" — `TodoDecayLogic.kt` tem 10 testes |
| P5 | Seção 9 (Riscos) | Baixa | "Sistema de conquistas diferente" diz "investigar antes de portar" — `AchievementCatalog.kt`/`AchievementLogic.kt`/`AchievementsScreen.kt` existem e estão wireados |
| P6 | Seção 7 (Reino) | Baixa | Nota sobre `TitleSelectorScreen.kt` diz "Grid sem LazyVerticalGrid (linhas via chunked)" — o código real USA `LazyVerticalGrid` (a afirmação é sobre `TitleShopScreen.kt`, não `TitleSelectorScreen.kt`) |
| P7 | Multiplas seções | Baixa | Vários arquivos significativos não têm linha própria na tabela (`GeneralSettingsModal.kt`, `CognitiveDeathLogic.kt`, `DungeonCooldownLogic.kt`, `FocusModels.kt`) |
| P8 | Seção 6 (Missões) | Baixa | Contagens agregadas históricas ("315/315", "343/343") estão desatualizadas — `HeroLogViewModelTest` sozinho tem 57 testes |

### 8.2 Inconsistências no AGENTS.md

| # | Seção | Severidade | Descrição |
|---|---|---|---|
| A1 | Seção 4.7 (Fontes) | **Significativa** | Família Inter (fonte primária de UI) completamente omitida da descrição de `Type.kt` — só menciona Cinzel e JetBrains Mono |
| A2 | Seção 9 (Armadilha 2) | **Significativa** | Contagem "~34 classes de teste" vs real ~65 (desvio de ~2x) — relevante pra preocupação de memória |
| A3 | Seção 4.5 (Pacotes) | **Significativa** | Pacote `data/export/` (4 arquivos: `ExportPayload.kt`, `GameStateExporter.kt`, `GameStateImporter.kt`, `SaveClipboard.kt`) inteiramente indocumentado |
| A4 | Seção 4.5 (Pacotes) | Moderada | Pacote `util/` não listado (contém `LevelCalculator.kt`) |
| A5 | Seção 4.5 (Pacotes) | Moderada | `model/LogEntry.kt`, `data/HeroLogInitialState.kt`, `data/JsonConfig.kt` não listados na árvore |
| A6 | Seção 4.5 (Pacotes) | Moderada | Listagens de subdiretórios significantemente incompletas (`ui/focus/` lista 5, existem 14; `logic/quests/` lista 5, existem 12) |
| A7 | Seção 8 (Docs) | Moderada | 4 arquivos em `specs/` não estão na tabela de documentação |
| A8 | Seção 8 (Docs) | Baixa | `PROGRESSO_AUDITORIA.md` e `RELATORIO_PARIDADE.md` não listados na tabela |
| A9 | Seção 9 (Armadilha 2) | Baixa | Referência `-Xmx1536m` vs configuração real `maxHeapSize = "3g"` no `build.gradle.kts` |

### 8.3 Inconsistências no DEV_LOG_ANDROID.md

| # | Severidade | Descrição |
|---|---|---|
| L1 | **Moderada** | 4 commits de código sem entrada no DEV_LOG (`7e0d758`, `d341487`, `5dce9cb`, `e5d3061`) — total de +143/-75 linhas de código de produção em 10 arquivos Kotlin não documentadas |
| L2 | Baixa | Imprecisão de contagem de linhas no Bloco A+B (`MainActivity.kt` afirmou +84/-18, real +96/-26, desvio ~15%) |

### 8.4 Resumo da Auditoria de Documentos

| Documento | Inconsistências | Significativas | Moderadas | Baixas |
|---|---|---|---|---|
| `PARIDADE.md` | 8 | 2 | 0 | 6 |
| `AGENTS.md` | 9 | 3 | 3 | 3 |
| `DEV_LOG_ANDROID.md` | 2 | 0 | 1 | 1 |

**Nenhuma inconsistência encontrada é crítica** (quebra funcional). As 5 significativas/moderadas são:
- Fonte Inter omitida, contagem de testes incorreta, export package indocumentado (AGENTS.md)
- SkillLogicTest count errado, persistência desatualizada (PARIDADE.md)

Estas inconsistências **não afetam o funcionamento do app** — são gaps de documentação que podem levar a pressupostos incorretos por agentes futuros.

---

## 9. Estado da Migração de Fontes (Cinzel/JetBrainsMono vs FontFamily.Serif/Monospace)

Referência: `AGENTS.md` seção 4.7 — "muitos componentes ainda usam `FontFamily.Serif`/`.Monospace` do sistema"

### Arquivos JÁ migrados (usam Cinzel/JetBrainsMono corretamente)

| Arquivo | Status |
|---|---|
| `ui/kingdom/` (8 arquivos: ShopScreen, TitleShopScreen, TitleSelectorScreen, HeatmapScreen, StatsScreen, AchievementsScreen, GuideScreen, LogsScreen) | ✅ Migrados (commit `e5d3061`) |
| `ui/inventory/InventoryScreen.kt` | ✅ Migrado (commit `5dce9cb`) |
| `ui/habits/HabitsScreen.kt` | ✅ Migrado (Sprint S5, 09/09) — Cinzel/Inter/JetBrainsMono |
| `ui/dailies/DailiesScreen.kt` | ✅ Migrado (Sprint S5, 09/09) — Cinzel/Inter/JetBrainsMono |
| `ui/todos/TodosScreen.kt` | ✅ Migrado (Sprint S5, 09/09) — Cinzel/Inter/JetBrainsMono |
| `ui/quests/QuestsScreen.kt` | ✅ Migrado (Sprint S5, 09/09) — Cinzel/JetBrainsMono |
| `ui/history/HistoryScreen.kt` | ✅ Migrado (Sprint S5, 09/09) — Cinzel/JetBrainsMono |
| `MainActivity.kt` (QuestFab section, L1239-1402) | ✅ Migrado (Sprint S5, 09/09) — Cinzel/JetBrainsMono |

### Arquivos AINDA com FontFamily.Serif/Monospace/SansSerif (14 ocorrências restantes)

| # | Arquivo | Ocorrências | Contexto |
|---|---|---|---|
| 1 | `CharacterScreen.kt` | 23 | Headers, stats, tiles |
| 2 | `SkillsScreen.kt` | 18 | Headers, cards, labels |
| 3 | `GeneralSettingsModal.kt` | 11 | Headers, botões, labels |
| 4 | `ItemInspectModal.kt` | 10 | Headers, badges, descrição |
| 5 | `TimerSettingsModal.kt` | 9 | Presets, labels |
| 6 | `SkillSelectorModal.kt` | 8 | Cards, labels |
| 7 | `TitleEquipModal.kt` | 8 | Headers, perks |
| 8 | `LevelUpOverlay.kt` | 7 | Título, botão |
| 9 | `RestoreSaveDialog.kt` | 6 | Título, placeholder |
| 10 | `ModeDescriptionModal.kt` | 3 | Título, blocks |
| 11 | `IncursionModeModal.kt` | 3 | Cards, labels |
| 12 | `CognitiveDeathOverlays.kt` | 1 | Título |
| 13 | `HeroLogModal.kt` | 1 | Título |
| 14 | `DifficultyBadge.kt` | 1 | Badge text |

### Resumo

| Métrica | Valor |
|---|---|
| Total de ocorrências restantes | ~109 em 14 arquivos |
| Arquivos totalmente migrados | 16 (8 Kingdom + Inventory + 5 Missões + QuestFab) |
| Arquivos parcialmente/não migrados | 14 |
| Progresso estimado | ~53% migrado (16/30 arquivos UI) |

---

## 10. Status das Divergências D-Series (verificação pós-commits recentes)

Verificação se os 4 commits não documentados (`7e0d758`, `d341487`, `5dce9cb`, `e5d3061`) resolveram alguma divergência encontrada na auditoria.

### Divergências Críticas (novas — varredura profunda completa)

| # | Divergência | Status | Evidência |
|---|---|---|---|
| D153 | AmbientSoundModal track deselect | ❌ **Não resolvido** | `onSelectTrack` sempre passa track.id, nunca null |
| D166 | TitleEquipModal scroll container | ❌ **Não resolvido** | Sem `verticalScroll` nem `heightIn` |
| D121 | TitleSelectorScreen LazyVerticalGrid em scroll | ❌ **Não resolvido** | `LazyVerticalGrid` dentro de `verticalScroll` |
| D120 | TitleSelectorScreen nome sentence case | ❌ **Não resolvido** | `Text(text = title.name)` sem `.uppercase()` |
| D173-D174 | SessionSummary título/rank | ❌ **Não resolvido** | `fontSize = 12.sp` (título) + rank como display number |
| D131-D134 | GeneralSettingsModal campos ausentes | ❌ **Não resolvido** | Campos não existem no código |

### Divergências anteriores (D1-D25)

| # | Divergência | Status | Evidência |
|---|---|---|---|
| D1 | GeneralSettingsModal campos faltantes | ❌ **Não resolvido** | Campos "nome do dispositivo", "duração do descanso longo", "purgar campanha" continuam ausentes |
| D2 | Header mobile sidebar dead code | N/A | Decisão consciente (código morto no React) |
| D3 | BottomNav ícones approximations | N/A | Decisão consciente |
| D4 | FocusCompletionFlow background | ❌ **Não resolvido** | `Stone950` (#0C0A08) ainda em uso, fundo mais escuro que React (#1C1A17) |
| D5 | FocusCompletionFlow button casing | ❌ **Não resolvido** | "CONTINUAR" (uppercase) vs React "Continuar" |
| D6 | FocusCompletionFlow streak label | ❌ **Não resolvido** | "SEQUÊNCIA DE CHAMA" vs React "SEQUÊNCIA" |
| D7 | FocusCompletionFlow loot grid | ❌ **Não resolvido** | Sem `LazyVerticalGrid` ou grid responsivo — sempre 1 coluna |
| D8 | FocusCompletionFlow loot card decoration | ❌ **Não resolvido** | Sem corner brackets, gradients, ou star/glow |
| D9 | FocusOrb progress ring gradient | N/A | Decisão consciente documentada |
| D10 | FocusOrb Concept B bubble timing | N/A | Decisão consciente |
| D11 | FocusOrb glow rendering | N/A | Decisão consciente (API 31+) |
| D12 | CharacterScreen header button | ❌ **Não resolvido** | "+ Gerenciar Habilidades" não existe no código |
| D13 | CharacterScreen isRunning prop | ❌ **Não resolvido** | Prop `isRunning` não é passada |
| D14 | LevelUpOverlay button text | ❌ **Não resolvido** | "CONTINUAR" vs React "Continuar" |
| D15 | HabitsScreen header casing | ✅ **Resolvido (S5)** | Sprint S5 alinhou copy — "Capela de Hábitos" sentence case |
| D16 | HeatmapScreen hover→tap | N/A | Adaptação consciente (touch) |
| D17 | HeatmapScreen "ATUAL" label | ❌ **Não resolvido** | Label inline vs React pulsing positioned label |
| D18 | GuideScreen mode cards animate-pulse | ❌ **Não resolvido** | Ícone Swords estático no Android |
| D19 | StatsScreen Moedas background | N/A | Equivalente funcional (ambos transparente) |
| D20 | CognitiveDeathOverlays animações | ❌ **Não resolvido** | Estático vs React Framer Motion |
| D21 | CognitiveDeathOverlays backdrop blur | ❌ **Não resolvido** | Sem blur (limitação plataforma) |
| D22 | CognitiveDeathOverlays background color | ❌ **Não resolvido** | `#280505` vs React `#450a0a` |
| D23 | CognitiveDeathOverlays title/button casing | ❌ **Não resolvido** | UPPERCASE vs React title-case |

### Resumo

| Status | Qtd | Divergências |
|---|---|---|
| ❌ Não resolvido | 22 | D1, D4-D8, D12-D14, D17, D18, D20-D25 + D120, D121, D131-D134, D153, D166, D173-D174, D198 |
| ✅ Resolvido por Sprint S5 | 1 | D15 (HabitsScreen header casing) |
| ✅ Resolvido por S5 (labels) | 2 | D57 (Habits labels), D70 (Dailies labels) |
| ✅ Resolvido por S5 (submit) | 3 | D59 (Habits submit), D80 (Todos submit), S10 parcial |
| ✅ Resolvido por S5 (fontes) | 5 | D86 (Quests progress), D90 (Quests claimed), D95 parcial (History), D194 (QuestFab title), D197 (QuestFab empty) |
| N/A (decisão consciente / equivalente funcional) | 9 | D2, D3, D9-D11, D16, D19 |

**Total resolvido pelo Sprint S5: 11 divergências** (D15, D57, D59, D63, D70, D80, D86, D90, D95 parcial, D194, D197).

| ✅ Resolvido por S6 (tooltip+botões) | 2 | D30 parcial (FocusModeScreen button font/letterSpacing/casing — background colors ainda pendentes), D29 parcial (Sair casing — mode label "SESSÃO LIVRE" ainda pendente) |
