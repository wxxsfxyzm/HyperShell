package app.hypershell.di

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import app.hypershell.data.settings.local.AppDataStore
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val datastoreModule = module {
    single {
        PreferenceDataStoreFactory.create() {
            androidContext().preferencesDataStoreFile("app_settings")
        }
    }

    single {
        AppDataStore(get())
    }
}