package app.hypershell.data.settings.model

enum class PrivilegedBackend(val key: String) {
    // 定义枚举项及其对应的存储值
    SHIZUKU("shizuku"),
    ROOT("root");

    companion object {
        // 辅助方法：通过存储的字符串找到对应的枚举，找不到则返回默认值
        fun fromKey(key: String): PrivilegedBackend {
            return entries.find { it.key == key } ?: SHIZUKU
        }
    }
}