package app.hypershell.data.settings.repo

import app.hypershell.data.settings.model.AppSettings
import app.hypershell.data.settings.model.PrivilegedBackend
import app.hypershell.data.settings.model.TerminalMode
import app.hypershell.ui.theme.m3color.PaletteStyle
import app.hypershell.ui.theme.m3color.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepo {
    val appSettings: Flow<AppSettings>

    suspend fun setUseMiuix(use: Boolean)
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setPaletteStyle(style: PaletteStyle)
    suspend fun setUseDynamicColor(use: Boolean)
    suspend fun setUseMiuixMonet(use: Boolean)
    suspend fun setSeedColor(colorInt: Int)

    suspend fun setTerminalFontSize(size: Int)
    suspend fun setTerminalMaxLines(lines: Int)
    suspend fun setPrivilegedBackend(backend: PrivilegedBackend)
    suspend fun setTerminalMode(mode: TerminalMode)
}