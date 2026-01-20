package app.hypershell.di

import androidx.room.Room
import app.hypershell.data.common.db.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single<AppDatabase> {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "hypershell.db"
        )
            .fallbackToDestructiveMigration(true) // 开发阶段允许破坏性迁移，正式版要去掉
            .build()
    }
}