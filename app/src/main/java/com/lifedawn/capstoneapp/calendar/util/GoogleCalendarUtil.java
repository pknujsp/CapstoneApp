package com.lifedawn.capstoneapp.calendar.util;

import android.app.Activity;
import android.widget.Toast;

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
import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.account.util.GoogleAccountLifeCycleObserver;
import com.lifedawn.capstoneapp.common.interfaces.OnHttpApiCallback;
import com.lifedawn.capstoneapp.main.MyApplication;

import java.io.IOException;
import java.util.List;
import java.util.TimeZone;

public class GoogleCalendarUtil {
	private static Calendar calendarService;
	public static final String MAIN_CALENDAR_SUMMARY = "약속";
	private GoogleAccountLifeCycleObserver googleAccountLifeCycleObserver;
	
	public GoogleCalendarUtil(GoogleAccountLifeCycleObserver googleAccountLifeCycleObserver) {
		this.googleAccountLifeCycleObserver = googleAccountLifeCycleObserver;
	}
	
	public Calendar getCalendarService(GoogleAccountCredential googleAccountCredential) {
		if (calendarService == null) {
			try {
				final HttpTransport httpTransport = new NetHttpTransport();
				final JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
				
				calendarService = new Calendar.Builder(httpTransport, jsonFactory, googleAccountCredential).setApplicationName(
						"promise").build();
				
			} catch (Exception e) {
				googleAccountLifeCycleObserver.launchUserRecoverableAuthIntent(((UserRecoverableAuthIOException) e).getIntent(),
						new ActivityResultCallback<ActivityResult>() {
							@Override
							public void onActivityResult(ActivityResult result) {
								if (result.getResultCode() == Activity.RESULT_OK) {
								
								} else {
									Toast.makeText(googleAccountCredential.getContext(), R.string.denied_access_to_calendar,
											Toast.LENGTH_SHORT).show();
								}
								
							}
						});
			}
		}
		return calendarService;
	}
	
	public void addPromiseCalendar(Calendar calendarService, OnHttpApiCallback<com.google.api.services.calendar.model.Calendar> callback) {
		com.google.api.services.calendar.model.Calendar newCalendar = new com.google.api.services.calendar.model.Calendar();
		newCalendar.setSummary(MAIN_CALENDAR_SUMMARY).setTimeZone(TimeZone.getDefault().getID());
		
		MyApplication.EXECUTOR_SERVICE.execute(new Runnable() {
			@Override
			public void run() {
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
	
	public void existingPromiseCalendar(Calendar calendarService, OnHttpApiCallback<CalendarListEntry> callback) {
		MyApplication.EXECUTOR_SERVICE.execute(new Runnable() {
			@Override
			public void run() {
				boolean existing = false;
				CalendarListEntry promiseCalendarListEntry = null;
				try {
					
					String pageToken = null;
					do {
						CalendarList calendarList = null;
						calendarList = calendarService.calendarList().list().setPageToken(pageToken).execute();
						
						List<CalendarListEntry> items = calendarList.getItems();
						
						for (CalendarListEntry entry : items) {
							if (entry.getSummary().equals(MAIN_CALENDAR_SUMMARY)) {
								promiseCalendarListEntry = entry;
								existing = true;
								break;
							}
						}
						pageToken = calendarList.getNextPageToken();
					} while (pageToken != null);
					
				} catch (Exception e1) {
					e1.printStackTrace();
					
					if (e1 instanceof UserRecoverableAuthIOException) {
						googleAccountLifeCycleObserver.launchUserRecoverableAuthIntent(((UserRecoverableAuthIOException) e1).getIntent(),
								new ActivityResultCallback<ActivityResult>() {
									@Override
									public void onActivityResult(ActivityResult result) {
										if (result.getResultCode() == Activity.RESULT_OK) {
										
										}
									}
								});
					}
				}
				callback.onResultSuccessful(promiseCalendarListEntry);
				
			}
		});
	}
	
	public void sendResponseForInvitedPromise(Calendar calendarService, String calendarId, String myEmail, Event event, boolean acceptance,
			OnHttpApiCallback<Boolean> callback) {
		MyApplication.EXECUTOR_SERVICE.execute(new Runnable() {
			@Override
			public void run() {
				try {
					for (EventAttendee eventAttendee : event.getAttendees()) {
						if (eventAttendee.getEmail().equals(myEmail) && !eventAttendee.getOrganizer()) {
							eventAttendee.setResponseStatus(acceptance ? "accepted" : "declined");
							break;
						}
					}
					
					Event updatedEvent = calendarService.events().update(calendarId, event.getId(), event).execute();
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
}

