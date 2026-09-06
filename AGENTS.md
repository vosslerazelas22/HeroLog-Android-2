# AGENTS.md — Guia para agentes de código trabalhando no HeroLog Android

## 1. Objetivo do projeto

Este é o **port Android/Kotlin do HeroLog**, um app web React de produtividade com mecânicas de
RPG. O objetivo inegociável é **fidelidade de lógica de negócio e fidelidade visual** entre o app
React (fonte de verdade) e o app Android nativo (Kotlin/Compose). Os dois apps rodam em paralelo;
o port é incremental.

O app Android não é um produto independente — ele é uma cópia fiel do React, feita para rodar
como app nativo. Qualquer divergência não documentada é um bug.

## 2. Relação React → Android

- **Fonte de verdade**: o código real do app React (`main branch`). Sempre que o Android
  divergir, o React é quem está certo, a menos que a divergência seja uma **decisão consciente
  registrada no `PARIDADE.md`** (ex: sync com Supabase fora de escopo por ora).
- **Distinção entre AI Studio e código React**: o AI Studio é o ambiente de desenvolvimento
  onde o código Android é escrito e testado. O código dentro do AI Studio pode divergir do repo
  React real — o AI Studio não tem acesso direto ao repositório React. Quando houver dúvida
  sobre comportamento, sempre buscar o trecho **real** do código React (fornecido por quem tem
  acesso ao repo), não supor a partir do que está no ambiente de desenvolvimento.
- Muitas decisões de divergência consciente já foram tomadas e documentadas (ex: `lastDungeonClearedTime`
  é persistido no Android mas não no React; `totalXP` é incrementado no Android mas não no React).
  **Consulte o `PARIDADE.md` antes de "corrigir" qualquer divergência** — ela pode ser intencional.

## 3. Fonte de verdade e regras de paridade

O arquivo **`PARIDADE.md`** é o mapa único do estado de paridade. Cada linha da tabela indica:
- Se o módulo foi iniciado, está em andamento, bloqueado ou validado
- Se a fidelidade lógica foi validada (exige teste unitário ou validação manual documentada)
- Se a fidelidade visual foi validada (exige print real comparando lado a lado com o React nos
  dois viewports de referência: 375×667 e 390×844)

**Regras para atualizar o PARIDADE.md:**
- Atualizar a linha correspondente **no mesmo bloco em que o módulo/item for trabalhado**,
  nunca retroativamente por memória
- "Fidelidade Lógica: Validada" só com teste unitário cobrindo o comportamento (ou validação
  manual explícita documentada no `DEV_LOG_ANDROID.md`)
- "Fidelidade Visual: Validada" só com print real comparando lado a lado com o React

## 4. Arquitetura atual

### Camadas

```
MainActivity (Compose, single Activity)
  └── HeroLogViewModel (único ViewModel)
        ├── CharacterRepository → Room (blob JSON)
        ├── FocusSessionRepository → Room (blob JSON)
        └── Logic functions (puros, sem side-effects)
```

### Persistência

- **Room com blob JSON** (Opção A): `CharacterState` e `ActiveFocusSession` são serializados
  como JSON string em tabelas de linha única (`CharacterStateEntity`, `ActiveFocusSessionEntity`)
- Serialização via `kotlinx.serialization` (`@Serializable` em todos os data classes)
- `HeroLogDatabase` (Room, versão 2, 2 entidades, migration 1→2 documentada)
- **A partir de agora, qualquer bump futuro de versão do banco exige `Migration` explícita** —
  sem ela, o app crasha em dev em vez de resetar dados silenciosamente

### Lógica

- Lógica de jogo vive predominantemente em `logic/` como funções puras (ou `object` singletons),
  organizada por domínio: `logic/quests/`, `logic/focus/`, `logic/achievements/`,
  `logic/kingdom/`, `logic/character/`
- O padrão predominante é: funções recebem estado e retornam novo estado, sem efeitos
  colaterais. **Não "corrigir" uma função existente para encaixar nesse padrão sem antes
  confirmar que se trata de um bug real** — algumas funções têm side-effects por design
- O ViewModel orquestra: chama funções de lógica, depois persiste com `saveCharacterState()`
- `saveCharacterState()` é o funil único de mutação, responsável pela detecção de level-up

### Navegação

- `activeTab: String` controla a aba ativa (5 módulos: Foco, Herói, Skills, Missões, Reino)
- Módulos com sub-abas (Herói, Missões, Reino) abrem `ModalBottomSheet`
- Sub-abas: `SUB_TABS` definidos em `HeroLogBottomNav.kt` (`Map<String, List<SubTabOption>>`)

### Dependências

- Sem framework de DI (Hilt/Dagger/Koin) — `HeroLogApplication` atua como container manual
- `HeroLogViewModelFactory` cria o ViewModel passando repositories
- Ícones: `com.composables:icons-lucide-android:2.2.1` (não Material Icons — decisão explícita)

### Estrutura de pacotes

```
com.iurispraecepta.herolog/
├── MainActivity.kt
├── HeroLogApplication.kt
├── model/            (CharacterModels.kt — todos os tipos @Serializable)
├── data/             (entity/, dao/, database/, repository/, TitleCatalog.kt)
├── logic/
│   ├── SkillLogic.kt, InventoryLogic.kt, TitleLogic.kt, CombatLogic.kt,
│   │   SkillCarouselLogic.kt, CharacterMappers.kt, SaveMigrationLogic.kt
│   ├── quests/       (HabitLogic, DailyLogic, TodoLogic, QuestLogic, RolloverLogic, etc.)
│   ├── focus/        (FocusRewardsLogic, FocusApplyLogic, CognitiveDeathLogic, etc.)
│   ├── achievements/ (AchievementCatalog, AchievementLogic)
│   ├── character/    (LevelUpLogic)
│   └── kingdom/      (ShopLogic, HeatmapLogic)
└── ui/               (HeroLogViewModel.kt, HeroLogViewModelFactory.kt)
    ├── theme/        (Color.kt, Type.kt, Theme.kt, ScoreColor.kt)
    ├── navigation/   (HeroLogBottomNav.kt)
    ├── components/   (HeroLogModal, ItemInspectModal, AppHeader, etc.)
    ├── focus/        (FocusModeScreen, FocusOrb, QuickActionsBar, RaidModeSection, etc.)
    ├── character/    (CharacterScreen, LevelUpOverlay, TitleEquipModal)
    ├── skills/       (SkillsScreen, SkillSelectorModal)
    ├── habits/       (HabitsScreen)
    ├── dailies/      (DailiesScreen)
    ├── todos/        (TodosScreen)
    ├── inventory/    (InventoryScreen)
    ├── quests/       (QuestsScreen)
    ├── history/      (HistoryScreen)
    └── kingdom/      (ShopScreen, TitleShopScreen, HeatmapScreen, StatsScreen, etc.)
```

### Estado atual conhecido

- **Toolchain Android completa disponível no WSL**: JDK 17 (`~/jdk-17`, compilação),
  JDK 21 (`~/jdk-21`, testes Robolectric), Gradle 9.3.1 com wrapper, Android SDK 36
  (`~/Android/Sdk`), Roborazzi 1.59.0. Build (`./gradlew assembleDebug`) e testes
  (`./gradlew testDebugUnitTest`) rodam diretamente nesta sessão. Validação visual
  (device/emulador) continua sendo responsabilidade do Android Studio.
- Fontes Cinzel/JetBrains Mono definidas em `Type.kt` com arquivos `.ttf` em `res/font/`
  (`cinzel_regular/semibold/bold/extrabold`, `jetbrains_mono_regular/semibold`).
  **Porém, muitos componentes ainda usam `FontFamily.Serif`/`.Monospace` do sistema** em vez
  de `Cinzel`/`JetBrainsMono` — ver `specs/002b-kingdom-visual-audit-addendum.md` para a
  lista completa de ocorrências. Pendência de wiring, não de existência de fontes.

## 5. Regras para alterações de código

### Fluxo de trabalho por bloco

Cada alteração é feita em "Blocos" (Bloco 0, Bloco 0.1, Bloco 1, ...). Cada bloco:
1. Tem escopo pequeno e auditável
2. É implementado, testado e validado numa sessão
3. É registrado no `DEV_LOG_ANDROID.md` imediatamente após fechamento

### Regras obrigatórias

1. **Nunca aceitar resumo em prosa como prova de teste** — sempre exigir XML bruto ou saída
   nominal por método. O ambiente de desenvolvimento já fabricou contagens e nomes de teste
   falsos múltiplas vezes (incidentes do Bloco 8, Bloco 14, Bloco 26 do `DEV_LOG_ANDROID.md`).

2. **Nunca aceitar "o arquivo não existe" ou "não há tela de config" sem grep real** — já
   ocorreu de um entry point estar escondido em componente existente (AppHeader.kt tinha o
   RestoreSaveDialog como dependência não documentada).

3. **Para catálogos/dados literais extensos** (títulos, quests, conquistas): sempre transcrito
   do código-fonte real, nunca inventado. Verificar campo a campo contra a fonte após escrita.
   O ambiente de desenvolvimento já demonstrou alta taxa de fabricação de conteúdo quando
   solicitado a transcrever catálogos extensos (incidente do Bloco 8 do `DEV_LOG_ANDROID.md`).

4. **Nunca presumir que dois sistemas usam a mesma lógica por analogia** — cada um precisa
   da própria verificação literal da fonte React (lição do Bloco B: Foco ≠ Habit ≠ Daily ≠ Todo).

5. **Importar explicitamente os tipos corretos** — o achado crítico do Bloco C (20/08) mostrou
   que `HabitLogic.kt`, `DailyLogic.kt` e `TodoLogic.kt` estavam operando sobre tipos mortos
   (`logic.quests.*`) por resolução de pacote silenciosa, não sobre os tipos reais (`model.*`).

6. **Antes de alterar qualquer arquivo**, ler o `PARIDADE.md` correspondente ao módulo para
   entender o estado atual, decisões conscientes e riscos registrados.

7. **Ao fechar um bloco, executar build e os testes apropriados**. Formato de validação:
   ```
   ./gradlew assembleDebug
   ./gradlew testDebugUnitTest
   ```
   Confirmar resultado nominal (não só "BUILD SUCCESSFUL"). Verificar XML bruto em
   `app/build/test-results/testDebugUnitTest/` para resultado nominal por método.
   Preferencialmente executar a suíte completa. Se a suíte completa não puder ser executada
   por limitações conhecidas (ex: memória insuficiente), executar subconjuntos relevantes
   por classe e registrar explicitamente como **validação parcial** — nunca declarar
   "validado" quando a validação foi apenas parcial.
   **Visual**: a validação de UI continua pendente até inspeção em device/emulador real.

8. **Nunca alterar o schema do Room** sem criar `Migration` explícita. Ver `HeroLogDatabase.kt`
   e os schemas versionados em `app/schemas/`.

## 6. Testes obrigatórios

### Tipos de teste

- **Testes unitários** (lógica pura): cobrem comportamento de funções em `logic/`. Exigidos
  para marcar "Fidelidade Lógica: Validada" no `PARIDADE.md`.
- **Testes de integração** (ViewModel + Room em memória via Robolectric): testam wiring real
  de persistência. Usam `Room.inMemoryDatabaseBuilder` + `ApplicationProvider.getApplicationContext`.
- **Testes de screenshot** (Roborazzi): gravam PNGs baseline em `app/src/test/screenshots/`.
  **NÃO substituem inspeção humana** — são apenas baseline para detecção de mudanças.

### Como validar testes

- Rodar `./gradlew testDebugUnitTest` e verificar XML bruto em
  `app/build/test-results/testDebugUnitTest/`
- Cada entrada de bloco no `DEV_LOG_ANDROID.md` deve listar os testes com resultado nominal
  (`PASSED`/`FAILED` por nome de método)
- Se a suíte completa não rodar (ambiente com memória limitada), rodar subconjuntos por
  classe e reportar como "parcialmente confirmado", não como "validado"

### Padrão de teste para lógica de negócio

```kotlin
@Test
fun function_scenario_expectedResult() {
    // Arrange: estado inicial
    // Act: chamar função de lógica
    // Assert: resultado esperado (campo a campo, não igualdade de objeto)
}
```

Nenhum teste de `SkillLogicTest` compara objetos `Skill` inteiros — todos comparam campo a
campo. Seguir esse padrão para todos os tipos.

## 7. Cuidados com persistência e migrações

1. **Blob JSON é o padrão**: `CharacterState` inteiro é serializado como uma string JSON
   numa tabela de linha única. Campos novos adicionados ao data class entram automaticamente
   na serialização (com default) — **sem necessidade de migration de schema** para campos novos.

2. **Migration de schema** só é necessária quando uma nova entidade é criada ou uma coluna
   existente é alterada/removida. Exemplo: `MIGRATION_1_2` criou `active_focus_session`.

3. **Schemas versionados** ficam em `app/schemas/`. `exportSchema = true` e
   `room.schemaLocation` configurados no `build.gradle.kts`.

4. **`fallbackToDestructiveMigration()` foi removido** (Bloco 29/08). A partir de agora,
   sem migration explícita o app crasha em dev — é intencional (evita reset silencioso de dados).

5. **`FocusRewardsLogic.calculate` usa `Random.Default`** (não determinístico). Por isso o
   resultado é persistido já pronto (`pendingCalculation`), nunca recalculado numa recuperação
   futura — evita reroll de loot fechando/reabrindo o app.

6. **Atenção ao `@SerialName`** em todos os enums: garante paridade byte a byte do JSON
   serializado entre React e Android. Nunca remover ou alterar sem verificar impacto na
   compatibilidade.

7. **Estado inicial com listas vazias**: `createInitialCharacterState()` inicializa `skills`,
   `habits`, `dailies` e `todos` como listas vazias de propósito — o personagem começa sem
   nenhum item. Não adicionar dados de exemplo ou dados pessoais ao estado inicial.

8. **Três formatos de data coexistem** no `CharacterState` do React, cada um com uso específico:
   - `toDateString()` (inglês, ex: `"Tue Aug 04 2026"`) — usado no seed do LCG e em `lastStudyDate`
   - `toLocaleDateString('pt-BR')` (ex: `"04/08/2026"`) — usado em `todayLocalStr()`/filtro de quests
   - `toLocaleString('pt-BR')` (ex: `"04/08/2026, 14:23:10"`) — usado em `HistoryEntry.date`

   Não tratar como equivalentes ao portar módulos que lidam com datas.

## 8. Documentação que deve ser consultada

| Documento | Quando consultar | O que contém |
|---|---|---|
| `PARIDADE.md` | **Antes de cada bloco** de trabalho | Estado de paridade de cada módulo, decisões conscientes, riscos registrados, status de fidelidade lógica/visual |
| `DEV_LOG_ANDROID.md` | **Durante e após** cada bloco | Log técnico granular de cada bloco: arquivos criados/alterados, resumo, validação (build/testes/visual), desvios aprovados |
| Código-fonte React real | Quando a lógica não está clara | O React é a fonte de verdade. Sempre que houver dúvida sobre comportamento, obter o trecho real (não supor) |
| `app/schemas/` | Ao alterar entidades Room | Schemas versionados para validação de migration |

**Não existe `README.md` no projeto.**

## 9. Armadilhas conhecidas

As seguintes armadilhas foram confirmadas durante o desenvolvimento e devem ser evitadas:

1. **Resumo em prosa não é prova de teste.** O ambiente de desenvolvimento já gerou relatórios
   com contagens e nomes de teste fabricados que não batiam com o XML bruto real. Sempre exigir
   a saída nominal por método (XML ou `--info`).

2. **A suíte completa pode travar em ambientes com pouca memória.** Já houve travamentos
   consistentes ao executar todas as ~34 classes de teste de uma vez (`-Xmx1536m` não
   foi suficiente). Quando isso ocorrer, rodar subconjuntos filtrados por classe e registrar
   como validação parcial.

3. **Minimizar a janela do AI Studio não equivale necessariamente a um `ON_STOP` real.** O
   preview do AI Studio roda num iframe/WebView dentro do navegador — minimizar a janela do
   navegador não dispara o ciclo de vida `ON_STOP`/`ON_PAUSE` da Activity Android. A
   validação de fluxos que dependem de lifecycle (ex: Morte Cognitiva) requer um APK de debug
   instalado num device/emulator real.

4. **"O arquivo não existe" deve ser verificado com grep, não aceito de imediato.** Já ocorreu
   de um componente ter dependências escondidas em outros arquivos que não foram consultados
   (ex: entry point do RestoreSaveDialog vivia em AppHeader.kt, não em arquivo dedicado).

5. **Nunca presumir que dois sistemas de jogo usam a mesma lógica.** Cada módulo (Foco, Habit,
   Daily, Todo) tem sua própria fórmula e regras — verificar sempre o trecho literal da fonte
   React correspondente, não supor por analogia com outro módulo já portado.

6. **Imports silenciosos podem ligar a tipos errados.** Verificar sempre se os tipos importados
   são de `model.*` (os reais) e não de algum pacote paralelo/morto (ex: `logic.quests.*`
   continha uma cópia duplicada dos tipos que nunca foi usada pelo estado real).

## 10. Comportamento esperado antes de implementar uma mudança

1. **Ler o `PARIDADE.md`** na seção correspondente ao módulo que será alterado. Verificar:
   - Status atual (`Não iniciado` / `Em andamento` / `Bloqueado` / `Validado`)
   - Notas de decisões conscientes e riscos registrados
   - Se há pendências explícitas que bloqueiam ou dependem do bloco atual

2. **Ler as últimas entradas do `DEV_LOG_ANDROID.md`** para entender o estado mais recente
   do código e quais pendências ficaram em aberto.

3. **Obter o trecho real da fonte React** correspondente ao que será portado. Não supor
   comportamento por analogia com outros módulos.

4. **Confirmar imports dos tipos corretos** — verificar se os tipos usados são de `model.*`
   (os reais) e não de algum pacote paralelo/morto.

5. **Implementar com escopo mínimo** — bloco pequeno e auditável. Separe lógica pura de UI
   sempre que possível.

6. **Validar**: build + testes unitários (resultado nominal) +, quando houver UI, inspeção
   visual (print real ou Roborazzi).

7. **Registrar no `DEV_LOG_ANDROID.md`** imediatamente após fechamento, seguindo o formato
   padrão (arquivos, resumo, validação, desvios aprovados).

8. **Atualizar o `PARIDADE.md`** se o bloco mudou o estado de paridade de algum módulo.
