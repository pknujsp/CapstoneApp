package com.lifedawn.capstoneapp.reminder.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.CalendarContract;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.lifedawn.capstoneapp.MainActivity;
import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.common.constants.SharedPreferenceConstant;
import com.lifedawn.capstoneapp.common.interfaces.BackgroundCallback;
import com.lifedawn.capstoneapp.common.repository.CalendarRepository;
import com.lifedawn.capstoneapp.common.util.NotificationHelper;
import com.lifedawn.capstoneapp.reminder.NotificationActivity;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class NotificationService extends Service {
	private Vibrator vibrator;
	private int originalAlarmVolume;
	private AudioManager audioManager;
	private MediaPlayer mediaPlayer;


	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		final String action = intent.getAction();
		final Bundle bundle = intent.getExtras();

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		final String soundUri = sharedPreferences.getString(SharedPreferenceConstant.REMINDER_SOUND_URI.getVal(), "");
		final boolean sound = sharedPreferences.getBoolean(SharedPreferenceConstant.REMINDER_SOUND_ON_OFF.getVal(), false);
		final boolean vibration = sharedPreferences.getBoolean(SharedPreferenceConstant.REMINDER_VIBRATION.getVal(), false);
		final boolean wake = sharedPreferences.getBoolean(SharedPreferenceConstant.REMINDER_WAKE.getVal(), false);
		final int soundVolume = sharedPreferences.getInt(SharedPreferenceConstant.REMINDER_SOUND_VOLUME.getVal(), 0);

		if (action.equals(CalendarContract.ACTION_EVENT_REMINDER)) {
			final Long alarmTime = bundle.getLong(CalendarContract.CalendarAlerts.ALARM_TIME);

			CalendarRepository.loadEvents(getApplicationContext(), alarmTime, new BackgroundCallback<List<ContentValues>>() {
				@Override
				public void onResultSuccessful(List<ContentValues> eventList) {
					NotificationManager notificationManager = getSystemService(NotificationManager.class);
					NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());
					final String title = getString(R.string.promise_reminder);

					int notificationId = (int) System.currentTimeMillis();
					final long[] eventIdArr = new long[eventList.size()];
					int i = 0;

					Intent confirmEventIntent = new Intent(getApplicationContext(), PromiseNotificationReceiver.class);
					confirmEventIntent.setAction(PromiseNotificationReceiver.ACTION_CONFIRM_EVENT);
					Bundle confirmBundle = new Bundle();
					confirmBundle.putInt("notificationId", notificationId);

					confirmEventIntent.putExtras(confirmBundle);

					PendingIntent confirmEventPendingIntent =
							PendingIntent.getBroadcast(getApplicationContext(), (int) System.currentTimeMillis(), confirmEventIntent,
									PendingIntent.FLAG_UPDATE_CURRENT);

					for (ContentValues event : eventList) {
						//약속 알림
						eventIdArr[i++] = event.getAsLong(CalendarContract.CalendarAlerts.EVENT_ID);
						NotificationHelper.NotificationItem notificationItem =
								notificationHelper.createNotificationItem(NotificationHelper.NotificationType.PROMISE_REMINDER);
						NotificationCompat.Builder builder = notificationItem.getBuilder();
						builder.setSmallIcon(R.drawable.ic_baseline_access_alarm_24).setContentTitle(title)
								.setContentText(event.getAsString(CalendarContract.Events.TITLE)).
								setAutoCancel(true)
								.addAction(R.drawable.ic_baseline_check_24, getString(R.string.ok), confirmEventPendingIntent);

						Intent clickIntent = new Intent(getApplicationContext(), MainActivity.class);
						clickIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

						PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), notificationId, clickIntent,
								PendingIntent.FLAG_UPDATE_CURRENT);
						builder.setContentIntent(pendingIntent);
						//알림 관련 처리
						notificationManager.notify(notificationId++, builder.build());
					}

					if (wake) {
						Bundle activityBundle = new Bundle();
						activityBundle.putLongArray("eventIdArr", eventIdArr);
						activityBundle.putInt("notificationId", notificationId);

						Intent activityIntent = new Intent(getApplicationContext(), NotificationActivity.class);
						activityIntent.putExtras(activityBundle);
						activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(activityIntent);
					}

					if (vibration) {
						vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
						final long[] vibratePattern = new long[]{600, 1000, 500, 1100};

						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
							vibrator.vibrate(VibrationEffect.createWaveform(vibratePattern, 0));
						} else {
							vibrator.vibrate(vibratePattern, 0);
						}
					}

					if (sound) {
						audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
						originalAlarmVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);

						final float volume = soundVolume / 100f;
						int newVolume = (int) (volume * audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM));
						audioManager.setStreamVolume(AudioManager.STREAM_ALARM, newVolume, AudioManager.FLAG_PLAY_SOUND);

						mediaPlayer = new MediaPlayer();
						try {
							Uri uri = Uri.parse(soundUri);

							mediaPlayer.setDataSource(getApplicationContext(), uri);
							mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
							mediaPlayer.setLooping(true);
							mediaPlayer.setVolume(1f, 1f);

							mediaPlayer.prepare();
						} catch (IOException e) {
							e.printStackTrace();
						}
						mediaPlayer.start();
					}

				}

				@Override
				public void onResultFailed(Exception e) {

				}
			});

		}

		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
		silent();
		super.onDestroy();
	}

	public void silent() {
		if (vibrator != null) {
			vibrator.cancel();
			vibrator = null;
		}
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			audioManager.setStreamVolume(AudioManager.STREAM_ALARM, originalAlarmVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

			mediaPlayer = null;
			audioManager = null;
		}
	}
}