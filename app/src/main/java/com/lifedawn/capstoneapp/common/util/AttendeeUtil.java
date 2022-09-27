package com.lifedawn.capstoneapp.common.util;

import android.content.ContentValues;
import android.content.Context;
import android.provider.CalendarContract;

import com.lifedawn.capstoneapp.common.viewmodel.FriendViewModel;

import java.util.List;

public class AttendeeUtil {
	private AttendeeUtil() {
	}

	public static String toListString(List<String> attendeeList) {
		StringBuilder stringBuilder = new StringBuilder();
		final String divider = ", ";

		final int totalCount = attendeeList.size();
		int count = 1;

		for (String attendee : attendeeList) {
			stringBuilder.append(attendee);
			if (count < totalCount) {
				stringBuilder.append(divider);
			}
			count++;
		}
		return stringBuilder.toString();
	}
}
