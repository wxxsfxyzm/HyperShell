package app.hypershell.data.terminal.repo

import app.hypershell.data.terminal.local.CommandHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * 终端数据仓库接口
 * 定义了 ViewModel 所需的所有数据操作标准
 */
interface TerminalRepo {
    // 获取历史记录流
    val commandHistory: Flow<List<CommandHistoryEntity>>

    // 创建新记录
    suspend fun createHistory(command: String): Long

    // 更新输出内容
    suspend fun updateOutput(id: Long, fullOutput: String)

    // 删除单条记录
    suspend fun deleteHistory(id: Long)

    // 清空所有记录
    suspend fun clearAllHistory()
}