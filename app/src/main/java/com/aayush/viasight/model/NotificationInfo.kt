package com.aayush.viasight.model

import android.os.Parcel
import com.aayush.viasight.util.*
import java.util.*

data class NotificationInfo(
    var packageName: String,
    var tickerText: String,
    var title: String,
    var text: String,
    var date: Date
): KParcelable {
    var isRead = false

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readDate()!!
    ) {
        isRead = parcel.readBoolean()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) = with(parcel) {
        writeString(packageName)
        writeString(tickerText)
        writeString(title)
        writeString(text)
        writeDate(date)
        writeBoolean(isRead)
    }

    companion object {
        @JvmField val CREATOR = parcelableCreator(::NotificationInfo)
    }
}