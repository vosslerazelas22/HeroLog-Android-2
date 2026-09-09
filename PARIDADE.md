# PARIDADE.md — Rastreamento de fidelidade React ↔ Android

## Por que este arquivo existe

O objetivo inegociável do port é **fidelidade de lógica de negócio e fidelidade visual** entre o
app React (fonte de verdade) e o app Android nativo (Kotlin/Compose). Com migração incremental e
os dois apps rodando em paralelo, é fácil perder de vista o que já foi portado e validado, o que
está em andamento, e o que nem foi tocado ainda. Este arquivo é o mapa único desse estado.

**Fonte de verdade para o que "correto" significa**: o código real do app React (`main branch`,
não o ambiente AI Studio — ver `ARCHITECTURE_MOC.md` seção 1 para as distinções conhecidas entre
os dois). Sempre que o Android divergir, o React é quem está certo, a menos que a divergência seja
uma decisão consciente registrada aqui (ex: sync com Supabase fora de escopo por ora).

## Como atualizar

Atualizar a linha correspondente **no mesmo bloco em que o módulo/item for trabalhado**, nunca
retroativamente por memória. "Fidelidade Lógica: Validada" só pode ser marcado quando houver teste
unitário cobrindo o comportamento (ou, na ausência de teste viável, validação manual explícita
documentada no `DEV_LOG_ANDROID.md`). "Fidelidade Visual: Validada" só pode ser marcado com print
real comparando lado a lado com o React nos dois viewports de referência (375×667 e 390×844).

Status possíveis: `Não iniciado` · `Em andamento` · `Bloqueado` · `Validado`

---

## 1. Camada de dados e persistência

| Item | Status | Fidelidade Lógica | Fidelidade Visual | Notas |
|---|---|---|---|---|
| Modelos de dados (`CharacterState` e tipos auxiliares) | Validado | Validada — todos os tipos portados 1:1 a partir do `types.ts` real, incluindo enums com `@SerialName` para paridade de JSON, `Daily.value` como `Int` confirmado por auditoria do código-fonte. `Skill.id: String` adicionado no Bloco 15 (default `UUID.randomUUID()`, decisão consciente: fidelidade + estabilidade de key futura no Compose + futuro sync Supabase) — auditoria confirmou que nenhum teste de `SkillLogicTest` faz comparação de objeto `Skill` inteiro (todos comparam campo a campo), então o id randômico de skills novas (`addCustomSkill`) não exigiu tratamento especial além de não ser testado | N/A | Bloco 1 + 1.1 + 15. Fora de escopo (não fazem parte de `CharacterState`): `Quest`, `Achievement`, `ActiveSession`, tipos de modal de level-up — ver seção 9 |
| Persistência local | Em andamento — abas Personagem e Inventário ligadas de verdade (Bloco 28); Habilidades e Foco ainda em dados fake | Validado (Room, Opção A: blob JSON, `CharacterRepository`) | N/A | Testado via `CharacterRepositoryTest` (Bloco 0.1, fake DAO), `HeroLogViewModelTest` (Bloco 27/28, Room real em memória via Robolectric), `HeroLogInitialStateTest` e `CharacterMappersTest` (Bloco 28). `HeroLogApplication`/`HeroLogViewModel` (Bloco 27) agora consumidos de verdade por `CharacterScreen` e `InventoryScreen` na `MainActivity` (Bloco 28) — `createInitialCharacterState()` auto-cria e persiste um personagem novo se o banco estiver vazio. **Bug real corrigido no Bloco 28**: `sellItem` do scaffolding anterior calculava o preço de venda mas nunca somava ao `gold` (só logava) — corrigido conferindo fonte real (`useInventory.ts`). Próximo passo natural: wiring do timer de Foco sobre o mesmo ViewModel |
| Sync remoto (Supabase) | Não iniciado | N/A | N/A | **Fora de escopo por decisão explícita** — só entra em fase avançada do port (ver conversa de fundação) |
| Migration real do Room (v1→v2) | **Fechado (29/08)** — `MIGRATION_1_2` substitui `fallbackToDestructiveMigration()`, ver `DEV_LOG_ANDROID.md` | Validada — diff de schema v1→v2 auditado a partir do código-fonte das entidades e do `HeroLogDatabase_Impl.kt` (sem `.git` disponível no container do AI Studio nesta auditoria). Única mudança: tabela nova `active_focus_session`; `character_state` inalterada. `exportSchema = true` + `room.schemaLocation` configurados, schemas `1.json`/`2.json` versionados | N/A | Motivada por warning de depreciação (`HeroLogApplication.kt:15`), que expôs risco maior: fallback destrutivo apagava o banco local inteiro sem aviso a cada bump de versão sem migration — sem rede de segurança (Supabase fora de escopo). Build limpo com `--rerun-tasks` (50/50 tasks executadas, não `UP-TO-DATE`) + XML bruto: 18 testes, 0 falhas/erros (`CharacterRepositoryTest` 1/1, `FocusSessionRepositoryTest` 5/5, `SaveMigrationLogicTest` 12/12). **A partir de agora, qualquer bump futuro de versão do banco exige `Migration` explícita** — sem ela, o app crasha em dev em vez de resetar dados silenciosamente |
| `normalizeGameState` / migração de save antigo | **Fechado (27/08)** — lógica (`SaveMigrationLogic.kt`) + UI de import (`RestoreSaveDialog.kt`) integradas, commit `24b3a26`. **Atualizado (07/09) — Bloco E: Backup/Restore completo em 4 mídias**: adicionado `ExportPayload` versionado + `GameStateExporter` (arquivo via `FileProvider` + string pra clipboard) + `GameStateImporter` (arquivo via `ActivityResultContracts.GetContent` + texto colado, reusa `SaveMigrationLogic`); `RestoreSaveDialog` reconectado como sub-fluxo do `GeneralSettingsModal` (era entry point perdido, idêntico ao achado de 28/08 do `AppHeader`); `JsonConfig` extraído pra `data/JsonConfig.kt` (default+pretty), refatorado `CharacterRepository`/`FocusSessionRepository` pra consumir do singleton (elimina duplicação). Build real (`assembleDebug`): BUILD SUCCESSFUL. Suíte: 19/19 testes novos (`GameStateExporterTest` 9/9 + `GameStateImporterTest` 10/10), `HeroLogViewModelTest` 57/57 sem regressão, 6 falhas pré-existentes do Roborazzi (`FlowLayout`/`FlowRow` NoSuchMethodError) não relacionadas a este bloco → **resolvidas em 08/09 via `FlowRowStable` (Bloco FS), ver DEV_LOG**. **Mudança de escopo consciente** vs registro anterior: o "recorte deliberado" de 27/08 deixava export/import por arquivo + copiar pra clipboard fora de escopo (por falta de equivalente Supabase/conta); Bruno reverteu essa posição em 07/09 após a fonte React continuar evoluindo sem sync remoto — agora as 4 mídias (arquivo/texto × export/import) estão dentro de escopo, paridade visual do modal "Ajustes Gerais" do React é meta declarada | Validada — `SaveMigrationLogic.kt` (`normalizeGameState`) + `GameStateImporter.parsePayload` (detecta `version` no JSON, faz spread raso do `character` em caso de `ExportPayload` ou delega direto no caso de `CharacterState` cru, ambos via mesma função de migração) + `GameStateExporter.encodePayload` (reusa `CharacterRepository`/`FocusSessionRepository` via `JsonConfig.pretty` com `encodeDefaults=true` pra arquivo robusto a campos novos futuros). Auditado contra `useGameState.ts`/`App.tsx` reais: spread raso `{...INITIAL_STATE, ...parsed}` reusando `createInitialCharacterState()` já existente, + migrações específicas (pomodoroSettings com fallback pro campo morto `longBreakMinutes`, `skills[].id/emoji/prestige` com defaults determinísticos `sk-N`/💻🧪📚🎯 — não UUID aleatório, fiel à fonte —, `dailies[].value/createdAt`, `todos[].createdAt`, guarda de `equippedEquipment`) | Não validada formalmente (sem print lado a lado) | React: `useGameState.ts` (`normalizeGameState`/`importGameState`) + `App.tsx` (`handleImportSaveFromText`/`handleExportSave`/`handleCopySaveToClipboard`, modal "Restaurar Código Rúnico" + botões de export/copy do modal "Ajustes Gerais"). Reusa o mesmo `RestoreSaveDialog` (texto colado) + `SaveImportResultDialog` (sucesso/falha do import) que já existiam. **Entry point**: engrenagem do `AppHeader` → `GeneralSettingsModal` (já existia com perfil/classe/orb concept) → nova seção "Backup da Campanha" com 4 ações: "Exportar arquivo .json" (`Intent.ACTION_SEND` + `FileProvider`, abre chooser do Android — Gmail/Drive/etc.), "Copiar código rúnico" (`ClipboardManager`, label "HeroLog Save"), "Importar arquivo .json" (`ActivityResultContracts.GetContent("application/json")` → `GameStateImporter.readFromUri`), "Restaurar código rúnico" (sub-fluxo do `RestoreSaveDialog`). Permissões: nenhuma nova (FileProvider concede `FLAG_GRANT_READ_URI_PERMISSION`, `cacheDir` é privado). `version=1` por enquanto; `version>1` retorna `InvalidJson` (sem migrations conhecidas, defensivo). `orbConcept` (campo do `INITIAL_STATE` real sem equivalente em `CharacterState.kt`) é silenciosamente ignorado na decodificação (`ignoreUnknownKeys = true`) — divergência de modelo já existente no port, não introduzida aqui. Nota: `HeroLogModal(...)` em `RestoreSaveDialog.kt` foi chamado sem o parâmetro `modifier` (não existe na assinatura real do componente) — ajuste feito pelo AI Studio na hora de compilar, não pedido por mim |

| **SFX (Efeitos Sonoros)** | **Fechado (11/09)** — Bloco D199: `SfxManager.kt` (factory `create(context)` + `noOp()`), 6 WAVs gerados via script Python replicando fórmulas exatas do `NativeAudioEngine` React (`audio.ts`). 39 call sites: 24 no `HeroLogViewModel` (playCoins/playLevelUp/playDeath/playWildernessWarning/playFocusBell), 4 em `SkillInlineCarousel` (playClick), 1 em `LevelUpOverlay` (playLevelUp). Toggle `isSfxMuted` sincronizado via `LaunchedEffect` no `MainActivity`. `CompositionLocalProvider` provê `LocalSfxManager` pra componentes UI. | **Implementada** — fidelidade de lógica: 6 métodos replicam fórmulas exatas do React (frequências, envelopes de ganho, durações). Call sites mapeados 1:1 contra os 39 do React, exceto 2 bugs do React (ShopTab:95, QuestsTab:20 sem guard `!muteSfx`) que não foram replicados. `SfxManager.isMuted` checado internamente em cada `play()` — universal guard. | **Não validada** — build OK, 491/491 testes passaram, mas inspeção visual em device pendente. | React: `src/utils/audio.ts` (`NativeAudioEngine`). Wavs: `sfx_focus_bell.wav` (3.5s), `sfx_level_up.wav` (1.54s), `sfx_coins.wav` (0.41s), `sfx_death.wav` (1.5s), `sfx_wilderness_warning.wav` (0.55s), `sfx_click.wav` (0.06s). Decisão: WAVs estáticos (não síntese em runtime) — performance e simplicidade. Bugs React não replicados: `ShopTab.tsx:95` e `QuestsTab.tsx:20` tocam sem checar `!muteSfx`. |

## 2. Navegação e shell do app

> **Correção de registro (2026-07-29):** o `ARCHITECTURE_MOC.md` menciona um menu lateral por
> gesto (`isMobileSidebarOpen`) como parte do layout mobile do React. Bruno confirmou que a
> versão mobile do React usa **apenas bottom nav** — não há menu lateral por gesto relevante
> nesse contexto. Removido da tabela abaixo; se isso mudar, atualizar aqui e no `ARCHITECTURE_MOC.md`.

| Item | Status | Fidelidade Lógica | Fidelidade Visual | Notas |
|---|---|---|---|---|
| Navegação inferior (mobile) | **Fechado funcionalmente (22/08)** — build verde + validação manual: sheet abre, sub-abas navegam, CRUD de Habits/Dailies/Todos testado em cada uma, Herói/Foco não quebraram. Bloqueio original ("não vejo Missões no app") resolvido | Fiel — 5 módulos top-level (Foco, Herói, Skills, Missões, Reino) replicando `navItems`/`getActiveModule` da fonte; Foco e Skills navegam direto, Herói/Missões/Reino abrem sub-abas (`SUB_TABS`) num `ModalBottomSheet` (equivalente nativo ao sheet deslizante `motion.div` da fonte). `activeTab` migrado de `Int` pra `String`, espelhando `useState<string>('focus')` real | **Funcional validado; formal (print lado a lado) ainda pendente** — screenshot do sheet confirma fidelidade visual básica (cores/ícones/layout), mas H1/H2/H3 seguem sem comparação formal pixel-a-pixel com o React | Equivalente do `BottomNav.tsx`. Mapeamento de ícones lucide-react → Material Icons confirmado por build real — `Castle` e `Checklist` existem na lib. 2 warnings de depreciação (`HelpOutline`, `MenuBook` → `AutoMirrored`) corrigidos. `MainActivity.kt` reescrita: navegação `Int`→`String`, `Scaffold.bottomBar` chama `HeroLogBottomNav`, `when` cobre os 5 módulos + sub-abas reais + `PlaceholderScreen` pras não portadas. **Persistência de Habits/Dailies/Todos**: mesmo blob JSON de `CharacterState` já validado pra Personagem/Inventário (Bloco 28) — reset observado foi só no preview efêmero do AI Studio, não em APK instalado; teste real de persistência (fechar/reabrir app instalado) ainda pendente de Bruno. **Alinhamento de ícones (08/09)**: `verticalAlignment = Alignment.CenterVertically` no Row + animação de offset (7dp, 200ms) + pill ativo animado (250ms) — parity com CSS Grid `items-center` do React. **Insets/visual (08/09, spec-bottom-nav-header-insets)**: `enableEdgeToEdge(SystemBarStyle.dark())` força ícones do sistema claros; `isNavigationBarContrastEnforced = false` (guard `>=Q`, corrigido de `Q..R` em 11/09) remove scrim de contraste da nav bar; `consumeWindowInsets` substitui `padding(innerPadding)` no Box hazeSource — conteúdo agora passa borrado atrás da bottom nav; `LocalBottomBarInset` (compositionLocal) fornece bottom padding para 12 telas scrolláveis. **Visual device: pendente** (T5-T10 da spec) |
| `HeroLogModal` (componente de modal reutilizável) | Validado | Validada — testado manualmente (não é lógica de jogo, é chrome de UI) | Validado — confirmado visualmente no preview real (Bloco 6.1) | Bloco 3 + 3.1. Port de `Modal.tsx`. TODO pendente: `incrementModalCount`/`decrementModalCount` (função real desconhecida) |
| `ItemInspectModal` (componente de modal de inspeção de item) | Validado | N/A (chrome de UI, não lógica de jogo) | Parcial — screenshot em modo record, 4 estados cobertos, ainda sem inspeção visual humana | Bloco 9. Port de `ItemInspectModal.tsx` — base compartilhada de `CharacterScreen` e `InventoryScreen`, ainda não portados |
| `AppHeader` (header global — logo, streak, gold, mute, ajustes) | **Regressão corrigida (28/08)** — `onOpenSettings` reconectado a `isRestoreSaveOpen = true` (mesmo estado que o `TopAppBar` antigo usava, `RestoreSaveDialog` nunca foi removido, só ficou desconectado do botão). `assembleDebug` real: `BUILD SUCCESSFUL in 54s`, diff de 1 linha confirmado (`MainActivity.kt`, `onOpenSettings`). **Re-reconectado (07/09) — Bloco E: Backup/Restore**: o `RestoreSaveDialog` tinha voltado a ficar sem caller (mesma regressão documentada acima, agora definitivamente resolvida). A engrenagem continua apontando pra `isGeneralSettingsOpen = true`, e o `RestoreSaveDialog` (import por texto) virou sub-fluxo do `GeneralSettingsModal` (4ª ação da nova seção "Backup da Campanha" do `GeneralSettingsModal`) — agora há um caminho estável e descobrível (engrenagem → modal de ajustes → 4 ações de backup: export arquivo / copy clipboard / import arquivo / restore texto). Os 3 outros caminhos de backup (export arquivo, copy clipboard, import arquivo) também vivem na mesma seção, com permissões zero (FileProvider+FLAG_GRANT_READ_URI_PERMISSION, `cacheDir` privado, `GetContent` sem runtime permission). **Ainda falta**: validação visual em device (0% feita) | Fiel — `streak`/`gold` do `HeroLogViewModel.characterState`, engrenagem abre `RestoreSaveDialog` de novo. `isSfxMuted` local (SFX fora de escopo) | Não validada — sem print, sem device real | **Atualizado (29/08)**: ícones Lucide agora vêm de `com.composables:icons-lucide-android:2.2.1` (`R.drawable.lucide_ic_*` + `painterResource`) — path data manual (`buildLucideIcon`/`ImageVector.Builder`) descontinuado nesta sessão, ver bloco Quick Actions Bar no `DEV_LOG`. Não Material Icons — decisão explícita do Bruno, mantida. Cores hardcoded localmente no arquivo (`champagne-400 #e5c158` etc.), **não tocou** `ui/theme/Color.kt` (ainda na paleta Amber antiga). Fontes Cinzel/JetBrains Mono definidas em `Type.kt` (arquivos `.ttf` em `res/font/`), mas muitos componentes ainda usam `FontFamily.Serif`/`.Monospace` do sistema — ver `specs/002b-kingdom-visual-audit-addendum.md` para a lista completa de ocorrências. Lição registrada: `TopAppBar` substituído por componente novo tinha entry point escondido (`RestoreSaveDialog`) que não estava documentado como dependência até o AI Studio relatar "não existe tela de config" sem checar o arquivo inteiro — reforça a regra de nunca aceitar essa frase sem grep real. **🔴 28/08, novo achado por print do Bruno**: header sobrepondo a status bar do Android (relógio/ícones em cima do "HEROLOG"/pills) — `AppHeader.kt` não tinha `statusBarsPadding()`, algo que não existe na fonte React (web não tem status bar) e por isso não foi portado na primeira versão. **Corrigido e build confirmado (28/08)**: diff de 1 import + 1 linha de modifier (`.statusBarsPadding()` entre `.background()` full-bleed e `.drawBehind{}`), `assembleDebug` real: `BUILD SUCCESSFUL in 50s`. **Ainda falta**: (a) `git log`/`git pull` — AI Studio reportou que o container não é um repo git local, então essa verificação PRECISA ser feita por Bruno fora do AI Studio (Armadilha da OAuth loop, "build verde ≠ push real"); (b) print real confirmando que o overlap sumiu — ainda não recebido |

## 3. Módulo Foco

| Item | Status | Fidelidade Lógica | Fidelidade Visual | Notas |
|---|---|---|---|---|
| Timer Pomodoro (controles de sessão) | **Completo** — wiring real na `MainActivity` (Blocos 35/36), UI fiel do timer de descanso (`BreakPrepScreen` + `FocusOrb` variante esmeralda, Bloco 41), reset de `isDungeonMode`/`dungeonSessionsProgress` ao completar a 4ª masmorra e ao abandonar sessão (Bloco 42) — **todos validados visualmente por Bruno de ponta a ponta** | Validada — `FocusApplyLogic.apply` chamado de fato em `confirmFocusSession`, confirmado com valores reais em screenshots humanas. `startBreakTimer`/`confirmFocusSession` decidindo curta/longa duração de descanso fiel a `handleConfirmClaimRewards` real (Bloco 39). `abandonSession()` nova (Bloco 42), isolada de `cancelSession()` para não interferir na chamada de segurança interna de `startBreakTimer()` — replica os dois pontos de reset da fonte real (`handleConfirmClaimRewards` linha ~1483-1499 E `handleAbandonSession` linha ~1241-1248). 51/51 testes de Foco sem regressão | **Confirmada por inspeção visual humana real** — fluxo completo incluindo timer de descanso (com e sem autostart) e os dois cenários de reset do segmented control (conclusão da 4ª sessão e abandono no meio), 4 prints reais (18/08) | React: `App.tsx` + `useFocusSession.ts` + `useBreakTimer.ts` (fonte real obtida 16/08). SFX (`audio.ts`) e som ambiente (`useAmbientSound.ts`) fora de escopo por decisão explícita — fontes reais já obtidas e registradas pra quando retomado. **Divergência de UX registrada, fora de escopo por ora**: React exige clique duplo em 5s para confirmar abandono de sessão (`isConfirmingAbandon`) + log de sistema "FRACASSO NA MASMORRA"; Android sai direto no primeiro clique, sem confirmação nem log (não há infraestrutura de log de sistema real ainda). **Bloco 41 (código) e Bloco 42 ainda não commitados no GitHub** — bloqueio de integração GitHub↔AI Studio (loop de login OAuth), deixado de lado por decisão de Bruno (17/08); código existe só no ambiente AI Studio. **Correção de texto (07/09)**: botões Pausar/Abandonar/Reto... **Correção visual (11/09, Bloco TooltipButtons)**: Iniciar Missão fontSize 13→14sp, padding 14→12dp, shape 6→4dp, +Cinzel; Pausar/Retomar +Cinzel, letterSpacing 0.8→1.5sp (tracking-widest); botão "?" 20→18dp, tooltip movido para Popup overlay (paridade React position:absolute). 491/491 testesmar/Confirmar corrigidos de ALL CAPS para sentence case (`"RETOMAR MISSÃO"` → `"Retomar Missão"`, `"CONFIRMAR?"` → `"Confirmar?"`, etc.), alinhando com `App.tsx:2775,2785` e `FocusModeScreen.tsx:215` |
| Persistência de sessão ativa de Foco | Validado — motor completo (Blocos 29/30/31/32): escrita nos pontos certos + recuperação nos 3 estados possíveis ao reabrir o app | Validada — `FocusSessionRepositoryTest` 5/5 + `FocusSessionViewModelTest` 12/12 + `FocusSessionRecoveryTest` 4/4 (nenhuma sessão / em andamento / expirada sem cálculo / já calculada aguardando confirmação) | N/A | React: `localStorage` (`herolog_active_session`, `App.tsx`) + `completeSessionOnReload`. **Bug conhecido no React, registrado e não replicado por decisão consciente (confirmada com Bruno)**: `completeSessionOnReload` aplica a recompensa silenciosamente ao detectar sessão expirada no reload, sem passar pela tela de confirmação. O Android nasce corrigido — nunca aplica automaticamente, sempre resolve para o mesmo estado "pendente de confirmação" que a conclusão natural em foreground usaria. **Decisão de design**: `FocusRewardsLogic.calculate` usa `Random.Default` (não determinístico) — por isso o resultado calculado é persistido já pronto (`pendingCalculation`), nunca recalculado numa recuperação futura, para não permitir reroll de loot fechando/reabrindo o app. Sessão pausada não é recuperável entre reinícios — comportamento herdado fielmente do React (`localStorage.removeItem` no branch de pausa), não é limitação nova do Android |
| Tela imersiva de foco | Validado | N/A (componente apresentacional, sem lógica própria — recebe tudo hoisted) | Validado — confirmado visualmente no preview real (Bloco 21): cobertura de tela cheia sem vazamento de chrome, tags de modo corretas (isoladas e combinadas), estado urgente (vermelho, <60s) disparando corretamente dentro da tela, Pausar/Sair funcionais, `FocusOrb` recebendo tempo real da tela pai | React: `FocusModeScreen.tsx`. Deliberadamente sem gerenciamento de timer próprio nem decisão de desmontagem (`isFocusCompleted` do React) — fica para bloco futuro de wiring com timer real/ViewModel. **Simplificações conscientes**: chuva de partículas cintilantes (15 '✦') omitida; atalho de teclado (Espaço) e auto-foco de botão tratados como N/A por design de plataforma (touch, não teclado/mouse). **Achado durante teste visual**: exclusividade mútua Masmorra/Wilderness confirmada no React real, mas a regra não vive nesse componente — ver nota de risco na seção 8 | **S6 (11/09)**: +Cinzel nos 3 botões (Retomar/Pausar/Sair), "SAIR"→"Sair" (React sentence case) |
| Focus Orb | Validado | N/A (componente puramente visual, sem lógica de negócio própria) | Validado — confirmado visualmente no preview real (Bloco 19/19.1): líquido preenchendo conforme progresso, gradiente âmbar (trabalho) e verde-esmeralda (descanso) corretos, ondulação da superfície do líquido visível, anel pontilhado rotativo, texto do timer legível. Nota de processo: primeira rodada de prints (Bloco 19) não mostrou o líquido — causa raiz identificada como erro no scaffolding do preview (`totalSeconds` fixo em 1500 com `timeLeft` inicial de 90, resultando em `progress` ≈ 6%, faixa de líquido imperceptível), não bug no `FocusOrb.kt`; corrigido no Bloco 19.1 (`totalSeconds = 90`) e reconfirmado | React: `FocusOrb.tsx` — atenção ao tamanho aumentado recentemente (v1.1.8). **2 simplificações conscientes registradas, pendentes de decisão futura sobre incluir ou não**: (1) glow externo com blur (`blur-2xl` no React) omitido — `Modifier.blur` só funciona API 31+, alternativa seria círculos concêntricos com alpha decrescente simulando blur; (2) bolhas ambientes decorativas (5 círculos pulsando dentro do líquido) omitidas inteiramente. Nenhuma das duas afeta forma/cor/animação da onda ou o texto do timer, que foram portados fielmente (path de 12 pontos, mesma frequência ~1.2 ciclos, throttle de 33ms replicado via loop manual em vez de `rememberInfiniteTransition`, para preservar o comportamento de start/stop da animação, não só zerar amplitude) |
| Tela de configuração pré-sessão (seletor de skill + duração + segmented control Padrão/Masmorra/Selvagem + modais) | **Completa** — segmented control (Bloco 22), `IncursionModeModal`/`ModeDescriptionModal` (Bloco 38), `SkillSelectorModal` (já existia, reusado) + `TimerSettingsModal` (Bloco 40) todos portados e wireados. Reset do checkbox `isDungeonMode`/`dungeonSessionsProgress` ao concluir a 4ª masmorra e ao abandonar sessão também fechado (Bloco 42) | Segmented control: fiel (125/125 testes). Modais: fiel, 9 testes (Bloco 38). `TimerSettingsModal`: fiel — presets/validação de faixa/toggles conferidos linha a linha contra `App.tsx` real, 5 testes de ViewModel + 5 screenshot (Bloco 40). Bug real corrigido durante o bloco: `cancelSession()` não cancelava `breakTickJob` (job vazado). Reset de `isDungeonMode` (Bloco 42): `abandonSession()` nova, fiel aos dois pontos de reset da fonte (conclusão de 4ª sessão + abandono), 3 testes novos | Segmented control + `IncursionModeModal` + `ModeDescriptionModal`: confirmados por print real. `TimerSettingsModal`: só screenshot automatizado ainda. Reset do checkbox (Bloco 42): confirmado por 3 prints reais (18/08) — conclusão da 4ª sessão, abandono no meio, e contador `(0/4)` limpo | **Correção de correção (16/08)**: `IncursionModeModal.tsx`/`ModeDescriptionModal.tsx` são dois arquivos distintos, ambos portados no Bloco 38. Fonte real do "Timer Settings Modal" obtida via clone direto do React (`b805fdd3...`) — 3 seções (presets, custom+validação, autostart toggles), tudo em `App.tsx`, não um componente dedicado. **Padrão de relatório observado no Bloco 40**: primeiro relatório de fechamento tinha 3 imprecisões de prosa (testes listados incompletos, arquivo dito movido quando não foi, arquivo dito alterado quando não constava no diff) — código em si estava correto, só a prosa-resumo que precisou de correção via leitura direta do diff. **Divergência de UX registrada no Bloco 42, fora de escopo**: React tem confirmação em duplo clique (5s) + log de sistema ao abandonar sessão em masmorra; Android sai direto, sem confirmação nem log (infraestrutura de log de sistema ainda não existe) |
| Som ambiente | **Validado (29/08 + 02/09)** — `AmbientSoundController` (nível de Composable via `remember` + `SharedPreferences`, não no ViewModel), 8 trilhas MP3 portadas, toggle na `AmbientSoundModal`, sincronizado com `isRunning && !isPaused && !isBreakActive`. **Corrigido (02/09)**: troca de trilha em tempo real via `setDataSource` + `prepareAsync` (paridade com React `audio.src = newSrc; audio.load()`), controller movido para escopo HeroLogTheme (sobrevive a troca de aba). Confirmado por Bruno no device | Fiel — porte de `useAmbientSound.ts`. Simplificação anterior removida (agora troca trilha em tempo real e sobrevive a navegação) | Confirmado manualmente por Bruno no device (29/08 + 02/09) | React: `useAmbientSound.ts`. Toggle icons (`ToggleLeft`/`ToggleRight`) e ícone de volume do modal (`Volume2`) migrados pra `com.composables:icons-lucide-android` — ver nota da Quick Actions Bar abaixo |
| Quick Actions Bar (Modo/Som/Ajustes/Tela Cheia) | **Validado (29/08)** — desacoplamento `isRunning`/`isFocusMode` implementado (sessão roda inline na aba Foco, tela cheia é ação explícita), 4 botões wireados. **Bug real corrigido**: `onExit` do `FocusModeScreen` abandonava a sessão ao sair da tela cheia — deveria só fechar o overlay; sessão segue rodando. Confirmado por Bruno no device. **Correções visuais (07/09)**: container com borda + padding-top, botões com border radius 8dp, press feedback (ripple + background stone-900/40), label com Cinzel + letter-spacing, dot verde pulsante no botão de som quando trilha ativa, botões com weight(1f) em vez de largura fixa | Fiel — porte do "Quick Actions Row" do `App.tsx`. Ícones migrados de Material Icons aproximados para `com.composables:icons-lucide-android:2.2.1` (317★, `isTransitive = false`) após avaliação de 3 alternativas (`thelacspace/lucide-compose` 1★ descartada, `dead8309/lucide-kotlin` descartada por ser wrapper Kotlin/JS de `lucide-react`, não gerar recurso Android nenhum). Todos os 12 ícones conferidos contra lista explícita aprovada por Bruno: `Sparkles`, `Shield`, `ShieldAlert`, `Volume2`, `VolumeX`, `Settings`, `Maximize`, `Pause`, `Play`, `X`, `ToggleLeft`, `ToggleRight`. Correções visuais alinham: container (`border-t`, `pt-2`, `gap-1`), botões (`rounded-lg`, `min-h-[48px]`, `hover:bg-stone-900/40`), label (`font-serif`, `tracking-wider`), dot verde pulsante (`bg-emerald-400 animate-pulse`) | Confirmado manualmente por Bruno no device (29/08) — header, quick actions idle e sessão rodando, modal de som, Pausar/Abandonar inline. Correções visuais: **PENDENTE** — aguardando inspeção em device | React: `App.tsx:3026-3080` (Quick Actions Row), `AmbientSoundButton.tsx:27-61` (compact variant com dot verde). **Nenhum teste de screenshot dedicado existe** pra `AppHeader`/`QuickActionsBar`/`AmbientSoundModal` — cobertura automatizada da migração de ícone é zero por design de teste atual, validação é 100% manual (aceito, ver `DEV_LOG_ANDROID.md` do bloco). `AppHeader.kt` também migrado nesta leva — path data manual (`buildLucideIcon`) descontinuado, ícones agora vêm de `R.drawable.lucide_ic_*` |
| Skill Inline Carousel (troca rápida de skill ativa, aba Foco) | **Fechado (29/08)** — `SkillCarouselLogic.kt` (lógica) + `SkillInlineCarousel.kt` (UI) escritos, `MainActivity.kt` wireado (substituiu o "Skill Selector Entry Card" estático). Build real: `assembleDebug` → `BUILD SUCCESSFUL in 1m 15s`, sem warning novo. Regressão completa: 425/425 testes, 0 falhas/erros/skips | Validada — port 1:1 de `activeIdx`/`handlePrev`/`handleNext`/guarda `skills.length <= 1`/checagem de swipe (`Math.abs(deltaX) > Math.abs(deltaY) && Math.abs(deltaX) > 35`) do `SkillInlineCarousel.tsx` real (clonado fresh, commit `9fa5380`, 26/08). `SkillCarouselLogicTest.kt`: 14/14, XML bruto confirmado (`tests="14" failures="0" errors="0" skipped="0"`), RED verificado antes do GREEN (sandbox `kotlinc`+`junit4` real). Reusa estado já existente do card antigo (`validSkillIdx`/`selectedSkillIdx`/`focusState.isRunning`), confirmado contra trecho real do `MainActivity.kt` (linhas 504-741) colado por Bruno, não suposição | **Não validada** — sem screenshot Roborazzi (não existe teste de screenshot pra este componente ainda) e sem print real em device | React: `src/components/SkillInlineCarousel.tsx`, importado e usado em `App.tsx` linha ~2386, dentro do fluxo de Foco (`sessionConfig.selectedSkillIdx`). Distinto do `SkillSelectorModal` (gerenciamento via modal, intocado) — o carrossel é entry point novo, `onOpenSkillsManager` abre o modal existente. **Divergências deliberadas**: SFX (`sound.playClick()`) omitido, mesma decisão já registrada pro módulo Foco inteiro; atalho de teclado ←/→ N/A (plataforma touch). Cores hardcoded localmente (`#e5c158` etc.), mesmo padrão do `AppHeader.kt` — sem token `Champagne*` em `Color.kt` ainda |
| Header "CÂMARA DE FOCO" (banner + tooltip + QuestFab) | **Fechado (06/09) + S5 paridade visual (09/09)** — QuestFab: fontes migradas, modal title com emoji, quest names `Cinzel`+`Black`, progress `JetBrainsMono`, desc `Cinzel` | Fiel — header banner + tooltip + QuestFab modal. S5: "📜 Contratos Ativos" title, `Cinzel`+`FontWeight.Black` nos nomes de quest, `JetBrainsMono` no progresso, desc `Cinzel`, "Todas as teses conquistadas!" `Cinzel` | **Parcialmente validada (S5)** — inspeção device pendente | React: `App.tsx:2338-2381`, `QuestFab.tsx`. S5: 8 alterações cosméticas no QuestFab section. **S6 (11/09, Bloco TooltipButtons)**: tooltip movido para Popup overlay (paridade React position:absolute, sem afetar layout do banner); botão "?" 20→18dp (React `w-4.5 h-4.5`); botão fechar "x"→"×" |

## 4. Módulo Personagem

| Item | Status | Fidelidade Lógica | Fidelidade Visual | Notas |
|---|---|---|---|---|
| Ficha de personagem | Validado | Validada (usa `CombatLogic.requiredXpForCombatLevel`, `TITLE_CATALOG`) | **Não validada** — headers Status/Inventário adicionados (Bloco A+B), copys corrigidas (pluralização, XP label, equipment header, charges, casing, tooltip), mas sem print real comparando lado a lado com React | React: `CharacterScreen.tsx` (só versão mobile portada). Bloco 10 + A+B (07/09). Headers "FICHA DO HERÓI" e "EQUIPAMENTOS" agora presentes (antes ausentes). Copys corrigidas: pluralização dia/dias, XP label descritivo, equipment header "Itens Equipados", charges com emoji 🔋, tooltip "Câmara", mixed case nos tiles. Botão "+ Gerenciar Habilidades" não portado (pendência). **Bug de crash corrigido (08/09, Bloco FS)**: `NoSuchMethodError` ao renderizar chips de buff via `FlowRow` (afetava qualquer buff ativo, incluindo `StreakShield`). Causa raiz: `haze:1.6.10` forçando Compose 1.8.0 sobre BOM 1.7.0, mudando assinatura de `FlowRow`. Correção: `FlowRowStable` (composable estável via `Layout`), 6 usos substituídos em 4 arquivos, 484/484 testes passando. Validado no device físico por Bruno. |
| Inventário | Validado | Validada (lógica pura: `InventoryLogic.kt`, Bloco 7 + `calculateSellPrice` extraído no Bloco Inventário.1) | Validado — confirmado visualmente no preview real (Bloco 13): badge Especial/Comum, borda âmbar de equipamento, pulse do estado vazio (Bloco Inventário.1) todos corretos. Teste cross-screen com estado compartilhado confirmado: equipar item de slot ocupado troca corretamente entre Inventário e Personagem (`equipItem_occupiedSlot_returnsPreviousItemToInventory` replicado visualmente) | React: `InventoryScreen.tsx` + `useInventory` (`equipItem`, `unequipItem`, `sellItem`, `discardItem`). `inspectItem`/`inspectingItem` ficam para a tela. Hover states do React (`hover:border-amber-400`, `group-hover:scale-105`) tratados como N/A por design de plataforma (touch, não mouse) |
| Títulos | Concluído (lógica + UI) — **atualizado 26/08, Bloco C**: `TitleSelectorScreen.kt` fechado (ver linha própria "Seletor de Títulos" abaixo, seção 7) | Validada (lógica pura: `TitleLogic.kt`, Bloco 7) — 2 bugs do React corrigidos deliberadamente (`buyTitle` descontava ouro em título já possuído; `equipTitle` não checava posse) | Parcial — `TitleEquipModal` com screenshot em modo record, ainda sem inspeção visual humana | React: `TitleEquipModal.tsx` (Bloco 12) + `useTitles`. **Correção de achado (26/08)**: a nota anterior aqui dizia que `TitleSelector.tsx` "não é usado por nenhum componente conhecido — fora de escopo" — **errada**. `TitleSelector.tsx` é o componente real por trás da rota de navegação `activeTab === 'titles'` (aba "TÍTULOS" do menu inferior), confirmado por auditoria de `App.tsx` no Bloco C. É uma tela cheia (lista plana, contador "Seus Títulos Desbloqueados (N)"), diferente de `TitleEquipModal.tsx` (modal categorizado por raridade, chamado de outro lugar) — os dois coexistem na fonte, não são o mesmo componente. Ver seção 7 pro detalhe do porte |
| Catálogo de Títulos (`TitleCatalog.kt`, 47 itens) | Validado | Validada — conferido campo a campo contra `titleCatalog.ts` real (Bloco 8), 6 testes cobrindo contagem, unicidade de ID, preços e `checkUnlocked` | N/A (cores/estilo portados, sem UI que os consuma ainda) | Ver nota de risco abaixo sobre o incidente de fabricação de dados |
| Level up (detector + popup) | Concluído (lógica + build); visual pendente | Validada — `LevelUpLogic.kt`, 14 testes. Validado 2x: local (RED→GREEN isolado) e depois via XML bruto real do AI Studio (`tests="14" failures="0" errors="0" skipped="0"`, nomes conferidos 1:1 contra os 14 `fun` do arquivo) | Não inspecionada — `LevelUpOverlay.kt` compila (`BUILD SUCCESSFUL`) e o Roborazzi gerou `level_up_overlay_combat.png`/`level_up_overlay_skill.png` com sucesso, mas os PNGs ainda não foram vistos (Bruno optou por não subir nesta sessão) | React: `useLevelUp` — arquiteturalmente diferente dos outros hooks (detector reativo via `useRef`/`useEffect`). Portado como comparação de estado no funil único de mutação (`saveCharacterState`), com flag `suppressLevelUpDetection` equivalente ao `isImportingRef` da fonte, já aplicada no import de save (`importSaveFromPastedText`). **Decisão fechada (29/08)**: campo `charClass` do evento `Combat` é código morto também na fonte (nunca lido no JSX) — mantido no model Kotlin só por paridade estrutural de tipo, sem lógica de apresentação em cima dele; confirmado por segunda opinião externa (ChatGPT) batendo com o achado original já auditado. Divergência sinalizada: botão físico de voltar do Android fecha o popup (fonte não trata teclado nele) |

## 5. Módulo Skills

| Item | Status | Fidelidade Lógica | Fidelidade Visual | Notas |
|---|---|---|---|---|
| Lógica pura (`SkillLogic.kt`) | Validado | Validada — 17 testes, incluindo o caso sensível de renomear para o mesmo nome (exclusão `sIdx != idx`) e índice inválido (`SkillError.InvalidIndex`, corrigido no Bloco 2.1) | N/A | Bloco 2 + 2.1 |
| Tela de skills (UI) | Validado | Validada (usa `SkillLogic.requiredXpForLevel`, `onDeleteSkill` sem lógica de confirmação) | Validado — confirmado visualmente no preview real, incluindo `applyPrestige` funcionando ponta a ponta (Bloco 6.1) | React: `SkillsScreen.tsx` — consumirá `SkillLogic.kt`. Bloco 5 + 5.1 + 6 + 6.1 |
| Seletor de skill ativa (UI) | Validado | Validada (usa `SkillLogic.requiredXpForLevel`, sem duplicar fórmula) | Parcial — screenshot em modo record, glow do card ativo corrigido no Bloco 4.1, ainda sem inspeção visual humana (não plugado na MainActivity ainda, só a Skills Screen foi) | React: `SkillSelectorModal.tsx` (`/src/components/`) — modal de cards, não `<select>` nativo. Bloco 4 + 4.1 |
| Skills customizadas (tags, rename, delete, prestige) | Validado (lógica) / Não iniciado (UI) | Validada via `SkillLogic.kt` | N/A | React: `useSkills.ts` |

## 6. Módulo Missões

| Item | Status | Fidelidade Lógica | Fidelidade Visual | Notas |
|---|---|---|---|---|
| Modelos de dados (Habit/Daily/Todo/ChecklistItem + tabela de recompensa compartilhada) | **Completo, corrigido 20/08** (achado crítico de tipo duplicado, ver nota abaixo) | Fiel — `getDifficultyRewards` idêntica à fonte, `up`/`down` preservados no modelo com KDoc explicando o gate ficar só na UI | N/A (sem UI ainda) | Tipos reais em `CharacterModels.kt` (`model.Habit/Daily/Todo/Difficulty`, Bloco A). `QuestTypes.kt` mantém só `DifficultyRewards`/`getDifficultyRewards` — cópia duplicada dos tipos (`logic.quests.*`) deletada em 20/08 |
| Habits (lógica de trigger up/down) | **Lógica completa (Blocos B/B2/B3), tipo corrigido (20/08), trigger wireado ao ViewModel (Bloco F) + CRUD completo — add/edit/delete (Bloco G) + UI (Bloco H1, fechado — visual NÃO validado, ver linha "UI" abaixo)** | Fiel — combatXP/streak/HP (achados #1-4) + morte global fora de sessão de foco (achado #5), todos corrigidos e testados. `HabitLogic.trigger()` agora chamado a partir do ViewModel (Bloco F) | **NÃO validado** — ver linha "UI (telas de Habits/Dailies/Todos)" | `HabitLogic.kt`, agora importa `model.Habit`/`model.Difficulty` corretamente (antes operava sobre tipo morto `logic.quests.Habit`, ver nota crítica abaixo). `isPlayerDead` em `CharacterState`/`CharacterSummary`, `respawnHero()` generalizado. 14/14 (`HabitLogicTest`, reescrito p/ tipo real). Bloco F adicionou `triggerHabit()` ao ViewModel + 3 testes novos (`triggerHabit_up_appliesRewardsAndUpdatesHabit`, `triggerHabit_down_appliesDamageAndUpdatesHabit`, `triggerHabit_unknownId_noOp`, todos PASSED, diff real conferido). Bloco G adicionou `addHabit`/`editHabit`/`deleteHabit` (CRUD puro, sem side-effect de gold/xp/hp) direto no `HeroLogViewModel.kt` — ver `DEV_LOG_ANDROID.md` Blocos F e G. Suíte completa pós-Bloco G: 315/315 |
| Dailies (toggle completo/incompleto) | **Lógica completa (Bloco C), tipo corrigido (20/08), toggle wireado ao ViewModel (Bloco F) + CRUD completo (Bloco G) + UI (Bloco H2, fechado — visual NÃO validado, ver linha "UI" abaixo)** | Fiel — combatXP 100% (confirmado, não 40%), HP só restaura no level-up ao completar, `value` sem clamp vs `streak` clampado em 0 (assimetrias reais preservadas). `DailyLogic.toggle()` agora chamado a partir do ViewModel (Bloco F) | **NÃO validado** — ver linha "UI (telas de Habits/Dailies/Todos)" | `DailyLogic.kt`, agora importa `model.Daily`/`model.ChecklistItem`/`model.Difficulty` corretamente. 9/9 testes XML bruto (reescrito p/ tipo real, ganhou 1 teste a mais: `combatXpAppliesFullAmount_notFortyPercent`). Bloco F adicionou `toggleDaily()` ao ViewModel + teste `toggleDaily_completesAndUncompletes_symmetric` (PASSED). Bloco G adicionou `addDaily`/`editDaily`/`deleteDaily`/`toggleDailyChecklistItem` (CRUD puro). Bloco H2 adicionou `DailiesScreen.kt` (8 testes novos, todos nominais PASSED) + extraiu `DifficultyBadge` pra componente compartilhado — ver `DEV_LOG_ANDROID.md` Blocos F, G e H2. Suíte completa pós-Bloco H2: 331/331 |
| Todos (toggle completo/incompleto) | **Lógica completa (Bloco C), tipo corrigido (20/08), toggle wireado ao ViewModel (Bloco F) + CRUD completo — add/edit/delete/toggle de item de checklist (Bloco G) — SEM UI ainda (pendência Bloco H)** | Fiel — mesmo padrão de Dailies, sem streak/value, `completedAt` ISO-8601 via `Instant`. `TodoLogic.toggle()` agora chamado a partir do ViewModel (Bloco F) | N/A (sem UI ainda) | `TodoLogic.kt`, agora importa `model.Todo`/`model.Difficulty` corretamente. 8/8 testes XML bruto (reescrito p/ tipo real). Bloco F adicionou `toggleTodo()` ao ViewModel + teste `toggleTodo_completesAndSetsCompletedAt` (PASSED, confirma `completedAt` setado — diff real conferido). Bloco G adicionou `addTodo`/`editTodo`/`deleteTodo`/`toggleTodoChecklistItem` (CRUD puro, sem parâmetro de streak — `Todo` não tem esse campo) — ver `DEV_LOG_ANDROID.md` Blocos F e G. Suíte completa pós-Bloco G: 315/315 |
| TodoDecay + agendamento de Dailies | **Completo (Bloco D fechado, 19/08)** — NÃO afetado pelo achado crítico de tipo duplicado, já usava `model.*` desde a criação | Fiel — `TodoDecayLogic.getTodoDecayValue`: `floor(diffDays/2)*-1`, piso -20, confirmado linha a linha contra `todoDecay.ts` (função pura, só usada em tempo de exibição, sem persistência — confirmado via grep no repo React). `DailySchedulingLogic.wasDailyScheduledForDate`: `Daily` com cálculo real via `createdAt+every`; `Weekly`/`Monthly` = fallback `true` incondicional, **fidelidade estrita e deliberada à limitação real da fonte** — correção de Weekly/Monthly é melhoria pós-paridade, fora de escopo | N/A (sem UI ainda) | `TodoDecayLogic.kt` (10/10 testes) + `DailySchedulingLogic.kt` (7/7 testes), XML bruto conferido. `parseDate()`/`truncateToMidnight()` consolidados em `DateParsingUtils.kt` compartilhado (20/08, correção de escopo do Bloco E). `RepeatInterval` (enum usado) auditado à parte — confirmado pré-existente do Bloco A, `@SerialName` fiel |
| Rollover de dia | **Completo (Bloco E fechado, 20/08)** + **Relatório Diário (Bloco DR, 08/09)** | Fiel + 2 decisões conscientes de desenho (não fidelidade estrita): consumo único de `StreakShield` por rollover (corrige bug real da fonte, que permitia proteção dupla de graça) e proteção tripla estendida (streak global + dano de HP + `Daily.streak` individual, decisão de Bruno — na fonte `Daily.streak` sempre zerava incondicionalmente). Delay de 100ms antes de `isPlayerDead=true` mantido (equivalente ao `setTimeout` da fonte). **Relatório Diário**: `DailyReportData` (7 campos, idêntico ao React `App.tsx:419-427`), calculado no `init` do ViewModel antes de persistir rollover (fiel a `App.tsx:1056-1097`). Modal fullscreen com `AnimatedVisibility`, `Modifier.blur(12.dp)` (guard API 31+), sparkle particles, bounce animation — fiel a `App.tsx:4319-4460` | **Relatório Diário: Fidelidade Visual NÃO validada** (sem print real em device); Lógica validada via 7 screenshot tests Roborazzi (baseline PNGs gerados) | `RolloverLogic.kt` + `DailyReportData.kt` (model) + `DailyReportModal.kt` (UI) + wiring `MainActivity.kt`. 12/12 `RolloverLogicTest` + 7/7 `DailyReportModalScreenshotTest` + 491/491 suíte completa. Pendências registradas: (a) tornar `StreakShield` mais raro/difícil de obter; (b) migrar armazenamento do shield de `inventory: List<Item>` genérico para contador dedicado (`streakShieldCount`, estilo Duolingo) — ambas antes do port ser considerado "terminado" |
| QuestProgress wiring (ViewModel) | `HabitLogic.trigger()`/`DailyLogic.toggle()`/`TodoLogic.toggle()` wireados ao ViewModel — Bloco F fechado (registrado no `DEV_LOG_ANDROID.md`, diff real + 5/5 testes PASSED + suíte 304/304 conferidos). QuestProgress da Guilda/Diárias (`QuestLogic`/`QuestCatalog`) **agora também wireado — Bloco H+I, 24/08**: `QuestApplyLogic.claimQuestReward` (lógica pura de resgate) + `dailyQuests()`/`guildQuestsProcessed()`/`claimQuestReward()` no `HeroLogViewModel.kt`. Diff conferido literal contra commits `13d7132`/`453aa11` do `HeroLog-Android-2` | Ver linha "Contratos (UI)" abaixo | Suíte em 351 testes (`QuestApplyLogicTest` + `QuestsViewModelIntegrationTest` novos) | Achado crítico de 20/08 motivou o Bloco F. `triggerHabit`/`toggleDaily`/`toggleTodo`/`claimQuestReward` no `HeroLogViewModel.kt`, cada um busca o item pela lista do estado atual, chama a função de lógica correspondente e persiste o `updatedState` retornado por ela (não o `current` original) |
| Contratos (UI — `QuestsScreen.kt`) | **Validado (24/08) + S5 paridade visual (09/09)** — fontes migradas (`Cinzel`/`JetBrainsMono`), header sentence case, label progresso 10sp | Fiel — título/ícone/sub-abas/legendas/paleta/textos rodapé/bolinha pulsante. S5: `Cinzel` no card título, `JetBrainsMono` no progresso, "Contratos" sentence case | **Parcialmente validada (S5)** — inspeção device pendente | React: `QuestsTab.tsx`. S5: 8 alterações cosméticas |
| CRUD de Habits/Dailies/Todos (add/edit/delete + toggle de item de checklist) | **Completo (Bloco G fechado, 20/08)** | Fiel — CRUD puro no `HeroLogViewModel.kt`, sem chamar `HabitLogic`/`DailyLogic`/`TodoLogic`, sem side-effect de gold/xp/hp (mesmo padrão de `discardItem`/`sellItem`). `editHabit`/`editDaily`/`editTodo` recebem o objeto já editado pronto (decisão de quais campos são editáveis fica na UI, Bloco H) | N/A (sem UI ainda) | 11 funções novas em `HeroLogViewModel.kt`: `addHabit`/`editHabit`/`deleteHabit`, `addDaily`/`editDaily`/`deleteDaily`/`toggleDailyChecklistItem`, `addTodo`/`editTodo`/`deleteTodo`/`toggleTodoChecklistItem`. 11/11 testes novos (XML bruto conferido) + suíte completa 315/315 (304 + 11), sem regressão. Diff real conferido: só imports novos + as 11 funções tocados. 2 arquivos, 509 linhas |
| UI (telas de Habits/Dailies/Todos) | **H1/H2/H3 fechados (22/08). Sprint paridade visual S5 (09/09): fontes (`Cinzel`/`Inter`/`JetBrainsMono`), labels `Zinc300`, submit bordered S10, copy/placeholders alinhados ao React** | N/A (componente apresentacional) | **Parcialmente validada (S5)** — tipografia e cores de labels fiéis ao React; placeholders/copy confirmados. **PENDENTE: inspeção device real** (375×667 e 390×844) | Sprint S5 (09/09): ~90 alterações cosméticas em 3 arquivos. `FontFamily.Serif`→`Cinzel`, `Monospace`→`JetBrainsMono`, `SansSerif`→`Inter`. Labels: `Amber300`/`0xB3FEF3C7`→`Zinc300`, 10sp, sentence case. Submit: S10 bordered. Placeholders alinhados. Histórico: H1 3 desvios, H2 4 desvios, H3 3 achados — todos já registrados |
| Wiring das telas H1/H2/H3 na navegação real (`MainActivity`) | **Completo (22/08)** — `HabitsScreen`/`DailiesScreen`/`TodosScreen`/`QuestsScreen`/`HistoryScreen` todas chamadas com callbacks reais. QuestFab wireado | N/A (wiring puro) | N/A | Sub-abas Missões (quests/history) portadas e wired |
| Guild Quests + Proclamações diárias (20 quests, rotação LCG determinística) | **Já existe no projeto, de sessão anterior — status "Não iniciado" desta linha estava desatualizado** | Não auditado nesta sessão contra a fonte (só a existência foi confirmada) | Não auditado | `QuestLogic.kt` + `QuestCatalog.kt`, já usados por `HeroLogInitialState.kt`. Achado ao investigar arquivos "extras" durante auditoria do Bloco C — recomendação: auditar formalmente contra a fonte numa sessão futura, mesmo padrão rigoroso dos outros módulos |
| Histórico / Crônicas Diárias (UI — `HistoryScreen.kt`) | **Validado (24/08) + S5 paridade visual (09/09)** — fontes migradas, header sentence case | Fiel — dois cards reais, textos literais, emojis. S5: `Cinzel` em todos os textos serif, `JetBrainsMono` em date/XP/GP/duration, "Crônicas Diárias" sentence case | **Parcialmente validada (S5)** — inspeção device pendente | React: `HistoryTab.tsx`. S5: 18 alterações cosméticas. 3 divergências cosméticas anteriores aceitas conscientemente |

### ✅ Nota histórica — bugs confirmados em `HabitLogic.kt` (Bloco B), todos corrigidos (Blocos B2 + B3)

Ao investigar (18/08) o achado do combatXP do módulo Foco, Claude pediu a Bruno o trecho REAL do
React (`useHabits.ts`, `handleTriggerHabit`) — não disponível ao AI Studio, que não tem acesso ao
repo React. Comparando com o `HabitLogic.kt` já fechado, confirmaram-se **5 divergências**:

1. **combatXP no Up**: fonte real é `combatXP + finalXP` (100%). Código atual aplicava
   `*0.4` — generalização indevida por analogia ao Foco (que de fato usa 40%, mas Habit não).
   **✅ Corrigido no Bloco B2 (19/08)**.
2. **streak no Up**: fonte real incrementa **sempre**, sem checar `lastTriggeredDate`. Código
   atual só incrementava 1x/dia — resposta anterior de Bruno sobre isso também estava errada
   (reconstruída de memória, não fonte literal). **✅ Corrigido no Bloco B2 (19/08)**.
3. **HP no Up**: fonte real restaura `hp=maxHp` em level-up (igual Daily/Todo/Foco). Código
   atual não tinha essa restauração — feature inteira faltando. **✅ Corrigido no Bloco B2
   (19/08)**.
4. **streak no Down**: fonte real é `max(0, streak-1)` (decrementa 1). Código atual resetava pra
   `0` sempre — mesma categoria do #2, resposta anterior também incorreta. **✅ Corrigido no
   Bloco B2 (19/08)**.
5. **Morte por HP=0 fora de sessão de foco**: fonte real tem mecanismo global (`isPlayerDead`
   como `useState` em `App.tsx`/`useFocusSession.ts`, mesmo `respawnHero()` reaproveitado tanto
   por Wilderness/Foco quanto por Habit Down, sem ramificação — reconfirmado literalmente 19/08).
   Android hoje: `isPlayerDead` existe só dentro de `FocusSessionState`, `CharacterState` não tem
   campo de morte global; `respawnHero()` (`HeroLogViewModel.kt`) está amarrado a
   `FocusSessionState` (reseta a sessão de foco) e não restaura `hp` — confirmado via corpo
   literal da função + `resolveRespawn()`/`CognitiveDeathLogic.kt`. **Bug real também presente na
   fonte React atual** (não é regressão do port): `respawnHero()` do React não restaura `hp`,
   apesar da mensagem de log dizer que restaura — correção (`hp: prev.maxHp`) já identificada por
   Bruno, ainda não mergeada lá. **✅ Corrigido no Bloco B3 (19/08)**: `isPlayerDead` movido de
   `FocusSessionState` para `CharacterState`/`CharacterSummary` (`CharacterModels.kt`);
   `respawnHero()` (`HeroLogViewModel.kt`) generalizado — restaura `hp = maxHp` e
   `isPlayerDead = false` sempre, e só reseta `FocusSessionState()` quando havia sessão de foco
   de fato ativa/pausada/em grace/com config setada (antes era incondicional). `HabitLogic.kt`
   Down seta `isPlayerDead = true` no `CharacterState` ao zerar hp, sem chamar `respawnHero()`
   diretamente — respawn continua ação do jogador, fiel ao fluxo real.

**Lição de metodologia**: o Bloco B foi fechado aceitando a alegação do AI Studio de que Habit Up
"segue exatamente o mesmo padrão do FocusApplyLogic" sem pedir o trecho literal da fonte React
correspondente ao Habit — só do Foco. Generalização por analogia entre sistemas do jogo (Foco,
Habit, Daily, Todo) não é segura mesmo quando aparentam a mesma forma; cada um precisa da própria
verificação literal, mesmo que outro sistema já tenha sido confirmado correto. Reforçada no B3:
a mesma regra vale para infraestrutura (não presumir que "morte global" seria uma feature nova a
desenhar do zero sem antes confirmar, literalmente, que os dois gatilhos da fonte real já
compartilham o mesmo mecanismo).

### 🔴 Nota crítica — dois sistemas de tipos paralelos e desconectados (achado 20/08, corrigido no mesmo dia)

Durante a auditoria do Bloco E, o AI Studio entregou resumo em prosa em vez do diff pedido — a
suíte completa de testes revelou que `HabitLogicTest.kt` tinha sido silenciosamente reescrito.
Investigação direta via `bash_tool`/`git clone` no repositório real (`HeroLog-Android-2`, novo
repo criado por Bruno como workaround do bug de integração GitHub↔AI Studio) confirmou: o
projeto tinha DOIS conjuntos de tipos `Habit`/`Daily`/`Todo`/`Difficulty` — os reais
(`model.*`, `@Serializable`, usados de fato por `CharacterState`) e uma cópia paralela
(`logic.quests.*`, em `QuestTypes.kt`, nunca usada pelo estado real). `HabitLogic.kt`,
`DailyLogic.kt`, `TodoLogic.kt` (Blocos A/B/B2/B3/C, todos fechados e "auditados" em múltiplas
sessões) não tinham import explícito de `Habit`/`Daily`/`Todo` — resolução de pacote do Kotlin
os ligou silenciosamente aos tipos LOCAIS (mortos), nunca aos reais. Grep no repositório inteiro
confirmou **zero chamada** a essas três classes em qualquer lugar de produção — toda a lógica de
Habits/Dailies/Todos fechada até aqui nunca esteve de fato conectada ao app.

Corrigido no mesmo dia: tipos duplicados deletados de `QuestTypes.kt`, imports explícitos
adicionados nos 3 arquivos de lógica (zero mudança de lógica de negócio), testes reescritos para
o tipo real. Suíte completa: 299 testes, 0 falhas, nenhuma regressão em nenhum outro módulo.

**Isso NÃO invalida a lógica de negócio auditada (fórmulas de XP/gold/streak/HP/dano continuam
corretas e testadas)** — invalidava só a CONEXÃO ao app real, agora corrigida quanto ao tipo. O
wiring de chamada de fato (`HabitLogic.trigger()`/`DailyLogic.toggle()`/`TodoLogic.toggle()`
sendo chamadas por algum lugar do ViewModel/UI) continua pendente — é o Bloco F, agora com
prioridade maior por causa deste achado.

**Lição de metodologia**: testes 100% verdes e diffs corretos em cada bloco individual não
garantem que o código está conectado ao app — a auditoria bloco a bloco olhava lógica interna,
não a cadeia de wiring/tipo ponta a ponta. A suspeita só surgiu porque o AI Studio entregou
prosa em vez de diff, forçando auditoria direta no repositório real. Reforça: nunca aceitar
resumo em prosa (Armadilha #9) — e sugere adicionar checagem periódica de "grep de call sites
reais em produção" para módulos fechados há muito tempo sem revisão.

## 7. Módulo Reino

| Item | Status | Fidelidade Lógica | Fidelidade Visual | Notas |
|---|---|---|---|---|
| Loja | Em andamento — sub-aba "Títulos" **fechada (26/08, Bloco F)**, ver linha "Loja de títulos" abaixo | Validado (`ShopLogic.kt`, 10/10 testes) | Não validado (sem print lado a lado) | React: `ShopTab.tsx` + `useShop` (`buyGoblinShopItem`). `ShopScreen.kt` (Bloco E) portado: wrapper com sub-toggle Itens/Títulos (`shopSubTab`, estado local efêmero, mesmo tratamento do `questsSubTab`), lista de itens, botão de compra. Gate de StreakShield duplicado replicado fielmente só na UI (não no `ShopLogic`), igual à fonte. |
| Loja de títulos | **Fechado (26/08, Bloco F)** — `TitleShopScreen.kt` colado no caminho real (`ui/kingdom/`), patches aplicados em `ShopScreen.kt`/`HeroLogViewModel.kt`/`MainActivity.kt` (commit `125aade`). Build real confirmado via XML cru: `TitleLogicTest` 8/8, `ShopLogicTest` 10/10, `HeatmapLogicTest` 17/17, `AchievementLogicTest` 3/3 — 0 falhas/erros/skips, nenhuma regressão | Sem lógica nova — reusa `TITLE_CATALOG` (Bloco 8) e `TitleLogic.buyTitle`/`claimAchievementTitle` (Bloco 7, já com os 2 bugs do React corrigidos). Duas funções novas em `HeroLogViewModel.kt` (`buyTitle`/`claimAchievementTitle`) — não existiam antes, só `equipTitle` tinha wiring | **Não validado formalmente** (sem print lado a lado) | React: `TitleShop.tsx` (commit `52822c2`, mesmo hash já auditado nos Blocos A/B/D/E/C — sem drift). **Não confundir com `TitleSelectorScreen.kt`** (Bloco C, rota `titles` da navegação inferior — serve pra *equipar*, não comprar). Grid sem `LazyVerticalGrid` (linhas via `chunked`) — tela vive dentro de `verticalScroll`, aninhar lazy grid quebraria em runtime. Preço formatado `pt-BR` (`NumberFormat`, ex. "1.500 GP") — suposição de localidade consistente com o resto do app. Ícones: `ShoppingBag→ShoppingCart`, `Award→WorkspacePremium`, `Sparkles→AutoAwesome` (mesmas escolhas do Bloco C) |
| Heatmap | Em andamento | Validado (`HeatmapLogic.kt`, 17/17 testes) | Não validado (sem print lado a lado) | React: `HeatmapTab.tsx` — atenção à lógica de `startI` condicional (fix de v1.1.7), replicada e coberta por teste. `HeatmapScreen.kt` (Bloco E) portado: card de consistência, grid, toggle 3/6 meses, badge de streak, legenda, cards de estatística. **Adaptação consciente**: tooltip de hover da fonte (`onMouseEnter`/`onTouchStart` + posicionamento via `getBoundingClientRect`) virou tap-to-select (toque na célula mostra detalhe numa linha fixa abaixo da grade) — não existe hover real em touch e posicionamento flutuante seria frágil em Compose. **Simplificações visuais conscientes** (mesmo precedente do `GuideScreen.kt`): sem corner brackets do card de consistência, sem glow/shadow customizado nas células, sem animação de pulso no rótulo "ATUAL". Estrutura/cores/textos fiéis — revisitar essas 3 ornamentações na validação visual formal se necessário. |
| Seletor de Títulos (rota `titles`) | **Fechado (26/08, Bloco C)** — `TitleSelectorScreen.kt` escrito, wiring em `MainActivity.kt` (`"titles" -> TitleSelectorScreen(...)`) usando `viewModel.equipTitle(titleId)` já existente (aceita `null` pra desequipar). Build real confirmado (BUILD GREEN, `AchievementLogicTest` 3/3 + `TitleLogicTest` 8/8, 0 falhas — sem lógica nova neste bloco, só confirma ausência de regressão) | Sem lógica nova — reusa `TITLE_CATALOG` (Bloco 8) e `TitleLogic.equipTitle` (Bloco 7, já com o bug de posse corrigido) | **Não validado formalmente** (sem print lado a lado) | React: `TitleSelector.tsx` (`src/modules/character/`, não `kingdom/` — mapeado pra `ui/kingdom/` no Android por ser consumido pela navegação do módulo Reino, mesma decisão de organização já aplicada a outras telas). Ver correção do achado na linha "Títulos" acima (seção 4) — este componente tinha sido erroneamente marcado como código morto numa auditoria anterior. Bug corrigido durante o porte: badge "ATIVO" do card usava `.padding()` negativo pra replicar o `-top-2 -right-2` do CSS (`padding` não aceita valor negativo no Compose, quebraria em runtime) — trocado por `.offset()`. |
| Estatísticas | **Fechado (26/08, Bloco C)** — `StatsScreen.kt` escrito, wiring em `MainActivity.kt` (`"stats" -> StatsScreen(state = state)`). Build real confirmado (BUILD GREEN) | Sem lógica nova — mapeamento direto de campos de `CharacterState` + `averageSessionLength = totalMinutes / totalSessions` com guarda de divisão por zero, igual à fonte | **Não validado formalmente** (sem print lado a lado) | React: `StatsTab.tsx`, 87L. Achado: `bg-amber-550/[0.02]` no card "Moedas Acumuladas" da fonte é classe Tailwind inválida (`amber-550` não existe na escala padrão) — não renderiza fundo nenhum no React real, mesma categoria do achado `stone-850` do Bloco D. Replicado como fundo transparente, não "consertado" pra `amber-500`. |
| Conquistas | **Fechado (26/08, Bloco C)** — `AchievementsScreen.kt` escrito, wiring em `MainActivity.kt` (`"achievements" -> AchievementsScreen(state = state)`). Build real confirmado (BUILD GREEN) | Sem lógica nova — `AchievementCatalog.kt`/`AchievementLogic.isUnlocked` já validados no Bloco 16, conferidos aqui como confirmação cruzada (batem exatamente com a fonte atual) | **Não validado formalmente** (sem print lado a lado) | React: `AchievementsTab.tsx`, 106L. |
| Guia | **Fechado (24/08, Bloco D)** — `GuideScreen.kt` escrito, colado no caminho real, wiring em `MainActivity.kt` (`"guide" -> GuideScreen()`) e build real confirmado (BUILD GREEN, suíte completa passando: `HeatmapLogicTest` 17/17, `HeroLogViewModelTest` 52/52, 0 falhas/erros/skips — nenhuma regressão) | Conteúdo 100% estático, texto colado literal a partir da fonte real (nenhuma lógica a validar) — auditado contra `GuideTab.tsx` (commit `52822c2`) + cabeçalho do wrapper em `App.tsx` (`activeTab === 'guide'`, título "TUTORIAL", ícone `HelpCircle` em `champagne-400`) | **Não validado formalmente** (sem print lado a lado com o React ainda) | React: `GuideTab.tsx`, 183L. Mapeamento de ícones lucide→Material por aproximação semântica (`Swords→SportsMartialArts`, `Skull→Warning`, reaproveitando a mesma escolha já aceita em `IncursionModeModal.kt` pro mesmo conceito de Modo Selvagem) — `SportsMartialArts` e `Restore` confirmados por build real neste bloco. `border-stone-850` da fonte (classe inválida, não definida em `index.css`, já não renderizava nada no React real) aproximado como `Stone900`. **Achado de processo**: primeira entrega tinha um `*/` acidental dentro do próprio texto do KDoc (`quest-*/ice-*/champagne-*`) que fechava o comentário antes da hora — corrigido por Bruno na colagem, registrado aqui pra não repetir (evitar `*/` literal em comentários Kotlin ao citar padrões com barra) |
| Registros (Logs) | **Fechado (29/08, Bloco Logs)** — `LogEntry.kt` (model, não persistido) + `LogsScreen.kt` escritos, `HeroLogViewModel.kt`: `_systemLogs` (`StateFlow`, cap 51) + `addSystemLog()` privada, wiring em `MainActivity.kt` (`"logs" -> LogsScreen(...)`, trocou `PlaceholderScreen`). Build real confirmado via XML cru — 14 suítes, 0 falhas/erros/skips em nenhuma (regressão completa checada, não só as 3 suítes novas: `HeroLogViewModelTest` 55/55 com 3 testes novos, `LogsScreenScreenshotTest` 2/2) | Sem lógica de jogo nova — 62 call-sites da fonte (`addSystemLog`) mapeados: 20 em Missões (1:1), combat level-up unificado (hook único em `saveCharacterState`, desvio deliberado das 4 variantes da fonte), cascata de `confirmFocusSession` (loot/título raro/quebra de equipamento/masmorra/skill level-up/conquistas, via diff de estado + campos de `calc`), rollover (escudo/dano, via `RolloverResult.missedCount`/`shieldConsumed` + diff de `hp`) | Validado por 2 screenshots reais inspecionadas (não só nome de arquivo) — texto/cor/ordem batem com a fonte | React: `App.tsx`, bloco `activeTab === 'logs'` + função `addSystemLog` (linha ~1223). Ícone/cor placeholder (`Icons.Filled.Menu`+`Amber500` no lugar de `Scroll`+`champagne-500` — `Color.kt` não tem token `Champagne*` neste commit, trocar se/quando existir). `survive_wilderness` mantém texto especial da fonte, demais conquistas usam texto genérico com id em caixa alta (desvio deliberado). Não existe mensagem de "missão completa" pro fluxo manual de `confirmFocusSession` na fonte atual — não foi inventada. Pendência explícita: validação visual formal (print lado a lado) ainda não feita, mesma pendência do resto do módulo Reino. |
| Diário de Bordo (Logs) | Ver linha "Histórico / Crônicas Diárias (UI — `HistoryScreen.kt`)" na seção 6 (Módulo Missões) — **linha duplicada removida em 27/08**: esta era uma cópia órfã que nunca foi atualizada depois do Bloco J (24/08) fechar o item na seção correta. `history` é sub-aba de Missões, não do Reino; ficou aqui por engano desde antes do fatiamento em blocos | N/A | N/A | Ver seção 6 |

## 8. Lógica de jogo transversal (boas candidatas a teste unitário puro)

| Item | Status | Fidelidade Lógica | Fidelidade Visual | Notas |
|---|---|---|---|---|
| Cálculo de XP / level up | Validado (skill e combate) | Fórmula de skill: `level * 80` (`SkillLogic`). Fórmula de combate: `combatLevel * 100` (`CombatLogic`, Bloco 8) — **são fórmulas diferentes, não intercambiáveis** | N/A | Ambas confirmadas contra o código-fonte real |
| Loot drop (redesenhado em v1.1.8 no React) | Validado (lógica) | Validada — `LootTable.kt` (14 itens, escrito diretamente por Claude) + `LootConfig.kt` (`calculateLootChance`, faixas por minutos/masmorra + multiplicador por título + cap 0.95%) + rolagem em `FocusRewardsLogic.kt`, Bloco 17. Versão pós-redesign confirmada (a única versão que já vimos na fonte) | N/A | Portado junto com o cálculo de recompensas (Bloco 17), não isolado — loot é só uma das saídas de `FocusRewardsLogic.calculate()` |
| Dungeon mode (bônus de +2.500 GP) | Validado (lógica) | Validada — `FocusRewardsLogic.kt` calcula `dungeonClearGoldBonus` na 4ª sessão consecutiva, `FocusApplyLogic.kt` soma ao `gold`/`totalGoldEarned` de fato (Bloco 17). Testes cobrindo aplicação (sessão 4) e não-aplicação (sessão 3) do bônus. Confirmado que o bug antigo (anunciado mas não aplicado) já estava corrigido na fonte React atual, e a correção foi preservada no port | N/A | React teve bug de bônus anunciado mas não aplicado, corrigido em ciclo recente — garantido que o port não reintroduziu esse bug (Bloco 17) |
| Streak / bestStreak | Validado (lógica) | Validada — `FocusApplyLogic.kt` incrementa streak quando `lastStudyDate != todayString` (formato `toDateStringJs`, mesmo do LCG das quests), mantém quando já é o mesmo dia; `bestStreak = max(newStreak, bestStreak)`. Testado com os dois casos (dia novo vs. mesmo dia), Bloco 17.1 | N/A | Nota desatualizada corrigida (19/08): `triggerCognitiveDeath`/`respawnHero` já portados desde o Bloco 37 (11-16/08) e generalizados no Bloco B3 (19/08) — mas reset de streak a zero por morte especificamente ainda não confirmado contra a fonte real, permanece candidato a auditoria futura |
| Daily quests (LCG seeding determinístico) | Validado (lógica) | Validada — `QuestLogic.kt`/`QuestCatalog.kt`/`QuestModels.kt` (Bloco 14). Catálogo de 20 quests diárias + 3 guild quests escrito diretamente por Claude (não delegado ao AI Studio, mesma metodologia do `TitleCatalog.kt`). LCG e Fisher-Yates portados com prova numérica independente: `mandatoryTest_rotateDailyQuests_aug4_2026` valida contra resultado calculado via simulação Python fora do projeto, não só "roda sem erro". 14 testes novos (73 no total). Incidente registrado: primeira resposta do AI Studio trouxe resumo em prosa com contagem e nomes de teste fabricados (73 "PASSED" citados não batiam com a lista XML real); saída crua do XML via script Python pediu e resolveu — reforça a regra de nunca aceitar resumo em prosa como prova | N/A (sem UI ainda) | Fonte: `useGuildQuests.ts`. **Risco RESOLVIDO (29/08)**: `todaySessions`/`todaySkillNames` dependiam de `HistoryEntry.date` nascer no formato `dd/MM/yyyy` (via `todayLocalStr`, locale pt-BR) — confirmado por auditoria direta do código real (`FocusApplyLogic.kt:99`, `HistoryEntry.date = SimpleDateFormat("dd/MM/yyyy, HH:mm:ss", ...)`; `QuestLogic.kt:20-21`, `todaySessions` filtra com `it.date.startsWith(todayLocalStr())`) que a cadeia é coerente por construção — sem quebra silenciosa. Nota: `FocusApplyLogic` usa `toDateStringJs` (formato diferente) só pra `lastStudyDate`, não pra `HistoryEntry.date` — dois formatos deliberadamente distintos, documentado no próprio código. Não precisou mudar nada; fica só como risco de manutenção futura. Compatibilidade de claim legado (`legacyClaimFromToday`) portada fielmente, sem consumidor conhecido ainda |

---

## 9. Descobertas do `types.ts` real — fora de escopo do Bloco 1, não perder de vista

| Item | Status | Fidelidade Lógica | Fidelidade Visual | Notas |
|---|---|---|---|---|
| Registro de Conquistas (`Achievement.check`) | Validado (lógica) | Validada — `AchievementCatalog.kt`/`AchievementModels.kt` (catálogo, 8 itens, escrito diretamente por Claude) + `AchievementLogic.kt` (Bloco 16). 8 testes cobrindo contagem, unicidade de ID, spot-checks de `check()` nos limites exatos, e os dois lados do OR de `isUnlocked` (81 testes no total). Catálogo real: `first_quest`, `streak_3`, `streak_7`, `xp_1000`, `xp_10000`, `gp_1000`, `sessions_10`, `survive_wilderness` | N/A (sem UI ainda — `AchievementsTab.tsx` não portado) | `isUnlocked = state.achievements.includes(id) || check(state)` — OR complementar por design, ver histórico do Bloco 14/15/16 para o raciocínio completo |
| Sessão ativa em andamento (`ActiveSession`) | Não iniciado | N/A | N/A | Não é campo do `CharacterState` — equivale à chave separada `herolog_active_session` do localStorage no React. Precisa de estratégia própria no Android (candidato a `WorkManager`/foreground service para sobreviver a processo morto), não é só mais um campo no Room |
| Estado de modal de level-up (`CombatLevelUpType`/`SkillLevelUpType`/`LevelUpModalType`) | Concluído (lógica + build); visual pendente | Validada — `LevelUpEvent` (sealed class `Combat`/`Skill`) em `LevelUpLogic.kt`, ver linha própria na seção 4 (Módulo Personagem) | Não inspecionada — `LevelUpOverlay.kt` | Ver linha "Level up (detector + popup)" na seção 4 pro detalhe completo |
| `incrementModalCount`/`decrementModalCount` (`modalHelper.ts`) | Validado | Validada — `ModalCountRegistry` (increment/decrement/isAnyModalOpen), testado via `ModalCountRegistryTest` | N/A (não é elemento visual próprio) | Bloco 6.2. Código-fonte real obtido: contador global + classe CSS `modal-open`. Portado como semântica equivalente (pausa animações via `effectiveAlpha`), não mecânica idêntica |

---

## Legenda de risco (preenchimento livre conforme o port avança)

Usar esta seção para registrar módulos onde a fidelidade lógica é particularmente arriscada de
errar silenciosamente (ex: fórmulas com múltiplos casos-borda, condicionais dependentes de data/
hora, LCG com seed específico) — candidatos prioritários a teste unitário antes de UI.

- **`Daily.value` / `getScoreColor`**: `value` é o score que determina a cor de fundo/borda do
  card da diária (`DailiesTab.tsx`). Atualizado por `+1`/`-1` em: conclusão (`useDailies.ts`),
  desmarcação (`useDailies.ts`), e penalidade de virada de dia sem cumprimento (`App.tsx`,
  reset diário). Ao portar o módulo Missões, essa fórmula de três pontos de mutação precisa ser
  replicada exatamente — é candidata a teste unitário antes de qualquer UI.
- **`Todo` tem sistema de decaimento próprio** (`todoDecay.ts` no React, não auditado ainda) —
  diferente do `Daily.value`, que é incremento/decremento simples. Não presumir que os dois usam
  a mesma lógica quando chegar a vez do módulo Missões.
- **Masmorra e Wilderness são mutuamente exclusivas no React — mecanismo real confirmado por
  auditoria de fonte (JSX da tela de configuração pré-sessão, ainda não localizado como arquivo
  próprio, aparentemente dentro do próprio `App.tsx`).** Não é um par de checkboxes independentes
  com regra de exclusão aplicada depois — é um **segmented control de 3 estados** ("Padrão" /
  "Masmorra ⚔️" / "Selvagem 💀"), e cada botão seta os dois campos explicitamente no `onClick`:
  `Padrão → {isDungeonMode:false, isWildernessChecked:false}`, `Masmorra →
  {isDungeonMode:true, isWildernessChecked:false}`, `Selvagem → {isWildernessChecked:true,
  isDungeonMode:false}`. **Decisão de port recomendada**: modelar como enum de 3 estados (ex.
  `RaidMode { PADRAO, MASMORRA, SELVAGEM }`) em vez de dois booleans soltos — evita reintroduzir
  o estado impossível (`isDungeonMode=true` E `isWildernessChecked=true` simultâneos), que os
  dois booleans do React só evitam por disciplina de cada `onClick`, não por construção do tipo.
  `FocusModeScreen.tsx`/`FocusModeScreen.kt` continuam corretos como estão (apresentacionais,
  recebem os dois campos prontos, sem soberania sobre a regra). **Risco RESOLVIDO (Bloco 22)**:
  os dois checkboxes independentes do scaffolding de preview (Bloco 21) foram substituídos pelo
  `RaidModeSegmentedControl`/enum `RaidMode`, eliminando a combinação inválida por construção do
  tipo — e o reset de `isDungeonMode` ao fim de ciclo/abandono (Bloco 42) fecha a última lacuna
  dessa família (ver os dois itens de risco abaixo, descobertos no mesmo trecho de fonte).
- **NOVO RISCO — Cooldown de Masmorra (2h), não portado, ausente da seção 3/8 até agora.** O
  botão "Masmorra" no segmented control acima tem uma segunda condição de `disabled`:
  `dungeonCooldownRemaining > 0`, derivado de `lastDungeonClearedTime`
  (`Date.now() - lastDungeonClearedTime < 2 * 60 * 60 * 1000`). O texto do modal de ajuda
  confirma a regra por extenso: recarga de 2h após concluir a masmorra, não acumulável com
  Terra Selvagem. **Fontes ainda não obtidas**: onde `dungeonCooldownRemaining` é calculado
  (provavelmente `useMemo`/derivado a cada render) e onde/como `lastDungeonClearedTime` é
  persistido (é `useState` local — não sobrevive a reload do app? Ou vai para
  `CharacterState`/localStorage? Precisa de auditoria antes de portar). Sem essa regra, o port
  Android permitiria re-entrar em Masmorra imediatamente após concluir, divergindo do React.
- **RISCO REVISADO E EXPANDIDO — não é "cancelamento de bônus", é um subsistema inteiro de
  Morte Cognitiva ("Wilderness infraction system"), ausente por completo do port Android.**
  Fonte real auditada em `useFocusSession.ts` (09/08/2026). O texto do modal de ajuda
  ("minimizar a aba... cancela o bônus automaticamente") **subestima a regra real** — não é
  cosmético, a sessão inteira é perdida, não só o bônus de +25%.

  **Mecanismo real:**
  1. Listeners de `document.visibilitychange` + `window.blur`/`window.focus` (não só
     minimizar — também perda de foco pra outra janela do SO). Só age se
     `isRunning && isWildernessChecked && !isPaused && !isGraceActive && !isPlayerDead`
     (exclusivo de Selvagem; Masmorra não é afetada).
  2. Exceção: título `DEATH-PROOF` equipado converte a infração em pausa normal, sem
     penalidade.
  3. Caso contrário, abre um **grace period de 3 segundos** (`graceSecondsLeft`, decrementado
     por `setInterval`). Se o jogador voltar a tempo, cancela e segue normal
     (`handleReturnToFocusCap`).
  4. Se os 3s esgotarem → `triggerCognitiveDeath()`: sessão inteira perdida (nenhuma
     recompensa, `completeFocusQuest` nunca roda), `streak` resetada a 0 — **exceto** classe
     `Ranger`, que tem 15% de chance (`Math.random() < 0.15`) de salvar a streak
     ("Esquiva Rápida"); `combo` também zerado; `isPlayerDead=true`.
  5. **`respawnHero()`** (fluxo separado, chamado após a morte): -1 nível de combate, -50 GP,
     `combatXP` zerado.

  **Escopo de port**: não é um ajuste pontual em `FocusRewardsLogic`/`FocusApplyLogic` — é um
  subsistema novo (lifecycle listeners + grace period + death state + respawn penalty +
  interação com streak/combo/classe Ranger). Precisa de decisão de escopo com Bruno antes de
  qualquer bloco (inclusive se listeners de lifecycle do Android/Compose — `ON_STOP`/
  `ON_PAUSE` do ciclo de vida, não exatamente equivalentes a `visibilitychange`/`blur` de
  browser — têm paridade 1:1 viável, ou se a fidelidade aqui precisa ser conscientemente
  adaptada).

  **Decisão de escopo aprovada por Bruno (09/08/2026)**: o gatilho não precisa de fidelidade
  estrita — o conceito de aba/janela perdendo foco não existe em Android nativo. Adaptar para o
  equivalente mais próximo do lifecycle real do Android (app indo para background —
  `ON_STOP`/`ON_PAUSE`), sem tentar replicar a distinção entre `visibilitychange` e
  `window.blur`/`focus` do browser. **O restante da lógica de negócio mantém fidelidade
  estrita**: grace period de 3s, morte cognitiva (sessão inteira perdida), reset de
  streak/combo, exceção de 15% da classe Ranger, penalidade de `respawnHero()` (-1 nível,
  -50 GP, XP de combate zerado), e a exceção do título `DEATH-PROOF` (converte em pausa).

  **Achado secundário — RESOLVIDO (09/08/2026)**: `completeFocusQuest` tem duas chamadas reais,
  confirmado por fonte. (1) Quando o timer chega a 0 com `isFocusMode=true`, cai no branch
  antecipado (só seta `isFocusCompleted=true`, sem calcular nada). (2) Essa mudança dispara um
  `useEffect` em `FocusModeScreen.tsx` (`handleAutoClaim`) que sai do fullscreen, seta
  `isFocusMode=false`, reseta `isFocusCompleted=false`, e só então chama `completeFocusQuest`
  de novo — agora sim calculando recompensas de verdade, com som de level-up em seguida.
  **Detalhe de UI a replicar**: enquanto `isFocusCompleted === true` (entre as duas chamadas),
  `FocusModeScreen` renderiza `null` — uma tela em branco momentânea antes do modal de
  recompensas. Relevante para o bloco de wiring real do timer (ainda não iniciado) e para o
  Bloco 2 do fatiamento de Morte Cognitiva (interação entre timer expirando e uma possível morte
  cognitiva em andamento ao mesmo tempo — cenário ainda não analisado, checar antes de portar).

- **Correção de localização (11/08/2026)**: os dois overlays visuais de Morte Cognitiva
  (`isGraceActive` e `isPlayerDead`) **não vivem em `FocusModeScreen.tsx`** — buscas por "grace"
  nesse arquivo retornam zero ocorrências. A UI real mora em `App.tsx`, montada como
  `<AnimatePresence>` condicional consumindo os estados devolvidos por `useFocusSession()`.
  Confirmado por busca `rg -l` (formato `GUIA_FONTE_REACT.md`) nos três nomes de variável.

- **CORREÇÃO DE CORREÇÃO (16/08/2026)**: a "correção de localização" de 09/08 (que apontava
  `IncursionModeModal.tsx` como a fonte real do segmented control já portado em
  `RaidModeSection.kt`) estava **errada** — conflava dois componentes React distintos. Dump
  literal real de ambos obtido nesta sessão:
  - **`src/modules/focus/IncursionModeModal.tsx`** (existe de fato, confirmado por busca no
    repo real, SHA `66f7bc3...`): é um **modal** com 3 cards empilhados clicáveis (Padrão/
    Masmorra/Selvagem, cada um com ícone `lucide-react`, badge "Ativo", cooldown de Masmorra) —
    uma **segunda superfície de UI** pra escolher o modo, distinta e visualmente diferente do
    segmented control horizontal já portado. `onSelectMode: (mode: 'standard' | 'dungeon' |
    'wilderness') => void` mapeia 1:1 pro `RaidMode` enum (`Android`) já existente. Ainda **não
    portado**.
  - **`src/modules/focus/ModeDescriptionModal.tsx`** (existe de fato, mesmo SHA): modal
    genérico de descrição (`title`, `variant: 'purple'|'red'|'amber'`, `blocks:
    ModeDescriptionBlock[]` com `label`/`text`/`icon?`), reusa o wrapper `Modal` base — é
    exatamente o que os 3 botões "?" (`onShowDungeonHelp`/`onShowWildernessHelp`/
    `onShowStandardHelp`) do `RaidModeSection.kt` já esperam, com o conteúdo já preparado
    (`RaidModeHelpContent`, `buildStandardLootHelpBlocks`) desde o Bloco daquele arquivo. Ainda
    **não portado**.
  - **Achado paralelo, resolve dependência do bloco anterior**: `components/Modal.tsx` (base
    React de ambos) já tem equivalente Android pronto — `HeroLogModal.kt`
    (`ui/components/HeroLogModal.kt`), com `ModalVariant.Amber/Purple/Red` batendo exatamente
    com as 3 variantes usadas pelos dois componentes React. Reusar, não recriar.
- **RISCO NOVO — `isPlayerDead` tem dois gatilhos reais e independentes na fonte, não um só
  (11/08/2026).** Além do grace period de Terra Selvagem esgotado (já mapeado acima,
  `triggerCognitiveDeath()`), existe um segundo caminho: dano por **negligência de Diárias**
  (módulo Hábitos, ainda não portado) — quando HP zera por diárias não cumpridas
  (`dailyNeglectDamage`, com `StreakShield` como escudo consumível e penalidade reduzida pra
  classe Ranger), a mesma flag `isPlayerDead = true` é setada. O overlay de morte no React é
  **agnóstico de causa**: só olha `isPlayerDead`, não sabe se foi Selvagem ou Diária.
  **Decisão de escopo aprovada por Bruno (11/08/2026)**: portar os overlays agora (Bloco 26)
  com `isPlayerDead`/`isGraceActive` genéricos, prontos para servir os dois gatilhos sem
  retrabalho futuro — mas o *gatilho* de dano por Diária fica fora de escopo até o módulo
  Hábitos ser portado (mesma categoria de decisão do `IncursionModeModal.tsx`).

  **Implementado (11/08/2026)**: `WildernessGracePeriodOverlay` e `CognitiveDeathOverlay`
  criados em `CognitiveDeathOverlays.kt` (Bloco 26), plugados em `FocusModeScreen.kt` via 4
  parâmetros novos com defaults seguros. Fidelidade visual confirmada contra o JSX real de
  `App.tsx` (localização corrigida acima). Wiring real de lifecycle (`ON_STOP`/`ON_PAUSE`) e o
  gatilho de dano por Diária permanecem fora de escopo (Bloco 3 futuro / módulo Hábitos).

  **RESOLVIDO (16/08/2026, Bloco 37)**: wiring real de lifecycle implementado e fechado.
  `HeroLogViewModel` ganhou `onAppBackgrounded`/`onAppForegrounded`/`startGracePeriod`/
  `triggerCognitiveDeath`/`returnToFocusFromGrace`/`respawnHero`, ligados via
  `DisposableEffect`+`LifecycleEventObserver` único no `MainActivity.kt`
  (`ON_STOP`→`onAppBackgrounded`, `ON_START`→`onAppForegrounded`, automático, sem clique —
  decisão de Bruno confirmada antes do bloco). 6 testes novos + 42 testes de Foco no total sem
  regressão (`HeroLogViewModelTest`, `FocusSessionViewModelTest`, `FocusSessionConfirmTest`,
  `FocusSessionRecoveryTest`, `FocusSessionRepositoryTest`). Confirmado por leitura direta do
  `.kt` real antes de aceitar o fechamento. **Gatilho de dano por Diária continua fora de
  escopo** (mesma categoria do módulo Hábitos, ainda não portado) — só o gatilho de Terra
  Selvagem foi wireado. **Ainda pendente**: inspeção visual humana do fluxo real (grace
  period/morte/respawn), mais trabalhosa de testar manualmente que o fluxo normal de conclusão
  (exige simular o app indo a background durante uma sessão Selvagem).
- **Cooldown de Masmorra — cálculo confirmado, decisões de escopo tomadas (09/08/2026).**
  Fonte real (`App.tsx`): `elapsed = Date.now() - lastDungeonClearedTime`,
  `cooldownTotal = 2h (7.200.000 ms)`, `dungeonCooldownRemaining = max(0, cooldownTotal -
  elapsed)`. **Achado**: `lastDungeonClearedTime` é `useState<number>(0)` — não persiste em
  lugar nenhum (nem localStorage, nem `CharacterState`). No React real, reload da página =
  cooldown reseta (porque `elapsed` vira gigantesco a partir de `0`).

  **Decisão de escopo aprovada por Bruno**: divergência consciente — o Android vai **persistir
  de verdade** o `lastDungeonClearedTime` (provavelmente novo campo em `CharacterState`/Room),
  não replicar o "furo" de não-persistência do React. Registrar como divergência consciente
  quando o campo for adicionado ao modelo (mesmo padrão do precedente `Skill.id` no Bloco 15).

  **Implementado (11/08/2026)**: `lastDungeonClearedTime` adicionado a `CharacterState`
  (`Long = 0L`, Bloco 25), escrito em `FocusApplyLogic.kt` sempre que
  `calc.dungeonClearGoldBonus > 0`. Persistido de verdade via blob JSON já existente — sem
  migração de schema Room. Divergência consciente do React confirmada e fechada.

  **Ainda faltam pra fechar o cálculo**: (1) a função `formatDungeonCooldown(ms): string` em si
  (texto "Xh Ym" ainda é só stub/placeholder no `RaidModeSection.kt`, não confirmado contra a
  fonte real); (2) o mecanismo de polling que re-executa `checkCooldown()` periodicamente
  (provavelmente `setInterval` dentro do mesmo `useEffect`, trecho cortado antes de mostrar isso
  — não crítico pra lógica de negócio em si, só pra cadência de atualização da UI).

  **Achado paralelo, não resolve o cooldown mas expande o mapa**: existe uma **segunda
  superfície de UI** pra escolher o modo, além do segmented control já portado no Bloco 22 —
  `IncursionModeModal.tsx` (modal com 3 cards empilhados, callback `onSelectMode`, mesma lógica
  de negócio e mesmos textos de log do segmented control, só apresentação diferente). **Decisão
  de escopo aprovada por Bruno**: portar só o segmented control por enquanto; o modal fica como
  pendência separada, não bloqueia o restante.
- **`HistoryEntry.date` — formato confirmado por auditoria de fonte real (`App.tsx`,
  `handleConfirmClaimRewards`)**: `new Date().toLocaleString('pt-BR')` (data + hora, ex.
  `"04/08/2026, 14:23:10"`), NÃO `toLocaleDateString`. Compatível com `QuestLogic.todayLocalStr()`
  /`todaySessions()` já portados e testados (Bloco 14) porque o prefixo de data bate — o
  `startsWith` do filtro tolera a hora extra no final. Quando `HistoryEntry` real for criado no
  Android, usar `SimpleDateFormat("dd/MM/yyyy, HH:mm:ss", Locale("pt","BR"))` ou equivalente,
  mantendo o prefixo `dd/MM/yyyy` intacto.
- **Três formatos de data distintos coexistem no `CharacterState` do React, cada um com um
  propósito**: `toDateString()` (inglês, ex. `"Tue Aug 04 2026"`, usado no seed do LCG e em
  `lastStudyDate`), `toLocaleDateString('pt-BR')` (usado em `todayLocalStr()`/filtro de quests),
  `toLocaleString('pt-BR')` (usado em `HistoryEntry.date`). Não confundir os três ao portar
  módulos futuros. `state.todayDate` (usado no fallback legado de `isQuestClaimed`, já portado)
  ainda não teve sua origem/formato confirmados contra a fonte real — suspeita forte de que segue
  a família `toDateString()` (inglês) do `lastStudyDate`, mas não verificado ainda.
- **`completeFocusQuest` (`useFocusSession.ts`) e `handleConfirmClaimRewards` (`App.tsx`) são
  duas fases distintas de conclusão de sessão, não uma função só** — descoberta de auditoria,
  não estava clara antes. A primeira só CALCULA (XP, gold, loot rolado, título dropado) e guarda
  em `rewardsModalData`, sem tocar em `gameState`. A segunda, disparada só quando o jogador
  confirma no `FocusCompletionFlow`, é quem aplica tudo de fato: soma gold/XP, decrementa cargas
  de equipamento usado, remove consumíveis de uso único (`DoubleLoot`/`FocusElixir`/
  `RuneFortune`/`CrystalClarity`), adiciona itens de loot ao inventário, cria o `HistoryEntry`,
  atualiza streak/combo/achievements. **Ao portar "Timer Pomodoro"/"Loot drop" (seção 3 e 8),
  as duas fases precisam ser portadas juntas** — calcular sem aplicar (ou vice-versa) não faz
  sentido isolado.
- **Sistema de conquistas encontrado é diferente do documentado na seção 9.** Dentro de
  `handleConfirmClaimRewards` existe uma lista `testAchievements` hardcoded inline
  (`first_quest`, `streak_3`, `streak_7`, `xp_1000`, mais `survive_wilderness` fora da lista) que
  desbloqueia conquistas diretamente — não usa nenhum registro externo. Isso pode ser um sistema
  paralelo ao `Achievement.check` de `types.ts` (que pode estar vestigial/não utilizado) ou a
  documentação da seção 9 estava incompleta. **Investigar o `Achievement` real em `types.ts`
  antes de portar Conquistas**, para não portar o sistema errado.
- **Bug antigo do bônus de masmorra (+2.500 GP anunciado mas não aplicado) confirmado como
  corrigido na versão atual da fonte**: `totalGoldGained = goldEarned + (dungeonClearGoldBonus
  || 0)` é de fato somado a `prev.gold`. Não reintroduzir a regressão ao portar essa lógica.
- **Bug real confirmado na fonte React atual (não corrigido lá, diferente do caso do bônus de
  masmorra): `totalXP` nunca é incrementado em `handleConfirmClaimRewards`.** O `return {...prev,
  ...}` atualiza `todayXP: prev.todayXP + xpEarned` mas nunca `totalXP`. Confirmado por Bruno:
  o teste de conquista `xp_1000` dentro da mesma função usa `current: prev.totalXP + finalXP`
  (recalculando a soma ali mesmo, em vez de usar um valor já sincronizado) — evidência de que o
  campo `totalXP` do estado principal fica congelado desde a criação do personagem, nunca
  refletindo sessões de foco. **CORRIGIDO no Android (Bloco 17.2)**: `totalXP = state.totalXP +
  calc.xpEarned` adicionado ao `candidateState` de `FocusApplyLogic.kt`, testado
  (`apply_incrementsTotalXp`, 106 testes no total). Diferente do bug do bônus de masmorra (que
  era um bug já corrigido na fonte — aqui o bug ainda está presente na fonte atual, então a
  correção é uma melhoria real, não só evitar regressão).
- **Catálogo de dados extensos (`TitleCatalog.kt`, 47 itens) é caso especial de risco.** O AI
  Studio fabricou conteúdo (IDs, nomes, preços, condições de desbloqueio) em duas tentativas
  consecutivas de transcrição, mesmo com a fonte real colada integralmente no prompt — ver
  Bloco 8 no `DEV_LOG_ANDROID.md` para o incidente completo. Metodologia adotada: para
  catálogos/dados literais extensos, Claude escreve o arquivo diretamente (usando a fonte real
  disponível na conversa) e Bruno cola manualmente no projeto, usando o AI Studio só para
  build/teste — não para gerar ou reescrever o conteúdo.
- **Rebrand de paleta `amber` → `champagne` no React (v1.1.17) — item reescrito em 26/08, ver
  nota de processo no `DEV_LOG_ANDROID.md` da mesma data.** A v1.1.17 renomeou a paleta em boa
  parte do app pra uma cor customizada (`champagne-500: #d4af37`, visivelmente diferente do
  `amber-500` padrão do Tailwind). Achado original (Bloco A): 3 tons (`champagne-100`/`900`/
  `950`) são referenciados em algum ponto do código-fonte sem definição em `index.css` — bug
  real na fonte, ainda sem lista completa de onde exatamente aparecem. Telas já auditadas
  especificamente contra esse risco, por bloco:
  - `GuideTab.tsx` (Bloco D): corpo do componente usa só `amber-*` padrão, não foi tocado pelo
    rebrand. Só o cabeçalho do wrapper em `App.tsx` usa `champagne-400`/`500` (ambos definidos,
    fora do trio ausente).
  - `ShopTab.tsx`/`HeatmapTab.tsx` (Bloco E): `ShopTab.tsx` usa só `champagne-300`/`500` (ambos
    definidos); `HeatmapTab.tsx` não usa `champagne` nenhum. Nenhum dos dois afetado.
  - `StatsTab.tsx`/`AchievementsTab.tsx`/`TitleSelector.tsx` (Bloco C, 26/08): headers dos
    wrappers em `App.tsx` usam `champagne-400`/`500` (definidos). Nenhum uso de
    `champagne-100`/`900`/`950` encontrado nos 3 componentes nem nos wrappers correspondentes.
  **Ainda em aberto**: onde exatamente `champagne-100`/`900`/`950` são referenciados na fonte
  (nenhum bloco portado até agora bateu neles) e se vale reauditoria formal das telas já
  "Validadas" de módulos anteriores ao rebrand (Foco/Personagem/Missões, portadas antes da
  v1.1.17 existir).
