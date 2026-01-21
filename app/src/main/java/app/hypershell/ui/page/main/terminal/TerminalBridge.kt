package app.hypershell.ui.page.main.terminal

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object TerminalBridge {
    private val _executionEvents = MutableSharedFlow<String>(replay = 0)
    val executionEvents = _executionEvents.asSharedFlow()

    suspend fun requestExecution(command: String) {
        _executionEvents.emit(command)
    }
}