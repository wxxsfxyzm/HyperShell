package app.hypershell.ui.page.main.settings

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import app.hypershell.R
import app.hypershell.ui.icon.AppIcons
import app.hypershell.ui.page.main.navigation.AppRoute
import app.hypershell.ui.page.main.widget.settings.SettingsNavigationItemWidget
import app.hypershell.ui.page.main.widget.settings.SplicedColumnGroup
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsPage(
    navController: NavController,
    viewModel: SettingsViewModel = koinViewModel()
) {
    // val state by viewModel.uiState.collectAsState()
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(topAppBarState)

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            LargeFlexibleTopAppBar(
                windowInsets = TopAppBarDefaults.windowInsets.add(WindowInsets(left = 12.dp)),
                title = { Text(text = stringResource(id = R.string.preferred)) },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                )
            )
        },
        //snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                SplicedColumnGroup(
                    title = stringResource(R.string.personalization)
                ) {
                    item {
                        SettingsNavigationItemWidget(
                            icon = AppIcons.Theme,
                            title = stringResource(R.string.theme_settings),
                            description = stringResource(R.string.theme_settings_desc),
                            onClick = { navController.navigate(AppRoute.Appearance.route) }
                        )
                    }
                    item {
                        SettingsNavigationItemWidget(
                            icon = AppIcons.BugReport,
                            title = "Terminal Settings",
                            description = "Terminal Settings",
                            onClick = { navController.navigate(AppRoute.TerminalSettings.route) }
                        )
                    }
                }
            }


        }
    }
}