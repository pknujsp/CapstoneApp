package com.lifedawn.capstoneapp.reminder.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
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
import com.lifedawn.capstoneapp.reminder.NotificationActivity;

import java.util.List;
import java.util.Set;

public class PromiseNotificationReceiver extends BroadcastReceiver {

	public static final String ACTION_CONFIRM_EVENT = "confirm_event";

	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		Log.e("PromiseNotificationReceiver", action);

		if (action.equals(CalendarContract.ACTION_EVENT_REMINDER)) {

			Bundle bundle = new Bundle();
			bundle.putLong(CalendarContract.CalendarAlerts.ALARM_TIME, intent.getExtras().getLong(CalendarContract.CalendarAlerts.ALARM_TIME));
			bundle.putString("action", intent.getAction());

			Intent notificationServiceIt = new Intent(context, NotificationService.class);
			notificationServiceIt.setAction(action);
			notificationServiceIt.putExtras(bundle);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				context.startForegroundService(notificationServiceIt);
			} else {
				context.startService(notificationServiceIt);
			}
		} else if (action.equals(ACTION_CONFIRM_EVENT)) {
			Bundle bundle = intent.getExtras();
			final int notificationId = bundle.getInt("notificationId");

			context.stopService(new Intent(context, NotificationService.class));

			NotificationManager notificationManager = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.cancel(notificationId);

			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) System.currentTimeMillis(),
					new Intent(context, NotificationActivity.EndNotificationReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT);

			try {
				pendingIntent.send();
			} catch (PendingIntent.CanceledException e) {
				e.printStackTrace();
			}
		}
	}
}