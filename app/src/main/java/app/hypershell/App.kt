package app.hypershell

import android.app.Application
import app.hypershell.di.init.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        // HiddenApiBypass.addHiddenApiExemptions("")

        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())

        startKoin {
            // Koin Android Logger
            androidLogger()
            // Koin Android Context
            androidContext(this@App)
            // use modules
            modules(appModules)
        }
    }
}
