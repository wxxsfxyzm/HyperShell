package app.hypershell.di.init

import app.hypershell.di.databaseModule
import app.hypershell.di.datastoreModule
import app.hypershell.di.settingsModule
import app.hypershell.di.terminalModule
import app.hypershell.di.viewModelModule

val appModules = listOf(
    databaseModule,
    datastoreModule,
    settingsModule,
    terminalModule,
    viewModelModule
)