package com.nytimes.entities.pojo

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Metadata(val url: String, val format: String) : Parcelable