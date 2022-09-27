package com.lifedawn.capstoneapp.util

class AttendeeUtil {
    companion object {
        fun toListString(attendeeList: ArrayList<String>): String {
            val stringBuilder = StringBuilder()
            val divider = ", "
            val totalCount = attendeeList.size
            var count = 1

            for (attendee in attendeeList) {
                stringBuilder.append(attendee)
                if (count < totalCount) {
                    stringBuilder.append(divider)
                }
                count++
            }
            return stringBuilder.toString()
        }
    }

}