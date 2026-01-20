package app.hypershell.ui.page.main.settings.terminal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.hypershell.data.settings.repo.SettingsRepo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TerminalSettingsViewModel(
    private val settingsRepo: SettingsRepo
) : ViewModel() {

    val state: StateFlow<TerminalSettingsState> = settingsRepo.appSettings
        .map { settings ->
            TerminalSettingsState(
                fontSize = settings.terminalFontSize,
                maxHistoryLines = settings.terminalMaxLines,
                terminalMode = settings.terminalMode,
                privilegedBackend = settings.privilegedBackend
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TerminalSettingsState()
        )

    fun dispatch(action: TerminalSettingsAction) {
        viewModelScope.launch {
            when (action) {
                is TerminalSettingsAction.SetFontSize ->
                    settingsRepo.setTerminalFontSize(action.size)

                is TerminalSettingsAction.SetMaxHistoryLines ->
                    settingsRepo.setTerminalMaxLines(action.lines)

                is TerminalSettingsAction.SetTerminalMode ->
                    settingsRepo.setTerminalMode(action.mode)

                is TerminalSettingsAction.SetPrivilegedBackend ->
                    settingsRepo.setPrivilegedBackend(action.backend)
            }
        }
    }
}