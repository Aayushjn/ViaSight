package com.aayush.viasight.model

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Parcel
import com.aayush.viasight.util.KParcelable
import com.aayush.viasight.util.parcelableCreator
import com.aayush.viasight.util.readTypedObjectCompat
import com.aayush.viasight.util.writeTypedObjectCompat

data class AppInfo(
    var appName: String,
    var packageName: String,
    var icon: Drawable?,
    var launchIntent: Intent?
): KParcelable, Comparable<AppInfo> {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readValue(Drawable::class.java.classLoader) as Drawable,
        parcel.readTypedObjectCompat(parcelableCreator { Intent() })!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) = with(parcel) {
        writeString(appName)
        writeString(packageName)
        writeValue(icon)
        writeTypedObjectCompat(launchIntent, flags)
    }

    override fun compareTo(other: AppInfo) = compareValuesBy(this, other, { it.appName }, { it.packageName })

    companion object {
        @JvmField val CREATOR = parcelableCreator(::AppInfo)
    }
}