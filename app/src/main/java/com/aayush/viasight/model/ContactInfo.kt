package com.aayush.viasight.model

import android.os.Parcel
import android.os.Parcelable
import com.aayush.viasight.util.android.KParcelable

data class ContactInfo(var id: String, var name: String, var phoneNumber: String): KParcelable, Comparable<ContactInfo> {
    private constructor(parcel: Parcel): this(
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

    companion object CREATOR: Parcelable.Creator<ContactInfo> {
        override fun createFromParcel(source: Parcel): ContactInfo = ContactInfo(source)
        override fun newArray(size: Int): Array<ContactInfo?> = arrayOfNulls(size)
    }
}