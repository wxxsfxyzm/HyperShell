package app.hypershell.di

import app.hypershell.data.settings.repo.SettingsRepo
import app.hypershell.ui.activity.MainViewModel
import app.hypershell.ui.page.main.settings.SettingsViewModel
import app.hypershell.ui.page.main.settings.appearance.AppearanceViewModel
import app.hypershell.ui.page.main.settings.terminal.TerminalSettingsViewModel
import app.hypershell.ui.page.main.terminal.PrivilegedViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { MainViewModel(get()) }      // 给 Activity 用
    viewModel { SettingsViewModel(get()) }  // 给 Settings Page 用
    viewModel { AppearanceViewModel(get()) }
    viewModel { TerminalSettingsViewModel(get()) }
    // PrivilegedViewModel needs TerminalRepo and SettingsRepo
    viewModel { PrivilegedViewModel(get(), get<SettingsRepo>()) }
}