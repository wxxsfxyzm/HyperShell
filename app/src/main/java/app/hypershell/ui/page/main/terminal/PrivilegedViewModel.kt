package app.hypershell.ui.page.main.terminal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.hypershell.data.recycle.model.impl.PrivilegedManager
import app.hypershell.data.settings.model.PrivilegedBackend
import app.hypershell.data.settings.repo.SettingsRepo
import app.hypershell.data.terminal.local.CommandHistoryEntity
import app.hypershell.data.terminal.repo.QuickCommandRepo
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

data class PrivilegedUiState(
    val inputCommand: String = "",
    val isExecuting: Boolean = false,
    val currentRunningId: Long? = null,
    val currentStreamOutput: String = "",
    val activeBackend: PrivilegedBackend = PrivilegedBackend.SHIZUKU
)

class PrivilegedViewModel(
    private val settingsRepo: SettingsRepo,
    private val terminalRepo: TerminalRepo,
    private val quickCommandRepo: QuickCommandRepo
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrivilegedUiState())
    val uiState: StateFlow<PrivilegedUiState> = _uiState.asStateFlow()

    val historyList: StateFlow<List<CommandHistoryEntity>> = terminalRepo.commandHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // 监听来自 QuickCommandPage 的执行请求
        viewModelScope.launch {
            TerminalBridge.executionEvents.collect { cmd ->
                _uiState.update { it.copy(inputCommand = cmd) }
                executeCommand()
            }
        }
    }

    fun onInputChange(newInput: String) {
        _uiState.update { it.copy(inputCommand = newInput) }
    }

    // 保存到快速指令
    fun saveToQuickCommands(name: String, description: String, command: String) {
        viewModelScope.launch {
            quickCommandRepo.addCommand(name, description, command)
        }
    }

    fun executeCommand() {
        val command = _uiState.value.inputCommand.trim()
        if (command.isBlank() || _uiState.value.isExecuting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isExecuting = true, inputCommand = "") }
            withContext(Dispatchers.IO) {
                val backend = try {
                    settingsRepo.appSettings.first().privilegedBackend
                } catch (_: Exception) {
                    PrivilegedBackend.SHIZUKU
                }
                _uiState.update { it.copy(activeBackend = backend) }
                val newId = terminalRepo.createHistory(command, backend)
                _uiState.update { it.copy(currentRunningId = newId, currentStreamOutput = "") }

                val args = command.split(Regex("\\s+")).filter { it.isNotBlank() }.toTypedArray()
                val fullOutputBuilder = StringBuilder()

                try {
                    PrivilegedManager.execArrWithCallback(backend, args)
                        .collect { chunk ->
                            fullOutputBuilder.append(chunk)
                            if (!chunk.endsWith("\n")) fullOutputBuilder.append('\n')
                            _uiState.update { it.copy(currentStreamOutput = fullOutputBuilder.toString()) }
                        }
                    terminalRepo.updateOutput(newId, fullOutputBuilder.toString())
                } catch (e: Exception) {
                    val err = "Execution error: ${e.message}\n"
                    fullOutputBuilder.append(err)
                    terminalRepo.updateOutput(newId, fullOutputBuilder.toString())
                    _uiState.update { it.copy(currentStreamOutput = fullOutputBuilder.toString()) }
                } finally {
                    _uiState.update { it.copy(isExecuting = false, currentRunningId = null) }
                }
            }
        }
    }
}