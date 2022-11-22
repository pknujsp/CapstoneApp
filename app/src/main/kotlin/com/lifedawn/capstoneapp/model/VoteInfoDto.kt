package com.lifedawn.capstoneapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VoteInfoDto(
        val voteId: Int,
        val chatRoomId: Int,
        val title: String,
        val description: String,
        val dateTime: String,
        val peopleCount: Int,
        val completed: Boolean,
        val voteOnlySingle: Boolean,
) : Parcelable
