package com.iurispraecepta.herolog.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.iurispraecepta.herolog.HeroLogApplication
import com.iurispraecepta.herolog.ui.sfx.SfxManager

class HeroLogViewModelFactory(
    private val application: HeroLogApplication,
    private val sfxManager: SfxManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        @Suppress("UNCHECKED_CAST")
        return HeroLogViewModel(
            repository = application.characterRepository,
            focusSessionRepository = application.focusSessionRepository,
            sfxManager = sfxManager
        ) as T
    }
}
