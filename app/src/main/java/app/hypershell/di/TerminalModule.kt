package app.hypershell.di

import app.hypershell.data.common.db.AppDatabase
import app.hypershell.data.terminal.local.CommandHistoryDao
import app.hypershell.data.terminal.local.QuickCommandDao
import app.hypershell.data.terminal.repo.QuickCommandRepo
import app.hypershell.data.terminal.repo.TerminalRepo
import app.hypershell.data.terminal.repo.impl.QuickCommandRepoImpl
import app.hypershell.data.terminal.repo.impl.TerminalRepoImpl
import org.koin.dsl.module

val terminalModule = module {
    // --- 1. DAO (Data Access Objects) ---
    // 现有的历史记录 DAO
    single<CommandHistoryDao> {
        get<AppDatabase>().commandHistoryDao()
    }

    // [新增] 快速指令 DAO
    single<QuickCommandDao> {
        get<AppDatabase>().quickCommandDao()
    }

    // --- 2. Repository (Single Source of Truth) ---
    // 现有的终端历史仓库
    single<TerminalRepo> {
        TerminalRepoImpl(historyDao = get())
    }

    // [新增] 快速指令仓库
    single<QuickCommandRepo> {
        QuickCommandRepoImpl(dao = get())
    }
}