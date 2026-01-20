package com.laba.firenze.ui.navigation

import com.laba.firenze.data.local.AppearancePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavigationManager @Inject constructor(
    private val preferences: AppearancePreferences
) {
    // Default active tabs
    private val defaultTabs = listOf(
        AppTab.HOME,
        AppTab.COURSES,
        AppTab.EXAMS,
        AppTab.SEMINARS,
        AppTab.PROFILE
    )

    private val _activeTabs = MutableStateFlow<List<AppTab>>(emptyList())
    val activeTabs = _activeTabs.asStateFlow()

    private val _hiddenTabs = MutableStateFlow<List<AppTab>>(emptyList())
    val hiddenTabs = _hiddenTabs.asStateFlow()

    init {
        loadConfiguration()
    }

    private fun loadConfiguration() {
        val saved = preferences.getActiveTabs()
        if (saved != null) {
            val tabs = saved.mapNotNull { name ->
                try {
                    AppTab.valueOf(name)
                } catch (e: Exception) {
                    null
                }
            }
            if (tabs.isNotEmpty()) {
                _activeTabs.value = tabs
            } else {
                _activeTabs.value = defaultTabs
            }
        } else {
            _activeTabs.value = defaultTabs
        }
        updateHiddenTabs()
    }

    private fun updateHiddenTabs() {
        val active = _activeTabs.value
        val all = AppTab.values().toList()
        // Hidden are all that are not in active
        val remaining = all.filter { !active.contains(it) }
        
        // Sort order for hidden tabs (as in iOS)
        val sortOrder = listOf(
            AppTab.PROGRAMS, 
            AppTab.HANDOUTS, 
            AppTab.REGULATIONS, 
            AppTab.THESIS,
            AppTab.COURSES,
            AppTab.EXAMS,
            AppTab.SEMINARS
        )
        
        _hiddenTabs.value = remaining.sortedBy { tab ->
            val idx = sortOrder.indexOf(tab)
            if (idx == -1) 999 else idx
        }
    }

    private fun saveConfiguration() {
        preferences.setActiveTabs(_activeTabs.value.map { it.name })
    }

    fun resetToDefault() {
        _activeTabs.value = defaultTabs
        updateHiddenTabs()
        saveConfiguration()
    }

    fun toggleTabVisibility(tab: AppTab) {
        val currentActive = _activeTabs.value.toMutableList()
        
        if (currentActive.contains(tab)) {
            // Remove (Hide)
            if (tab.isLocked) return // Should not happen but safety check
            currentActive.remove(tab)
        } else {
            // Add (Show)
            if (currentActive.size >= 5) return // Max limit
            
            // Insert before Profile (assuming Profile is usually last)
            val profileIndex = currentActive.indexOf(AppTab.PROFILE)
            if (profileIndex != -1) {
                currentActive.add(profileIndex, tab)
            } else {
                currentActive.add(tab)
            }
        }
        
        _activeTabs.value = currentActive
        updateHiddenTabs()
        saveConfiguration()
    }

    // Move tab within active list
    fun moveTab(fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return
        
        val list = _activeTabs.value.toMutableList()
        if (fromIndex in list.indices && toIndex in list.indices) {
            val item = list.removeAt(fromIndex)
            list.add(toIndex, item)
            _activeTabs.value = list
            updateHiddenTabs() // Usually not needed but safe
            saveConfiguration()
        }
    }
}
