# Spec 001: FocusOrb 4 Conceitos Visuais + Seletor de Classe/Orb nos Ajustes

## Resumo
Portar os 4 conceitos visuais do `FocusOrb` (A: Chrono-Relic, B: Obsidian Core, C: Arcane Dial, D: Alchemist Flask) e o seletor de classe de personagem + seletor de conceito do orb na tela de **Ajustes Gerais**, com fidelidade 1:1 ao React (fonte de verdade).

---

## 1. Fonte de Verdade (React)

### 1.1 FocusOrb.tsx — 4 Conceitos
**Arquivo:** `src/modules/focus/FocusOrb.tsx` (628 linhas)

| Conceito | Nome | Descrição Visual | Arquivo React (linhas) |
|----------|------|------------------|------------------------|
| **A** | Chrono-Relic | Astrolábio de Obsidiana & Ouro Suíço — onda líquida + anel gradiente + 12 ticks horários + glows | 73–243 |
| **B** | Obsidian Core | Reator Rúnico & Cristal Negro — onda líquida + polígonos rúnicos (triângulos + círculo) + vignette radial + anel tracejado dourado | 248–388 |
| **C** | Arcane Dial | Minimalismo Alquímico — anel único gradiente + grade sutil (círculos concêntricos + cruz + diagonais) + label "Cronômetro"/"Recuperação" | 393–473 |
| **D** | Alchemist Flask | Frasco Alquimista — onda líquida + octógono decorativo + pulse no container + label "Ritmo de Foco" | 478–574 |

**Wrapper `FocusOrb` (linhas 579–628):**
- Recebe `orbConcept?: OrbConceptType` (default `'D'`)
- Calcula `mode: ModeType` = `'work' | 'dungeon' | 'wilderness' | 'break' | 'urgent' | 'paused'`
- Calcula `progress = timeLeft / totalSeconds` (clamp 0..1)
- Delega para `OrbConceptA/B/C/D` via `switch`

**Paleta por modo (cada conceito tem sua própria):**
- `work` (dourado/champagne), `dungeon` (roxo), `wilderness` (rosa/vermelho), `break` (esmeralda), `urgent` (vermelho), `paused` (zinco)

**Animações:**
- Onda: `phase` incrementa `0.05`/`0.06` a cada 33ms (~30fps) via `setInterval` — **para completamente quando pausado/parado** (não só amplitude zero)
- Anel rotativo (Conceito A/B): rotação contínua 45s (Conceito B) ou via CSS (Conceito A)
- Pulse (Conceito D): `animate-pulse` 4s no container

### 1.2 App.tsx — Ajustes Gerais (Seletor de Classe + Orb)
**Arquivo:** `src/App.tsx` (linhas 3872–4092)

**Estrutura do modal "Ajustes Gerais":**
1. **Perfil do Herói**
   - Nome do Herói (input, max 24 chars)
   - Nome do dispositivo (input, max 32 chars)
   - Duração do descanso longo (number input 1–120)
   - **Seletor de Classe** (grid 3 colunas):
     - **Mage** 🧙 — "+20% XP" (roxo)
     - **Warrior** ⚔️ — "+20% Ouro" (âmbar)
     - **Ranger** 🏹 — "+15% Streak" (esmeralda)
     - Selecionado: `bg-amber-500/8 border-amber-400 shadow scale-101` + barra topo gradiente âmbar
2. **Formato do Núcleo de Foco** (grid 2/4 colunas):
   - **A** ⏳ "Chrono-Relic" / "Relíquia Rúnica"
   - **B** 🔮 "Obsidian Core" / "Núcleo Cristalino"
   - **C** ⏱️ "Arcane Dial" / "Mostrador Arcano"
   - **D** 🧪 "Alchemist Flask" / "Frasco Alquimista"
   - Selecionado: `bg-champagne-500/8 border-champagne-400 shadow scale-101` + barra topo gradiente champagne

**Handlers:**
- `handleApplyCharacterSetupChanges(name, characterClass)` → `applyCharacterSetupChanges` (atualiza `charName` + `charClass` no gameState)
- `handleApplyOrbConceptChange(conceptId)` → `setGameState(prev => ({ ...prev, orbConcept: conceptId }))` + log

---

## 2. Estado Atual Android

### 2.1 FocusOrb.kt (app/src/main/java/.../ui/focus/FocusOrb.kt)
- **Implementa apenas Conceito D** (estilo "frasco alquimista" com onda líquida)
- **Não tem parâmetro `orbConcept`** — hardcoded para estilo D
- **Cores hardcoded** no `OrbColors` object (são as cores do modo `work`/`break`/`urgent`/`paused` do Conceito D)
- **Não suporta modos `dungeon`/`wilderness`** no seletor de cores (usa cores de `work` para ambos)
- Tamanhos: `COMPACT` (172dp), `STANDARD` (172dp/218dp wide), `FULLSCREEN` (253dp) — **não batem 1:1 com React** (React: compact=172px, standard=256px, fullscreen=253px→345px→437px responsivo)
- Animação de onda: `phase += 0.05f` a cada 33ms (OK, fiel)
- Anel rotativo tracejado: `Animatable` 45s (fiel ao Conceito B, mas Conceito A usa CSS transition)
- **Falta**: ticks horários (Conceito A), polígonos rúnicos (Conceito B), grade sutil (Conceito C), octógono (Conceito D), labels textuais específicos por conceito

### 2.2 CharacterModels.kt
- **Não tem campo `orbConcept`** em `CharacterState`
- `CharClass` enum existe (Mage, Warrior, Ranger) — **OK**
- `PomodoroSettings` existe — **OK**

### 2.3 HeroLogViewModel.kt
- **Não tem função** para alterar `orbConcept`
- **Não tem função** para alterar `charName` + `charClass` em conjunto (tem `changeFocusDuration`, `saveCustomTimerSettings`, etc. mas não profile)
- Settings button no `AppHeader` abre `RestoreSaveDialog` — **não abre modal de Ajustes Gerais**

### 2.4 MainActivity.kt
- `AppHeader.onOpenSettings = { isRestoreSaveOpen = true }` — **aponta para RestoreSaveDialog**
- Não existe estado para modal de Ajustes Gerais
- Não existe `SettingsModal` / `GeneralSettingsScreen` Composable

### 2.5 SaveMigrationLogic.kt
- Linha 52: `orbConcept` do `INITIAL_STATE` React é **ignorado na decodificação** (`ignoreUnknownKeys = true`) — divergência conhecida, registrada no PARIDADE.md

---

## 3. Paridade Necessária (O que Implementar)

### 3.1 Modelo de Dados
**CharacterModels.kt** — adicionar em `CharacterState`:
```kotlin
@Serializable
enum class OrbConcept {
    @SerialName("A") A,
    @SerialName("B") B,
    @SerialName("C") C,
    @SerialName("D") D
}

@Serializable
data class CharacterState(
    // ... campos existentes ...
    val orbConcept: OrbConcept = OrbConcept.D,  // default = D (fiel ao React)
    // ...
)
```

### 3.2 FocusOrb.kt — Refatoração Completa
**Estratégia:** Separar cada conceito em sua própria função Composable privada, igual ao React.

**Mudanças:**
1. Adicionar parâmetro `orbConcept: OrbConcept = OrbConcept.D`
2. Adicionar parâmetro `isDungeonMode: Boolean = false`, `isWildernessMode: Boolean = false` (hoje não existem)
3. Reutilizar as cores existentes no design system/paleta Android sempre que houver correspondência exata. Para cores específicas do React que não possuam equivalente Android, criar constantes nomeadas e centralizadas (não utilizar hexcodes diretamente nos Composables). Os valores hex/RGBA devem ser transcritos exatamente da fonte React, sem aproximação.
4. Implementar 4 funções:
   - `OrbConceptA(...)`
   - `OrbConceptB(...)`
   - `OrbConceptC(...)`
   - `OrbConceptD(...)`
5. Wrapper `FocusOrb` faz `when (orbConcept)` e delega
6. **Tamanhos**: ajustar para bater 1:1 com React:
   - `COMPACT` = 172dp
   - `STANDARD` = 256dp (fixo, não responsivo — React `w-64 h-64` = 256px)
   - `FULLSCREEN` = 253dp base, mas React tem breakpoints sm/lg — **decisão**: usar 253dp fixo (mobile-only) ou adicionar breakpoint 390dp/430dp? Ver handoff item 7.
7. **Modos de cor**: suportar `dungeon` e `wilderness` com paletas corretas por conceito
8. **Labels textuais** por conceito:
   - A: `"{progress}% Restante"` (serif, uppercase)
   - B: `"⚔️ Foco Puro" | "🗝️ Masmorra" | "💀 Selvagem" | "☕ Pausa"` (mono, uppercase)
   - C: `"Cronômetro" | "Recuperação"` (serif) + `"{progress}%"` com dot colorido
   - D: `"Ritmo de Foco"` (serif, uppercase)

### 3.3 HeroLogViewModel.kt — Novas Funções
```kotlin
fun updateCharacterProfile(name: String, charClass: CharClass) {
    val current = _characterState.value ?: return
    saveCharacterState(current.copy(charName = name, charClass = charClass))
}

fun updateOrbConcept(concept: OrbConcept) {
    val current = _characterState.value ?: return
    saveCharacterState(current.copy(orbConcept = concept))
    addSystemLog("🔮 Formato do Núcleo de Foco atualizado: Conceito ${concept.name}", true)
}
```

### 3.4 Settings Modal (Novo Arquivo)
**Arquivo sugerido:** `ui/components/GeneralSettingsModal.kt` ou `ui/settings/GeneralSettingsScreen.kt`

**Componentes:**
- `GeneralSettingsModal(isOpen, onDismiss, characterState, onUpdateProfile, onUpdateOrbConcept)`
- Seção **Perfil do Herói**: inputs + grid 3 classes
- Seção **Formato do Núcleo de Foco**: grid 2/4 conceitos
- Estilo visual: `bg-quest-panel border-2 border-amber-500/20 rounded-lg` + seções `rounded-xl border border-amber-500/15 bg-stone-950/40 p-4`
- Cards de seleção: `min-h-[96px] border rounded-lg` com estados `selected`/`unselected`/`hover` (hover N/A no touch, manter visual)

**Cores dos cards (transcritas do React):**
- Classe selecionada: `bg-amber-500/8 border-amber-400 shadow-[0_0_0_1px_rgba(245,158,11,0.15)] scale-1.01` + barra topo `bg-gradient-to-r from-amber-400/0 via-amber-300 to-amber-400/0`
- Classe não selecionada: `bg-stone-900/70 border-amber-500/10 opacity-70` + hover `opacity-100 border-amber-400/30 bg-stone-800/80`
- Orb selecionado: `bg-champagne-500/8 border-champagne-400 shadow-[0_0_0_1px_rgba(229,193,88,0.15)] scale-1.01` + barra topo champagne
- Orb não selecionado: `bg-stone-900/70 border-white/10 opacity-70` + hover `opacity-100 border-champagne-400/30 bg-stone-800/80`

### 3.5 MainActivity.kt — Integração
1. Adicionar estado `var isGeneralSettingsOpen by remember { mutableStateOf(false) }`
2. `AppHeader.onOpenSettings = { isGeneralSettingsOpen = true }`
3. Renderizar `GeneralSettingsModal(...)` no `Box` do `Scaffold`
4. Passar callbacks para `HeroLogViewModel.updateCharacterProfile` e `updateOrbConcept`
5. Passar `characterState.orbConcept` e `characterState.charClass` para o modal

### 3.6 FocusModeScreen / FocusOrbPreviewScreen — Passar orbConcept
- `FocusModeScreen` recebe `orbConcept` e passa para `FocusOrb`
- `FocusOrbPreviewScreen` lê `characterState.orbConcept` e passa para `FocusOrb`

---

## 4. Detalhes de Implementação por Conceito

### 4.1 Conceito A: Chrono-Relic
**Elementos visuais (React lines 73–243):**
- Glow externo: `blur-2xl` com `colorMap.glow`
- SVG viewBox 0-100
- Defs: `clipPath` (circle r=39.5), 3 linearGradients (`front`, `back`, `gauge`)
- Círculos de fundo: r=48.5 (fill #0c0c10 stroke #27272a), r=46 (stroke #18181b)
- **12 ticks horários**: a cada 30°, major (i%3==0) mais longos/grossos/opacos
- Anel de progresso: stroke `url(#gauge)`, strokeDasharray=circunferência, dashoffset animado, strokeLinecap=round, rotate(-90)
- Círculo interno r=40 (fill #09090b stroke #3f3f46)
- **ClipPath com ondas**: `backPath` (phase invertida, amplitude 0.7x) + `frontPath` (phase normal) + linha divisória + 3 bolhas decorativas
- Highlight superior: path elíptico `M 20,30 A 32,32 0 0,1 80,30 A 35,35 0 0,0 20,30 Z` fill white opacity 0.12
- Texto central: tempo (mono, black, cor `accent`) + label `% Restante` (serif, uppercase, zinc-400)

**Cores por modo (extraídas do React):**
```kotlin
// work
accent = #e5c158
ringGrad = [#d4af37, #e5c158, #fef08a]
frontGrad = [#fef08a, #e5c158, #855d14]
backGrad = [#a1781b, #3b2505]
glow = rgba(229,193,88,0.25)
tickColor = #e5c158

// dungeon
accent = #c084fc
ringGrad = [#9333ea, #c084fc, #e9d5ff]
frontGrad = [#e9d5ff, #a855f7, #581c87]
backGrad = [#6b21a8, #2e1065]
glow = rgba(192,132,252,0.25)
tickColor = #c084fc

// wilderness
accent = #fb7185
ringGrad = [#e11d48, #fb7185, #ffe4e6]
frontGrad = [#ffe4e6, #f43f5e, #881337]
backGrad = [#be123c, #4c0519]
glow = rgba(251,113,133,0.25)
tickColor = #fb7185

// break
accent = #10b981
ringGrad = [#059669, #10b981, #a7f3d0]
frontGrad = [#a7f3d0, #10b981, #064e3b]
backGrad = [#047857, #022c22]
glow = rgba(16,185,129,0.25)
tickColor = #10b981

// urgent
accent = #f43f5e
ringGrad = [#dc2626, #f43f5e, #fecdd3]
frontGrad = [#fecdd3, #ef4444, #7f1d1d]
backGrad = [#b91c1c, #450a0a]
glow = rgba(244,63,94,0.4)
tickColor = #f43f5e

// paused
accent = #a1a1aa
ringGrad = [#71717a, #a1a1aa, #d4d4d8]
frontGrad = [#d4d4d8, #71717a, #27272a]
backGrad = [#52525b, #18181b]
glow = rgba(161,161,170,0.1)
tickColor = #71717a
```

### 4.2 Conceito B: Obsidian Core
**Elementos visuais (React lines 248–388):**
- Glow interno `inset-2 blur-2xl`
- SVG viewBox 0-100
- Defs: `clipPath` (circle r=43), `linearGradient front/back`, `radialGradient vignette`
- Círculo base r=45 (fill #09090b stroke #1f1f23)
- **Runas decorativas** (opacity 0.08, stroke `rune` color): 2 triângulos invertidos + círculo r=18
- ClipPath com ondas (back 0.75x amplitude, phase invertida)
- Linha divisória + 3 bolhas `animate-pulse`
- Vignette radial (cx=50% cy=50% r=50%, fx=35% fy=35%): stops 0% white/0.08, 70% black/0.4, 100% black/0.85
- Anel tracejado dourado r=44.5 (stroke #e5c158 width 0.75 opacity 0.3) + anel escuro r=45.5
- Highlight superior: path elíptico maior `M 16,36 A 38,38 0 0,1 84,36 A 42,42 0 0,0 16,36 Z` white/0.22
- Bolha canto inferior direito r=2.5 white/0.08
- Texto central: tempo com `:` pulsante (split MM:SS, dois spans) + label modo (mono, uppercase, tracking-widest)

**Cores por modo** (extraídas do React `colors` object):
- `work`: accent #fef08a, liquidFront [#fef08a, #e5c158, #713f12], liquidBack [#b48c26, #422006], glow rgba(229,193,88,0.2), rune #e5c158
- `dungeon`: accent #e9d5ff, liquidFront [#e9d5ff, #a855f7, #3b0764], liquidBack [#7e22ce, #1e0538], glow rgba(168,85,247,0.2), rune #a855f7
- `wilderness`: accent #ffe4e6, liquidFront [#ffe4e6, #f43f5e, #4c0519], liquidBack [#be123c, #20020a], glow rgba(244,63,94,0.2), rune #f43f5e
- `break`: accent #a7f3d0, liquidFront [#a7f3d0, #10b981, #022c22], liquidBack [#059669, #011711], glow rgba(16,185,129,0.2), rune #10b981
- `urgent`: accent #fecdd3, liquidFront [#fecdd3, #ef4444, #450a0a], liquidBack [#b91c1c, #200404], glow rgba(239,68,68,0.35), rune #ef4444
- `paused`: accent #e4e4e7, liquidFront [#d4d4d8, #71717a, #18181b], liquidBack [#52525b, #09090b], glow rgba(113,113,122,0.1), rune #71717a

### 4.3 Conceito C: Arcane Dial
**Elementos visuais (React lines 393–473):**
- Glow interno `inset-4 blur-2xl`
- SVG viewBox 0-100
- Defs: `linearGradient track-grad` (0% white/0.8, 40% track color, 100% track color/0.6)
- Círculos base: r=48 (fill #111116 stroke #27272a), r=46.5 (fill #09090b)
- **Grade sutil** (stroke white/0.06): círculos r=32 (dash 1,3), r=20, cruz + diagonais
- Track background: circle r=43 stroke `trackBg` width 3
- Progress ring: circle r=43 stroke `url(#track-grad)` width 3.2, dashoffset animado, linecap round, rotate(-90)
- Círculo interno r=36 (fill #0c0c10 stroke #1f1f23)
- Texto central: label "Cronômetro"/"Recuperação" (serif, uppercase, zinc-500) + tempo (mono, black, cor `text`) + row: dot colorido (w-1.5 h-1.5) + `%` (mono, zinc-400)

**Cores por modo** (extraídas do React `colors` object):
- `work`: track #e5c158, trackBg #27272a, bgGlow rgba(229,193,88,0.15), text #fef08a
- `dungeon`: track #c084fc, trackBg #2e1065, bgGlow rgba(192,132,252,0.15), text #e9d5ff
- `wilderness`: track #fb7185, trackBg #4c0519, bgGlow rgba(251,113,133,0.15), text #ffe4e6
- `break`: track #10b981, trackBg #022c22, bgGlow rgba(16,185,129,0.15), text #a7f3d0
- `urgent`: track #f43f5e, trackBg #450a0a, bgGlow rgba(244,63,94,0.3), text #fecdd3
- `paused`: track #71717a, trackBg #18181b, bgGlow rgba(113,113,122,0.05), text #d4d4d8

### 4.4 Conceito D: Alchemist Flask (Atual Android — expandir)
**Elementos visuais (React lines 478–574):**
- Container com `animate-pulse` 4s quando running
- Glow `inset-2 blur-2xl`
- SVG viewBox 0-100
- Defs: `clipPath` (circle r=43.5), `linearGradient front/back`
- Círculo base r=45 (fill #09090b stroke #3f3f46)
- **Octógono decorativo**: `points="50,6 81,19 94,50 81,81 50,94 19,81 6,50 19,19"` stroke #e5c158 width 0.5 opacity 0.15
- ClipPath com ondas (back 0.7x amplitude)
- Linha divisória + 4 bolhas decorativas
- Anel branco r=44.5 stroke width 1.2 opacity 0.2
- Highlight superior: path `M 18,34 A 36,36 0 0,1 82,34 A 40,40 0 0,0 18,34 Z` white/0.2
- Texto central: tempo (mono, black, cor `text`) + label "Ritmo de Foco" (serif, uppercase, zinc-400)

**Cores por modo** (extraídas do React `colors` object):
- `work`: text #fef08a, liquid [#fef08a, #e5c158, #854d0e], back [#a16207, #451a03], glow rgba(229,193,88,0.25)
- `dungeon`: text #e9d5ff, liquid [#e9d5ff, #a855f7, #581c87], back [#7e22ce, #3b0764], glow rgba(168,85,247,0.25)
- `wilderness`: text #ffe4e6, liquid [#ffe4e6, #f43f5e, #881337], back [#be123c, #4c0519], glow rgba(244,63,94,0.25)
- `break`: text #a7f3d0, liquid [#a7f3d0, #10b981, #064e3b], back [#059669, #022c22], glow rgba(16,185,129,0.25)
- `urgent`: text #fecdd3, liquid [#fecdd3, #ef4444, #7f1d1d], back [#b91c1c, #450a0a], glow rgba(239,68,68,0.35)
- `paused`: text #d4d4d8, liquid [#d4d4d8, #71717a, #27272a], back [#52525b, #18181b], glow rgba(113,113,122,0.1)

---

## 5. Arquivos a Criar/Alterar

| Arquivo | Ação | Descrição |
|---------|------|-----------|
| `model/CharacterModels.kt` | **Alterar** | Adicionar `OrbConcept` enum + campo `orbConcept` em `CharacterState` |
| `ui/focus/FocusOrb.kt` | **Reescrever** | Implementar 4 conceitos + wrapper com switch, paletas completas por modo |
| `ui/HeroLogViewModel.kt` | **Alterar** | Adicionar `updateCharacterProfile` + `updateOrbConcept` |
| `ui/components/GeneralSettingsModal.kt` | **Criar** | Modal completo de Ajustes Gerais (perfil + orb concept) |
| `MainActivity.kt` | **Alterar** | Trocar `onOpenSettings` para abrir `GeneralSettingsModal`, integrar estado |
| `ui/focus/FocusModeScreen.kt` | **Alterar** | Aceitar e repassar `orbConcept` para `FocusOrb` |
| `ui/focus/FocusOrbPreviewScreen.kt` (em MainActivity) | **Alterar** | Ler `characterState.orbConcept` e passar para `FocusOrb` |

---

## 6. Testes Obrigatórios

### 6.1 Unitários (lógica pura)
- `FocusOrbConceptTest`: verificar que cada conceito renderiza sem crash para todos os 6 modos × 3 tamanhos
- `HeroLogViewModelTest`: testar `updateOrbConcept` persiste e emite log; `updateCharacterProfile` atualiza nome e classe

### 6.2 Screenshot Testing
- **Screenshot testing**: utilizar Roborazzi se já estiver configurado no projeto. Caso contrário, **não introduzir nova infraestrutura de screenshot testing** neste bloco sem aprovação prévia; realizar a validação visual pelo mecanismo de screenshot já disponível no projeto.
- `FocusOrbScreenshotTest`: gerar PNGs para cada conceito × modo × tamanho (4 × 6 × 3 = 72 screenshots) — **apenas se Roborazzi já configurado**
- `GeneralSettingsModalScreenshotTest`: modal aberto com cada classe/orb selecionado — **apenas se Roborazzi já configurado**

### 6.3 Validação Visual Manual
- Comparar lado a lado React (375×667 e 390×844) vs Android em cada conceito
- Verificar: cores exatas, gradientes, animações (onda, rotação, pulse), labels, espaçamento

---

## 7. Riscos e Decisões Conscientes

| Risco | Decisão |
|-------|---------|
| Fontes Cinzel/JetBrains Mono | As fontes **Cinzel** e **JetBrains Mono** já estão disponíveis em `res/font/`. Utilizar as fontes reais existentes no projeto, reproduzindo a tipografia do React. **Não utilizar** `FontFamily.Serif` ou `FontFamily.Monospace` como substitutos quando houver a fonte correspondente em `res/font/`. |
| Tamanho `STANDARD` no React é 256px fixo (`w-64 h-64`), Android usa 172dp/218dp | **Ajustar para 256dp fixo** — fidelidade visual > responsividade mobile-only |
| `FULLSCREEN` no React tem breakpoints sm/lg | **Usar 253dp fixo** (valor base do React) — mobile-only, breakpoint não alcançável |
| Animação de onda para quando pausado (React `setInterval` limpo) | **Manter `LaunchedEffect` com `while(isActive)`** — já implementado corretamente no Android atual |
| `orbConcept` não existia no model Android | **Adicionar com default `D`** — migração automática via `ignoreUnknownKeys` + default no data class |
| Settings modal substitui entry point do `RestoreSaveDialog` | **Manter `RestoreSaveDialog` acessível** — adicionar botão "Restaurar Salvamento" dentro do modal de Ajustes (seção Backup) |

---

## 8. Atualização PARIDADE.md (Sugestão)

Após conclusão, atualizar seção "Focus / Foco":

| Módulo | Status | Fidelidade Lógica | Fidelidade Visual | Notas |
|--------|--------|-------------------|-------------------|-------|
| FocusOrb (4 conceitos) | **Validado** | Validada — testes unitários + build | Validada — prints lado a lado 375×667 / 390×844 | 4 conceitos portados 1:1 do `FocusOrb.tsx` |
| Ajustes Gerais (Classe + Orb) | **Validado** | Validada — `updateCharacterProfile`/`updateOrbConcept` testados | Validada — modal fiel ao React App.tsx linhas 3872–4092 | Entry point via `AppHeader` → `GeneralSettingsModal` |

---

## 9. Checklist de Entrega (Definition of Done)

- [ ] `CharacterModels.kt` com `OrbConcept` enum e campo em `CharacterState`
- [ ] `FocusOrb.kt` reescrito com 4 conceitos, paletas completas, todos os 6 modos
- [ ] `HeroLogViewModel.kt` com `updateCharacterProfile` e `updateOrbConcept`
- [ ] `GeneralSettingsModal.kt` criado com UI fiel ao React (duas seções, grids, estados selecionado/hover)
- [ ] `MainActivity.kt` integrado: abre modal de Ajustes, passa callbacks, repassa `orbConcept` para `FocusOrb`
- [ ] `FocusModeScreen.kt` e `FocusOrbPreviewScreen` recebem e usam `orbConcept`
- [ ] Build `./gradlew assembleDebug` **sucesso**
- [ ] Testes unitários `./gradlew testDebugUnitTest` **todos passam** (XML bruto conferido)
- [ ] Screenshots Roborazzi geradas para todos os conceitos × modos
- [ ] Validação visual manual: prints lado a lado React vs Android (2 viewports)
- [ ] `PARIDADE.md` atualizado com novas linhas validadas
- [ ] `DEV_LOG_ANDROID.md` registrado com bloco, arquivos, validação, desvios

---

## 10. Referências de Código React (para transcrição exata)

- **FocusOrb.tsx completo**: `/home/bruno/projetos/HeroLog-Ref/src/modules/focus/FocusOrb.tsx`
- **App.tsx Ajustes Gerais**: `/home/bruno/projetos/HeroLog-Ref/src/App.tsx` linhas 3872–4092
- **Types**: `OrbConceptType`, `ModeType`, `CommonOrbProps`, `FocusOrbProps` (FocusOrb.tsx linhas 6–30)
- **Cores**: objetos `colorMap` (Conceito A), `colors` (B, C, D) — **transcrever campo a campo, não aproximar**
- **generateWave**: função lines 59–68 — **replicar matemática exata** (12 pontos, 1.2 ciclos, fechamento M -10,110 ... L 110,110 Z)

---

**Próximo passo:** Aprovação do spec → Implementação em bloco único (escopo auditável) → Validação → Registro no DEV_LOG_ANDROID.md + PARIDADE.md