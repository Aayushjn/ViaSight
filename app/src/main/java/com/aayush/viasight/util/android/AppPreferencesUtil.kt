package com.aayush.viasight.util.android

import android.content.SharedPreferences
import com.aayush.viasight.model.NotificationInfo
import com.aayush.viasight.util.common.JSON
import com.aayush.viasight.util.common.PREF_IS_TUTORIAL_COMPLETED
import com.aayush.viasight.util.common.PREF_NOTIFICATIONS
import kotlinx.serialization.set

fun isLaunchedFirstTime(sharedPreferences: SharedPreferences) =
    sharedPreferences.getBoolean(PREF_IS_TUTORIAL_COMPLETED, true)

fun setLaunchedFirstTime(sharedPreferences: SharedPreferences) = sharedPreferences.edit {
    putBoolean(PREF_IS_TUTORIAL_COMPLETED, false)
}

fun saveNotifications(sharedPreferences: SharedPreferences, notifications: Set<NotificationInfo>) =
    sharedPreferences.edit {
        putString(
            PREF_NOTIFICATIONS,
            JSON.stringify(NotificationInfo.serializer().set, notifications)
        )
    }

fun getNotifications(sharedPreferences: SharedPreferences): MutableSet<NotificationInfo> {
    val storedValue: String = sharedPreferences.getString(PREF_NOTIFICATIONS, "")!!
    return if (storedValue != "") {
        JSON.parse(NotificationInfo.serializer().set, storedValue).toMutableSet()
    } else {
        mutableSetOf()
    }
}