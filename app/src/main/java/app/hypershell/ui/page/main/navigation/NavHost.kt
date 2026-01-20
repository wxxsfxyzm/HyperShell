package app.hypershell.ui.page.main.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.hypershell.data.settings.model.TerminalMode
import app.hypershell.ui.page.appearance.AppearancePage
import app.hypershell.ui.page.main.MainPage
import app.hypershell.ui.page.main.settings.terminal.TerminalSettingsPage

@Composable
fun NavHost(
    terminalMode: TerminalMode
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home", // 简单起见用字符串，也可以用对象
        modifier = Modifier.fillMaxSize()
    ) {
        // 首页 (包含 BottomBar 的那个大页面)
        composable(
            route = "home",
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) {
            MainPage(navController = navController, terminalMode = terminalMode)
        }

        // 未来在这里添加其他独立页面，例如：
        // composable("about") { AboutPage(...) }
        composable(
            route = AppRoute.Appearance.route,
            // 这里可以加进出场动画，复用 Installer 的 slideIn/slideOut
            enterTransition = { slideInHorizontally { it } },
            exitTransition = { slideOutHorizontally { -it / 3 } },
            popEnterTransition = { slideInHorizontally { -it / 3 } },
            popExitTransition = { slideOutHorizontally { it } }
        ) {
            // 这里直接用我们刚才写的 AppearancePage
            AppearancePage(onBackClick = { navController.popBackStack() })
        }
        composable(
            route = AppRoute.TerminalSettings.route,
            // 这里可以加进出场动画，复用 Installer 的 slideIn/slideOut
            enterTransition = { slideInHorizontally { it } },
            exitTransition = { slideOutHorizontally { -it / 3 } },
            popEnterTransition = { slideInHorizontally { -it / 3 } },
            popExitTransition = { slideOutHorizontally { it } }
        ) {
            // 这里直接用我们刚才写的 AppearancePage
            TerminalSettingsPage(onBackClick = { navController.popBackStack() })
        }
    }
}