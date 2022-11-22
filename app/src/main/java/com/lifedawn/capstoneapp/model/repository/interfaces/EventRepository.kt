package com.lifedawn.capstoneapp.model.repository.interfaces

import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot
import com.lifedawn.capstoneapp.model.firestore.EventDto

interface EventRepository {
    fun loadEvents(eventIds: List<String>, listener: EventListener<QuerySnapshot>)
}