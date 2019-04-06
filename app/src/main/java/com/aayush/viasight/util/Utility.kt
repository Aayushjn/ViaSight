package com.aayush.viasight.util

import android.content.ContentResolver
import android.content.pm.PackageManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.ContactsContract
import com.aayush.viasight.model.AppInfo
import com.aayush.viasight.model.ContactInfo

fun getInstalledApps(packageManager: PackageManager): List<AppInfo> {
    val res = mutableListOf<AppInfo>()
    val packages = packageManager.getInstalledPackages(0)
    for (pack in packages) {
        val appInfo = AppInfo(
            pack.applicationInfo.loadLabel(packageManager).toString(),
            pack.packageName,
            packageManager.getLaunchIntentForPackage(pack.packageName)
        )
        res.add(appInfo)
    }
    return res.sorted().toList()
}

fun getContacts(contentResolver: ContentResolver): List<ContactInfo> {
    val res = mutableListOf<ContactInfo>()
    val cursor = contentResolver.query(
        ContactsContract.Contacts.CONTENT_URI,
        null,
        null,
        null,
        null
    )
    if ((cursor?.count ?: 0) > 0) {
        while (cursor?.moveToNext() == true) {
            val id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
            if (cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                val cursorInfo = contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                    arrayOf(id),
                    null
                )

                while (cursorInfo?.moveToNext() == true) {
                    val info = ContactInfo(
                        id,
                        cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)),
                        cursorInfo.getString(cursorInfo.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    )
                    res.add(info)
                }
                cursorInfo?.close()
            }
        }
        cursor?.close()
    }
    return res.sorted().toList()
}

fun vibrate(vibrator: Vibrator, waveform: LongArray) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createWaveform(waveform, -1))
    }
    else {
        vibrator.vibrate(waveform, -1)
    }
}