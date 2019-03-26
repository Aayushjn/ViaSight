package com.aayush.viasight.util

import android.content.pm.PackageManager
import com.aayush.viasight.model.AppInfo

fun getInstalledApps(packageManager: PackageManager): List<AppInfo> {
    val res = mutableListOf<AppInfo>()
    val applications = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
    for (app in applications) {
        if (packageManager.getLaunchIntentForPackage(app.packageName) == null || app.name == null) {
            continue
        }
        val appInfo = AppInfo()
        appInfo.appName = app.name.substring(app.name.lastIndexOf(".") + 1)
        appInfo.packageName = app.packageName
        appInfo.icon = app.loadIcon(packageManager)!!
        appInfo.launchIntent = packageManager.getLaunchIntentForPackage(app.packageName)
        res.add(appInfo)
    }
    return res
}