package com.iurispraecepta.herolog.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Formato "hoje" usado em `Habit.lastTriggeredDate` (mesmo padrão de `HabitLogic`).
 * Fuso: o do dispositivo, igual ao `HabitLogic`.
 */
fun formatTodayDate(now: Date = Date()): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.US).format(now)

/**
 * "Hoje" como string, recalculado quando:
 *  - o app volta ao primeiro plano (`ON_RESUME`), ou
 *  - [refreshKey] muda (ex.: a lista de hábitos mudou depois de um registro).
 *
 * Substitui o `remember { ... }` sem chave, que congelava o dia enquanto a tela
 * permanecesse composta (após a virada do dia o hábito registrado não aparecia como feito).
 * Não há timer até a meia-noite: com a tela parada e sem retomada, o valor só é
 * atualizado na próxima retomada ou mudança de [refreshKey].
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
    return today
}
