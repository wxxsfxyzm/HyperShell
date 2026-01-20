package app.hypershell.ui.page.main.settings.terminal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.twotone.ArrowBack
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.hypershell.R
import app.hypershell.data.settings.model.PrivilegedBackend
import app.hypershell.data.settings.model.TerminalMode
import app.hypershell.ui.page.main.widget.settings.AppBackButton
import app.hypershell.ui.page.main.widget.settings.BaseWidget
import app.hypershell.ui.page.main.widget.settings.SplicedColumnGroup
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TerminalSettingsPage(
    onBackClick: () -> Unit,
    viewModel: TerminalSettingsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    // UI Dialog States
    var showFontSizeDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showModeDialog by remember { mutableStateOf(false) }
    var showAuthDialog by remember { mutableStateOf(false) }

    // Scroll Behavior
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(topAppBarState)

    // --- Dialogs ---

    // 1. Terminal Mode Dialog
    if (showModeDialog) {
        SimpleSelectionDialog(
            title = "Terminal Mode",
            options = TerminalMode.entries,
            currentValue = state.terminalMode,
            onDismiss = { showModeDialog = false },
            onSelect = {
                viewModel.dispatch(TerminalSettingsAction.SetTerminalMode(it))
                showModeDialog = false
            },
            displayTransform = {
                when (it) {
                    TerminalMode.INTERACTIVE -> "Interactive (Shell)"
                    TerminalMode.PRIVILEGED -> "Privileged (One-shot)"
                }
            }
        )
    }

    // 2. Auth Backend Dialog
    if (showAuthDialog) {
        SimpleSelectionDialog(
            title = "Authorization Backend",
            options = PrivilegedBackend.entries,
            currentValue = state.privilegedBackend,
            onDismiss = { showAuthDialog = false },
            onSelect = {
                viewModel.dispatch(TerminalSettingsAction.SetPrivilegedBackend(it))
                showAuthDialog = false
            },
            displayTransform = {
                when (it) {
                    PrivilegedBackend.SHIZUKU -> "Shizuku"
                    PrivilegedBackend.ROOT -> "Root (Su)"
                }
            }
        )
    }

    // 3. Font Size & History (保持原有逻辑)
    if (showFontSizeDialog) {
        SimpleSelectionDialog(
            title = "Font Size",
            options = listOf(10, 12, 13, 14, 15, 16, 18, 20, 24),
            currentValue = state.fontSize,
            onDismiss = { showFontSizeDialog = false },
            onSelect = {
                viewModel.dispatch(TerminalSettingsAction.SetFontSize(it))
                showFontSizeDialog = false
            },
            displayTransform = { "${it}sp" }
        )
    }

    if (showHistoryDialog) {
        SimpleSelectionDialog(
            title = "History Limit",
            options = listOf(100, 500, 1000, 2000, 5000, 10000),
            currentValue = state.maxHistoryLines,
            onDismiss = { showHistoryDialog = false },
            onSelect = {
                viewModel.dispatch(TerminalSettingsAction.SetMaxHistoryLines(it))
                showHistoryDialog = false
            },
            displayTransform = { "$it lines" }
        )
    }

    // --- Content ---
    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            LargeFlexibleTopAppBar(
                windowInsets = TopAppBarDefaults.windowInsets.add(WindowInsets(left = 12.dp)),
                title = { Text("Terminal Settings") },
                navigationIcon = {
                    AppBackButton(
                        onClick = onBackClick,
                        icon = Icons.AutoMirrored.TwoTone.ArrowBack,
                        modifier = Modifier.size(36.dp),
                        containerColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                    )
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            // --- Group 1: Mode & Authorization (新增分组) ---
            item {
                SplicedColumnGroup(title = "Mode & Authorization") {

                    // 1. Terminal Mode
                    item {
                        BaseWidget(
                            icon = Icons.Outlined.Code,
                            title = "Terminal Mode",
                            description = if (state.terminalMode == TerminalMode.INTERACTIVE)
                                "Interactive (Shell)" else "Privileged (One-shot)",
                            onClick = { showModeDialog = true }
                        ) {}
                    }

                    // 2. Authorization Backend (联动隐藏)
                    item(visible = state.terminalMode == TerminalMode.PRIVILEGED) {
                        BaseWidget(
                            icon = Icons.Outlined.AdminPanelSettings,
                            title = "Authorization Backend",
                            description = if (state.privilegedBackend == PrivilegedBackend.SHIZUKU)
                                "Shizuku" else "Root (Su)",
                            onClick = { showAuthDialog = true }
                        ) {}
                    }
                }
            }

            // --- Group 2: Display ---
            item {
                SplicedColumnGroup(title = "Display") {
                    item {
                        BaseWidget(
                            icon = Icons.Outlined.FormatSize,
                            title = "Font Size",
                            description = "${state.fontSize}sp",
                            onClick = { showFontSizeDialog = true }
                        ) {}
                    }
                }
            }

            // --- Group 3: Buffer ---
            item {
                SplicedColumnGroup(title = "Buffer") {
                    item {
                        BaseWidget(
                            icon = Icons.Outlined.History,
                            title = "History Limit",
                            description = "${state.maxHistoryLines} lines",
                            onClick = { showHistoryDialog = true }
                        ) {}
                    }
                }
            }

            item { Spacer(Modifier.size(24.dp)) }
        }
    }
}

// 简单的单选 Dialog
@Composable
private fun <T> SimpleSelectionDialog(
    title: String,
    options: List<T>,
    currentValue: T,
    onDismiss: () -> Unit,
    onSelect: (T) -> Unit,
    displayTransform: (T) -> String = { it.toString() }
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEach { option ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(option) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (option == currentValue),
                            onClick = { onSelect(option) }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = displayTransform(option),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}