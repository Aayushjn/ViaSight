package com.aayush.viasight.model

import android.os.Parcel
import com.aayush.viasight.util.KParcelable
import com.aayush.viasight.util.parcelableCreator

data class ContactInfo(
    var id: String,
    var name: String,
    var phoneNumber: String
): KParcelable, Comparable<ContactInfo> {
    constructor(parcel: Parcel): this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) = with(parcel) {
        writeString(id)
        writeString(name)
        writeString(phoneNumber)
    }

    override fun compareTo(other: ContactInfo) =
        compareValuesBy(this, other, { it.name }, { it.id })

    companion object {
        @JvmField val CREATOR = parcelableCreator(::ContactInfo)
    }
}