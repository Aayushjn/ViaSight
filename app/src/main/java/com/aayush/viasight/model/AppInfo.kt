package com.aayush.viasight.model

import android.content.Intent
import android.os.Parcel
import android.os.Parcelable
import com.aayush.viasight.util.android.KParcelable
import com.aayush.viasight.util.android.parcelableCreator
import com.aayush.viasight.util.android.readTypedObjectCompat
import com.aayush.viasight.util.android.writeTypedObjectCompat

data class AppInfo(var appName: String, var packageName: String, var launchIntent: Intent?): KParcelable, Comparable<AppInfo> {
    private constructor(parcel: Parcel): this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readTypedObjectCompat(parcelableCreator { Intent() })!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) = with(parcel) {
        writeString(appName)
        writeString(packageName)
        writeTypedObjectCompat(launchIntent, flags)
    }

    override fun compareTo(other: AppInfo) = compareValuesBy(this, other, { it.appName }, { it.packageName })

    companion object CREATOR: Parcelable.Creator<AppInfo> {
        override fun createFromParcel(source: Parcel): AppInfo = AppInfo(source)
        override fun newArray(size: Int): Array<AppInfo?> = arrayOfNulls(size)
    }
}