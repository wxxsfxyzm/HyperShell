package app.hypershell.data.terminal.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for command history.
 */
@Dao
interface CommandHistoryDao {
    @Query("SELECT * FROM terminal_history ORDER BY timestamp DESC")
    fun getAllFlow(): Flow<List<CommandHistoryEntity>>

    @Insert
    suspend fun insert(entity: CommandHistoryEntity): Long

    @Update
    suspend fun update(entity: CommandHistoryEntity)

    @Query("UPDATE terminal_history SET output = :output WHERE id = :id")
    suspend fun updateOutput(id: Long, output: String)

    // 删除单条记录
    @Query("DELETE FROM terminal_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    // 清空所有记录
    @Query("DELETE FROM terminal_history")
    suspend fun clearAll()
}