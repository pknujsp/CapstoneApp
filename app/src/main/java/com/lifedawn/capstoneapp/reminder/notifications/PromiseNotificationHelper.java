package com.lifedawn.capstoneapp.reminder.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class PromiseNotificationHelper {
	private Context context;

	public PromiseNotificationHelper(Context context) {
		this.context = context;
	}

	public void setNotification(int eventId) {
		PendingIntent pendingIntent = getPendingIntent(eventId,PendingIntent.FLAG_UPDATE_CURRENT);
	}

	public PendingIntent getPendingIntent(int eventId, int flags) {
		Intent intent = new Intent(context, PromiseNotificationReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, eventId, intent, flags);
		return pendingIntent;
	}

	public boolean isRepeating(int eventId) {
		PendingIntent pendingIntent = getPendingIntent(eventId, PendingIntent.FLAG_NO_CREATE);
		if (pendingIntent != null) {
			return true;
		} else {
			return false;
		}
	}
}
