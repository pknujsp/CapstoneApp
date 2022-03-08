package com.lifedawn.capstoneapp.reminder.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.provider.CalendarContract;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.api.services.calendar.Calendar;
import com.lifedawn.capstoneapp.MainActivity;
import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.common.constants.ActionConstant;
import com.lifedawn.capstoneapp.common.interfaces.BackgroundCallback;
import com.lifedawn.capstoneapp.common.repository.CalendarRepository;
import com.lifedawn.capstoneapp.common.util.NotificationHelper;

import java.util.List;

public class PromiseNotificationReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		Log.e("PromiseNotificationReceiver", action);

		/*
		 android.database.sqlite.SQLiteException: no such column: alarmTime (code 1 SQLITE_ERROR[1]):
		 , while compiling: SELECT originalAllDay, account_type, exrule, facebook_schedule_id, mutators,
		  originalInstanceTime, sticker_type, rrule, secExtraCal, secOriginalSyncId, contactEventType, calendar_access_level,
		   facebook_photo_url, eventColor_index, guestsCanInviteOthers, facebook_mem_count, allowedAttendeeTypes,
		    guestsCanSeeGuests, latitude, availability, lastSynced, facebook_hostname, rdate, cal_sync10, account_name,
		    calendar_color, dirty, calendar_timezone, packageId, hasAlarm, uid2445, deleted, organizer, eventStatus,
		    customAppUri, canModifyTimeZone, customAppPackage, displayColor, original_id, secExtraOthers, calendar_displayName,
		     sticker_group, sticker_ename, allDay, allowedReminders, filepath, canOrganizerRespond, lastDate, longitude,
		      contact_account_type, visible, calendar_id, hasExtendedProperties, selfAttendeeStatus, allowedAvailability,
		       isOrganizer, _sync_id, name, phone_number, calendar_color_index, _id, facebook_post_time, dtstart, sync_data9,
		       sync_data8, exdate, sync_data7, secTimeStamp, sync_data6, contact_data_id, sync_data1, description, eventTimezone,
		        title, contact_id, ownerAccount, sync_data5, sync_data4, sync_data3, sync_data2, duration, guestsCanModify, cal_sync3,
		         cal_sync2, maxReminders, cal_sync1, cal_sync7, cal_sync6, cal_sync5, availabilityStatus, cal_sync4, cal_sync9,
		         cal_sync8, setLunar, facebook_service_provider, accessLevel, eventLocation, facebook_event_type, facebook_owner,
		          eventColor, secExtra4, eventEndTimezone, secExtra3, original_sync_id, hasAttendeeData, secExtra5, dtend,
		           sync_data10, secExtra2, secExtra1 FROM view_events WHERE (lastSynced = 0 AND (alarmTime = ?))

		 */
		if (action.equals(CalendarContract.ACTION_EVENT_REMINDER)) {
			final Long alarmTime = intent.getExtras().getLong(CalendarContract.CalendarAlerts.ALARM_TIME);
			final String selection = CalendarContract.CalendarAlerts.ALARM_TIME + " = ?";
			final String[] selectionArgs = {alarmTime.toString()};

			CalendarRepository.loadEvents(context, selection, selectionArgs, new BackgroundCallback<List<CalendarRepository.EventObj>>() {
				@Override
				public void onResultSuccessful(List<CalendarRepository.EventObj> eventObjList) {
					final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
					final NotificationHelper notificationHelper = new NotificationHelper(context);
					final String title = context.getString(R.string.promise_reminder);

					int notificationId = (int) System.currentTimeMillis();

					for (CalendarRepository.EventObj eventObj : eventObjList) {
						//약속 알림
						NotificationHelper.NotificationItem notificationItem =
								notificationHelper.createNotificationItem(NotificationHelper.NotificationType.PROMISE_REMINDER);
						final NotificationCompat.Builder builder = notificationItem.getBuilder();
						builder.setContentTitle(title).setContentText(eventObj.getEvent().getAsString(CalendarContract.Events.TITLE)).
								setAutoCancel(true);

						Intent clickIntent = new Intent(context, MainActivity.class);
						clickIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

						PendingIntent pendingIntent = PendingIntent.getActivity(context, notificationId, clickIntent,
								PendingIntent.FLAG_UPDATE_CURRENT);

						builder.setContentIntent(pendingIntent);

						//알림 관련 처리
						notificationManager.notify(notificationId++, builder.build());
					}
				}

				@Override
				public void onResultFailed(Exception e) {

				}
			});

		}
	}
}