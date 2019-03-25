package com.aayush.viasight.util

import android.os.Parcel
import android.os.Parcelable

inline fun <reified T> parcelableCreator(
    crossinline create: (Parcel) -> T) =
    object: Parcelable.Creator<T> {
        override fun createFromParcel(source: Parcel) = create(source)

        override fun newArray(size: Int) = arrayOfNulls<T>(size)
    }

inline fun <reified T> parcelableClassLoaderCreator(
    crossinline create: (Parcel, ClassLoader) -> T) =
    object: Parcelable.ClassLoaderCreator<T> {
        override fun createFromParcel(source: Parcel, loader: ClassLoader) =
            create(source, loader)

        override fun createFromParcel(source: Parcel) =
            createFromParcel(source, T::class.java.classLoader!!)

        override fun newArray(size: Int) = arrayOfNulls<T>(size)
    }

fun Parcel.readBoolean() = readInt() == 1

fun Parcel.writeBoolean(`val`: Boolean) = writeInt(if (`val`) 1 else 0)

inline fun <T> Parcel.readNullable(reader: () -> T) =
    if (readInt() != 0) reader() else null

inline fun <T> Parcel.writeNullable(value: T?, writer: (T) -> Unit) {
    if (value != null) {
        writeInt(1)
        writer(value)
    } else {
        writeInt(0)
    }
}

fun <T: Parcelable> Parcel.readTypedObjectCompat(creator: Parcelable.Creator<T>) =
    readNullable { creator.createFromParcel(this) }

fun <T: Parcelable> Parcel.writeTypedObjectCompat(`val`: T?, flags: Int) =
    writeNullable(`val`) { it.writeToParcel(this, flags) }

interface KParcelable: Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int)

    override fun describeContents() = 0
}