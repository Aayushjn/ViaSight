package com.aayush.viasight

import android.app.Application
import android.content.res.Configuration
import android.os.Build
import com.aayush.viasight.util.logging.DebugLogTree
import com.aayush.viasight.util.logging.ReleaseLogTree
import timber.log.Timber
import java.util.*

class ViaSightApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.uprootAll()
            Timber.plant(DebugLogTree())
        } else {
            Timber.plant(ReleaseLogTree())
        }

        defaultLocale = Locale.getDefault()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        defaultLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            newConfig.locales[0]
        } else {
            newConfig.locale
        }
    }

    companion object {
        lateinit var defaultLocale: Locale
            private set
    }
}