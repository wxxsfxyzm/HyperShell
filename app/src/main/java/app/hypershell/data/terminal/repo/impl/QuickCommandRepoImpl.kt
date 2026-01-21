package app.hypershell.data.terminal.repo.impl

import app.hypershell.data.terminal.local.QuickCommandDao
import app.hypershell.data.terminal.local.QuickCommandEntity
import app.hypershell.data.terminal.repo.QuickCommandRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class QuickCommandRepoImpl(
    private val dao: QuickCommandDao
) : QuickCommandRepo {
    override val quickCommands: Flow<List<QuickCommandEntity>> = dao.getAllFlow()
        .flowOn(Dispatchers.IO)

    override suspend fun addCommand(name: String, description: String, command: String) = withContext(Dispatchers.IO) {
        dao.insert(QuickCommandEntity(name = name, description = description, command = command))
        Unit
    }

    override suspend fun updateCommand(entity: QuickCommandEntity) = withContext(Dispatchers.IO) {
        dao.update(entity)
    }

    override suspend fun deleteCommand(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteById(id)
    }
}