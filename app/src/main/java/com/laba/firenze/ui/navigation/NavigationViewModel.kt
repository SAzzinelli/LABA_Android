package com.laba.firenze.ui.navigation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor(
    val navigationManager: NavigationManager
) : ViewModel() {
    val activeTabs = navigationManager.activeTabs
}
