package app.hypershell.data.terminal.repo

import app.hypershell.data.terminal.local.QuickCommandEntity
import kotlinx.coroutines.flow.Flow

interface QuickCommandRepo {
    val quickCommands: Flow<List<QuickCommandEntity>>
    suspend fun addCommand(name: String, description: String, command: String)
    suspend fun updateCommand(entity: QuickCommandEntity)
    suspend fun deleteCommand(id: Long)
}