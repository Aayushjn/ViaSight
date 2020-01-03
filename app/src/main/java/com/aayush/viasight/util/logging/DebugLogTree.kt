package com.aayush.viasight.util.logging

import android.os.Build
import android.util.Log
import timber.log.Timber.DebugTree

class DebugLogTree: DebugTree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        var updatedPriority = priority
        // Workaround for devices that doesn't show lower priority logs
        if (Build.MANUFACTURER == "HUAWEI" || Build.MANUFACTURER == "samsung") {
            if (updatedPriority == Log.VERBOSE || updatedPriority == Log.DEBUG || updatedPriority == Log.INFO)
                updatedPriority = Log.ERROR
        }
        super.log(updatedPriority, tag, message, t)
    }

    override fun createStackElementTag(element: StackTraceElement): String? =
        "Class: ${super.createStackElementTag(element)}: Line: ${element.lineNumber}, Method: ${element.methodName}"
}