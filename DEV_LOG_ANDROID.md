# DEV_LOG_ANDROID.md

## Por que este arquivo existe

Mesma lógica do `DEV_LOG_v1.1.x.md` do projeto React: o Google AI Studio Build não mantém
controle de versão nativo dentro do container de trabalho, e o histórico de mudanças some entre
sessões a menos que seja registrado manualmente. Este arquivo é o registro técnico granular do
**port Android/Kotlin do HeroLog**, mantido em paralelo ao projeto React (que continua com seu
próprio `DEV_LOG_v1.1.x.md`, sem relação com este).

Este não é o `CHANGELOG.md` do app Android (esse ainda não existe — só passa a fazer sentido a
partir do momento em que houver uma primeira versão publicável). Este é o log técnico interno,
granularidade de arquivo e bloco de trabalho.

## Diferença de escopo em relação ao template original

O template React fecha um arquivo por ciclo/versão (ex: v1.1.3) porque o app já tem versionamento
semântico estabelecido. O port Android ainda não tem — estamos na fase de fundação. Por isso,
**a unidade de registro aqui é o Bloco** (Bloco 0, Bloco 0.1, Bloco 1, ...), não uma versão
semântica. Quando o app Android atingir sua primeira versão publicável com paridade mínima
viável, este arquivo é encerrado e arquivado, e passamos a usar o mesmo padrão de versionamento
por ciclo do projeto React (`DEV_LOG_ANDROID_v1.0.x.md`).

## Quando atualizar

Mesma regra do projeto React: uma entrada por Bloco, adicionada **imediatamente após o bloco ser
implementado e validado nesta mesma sessão** — nunca reconstruída de memória depois. A validação
aqui inclui, sempre que aplicável: build bem-sucedido, teste unitário com resultado literal
(`PASSED`/`FAILED` por nome de método, não só "BUILD SUCCESSFUL"), e print de tela real quando
houver UI.

Formato de cada entrada:

```
## [AAAA-MM-DD] Bloco: <identificador>

**Arquivos criados/alterados:**
- caminho/completo/arquivo1.kt
- caminho/completo/arquivo2.kt

**Resumo:**
- Ponto objetivo 1
- Ponto objetivo 2

**Validação:**
- Build: <resultado>
- Testes: <lista de testes com resultado individual, ou "N/A">
- Visual: <print confirmado / N/A>

**Desvios de escopo aprovados:**
- <item, ou "Nenhum">
```

## O que NÃO deve ir aqui

- Linguagem de marketing ou voltada a usuário final.
- Especulação sobre mudanças não verificadas via diff real ou output literal.
- Resumos vagos tipo "melhorias diversas".
- Alegações de teste sem o resultado nominal por método (ver `ARMADILHAS_CONHECIDAS.md`).

---

## Log de alterações

## [2026-07-29] Bloco: 0 — Fundação do projeto

**Arquivos criados/alterados:**
- /app/src/main/java/com/example/... (estrutura inicial de template, posteriormente removida no Bloco 0.1)
- /app/src/main/java/com/iurispraecepta/herolog/model/CharacterModels.kt
- /app/src/main/java/com/iurispraecepta/herolog/util/LevelCalculator.kt
- /app/src/test/java/com/iurispraecepta/herolog/LevelCalculatorTest.kt
- /metadata.json
- /settings.gradle.kts
- /app/build.gradle.kts
- /app/src/main/res/values/strings.xml

**Resumo:**
- Scaffold inicial do projeto Android (Kotlin + Jetpack Compose + Material 3).
- Modelos de dados provisórios (`Skill`, `HistoryEntry`, `InventoryItem`, `Habit`, `Daily`,
  `Todo`, `PomodoroSettings`) criados como placeholders `TODO`, aguardando dump literal do
  `types.ts` do projeto React.
- `CharacterState` portado como `data class` com fidelidade 1:1 aos nomes/tipos do React,
  exceto os placeholders acima.
- `CharClass` portado como `enum class` (`Mage`, `Warrior`, `Ranger`).
- Dependências do Room adicionadas (sem entidade/DAO ainda — decisão de schema pendente).
- Função de exemplo (`LevelCalculator.calculateExampleThreshold`) criada exclusivamente para
  testar viabilidade de execução de testes unitários no ambiente AI Studio.

**Validação:**
- Build: sucesso (relatado, sem output literal nesta rodada).
- Testes: execução relatada apenas via grafo de tasks do Gradle, sem confirmação nominal do
  teste individual — **insuficiente**, corrigido no Bloco 0.1.
- Visual: tela padrão do template ("hello android", fundo preto) — não avaliado formalmente,
  nenhuma UI real implementada ainda.

**Desvios de escopo aprovados:**
- `namespace = com.example` mantido divergente do `applicationId` — **não aprovado**,
  correção obrigatória registrada e executada no Bloco 0.1.

---

## [2026-07-29] Bloco: 0.1 — Correção de namespace + Persistência Room (Opção A)

**Arquivos criados/alterados:**
- /app/build.gradle.kts (namespace corrigido para `com.iurispraecepta.herolog`, plugin e
  dependência de kotlinx.serialization adicionados)
- /gradle/libs.versions.toml (declarações de kotlinx-serialization-json e plugin)
- /build.gradle.kts (plugin kotlin.serialization, apply false)
- /app/src/main/java/com/iurispraecepta/herolog/model/CharacterModels.kt (anotação
  `@Serializable` em todas as data classes/enums)
- /app/src/main/java/com/iurispraecepta/herolog/data/entity/CharacterStateEntity.kt (novo)
- /app/src/main/java/com/iurispraecepta/herolog/data/dao/CharacterStateDao.kt (novo)
- /app/src/main/java/com/iurispraecepta/herolog/data/database/HeroLogDatabase.kt (novo)
- /app/src/main/java/com/iurispraecepta/herolog/data/repository/CharacterRepository.kt (novo)
- /app/src/main/java/com/iurispraecepta/herolog/MainActivity.kt (novo, scaffold mínimo)
- /app/src/main/java/com/iurispraecepta/herolog/ui/theme/{Color,Type,Theme}.kt (novo, scaffold
  mínimo de tema Compose)
- /app/src/test/java/com/iurispraecepta/herolog/CharacterRepositoryTest.kt (novo)
- Pacote antigo `com.example.*` removido (main e test).

**Resumo:**
- Namespace alinhado ao applicationId (`com.iurispraecepta.herolog` nos dois).
- Persistência local implementada via Room, Opção A (blob JSON): entidade única
  `CharacterStateEntity` com `jsonPayload: String`, serialização via kotlinx.serialization.
- `CharacterRepository` expõe a API em termos de `CharacterState` puro — serialização JSON
  fica encapsulada, não vaza para quem consome o repositório.
- Scaffold mínimo de `MainActivity` e tema Compose criado (não pedido explicitamente, mas
  necessário para o projeto compilar como app Compose válido — aprovado como boilerplate
  de baixo risco, sem lógica de negócio envolvida).

**Validação:**
- Build: sucesso, confirmado após a correção do namespace.
- Testes: 4 testes, resultado nominal individual confirmado —
  `CharacterRepositoryTest > repository_saves_and_retrieves_characterState_correctly PASSED`,
  `ExampleRobolectricTest > read string from context PASSED`,
  `GreetingScreenshotTest > greeting_screenshot PASSED`,
  `LevelCalculatorTest > calculateExampleThreshold_returnsCorrectValues PASSED`.
  **Confirma que testes unitários JVM (via Robolectric) rodam de fato no ambiente AI Studio.**
  Isso passa a ser critério padrão de fechamento de bloco a partir de agora.
- Visual: pendente confirmação de print real da tela atual (solicitado, ainda sem resposta).

**Desvios de escopo aprovados:**
- Scaffold de `MainActivity` e arquivos de tema Compose (`Color.kt`, `Type.kt`, `Theme.kt`) —
  não pedidos explicitamente, aprovados como boilerplate mínimo necessário à compilação.

---

## [2026-07-29] Bloco: 1 — Port fiel dos modelos de dados (types.ts real)

**Arquivos criados/alterados:**
- /app/src/main/java/com/iurispraecepta/herolog/model/CharacterModels.kt (reescrito por completo)
- /app/src/test/java/com/iurispraecepta/herolog/CharacterRepositoryTest.kt (fixture atualizado)

**Resumo:**
- Todos os placeholders substituídos pelos tipos reais do `types.ts`: `InventoryItem`, `Skill`,
  `HistoryEntry`, `Habit`, `Daily`, `Todo`, `PomodoroSettings`, `CharacterState`.
- Uniões de string literal do TS portadas como `enum class` com `@SerialName` explícito em cada
  entrada, garantindo paridade byte a byte do JSON serializado (`BuffType`, `Rarity`,
  `Difficulty`, `RepeatInterval`, `CharClass`).
- Tipo inline repetido `{ id, text, completed }` extraído como `ChecklistItem` compartilhada.
- Campos opcionais do TS (`?`) mapeados como nulos com default `null` — não `emptyList()` — para
  não confundir "campo ausente" com "lista vazia intencional".
- `Quest`, `Achievement`, `ActiveSession` e os tipos de modal de level-up deliberadamente fora de
  escopo (não são parte de `CharacterState`; registrados no `PARIDADE.md` seção 9).

**Validação:**
- Build: sucesso.
- Testes: 4 testes reportados nominalmente nesta rodada (`CharacterRepositoryTest`,
  `LevelCalculatorTest`, `ExampleUnitTest`, `ExampleRobolectricTest`) — **incompleto**, faltava
  `GreetingScreenshotTest` sem explicação. Corrigido no Bloco 1.1.

**Desvios de escopo aprovados:**
- `Daily.value` mapeado como `Double` sem justificativa na seção de ambiguidades — **não
  aprovado**, investigado e corrigido no Bloco 1.1.

---

## [2026-07-29] Bloco: 1.1 — Correção de `Daily.value` + esclarecimento da suíte de testes

**Arquivos criados/alterados:**
- /app/src/main/java/com/iurispraecepta/herolog/model/CharacterModels.kt (`Daily.value` de
  `Double?` para `Int?`)

**Resumo:**
- Investigação no código-fonte React real (`useDailies.ts`, `App.tsx`, `useGameState.ts`,
  `DailiesTab.tsx`) confirmou que `Daily.value` só é alterado por incrementos/decrementos
  inteiros de `1` — nunca fracionário. Corrigido de `Double?` para `Int?`.
- `Daily.value` alimenta `getScoreColor` (cor de fundo/borda do card da diária) — é lógica de
  jogo real, não só metadado; registrar isso quando o módulo Missões/Daily for portado, não só
  como campo de dados.
- Confirmado que `GreetingScreenshotTest` nunca foi deletado — omissão de reporte na resposta
  anterior (leitura seletiva de artefatos individuais, não um problema real no projeto).
  `ExampleUnitTest` já existia desde o template original (Bloco 0), sob outro pacote, e migrou
  junto com a correção de namespace do Bloco 0.1 sem ter sido destacado antes.

**Validação:**
- Build: sucesso.
- Testes: 5 testes, todos nominais e `PASSED` — `CharacterRepositoryTest`, `LevelCalculatorTest`,
  `ExampleUnitTest`, `ExampleRobolectricTest`, `GreetingScreenshotTest`. Suíte completa e
  reconciliada com a listagem real do diretório de testes.

**Desvios de escopo aprovados:**
- Nenhum.

---

## [2026-07-29] Bloco: 2 — Lógica pura do módulo Skills

**Arquivos criados/alterados:**
- /app/src/main/java/com/iurispraecepta/herolog/logic/SkillLogic.kt (novo)
- /app/src/test/java/com/iurispraecepta/herolog/SkillLogicTest.kt (novo)

**Resumo:**
- Lógica de `useSkills.ts` portada como funções puras em `object SkillLogic`, sem efeitos
  colaterais de UI (diálogo, som, log).
- Fórmula de XP de skill consolidada: `requiredXpForLevel(level) = level * 80` (estava duplicada
  em dois arquivos React, aqui só existe em um lugar).
- Validações de nome (vazio/duplicado, case-insensitive) e de tags expostas via sealed classes
  (`SkillOperationResult`, `SkillError`) — a UI decide a mensagem, a lógica só decide o resultado.
- `canDeleteSkill`/`deleteSkillAt` separam as duas guardas de negócio (sessão ativa, mínimo de 1
  skill) da execução da remoção em si, que só roda depois de confirmação (fica pro Bloco 3).
- `applyPrestige` confirmadamente não inclui o multiplicador de +25% XP — investigado e
  confirmado ausente em `useSkills.ts`; TODO documentado no código apontando para o `PARIDADE.md`.

**Validação:**
- Build: sucesso.
- Testes: 16 testes no total (11 novos de `SkillLogicTest` + 5 pré-existentes), todos nominais e
  `PASSED`. Cobertura inclui o caso sensível de renomear uma skill para o mesmo nome que já tem
  (`sIdx != idx` na exclusão de duplicidade) — validado corretamente.
- Visual: N/A, nenhuma UI neste bloco.

**Desvios de escopo aprovados:**
- Primeira tentativa incluiu fallback de emoji (`emojiInput.ifBlank { "🎯" }`) e `tags =
  emptyList()` não pedidos e não sinalizados — **não aprovados**, revertidos na resposta final.
- Pendência menor, não bloqueante: `renameSkill` com índice fora do range devolve
  `SkillError.BlankName` (semanticamente incorreto, caminho inalcançável na prática) — corrigido
  no Bloco 2.1.

---

## [2026-07-29] Bloco: 2.1 — Correção rápida: `SkillError.InvalidIndex`

**Arquivos criados/alterados:**
- /app/src/main/java/com/iurispraecepta/herolog/logic/SkillLogic.kt (`SkillError` ganhou o caso
  `InvalidIndex`; `renameSkill` passou a retorná-lo em vez de `BlankName` para índice fora do
  range)
- /app/src/test/java/com/iurispraecepta/herolog/SkillLogicTest.kt
  (`renameSkill_fails_on_invalid_index` adicionado)

**Resumo:**
- Correção pontual e isolada, sem efeitos colaterais em outras funções.

**Validação:**
- Build: sucesso.
- Testes: 17 testes no total (12 de `SkillLogicTest` + 5 pré-existentes), todos nominais e
  `PASSED`.
- Visual: N/A.

**Desvios de escopo aprovados:**
- Nenhum.

---

## [2026-07-29] Bloco: 3 — `HeroLogModal` (componente de modal reutilizável)

**Arquivos criados/alterados:**
- /app/src/main/java/com/iurispraecepta/herolog/ui/components/HeroLogModal.kt (novo)
- /app/src/test/java/com/iurispraecepta/herolog/HeroLogModalScreenshotTest.kt (novo)

**Resumo:**
- Port fiel de `Modal.tsx`: variantes Amber/Purple/Red com tokens de cor exatos (hex do Tailwind),
  barra de gradiente superior, header com título serif + botão fechar, conteúdo scrollável.
- Backdrop com blur condicional (`SDK_INT >= 31`), fallback sem blur em versões antigas.
- `incrementModalCount`/`decrementModalCount` deixados como TODO — função real desconhecida
  (`modalHelper.ts` não fornecido), suspeita de pausar animações de fundo.
- Primeira versão do teste de screenshot Roborazzi criada (2 estados: aberto/fechado).

**Validação:**
- Build: sucesso.
- Testes: 19 testes nominais, todos `PASSED`.
- Visual: screenshot gerado em **modo record** (grava baseline, não compara) — ainda sem
  confirmação visual humana das imagens reais.

**Desvios de escopo aprovados:**
- Primeira versão tinha dois bugs reais de fidelidade — **não aprovados**, corrigidos no
  Bloco 3.1: (1) animação de saída inexistente (`if (!isOpen) return` cortava antes do
  `AnimatePresence` equivalente rodar); (2) `dismissOnBackPress` do `Dialog` e `BackHandler`
  manual concorrendo, com `onDismissRequest` checando a flag errada (`allowBackdropClose` em vez
  de `disableEscClose`) para decisão de back press.

---

## [2026-07-29] Bloco: 3.1 — Correção de animação de saída + conflito de back handling

**Arquivos criados/alterados:**
- /app/src/main/java/com/iurispraecepta/herolog/ui/components/HeroLogModal.kt

**Resumo:**
- Padrão `shouldRenderDialog` + `animatedVisible` com `LaunchedEffect`/`delay(220L)` mantém o
  `Dialog` montado durante a animação de saída, replicando o comportamento do `AnimatePresence`.
- `dismissOnBackPress = false` no `Dialog`; `BackHandler` manual é agora a única fonte de verdade
  para back press, gatilhado por `!disableEscClose` (flag correta). `dismissOnClickOutside`
  continua gatilhado por `allowBackdropClose`, sem sobreposição de responsabilidade.

**Validação:**
- Build: sucesso.
- Testes: 19 testes, todos nominais e `PASSED`.
- Visual: modo record confirmado explicitamente pelo AI Studio — pendência de inspeção visual
  humana das imagens reais permanece em aberto, não bloqueante.

**Desvios de escopo aprovados:**
- Nenhum.

---

## [2026-07-29] Bloco: 4 — `SkillSelectorModal`

**Arquivos criados/alterados:**
- /app/src/main/java/com/iurispraecepta/herolog/ui/skills/SkillSelectorModal.kt (novo)
- /app/src/test/java/com/iurispraecepta/herolog/SkillSelectorModalScreenshotTest.kt (novo)

**Resumo:**
- Port fiel de `SkillSelectorModal.tsx`, consumindo `HeroLogModal` (Bloco 3) e
  `SkillLogic.requiredXpForLevel` (Bloco 2) — fórmula de XP não duplicada novamente, apesar do
  React original duplicar nos dois arquivos.
- Opacidades traduzidas com precisão de hex (conferido manualmente: 4%, 15%, 40%, 50%, 60%, 70%,
  80%, 90% todos batendo com os valores originais do Tailwind).
- Aproximações sinalizadas: `stone-850` (cor customizada não documentada, aproximada como
  `#211E1C`), ícone `Tag` do lucide-react (aproximado como `Icons.Outlined.Sell`).
- Screenshot único cobrindo os 3 estados relevantes numa lista de exemplo (inativa sem
  prestígio/tags, ativa, com prestígio+tags).

**Validação:**
- Build: sucesso.
- Testes: 19 testes nominais, todos `PASSED`.
- Visual: modo record, sem inspeção humana ainda (mesma pendência do Bloco 3).

**Desvios de escopo aprovados:**
- Glow do card ativo (`shadow-[0_0_15px_rgba(245,158,11,0.15)]` no React) ausente na primeira
  entrega, sem sinalização — **não aprovado**, corrigido no Bloco 4.1.

---

## [2026-07-29] Bloco: 4.1 — Correção do glow do card ativo

**Arquivos criados/alterados:**
- /app/src/main/java/com/iurispraecepta/herolog/ui/skills/SkillSelectorModal.kt

**Resumo:**
- Adicionado `Modifier.shadow(...)` condicional (`isActive`) com `ambientColor`/`spotColor` em
  `Amber500` a 15% de opacidade, aplicado antes do `.clip(...)`.
- Limitação de plataforma sinalizada corretamente: sombras coloridas só funcionam de fato em
  API 28+; em versões anteriores, degrada para cinza/preto padrão do sistema.

**Validação:**
- Build: sucesso.
- Testes: 19 testes — a primeira resposta reportou incorretamente "10" testes em
  `SkillLogicTest` num resumo agregado malfeito; segunda checagem com saída nominal completa
  confirmou os 12 nomes intactos e 19/19 no total. Erro de relato, não perda real de teste.
- Visual: pendência de inspeção humana ainda em aberto (Bloco 3 + 4).

**Desvios de escopo aprovados:**
- Nenhum.

---

## [2026-07-29] Bloco: 5 — `SkillsScreen` (tela principal do módulo Skills)

**Arquivos criados/alterados:**
- /app/src/main/java/com/iurispraecepta/herolog/ui/skills/SkillsScreen.kt (novo)
- /app/src/test/java/com/iurispraecepta/herolog/SkillsScreenScreenshotTest.kt (novo)

**Resumo:**
- Port fiel de `SkillsScreen.tsx`: lista de skills com edição inline de nome, gerenciador de
  subskills (tags), botão de prestígio pulsante (nível 99+), e modal de criação de skill
  (picker de emoji, campo de nome, sugestões rápidas com estado desabilitado por duplicidade).
- `reqXP` consolidado via `SkillLogic.requiredXpForLevel` — terceira e última ocorrência da
  fórmula duplicada no React, agora existe só em um lugar no Kotlin.
- `onDeleteSkill: (Int) -> Unit`, sem diálogo de confirmação interno — orquestração de
  confirmação fica pra camada de ViewModel futura, conforme decidido no Bloco 2.
- `isCreateModalOpen`/`onCreateModalOpenChange` sempre controlado (sem padrão dual
  controlled/uncontrolled do React, desnecessário aqui).
- Glow do emoji selecionado no picker implementado corretamente desde a primeira entrega desta
  vez (usando o padrão `Modifier.shadow` estabelecido no Bloco 4.1).
- Animação de pulso do botão de prestígio: alpha 1.0↔0.5, ciclo de 2s (`tween(1000)` + reverse),
  com TODO apontando para o sistema de pausa de animação por modal ainda não investigado.

**Validação:**
- Build: sucesso.
- Testes: primeira entrega teve cobertura de screenshot incompleta (faltava estado de edição e
  modal de criação separado) — corrigido no Bloco 5.1.
- Visual: pendência de inspeção humana ainda em aberto.

**Desvios de escopo aprovados:**
- Primeira entrega da resposta veio como resumo em prosa sem código-fonte, duas vezes seguidas
  — **não aprovado**, insistido até vir o código literal.
- `subskillInput` (e implicitamente outros estados por item) usava a posição da lista (`idx`)
  como identidade em vez da skill em si — risco real de estado vazar pra skill errada após
  exclusão de item no meio da lista. Existe no React também (`key={idx}`), mas decidido corrigir
  na versão Android mesmo assim — corrigido no Bloco 5.1 com `key(sk.name)`.
- Cobertura de screenshot pedida (skill em edição, skill elegível a prestígio, modal de criação
  separado) não veio completa na primeira entrega — corrigido no Bloco 5.1.

---

## [2026-07-29] Bloco: 5.1 — `key(sk.name)` + cobertura de screenshot completa

**Arquivos criados/alterados:**
- /app/src/main/java/com/iurispraecepta/herolog/ui/skills/SkillsScreen.kt (`key(sk.name)`
  envolvendo cada card; parâmetro `initialEditingIdx` adicionado para testabilidade)
- /app/src/test/java/com/iurispraecepta/herolog/SkillsScreenScreenshotTest.kt (3 testes:
  lista padrão, estado de edição via `initialEditingIdx`, modal de criação aberto)

**Resumo:**
- Estado por item agora escopado pela identidade estável da skill (`sk.name`, único por
  construção via `SkillLogic`), não pela posição na lista.
- `initialEditingIdx: Int? = null` adicionado como parâmetro opcional só para permitir forçar o
  estado de edição em teste, sem poluir a API de produção normal.

**Validação:**
- Build: sucesso.
- Testes: 23 testes no total, todos nominais e `PASSED`, soma conferida e correta desta vez.
- Visual: pendência de inspeção humana ainda em aberto — mas agora finalmente temos uma tela
  completa o suficiente pra valer a pena plugar na `MainActivity` e ver de verdade.

**Desvios de escopo aprovados:**
- Nenhum.

---

## [2026-07-29] Bloco: 6 — Wiring mínimo na `MainActivity` (preview visual, sem persistência)

**Arquivos criados/alterados:**
- /app/src/main/java/com/iurispraecepta/herolog/MainActivity.kt (reescrito)

**Resumo:**
- `SkillsScreen` plugada na `MainActivity` com estado em memória (`remember { mutableStateOf
  (sampleSkills) }`) — scaffolding temporário e descartável, sem ViewModel nem
  `CharacterRepository`/Room ainda.
- Todos os callbacks (`onAddCustomSkill`, `onRenameSkill`, `onAddTagToSkill`,
  `onRemoveTagFromSkill`, `onDeleteSkill`, `onPrestigeSkill`) chamam `SkillLogic` diretamente,
  tratando `Success`/`Error` com `Log.d` e TODO explícito para sistema de toast/snackbar futuro.
- `onDeleteSkill` sem diálogo de confirmação (conforme decidido no Bloco 2/5) — só verifica
  `canDeleteSkill` e loga se não elegível.
- 3 skills de exemplo: uma comum, uma com prestígio+tags, uma no nível 99 (ajustada de um erro
  de instrução original — nível 98 não teria mostrado o botão de prestígio, que exige `>= 99`).
- FAB abre o modal de criação de skill.

**Validação:**
- Build: sucesso, confirmado literalmente (`BUILD SUCCESSFUL`) nas duas rodadas (antes e depois
  do ajuste de nível).
- Testes: suíte completa intacta, 23/23 (nenhum teste novo neste bloco, escopo era só wiring).
- Visual: **primeira vez que há algo real pra ver no preview/emulador** — pendência agora é
  Bruno confirmar visualmente, não mais um teste automatizado.

**Desvios de escopo aprovados:**
- Nenhum (o erro do nível 98→99 foi instrução minha errada, não desvio do AI Studio).

---

## [2026-07-29] Bloco: 6.1 — Correção do tema Obsidiana (FAB azul destoando)

**Arquivos criados/alterados:**
- /app/src/main/java/com/iurispraecepta/herolog/ui/theme/Theme.kt (colorScheme atualizado)
- /app/src/main/java/com/iurispraecepta/herolog/MainActivity.kt (`FloatingActionButton` e
  `TopAppBar` com cores explícitas Amber400/Stone900/Stone950)

**Resumo:**
- **Primeira confirmação visual real feita por Bruno** (prints do preview do AI Studio) — achado
  concreto: o FAB aparecia azul (cor padrão do template Compose, nunca customizada desde o
  Bloco 0.1), destoando da paleta dourada estabelecida em todos os outros componentes.
- `Theme.kt` corrigido para usar os tokens de cor já estabelecidos (Amber400/500, Stone950/900/
  800/400) no `colorScheme` do `MaterialTheme`, tanto dark quanto "light" (app é visualmente
  sempre escuro por design).
- `dynamicColor = false` por padrão — evita que o Material You do Android 12+ sobrescreva a
  paleta Obsidiana com cores extraídas do wallpaper do usuário.
- Print de confirmação: FAB e título da TopAppBar agora dourados, consistentes.
- Bônus confirmado no mesmo print: `applyPrestige` funciona ponta a ponta no app real — a skill
  "Academia" voltou pro Nível 1, zerou XP, e ganhou a coroa + "Bônus: +25% XP" ao tocar no botão.

**Validação:**
- Build: sucesso.
- Visual: **primeira confirmação visual humana real do projeto**, via prints do preview.

**Desvios de escopo aprovados:**
- Débito técnico trivial, não bloqueante: `LightColorScheme` usa `darkColorScheme(...)` como
  builder (provável copy-paste), em vez de `lightColorScheme(...)`. Sem efeito visível hoje
  (app é sempre escuro por design), mas semanticamente incorreto — corrigir na próxima vez que
  `Theme.kt` for tocado.

---

## [2026-07-29] Bloco: 6.2 — Fechamento do débito técnico: `modalHelper.ts` real

**Arquivos criados/alterados:**
- /app/src/main/java/com/iurispraecepta/herolog/ui/theme/Theme.kt (`LightColorScheme` corrigido
  para usar `lightColorScheme(...)`, débito técnico do Bloco 6.1 fechado)
- /app/src/main/java/com/iurispraecepta/herolog/ui/components/ModalCountRegistry.kt (novo)
- /app/src/main/java/com/iurispraecepta/herolog/ui/components/HeroLogModal.kt
  (`DisposableEffect(isOpen)` chamando `increment`/`decrement`, separado do `LaunchedEffect` de
  animação)
- /app/src/main/java/com/iurispraecepta/herolog/ui/skills/SkillsScreen.kt (`effectiveAlpha`
  substituindo `alphaPulse` direto no botão de prestígio; TODO removido)
- /app/src/test/java/com/iurispraecepta/herolog/ModalCountRegistryTest.kt (novo, 2 testes)

**Resumo:**
- Código-fonte real de `modalHelper.ts` obtido de Bruno: contador global simples controlando
  uma classe CSS `modal-open` no `<body>`. Confirma a hipótese registrada desde o Bloco 3/5 —
  provavelmente usado por CSS externo (`.modal-open .pausable-anim { animation-play-state:
  paused }`) para pausar animações de fundo enquanto qualquer modal está aberto.
- Portado como semântica equivalente, não mecânica idêntica (não existe "classe CSS no body"
  em Android): `ModalCountRegistry` object com `increment()`/`decrement()`/`isAnyModalOpen`
  observável via `mutableIntStateOf`.
- `DisposableEffect` (não `LaunchedEffect`) garante decremento mesmo se o composable sair de
  composição com o modal aberto — replica a garantia de cleanup do `useEffect` do React.
- Botão de prestígio agora "congela" em alpha totalmente opaco enquanto qualquer modal está
  aberto, em vez de continuar pulsando — aproximação razoável do `animation-play-state: paused`
  do CSS (sinalizada como tal, não pixel-perfect ao frame exato do congelamento).

**Validação:**
- Build: sucesso.
- Testes: contagem por classe reportada incorretamente na primeira resposta (visualizador de
  XML truncou após linha 8) — segunda checagem com XML literal completo confirmou 25/25 testes,
  nada perdido. Mesma categoria de erro de relato já vista antes, desta vez com causa técnica
  identificada (truncamento de visualização, não erro de contagem humana).

**Desvios de escopo aprovados:**
- Nenhum.

---

## [2026-07-29] Bloco: 7 — Lógica pura dos módulos Inventário e Títulos

**Arquivos criados/alterados:**
- /app/src/main/java/com/iurispraecepta/herolog/logic/InventoryLogic.kt (novo)
- /app/src/main/java/com/iurispraecepta/herolog/logic/TitleLogic.kt (novo)
- /app/src/test/java/com/iurispraecepta/herolog/InventoryLogicTest.kt (novo, 7 testes)
- /app/src/test/java/com/iurispraecepta/herolog/TitleLogicTest.kt (novo, 8 testes)

**Resumo:**
- Port fiel de `useInventory.ts`/`inventoryUtils.ts`/`useTitles.ts`, extraindo só a lógica de
  estado (sem log/som/UI).
- Dois bugs reais encontrados no React e **corrigidos deliberadamente** na versão Kotlin (Bruno
  aprovou ambos): `buyTitle` descontava ouro mesmo comprando um título já possuído (só checava
  `gold < price`); `equipTitle` não verificava posse do título antes de equipar. Ambos agora
  retornam resultados sealed (`TitlePurchaseResult.AlreadyOwned`, `EquipTitleResult.NotOwned`)
  em vez de silenciosamente permitir/descontar.
- `equipItem`/`unequipItem` com fallback de 3 slots (`[null, null, null]`) quando
  `equippedEquipment` é nulo, replicando o fallback do React.
- `sellItem`: `floor(price * 0.5)` para equipamento, `50` fixo caso contrário — testado
  explicitamente com preço ímpar (101 → 50) pra confirmar o floor.
- `inspectingItem`/`inspectItem`/`closeInspection` deliberadamente fora de escopo (estado de UI
  trivial, entra na tela de Inventário).
- `useLevelUp.ts` deliberadamente adiado para bloco futuro — arquiteturalmente diferente (detector
  reativo via `useRef`/`useEffect`, não mutador de estado simples).

**Validação:**
- Build: sucesso.
- Testes: 40 testes no total (15 novos + 25 pré-existentes), todos nominais e `PASSED`. Duas
  rodadas de esclarecimento de contagem necessárias: primeira teve `CharacterRepositoryTest`
  incorretamente reportado como "2 testes" (era erro de digitação, sempre foi 1) e soma total
  errada (relatada como 41 mesmo com a lista nominal somando 40 corretamente) — lista nominal
  final confirmada consistente e completa.
- Visual: N/A, nenhuma UI neste bloco.

**Desvios de escopo aprovados:**
- Nenhum (as correções de `buyTitle`/`equipTitle` foram aprovadas explicitamente por Bruno antes
  da implementação, não desvios não solicitados).

---

## [2026-07-29] Bloco: 8 — `CombatLogic.kt` + `TitleCatalog.kt` (35→47 títulos)

**Arquivos criados/alterados:**
- /app/src/main/java/com/iurispraecepta/herolog/logic/CombatLogic.kt (novo)
- /app/src/main/java/com/iurispraecepta/herolog/data/TitleCatalog.kt (novo)
- /app/src/test/java/com/iurispraecepta/herolog/CombatLogicTest.kt (novo, 2 testes)
- /app/src/test/java/com/iurispraecepta/herolog/TitleCatalogTest.kt (novo, 6 testes)

**Resumo:**
- Descoberta importante em `CharacterScreen.tsx`: fórmula de XP de combate é `combatLevel * 100`,
  **diferente** da fórmula de skill (`level * 80`) — resolve pendência aberta desde o Bloco 4.
  `CombatLogic.requiredXpForCombatLevel` criado como função separada, sem reaproveitar
  `SkillLogic`.
- **Incidente grave de fidelidade — catálogo de 47 títulos fabricado, não transcrito, em
  DUAS tentativas consecutivas do AI Studio**, apesar do `titleCatalog.ts` real estar colado
  integralmente no prompt em ambas as vezes. Erros incluíam: IDs em minúsculo (quebraria
  compatibilidade com `ownedTitles`/`equippedTitle` persistidos), nomes trocados, emojis
  trocados, **todos os preços diferentes do original em praticamente todos os 47 itens**, e a
  maioria das condições de `checkUnlocked` das 18 conquistas substituídas por condições
  completamente diferentes e não relacionadas (ex: `CENTURION` deveria checar
  `totalSessions >= 100`, veio checando `combatLevel >= 20`). A segunda tentativa incluiu uma
  frase de confirmação ("os 47 IDs foram conferidos individualmente") comprovadamente falsa.
  Uma terceira tentativa de pedir só a *execução* (sem regeneração) devolveu um bloco de código
  vazio e uma saída de teste stale (nome de teste que nem existia no arquivo pedido).
- **Mudança de metodologia**: dado o padrão de fabricação em tarefa de transcrição pura (sem
  nenhuma decisão criativa envolvida), o `TitleCatalog.kt` e o `TitleCatalogTest.kt` foram
  **escritos diretamente por Claude** (fonte real disponível na conversa) e colados manualmente
  por Bruno no projeto, com o AI Studio usado só para rodar build/testes — não para gerar ou
  reescrever o conteúdo. Essa abordagem deu certo de primeira. **Recomendação para o futuro**:
  para catálogos/dados extensos e literais (não lógica), preferir esse fluxo em vez de delegar
  a transcrição ao AI Studio.

**Validação:**
- Build: sucesso.
- Testes: 48 no total (40 pré-existentes + 2 `CombatLogicTest` + 6 `TitleCatalogTest`,
  substituindo o 1 teste incorreto das tentativas anteriores). Conteúdo do catálogo conferido
  campo a campo por Claude contra o arquivo fonte real após a colagem manual — 100% de match.

**Desvios de escopo aprovados:**
- Nenhum (a mudança de metodologia foi uma decisão de processo, não um desvio de conteúdo).

---

## [2026-07-29] Bloco: 9 — `ItemInspectModal`

**Arquivos criados/alterados:**
- /app/src/main/java/com/iurispraecepta/herolog/ui/components/ItemInspectModal.kt (novo)
- /app/src/test/java/com/iurispraecepta/herolog/ItemInspectModalScreenshotTest.kt (novo, 4 testes)

**Resumo:**
- Port fiel de `ItemInspectModal.tsx`, componente base compartilhado por `CharacterScreen`
  (slots de equipamento, buffs ativos) e `InventoryScreen` (itens da mochila) — ainda não
  portados.
- Backdrop próprio (preto 80% + blur 4dp condicional a `SDK_INT >= 31`), distinto do
  `HeroLogModal` (que usa stone-950/80 + blur 2dp) — componentes com tokens visuais diferentes
  no React, não unificados propositalmente.
- Reaproveita `ModalCountRegistry` (Bloco 6.2) para `increment`/`decrement`.
- Padrão `currentItem` local para manter o conteúdo do item visível durante os 200ms de
  animação de saída, mesmo depois de `item` virar `null` — solução não pedida explicitamente,
  mas resolve corretamente o mesmo tipo de problema do Bloco 3.1 (animação de saída cortada).
- Assimetria de largura dos botões de ação (`success`/`primary`/`amber` crescem, `danger` só
  quando é ação única, `stone` nunca cresce) replicada fielmente do `getButtonClass` original.

**Validação:**
- Build: sucesso.
- Testes: 52 no total (48 pré-existentes + 4 novos), nominal completo, soma conferida e
  correta.
- Visual: modo record, ainda sem inspeção humana.

**Desvios de escopo aprovados:**
- Lógica de crescimento (`isGrowing`) da variante `Stone` incorretamente herdava `isSingle`
  (cresceria sozinha, quando o React nunca aplica `w-full`/`flex-1` a essa variante) — **não
  aprovado**, corrigido no mesmo ciclo antes de fechar o bloco. Sem impacto visual atual (nenhum
  consumidor conhecido usa a variante `Stone` ainda), mas corrigido por ser componente genérico
  reutilizável.

---

## [2026-07-29] Bloco: 10 — `CharacterScreen` (versão mobile)

**Arquivos criados/alterados:**
- /app/src/main/java/com/iurispraecepta/herolog/model/CharacterModels.kt (`CharacterSummary`
  adicionado)
- /app/src/main/java/com/iurispraecepta/herolog/ui/character/CharacterScreen.kt (novo)
- /app/src/test/java/com/iurispraecepta/herolog/CharacterScreenScreenshotTest.kt (novo, 2 testes)

**Resumo:**
- Port fiel da versão mobile de `CharacterScreen.tsx` (`sm:hidden`) — versão desktop
  deliberadamente ignorada, não se aplica ao Android.
- Avatares reais (`mage_idle`/`warrior_idle`/`ranger_idle`, subidos por Bruno em
  `res/drawable/`) mapeados via `painterResource`, com `warrior_idle` como fallback.
- XP de combate via `CombatLogic.requiredXpForCombatLevel` — segunda ocorrência da fórmula
  consolidada, não duplicada.
- Duas formas de desequipar (botão `×` inline no slot + `ItemInspectModal` com ação
  "Desequipar") ambas funcionais, replicando o React.
- Buffs ativos abrem `ItemInspectModal` sem ações (`inspectingSlotIdx = null`), diferente dos
  slots de equipamento.
- Título equipado buscado em `TITLE_CATALOG`; se não encontrado, trata como se não houvesse
  título (replica `if (!found) return null` do React).
- `TitleEquipModal` real ainda não existe — placeholder simples ("Em construção") no lugar,
  será substituído no Bloco 11.

**Validação:**
- Build: sucesso.
- Testes: 54 no total (52 pré-existentes + 2 novos), nominal completo, soma conferida e
  correta.
- Visual: modo record, ainda sem inspeção humana.

**Desvios de escopo aprovados:**
- Ícone de HP veio como emoji estático ("❤️ Pontos de Vida:") em vez do ícone real com
  animação de pulso do React (`Heart` + `animate-pulse`) — **não aprovado**, corrigido no mesmo
  ciclo (ícone `Icons.Default.Favorite` com pulso de alpha, rótulo "HP (PONTOS DE VIDA)" e cor
  `rose-400` corretos).

---

## [2026-07-29] Bloco: 11 — Plugar `CharacterScreen` na `MainActivity` (preview visual)

**Arquivos criados/alterados:**
- /app/src/main/java/com/iurispraecepta/herolog/MainActivity.kt (reescrito)

**Resumo:**
- Navegação mínima por abas (`TabRow`, 2 tabs: "Habilidades"/"Personagem") controlando qual
  tela renderiza — não é o `BottomNav` definitivo do app, só scaffolding temporário pra validar
  visualmente as duas telas lado a lado.
- FAB de criar skill oculto automaticamente fora da aba "Habilidades".
- `onUnequipItem` usa `InventoryLogic.unequipItem` de verdade, atualizando estado em memória
  (mesmo padrão do Bloco 6 com `SkillLogic`).
- Dados de exemplo do personagem reaproveitados do teste de screenshot do Bloco 10 (mesmo
  personagem, equipamentos e buffs), evitando inventar dados novos sem necessidade.

**Validação:**
- Build: sucesso, confirmado literalmente (duas vezes: applet + suíte JVM completa).
- Testes: suíte intacta (nenhum teste novo, escopo era só wiring).
- Visual: pendência de inspeção humana no preview — nota entregue a Bruno sobre uma descrição
  em prosa da resposta anterior mencionar um badge "Campeão da Arena" que não existe no código
  real (`TITLE_CATALOG` tem `name = "Campeão"`); alucinação só na descrição, não no código.

**Desvios de escopo aprovados:**
- Nenhum.

---

## [2026-07-29] Bloco: 12 — `TitleEquipModal` (substituindo o placeholder)

**Arquivos criados/alterados:**
- /app/src/main/java/com/iurispraecepta/herolog/ui/character/TitleEquipModal.kt (novo)
- /app/src/main/java/com/iurispraecepta/herolog/ui/character/CharacterScreen.kt (placeholder
  substituído pelo modal real)
- /app/src/test/java/com/iurispraecepta/herolog/TitleEquipModalScreenshotTest.kt (novo, 2 testes)

**Resumo:**
- Port fiel de `TitleEquipModal.tsx`: agrupamento em 5 seções (Lendário/Épico/Raro/Comum/
  Conquistas, com Achievement+Drop caindo ambos em "Conquistas"), seções vazias puladas.
- **Achado importante de arquitetura**: os cards de título reaproveitam `TitleItem.visualStyle`
  (já existente desde o Bloco 8) para a cor quando não-equipado — as cores de
  `categoriesConfig` (rose/purple/amber/stone/sky) são só para os cabeçalhos de seção, conjunto
  totalmente separado. Evitou duplicação de lógica de cor.
- Ícone `Award` do lucide aproximado como `Icons.Default.MilitaryTech` (sem disclosure formal
  na resposta, registrado aqui).

**Validação:**
- Build: sucesso.
- Testes: 56 no total (54 pré-existentes + 2 novos), nominal completo, soma conferida e
  correta.
- Visual: modo record, ainda sem inspeção humana.

**Desvios de escopo aprovados:**
- Padding reservado (`pr-20`) pra evitar sobreposição com o badge "Equipado" veio condicional
  a `isEquipped` na primeira entrega, quando o React aplica isso sempre, independente do
  estado — **não aprovado**, corrigido no mesmo ciclo.

---

## NOTA DE PROCESSO — Reconstrução retroativa dos Blocos 13 a 19.1

Este arquivo ficou sem atualização entre o Bloco 12 (2026-07-29) e o momento desta nota — os
Blocos 13 a 19.1 foram implementados e validados numa sessão de chat contínua sem que as
entradas correspondentes fossem registradas aqui, violando a própria regra deste arquivo
("adicionada imediatamente após o bloco ser implementado e validado nesta mesma sessão — nunca
reconstruída de memória depois"). As entradas abaixo foram escritas retroativamente, a partir do
histórico completo da conversa (incluindo saídas literais de build/teste já fornecidas pelo AI
Studio naquele momento, não de memória vaga) — mas marco isso explicitamente porque a garantia
de contemporaneidade que a regra original oferece não se aplica a elas. A partir do Bloco 20,
volta o registro imediato, um bloco por vez.

---

## [2026-08-05] Bloco: 13 — Plugar `InventoryScreen` na `MainActivity` (terceira aba)

**Arquivos criados/alterados:**
- /app/src/main/java/com/iurispraecepta/herolog/MainActivity.kt (terceira aba "Inventário")

**Resumo:**
- `TabRow` ganhou terceira aba. Estado de `inventory`/`equippedEquipment` passou a ser
  **compartilhado de verdade** entre as abas Personagem e Inventário (mesma variável
  `mutableStateOf`, não duas cópias) — ponto central do bloco, prova de integração real.
- Callbacks reais via `InventoryLogic` (equip/sell/discard), fechando o modal de inspeção após
  cada ação, replicando o comportamento do `useInventory.ts` real.
- Gold ainda não existe como estado compartilhado no scaffolding — venda só loga o valor via
  `Log.d` com TODO apontando pro `gameState` real futuro.

**Validação:**
- Build: sucesso.
- Testes: 59 no total, suíte intacta (escopo era só wiring, sem lógica nova testável).
- Visual: confirmado por Bruno via prints reais — equipou "Espada Inabalável" num slot ocupado
  pela "Espada Flamejante" de exemplo, confirmando a troca correta
  (`equipItem_occupiedSlot_returnsPreviousItemToInventory`) refletida nas duas abas
  simultaneamente.

**Desvios de escopo aprovados:**
- Nenhum.

---

## [2026-08-06] Bloco: 14 — `QuestLogic.kt` (LCG determinístico) + `QuestCatalog.kt`/`QuestModels.kt`

**Arquivos criados/alterados:**
- /app/src/main/java/com/iurispraecepta/herolog/logic/quests/QuestModels.kt (novo, escrito
  diretamente por Claude)
- /app/src/main/java/com/iurispraecepta/herolog/logic/quests/QuestCatalog.kt (novo, escrito
  diretamente por Claude — 20 quests diárias + 3 quests de guilda, catálogo literal extenso,
  mesma metodologia do `TitleCatalog.kt` pós-incidente do Bloco 8)
- /app/src/main/java/com/iurispraecepta/herolog/logic/quests/QuestLogic.kt (novo, via AI Studio)
- /app/src/test/java/com/iurispraecepta/herolog/QuestCatalogTest.kt (novo, 6 testes)
- /app/src/test/java/com/iurispraecepta/herolog/QuestLogicTest.kt (novo, 8 testes)

**Resumo:**
- Port fiel de `useGuildQuests.ts`: catálogo de quests, rotação LCG (`toDateString()` inglês
  para seed, Fisher-Yates, `rand()` via unsigned 32-bit), sistema de claim com fallback legado.
- **Validação mais forte já usada no projeto até este ponto**: valor esperado da rotação para
  04/08/2026 calculado de forma independente via simulação em Python (fora do Kotlin), embutido
  como teste obrigatório (`mandatoryTest_rotateDailyQuests_aug4_2026`) — não é só "roda sem
  erro", é um número calculado por fora que precisa bater exatamente.
- Risco registrado para o futuro: `todaySessions`/`todaySkillNames` dependem de
  `HistoryEntry.date` nascer no formato `dd/MM/yyyy` (via `todayLocalStr`, pt-BR) quando o
  módulo de Histórico for portado.

**Validação:**
- Build: sucesso.
- Testes: 73 no total (14 novos), nominal completo via script Python de contagem por XML.
- Visual: N/A (lógica pura, sem UI).

**Desvios de escopo aprovados:**
- Nenhum desvio de escopo, mas **incidente de processo registrado**: a primeira resposta do AI
  Studio trouxe um resumo em prosa afirmando "73/73 PASSED" com nomes de teste parcialmente
  fabricados (não batiam com a lista XML real). Resolvido exigindo a saída crua do script Python
  de contagem — reforça a regra de nunca aceitar resumo em prosa como prova de teste.

---

## [2026-08-06] Bloco: 15 — `Skill.id: String` (correção de divergência de modelo)

**Arquivos criados/alterados:**
- /app/src/main/java/com/iurispraecepta/herolog/model/CharacterModels.kt (`Skill` ganha campo
  `id: String`, default `UUID.randomUUID().toString()`)
- /app/src/test/java/com/iurispraecepta/herolog/SkillLogicTest.kt (ids explícitos e estáveis
  adicionados aos `Skill` de teste: `sk_foco`, `sk_treino`, `sk_98`, `sk_99`)
- /app/src/main/java/com/iurispraecepta/herolog/MainActivity.kt (`sampleSkills` com ids
  explícitos: `sample_skill_estudos`, `sample_skill_programacao`, `sample_skill_academia`)

**Resumo:**
- Auditoria pós-Bloco 1 encontrou que `Skill.kt` não tinha o campo `id` presente no `Skill` real
  de `types.ts` — o `PARIDADE.md` afirmava fidelidade 1:1 incorretamente. Decisão consciente:
  adicionar por fidelidade + motivo arquitetural (key estável para `LazyColumn`/Compose no
  futuro, e futuro sync com Supabase já roteirizado no handoff original).
- Auditoria dos testes alterados confirmou que nenhum teste de `SkillLogicTest` compara `Skill`
  por igualdade total (todos comparam campo a campo) — o id randômico de `addCustomSkill` não
  exigiu tratamento especial além de simplesmente não ser testado.

**Validação:**
- Build: sucesso.
- Testes: 73 no total, suíte intacta (nenhum teste novo, só ajuste de dados existentes).
- Visual: N/A.

**Desvios de escopo aprovados:**
- Nenhum (decisão de adicionar o campo já era o próprio objetivo do bloco, não um desvio).

---

## [2026-08-06] Bloco: 16 — `AchievementCatalog.kt`/`AchievementLogic.kt`

**Arquivos criados/alterados:**
- /app/src/main/java/com/iurispraecepta/herolog/logic/achievements/AchievementModels.kt (novo,
  escrito diretamente por Claude)
- /app/src/main/java/com/iurispraecepta/herolog/logic/achievements/AchievementCatalog.kt (novo,
  escrito diretamente por Claude — 8 conquistas, catálogo literal)
- /app/src/main/java/com/iurispraecepta/herolog/logic/achievements/AchievementLogic.kt (novo,
  escrito diretamente por Claude)
- /app/src/test/java/com/iurispraecepta/herolog/AchievementCatalogTest.kt (novo, 5 testes)
- /app/src/test/java/com/iurispraecepta/herolog/AchievementLogicTest.kt (novo, 3 testes)

**Resumo:**
- Port fiel de `ACHIEVEMENTS_LIST` (`AchievementsTab.tsx`) e da lógica
  `isUnlocked = state.achievements.includes(id) || check(state)`.
- Investigação anterior (durante o Bloco 14) tinha levantado dúvida sobre um possível sistema
  duplicado de conquistas (`testAchievements` inline em `handleConfirmClaimRewards`, só 4 dos 8
  ids). Resolvido: não são sistemas conflitantes, é um OR complementar por design — `check()` ao
  vivo sempre correto, array `achievements[]` só dispara notificação no momento do evento.

**Validação:**
- Build: sucesso, catálogo/lógica compilaram sem alteração (arquivos colados diretamente).
- Testes: 81 no total (8 novos), nominal completo.
- Visual: N/A.

**Desvios de escopo aprovados:**
- Nenhum.

---

## [2026-08-07] Bloco: 17 — `FocusRewardsLogic.kt`/`FocusApplyLogic.kt` (núcleo do Módulo Foco)

**Arquivos criados/alterados:**
- /app/src/main/java/com/iurispraecepta/herolog/logic/focus/FocusModels.kt (novo, Claude)
- /app/src/main/java/com/iurispraecepta/herolog/logic/focus/LootTable.kt (novo, Claude — 14
  itens, catálogo literal)
- /app/src/main/java/com/iurispraecepta/herolog/logic/focus/LootConfig.kt (novo, Claude)
- /app/src/main/java/com/iurispraecepta/herolog/logic/focus/FocusRewardsLogic.kt (novo, Claude —
  equivalente puro de `completeFocusQuest`, ~25 condicionais de perk de título)
- /app/src/main/java/com/iurispraecepta/herolog/logic/focus/FocusApplyLogic.kt (novo, Claude —
  equivalente puro de `handleConfirmClaimRewards`)
- /app/src/test/java/com/iurispraecepta/herolog/LootConfigTest.kt (novo, 5 testes)
- /app/src/test/java/com/iurispraecepta/herolog/FocusRewardsLogicTest.kt (novo, 7 testes)
- /app/src/test/java/com/iurispraecepta/herolog/FocusApplyLogicTest.kt (novo, 5 testes)

**Resumo:**
- Confirmado por auditoria de fonte que `completeFocusQuest`/`handleConfirmClaimRewards` são
  duas fases distintas (calcular vs. aplicar), não uma função só — portadas como dois arquivos
  espelhando essa separação.
- `Random` injetável em `FocusRewardsLogic.calculate()` para testabilidade determinística de
  loot/drop de título.
- **Melhoria consciente registrada**: `FocusApplyLogic` usa o catálogo completo de 8 conquistas
  (`AchievementCatalog`/`AchievementLogic.isUnlocked`) contra o estado já atualizado, em vez da
  lista de 4 ids hardcoded do React (`testAchievements`) — decisão tomada com Bruno após
  discussão sobre se a lista de 4 era proposital ou um gap de manutenção não confirmável.

**Validação:**
- Build: sucesso.
- Testes: 98 no total (17 novos), nominal completo.
- Visual: N/A (lógica pura).

**Desvios de escopo aprovados:**
- Melhoria consciente de conquistas descrita acima — aprovada explicitamente por Bruno antes da
  implementação.

---

## [2026-08-07] Bloco: 17.1 — Correção de testes faltantes críticos do Bloco 17

**Arquivos criados/alterados:**
- /app/src/test/java/com/iurispraecepta/herolog/FocusRewardsLogicTest.kt (3 testes adicionados)
- /app/src/test/java/com/iurispraecepta/herolog/FocusApplyLogicTest.kt (4 testes adicionados)

**Resumo:**
- Bloco 17 tinha ficado sem os dois testes mais importantes: a assimetria do título
  THUNDERSTRUCK (gold incondicional, xp condicional a wilderness — único trecho da fonte onde
  uma condicional protege só parte de um bloco) e a prova de que a melhoria consciente de
  conquistas realmente desbloqueia uma conquista que o React nunca cobriria (`xp_10000`).
- Adicionados também: caso negativo do bônus de masmorra, teste explícito de "sem loot",
  streak em dia novo vs. mesmo dia, formato de data do `HistoryEntry` via regex,
  `survive_wilderness` automático via `candidateState`.

**Validação:**
- Build: sucesso.
- Testes: 105 no total (7 novos), nominal completo. Assimetria THUNDERSTRUCK conferida
  manualmente contra a fórmula (Caso A: 50 XP/82 GP sem wilderness; Caso B: 75 XP/101 GP com
  wilderness) — bateu exato.
- Visual: N/A.

**Desvios de escopo aprovados:**
- Nenhum (bloco de correção, não introduziu desvio novo).

---

## [2026-08-07] Bloco: 17.2 — Correção consciente: `totalXP` nunca incrementado (bug real da fonte)

**Arquivos criados/alterados:**
- /app/src/main/java/com/iurispraecepta/herolog/logic/focus/FocusApplyLogic.kt (1 linha:
  `totalXP = state.totalXP + calc.xpEarned` no `candidateState`)
- /app/src/test/java/com/iurispraecepta/herolog/FocusApplyLogicTest.kt (1 teste:
  `apply_incrementsTotalXp`)

**Resumo:**
- Auditoria da fonte real (`handleConfirmClaimRewards`) confirmou que `totalXP` nunca é
  incrementado no `gameState` do React — só `todayXP`. Evidência: o próprio código de checagem
  de conquista `xp_1000` recalcula `prev.totalXP + finalXP` ali mesmo, em vez de usar um valor
  já sincronizado, sinal de que o campo fica congelado desde a criação do personagem.
- Diferente do bug do bônus de masmorra (já corrigido na fonte atual), este é um bug real e
  **ainda presente** na fonte React no momento da auditoria. Decisão consciente, aprovada por
  Bruno: corrigir no Android.

**Validação:**
- Build: sucesso.
- Testes: 106 no total (1 novo), nominal completo.
- Visual: N/A.

**Desvios de escopo aprovados:**
- Correção consciente de bug real da fonte (não uma regressão evitada, uma melhoria real) —
  aprovada explicitamente por Bruno.

---

## [2026-08-08] Bloco: 18 — `FocusOrb.kt` (componente visual do timer)

**Arquivos criados/alterados:**
- /app/src/main/java/com/iurispraecepta/herolog/ui/focus/FocusOrb.kt (novo, escrito diretamente
  por Claude — alto risco de fabricação por AI Studio dado o volume de cores/gradientes/paths)
- /app/src/test/java/com/iurispraecepta/herolog/FocusOrbScreenshotTest.kt (novo, 5 testes)

**Resumo:**
- Port de `FocusOrb.tsx`: SVG animado via `requestAnimationFrame` reescrito como `Canvas` +
  loop manual (`LaunchedEffect` com `delay(33)`, mesmo throttle ~30fps do original), preservando
  o comportamento de *parar* a animação (não só zerar amplitude) quando pausado/parado.
- Path de onda líquida com 12 pontos, mesma frequência (~1.2 ciclos), gradientes por estado
  (trabalho/urgente/pausado/descanso) transcritos 1:1 dos hex codes SVG reais.
- Breakpoints responsivos `sm:`/`lg:` do React (inaplicáveis, app mobile-only) substituídos por
  checagem de `screenWidthDp >= 390` (único breakpoint real alcançável em celular).
- **2 simplificações conscientes registradas como pendência**, aprovadas por Bruno: (1) glow
  externo com blur omitido (`Modifier.blur` só API 31+), (2) bolhas ambientes decorativas
  omitidas inteiramente. Nenhuma afeta forma/cor/animação da onda ou texto do timer.

**Validação:**
- Build: sucesso, 1 import faltante corrigido (`clipPath`) — única alteração no arquivo
  original, confirmada como não afetando lógica de desenho.
- Testes: 111 no total (5 novos, screenshot em modo record).
- Visual: pendente de inspeção humana neste bloco (resolvida no Bloco 19.1).

**Desvios de escopo aprovados:**
- As 2 simplificações conscientes descritas acima.

---

## [2026-08-08] Bloco: 19 — Plugar `FocusOrb` na `MainActivity` (quarta aba, preview)

**Arquivos criados/alterados:**
- /app/src/main/java/com/iurispraecepta/herolog/MainActivity.kt (quarta aba "Foco (Preview)",
  novo Composable `FocusOrbPreviewScreen`)

**Resumo:**
- Scaffolding temporário isolado (sem `CharacterState`/`FocusRewardsLogic` reais ainda): estado
  local de `timeLeft`/`isRunning`/`isPaused`/`isBreakActive`/`orbSize`, countdown real via
  `LaunchedEffect` com loop `delay(1000)`, controles de Play/Pause/Reset/Alternar Descanso/
  Alternar Tamanho.

**Validação:**
- Build: sucesso.
- Testes: 111 no total, suíte intacta (wiring de preview, sem lógica nova testável).
- Visual: **primeira rodada de prints não mostrou o líquido do orbe** — só o anel pontilhado e o
  texto apareciam. Causa raiz identificada na mesma sessão: erro no scaffolding do preview, não
  no `FocusOrb.kt` (ver Bloco 19.1).

**Desvios de escopo aprovados:**
- Nenhum.

---

## [2026-08-08] Bloco: 19.1 — Correção de escala do preview (`totalSeconds`)

**Arquivos criados/alterados:**
- /app/src/main/java/com/iurispraecepta/herolog/MainActivity.kt (1 linha: `totalSeconds = 1500`
  → `totalSeconds = 90` em `FocusOrbPreviewScreen`)

**Resumo:**
- Diagnóstico: `totalSeconds` fixo em 1500 com `timeLeft` inicial de 90 mantinha `progress`
  sempre ≈ 6%, deixando a faixa de líquido do orbe (calculada sobre um viewBox lógico de 100
  unidades) visualmente imperceptível mesmo com o desenho correto. Ajuste isolado no
  scaffolding, sem tocar em `FocusOrb.kt`.

**Validação:**
- Build: sucesso.
- Testes: 111 no total, suíte intacta.
- Visual: **confirmado por Bruno via prints reais** — líquido preenchendo conforme progresso,
  gradiente âmbar (trabalho) e verde-esmeralda (descanso) corretos, ondulação da superfície
**Desvios de escopo aprovados:**
- Nenhum.

---

## [2026-08-08] Bloco: 20 — `FocusModeScreen.kt` (tela imersiva de foco)

**Arquivos criados/alterados:**
- /app/src/main/java/com/iurispraecepta/herolog/ui/focus/FocusModeScreen.kt (novo, escrito
  diretamente por Claude)
- /app/src/test/java/com/iurispraecepta/herolog/FocusModeScreenScreenshotTest.kt (novo, 5
  testes)

**Resumo:**
- Port de `FocusModeScreen.tsx`: pulso radial de fundo (`rememberInfiniteTransition`), tags de
  modo (Masmorra/Wilderness) condicionais, cabeçalho com emoji+nome de skill, `FocusOrb`
  centralizado (tamanho FULLSCREEN), barra inferior com botões Pausar/Retomar (estilo condicional
  a `isPaused`) e Sair.
- Componente deliberadamente apresentacional: não gerencia timer nem decide desmontagem —
  aguarda bloco futuro de wiring real.
- 2 simplificações conscientes registradas: chuva de partículas cintilantes omitida; atalho de
  teclado e auto-foco de botão tratados como N/A (não se aplicam a touch/mobile).
- 2 correções sintáticas legítimas identificadas pelo AI Studio (erros meus na primeira
  entrega, confirmados por auditoria do diff completo antes de aprovar): `OutlinedButtonDefaults`
  não existe como objeto separado no M3 (corrigido para `ButtonDefaults.outlinedButtonColors`);
  import de `weight` inválido (é extension function de `RowScope`/`ColumnScope`, não top-level)
  removido sem afetar as chamadas `.weight(1f)` já existentes no corpo.

**Validação:**
- Build: sucesso.
- Testes: 116 no total (5 novos), nominal completo, conferido contra a lista suíte-por-suíte.
- Visual: pendente — próximo passo é plugar na `MainActivity` para inspeção humana.

**Desvios de escopo aprovados:**
- As 2 simplificações conscientes descritas acima.

---

## [2026-08-08] Bloco: 21 — Plugar `FocusModeScreen` na `MainActivity` (preview imersivo)

**Arquivos criados/alterados:**
- /app/src/main/java/com/iurispraecepta/herolog/MainActivity.kt (`FocusOrbPreviewScreen`
  expandido com seção de entrada em modo imersivo, sem remover os controles do Orb isolado)

**Resumo:**
- Adicionados 2 checkboxes de preview (Masmorra/Wilderness) + botão "Entrar em Modo Foco" que
  sobrepõe `FocusModeScreen` em tela cheia (`Box` externo, composição por cima do conteúdo da
  aba) com countdown real via `LaunchedEffect`.
- Confirmação visual humana completa: cobertura de tela cheia sem vazamento de chrome, tags de
  modo corretas (isoladas e combinadas), estado urgente (vermelho, <60s) disparando dentro da
  tela, Pausar/Sair funcionais, `FocusOrb` recebendo tempo real da tela pai — fecha a validação
  visual de `FocusModeScreen.kt` (Bloco 20).

**Validação:**
- Build: sucesso.
- Testes: 116 no total, suíte intacta (wiring de preview, sem lógica nova testável).
- Visual: **confirmado por Bruno via 5 prints reais**, incluindo teste do estado urgente e das
  duas tags de modo simultâneas.

**Desvios de escopo aprovados:**
- Nenhum.

**Achado durante o teste visual (não é bug do bloco, é lacuna de escopo ainda não portada):**
- Bruno identificou que Masmorra e Wilderness são mutuamente exclusivas no React real, mas essa
  regra não vive em `FocusModeScreen.tsx` (componente puramente apresentacional, replicado
  fielmente). A exclusão mútua deve viver na tela de configuração pré-sessão (ainda não
  localizada/portada — provavelmente handlers de checkbox que setam
  `sessionConfig.isDungeonMode`/`isWildernessChecked`). Registrado como risco pendente no
  `PARIDADE.md`, seção 8. O scaffolding de preview deste bloco permite a combinação inválida —
  correção adiada até a regra real ser confirmada por auditoria de fonte.

---

## [2026-08-09] Bloco: 22 — `RaidModeSection.kt` (segmented control Padrão/Masmorra/Selvagem)

**Arquivos criados/alterados:**
- /app/src/main/java/com/iurispraecepta/herolog/ui/focus/RaidModeSection.kt (novo, escrito
  diretamente por Claude — número mágico de cor/threshold, regra 4 da metodologia)
- /app/src/main/java/com/iurispraecepta/herolog/MainActivity.kt (`FocusOrbPreviewScreen`: os 2
  `Checkbox` independentes de Masmorra/Wilderness do Bloco 21 substituídos pelo par
  `RaidModeSegmentedControl`/`RaidModeInfoBox`; import de `Checkbox` removido por não ter mais
  uso)
- /app/src/test/java/com/iurispraecepta/herolog/RaidModeSectionTest.kt (novo, 4 testes)
- /app/src/test/java/com/iurispraecepta/herolog/RaidModeSectionScreenshotTest.kt (novo, 5 testes)

**Resumo:**
- Auditoria de fonte (JSX real do `App.tsx`, tela de configuração pré-sessão, colado por Bruno)
  revelou que a exclusividade Masmorra/Wilderness (achado do Bloco 21) não é um par de
  checkboxes com regra de exclusão aplicada depois — é um **segmented control de 3 estados**
  ("Padrão"/"Masmorra ⚔️"/"Selvagem 💀"), cada botão setando os dois booleans legados
  explicitamente no `onClick`.
- Decisão de port: modelado como `enum class RaidMode { PADRAO, MASMORRA, SELVAGEM }` — exclusão
  mútua garantida pelo tipo, não por disciplina de handler. `raidModeFrom()`/`toLegacyFlags()`
  convertem nos dois sentidos para não alterar o formato real persistido em
  `SessionConfig`/`ActiveSession` (que continuam com os 2 booleans).
- `RaidModeInfoBox` porta a caixa contextual abaixo do controle (texto literal do JSX real, não
  parafraseado — copy do próprio produto). Bloco de ajuda "Padrão" (chance de saque) faz wiring
  real com `LootConfig.calculateLootChance` já existente (sem duplicar constantes) via
  `lootChancePercentFrom()`/`buildStandardLootHelpBlocks()`.
- Mesmo trecho de fonte revelou **2 riscos novos, registrados no `PARIDADE.md`, ainda não
  portados**: cooldown de Masmorra de 2h (`dungeonCooldownRemaining`/`lastDungeonClearedTime`,
  fonte ainda não obtida — `formatDungeonCooldownStub()` é só placeholder de leiaute) e
  cancelamento automático do bônus Selvagem ao minimizar a aba (mais crítico, afeta
  `FocusRewardsLogic`/`FocusApplyLogic` já validados — fonte ainda não obtida).
- Plugado apenas no *scaffolding de preview* (`FocusOrbPreviewScreen`), não na tela de
  configuração pré-sessão real de produção (que ainda não existe como componente próprio no
  Android — só existe hoje dentro do `App.tsx` no React).
- 1 correção sintática legítima do AI Studio (erro meu, confirmado por auditoria do diff
  completo antes de aprovar): faltava `import androidx.compose.material3.Text` no arquivo
  original — sem ele o arquivo não compilava.

**Validação:**
- Build: sucesso.
- Testes: 125 no total (9 novos: 4 unitários para `raidModeFrom`/`toLegacyFlags`/roundtrip/
  `lootChancePercentFrom`, 5 screenshots para os estados Padrão/Masmorra/Selvagem/desabilitado/
  cooldown), nominal completo, conferido contra a lista suíte-por-suíte e contra a matemática
  116→125.
- Visual: **golden images gravadas pela primeira vez nesta execução** (`recordRoborazziDebug`,
  não existiam referências prévias) — o `5/5 PASSED` confirma que o Roborazzi bateu contra a
  própria referência recém-criada, **não que o visual esteja correto**, diferente de
  `FocusOrbScreenshotTest`/`FocusModeScreenScreenshotTest` (Blocos 19–20), que têm confirmação
  humana real via print. Descrição textual literal obtida do AI Studio (cores/pixels por
  imagem, sem acesso a anexo de imagem no ambiente) é internamente consistente com os valores
  reais de `RaidColors` e com a estrutura do componente (ex.: altura extra de
  `masmorra_cooldown` bate com a segunda linha de texto do cooldown), o que dá alguma confiança,
  mas **não substitui inspeção humana das 5 PNGs em
  `app/src/test/screenshots/raid_mode_section_*.png`** antes de considerar o visual fechado.
- Espaçamento entre `RaidModeSegmentedControl` e `RaidModeInfoBox`: dúvida do bloco anterior
  esclarecida — é um único `Spacer(8.dp)`, sem duplicação; o spacer de contexto que sobrou no
  diff fica depois do `RaidModeInfoBox`, separando o bloco inteiro do próximo elemento
  (provavelmente o botão "Entrar em Modo Foco" do Bloco 21), não entre control/infobox.
- **Decisão consciente (Bruno, 08/09)**: PNGs do Roborazzi não localizáveis/acessíveis no
  ambiente AI Studio Build atual — inspeção visual humana adiada até a tela real de configuração
  pré-sessão ser plugada na `MainActivity`/navegação de produção (fora do scaffolding de
  preview), momento em que dá pra testar ao vivo no emulador sem depender do Roborazzi.

---

## [2026-08-09] Bloco: 23 — `CognitiveDeathLogic.kt` (regras puras da Morte Cognitiva)

**Arquivos criados/alterados:**
- /app/src/main/java/com/iurispraecepta/herolog/logic/focus/CognitiveDeathLogic.kt (novo,
  escrito diretamente por Claude — número mágico de threshold/constante, regra 4 da metodologia)
- /app/src/test/java/com/iurispraecepta/herolog/CognitiveDeathLogicTest.kt (novo, 9 testes)

**Resumo:**
- Auditoria de fonte (`useFocusSession.ts` real, colado por Bruno) revelou que o "cancelamento
  do bônus Selvagem" registrado como risco nos Blocos 21/22 é, na verdade, um subsistema inteiro
  de infração/morte ("Morte Cognitiva"), não portado nem escopado até agora — achado registrado
  em detalhe no `PARIDADE.md`.
- Primeiro bloco da fatia: só a lógica pura, sem lifecycle/UI/aplicação em `CharacterState`.
  3 funções: `resolveWildernessInfraction` (título `DEATH-PROOF` → converte em pausa, resto →
  inicia grace period), `resolveCognitiveDeath` (rolagem de 15% de salvamento de streak da
  classe Ranger via `Random` injetável, reset de combo), `resolveRespawn` (penalidade de
  reviver: -1 nível de combate com piso 1, -50 GP com piso 0, XP de combate zerado).
- **Decisão de escopo aprovada por Bruno**: o gatilho (no React, `visibilitychange`/
  `window.blur`) NÃO precisa de fidelidade estrita — não existe conceito de aba em Android
  nativo. Gatilho real será o app indo para background (`ON_STOP`/`ON_PAUSE`), a portar num
  bloco futuro de wiring. A lógica de negócio em si (grace period 3s, morte, streak/combo,
  exceção Ranger, penalidade de respawn) mantém fidelidade estrita com a fonte.
- `CharClass` confirmado como enum (não `String`) via exemplo real de `FocusRewardsLogic.kt`
  fornecido por Bruno (`state.charClass == CharClass.Mage`) — usado corretamente no arquivo.
  Import `com.iurispraecepta.herolog.model.CharClass` foi uma suposição de pacote (por analogia
  com `Skill` no mesmo pacote `.model`), **confirmada correta pelo AI Studio sem necessidade de
  ajuste** (`CharClass` vive em `CharacterModels.kt`, mesmo pacote).

**Validação:**
- Build: sucesso.
- Testes: 134 no total (9 novos, nominal completo conferido contra a lista suíte-por-suíte e
  contra a matemática 125→134; posição alfabética de `CognitiveDeathLogicTest` também conferida).
- Visual: N/A (lógica pura, sem componente visual neste bloco).

**Desvios de escopo aprovados:**
- Adaptação do gatilho de lifecycle (ver decisão de escopo acima) — não é desvio de fidelidade
  da regra de negócio, só do mecanismo de detecção, aprovado por não existir equivalente 1:1 em
  Android nativo.

**Próximos passos do fatiamento (não iniciados):**
- Bloco 2: UI do grace period (countdown 3s) + tela/estado de morte cognitiva.
- Bloco 3: wiring real do lifecycle Android chamando a lógica pura deste bloco.
- Pendência à parte: localizar a 2ª chamada de `completeFocusQuest` (achado secundário do
  Bloco 22, branch de saída antecipada em `isFocusMode && !isFocusCompleted`).

---

## [2026-08-09] Bloco: 24 — `DungeonCooldownLogic.kt` (cooldown real de Masmorra) + correção de `RaidModeSection.kt`

**Arquivos criados/alterados:**
- /app/src/main/java/com/iurispraecepta/herolog/logic/focus/DungeonCooldownLogic.kt (novo,
  escrito diretamente por Claude — regra 4 da metodologia)
- /app/src/main/java/com/iurispraecepta/herolog/ui/focus/RaidModeSection.kt (corrigido:
  `formatDungeonCooldownStub` removido, substituído pela função real importada de
  `DungeonCooldownLogic.kt`)
- /app/src/test/java/com/iurispraecepta/herolog/DungeonCooldownLogicTest.kt (novo, 8 testes)

**Resumo:**
- Fonte real de `App.tsx` (`checkCooldown`/`formatDungeonCooldown`) auditada, resolvendo o
  risco pendente desde o Bloco 21. Fórmula: `elapsed = now - lastClearedAt`,
  `cooldownTotal = 2h`, `remaining = max(0, cooldownTotal - elapsed)`. Formato real de exibição
  é **`HH:MM:SS`** — corrige o stub `"Xh Ym"` que estava em `RaidModeSection.kt` desde o Bloco
  22 (nunca tinha sido confirmado contra a fonte).
- **Achado**: no React, `lastDungeonClearedTime` é `useState<number>(0)` — não persiste em
  lugar nenhum, reseta a cada reload de página. **Decisão de escopo aprovada por Bruno**:
  divergência consciente — Android vai persistir esse campo de verdade (local ainda não
  decidido — provavelmente `CharacterState`/Room), diferente do React. `resolveDungeonCooldown
  RemainingMs` recebe `lastClearedAtMs` já pronto de quem chama (mesmo padrão de
  `referenceDate` injetável de `QuestLogic`/`FocusApplyLogic`), não decide onde persistir.
- **Achado paralelo, registrado mas não portado**: existe uma segunda superfície de UI pra
  escolher o modo de incursão, `IncursionModeModal.tsx` (modal com 3 cards, mesma lógica de
  negócio do segmented control já portado). **Decisão de escopo aprovada por Bruno**: só o
  segmented control por enquanto, modal fica como pendência separada.

**Validação:**
- Build: sucesso.
- Testes: 142 no total (8 novos, nominal completo conferido contra a lista suíte-por-suíte,
  matemática 134→142 e posição alfabética de `DungeonCooldownLogicTest` conferidas).
- Visual: N/A neste bloco (lógica pura); a correção do stub em `RaidModeSection.kt` só muda o
  texto exibido no cooldown, ainda dentro da mesma pendência de inspeção visual humana adiada
  (Bloco 22).

**Desvios de escopo aprovados:**
- Persistência real de `lastDungeonClearedTime` no Android (React não persiste) — ver achado
  acima.

**Pendências que continuam em aberto:**
- Localização real e persistência de `lastDungeonClearedTime` no modelo Android (onde salvar,
  quando escrever o valor — só ao concluir a 4ª sessão da masmorra).
- `IncursionModeModal.tsx` — 2ª superfície de UI, ainda não portada.
- Bloco 2/3 do fatiamento de Morte Cognitiva (UI do grace period + wiring de lifecycle).
- 2ª chamada de `completeFocusQuest` (achado do Bloco 22) — já resolvida via fonte
  (`FocusModeScreen.tsx`/`handleAutoClaim`), registrado no `PARIDADE.md`, ainda não portada.

**Desvios de escopo aprovados:**
- Nenhum (cooldown de Masmorra e cancelamento do bônus Selvagem deliberadamente fora de escopo
  deste bloco, ver riscos acima).

---

## [2026-08-11] Bloco: 25 — Persistência real de `lastDungeonClearedTime`

**Arquivos criados/alterados:**
- /app/src/main/java/com/iurispraecepta/herolog/model/CharacterModels.kt (campo novo em
  `CharacterState`)
- /app/src/main/java/com/iurispraecepta/herolog/logic/focus/FocusApplyLogic.kt (escrita do
  campo no `candidateState`)
- /app/src/test/java/com/iurispraecepta/herolog/FocusApplyLogicTest.kt (2 testes novos)

**Resumo:**
- Fecha a divergência consciente registrada nos Blocos 22/24: campo
  `lastDungeonClearedTime: Long = 0L` adicionado a `CharacterState`, persistido de verdade via
  blob JSON existente (`CharacterStateEntity.jsonPayload`) — sem migração de schema Room, já que
  o campo entra automaticamente na serialização.
- `FocusApplyLogic.apply()` escreve `referenceDate.time` no campo sempre que
  `calc.dungeonClearGoldBonus > 0` (mesmo sinal que `FocusRewardsLogic` já usa pra decidir o
  bônus de +2.500 GP na 4ª sessão) — reaproveita a detecção existente em vez de duplicá-la, e o
  `referenceDate` já injetável na função garante testabilidade determinística.
- Nenhuma alteração em `FocusRewardsLogic.kt` nem em `DungeonCooldownLogic.kt` (Bloco 24) —
  ambos já esperavam esse campo pronto (`lastClearedAtMs` injetável), só faltava a origem real
  do dado.

**Validação:**
- Build: sucesso.
- Testes: 144 no total (2 novos, nominal completo conferido contra a lista suíte-por-suíte,
  matemática 142→144 e posição alfabética de ambos dentro de `FocusApplyLogicTest` conferidas).
- Visual: N/A (lógica pura).

**Desvios de escopo aprovados:**
- Persistência real do campo no Android — React usa `useState<number>(0)` local, não persiste
  em lugar nenhum (reseta a cada reload). Divergência já aprovada nos Blocos 22/24, implementada
  aqui.

**Pendências que continuam em aberto:**
- `IncursionModeModal.tsx` — 2ª superfície de UI, ainda não portada.
- Bloco 2/3 do fatiamento de Morte Cognitiva (UI do grace period + wiring de lifecycle).
- 2ª chamada de `completeFocusQuest` (achado do Bloco 22) — resolvida via fonte
  (`FocusModeScreen.tsx`/`handleAutoClaim`), ainda não portada.
- Inspeção visual humana das 5 PNGs do `RaidModeSection.kt` (Bloco 22), adiada até a tela real
  de configuração pré-sessão ser plugada na `MainActivity`/navegação de produção.

---

## [2026-08-11] Bloco: 26 — Overlays de Morte Cognitiva (UI, sem wiring de lifecycle)

**Arquivos criados/alterados:**
- /app/src/main/java/com/iurispraecepta/herolog/ui/focus/CognitiveDeathOverlays.kt (novo —
  `WildernessGracePeriodOverlay` e `CognitiveDeathOverlay`)
- /app/src/main/java/com/iurispraecepta/herolog/ui/focus/FocusModeScreen.kt (4 parâmetros novos
  com defaults seguros: `isGraceActive`, `graceSecondsLeft`, `isPlayerDead`,
  `onReturnToFocusCap`, `onRespawn`)
- /app/src/test/java/com/iurispraecepta/herolog/FocusModeScreenScreenshotTest.kt (2 screenshot
  tests novos)

**Resumo:**
- 2ª frente do fatiamento de Morte Cognitiva (Bloco 23 = lógica pura, este bloco = UI). Fonte
  real localizada via busca por variável (`rg -l "isGraceActive"`), não em
  `FocusModeScreen.tsx` como se poderia supor — os dois overlays (`isGraceActive`/
  `graceSecondsLeft` e `isPlayerDead`) vivem em `App.tsx`, montados via `<AnimatePresence>`
  condicional consumindo o retorno de `useFocusSession()`. Corrigido no `PARIDADE.md`.
- **Achado importante**: `isPlayerDead` tem dois gatilhos reais e independentes na fonte — Morte
  Cognitiva (Terra Selvagem, já coberta) e dano por negligência de Diárias (módulo Hábitos,
  ainda não portado). O overlay de morte no React é agnóstico de causa (só olha o booleano). Os
  composables novos foram desenhados do mesmo jeito — genéricos, prontos pra também servir o
  gatilho de Hábitos no futuro sem retrabalho. **Decisão de escopo aprovada por Bruno**: o
  gatilho de dano por Diária fica fora de escopo até o módulo Hábitos ser portado.
- `FocusModeScreen.kt` recebeu os 4 parâmetros novos com defaults seguros — nenhuma chamada
  existente (produção ou os 5 screenshot tests já existentes do Bloco 20/22) precisou de ajuste.
- Wiring real de lifecycle (`ON_STOP`/`ON_PAUSE` disparando `resolveWildernessInfraction` de
  verdade) continua fora de escopo — é o Bloco 3, ainda pendente.

**Validação:**
- Build: sucesso.
- Testes: 146 no total (2 novos). **Nominal inicial recebido em prosa estava incorreto** — a
  suíte `AchievementCatalogTest` (5 testes) apareceu fundida/sumida do relatório resumido do AI
  Studio, resultando numa contagem manual de 141 contra o "Total: 146" declarado no rodapé.
  Rerun isolado (`--tests "com.iurispraecepta.herolog.AchievementCatalogTest"` +
  `AchievementLogicTest`) com XML bruto solicitado confirmou as duas suítes intactas (5 + 3 = 8
  testes, todos `PASSED`), fechando a divergência: 141 + 5 = 146 bate. Sem regressão real —
  erro de formatação do resumo em prosa, não do build. Método de checagem: nunca aceitar total
  declarado sem contagem manual da lista; pedir XML bruto quando a matemática não bater.
- Visual: pendente (screenshots dos 2 overlays novos não inspecionados visualmente ainda — mesmo
  padrão de adiamento consciente do Bloco 22).

**Desvios de escopo aprovados:**
- Overlays genéricos por design (booleanos simples, sem enum de "motivo da morte") — decisão
  registrada acima, antecipando o módulo Hábitos.
- Animações de "pulse"/"ping" do Tailwind simplificadas (fade/leve pulsação de opacidade em vez
  de reproduzir a curva exata) — decisão consciente por não afetar lógica de negócio.

**Pendências que continuam em aberto:**
- `IncursionModeModal.tsx` — 2ª superfície de UI, ainda não portada.
- Bloco 3 da Morte Cognitiva — wiring real de lifecycle Android (`ON_STOP`/`ON_PAUSE`) chamando
  `resolveWildernessInfraction`/`resolveCognitiveDeath`/`resolveRespawn` de verdade, plugando os
  overlays deste bloco a um estado real de sessão.
- Inspeção visual humana das 5 PNGs do `RaidModeSection.kt` (Bloco 22) + das 2 novas deste bloco,
  adiada até a tela real de configuração pré-sessão ser plugada na `MainActivity`/navegação de
  produção.
- 2ª chamada de `completeFocusQuest` (achado do Bloco 22) — resolvida via fonte, ainda não
  portada.
- Gatilho de dano por Diária (`isPlayerDead`) — aguardando módulo Hábitos.

---

## [2026-08-11] AÇÃO PENDENTE (não-Android) — Dados pessoais no `INITIAL_STATE` do HeroLog React

**Achado durante auditoria de fonte para o wiring do `CharacterScreen` (Android):** o
`INITIAL_STATE` real em `src/hooks/useGameState.ts` (React) contém **dados de exemplo pessoais
de Bruno**, não dado genérico de produto:

- **3 skills de exemplo** com tags de estudo pessoal reais: `'Código Sagrado (Programação)'`
  (tags: `'React Backend'`, `'Vite CSS'`, `'Solução de Bugs'`), `'Alquimia & Foco Geral'` (tags:
  `'Exercício Físico'`, `'Planejamento Semanal'`, `'Meditação'`), `'Sábias Letras (Leitura)'`
  (tags: `'Direito Civil'`, `'História Geral'`, `'Filosofia Estoica'`) — claramente ligado à
  rotina de concurseiro de Bruno, não genérico.
- **3 hábitos, 2 diárias, 1 afazer de exemplo** com referências pessoais específicas: diária
  `'Remédio da Milk'` ("Dar medicação da querida companheira" — provável nome de animal de
  estimação), afazer `'Ler WAY OF THE KINGS'` (livro específico de Brandon Sanderson).

**Decisão de Bruno (11/08/2026)**: esses dados pessoais **não devem ser portados pro Android**
(`createInitialCharacterState()` do bloco em andamento nasce com essas listas vazias) e
**também devem ser removidos do React**, já que o produto pode vir a ser usado por outras
pessoas além de Bruno, ou compartilhado/exibido publicamente em algum momento (contexto
@iurispraecepta).

**Ação pendente, fora do escopo do port Android**: revisar `INITIAL_STATE` em
`src/hooks/useGameState.ts` (React) e substituir esses exemplos por dado genérico
(ex.: skills sem tags pessoais, ou `habits`/`dailies`/`todos` vazios por padrão, com onboarding
guiando a criação do primeiro item). Não é tarefa deste projeto de port — registrar aqui só para
não perder de vista, e retomar numa sessão dedicada ao HeroLog React (v1.1.x).

---

## [2026-08-11] Bloco: 27 — Alicerce de estado real (`HeroLogApplication` + `HeroLogViewModel`)

**Arquivos criados/alterados:**
- /app/src/main/java/com/iurispraecepta/herolog/HeroLogApplication.kt (novo)
- /app/src/main/AndroidManifest.xml (`android:name=".HeroLogApplication"` registrado)
- /app/src/main/java/com/iurispraecepta/herolog/ui/HeroLogViewModel.kt (novo)
- /app/src/main/java/com/iurispraecepta/herolog/ui/HeroLogViewModelFactory.kt (novo)
- /app/src/test/java/com/iurispraecepta/herolog/HeroLogViewModelTest.kt (novo)

**Resumo:**
- Primeira frente do plano "Opção A" (alicerce isolado antes de ligar telas). Auditoria via
  `git clone` direto do repositório GitHub (`vosslerazelas22/HeroLog-Android`) confirmou que
  **nenhuma camada de estado real existia** — todo o `MainActivity.kt` roda sobre
  `remember { mutableStateOf(sampleX) }` com dados hardcoded, `CharacterRepository` (Bloco 0)
  nunca foi conectado a uma instância real de `HeroLogDatabase` (nem em produção, nem no teste
  original, que usava `FakeCharacterStateDao`), e não existia `Application` customizada nem
  registro em `AndroidManifest.xml`.
- `HeroLogApplication` constrói `HeroLogDatabase` (Room) e `CharacterRepository` como
  singletons `by lazy`. `HeroLogViewModel` expõe `StateFlow<CharacterState?>`, carrega no
  `init` via `viewModelScope.launch`, e `saveCharacterState()` persiste + atualiza o flow.
  `HeroLogViewModelFactory` preparado para uso futuro (nenhuma tela o usa ainda).
- **Nenhuma tela foi tocada neste bloco** — `MainActivity.kt` continua com os dados de exemplo,
  intacto, por decisão de escopo (bloco pequeno e auditável, sem misturar alicerce com wiring de
  UI).
- Dependências necessárias (`androidx.lifecycle.viewmodel.compose`, `androidx.room.ktx`,
  `androidx.room.runtime`, `kotlinx.coroutines.test`) já existiam no `build.gradle.kts` —
  nenhuma adição.

**Validação:**
- Build: sucesso.
- Testes: 148 no total (2 novos, matemática 146→148 confirmada). XML bruto solicitado e
  recebido diretamente (não resumo em prosa) — método adotado desde a divergência do Bloco 26,
  evita repetir erro de formatação do relatório em prosa do AI Studio.
- Teste novo usa **Room real em memória via Robolectric** (`Room.inMemoryDatabaseBuilder` +
  `ApplicationProvider.getApplicationContext`), não um fake DAO — prova persistência de verdade
  entre duas instâncias de `HeroLogViewModel` apontando pro mesmo banco, mais forte que o
  round-trip do `CharacterRepositoryTest` original (Bloco 0), que usava fake.
- Visual: N/A (sem UI neste bloco).

**Desvios de escopo aprovados:**
- Nenhum.

**Pendências que continuam em aberto:**
- Ligar a 1ª tela real ao `HeroLogViewModel` (próximo bloco natural — `CharacterScreen` é a
  mais simples, só leitura + `onUnequipItem`).
- Depois disso: wiring do timer de Foco (`startSession`/`cancelSession`/`togglePauseQuest`/
  `completeFocusQuest`) em cima do alicerce agora pronto, seguido do Bloco 3 da Morte Cognitiva
  (lifecycle `ON_STOP`/`ON_PAUSE` real).
- `IncursionModeModal.tsx` — 2ª superfície de UI, ainda não portada (frente independente).
- 2ª chamada de `completeFocusQuest` (achado do Bloco 22) — resolvida via fonte, ainda não
  portada.
- Inspeção visual humana das 7 screenshots acumuladas (5 do Bloco 22 + 2 do Bloco 26), adiada
  até a tela real de configuração pré-sessão ser plugada na `MainActivity`/navegação de
  produção.
- Gatilho de dano por Diária (`isPlayerDead`) — aguardando módulo Hábitos.
---

## [2026-08-12] Bloco: 28 — Ligação real de `CharacterScreen` + `InventoryScreen` ao `HeroLogViewModel`

**Arquivos criados/alterados:**
- /app/src/main/java/com/iurispraecepta/herolog/data/HeroLogInitialState.kt (novo —
  `createInitialCharacterState()`)
- /app/src/main/java/com/iurispraecepta/herolog/logic/InventoryLogic.kt (`activeBuffs()` nova)
- /app/src/main/java/com/iurispraecepta/herolog/logic/CharacterMappers.kt (novo —
  `CharacterState.toSummary()`)
- /app/src/main/java/com/iurispraecepta/herolog/ui/HeroLogViewModel.kt (reescrito — auto-cria
  estado inicial se `null`; `unequipItem`/`equipItem`/`sellItem`/`discardItem`/`equipTitle`)
- /app/src/main/java/com/iurispraecepta/herolog/MainActivity.kt (abas Personagem e Inventário
  ligadas ao `HeroLogViewModel` real via `viewModel(factory = HeroLogViewModelFactory(...))`;
  `sampleCharacter`/`sampleEquippedEquipment`/`sampleActiveBuffs`/`sampleInventory` removidos)
- /app/src/test/java/com/iurispraecepta/herolog/HeroLogInitialStateTest.kt (novo, 2 testes)
- /app/src/test/java/com/iurispraecepta/herolog/InventoryLogicTest.kt (+1 teste,
  `activeBuffs_filtersOnlyRelevantBuffTypes`)
- /app/src/test/java/com/iurispraecepta/herolog/CharacterMappersTest.kt (novo, 1 teste)
- /app/src/test/java/com/iurispraecepta/herolog/HeroLogViewModelTest.kt (+3 testes novos, -1
  teste do Bloco 27 removido — ver nota abaixo)

**Resumo:**
- Primeira ligação real de telas de produção ao alicerce criado no Bloco 27. Escopo ampliado
  conscientemente (decisão "saída a", aprovada por Bruno) para incluir `CharacterScreen` **e**
  `InventoryScreen` no mesmo bloco — as duas telas compartilhavam `inventory`/
  `equippedEquipment` no scaffolding fake do `MainActivity`; ligar só uma delas criaria
  dessincronia real e visível entre as abas (equipar num lado não apareceria no outro).
- **`createInitialCharacterState()`**: replica `INITIAL_STATE` real (React,
  `useGameState.ts`), mas com `skills`/`habits`/`dailies`/`todos` vazios de propósito — ver o
  registro de "AÇÃO PENDENTE" acima sobre os dados pessoais de Bruno no React que motivaram essa
  decisão.
- **Bug real encontrado e corrigido, não herdado**: o `onSellItem` do scaffolding anterior do
  `MainActivity` calculava `sellPrice` via `InventoryLogic.sellItem` mas nunca somava ao `gold`
  — só logava o valor (`Log.d`). Fonte real auditada (`useInventory.ts`, função `sellItem`)
  confirma que o `gold` deve ser incrementado. `HeroLogViewModel.sellItem()` corrige isso.
  Também corrigida a localização documentada: a lógica de venda mora no hook `useInventory.ts`,
  não solta no `App.tsx`.
- `HeroLogViewModel.init` agora cria e persiste `createInitialCharacterState()` automaticamente
  quando `repository.getCharacterState()` devolve `null`, em vez de deixar o flow parado em
  `null` para sempre.
- `MainActivity`: abas Personagem e Inventário mostram um texto simples de carregamento
  enquanto `characterState == null` (só o instante entre abrir o app e o `init` popular o
  estado). Abas Habilidades e Foco (Preview) **não foram tocadas** — fora de escopo.

**Falha de processo registrada (não é erro do AI Studio desta vez):** o prompt enviado ao AI
Studio mudou o comportamento de `HeroLogViewModel.init` (Parte 4) sem instruir explicitamente
a remoção do teste `viewModel_loadsNullInitially_whenNoSavedState` (Bloco 27), que afirmava o
comportamento antigo (`null` permanente sem estado salvo) — agora contraditório com a nova
regra. O AI Studio removeu o teste por conta própria de forma limpa (confirmado via
`grep -n "fun viewModel_"` no arquivo fonte — sem sobra comentada/duplicada), e a matemática de
testes reconciliou exatamente com essa hipótese, mas o ideal seria eu ter instruído a remoção
explicitamente no prompt. Lição para blocos futuros que alteram contrato de comportamento já
coberto por teste: sempre listar explicitamente quais testes antigos precisam ser
removidos/atualizados, não só quais são novos.

**Validação:**
- Build: sucesso.
- Testes: 154 no total. Nominal em prosa do AI Studio declarou "153" incorretamente — divergência
  detectada por contagem manual dos deltas (148 + 2+1+1+2 = 154) e reconciliada por dois comandos
  brutos: `ls .../testDebugUnitTest/ | wc -l` (35 arquivos de suíte) e
  `grep -c "<testcase" *.xml | soma` (154 exato). Sem sinal de regressão — confirmado que a
  divergência era só o teste removido de propósito, não uma suíte quebrada silenciosamente.
- Visual: N/A (sem screenshot tests novos neste bloco — `CharacterScreen`/`InventoryScreen` já
  tinham os seus desde blocos anteriores, sem mudança visual, só a fonte de dado mudou).

**Desvios de escopo aprovados:**
- Bug do `gold` no `sellItem` corrigido em vez de herdado (ver acima).
- `skills`/`habits`/`dailies`/`todos` vazios no estado inicial (decisão já registrada na entrada
  "AÇÃO PENDENTE" acima).

**Pendências que continuam em aberto:**
- Wiring do timer de Foco (`startSession`/`cancelSession`/`togglePauseQuest`/
  `completeFocusQuest`) sobre o mesmo `HeroLogViewModel`/`CharacterState` — próximo bloco
  natural, e pré-requisito pro Bloco 3 da Morte Cognitiva (lifecycle real).
- `IncursionModeModal.tsx` — 2ª superfície de UI, ainda não portada (frente independente).
- 2ª chamada de `completeFocusQuest` (achado do Bloco 22) — resolvida via fonte, ainda não
  portada.
- Inspeção visual humana das 7 screenshots acumuladas (5 do Bloco 22 + 2 do Bloco 26), adiada
  até a tela real de configuração pré-sessão ser plugada na `MainActivity`/navegação de
  produção.
- Gatilho de dano por Diária (`isPlayerDead`) — aguardando módulo Hábitos.
- Aba Habilidades (`SkillsScreen`) e Foco (Preview) ainda rodam sobre estado local/fake — fora
  de escopo deste bloco, candidatas a blocos futuros de wiring.
---

## [2026-08-13] Bloco: 29 — Motor de sessão de Foco (`start`/`pause`/`cancel`/conclusão natural)

**Arquivos criados/alterados:**
- app/src/main/java/com/iurispraecepta/herolog/logic/focus/FocusSessionState.kt (novo)
- app/src/main/java/com/iurispraecepta/herolog/ui/HeroLogViewModel.kt (modificado — construtor
  com `clock: () -> Long` injetável, `StateFlow<FocusSessionState>`, `startSession`/
  `togglePauseQuest`/`cancelSession`, job de tick interno, `onFocusSessionCompleted`)
- app/src/test/java/com/iurispraecepta/herolog/FocusSessionViewModelTest.kt (novo — 7 testes)

**Resumo:**
- Auditoria de fonte real (`useFocusSession.ts`, `App.tsx`) revelou que o achado do Bloco 22
  sobre "2ª chamada de `completeFocusQuest`" ainda procede em parte — `completeFocusQuest` tem
  um early-return guiado por `isFocusMode`/`isFocusCompleted` — mas a peça extra suposta
  (`hasAutoClaimedRef`/`handleAutoClaim` em `FocusCompletionFlow.tsx`) **não existe** no código
  atual; foi uma hipótese incorreta levantada durante a auditoria desta sessão e corrigida antes
  de virar prompt. `FocusCompletionFlow.tsx` é hoje só a stepper UI (notas/tag), sem lógica de
  aplicação — quem aplica é `onConfirm` em `App.tsx`, chamando `handleAutoClaim(editedNotes,
  selectedTag)` (assinatura compatível com `FocusApplyLogic.apply`, já portado no Bloco 17).
- Motor portado com padrão de `endTime` absoluto (`clock() + duração`), replicando fielmente o
  React para evitar drift — nunca decrementa `timeLeft` por contagem ingênua. `clock: () -> Long`
  injetável no construtor do `ViewModel` (default `System::currentTimeMillis`) para
  testabilidade determinística, avançado em paralelo ao `StandardTestDispatcher` nos testes.
- Conclusão natural chama `FocusRewardsLogic.calculate` (já existente) e armazena o resultado em
  `pendingRewardsCalculation` — **não chama `FocusApplyLogic.apply`** neste bloco (proposital,
  fica para bloco futuro de UI de confirmação). `studiedMinutes` usado no cálculo é sempre a
  duração *configurada*, não o tempo decorrido — mesmo após pausas, fiel ao React.
- Decisão consciente já registrada nesta sessão para bloco futuro: a recuperação de sessão após
  reinício do app (`completeSessionOnReload`, React) tem um bug conhecido — aplica recompensa
  silenciosamente sem passar pela tela de conclusão. O Android vai nascer corrigido (nunca
  aplicar automaticamente), decisão confirmada com Bruno — ver `PARIDADE.md`.
- Correção pontual de teste: `startSession_setsIsRunningTrueAndCorrectTimeLeftAndTotalSeconds`
  travava a suíte inteira (job de tick com `while(true)` nunca encerrado antes do fim do teste,
  preso em `delay()` sobre o `testDispatcher`) — corrigido com `cancelSession()` explícito no
  fim do teste. Isolado via execução individual de cada um dos 7 testes com timeout, não por
  leitura de código (só a leitura confirmou a causa depois de isolado).

**Validação:**
- Build: sucesso.
- Testes: `FocusSessionViewModelTest` 7/7, `HeroLogViewModelTest` 4/4 (sem regressão), suíte
  completa 154 testes, 0 falhas — XML bruto reconferido **duas vezes** nesta sessão após um
  incidente de XML fabricado pelo AI Studio (nomes de teste inventados que não batiam com o
  `.kt` real) — ver `ARMADILHAS_CONHECIDAS.md` #9.
- Visual: N/A (sem UI nova neste bloco).

**Desvios de escopo aprovados:**
- Nenhum.

**Pendências que continuam em aberto:**
- Persistência da sessão ativa entre reinícios do app (Bloco 30, em andamento).
- Tela de conclusão (stepper, port de `FocusCompletionFlow.tsx`) + `onConfirm` real chamando
  `FocusApplyLogic.apply` — bloco futuro.
- `IncursionModeModal.tsx`, Bloco 3 da Morte Cognitiva, inspeção visual das 7 screenshots
  acumuladas — mesmas pendências de sempre, ainda bloqueadas/independentes conforme registrado
  nos blocos anteriores.

---

## [2026-08-14] Bloco: 30 — Infraestrutura de persistência da sessão de Foco (sub-bloco B1)

**Arquivos criados/alterados:**
- app/src/main/java/com/iurispraecepta/herolog/logic/focus/FocusModels.kt (modificado —
  `@Serializable` em `FocusSessionConfig`, `UsedEquipmentCharge`, `DroppedTitle`, `LootItem`,
  `FocusRewardsCalculation`)
- app/src/main/java/com/iurispraecepta/herolog/logic/focus/PersistedFocusSession.kt (novo)
- app/src/main/java/com/iurispraecepta/herolog/data/entity/ActiveFocusSessionEntity.kt (novo)
- app/src/main/java/com/iurispraecepta/herolog/data/dao/ActiveFocusSessionDao.kt (novo)
- app/src/main/java/com/iurispraecepta/herolog/data/database/HeroLogDatabase.kt (modificado —
  entity nova no `@Database`, `version` 1→2)
- app/src/main/java/com/iurispraecepta/herolog/data/repository/FocusSessionRepository.kt (novo)
- app/src/main/java/com/iurispraecepta/herolog/HeroLogApplication.kt (modificado — expõe
  `focusSessionRepository` via `by lazy`, `.fallbackToDestructiveMigration()` no builder do Room)
- app/src/test/java/com/iurispraecepta/herolog/FocusSessionRepositoryTest.kt (novo — 5 testes)

**Resumo:**
- Bloco B (persistência + recuperação) dividido em três sub-blocos (30/31/32 — nomeados B1/B2/B3
  durante a sessão) depois que um prompt único maior travou o Gradle Test Executor do AI Studio.
  Este sub-bloco cobre só a infraestrutura (serialização, entity, DAO, migração de schema,
  repository) — **sem tocar em `HeroLogViewModel.kt`** ainda (isso é o Bloco 31).
- Decisão de design importante: `FocusRewardsLogic.calculate` usa `random: Random = Random.Default`
  (não determinístico). Por isso `PersistedFocusSession.pendingCalculation` guarda o **resultado
  já calculado**, nunca recalculado nas recuperações futuras — evita que o loot/título sorteado
  mude a cada reinício do app antes de confirmar (o que equivaleria a permitir "rerolar" a sorte
  fechando/reabrindo o app). `pendingCalculation == null` = sessão em andamento;
  `pendingCalculation != null` = já calculado, congelado, aguardando confirmação (bloco futuro).
- Migração de schema via `.fallbackToDestructiveMigration()` — não existe estratégia de migração
  real no projeto ainda; perda de dados local em upgrade de schema aceitável nesta fase (sem
  usuários reais salvando dados).
- Entity/DAO/Repository seguem exatamente o mesmo padrão já usado por
  `CharacterStateEntity`/`CharacterStateDao`/`CharacterRepository` (linha única, id fixo = 0,
  payload JSON via kotlinx.serialization).

**Validação:**
- Build: sucesso (após correção de um erro de compilação do próprio teste novo — o AI Studio
  usou `Rarity.Lendario` em um `LootItem` de teste, valor que não existe no enum real, só
  `Comum`/`Especial`; corrigido para `Especial`).
- Testes: `FocusSessionRepositoryTest` 5/5 (rodado isolado, XML bruto confirmado —
  `BUILD SUCCESSFUL in 53s`). Suíte completa (34 classes) **não pôde ser confirmada numa única
  run** — o ambiente AI Studio travou consistentemente na task `:app:testDebugUnitTest` mesmo
  após limpeza de cache e aumento de heap (`-Xmx1536m`); ver `ARMADILHAS_CONHECIDAS.md` #10.
  Mitigado rodando um subconjunto filtrado das classes que tocam Room/persistência/ViewModel:
  `FocusSessionRepositoryTest` (5/5) + `HeroLogViewModelTest` (4/4, sem regressão) +
  `CharacterRepositoryTest` (1/1) + `HeroLogInitialStateTest` (2/2) — 12 testes, 0 falhas. Risco
  residual de regressão nas outras ~22 classes (UI/screenshot/lógica de quest não relacionada a
  Room) considerado baixo, já que este bloco não altera nada usado por elas, mas fica registrado
  como não-confirmado, não como "validado".
- Visual: N/A (sem UI neste bloco).

**Desvios de escopo aprovados:**
- Nenhum (o erro de `Rarity.Lendario` foi correção de um teste recém-escrito pelo próprio AI
  Studio dentro do escopo pedido, não um desvio de escopo).

**Pendências que continuam em aberto:**
- Bloco 31 (B2): wiring de escrita no `HeroLogViewModel` — persistir nos pontos certos do motor
  de sessão (`startSession`/pausar-limpa/retomar-persiste/`cancelSession`-limpa/conclusão
  natural-persiste-com-cálculo).
- Bloco 32 (B3): recuperação no `init` do `ViewModel` — os 3 estados (em andamento / expirada
  sem cálculo / expirada com cálculo já persistido).
- Mesmas pendências de sempre (tela de conclusão real, `IncursionModeModal.tsx`, Bloco 3 da
  Morte Cognitiva, inspeção visual das 7 screenshots acumuladas).
- Confirmar a suíte completa de 34 classes numa única run assim que possível (não bloqueante).
---

## [2026-08-15] Bloco: 31 — Wiring de escrita da persistência no motor de sessão (sub-bloco B2)

**Arquivos criados/alterados:**
- app/src/main/java/com/iurispraecepta/herolog/ui/HeroLogViewModel.kt (modificado — novo
  parâmetro obrigatório `focusSessionRepository: FocusSessionRepository` no construtor,
  chamadas de persistência em `startSession`/`togglePauseQuest`/`cancelSession`/
  `onFocusSessionCompleted`)
- app/src/main/java/com/iurispraecepta/herolog/ui/HeroLogViewModelFactory.kt (modificado —
  passa `application.focusSessionRepository`)
- app/src/test/java/com/iurispraecepta/herolog/FocusSessionViewModelTest.kt (modificado — 5
  testes novos de persistência + os 7 do Bloco 29 tiveram a sincronização de tempo reescrita)

**Resumo:**
- Wiring de ESCRITA apenas — `startSession`/retomar persistem via `focusSessionRepository.
  saveSession`; pausar/cancelar limpam via `clearSession` (replica fielmente o
  `localStorage.removeItem` do React no branch de pausa); conclusão natural persiste o registro
  já com `pendingCalculation` preenchido (não limpa — fica pendente de confirmação futura,
  decisão de design do Bloco 30). Recuperação/leitura no `init` do ViewModel continua fora de
  escopo (Bloco 32 / B3, próximo).
- Desvio de escopo identificado e aceito: o prompt pedia para não alterar os 7 testes
  originais do Bloco 29, mas o AI Studio reescreveu a sincronização de tempo deles — trocou a
  variável `fakeNow` manual por `clock = { testDispatcher.scheduler.currentTime }` (usa o
  relógio virtual do próprio scheduler, eliminando uma fonte de dessincronia entre dois
  "relógios" paralelos). Avaliado como melhoria de qualidade com justificativa técnica sólida,
  não desvio disfarçado — aceito conscientemente. Duas asserções específicas de
  `sessionNaturalCompletion_calculatesPendingRewards_...` (skillIdx/durationMins) migraram para
  o novo teste `sessionNaturalCompletion_persistsSessionWithPendingCalculation` — cobertura
  preservada, só reorganizada.
- Incidente de processo: um "Resolve conflicts" (dois snapshots divergentes do mesmo arquivo,
  gerados por reexecução do prompt) apareceu no AI Studio antes da versão final aceita aqui —
  resolvido descartando o snapshot mais antigo e rodando o prompt de novo do zero, não fazendo
  merge manual dos dois.

**Validação:**
- Build: sucesso.
- Testes: `FocusSessionViewModelTest` 12/12 (7 originais + 5 novos), `FocusSessionRepositoryTest`
  5/5 (sem regressão), `HeroLogViewModelTest` 4/4 (sem regressão), `CharacterRepositoryTest` 1/1
  (sem regressão) — 22 testes, 0 falhas. XML bruto conferido, nomes de teste consistentes com o
  `.kt` lido linha a linha antes da execução (sem sinal do padrão de fabricação registrado em
  `ARMADILHAS_CONHECIDAS.md` #9). Suíte completa (34 classes) não rodada (ver #10) — mesmo
  racional de risco residual baixo do Bloco 30 se aplica aqui.
- Visual: N/A (sem UI neste bloco).

**Desvios de escopo aprovados:**
- Reescrita da sincronização de tempo dos 7 testes originais do Bloco 29 (ver Resumo acima) —
  aprovado retroativamente por qualidade técnica, não solicitado no prompt original.

**Pendências que continuam em aberto:**
- Bloco 32 (B3): recuperação no `init` do `ViewModel` — os 3 estados (em andamento / expirada
  sem cálculo / expirada com cálculo já persistido).
- Mesmas pendências de sempre (tela de conclusão real, `IncursionModeModal.tsx`, Bloco 3 da
  Morte Cognitiva, inspeção visual das 7 screenshots acumuladas).
---

## [2026-08-15] Bloco: 32 — Recuperação de sessão de Foco no init do ViewModel (sub-bloco B3)

**Arquivos criados/alterados:**
- app/src/main/java/com/iurispraecepta/herolog/ui/HeroLogViewModel.kt (modificado —
  `recoverFocusSession()` privada, chamada dentro do `init { viewModelScope.launch { ... } }`
  já existente, encadeada após `_characterState.value` ser definido)
- app/src/test/java/com/iurispraecepta/herolog/FocusSessionRecoveryTest.kt (novo — 4 testes)

**Resumo:**
- Fecha o Bloco B (Blocos 29/30/31/32): `recoverFocusSession()` distingue os 3 estados
  possíveis de uma sessão persistida ao reabrir o app — (1) em andamento → retoma o timer
  normalmente a partir do `endTimeMillis` salvo; (2) expirada sem cálculo → calcula UMA VEZ
  agora, reaproveitando `onFocusSessionCompleted()` (que já persiste o resultado, Bloco 31),
  nunca aplica a recompensa automaticamente; (3) já calculada, aguardando confirmação → só
  recarrega o `pendingCalculation` já persistido, nunca recalcula (proteção contra reroll de
  loot via `Random.Default` não-determinístico, decisão de design do Bloco 30).
- Esta é a correção consciente e definitiva do bug documentado do React
  (`completeSessionOnReload` aplicando recompensa silenciosamente no reload, sem tela de
  confirmação) — o Android nunca teve esse caminho, nasceu sempre roteando pro estado
  "pendente de confirmação".
- Segundo `ViewModel` sobre o mesmo banco/repositories usado nos testes para simular reabertura
  do app, seguindo o mesmo padrão já validado em `HeroLogViewModelTest`
  (`viewModel_savesAndReloadsState_persistsAcrossNewViewModelInstance`, Bloco 27/28).

**Validação:**
- Build: sucesso.
- Testes: `FocusSessionRecoveryTest` 4/4 (nenhuma sessão / estado 1 / estado 2 / estado 3),
  `FocusSessionViewModelTest` 12/12, `FocusSessionRepositoryTest` 5/5, `HeroLogViewModelTest`
  4/4 — todos sem regressão. 26 testes, 0 falhas. XML bruto conferido, nomes consistentes com o
  esperado. Suíte completa (34 classes) não rodada (ver `ARMADILHAS_CONHECIDAS.md` #10) — mesmo
  racional de risco residual baixo dos Blocos 30/31 se aplica aqui.
- Visual: N/A (sem UI neste bloco).

**Desvios de escopo aprovados:**
- Nenhum.

**Pendências que continuam em aberto:**
- Bloco B (motor + persistência de sessão de Foco) está **fechado**. Próximo passo natural:
  UI de conclusão/confirmação real (port de `FocusCompletionFlow.tsx` — notas/tag — com
  `onConfirm` chamando `FocusApplyLogic.apply` de fato, único ponto que ainda falta pra fechar
  o ciclo completo do timer de Foco).
- Mesmas pendências de sempre: `IncursionModeModal.tsx`, Bloco 3 da Morte Cognitiva (agora
  desbloqueado, já que depende do motor de sessão estar pronto), inspeção visual das 7
  screenshots acumuladas.

---

## [2026-08-15] Correção de hipótese — localização real de `handleAutoClaim`/`onConfirm`

**Não é um Bloco fechado — é correção de registro, feita antes de escrever o prompt de porte da
UI de conclusão, a partir do dump literal real de `FocusCompletionFlow.tsx` e de um trecho de
`FocusModeScreen.tsx` colados por Bruno.**

- **Hipótese anterior (registrada no Bloco 29 do `DEV_LOG_ANDROID.md` e na seção 3 do
  `PARIDADE.md`), agora corrigida**: supunha-se que `onConfirm` de `FocusCompletionFlow.tsx`
  batia com uma função `handleAutoClaim` vivendo em `App.tsx`. **Confirmado por Bruno: `App.tsx`
  não tem `handleAutoClaim`.**
- **Confirmado por dump literal**: `handleAutoClaim` vive em `FocusModeScreen.tsx`, dentro de um
  `useEffect` que dispara **automaticamente** quando `isFocusCompleted` vira `true` (não é
  acionado por confirmação do usuário) — sai do fullscreen, desmonta o modo foco
  (`setIsFocusMode(false)`), toca SFX de level-up, e chama `completeFocusQuest()` (fase de
  **cálculo**, não aplicação — consistente com o que já se sabia de `useFocusSession.ts`).
- **Ainda não confirmado, aberto para a próxima leitura de fonte**: onde `FocusCompletionFlow`
  é de fato renderizado (`<FocusCompletionFlow onConfirm={...} .../>`) e qual função real é
  passada como `onConfirm` ali — não é `App.tsx`/`handleAutoClaim` como se supunha. Não presumir
  novamente sem ver o trecho real.
- **Atualização (mesmo dia, com o dump real de `App.tsx` e `handleConfirmClaimRewards`
  colados)**: confirmado — `FocusCompletionFlow` é renderizado em `App.tsx`
  (`{rewardsModalData?.visible && <FocusCompletionFlow ... onConfirm={(n, t) =>
  handleConfirmClaimRewards(n, t)} />}`), e `handleConfirmClaimRewards` (também em `App.tsx`) é
  de fato a fase de **aplicação**. A hipótese original (Bloco 29) sobre `App.tsx` estava certa
  nesse ponto específico — só estava indevidamente fundida com `handleAutoClaim` (que é uma
  função diferente, em `FocusModeScreen.tsx`, e só dispara a fase de **cálculo** automaticamente).

---

## [2026-08-15] Auditoria de `handleConfirmClaimRewards` × `FocusApplyLogic.apply` — decisões de escopo pré-Bloco 33

**Não é um Bloco fechado — é registro de auditoria/decisão de escopo, feito antes de escrever o
prompt do próximo bloco (UI de conclusão real).**

- **Comparação linha a linha confirmou fidelidade já existente** de `FocusApplyLogic.apply`
  (Blocos 17/17.1/17.2) contra o `handleConfirmClaimRewards` real colado por Bruno: skills com
  prestígio, consumíveis de uso único, cargas de equipamento, loot, progressão de combate,
  streak/bestStreak, `totalXP` (correção já registrada), `lastDungeonClearedTime` (gatilho
  `calc.dungeonClearGoldBonus > 0` equivalente a `nextSessions >= 4` do React), conquistas
  (catálogo completo, melhoria já registrada). Nenhuma divergência nova encontrada nesses pontos.
- **`aiChronicle` — decisão consciente registrada agora**: o campo existe em `HistoryEntry`
  (`CharacterModels.kt`) e `FocusApplyLogic.apply` já grava `null` explicitamente. Bruno
  confirmou a origem: era uma sugestão do próprio Google AI Studio (não pedido original de
  Bruno) para resumir as notas manuais do usuário em cada sessão e gerar uma "lore" via IA
  generativa ao final. **Descontinuada por decisão consciente de Bruno — o resultado soaria
  como "AI slop"**. Não confirmado se foi completamente removido do lado React (`rewardsModalData
  .aiChronicleResult` ainda existe na assinatura vista). **Decisão para o Android**: manter
  `aiChronicle = null` indefinidamente, sem replicar a geração via IA — não é lacuna, é escopo
  intencionalmente descartado.
- **Contador de progressão de masmorra (`dungeonSessions`/`isDungeonMode`) — escopo novo
  identificado, aprovado para o próximo bloco**: no React, esse bloco (`if (isDungeonMode)
  {...}` no topo de `handleConfirmClaimRewards`) fica **fora** de `setGameState`/`candidateState`
  — mexe em `sessionConfig`, não em `CharacterState`. Não há wiring equivalente ainda em nenhum
  lugar do `HeroLogViewModel.kt` (confirmado por grep). **Aprovado por Bruno como parte do
  próximo bloco de wiring de confirmação** (incrementar/resetar `dungeonSessions`, desligar
  `isDungeonMode` ao completar 4 sessões).
- **Timer de descanso automático (`setSelectedBreakMins`/`startBreakTimer`/`setIsBreakPrep`) e
  SFX (`sound.playCoins`/`playLevelUp`) — explicitamente FORA de escopo do próximo bloco,
  aprovado por Bruno.** Não existe nenhuma infraestrutura de break timer nem de áudio no Android
  ainda (confirmado por grep). `addSystemLog` também não existe como sistema real — só um
  parâmetro isolado `onLog: (String) -> Unit` em `RaidModeSection.kt`, ainda não ligado a nada.
  Ficam registrados como pendências separadas, não bloqueiam o fechamento da UI de
  conclusão/confirmação.

---

## [2026-08-15] Bloco: 33 — Port de `FocusCompletionFlow.tsx` (UI de conclusão, presentational)

**Arquivos criados/alterados:**
- app/src/main/java/com/iurispraecepta/herolog/ui/focus/FocusCompletionFlow.kt (novo)
- app/src/test/java/com/iurispraecepta/herolog/FocusCompletionFlowScreenshotTest.kt (novo)

**Resumo:**
- Port 1:1 do stepper `streak → summary → loot → notes` (`streak`/`loot` condicionais),
  `CompletionShell` + 4 sub-composables (`StreakCelebrationScreen`, `SessionSummaryScreen`,
  `LootDropScreen`, `SessionNotesScreen`), puramente presentational — sem tocar em
  `HeroLogViewModel.kt`, sem chamar `FocusApplyLogic.apply`, sem persistência.
- Lógica de rank (`getRank`/`getRankDescription`, 5 ramos cada) portada exatamente, conferida
  contra o `.kt` real. `effectiveStreak = if (streak > 0) streak else 1` replica fielmente
  `gameState.streak || 1` do React.
- `pauseCount` recebido como parâmetro solto (não é campo de `FocusRewardsCalculation`),
  conforme decisão de escopo pré-bloco.
- Adição sensata não pedida explicitamente, mas dentro do espírito do prompt: parâmetro
  `initialStepIndex` no composable principal, permitindo aos testes de screenshot forçar cada
  step sem simular cliques — aceito, não é desvio de escopo.
- Simplificações conscientes aplicadas e confirmadas no relatório: `Stone950` como aproximação
  de `bg-quest-panel` (cor exata não mapeada no `Color.kt` atual); cantos decorativos em L e
  drop-shadow/glow dos cards de tesouro simplificados para `Card` M3 com borda simples; blur e
  animação de bounce da chama omitidos.
- `aiChronicle`/geração de lore via IA — não aparece em nenhuma tela, conforme decisão registrada.

**Validação:**
- Build: sucesso.
- Testes: `FocusCompletionFlowScreenshotTest` 6/6 — `focusCompletionFlow_summary_noCombo_noLoot`,
  `focusCompletionFlow_summary_withComboBonus`, `focusCompletionFlow_streakCelebration`,
  `focusCompletionFlow_lootDrop`, `focusCompletionFlow_notes_noSkillTags`,
  `focusCompletionFlow_notes_withSkillTags`, todos PASSED. XML bruto conferido (nomes de teste
  no `.kt` real batem 1:1 com o XML reportado, sem sinal do padrão de `ARMADILHAS_CONHECIDAS.md`
  #9). Commit `1b66e0d` puxado e conferido via `bash_tool` antes de aceitar o fechamento.
- Visual: screenshots gerados (Roborazzi), ainda pendente de inspeção visual humana por Bruno
  (mesma pendência acumulada de sessões anteriores — 7 screenshots antigas + os novos deste
  bloco).

**Desvios de escopo aprovados:**
- `initialStepIndex` (ver Resumo acima) — aceito como interpretação razoável do pedido de
  viabilizar screenshot por step, não como desvio disfarçado.

**Pendências que continuam em aberto:**
- Bloco 34 (próximo): wiring real — `confirmFocusSession()` no `HeroLogViewModel` chamando
  `FocusApplyLogic.apply` de fato + contador de progressão de masmorra
  (`dungeonSessions`/`isDungeonMode`), conforme escopo já aprovado.
- Mesmas pendências de sempre: `IncursionModeModal.tsx`, Bloco 3 da Morte Cognitiva, inspeção
  visual das screenshots acumuladas (agora incluindo as 6 novas deste bloco), timer de descanso
  automático e SFX (explicitamente fora de escopo até nova decisão).

---

## [2026-08-15] Bloco: 34 — Wiring real de confirmação de sessão de Foco (`confirmFocusSession`)

**Arquivos criados/alterados:**
- app/src/main/java/com/iurispraecepta/herolog/ui/HeroLogViewModel.kt (modificado — novo
  `_dungeonSessionsProgress`/`dungeonSessionsProgress` (`StateFlow<Int>`, só em memória, sem
  Room) + função `confirmFocusSession(editedNotes, selectedTag)`)
- app/src/test/java/com/iurispraecepta/herolog/FocusSessionConfirmTest.kt (novo — 5 testes)

**Resumo:**
- `confirmFocusSession` fecha o ciclo do timer de Foco de ponta a ponta: chama
  `FocusApplyLogic.apply` de fato (fase de APLICAÇÃO, equivalente real a
  `handleConfirmClaimRewards`), persiste via `saveCharacterState` já existente, incrementa/reseta
  o contador de progressão de masmorra (`nextSessions >= 4` reseta pra 0, mesmo gatilho já usado
  em `lastDungeonClearedTime`/`dungeonClearGoldBonus`), reseta `_focusSessionState` pro estado
  default e limpa a sessão persistida via `focusSessionRepository.clearSession()`.
- `_dungeonSessionsProgress` mantido estritamente em memória (`MutableStateFlow`, sem
  entidade/DAO novo) — fidelidade consciente ao `sessionConfig.dungeonSessions` do React, que
  também é `useState` efêmero, não salvo em `localStorage`. Confirmado por auditoria do diff: só
  os dois arquivos esperados foram tocados, nenhuma tabela/DAO nova criada.
- `Date(clock())` usado como `referenceDate` de `FocusApplyLogic.apply`, reaproveitando o mesmo
  `clock` injetável já usado no resto do ViewModel — mantém testabilidade determinística.
- Reset do checkbox `isDungeonMode` da UI de seleção pré-sessão explicitamente fora de escopo
  (não existe ainda tela real consumindo isso) — só o contador numérico é resetado aqui.

**Validação:**
- Build: sucesso.
- Testes: `FocusSessionConfirmTest` 5/5 (`confirmFocusSession_withPendingCalculation_...`,
  `confirmFocusSession_withoutPendingCalculation_isNoOp`,
  `confirmFocusSession_dungeonMode2Sessions_incrementsDungeonSessionsProgressTo3`,
  `confirmFocusSession_dungeonMode3Sessions_resetsDungeonSessionsProgressTo0`,
  `confirmFocusSession_nonDungeonMode_doesNotChangeDungeonSessionsProgress`, todos PASSED). Sem
  regressão: `FocusSessionViewModelTest` 12/12, `FocusSessionRecoveryTest` 4/4,
  `FocusSessionRepositoryTest` 5/5, `HeroLogViewModelTest` 4/4 — 30 testes, 0 falhas. XML bruto
  conferido, nomes de teste no `.kt` real batem 1:1 com o XML reportado (sem sinal do padrão de
  `ARMADILHAS_CONHECIDAS.md` #9). Commit `1ce88fd` puxado e conferido via `bash_tool` antes de
  aceitar o fechamento — `git show --stat` confirmou que só os dois arquivos esperados foram
  tocados, nenhum DAO/entidade novo, `import java.util.Date` presente.
- Visual: N/A (sem UI neste bloco).

**Desvios de escopo aprovados:**
- Nenhum.

**Pendências que continuam em aberto:**
- Wiring de `FocusCompletionFlow.kt` (Bloco 33) + `confirmFocusSession` (Bloco 34) na
  `MainActivity`/telas reais — o ciclo lógico do timer de Foco está fechado ponta a ponta, mas
  ainda não há nenhum caminho de UI real que dispare `onConfirm` de verdade. Próximo passo
  natural pra fechar o módulo Foco de fato.
- Mesmas pendências de sempre: `IncursionModeModal.tsx`, Bloco 3 da Morte Cognitiva, inspeção
  visual das screenshots acumuladas, timer de descanso automático e SFX (fora de escopo até
  nova decisão), reset do checkbox `isDungeonMode` na futura tela de seleção pré-sessão.

---

## [2026-08-15] Bloco: 35 — Wiring real do módulo Foco na `MainActivity` (fecha o ciclo ponta a ponta)

**Arquivos alterados:**
- app/src/main/java/com/iurispraecepta/herolog/MainActivity.kt

**Resumo:**
- `FocusOrbPreviewScreen` remodelada para consumir `heroLogViewModel.focusSessionState` e
  `characterState` reais em vez do scaffolding local falso anterior. Fase de preparação (seletor
  de Raid Mode real + botão que monta `FocusSessionConfig` real com 25min fixo + primeira skill
  real), fase imersiva (`FocusModeScreen` real, `onTogglePause`/`onExit` ligados a
  `togglePauseQuest`/`cancelSession`), fase de conclusão (`FocusCompletionFlow` real, `onConfirm`
  ligado a `confirmFocusSession`).
- Skill/duração mantidas fixas conforme escopo aprovado (primeira skill de
  `characterState.skills`, 25 minutos) — não é seletor real, tela de configuração pré-sessão
  continua pendência separada.
- Controles de preview isolado do `FocusOrb` (Play/Pause/Reset/Alternar Descanso/Tamanho)
  mantidos intactos, sem misturar com o fluxo real.

**Validação:**
- Build: sucesso. Testes: sem teste novo dedicado (é wiring de `MainActivity`/Compose, não
  testável por unit test puro no padrão dos blocos anteriores — relatado como tal, não fabricado
  teste que não roda). Regressão: `FocusSessionConfirmTest` 5/5, `FocusSessionViewModelTest`
  12/12, todos PASSED, XML bruto conferido.
- Commit `e426ecc` puxado e conferido.
- **Achado real via inspeção visual humana (não pelo AI Studio)**: Bruno testou o preview real e
  encontrou o aviso "crie uma habilidade primeiro" persistindo mesmo após criar uma habilidade —
  ver Bloco 36 abaixo pra causa raiz e correção. Bloco 35 em si está correto (usa
  `characterState.skills` real, como devia); a causa é uma lacuna mais antiga, não uma regressão
  deste bloco.
- **Pendência ainda aberta**: os 2 prints reais do fluxo completo do Foco funcionando (pedidos na
  validação original do Bloco 35) ainda não foram entregues — dependiam do Bloco 36 pra sequer
  serem possíveis. Continuam pendentes após o Bloco 36 fechar.

**Desvios de escopo aprovados:**
- Nenhum.

---

## [2026-08-15] Bloco: 36 — Wiring real da aba Habilidades com `characterState`

**Contexto**: descoberto durante a validação visual do Bloco 35 — a aba Habilidades nunca foi
conectada ao `HeroLogViewModel`/`characterState` (todas as operações mexiam só numa variável
local `skills`/`sampleSkills` em `MainActivity.kt`, desconectada). `characterState.skills` começa
vazio (`createInitialCharacterState()`) e nunca era escrito, por isso o Foco (corretamente)
sempre mostrava "crie uma habilidade primeiro", mesmo com skills criadas na aba.

**Arquivos criados/alterados:**
- app/src/main/java/com/iurispraecepta/herolog/ui/HeroLogViewModel.kt (6 métodos novos:
  `addCustomSkill`, `addTagToSkill`, `removeTagFromSkill`, `renameSkill`, `deleteSkill`,
  `prestigeSkill`, todos seguindo o padrão já estabelecido de `unequipItem`/`equipItem`/
  `sellItem`/`equipTitle` — ler `_characterState.value`, chamar `SkillLogic` puro, persistir via
  `saveCharacterState`)
- app/src/main/java/com/iurispraecepta/herolog/MainActivity.kt (aba Habilidades religada aos
  métodos reais do ViewModel; variável local `skills`/lista `sampleSkills` removida por completo)
- app/src/test/java/com/iurispraecepta/herolog/HeroLogViewModelTest.kt (6 testes novos)

**Resumo:**
- `deleteSkill` usa `_focusSessionState.value.isRunning` real (em vez do `false` hardcoded do
  scaffolding antigo) — melhoria aprovada, só ficou possível depois do wiring real do Foco no
  Bloco 35, não é desvio de escopo.
- Conferido por leitura direta do `.kt` real: as 6 funções batem exatamente com o esqueleto
  proposto no prompt, `SkillsScreen` em `MainActivity.kt` usa `state.skills` real e os 6
  callbacks chamam `heroLogViewModel` diretamente, `sampleSkills`/`var skills by remember`
  confirmadamente ausentes (grep vazio).

**Validação:**
- Build: sucesso.
- Testes: `HeroLogViewModelTest` 10/10 (4 antigos preservados + 6 novos:
  `viewModel_addCustomSkill_success_persistsSkills`,
  `viewModel_deleteSkill_eligibleAndDeletesSuccessfully`,
  `viewModel_prestigeSkill_resetsXpAndLevel_increasesPrestigeCounter`,
  `viewModel_deleteSkill_blockedDuringActiveFocusSession`,
  `viewModel_renameAndTagOperations_persistsCorrectly`,
  `viewModel_addCustomSkill_validationError_doesNotPersist`, todos PASSED).
- **Regressão do módulo Foco cobrada explicitamente** (relatório inicial só trouxe
  `HeroLogViewModelTest`, faltando as 4 suítes de Foco — pedido de novo e entregue): 26/26 testes
  PASSED (`FocusSessionViewModelTest` 12/12, `FocusSessionConfirmTest` 5/5,
  `FocusSessionRecoveryTest` 4/4, `FocusSessionRepositoryTest` 5/5). Nomes de teste conferidos
  contra execuções anteriores já auditadas (Blocos 34/35), sem sinal de fabricação
  (`ARMADILHAS_CONHECIDAS.md` #9).
- Commit `d521044` puxado e conferido via `bash_tool` — as 6 funções do ViewModel e o wiring da
  `MainActivity` lidos diretamente do arquivo real antes de aceitar o fechamento.

**Desvios de escopo aprovados:**
- `deleteSkill` usando `isFocusSessionRunning` real em vez de hardcoded (ver Resumo acima).

**Pendências que continuam em aberto:**
- Os 2 prints reais do fluxo completo do Foco (pendência acumulada do Bloco 35) — agora
  desbloqueados, já que criar uma skill pela aba Habilidades finalmente persiste em
  `characterState.skills`. Bruno vai validar manualmente no preview e trazer os prints.
- Mesmas pendências de sempre: `IncursionModeModal.tsx`, Bloco 3 da Morte Cognitiva, inspeção
  visual das screenshots acumuladas, timer de descanso automático e SFX, tela de configuração
  pré-sessão real (seletor de skill/duração), reset do checkbox `isDungeonMode`.

---

## [2026-08-15] Validação visual humana do fluxo completo do Foco (Blocos 33-36) — CONFIRMADA

Bruno rodou o preview real (com `durationMinutes` temporariamente trocado de 25 para 1, só pra
teste manual — troca hardcoded pontual, não é a tela de duração real) e trouxe 4 screenshots
reais do fluxo ponta a ponta: sessão ativa contando (`FocusModeScreen`, `togglePauseQuest`/
`cancelSession` funcionando) → `FocusCompletionFlow` completo — `streak` → `summary` → `loot`
pulado corretamente (sem drop nessa sessão) → `notes` → confirmação real.

**Confirmado por inspeção visual direta**: textos, cores e valores batem com o esperado —
rank "S" / "Sem Pausas — Lendário", "DURAÇÃO DA SESSÃO / 1 MIN", "SEQUÊNCIA DE CHAMA / 🔥 1 DIA",
caixa de XP/GP ("+2 XP" verde, "+3 GP" âmbar) todos vindos do cálculo real via
`FocusApplyLogic`/`FocusRewardsLogic`, não de dado mockado. Botão final "RECEBER RECOMPENSAS"
correto (última etapa, sem tag de skill pois a skill de teste não tinha tags). Streak screen com
ícone de chama, texto e "1 dia" conforme spec do Bloco 33.

**Isso fecha, com validação humana real (não só teste automatizado), o ciclo completo do módulo
de Foco: motor de sessão (29) → persistência (30-32) → UI de conclusão (33) → aplicação real (34)
→ wiring de tela (35) → correção da lacuna de Habilidades que travava tudo (36).**

**Pendência de limpeza registrada, não esquecer**: `durationMinutes = 1` em `MainActivity.kt`
(trecho do botão "Entrar em Modo Foco") precisa voltar para `25` (ou para o que for decidido na
futura tela de configuração pré-sessão real) antes de considerar esse trecho "de produção" —
troca temporária feita só pra viabilizar o teste manual rápido acima, registrada aqui para não
ser esquecida no meio de outra frente de trabalho.

**[Fechado em seguida]**: revertido para `25` por prompt dedicado, confirmado por Bruno.

---

## [2026-08-16] Bloco: 37 — Wiring real da Morte Cognitiva (lifecycle ON_STOP/ON_START)

**Contexto**: `CognitiveDeathLogic.kt` (regras puras) e `CognitiveDeathOverlays.kt` +
`FocusModeScreen.kt` (UI, já hoisted com `isGraceActive`/`graceSecondsLeft`/`isPlayerDead`/
`onReturnToFocusCap`/`onRespawn`) já existiam prontos de sessões anteriores — faltava só a
camada de lifecycle + timer que liga os dois.

**Decisão de design confirmada por Bruno antes do prompt**: gatilho = app indo pra background
(`ON_STOP`/`ON_PAUSE`, decisão de escopo já registrada anteriormente). Mecânica: o timer de
grace period roda enquanto o app está em background e para **automaticamente** ao voltar
(`ON_START`/`ON_RESUME`), sem precisar de clique em botão nenhum — propósito é penalizar abrir
outro app durante o Pomodoro; voltar já resolve. `onReturnToFocusCap` (botão "SELAR PORTAL DE
FOCO") chama a mesma função de cancelamento do grace, só como reforço/redundância, não como
gatilho principal.

**Arquivos criados/alterados:**
- app/src/main/java/com/iurispraecepta/herolog/logic/focus/FocusSessionState.kt (3 campos
  novos: `isGraceActive`, `graceSecondsLeft`, `isPlayerDead`)
- app/src/main/java/com/iurispraecepta/herolog/ui/HeroLogViewModel.kt (`graceTickJob`/
  `graceEndTimeMillis` + `onAppBackgrounded`, `startGracePeriod`, `triggerCognitiveDeath`,
  `onAppForegrounded`, `returnToFocusFromGrace`, `respawnHero`)
- app/src/main/java/com/iurispraecepta/herolog/MainActivity.kt (`DisposableEffect` com
  `LifecycleEventObserver` no `setContent` raiz, uma única vez, mapeando `ON_STOP`→
  `onAppBackgrounded()` e `ON_START`→`onAppForegrounded()`; wiring dos parâmetros hoisted em
  `FocusModeScreen`; condição de renderização ajustada)
- app/src/test/java/com/iurispraecepta/herolog/HeroLogViewModelTest.kt (6 testes novos)

**Melhorias não pedidas explicitamente, verificadas e aceitas como corretas:**
- Cancelamento de `graceTickJob` também em `startSession()`/`cancelSession()` — evita job
  vazado, sensato.
- `isRunning = false` setado dentro de `triggerCognitiveDeath()` (eu não tinha especificado
  isso no esqueleto do prompt) — correto, pois a sessão de fato "morreu"/parou de contar.
- Condição de renderização da aba Foco ajustada de `focusState.isRunning` para
  `focusState.isRunning || focusState.isPlayerDead` — consequência direta e necessária da
  melhoria acima (senão o overlay de morte sumiria da tela assim que `isRunning` virasse
  `false`, antes do respawn).

**Validação:**
- Build: sucesso.
- Testes: `HeroLogViewModelTest` 16/16 (10 antigos preservados + 6 novos:
  `viewModel_onAppBackgrounded_withWildernessActive_startsGracePeriod`,
  `viewModel_onAppBackgrounded_withDeathProofTitle_convertsToPauseInsteadOfGrace`,
  `viewModel_onAppBackgrounded_withoutWildernessChecked_isNoOp`,
  `viewModel_gracePeriodExpiring_triggersCognitiveDeath`,
  `viewModel_onAppForegrounded_duringGrace_cancelsGraceWithoutClick`,
  `viewModel_respawnHero_appliesPenalties_andResetsFocusSessionState`, todos PASSED). Regressão
  completa do módulo Foco: `FocusSessionViewModelTest` 12/12, `FocusSessionConfirmTest` 5/5,
  `FocusSessionRecoveryTest` 4/4, `FocusSessionRepositoryTest` 5/5 — 42 testes no total, 0
  falhas. XML bruto conferido, nomes batem 1:1 com o `.kt` real
  (`ARMADILHAS_CONHECIDAS.md` #9 — sem sinal do padrão).
- Commit `77b56cb` puxado e conferido via `bash_tool` — as 6 funções do ViewModel, os 3 campos
  novos de `FocusSessionState`, o `DisposableEffect` único no `MainActivity.kt` e a mudança de
  condição de renderização lidos diretamente do arquivo real antes de aceitar o fechamento.
- Visual: ainda pendente (nenhuma inspeção visual humana deste fluxo específico ainda — grace
  period/morte/respawn requerem simular app indo a background, mais trabalhoso de testar
  manualmente que o fluxo normal de conclusão).

**Desvios de escopo aprovados:**
- Os três itens listados em "Melhorias não pedidas explicitamente" acima — todos verificados
  como corretos e necessários, não como scope creep disfarçado.

**Pendências que continuam em aberto:**
- Inspeção visual humana do fluxo de grace period/morte/respawn (pendência nova, específica
  deste bloco) — precisa simular app indo a background durante sessão wilderness.
- Mesmas pendências de sempre: `IncursionModeModal.tsx`, timer de descanso automático e SFX,
  tela de configuração pré-sessão real (seletor de skill/duração), reset do checkbox
  `isDungeonMode`.

---

## [2026-08-16] Bloco: 38 — Port de `IncursionModeModal.tsx` + `ModeDescriptionModal.tsx`

**Contexto**: correção de registro prévia (ver `PARIDADE.md`, entrada "CORREÇÃO DE CORREÇÃO") —
`IncursionModeModal.tsx` não era o segmented control já portado, é um modal separado com 3 cards.
`ModeDescriptionModal.tsx` (modal genérico de descrição) também existe de fato e é o que os 3
botões "?" já prontos em `RaidModeSection.kt` esperavam. Ambos os arquivos React dumpados
literalmente por Bruno nesta sessão (SHA `66f7bc3...`) antes do prompt.

**Arquivos criados/alterados:**
- app/src/main/java/com/iurispraecepta/herolog/ui/focus/IncursionModeModal.kt (novo)
- app/src/main/java/com/iurispraecepta/herolog/ui/focus/ModeDescriptionModal.kt (novo)
- app/src/test/java/com/iurispraecepta/herolog/IncursionModeModalScreenshotTest.kt (novo, 5 testes)
- app/src/test/java/com/iurispraecepta/herolog/ModeDescriptionModalScreenshotTest.kt (novo, 4 testes)
- app/src/main/java/com/iurispraecepta/herolog/MainActivity.kt (wiring dos 3 botões "?" +
  ponto de entrada novo "Explorar Modos de Incursão" pro `IncursionModeModal`)

**Resumo:**
- Reusa `HeroLogModal`/`ModalVariant` já existente (equivalente de `components/Modal.tsx`), sem
  recriar wrapper de modal.
- `ModeDescriptionBlock` implementado como `typealias` de `RaidModeHelpBlock` já existente —
  decisão de reuso total, zero duplicação de dados, conforme pedido no prompt.
- Ícones `lucide-react` (`Sparkles`/`Swords`/`Skull`) aproximados por Material Icons
  (`AutoAwesome`/`Shield`/`Warning`) — mesma linha de decisão já usada em blocos anteriores.
- Badge de Masmorra em cooldown reusa `formatDungeonCooldown` já existente, sem duplicar.
- Ponto de entrada do `IncursionModeModal`: botão "Explorar Modos de Incursão" na aba Foco,
  entre `RaidModeInfoBox` e o botão de iniciar sessão.

**Validação:**
- Build: sucesso.
- Testes: `IncursionModeModalScreenshotTest` 5/5, `ModeDescriptionModalScreenshotTest` 4/4,
  `RaidModeSectionTest` 4/4, `RaidModeSectionScreenshotTest` 5/5 (regressão), todos PASSED. XML
  bruto conferido, nomes batem 1:1 com os `.kt` reais.
- Commit `38d296e` puxado e conferido via `bash_tool` — typealias, ícones, `RaidMode`/
  `onSelectMode`, e wiring da `MainActivity` lidos diretamente do arquivo real antes de aceitar.
- **Visual confirmado por print real de Bruno**: `IncursionModeModal` aberto mostrando os 3
  cards (Padrão/Masmorra ativa com badge/Selvagem), cores e textos batendo com o esperado.

**Desvios de escopo aprovados:**
- Nenhum.

**Pendências que continuam em aberto:**
- Inspeção visual humana do fluxo de grace period/morte/respawn (Bloco 37).
- `ModeDescriptionModal` ainda sem print real confirmado (só screenshot automatizado).
- Timer de descanso automático e SFX, tela de configuração pré-sessão real completa (seletor de
  skill/duração de verdade — o "Explorar Modos de Incursão" deste bloco é só uma peça dela),
  reset do checkbox `isDungeonMode`.

---

## [2026-08-16] Validação visual humana do `ModeDescriptionModal` (Bloco 38) — CONFIRMADA

Bruno trouxe 3 prints reais dos 3 modais de ajuda abertos: Terra Selvagem (vermelho), Incursão
por Masmorra (roxo), Modo Padrão & Saques (âmbar). Cores por variante corretas, texto literal
batendo com `RaidModeHelpContent`. Bloco "BÔNUS POR TÍTULO RARO" (modo Padrão) confirma dado
dinâmico real vindo de `LootConfig.TITLE_LOOT_MULTIPLIERS` (6 títulos: Transcendente, Celestial,
Shadow, Caminhante do Vazio, Immortal Scholar, Nocturnal), não mockado. Botão "ENTENDIDO" com a
cor correta em cada variante.

**Isso fecha, com validação humana real, o Bloco 38 por completo** (`IncursionModeModal` já
confirmado anteriormente, `ModeDescriptionModal` confirmado agora).

---

## [2026-08-16] Bloco: 39 — Wiring real do Timer de Descanso (`useBreakTimer.ts`)

**Contexto**: fonte real de `useBreakTimer.ts` (e, por descoberta paralela,
`useAmbientSound.ts`/`audio.ts`) obtida nesta sessão (commit `5a008c16...` do HeroLog React).
Escopo restrito por decisão de Bruno: só o timer de descanso em si — SFX sintetizado e som
ambiente ficaram de fora (feature maior à parte, fora de escopo até nova decisão).

**Arquivos criados/alterados:**
- app/src/main/java/com/iurispraecepta/herolog/logic/focus/BreakTimerState.kt (novo)
- app/src/main/java/com/iurispraecepta/herolog/ui/HeroLogViewModel.kt (`_breakTimerState`/
  `breakTimerState`, `breakTickJob`/`breakEndTimeMillis`, `enterBreakPrep`, `startBreakTimer`,
  `onBreakTimerCompleted`, `skipBreak`; wiring de auto-start dentro de `confirmFocusSession`)
- app/src/main/java/com/iurispraecepta/herolog/MainActivity.kt (layout mínimo testável na aba
  Foco: indicador de tempo + botão "Pular Descanso"/"Iniciar Descanso")
- app/src/test/java/com/iurispraecepta/herolog/HeroLogViewModelTest.kt (6 testes novos)

**Resumo:**
- `startBreakTimer`/`onBreakTimerCompleted` seguem o mesmo padrão de `endTime` absoluto +
  `clock()` injetável já usado em `startFocusTickJob` — decisão de design confirmada: não
  precisa de listener de lifecycle extra pra resync (diferente do React, que precisa de
  `visibilitychange` por causa de `setInterval` no browser).
- `confirmFocusSession` agora decide entre `startBreakTimer` (duração curta ou longa, conforme
  `pomodoroSettings.shortBreakDuration`/`longBreakDuration`, e se é a 4ª sessão de masmorra) ou
  `enterBreakPrep`, replicando fielmente a lógica de `handleConfirmClaimRewards` real.
- **Melhoria não pedida explicitamente, verificada e aceita como correta**: cancelamento
  preventivo de `breakTickJob` + reset de `_breakTimerState` dentro de `startSession()` — evita
  job de descanso vazado se o usuário iniciar uma nova sessão de foco durante o prep/timer de
  descanso anterior.
- SFX (`sound.playLevelUp()`) deixado como comentário/ponto de extensão vazio dentro de
  `onBreakTimerCompleted`, conforme pedido — nenhum áudio real implementado.

**Validação:**
- Build: sucesso.
- Testes: `HeroLogViewModelTest` 22/22 (16 antigos preservados + 6 novos:
  `breakTimer_startBreakTimer_setsBreakActive_andCountsDownCorrectly`,
  `breakTimer_skipBreak_resetsToDefaultState`,
  `breakTimer_reachingZero_completesBreakAndDeactivates`,
  `confirmFocusSession_withAutoStartBreakTrue_standardSession_startsShortBreakTimer`,
  `confirmFocusSession_withAutoStartBreakTrue_fourthDungeonSession_startsLongBreakTimer`,
  `confirmFocusSession_withAutoStartBreakFalse_entersBreakPrepInsteadOfTimer`, todos PASSED).
  Regressão completa do módulo Foco: `FocusSessionViewModelTest` 12/12, `FocusSessionConfirmTest`
  5/5, `FocusSessionRecoveryTest` 4/4, `FocusSessionRepositoryTest` 5/5 — 48 testes no total, 0
  falhas. XML bruto conferido, nomes batem 1:1 com o `.kt` real.
- Commit `b88bd5d` já veio commitado (sem necessidade de pedido extra) — puxado e conferido via
  `bash_tool`. Verificado especificamente que `cancelSession()` (chamada dentro de
  `startBreakTimer` por segurança) não zera `_breakTimerState` por engano — sem conflito de
  ordem entre as duas chamadas.
- Visual: ainda pendente (layout mínimo testável, não a UI fiel ao React — fica pra bloco
  dedicado futuro).

**Desvios de escopo aprovados:**
- Cancelamento preventivo em `startSession()` (ver Resumo acima) — verificado como correto e
  necessário, não scope creep disfarçado.

**Pendências que continuam em aberto:**
- UI fiel ao React do timer de descanso (hoje é só layout mínimo testável).
- SFX sintetizado (`audio.ts`) e som ambiente (`useAmbientSound.ts`) — fora de escopo até nova
  decisão, fonte real já obtida e registrada para quando for retomado.
- Inspeção visual humana do fluxo de grace period/morte/respawn (Bloco 37).
- Tela de configuração pré-sessão real completa (seletor de skill/duração de verdade), reset do
  checkbox `isDungeonMode`.

---

## [2026-08-16] Bloco: 40 — Port do modal "Ajustes do Timer" + wiring do seletor de skill real

**Contexto**: fonte real obtida via clone direto do repositório React (`b805fdd3...`), trecho de
`App.tsx` ("Timer Settings Modal") colado por Bruno. Motivação: destravar teste manual real do
timer de descanso (Bloco 39) sem hack hardcoded, e resolver a pendência de skill/duração fixas
do Bloco 35.

**Arquivos criados/alterados:**
- app/src/main/java/com/iurispraecepta/herolog/ui/focus/TimerSettingsModal.kt (novo)
- app/src/main/java/com/iurispraecepta/herolog/ui/HeroLogViewModel.kt (`changeFocusDuration`,
  `saveCustomTimerSettings`, `toggleAutoStartBreak`, `toggleAutoStartFocus`; depois,
  `cancelAllTimers()`/`onCleared()`, ver "correção" abaixo)
- app/src/main/java/com/iurispraecepta/herolog/MainActivity.kt (wiring de `SkillSelectorModal`
  já existente — reusado, não duplicado — + `TimerSettingsModal` novo na aba Foco)
- app/src/test/java/com/iurispraecepta/herolog/HeroLogViewModelTest.kt (5 testes novos)
- app/src/test/java/com/iurispraecepta/herolog/TimerSettingsModalScreenshotTest.kt (novo, 5 testes)

**Resumo:**
- Presets (25/50/90min), duração personalizada com validação de faixa (Foco 1-180, Pausas
  1-60), 2 toggles de auto-start — tudo conferido linha a linha contra a fonte real, sem
  divergência.
- `SkillSelectorModal.kt` (já existente desde antes) reusado direto, não recriado nem duplicado
  — apesar do relatório inicial ter afirmado incorretamente que o arquivo mudou de pasta.
- Ponto de entrada: ícone de engrenagem no `FocusModeScreen`/card do timer pro
  `TimerSettingsModal`; card de skill ativa clicável pro `SkillSelectorModal`.

**Processo desta sessão — padrão de relatório digno de nota (não é armadilha nova de AI Studio
no sentido usual, mas vale registrar)**: o primeiro relatório de fechamento tinha 3 imprecisões
na prosa (listou só 1 teste de cada suíte quando existiam 5; afirmou que `SkillSelectorModal.kt`
tinha mudado de `ui/skills/` pra `ui/focus/`, o que não era verdade; afirmou alteração em
`FocusModeScreen.kt` que não constava no diff real) — mas o CÓDIGO em si estava correto; era só
a prosa-resumo que estava errada/desatualizada. Confirmado por leitura direta do diff
(`git diff`) e dos arquivos reais via `bash_tool` antes de aceitar qualquer coisa. Também faltou
XML bruto nas duas primeiras tentativas (só tabela-resumo em prosa) — pedido explicitamente uma
terceira vez até vir o XML literal.

**Correção real de bug encontrada durante a auditoria** (não pedida no prompt original, aceita
como correta): `cancelSession()` não cancelava `breakTickJob` — se um timer de descanso estivesse
rodando e o usuário cancelasse/iniciasse nova sessão de foco, o job antigo podia ficar vazado
rodando em paralelo. Corrigido extraindo `cancelAllTimers()` (cancela `focusTickJob`+
`graceTickJob`+`breakTickJob`) e adicionando `override fun onCleared()` chamando essa função —
boa prática padrão de ViewModel Android. Verificado via diff real (`git diff`), não só descrito.

**Validação:**
- Build: sucesso.
- Testes: `HeroLogViewModelTest` 27/27 (22 antigos + 5 novos:
  `changeFocusDuration_persistsNewFocusDuration_whenIdle`,
  `changeFocusDuration_isNoOp_whenSessionRunningOrBreakActive`,
  `saveCustomTimerSettings_persistsCustomValues_whenIdle`,
  `saveCustomTimerSettings_isNoOp_whenSessionRunningOrBreakActive`,
  `toggleAutoStartBreak_and_toggleAutoStartFocus_invertsAndPersists`).
  `TimerSettingsModalScreenshotTest` 5/5 (`timerSettingsModal_preset25_active_screenshot`,
  `_preset50_active_screenshot`, `_custom_valid_screenshot`, `_custom_invalid_screenshot`,
  `_toggles_both_active_screenshot`). Todos PASSED, XML bruto conferido na terceira tentativa,
  nomes batem 1:1 com os `.kt` reais.
- Commits `f664c17` (feature) + `683fec4` (correção de `cancelAllTimers`/`onCleared` +
  ajuste de scheduler de teste `advanceUntilIdle→runCurrent`, que resolveu um hang real do
  Gradle Test Executor causado por loop `while(true)` de tick job tentando avançar tempo virtual
  por 25+ minutos) — ambos puxados e conferidos via `bash_tool`.

**Desvios de escopo aprovados:**
- `cancelAllTimers()`/`onCleared()` (ver "Correção real de bug" acima) — verificado como correto
  e necessário, não scope creep disfarçado.

**Pendências que continuam em aberto:**
- UI fiel ao React do timer de descanso (Bloco 39, ainda só layout mínimo).
- SFX sintetizado e som ambiente — fora de escopo até nova decisão.
- Inspeção visual humana do fluxo de grace period/morte/respawn (Bloco 37).
- Reset do checkbox `isDungeonMode` na UI de seleção pré-sessão.

---

## [2026-08-16] Validação visual humana do Bloco 40 — CONFIRMADA (e limitação registrada pro Bloco 37)

Bruno trouxe 2 prints reais: (1) aba Foco em estado idle mostrando card de skill real ("Foco
Profundo", Nível 1, botão "Trocar"), segmented control de modo, botões "Modos Incursão"/
"Ajustes (25 m) ⚙️" e "Entrar em Modo Foco (25 min)" — tudo refletindo dados reais de
`characterState`, não mais fixo; (2) `TimerSettingsModal` aberto mostrando os 3 presets, seção
de duração personalizada ligada com campos Foco/P.Curta/P.Longa preenchidos, botão "Salvar
Personalizado", e os 2 toggles de auto-start (desligados). **Bloco 40 fechado com validação
visual completa.**

Tentativa de validar visualmente o Bloco 37 (Morte Cognitiva) esbarrou numa limitação de
ambiente, não um bug — ver `ARMADILHAS_CONHECIDAS.md` #11 (nova entrada): minimizar a janela do
preview do AI Studio não dispara `ON_STOP` real da Activity, porque o preview roda num
iframe/WebView do navegador. Validação real desse bloco específico vai precisar de um APK de
debug instalado num device/emulator de verdade, fora do preview.

---

## [2026-08-16] Bloco: 41 — UI fiel do timer de descanso (`BreakPrepScreen` + `FocusOrb` esmeralda)

**Nota de processo**: este bloco foi implementado no AI Studio no dia 16/08, mas só documentado
aqui em 18/08, retroativamente — Bruno não conseguiu commitar (loop de login OAuth GitHub↔AI
Studio, ver `ARMADILHAS_CONHECIDAS.md`/sessão de 17/08), então o fechamento formal ficou pra trás
até o código real ser auditado nesta sessão via colagem literal. Escopo de arquivos tocados
inicialmente sub-reportado pelo AI Studio (só 3 de 6 arquivos) — corrigido após auditoria (ver
"Correção de escopo" abaixo). Não reconstruído de memória: todo trecho citado abaixo foi colado
literalmente por Bruno nesta sessão de auditoria.

**Arquivos criados/alterados:**
- app/src/main/java/com/iurispraecepta/herolog/ui/focus/BreakPrepScreen.kt (novo)
- app/src/main/java/com/iurispraecepta/herolog/logic/focus/BreakTimerState.kt (modificado —
  campo novo `wasLastSessionDungeonMode: Boolean`)
- app/src/main/java/com/iurispraecepta/herolog/ui/HeroLogViewModel.kt (modificado —
  `enterBreakPrep(wasDungeonMode: Boolean = false)` agora seta `wasLastSessionDungeonMode`;
  método novo `selectBreakDuration(minutes: Int)`)
- app/src/main/java/com/iurispraecepta/herolog/MainActivity.kt (modificado — fallback textual
  simples de break prep substituído pelo composable `BreakPrepScreen(...)` real)
- app/src/test/java/com/iurispraecepta/herolog/BreakPrepScreenScreenshotTest.kt (novo)
- app/src/test/java/com/iurispraecepta/herolog/HeroLogViewModelTest.kt (modificado — testes de
  `selectBreakDuration`/`enterBreakPrep`, incluindo `confirmFocusSession_setsWasLastSession
  DungeonMode_inBreakPrepState`)

**Resumo:**
- `BreakPrepScreen` (tela nova) usada quando `autoStartBreak` está desligado — apresenta seletor
  de duração curta/longa (`BreakDurationOption`) e botões Iniciar/Pular Descanso.
- `isDungeonMode` (prop de `BreakPrepScreen`, alimentada por `breakTimerState.
  wasLastSessionDungeonMode`) oculta o seletor curto/longo quando a sessão concluída era de
  masmorra — fiel à regra de que a duração do descanso de masmorra é prescrita pelo jogo
  (curta/longa conforme progressão, decidida em `confirmFocusSession`), não escolha do jogador.
- `FocusOrb` já existente reaproveitado sem alteração para o estado `isBreakActive` — já suportava
  a variante esmeralda desde antes (Bloco 18), confirmado sem necessidade de mudança no
  `FocusOrb.kt`.

**Correção de escopo (18/08, achado durante auditoria retroativa)**: relatório original do AI
Studio (16/08) listava só 3 arquivos tocados (`BreakPrepScreen.kt`, `BreakTimerState.kt`,
`BreakPrepScreenScreenshotTest.kt`), afirmando explicitamente "nenhum outro arquivo de código do
projeto foi alterado ou impactado". Auditoria de 18/08 (motivada por um teste de
`HeroLogViewModelTest` referenciando `wasLastSessionDungeonMode`, campo que só existe por causa
deste bloco) revelou que `HeroLogViewModel.kt` e `MainActivity.kt` também foram modificados, e não
constavam na lista original. Corpo de `confirmFocusSession`/`enterBreakPrep`/`selectBreakDuration`
conferido literal antes de aceitar a correção. Mesma categoria de risco da Armadilha #4 (desvio
silencioso), mas aqui o desvio era uma omissão de escopo já implementado, não um scope creep
ativo — o código em si parece correto, só o relatório de arquivos tocados estava incompleto.

**Validação:**
- Build/Testes: não confirmados via XML bruto nesta sessão de auditoria retroativa — só o corpo
  de código foi conferido literal, não a execução dos testes. Pendência: pedir XML bruto de
  `BreakPrepScreenScreenshotTest` e da fatia nova de `HeroLogViewModelTest` numa próxima sessão,
  se quiser fechar a validação de teste com o mesmo rigor dos blocos anteriores.
- Visual: **confirmado por print real de Bruno em 18/08** — ver entrada de validação conjunta
  abaixo (Blocos 41+42).
- Commit: **ainda não realizado** — bloqueio de integração GitHub↔AI Studio, ver seção de risco
  correspondente na sessão de 17/08. Código existe só no ambiente AI Studio.

**Desvios de escopo aprovados:**
- Nenhum (a correção de escopo acima é sobre relato incompleto, não sobre trabalho não aprovado).

---

## [2026-08-17/18] Bloco: 42 — Reset de `isDungeonMode`/`dungeonSessionsProgress` (fim de ciclo e abandono)

**Contexto**: dois pontos de reset confirmados na fonte real (`App.tsx`, commit `b805fdd3`) que
resetam `isDungeonMode`+`dungeonSessions` juntos: (1) `handleConfirmClaimRewards`
(linha ~1483-1499), ao completar a 4ª sessão de masmorra — já parcialmente portado no Bloco 34
(só a metade numérica); (2) `handleAbandonSession` (linha ~1241-1248), ao abandonar sessão em
modo masmorra — não portado até este bloco. Bruno aprovou tratar os dois juntos, mesma família de
bug.

**Arquivos criados/alterados:**
- app/src/main/java/com/iurispraecepta/herolog/ui/HeroLogViewModel.kt (nova função
  `abandonSession()`)
- app/src/main/java/com/iurispraecepta/herolog/MainActivity.kt (`onExit` do `FocusModeScreen`
  trocado de `cancelSession()` para `abandonSession()` + reset direto de `isDungeonModePreview`;
  `LaunchedEffect(dungeonSessionsProgress)` novo)
- app/src/test/java/com/iurispraecepta/herolog/HeroLogViewModelTest.kt (3 testes novos)

**Resumo:**
- `abandonSession()` captura `_focusSessionState.value.config?.isDungeonMode` **antes** de chamar
  `cancelSession()` (que fica intocada), e só então zera `_dungeonSessionsProgress` se o valor
  capturado era `true`. Deliberadamente isolada de `cancelSession()` — essa é chamada
  internamente como rede de segurança de dentro de `startBreakTimer()` (por sua vez chamada de
  dentro de `confirmFocusSession`, antes do próprio `_focusSessionState` ser resetado); colocar a
  lógica de reset dentro de `cancelSession()` zeraria `dungeonSessionsProgress` por engano no meio
  de uma progressão de masmorra em andamento (ex.: 2/4 virando 0/4 sem o usuário ter abandonado
  nada).
- `MainActivity.kt`: `isDungeonModePreview = false` setado direto e incondicionalmente dentro do
  `onExit` (idempotente se já era `false`), cobrindo o cenário de abandono independente de
  timing/recomposição.
- `LaunchedEffect(dungeonSessionsProgress)`: reseta `isDungeonModePreview` quando o contador chega
  a zero vindo de um valor positivo — cobre o cenário de conclusão da 4ª sessão (que já zera
  `dungeonSessionsProgress` desde o Bloco 34), sem precisar expor evento novo do ViewModel.

**Validação:**
- Build: sucesso.
- Testes: `HeroLogViewModelTest` 32/32 (3 novos: `abandonSession_whenDungeonModeActive_
  resetsDungeonSessionsProgressToZero`, `abandonSession_whenNotDungeonMode_
  leavesDungeonSessionsProgressUnchanged`, `abandonSession_alwaysClearsFocusSessionState
  AndPersistedSession`, todos PASSED). XML bruto conferido, nomes batem 1:1 com o `.kt` real.
- Diff restrito confirmado via `git diff --stat` equivalente (ambiente sem `.git` ativo, output
  do próprio sandbox do AI Studio): `HeroLogViewModel.kt` +8, `MainActivity.kt` +9/-1,
  `HeroLogViewModelTest.kt` +109 — só os 3 arquivos esperados, nenhum desvio.
- Confirmado explicitamente que `cancelSession()` **não** foi alterada (só a nova
  `abandonSession()` foi adicionada) — ponto mais frágil da correção, verificado por trecho
  literal antes de aceitar.
- Visual: **confirmado por print real de Bruno em 18/08** — ver entrada de validação conjunta
  abaixo.
- Commit: **ainda não realizado**, mesmo bloqueio do Bloco 41.

**Desvios de escopo aprovados:**
- Nenhum.

**Fora de escopo, registrado como pendência separada (não implementado neste bloco)**:
- Fluxo de dupla confirmação de abandono (`isConfirmingAbandon`, clique de novo em 5s pra
  confirmar) que existe no React — Android sai direto no primeiro clique, sem confirmação.
- Log de sistema "FRACASSO NA MASMORRA" ao abandonar — não há infraestrutura de log real no
  Android ainda (`addSystemLog`/`onLog` não plugado a nada, mesma pendência de sempre).

---

## [2026-08-18] Validação visual humana dos Blocos 41 e 42 — CONFIRMADA

Bruno trouxe 4 prints reais:

1. Card de preparação com "Explorando Masmorra (3/4)" e sessão de 1 min configurada (via
   `TimerSettingsModal`, usado pra acelerar o teste manual).
2. Tela de conclusão da 4ª sessão: rank "S — Sem Pausas — Lendário", `+3 XP`/`+2503 GP` — confirma
   que o bônus de 2.500 GP da 4ª masmorra continua aplicado corretamente, sem regressão do reset.
3. `RaidModeInfoBox` mostrando "Explorando Masmorra (0/4)" — confirma contador limpo depois do
   reset, tanto no caminho de conclusão quanto no de abandono (Bruno testou os dois manualmente:
   completar as 4 sessões E abandonar no meio de uma masmorra — em ambos os casos o segmented
   control voltou sozinho pra "Padrão").
4. `FocusOrb` em estado de descanso ativo (verde-esmeralda, `04:59`, botão "Pular Descanso") —
   confirma autostart de descanso funcionando; Bruno confirmou separadamente (sem print) que sem
   autostart aparece a tela de escolha iniciar/pular, batendo com o `BreakPrepScreen` do Bloco 41.

**Isso fecha, com validação humana real, os Blocos 41 e 42 por completo** — lógica, teste (onde
auditado) e visual. Pendência que continua: commit real no GitHub, bloqueado pela integração
GitHub↔AI Studio (ver sessão de 17/08); código existe só no ambiente AI Studio até a trava ser
resolvida ou contornada.

---

## [2026-08-18] Bloco: A — Módulo Missões, modelos de dados (`QuestTypes.kt`)

**Contexto**: início do módulo Missões (Habits/Dailies/Todos/Quests/History). Mapeamento
pré-port completo recebido (`MOC_missoes_pre_port.md`, contra commit React `d8fe53586`).

**Arquivos criados:**
- app/src/main/java/com/iurispraecepta/herolog/logic/quests/QuestTypes.kt (novo)
- app/src/test/java/com/iurispraecepta/herolog/QuestTypesTest.kt (novo)

**Resumo:** `Habit`/`Daily`/`Todo`/`ChecklistItem`/`DifficultyRewards` + `getDifficultyRewards`
(tabela XP/Gold/Dano por dificuldade, compartilhada pelos três tipos, fiel à fonte). `Habit.up`/
`Habit.down` preservados no modelo com comentário KDoc explicando decisão de escopo aprovada: o
gate de qual botão a UI desenha fica só na Composable, o ViewModel não valida (endurecer isso é
pendência registrada, só depois de paridade total React↔Kotlin).

**Validação:**
- Build: sucesso.
- Testes: 8/8, XML bruto conferido.
- Diff: 2 arquivos, conferido contra Armadilha #1 (dump literal do `.kt`, não resumo) — bate
  1:1 com o pedido.

**Desvios de escopo aprovados:** nenhum.

---

## [2026-08-18] Bloco: B — `HabitLogic.kt` (trigger up/down) — **FECHADO COM BUGS, ver correção pendente**

**Arquivos criados:**
- app/src/main/java/com/iurispraecepta/herolog/logic/quests/HabitLogic.kt (novo)
- app/src/test/java/com/iurispraecepta/herolog/HabitLogicTest.kt (novo, 11 testes)

**Resumo do que foi implementado:** `trigger(habit, state, isUp, referenceDate)` — Up aplica
bônus de classe (Mage XP+20%/Warrior Gold+20%), gate de `streak` por `lastTriggeredDate`
(só incrementa 1x/dia), `combatXP += finalXP*0.4`. Down aplica dano com redução de Ranger
(`max(1, dano*0.7)`), reseta `streak=0`, não mexe em XP/Gold/CombatLevel.

**Validação no momento do fechamento:** build sucesso, 11/11 testes XML bruto, diff restrito a
2 arquivos, paths confirmados. **Passou na auditoria de processo padrão, mas a auditoria não
incluiu comparação literal contra a fonte React real do Habit especificamente** — a lógica foi
aceita por analogia declarada ("segue o mesmo padrão do FocusApplyLogic"), sem pedir o trecho
literal correspondente ao Habit.

### ⚠️ Correção retroativa necessária — 5 bugs confirmados (mesma sessão, horas depois)

Investigando um achado do combatXP no módulo Foco, foi pedido a Bruno o trecho real de
`useHabits.ts` (`handleTriggerHabit`) — Google AI Studio não tem acesso ao repo React, só Bruno.
Comparando com o código acima, linha a linha:

1. `combatXP` no Up: fonte real `+ finalXP` (100%), não `*0.4`.
2. `streak` no Up: fonte real incrementa **sempre**, sem gate de `lastTriggeredDate`.
3. HP no Up: fonte real restaura `hp=maxHp` em level-up — feature ausente no código atual.
4. `streak` no Down: fonte real `max(0, streak-1)`, não reset pra `0`.
5. Morte por HP=0 fora de sessão de foco: fonte real tem `isPlayerDead` global (`App.tsx`,
   overlay `fixed inset-0`, `respawnHero()` reaproveitado) — Habit Down aciona isso. Equivalente
   Android não confirmado ainda, investigação em andamento no fim desta sessão.

Ver detalhes completos e a lição de metodologia extraída no `PARIDADE.md`, seção 6 (nota
crítica). **Bloco B2 (correção) ainda não foi escrito/executado** — próximo passo da próxima
sessão, ver handoff.

**Desvios de escopo aprovados:** nenhum (os bugs acima não foram aprovados — foram erros de
port não detectados na auditoria original).

---

## [2026-08-18] Bloco: C — `DailyLogic.kt` + `TodoLogic.kt` (toggle completo/incompleto)

**Contexto**: aproveitando a lição do Bloco B, este bloco teve a fórmula de `combatXP` e as
regras de HP/streak/value confirmadas contra o trecho literal de `handleToggleDaily`/
`handleToggleTodo` (`App.tsx`) **antes** de escrever o prompt, não depois.

**Arquivos criados:**
- app/src/main/java/com/iurispraecepta/herolog/logic/quests/DailyLogic.kt (novo)
- app/src/main/java/com/iurispraecepta/herolog/logic/quests/TodoLogic.kt (novo)
- app/src/test/java/com/iurispraecepta/herolog/DailyLogicTest.kt (novo, 9 testes)
- app/src/test/java/com/iurispraecepta/herolog/TodoLogicTest.kt (novo, 8 testes)

**Resumo:**
- `combatXP` usa 100% do XP ganho em ambos (`combatXP + finalXP`), confirmado contra fonte —
  diferente do Habit (bug) e do Foco (40%, mas esse é o valor correto lá).
- HP só restaura (`hp=maxHp`) no level-up ao **completar**; desmarcar nunca mexe em HP, mesmo
  causando level-down — assimetria real da fonte, preservada.
- `Daily.value` incrementa/decrementa sem clamp; `Daily.streak` clampa em 0 ao desmarcar —
  assimetria real preservada (não inventar clamp que a fonte não tem).
- `Todo.completedAt`: `Instant.now().toString()` (ISO-8601), equivalente ao
  `new Date().toISOString()` da fonte.
- `toggleChecklistItem` de Daily: puro, sem recompensa.
- Level-down (ao desmarcar) usa `CombatLogic.requiredXpForCombatLevel()` em vez do
  `currentCombatLevel*100` inline da fonte — mesmo resultado matemático, evita número mágico
  duplicado, já é o padrão usado em Habit/Foco.

**Validação:**
- Build: sucesso.
- Testes: 17/17 (9 Daily + 8 Todo), XML bruto conferido.
- Diff: 4 arquivos, paths confirmados explicitamente (Armadilha #12 — relatório inicial só
  citou os arquivos de teste, path de `DailyLogic.kt`/`TodoLogic.kt` confirmado à parte).

**Achado lateral resolvido**: `QuestLogic.kt`/`QuestCatalog.kt` apareceram mencionados na mesma
pasta sem terem sido pedidos nos Blocos A/B/C — investigado e esclarecido: pré-existentes de
módulo anterior (Missões da Guilda/Diárias de Foco, rotação LCG determinística), sem relação
com Habits/Dailies/Todos, sem desvio de escopo real. Ver `PARIDADE.md` seção 6 — recomendação de
auditoria formal futura desse sistema (nunca foi verificado contra a fonte com o mesmo rigor).

**Desvios de escopo aprovados:** nenhum.

---

## [2026-08-19] Bloco: B2 — Correção de Paridade do `HabitLogic.kt` (4 dos 5 achados do Bloco B)

**Contexto**: retomada da sessão de 18/08. Correção retroativa dos achados #1-4 registrados no
fechamento do Bloco B (ver acima) — achado #5 (morte global fora de sessão de foco) tratado à
parte, como Bloco B3, por ser estrutural (mexe em `FocusSessionState`/`CharacterState`), e ainda
pendente de execução. Antes de escrever o prompt, achado #5 foi reconfirmado com o AI Studio
(nenhum campo de morte global existe hoje fora de `FocusSessionState`; `respawnHero()` está
amarrado a `FocusSessionState`, não restaura `hp`) e com Bruno via fonte real de
`useFocusSession.ts` (`respawnHero`) — confirmado: mesmo `isPlayerDead`/mesmo `respawnHero()`
para os dois gatilhos (Wilderness/Foco e Habit), sem ramificação; bug real da fonte é a mesma
ausência de `hp: prev.maxHp` no respawn, já identificada por Bruno, ainda não mergeada no React.

**Arquivos alterados:**
- app/src/main/java/com/iurispraecepta/herolog/logic/quests/HabitLogic.kt

**Resumo do que foi corrigido (diff real conferido):**
1. `combatXP` no Up: `combatXP + (xpEarned * 0.4)` → `combatXP + xpEarned` (100%, igual à fonte).
2. `streak` no Up: removida a checagem `isFirstTriggerToday`/`lastTriggeredDate` que limitava a
   1x/dia — agora incrementa a cada trigger Up, mesmo múltiplos no mesmo dia.
3. HP no Up: adicionado `didLevelUp` + `nextHp = if (didLevelUp) state.maxHp else state.hp`,
   aplicado ao `updatedState` — restaura HP cheio em level-up, mesmo padrão de Daily/Todo/Foco.
4. `streak` no Down: `streak = 0` → `streak = max(0, habit.streak - 1)` (decremento, nunca
   negativo).

**Validação:**
- Diff real conferido linha a linha contra os 4 pontos pedidos — nada além do escopo foi tocado
  (achado #5/infra de morte não mexido, confirmado).
- Testes: 12/12, XML bruto conferido (`TEST-com.iurispraecepta.herolog.HabitLogicTest.xml`), 0
  falha, 0 erro. Nomes cobrem os 4 casos corrigidos (`appliesFullCombatXp_100Percent`,
  `incrementsStreakAndUpCount_evenOnMultipleTriggersSameDay`, `levelUp_restoresHpToMax`,
  `decrementsStreakClampedAtZero`) + regressão preservada (`noLevelUp_preservesCurrentHp`,
  `hpNeverGoesBelowZero`, bônus de classe Mage/Warrior/Ranger).

**Ponto de atenção registrado, não bloqueia fechamento**: `max(0, habit.streak - 1)` usa `max`
de dois `Int` — só compila sem import explícito se já havia `import kotlin.math.max` no arquivo
ou se o AI Studio adicionou; como o build/testes passaram, compilou de fato. Sem ação necessária.

**Bloco B2 fechado.** Bloco B3 (achado #5 — morte global) segue pendente: desenho proposto
(generalizar `isPlayerDead` para fora de `FocusSessionState`, `respawnHero()` aceitar chamada de
qualquer gatilho e restaurar `hp = maxHp`) aguardando confirmação de Bruno antes do prompt.

**Desvios de escopo aprovados:** nenhum.

---

## [2026-08-19] Bloco: B3 — Morte global fora de sessão de Foco (achado #5, último dos 5)

**Contexto**: achado #5 do Bloco B (ver acima) — fonte React confirmada: `isPlayerDead` e
`respawnHero()` são únicos e compartilhados sem ramificação entre os dois gatilhos de morte
(Wilderness/Foco e Habit Down). Android tinha `isPlayerDead` preso a `FocusSessionState`, e
`respawnHero()` amarrado a isso, sem restaurar `hp`. Desenho aprovado por Bruno antes do prompt:
mover `isPlayerDead` pra `CharacterState` (campo global), tornar `respawnHero()` genérico
(qualquer gatilho pode chamar) e restaurar `hp = maxHp` no respawn — corrigindo também o mesmo
bug que existe na fonte React atual (não mergeado lá ainda).

**Arquivos alterados:**
- app/src/main/java/com/iurispraecepta/herolog/model/CharacterModels.kt
- app/src/main/java/com/iurispraecepta/herolog/logic/CharacterMappers.kt
- app/src/main/java/com/iurispraecepta/herolog/logic/focus/FocusSessionState.kt
- app/src/main/java/com/iurispraecepta/herolog/logic/quests/HabitLogic.kt
- app/src/main/java/com/iurispraecepta/herolog/ui/HeroLogViewModel.kt
- app/src/main/java/com/iurispraecepta/herolog/MainActivity.kt

**Resumo do que foi implementado (diff real conferido):**
1. `isPlayerDead: Boolean = false` adicionado em `CharacterState` e espelhado em
   `CharacterSummary` (`CharacterModels.kt`); removido de `FocusSessionState.kt`.
   `CharacterMappers.kt` atualizado pra propagar o campo.
2. `HabitLogic.kt` (trigger Down): quando `nextHp <= 0`, seta `isPlayerDead = true` no
   `updatedState` — não chama `respawnHero()` diretamente (respawn continua ação do jogador via
   botão, igual à fonte). Up intocado (achados #1-4 do B2 preservados).
3. `respawnHero()` (`HeroLogViewModel.kt`): agora restaura `hp = charState.maxHp` e
   `isPlayerDead = false` no `characterState` salvo. Reset de `FocusSessionState()` deixou de ser
   incondicional — só reseta se havia sessão de fato em andamento/pausada/com config
   (`isRunning || isGraceActive || isPaused || config != null`), permitindo chamada fora de
   contexto de Foco sem efeito colateral indevido na sessão.
4. `triggerCognitiveDeath` (gatilho Wilderness/Foco, mesmo `HeroLogViewModel.kt`): setava
   `isPlayerDead = true` em `FocusSessionState` — migrado pra setar em `charState`, consequência
   direta da mudança de local do campo, não é lógica nova.
5. `onAppBackgrounded` e leitura de `isPlayerDead` em `MainActivity.kt`: migrados de
   `focusState.isPlayerDead` pra `characterState.isPlayerDead` — wiring, sem mudança de
   comportamento visível.

**Validação:**
- Diff real conferido arquivo a arquivo — escopo bate com o pedido (campo global + respawn
  genérico + hp restaurado + Habit Down setando o novo campo). Nenhuma mudança fora do previsto.
- Build passou — prova indireta de que nenhuma referência órfã a `FocusSessionState.isPlayerDead`
  sobrou em nenhum ponto do projeto (o campo foi de fato removido de lá).
- Testes: `HabitLogicTest` 14/14 (2 novos: `triggerDown_fatalDamage_setsIsPlayerDeadToTrue`,
  `triggerDown_nonFatalDamage_preservesIsPlayerDeadFalse`), 0 falha. `HeroLogViewModelTest`
  33/33, cobrindo os dois ramos do novo `respawnHero()` genérico:
  `viewModel_respawnHero_whenDeadOutsideFocus_restoresHpAndClearsDead_withoutFocusSession` (fora
  de sessão) e `viewModel_respawnHero_appliesPenalties_restoresHp_clearsDead_andResetsFocusSessionState`
  (dentro de sessão, reset de `FocusSessionState` preservado).

**Bloco B3 fechado.** Com isso, os 5 achados do Bloco B (18/08) estão todos corrigidos —
`HabitLogic.kt` volta a ser fiel à fonte React em Up, Down, streak, combatXP, HP e morte global.
Módulo Missões: Habits/Dailies/Todos (Blocos A/B2/B3/C) fechados e auditados; próximos blocos
(D — TodoDecay/agendamento, E — rollover de dia, F — QuestProgress wiring, G — UI) seguem não
iniciados.

**Desvios de escopo aprovados:** nenhum.

---

## [2026-08-19] Bloco: B3 — Morte global fora de sessão de Foco (achado #5 do Bloco B, fecha o módulo Habits)

**Contexto**: último achado pendente do Bloco B. Fonte real (`respawnHero()`,
`useFocusSession.ts`) reconfirmada literalmente antes do prompt: `isPlayerDead`/`respawnHero()`
são únicos e compartilhados por dois gatilhos (Wilderness/Foco e Habit Down), sem ramificação —
mesma lição de metodologia do B2, agora aplicada a infraestrutura, não só a fórmulas.

**Arquivos alterados:**
- app/src/main/java/com/iurispraecepta/herolog/model/CharacterModels.kt
- app/src/main/java/com/iurispraecepta/herolog/logic/CharacterMappers.kt
- app/src/main/java/com/iurispraecepta/herolog/logic/focus/FocusSessionState.kt
- app/src/main/java/com/iurispraecepta/herolog/logic/quests/HabitLogic.kt
- app/src/main/java/com/iurispraecepta/herolog/ui/HeroLogViewModel.kt
- app/src/main/java/com/iurispraecepta/herolog/MainActivity.kt

**Resumo do que foi implementado (diff real conferido):**
- `isPlayerDead: Boolean = false` movido de `FocusSessionState` para `CharacterState` (e
  espelhado em `CharacterSummary`); mapper (`CharacterMappers.kt`) atualizado. Nenhuma referência
  órfã ao campo antigo restou (build só passa se todas as leituras/escritas foram migradas).
- `HabitLogic.kt` (trigger Down): ao `hp` chegar a `<= 0`, seta `isPlayerDead = true` no
  `CharacterState` retornado. Não chama `respawnHero()` diretamente — respawn continua ação do
  jogador (botão), fiel ao fluxo real.
- `respawnHero()` (`HeroLogViewModel.kt`): agora restaura `hp = charState.maxHp` e
  `isPlayerDead = false` no `characterState` salvo (bug real também presente na fonte React
  atual, já identificado por Bruno, corrigido aqui independente do estado lá). Reset de
  `FocusSessionState()` deixou de ser incondicional — só ocorre se havia sessão de foco ativa/
  pausada/em grace/com config setada, permitindo respawn vindo de fora do módulo Foco (Habit)
  sem tocar em estado de sessão inexistente.
- `triggerCognitiveDeath()` e `onAppBackgrounded()`/`MainActivity.kt`: leituras/escritas de
  `isPlayerDead` migradas de `focusState` para `characterState` — consequência direta da
  migração do campo, sem mudança de comportamento nesses dois pontos.

**Validação:**
- Diff real conferido arquivo a arquivo — nenhuma alteração fora do escopo pedido.
- `HabitLogicTest`: 14/14, XML bruto conferido, 0 falha/erro. 2 testes novos:
  `triggerDown_fatalDamage_setsIsPlayerDeadToTrue`,
  `triggerDown_nonFatalDamage_preservesIsPlayerDeadFalse`.
- `HeroLogViewModelTest`: 33/33, XML bruto conferido, 0 falha/erro. 2 testes novos cobrindo os
  dois ramos do respawn genérico:
  `viewModel_respawnHero_whenDeadOutsideFocus_restoresHpAndClearsDead_withoutFocusSession`
  (Habit, sem sessão de foco) e
  `viewModel_respawnHero_appliesPenalties_restoresHp_clearsDead_andResetsFocusSessionState`
  (Wilderness/Foco, com reset de sessão).

**Bloco B3 fechado. Módulo Habits (Blocos A/B/B2/B3) fica 100% fiel à fonte real nos pontos
auditados** — os 5 achados do Bloco B (combatXP, streak Up, HP Up, streak Down, morte global)
todos corrigidos e testados.

**Desvios de escopo aprovados:** nenhum.

---

## [2026-08-19] Bloco: D — TodoDecayLogic + DailySchedulingLogic (funções puras de cálculo)

**Contexto**: dois utilitários puros, confirmados linha a linha contra a fonte real antes do
prompt (`src/utils/todoDecay.ts` — `getTodoDecayValue`; `src/utils/dailyScheduling.ts` —
`wasDailyScheduledForDate`). Grep no repo React confirmou que `getTodoDecayValue` só é chamado em
`TodosTab.tsx`, em tempo de renderização — não há aplicação/persistência do decay em
`useTodos.ts`/`App.tsx`/rollover. Decisão de paridade estrita confirmada com Bruno antes do
prompt: `wasDailyScheduledForDate` só tem cálculo real pra `repeats == 'Daily'`; `Weekly`/
`Monthly` são fallback `true` (sempre exigível) na fonte — portado exatamente assim, sem
implementar cálculo de dia da semana/mês. Correção de Weekly/Monthly registrada explicitamente
como melhoria pós-paridade, fora de escopo deste bloco.

**Arquivos criados:**
- app/src/main/java/com/iurispraecepta/herolog/logic/quests/TodoDecayLogic.kt
- app/src/main/java/com/iurispraecepta/herolog/logic/quests/DailySchedulingLogic.kt
- app/src/test/java/com/iurispraecepta/herolog/TodoDecayLogicTest.kt
- app/src/test/java/com/iurispraecepta/herolog/DailySchedulingLogicTest.kt

**Resumo do que foi implementado (diff real conferido):**
- `TodoDecayLogic.getTodoDecayValue(todo, currentDate)`: função pura, fiel à fórmula real —
  `completed == true` → 0; `createdAt` nulo → 0; `diffDays = max(0, round((dateZero-createdZero)
  /86400000))` (datas truncadas à meia-noite antes da subtração); `decayed = floor(diffDays/2)*-1`;
  retorno `max(decayed, -20)` (piso). Não é chamada de dentro de `TodoLogic.kt`/ViewModel — uso
  esperado é em tempo de exibição na Composable de listagem de Todos, igual à fonte.
- `DailySchedulingLogic.wasDailyScheduledForDate(daily, date)`: fiel à fórmula real —
  `repeats == Daily`: `createdAt` nulo → true; senão `diffDays = round((dateZero-createdZero)
  /86400000))`, `every = if (daily.every > 0) daily.every else 1`, retorno
  `diffDays >= 0 && diffDays % every == 0`. `repeats != Daily` (Weekly/Monthly) → `true`
  incondicional, fiel ao fallback real da fonte (limitação deliberada, não lacuna).

**Validação:**
- Diff real conferido arquivo a arquivo contra o pedido do prompt.
- `TodoDecayLogicTest`: 10/10, XML bruto conferido, 0 falha/erro — cobre completed, createdAt
  nulo, diffDays 0/1/2/3/4, clamp no piso -20, data futura anterior a createdAt (clamp em 0),
  formato ISO Instant de createdAt.
- `DailySchedulingLogicTest`: 7/7, XML bruto conferido, 0 falha/erro — cobre every=1 mesmo dia,
  every=2 par/ímpar, every=0 tratado como 1, createdAt nulo, Weekly/Monthly incondicional true,
  data anterior a createdAt.
- Build completo (`testDebugUnitTest`) confirmado `BUILD SUCCESSFUL`, 0 falhas em toda a suíte
  (incluindo `HabitLogicTest`/`HeroLogViewModelTest` de blocos anteriores, sem regressão).

**Ressalvas registradas, não bloqueiam fechamento:**
1. `parseDate()` (interno aos dois arquivos, não pedido no prompt) implementa cadeia de fallback
   de parsing (`Instant.parse` ISO-8601 → `yyyy-MM-dd` → timestamp `Long`). A fonte JS usa só
   `new Date(todo.createdAt)`, que é parseado de forma lenient pelo engine — o Kotlin
   `Instant.parse()` é estrito. O fallback é adaptação razoável do AI Studio, mas **não foi
   pedido explicitamente** e só tem cobertura de teste pro formato ISO Instant
   (`supportsIso8601InstantCreatedAtString`) — os ramos `yyyy-MM-dd` puro e timestamp `Long` não
   têm teste dedicado. Candidato a atenção futura se o formato real de `createdAt` persistido no
   Android divergir do testado.
2. `RepeatInterval` (usado em `DailySchedulingLogic`) — auditado à parte: confirmado enum
   pré-existente do Bloco A (`CharacterModels.kt`), `@Serializable`, valores
   `Daily/Weekly/Monthly` com `@SerialName` idêntico aos literais reais da fonte
   (`'Daily' | 'Weekly' | 'Monthly'`). Sem enum novo fora de escopo, sem variante da fonte não
   coberta.

**Bloco D fechado.**

**Desvios de escopo aprovados:** nenhum (ressalva #1 acima é decisão de implementação do AI
Studio dentro do próprio arquivo pedido, não expansão de escopo — registrada por transparência).

---

## [2026-08-20] Bloco: E — Rollover de Dia + Login Reward (mais arriscado do módulo Missões)

**Contexto**: fonte real (`App.tsx`, `useEffect` de mount) auditada linha a linha, sem
reticências — mutação simultânea de streak global, Dailies, HP/morte, inventário (StreakShield)
e reset de contadores diários. Bug real confirmado na fonte: dois checks INDEPENDENTES de
`StreakShield` no mesmo rollover (um pra streak, que remove item de um array local nunca
retornado — não consome de fato; outro pra dano, que consome de verdade) — permitindo, na fonte,
um único shield proteger streak E dano de graça.

**Decisões de desenho conscientes (Bruno), registradas como NÃO-fidelidade estrita neste ponto:**
1. Correção do bug: consumo único de `StreakShield` por rollover (nunca protege duas vezes de
   graça).
2. Extensão de escopo (não é bug da fonte, decisão de balanceamento): o shield passa a proteger
   TRÊS coisas no mesmo evento — streak global, dano de HP, E o contador individual de streak de
   cada Daily perdida (`Daily.streak`) — que na fonte sempre zerava incondicionalmente. Gatilho
   único: existe shield E (`diffDays > 1` OU `missedCount > 0`).
3. Delay de 100ms antes de `isPlayerDead = true` mantido (equivalente ao `setTimeout` da fonte,
   sincronização de UX intencional, não bug).
4. **Pendências registradas para ANTES de considerar o port "terminado"** (fora deste bloco):
   (a) tornar `StreakShield` mais raro/difícil de obter (compensar o item agora mais forte);
   (b) migrar armazenamento do shield de `inventory: List<Item>` genérico para um contador
   dedicado (`streakShieldCount: Int` ou similar, estilo Duolingo) — resolve a causa raiz do
   bug original (`findIndex`/`splice` disputando array genérico), não só o sintoma.

**Arquivos criados:**
- app/src/main/java/com/iurispraecepta/herolog/logic/quests/RolloverLogic.kt

**Arquivos alterados (wiring):**
- app/src/main/java/com/iurispraecepta/herolog/ui/HeroLogViewModel.kt

**Resumo do que foi implementado (diff real conferido):**
- `RolloverLogic.applyRollover(state, dailies, referenceDate)`: se `todayDate` já é hoje, no-op.
  Senão: calcula `missedCount` (Dailies não completas + `DailySchedulingLogic.
  wasDailyScheduledForDate`) ANTES de decidir o shield (gatilho depende dele). Gatilho do shield
  dispara proteção tripla (streak mantida, sem dano, `Daily.streak` preservado) com consumo único.
  Sem shield: streak zera se `diffDays > 1`, dano aplicado (Ranger com redução 30%, mínimo 1),
  `Daily.streak = 0` + `value -= 1` nas perdidas. Dailies completas → `completed = false`. Reset:
  `todayDate`, `todayMinutes = 0`, `todayXP = 0`, `hasClaimedLogin = false`, remove tags
  `claimed_daily_*`. `died = true` se `finalHp == 0`.
- Wiring no `init()` do ViewModel: chamado uma única vez no carregamento do estado (equivalente
  ao mount do App React). Login reward aplicado depois do rollover, lendo o estado já atualizado.
  Morte aplicada após delay de 100ms via coroutine.

**Validação:**
- `RolloverLogicTest`: 12/12, XML bruto conferido, 0 falha/erro — cobre same-day no-op,
  diffDays==1, diffDays>1 com/sem shield, gatilho por missedCount isolado (streak segura), os
  três protegidos simultaneamente com 1 shield só, sem shield (streak+dano+Daily.streak
  penalizados juntos), Ranger com redução, HP=0→died, filtro de achievements.
- `HeroLogViewModelTest`: 36/36 (3 novos: `mounts_appliesRolloverAndLoginReward_
  warriorGets120Gold`, `mounts_whenRolloverCausesDeath_setsIsPlayerDeadAfterDelay`,
  `mounts_whenAlreadyClaimedLoginToday_doesNotAddGoldAgain`).

**Correções de escopo aplicadas antes do fechamento (3 pontos, auditoria pegou desvio do AI
Studio na primeira entrega):**
1. `RolloverLogic.kt` tinha `getDailyDamage()` local duplicando a tabela de dano já existente em
   `getDifficultyRewards(difficulty).damage` (Bloco A) — corrigido para reusar a função
   existente, eliminando a duplicação/risco de divergência futura.
2. Três implementações quase idênticas de `parseDate()`/`truncateToMidnight()` (`TodoDecayLogic`,
   `DailySchedulingLogic`, `RolloverLogic` — esta última tinha crescido sozinha com formatos
   extras não pedidos) consolidadas num único `DateParsingUtils.kt` compartilhado.
3. Função pública `claimLoginReward()` no ViewModel, criada sem pedido (duplicava a lógica
   inline do `init()`), removida.

Reconfirmado depois da correção: suíte completa (`testDebugUnitTest`) 0 falhas, nenhuma
regressão nos blocos anteriores.

**Bloco E fechado.**

---

## [2026-08-20] Achado crítico pós-Bloco E: dois sistemas de tipos paralelos e desconectados
(Habit/Daily/Todo/Difficulty) — módulo Missões nunca esteve wireado à produção

**Como foi encontrado**: durante a auditoria do Bloco E, o AI Studio entregou um resumo em
prosa em vez do diff pedido, revelando (pelos nomes de teste na suíte completa) que
`HabitLogicTest.kt` havia sido silenciosamente reescrito por inteiro. Suspeita levou a auditoria
direta via `bash_tool`/`git clone` no repositório real (`HeroLog-Android-2`, novo repo criado por
Bruno como workaround do bug de integração GitHub↔AI Studio, com push funcionando via commit
normal).

**Achado**: o projeto tinha DOIS conjuntos de tipos `Habit`/`Daily`/`Todo`/`Difficulty`:
- `model.*` (`CharacterModels.kt`, Bloco A): `@Serializable`, usado de fato por `CharacterState`
  (`habits`, `dailies`, `todos` reais, persistidos).
- `logic.quests.*` (`QuestTypes.kt`): tipos PARALELOS, não serializáveis, nunca usados por
  `CharacterState`.

`HabitLogic.kt`, `DailyLogic.kt`, `TodoLogic.kt` (Blocos A/B/B2/B3/C — todos fechados e
"auditados" ao longo de várias sessões) não tinham import explícito de `Habit`/`Daily`/`Todo` —
resolução de tipo do Kotlin, por estarem no mesmo pacote de `QuestTypes.kt`, os ligou aos tipos
LOCAIS (`logic.quests.*`), nunca aos reais (`model.*`). Confirmado via grep no repositório
inteiro (`app/src/main/java/`): ZERO chamada a `HabitLogic.`/`DailyLogic.`/`TodoLogic.` em
qualquer lugar da produção — nenhum wiring, nenhum mapper/conversor entre os dois sistemas.
**Toda a lógica de Habits/Dailies/Todos fechada até aqui operava sobre um tipo morto, nunca
conectado ao estado real do personagem.**

Auditado e confirmado NÃO afetado: `RolloverLogic.kt`/`TodoDecayLogic.kt`/
`DailySchedulingLogic.kt` (Blocos D/E) já importavam `model.*` corretamente desde a criação —
só os três arquivos do módulo Habit/Daily/Todo core (A/B/C) tinham o problema. `QuestCatalog.kt`/
`QuestLogic.kt` (Guild Quests/LCG) confirmados sem dependência de nenhum tipo `logic.quests.*`
deletado.

**Correção aplicada:**
- `QuestTypes.kt`: deletados `enum Difficulty` (local, maiúsculo), `data class Habit`,
  `data class Daily`, `enum RepeatFrequency`, `data class Todo`, `data class ChecklistItem`
  (local), e a sobrecarga de `getDifficultyRewards(quests.Difficulty)`. Mantido só
  `DifficultyRewards` e `getDifficultyRewards(model.Difficulty)`.
- `HabitLogic.kt`, `DailyLogic.kt`, `TodoLogic.kt`: adicionados imports explícitos de
  `model.Habit`/`model.Daily`/`model.Todo`/`model.Difficulty`/`model.ChecklistItem` — ZERO
  mudança de lógica, correção puramente de resolução de tipo.
- `HabitLogicTest.kt`, `DailyLogicTest.kt`, `TodoLogicTest.kt`, `QuestTypesTest.kt`: reescritos
  para usar os tipos reais (`model.*`), mesmos cenários/asserts de antes (`DailyLogicTest` ganhou
  1 teste a mais, `combatXpAppliesFullAmount_notFortyPercent`). `QuestTypesTest` podado dos 4
  testes de tipos deletados + 4 de `getDifficultyRewards(quests.Difficulty)` morto, mantendo só
  os 4 válidos de `model.Difficulty`.

**Validação**: diff real conferido arquivo a arquivo. Suíte completa: **299 testes, 0 falhas, 0
erros** — nenhuma regressão em nenhum bloco anterior (Guild Quests/LCG, Foco, Skills,
Personagem, Inventário, etc, todos intactos).

**Pendência criada por este achado, não resolvida ainda**: `HabitLogic.trigger()`,
`DailyLogic.toggle()`, `TodoLogic.toggle()` continuam SEM NENHUMA chamada em produção mesmo após
a correção de tipo — o wiring de fato (ligar essas funções ao ViewModel/UI) é responsabilidade
do Bloco F (QuestProgress wiring), ainda não iniciado. A correção deste achado resolveu o TIPO
errado, não a ausência de wiring, que já era uma pendência conhecida e catalogada.

**Lição de metodologia**: mesmo com testes 100% verdes e diffs aparentemente corretos em cada
bloco individual (A/B/B2/B3/C), um problema estrutural (dois tipos com nomes idênticos, mesmo
pacote, resolução silenciosa) passou despercebido por múltiplas sessões de auditoria — porque a
auditoria olhava a lógica interna de cada arquivo, não a cadeia de wiring/tipo ponta a ponta. A
suspeita só surgiu porque o AI Studio entregou prosa em vez de diff no Bloco E, forçando
auditoria direta via `bash_tool` no repositório real em vez de confiar em qualquer resposta
textual — reforça a prática já adotada (nunca aceitar resumo em prosa) e sugere adicionar uma
checagem periódica de "grep de call sites reais em produção" para módulos fechados há muito
tempo sem revisão, não só verificação de lógica isolada.

**Este achado NÃO invalida a lógica de negócio auditada nos Blocos B/B2/B3/C** (as fórmulas de
XP/gold/streak/HP/dano continuam corretas e testadas) — invalidava apenas a CONEXÃO desses
arquivos ao app real, agora corrigida quanto ao tipo (wiring de chamada ainda pendente, Bloco F).

---

## [2026-08-20] Bloco: F — Wiring de trigger/toggle (Habit/Daily/Todo) ao ViewModel

**Contexto**: achado crítico pós-Bloco E (20/08, ver entrada acima) confirmou que
`HabitLogic.trigger()`, `DailyLogic.toggle()` e `TodoLogic.toggle()` existiam e tinham lógica
testada, mas nunca eram chamados de nenhum lugar da produção. Este bloco fecha essa lacuna de
wiring — só a ligação ViewModel → funções de lógica já existentes, sem alterar as funções em si.

**Arquivos alterados:**
- app/src/main/java/com/iurispraecepta/herolog/ui/HeroLogViewModel.kt
- app/src/test/java/com/iurispraecepta/herolog/HeroLogViewModelTest.kt

**Resumo (diff real conferido):**
- Imports novos: `DailyLogic`, `HabitLogic`, `TodoLogic` (`logic.quests.*`).
- 3 funções novas no `HeroLogViewModel.kt`, mesmo padrão nos três casos (buscar item pelo id na
  lista do `_characterState.value`, chamar a função de lógica correspondente, substituir o item
  atualizado na lista, `saveCharacterState` sobre o `updatedState` já retornado pela lógica — não
  sobre o `current` original, já que `trigger`/`toggle` também mexem em gold/xp/hp/streak do
  personagem, não só no item):
  - `triggerHabit(habitId, isUp)` → `HabitLogic.trigger(habit, current, isUp)`.
  - `toggleDaily(dailyId)` → `DailyLogic.toggle(daily, current)`.
  - `toggleTodo(todoId)` → `TodoLogic.toggle(todo, current)`.
- Nenhuma mudança nas funções de lógica em si (`HabitLogic`/`DailyLogic`/`TodoLogic`) — bloco é
  wiring puro.

**Validação:**
- Build: sucesso.
- Testes novos (resultado nominal colado por Bruno):
  - `triggerHabit_up_appliesRewardsAndUpdatesHabit`: PASSED
  - `triggerHabit_down_appliesDamageAndUpdatesHabit`: PASSED
  - `triggerHabit_unknownId_noOp`: PASSED
  - `toggleDaily_completesAndUncompletes_symmetric`: PASSED
  - `toggleTodo_completesAndSetsCompletedAt`: PASSED
- Suíte completa (`testDebugUnitTest`): **304 testes, 0 falhas, 0 erros, 0 skipped** — sem
  regressão em nenhum bloco anterior. (Esse é o baseline "304" referenciado no Bloco G logo
  abaixo, que soma mais 11 pra chegar a 315.)
- Visual: N/A (sem UI neste bloco).

**Desvios de escopo aprovados:**
- Nenhum.

**Escopo NÃO coberto por este bloco, registrado para não confundir com "wiring completo do
módulo Missões"**: QuestProgress da Guilda (`QuestLogic`/`QuestCatalog`) não foi tocado aqui —
este bloco cobriu só `HabitLogic.trigger()`/`DailyLogic.toggle()`/`TodoLogic.toggle()`. Se o
wiring de Guild Quests também estiver pendente, é item separado, não incluído neste fechamento.

**Bloco F fechado.**

---

## [2026-08-20] Bloco: G — CRUD de Habit/Daily/Todo + toggle de checklist (lógica pura, sem UI)

**Contexto**: trigger/toggle (Bloco F) já wireados. Faltava adicionar/editar/remover itens das 3
listas (Habits/Dailies/Todos) e marcar/desmarcar item de checklist (Daily e Todo). Confirmado na
fonte React que `create`/`edit`/`delete`/`toggleChecklistItem` são operações puras de lista — sem
side-effect de gold/xp/hp, sem chamar `HabitLogic`/`DailyLogic`/`TodoLogic`. Implementado inline
no `HeroLogViewModel.kt`, mesmo padrão de `discardItem`/`sellItem` (`val current =
_characterState.value ?: return`, montar lista atualizada, `saveCharacterState(current.copy(...))`).

**Arquivos alterados:**
- app/src/main/java/com/iurispraecepta/herolog/ui/HeroLogViewModel.kt
- app/src/test/java/com/iurispraecepta/herolog/HeroLogViewModelTest.kt

**Resumo (diff real conferido):**
- 11 funções novas em `HeroLogViewModel.kt`, só imports novos + essas funções tocados (nada mais
  no arquivo alterado):
  - `addHabit`/`editHabit`/`deleteHabit` — `id` novo via `UUID.randomUUID().toString()`,
    contadores (`upCount`/`downCount`/`streak`) zerados na criação. `editHabit` recebe o objeto já
    editado pronto (decisão de quais campos a UI expõe pra edição fica pro Bloco H).
  - `addDaily`/`editDaily`/`deleteDaily`/`toggleDailyChecklistItem` — `addDaily` aceita `streak`
    inicial (campo "Série Inicial" existe no form React, não é sempre 0), monta `checklist` a
    partir de `List<String>` de textos com `ChecklistItem(id = UUID novo, completed = false)`.
    `toggleDailyChecklistItem` inverte `completed` do item pelo `itemId` dentro da `Daily`
    identificada por `dailyId`.
  - `addTodo`/`editTodo`/`deleteTodo`/`toggleTodoChecklistItem` — mesmo padrão de Daily, sem
    parâmetro de `streak` (`Todo` não tem esse campo na fonte).
- Nenhuma das 11 funções chama `HabitLogic`/`DailyLogic`/`TodoLogic` nem mexe em gold/xp/hp —
  escopo mantido estritamente como CRUD/toggle de lista.

**Validação:**
- Build: sucesso.
- Testes: 11 testes novos, nomes confirmados por grep como inéditos no commit anterior (não são
  duplicata/fake) — `addHabit_appendsWithZeroedCounters`, `editHabit_replacesById`,
  `deleteHabit_removesById`, `addDaily_appendsWithGivenStreakAndChecklist`,
  `editDaily_replacesById`, `deleteDaily_removesById`,
  `toggleDailyChecklistItem_flipsCompletedById`, `addTodo_appendsWithEmptyChecklistDefault`,
  `editTodo_replacesById`, `deleteTodo_removesById`,
  `toggleTodoChecklistItem_flipsCompletedById` — todas confirmadas presentes após commit (grep
  no repositório real via `bash_tool`). Suíte completa: **315/315** (304 + 11), sem regressão em
  nenhum bloco anterior. Commit confirmado por Bruno: 2 arquivos alterados, 509 linhas — bate com
  o diff conferido antes do commit.
- Visual: N/A (sem UI neste bloco).

**Desvios de escopo aprovados:**
- Nenhum. Escopo fechado exatamente como pedido (CRUD puro, sem lógica de recompensa, sem UI).

**Bloco G fechado.** Próximo: Bloco H (UI Compose — 3 telas: Habits, Dailies, Todos), fatiado em
H1/H2/H3. Antes de começar, verificar como o tema/cores/tipografia já existentes no projeto
Compose resolvem, para não inventar estilo visual novo fora do padrão do app.

---

## [2026-08-20] Bloco: H1 — Tela de Habits (Compose)

**Contexto**: `HabitLogic.trigger()`/`addHabit`/`editHabit`/`deleteHabit` já wireados (Blocos F/G).
Faltava só a UI. Tema auditado direto no repositório real via `bash_tool` antes do prompt (não
reconstruído de memória): paleta Stone/Amber (`ui/theme/Color.kt`), `HeroLogModal.kt` já existente
reaproveitado, ícones do projeto restritos a `Icons.Default/Filled/Outlined.*` (Material, sem
lucide-compose). Fonte React auditada linha a linha: `HabitsTab.tsx` (layout/form) +
`useHabits.ts` (side-effects, já cobertos pelo ViewModel).

**Arquivos criados:**
- app/src/main/java/com/iurispraecepta/herolog/ui/theme/ScoreColor.kt
- app/src/main/java/com/iurispraecepta/herolog/ui/habits/HabitsScreen.kt

**Resumo (diff real conferido):**
- `ScoreColor.kt`: porte fiel de `src/utils/scoreColor.ts` (7 faixas de gradiente por
  `upCount - downCount`), alphas Tailwind convertidos pra hex ARGB, hex de paleta conferidos contra
  a paleta Tailwind padrão.
- `HabitsScreen.kt`: `HabitsScreen` (header + lista/empty state + modal HeroLogModal variant Amber)
  e `HabitCardItem` (card com zonas laterais de trigger up/down, 48dp clicável quando
  `up`/`down` true, faixa fina 8dp Stone900 quando false; fundo em gradiente por `ScoreColor`).
  Form replica `HabitsTab.tsx`: toggles Positivo/Negativo independentes (não segmented), dropdown
  de dificuldade, campo de tags (parse `split(",")` + `trim().lowercase()` + filtro de vazio),
  confirmação inline dupla e independente (excluir / cancelar), reset de form ao fechar/salvar.
  "Feito hoje" comparado via `SimpleDateFormat("yyyy-MM-dd")` — mesmo formato que
  `HabitLogic.kt` grava em `lastTriggeredDate` (divergência de formato vs. `toDateString()` inglês
  da fonte React já existente desde o Bloco B, intencional, aqui só mantida consistente).

**Correção de escopo aplicada antes do fechamento (3 pontos, auditoria pegou desvio da primeira
entrega):**
1. Tags no card da lista (`FlowRow` com `#tag`) — não existe em `HabitsTab.tsx` (tag só aparece no
   form), removido.
2. Streak (🔥) na linha de status do card — não existe na fonte, removido.
3. `allowBackdropClose = false` no `HeroLogModal` — divergência real de comportamento (não
   cosmética): matava clique-fora do modal inteiro, sem disparar nada. Fonte: clique fora chama
   `handleCancelAttempt` (mesma função que já cobre o `X`/botão Cancelar). Corrigido pra `true` —
   `onClose` já tinha a lógica certa (`if (isConfirmingCancel) resetForm() else
   isConfirmingCancel = true`), só o parâmetro estava errado.
   Dead code removido junto: `Stone700`, `Red600` (declarados, não usados).

**Validação:**
- Build: sucesso (`compile_applet`, confirmado nas duas rodadas — entrega inicial e correção).
- Testes: suíte completa (`testDebugUnitTest`) **323 testes, 0 falhas, 0 erros, 0 skipped**
  (315 + 8 telas Roborazzi de Habits), sem regressão. **Pendência de evidência**: só o `TOTAL`
  agregado foi confirmado — o nome nominal de cada um dos 8 métodos de teste Roborazzi (por
  PASSED/FAILED individual, padrão exigido pelos demais blocos) não foi colado ainda.
- Visual: **NÃO validado.** Ambiente do Google AI Studio não conseguiu entregar os PNGs gerados
  pelo Roborazzi (`habits_screen_empty`, `_up_down`, `_only_up`, `_only_down`, `_create_modal`,
  `_edit_modal`, `_confirm_delete`, `_confirm_cancel`) — só os nomes dos arquivos foram
  descritos, sem o print em si. **Decisão de Bruno**: seguir sem bloquear o Bloco H, validação
  visual real (print comparado com o React, os dois viewports de referência) fica para uma sessão
  posterior, antes de marcar "Fidelidade Visual: Validada" na `PARIDADE.md`.

**Desvios de escopo aprovados:**
- Nenhum — os 3 desvios encontrados (tags/streak/backdrop) foram corrigidos, não aprovados como
  extensão.

**Bloco H1 fechado, com duas pendências explícitas (evidência nominal de teste + validação
visual) — não bloqueiam o avanço pro H2, mas ficam registradas até serem resolvidas.** Próximo:
Bloco H2 (tela de Dailies), reaproveitando `ScoreColor.kt` e o mesmo padrão de `HeroLogModal`.

---

## [2026-08-20] Bloco: H2 — Tela de Dailies (Compose) + extração de DifficultyBadge

**Contexto**: `DailyLogic.toggle()`/`toggleChecklistItem()` e `addDaily`/`editDaily`/`deleteDaily`/
`toggleDailyChecklistItem` já wireados (Blocos F/G). `ScoreColor.kt` (Bloco H1) reaproveitado,
`score = d.value ?: 0` (não `upCount - downCount`, essa fórmula é só de Habit). Fonte auditada
linha a linha: `DailiesTab.tsx` + `useDailies.ts` (side-effects já cobertos pelo
ViewModel/`DailyLogic`).

**Passo 0 (refactor, antes do H2 em si)**: `DifficultyBadge`/`difficultyLabel`/`difficultyColor`
extraídos de `HabitsScreen.kt` (onde eram `private`) para
`app/src/main/java/com/iurispraecepta/herolog/ui/components/DifficultyBadge.kt` (público) —
badge é idêntico em Habit e Daily, evita duplicação. `HabitsScreen.kt` atualizado pra importar de
lá. Nenhuma cor/label mudou nesse passo, só o local.

**Arquivo criado:**
- app/src/main/java/com/iurispraecepta/herolog/ui/dailies/DailiesScreen.kt

**Resumo (diff real conferido):**
- `DailiesScreen` (header + lista/empty state + modal) + `DailyCardItem` (card com banda esquerda
  única de toggle completo/incompleto — não duas bandas como Habit) + `repeatIntervalLabel`/
  `getFrequencyText` (porta fiel do `getFrequencyText` da fonte: `every==1` vira frase direta
  "Repete diariamente/semanalmente/mensalmente", senão "Repete: Diário/Semanal/Mensal (a cada Nx)").
- Form: título, notas (56dp, menor que os 64dp de Habit — bate com `h-14` vs `h-16` da fonte),
  dificuldade + regularidade (dropdown Diário/Semanal/Mensal + campo "every" numérico clamped
  1-99), **checklist criador só em modo criação** (`editingDaily == null` — em edição a checklist
  existente não é tocada, fiel ao comentário original da fonte), streak inicial e tags **presentes
  em ambos os modos** (criar e editar — streak é "manual adjust" na edição, conforme
  `onEditDaily` da fonte), confirmação inline dupla de excluir/cancelar, reset completo de form
  (incluindo `checklistItems`/`checklistInput`).
- Card: banda esquerda (Check + fundo sky se completo; quadrado âmbar pequeno se não), corpo
  central com título riscado+sky se completo, `DifficultyBadge` (agora do componente
  compartilhado), notas, linha de status (streak 🏅 + frequência + progresso de checklist "📋 X
  de Y critérios" em purple se houver checklist), banda direita de expandir/colapsar checklist
  (só se `checklist.isNotEmpty()`). Painel de checklist expandido visível se
  `isExpanded || daily.completed` (OR, não AND — daily completa sempre mostra a checklist aberta
  mesmo sem o usuário clicar no chevron). Card com alpha 0.7 quando completo.
- Fundo em gradiente por `ScoreColor.forScore(daily.value ?: 0)`, `value` decrementa **sem clamp
  em 0** no desmarcar (mesmo comportamento assimétrico de `DailyLogic.kt`, já testado no Bloco F —
  não é bug, é fiel à fonte).

**Correção de escopo aplicada antes do fechamento (checklist expandido, 4 pontos — auditoria
pegou desvio da primeira entrega):**
1. Cor do texto do item completo estava invertida — Kotlin destacava em roxo vivo
   (`Color(0xFFC084FC)`), fonte apaga em cinza (`text-stone-500`). Corrigido pra `Stone500`.
2. Ícone `Icons.Default.Check` dentro do checkbox não existe na fonte — substituído por um
   quadradinho sólido interno 6dp (`Purple400`), replicando o `w-1.5 h-1.5` da fonte.
3. Tamanho/cor do checkbox: `18dp` → `14dp` (`w-3.5 h-3.5`); fundo completo de `Purple500` sólido
   pra `Color(0x33A855F7)` (purple-500/20) translúcido; fundo/borda não-completo pra
   `Stone950`/`Stone700` (constante `Stone700` reintroduzida como `private val` no arquivo, tinha
   sido removida como dead code no H1 por outro contexto e fazia falta aqui).
4. Borda da banda do chevron: trocado `.border()` nas 4 bordas por `.drawBehind { drawLine(...) }`
   desenhando só a linha esquerda, cor `Color(0x1AF59E0B)` — mesmo padrão já usado no
   `HeroLogModal.kt` pro separador do header.

**Validação:**
- Build: sucesso (`compile_applet`, confirmado nas duas rodadas — entrega inicial e correção).
- Testes: 8 testes novos, todos nominais e PASSED (padrão de evidência completo, ao contrário do
  H1): `dailiesScreen_emptyList_screenshot`, `dailiesScreen_incompleteItemWithoutChecklist_screenshot`,
  `dailiesScreen_completedItemWithoutChecklist_screenshot`,
  `dailiesScreen_incompleteItemWithChecklistCollapsed_screenshot`,
  `dailiesScreen_incompleteItemWithChecklistExpandedManually_screenshot`,
  `dailiesScreen_completedItemWithChecklist_screenshot` (confirma o OR: expande sozinha),
  `dailiesScreen_createModalWithChecklistBeingBuilt_screenshot`,
  `dailiesScreen_editModalWithoutChecklistBuilder_screenshot`. Suíte completa: **331/331**
  (323 + 8), sem regressão. Correção dos 4 pontos revalidada via `recordRoborazziDebug`, também
  sem regressão.
- Visual: mesma pendência do H1 — Google AI Studio não entrega PNG. Diferença aqui: pelo menos o
  código-fonte final foi conferido linha a linha direto no repositório real (`git pull` +
  `bash_tool`) após o commit de Bruno, incluindo os 4 pontos de correção — reduz o risco vs.
  aceitar só o diff colado, mas não substitui o print real comparado com o React.

**Desvios de escopo aprovados:**
- Nenhum — os 4 desvios do checklist expandido foram corrigidos, não aprovados como extensão.

**Bloco H2 fechado.** Pendência única: validação visual real (herda do H1, mesmo motivo).
Próximo: Bloco H3 (tela de Todos) — mesmo padrão de `HeroLogModal`/`DifficultyBadge`/`ScoreColor`,
sem streak (Todo não tem esse campo) e sem toggle up/down (só completo/incompleto, como Daily,
mas sem repetição/frequência).

## [2026-08-22] Bloco H3 — Tela de Todos (Compose) + auditoria contra fonte real

- `TodosScreen.kt` entregue pelo AI Studio: mesmo padrão de `HeroLogModal`/`DifficultyBadge`/
  `ScoreColor` dos blocos anteriores, sem streak (`Todo` não tem esse campo) e sem toggle up/down
  (só completo/incompleto). 12 testes novos, suíte completa 343/343 (331 do H2 + 12), sem
  regressão. Build OK.
- Commitado no GitHub por Bruno em `vosslerazelas22/HeroLog-Android-2`, commit `fe6f29d`
  ("feat(todos): implement TodosScreen UI and tests").
- Auditoria feita contra a fonte real `TodosTab.tsx`: lógica, wiring e testes corretos. 3 achados,
  nenhum bloqueante:
  1. Guard de filtro desabilitado usa `isModalOpen` (`isCreating || editingTodo != null`); fonte
     usa só `editingTodo !== null` — filtro fica habilitado durante criação de item novo no React.
     Motivo provável na fonte: evitar que o item em edição suma da lista ao trocar filtro (risco
     que não existe na criação, por não haver item "atual" na lista). Invisível na prática porque
     o backdrop do modal cobre os filtros nos dois modos, mas diverge da fonte literal.
  2. Texto do item de checklist expandido é clicável no Android (chama toggle), fonte só tem
     clique no checkbox — área de toque maior.
  3. Cor de fundo do card concluído usava `emerald-700` em baixa opacidade (`0x0D047857`); fonte
     usa `emerald-950/[0.03]`, tom bem mais escuro e ainda mais sutil. Border já batia exato
     (`emerald-500/30%`).

**Decisão de Bruno (22/08):**
- Achado 1 (guard de filtro): aceito como melhoria consciente, mantido como está — não corrigido.
- Achado 2 (checklist clicável): já registrado como melhoria consciente desde a auditoria.
- Achado 3 (cor do card): corrigido para fidelidade 1:1. `TodosScreen.kt` linha do `.background()`
  do card completo trocada de `Color(0x0D047857)` para `Color(0x08022C22)` — `emerald-950`
  (`#022C22`) a 3% de opacidade (`0.03 × 255 ≈ 8` = `0x08`), replicando `emerald-950/[0.03]` da
  fonte. Border não tocado (já fiel). Fix aplicado direto no clone real do repositório
  (`vosslerazelas22/HeroLog-Android-2`), pendente de Bruno colar/commitar de volta.

**Bloco H3 fechado.** Módulo H (Habits/Dailies/Todos) sem mais achados pendentes de decisão.
Pendências remanescentes do módulo: validação visual real de H1/H2/H3 (nenhum print comparado ao
React ainda — Roborazzi nunca entregou PNG nas três telas). Próximo módulo real: Missões
(Quests/History/Guild Quests), ainda não portado.

## [2026-08-22] Bloqueio identificado: telas H1/H2/H3 nunca foram plugadas na MainActivity

- Bruno reportou não estar vendo as abas de Habits/Dailies/Todos no app. Auditoria direta do
  `MainActivity.kt` real confirmou: `TabRow` tinha só 4 abas fixas (Habilidades, Personagem,
  Inventário, Foco (Preview)), nenhuma referência a `HabitsScreen`/`DailiesScreen`/`TodosScreen`
  em lugar nenhum do arquivo. As três telas existiam prontas no código desde H1/H2/H3, mas nunca
  foram conectadas à navegação real — pendência que já estava registrada em `PARIDADE.md`
  ("Persistência local", seção 1) mas não tinha sido isolada como bloqueio explícito até agora.
- `HeroLogViewModel.kt` já expõe tudo necessário pro wiring sem lógica nova: `triggerHabit`,
  `toggleDaily`/`toggleTodo`, `addHabit`/`editHabit`/`deleteHabit`, `addDaily`/`editDaily`/
  `deleteDaily`/`toggleDailyChecklistItem`, `addTodo`/`editTodo`/`deleteTodo`/
  `toggleTodoChecklistItem` — todos já testados nos Blocos F/G.
- Causa raiz do bloqueio: a `MainActivity` atual usa uma navegação flat de 4 abas (`TabRow`
  `Int`-based) que não tem estrutura pra acomodar os 5 módulos + sub-abas da fonte real. Decisão
  de Bruno: resolver a navegação (bottom nav com sub-abas) antes de plugar Missões.

## [2026-08-22] Auditoria da fonte real: `BottomNav.tsx`

- Clone fresco de `vosslerazelas22/HeroLog` confirmou a estrutura real de navegação (fonte de
  verdade, nunca auditada em detalhe antes — só mencionada de leve na seção 2 do `PARIDADE.md`
  como "sidebar desktop não se aplica, sem menu lateral por gesto no mobile").
- **5 módulos top-level** (`navItems`, grid fixo de 5 colunas na base): Foco (`Timer`, sem
  sub-abas, `targetTab: 'focus'`), Herói (`ShieldUser`, sub-abas Status/Inventário), Skills
  (`BookOpen`, sem sub-abas), Missões (`Compass`, sub-abas Capela de Hábitos/Tarefas Diárias/
  Missões Avulsas/CONTRATOS/Crônicas Diárias — `targetTab: 'habits'` quando clicado direto),
  Reino (`Castle`, sub-abas Bazar/TÍTULOS/Heatmap/ESTATÍSTICAS/CONQUISTAS/REGISTROS/Tutorial —
  `targetTab: 'shop'`).
- **Mecânica**: módulo sem sub-abas troca `activeTab` direto no clique. Módulo com sub-abas abre
  um bottom sheet (`motion.div` deslizante de baixo pra cima, dismiss por clique fora via
  listener de `mousedown`/`touchstart`) listando as sub-abas; escolher uma fecha o sheet e troca
  `activeTab`. Item ativo do nav calculado por `getActiveModule(activeTab)` — ex.: `activeTab ===
  'dailies'` destaca "Missões" mesmo com o sheet fechado.
- `activeTab` no `App.tsx` real é `useState<string>('focus')` — confirma que o estado de
  navegação da fonte é `String`, não índice numérico (diferença estrutural do `selectedTab: Int`
  que o Android usava até este bloco).

## [2026-08-22] Bloco: Navegação — `HeroLogBottomNav` + wiring de Habits/Dailies/Todos na MainActivity

- Novo arquivo `ui/navigation/HeroLogBottomNav.kt`: porte fiel de `BottomNav.tsx`. `NAV_ITEMS`
  (5 módulos), `SUB_TABS` (mapa módulo→sub-abas, só `character`/`missions`/`kingdom` têm), 
  `MODULE_TITLES`, `getActiveModule()` — todos espelhando as constantes/função homônimas da
  fonte. Sheet de sub-abas via `ModalBottomSheet` (Material 3) — escolha nativa consciente:
  cobre dismiss-por-fora e gesto de arrastar pra fechar nativamente, equivalente funcional ao
  `motion.div` + listener manual da fonte, sem precisar reimplementar animação/gesture handling
  na mão.
- **Mapeamento de ícones lucide-react → Material Icons por aproximação semântica** (sem
  equivalente 1:1 disponível na biblioteca): `Timer`→`Timer`, `ShieldUser`→`Shield`,
  `BookOpen`→`MenuBook`, `Compass`→`Explore`, `Castle`→`Castle`, `UserCircle`→`AccountCircle`,
  `Backpack`→`Backpack` (exato), `Repeat`→`Repeat` (exato), `Calendar`→`DateRange`,
  `ClipboardList`→`Checklist`, `ScrollText`→`Description`, `History`→`History` (exato),
  `Coins`→`MonetizationOn`, `Medal`→`MilitaryTech`, `Grid3x3`→`GridView`,
  `ChartColumn`→`BarChart`, `Trophy`→`EmojiEvents`, `FileText`→`Description`,
  `HelpCircle`→`HelpOutline`, `Check`→`Check` (exato). **`Castle` e `Checklist` não confirmados
  por build real** (sem Gradle/Android SDK neste ambiente) — fallback documentado em comentário
  no código (`AccountBalance`/`List`) caso o símbolo não exista na versão da biblioteca.
- Cores dos ícones de sub-aba portadas das classes Tailwind da fonte (`text-sky-400`,
  `text-emerald-400` etc.) como constantes `private val` locais ao arquivo, mesmo padrão já usado
  em `TodosScreen.kt` pra cores sem equivalente nomeado no tema.
- Novo arquivo `ui/components/PlaceholderScreen.kt`: composable simples (título + "Ainda não
  portado do React") pra dar destino visual às sub-abas que existem na navegação mas não têm
  módulo real ainda — decisão explícita de Bruno de não bloquear o wiring de Habits/Dailies/Todos
  esperando Reino/Contratos/Crônicas serem portados.
- `MainActivity.kt` reescrita: `var selectedTab by remember { mutableStateOf(0) }` (Int) trocado
  por `var activeTab by remember { mutableStateOf("focus") }` (String), espelhando o
  `useState<string>('focus')` real. `TopAppBar`+`TabRow` antigos (4 abas fixas) removidos;
  `Scaffold.bottomBar` agora chama `HeroLogBottomNav`. Título do `TopAppBar` calculado via
  `MODULE_TITLES[getActiveModule(activeTab)]` com fallback pra Foco/Skills (que não têm entrada
  em `MODULE_TITLES`, só os 3 módulos com sub-abas têm). FAB de adicionar habilidade migrado de
  `selectedTab == 0` pra `activeTab == "skills"`.
- Corpo do `when` migrado de `Int` pra `String`, cobrindo os 5 módulos reais mais as sub-abas:
  `"skills"`, `"character"`, `"inventory"`, `"habits"`, `"dailies"`, `"todos"`, `"focus"` chamam
  as telas reais (`HabitsScreen`/`DailiesScreen`/`TodosScreen` plugadas pela primeira vez, com
  `state.habits`/`state.dailies`/`state.todos` e os callbacks do ViewModel já testados nos
  Blocos F/G). `"quests"`, `"history"`, `"shop"`, `"titles"`, `"heatmap"`, `"stats"`,
  `"achievements"`, `"logs"`, `"guide"` chamam `PlaceholderScreen` com o título correspondente.
- **Validação**: balanceamento de chaves/parênteses do arquivo inteiro conferido via script
  (157/157 chaves, 251/251 parênteses) — não substitui build real. **Sem acesso a Gradle/Android
  SDK neste ambiente** (rede restrita a domínios específicos, sem `dl.google.com`/Maven do
  Android) — nenhum dos dois arquivos novos nem a `MainActivity` reescrita foram compilados de
  fato. Pendente: Bruno colar os 3 arquivos (`HeroLogBottomNav.kt`, `PlaceholderScreen.kt`,
  `MainActivity.kt`) no projeto real e rodar o build no AI Studio antes de aceitar o bloco como
  fechado.

**Bloco de navegação aberto, não fechado.** Pendências: (1) build real confirmando que compila,
com atenção especial aos 2 ícones não verificados (`Castle`, `Checklist`); (2) validação visual
do bottom nav e dos sheets de sub-aba; (3) confirmar que Habits/Dailies/Todos aparecem e
funcionam de fato no app rodando, fechando o bloqueio original reportado por Bruno.

## [2026-08-22] Build real confirmado — `assembleDebug` BUILD SUCCESSFUL

- Bruno moveu os 2 arquivos pros diretórios corretos (`ui/navigation/HeroLogBottomNav.kt`,
  `ui/components/PlaceholderScreen.kt`) e rodou `assembleDebug`. **BUILD SUCCESSFUL em 1m 1s**,
  0 erros, 39 tasks (7 executadas, 32 up-to-date).
- **Os 2 ícones não verificados compilaram sem problema**: `Icons.Filled.Castle` e
  `Icons.Filled.Checklist` existem de fato na versão do `material-icons-extended` do projeto —
  incerteza da seção "Mapeamento de ícones" resolvida, sem necessidade de fallback.
- 2 warnings de depreciação nos arquivos novos, sem impacto no build: `Icons.Filled.HelpOutline`
  e `Icons.Filled.MenuBook` têm versão `AutoMirrored` recomendada (suporte a RTL). Corrigidos
  direto no `HeroLogBottomNav.kt` (import + uso trocados pra
  `Icons.AutoMirrored.Filled.HelpOutline`/`MenuBook`) — puramente cosmético, sem mudança de
  comportamento, pendente de Bruno colar de volta.
- Warnings restantes do log (`fallbackToDestructiveMigration` deprecated em
  `HeroLogApplication.kt`, 3 "Condition is always 'true'" em `TimerSettingsModal.kt`) são
  pré-existentes, de blocos anteriores — fora do escopo deste bloco, não tocados.

**Compilação confirmada. Falta só validação visual/funcional real** (instalar o APK e confirmar
que as 5 abas aparecem, sub-abas abrem em sheet, e Habits/Dailies/Todos renderizam de fato) pra
fechar o bloco de navegação por completo.

## [2026-08-22] Validação manual no dispositivo (preview Google AI Studio)

Bruno testou os 4 pontos levantados após o build verde:

1. **Sheet de Missões abre corretamente** — screenshot confirma as 5 sub-abas (Capela de
   Hábitos/Tarefas Diárias/Missões Avulsas/CONTRATOS/Crônicas Diárias), ícones e cores fiéis à
   fonte, `TopAppBar` mostrando "HeroLog — Skills" (título dinâmico via `MODULE_TITLES`
   funcionando).
2. **Sub-abas navegam pras telas reais e CRUD funciona** — `HabitsScreen`/`DailiesScreen`/
   `TodosScreen` renderizam sem crash, ciclo completo (criar/editar/marcar concluído/deletar)
   confirmado em cada uma. **Isso fecha o bloqueio original** ("não vejo Missões no app") —
   Habits/Dailies/Todos estão de fato acessíveis e funcionais pela primeira vez.
3. **Persistência: reset ao dar reload no preview do AI Studio.** Esperado — o preview do AI
   Studio roda em sandbox efêmero, não é o mesmo runtime de um APK instalado com Room persistindo
   em disco de verdade. Não é um bug de regressão do wiring de navegação. **Validação real de
   persistência (fechar/reabrir o app instalado) ainda pendente** — teste equivalente já foi feito
   pra Personagem/Inventário no Bloco 28 fora do preview; falta repetir pra Habits/Dailies/Todos
   uma vez que Bruno instale o APK.
4. **Herói e Foco não quebraram** com a reescrita da `MainActivity` — confirmado.

**Bloco de navegação fechado — funcionalmente.** Bloqueio original resolvido: Habits/Dailies/
Todos acessíveis e operacionais no app. Única pendência remanescente é o teste de persistência
real fora do preview (ponto 3), a ser feito quando Bruno instalar o APK — não bloqueia uso diário
via preview, mas fica registrado como validação em aberto até confirmação fora do sandbox.
Validação visual formal (print lado a lado com React) de H1/H2/H3 segue como pendência separada,
já registrada em `PARIDADE.md`.

## [2026-08-24] Bloco H — QuestApplyLogic.claimQuestReward (lógica pura de resgate)

- `QuestApplyLogic.kt` criado em `logic/quests/`, porte fiel de `handleClaimQuestRewards`
  (`useGuildQuests.ts`): idempotência via `QuestLogic.getQuestClaimId` (no-op se já resgatado),
  progressão de combate (`floor(xpReward*0.4)` + loop de level-up reaproveitando
  `CombatLogic.requiredXpForCombatLevel`), `gold`/`totalGoldEarned`/`totalXP` incrementados,
  `achievements` registrando o `claimId`. Confirmado sem o bug do Foco — aqui `totalXP` soma
  normal mesmo na fonte, não há nada a corrigir.
- Deliberadamente **não portado**: `addSystemLog(...)`/`sound.playLevelUp()` — consistente com a
  convenção já registrada no projeto (SFX/log de sistema são ponto de extensão futuro fora de
  escopo, ver `HeroLogViewModel.kt`/`onBreakTimerCompleted`).
- Testes novos (`QuestApplyLogicTest.kt`): claim diário com data no `claimId`, claim de guilda sem
  data, idempotência (claim duplicado é no-op — `assertSame`), level-up de combate ao cruzar o
  limiar, incremento correto de `totalXP`. Suíte total: 348 testes.
- **Auditado literal por Claude** (não só resumo em prosa) via `git pull` no
  `HeroLog-Android-2` (commit `13d7132`) — arquivo e testes conferem exatamente com o pedido.
  **Aprovado.**

## [2026-08-24] Bloco I — Wiring + UI de Contratos (`QuestsScreen.kt`) — duas rodadas

**Primeira entrega (commit `13d7132`):** wiring no `HeroLogViewModel.kt`
(`ProcessedQuest`, `dailyQuests()`, `guildQuestsProcessed()`, `claimQuestReward()`) e no
`MainActivity.kt` (`questsSubTab` no nível certo, sobrevivendo a troca de aba dentro da sessão,
igual ao `App.tsx` da fonte) **auditados e aprovados sem ressalvas**.

`QuestsScreen.kt` da primeira entrega, porém, **reprovado** — divergência real de fidelidade
visual, não cosmética: título/ícone inventados ("Quadro de Contratos" + troféu em vez de
"CONTRATOS" + `Layers`), rótulos de sub-aba errados, legenda parafraseada em vez de literal,
paleta usando esmeralda onde a fonte só usa âmbar, emojis trocados (🪙⭐ em vez de 💎⚡), textos de
botão/rodapé errados ("Resgatar" em vez de "Reivindicar", "Resgatado" em vez de "Baú de espólios
recolhido"), sem bolinha pulsante, sem badge "✔️ Concluído" no cabeçalho.

**Segunda rodada (commit `453aa11`, mesmo dia):** prompt de correção citando o texto exato da
fonte (colado literal, não descrito) resolveu todos os pontos acima. Conferido literal contra
`QuestsTab.tsx` + wrapper `App.tsx` (`activeTab === 'quests'`): título, ícone, rótulos de sub-aba,
legendas, paleta (esmeralda restrita a XP e badge "Concluído", igual à fonte), textos de rodapé,
opacidade 0.6 do card inteiro quando resgatado, bolinha pulsante via `rememberInfiniteTransition`.
Diff confirmado tocando só `QuestsScreen.kt` — wiring do Bloco I original preservado intacto.
**Aprovado.**

**Achado de processo a registrar**: pedir fidelidade "linha por linha" no prompt não é garantia —
mesmo com a instrução explícita, a primeira rodada saiu com texto/cor inventados. Auditoria
literal pós-entrega continua obrigatória mesmo quando o prompt já pede rigor. Reforça também o
padrão já conhecido de nunca fechar bloco só com o resumo em prosa do AI Studio: nas duas rodadas
deste bloco, o commit real só apareceu no GitHub depois de Bruno confirmar explicitamente "dei
commit" — a resposta do AI Studio relatando build verde não implica push feito (loop de OAuth
conhecido).

**Pendente**: Crônicas Diárias (`HistoryScreen.kt`, Bloco J) segue como próxima etapa — mesma
divergência de fidelidade identificada na primeira auditoria (estrutura de dois layouts de card
diferentes na fonte, textos de estado vazio, emojis, rótulos de toggle), ainda não corrigida.

## [2026-08-24] Bloco J — Crônicas Diárias (`HistoryScreen.kt`) — duas rodadas

Sem lógica nova neste bloco — `HistoryEntry` já era criado por `FocusApplyLogic.kt` desde o
módulo Foco. Só UI.

**Primeira entrega**: reprovada por divergência estrutural real, não só de texto. A fonte
(`HistoryTab.tsx`) tem DOIS layouts de card completamente diferentes dependendo do modo de
visualização (compacto pra notas, completo pra visão geral); a primeira entrega reaproveitou o
mesmo card pros dois modos, só filtrando a lista. Também tinha textos parafraseados (toggle,
estados vazios), emojis errados (⭐🪙 em vez de ⚡💎) e badge de wilderness em inglês
("Wilderness" em vez de "Terra Selvagem").

**Segunda rodada (commit `acfa516`, mesmo dia)**: prompt de correção com texto literal da fonte
colado, exigindo explicitamente os dois componentes de card separados (`NotesHistoryCard` e
`FullHistoryCard`). Resultado auditado linha por linha contra `HistoryTab.tsx` + wrapper
`App.tsx` (`activeTab === 'history'`):

- Estrutura de dois cards reais confirmada — `NotesHistoryCard` compacto (skill+data, citação em
  blockquote, "Duração do Estudo: X min") e `FullHistoryCard` completo (badge "Terra Selvagem",
  XP/GP empilhados, notas, rodapé com duração + botão de crônica).
- Todos os textos literais corretos: toggle "Crônicas completas"/"Compilado de Notas", estados
  vazios (geral e de notas, incluindo a menção a "Notas Teológicas"), badge "TERRA SELVAGEM",
  "Revelar/Ocultar Crônica Mística", "Nenhuma crônica antiga disponível".
- Emojis corrigidos: ⚡ XP em esmeralda, 💎 GP em âmbar (mesmo padrão do Contratos).

**3 divergências cosméticas aceitas, não corrigidas** (documentadas no `PARIDADE.md`): uso de
`Stone400` em alguns textos secundários onde a fonte usa `amber-100` em alpha baixo; título
"📜 Crônica Mística" adicionado ao painel expansível (não existe na fonte); gradiente do painel
expansível horizontal em vez de vertical. Nenhuma muda leitura ou comportamento — aceitas pra não
gastar uma terceira rodada em diferença de tom de cor.

Diff confirmado tocando só `HistoryScreen.kt`. **Aprovado.**

**Contratos + Crônicas Diárias fecham o módulo Missões na frente de UI.** Pendências que
permanecem fora deste port (não bloqueiam): módulo Reino inteiro (Bazar/Títulos/Heatmap/
Estatísticas/Conquistas/Guia) e Diário de Bordo continuam em `PlaceholderScreen`; validação visual
formal (print lado a lado com o React) ainda não foi feita pra nenhuma tela do módulo Missões,
incluindo H1/H2/H3 — mesma pendência já registrada antes, agora estendida a Contratos e Crônicas
Diárias.

## [2026-08-25] Bloco E — `ShopScreen.kt` + `HeatmapScreen.kt` — duas rodadas

Antes de escrever qualquer coisa, `git log -1` no fonte React confirmou main ainda no commit
`52822c2` (mesmo hash já auditado nos Blocos A/B/D — sem drift). Auditoria contra `ShopTab.tsx`,
`HeatmapTab.tsx` e os respectivos wrappers em `App.tsx` (`activeTab === 'shop'`/`'heatmap'`),
com um achado estrutural relevante não capturado no `MOC_reino_pre_port.md`: o bloco `'shop'` de
`App.tsx` não é só `ShopTab.tsx` — é um wrapper com sub-toggle interno (`shopSubTab`, estado local
`useState<'items'|'titles'>('items')`) que alterna entre `ShopTab.tsx` e `TitleShop.tsx`,
redundante com o item "titles" separado do próprio menu de navegação inferior
(`SUB_TABS.kingdom` tem 7 entradas independentes: shop/titles/heatmap/stats/achievements/logs/
guide). Tratamento do `shopSubTab` replicado igual ao já aprovado pro `questsSubTab` de Missões
(Bloco I): estado local efêmero, sobrevive à navegação dentro da sessão, reseta ao trocar de
módulo — fiel ao `useState` real.

**Dependência de sequenciamento identificada e não bloqueante**: a sub-aba "Selos & Títulos
Reais" dentro do `ShopScreen.kt` não tem `TitleShopScreen.kt` pra renderizar (Bloco C do módulo
Reino, ainda não feito) — mostra um placeholder temporário até esse Bloco fechar. Documentado no
próprio KDoc do arquivo e no `PARIDADE.md`.

**Adaptação consciente de UI — tooltip de hover não existe em touch**: a fonte usa
`onMouseEnter`/`onTouchStart` + `getBoundingClientRect` pra um tooltip flutuante no Heatmap.
Substituído por tap-to-select (toque na célula seleciona ela, detalhe aparece numa linha fixa
abaixo da grade, mesma informação — data por extenso + minutos). Não há equivalente real de hover
em touch puro, e posicionamento absoluto sobre coordenadas de tela é frágil em Compose.

**Simplificações visuais conscientes** (mesmo precedente já estabelecido em `GuideScreen.kt` de
remover o painel externo bordado com sombra): sem os 4 corner brackets decorativos do card de
consistência do Heatmap, sem glow/shadow customizado nas bordas de célula, sem animação de pulso
no rótulo "ATUAL" da semana corrente. Estrutura, cores, textos e hierarquia de informação
replicados fielmente — só ornamentação CSS não-funcional foi simplificada. Revisitar na validação
visual formal se necessário.

**Achado transversal do Bloco A revisitado**: a v1.1.17 do React renomeou a paleta `amber` →
`champagne` (custom, não é alias — `champagne-500: #d4af37` é visivelmente diferente do
`amber-500` padrão do Tailwind) em quase todo o app, com 3 tons (`champagne-100/900/950`)
referenciados no código-fonte sem definição em `index.css` (bug real na fonte, suspeita não
confirmada). Auditado especificamente pra este Bloco: `ShopTab.tsx` usa só `champagne-300`/`500`
(ambos definidos) e `HeatmapTab.tsx` não usa `champagne` nenhum — **Bloco E não é afetado pelo
achado das shades indefinidas**. O achado geral (paleta renomeada em telas já "Validadas" de
outros módulos) permanece em aberto, sem re-auditoria completa feita ainda — pendência registrada
abaixo.

Dois arquivos escritos diretamente por Claude (não delegados ao AI Studio, mesma metodologia de
sempre pra evitar fabricação de conteúdo). Wiring adicional: `buyShopItem` em
`HeroLogViewModel.kt` (mesmo padrão de `sellItem`) e dois blocos `"shop"`/`"heatmap"` em
`MainActivity.kt` substituindo `PlaceholderScreen`, com guarda de `state == null`.

**Primeira entrega (commit `1f75097`)**: diff auditado byte a byte (CRLF normalizado) contra os
arquivos originais — 100% idêntico, exceto uma linha de comentário residual
(`// ... HeatmapScreen.kt — PARTE 2/2 (continuação...)`) que vazou da formatação do chat (arquivo
foi colado em duas mensagens) e foi parar dentro do código real, entre as duas chamadas de
`ScopeToggleButton`. Comentário Kotlin válido, não quebrava build, mas era lixo sem sentido.

**Segunda rodada (commit `9ec5c37`)**: pedido só a remoção da linha. Diff confirmado — exatamente
1 linha removida, nada mais tocado. **Aprovado.**

Testes: `ShopLogicTest` (10/10, Bloco A) e `HeatmapLogicTest` (17/17, Bloco B) seguem passando —
XML bruto conferido, build verde. Nenhum teste de UI/Screenshot criado neste Bloco (mesma lacuna
já registrada pra outras telas do projeto).

**Pendências que ficam abertas**:
- Fidelidade visual do Bazar/Heatmap não validada (sem print lado a lado ainda).
- Loja de Títulos (Bloco C) — quando fechar, trocar o placeholder da sub-aba "Títulos" dentro do
  `ShopScreen.kt` por `TitleShopScreen(...)` real.
- Achado da paleta `amber`→`champagne` (v1.1.17) — decidir se vale reauditoria formal das telas
  já "Validadas" (Foco/Personagem/Missões) ou só tratar caso a caso quando cada uma for revisitada.

## [2026-08-26] ⚠️ Achado de processo — `.md` de dois chats em paralelo causou perda de edição

Antes de registrar o fechamento do Bloco C: ao receber o `PARIDADE.md` de volta pra atualizar,
percebi que a linha "Guia" tinha voltado pra "Não iniciado" e o item de risco inteiro sobre o
rebrand `amber`→`champagne` (registrado originalmente no Bloco A) tinha sumido — nenhum dos dois
sobreviveu ao chat que fez o Bloco E (entrada `[2026-08-25]` acima). O dev log em si não perdeu
nada (é append-only, cada chat só acrescenta no fim), mas o `PARIDADE.md` é editado por trecho —
se um chat abre uma cópia desatualizada do arquivo antes de outro salvar as próprias edições, a
versão mais recente sobrescreve sem aviso, e conteúdo unicamente registrado ali (não repetido em
nenhuma entrada do dev log) se perde de verdade.

**Reconstituído nesta sessão**: linha "Guia" reaplicada (texto idêntico ao que foi escrito em
24/08). O item de risco do rebrand `amber`→`champagne` foi **reescrito, não recuperado
verbatim** — não tenho certeza de que bater exatamente com o texto original perdido, então preferi
deixar isso explícito em vez de fingir que era uma restauração exata. Consolidei os fatos que sei
com certeza (achado original do Bloco A + as 3 telas já auditadas contra esse risco desde então:
Guia/Bloco D, Loja+Heatmap/Bloco E, Stats+Conquistas+Seletor de Títulos/Bloco C).

**Recomendação pro Bruno**: se o `PARIDADE.md` estiver sob controle de versão (git ou histórico
de edição do Google Drive/Notion/etc.), vale checar se dá pra recuperar o texto original do item
de risco perdido, pra comparar com a reconstrução acima. Pra evitar repetir isso: ao abrir um novo
chat pra fechar um Bloco, recomendo sempre re-anexar a versão mais recente do `PARIDADE.md` (não
reusar uma cópia de uma conversa anterior), principalmente quando blocos estão rodando em paralelo
em chats diferentes — que é exatamente o caso aqui (Bloco C e Bloco E fechados quase ao mesmo
tempo, em chats separados).

## [2026-08-26] Bloco C — `StatsScreen.kt` + `AchievementsScreen.kt` + `TitleSelectorScreen.kt` — fechado

**Achado de escopo relevante, descoberto durante a auditoria de fonte antes de escrever**: o
`MOC_reino_pre_port.md` e o plano original chamavam `TitleShop.tsx` de "Loja de Títulos" e
listavam como terceiro item deste Bloco. Auditoria de `App.tsx` mostrou que isso está errado —
`TitleShop.tsx` **não tem rota própria**, é renderizado como sub-aba (`shopSubTab === 'titles'`)
*dentro* da tela do Bazar (`activeTab === 'shop'`), confirmando de forma independente o mesmo
achado que o chat do Bloco E documentou (entrada `[2026-08-25]` acima) sobre o `shopSubTab`. Quem
responde pela rota real `activeTab === 'titles'` é outro componente — `TitleSelector.tsx`
(`src/modules/character/`) — diferente também do `TitleEquipModal.kt` já portado no Bloco 12
(mesma função de equipar título, UI e call-site diferentes). Troquei o terceiro entregável do
Bloco C de "TitleShopScreen" pra `TitleSelectorScreen.kt` (o componente certo pra rota `titles`).

**⚠️ Colisão de nomenclatura com o Bloco E**: o chat que fechou o Bloco E (25/08) também usou
"Bloco C" como referência — mas pra o trabalho de `TitleShop.tsx` (compra de títulos no Bazar),
que continua **não portado**. Ou seja, dois chats diferentes trataram de coisas diferentes sob o
mesmo rótulo "Bloco C do módulo Reino". Resolvido no `PARIDADE.md` desta atualização: a linha
"Loja de títulos" (compra, ainda pendente) foi desvinculada do rótulo "Bloco C" pra não confundir
com o Bloco C que efetivamente fechou hoje (Stats/Conquistas/Seletor de Títulos). Se quiser dar um
nome a esse trabalho pendente no futuro, sugiro não reusar "C" — já está ambíguo nos dois `.md`.

**3 arquivos escritos diretamente por Claude** (mesma metodologia de sempre), auditados contra
`StatsTab.tsx` (87L), `AchievementsTab.tsx` (106L) e `TitleSelector.tsx` + os respectivos
cabeçalhos de wrapper em `App.tsx` (`activeTab === 'stats'`/`'achievements'`/`'titles'`).

- **`StatsScreen.kt`**: grid 2 colunas, 6 cards, mapeamento direto de `CharacterState` +
  `averageSessionLength = totalMinutes / totalSessions` com guarda de divisão por zero. Achado:
  `bg-amber-550/[0.02]` no card de GP é classe Tailwind inválida (`amber-550` não existe na
  escala padrão, que pula de 500 pra 600) — mesma categoria do achado `stone-850` do Bloco D,
  tratado como fundo transparente.
- **`AchievementsScreen.kt`**: sem lógica nova — `AchievementCatalog.kt`/`AchievementLogic.kt`
  (Bloco 16) só confirmados como batendo exatamente com a fonte atual, sem achado.
- **`TitleSelectorScreen.kt`**: sem lógica nova — reusa `TITLE_CATALOG` (Bloco 8) e
  `viewModel.equipTitle(titleId: String?)`, que já existe e já aceita `null` pra desequipar.
  **Bug pego e corrigido antes da entrega**: badge "ATIVO" do card usava
  `.padding(top = -4.dp, end = -2.dp)` tentando replicar o `-top-2 -right-2` do CSS — `padding`
  não aceita valor negativo no Compose, quebraria em runtime. Trocado por
  `.offset(x = 6.dp, y = (-6).dp)`.

**Ícones novos** (mapeamento por aproximação semântica, mesmo processo dos blocos anteriores):
`Clock→AccessTime`, `Timer→Timer` (exato), `ShieldAlert→GppBad`, `Award→WorkspacePremium`. Todos
confirmados por build real neste bloco — nenhum precisou de substituto.

**Processo**: Bruno moveu os 3 arquivos pro pacote real
(`app/src/main/java/com/iurispraecepta/herolog/ui/kingdom/`), integrou as 3 rotas em
`MainActivity.kt` (`"stats"`/`"achievements"`/`"titles"`, usando `state`/`heroLogViewModel` já
existentes no escopo). Único ajuste de compilação: import faltante
(`androidx.compose.foundation.layout.width`) em `TitleSelectorScreen.kt` — nenhuma estrutura
visual ou texto alterado.

**Build real confirmado via XML bruto**: BUILD GREEN — `AchievementLogicTest` 3/3 e
`TitleLogicTest` 8/8, 0 falhas/erros/skips. Sem suíte nova (nenhum dos 3 arquivos tem lógica
própria pra testar), validação é a compilação limpa + confirmação de que as suítes já existentes
de `AchievementLogic`/`TitleLogic` seguem passando sem regressão.

**Bloco C fechado e aprovado.** Módulo Reino até aqui: Bloco A (Loja, lógica), Bloco B (Heatmap,
lógica), Bloco D (Guia, UI) e Bloco E (`ShopScreen`+`HeatmapScreen`, UI) fechados antes; Bloco C
(Estatísticas/Conquistas/Seletor de Títulos, UI) fecha agora. Resta: `TitleShopScreen.kt` (compra
de títulos, sub-aba do Bazar — sem bloco atribuído, ver achado de nomenclatura acima) e Diário de
Bordo (segue fora do fatiamento, nasce organicamente quando o `addSystemLog` centralizado existir
no ViewModel principal).

## [2026-08-26] Bloco F — `TitleShopScreen.kt` (Loja de Títulos, sub-aba do Bazar) — escrito, aguardando patches + build real

Retomada nesta sessão (chat original dos Blocos A/B) depois de sincronizar contra os `.md` mais
recentes enviados por Bruno — confirmado que Bloco C (outro chat) e Bloco E (outro chat) já
tinham fechado StatsScreen/AchievementsScreen/TitleSelectorScreen e ShopScreen/HeatmapScreen
respectivamente. Adotado o rótulo **Bloco F** pra este trabalho (próxima letra livre — "C" já
ficou ambíguo entre dois chats paralelos, conforme registrado no achado de processo de 26/08
acima; evitando repetir).

Antes de escrever, `git pull` no Android confirmou `main` em `98d37b3` (Blocos A/B/D/C/E todos já
mesclados) e `git log -1` no React confirmou `52822c2` — mesmo hash já auditado nos blocos
anteriores, sem drift. Auditei `StatsTab.tsx`/`AchievementsTab.tsx`/`TitleShop.tsx` direto do
clone (não do MOC de memória): as duas primeiras já estão portadas (Bloco C, outro chat) e
conferem contra a fonte real sem divergência nova digna de nota; `TitleShop.tsx` (209L) ainda
não tinha `TitleShopScreen.kt`.

**Confirmação prévia contra o repo Android real** (antes de escrever qualquer Kotlin):
`TitleLogic.kt` (Bloco 7) já tem `buyTitle`/`claimAchievementTitle` prontos (com os 2 bugs do
React já corrigidos), mas o `HeroLogViewModel.kt` só tinha wiring de `equipTitle` — as funções de
compra/resgate nunca tinham sido conectadas (não havia tela que precisasse delas até agora).
`TITLE_CATALOG` confirmado como propriedade top-level em `com.iurispraecepta.herolog.data`
(não dentro de um `object`, diferente do `ShopCatalog.ITEMS`).

**Arquivo novo escrito diretamente por Claude** (mesma metodologia de sempre):

- `ui/kingdom/TitleShopScreen.kt`: 4 seções de compra (Common/Rare/Epic/Legendary, filtradas de
  `TITLE_CATALOG` por `category`) + Achievement + Drop, espelhando as 3 seções reais da fonte.
  Grid de 2 colunas (Common/Rare) e 1 coluna (Epic/Legendary/Achievement/Drop), replicando o
  breakpoint mobile mais estreito da fonte (`grid-cols-2`/`grid-cols-1`) — os breakpoints `sm:`/
  `lg:` não se aplicam a uma tela de celular. **Decisão técnica**: grid implementado sem
  `LazyVerticalGrid` (linhas fixas via `chunked`), porque a tela inteira já vive dentro de um
  `verticalScroll` — aninhar um `LazyVerticalGrid` numa coluna rolável quebra em runtime (altura
  infinita), mesmo problema de categoria já evitado em outras telas do projeto.
- Preço formatado com `NumberFormat` `pt-BR` (`1.500 GP`, não `1500 GP`) — a fonte usa
  `toLocaleString()` sem locale explícito, mas todo o resto do app já assume pt-BR (datas
  `dd/MM/yyyy` etc.), mesma suposição de localidade de sempre, registrada explicitamente.
- Ícones: `ShoppingBag→ShoppingCart`, `Award→WorkspacePremium` (mesma escolha do Bloco C em
  `AchievementsScreen.kt`/`TitleSelectorScreen.kt`), `Sparkles→AutoAwesome` (idem). Animação de
  pulso no rótulo de categoria Epic/Legendary e no botão "Resgatar" reaproveita a mesma técnica
  (`rememberInfiniteTransition` + `alpha`) já usada em `TitleSelectorScreen.kt` — ao contrário do
  Bloco E, que tinha optado por *remover* o pulso do rótulo "ATUAL" do Heatmap como simplificação
  consciente; aqui o precedente já existia no mesmo tipo de tela (títulos), então mantive.
- `Coins` (import da fonte) não é renderizado em nenhum lugar do JSX real de `TitleShop.tsx` —
  confirmado na leitura, não portado (import morto na fonte, não um achado de bug).

**Patches pontuais necessários em 3 arquivos já existentes** (não reescritos por inteiro — edições
específicas, ver instruções passadas a Bruno na mesma mensagem que este arquivo): `ShopScreen.kt`
(troca o placeholder da sub-aba "Títulos" por `TitleShopScreen(...)` real, novos parâmetros
`state`/`onBuyTitle`/`onClaimAchievementTitle`), `HeroLogViewModel.kt` (novas funções `buyTitle`/
`claimAchievementTitle`, espelhando o padrão já existente de `buyShopItem`/`equipTitle`),
`MainActivity.kt` (passa os 2 novos callbacks + `state` na chamada existente de `ShopScreen`).

**Bloco F com arquivo novo pronto, mas NÃO fechado** — sem Gradle/Android SDK neste ambiente pra
verificar Compose (diferente de `ShopLogic.kt`/`HeatmapLogic.kt`, que são Kotlin puro e puderam
ser compilados localmente; UI com Compose não tem esse caminho de verificação aqui, mesma
limitação que já valeu pra todas as outras telas do projeto). Falta: Bruno aplicar os 3 patches +
colar `TitleShopScreen.kt` no caminho real, buildar, e confirmar BUILD GREEN antes de eu marcar
como fechado.

## [2026-08-26] Bloco F — patches aplicados, build real confirmado

Bruno aplicou os 3 patches (`ShopScreen.kt`/`HeroLogViewModel.kt`/`MainActivity.kt`) e colou
`TitleShopScreen.kt` no caminho real — commit `125aade` (confirmado via `git pull` neste chat:
4 arquivos, exatamente os esperados, nada a mais). Primeira resposta do AI Studio veio como
resumo em prosa (`BUILD SUCCESSFUL`, "0 failures") — pedido o XML cru de 4 suítes específicas
(as mais próximas do código tocado: `ShopLogicTest`, `HeatmapLogicTest`, `TitleLogicTest`,
`AchievementLogicTest`), já que este Bloco não tem teste novo próprio (é só wiring, sem lógica
nova) — a validação aqui é confirmar ausência de regressão nas suítes existentes.

**XML cru recebido e conferido**: `ShopLogicTest` 10/10, `HeatmapLogicTest` 17/17,
`TitleLogicTest` 8/8, `AchievementLogicTest` 3/3 — 0 falhas/erros/skips nas 4, timestamps
consistentes entre si (mesma rodada de build, 26/08 ~20:54 UTC).

**Bloco F fechado.** Módulo Reino completo na frente de UI: Bloco A (Loja, lógica), Bloco B
(Heatmap, lógica), Bloco D (Guia), Bloco E (`ShopScreen`+`HeatmapScreen`), Bloco C
(`StatsScreen`/`AchievementsScreen`/`TitleSelectorScreen`), Bloco F (`TitleShopScreen`) — todos
fechados. Resta apenas Diário de Bordo (fora do fatiamento por design, nasce organicamente
quando o `addSystemLog` centralizado existir no ViewModel principal) e a validação visual formal
(print lado a lado com o React) de todas as telas de Reino, ainda pendente em todo o módulo.

## [2026-08-27] Import de save (React → Android) — fechado, build real confirmado sem regressão

Retomada de "o que falta pra usar o app de verdade" (instalar/login/importar save), em outro chat
paralelo (registrado aqui só pra manter o histórico linear — este chat estava no Bloco H, abaixo,
ao mesmo tempo). Achado importante ao auditar `useGameState.ts` real: o React tem conta de usuário
e sync via Supabase de verdade (login, resolução de conflito entre dispositivos, `flush_state`
RPC) — o Android **não tem login nenhum**, por decisão de escopo já registrada (`PARIDADE.md`
seção 1, "Sync remoto"). Bruno optou por seguir só com import de save local (sem login), caminho
mais rápido pra usar.

**Lógica** (`SaveMigrationLogic.kt`, porte de `normalizeGameState`): sem poder compilar localmente
naquele chat (depende de `kotlinx.serialization.json` real, sem jar offline nem acesso a Maven
Central — diferente de `ShopLogic`/`HeatmapLogic`, que são Kotlin puro). Revisão manual cuidadosa
contra a API real, build do Bruno foi o único crivo de verdade. `SaveMigrationLogicTest` 12/12
real.

**UI** (`RestoreSaveDialog.kt`, porte do modal "Restaurar Código Rúnico" de `App.tsx`): recorte
deliberado — só a via de colar o código de save como texto, não o modal "Ajustes Gerais" inteiro.
Reusa `HeroLogModal` (Bloco 3). Textos copiados literalmente, incluindo a mensagem de erro
específica da variante "texto colado" (difere da variante "arquivo"). Novo entry point: ícone de
engrenagem na `TopAppBar` de `MainActivity.kt` (primeira tela de configurações do app).

Patches em `HeroLogViewModel.kt` (`importSaveFromPastedText`) e `MainActivity.kt` commitados
(`24b3a26`). Ajuste de compilação feito pelo AI Studio, não solicitado: removeu `modifier =
modifier` da chamada de `HeroLogModal` (parâmetro não existe na assinatura real do componente).

**Build real confirmado via XML cru, sem regressão**: `SaveMigrationLogicTest` 12/12 e
`HeroLogViewModelTest` 52/52 (suíte grande já existente, tocada pela nova função) — 0
falhas/erros/skips nas duas. **Import de save fechado.**

Com isso, os 3 itens levantados na pergunta "o que falta pra usar o app" ficam: instalar (já
funcionava), importar save (fechado agora), login (deliberadamente fora de escopo, sem equivalente
Supabase no Android) — ou seja, o app já pode ser instalado, ter o save do React importado, e
usado localmente sem conta.

**Nota de coordenação entre chats paralelos**: este fechamento aconteceu depois que o Bloco H
(abaixo) já tinha lido `HeroLogViewModel.kt`/`saveCharacterState` no estado pós-`24b3a26` (sem
login, sem os testes de `HeroLogViewModelTest` ainda tocados por essa suíte específica) — o patch
de `saveCharacterState` do Bloco H (novo parâmetro `suppressLevelUpDetection`) foi escrito depois
desse commit, então não conflita com o que está descrito aqui. Nenhuma ação adicional necessária.

## [2026-08-27] Bloco H — Level Up (detector + popup) — escrito, aguardando build real

Retomada do próximo item aberto do Módulo Personagem depois do fechamento do Bloco F. Rótulo
**Bloco H** escolhido como próxima letra livre no momento da escrita — mas **sinalizado pra
reconciliação do Bruno**: o import de save (entrada acima) pode ter sido logado como "Bloco G" em
outro chat paralelo sem que este chat tivesse visibilidade disso ainda, mesma ambiguidade de rótulo
já registrada no achado de processo de 26/08 (seção do Bloco F). O rótulo em si não afeta o código,
só a numeração do dev log.

Antes de escrever: `git clone` fresco de `HeroLog-Android-2` (confirmou `main` em `24b3a26`, já
incluindo o import de save) e de `HeroLog` (confirmou HEAD em `9fa5380`). Diff de
`useGameState.ts` entre o hash antigo auditado (`52822c2`) e o HEAD atual conferido — mudanças são
só de sincronização Supabase (`buildDiff`, conflitos remoto/local), `normalizeGameState` em si não
mudou, sem impacto no `SaveMigrationLogic.kt` já commitado. `useLevelUp.ts` (75 linhas,
`src/modules/character/useLevelUp.ts`) lido por inteiro, junto com o bloco JSX que consome
`activeLevelUp`/`dismissCurrentLevelUp` em `App.tsx` (~4465-4630) pra confirmar textos exatos e
que `hasPendingLevelUps` é destructurado mas **nunca usado em lugar nenhum** — nem no popup, nem
fora dele (confirmado por grep na fonte inteira).

**Arquivos novos, escritos diretamente por Claude:**

- `logic/character/LevelUpLogic.kt` — porte de `useLevelUp.ts`. Diferença estrutural deliberada:
  a fonte usa `useRef`/`useEffect` pra comparar contra o render anterior; o Android não tem
  "render", então virou uma função pura (`detectLevelUps(previousState, newState)`) chamada no
  funil único de mutação (`HeroLogViewModel.saveCharacterState`), comparando o `CharacterState`
  antes/depois de cada chamada. Mesmo gatilho (`novo > antigo && antigo > 0`), mesmo match de
  skill por `name` (não por índice/id). `LevelUpEvent` (sealed class `Combat`/`Skill`) é o porte
  de `LevelUpModalType`.
  - **Achado sobre `charClass`**: o campo existe em `CombatLevelUpType` na fonte, mas o JSX do
    popup só renderiza `charName` — `charClass` é código morto também na fonte (o fallback
    `gameState.charClass || 'Guerreiro'` nunca dispara, porque `charClass` real nunca é falsy).
    Mapeado pro nome serializado do enum (`CharClass.name`) só por paridade de tipo. **Aberto pra
    Bruno**: manter assim ou remover, já que não tem uso visual em nenhum dos dois lados?
- `LevelUpLogicTest.kt` — 14 casos, cobrindo: subida/não-subida de combate (nível anterior zero,
  nível igual, nível caindo), subida de skill (idem + emoji padrão `🎯` quando skill não tem
  emoji + skill nova sem nível anterior conhecido não dispara + skill removida entre os dois
  estados não quebra), múltiplos eventos simultâneos mantendo ordem (combate primeiro, depois
  skills na ordem da lista), e fallback de nome `"Aventureiro"` quando `charName` está em branco.
  **Validação local real, TDD de verdade** (não só teoria): montado um classpath Kotlin isolado
  no sandbox (model + `HeroLogInitialState.kt` + stub local de `QuestLogic` só com a assinatura de
  `toDateStringJs` — os módulos `focus`/`kingdom`/`quests` reais usam sintaxe de stdlib Kotlin que
  o `kotlinc` 1.3 disponível neste ambiente não entende, mas isso é só do sandbox de verificação,
  não afeta os arquivos reais). **RED confirmado**: compilado o teste antes de `LevelUpLogic.kt`
  existir → `unresolved reference: LevelUpLogic`/`LevelUpEvent` (falha pelo motivo certo). **GREEN
  confirmado**: implementado, recompilado, rodado via `org.junit.runner.JUnitCore` real → `OK (14
  tests)`. Só depois disso os dois arquivos foram copiados pro caminho real do projeto.
- `ui/character/LevelUpOverlay.kt` — porte da seção JSX "EPIC LEVEL UP & SKILL EVOLUTION POPUP
  SYSTEM" do `App.tsx`. Dois visuais (âmbar/combate, esmeralda/skill), textos transcritos
  literalmente ("Você evoluiu!", "LEVEL UP!", "MAESTRIA APRIMORADA", "CONTINUAR"). Partículas
  (`.rising-spark`, 16 por popup) portadas como lista sorteada uma vez por evento ativo
  (`remember(event)`), não a cada recomposição como a fonte re-sorteia a cada render — resultado
  visual equivalente. Rotação aleatória da fonte (`--rotate`) não portada (glifo "✦" é
  simétrico). **Divergência de interação sinalizada pra Bruno**: fonte não trata teclado/ESC pra
  este popup específico (só o clique no botão despacha `dismissCurrentLevelUp`); aqui o botão
  físico de voltar do Android também fecha o popup, clique fora continua sem efeito (igual à
  fonte). Escrito do zero uma segunda vez depois de uma primeira tentativa com posicionamento de
  partículas quebrado (funções auxiliares encadeadas sem sentido, botão "Continuar" sem
  `onClick` real) — descartada antes de qualquer entrega, corrigida com `BoxWithConstraints`
  pra posicionamento proporcional real.
- `LevelUpOverlayScreenshotTest.kt` — 2 casos (combate/skill), mesmo padrão Roborazzi/Robolectric
  dos outros modais do projeto (`TitleEquipModalScreenshotTest.kt` como referência de estrutura).

**Patch em arquivo existente:**

- `HeroLogViewModel.kt`: novo `_levelUpQueue: MutableStateFlow<List<LevelUpEvent>>` (exposto como
  `levelUpQueue`); `saveCharacterState` ganhou parâmetro `suppressLevelUpDetection: Boolean =
  false` (porte de `isImportingRef.current = true` da fonte) e agora chama
  `LevelUpLogic.detectLevelUps` comparando o estado anterior (capturado de forma síncrona, antes
  do `viewModelScope.launch`, pra evitar corrida com chamadas em sequência) contra o novo; nova
  `dismissCurrentLevelUp()`. `importSaveFromPastedText` (import de save, commit `24b3a26` acima)
  atualizado pra passar `suppressLevelUpDetection = true` — único ponto do projeto que hoje faz
  substituição completa de estado, evitando uma cascata de popups espúrios comparando o save
  recém-importado contra o personagem anterior. Nenhum outro fluxo de reset/restart de campanha
  existe ainda no Android (confirmado por grep — `Sanctize`/reset da fonte ainda não foi portado),
  então esse é o único call site que precisa da flag por enquanto.
- `MainActivity.kt`: `LevelUpOverlay` renderizado no nível global (junto a `RestoreSaveDialog`/
  `SaveImportResultDialog`, não preso a nenhuma aba específica) — igual à fonte, que renderiza
  `activeLevelUp` no nível do `App.tsx`, visível por cima de qualquer tela.

**Bloco H com lógica validada localmente (GREEN real), mas NÃO fechado** — UI em Compose sem
caminho de verificação local (mesma limitação de sempre, sem Gradle/Android SDK neste ambiente).
Falta: Bruno colar os 3 arquivos novos + aplicar o patch em `HeroLogViewModel.kt`/`MainActivity.kt`,
buildar, e confirmar BUILD GREEN (idealmente XML bruto de `LevelUpLogicTest` pra bater com o `14
tests` já confirmado localmente, mais screenshots de `LevelUpOverlayScreenshotTest` pra inspeção
visual) antes de marcar como fechado.

## [2026-08-28] Bloco H — build real confirmado, visual ainda pendente

Bruno aplicou os patches via prompt no AI Studio (movimentação dos 4 arquivos da raiz pro caminho
real + edições em `HeroLogViewModel.kt`/`MainActivity.kt`, conforme instruções passadas). Resultado:
`BUILD SUCCESSFUL in 1m 3s`, 0 erros de compilação.

**XML cru recebido e conferido nome a nome**: `LevelUpLogicTest` — `tests="14" failures="0"
errors="0" skipped="0"`. Os 14 nomes de teste do XML foram comparados programaticamente contra os
14 `fun` do arquivo `LevelUpLogicTest.kt` entregue — **idênticos, 1:1**, mesmo resultado que a
validação local (RED→GREEN) já tinha confirmado antes da entrega. Sem drift, sem teste faltando,
sem teste extra.

`LevelUpOverlayScreenshotTest` também passou (Roborazzi, 100% de sucesso) e gerou
`level_up_overlay_combat.png`/`level_up_overlay_skill.png` em `app/src/test/screenshots/` — mas
**as imagens não foram inspecionadas** (Bruno optou por não subir os PNGs nesta sessão). Fidelidade
visual do popup de level up segue como pendência aberta, mesmo padrão já usado em outras telas do
projeto (ex. `TitleEquipModal`: screenshot gerado em modo record, sem inspeção humana ainda).

**Bloco H fechado no que depende de build/lógica** (compilação + testes unitários confirmados via
XML bruto, sem regressão). Pendência explícita, não bloqueante: inspeção visual dos 2 screenshots
gerados. Módulo Personagem: resta a inspeção visual do Level Up (aqui) + a validação visual formal
já pendente em todo o módulo Reino (ver Bloco F).

## [2026-08-28] Conferência cruzada — transcript colado de outro chat (AI Studio) vs. `.md` atuais

Bruno colou nesta sessão um trecho de conversa de outro chat (AI Studio) que fechou o Bloco C
(`StatsScreen.kt`/`AchievementsScreen.kt`/`TitleSelectorScreen.kt`), pedindo pra aproveitar o que
desse dos `.md` recebidos. Conferência item a item contra o `PARIDADE.md`/`DEV_LOG_ANDROID.md` já
enviados por Bruno nesta mesma sessão: **tudo do transcript já estava registrado**, inclusive
detalhes finos (XML bruto de `AchievementLogicTest` 3/3 e `TitleLogicTest` 8/8, o import faltante
`androidx.compose.foundation.layout.width`, o achado do bug de padding negativo, o achado da classe
`amber-550` inválida, a correção do achado antigo sobre `TitleSelector.tsx`, o achado de processo
sobre perda de conteúdo do `.md` entre chats paralelos, e a colisão de nomenclatura "Bloco C" usado
por dois chats pra coisas diferentes) — ver entradas `[2026-08-26]` acima. Nenhuma informação nova
pra mesclar.

**Engano meu a corrigir**: antes de ler o `DEV_LOG_ANDROID.md` por inteiro (só tinha visto o início
e o final do arquivo), cheguei a reportar pra Bruno como "achado crítico" que os Blocos C/E/Diário
de Bordo estariam sem documentação — errado. Um `git clone` fresco dos dois repos (Android em
`46860eb`, React em `9fa538093`) confirmou que o código bate com o que os `.md` já diziam:
`StatsScreen.kt`/`AchievementsScreen.kt`/`TitleSelectorScreen.kt`/`ShopScreen.kt`/
`TitleShopScreen.kt`/`HistoryScreen.kt`/`HeatmapScreen.kt`/`GuideScreen.kt` todos existem, sem
`TODO`, e wireados em `MainActivity.kt`. Nenhuma tela nova precisa ser escrita — o achado real era
outro, bem menor (ver abaixo).

**Achado real, esse sim novo**: `PARIDADE.md` tinha uma linha órfã em duplicidade — seção 7 (Módulo
Reino) trazia "Diário de Bordo (Logs) | Não iniciado", nunca atualizada depois do Bloco J (24/08)
ter fechado esse item na seção correta (seção 6, Módulo Missões — `history` é sub-aba de Missões,
não do Reino). A linha certa e completa (com as 3 divergências cosméticas aceitas, emojis
corrigidos, etc.) sempre esteve na seção 6; a linha da seção 7 era um resíduo de antes do
fatiamento em blocos, nunca removido. Substituída por uma linha de redirecionamento pra seção 6, em
vez de duplicar o conteúdo de novo (risco de as duas divergirem de novo no futuro).

**Estado real do módulo Reino confirmado (não muda nada, só reafirma)**: A, B, C, D, E/F fechados
(lógica+UI escritas, build confirmado onde há XML — visual formal ainda pendente em todas, mesma
pendência já registrada). Único item do Reino ainda em `PlaceholderScreen`: `"logs"` (Registros —
distinto do Diário de Bordo/Histórico, que já está fechado). Nenhum Bloco novo foi aberto nesta
entrada — só correção de documentação.

## [2026-08-29] Bloco Logs (Registro de Atividades) — fechado

Porte de `activeTab === 'logs'` (`App.tsx`) + `addSystemLog` (62 call-sites na fonte). Arquivos
novos: `model/LogEntry.kt` (efêmero, não persistido — decisão deliberada, `logs` é `useState` local
na fonte, fora de `gameState`), `ui/kingdom/LogsScreen.kt`. `HeroLogViewModel.kt`: `_systemLogs`
(`StateFlow<List<LogEntry>>`, cap 51, mais recente primeiro) + `addSystemLog()` privada, chamada em:
combat level-up (hook único em `saveCharacterState`, texto unificado — desvio deliberado das 4
variantes da fonte), 20 sites do módulo Missões (hábitos/diárias/afazeres/recompensa de guilda,
1:1 com a fonte), cascata de `confirmFocusSession` (loot, título raro, quebra de equipamento,
masmorra, level-up de skill, conquistas — via diff `charState`/`newState` + campos de `calc`, sem
duplicar detecção), rollover (escudo/dano, via `missedCount`+`shieldConsumed`+diff de `hp` —
`RolloverResult` real não tem `shieldUsed`/`damageApplied`/`hpLost` como uma rodada do AI Studio
tinha reportado por engano; corrigido na rodada seguinte). `MainActivity.kt`: `"logs"` trocado de
`PlaceholderScreen` para `LogsScreen(logs = ...)`.

**Achados registrados**: (1) ícone/cor placeholder — `Icons.Filled.Menu` + `Amber500` no lugar do
`Scroll`+`champagne-500` da fonte, porque `Color.kt` neste commit não tem nenhum token
`Champagne*`; trocar quando/se esse token existir. (2) `survive_wilderness` mantém texto especial
("Desbloqueaste o selo [Sobrevivente da Wilderness]!"), demais conquistas usam texto genérico com
o id em caixa alta — desvio deliberado, só esse id tem tratamento especial confirmado no
`AchievementCatalog.kt`. (3) Não existe mensagem de "missão completa" pro fluxo manual de
`confirmFocusSession` — só a variante "(Auto-Recuperada)" existe na fonte, fora do escopo deste
bloco; não foi inventada. (4) Texto de estado vazio confirmado batendo fonte+screenshot: "Nenhum
sussurro celestial registrado até o momento...".

**Testes**: 3 novos em `HeroLogViewModelTest` (`systemLogs_surviveWildernessAchievement_...`,
`systemLogs_combatLevelUp_...`, `systemLogs_rollover_...`) + `LogsScreenScreenshotTest` (2,
Roborazzi). XML bruto de 14 suítes no total confirmado sem falha/erro em nenhuma (regressão
completa, não só as novas) — `HeroLogViewModelTest` 55, `HabitLogicTest` 14, `RolloverLogicTest` 12,
`TitleLogicTest` 8, `ShopLogicTest` 10, `HeatmapLogicTest` 17, `AchievementLogicTest` 3,
`AchievementCatalogTest` 5, `DailyLogicTest` 9, `TodoLogicTest` 8, `FocusRewardsLogicTest` 10,
`FocusApplyLogicTest` 12, `QuestLogicTest` 8, `QuestCatalogTest` 6, `SaveMigrationLogicTest` 12,
`LogsScreenScreenshotTest` 2. Screenshots reais inspecionadas (não só nome de arquivo) — texto/cor/
ordem batem com a fonte.

**Pendência explícita, não bloqueante**: validação visual formal (print lado a lado com a fonte
React) segue em aberto pra esta tela e pras demais do módulo Reino (Bazar, Loja de Títulos,
Heatmap, Seletor de Títulos, Estatísticas, Conquistas, Guia, Diário de Bordo, Level Up) — adiada
por decisão de Bruno, não esquecida.

**Estado do módulo Reino**: A–J fechados em lógica/build. `PlaceholderScreen` zerado. Resta só a
validação visual formal listada acima.

## [2026-08-29] Duas pendências antigas fechadas — `charClass` e formato de `HistoryEntry.date`

**`charClass` em `CombatLevelUp` (aberta desde o Bloco H)**: decisão fechada, mantém o campo no
model Kotlin por paridade estrutural de dado com a fonte (`CombatLevelUpType.charClass` existe e é
preenchido em `useLevelUp.ts`, mas não é lido em nenhum lugar do JSX do modal) — sem nenhuma lógica
de apresentação baseada nele. Confirmado por segunda opinião externa (Bruno consultou ChatGPT),
batendo com o achado original já auditado neste projeto — sem mudança de código.

**Formato de `HistoryEntry.date` (risco aberto desde o Bloco 14, seção 8 do `PARIDADE.md`)**:
resolvido. Auditoria direta do código real (não só a resposta do ChatGPT que Bruno colou —
conferido de novo, independentemente, no repo já clonado nesta sessão) confirma a cadeia:
`FocusApplyLogic.kt:99` gera `HistoryEntry.date` no formato `"dd/MM/yyyy, HH:mm:ss"` (pt-BR);
`QuestLogic.kt:20-21` (`todaySessions`) filtra com `it.date.startsWith(todayLocalStr())`, e
`todayLocalStr()` produz `dd/MM/yyyy` — o prefixo bate por construção, sem quebra silenciosa. Sem
mudança de código necessária. Fica só como risco de manutenção futura, não mais como pendência
ativa.

**Nota de processo**: esta entrada foi escrita numa sessão paralela à do agente que fez o trabalho
de `AppHeader.kt` abaixo — os dois ramos partiram do mesmo ponto (fim do Bloco Logs) sem
visibilidade um do outro. Inserida aqui, antes do `AppHeader`, pra preservar a ordem real da minha
sessão; não reflete necessariamente a ordem real entre os dois trabalhos.

## [2026-08-28] `AppHeader.kt` movido + wireado na `MainActivity.kt` — build unitário verde, achado crítico não confirmado

AI Studio executou o prompt de integração. Relatório resumido (prosa do AI Studio, ainda não
auditado por diff literal nesta sessão — só relato):

- Movido de `/AppHeader.kt` pra `ui/components/AppHeader.kt`, `package` corrigido pra
  `com.iurispraecepta.herolog.ui.components`. Cores/fontes locais mantidas intactas, `ui/theme/Color.kt`
  não tocado (correto, era o pedido).
- `MainActivity.kt`: `AppHeader` virou `topBar` do `Scaffold`, **substituindo o `TopAppBar` antigo**.
  `streak`/`gold` lidos de `characterState?.streak ?: 0` / `characterState?.gold ?: 0`
  (`HeroLogViewModel.characterState`, `StateFlow<CharacterState?>`). `isSfxMuted` virou
  `remember { mutableStateOf(false) }` local, só troca ícone (sem áudio real, como pedido).
  `onOpenSettings` ficou como lambda vazia com `// TODO`, porque o AI Studio reportou "não existe
  tela nem modal de configurações gerais de campanha no projeto".
- `res/font/` confirmada inexistente — fontes seguem placeholder (`FontFamily.Serif`/`.Monospace`).
- Build reportado: `BUILD SUCCESSFUL in 1m 10s` — mas foi `testDebugUnitTest`, não `assembleDebug`.
  **Sem `git log`/`git pull` confirmando o push ainda** (regra padrão do projeto, Armadilha da
  OAuth loop).

**🔴 Achado crítico, levantado nesta sessão ao comparar com o `DEV_LOG_ANDROID.md`/`PARIDADE.md`
mais atualizados (que outro agente vinha mantendo em paralelo):** o `TopAppBar` que o AppHeader
substituiu **não era um TopAppBar genérico** — ele tinha um ícone de engrenagem que era o **único
entry point pro `RestoreSaveDialog`** (fluxo de import/restauração de save, fechado em 27/08,
commit `24b3a26`). O AI Studio, ao dizer "não existe tela de configurações gerais" e deixar
`onOpenSettings` vazio, muito provavelmente **removeu o único jeito de abrir o `RestoreSaveDialog`
pela UI** — sem que nenhum teste unitário pegasse isso (testes não cobrem clique de UI). Isso bate
exatamente com o padrão já documentado de "AI Studio relata build verde mas não é prova de
comportamento correto".

**Não confirmado ainda — pendente de Bruno:**
1. Confirmar `git pull` + `git log -3 --oneline` pra saber se o que o AI Studio descreveu foi
   realmente commitado/pushado.
2. Abrir o app de verdade (ou pedir pro AI Studio colar o `MainActivity.kt` inteiro) e verificar se
   sobrou algum caminho pro `RestoreSaveDialog` — se não sobrou, é regressão real, precisa reconectar
   `onOpenSettings` a esse dialog (ou dar um entry point alternativo) antes de fechar o bloco.
3. Rodar `assembleDebug` de verdade (não só `testDebugUnitTest`) e visual real em device/emulador —
   este bloco continua **0% validado visualmente**.

Bloco `AppHeader` **NÃO fechado** — build unitário verde não é suficiente dado o achado acima.

## [2026-08-28] Regressão do `onOpenSettings` confirmada por Bruno

Bruno testou no app real: botão de engrenagem do `AppHeader` não faz nada. Confirma a suspeita
levantada no achado crítico acima — `RestoreSaveDialog` (import/restore de save) ficou sem entry
point na UI depois da substituição do `TopAppBar` antigo. Prompt de correção enviado ao AI Studio
(pedindo pra ele mesmo localizar o estado real que controla o `RestoreSaveDialog`, sem adivinhar
nome de variável). Bloco continua **bloqueado** até build real + confirmação visual de que o
botão volta a abrir o diálogo.

## [2026-08-28] `onOpenSettings` corrigido — `assembleDebug` real verde, diff mínimo confirmado

Correção aplicada pelo AI Studio, seguindo o processo de investigação pedido (achar antes de
mexer, sem adivinhar nome de variável):

- `isRestoreSaveOpen` (linha 135) e a chamada de `RestoreSaveDialog(...)` (linhas 440-446) **nunca
  foram removidos** — só ficaram órfãos, sem nenhum botão setando `isRestoreSaveOpen = true` depois
  que o `TopAppBar` antigo saiu.
- Correção de 1 linha: `onOpenSettings = { isRestoreSaveOpen = true }`, dentro do `AppHeader(...)`
  no `topBar` do `Scaffold`. Diff colado por Bruno confirma exatamente essa linha, nada mais mudou.
- `gradle assembleDebug` (build completo, não só testes unitários desta vez): `BUILD SUCCESSFUL in
  54s`, todas as tasks relevantes executadas/`UP-TO-DATE` sem erro.

**Bloco `AppHeader` — regressão resolvida.** Ainda não fechado por completo:
1. `git log`/`git pull` não confirmados nesta sessão (push real ainda não verificado).
2. Fontes Cinzel/JetBrains Mono seguem placeholder (`res/font/` não existe no projeto).
3. Validação visual real (device/emulador, comparação com o React) — 0% feita até agora.

**Lição pro processo**: o AI Studio, quando perguntado "existe tela de Ajustes de Campanha?",
respondeu "não existe" sem checar o arquivo inteiro — a tela existia, só estava desconectada. Isso
reforça a regra já documentada de nunca aceitar resposta em prosa sem grep/cat real por trás; desta
vez o prompt de correção já pedia "cole a linha real antes de editar", o que preveniu uma segunda
rodada de erro.

## [2026-08-28] Header sobrepondo status bar do Android — fix `statusBarsPadding()`

Bruno mandou print: relógio/ícones da status bar do Android desenhando por cima do "HEROLOG" e
das pills de streak/gold. Causa: `AppHeader.kt` não tinha nenhum tratamento de inset de status bar
— não é algo que exista na fonte React (web não tem status bar), então não foi portado na primeira
versão, e a `MainActivity.kt` provavelmente usa edge-to-edge (a `Scaffold.topBar` não aplica inset
de status bar sozinha pra um Composable customizado, só pro `TopAppBar` do Material 3, que tinha
isso de graça).

Fix aplicado no `AppHeader.kt` local: `Modifier.statusBarsPadding()` inserido entre o
`.background(QuestPanel.copy(alpha = 0.95f))` (fica full-bleed, pinta atrás da status bar também —
efeito translúcido igual ao `sticky`/`backdrop-blur` da fonte) e o `.padding(horizontal/vertical)`
de conteúdo (empurra só o conteúdo — logo, título, pills, botões — pra baixo da status bar).

Arquivo `AppHeader.kt` reenviado a Bruno com o patch. Ele precisa colar a versão nova (substituindo
a que já está em `ui/components/AppHeader.kt` no projeto) ou aplicar o diff equivalente via AI
Studio, buildar (`assembleDebug`), e mandar print real confirmando que o overlap sumiu antes de
fechar esse achado.

## [2026-08-28] Fix `statusBarsPadding()` aplicado — build confirmado, push ainda não verificado

AI Studio aplicou exatamente o diff cirúrgico pedido: 1 import (`statusBarsPadding`) + 1 linha de
modifier + comentário de decisão no cabeçalho do arquivo. `assembleDebug`: `BUILD SUCCESSFUL in
50s`. Diff colado bate 100% com o patch pedido, nada além disso mudou.

**Atenção**: o próprio AI Studio avisou que "o ambiente do container não é inicializado como
repositório Git local" — ou seja, não dá pra confiar em `git log`/`git diff` rodado por ele pra
provar push. Isso é exatamente a Armadilha já documentada ("BUILD GREEN em AI Studio ≠ commit
pushado no GitHub"). **Bruno precisa confirmar `git pull` + `git log -3 --oneline` por fora do AI
Studio** antes de considerar esse fix realmente publicado.

Pendente ainda: print real do device confirmando que o overlap com a status bar sumiu (não
recebido até agora — só temos a palavra do build, não a validação visual).

## [2026-08-29] [Infra] Migration real do Room — substitui `fallbackToDestructiveMigration()`

**Motivação**: warning de depreciação em `HeroLogApplication.kt:15`
(`fallbackToDestructiveMigration()`) expôs um risco maior por trás do warning em si — esse método
apaga e recria o banco local inteiro (`character_state`, `active_focus_session`) sem aviso, sempre
que a versão do banco sobe sem uma `Migration` explícita correspondente. Sem Supabase sync (fora de
escopo), não existe rede de segurança: um bump de versão futuro sem migration zeraria o progresso
do usuário silenciosamente.

**Arquivos criados/alterados:**
- `/app/src/main/java/com/iurispraecepta/herolog/data/database/HeroLogDatabase.kt` (adicionado
  `MIGRATION_1_2`; `@Database(..., exportSchema = true)`)
- `/app/src/main/java/com/iurispraecepta/herolog/HeroLogApplication.kt`
  (`.fallbackToDestructiveMigration()` removido, substituído por `.addMigrations(MIGRATION_1_2)`)
- `/app/build.gradle.kts` (bloco `ksp { arg("room.schemaLocation", "$projectDir/schemas") }`)
- `/app/schemas/com.iurispraecepta.herolog.data.database.HeroLogDatabase/1.json` (novo, gerado)
- `/app/schemas/com.iurispraecepta.herolog.data.database.HeroLogDatabase/2.json` (novo, gerado)

**Resumo:**
- Auditoria de diff de schema v1→v2 feita a partir do código-fonte das entidades e do
  `HeroLogDatabase_Impl.kt` gerado pelo KSP (`.git` não disponível no container do AI Studio, então
  não foi possível usar `git log`/diff de commit para essa auditoria específica — método
  alternativo documentado aqui para futura referência).
- Diff encontrado: única mudança entre v1 e v2 é a tabela nova `active_focus_session`
  (`id INTEGER NOT NULL PRIMARY KEY`, `jsonPayload TEXT NOT NULL`, `updatedAt INTEGER NOT NULL`).
  Tabela `character_state` inalterada.
- `MIGRATION_1_2` implementada com `CREATE TABLE IF NOT EXISTS active_focus_session (...)`
  idêntica ao DDL real gerado pelo Room.
- `exportSchema = true` + `room.schemaLocation` configurados para que futuras migrations tenham
  schema JSON versionado como referência confiável, em vez de depender de diff manual.
- `.fallbackToDestructiveMigration()` removido por completo — a partir de agora, qualquer bump de
  versão do banco sem migration correspondente **crasha em dev** em vez de resetar dados
  silenciosamente em produção. Comportamento desejado, mas exige atenção de qualquer bloco futuro
  que altere `CharacterStateEntity`/`ActiveFocusSessionEntity`.

**Validação:**
- Build: `./gradlew clean assembleDebug testDebugUnitTest --rerun-tasks` — `BUILD SUCCESSFUL in
  2m 39s`, 50/50 tasks **executadas** (não `UP-TO-DATE`, build anterior com cache foi rejeitado
  como validação insuficiente antes desta rodada).
- Testes: XML bruto confirmado, 18 testes / 0 falhas / 0 erros / 0 skips no total —
  `CharacterRepositoryTest` 1/1, `FocusSessionRepositoryTest` 5/5, `SaveMigrationLogicTest` 12/12.
- Schemas: `1.json` e `2.json` confirmados presentes no filesystem via `ls`.
- Visual: N/A (mudança de infraestrutura de persistência, sem UI).

**Desvios de escopo aprovados:**
- Nenhum.

**Status: FECHADO.**

## [2026-08-29] Bloco: Quick Actions Bar + split isRunning/isFocusMode + Som Ambiente + migração para com.composables:icons-lucide-android

**Arquivos criados/alterados:**
- `app/src/main/res/raw/{floresta,chuva,taverna,biblioteca,ruinas,montanha,cidade,templo}.mp3` (novos)
- `app/src/main/java/com/iurispraecepta/herolog/ui/focus/AmbientSoundConstants.kt` (novo)
- `app/src/main/java/com/iurispraecepta/herolog/ui/focus/AmbientSoundController.kt` (novo)
- `app/src/main/java/com/iurispraecepta/herolog/ui/focus/AmbientSoundModal.kt` (novo)
- `app/src/main/java/com/iurispraecepta/herolog/ui/focus/QuickActionsBar.kt` (novo)
- `app/src/main/java/com/iurispraecepta/herolog/MainActivity.kt` (split `isRunning`/`isFocusMode`, branch inline de sessão rodando, fix `onExit`)
- `app/build.gradle.kts` (dependência `com.composables:icons-lucide-android:2.2.1`, `isTransitive = false`)
- `app/src/main/java/com/iurispraecepta/herolog/ui/components/AppHeader.kt` (ícones migrados de path data manual pra lib)

**Resumo:**
- Rodar sessão (`isRunning`) e estar em tela cheia (`isFocusMode`) desacoplados, fiéis ao
  `App.tsx` real — timer roda inline na aba Foco (orb padrão + Pausar/Abandonar), tela cheia é
  ação explícita via botão "Tela Cheia" na Quick Actions Bar.
- **Bug real encontrado e corrigido**: `onExit` do `FocusModeScreen` chamava
  `viewModel.abandonSession()` ao sair da tela cheia — deveria só fechar o overlay
  (`isFocusMode = false`), sessão continua rodando. Corrigido.
- Som ambiente: `AmbientSoundController` (nível de Composable via `remember`, não no ViewModel —
  `HeroLogViewModel` é `ViewModel()` puro sem `Application` context), persistência via
  `SharedPreferences`, sincronizado com `isRunning && !isPaused && !isBreakActive`.
  **Simplificação consciente registrada**: `MediaPlayer` não recria ao trocar de trilha em
  sessão já iniciada (fonte real troca `.src` do `<audio>` em tempo real) — avisado a Bruno,
  ajuste pequeno se pedir paridade completa aqui.
- Quick Actions Bar (Modo/Som/Ajustes/Tela Cheia) implementada, ícones inicialmente com Material
  Icons aproximados (`AutoAwesome`, `WarningAmber`, `VolumeOff`, `Fullscreen`, etc.) — **corrigido
  na mesma sessão** após Bruno apontar que o app usa ícones Lucide reais nos mesmos lugares que o
  React (`herolog-android-2` já tinha esse padrão estabelecido em `AppHeader.kt`, portado
  manualmente via `buildLucideIcon`/`ImageVector.Builder` com path data extraído à mão do SVG
  oficial).
- **Decisão de arquitetura tomada nesta sessão**: em vez de continuar portando path data
  manualmente (risco de erro humano em conversões `rect`/`circle`→`path`, como ocorreu na
  primeira tentativa de correção), migrado para a lib `com.composables:icons-lucide-android:2.2.1`
  (317★ no momento da avaliação, mantida pela `composablehorizons`, módulo dedicado ao Lucide,
  usada via `R.drawable.lucide_ic_*` + `painterResource`). Avaliadas e descartadas antes:
  `thelacspace/lucide-compose` (1★, parada) e `dead8309/lucide-kotlin` (é wrapper Kotlin/JS pra
  `lucide-react`, não gera `ImageVector`/drawable nenhum — ferramenta errada).
- Ícones migrados, todos conferidos contra a lista explícita de Bruno (React Lucide → Android):
  `Sparkles`, `Shield`, `ShieldAlert`, `Volume2`, `VolumeX`, `Settings`, `Maximize`, `Pause`,
  `Play`, `X`, `ToggleLeft`, `ToggleRight`. `AppHeader.kt` também migrado (`Flame`, `Coins`,
  `Volume2`, `VolumeX`, `Settings`) — path data manual removido, `buildLucideIcon` descontinuado.
- **Achado no meio da migração**: primeira rodada deixou `AmbientSoundModal.kt` com
  `Icons.Default.VolumeUp` (Material) intocado — ícone do volume no cabeçalho do modal não fazia
  parte da lista de 12 originalmente revisada. Corrigido no commit seguinte (`Volume2`, dentro da
  lista aprovada por Bruno).

**Validação:**
- Build: `assembleDebug` real reportado verde pelo AI Studio (não confirmado por output literal
  colado nesta sessão).
- Testes: XML bruto recebido em duas rodadas.
  - 1ª rodada (pré-fix do `VolumeUp`): 4 arquivos reciclados de uma execução anterior — **rejeitados**,
    timestamps idênticos a uma resposta anterior no mesmo bloco, não uma nova execução.
  - 2ª rodada, pós-fix, execução `--rerun-tasks` confirmada por timestamp novo (19:23 vs 18:58 da
    reciclada): 411 testes totais reportados, 14 arquivos XML brutos recebidos (128 testes
    nominais conferidos, 0 falhas/erros) — `AchievementCatalogTest`, `AchievementLogicTest`,
    `BreakPrepScreenScreenshotTest`, `CharacterMappersTest`, `CharacterRepositoryTest`,
    `CharacterScreenScreenshotTest`, `CognitiveDeathLogicTest`, `CombatLogicTest`,
    `FocusModeScreenScreenshotTest`, `FocusRewardsLogicTest`, `HeroLogViewModelTest`,
    `TimerSettingsModalScreenshotTest`, `SkillsScreenScreenshotTest`, `TodosScreenScreenshotTest`.
    Restante dos 411 (~283 testes) não recebido em XML individual — aceito porque **não existe
    teste de screenshot dedicado** pra nenhum dos 4 arquivos realmente alterados neste bloco
    (`AppHeaderScreenshotTest`, `QuickActionsBarTest`, `AmbientSoundModalTest` — nenhum dos três
    existe, confirmado por Bruno), então a cobertura direta do bloco é zero por design de teste
    atual, não por omissão de relatório.
- Git: `git pull` + `git log -3 --oneline` confirmados fora do AI Studio em 3 momentos —
  `6e678b9` (bloco original, Material Icons), `7c56d64 feat: integrate Lucide icons library`
  (migração da lib), `adcdab6 feat(ui): update ambient sound icon to lucide-ic-volume-2` (fix do
  `VolumeUp` remanescente). Todos os 3 confirmados presentes no GitHub via clone direto, não só
  relato do AI Studio — 1ª tentativa de fechar sem essa checagem teria fechado com o commit da
  migração ainda não pushado (mesmo padrão da Armadilha da OAuth loop, pego antes de fechar).
- Visual: **confirmado manualmente por Bruno no device** (29/08) — header, Quick Actions Bar
  (idle e sessão rodando), modal de Som Ambiente, botões Pausar/Abandonar inline. Aprovado.

**Desvios de escopo aprovados:**
- Migração pra lib externa de ícones (não estava no escopo original do bloco, motivada pelo
  achado de ícones aproximados incorretos) — aprovada por Bruno após comparação de risco entre
  as 3 libs candidatas e o método manual.
- `MediaPlayer` sem troca de trilha em tempo real durante sessão ativa — simplificação avisada,
  não corrigida (fora de escopo desta sessão, ver Resumo acima).

**Status: FECHADO.**

## [2026-08-29] Bloco: Skill Inline Carousel (módulo Foco)

**Arquivos criados/alterados:**
- `app/src/main/java/com/iurispraecepta/herolog/logic/SkillCarouselLogic.kt` (novo)
- `app/src/test/java/com/iurispraecepta/herolog/logic/SkillCarouselLogicTest.kt` (novo)
- `app/src/main/java/com/iurispraecepta/herolog/ui/focus/SkillInlineCarousel.kt` (novo)
- `app/src/main/java/com/iurispraecepta/herolog/MainActivity.kt` (import + substituição do
  "Skill Selector Entry Card" estático pela chamada de `SkillInlineCarousel`, remoção da
  variável local `currentSelectedSkill` que ficou sem uso)

**Resumo:**
- Port de `src/components/SkillInlineCarousel.tsx` (React, fonte real clonada fresh nesta
  sessão, commit `9fa5380`, 26/08) — troca rápida da skill ativa direto no fluxo de Foco
  (setas, dots, swipe horizontal), distinto do `SkillSelectorModal` já existente (gerenciamento
  via modal, mantido intocado, ainda aberto pelo mesmo `onOpenSkillsManager`).
- Lógica de índice/swipe isolada em `SkillCarouselLogic.kt` (`clampIndex`/`nextIndex`/
  `prevIndex`/`canNavigate`/`isSwipe`), port 1:1 de `activeIdx`/`handlePrev`/`handleNext`/
  guarda `skills.length <= 1`/checagem de swipe (`Math.abs(deltaX) > Math.abs(deltaY) &&
  Math.abs(deltaX) > 35`) do componente React real.
- UI (`SkillInlineCarousel.kt`): empty state (botão tracejado "Adicione sua primeira
  Habilidade de Foco"), setas prev/next, indicadores de posição (dots), `AnimatedContent` com
  slide (equivalente ao `AnimatePresence` da fonte), swipe via `detectHorizontalDragGestures`,
  clique central abre `SkillSelectorModal` via `onOpenSkillsManager`.
- Wiring no `MainActivity.kt`: reusa as variáveis de estado já existentes do card antigo
  (`validSkillIdx`, `selectedSkillIdx`, `characterState.skills`, `focusState.isRunning`) — sem
  variável nova de estado, confirmado por trecho real colado por Bruno (linhas 504-741) antes
  da edição, não por suposição.
- **Divergências deliberadas registradas** (mesmo padrão já usado no resto do módulo Foco):
  SFX (`sound.playClick()`) omitido — SFX inteiro fora de escopo por decisão já registrada;
  atalho de teclado ←/→ tratado como N/A por design de plataforma (touch, sem teclado físico).
- Cores hardcoded localmente no arquivo (`#e5c158`/`#1c1917` etc.), mesmo padrão do
  `AppHeader.kt` — `Color.kt` ainda não tem token `Champagne*` neste commit.

**Validação:**
- Build: `assembleDebug` real — `BUILD SUCCESSFUL in 1m 15s`, 39 actionable tasks (7
  executadas, 32 up-to-date). Nenhum warning novo nos 3 arquivos deste bloco; os 4 warnings
  reportados são todos de arquivos pré-existentes não tocados (`AmbientSoundConstants.kt`,
  `TimerSettingsModal.kt` x3, `TitleShopScreen.kt`).
- Testes: RED→GREEN confirmado antes da entrega — RED rodado em sandbox isolado
  (`kotlinc`/`junit4` real via apt, não simulado) com `SkillCarouselLogic` inexistente,
  falhas de `unresolved reference` confirmadas nos 14 casos; GREEN local via
  `org.junit.runner.JUnitCore`, `OK (14 tests)`. Depois, XML bruto real do projeto (não
  sandbox) via `./gradlew testDebugUnitTest`: `SkillCarouselLogicTest`
  `tests="14" failures="0" errors="0" skipped="0"`, 14 nomes conferidos 1:1 contra os `@Test`
  do arquivo. Regressão completa: 62 arquivos XML, 425 testes totais, 0 falhas/erros/skips em
  qualquer suíte, não só as novas.
- Visual: **não validado** — sem screenshot Roborazzi (não existe teste de screenshot
  dedicado pra este componente ainda) e sem print real em device. Mesma pendência já registrada
  em outras entradas do módulo Foco/Reino ("lógica fechada, visual pendente").

**Desvios de escopo aprovados:**
- Nenhum.

**Status: Fechado (lógica + build + testes); visual pendente.**

## [2026-08-30] Bloco: Criação do AGENTS.md — constituição operacional para agentes de código

**Motivação**: estabelecer uma constituição operacional para qualquer agente de código
trabalhando no repositório, incorporando lições de incidentes já documentados neste
DEV_LOG (fabricação de resultado de teste, imports silenciosos, etc.).

**Arquivos criados/alterados:**
- `AGENTS.md` (novo)

**Resumo:**
- Arquivo criado a partir da leitura de `PARIDADE.md` e `DEV_LOG_ANDROID.md`, com validação
  factual de todas as afirmações arquiteturais contra o código-fonte real (15 claims
  verificados: single ViewModel, Room blob JSON, 2 entidades, migration 1→2, 5 módulos de
  aba, SUB_TABS em HeroLogBottomNav, Material não usado, sem DI, `@Serializable` com
  `@SerialName` em todos os enums, `createInitialCharacterState` com listas vazias de
  propósito, 3 formatos de data, `Random.Default` no loot, `fallbackToDestructiveMigration`
  removido, fontes placeholder sem `res/font/`).
- **Primeira rodada (autoauditoria)**: 10 problemas encontrados e corrigidos — (1) árvore de
  pacotes incompleta (faltava `logic/character/` e 7 arquivos soltos em `logic/`), (2) regra
  de catálogos assume workflow Claude+Bruno, (3) distinção React/AI Studio vaga sem
  diferenciação clara, (4) `createInitialCharacterState` não documentava intencionalidade das
  listas vazias, (5) 3 formatos de data não documentados, (6) referências ao inexistente
  `ARCHITECTURE_MOC.md` (3×) e `ARMADILHAS_CONHECIDAS.md` (12× no DEV_LOG) — ambos
  confirmados ausentes via grep + filesystem, (7) contradição entre seções 5.7 e 6 sobre
  testes parciais, (8) `res/font/` misturado com regras de dependências, (9) detalhes de
  baixa importância não adicionados (isTransitive, nomes de tasks Roborazzi, estrutura
  SubTabOption), (10) referências a workflow com Bruno presentes em regras genéricas.
- **Segunda rodada (auditoria independente)**: 3 problemas adicionais encontrados — (1)
  afirmação absoluta "sem efeitos colaterais" na seção 4 Lógica estava fora do escopo das
  10 correções originais (deveria descrever padrão predominante, não regra), (2) erro de
  digitação "dipendência" na seção 9 armadilha #4, (3) valor `-Xmx1536m` na seção 9 armadilha
  #2 precisava confirmação na fonte. Todos os 3 corrigidos e verificados.
- Validação de `-Xmx1536m`: confirmado em `DEV_LOG_ANDROID.md` linha 1655 — "o ambiente AI
  Studio travou consistentemente na task `:app:testDebugUnitTest` mesmo após limpeza de cache
  e aumento de heap (`-Xmx1536m`)".
- Arquivo final: 306 linhas, 10 seções, zero referências a `ARCHITECTURE_MOC.md` ou
  `ARMADILHAS_CONHECIDAS.md` como arquivos existentes, zero referências a workflow
  específico com Claude/Bruno. Pronto para commit.

**Validação:**
- Duas rodadas completas de auditoria (autoauditoria via OpenCode/MiMo + auditoria
  independente externa).
- Sem build/testes aplicável — alteração é puramente de documentação.
- Toda afirmação factual do documento final conferida contra código-fonte real
  (app/src/main/java/) ou contra o próprio `DEV_LOG_ANDROID.md` (incluindo confirmação
  do valor `-Xmx1536m` na linha 1655).

**Desvios de escopo aprovados:**
- Nenhum.

**Status: FECHADO.**

## [2026-09-02] Bloco: Bug 1 AmbientSoundController — troca de trilha em tempo real durante sessão ativa

**Arquivos criados/alterados:**
- `app/src/main/java/com/iurispraecepta/herolog/MainActivity.kt` (LaunchedEffect(ambientController.selectedTrack))
- `app/src/test/java/com/iurispraecepta/herolog/AmbientSoundControllerTest.kt` (novo)

**Resumo:**
- Adicionado `LaunchedEffect(ambientController.selectedTrack)` em `MainActivity.kt` que chama `sync()` com estado atual da sessão sempre que `selectedTrack` muda. Paridade com React `useAmbientSound.ts:60-96` (useEffect com dependência em `selectedTrack` que faz `audio.src = newSrc; audio.load()` imediatamente).
- Causa raiz: `selectTrack()` só atualizava estado/prefs; `sync()` só disparado por `LaunchedEffect` vigiando `isRunning`/`isPaused`/`isBreakActive`, não `selectedTrack`. Confirmado preexistente (não regressão do 4133e59) via `git show 4133e59^:...AmbientSoundController.kt`.
- Criado `AmbientSoundControllerTest.kt` com 8 testes Robolectric: toggle `selectTrack`, `sync` com track nula, troca durante sessão ativa/pausada, clamp de volume, `release()`, validação lista de 8 trilhas.

**Validação:**
- Build: `assembleDebug` — BUILD SUCCESSFUL
- Testes: `testDebugUnitTest` — `AmbientSoundControllerTest` 8/8 PASSED, suíte completa sem regressões
- Visual: **Confirmado manualmente por Bruno no device (02/09)** — troca imediata de trilha durante sessão rodando (ex.: Floresta → Chuva), sem pausar/retomar, sem corte perceptível

**Desvios de escopo aprovados:**
- Nenhum.

**Status: FECHADO.**

## [2026-09-02] Bloco: Bug 2 AmbientSoundController — som para ao trocar de aba

**Arquivos criados/alterados:**
- `app/src/main/java/com/iurispraecepta/herolog/MainActivity.kt` (move ambientController para escopo HeroLogTheme)

**Resumo:**
- Movido `ambientController = rememberAmbientSoundController()` do content lambda do Scaffold para escopo do HeroLogTheme (acima do Scaffold). O `remember` dentro do lambda não sobrevive a recomposição quando `activeTab` muda e o lambda produz árvores de UI diferentes — disparava `DisposableEffect(Unit).release()` indevidamente.
- Causa raiz confirmada preexistente desde primeiro commit de som ambiente (`6e678b9`), não regressão do 4133e59 nem do 083e6a6.
- `LaunchedEffect` para `sync()` permanecem no content lambda (observam `focusState`/`breakTimerState`), mas capturam `ambientController` do escopo externo estável.

**Validação:**
- Build: `assembleDebug` — BUILD SUCCESSFUL (após fix de compilação do bloco seguinte)
- Testes: `testDebugUnitTest` — suíte completa sem regressões
- Visual: **Confirmado manualmente por Bruno no device (02/09)** — som continua tocando ao trocar Foco → Herói → Skills → Foco com sessão ativa; trilha selecionada persiste

**Desvios de escopo aprovados:**
- Nenhum. Depende do Bug 1 já commitado (`083e6a6`) — ambos compatíveis.

**Status: FECHADO.**

## [2026-09-02] Bloco: Fix compilação — passar ambientController para FocusOrbPreviewScreen

**Arquivos criados/alterados:**
- `app/src/main/java/com/iurispraecepta/herolog/MainActivity.kt` (import + parâmetro + chamada)

**Resumo:**
- Fix de 9 erros "Unresolved reference 'ambientController'" introduzidos no commit `c92311b`: `FocusOrbPreviewScreen` é função de nível superior (não lambda), não captura variável do escopo externo.
- Adicionado import `AmbientSoundController`, parâmetro `ambientController: AmbientSoundController` na assinatura, e passagem no call site quando `activeTab == "focus"`.
- Preserva mudança de escopo do Bug 2 (controller no HeroLogTheme scope) — apenas threading do parâmetro.

**Validação:**
- Build: `compileDebugKotlin --console=plain` — **compilação limpa** (0 erros)
- Build completo: `assembleDebug` — BUILD SUCCESSFUL
- Testes: `testDebugUnitTest` — suíte completa sem regressões
- Visual: **Confirmado manualmente por Bruno no device (02/09)** — mesmos cenários do Bug 2 validados

**Desvios de escopo aprovados:**
- Nenhum. Fix mínimo e direcionado ao erro de compilação.

**Status: FECHADO.**
