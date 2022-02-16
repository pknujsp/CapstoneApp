package com.lifedawn.capstoneapp.common.repositoryinterface;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.lifedawn.capstoneapp.common.interfaces.HttpCallback;
import com.lifedawn.capstoneapp.main.MyApplication;

import java.io.IOException;

public interface ICalendarRepository {
	void saveEvent(Calendar calendarService, Event newEvent, String calendarId, HttpCallback<Event> callback);

	void updateEvent(Calendar calendarService, Event editEvent, String calendarId, HttpCallback<Event> callback);
}
