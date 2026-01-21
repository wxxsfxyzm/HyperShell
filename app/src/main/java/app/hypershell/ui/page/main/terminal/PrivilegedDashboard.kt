package app.hypershell.ui.page.main.terminal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.hypershell.data.settings.model.PrivilegedBackend
import app.hypershell.data.terminal.local.CommandHistoryEntity
import app.hypershell.ui.page.main.widget.settings.SplicedColumnGroup
import app.hypershell.ui.util.getPromptSymbol
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivilegedDashboard(
    viewModel: PrivilegedViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val historyList by viewModel.historyList.collectAsState()

    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    var selectedHistoryItem by remember { mutableStateOf<CommandHistoryEntity?>(null) }
    var isSheetOpen by remember { mutableStateOf(false) }
    var isHistoryExpanded by remember { mutableStateOf(false) }

    // Dialog State
    var showSaveDialog by remember { mutableStateOf(false) }

    // 将历史记录拆分为：[归档的老记录] 和 [最新的一条结果]
    // 这样最新的一条永远展示在外面，不会被折叠
    val sortedHistory = remember(historyList) { historyList.sortedBy { it.id } }
    val archiveHistory = remember(sortedHistory) { if (sortedHistory.isNotEmpty()) sortedHistory.dropLast(1) else emptyList() }
    val latestResult = remember(sortedHistory) { sortedHistory.lastOrNull() }

    // 自动滚动到底部
    LaunchedEffect(historyList.size, uiState.isExecuting) {
        if (uiState.isExecuting || historyList.isNotEmpty()) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        bottomBar = {
            InputArea(
                inputCommand = uiState.inputCommand,
                isExecuting = uiState.isExecuting,
                onInputChange = viewModel::onInputChange,
                onExecute = {
                    viewModel.executeCommand()
                    focusManager.clearFocus()
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
        ) {
            // --- 区域 1: 归档的历史记录 (可折叠) ---
            if (archiveHistory.isNotEmpty()) {
                HistoryHeader(
                    count = archiveHistory.size,
                    isExpanded = isHistoryExpanded,
                    onToggle = { isHistoryExpanded = !isHistoryExpanded }
                )

                AnimatedVisibility(
                    visible = isHistoryExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    SplicedColumnGroup(
                        title = "",
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        archiveHistory.forEach { history ->
                            item(key = history.id) {
                                TerminalItemCard(
                                    backend = history.backend,
                                    command = history.command,
                                    output = history.output,
                                    isStreaming = false,
                                    onClick = {
                                        selectedHistoryItem = history
                                        isSheetOpen = true
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // --- 区域 2: 最新的执行结果 (始终可见) ---
            // 只有当不在执行新命令时才显示这里，否则显示下面的 Active Task
            if (!uiState.isExecuting && latestResult != null) {
                Text(
                    text = "Last Result",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 32.dp, top = 8.dp, bottom = 4.dp)
                )
                SplicedColumnGroup(title = "") {
                    item(key = latestResult.id) {
                        TerminalItemCard(
                            backend = latestResult.backend,
                            command = latestResult.command,
                            output = latestResult.output,
                            isStreaming = false,
                            onClick = {
                                selectedHistoryItem = latestResult
                                isSheetOpen = true
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // --- 区域 3: 当前正在执行的任务 (Active Task) ---
            if (uiState.isExecuting) {
                Text(
                    text = "Active Task",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 32.dp, top = 8.dp, bottom = 4.dp)
                )

                SplicedColumnGroup(title = "") {
                    item(key = "streaming_current") {
                        TerminalItemCard(
                            backend = uiState.activeBackend,
                            command = uiState.inputCommand.ifEmpty { "Running..." },
                            output = uiState.currentStreamOutput,
                            isStreaming = true,
                            onClick = { }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (isSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { isSheetOpen = false },
            sheetState = sheetState
        ) {
            BottomSheetContent(
                selectedItem = selectedHistoryItem,
                onSaveClick = {
                    showSaveDialog = true
                    scope.launch { sheetState.hide() }.invokeOnCompletion { isSheetOpen = false }
                },
                onClose = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion { isSheetOpen = false }
                }
            )
        }
    }

    if (showSaveDialog && selectedHistoryItem != null) {
        SaveCommandDialog(
            initialCommand = selectedHistoryItem!!.command,
            onDismiss = { showSaveDialog = false },
            onConfirm = { name, desc, cmd ->
                viewModel.saveToQuickCommands(name, desc, cmd)
                showSaveDialog = false
            }
        )
    }
}

// ... HistoryHeader, InputArea, TerminalItemCard ... (Keep existing implementation)
// ... 下面是修改后的 BottomSheetContent 和新增的 SaveCommandDialog ...

@Composable
fun BottomSheetContent(
    selectedItem: CommandHistoryEntity?,
    onSaveClick: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
    ) {
        if (selectedItem != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Details",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Add to Quick Command Button
                OutlinedButton(onClick = onSaveClick) {
                    Icon(Icons.Rounded.BookmarkAdd, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                    Text("Save")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            // ... (Rest of existing content)
            Text(
                text = "Command: ${selectedItem.command}",
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceContainerHigh,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                SelectionContainer {
                    Text(
                        text = selectedItem.output,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun SaveCommandDialog(
    initialCommand: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, desc: String, command: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var command by remember { mutableStateOf(initialCommand) }
    var isNameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save to Quick Commands") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        isNameError = false
                    },
                    label = { Text("Name (Required)") },
                    isError = isNameError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = command,
                    onValueChange = { command = it },
                    label = { Text("Command") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        isNameError = true
                    } else {
                        onConfirm(name, description, command)
                    }
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// 补充 HistoryHeader, InputArea, TerminalItemCard 的占位以保证编译（使用你之前提供的代码）
@Composable
fun HistoryHeader(count: Int, isExpanded: Boolean, onToggle: () -> Unit) {
    val rotation by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "arrow")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Archived Records", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(text = "$count", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
            }
        }
        Icon(Icons.Rounded.KeyboardArrowDown, "Toggle", modifier = Modifier.rotate(rotation), tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun InputArea(
    inputCommand: String,
    isExecuting: Boolean,
    onInputChange: (String) -> Unit,
    onExecute: () -> Unit
) { /* Use previous implementation */
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = inputCommand,
            onValueChange = onInputChange,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            placeholder = { Text("Enter command...") },
            singleLine = true,
            enabled = !isExecuting,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onExecute() }),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )
        IconButton(
            onClick = onExecute,
            enabled = !isExecuting && inputCommand.isNotBlank(),
            modifier = Modifier.background(
                color = if (!isExecuting) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
        ) {
            if (isExecuting) CircularProgressIndicator(
                modifier = Modifier.padding(8.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            ) else Icon(Icons.AutoMirrored.Rounded.Send, "Execute", tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TerminalItemCard(
    backend: PrivilegedBackend,
    command: String,
    output: String,
    isStreaming: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isStreaming, onClick = onClick)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(
                "${backend.getPromptSymbol()} ",
                style = MaterialTheme.typography.labelLargeEmphasized,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                command,
                style = MaterialTheme.typography.labelLargeEmphasized,
                color = MaterialTheme.colorScheme.primary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            if (!isStreaming) Icon(
                Icons.Rounded.KeyboardArrowDown,
                "More",
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .rotate(-90f)
                    .height(16.dp)
            )
        }
        if (output.isNotBlank()) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
            SelectionContainer {
                Text(
                    output,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                    color = if (isStreaming) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace,
                    maxLines = if (isStreaming) 100 else 6,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}