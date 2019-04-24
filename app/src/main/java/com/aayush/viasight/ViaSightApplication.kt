package com.aayush.viasight

import android.app.Application
import com.aayush.viasight.util.logging.DebugLogTree
import com.aayush.viasight.util.logging.ReleaseLogTree
import timber.log.Timber

class ViaSightApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.uprootAll()
            Timber.plant(DebugLogTree())
        }
        else {
            Timber.plant(ReleaseLogTree())
        }
    }
}