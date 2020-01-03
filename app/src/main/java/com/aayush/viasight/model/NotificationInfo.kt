package com.aayush.viasight.model

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import com.aayush.viasight.util.android.*
import com.aayush.viasight.util.common.DateSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
class NotificationInfo(
    var packageName: String,
    var tickerText: String,
    var title: String,
    var text: String,
    @Serializable(DateSerializer::class) var date: Date
): KParcelable {
    var isRead = false

    private constructor(parcel: Parcel): this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readDate()!!
    ) {
        isRead = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) parcel.readBoolean() else parcel.readBool()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) = with(parcel) {
        writeString(packageName)
        writeString(tickerText)
        writeString(title)
        writeString(text)
        writeDate(date)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) writeBoolean(isRead) else writeBool(isRead)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NotificationInfo) return false

        if (packageName != other.packageName) return false
        if (tickerText != other.tickerText) return false
        if (title != other.title) return false
        if (text != other.text) return false
        if (date != other.date) return false
        if (isRead != other.isRead) return false

        return true
    }

    override fun hashCode(): Int {
        var result = packageName.hashCode()
        result = 31 * result + tickerText.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + text.hashCode()
        result = 31 * result + date.hashCode()
        result = 31 * result + isRead.hashCode()
        return result
    }

    override fun toString(): String =
        "NotificationInfo(packageName=$packageName, tickerText=$tickerText, title=$title, text=$text, date=$date, isRead=$isRead)"

    companion object CREATOR: Parcelable.Creator<NotificationInfo> {
        override fun createFromParcel(source: Parcel): NotificationInfo = NotificationInfo(source)
        override fun newArray(size: Int): Array<NotificationInfo?> = arrayOfNulls(size)
    }
}