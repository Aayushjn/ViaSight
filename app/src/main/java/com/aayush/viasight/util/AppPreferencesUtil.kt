package com.aayush.viasight.util

import android.content.SharedPreferences
import com.aayush.viasight.model.NotificationInfo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


fun isLaunchedFirstTime(sharedPreferences: SharedPreferences) =
    sharedPreferences.getBoolean(PREF_IS_TUTORIAL_COMPLETED, true)

fun setLaunchedFirstTime(sharedPreferences: SharedPreferences) =
    sharedPreferences.edit().putBoolean(PREF_IS_TUTORIAL_COMPLETED, false).apply()

fun saveNotifications(sharedPreferences: SharedPreferences, notifications: Set<NotificationInfo>) =
    sharedPreferences.edit().putString(PREF_NOTIFICATIONS, Gson().toJson(notifications.toList())).apply()

fun getNotifications(sharedPreferences: SharedPreferences) =
    if (sharedPreferences.getString(PREF_NOTIFICATIONS, "") != "") {
        Gson().fromJson<List<NotificationInfo>>(
            sharedPreferences.getString(PREF_NOTIFICATIONS, ""),
            object: TypeToken<List<NotificationInfo>>(){}.type
        ).toMutableSet()
    } else {
        mutableSetOf()
    }