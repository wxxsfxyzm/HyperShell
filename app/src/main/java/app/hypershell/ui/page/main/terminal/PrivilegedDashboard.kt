package app.hypershell.ui.page.main.terminal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel

@Composable
fun PrivilegedDashboard(
    viewModel: PrivilegedViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val historyList by viewModel.historyList.collectAsState()
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current

    // Auto-scroll to bottom when new history is added or output streams
    LaunchedEffect(historyList.size, uiState.currentStreamOutput) {
        if (historyList.isNotEmpty()) {
            listState.animateScrollToItem(0) // Since we reverse the list in UI, 0 is the "bottom" logically if distinct
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding() // Handle software keyboard
            .padding(16.dp)
    ) {
        // --- Output & History Area ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(8.dp)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                reverseLayout = true, // Newest items at the bottom (visually standard for terminal)
                verticalArrangement = Arrangement.Top
            ) {
                // 1. Show the currently executing command (Streaming state)
                if (uiState.isExecuting) {
                    item {
                        TerminalItem(
                            command = uiState.inputCommand.ifEmpty { "Running..." }, // Fallback text
                            output = uiState.currentStreamOutput,
                            isStreaming = true
                        )
                    }
                }

                // 2. Show past history
                items(historyList) { history ->
                    // Avoid duplicating the item if it's the one currently being updated in DB background
                    // (Though our VM logic separates streaming state from DB list mostly)
                    if (history.id != uiState.currentRunningId) {
                        TerminalItem(
                            command = history.command,
                            output = history.output,
                            isStreaming = false
                        )
                    }
                }
            }
        }

        // --- Input Area ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = uiState.inputCommand,
                onValueChange = viewModel::onInputChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                placeholder = { Text("Enter command (e.g., 'ls -la')") },
                singleLine = true,
                enabled = !uiState.isExecuting,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    viewModel.executeCommand()
                    focusManager.clearFocus()
                }),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            IconButton(
                onClick = {
                    viewModel.executeCommand()
                    focusManager.clearFocus()
                },
                enabled = !uiState.isExecuting && uiState.inputCommand.isNotBlank(),
                modifier = Modifier
                    .background(
                        color = if (!uiState.isExecuting) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                if (uiState.isExecuting) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(8.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Send,
                        contentDescription = "Execute",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun TerminalItem(
    command: String,
    output: String,
    isStreaming: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Command Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$ ",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.secondary,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = command,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontFamily = FontFamily.Monospace
            )
        }

        // Output Content
        SelectionContainer {
            Text(
                text = output,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                ),
                color = if (isStreaming) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, top = 4.dp)
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(top = 8.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    }
}