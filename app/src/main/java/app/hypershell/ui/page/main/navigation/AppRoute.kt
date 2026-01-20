package app.hypershell.ui.page.main.navigation

sealed class AppRoute(val route: String) {
    // 主界面（包含终端和设置两个Tab）
    data object Home : AppRoute("home")

    // 示例：未来如果有独立的详情页
    // data class Detail(val id: Long) : AppRoute()
    data object Appearance : AppRoute("settings/appearance")
    data object TerminalSettings : AppRoute("settings/terminal")
}