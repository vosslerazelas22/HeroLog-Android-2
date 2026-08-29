package com.iurispraecepta.herolog.model

/**
 * Porte de uma entrada do "Registro de Atividades" (aba `logs` / Diário -> Reino).
 *
 * Fonte real: `App.tsx`, linha ~373 —
 * `const [logs, setLogs] = useState<{ id: string; time: string; text: string; highlighted: boolean }[]>([]);`
 *
 * DECISÃO DELIBERADA (flagrar pra Bruno confirmar): esta classe NÃO é `@Serializable` e NÃO faz
 * parte de `CharacterState`. Isso é fidelidade estrutural à fonte, não um corte de escopo — na
 * React o array `logs` vive num `useState` local ao componente `App`, fora de `gameState`, e
 * portanto nunca é persistido em save/export/import. O equivalente Android correto é um
 * `MutableStateFlow<List<LogEntry>>` só-em-memória no `HeroLogViewModel` (ver instruções de patch),
 * não uma tabela Room nem um campo em `CharacterState`. Se essa decisão estiver errada — i.e., se
 * o comportamento desejado for logs sobreviverem a um restart do processo — isso muda a arquitetura
 * (precisaria de uma entidade Room nova) e precisa ser decidido antes de aplicar o patch no
 * ViewModel.
 *
 * @param id Porte de `` `${Date.now()}_${Math.random()}` ``. Único, mas sem significado semântico.
 * @param time Porte de `new Date().toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit', second: '2-digit' })`,
 *   ou seja, já formatado como string `HH:mm:ss` no momento da criação (não é um timestamp cru).
 * @param text Mensagem do evento, já com emoji/formatação embutidos (ex.: "🆙 COMBAT LEVEL UP: ...").
 * @param highlighted Porte do segundo parâmetro de `addSystemLog(text, highlighted = false)`.
 *   Controla o estilo visual: `true` = `text-amber-200 font-bold` (destaque), `false` = `text-amber-100/40` (apagado).
 */
data class LogEntry(
    val id: String,
    val time: String,
    val text: String,
    val highlighted: Boolean = false
)
