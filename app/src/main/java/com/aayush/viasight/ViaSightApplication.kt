package com.aayush.viasight

import android.app.Application
import com.aayush.viasight.util.NoLogTree
import timber.log.Timber

class ViaSightApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        else {
            Timber.plant(NoLogTree())
        }
    }
}