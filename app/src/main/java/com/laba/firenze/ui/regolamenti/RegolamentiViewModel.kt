package com.laba.firenze.ui.regolamenti

import androidx.lifecycle.ViewModel
import com.laba.firenze.data.gamification.AchievementManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RegolamentiViewModel @Inject constructor(
    val achievementManager: AchievementManager
) : ViewModel()
