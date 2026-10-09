package com.khti.fastikvpn.ui.mainscreen

import androidx.lifecycle.ViewModel
import com.khti.fastikvpn.ui.navigation.AppTab
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

// ViewModel каркаса. Её единственная задача - помнить, какая вкладка выбрана.
// Тему мы тут не трогаем, этим занимается MainViewModel (один класс - одна задача).
// @HiltViewModel и @Inject constructor() говорят Hilt, что он сам создаст этот класс.
// Скобки пока пустые, потому что зависимостей у нас нет, позже сюда добавится VPN-менеджер.
@HiltViewModel
class MainScreenViewModel @Inject constructor() : ViewModel() {

    // Изменяемое хранилище. Менять его можно только внутри этого класса (private).
    // Нижнее подчёркивание в названии - общепринятая договорённость для таких полей.
    private val _selectedTab = MutableStateFlow(AppTab.HOME)

    // Версия для чтения. Экраны могут только смотреть на значение и подписываться,
    // а изменить его напрямую не могут. Так никто посторонний не сломает состояние.
    val selectedTab: StateFlow<AppTab> = _selectedTab.asStateFlow()

    // Эту функцию вызывает меню, когда пользователь нажал на иконку.
    fun onTabSelected(tab: AppTab) {
        _selectedTab.value = tab
    }
}