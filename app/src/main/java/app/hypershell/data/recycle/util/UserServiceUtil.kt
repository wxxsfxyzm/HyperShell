package app.hypershell.data.recycle.util

import app.hypershell.data.recycle.model.exception.ShizukuNotWorkException
import app.hypershell.data.recycle.model.impl.recycler.ShizukuUserServiceRecycler
import app.hypershell.data.recycle.repo.recyclable.UserService
import timber.log.Timber

private const val TAG = "PrivilegedService"

/**
 * Executes an action using Shizuku's UserService.
 * This is specifically for Shizuku backend. Root backend runs locally and doesn't need this.
 */
fun useShizukuUserService(
    action: (UserService) -> Unit
) {
    val recycler = ShizukuUserServiceRecycler.make()
    try {
        Timber.tag(TAG).d("Processing Shizuku with recycler: ShizukuUserServiceRecycler")
        recycler.use { action.invoke(it.entity) }
    } catch (e: IllegalStateException) {
        if (e.message?.contains("binder haven't been received") == true) {
            throw ShizukuNotWorkException("Shizuku service connection lost during privileged action.", e)
        }
        throw e
    }
}
