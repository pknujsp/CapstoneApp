package com.lifedawn.capstoneapp.viewmodel

import androidx.lifecycle.ViewModel

class FriendsViewModel : ViewModel() {
    companion object {
        //id, name
        val FRIENDS_MAP = mutableMapOf<String, String>()
    }
}
