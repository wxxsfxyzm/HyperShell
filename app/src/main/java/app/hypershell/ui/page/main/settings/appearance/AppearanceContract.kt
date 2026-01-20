package app.hypershell.ui.page.main.settings.appearance

import androidx.compose.ui.graphics.Color
import app.hypershell.ui.theme.m3color.PaletteStyle
import app.hypershell.ui.theme.m3color.RawColor
import app.hypershell.ui.theme.m3color.ThemeMode

// 1. UI 状态：只包含外观相关的数据
data class AppearanceState(
    val isLoading: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val paletteStyle: PaletteStyle = PaletteStyle.TonalSpot,
    val useDynamicColor: Boolean = true,
    val useMiuixMonet: Boolean = false,
    val useMiuixUi: Boolean = false,
    val seedColor: Color = Color.Blue, // UI 层用 Color 对象
    val availableColors: List<RawColor> = emptyList() // 预设颜色列表
)

// 2. 用户行为：明确的意图
sealed class AppearanceAction {
    data class SetThemeMode(val mode: ThemeMode) : AppearanceAction()
    data class SetPaletteStyle(val style: PaletteStyle) : AppearanceAction()
    data class SetUseDynamicColor(val use: Boolean) : AppearanceAction()
    data class SetUseMiuixMonet(val use: Boolean) : AppearanceAction()
    data class SetUseMiuixUi(val use: Boolean) : AppearanceAction()
    data class SetSeedColor(val color: Color) : AppearanceAction()
}