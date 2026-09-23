# AGENTS.md — Guia para agentes de código no HeroLog Android

> Este arquivo é um **guia de regras e arquitetura**, não um log. Histórico de incidentes,
> datas de blocos e detalhes de bugs resolvidos vivem no `DEV_LOG_ANDROID.md`.
> Estado de paridade por módulo vive no `PARIDADE.md`.

## 1. Objetivo

Port Android/Kotlin (Compose) do HeroLog, app web React de produtividade com mecânicas de RPG.
O objetivo inegociável é **fidelidade de lógica de negócio e fidelidade visual** ao React.
Os dois apps rodam em paralelo; o port é incremental. Qualquer divergência não documentada
é um bug.

## 2. Relação React → Android

- **Fonte de verdade**: o código real do app React (`main`). Em caso de divergência, o React
  está certo, salvo **decisão consciente registrada no `PARIDADE.md`**.
- O AI Studio não tem acesso ao repo React e seu código pode divergir do real. Em dúvida,
  exigir o **trecho real** do React (fornecido por quem tem acesso ao repo); nunca supor.
- Há divergências intencionais (ex.: `lastDungeonClearedTime` persistido só no Android,
  `totalXP` incrementado só no Android). **Consulte o `PARIDADE.md` antes de "corrigir"
  qualquer divergência.**

## 3. Documentos de controle

| Documento | Quando consultar | Conteúdo |
|---|---|---|
| `PARIDADE.md` | **Antes** de cada bloco | Estado de paridade por módulo, decisões conscientes, riscos |
| `DEV_LOG_ANDROID.md` | Antes (últimas entradas) e **depois** de cada bloco | Log técnico por bloco: arquivos, validação, desvios aprovados |
| Código React real | Quando a lógica não está clara | Fonte de verdade |
| `app/schemas/` | Ao alterar entidades Room | Schemas versionados |
| `specs/` | Quando houver spec para o trabalho | Escopo e auditorias (ex.: `002b-kingdom-visual-audit-addendum.md`) |

Regras do `PARIDADE.md`:
- Atualizar a linha **no mesmo bloco** em que o módulo foi trabalhado, nunca retroativamente.
- "Fidelidade Lógica: Validada" exige teste unitário cobrindo o comportamento (ou validação
  manual explícita registrada no `DEV_LOG_ANDROID.md`).
- "Fidelidade Visual: Validada" exige print real lado a lado com o React nos viewports
  375×667 e 390×844.
- Validação de **interação** (Voltar, gestos, foco de janela) exige **device/emulador real**;
  leitura de código não basta.

Não existe `README.md` no projeto.

## 4. Arquitetura

```
MainActivity (Compose, single Activity)
  └── HeroLogViewModel (único ViewModel)
        ├── CharacterRepository → Room (blob JSON)
        ├── FocusSessionRepository → Room (blob JSON)
        └── Logic functions (puras, sem side-effects por padrão)
```

- **Lógica** em `logic/` (funções puras ou `object`), por domínio: `quests/`, `focus/`,
  `achievements/`, `kingdom/`, `character/`. Algumas funções têm side-effects **por design**:
  não as "corrija" para o padrão puro sem confirmar que é bug real.
- **ViewModel** orquestra: chama a lógica e persiste via `saveCharacterState()`, o **funil
  único de mutação** (inclui detecção de level-up).
- **Navegação**: `activeTab: String` (5 módulos: Foco, Herói, Skills, Missões, Reino).
  Herói, Missões e Reino abrem `ModalBottomSheet` com sub-abas; `SUB_TABS` em
  `HeroLogBottomNav.kt`.
- **Sem DI** (Hilt/Dagger/Koin): `HeroLogApplication` é o container manual e
  `HeroLogViewModelFactory` injeta os repositories.
- **Pacotes** (`com.iurispraecepta.herolog/`): `model/` (tipos `@Serializable`), `data/`
  (entity, dao, database, repository), `logic/`, `ui/` (ViewModel, `theme/`, `navigation/`,
  `components/` e uma pasta por módulo). Use a árvore de arquivos como referência, não este
  resumo.

### Persistência

- **Room com blob JSON**: `CharacterState` e `ActiveFocusSession` serializados via
  `kotlinx.serialization` em tabelas de linha única. Campos novos com default entram na
  serialização **sem migration**.
- **Migration explícita é obrigatória** ao criar entidade ou alterar/remover coluna.
  `fallbackToDestructiveMigration()` foi removido de propósito: sem migration o app crasha em
  dev em vez de resetar dados. Schemas em `app/schemas/` (`exportSchema = true`).
- **`@SerialName` em todos os enums** garante paridade byte a byte do JSON com o React.
  Nunca remover ou alterar sem verificar impacto.
- `FocusRewardsLogic.calculate` usa `Random.Default`. O resultado é persistido pronto
  (`pendingCalculation`) e **nunca recalculado**, para evitar reroll de loot ao reabrir o app.
- `createInitialCharacterState()` começa com `skills`, `habits`, `dailies` e `todos` vazios
  de propósito. Não adicionar dados de exemplo ou pessoais.
- **Três formatos de data coexistem** no `CharacterState` (não são equivalentes):
  - `toDateString()` (`"Tue Aug 04 2026"`): seed do LCG e `lastStudyDate`
  - `toLocaleDateString('pt-BR')` (`"04/08/2026"`): `todayLocalStr()` e filtro de quests
  - `toLocaleString('pt-BR')` (`"04/08/2026, 14:23:10"`): `HistoryEntry.date`

### Dependências

- Ícones: `com.composables:icons-lucide-android` (**não** Material Icons; decisão explícita).
- **Haze** (`dev.chrisbanes.haze:haze` + `haze-materials`): blur de backdrop.
  - Uso: `hazeSource` no `Scaffold` raiz (`MainActivity`); `hazeEffect` em `HeroLogModal`
    (backdrop dos modais) e `HeroLogBottomNav` (nav bar ultra-thin). O estado é compartilhado
    via `LocalHazeState`, definido em `HeroLogModal.kt` e provido em `MainActivity.kt`.
  - Configuração: `noiseFactor = 0f` explícito (o default da lib é 0.15f). O `blurRadius`
    do Haze **não** equivale a px de CSS: `0.25dp` foi calibrado como equivalente ao
    `backdrop-blur` de 2px do React. Não "corrija" esse valor por analogia.
  - Em API sem suporte a blur de `RenderEffect` (abaixo do Android 12), a expectativa é ficar
    sem blur, só com scrim escuro (comportamento reportado; **não verificado em device**).
  - **Versão do Compose divergia da BOM (corrigido, Bloco BOM/FlowRow, 21/09)**: a BOM
    `2024.09.00` fixava o Compose em 1.7.0, mas `haze` e `haze-materials` puxam
    `org.jetbrains.compose.*:1.8.0` no runtime; o compile do `foundation-layout` ficava em
    1.7.0, e `FlowRow` compilava contra a assinatura 1.7.0 e quebrava em runtime com
    `NoSuchMethodError`. **Correção**: BOM subida para `2025.04.01` (`foundation-layout`
    1.8.0 tanto em `debugCompileClasspath` quanto em `releaseRuntimeClasspath`); `FlowRow`
    real restaurado nos 5 usos (4 arquivos); `FlowRowStable` removido. Ver `DEV_LOG_ANDROID.md`
    (Bloco BOM/FlowRow) e `PARIDADE.md` (Ficha de personagem, Tela de skills).
  - **Ao subir Haze ou a BOM**, comparar `debugCompileClasspath` com `releaseRuntimeClasspath`
    (`./gradlew :app:dependencies --configuration <cfg> | grep foundation-layout:`) antes de
    fechar o bloco.
- Fontes Cinzel/JetBrains Mono existem em `res/font/` e `Type.kt`, mas vários componentes
  ainda usam `FontFamily.Serif/Monospace` do sistema (pendência de wiring; lista em
  `specs/002b-kingdom-visual-audit-addendum.md`).

### Toolchain (WSL)

JDK 17 (`~/jdk-17`, compilação), JDK 21 (`~/jdk-21`, Robolectric), Gradle 9.3.1 (wrapper),
Android SDK 36 (`~/Android/Sdk`), Roborazzi. `./gradlew assembleDebug` e
`./gradlew testDebugUnitTest` rodam direto na sessão. Validação visual e de interação é em
device/emulador.
Screenshot: `-Proborazzi.test.verify=true` compara; `-Proborazzi.test.record=true`
regrava. O filtro `--tests "*Classe.metodo"` usa o nome da classe Kotlin, não o do
arquivo (uma classe pode viver dentro do arquivo de outra).

## 5. Fluxo por bloco

Cada alteração é um **Bloco** (0, 0.1, 1, ...): escopo pequeno e auditável, implementado,
validado e registrado no `DEV_LOG_ANDROID.md` na mesma sessão.

Antes de implementar:
1. Ler a seção do módulo no `PARIDADE.md` (status, decisões conscientes, riscos, pendências).
2. Ler as últimas entradas do `DEV_LOG_ANDROID.md`.
3. Obter o trecho real do React correspondente.
4. Confirmar que os imports são de `model.*` (os tipos reais).
5. Implementar com escopo mínimo, separando lógica pura de UI.

Ao fechar:
6. Rodar `./gradlew assembleDebug` e `./gradlew testDebugUnitTest`; conferir o **XML bruto**
   em `app/build/test-results/testDebugUnitTest/` (resultado nominal por método, não só
   "BUILD SUCCESSFUL").
7. Registrar no `DEV_LOG_ANDROID.md` (arquivos, resumo, validação, desvios aprovados).
8. Atualizar o `PARIDADE.md` se o estado de paridade mudou.

## 6. Regras obrigatórias

1. **Resumo em prosa não é prova de teste.** O ambiente de desenvolvimento já fabricou
   contagens e nomes de teste. Exigir XML bruto ou saída nominal por método.
2. **"O arquivo não existe" / "não há tela de config" exige grep real.** Entry points podem
   estar escondidos em componentes existentes.
3. **Catálogos e dados literais extensos** (títulos, quests, conquistas) são transcritos do
   código real, nunca inventados, e verificados campo a campo. A taxa de fabricação em
   transcrição extensa já foi alta.
4. **Nunca presumir a mesma lógica por analogia.** Foco, Habit, Daily e Todo têm fórmulas
   próprias; cada um exige verificação literal do React.
5. **Imports silenciosos podem ligar a tipos errados.** Conferir que os tipos vêm de
   `model.*`, não de pacote paralelo/morto (`logic.quests.*` já teve cópias mortas).
6. **Nunca alterar o schema Room sem `Migration` explícita** (ver Persistência).
7. **Nunca executar `git push` sem autorização explícita.** Commitar apenas quando pedido.
8. **Suíte completa pode travar por memória** (~34 classes de teste; `-Xmx1536m` já foi
   insuficiente). Nesse caso, rodar por classe e registrar como **validação parcial**; nunca
   declarar "validado" numa validação parcial.
9. **Testes de screenshot (Roborazzi) são baseline, não substituem inspeção humana.**
   Diferença de pixels não diz qual lado está certo: comparar com o React antes de
   regravar (`_compare.png` em `app/build/outputs/roborazzi/`). Regravar só o teste alvo
   e conferir com `git status --short app/src/test/screenshots/` que só os PNGs
   esperados mudaram.
10. **Dialogs e `BackHandler`**: dentro de `Dialog`, o handler precisa estar na janela do
    próprio `Dialog` e ter guard `enabled = isOpen && ...`. Só considerar "validado" após
    teste em device real. Detalhes estão no `DEV_LOG_ANDROID.md`.

### Padrão de teste de lógica

```kotlin
@Test
fun function_scenario_expectedResult() {
    // Arrange: estado inicial
    // Act: chamar a função de lógica
    // Assert: campo a campo (nunca igualdade de objeto inteiro)
}
```

Tipos de teste: **unitário** (lógica pura, exigido para "Lógica: Validada"); **integração**
(ViewModel + Room em memória via Robolectric, `Room.inMemoryDatabaseBuilder`);
**screenshot** (Roborazzi, PNGs em `app/src/test/screenshots/`).

## 7. Worktrees e branches secundárias

Em **branch secundária**, **não atualizar** `AGENTS.md`, `PARIDADE.md` nem
`DEV_LOG_ANDROID.md` (risco de conflito com a main). Ao fechar o bloco, entregar ao usuário:
- o diff, arquivo por arquivo;
- uma entrada sugerida para o `DEV_LOG_ANDROID.md` e para o `PARIDADE.md` (indicando a
  linha afetada e o novo conteúdo), pronta para colar na main.

Exceção: `AGENTS.md` pode ser atualizado se a mudança for exclusiva da branch, e só com
confirmação explícita do usuário.

**Ao atualizar docs após merge**, confirmar antes que está no worktree da main
(`/home/bruno/projetos/HeroLog-Android-2`) com `git worktree list` e `pwd`. Se estiver em
worktree de feature, **parar e avisar o usuário**: esse worktree pode estar desatualizado
ou conter arquivos revertidos.
