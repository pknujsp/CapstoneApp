package com.lifedawn.capstoneapp.model.firestore

import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import kotlinx.parcelize.Parcelize

@Parcelize
data class FriendDto(
        @PropertyName("id")
        var id: String,
        @PropertyName("name")
        var name: String,
) : Parcelable {
    constructor() : this("", "")
}
