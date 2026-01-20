package app.hypershell.di

import app.hypershell.data.settings.repo.SettingsRepo
import app.hypershell.data.settings.repo.impl.SettingsRepoImpl
import org.koin.dsl.module

val settingsModule = module {
    single<SettingsRepo> { SettingsRepoImpl(get()) }
}