package com.lifedawn.capstoneapp.common.repositoryinterface;

import android.accounts.Account;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.lifedawn.capstoneapp.account.GoogleAccountLifeCycleObserver;
import com.lifedawn.capstoneapp.common.interfaces.HttpCallback;
import com.lifedawn.capstoneapp.common.interfaces.BackgroundCallback;

public interface ICalendarRepository {
	void saveEvent(Calendar calendarService, Event newEvent, String calendarId, HttpCallback<Event> callback);

	void updateEvent(Calendar calendarService, Event editEvent, String calendarId, HttpCallback<Event> callback);

	void sendResponseForInvitedPromise(Calendar calendarService, String calendarId, String myEmail, Event event, boolean acceptance,
	                                   BackgroundCallback<Boolean> callback);

	void createCalendarService(GoogleAccountCredential googleAccountCredential, GoogleAccountLifeCycleObserver googleAccountLifeCycleObserver
			, BackgroundCallback<Calendar> callback);

	void addPromiseCalendar(Calendar calendarService, BackgroundCallback<com.google.api.services.calendar.model.Calendar> callback);

	void existingPromiseCalendar(Calendar calendarService, BackgroundCallback<CalendarListEntry> callback);

	void syncCalendars(Account account, BackgroundCallback<Boolean> callback);
}
