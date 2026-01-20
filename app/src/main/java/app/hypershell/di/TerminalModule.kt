package app.hypershell.di

import app.hypershell.data.common.db.AppDatabase
import app.hypershell.data.terminal.local.CommandHistoryDao
import app.hypershell.data.terminal.repo.TerminalRepo
import app.hypershell.data.terminal.repo.impl.TerminalRepositoryImpl
import org.koin.dsl.module

val terminalModule = module {
    // 1. DAO (Data Source)
    single<CommandHistoryDao> {
        get<AppDatabase>().commandHistoryDao()
    }

    // 2. Repository (Single Source of Truth)
    // It depends on the DAO
    single<TerminalRepo> {
        TerminalRepositoryImpl(historyDao = get())
    }
}