package app.hypershell.ui.page.appearance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.twotone.InvertColors
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
import androidx.compose.ui.unit.sp
import app.hypershell.R
import app.hypershell.ui.page.main.settings.appearance.AppearanceAction
import app.hypershell.ui.page.main.settings.appearance.AppearanceViewModel
import app.hypershell.ui.page.main.widget.card.ColorSwatchPreview
import app.hypershell.ui.page.main.widget.settings.AppBackButton
import app.hypershell.ui.page.main.widget.settings.BaseWidget
import app.hypershell.ui.page.main.widget.settings.SelectableSettingItem
import app.hypershell.ui.page.main.widget.settings.SplicedColumnGroup
import app.hypershell.ui.page.main.widget.settings.SwitchWidget
import app.hypershell.ui.theme.m3color.PaletteStyle
import app.hypershell.ui.theme.m3color.ThemeMode
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppearancePage(
    onBackClick: () -> Unit,
    viewModel: AppearanceViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    var showPaletteDialog by remember { mutableStateOf(false) }
    var showThemeModeDialog by remember { mutableStateOf(false) }

    // Scroll Behavior
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(topAppBarState)

    // --- Dialogs ---
    if (showPaletteDialog) {
        PaletteStyleDialog(
            currentStyle = state.paletteStyle,
            onDismiss = { showPaletteDialog = false },
            onSelect = { style ->
                viewModel.dispatch(AppearanceAction.SetPaletteStyle(style))
                showPaletteDialog = false
            }
        )
    }

    if (showThemeModeDialog) {
        ThemeModeDialog(
            currentMode = state.themeMode,
            onDismiss = { showThemeModeDialog = false },
            onSelect = { mode ->
                viewModel.dispatch(AppearanceAction.SetThemeMode(mode))
                showThemeModeDialog = false
            }
        )
    }

    // --- Content ---
    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            LargeFlexibleTopAppBar(
                windowInsets = TopAppBarDefaults.windowInsets.add(WindowInsets(left = 12.dp)),
                title = { Text(stringResource(R.string.theme_settings)) },
                navigationIcon = {
                    Row {
                        AppBackButton(
                            onClick = onBackClick,
                            icon = Icons.AutoMirrored.TwoTone.ArrowBack,
                            modifier = Modifier.size(36.dp),
                            containerColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                        )
                        Spacer(modifier = Modifier.size(16.dp))
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
    ) { paddingValues ->

        if (state.isLoading) return@Scaffold

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // --- Group 1: UI Style Selection (UI 风格) ---
            item {
                SplicedColumnGroup(
                    title = stringResource(R.string.theme_settings_ui_style)
                ) {
                    // Option 1: Google UI (Material Design 3)
                    item {
                        SelectableSettingItem(
                            title = stringResource(R.string.theme_settings_google_ui),
                            description = stringResource(R.string.theme_settings_google_ui_desc),
                            selected = !state.useMiuixUi,
                            onClick = {
                                if (state.useMiuixUi) {
                                    viewModel.dispatch(AppearanceAction.SetUseMiuixUi(false))
                                }
                            }
                        )
                    }
                    // Option 2: MIUIX UI
                    item {
                        SelectableSettingItem(
                            title = stringResource(R.string.theme_settings_miuix_ui),
                            description = stringResource(R.string.theme_settings_miuix_ui_desc),
                            selected = state.useMiuixUi,
                            onClick = {
                                if (!state.useMiuixUi) {
                                    viewModel.dispatch(AppearanceAction.SetUseMiuixUi(true))
                                }
                            }
                        )
                    }
                }
            }

            // --- Group 2: Basic Appearance (基础外观) ---
            item {
                SplicedColumnGroup(
                    title = stringResource(R.string.basic) // 复用通用标题
                ) {
                    // 1. Theme Mode (主题模式)
                    item {
                        BaseWidget(
                            icon = Icons.Default.DarkMode,
                            title = stringResource(R.string.theme_settings_theme_mode),
                            description = when (state.themeMode) {
                                ThemeMode.LIGHT -> stringResource(R.string.theme_settings_theme_mode_light)
                                ThemeMode.DARK -> stringResource(R.string.theme_settings_theme_mode_dark)
                                ThemeMode.SYSTEM -> stringResource(R.string.theme_settings_theme_mode_system)
                            },
                            onClick = { showThemeModeDialog = true }
                        ) {}
                    }

                    // 2. Palette Style (调色板风格)
                    item {
                        BaseWidget(
                            icon = Icons.Default.Style,
                            title = stringResource(R.string.theme_settings_palette_style),
                            description = state.paletteStyle.displayName,
                            onClick = { showPaletteDialog = true }
                        ) {}
                    }

                    // 3. Dynamic Color (动态取色)
                    item {
                        SwitchWidget(
                            icon = Icons.TwoTone.InvertColors,
                            title = stringResource(R.string.theme_settings_dynamic_color),
                            description = stringResource(R.string.theme_settings_dynamic_color_desc),
                            checked = state.useDynamicColor,
                            onCheckedChange = {
                                viewModel.dispatch(AppearanceAction.SetUseDynamicColor(it))
                            }
                        )
                    }
                }
            }

            // --- Group 3: Theme Color (Manual Selection) ---
            // 只有当关闭动态取色时才显示
            item {
                AnimatedVisibility(
                    visible = !state.useDynamicColor,
                    enter = fadeIn(animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)) +
                            expandVertically(animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)),
                    exit = fadeOut(animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)) +
                            shrinkVertically(animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing))
                ) {
                    SplicedColumnGroup(
                        title = stringResource(R.string.theme_settings_theme_color)
                    ) {
                        item {
                            BoxWithConstraints(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 16.dp)
                            ) {
                                val itemMinWidth = 88.dp
                                val columns = (this.maxWidth / itemMinWidth).toInt().coerceAtLeast(1)
                                val chunkedColors = state.availableColors.chunked(columns)

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    chunkedColors.forEach { rowItems ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            rowItems.forEach { rawColor ->
                                                Box(
                                                    modifier = Modifier.weight(1f),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    ColorSwatchPreview(
                                                        rawColor = rawColor,
                                                        currentStyle = state.paletteStyle,
                                                        textStyle = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                                                        textColor = MaterialTheme.colorScheme.onSurface,
                                                        isSelected = !state.useDynamicColor && state.seedColor == rawColor.color,
                                                    ) {
                                                        viewModel.dispatch(AppearanceAction.SetSeedColor(rawColor.color))
                                                    }
                                                }
                                            }

                                            val remaining = columns - rowItems.size
                                            if (remaining > 0) {
                                                repeat(remaining) {
                                                    Spacer(Modifier.weight(1f))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.size(24.dp)) }
        }
    }
}

// --- 辅助 Dialogs (直接保留) ---

@Composable
private fun PaletteStyleDialog(
    currentStyle: PaletteStyle,
    onDismiss: () -> Unit,
    onSelect: (PaletteStyle) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.theme_settings_palette_style_desc)) },
        text = {
            Column {
                PaletteStyle.entries.forEach { style ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(style) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (style == currentStyle),
                            onClick = { onSelect(style) }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(style.displayName)
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

@Composable
private fun ThemeModeDialog(
    currentMode: ThemeMode,
    onDismiss: () -> Unit,
    onSelect: (ThemeMode) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.theme_settings_theme_mode_desc)) },
        text = {
            Column {
                ThemeMode.entries.forEach { mode ->
                    val modeText = when (mode) {
                        ThemeMode.LIGHT -> stringResource(R.string.theme_settings_theme_mode_light)
                        ThemeMode.DARK -> stringResource(R.string.theme_settings_theme_mode_dark)
                        ThemeMode.SYSTEM -> stringResource(R.string.theme_settings_theme_mode_system)
                    }
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(mode) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (mode == currentMode),
                            onClick = { onSelect(mode) }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(modeText)
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