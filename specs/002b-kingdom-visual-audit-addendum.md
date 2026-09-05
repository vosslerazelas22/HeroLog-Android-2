# Spec 002b — Auditoria Visual Complementar: Tipografia, Layout e Espaçamento

**Data**: 2026-09-03
**Escopo**: Todas as telas do módulo Reino (`app/src/main/java/.../kingdom/*.kt`) comparadas com
suas fontes React correspondentes. Foco em tipografia (família, peso, tamanho, line-height),
layout/alinhamento e outros elementos visuais **fora** do escopo de cores/ícone/prefixo já
fechados no spec 002.

**Arquivos examinados** (React):
- `src/modules/kingdom/ShopTab.tsx` (154L)
- `src/modules/kingdom/TitleShop.tsx` (209L)
- `src/modules/character/TitleSelector.tsx` (115L)
- `src/modules/kingdom/HeatmapTab.tsx` (500L)
- `src/modules/kingdom/StatsTab.tsx` (87L)
- `src/modules/kingdom/AchievementsTab.tsx` (106L)
- `src/modules/kingdom/GuideTab.tsx` (183L)
- `src/App.tsx` bloco `activeTab === 'logs'` (linhas 3517-3539)

**Arquivos examinados** (Android):
- `app/src/main/java/.../kingdom/ShopScreen.kt` (332L)
- `app/src/main/java/.../kingdom/TitleShopScreen.kt` (636L)
- `app/src/main/java/.../kingdom/TitleSelectorScreen.kt` (360L)
- `app/src/main/java/.../kingdom/LogsScreen.kt` (138L)
- `app/src/main/java/.../kingdom/StatsScreen.kt` (242L)
- `app/src/main/java/.../kingdom/AchievementsScreen.kt` (191L)
- `app/src/main/java/.../kingdom/GuideScreen.kt` (639L)
- `app/src/main/java/.../kingdom/HeatmapScreen.kt` (648L)
- `app/src/main/java/.../ui/theme/Type.kt` (119L) — definição de fontes customizadas
- `app/src/main/java/.../ui/theme/Color.kt` (23L) — tokens de cor do tema

**Decisões conscientes já documentadas** (não são bugs):
- Tooltips de hover do Heatmap viram tap-to-select (touch puro, sem hover real)
- Sem corner brackets decorativos no card de consistência (ornamentação CSS)
- Sem glow/shadow customizado nas células do heatmap
- Sem animação de pulso no rótulo "ATUAL"
- Preço formatado `pt-BR` com `NumberFormat`
- Grid sem `LazyVerticalGrid` (aninhado dentro de `verticalScroll`)

---

## Achados

### Problema 1 — FontFamily.Serif não resolve para Cinzel (afeta TODAS as telas)

**Trecho/local**: Todos os arquivos `kingdom/*.kt` que usam `FontFamily.Serif` —
ex: `ShopScreen.kt:113`, `TitleShopScreen.kt:133`, `TitleSelectorScreen.kt:119`,
`LogsScreen.kt` (sem-serif override), `StatsScreen.kt:172`, `AchievementsScreen.kt:86`,
`GuideScreen.kt:118`, `HeatmapScreen.kt:129` (e ~30+ outras ocorrências)

**Por que é problemático**: `FontFamily.Serif` é a família de fontes **sistema** do Android
(geralmente Noto Serif). O `Type.kt` define `Cinzel` como a família serif customizada
(`res/font/cinzel_*.ttf`), mas nenhum componente do Reino a referencia — usam
`FontFamily.Serif` diretamente. O React usa `font-serif` que mapeia para Cinzel via
`index.css`. Resultado: o Android renderiza Noto Serif em vez de Cinzel em todo o módulo
Reino.

**Severidade**: CRÍTICO

**Correção proposta**: Trocar todas as ocorrências de `FontFamily.Serif` nos arquivos do
Reino por `Cinzel` (import de `com.iurispraecepta.herolog.ui.theme.Cinzel`). Alternativa:
criar um `val HeroSerif = Cinzel` no theme para clareza semântica.

---

### Problema 2 — FontFamily.Monospace não resolve para JetBrains Mono (afeta TODAS as telas)

**Trecho/local**: Todos os arquivos `kingdom/*.kt` que usam `FontFamily.Monospace` —
ex: `ShopScreen.kt:318`, `TitleShopScreen.kt:325`, `TitleSelectorScreen.kt:281`,
`LogsScreen.kt:122`, `StatsScreen.kt:228`, `HeatmapScreen.kt:224` (e ~15+ outras
ocorrências)

**Por que é problemático**: `FontFamily.Monospace` é a família monospace **sistema**
(geralmente Droid Sans Mono ou Noto Sans Mono). O `Type.kt` define `JetBrainsMono` como
a família monospace customizada (`res/font/jetbrains_mono_*.ttf`), mas nenhum componente a
referencia. O React usa `font-mono` que mapeia para JetBrains Mono. Resultado: o Android
renderiza a monospace do sistema em vez de JetBrains Mono.

**Severidade**: CRÍTICO

**Correção proposta**: Trocar todas as ocorrências de `FontFamily.Monospace` nos arquivos
do Reino por `JetBrainsMono` (import de
`com.iurispraecepta.herolog.ui.theme.JetBrainsMono`).

---

### Problema 3 — LogsScreen usa `androidx.compose.ui.text.font.FontFamily.Monospace` em vez de `FontFamily.Monospace` do imports

**Trecho/local**: `LogsScreen.kt:122`, `LogsScreen.kt:130`

**Por que é problemático**: Enquanto todos os outros arquivos do Reino usam
`FontFamily.Monospace` (via import estático de `FontFamily`), o `LogsScreen.kt` usa o
caminho totalmente qualificado `androidx.compose.ui.text.font.FontFamily.Monospace`. Isso
é funcionalmente equivalente mas inconsistentemente escrito — e reforça o bug dos
Problemas 1/2 porque escapa de eventual correção centralizada.

**Severidade**: BAIXO (inconsistência de estilo, não funcional)

**Correção proposta**: Adicionar `import androidx.compose.ui.text.font.FontFamily` e usar
`FontFamily.Monospace` diretamente (padrão dos demais arquivos).

---

### Problema 4 — FontWeight.Bold usado onde React usa font-black (FontWeight.Black)

**Trecho/local**:
- `AchievementsScreen.kt:149` — nome da conquista: `FontWeight.Bold`
  React: `AchievementsTab.tsx:89` — `font-bold` (`FontWeight.Bold`) ✓ OK aqui
- `AchievementsScreen.kt:86` — cabeçalho "CONQUISTAS": `FontWeight.Black` ✓ OK
- `StatsScreen.kt:172` — cabeçalho "ESTATÍSTICAS DO HERÓI": `FontWeight.Black` ✓ OK
- `StatsScreen.kt:213` — label do stat card: `FontWeight.Normal`
  React: `StatsTab.tsx:72` — `font-serif` (sem weight explícito = normal) ✓ OK
- `TitleShopScreen.kt:273` — section header "Títulos Adquiríveis (GP)": `FontWeight.Bold`
  React: `TitleShop.tsx:142` — `font-bold` ✓ OK
- `GuideScreen.kt:118` — cabeçalho "TUTORIAL": `FontWeight.Black` ✓ OK
- `GuideScreen.kt:223` — "MODO PADRÃO (NEUTRO)": `FontWeight.Black`
  React: `GuideTab.tsx:48` — `font-black` ✓ OK
- `GuideScreen.kt:438` — "ORIENTAÇÃO: QUANDO ESCOLHER...": `FontWeight.Black`
  React: `GuideTab.tsx:135` — `font-black` ✓ OK

**Análise**: Na maioria dos casos o peso está correto. Porém, há divergências específicas
nos componentes internos (ver Problema 5).

**Severidade**: MÉDIO (afeta sub-elementos, não cabeçalhos principais)

---

### Problema 5 — Name style dos title cards usa Bold em vez de Black

**Trecho/local**: `TitleShopScreen.kt:415` — `fontWeight = FontWeight.Bold` (nome do título
no card da loja)

**Por que é problemático**: React: `TitleShop.tsx:37` — `<strong className="text-[11px]
font-serif uppercase tracking-wider ...">` — `strong` em HTML não define weight
especificamente, mas o CSS base do React aplica `font-weight: 700` (Bold) via
`font-serif` (Cinzel Bold). No entanto, no `TitleSelector.tsx:67` o mesmo componente usa
`text-[12px] font-serif` sem weight explícito. Ambos os casos são `Bold` no React. No
Android, `TitleShopScreen.kt:415` usa `FontWeight.Bold` e `TitleSelectorScreen.kt:265`
também usa `FontWeight.Bold` — **correto**.

**Correção proposta**: Nenhuma — verificado e está correto.

---

### Problema 6 — Tamanho de fonte do nome do título no TitleShopScreen

**Trecho/local**: `TitleShopScreen.kt:416` — `fontSize = 11.sp`

**Por que é problemático**: React: `TitleShop.tsx:37` — `text-[11px]` = 11px. No Android,
11sp ≈ 11px em densidade padrão. **Correto**.

**Severidade**: N/A (verificado, sem bug)

---

### Problema 7 — AchievementsScreen: letterSpacing dos nomes de conquista

**Trecho/local**: `AchievementsScreen.kt:152` — `letterSpacing = 0.02.em`

**Por que é problemático**: React: `AchievementsTab.tsx:89` — `tracking-wide` = `letter-spacing:
0.025em`. Android usa `0.02.em`. Diferença de 0.005em — imperceptível visualmente.

**Severidade**: BAIXO

**Correção proposta**: Ajustar para `0.025.em` para fidelidade exata.

---

### Problema 8 — AchievementsScreen: descrição da conquista sem fontFamily explícito

**Trecho/local**: `AchievementsScreen.kt:184-187` — `Text(text = achievement.desc, fontSize = 12.sp, color = ...)` — sem `fontFamily`

**Por que é problemático**: A `Text` sem `fontFamily` herda do `Typography` default
(bodyLarge/bodyMedium = Inter). React: `AchievementsTab.tsx:98` — `<p className="text-xs
text-amber-100/40 font-serif">` — usa `font-serif` (Cinzel). No Android, a descrição
renderiza com Inter (sans-serif) em vez de Cinzel (serif).

**Severidade**: MÉDIO

**Correção proposta**: Adicionar `fontFamily = Cinzel` ao `Text` da descrição.

---

### Problema 9 — StatsScreen: letterSpacing dos labels dos stat cards

**Trecho/local**: `StatsScreen.kt:215` — `letterSpacing = 0.08.em`

**Por que é problemático**: React: `StatsTab.tsx:72` — `tracking-wider` = `letter-spacing:
0.05em`. Android usa `0.08.em`. Diferença de 0.03em — moderadamente perceptível em
texto pequeno (10sp).

**Severidade**: MÉDIO

**Correção proposta**: Ajustar para `0.05.em`.

---

### Problema 10 — StatsScreen: tamanho do valor dos stat cards

**Trecho/local**: `StatsScreen.kt:230` — `fontSize = 22.sp`

**Por que é problemático**: React: `StatsTab.tsx:78` — `text-xl md:text-2xl` = 20px mobile,
24px desktop. Android usa 22sp (intermediário). Aceitável como adaptação mobile, mas
não idêntico.

**Severidade**: BAIXO

**Correção proposta**: Considerar 20.sp para mobile (fiel ao breakpoint da fonte) ou
manter 22.sp como decisão consciente de adaptação.

---

### Problema 11 — LogsScreen: letterSpacing do título

**Trecho/local**: `LogsScreen.kt:90` — `letterSpacing = 1.5.sp`

**Por que é problemático**: React: `App.tsx:3520` — `tracking-wider` = `letter-spacing:
0.05em`. Em `text-xs` (12sp), 0.05em = 0.6sp. Android usa 1.5sp — **2.5x mais largo**.
Isso faz o título "REGISTRO DE ATIVIDADES" ficar mais espalhado que na fonte.

**Severidade**: MÉDIO

**Correção proposta**: Trocar para `0.05.em` (ou `0.6.sp`).

---

### Problema 12 — HeatmapScreen: tamanho da porcentagem de consistência

**Trecho/local**: `HeatmapScreen.kt:226` — `fontSize = 34.sp`

**Por que é problemático**: React: `HeatmapTab.tsx:217` — `text-3xl md:text-4xl` = 30px
mobile, 36px desktop. Android usa 34sp — acima do breakpoint mobile (30sp). Em telas
pequenas, o número pode ficar proporcionalmente maior que na fonte.

**Severidade**: MÉDIO

**Correção proposta**: Reduzir para 30.sp para telas mobile (fiel ao `text-3xl` da fonte).

---

### Problema 13 — HeatmapScreen: tamanho dos labels e valores dos stat cards

**Trecho/local**: `HeatmapScreen.kt:632` — label `fontSize = 8.5.sp`;
`HeatmapScreen.kt:642` — value `fontSize = 11.5.sp`

**Por que é problemático**: React: `HeatmapTab.tsx:467` — label `text-[8.5px]` = 8.5sp ✓ OK;
`HeatmapTab.tsx:468` — value `text-[11.5px]` = 11.5sp ✓ OK. **Correto**.

**Severidade**: N/A (verificado, sem bug)

---

### Problema 14 — HeatmapScreen: letterSpacing dos labels dos stat cards

**Trecho/local**: `HeatmapScreen.kt:633` — `letterSpacing = 0.06.em`

**Por que é problemático**: React: `HeatmapTab.tsx:467` — `tracking-widest` = `letter-spacing:
0.1em`. Android usa `0.06.em`. Diferença de 0.04em — moderadamente perceptível em
texto tiny (8.5sp).

**Severidade**: MÉDIO

**Correção proposta**: Ajustar para `0.10.em`.

---

### Problema 15 — HeatmapScreen: letterSpacing do título do painel de período

**Trecho/local**: `HeatmapScreen.kt:303` — `letterSpacing = 0.08.em`

**Por que é problemático**: React: `HeatmapTab.tsx:252` — `tracking-widest` = `letter-spacing:
0.1em`. Android usa `0.08.em`. Diferença de 0.02em.

**Severidade**: BAIXO

**Correção proposta**: Ajustar para `0.10.em`.

---

### Problema 16 — TitleShopScreen: letterSpacing do intro header

**Trecho/local**: `TitleShopScreen.kt:136` — `letterSpacing = 0.10.em`

**Por que é problemático**: React: `TitleShop.tsx:133` — `tracking-[0.14em]` = 0.14em. Android
usa 0.10em. Diferença de 0.04em — perceptível em uppercase tracking.

**Severidade**: MÉDIO

**Correção proposta**: Ajustar para `0.14.em`.

---

### Problema 17 — TitleSelectorScreen: letterSpacing do intro header

**Trecho/local**: `TitleSelectorScreen.kt:141` — `letterSpacing = 0.10.em`

**Por que é problemático**: React: `TitleSelector.tsx:23` — `tracking-[0.14em]` = 0.14em. Android
usa 0.10em. Mesmo gap do Problema 16.

**Severidade**: MÉDIO

**Correção proposta**: Ajustar para `0.14.em`.

---

### Problema 18 — ShopScreen: tamanho da descrição do item

**Trecho/local**: `ShopScreen.kt:274` — `fontSize = 11.sp, lineHeight = 15.sp`

**Por que é problemático**: React: `ShopTab.tsx:124` — `text-xs leading-relaxed` = 12sp,
line-height ~16-18sp (relaxed = 1.625). Android usa 11sp/15sp — levemente menor.

**Severidade**: BAIXO

**Correção proposta**: Ajustar para `fontSize = 12.sp, lineHeight = 18.sp` para fidelidade.

---

### Problema 19 — ShopScreen: tamanho do preço no botão

**Trecho/local**: `ShopScreen.kt:320` — `fontSize = 11.sp`

**Por que é problemático**: React: `ShopTab.tsx:137` — `text-xs` = 12sp. Android usa 11sp.
Diferença de 1sp — sutil mas perceptível lado a lado.

**Severidade**: BAIXO

**Correção proposta**: Ajustar para `12.sp`.

---

## Resumo por severidade

| Severidade | Contagem | Itens |
|---|---|---|
| CRÍTICO | 2 | #1 (FontFamily.Serif → Cinzel), #2 (FontFamily.Monospace → JetBrainsMono) |
| ALTO | 0 | — |
| MÉDIO | 7 | #5 (não-bug), #8 (desc sem serif), #9 (tracking stat label), #11 (tracking logs title), #12 (tamanho % heatmap), #14 (tracking heatmap stat label), #16-#17 (tracking intro headers) |
| BAIXO | 5 | #3 (LogsScreen import path), #7 (tracking achievement name), #10 (tamanho valor stat), #15 (tracking heatmap panel title), #18-#19 (tamanho desc/preço shop) |
| N/A (sem bug) | 2 | #5 (verificado OK), #13 (verificado OK) |

## Itens que NÃO foram verificados nesta auditoria

1. **Testes unitários**: NÃO executados (sem toolchain Android neste ambiente). Os bugs
   de tipografia são puramente visuais — não devem afetar testes de lógica.
2. **Screenshots/Roborazzi**: NÃO executados. Baselines podem precisar de atualização
   após correção dos bugs de fonte.
3. **Validação visual real**: Necessária em device/emulator para confirmar que a troca
   de `FontFamily.Serif` → `Cinzel` e `FontFamily.Monospace` → `JetBrainsMono` não
   causa overflow de texto ou quebra de layout (fontes métricas diferentes).
4. **Módulos fora do Reino**: Esta auditoria cobriu apenas `kingdom/*.kt`. Outros módulos
   (Foco, Herói, Skills, Missões) podem ter o mesmo bug de FontFamily.Serif/Monospace —
   escopo desta auditoria era limitado ao Reino.
