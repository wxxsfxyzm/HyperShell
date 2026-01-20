package app.hypershell.data.recycle.model.impl.recycler

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import androidx.annotation.Keep
import app.hypershell.IUserService
import app.hypershell.data.recycle.model.entity.PrivilegedService
import app.hypershell.data.recycle.repo.Recycler
import app.hypershell.data.recycle.repo.recyclable.UserService
import app.hypershell.data.recycle.util.requireShizukuPermissionGranted
import app.hypershell.di.init.processModules
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.koin.androidContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import rikka.shizuku.Shizuku
import kotlin.system.exitProcess

object ShizukuUserServiceRecycler : Recycler<ShizukuUserServiceRecycler.UserServiceProxy>(),
    KoinComponent {
    class UserServiceProxy(val service: IUserService) : UserService {
        override val privileged: IUserService = service

        override fun close() = service.destroy()
    }

    class ShizukuUserService @Keep constructor(context: Context) : IUserService.Stub() {
        init {
            startKoin {
                modules(processModules)
                androidContext(context)
            }
        }

        private val privileged = PrivilegedService()

        override fun execArr(command: Array<String>): String = privileged.execArr(command)

        override fun execArrWithCallback(
            command: Array<String>,
            listener: app.hypershell.ICommandOutputListener?
        ) = privileged.execArrWithCallback(command, listener)

        override fun destroy() {
            exitProcess(0)
        }
    }

    private val context by inject<Context>()

    override fun onMake(): UserServiceProxy = runBlocking {
        requireShizukuPermissionGranted {
            onInnerMake()
        }
    }

    private suspend fun onInnerMake(): UserServiceProxy = callbackFlow {
        Shizuku.bindUserService(
            Shizuku.UserServiceArgs(
                ComponentName(
                    context, ShizukuUserService::class.java
                )
            ).processNameSuffix("shizuku_privileged"), object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    trySend(UserServiceProxy(IUserService.Stub.asInterface(service)))
                    service?.linkToDeath({
                        if (entity?.service?.asBinder() == service) recycleForcibly()
                    }, 0)
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                    close()
                }
            })
        awaitClose { }
    }.first()
}
