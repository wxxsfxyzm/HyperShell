package app.hypershell.data.recycle.model.entity

import android.annotation.SuppressLint
import android.os.RemoteException
import android.util.Log
import app.hypershell.ICommandOutputListener
import app.hypershell.IUserService
import org.koin.core.component.KoinComponent
import java.io.IOException
import java.nio.charset.StandardCharsets

@SuppressLint("LogNotTimber")
class PrivilegedService() : IUserService.Stub(), KoinComponent {
    companion object {
        private const val TAG = "PrivilegedService"
        private const val SHELL_PATH = "/system/bin/sh"
    }

    @Throws(RemoteException::class)
    override fun execArr(command: Array<String>): String {
        val shellCmd = arrayOf(SHELL_PATH, "-c", command.toShellCommand())

        return try {
            val process = Runtime.getRuntime().exec(shellCmd)
            readResult(process)
        } catch (e: IOException) {
            throw RemoteException(e.message)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            throw RemoteException(e.message)
        }
    }

    @Throws(RemoteException::class)
    override fun execArrWithCallback(command: Array<String>, listener: ICommandOutputListener?) {
        if (listener == null) {
            Log.w(TAG, "execArrWithCallback called with a null listener.")
            return
        }

        var process: Process? = null
        try {
            // [修改 2]：改为通过 Shell 执行
            // 拼接原参数为完整字符串
            val rawCommandString = command.toShellCommand()
            // 包装进 sh -c
            val wrappedCommand = arrayOf(SHELL_PATH, "-c", rawCommandString)

            process = Runtime.getRuntime().exec(wrappedCommand)

            // Thread to read standard output
            val stdoutThread = Thread {
                try {
                    process.inputStream.bufferedReader(StandardCharsets.UTF_8).forEachLine {
                        listener.onOutput(it)
                    }
                } catch (e: Exception) {
                    if (e !is IOException && e !is RemoteException) {
                        Log.e(TAG, "Error reading stdout", e)
                    }
                }
            }

            // Thread to read standard error
            val stderrThread = Thread {
                try {
                    process.errorStream.bufferedReader(StandardCharsets.UTF_8).forEachLine {
                        // 这里直接透传 Shell 的 stderr
                        // 如果命令不存在，Shell 会自己往这里打印 "inaccessible or not found"
                        listener.onError(it)
                    }
                } catch (e: Exception) {
                    if (e !is IOException && e !is RemoteException) {
                        Log.e(TAG, "Error reading stderr", e)
                    }
                }
            }

            stdoutThread.start()
            stderrThread.start()

            val exitCode = process.waitFor()
            process.destroy()

            stdoutThread.join()
            stderrThread.join()

            listener.onComplete(exitCode)

        } catch (e: Exception) {
            // 此时几乎不会抛出 IOException，因为 /system/bin/sh 肯定是存在的。
            // 任何命令找不到的错误都会由 shell 进程内部处理并输出到 stderr，而不是抛出 Java 异常。
            val errorMessage = "Failed to spawn shell: ${e.message}"
            Log.e(TAG, errorMessage, e)
            try {
                listener.onError(errorMessage)
                listener.onComplete(-1)
            } catch (re: RemoteException) {
                Log.e(TAG, "Failed to send execution error to client.", re)
            }
        } finally {
            process?.destroy()
        }
    }

    override fun destroy() {
        // 如果需要，可以在这里执行清理操作
        Log.d(TAG, "PrivilegedService destroy called")
    }

    private fun Array<String>.toShellCommand(): String {
        return this.joinToString(" ") { arg ->
            // 如果参数包含空格或特殊字符，加上单引号保护（简单处理）
            if (arg.contains(" ") || arg.contains("'") || arg.contains("\"")) {
                "'" + arg.replace("'", "'\\''") + "'"
            } else {
                arg
            }
        }
    }

    @Throws(IOException::class, InterruptedException::class)
    private fun readResult(process: Process): String {
        // Read standard output and standard error respectively
        val output = process.inputStream.bufferedReader().use { it.readText() }
        val error = process.errorStream.bufferedReader().use { it.readText() }

        // Wait for command execution to complete and get the exit code
        val exitCode = process.waitFor()

        // Check exit code. 0 typically represents success, any non-zero value represents failure.
        if (exitCode != 0) {
            // 这里也一样，Shell 的报错在 error 字符串里
            throw IOException(error.ifBlank { "Exit code: $exitCode" })
        }

        // If successful, return the content of standard output
        return output
    }
}
