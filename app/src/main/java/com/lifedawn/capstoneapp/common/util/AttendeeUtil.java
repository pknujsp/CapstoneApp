package com.lifedawn.capstoneapp.common.util;

import android.content.ContentValues;
import android.provider.CalendarContract;

import com.google.api.services.calendar.model.EventAttendee;

import java.util.List;

public class AttendeeUtil {
	private AttendeeUtil() {
	}

	public static String toListString(List<ContentValues> eventAttendeeList) {
		StringBuilder stringBuilder = new StringBuilder();
		final String divider = ", ";

		final int totalCount = eventAttendeeList.size();
		int count = 1;

		for (ContentValues eventAttendee : eventAttendeeList) {
			stringBuilder.append(eventAttendee.getAsString(CalendarContract.Attendees.ATTENDEE_EMAIL));
			if (count < totalCount) {
				stringBuilder.append(divider);
			}
			count++;
		}

		return stringBuilder.toString();
	}
}
