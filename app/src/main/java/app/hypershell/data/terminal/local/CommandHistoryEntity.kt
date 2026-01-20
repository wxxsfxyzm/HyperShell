package app.hypershell.data.terminal.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a single command execution record.
 * Stores the command input, the full output, and the execution timestamp.
 */
@Entity(tableName = "terminal_history")
data class CommandHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val command: String,
    val output: String = "",
    val timestamp: Long = System.currentTimeMillis()
)