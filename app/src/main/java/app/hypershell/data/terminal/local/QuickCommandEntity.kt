package app.hypershell.data.terminal.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quick_commands")
data class QuickCommandEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val command: String,
    val timestamp: Long = System.currentTimeMillis()
)