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

import java.util.List;
import java.util.Set;

public class PromiseNotificationReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		Log.e("PromiseNotificationReceiver", action);

		PersistableBundle bundle = new PersistableBundle();
		bundle.putLong(CalendarContract.CalendarAlerts.ALARM_TIME, intent.getExtras().getLong(CalendarContract.CalendarAlerts.ALARM_TIME));
		bundle.putString("action", intent.getAction());

		JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

		final int newJobId = 1000;
		JobInfo newJobInfo = new JobInfo.Builder(newJobId, new ComponentName(context, PromiseNotificationJobService.class))
				.setMinimumLatency(0).setOverrideDeadline(2000).setExtras(bundle).build();

		jobScheduler.schedule(newJobInfo);
	}
}