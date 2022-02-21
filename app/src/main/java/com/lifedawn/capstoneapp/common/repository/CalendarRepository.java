package com.lifedawn.capstoneapp.common.repository;

import android.app.Activity;
import android.content.Context;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;

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
import java.util.List;
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
		final HttpTransport httpTransport = new NetHttpTransport();
		final JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

		try {
			calendarService = new Calendar.Builder(httpTransport, jsonFactory, googleAccountCredential).setApplicationName(
					"promise").build();
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
									callback.onResultFailed(new Exception("rejected google calendar permission"));
								}

							}
						});
			}
		}
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

}
