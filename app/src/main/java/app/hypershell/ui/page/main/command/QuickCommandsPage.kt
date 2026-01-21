package app.hypershell.ui.page.main.command

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import app.hypershell.data.terminal.local.QuickCommandEntity
import app.hypershell.ui.page.main.widget.settings.SplicedColumnGroup
import org.koin.androidx.compose.koinViewModel

@Composable
fun QuickCommandsPage(
    viewModel: QuickCommandsViewModel = koinViewModel()
) {
    val quickCommands by viewModel.quickCommands.collectAsState()
    val scrollState = rememberScrollState()

    // State for Edit Dialog
    var editingCommand by remember { mutableStateOf<QuickCommandEntity?>(null) }

    // State for Delete Confirmation
    var deletingCommand by remember { mutableStateOf<QuickCommandEntity?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
        ) {
            SplicedColumnGroup(
                title = "Saved Commands",
                modifier = Modifier.padding(top = 16.dp)
            ) {
                if (quickCommands.isEmpty()) {
                    item(key = "empty") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No quick commands saved.\nAdd one from the Terminal history.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    quickCommands.forEach { cmd ->
                        item(key = cmd.id) {
                            QuickCommandItem(
                                entity = cmd,
                                onExecute = { viewModel.executeCommand(cmd.command) },
                                onEdit = { editingCommand = cmd },
                                onDelete = { deletingCommand = cmd }
                            )
                        }
                    }
                }
            }
        }
    }

    // Edit Dialog
    if (editingCommand != null) {
        EditCommandDialog(
            entity = editingCommand!!,
            onDismiss = { editingCommand = null },
            onConfirm = { updated ->
                viewModel.updateCommand(updated)
                editingCommand = null
            }
        )
    }

    // Delete Dialog
    if (deletingCommand != null) {
        AlertDialog(
            onDismissRequest = { deletingCommand = null },
            title = { Text("Delete Command") },
            text = { Text("Are you sure you want to delete '${deletingCommand?.name}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        deletingCommand?.let { viewModel.deleteCommand(it.id) }
                        deletingCommand = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingCommand = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun QuickCommandItem(
    entity: QuickCommandEntity,
    onExecute: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onExecute) // Clicking the card executes it? Or opens details? Let's say click card executes.
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entity.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (entity.description.isNotEmpty()) {
                Text(
                    text = entity.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = entity.command,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }

        // Actions
        Row {
            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Rounded.Edit,
                    contentDescription = "Edit",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Rounded.Delete,
                    contentDescription = "Delete",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
            FilledIconButton(
                onClick = onExecute,
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    Icons.Rounded.PlayArrow,
                    contentDescription = "Run",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun EditCommandDialog(
    entity: QuickCommandEntity,
    onDismiss: () -> Unit,
    onConfirm: (QuickCommandEntity) -> Unit
) {
    var name by remember { mutableStateOf(entity.name) }
    var description by remember { mutableStateOf(entity.description) }
    var command by remember { mutableStateOf(entity.command) }
    var isNameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Command") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; isNameError = false },
                    label = { Text("Name") },
                    isError = isNameError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
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
            Button(onClick = {
                if (name.isBlank()) {
                    isNameError = true
                } else {
                    onConfirm(entity.copy(name = name, description = description, command = command))
                }
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}