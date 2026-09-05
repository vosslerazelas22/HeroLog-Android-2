# Audita Visual — Módulo Reino (Kingdom)

**Data:** 02/09/2026
**Módulo avaliado:** Reino (Loja, Loja de Títulos, Heatmap, Seletor de Títulos, Estatísticas, Conquistas, Guia, Registros)
**Fontes React:** `ShopTab.tsx`, `TitleShop.tsx`, `HeatmapTab.tsx`, `TitleSelector.tsx`, `StatsTab.tsx`, `AchievementsTab.tsx`, `GuideTab.tsx`, bloco `activeTab === 'logs'` de `App.tsx`
**Implementações Android:** `ui/kingdom/` (8 telas) + `logic/kingdom/` (2 arquivos)

---

## Resultado Geral

| Critério | Status |
|---|---|
| Fidelidade lógica | **Fiel** em todos os 8 sub-módulos |
| Fidelidade visual | **Não validada** (sem prints reais lado a lado) |

---

## 1. Loja (ShopTab.tsx → ShopScreen.kt + ShopLogic.kt)

### Fidelidade Lógica: Fiel

- Catálogo: 9 itens, campo a campo idênticos (nomes, emojis, descrições, preços, buff types, isEquipment, charges)
- `ShopLogic.buyItem`: replica `buyGoblinShopItem` + `handlePurchase` — guard de gold, lookup de rarity via LootTable, id `"${buff}_$now"`, `InventoryLogic.addItemToInventory`
- StreakShield gate: duplicado só na UI (não no logic), fiel à fonte

### Fidelidade Visual: Não validada — Análise campo a campo

| Elemento | React | Android | Status |
|---|---|---|---|
| Card background | `bg-stone-950/20` | `Stone950.copy(alpha = 0.20f)` | Fiel |
| Card border (afford) | `border-amber-500/10` | `Amber500.copy(alpha = 0.10f)` | Fiel |
| Card border (can't afford) | `border-stone-800` | `Stone800` | Fiel |
| Emoji box | `bg-amber-500/5 border border-amber-500/10 p-2.5 rounded-lg` | `size(44.dp), bg Amber500/5%, border Amber500/10%, rounded 8.dp` | Fiel |
| Item name | `font-serif font-bold text-sm text-amber-200` | `FontFamily.Serif, FontWeight.Bold, 13.sp, Amber200` | Fiel |
| Item desc | `text-xs text-amber-100/50 leading-relaxed` | `11.sp, Amber100Half.copy(alpha=0.50f), lineHeight=15.sp` | Fiel |
| Button (afford) | `bg-champagne-500/10 text-champagne-300 border-champagne-500/40` | `Champagne500/10%, Champagne300, border Champagne500/40%` | Fiel |
| Button (can't afford) | `bg-stone-800/10 text-stone-500 border-stone-800` | `Stone800/10%, Stone400, Stone800` | Bug #1 |
| "Já ativo" badge | `text-amber-500 border-amber-500/20 bg-amber-500/5` | `Amber500, border Amber500/20%, bg Amber500/5%` | Fiel |
| ChevronRight icon | `w-3.5 h-3.5` | `Modifier.size(14.dp)` | Fiel |
| Padding do card | `p-3.5` | `padding(14.dp)` | Fiel |
| Gap between items | `space-y-3` | `LazyColumn spacedBy(12.dp)` | Fiel |
| Ícone cabeçalho | `ShoppingBag` (import mas não renderizado no JSX) | `MonetizationOn` | Verificar wrapper React |

### Divergências Loja

**Bug #1 — Cor do texto botão "can't afford"**
- React: `text-stone-500` (`#78716C`)
- Android: `Stone400` (`#A8A29E`) em `ShopScreen.kt:320`
- Impacto: texto mais claro que a fonte
- Ação: trocar `Stone400` por `Stone500` (já definido em `TitleShopScreen.kt:67`)

---

## 2. Loja de Títulos (TitleShop.tsx → TitleShopScreen.kt)

### Fidelidade Lógica: Fiel

- 6 seções: Common/Rare/Epic/Legendary/Achievement/Drop — cada uma filtrando `TITLE_CATALOG` por `category`
- `onBuyTitle` e `onClaimAchievementTitle` chamam `HeroLogViewModel`
- Lógica de `isAchievementUnlocked` idêntica

### Fidelidade Visual: Não validada — Análise campo a campo

| Elemento | React | Android | Status |
|---|---|---|---|
| Header intro | `text-xs uppercase font-serif tracking-[0.14em] text-amber-400` | `11.sp, letterSpacing 0.10.em, Amber500` | Bug #2 |
| Section header (Comprar) | `ShoppingBag w-4 h-4 text-amber-400` | `ShoppingCart size(16.dp) tint Amber500` | Desvio #1 |
| COMMON label | `text-amber-100/30` | `Amber100Half.copy(alpha=0.30f)` | Fiel |
| RARE label | `text-[#d97706]` | `AmberD97706` (`#D97706`) | Fiel |
| EPIC label | `text-purple-400` | `Purple400` (`#C084FC`) | Fiel |
| LEGENDARY label | `text-yellow-400` | `Yellow400` (`#FACC15`) | Fiel |
| Pulse dot (Epic/Legendary) | `animate-pulse` | `infiniteRepeatable alpha animation` | Fiel |
| Sublabel (Epic) | `text-[8px] text-[#38bdf8]` | `8.sp, Cyan400` | Fiel |
| Card background | `${title.colorClass}` | `title.visualStyle.backgroundColor` | Fiel |
| Perks color | `text-[#38bdf8]` | `Cyan400` | Fiel |
| "Comprar" button bg | `bg-[#40301a]` | `Color(0xFF40301A)` | Fiel |
| "Comprar" button text | `text-amber-300` | `Champagne300` (`#F5DFA0`) | Bug #3 |
| Price format | `toLocaleString()` | `NumberFormat pt-BR` | Fiel (localidade) |
| "Desbloqueado" badge | `text-emerald-400/80 bg-emerald-500/[0.04] border-emerald-500/20` | `Emerald400/80%, Emerald400/4%, Emerald400/20%` | Fiel |
| Achievement "Resgatar" button | `bg-amber-600 animate-pulse` | `Color(0xFFD97706) + infinite alpha pulse` | Fiel |
| Grid columns Common/Rare | `grid-cols-2` | `columns = 2` | Fiel |
| Grid columns Epic/Legendary | `grid-cols-1` | `columns = 1` | Fiel |

### Divergências Loja de Títulos

**Bug #2 — Cor do header intro**
- React: `text-amber-400`
- Android: `Amber500` em `TitleShopScreen.kt:135`
- Impacto: tom mais escuro que a fonte
- Ação: definir `Amber400` local e usar

**Bug #3 — Cor do texto "Comprar" button**
- React: `text-amber-300` (`#FCD34D`, mais amarelo)
- Android: `Champagne300` (`#F5DFA0`, mais dourado/pálido) em `TitleShopScreen.kt:629`
- Impacto: tom levemente diferente
- Ação: definir `Amber300 = Color(0xFFFCD34D)` local e usar

**Desvio #1 — Ícone da seção "Títulos Adquiríveis"**
- React: `ShoppingBag`
- Android: `ShoppingCart`
- Classificação: desvio consciente — `ShoppingCart` é semanticamente equivalente mas visualmente diferente

---

## 3. Heatmap (HeatmapTab.tsx → HeatmapScreen.kt + HeatmapLogic.kt)

### Fidelidade Lógica: Fiel

- `HeatmapLogic.calculate`: replicado literalmente — minutesByDay, parseDatePart, consistência 30 dias com fix de startI, grid de cells, monthLabels
- 5 faixas de MinuteBucket idênticas
- `formatMinutes` idêntico

### Fidelidade Visual: Não validada — Análise campo a campo

| Elemento | React | Android | Status |
|---|---|---|---|
| Consistency card bg | `bg-gradient-to-r from-stone-950 via-purple-950/20 to-stone-950` | `Brush.horizontalGradient(Stone950, Purple950/20, Stone950)` | Fiel |
| Corner brackets | 4x `absolute w-2 h-2 border-t border-l ...` | Ausente | Desvio #2 |
| Title "CONSISTÊNCIA..." | `text-[#E2B054] font-black text-xs md:text-sm` | `GoldAccent, FontWeight.Black, 11.sp` | Fiel |
| Percentage | `text-3xl md:text-4xl font-black text-amber-200` | `34.sp, FontWeight.Black, Amber200` | Fiel |
| Progress bar segments | `w-2 h-5 rounded-[1px]` gradient/emerald | `width(8.dp) height(20.dp)` solid Emerald600 | Desvio #3 |
| Scope toggle | `bg-stone-950/80 border-amber-500/20` | `Stone950/80%, Amber500/20%` | Fiel |
| Toggle selected | `bg-amber-500/15 text-[#E2B054] border-amber-500/25` | `Amber500/15%, GoldAccent, Amber500/25%` | Fiel |
| Streak badge | `text-[#E2B054] bg-[#C29544]/5 border-[#C29544]/20` | `GoldAccent, GoldMedieval/5%, GoldMedieval/20%` | Fiel |
| Month labels | `text-amber-500/80` | `Amber500/80%` | Fiel |
| Day labels (visible) | `text-amber-100/40` | `Amber100Half/40%` | Fiel |
| Cell: 0 min | `bg-stone-950/40 border-amber-500/5` | `BucketZeroBg, Amber500/5%` | Fiel |
| Cell: 1-29 min | `bg-[#132c1c] border-[#1a452a]` | `#132C1C, #1A452A` | Fiel |
| Cell: 30-59 min | `bg-[#1b5e20] border-[#2e7d32]` | `#1B5E20, #2E7D32` | Fiel |
| Cell: 60-89 min | `bg-[#4caf50] border-[#81c784]` | `#4CAF50, #81C784` | Fiel |
| Cell: 90+ min | `bg-[#C29544] border-amber-300 shadow` | `GoldMedieval, Amber300` (sem shadow) | Desvio #4 |
| Future cell | `bg-stone-900 opacity-20` | `Stone900/20%` | Fiel |
| Today cell (0 min) | `border-[#C29544] shadow-[0_0_8px...]` | `GoldMedieval border` (sem shadow) | Desvio #4 |
| "★" indicator | `font-extrabold` | `FontWeight.Black` | Fiel |
| "ATUAL" label | `animate-pulse bg-stone-950 border-amber-500/30` | Ausente | Desvio #5 (registrado) |
| Stat cards bg | `bg-stone-950/40 border-[#C29544]/20` | `Stone950/40%, GoldMedieval/20%` | Fiel |
| "Este Mês" card | `bg-stone-950/50 border-[#C29544]/30 shadow-[inset...]` | `Stone950/50%, GoldMedieval/30%` (sem inset shadow) | Desvio #4 |
| Legend hint text | `text-[9px] text-[#A2A2A2]` | `9.sp, HintMuted (#A2A2A2)` | Fiel |

### Divergências Heatmap

**Desvio #2 — Corner brackets do card de consistência**
- React: 4 cantos decorativos (`absolute w-2 h-2 border-t border-l ...`)
- Android: ausente
- Classificação: simplificação visual consciente já documentada em `PARIDADE.md`

**Desvio #3 — Barra de progresso segmentada**
- React: gradiente (`bg-gradient-to-t from-emerald-600 to-emerald-400`) com glow (`shadow-[0_0_8px...]`)
- Android: cor sólida `Emerald600`
- Classificação: simplificação visual — gradiente e glow são ornamentação CSS não-funcional
- Ação: registrar como desvio consciente

**Desvio #4 — Shadows em células e cards**
- React: `shadow-[0_0_8px_rgba(194,149,68,0.25/0.45)]` em células 90+, today, e "Este Mês"
- Android: sem shadow
- Classificação: simplificação visual — shadows customizados são frágeis em Compose e de baixo impacto funcional
- Ação: registrar como desvio consciente

**Desvio #5 — "ATUAL" label**
- React: label animado `animate-pulse` posicionado abaixo da semana corrente
- Android: ausente
- Classificação: simplificação consciente já documentada em `PARIDADE.md`
- Ação: revisitar durante validação visual formal se necessário

**Desvio #6 — Tooltip flutuante → tap-to-select**
- React: tooltip flutuante posicionado via `getBoundingClientRect` com `onMouseEnter`/`onTouchStart`
- Android: toque seleciona célula, detalhe aparece em linha fixa abaixo da grade
- Classificação: adaptação consciente para touch (sem hover real) já documentada em `PARIDADE.md`

---

## 4. Seletor de Títulos (TitleSelector.tsx → TitleSelectorScreen.kt)

### Fidelidade Lógica: Fiel

- Filtra `TITLE_CATALOG` por `ownedTitles`
- `onEquipTitle` / `onEquipTitle(null)` para equipar/desequipar
- Empty state com texto idêntico

### Fidelidade Visual: Não validada — Análise campo a campo

| Elemento | React | Android | Status |
|---|---|---|---|
| Header intro | `text-champagne-400` | `Champagne400` | Fiel |
| Empty state icon | `Award w-12 h-12 text-amber-500/20 animate-pulse` | `WorkspacePremium 48.dp Amber500/20% alpha pulse` | Fiel |
| Empty state title | `text-champagne-400` | `Champagne400` | Fiel |
| "Seus Títulos" header | `Shield w-4 h-4 text-champagne-400` | `Shield 14.dp Champagne400` | Fiel |
| Counter text | `text-champagne-300` | `Champagne300` | Fiel |
| Grid columns | `grid-cols-1 sm:grid-cols-2` | `LazyVerticalGrid Fixed(2)` | Fiel (mobile = sm) |
| Card equipped bg | `border-amber-500 bg-[#1c1917]/90 shadow-[0_0_15px...]` | `Amber500 border, Stone900/90% bg` (sem shadow) | Desvio #7 |
| "ATIVO" badge | `absolute -top-2.5 -right-2` | `offset(x=6.dp, y=-6.dp)` | Fiel (decisão registrada) |
| Category label | `text-[8px] uppercase bg-stone-950/40 border-stone-800/45` | `8.sp bg Stone950/40% border Stone800/45%` | Fiel |
| Perks ⚡ prefix | `⚡ {p}` | `Text(text = perk)` — sem ⚡ | Bug #4 |
| "Equipar" button | `bg-amber-500 text-stone-950 border border-amber-400` | `Amber500 bg, Stone950 text, sem border` | Bug #5 |
| "Remover" button | `bg-stone-800 text-stone-300 border-stone-700/50` | `Stone800 bg, Stone400 text, Stone700/50% border` | Bug #6 |
| Grid height | CSS grid (intrinsic) | `LazyVerticalGrid` com `height(600.dp)` fixo | Desvio #8 |

### Divergências Seletor de Títulos

**Bug #4 — Prefixo ⚡ ausente nos perks**
- React: `⚡ {p}` (linha 82 de `TitleSelector.tsx`)
- Android: `Text(text = perk)` sem prefixo em `TitleSelectorScreen.kt:291`
- Impacto: visual dos perks difere — falta o emoji de raio
- Ação: adicionar `"⚡ "` como prefixo do texto de cada perk

**Bug #5 — Borda ausente no botão "Equipar"**
- React: `border border-amber-400` (linha 101 de `TitleSelector.tsx`)
- Android: sem `.border()` no modifier em `TitleSelectorScreen.kt:325`
- Impacto: botão sem contorno visual
- Ação: adicionar `.border(1.dp, Amber400)` ao modifier

**Bug #6 — Cor do texto "Remover"**
- React: `text-stone-300` (`#D6D3D1`)
- Android: `Stone400` (`#A8A29E`) em `TitleSelectorScreen.kt:318`
- Impacto: texto mais escuro que a fonte
- Ação: trocar por `Stone300` local

**Desvio #7 — Shadow ausente no card equipado**
- React: `shadow-[0_0_15px_rgba(245,158,11,0.25)]`
- Android: sem shadow
- Classificação: simplificação visual — shadow CSS não-funcional

**Desvio #8 — Altura fixa do grid (600.dp)**
- React: CSS grid com altura intrínseca
- Android: `LazyVerticalGrid` com `height(600.dp)`
- Classificação: desvio consciente — necessidade do `LazyVerticalGrid` em Compose

---

## 5. Estatísticas (StatsTab.tsx → StatsScreen.kt)

### Fidelidade Lógica: Fiel

- `averageSessionLength = totalMinutes / totalSessions` com guarda de div/0
- `totalHours = (totalMinutes / 60).toFixed(1)` — Android usa `String.format("%.1f", ...)` idêntico
- 6 cards com labels, values, descriptions idênticos

### Fidelidade Visual: Não validada — Análise campo a campo

| Elemento | React | Android | Status |
|---|---|---|---|
| Grid | `grid-cols-2 gap-3.5` | `LazyVerticalGrid Fixed(2) spacedBy(14.dp)` | Fiel |
| Card padding | `p-3.5` | `padding(14.dp)` | Fiel |
| Label | `text-[10px] uppercase font-serif text-amber-100/40` | `10.sp, FontFamily.Serif, Amber100/40%` | Fiel |
| Value | `text-xl md:text-2xl font-bold font-mono text-amber-100` | `FontFamily.Monospace, FontWeight.Bold, 22.sp, Amber100` | Fiel |
| Desc | `text-[10px] text-amber-100/30` | `10.sp, Amber100/30%` | Fiel |
| Icon opacity | `opacity-50` | `tint.copy(alpha = 0.50f)` | Fiel |
| Cards 1-4,6 bg | respectivos `bg-{color}-500/[0.02]` | respectivos `Color.copy(alpha = 0.02f)` | Fiel |
| Card 5 (Moedas) bg | `bg-amber-550/[0.02]` (inválido) | `Color.Transparent` | Fiel (replicou o bug) |

### Divergências Estatísticas

Nenhuma divergência encontrada. Todas as 6 combinações de cor de card batem campo a campo.

---

## 6. Conquistas (AchievementsTab.tsx → AchievementsScreen.kt)

### Fidelidade Lógica: Fiel

- 8 conquistas: ids, nomes, descrições, emojis, check functions — todos idênticos ao `AchievementCatalog`
- `AchievementLogic.isUnlocked` = `state.achievements.includes(id) || check(state)` — idêntico

### Fidelidade Visual: Não validada — Análise campo a campo

| Elemento | React | Android | Status |
|---|---|---|---|
| Card bg (unlocked) | `bg-amber-500/[0.03] border-amber-500/30` | `Amber500/3%, Amber500/30%` | Fiel |
| Card bg (locked) | `bg-stone-950/10 border-stone-900 opacity-40` | `Stone950/10%, Stone900, alpha 0.40f` | Fiel |
| Icon box | `text-3xl bg-stone-950/40` | `28.sp, Stone950/40%` | Fiel |
| Lock icon | `Lock w-6 h-6 text-stone-600` | `Lock 24.dp Stone600` | Fiel |
| Name | `font-serif font-bold text-sm text-amber-100/90` | `FontFamily.Serif, FontWeight.Bold, 13.sp, Amber100/90%` | Fiel |
| "Desbloqueado" badge | `text-champagne-400 bg-champagne-500/10 border-champagne-500/20` | `Champagne400, Champagne500/10%, Champagne500/20%` | Fiel |
| Desc | `text-xs text-amber-100/40` | `12.sp, Amber100/40%` | Fiel |
| Badge text | `"Desbloqueado"` (capitalized) | `"DESBLOQUEADO"` (uppercase) | Desvio #9 |

### Divergências Conquistas

**Desvio #9 — Capitalização do badge "Desbloqueado"**
- React: `Desbloqueado` (capitalized)
- Android: `DESBLOQUEADO` (uppercase)
- Classificação: desvio consciente — Compose não tem `text-transform: capitalize`, ambos transmitem o mesmo significado
- Ação: registrar como desvio consciente

---

## 7. Guia (GuideTab.tsx → GuideScreen.kt)

### Fidelidade Lógica: Fiel

Conteúdo 100% estático, texto transcrito literalmente a partir da fonte.

### Fidelidade Visual: Não validada — Análise campo a campo

| Elemento | React | Android | Status |
|---|---|---|---|
| Section 1 bg | `bg-amber-500/[0.02] border-amber-500/10` | `Amber500/2%, Amber500/10%` | Fiel |
| Section 3 bg | `bg-stone-950/40 border-amber-500/15` | `Stone950/40%, Amber500/15%` | Fiel |
| Modo Padrão bg | `bg-stone-900/30 border-stone-850` | `Stone900/30%, Stone900` | Fiel (stone-850 inválido) |
| Modo Masmorra bg | `bg-purple-950/[0.08] border-purple-500/20` | `Purple950/8%, Purple500/20%` | Fiel |
| Modo Selvagem bg | `bg-red-950/[0.08] border-red-500/20` | `Red950/8%, Red500/20%` | Fiel |
| Table header bg | `bg-stone-900/60` | `Stone900/60%` | Fiel |
| Masmorra row bg | `bg-purple-950/15` | `Purple950/15%` | Fiel |
| Selvagem row bg | `bg-red-950/15` | `Red950/15%` | Fiel |
| Section 4 bg | `bg-red-500/[0.02] border-red-500/10` | `Red500/2%, Red500/10%` | Fiel |
| Decision guide bg | `bg-amber-500/[0.01] border-amber-500/5` | `Amber500/1%, Amber500/5%` | Fiel |

### Divergências Guia

Nenhuma divergência funcional. Ícones mapeados por aproximação semântica (já documentado). Texto transcrito literalmente.

---

## 8. Registros / Logs (App.tsx logs block → LogsScreen.kt)

### Fidelidade Lógica: Fiel

- Lista de `LogEntry` com `id`, `time`, `text`, `highlighted`
- Empty state com texto idêntico: "Nenhum sussurro celestial registrado até o momento..."
- Cap de 51 entradas no ViewModel (não na tela)

### Fidelidade Visual: Não validada — Análise campo a campo

| Elemento | React | Android | Status |
|---|---|---|---|
| Wrapper bg | `bg-quest-panel` (custom) | `Color(0xFF1A1614)` | Fiel |
| Wrapper border | `border-amber-500/15` | `Amber500/15%` | Fiel |
| Wrapper shadow | `shadow-[0_12px_40px...]` | Ausente | Desvio #10 |
| Header icon | `Scroll` (lucide) | `Icons.Filled.Menu` | Bug #7 |
| Header icon color | `text-champagne-500` | `Amber500` | Bug #8 |
| Header text | `text-champagne-400` | `Amber400` | Bug #9 |
| Log panel bg | `bg-stone-950/90` | `Stone950/90%` | Fiel |
| Log panel border | `border-amber-500/10` | `Amber500/10%` | Fiel |
| Log panel height | `h-[420px]` | `height(420.dp)` | Fiel |
| Timestamp color | `text-amber-400/50` | `Amber400/50%` | Fiel |
| Log text (highlighted) | `text-amber-200 font-bold` | `Amber100/92%, FontWeight.Bold` | Bug #10 |
| Log text (normal) | `text-amber-100/40` | `Amber100/40%` | Fiel |
| Empty state | `text-amber-100/30 italic` | `Amber100/30%, FontStyle.Italic` | Fiel |
| Font | `font-mono` | `FontFamily.Monospace` | Fiel |

### Divergências Logs

**Bug #7 — Ícone do header**
- React: `Scroll` (lucide-react, ícone de pergaminho)
- Android: `Icons.Filled.Menu` (hamburger) em `LogsScreen.kt:76`
- Impacto: ícone não representa "registro/atividade"
- Ação: trocar por `Icons.AutoMirrored.Filled.Article` ou `Icons.Filled.Note`

**Bug #8 — Cor do ícone header**
- React: `text-champagne-500`
- Android: `Amber500` em `LogsScreen.kt:78`
- Impacto: cor mais amarela que a fonte
- Ação: trocar por `Champagne500` (já definido localmente)

**Bug #9 — Cor do texto header**
- React: `text-champagne-400`
- Android: `Amber400` em `LogsScreen.kt:83`
- Impacto: cor mais amarela que a fonte
- Ação: trocar por `Champagne400`

**Bug #10 — Cor do log highlighted**
- React: `text-amber-200` (`#FDE68A`)
- Android: `Amber100/92%` (`#FEF3C7` com alpha) em `LogsScreen.kt:123`
- Impacto: tom levemente diferente
- Ação: definir `Amber200` local e usar com alpha alto

**Desvio #10 — Shadow ausente no wrapper**
- React: `shadow-[0_12px_40px_rgba(0,0,0,0.7)]`
- Android: sem shadow
- Classificação: simplificação visual — shadow CSS pesado, baixo impacto funcional

---

## Resumo das Divergências

### Bugs reais (devem ser corrigidos)

| # | Módulo | Descrição | Arquivo:linha Android | Ação proposta |
|---|---|---|---|---|
| 1 | Loja | Cor do texto botão "can't afford": `Stone400` em vez de `Stone500` | `ShopScreen.kt:320` | Trocar `Stone400` por `Stone500` local |
| 2 | Loja Títulos | Cor do header intro: `Amber500` em vez de `Amber400` | `TitleShopScreen.kt:135` | Definir `Amber400` local, usar |
| 3 | Loja Títulos | Cor texto "Comprar": `Champagne300` em vez de `Amber300` | `TitleShopScreen.kt:629` | Definir `Amber300` local, usar |
| 4 | Seletor Títulos | Prefixo ⚡ ausente nos perks | `TitleSelectorScreen.kt:291` | Adicionar `"⚡ "` ao texto |
| 5 | Seletor Títulos | Borda ausente no botão "Equipar" | `TitleSelectorScreen.kt:325` | Adicionar `.border(1.dp, ...)` |
| 6 | Seletor Títulos | Cor texto "Remover": `Stone400` em vez de `Stone300` | `TitleSelectorScreen.kt:318` | Trocar por `Stone300` local |
| 7 | Logs | Ícone `Menu` em vez de `Article`/`Scroll` | `LogsScreen.kt:76` | Trocar ícone |
| 8 | Logs | Cor ícone: `Amber500` em vez de `Champagne500` | `LogsScreen.kt:78` | Trocar por `Champagne500` |
| 9 | Logs | Cor texto header: `Amber400` em vez de `Champagne400` | `LogsScreen.kt:83` | Trocar por `Champagne400` |
| 10 | Logs | Cor highlighted: `Amber100/92%` em vez de `Amber200` | `LogsScreen.kt:123` | Usar `Amber200` com alpha alto |

### Desvios conscientes (a registrar ou já registrados)

| # | Módulo | Descrição | Registrado? |
|---|---|---|---|
| 1 | Loja Títulos | Ícone `ShoppingCart` vs `ShoppingBag` | Novo — registrar |
| 2 | Heatmap | Corner brackets ausentes | Sim (`PARIDADE.md`) |
| 3 | Heatmap | Barra progresso: gradiente → cor sólida | Novo — registrar |
| 4 | Heatmap | Shadows em células e "Este Mês" ausentes | Novo — registrar |
| 5 | Heatmap | "ATUAL" label ausente | Sim (`PARIDADE.md`) |
| 6 | Heatmap | Tooltip hover → tap-to-select | Sim (`PARIDADE.md`) |
| 7 | Seletor Títulos | Shadow ausente no card equipado | Novo — registrar |
| 8 | Seletor Títulos | Grid height fixa 600.dp (LazyVerticalGrid) | Novo — registrar |
| 9 | Conquistas | Badge "DESBLOQUEADO" uppercase vs capitalized | Novo — registrar |
| 10 | Logs | Shadow ausente no wrapper | Novo — registrar |

### Simplificações visuais conscientes (ornamentação CSS não-funcional)

- Heatmap: sem glow/shadow em células, sem animação pulso no "ATUAL", sem inset shadow no "Este Mês"
- Logs: sem shadow no wrapper
- TitleSelector: sem shadow no card equipado
- TitleShop: sem tooltip HTML no "Loot Raro" (touch, sem hover)

---

## Atualização sugerida para o PARIDADE.md

Nenhuma alteração de status necessária — todos os sub-módulos permanecem "Em andamento" com "Fidelidade Visual: Não validado". As divergências acima são pendências de correção visual, não de lógica.

A seção de Logs pode ter sua nota atualizada para incluir as 3 divergências de ícone/cor encontradas (#7-9), que são bugs visuais reais mas de baixo impacto.
