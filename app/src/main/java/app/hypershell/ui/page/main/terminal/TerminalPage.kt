package app.hypershell.ui.page.main.terminal

import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import app.hypershell.data.settings.model.TerminalMode

@Composable
fun TerminalPage(
    mode: TerminalMode
) {

    // 使用 Crossfade 增加平滑的过渡动画
    Crossfade(targetState = mode, label = "TerminalModeSwitch") { mode ->
        when (mode) {
            TerminalMode.INTERACTIVE -> InteractiveTerminal()
            TerminalMode.PRIVILEGED -> PrivilegedDashboard()
        }
    }
}