package com.lifedawn.capstoneapp.reminder.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.provider.CalendarContract;

import com.google.api.services.calendar.Calendar;
import com.lifedawn.capstoneapp.common.constants.ActionConstant;
import com.lifedawn.capstoneapp.common.util.NotificationHelper;

public class PromiseNotificationReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();

		if (action.equals(ActionConstant.PROMISE_REMINDER.name())) {
			//약속 알림
			NotificationHelper notificationHelper = new NotificationHelper(context);
			NotificationHelper.NotificationItem notificationItem =
					notificationHelper.createNotificationItem(NotificationHelper.NotificationType.PROMISE_REMINDER);

			Notification.Builder builder = notificationItem.getBuilder();

			//알림 관련 처리

			Notification notification = notificationItem.getBuilder().build();
			NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.notify((int) System.currentTimeMillis(), notification);
		}
	}
}