package app.hypershell.ui.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.hypershell.data.settings.repo.SettingsRepo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class MainViewModel(
    settingsRepo: SettingsRepo
) : ViewModel() {
    val uiState: StateFlow<MainUiState> = settingsRepo.appSettings
        .map { it.toMainUiState() } // 调用新的转换函数
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = MainUiState(isLoaded = false) // 使用新类
        )
}