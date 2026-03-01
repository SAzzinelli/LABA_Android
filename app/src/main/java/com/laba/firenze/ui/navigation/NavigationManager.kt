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
    // Default active tabs (allineato a iOS: home, dispense, esami, seminari, profilo)
    private val defaultTabs = listOf(
        AppTab.HOME,
        AppTab.HANDOUTS,
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
                } catch (@Suppress("UNUSED_PARAMETER") e: Exception) {
                    null
                }
            }
            if (tabs.isNotEmpty()) {
                // Assicura che Home sia sempre primo e Profilo sempre ultimo
                _activeTabs.value = enforceHomeAndProfilePosition(tabs)
            } else {
                _activeTabs.value = defaultTabs
            }
        } else {
            _activeTabs.value = defaultTabs
        }
        updateHiddenTabs()
    }
    
    /**
     * Assicura che Home sia sempre in prima posizione e Profilo sempre in ultima
     */
    private fun enforceHomeAndProfilePosition(tabs: List<AppTab>): List<AppTab> {
        val mutable = tabs.toMutableList()
        
        // Rimuovi Home e Profilo se presenti
        mutable.remove(AppTab.HOME)
        mutable.remove(AppTab.PROFILE)
        
        // Aggiungi Home all'inizio e Profilo alla fine
        mutable.add(0, AppTab.HOME)
        mutable.add(AppTab.PROFILE)
        
        return mutable
    }

    private fun updateHiddenTabs() {
        val active = _activeTabs.value
        val all = AppTab.entries
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
        // Assicura che Home sia sempre primo e Profilo sempre ultimo anche nel default
        _activeTabs.value = enforceHomeAndProfilePosition(defaultTabs)
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
            
            // Insert before Profile (Profile is always last)
            val profileIndex = currentActive.indexOf(AppTab.PROFILE)
            if (profileIndex != -1) {
                currentActive.add(profileIndex, tab)
            } else {
                // Se Profile non c'è, aggiungi prima dell'ultimo elemento (che dovrebbe essere Profile)
                currentActive.add(currentActive.size - 1, tab)
            }
        }
        
        // Assicura che Home sia sempre primo e Profilo sempre ultimo
        _activeTabs.value = enforceHomeAndProfilePosition(currentActive)
        updateHiddenTabs()
        saveConfiguration()
    }

    // Move tab within active list
    fun moveTab(fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return
        
        val list = _activeTabs.value.toMutableList()
        if (fromIndex in list.indices && toIndex in list.indices) {
            val item = list[fromIndex]
            
            // Blocca movimento di Home (sempre primo) e Profilo (sempre ultimo)
            if (item == AppTab.HOME || item == AppTab.PROFILE) return
            // Blocca movimento verso la prima posizione (Home) o ultima posizione (Profilo)
            if (toIndex == 0 || toIndex == list.lastIndex) return
            
            list.removeAt(fromIndex)
            list.add(toIndex, item)
            
            // Assicura che Home sia sempre primo e Profilo sempre ultimo
            _activeTabs.value = enforceHomeAndProfilePosition(list)
            updateHiddenTabs() // Usually not needed but safe
            saveConfiguration()
        }
    }
}
