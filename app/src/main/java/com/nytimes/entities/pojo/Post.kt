package com.nytimes.entities.pojo

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Post(val id: Long, val url: String, val title: String, val abstract: String, val media: List<Media>) : Parcelable