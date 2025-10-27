package com.laba.firenze.ui.tutorial.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TutorialViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val _isTutorialCompleted = MutableStateFlow(false)
    val isTutorialCompleted = _isTutorialCompleted.asStateFlow()
    
    private val sharedPrefs: SharedPreferences = context.getSharedPreferences(
        "LABA_PREFS", Context.MODE_PRIVATE
    )
    
    init {
        _isTutorialCompleted.value = sharedPrefs.getBoolean("tutorial_completed", false)
    }
    
    fun markTutorialCompleted() {
        viewModelScope.launch {
            sharedPrefs.edit().putBoolean("tutorial_completed", true).apply()
            _isTutorialCompleted.value = true
        }
    }
    
    fun isTutorialCompleted(): Boolean {
        return sharedPrefs.getBoolean("tutorial_completed", false)
    }
}

