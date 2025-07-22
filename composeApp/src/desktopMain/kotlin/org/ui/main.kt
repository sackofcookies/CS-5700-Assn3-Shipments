package org.ui

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "CS-5700-Assn2-Shipments",
    ) {
        App()
    }
}