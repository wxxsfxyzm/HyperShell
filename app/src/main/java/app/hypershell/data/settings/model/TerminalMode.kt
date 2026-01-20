package app.hypershell.data.settings.model

enum class TerminalMode(val key: String) {
    // 特权命令模式 (单次执行，无交互，走 Recycler/AIDL)
    PRIVILEGED("privileged"),

    // 自由终端模式 (交互式 Shell，走 PTY/JNI)
    INTERACTIVE("interactive");

    companion object {
        fun fromKey(key: String): TerminalMode {
            return entries.find { it.key == key } ?: PRIVILEGED
        }
    }
}