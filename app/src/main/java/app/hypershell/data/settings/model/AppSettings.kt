package app.hypershell.data.settings.model

import app.hypershell.ui.theme.m3color.PaletteStyle
import app.hypershell.ui.theme.m3color.ThemeMode

data class AppSettings(
    // UI
    val useMiuix: Boolean,
    val themeMode: ThemeMode,
    val paletteStyle: PaletteStyle,
    val useDynamicColor: Boolean,
    val useMiuixMonet: Boolean,
    val seedColor: Int,

    // Terminal Config
    val terminalFontSize: Int,
    val terminalMaxLines: Int,

    // Mode & Auth
    val privilegedBackend: PrivilegedBackend, // Shizuku vs Root
    val terminalMode: TerminalMode            // Privileged vs Interactive
)