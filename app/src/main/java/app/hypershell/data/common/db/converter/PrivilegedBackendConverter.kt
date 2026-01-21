package app.hypershell.data.common.db.converter

import androidx.room.TypeConverter
import app.hypershell.data.settings.model.PrivilegedBackend

class PrivilegedBackendConverter {

    @TypeConverter
    fun restore(value: String?): PrivilegedBackend {
        // 利用你已有的 fromKey 方法，它包含了默认值逻辑 (?: SHIZUKU)
        // 如果数据库里存的是 null，也返回默认值
        return value?.let { PrivilegedBackend.fromKey(it) } ?: PrivilegedBackend.SHIZUKU
    }

    @TypeConverter
    fun save(backend: PrivilegedBackend): String {
        // 存储时使用 key ("shizuku" 或 "root")
        return backend.key
    }
}