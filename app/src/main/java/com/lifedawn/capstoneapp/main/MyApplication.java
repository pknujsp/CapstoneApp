package com.lifedawn.capstoneapp.main;

import android.app.Application;

import com.lifedawn.capstoneapp.common.util.NotificationHelper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyApplication extends Application {
	public static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(3);

	@Override
	public void onCreate() {
		super.onCreate();

		NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());
		notificationHelper.createNotificationChannel(NotificationHelper.NotificationType.PROMISE_REMINDER);
	}
}
