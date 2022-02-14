package com.lifedawn.capstoneapp.common.util;

import com.google.api.services.calendar.model.EventAttendee;

import java.util.List;

public class AttendeeUtil {
	private AttendeeUtil() {}
	
	public static String toListString(List<EventAttendee> eventAttendeeList) {
		StringBuilder stringBuilder = new StringBuilder();
		final String divider = ", ";
		
		final int totalCount = eventAttendeeList.size();
		int count = 1;
		
		for (EventAttendee eventAttendee : eventAttendeeList) {
			stringBuilder.append(eventAttendee.getDisplayName());
			if (count++ < totalCount) {
				stringBuilder.append(divider);
			}
		}
		
		return stringBuilder.toString();
	}
}
