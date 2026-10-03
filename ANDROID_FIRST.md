# ANDROID_FIRST.md — Features exclusivas do Android

> Catálogo de features que existem **apenas** no port Android, sem equivalente no React,
> por decisão de produto. Não são divergência de paridade e não devem ser "corrigidas"
> ou reportadas como ausência no React.

## Como usar
- Antes de dizer "feature X não existe no React" ou tratar algo como port pendente,
  checar esta lista.
- Toda feature aqui **não tem seção correspondente no PARIDADE.md** (paridade não se
  aplica — não há o que comparar).
- Ao adicionar uma feature Android-first, registrar aqui **no mesmo bloco** em que foi
  implementada (mesma disciplina do PARIDADE.md).

## Features

| Feature | Módulo | Status | Descrição | Bloco/data |
|---|---|---|---|---|
| (exemplo) | Foco | Implementada | — | — |
| Celebrações pendentes de Foco (fila + agregação) | Foco | Concluído (PR #29) | Sessões concluídas em background enfileiram recompensa; ao retornar, a fila é apresentada em fluxo fullscreen. Single session reutiliza FocusCompletionFlow; múltiplas sessões usam celebração agregada. A confirmação pode atualizar notas/tag do HistoryEntry via historyId. Estado de streak/pausas necessário à celebração é persistido na fila. | Bloco spec-008 núcleo / 26-09 → PR #29 / 27-09 → 2a23e492 / 29-09 |
| Notificações de foco + Foreground Service | Foco | Implementada | Timer com app fechado; notificação persistente com Pausar/Retomar/Parar; recompensa aplicada em background; encadeamento sessão→descanso→nova sessão pela notificação; celebração single/agregada ao reabrir → Por modo (PR #32 / 02-10, merge f325b4f): acento+ícone por modo (Padrão/Masmorra/Wilderness, descanso esmeralda), cronômetro nativo sem republicação, Wilderness mínima sem ações, Morte Cognitiva pós-fato (canal de eventos), "Abandonar" com consequência de Masmorra. Nenhuma contrapartida no React — todo o bloco é Android-first (app web não tem drawer de notificações). | PR #29 (2026-09-27) → PR #32 / 02-10 |
| Dirty check dos formulários de quests | Missões (Habits/Dailies/Todos) | Concluído (PR #11) | Escopo Android-first: apenas o **gatilho**. Snapshot do draft capturado ao abrir (`openCreateModal`/`openEditModal`) e comparado via `isEqualTo`; a confirmação só aparece se `isDirty`, formulário limpo fecha direto. O React não detecta alterações: Cancelar/X/fundo sempre abrem a confirmação. **Fora deste escopo:** o visual da confirmação "Descartar?" (texto, cores, botões) existe no React e segue sujeito a paridade, registrado no `PARIDADE.md`. | spec-003 / PR #11 (`9d7c4db`) |
| Rota única de fechamento dos formulários | Missões (Habits/Dailies/Todos) | Concluído (PR #11) | X, toque no fundo, botão Voltar e Cancelar passam por um único `requestClose`, que decide entre fechar e exibir o descarte. No `HeroLogModal`, `dismissOnBackPress = false` e o `BackHandler` manual é a única fonte de verdade. `QuestFormShell` concentra rodapé, divisor e estados `showDiscard`/`deleteConfirmState`. | spec-003 / PR #11 (`9d7c4db`) |
| Incorporação do input pendente do checklist no submit (FR-008) | Missões (Dailies/Todos) | Concluído (PR #11) | Se o campo de checklist tiver texto digitado e não adicionado, o submit o inclui via `mergePendingChecklistInput`, evitando perda silenciosa. O React descarta o texto pendente. | spec-003 / PR #11 (`9d7c4db`) |
| App Shortcuts estáticos do launcher | Navegação (Foco/Rituais) | Implementada | Long-press no ícone abre 3 atalhos: "Iniciar Pomodoro" → aba `focus` (sem auto-iniciar o timer, por fronteira de negócio), "Nova Todo" → aba `todos` com o modal de criação aberto (`key`+nonce sobre `initialIsCreating`), "Dailies" → aba `dailies`. `shortcuts.xml` + `android.app.shortcuts` no Manifest (`singleTop` + `onNewIntent`); roteamento puro em `ui.navigation.AppShortcuts` com teste unitário 6/6. Ícones Lucide do AAR. Cota estática prática é 4 — 4º slot mantido livre por decisão de produto. | Bloco feature/app-shortcuts / 03-10-2026 |
| Ícone de vela para streak (`StreakIcon` centralizado) | Global (header, Foco, Personagem, Taverna) | Implementada | O React usa `Flame` do lucide-react para streak em todos os lugares (`App.tsx` header, `FocusCompletionFlow.tsx` celebration/summary, `CharacterScreen.tsx` tiles); no Android a sequência é uma vela com estado acesa (`streak > 0`, laranja) / apagada (zerada, cinza stone-500). `StreakIcon(isLit)` em `ui.components`, usado nos 5 pontos: pill do AppHeader, celebration + summary do FocusCompletionFlow, tile da CharacterScreen, tile da TavernScreen. Implementação usa vetores próprios `streak_candle_lit/unlit.xml` (estilo Lucide stroke-based, sem `candle` na lib `icons-lucide-android:2.2.1`), tingidos via `litTint`/`unlitTint` por tela. | Bloco feature/streak-candle-icon / 03-10-2026 |

## Decisões relacionadas
- Celebração fullscreen é Android-first: PendingCelebrationHost substitui o antigo modal para suportar o fluxo de recuperação de sessões concluídas em background, sem exigir alteração correspondente no React.