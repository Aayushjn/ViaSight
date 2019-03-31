package com.aayush.viasight.util

import android.content.ContentResolver
import android.content.pm.PackageManager
import android.provider.ContactsContract
import com.aayush.viasight.model.AppInfo
import com.aayush.viasight.model.ContactInfo


fun getInstalledApps(packageManager: PackageManager): List<AppInfo> {
    val res = mutableListOf<AppInfo>()
    val applications = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
    for (app in applications) {
        if (packageManager.getLaunchIntentForPackage(app.packageName) == null || app.name == null) {
            continue
        }
        val appInfo = AppInfo()
        appInfo.appName = if (app.labelRes == 0) {
            app.nonLocalizedLabel.toString()
        }
        else {
            app.name.substring(app.name.indexOf("."))
        }
        appInfo.packageName = app.packageName
        appInfo.icon = app.loadIcon(packageManager)!!
        appInfo.launchIntent = packageManager.getLaunchIntentForPackage(app.packageName)
        res.add(appInfo)
    }
    return res
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
                    val info = ContactInfo()
                    info.id = id
                    info.name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                    info.phoneNumber =
                        cursorInfo.getString(cursorInfo.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

                    res.add(info)
                }

                cursorInfo?.close()
            }
        }
        cursor?.close()
    }

    return res.sorted().toList()
}