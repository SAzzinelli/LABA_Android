package com.laba.firenze.ui.common

fun prettifyTitle(title: String): String {
    return title.lowercase()
        .split(" ")
        .joinToString(" ") { word ->
            word.replaceFirstChar { it.uppercase() }
        }
}


