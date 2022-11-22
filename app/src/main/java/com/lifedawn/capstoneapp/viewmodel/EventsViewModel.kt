package com.lifedawn.capstoneapp.viewmodel

import androidx.collection.ArrayMap
import androidx.collection.arrayMapOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ktx.toObject
import com.lifedawn.capstoneapp.model.firestore.EventDto
import com.lifedawn.capstoneapp.model.repository.impl.EventRepositoryImpl
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EventsViewModel : ViewModel() {
    private val eventRepository = EventRepositoryImpl.INSTANCE
    val myEventIds = mutableSetOf<String>()
    val eventArrMap = MutableLiveData<ArrayMap<String, EventDto>>(arrayMapOf<String, EventDto>())

    fun loadEvents() {
        //events에 스냅샷 등록
        eventRepository.loadEvents(myEventIds.toList()) { snapshot, exception ->
            viewModelScope.launch {
                val arrMap = eventArrMap.value!!

                if (snapshot == null) {
                    // error
                    withContext(Main) {
                        eventArrMap.value = arrMap
                    }
                } else {
                    snapshot.documentChanges.forEach { documentChange ->
                        val eventDto = documentChange.document.toObject<EventDto>()
                        eventDto.id = documentChange.document.id

                        when (documentChange.type) {
                            DocumentChange.Type.ADDED -> {
                                arrMap[eventDto.id] = eventDto
                            }
                            DocumentChange.Type.MODIFIED -> {
                                arrMap[eventDto.id] = eventDto
                            }
                            else -> {
                                arrMap.remove(eventDto.id)
                            }
                        }

                    }

                    withContext(Main) {
                        eventArrMap.value = arrMap
                    }
                }

            }

        }


    }
}