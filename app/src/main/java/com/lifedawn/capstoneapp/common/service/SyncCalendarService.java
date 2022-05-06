package com.lifedawn.capstoneapp.common.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.common.util.NotificationHelper;

public class SyncCalendarService extends Service {
	public SyncCalendarService() {
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		showNotification();
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

	}

	private void showNotification() {
		NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());
		NotificationHelper.NotificationItem notificationItem =
				notificationHelper.createNotificationItem(NotificationHelper.NotificationType.SYNC_CALENDAR);

		NotificationCompat.Builder builder = notificationItem.getBuilder();
		builder.setSmallIcon(R.mipmap.ic_launcher_round).setContentText(getString(R.string.syncCalendar)).setContentTitle(getString(R.string.syncing_calendar))
				.setWhen(0).setOngoing(true);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
			builder.setPriority(NotificationCompat.PRIORITY_LOW).setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
		}

		Notification notification = builder.build();
		startForeground(notificationItem.getNotificationType().NotificationId(), notification);
	}
}