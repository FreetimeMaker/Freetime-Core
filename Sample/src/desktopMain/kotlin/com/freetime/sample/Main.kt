package com.freetime.sample

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Freetime Core Sample") {
        SampleApp()
    }
}
