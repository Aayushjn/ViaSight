package com.aayush.viasight.util.android

import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.ContactsContract
import android.speech.tts.TextToSpeech
import android.text.format.DateUtils
import android.widget.Toast
import com.aayush.viasight.model.AppInfo
import com.aayush.viasight.model.ContactInfo
import java.util.*

fun PackageManager.getInstalledApps(): List<AppInfo> {
    val res: MutableList<AppInfo> = mutableListOf()
    val packages = getInstalledPackages(0)
    packages.forEach {
        val appInfo = AppInfo(
            it.applicationInfo.loadLabel(this).toString(),
            it.packageName,
            getLaunchIntentForPackage(it.packageName)
        )
        res.add(appInfo)
    }
    return res.sorted().toList()
}

fun ContentResolver.getContacts(): List<ContactInfo> {
    val res: MutableList<ContactInfo> = mutableListOf()
    val cursor: Cursor? = query(
        ContactsContract.Contacts.CONTENT_URI,
        null,
        null,
        null,
        null
    )
    cursor?.let {
        if (it.count > 0) {
            while (it.moveToNext()) {
                val id: String = it.getString(it.getColumnIndex(ContactsContract.Contacts._ID))
                if (it.getInt(it.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    val cursorInfo: Cursor? = query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(id),
                        null
                    )
                    cursorInfo?.let { info ->
                        while(info.moveToNext()) {
                            res.add(
                                ContactInfo(
                                    id,
                                    it.getString(it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)),
                                    info.getString(info.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                                )
                            )
                        }
                    }
                    cursorInfo?.close()
                }
            }
        }
    }
    cursor?.close()
    return res.sorted().toList()
}

fun Context.getVibrator(): Vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

fun Context.toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

fun Vibrator.vibrate(waveform: LongArray) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrate(VibrationEffect.createWaveform(waveform, -1))
    } else {
        vibrate(waveform, -1)
    }
}

fun TextToSpeech.speak(message: String, utteranceId: String) =
    speak(message, TextToSpeech.QUEUE_FLUSH, null, utteranceId)

fun Date.toSpeechString(context: Context): String = DateUtils.formatDateTime(context, time, DateUtils.FORMAT_SHOW_TIME)

inline fun SharedPreferences.edit(func: SharedPreferences.Editor.() -> Unit) {
    edit().apply {
        func()
        apply()
    }
}