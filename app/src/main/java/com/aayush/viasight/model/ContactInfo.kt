package com.aayush.viasight.model

import android.os.Parcel
import com.aayush.viasight.util.KParcelable
import com.aayush.viasight.util.parcelableCreator

data class ContactInfo(
    var id: String,
    var name: String,
    var phoneNumber: String
): KParcelable {
    constructor(parcel: Parcel): this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    )

    constructor(): this(
        "",
        "",
        ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) = with(parcel) {
        writeString(id)
        writeString(name)
        writeString(phoneNumber)
    }

    companion object {
        @JvmField val CREATOR = parcelableCreator(::ContactInfo)
    }
}