package app.hypershell.ui.page.main.terminal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun InteractiveTerminal() {
    // 这里未来会集成 TerminalView / PTY 输出
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black), // 终端通常是黑色背景
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Interactive Shell (bash/zsh)",
            color = Color.Green,
            style = MaterialTheme.typography.headlineMedium
        )
    }
}