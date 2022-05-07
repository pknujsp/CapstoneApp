package com.lifedawn.capstoneapp.common.repositoryinterface;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.lifedawn.capstoneapp.account.GoogleAccountLifeCycleObserver;
import com.lifedawn.capstoneapp.calendar.fragments.SyncCalendarCallback;
import com.lifedawn.capstoneapp.common.interfaces.HttpCallback;
import com.lifedawn.capstoneapp.common.interfaces.BackgroundCallback;

public interface ICalendarRepository {
	void saveEvent(Calendar calendarService, Event newEvent, HttpCallback<Event> callback);

	void updateEvent(Calendar calendarService, Event editEvent, HttpCallback<Event> callback);

	void sendResponseForInvitedPromise(Calendar calendarService, String myEmail, Event event, boolean acceptance,
	                                   BackgroundCallback<Boolean> callback);

	void createCalendarService(GoogleAccountCredential googleAccountCredential, GoogleAccountLifeCycleObserver googleAccountLifeCycleObserver
			, BackgroundCallback<Calendar> callback);

	void syncCalendars(GoogleSignInAccount account, SyncCalendarCallback<Boolean> callback);
}
