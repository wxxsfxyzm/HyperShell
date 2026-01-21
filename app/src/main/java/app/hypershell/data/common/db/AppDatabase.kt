package app.hypershell.data.common.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import app.hypershell.data.common.db.converter.PrivilegedBackendConverter
import app.hypershell.data.terminal.local.CommandHistoryDao
import app.hypershell.data.terminal.local.CommandHistoryEntity
import app.hypershell.data.terminal.local.QuickCommandDao
import app.hypershell.data.terminal.local.QuickCommandEntity

@Database(
    entities = [
        CommandHistoryEntity::class,
        QuickCommandEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(PrivilegedBackendConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun commandHistoryDao(): CommandHistoryDao
    abstract fun quickCommandDao(): QuickCommandDao
}