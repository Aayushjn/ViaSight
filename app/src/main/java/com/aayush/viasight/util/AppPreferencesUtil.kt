package com.aayush.viasight.util

import android.content.SharedPreferences

fun isLaunchedFirstTime(sharedPreferences: SharedPreferences) =
    sharedPreferences.getBoolean(PREF_IS_TUTORIAL_COMPLETED, true)

fun setLaunchedFirstTime(sharedPreferences: SharedPreferences) =
    sharedPreferences.edit().putBoolean(PREF_IS_TUTORIAL_COMPLETED, false).apply()