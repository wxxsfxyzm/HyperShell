package app.hypershell.data.recycle.model.impl

import app.hypershell.ICommandOutputListener
import app.hypershell.data.recycle.util.execLocalArr
import app.hypershell.data.recycle.util.execLocalArrWithCallback
import app.hypershell.data.recycle.util.useShizukuUserService
import app.hypershell.data.settings.model.PrivilegedBackend
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.koin.core.component.KoinComponent
import timber.log.Timber

/**
 * A centralized manager for handling all privileged actions.
 *
 * This singleton object implements [KoinComponent] to self-inject dependencies
 * like [app.hypershell.data.settings.local.AppDataStore], without the need for the UI layer to pass configuration
 * flags manually.
 */
object PrivilegedManager : KoinComponent {

    /**
     * Executes a shell command array (safer).
     */
    fun execArr(backend: PrivilegedBackend, command: Array<String>): String {
        if (backend == PrivilegedBackend.ROOT) {
            return execLocalArr(command)
        }

        var result = ""
        useShizukuUserService {
            try {
                result = it.privileged.execArr(command)
            } catch (e: Exception) {
                Timber.e(e, "Failed to execute array command via IPC: ${command.joinToString(" ")}")
                result = e.message ?: "Execution failed"
            }
        }
        return result
    }

    /**
     * Executes a shell command array with real-time output via Flow.
     */
    fun execArrWithCallback(
        backend: PrivilegedBackend,
        command: Array<String>
    ): Flow<String> {
        if (backend == PrivilegedBackend.ROOT) {
            return execLocalArrWithCallback(command)
        }

        return callbackFlow {
            val listener = object : ICommandOutputListener.Stub() {
                override fun onOutput(line: String) {
                    trySend(line)
                }

                override fun onError(line: String) {
                    trySend(line)
                }

                override fun onComplete(exitCode: Int) {
                    if (exitCode == 0) {
                        close()
                    } else {
                        close(RuntimeException("Remote command failed with exit code $exitCode"))
                    }
                }
            }

            try {
                useShizukuUserService { userService ->
                    userService.privileged.execArrWithCallback(command, listener)
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to initiate remote command execution.")
                close(e)
            }

            awaitClose {
                Timber.d("Remote command flow cancelled.")
            }
        }
    }
}
