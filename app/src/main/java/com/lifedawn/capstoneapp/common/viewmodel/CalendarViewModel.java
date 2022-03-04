package com.lifedawn.capstoneapp.common.viewmodel;

import android.accounts.Account;
import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.lifedawn.capstoneapp.account.GoogleAccountLifeCycleObserver;
import com.lifedawn.capstoneapp.common.interfaces.HttpCallback;
import com.lifedawn.capstoneapp.common.interfaces.BackgroundCallback;
import com.lifedawn.capstoneapp.common.repository.CalendarRepository;
import com.lifedawn.capstoneapp.common.repositoryinterface.ICalendarRepository;

public class CalendarViewModel extends AndroidViewModel implements ICalendarRepository {
	private CalendarRepository calendarRepository;
	private MutableLiveData<String> mainCalendarIdLiveData = new MutableLiveData<>();

	public CalendarViewModel(@NonNull Application application) {
		super(application);
		this.calendarRepository = new CalendarRepository(application.getApplicationContext());
	}


	public Calendar getCalendarService() {
		return calendarRepository.getCalendarService();
	}


	public LiveData<String> getMainCalendarIdLiveData() {
		return mainCalendarIdLiveData;
	}


	@Override
	public void saveEvent(Calendar calendarService, Event newEvent, HttpCallback<Event> callback) {
		calendarRepository.saveEvent(calendarService, newEvent, new HttpCallback<Event>() {
			@Override
			public void onResponseSuccessful(Event result) {
				callback.onResponseSuccessful(result);
			}

			@Override
			public void onResponseFailed(Exception e) {

			}
		});
	}

	@Override
	public void updateEvent(Calendar calendarService, Event editEvent, HttpCallback<Event> callback) {
		calendarRepository.updateEvent(calendarService, editEvent, new HttpCallback<Event>() {
			@Override
			public void onResponseSuccessful(Event result) {
				callback.onResponseSuccessful(result);
			}

			@Override
			public void onResponseFailed(Exception e) {

			}
		});
	}

	@Override
	public void sendResponseForInvitedPromise(Calendar calendarService, String myEmail, Event event, boolean acceptance, BackgroundCallback<Boolean> callback) {
		calendarRepository.sendResponseForInvitedPromise(calendarService, myEmail, event, acceptance, new BackgroundCallback<Boolean>() {
			@Override
			public void onResultSuccessful(Boolean e) {
				callback.onResultSuccessful(e);
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
	public void syncCalendars(Account account, BackgroundCallback<Boolean> callback) {
		calendarRepository.syncCalendars(account, callback);
	}
}
