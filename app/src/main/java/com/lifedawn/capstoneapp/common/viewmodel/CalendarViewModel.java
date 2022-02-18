package com.lifedawn.capstoneapp.common.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.lifedawn.capstoneapp.account.GoogleAccountLifeCycleObserver;
import com.lifedawn.capstoneapp.common.interfaces.HttpCallback;
import com.lifedawn.capstoneapp.common.interfaces.BackgroundCallback;
import com.lifedawn.capstoneapp.common.repository.CalendarRepository;
import com.lifedawn.capstoneapp.common.repositoryinterface.ICalendarRepository;

public class CalendarViewModel extends AndroidViewModel implements ICalendarRepository {
	private CalendarRepository calendarRepository;
	private MutableLiveData<Event> editEventLiveData = new MutableLiveData<>();
	private MutableLiveData<String> mainCalendarIdLiveData = new MutableLiveData<>();
	private String mainCalendarId;

	public CalendarViewModel(@NonNull Application application) {
		super(application);
		this.calendarRepository = new CalendarRepository(application.getApplicationContext());
	}

	public CalendarRepository getCalendarRepository() {
		return calendarRepository;
	}

	public Calendar getCalendarService() {
		return calendarRepository.getCalendarService();
	}

	public LiveData<Event> getEditEventLiveData() {
		return editEventLiveData;
	}

	public LiveData<String> getMainCalendarIdLiveData() {
		return mainCalendarIdLiveData;
	}

	public String getMainCalendarId() {
		return mainCalendarId;
	}

	@Override
	public void saveEvent(com.google.api.services.calendar.Calendar calendarService, Event newEvent, String calendarId, HttpCallback<Event> callback) {
		calendarRepository.saveEvent(calendarService, newEvent, calendarId, new HttpCallback<Event>() {
			@Override
			public void onResponseSuccessful(Event result) {
				callback.onResponseSuccessful(result);
				editEventLiveData.postValue(result);
			}

			@Override
			public void onResponseFailed(Exception e) {

			}
		});
	}

	@Override
	public void updateEvent(com.google.api.services.calendar.Calendar calendarService, Event editEvent, String calendarId, HttpCallback<Event> callback) {
		calendarRepository.updateEvent(calendarService, editEvent, calendarId, new HttpCallback<Event>() {
			@Override
			public void onResponseSuccessful(Event result) {
				callback.onResponseSuccessful(result);
				editEventLiveData.postValue(result);
			}

			@Override
			public void onResponseFailed(Exception e) {

			}
		});
	}

	@Override
	public void sendResponseForInvitedPromise(com.google.api.services.calendar.Calendar calendarService, String calendarId, String myEmail, Event event, boolean acceptance, BackgroundCallback<Boolean> callback) {
		calendarRepository.sendResponseForInvitedPromise(calendarService, calendarId, myEmail, event, acceptance, new BackgroundCallback<Boolean>() {
			@Override
			public void onResultSuccessful(Boolean e) {
				callback.onResultSuccessful(e);
				editEventLiveData.postValue(null);
			}

			@Override
			public void onResultFailed(Exception e) {

			}
		});
	}

	@Override
	public void createCalendarService(GoogleAccountCredential googleAccountCredential, GoogleAccountLifeCycleObserver googleAccountLifeCycleObserver, BackgroundCallback<Calendar> callback) {
		calendarRepository.createCalendarService(googleAccountCredential, googleAccountLifeCycleObserver, callback);
	}

	@Override
	public void addPromiseCalendar(Calendar calendarService, BackgroundCallback<com.google.api.services.calendar.model.Calendar> callback) {
		calendarRepository.addPromiseCalendar(calendarService, callback);
	}

	@Override
	public void existingPromiseCalendar(Calendar calendarService, BackgroundCallback<CalendarListEntry> callback) {
		calendarRepository.existingPromiseCalendar(calendarService, new BackgroundCallback<CalendarListEntry>() {
			@Override
			public void onResultSuccessful(CalendarListEntry e) {
				mainCalendarId = e == null ? null : e.getId();
				callback.onResultSuccessful(e);
			}

			@Override
			public void onResultFailed(Exception e) {

			}
		});
	}


}
