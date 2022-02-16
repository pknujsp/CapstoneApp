package com.lifedawn.capstoneapp.common.repository;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.lifedawn.capstoneapp.common.interfaces.HttpCallback;
import com.lifedawn.capstoneapp.common.repositoryinterface.ICalendarRepository;
import com.lifedawn.capstoneapp.main.MyApplication;

import java.io.IOException;

public class CalendarRepository implements ICalendarRepository {

	public CalendarRepository() {
	}

	@Override
	public void saveEvent(Calendar calendarService, Event newEvent, String calendarId, HttpCallback<Event> callback) {
		MyApplication.EXECUTOR_SERVICE.execute(new Runnable() {
			@Override
			public void run() {
				Event savedEvent = null;
				try {
					savedEvent = calendarService.events().insert(calendarId, newEvent).execute();

				} catch (IOException e) {
					e.printStackTrace();
				}

				callback.onResponseSuccessful(savedEvent);
			}
		});
	}

	@Override
	public void updateEvent(Calendar calendarService, Event editEvent, String calendarId, HttpCallback<Event> callback) {
		MyApplication.EXECUTOR_SERVICE.execute(new Runnable() {
			@Override
			public void run() {
				Event updatedEvent = null;
				try {
					updatedEvent =
							calendarService.events().update(calendarId, editEvent.getId(), editEvent).execute();
				} catch (IOException e) {
					e.printStackTrace();
				}

				callback.onResponseSuccessful(updatedEvent);
			}
		});
	}
}
