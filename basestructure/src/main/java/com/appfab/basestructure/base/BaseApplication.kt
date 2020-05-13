package com.appfab.basestructure.base

import android.app.Dialog
import androidx.multidex.MultiDexApplication
import com.appfab.basestructure.BuildConfig
import com.appfab.basestructure.utils.CacheManager
import com.appfab.basestructure.utils.DialogModule
import com.appfab.basestructure.utils.ProgressDialogModule
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.get
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.math.sin

abstract class BaseApplication : KoinComponent, MultiDexApplication() {
    val cacheManager: CacheManager by inject()

    private val progressModule = module {
        single { ProgressDialogModule() }
    }

    private val dialogModule = module {
        scope(named("dialog_scope")) {
            scoped { DialogModule() }
        }
    }

    private val cachemanagerModule = module {
        single { CacheManager(androidContext(), getPrefsName()) }
    }

    open fun getPrefsName(): String {
        return "prefs_${BuildConfig.APPLICATION_ID}"
    }

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(applicationContext)
            modules(listOf(progressModule, dialogModule, cachemanagerModule))
        }
    }

}