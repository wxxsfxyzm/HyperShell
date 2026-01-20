package app.hypershell.ui.page.main.terminal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.hypershell.data.recycle.model.impl.PrivilegedManager
import app.hypershell.data.settings.model.PrivilegedBackend
import app.hypershell.data.settings.repo.SettingsRepo
import app.hypershell.data.terminal.local.CommandHistoryEntity
import app.hypershell.data.terminal.repo.TerminalRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Data class to represent the UI state
data class PrivilegedUiState(
    val inputCommand: String = "",
    val isExecuting: Boolean = false,
    // Holds the ID of the command currently running/streaming
    val currentRunningId: Long? = null,
    // Temporary buffer for the currently streaming output
    val currentStreamOutput: String = ""
)

class PrivilegedViewModel(
    private val repo: TerminalRepo, // Inject Repo, not DAO
    private val settingsRepo: SettingsRepo
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrivilegedUiState())
    val uiState: StateFlow<PrivilegedUiState> = _uiState.asStateFlow()

    // Data flow comes from Repository
    val historyList: StateFlow<List<CommandHistoryEntity>> = repo.commandHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onInputChange(newInput: String) {
        _uiState.update { it.copy(inputCommand = newInput) }
    }

    fun executeCommand() {
        val command = _uiState.value.inputCommand.trim()
        if (command.isBlank() || _uiState.value.isExecuting) return

        viewModelScope.launch {
            // 1. 预处理（主线程 UI 状态更新）
            _uiState.update { it.copy(isExecuting = true, inputCommand = "") }

            // 2. 切换到 IO 线程执行耗时操作
            withContext(Dispatchers.IO) {
                val newId = repo.createHistory(command)
                _uiState.update { it.copy(currentRunningId = newId, currentStreamOutput = "") }

                val backend = try {
                    settingsRepo.appSettings.first().privilegedBackend
                } catch (e: Exception) {
                    PrivilegedBackend.SHIZUKU
                }

                val args = command.split(Regex("\\s+")).filter { it.isNotBlank() }.toTypedArray()
                val fullOutputBuilder = StringBuilder()

                try {
                    // 确保 Flow 在 IO 线程生产
                    PrivilegedManager.execArrWithCallback(backend, args)
                        .collect { chunk ->
                            fullOutputBuilder.append(chunk)
                            if (!chunk.endsWith("\n")) fullOutputBuilder.append('\n')

                            // 更新 UI 状态返回主线程（MutableStateFlow 是线程安全的，但大量更新建议在这里控制频率）
                            _uiState.update { it.copy(currentStreamOutput = fullOutputBuilder.toString()) }
                        }

                    repo.updateOutput(newId, fullOutputBuilder.toString())
                } catch (e: Exception) {
                    val err = "Execution error: ${e.message}\n"
                    fullOutputBuilder.append(err)
                    repo.updateOutput(newId, fullOutputBuilder.toString())
                    _uiState.update { it.copy(currentStreamOutput = fullOutputBuilder.toString()) }
                } finally {
                    _uiState.update {
                        it.copy(isExecuting = false, currentRunningId = null)
                    }
                }
            }
        }
    }
}