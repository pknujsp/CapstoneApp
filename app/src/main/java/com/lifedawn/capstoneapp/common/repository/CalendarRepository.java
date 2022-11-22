package com.lifedawn.capstoneapp.common.repository;

import android.accounts.Account;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncStatusObserver;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.CalendarContract;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.calendar.fragments.SyncCalendarCallback;
import com.lifedawn.capstoneapp.common.interfaces.HttpCallback;
import com.lifedawn.capstoneapp.common.interfaces.BackgroundCallback;
import com.lifedawn.capstoneapp.common.repositoryinterface.ICalendarRepository;
import com.lifedawn.capstoneapp.common.util.NotificationHelper;
import com.lifedawn.capstoneapp.main.MyApplication;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalendarRepository implements ICalendarRepository {
	private static Calendar calendarService;
	private static CalendarRepository instance;
	private CalendarSyncStatusObserver calendarSyncStatusObserver = new CalendarSyncStatusObserver();
	private Context context;

	private CalendarRepository(Context context) {
		this.context = context;
	}

	public static CalendarRepository getInstance(Context context) {
		if (instance == null) {
			instance = new CalendarRepository(context);
		}
		return instance;
	}


	@Override
	public void saveEvent(Calendar calendarService, Event newEvent, HttpCallback<Event> callback) {
		MyApplication.EXECUTOR_SERVICE.execute(new Runnable() {
			@Override
			public void run() {
				Event savedEvent = null;
				try {
					savedEvent = calendarService.events().insert("primary", newEvent).execute();
				} catch (IOException e) {
					e.printStackTrace();
				}

				callback.onResponseSuccessful(savedEvent);
			}
		});
	}

	@Override
	public void updateEvent(Calendar calendarService, Event editEvent, HttpCallback<Event> callback) {
		MyApplication.EXECUTOR_SERVICE.execute(new Runnable() {
			@Override
			public void run() {
				Event updatedEvent = null;
				try {
					updatedEvent =
							calendarService.events().update("primary", editEvent.getId(), editEvent).execute();
				} catch (IOException e) {
					e.printStackTrace();
				}

				callback.onResponseSuccessful(updatedEvent);
			}
		});
	}

	@Override
	public void sendResponseForInvitedPromise(Calendar calendarService, String myEmail, Event event, boolean acceptance,
	                                          BackgroundCallback<Boolean> callback) {
		MyApplication.EXECUTOR_SERVICE.execute(new Runnable() {
			@Override
			public void run() {
				try {
					for (EventAttendee eventAttendee : event.getAttendees()) {
						if (eventAttendee.getEmail().equals(myEmail)) {
							eventAttendee.setResponseStatus(acceptance ? "accepted" : "declined");
							break;
						}
					}

					Event updatedEvent =
							calendarService.events().update("primary", event.getId(), event).execute();
					if (updatedEvent != null) {
						callback.onResultSuccessful(true);
					}
				} catch (IOException e) {
					e.printStackTrace();
					callback.onResultSuccessful(false);
				}

			}
		});
	}

	@Override
	public void createCalendarService(GoogleAccountCredential googleAccountCredential, GoogleAccountLifeCycleObserver googleAccountLifeCycleObserver
			, BackgroundCallback<Calendar> callback) {
		MyApplication.EXECUTOR_SERVICE.execute(new Runnable() {
			@Override
			public void run() {
				final HttpTransport httpTransport = new NetHttpTransport();
				final JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

				try {
					calendarService = new Calendar.Builder(httpTransport, jsonFactory, googleAccountCredential).setApplicationName(
							"promise").build();
					calendarService.events().list("primary").execute();
					callback.onResultSuccessful(calendarService);
				} catch (Exception e) {
					if (e instanceof UserRecoverableAuthIOException) {
						googleAccountLifeCycleObserver.launchUserRecoverableAuthIntent(((UserRecoverableAuthIOException) e).getIntent(),
								new ActivityResultCallback<ActivityResult>() {
									@Override
									public void onActivityResult(ActivityResult result) {
										if (result.getResultCode() == Activity.RESULT_OK) {
											calendarService = new Calendar.Builder(httpTransport, jsonFactory, googleAccountCredential)
													.setApplicationName("promise").build();
											callback.onResultSuccessful(calendarService);
										} else {
											calendarService = null;
											callback.onResultFailed(e);
										}

									}
								});
					}
				}
			}
		});

	}

	public Calendar getCalendarService() {
		return calendarService;
	}

	@Override
	public void syncCalendars(GoogleSignInAccount account, SyncCalendarCallback<Boolean> callback) {
		if (account == null) {
			callback.onResultFailed(new NullPointerException("account"));
			return;
		}

		if (calendarSyncStatusObserver.isSyncing()) {
			callback.onAlreadySyncing();
			return;
		}
		callback.onSyncStarted();
		showNotification();

		calendarSyncStatusObserver.setSyncCallback(new SyncCalendarCallback<Boolean>() {
			@Override
			public void onResultSuccessful(Boolean e) {
				cancelNotification();
				callback.onResultSuccessful(e);
			}

			@Override
			public void onResultFailed(Exception e) {
				cancelNotification();
				callback.onResultFailed(e);
			}

			@Override
			public void onSyncStarted() {

			}

			@Override
			public void onAlreadySyncing() {

			}
		});
		calendarSyncStatusObserver.setProviderHandle(ContentResolver.addStatusChangeListener(ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE, calendarSyncStatusObserver));
		calendarSyncStatusObserver.setAccount(account.getAccount());

		Bundle arguments = new Bundle();
		arguments.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
		arguments.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
		ContentResolver.requestSync(account.getAccount(), CalendarContract.AUTHORITY, arguments);
	}

	private void cancelNotification() {
		NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
		notificationManagerCompat.cancel(NotificationHelper.NotificationType.SYNC_CALENDAR.NotificationId());
	}

	private void showNotification() {
		NotificationHelper notificationHelper = new NotificationHelper(context);
		NotificationHelper.NotificationItem notificationItem =
				notificationHelper.createNotificationItem(NotificationHelper.NotificationType.SYNC_CALENDAR);

		NotificationCompat.Builder builder = notificationItem.getBuilder();
		builder.setSmallIcon(R.drawable.ic_baseline_refresh_24).setContentText(context.getString(R.string.syncCalendar))
				.setContentTitle(context.getString(R.string.syncing_calendar))
				.setWhen(0).setOngoing(true).setProgress(0, 0, true);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
			builder.setPriority(NotificationCompat.PRIORITY_LOW).setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
		}

		Notification notification = builder.build();
		NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
		notificationManagerCompat.notify(notificationItem.getNotificationType().NotificationId(), notification);
	}


	private static class CalendarSyncStatusObserver implements SyncStatusObserver {
		private final int PENDING = 0;
		private final int PENDING_ACTIVE = 10;
		private final int ACTIVE = 20;
		private final int FINISHED = 30;

		private final Map<Account, Integer> mAccountSyncState =
				Collections.synchronizedMap(new HashMap<Account, Integer>());

		private final String mCalendarAuthority = CalendarContract.AUTHORITY;

		private Object mProviderHandle;
		private SyncCalendarCallback<Boolean> syncCallback;
		private Account account;

		public Object getmProviderHandle() {
			return mProviderHandle;
		}

		public void setAccount(Account account) {
			this.account = account;
		}

		public void setSyncCallback(SyncCalendarCallback<Boolean> syncCallback) {
			this.syncCallback = syncCallback;
		}

		public boolean isSyncing() {
			return syncCallback != null;
		}

		public void setProviderHandle(@NonNull final Object providerHandle) {
			mProviderHandle = providerHandle;
		}


		@Override
		public void onStatusChanged(int which) {
			if (which == ContentResolver.SYNC_OBSERVER_TYPE_PENDING) {
				if (ContentResolver.isSyncPending(account, mCalendarAuthority)) {
					mAccountSyncState.put(account, PENDING);
				} else {
					mAccountSyncState.put(account, PENDING_ACTIVE);
				}
			} else if (which == ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE) {
				if (ContentResolver.isSyncActive(account, mCalendarAuthority)) {
					mAccountSyncState.put(account, ACTIVE);
				} else {
					mAccountSyncState.put(account, FINISHED);
				}
			}

			if (1 == mAccountSyncState.size()) {
				int finishedCount = 0;

				for (Integer syncState : mAccountSyncState.values()) {
					if (syncState == FINISHED) {
						finishedCount++;
					}
				}

				if (finishedCount == 1) {
					if (mProviderHandle != null) {
						ContentResolver.removeStatusChangeListener(mProviderHandle);
						mProviderHandle = null;
					}
					if (syncCallback != null) {
						syncCallback.onResultSuccessful(true);
						syncCallback = null;
					}
					mAccountSyncState.clear();
				}
			}

		}

	}


	public static void loadMyEvents(Context context, String accountName, String calendarId, BackgroundCallback<List<EventObj>> callback) {
		String selection = CalendarContract.Events.CALENDAR_ID + "=? AND " + CalendarContract.Events.ACCOUNT_NAME + "=?";
		String[] selectionArgs = new String[]{calendarId, accountName};
		loadEvents(context, selection, selectionArgs, callback);
	}

	@SuppressLint("Range")
	public static void loadReceivedInvitationEvents(Context context, String accountName,
	                                                BackgroundCallback<List<EventObj>> callback) {
		String selection = CalendarContract.Events.ORGANIZER + " LIKE '%@gmail.com' AND " + CalendarContract.Events.CALENDAR_DISPLAY_NAME + "==?";
		String[] selectionArgs = {accountName};
		loadEvents(context, selection, selectionArgs, callback);
	}

	public static void loadEvents(Context context, String calendarId, ZonedDateTime begin, ZonedDateTime end,
	                              BackgroundCallback<List<EventObj>> callback) {
		String selection = CalendarContract.Events.CALENDAR_ID + "=? AND " + CalendarContract.Events.DTSTART + ">=? AND " +
				CalendarContract.Events.DTEND + "<=?";
		String[] selectionArgs = {calendarId, String.valueOf(begin.toInstant().getEpochSecond() * 1000L),
				String.valueOf(end.toInstant().getEpochSecond() * 1000L)};
		loadEvents(context, selection, selectionArgs, callback);
	}


	@SuppressLint("Range")
	public static void loadCalendar(Context context, String accountName, BackgroundCallback<ContentValues> callback) {
		MyApplication.EXECUTOR_SERVICE.execute(new Runnable() {
			@Override
			public void run() {
				if (accountName == null) {
					callback.onResultFailed(new Exception("accountName is null"));
					return;
				}

				String selection = CalendarContract.Calendars.ACCOUNT_NAME + "=? AND " + CalendarContract.Calendars.IS_PRIMARY + "=?";
				String[] selectionArgs = new String[]{accountName, "1"};
				Cursor cursor = context.getContentResolver().query(CalendarContract.Calendars.CONTENT_URI, null, selection, selectionArgs, null);

				ContentValues calendar = new ContentValues();
				if (cursor != null) {
					while (cursor.moveToNext()) {
						String[] keys = cursor.getColumnNames();
						for (String key : keys) {
							calendar.put(key, cursor.getString(cursor.getColumnIndex(key)));
						}
					}
					cursor.close();
				}
				callback.onResultSuccessful(calendar);
			}
		});

	}


	@SuppressLint("Range")
	public static void loadAttendees(Context context, ZonedDateTime begin, ZonedDateTime end, BackgroundCallback<List<ContentValues>> callback) {
		MyApplication.EXECUTOR_SERVICE.execute(new Runnable() {
			@Override
			public void run() {
				final String selection = CalendarContract.Attendees.DTSTART + ">=? AND " +
						CalendarContract.Attendees.DTEND + "<=?";
				final String[] selectionArgs = {String.valueOf(begin.toInstant().getEpochSecond() * 1000L),
						String.valueOf(end.toInstant().getEpochSecond() * 1000L)};
				Cursor cursor = context.getContentResolver().query(CalendarContract.Attendees.CONTENT_URI, null, selection, selectionArgs,
						null);
				List<ContentValues> attendeeList = new ArrayList<>();

				if (cursor != null) {
					while (cursor.moveToNext()) {
						ContentValues attendee = new ContentValues();
						String[] keys = cursor.getColumnNames();
						for (String key : keys) {
							attendee.put(key, cursor.getString(cursor.getColumnIndex(key)));
						}

						attendeeList.add(attendee);
					}
					cursor.close();
				}

				callback.onResultSuccessful(attendeeList);
			}
		});

	}

	@SuppressLint("Range")
	public static void loadReminders(Context context, Long eventId, BackgroundCallback<List<ContentValues>> callback) {
		MyApplication.EXECUTOR_SERVICE.execute(new Runnable() {
			@Override
			public void run() {
				Cursor cursor = CalendarContract.Reminders.query(context.getContentResolver(), eventId, null);

				List<ContentValues> reminderList = new ArrayList<>();

				if (cursor != null) {
					while (cursor.moveToNext()) {
						ContentValues reminder = new ContentValues();
						String[] keys = cursor.getColumnNames();
						for (String key : keys) {
							reminder.put(key, cursor.getString(cursor.getColumnIndex(key)));
						}
						reminderList.add(reminder);
					}
					cursor.close();
				}
				callback.onResultSuccessful(reminderList);
			}
		});

	}


	public static void loadEvents(Context context, String calendarId, BackgroundCallback<List<EventObj>> callback) {
		String[] selectionArgs = {calendarId};
		String selection = CalendarContract.Events.CALENDAR_ID + "=?";
		loadEvents(context, selection, selectionArgs, callback);
	}

	public static void loadEvent(Context context, String eventId, BackgroundCallback<List<EventObj>> callback) {
		String[] selectionArgs = {eventId};
		String selection = CalendarContract.Events._ID + "=?";
		loadEvents(context, selection, selectionArgs, callback);
	}

	@SuppressLint("Range")
	public static void loadEvents(Context context, String selection, String[] selectionArgs, BackgroundCallback<List<EventObj>> callback) {
		MyApplication.EXECUTOR_SERVICE.execute(new Runnable() {
			@Override
			public void run() {
				final List<EventObj> eventObjList = new ArrayList<>();
				Cursor cursor = context.getContentResolver().query(CalendarContract.Events.CONTENT_URI, null, selection, selectionArgs,
						null, null);

				List<ContentValues> eventList = new ArrayList<>();

				String value = null;

				if (cursor != null) {
					while (cursor.moveToNext()) {
						ContentValues event = new ContentValues();
						String[] keys = cursor.getColumnNames();
						for (String key : keys) {
							value = cursor.getString(cursor.getColumnIndex(key));
							if (value != null && !value.isEmpty()) {
								event.put(key, value);
							}
						}
						eventList.add(event);
						EventObj eventObj = new EventObj();
						eventObj.setEvent(event);
						eventObjList.add(eventObj);
					}
					cursor.close();
				}

				int i = 0;
				for (ContentValues event : eventList) {
					cursor = CalendarContract.Reminders.query(context.getContentResolver(), event.getAsLong(CalendarContract.Events._ID),
							null);

					List<ContentValues> reminderList = new ArrayList<>();

					if (cursor != null) {
						while (cursor.moveToNext()) {
							ContentValues reminder = new ContentValues();
							String[] keys = cursor.getColumnNames();
							for (String key : keys) {
								value = cursor.getString(cursor.getColumnIndex(key));
								if (value != null && !value.isEmpty()) {
									reminder.put(key, value);
								}
							}
							reminderList.add(reminder);
						}
						cursor.close();
					}
					eventObjList.get(i).setReminderList(reminderList);
					i++;
				}

				i = 0;
				for (ContentValues event : eventList) {
					cursor = CalendarContract.Attendees.query(context.getContentResolver(), event.getAsLong(CalendarContract.Events._ID), null);
					List<ContentValues> attendeeList = new ArrayList<>();

					if (cursor != null) {
						while (cursor.moveToNext()) {
							ContentValues attendee = new ContentValues();
							String[] keys = cursor.getColumnNames();
							for (String key : keys) {
								value = cursor.getString(cursor.getColumnIndex(key));
								if (value != null && !value.isEmpty()) {
									attendee.put(key, value);
								}
							}

							attendeeList.add(attendee);
						}
						cursor.close();
						eventObjList.get(i).setAttendeeList(attendeeList);
						i++;
					}
				}


				callback.onResultSuccessful(eventObjList);

			}
		});
	}

	@SuppressLint("Range")
	public static void loadEvents(Context context, Long alarmTime, BackgroundCallback<List<ContentValues>> callback) {
		String selection = CalendarContract.CalendarAlerts.ALARM_TIME + "=?";
		String[] selectionArgs = {alarmTime.toString()};

		Cursor cursor = context.getContentResolver().query(CalendarContract.CalendarAlerts.CONTENT_URI, null, selection, selectionArgs, null);
		List<ContentValues> eventList = new ArrayList<>();

		while (cursor.moveToNext()) {
			ContentValues event = new ContentValues();
			String[] keys = cursor.getColumnNames();
			for (String key : keys) {
				event.put(key, cursor.getString(cursor.getColumnIndex(key)));
			}

			eventList.add(event);
		}
		cursor.close();

		callback.onResultSuccessful(eventList);
	}

	public static void removeEvent(Context context, ContentValues event, BackgroundCallback<ContentValues> callback) {
		MyApplication.EXECUTOR_SERVICE.execute(new Runnable() {
			@Override
			public void run() {
				int result = context.getContentResolver().delete(ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, event.getAsLong(CalendarContract.Events._ID))
						, null, null);
				callback.onResultSuccessful(event);
			}
		});
	}

	public static class EventObj implements Parcelable {
		private String date;
		private boolean isMyEvent;
		private ContentValues event;
		private List<ContentValues> attendeeList;
		private List<ContentValues> reminderList;

		public EventObj() {
		}


		protected EventObj(Parcel in) {
			date = in.readString();
			isMyEvent = in.readByte() != 0;
			event = in.readParcelable(ContentValues.class.getClassLoader());
			attendeeList = in.createTypedArrayList(ContentValues.CREATOR);
			reminderList = in.createTypedArrayList(ContentValues.CREATOR);
		}

		public static final Creator<EventObj> CREATOR = new Creator<EventObj>() {
			@Override
			public EventObj createFromParcel(Parcel in) {
				return new EventObj(in);
			}

			@Override
			public EventObj[] newArray(int size) {
				return new EventObj[size];
			}
		};

		public String getDate() {
			return date;
		}

		public void setDate(String date) {
			this.date = date;
		}

		public ContentValues getEvent() {
			return event;
		}

		public void setEvent(ContentValues event) {
			this.event = event;
		}

		public List<ContentValues> getAttendeeList() {
			return attendeeList;
		}

		public void setAttendeeList(List<ContentValues> attendeeList) {
			this.attendeeList = attendeeList;
		}

		public List<ContentValues> getReminderList() {
			return reminderList;
		}

		public void setReminderList(List<ContentValues> reminderList) {
			this.reminderList = reminderList;
		}

		public boolean isMyEvent() {
			return isMyEvent;
		}

		public void setMyEvent(boolean myEvent) {
			isMyEvent = myEvent;
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeString(date);
			dest.writeByte((byte) (isMyEvent ? 1 : 0));
			dest.writeParcelable(event, flags);
			dest.writeTypedList(attendeeList);
			dest.writeTypedList(reminderList);
		}
	}
}
