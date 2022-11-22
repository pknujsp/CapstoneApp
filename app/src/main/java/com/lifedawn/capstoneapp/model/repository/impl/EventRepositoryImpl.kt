package com.lifedawn.capstoneapp.model.repository.impl

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.lifedawn.capstoneapp.common.constants.FirestoreConstants
import com.lifedawn.capstoneapp.model.repository.interfaces.EventRepository

class EventRepositoryImpl private constructor() : EventRepository {
    private var loadEventsListener: ListenerRegistration? = null
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    companion object {
        private var instance: EventRepositoryImpl? = null
        val INSTANCE get() = instance!!

        fun initialize() {
            if (instance == null)
                instance = EventRepositoryImpl()
        }
    }

    override fun loadEvents(eventIds: List<String>, listener: EventListener<QuerySnapshot>) {
        val myId = auth.currentUser?.uid!!

        loadEventsListener?.remove()
        loadEventsListener = firestore.collection(FirestoreConstants.events.name).whereIn(FieldPath.documentId(), eventIds)
                .orderBy("dateTime").addSnapshotListener(listener)
    }
}