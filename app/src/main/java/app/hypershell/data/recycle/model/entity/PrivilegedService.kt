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
    }

    @Throws(RemoteException::class)
    override fun execArr(command: Array<String>): String {
        return try {
            // Execute shell command
            val process = Runtime.getRuntime().exec(command)
            // Read execution result
            readResult(process)
        } catch (e: IOException) {
            // Wrap IOException in RemoteException and throw
            throw RemoteException(e.message)
        } catch (e: InterruptedException) {
            // Restore thread's interrupted status
            Thread.currentThread().interrupt()
            // Wrap InterruptedException in RemoteException and throw
            throw RemoteException(e.message)
        }
    }

    @Throws(RemoteException::class)
    override fun execArrWithCallback(command: Array<String>, listener: ICommandOutputListener?) {
        if (listener == null) {
            // If no listener is provided, we can't stream output.
            // You could either throw an exception or just execute without feedback.
            Log.w(TAG, "execArrWithCallback called with a null listener.")
            return
        }

        var process: Process? = null
        try {
            process = Runtime.getRuntime().exec(command)

            // Thread to read standard output
            val stdoutThread = Thread {
                try {
                    process.inputStream.bufferedReader(StandardCharsets.UTF_8).forEachLine {
                        listener.onOutput(it)
                    }
                } catch (e: Exception) {
                    if (e is IOException || e is RemoteException) {
                        Log.e(TAG, "Error reading stdout or sending callback", e)
                    }
                }
            }

            // Thread to read standard error
            val stderrThread = Thread {
                try {
                    process.errorStream.bufferedReader(StandardCharsets.UTF_8).forEachLine {
                        listener.onError(it)
                    }
                } catch (e: Exception) {
                    if (e is IOException || e is RemoteException) {
                        Log.e(TAG, "Error reading stderr or sending callback", e)
                    }
                }
            }

            stdoutThread.start()
            stderrThread.start()

            // Wait for the process to complete
            val exitCode = process.waitFor()
            process.destroy()

            // Wait for reader threads to finish to ensure all output is captured
            stdoutThread.join()
            stderrThread.join()

            // Notify client that the process is complete
            listener.onComplete(exitCode)

        } catch (e: Exception) {
            // If process creation itself fails
            val errorMessage = "Failed to execute command: ${e.message}"
            Log.e(TAG, errorMessage, e)
            try {
                listener.onError(errorMessage)
                listener.onComplete(-1) // Send a failure exit code
            } catch (re: RemoteException) {
                // The client might be dead, just log it.
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

    @Throws(IOException::class, InterruptedException::class)
    private fun readResult(process: Process): String {
        // Read standard output and standard error respectively
        val output = process.inputStream.bufferedReader().use { it.readText() }
        val error = process.errorStream.bufferedReader().use { it.readText() }

        // Wait for command execution to complete and get the exit code
        val exitCode = process.waitFor()

        // Check exit code. 0 typically represents success, any non-zero value represents failure.
        if (exitCode != 0) {
            // If it failed, construct a detailed error message and throw IOException
            // This way the catch block in the execArr method can catch it and convert it to RemoteException
            throw IOException("Command execution failed, exit code: $exitCode, error message: '$error'")
        }

        // If successful, return the content of standard output
        return output
    }
}
