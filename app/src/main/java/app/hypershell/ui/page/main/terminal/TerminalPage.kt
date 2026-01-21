package app.hypershell.ui.page.main.terminal

import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import app.hypershell.data.settings.model.TerminalMode

@Composable
fun TerminalPage(
    mode: TerminalMode
) {
    Crossfade(targetState = mode, label = "TerminalModeSwitch") { mode ->
        when (mode) {
            TerminalMode.INTERACTIVE -> InteractiveTerminal()
            TerminalMode.PRIVILEGED -> PrivilegedDashboard()
        }
    }
}