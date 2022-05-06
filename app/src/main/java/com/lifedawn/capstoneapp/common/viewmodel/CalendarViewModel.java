package com.lifedawn.capstoneapp.common.viewmodel;

import android.app.Application;
import android.app.Notification;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.account.GoogleAccountLifeCycleObserver;
import com.lifedawn.capstoneapp.common.constants.SharedPreferenceConstant;
import com.lifedawn.capstoneapp.common.interfaces.HttpCallback;
import com.lifedawn.capstoneapp.common.interfaces.BackgroundCallback;
import com.lifedawn.capstoneapp.common.repository.CalendarRepository;
import com.lifedawn.capstoneapp.common.repositoryinterface.ICalendarRepository;
import com.lifedawn.capstoneapp.common.util.NotificationHelper;
import com.lifedawn.capstoneapp.main.MyApplication;

import java.time.ZonedDateTime;

public class CalendarViewModel extends AndroidViewModel implements ICalendarRepository {
	private CalendarRepository calendarRepository;
	private MutableLiveData<Boolean> syncCalendarLiveData = new MutableLiveData<>();
	private boolean syncingCalendar = false;

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
	public void syncCalendars(GoogleSignInAccount account, BackgroundCallback<Boolean> callback) {
		if (syncingCalendar) {
			return;
		}
		syncingCalendar = true;
		showNotification();

		MyApplication.EXECUTOR_SERVICE.execute(new Runnable() {
			@Override
			public void run() {
				calendarRepository.syncCalendars(account, new BackgroundCallback<Boolean>() {
					@Override
					public void onResultSuccessful(Boolean e) {
						ZonedDateTime now = ZonedDateTime.now();
						SharedPreferences.Editor editor =
								PreferenceManager.getDefaultSharedPreferences(getApplication().getApplicationContext()).edit();
						editor.putString(SharedPreferenceConstant.LAST_UPDATE_DATETIME.getVal(), now.toString()).commit();

						NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplication().getApplicationContext());
						notificationManagerCompat.cancel(NotificationHelper.NotificationType.SYNC_CALENDAR.NotificationId());
						syncingCalendar = false;
						syncCalendarLiveData.setValue(true);
						callback.onResultSuccessful(e);
					}

					@Override
					public void onResultFailed(Exception e) {
						NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplication().getApplicationContext());
						notificationManagerCompat.cancel(NotificationHelper.NotificationType.SYNC_CALENDAR.NotificationId());
						syncingCalendar = false;
						callback.onResultFailed(e);
					}
				});
			}
		});


	}

	private void showNotification() {
		Context context = getApplication().getApplicationContext();
		NotificationHelper notificationHelper = new NotificationHelper(context);
		NotificationHelper.NotificationItem notificationItem =
				notificationHelper.createNotificationItem(NotificationHelper.NotificationType.SYNC_CALENDAR);

		NotificationCompat.Builder builder = notificationItem.getBuilder();
		builder.setSmallIcon(R.drawable.ic_baseline_refresh_24).setContentText(context.getString(R.string.syncCalendar))
				.setContentTitle(context.getString(R.string.syncing_calendar))
				.setWhen(0).setOngoing(true);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
			builder.setPriority(NotificationCompat.PRIORITY_LOW).setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
		}

		Notification notification = builder.build();
		NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
		notificationManagerCompat.notify(notificationItem.getNotificationType().NotificationId(), notification);
	}

}
