package com.aayush.viasight.util

const val PERMISSION_RECORD_AUDIO = 100
const val PERMISSION_SETTINGS_REQUEST = 101
const val PERMISSION_CALL_PHONE = 102

const val INTENT_ACTION_NOTIFICATION = "NotificationIntentActionFilter"

const val EXTRA_NOTIFICATION = "NotificationExtra"

const val UTTERANCE_ID_NOTIFICATION = "NotificationUtteranceId"
const val UTTERANCE_ID_TUTORIAL = "TutorialUtteranceId"
const val UTTERANCE_ID_DATE_TIME = "DateTimeUtteranceId"
const val UTTERANCE_ID_MISC = "MiscUtteranceId"

const val MIN_SWIPE_DISTANCE_X = 100f
const val MAX_SWIPE_DISTANCE_X = 1000f

const val PREF_IS_TUTORIAL_COMPLETED = "IsTutorialCompletedPreference"
const val PREF_NOTIFICATIONS = "NotificationsPreference"

val POSITIVE_WAVEFORM = longArrayOf(0, 150, 300, 150, 0)
val NEGATIVE_WAVEFORM = longArrayOf(0, 150, 0, 150, 0)
val NOTIFICATION_WAVEFORM = longArrayOf(0, 400, 200, 400, 0)