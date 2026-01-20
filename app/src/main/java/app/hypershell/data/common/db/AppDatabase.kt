package app.hypershell.data.common.db

import androidx.room.Database
import androidx.room.RoomDatabase
import app.hypershell.data.terminal.local.CommandHistoryDao
import app.hypershell.data.terminal.local.CommandHistoryEntity

@Database(
    entities = [CommandHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun commandHistoryDao(): CommandHistoryDao
}