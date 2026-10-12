package com.iurispraecepta.herolog.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Formato "hoje" usado em `Habit.lastUpDate`/`Habit.lastDownDate`
 * (mesmo padrão de `HabitLogic`).
 * Fuso: o do dispositivo, igual ao `HabitLogic`.
 */
fun formatTodayDate(now: Date = Date()): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.US).format(now)

/**
 * Ms até a próxima meia-noite local (SPEC-Rebranding item 4 — "virada de dia").
 *
 * Usa [Calendar] (e não um delta de 24h) para atravessar corretamente transições de
 * horário de verão. [now] em epoch-ms, injetável para teste.
 */
fun millisUntilNextMidnight(now: Long = System.currentTimeMillis()): Long {
    val nextMidnight = Calendar.getInstance().apply {
        timeInMillis = now
        add(Calendar.DAY_OF_YEAR, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return nextMidnight.timeInMillis - now
}

/**
 * "Hoje" como string, recalculado quando:
 *  - o app volta ao primeiro plano (`ON_RESUME`), ou
 *  - [refreshKey] muda (ex.: a lista de hábitos mudou depois de um registro, ou o
 *    rollover do `HeroLogViewModel` rodou — `rolloverTick`), ou
 *  - bate a **meia-noite local** ([millisUntilNextMidnight]) com o app em primeiro
 *    plano.
 *
 * Substitui o `remember { ... }` sem chave, que congelava o dia enquanto a tela
 * permanecesse composta (após a virada do dia o hábito registrado não aparecia como feito).
 *
 * O timer de meia-noite existe porque o único rollover do app é o do `init` do
 * `HeroLogViewModel` (roda uma vez, na abertura): sem ele, os botões `+`/`−` do card
 * de hábito só apagariam na próxima retomada do app.
 */
@Composable
fun rememberTodayDateString(refreshKey: Any? = null): String {
    var today by remember(refreshKey) { mutableStateOf(formatTodayDate()) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) today = formatTodayDate()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(today) {
        delay(millisUntilNextMidnight())
        today = formatTodayDate()
    }
    return today
}
