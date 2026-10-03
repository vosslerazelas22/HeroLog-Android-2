package com.iurispraecepta.herolog.ui.navigation

/**
 * App Shortcuts estáticos do ícone do launcher (feature Android-first, sem equivalente no
 * React — ver `ANDROID_FIRST.md`).
 *
 * Mapeamento puro action de Intent → destino de navegação, separado da Activity para ser
 * testável como teste unitário JVM (AGENTS.md §6: "Lógica: Validada" exige teste unitário).
 * As actions abaixo são as mesmas declaradas em `res/xml/shortcuts.xml` e referenciadas no
 * `AndroidManifest.xml`.
 *
 * Destinos (todos ids de `activeTab` já existentes, sem aba nova):
 * - Pomodoro → aba `focus` (o timer NÃO é auto-iniciado: iniciar sessão tem side-effects de
 *   negócio — serviço, streak, masmorra — e a fronteira do bloco proíbe alterar o timer).
 * - Nova Todo → aba `todos` com o modal de criação aberto (`initialIsCreating`).
 * - Dailies → aba `dailies`.
 */
object AppShortcuts {

    const val ACTION_START_POMODORO =
        "com.iurispraecepta.herolog.action.START_POMODORO"
    const val ACTION_NEW_TODO =
        "com.iurispraecepta.herolog.action.NEW_TODO"
    const val ACTION_OPEN_DAILIES =
        "com.iurispraecepta.herolog.action.OPEN_DAILIES"

    sealed interface Destination {
        data object Focus : Destination
        data object NewTodo : Destination
        data object Dailies : Destination
    }

    fun destinationFor(action: String?): Destination? = when (action) {
        ACTION_START_POMODORO -> Destination.Focus
        ACTION_NEW_TODO -> Destination.NewTodo
        ACTION_OPEN_DAILIES -> Destination.Dailies
        else -> null
    }

    /** Id de `activeTab` para o qual a Activity deve navegar. Nulo = intent comum, ignorar. */
    fun activeTabFor(action: String?): String? = when (destinationFor(action)) {
        Destination.Focus -> "focus"
        Destination.NewTodo -> "todos"
        Destination.Dailies -> "dailies"
        null -> null
    }

    /** Se o destino exige abrir o modal de criação de Todo ao chegar na aba. */
    fun opensTodoCreator(action: String?): Boolean =
        destinationFor(action) is Destination.NewTodo
}
