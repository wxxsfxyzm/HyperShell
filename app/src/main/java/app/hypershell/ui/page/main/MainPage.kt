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
import androidx.compose.material.icons.rounded.FlashOn
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import app.hypershell.data.settings.model.TerminalMode
import app.hypershell.ui.page.main.command.QuickCommandsPage
import app.hypershell.ui.page.main.settings.SettingsPage
import app.hypershell.ui.page.main.terminal.TerminalBridge
import app.hypershell.ui.page.main.terminal.TerminalPage
import kotlinx.coroutines.launch

data class NavItem(
    val label: String,
    val icon: ImageVector,
    val content: @Composable () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MainPage(navController: NavController, terminalMode: TerminalMode) {
    val navItems = remember(terminalMode) {
        buildList {
            add(NavItem("Terminal", Icons.Rounded.Terminal) { TerminalPage(mode = terminalMode) })
            if (terminalMode == TerminalMode.PRIVILEGED) {
                add(NavItem("Quick", Icons.Rounded.FlashOn) { QuickCommandsPage() })
            }
            add(NavItem("Settings", Icons.Rounded.Settings) { SettingsPage(navController = navController) })
        }
    }

    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { navItems.size })
    val uiSelectedIndex = if (pagerState.isScrollInProgress) {
        pagerState.targetPage
    } else {
        pagerState.currentPage
    }

    fun onPageChanged(page: Int) {
        scope.launch {
            pagerState.animateScrollToPage(page = page)
        }
    }

    // --- 新增: 监听 TerminalBridge 事件自动跳转回首页 ---
    LaunchedEffect(Unit) {
        TerminalBridge.executionEvents.collect {
            // 当收到执行请求时，自动滚动到终端页 (索引 0)
            pagerState.animateScrollToPage(0)
        }
    }
    // --------------------------------------------------

    Box(modifier = Modifier.fillMaxSize()) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val useBottomNavigation = this.maxHeight.value / this.maxWidth.value > 1.4
            val navigationSide = if (useBottomNavigation) WindowInsetsSides.Bottom else WindowInsetsSides.Left
            val navigationWindowInsets = WindowInsets.safeDrawing.only(
                (if (useBottomNavigation) WindowInsetsSides.Horizontal else WindowInsetsSides.Vertical) + navigationSide
            )

            Row(modifier = Modifier.fillMaxSize()) {
                if (!useBottomNavigation) {
                    ColumnNavigation(
                        windowInsets = navigationWindowInsets,
                        data = navItems,
                        currentPage = uiSelectedIndex,
                        onPageChanged = { onPageChanged(it) }
                    )
                }

                Column(modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()) {
                    HorizontalPager(
                        state = pagerState,
                        userScrollEnabled = true,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                    ) { page ->
                        navItems[page].content()
                    }

                    if (useBottomNavigation) {
                        RowNavigation(
                            windowInsets = navigationWindowInsets,
                            data = navItems,
                            currentPage = uiSelectedIndex,
                            onPageChanged = { onPageChanged(it) }
                        )
                    }
                }
            }
        }
    }
}

// ... RowNavigation 和 ColumnNavigation 保持不变 ...
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
                        Icon(
                            imageVector = navigationData.icon,
                            contentDescription = navigationData.label
                        )
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