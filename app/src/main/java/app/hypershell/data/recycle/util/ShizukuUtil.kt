package app.hypershell.data.recycle.util

import android.content.AttributionSource
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.os.Process
import app.hypershell.BuildConfig
import app.hypershell.data.recycle.model.exception.ShizukuNotWorkException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import rikka.shizuku.Shizuku
import rikka.sui.Sui

suspend fun <T> requireShizukuPermissionGranted(action: suspend () -> T): T {
    callbackFlow {
        Sui.init(BuildConfig.APPLICATION_ID)
        if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            send(Unit)
            awaitClose()
        } else {
            val requestCode = (Int.MIN_VALUE..Int.MAX_VALUE).random()
            val listener =
                Shizuku.OnRequestPermissionResultListener { _requestCode, grantResult ->
                    if (_requestCode != requestCode) return@OnRequestPermissionResultListener
                    if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED)
                        trySend(Unit)
                    else close(Exception("sui/shizuku permission denied"))
                }
            Shizuku.addRequestPermissionResultListener(listener)
            Shizuku.requestPermission(requestCode)
            awaitClose { Shizuku.removeRequestPermissionResultListener(listener) }
        }
    }.catch {
        throw ShizukuNotWorkException(it)
    }.first()

    return action()
}

class ShizukuContext(base: Context) : ContextWrapper(base) {
    override fun getOpPackageName(): String {
        return "com.android.shell"
    }

    override fun getAttributionSource(): AttributionSource {
        val shellUid = Shizuku.getUid()
        val builder = AttributionSource.Builder(shellUid)
            .setPackageName("com.android.shell")

        builder.setPid(Process.INVALID_PID)

        return builder.build()
    }
}
