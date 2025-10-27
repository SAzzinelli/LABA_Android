package com.laba.firenze.ui.documents

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

/**
 * Backwards-compat: old entry point now forwards to Programmi
 * Like iOS MaterialiView that forwards to ProgrammiView
 */
@Composable
fun MaterialiScreen(
    navController: NavController,
    viewModel: DocumentsViewModel = hiltViewModel()
) {
    ProgrammiScreen(navController, viewModel)
}
