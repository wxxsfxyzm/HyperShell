package app.hypershell.ui.page.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuOpen
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FlexibleBottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.WideNavigationRail
import androidx.compose.material3.WideNavigationRailItem
import androidx.compose.material3.WideNavigationRailValue
import androidx.compose.material3.rememberWideNavigationRailState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import app.hypershell.data.settings.model.TerminalMode
import app.hypershell.ui.page.main.settings.SettingsPage
import app.hypershell.ui.page.main.terminal.TerminalPage
import kotlinx.coroutines.launch

// Data class for navigation items
data class NavItem(
    val label: String,
    val icon: ImageVector,
    val content: @Composable () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MainPage(navController: NavController, terminalMode: TerminalMode) {
    // Define navigation items
    val navItems = remember {
        listOf(
            NavItem("Terminal", Icons.Rounded.Terminal) { TerminalPage(mode = terminalMode) },
            NavItem("Settings", Icons.Rounded.Settings) { SettingsPage(navController = navController) }
        )
    }

    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { navItems.size })
    val currentPage = pagerState.currentPage

    fun onPageChanged(page: Int) {
        scope.launch {
            pagerState.animateScrollToPage(page = page)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            // Logic adapted from Installer:
            // Calculate aspect ratio. > 1.4 usually means Tall (Portrait).
            // Note: In the source installer, 'isLandscapeScreen' was calculated as H/W > 1.4,
            // which actually detects Portrait. We use 'useBottomNavigation' for clarity.
            val useBottomNavigation = this.maxHeight.value / this.maxWidth.value > 1.4

            val navigationSide =
                if (useBottomNavigation) WindowInsetsSides.Bottom
                else WindowInsetsSides.Left

            // Manually calculate insets to prevent double padding
            val navigationWindowInsets = WindowInsets.safeDrawing.only(
                (if (useBottomNavigation) WindowInsetsSides.Horizontal
                else WindowInsetsSides.Vertical) + navigationSide
            )

            // Main Layout Structure
            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                // Render Rail on the left if not using bottom navigation
                if (!useBottomNavigation) {
                    ColumnNavigation(
                        windowInsets = navigationWindowInsets,
                        data = navItems,
                        currentPage = currentPage,
                        onPageChanged = { onPageChanged(it) }
                    )
                }

                // Main Content Column
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                ) {
                    HorizontalPager(
                        state = pagerState,
                        userScrollEnabled = true,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                    ) { page ->
                        // Pass content without adding extra insets here if Scaffold is used inside
                        navItems[page].content()
                    }

                    // Render Bottom Bar if using bottom navigation
                    if (useBottomNavigation) {
                        RowNavigation(
                            windowInsets = navigationWindowInsets,
                            data = navItems,
                            currentPage = currentPage,
                            onPageChanged = { onPageChanged(it) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RowNavigation(
    windowInsets: WindowInsets,
    data: List<NavItem>,
    currentPage: Int,
    onPageChanged: (Int) -> Unit
) {
    FlexibleBottomAppBar(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentSize(),
        windowInsets = windowInsets,
        expandedHeight = 72.dp,
        horizontalArrangement = BottomAppBarDefaults.FlexibleFixedHorizontalArrangement,
        content = {
            data.forEachIndexed { index, navigationData ->
                NavigationBarItem(
                    selected = currentPage == index,
                    onClick = { onPageChanged(index) },
                    icon = {
                        // Example badge logic reserved (copied structure from Installer)
                        /*                        val showBadge = false
                                                BadgedBox(
                                                    badge = {
                                                        androidx.compose.animation.AnimatedVisibility(
                                                            visible = showBadge,
                                                            enter = scaleIn() + fadeIn(),
                                                            exit = scaleOut() + fadeOut(),
                                                            label = "badge"
                                                        ) {
                                                            Badge { Text("0") }
                                                        }
                                                    }
                                                ) {*/
                        Icon(
                            imageVector = navigationData.icon,
                            contentDescription = navigationData.label
                        )
                        //}
                    },
                    label = { Text(text = navigationData.label) },
                    alwaysShowLabel = true
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ColumnNavigation(
    windowInsets: WindowInsets,
    data: List<NavItem>,
    currentPage: Int,
    onPageChanged: (Int) -> Unit
) {
    val state = rememberWideNavigationRailState()
    val scope = rememberCoroutineScope()

    WideNavigationRail(
        state = state,
        windowInsets = windowInsets,
        header = {
            IconButton(
                modifier = Modifier
                    .padding(start = 24.dp)
                    .semantics {
                        stateDescription =
                            if (state.currentValue == WideNavigationRailValue.Expanded) {
                                "Expanded"
                            } else {
                                "Collapsed"
                            }
                    },
                onClick = {
                    scope.launch {
                        if (state.targetValue == WideNavigationRailValue.Expanded)
                            state.collapse()
                        else state.expand()
                    }
                },
            ) {
                if (state.targetValue == WideNavigationRailValue.Expanded) {
                    Icon(Icons.AutoMirrored.Rounded.MenuOpen, "Collapse rail")
                } else {
                    Icon(Icons.Rounded.Menu, "Expand rail")
                }
            }
        }
    ) {
        data.forEachIndexed { index, navigationData ->
            WideNavigationRailItem(
                railExpanded = state.targetValue == WideNavigationRailValue.Expanded,
                selected = currentPage == index,
                onClick = { onPageChanged(index) },
                icon = {
                    Icon(
                        imageVector = navigationData.icon,
                        contentDescription = navigationData.label
                    )
                },
                label = {
                    Text(text = navigationData.label)
                }
            )
        }
    }
}