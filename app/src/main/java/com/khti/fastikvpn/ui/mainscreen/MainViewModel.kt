package com.khti.fastikvpn.ui.mainscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khti.fastikvpn.core.model.ThemeMode
import com.khti.fastikvpn.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel корневого экрана: отдаёт активности текущую тему и принимает её смену.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    /**
     * Текущая тема. Тип ThemeMode? (с вопросом) означает: значение может быть null.
     *
     * null = «тема ещё читается из хранилища». Пока она null, MainActivity держит
     * заставку и ничего не рисует. Так на экране не появляется «неправильная» тема.
     *
     * Eagerly - чтение начинается сразу при создании ViewModel,
     * а не когда на неё кто-то подпишется. Это ускоряет загрузку.
     */
    val themeMode: StateFlow<ThemeMode?> = settingsRepository.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    /** Вызывается из UI, когда пользователь выбрал тему. */
    fun onThemeSelected(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }
}