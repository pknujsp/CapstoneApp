package com.lifedawn.capstoneapp.model.firestore

import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserDto(
        @get:Exclude
        var userId: String,
        @PropertyName("email")
        var email: String,
        @PropertyName("name")
        var name: String,
        @PropertyName("eventIds")
        var eventIds: MutableList<String>,
) : Parcelable {
    constructor() : this("", "", "", mutableListOf())
}
