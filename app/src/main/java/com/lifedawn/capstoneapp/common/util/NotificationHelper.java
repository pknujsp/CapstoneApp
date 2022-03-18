package com.lifedawn.capstoneapp.common.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.lifedawn.capstoneapp.R;

public class NotificationHelper {
	private Context context;

	public NotificationHelper(Context context) {
		this.context = context;
	}

	public void createNotificationChannel(NotificationType notificationType) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

			//알림 채널 생성 여부 확인
			if (notificationManager.getNotificationChannel(notificationType.getChannelId()) == null) {
				String notificationName = null;
				String notificationDescription = null;
				int importance = 0;

				if (notificationType == NotificationType.PROMISE_REMINDER) {
					notificationName = context.getString(R.string.promise_reminder_notification_channel_name);
					notificationDescription = context.getString(R.string.promise_reminder_notification_channel_description);
					importance = NotificationManager.IMPORTANCE_HIGH;
				}

				NotificationChannel notificationChannel = new NotificationChannel(notificationType.getChannelId(), notificationName, importance);
				notificationChannel.setDescription(notificationDescription);
				notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

				notificationManager.createNotificationChannel(notificationChannel);
			}
		}
	}

	public NotificationItem createNotificationItem(NotificationType notificationType) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			createNotificationChannel(notificationType);
		}
		NotificationItem notificationItem = new NotificationItem(notificationType);
		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, notificationType.getChannelId());

		if (notificationType == NotificationType.PROMISE_REMINDER) {
			notificationBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
		}
		notificationItem.setBuilder(notificationBuilder);

		return notificationItem;
	}

	public static class NotificationItem {
		private final NotificationType notificationType;
		private NotificationCompat.Builder builder;

		public NotificationItem(NotificationType notificationType) {
			this.notificationType = notificationType;
		}

		public void setBuilder(NotificationCompat.Builder builder) {
			this.builder = builder;
		}

		public NotificationCompat.Builder getBuilder() {
			return builder;
		}

		public NotificationType getNotificationType() {
			return notificationType;
		}
	}

	public enum NotificationType {
		PROMISE_REMINDER("1000");

		private final String channelId;

		NotificationType(String channelId) {
			this.channelId = channelId;
		}

		public String getChannelId() {
			return channelId;
		}
	}
}
