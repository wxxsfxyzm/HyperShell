package app.hypershell.data.recycle.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Executes a shell command array locally via [ProcessBuilder].
 */
fun execLocalArr(command: Array<String>): String {
    return try {
        val shellBinary = SHELL_ROOT
        val escapedCommand = command.joinToString(" ") { arg ->
            "'" + arg.replace("'", "'\\''") + "'"
        }

        Timber.d("Executing local shell command: $shellBinary -c $escapedCommand")

        val process = ProcessBuilder(shellBinary, "-c", escapedCommand)
            .redirectErrorStream(true)
            .start()

        val output = process.inputStream.bufferedReader().use { it.readText() }
        val exitCode = process.waitFor()

        if (exitCode != 0) {
            Timber.w("Local command failed with exit code $exitCode: $output")
            "Exit Code $exitCode: $output"
        } else {
            output
        }
    } catch (e: Exception) {
        Timber.e(e, "Failed to execute local array command")
        e.message ?: "Local execution failed"
    }
}

/**
 * Executes a shell command array locally and returns a [Flow] of its output.
 */
fun execLocalArrWithCallback(command: Array<String>): Flow<String> = callbackFlow {
    val shellBinary = SHELL_ROOT
    val escapedCommand = command.joinToString(" ") { arg ->
        "'" + arg.replace("'", "'\\''") + "'"
    }

    Timber.d("Locally executing command with callback: $shellBinary -c $escapedCommand")

    var process: Process? = null
    try {
        process = ProcessBuilder(shellBinary, "-c", escapedCommand)
            .redirectErrorStream(true)
            .start()

        val reader = process.inputStream.bufferedReader()
        var line: String?
        withContext(Dispatchers.IO) {
            while (reader.readLine().also { line = it } != null) {
                trySend(line!!)
            }
        }

        val exitCode = process.waitFor()
        if (exitCode == 0) {
            close()
        } else {
            close(RuntimeException("Command failed with exit code $exitCode"))
        }
    } catch (e: Exception) {
        Timber.e(e, "Failed to execute local command with callback")
        close(e)
    } finally {
        process?.destroy()
    }

    awaitClose {
        process?.destroy()
        Timber.d("Local command flow cancelled.")
    }
}
