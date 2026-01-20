package app.hypershell.ui.activity // 建议调整包名

import androidx.compose.ui.graphics.Color
import app.hypershell.data.settings.model.AppSettings
import app.hypershell.data.settings.model.TerminalMode
import app.hypershell.ui.theme.m3color.PaletteStyle
import app.hypershell.ui.theme.m3color.PresetColors
import app.hypershell.ui.theme.m3color.ThemeMode

// [修改] 类名改为 MainUiState
data class MainUiState(
    val isLoaded: Boolean = false,

    // --- Appearance (主题外观) ---
    val useMiuix: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val paletteStyle: PaletteStyle = PaletteStyle.TonalSpot,
    val useDynamicColor: Boolean = true,
    val useMiuixMonet: Boolean = false,
    val seedColor: Color = PresetColors.first().color,

    // --- Behavior (应用行为/路由) ---
    val terminalMode: TerminalMode = TerminalMode.PRIVILEGED
)

// [修改] 扩展函数名也可以通用化，或者保持 toUiState
fun AppSettings.toMainUiState(): MainUiState {
    return MainUiState(
        isLoaded = true,
        useMiuix = this.useMiuix,
        themeMode = this.themeMode,
        paletteStyle = this.paletteStyle,
        useDynamicColor = this.useDynamicColor,
        useMiuixMonet = this.useMiuixMonet,
        seedColor = Color(this.seedColor),
        terminalMode = this.terminalMode
    )
}