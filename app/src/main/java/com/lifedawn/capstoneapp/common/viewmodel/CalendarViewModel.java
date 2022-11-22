package com.lifedawn.capstoneapp.common.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.lifedawn.capstoneapp.calendar.fragments.SyncCalendarCallback;
import com.lifedawn.capstoneapp.common.constants.SharedPreferenceConstant;
import com.lifedawn.capstoneapp.common.interfaces.HttpCallback;
import com.lifedawn.capstoneapp.common.interfaces.BackgroundCallback;
import com.lifedawn.capstoneapp.common.repository.CalendarRepository;
import com.lifedawn.capstoneapp.common.repositoryinterface.ICalendarRepository;

import java.time.ZonedDateTime;

public class CalendarViewModel extends AndroidViewModel implements ICalendarRepository {
	private CalendarRepository calendarRepository;
	private MutableLiveData<Boolean> syncCalendarLiveData = new MutableLiveData<>();


	public CalendarViewModel(@NonNull Application application) {
		super(application);
		this.calendarRepository = CalendarRepository.getInstance(application.getApplicationContext());
	}

	public LiveData<Boolean> getSyncCalendarLiveData() {
		return syncCalendarLiveData;
	}

	public Calendar getCalendarService() {
		return calendarRepository.getCalendarService();
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
	public void syncCalendars(GoogleSignInAccount account, SyncCalendarCallback<Boolean> callback) {
		calendarRepository.syncCalendars(account, new SyncCalendarCallback<Boolean>() {
			@Override
			public void onResultSuccessful(Boolean e) {
				super.onResultSuccessful(e);
				ZonedDateTime now = ZonedDateTime.now();
				SharedPreferences.Editor editor =
						PreferenceManager.getDefaultSharedPreferences(getApplication().getApplicationContext()).edit();
				editor.putString(SharedPreferenceConstant.LAST_UPDATE_DATETIME.getVal(), now.toString()).commit();

				callback.onResultSuccessful(e);
				syncCalendarLiveData.postValue(true);
			}

			@Override
			public void onResultFailed(Exception e) {
				super.onResultFailed(e);
				callback.onResultFailed(e);
			}

			@Override
			public void onAlreadySyncing() {
				callback.onAlreadySyncing();
			}

			@Override
			public void onSyncStarted() {
				super.onSyncStarted();
				callback.onSyncStarted();
			}
		});

	}


}
