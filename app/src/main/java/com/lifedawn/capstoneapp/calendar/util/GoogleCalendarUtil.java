package com.lifedawn.capstoneapp.calendar.util;

import android.content.Context;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.lifedawn.capstoneapp.common.interfaces.OnHttpApiCallback;
import com.lifedawn.capstoneapp.main.MyApplication;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class GoogleCalendarUtil {
	private static GoogleCalendarUtil instance;
	private Context context;
	private Calendar calendarService;
	private ExecutorService executorService;
	
	public static GoogleCalendarUtil getInstance(Context context) {
		if (instance == null) {
			instance = new GoogleCalendarUtil(context);
		}
		return instance;
	}
	
	private GoogleCalendarUtil(Context context) {
		this.context = context;
		this.executorService = MyApplication.EXECUTOR_SERVICE;
	}
	
	public void createService(GoogleAccountCredential googleAccountCredential) {
		final HttpTransport httpTransport = new NetHttpTransport();
		final JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
		calendarService = new Calendar.Builder(httpTransport, jsonFactory, googleAccountCredential).setApplicationName("promise").build();
	}
	
	public void getCalendarListEntry(OnHttpApiCallback<List<CalendarListEntry>> callback) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				try {
					List<CalendarListEntry> calendarListEntries = new ArrayList<>();
					
					String pageToken = null;
					do {
						CalendarList calendarList = calendarService.calendarList().list().setPageToken(pageToken).execute();
						calendarListEntries.addAll(calendarList.getItems());
						pageToken = calendarList.getNextPageToken();
					} while (pageToken != null);
					
					callback.onResultSuccessful(calendarListEntries);
				} catch (Exception e) {
					callback.onResultFailed(e);
				}
			}
		});
		
	}
	
	public void insertCalendar(com.google.api.services.calendar.model.Calendar newCalendar,
			OnHttpApiCallback<com.google.api.services.calendar.model.Calendar> callback) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				try {
					com.google.api.services.calendar.model.Calendar createdCalendar = calendarService.calendars().insert(
							newCalendar).execute();
					
					callback.onResultSuccessful(createdCalendar);
				} catch (Exception e) {
					callback.onResultFailed(e);
				}
			}
		});
		
	}
	
	public void getEvents(String calendarId, OnHttpApiCallback<List<Event>> callback) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				try {
					List<Event> eventList = new ArrayList<>();
					
					String pageToken = null;
					do {
						Events events = calendarService.events().list(calendarId).setPageToken(pageToken).execute();
						pageToken = events.getNextPageToken();
						eventList.addAll(events.getItems());
					} while (pageToken != null);
					
					callback.onResultSuccessful(eventList);
				} catch (Exception e) {
					callback.onResultFailed(e);
				}
			}
		});
		
	}
	
	public void getEvent(String calendarId, String eventId, OnHttpApiCallback<Event> callback) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				try {
					Event event = calendarService.events().get(calendarId, eventId).execute();
					callback.onResultSuccessful(event);
				} catch (Exception e) {
					callback.onResultFailed(e);
				}
			}
		});
		
	}
	
	public void insertEvent(String calendarId, Event newEvent, OnHttpApiCallback<Event> callback) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				try {
					Event event = calendarService.events().insert(calendarId, newEvent).execute();
					callback.onResultSuccessful(event);
				} catch (Exception e) {
					callback.onResultFailed(e);
				}
			}
		});
		
	}
	
	public void getInstances(String calendarId, String eventId, OnHttpApiCallback<List<Event>> callback) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				try {
					List<Event> eventList = new ArrayList<>();
					String pageToken = null;
					do {
						Events events = calendarService.events().instances(calendarId, eventId).setPageToken(pageToken).execute();
						eventList.addAll(events.getItems());
						pageToken = events.getNextPageToken();
					} while (pageToken != null);
					callback.onResultSuccessful(eventList);
				} catch (Exception e) {
					callback.onResultFailed(e);
				}
			}
		});
		
	}
	
	public void updateEvent(String calendarId, Event updateEvent, OnHttpApiCallback<Event> callback) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				try {
					Event updatedEvent = calendarService.events().update(calendarId, updateEvent.getId(), updateEvent).execute();
					callback.onResultSuccessful(updatedEvent);
				} catch (Exception e) {
					callback.onResultFailed(e);
				}
			}
		});
		
	}
	
	public void deleteEvent(String calendarId, Event deleteEvent, OnHttpApiCallback<Event> callback) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				try {
					calendarService.events().delete(calendarId, deleteEvent.getId()).execute();
					callback.onResultSuccessful(deleteEvent);
				} catch (Exception e) {
					callback.onResultFailed(e);
				}
			}
		});
		
	}
}
