package com.aayush.viasight.util

import android.content.SharedPreferences

fun isLaunchedFirstTime(sharedPreferences: SharedPreferences) =
    sharedPreferences.getBoolean(PREF_IS_TUTORIAL_COMPLETED, false)

fun setTutorialCompleted(sharedPreferences: SharedPreferences) =
    sharedPreferences.edit().putBoolean(PREF_IS_TUTORIAL_COMPLETED, true).apply()