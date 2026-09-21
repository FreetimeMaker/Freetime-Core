package com.freetime.design

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun FreetimeDatePicker(
    value: LocalDate,
    onValueChange: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    var year by remember(value) { mutableStateOf(value.year.toString()) }
    var month by remember(value) { mutableStateOf(value.monthValue.toString()) }
    var day by remember(value) { mutableStateOf(value.dayOfMonth.toString()) }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        FreetimeText("Date", style = FreetimeDesign.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FreetimeTextField(day, { day = it.filter(Char::isDigit).take(2) }, Modifier.weight(1f), label = "Day")
            FreetimeTextField(month, { month = it.filter(Char::isDigit).take(2) }, Modifier.weight(1f), label = "Month")
            FreetimeTextField(year, { year = it.filter(Char::isDigit).take(4) }, Modifier.weight(1.5f), label = "Year")
        }
        FreetimeButton("Apply", {
            runCatching { LocalDate.of(year.toInt(), month.toInt(), day.toInt()) }
                .getOrNull()?.let(onValueChange)
        })
    }
}

@Composable
fun FreetimeTimePicker(
    value: LocalTime,
    onValueChange: (LocalTime) -> Unit,
    modifier: Modifier = Modifier,
) {
    var hour by remember(value) { mutableStateOf(value.hour.toString().padStart(2, '0')) }
    var minute by remember(value) { mutableStateOf(value.minute.toString().padStart(2, '0')) }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        FreetimeText("Time", style = FreetimeDesign.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FreetimeTextField(hour, { hour = it.filter(Char::isDigit).take(2) }, Modifier.weight(1f), label = "Hour")
            FreetimeTextField(minute, { minute = it.filter(Char::isDigit).take(2) }, Modifier.weight(1f), label = "Minute")
        }
        FreetimeButton("Apply", {
            runCatching { LocalTime.of(hour.toInt(), minute.toInt()) }
                .getOrNull()?.let(onValueChange)
        })
    }
}
