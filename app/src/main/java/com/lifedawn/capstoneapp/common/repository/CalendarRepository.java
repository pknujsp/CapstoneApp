package com.lifedawn.capstoneapp.common.repository;

import android.accounts.Account;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncStatusObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.CalendarContract;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.annotation.NonNull;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.lifedawn.capstoneapp.account.GoogleAccountLifeCycleObserver;
import com.lifedawn.capstoneapp.common.interfaces.HttpCallback;
import com.lifedawn.capstoneapp.common.interfaces.BackgroundCallback;
import com.lifedawn.capstoneapp.common.repositoryinterface.ICalendarRepository;
import com.lifedawn.capstoneapp.main.MyApplication;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class CalendarRepository implements ICalendarRepository {
	public static final String MAIN_CALENDAR_SUMMARY = "약속";
	private static Calendar calendarService;

	private Context context;

	public CalendarRepository(Context context) {
		this.context = context;
	}

	@Override
	public void saveEvent(Calendar calendarService, Event newEvent, String calendarId, HttpCallback<Event> callback) {
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
	public void updateEvent(Calendar calendarService, Event editEvent, String calendarId, HttpCallback<Event> callback) {
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
	public void sendResponseForInvitedPromise(Calendar calendarService, String calendarId, String myEmail, Event event, boolean acceptance,
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
											callback.onResultFailed(new Exception("rejected google calendar permission"));
										}

									}
								});
					}
				}
				callback.onResultSuccessful(calendarService);
			}
		});

	}

	public Calendar getCalendarService() {
		return calendarService;
	}

	@Override
	public void addPromiseCalendar(Calendar calendarService, BackgroundCallback<com.google.api.services.calendar.model.Calendar> callback) {
		MyApplication.EXECUTOR_SERVICE.execute(new Runnable() {
			@Override
			public void run() {
				com.google.api.services.calendar.model.Calendar newCalendar = new com.google.api.services.calendar.model.Calendar();
				newCalendar.setSummary(MAIN_CALENDAR_SUMMARY).setTimeZone(TimeZone.getDefault().getID());
				com.google.api.services.calendar.model.Calendar createdCalendar = null;
				try {
					createdCalendar = calendarService.calendars().insert(newCalendar).execute();
				} catch (IOException e) {
					e.printStackTrace();
				}

				callback.onResultSuccessful(createdCalendar);
			}
		});
	}

	@Override
	public void existingPromiseCalendar(Calendar calendarService, BackgroundCallback<CalendarListEntry> callback) {
		MyApplication.EXECUTOR_SERVICE.execute(new Runnable() {
			@Override
			public void run() {
				try {
					CalendarListEntry promiseCalendarListEntry = null;
					String pageToken = null;

					do {
						CalendarList calendarList = null;
						calendarList = calendarService.calendarList().list().setPageToken(pageToken).execute();

						List<CalendarListEntry> items = calendarList.getItems();

						for (CalendarListEntry entry : items) {
							if (entry.getSummary().equals(MAIN_CALENDAR_SUMMARY)) {
								promiseCalendarListEntry = entry;
								break;
							}
						}
						pageToken = calendarList.getNextPageToken();
					} while (pageToken != null);
					callback.onResultSuccessful(promiseCalendarListEntry);
				} catch (Exception e) {
					e.printStackTrace();
					callback.onResultFailed(e);
				}

			}
		});
	}

	@Override
	public void syncCalendars(Account account, BackgroundCallback<Boolean> callback) {
		CalendarSyncStatusObserver calendarSyncStatusObserver = new CalendarSyncStatusObserver();

		calendarSyncStatusObserver.setProviderHandle(ContentResolver.addStatusChangeListener(ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE, calendarSyncStatusObserver));
		calendarSyncStatusObserver.setSyncCallback(callback);
		calendarSyncStatusObserver.setAccount(account);

		Bundle arguments = new Bundle();
		arguments.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
		arguments.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
		ContentResolver.requestSync(account, CalendarContract.AUTHORITY, arguments);
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
		private BackgroundCallback<Boolean> syncCallback;
		private Account account;

		public Object getmProviderHandle() {
			return mProviderHandle;
		}

		public void setAccount(Account account) {
			this.account = account;
		}

		public void setSyncCallback(BackgroundCallback<Boolean> syncCallback) {
			this.syncCallback = syncCallback;
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


	public static void loadMyEvents(Context context, Account account, String calendarId, BackgroundCallback<List<EventObj>> callback) {
		String selection = CalendarContract.Events.CALENDAR_ID + "=? AND " + CalendarContract.Events.ACCOUNT_NAME + "=?";
		String[] selectionArgs = new String[]{calendarId, account.name};
		loadAllEvents(context, selection, selectionArgs, callback);
	}

	@SuppressLint("Range")
	public static void loadReceivedInvitationEvents(Context context, Account account,
	                                                BackgroundCallback<List<EventObj>> callback) {
		String selection = CalendarContract.Events.ORGANIZER + " LIKE '%@gmail.com' AND " + CalendarContract.Events.ORGANIZER + "!=?";
		String[] selectionArgs = {account.name};
		loadAllEvents(context, selection, selectionArgs, callback);
	}

	@SuppressLint("Range")
	public static void loadAllEvents(Context context, String calendarId, ZonedDateTime begin, ZonedDateTime end,
	                                 BackgroundCallback<List<ContentValues>> callback) {
		MyApplication.EXECUTOR_SERVICE.execute(new Runnable() {
			@Override
			public void run() {
				final String selection = CalendarContract.Events.CALENDAR_ID + "=? AND " + CalendarContract.Events.DTSTART + ">=? AND " +
						CalendarContract.Events.DTEND + "<=?";
				final String[] selectionArgs = {calendarId, String.valueOf(begin.toInstant().getEpochSecond() * 1000L),
						String.valueOf(end.toInstant().getEpochSecond() * 1000L)};

				Cursor cursor = context.getContentResolver().query(CalendarContract.Events.CONTENT_URI, null, selection, selectionArgs,
						null);

				List<ContentValues> eventList = new ArrayList<>();

				if (cursor != null) {
					while (cursor.moveToNext()) {
						ContentValues event = new ContentValues();
						String[] keys = cursor.getColumnNames();
						for (String key : keys) {
							event.put(key, cursor.getString(cursor.getColumnIndex(key)));
						}
						eventList.add(event);
					}
					cursor.close();
				}
				callback.onResultSuccessful(eventList);
			}
		});

	}


	@SuppressLint("Range")
	public static void loadCalendar(Context context, Account account, BackgroundCallback<ContentValues> callback) {
		MyApplication.EXECUTOR_SERVICE.execute(new Runnable() {
			@Override
			public void run() {
				final String selection = CalendarContract.Calendars.ACCOUNT_NAME + "=? AND " + CalendarContract.Calendars.IS_PRIMARY + "=?";
				final String[] selectionArgs = new String[]{account.name, "1"};
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
	public static void loadAttendees(Context context, Long eventId, BackgroundCallback<List<ContentValues>> callback) {
		MyApplication.EXECUTOR_SERVICE.execute(new Runnable() {
			@Override
			public void run() {
				Cursor cursor = CalendarContract.Attendees.query(context.getContentResolver(), eventId, null);
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


	public static void loadAllEvents(Context context, String calendarId, BackgroundCallback<List<EventObj>> callback) {
		String[] selectionArgs = {calendarId};
		String selection = CalendarContract.Events.CALENDAR_ID + "=?";
		loadAllEvents(context, selection, selectionArgs, callback);
	}

	@SuppressLint("Range")
	private static void loadAllEvents(Context context, String selection, String[] selectionArgs, BackgroundCallback<List<EventObj>> callback) {
		MyApplication.EXECUTOR_SERVICE.execute(new Runnable() {
			@Override
			public void run() {
				List<EventObj> eventObjList = new ArrayList<>();
				Cursor cursor = context.getContentResolver().query(CalendarContract.Events.CONTENT_URI, null, selection, selectionArgs,
						null);

				List<ContentValues> eventList = new ArrayList<>();

				if (cursor != null) {
					while (cursor.moveToNext()) {
						ContentValues event = new ContentValues();
						String[] keys = cursor.getColumnNames();
						for (String key : keys) {
							event.put(key, cursor.getString(cursor.getColumnIndex(key)));
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
								reminder.put(key, cursor.getString(cursor.getColumnIndex(key)));
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
								attendee.put(key, cursor.getString(cursor.getColumnIndex(key)));
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

	public static class EventObj implements Parcelable {
		private ContentValues event;
		private List<ContentValues> attendeeList;
		private List<ContentValues> reminderList;

		public EventObj() {
		}

		protected EventObj(Parcel in) {
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

		public ContentValues getEvent() {
			return event;
		}

		public EventObj setEvent(ContentValues event) {
			this.event = event;
			return this;
		}

		public List<ContentValues> getAttendeeList() {
			return attendeeList;
		}

		public EventObj setAttendeeList(List<ContentValues> attendeeList) {
			this.attendeeList = attendeeList;
			return this;
		}

		public List<ContentValues> getReminderList() {
			return reminderList;
		}

		public EventObj setReminderList(List<ContentValues> reminderList) {
			this.reminderList = reminderList;
			return this;
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeParcelable(event, flags);
			dest.writeTypedList(attendeeList);
			dest.writeTypedList(reminderList);
		}
	}
}
