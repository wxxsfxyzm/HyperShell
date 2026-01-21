package app.hypershell.data.terminal.repo.impl

import app.hypershell.data.settings.model.PrivilegedBackend
import app.hypershell.data.terminal.local.CommandHistoryDao
import app.hypershell.data.terminal.local.CommandHistoryEntity
import app.hypershell.data.terminal.repo.TerminalRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * 终端数据仓库的具体实现
 * 负责与 Room 数据库 (DAO) 进行交互
 */
class TerminalRepoImpl(
    private val historyDao: CommandHistoryDao
) : TerminalRepo {

    override val commandHistory: Flow<List<CommandHistoryEntity>> = historyDao.getAllFlow()
        .flowOn(Dispatchers.IO)

    override suspend fun createHistory(command: String, backend: PrivilegedBackend): Long = withContext(Dispatchers.IO) {
        val entity = CommandHistoryEntity(command = command, backend = backend)
        historyDao.insert(entity)
    }

    override suspend fun updateOutput(id: Long, fullOutput: String) = withContext(Dispatchers.IO) {
        historyDao.updateOutput(id, fullOutput)
    }

    override suspend fun deleteHistory(id: Long) = withContext(Dispatchers.IO) {
        historyDao.deleteById(id)
    }

    override suspend fun clearAllHistory() = withContext(Dispatchers.IO) {
        historyDao.clearAll()
    }
}