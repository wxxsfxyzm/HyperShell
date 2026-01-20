package app.hypershell.data.settings.repo.impl

import androidx.compose.ui.graphics.toArgb
import app.hypershell.data.settings.local.AppDataStore
import app.hypershell.data.settings.model.AppSettings
import app.hypershell.data.settings.model.PrivilegedBackend
import app.hypershell.data.settings.model.TerminalMode
import app.hypershell.data.settings.repo.SettingsRepo
import app.hypershell.ui.theme.m3color.PaletteStyle
import app.hypershell.ui.theme.m3color.PresetColors
import app.hypershell.ui.theme.m3color.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class SettingsRepoImpl(
    private val dataStore: AppDataStore
) : SettingsRepo {

    override val appSettings: Flow<AppSettings> = combine(
        // 1. UI Settings
        dataStore.getBoolean(AppDataStore.UI_USE_MIUIX, false),
        dataStore.getString(AppDataStore.THEME_MODE, ThemeMode.SYSTEM.name)
            .map { runCatching { ThemeMode.valueOf(it) }.getOrDefault(ThemeMode.SYSTEM) },
        dataStore.getString(AppDataStore.THEME_PALETTE_STYLE, PaletteStyle.TonalSpot.name)
            .map { runCatching { PaletteStyle.valueOf(it) }.getOrDefault(PaletteStyle.TonalSpot) },
        dataStore.getBoolean(AppDataStore.THEME_USE_DYNAMIC_COLOR, true),
        dataStore.getBoolean(AppDataStore.UI_USE_MIUIX_MONET, false),
        dataStore.getInt(AppDataStore.THEME_SEED_COLOR, PresetColors.first().color.toArgb()),
        // 2. Terminal Config
        dataStore.getInt(AppDataStore.TERMINAL_FONT_SIZE, 14),
        dataStore.getInt(AppDataStore.TERMINAL_MAX_LINES, 1000),

        // 3. Mode & Auth (使用 fromKey 解析)
        dataStore.getString(AppDataStore.PRIVILEGED_BACKEND, PrivilegedBackend.SHIZUKU.key)
            .map { PrivilegedBackend.fromKey(it) },

        dataStore.getString(AppDataStore.TERMINAL_MODE, TerminalMode.PRIVILEGED.key)
            .map { TerminalMode.fromKey(it) }
    ) { values: Array<Any?> ->
        var i = 0

        val useMiuix = values[i++] as Boolean
        val themeMode = values[i++] as ThemeMode
        val paletteStyle = values[i++] as PaletteStyle
        val useDynamic = values[i++] as Boolean
        val useMonet = values[i++] as Boolean
        val seedColor = values[i++] as Int

        val fontSize = values[i++] as Int
        val maxLines = values[i++] as Int

        val privBackend = values[i++] as PrivilegedBackend
        val terminalMode = values[i] as TerminalMode

        AppSettings(
            useMiuix = useMiuix,
            themeMode = themeMode,
            paletteStyle = paletteStyle,
            useDynamicColor = useDynamic,
            useMiuixMonet = useMonet,
            seedColor = seedColor,

            terminalFontSize = fontSize,
            terminalMaxLines = maxLines,
            privilegedBackend = privBackend,
            terminalMode = terminalMode
        )
    }

    override suspend fun setUseMiuix(use: Boolean) {
        dataStore.putBoolean(AppDataStore.UI_USE_MIUIX, use)
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.putString(AppDataStore.THEME_MODE, mode.name)
    }

    override suspend fun setPaletteStyle(style: PaletteStyle) {
        dataStore.putString(AppDataStore.THEME_PALETTE_STYLE, style.name)
    }

    override suspend fun setUseDynamicColor(use: Boolean) {
        dataStore.putBoolean(AppDataStore.THEME_USE_DYNAMIC_COLOR, use)
    }

    override suspend fun setUseMiuixMonet(use: Boolean) {
        dataStore.putBoolean(AppDataStore.UI_USE_MIUIX_MONET, use)
    }

    override suspend fun setSeedColor(colorInt: Int) {
        dataStore.putInt(AppDataStore.THEME_SEED_COLOR, colorInt)
    }

    // --- Terminal & Mode Setters ---
    override suspend fun setTerminalFontSize(size: Int) {
        dataStore.putInt(AppDataStore.TERMINAL_FONT_SIZE, size)
    }

    override suspend fun setTerminalMaxLines(lines: Int) {
        dataStore.putInt(AppDataStore.TERMINAL_MAX_LINES, lines)
    }

    override suspend fun setPrivilegedBackend(backend: PrivilegedBackend) {
        // 存储 key 字符串
        dataStore.putString(AppDataStore.PRIVILEGED_BACKEND, backend.key)
    }

    override suspend fun setTerminalMode(mode: TerminalMode) {
        // 存储 key 字符串
        dataStore.putString(AppDataStore.TERMINAL_MODE, mode.key)
    }
}