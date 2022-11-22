package com.lifedawn.capstoneapp.model.firestore

import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import com.lifedawn.capstoneapp.model.events.EventParticipantDto
import kotlinx.parcelize.Parcelize

@Parcelize
data class EventDto(
        @get:Exclude
        var id: String,
        @PropertyName("createDateTime")
        var createdDateTime: String,
        @PropertyName("creatorId")
        var creatorId: String,
        @PropertyName("dateTime")
        var dateTime: String,
        @PropertyName("lastModifiedDateTime")
        var lastModifiedDateTime: String,
        @PropertyName("msg")
        var msg: String,
        @PropertyName("title")
        var title: String,
        @PropertyName("memberIds")
        var memberIds: MutableList<String>,
) : Parcelable {
    constructor() : this("", "", "", "", "", "", "", mutableListOf())

    @get:Exclude
    val places: MutableList<PlaceDto> = mutableListOf()

    @get:Exclude
    val participants: MutableList<EventParticipantDto> = mutableListOf()

    @get:Exclude
    var creatorIsMe: Boolean = false
}
