package com.lifedawn.capstoneapp.reminder.notifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.provider.CalendarContract;

import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;
import androidx.work.Configuration;

import com.lifedawn.capstoneapp.MainActivity;
import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.common.constants.SharedPreferenceConstant;
import com.lifedawn.capstoneapp.common.interfaces.BackgroundCallback;
import com.lifedawn.capstoneapp.common.repository.CalendarRepository;
import com.lifedawn.capstoneapp.common.util.NotificationHelper;
import com.lifedawn.capstoneapp.reminder.NotificationActivity;

import java.util.ArrayList;
import java.util.List;

public class PromiseNotificationJobService extends JobService {
	public PromiseNotificationJobService() {
		Configuration.Builder builder = new Configuration.Builder();
		builder.setJobSchedulerJobIdRange(0, Integer.MAX_VALUE);
	}

	@Override
	public boolean onStartJob(JobParameters params) {
		final String action = params.getExtras().getString("action");

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		final boolean notificationWakeUpDisplay =
				sharedPreferences.getBoolean(SharedPreferenceConstant.REMINDER_WAKE.getVal(), true);

		if (action.equals(CalendarContract.ACTION_EVENT_REMINDER)) {
			PersistableBundle bundle = params.getExtras();
			final Long alarmTime = bundle.getLong(CalendarContract.CalendarAlerts.ALARM_TIME);

			CalendarRepository.loadEvents(getApplicationContext(), alarmTime, new BackgroundCallback<List<ContentValues>>() {
				@Override
				public void onResultSuccessful(List<ContentValues> eventList) {
					NotificationManager notificationManager = getSystemService(NotificationManager.class);
					NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());
					final String title = getString(R.string.promise_reminder);

					int notificationId = (int) System.currentTimeMillis();
					for (ContentValues event : eventList) {
						//약속 알림
						NotificationHelper.NotificationItem notificationItem =
								notificationHelper.createNotificationItem(NotificationHelper.NotificationType.PROMISE_REMINDER);
						NotificationCompat.Builder builder = notificationItem.getBuilder();
						builder.setSmallIcon(R.drawable.ic_baseline_access_alarm_24).setContentTitle(title)
								.setContentText(event.getAsString(CalendarContract.Events.TITLE)).
								setAutoCancel(true);

						Intent clickIntent = new Intent(getApplicationContext(), MainActivity.class);
						clickIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

						PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), notificationId, clickIntent,
								PendingIntent.FLAG_ONE_SHOT);
						builder.setContentIntent(pendingIntent);
						//알림 관련 처리
						notificationManager.notify(notificationId++, builder.build());
					}

					if (notificationWakeUpDisplay) {
						Bundle activityBundle = new Bundle();
						activityBundle.putParcelableArrayList("eventList", (ArrayList<? extends Parcelable>) eventList);

						Intent activityIntent = new Intent(getApplicationContext(), NotificationActivity.class);
						activityIntent.putExtras(activityBundle);
						activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(activityIntent);
					}

					jobFinished(params, false);
				}

				@Override
				public void onResultFailed(Exception e) {

				}
			});

		}
		return true;
	}

	@Override
	public boolean onStopJob(JobParameters params) {
		return false;
	}
}
