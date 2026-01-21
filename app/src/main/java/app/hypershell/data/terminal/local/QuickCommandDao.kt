package app.hypershell.data.terminal.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface QuickCommandDao {
    @Query("SELECT * FROM quick_commands ORDER BY timestamp DESC")
    fun getAllFlow(): Flow<List<QuickCommandEntity>>

    @Insert
    suspend fun insert(entity: QuickCommandEntity): Long

    @Update
    suspend fun update(entity: QuickCommandEntity)

    @Query("DELETE FROM quick_commands WHERE id = :id")
    suspend fun deleteById(id: Long)
}