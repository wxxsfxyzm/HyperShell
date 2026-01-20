package app.hypershell.ui.page.main.settings.terminal

import app.hypershell.data.settings.model.PrivilegedBackend
import app.hypershell.data.settings.model.TerminalMode

// 1. UI 状态：只包含终端设置页面需要的数据
data class TerminalSettingsState(
    // 默认值只是占位，实际数据会从 DataStore 加载
    val fontSize: Int = 14,
    val maxHistoryLines: Int = 1000,

    val terminalMode: TerminalMode = TerminalMode.INTERACTIVE,
    val privilegedBackend: PrivilegedBackend = PrivilegedBackend.SHIZUKU
)

// 2. 用户行为：明确的意图
sealed class TerminalSettingsAction {
    // 设置字体大小
    data class SetFontSize(val size: Int) : TerminalSettingsAction()

    // 设置最大历史回滚行数
    data class SetMaxHistoryLines(val lines: Int) : TerminalSettingsAction()

    data class SetTerminalMode(val mode: TerminalMode) : TerminalSettingsAction()
    data class SetPrivilegedBackend(val backend: PrivilegedBackend) : TerminalSettingsAction()
}