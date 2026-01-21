package app.hypershell.ui.page.main.command

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.hypershell.data.terminal.local.QuickCommandEntity
import app.hypershell.data.terminal.repo.QuickCommandRepo
import app.hypershell.ui.page.main.terminal.TerminalBridge
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class QuickCommandsViewModel(
    private val quickCommandRepo: QuickCommandRepo
) : ViewModel() {

    val quickCommands: StateFlow<List<QuickCommandEntity>> = quickCommandRepo.quickCommands
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteCommand(id: Long) {
        viewModelScope.launch {
            quickCommandRepo.deleteCommand(id)
        }
    }

    fun updateCommand(entity: QuickCommandEntity) {
        viewModelScope.launch {
            quickCommandRepo.updateCommand(entity)
        }
    }

    fun executeCommand(command: String) {
        viewModelScope.launch {
            TerminalBridge.requestExecution(command)
        }
    }
}