package com.lifedawn.capstoneapp.reminder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.lifedawn.capstoneapp.MainActivity;
import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.common.constants.Constant;
import com.lifedawn.capstoneapp.common.interfaces.BackgroundCallback;
import com.lifedawn.capstoneapp.common.repository.CalendarRepository;
import com.lifedawn.capstoneapp.common.viewmodel.AccountViewModel;
import com.lifedawn.capstoneapp.common.viewmodel.FriendViewModel;
import com.lifedawn.capstoneapp.databinding.ActivityNotificationBinding;
import com.lifedawn.capstoneapp.databinding.AttendeeSimpleInfoBinding;
import com.lifedawn.capstoneapp.map.LocationDto;
import com.lifedawn.capstoneapp.reminder.notifications.NotificationService;
import com.lifedawn.capstoneapp.reminder.notifications.PromiseNotificationReceiver;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationActivity extends AppCompatActivity {
	private long[] eventIdList;
	private int notificationId;
	private ActivityNotificationBinding binding;
	private FriendViewModel friendViewModel;
	private AccountViewModel accountViewModel;
	private EndNotificationReceiver endNotificationReceiver = new EndNotificationReceiver();
	private final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("M.d E a hh:mm");

	private Bundle bundle;

	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putAll(bundle);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
						WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
						WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
						WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
						WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
		super.onCreate(savedInstanceState);

		IntentFilter intentFilter = new IntentFilter(PromiseNotificationReceiver.ACTION_CONFIRM_EVENT);
		registerReceiver(endNotificationReceiver, intentFilter);

		binding = DataBindingUtil.setContentView(this, R.layout.activity_notification);
		friendViewModel = new ViewModelProvider(this).get(FriendViewModel.class);
		accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);

		bundle = savedInstanceState == null ? getIntent().getExtras() : savedInstanceState;
		eventIdList = bundle.getLongArray("eventIdArr");
		notificationId = bundle.getInt("notificationId");

		binding.attendeeList.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));

		for (Long eventId : eventIdList) {
			CalendarRepository.loadEvent(getApplicationContext(), eventId.toString(), new BackgroundCallback<List<CalendarRepository.EventObj>>() {
				@Override
				public void onResultSuccessful(List<CalendarRepository.EventObj> e) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							ContentValues event = e.get(0).getEvent();

							binding.eventTitle.setText(event.getAsString(CalendarContract.Events.TITLE));
							String dtStart = event.getAsString(CalendarContract.Events.DTSTART);
							String eventTimeZone = event.getAsString(CalendarContract.Events.EVENT_TIMEZONE);
							ZonedDateTime start = ZonedDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(dtStart)), ZoneId.of(eventTimeZone));

							binding.dateTime.setText(start.format(DATE_TIME_FORMATTER));

							binding.description.setText(!event.containsKey(CalendarContract.Events.DESCRIPTION) ? getString(R.string.noDescription) :
									event.getAsString(CalendarContract.Events.DESCRIPTION));

							if (event.getAsString(CalendarContract.Events.EVENT_LOCATION) != null) {
								if (event.getAsString(CalendarContract.Events.EVENT_LOCATION).isEmpty()) {
									binding.location.setText(getString(R.string.no_promise_location));
								} else {
									LocationDto locationDto = LocationDto.toLocationDto(event.getAsString(CalendarContract.Events.EVENT_LOCATION));
									if (locationDto != null) {
										binding.location.setText(
												locationDto.getLocationType() == Constant.ADDRESS ? locationDto.getAddressName() : locationDto.getPlaceName());
									} else {
										binding.location.setText(event.getAsString(CalendarContract.Events.EVENT_LOCATION));
									}
								}
							} else {
								binding.location.setText(getString(R.string.no_promise_location));
							}

							List<Map<String, String>> attendeeMapList = new ArrayList<>();
							List<ContentValues> attendeeList = e.get(0).getAttendeeList();

							String status = null;
							String name = null;
							boolean isOrganizer = false;

							ContentValues organizer = new ContentValues();
							organizer.put(CalendarContract.Attendees.ATTENDEE_EMAIL, event.getAsString(CalendarContract.Events.ORGANIZER));
							attendeeList.add(organizer);

							final String signInAccountName = accountViewModel.getLastSignInAccountName();

							for (ContentValues attendee : attendeeList) {
								Map<String, String> map = new HashMap<>();

								isOrganizer =
										event.getAsString(CalendarContract.Events.ORGANIZER).equals(attendee.getAsString(CalendarContract.Attendees.ATTENDEE_EMAIL));

								if (!isOrganizer && attendee.containsKey(CalendarContract.Attendees.ATTENDEE_STATUS)) {
									switch (attendee.getAsInteger(CalendarContract.Attendees.ATTENDEE_STATUS)) {
										case CalendarContract.Attendees.ATTENDEE_STATUS_ACCEPTED:
											status = getString(R.string.acceptance);
											break;
										case CalendarContract.Attendees.ATTENDEE_STATUS_DECLINED:
											status = getString(R.string.declined);
											break;
										default:
											status = getString(R.string.no_response);
									}
								} else {
									status = getString(R.string.invitee);
								}

								name = status + " - ";
								if (signInAccountName == null) {
									name += friendViewModel.getName(attendee);
								} else {
									name += (attendee.getAsString(CalendarContract.Attendees.ATTENDEE_EMAIL).equals(signInAccountName) ?
											getString(R.string.me) :
											friendViewModel.getName(attendee));
								}

								map.put("name", name);
								map.put("email", attendee.getAsString(CalendarContract.Attendees.ATTENDEE_EMAIL));

								attendeeMapList.add(map);
							}

							if (attendeeMapList.size() > 0) {
								AttendeeListAdapter adapter = new AttendeeListAdapter(attendeeMapList);
								binding.attendeeList.setAdapter(adapter);
							}

						}
					});


				}

				@Override
				public void onResultFailed(Exception e) {

				}
			});

		}


		binding.detailBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), MainActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				end();
			}
		});

		binding.closeBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				end();
			}
		});


	}

	private void end() {
		stopService(new Intent(getApplicationContext(), NotificationService.class));
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(notificationId);
		finish();
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(endNotificationReceiver);
		super.onDestroy();
	}

	private static class AttendeeListAdapter extends RecyclerView.Adapter<AttendeeListAdapter.ViewHolder> {
		private List<Map<String, String>> attendeeMapList;

		public AttendeeListAdapter(List<Map<String, String>> attendeeMapList) {
			this.attendeeMapList = attendeeMapList;
		}

		@NonNull
		@Override
		public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.attendee_simple_info, null));
		}

		@Override
		public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
			holder.bind();
		}

		@Override
		public int getItemCount() {
			return attendeeMapList.size();
		}

		private class ViewHolder extends RecyclerView.ViewHolder {
			private AttendeeSimpleInfoBinding binding;

			public ViewHolder(@NonNull View itemView) {
				super(itemView);
				binding = AttendeeSimpleInfoBinding.bind(itemView);
			}

			public void bind() {
				Map<String, String> attendeeMap = attendeeMapList.get(getBindingAdapterPosition());
				binding.name.setText(attendeeMap.get("name"));
				binding.email.setText(attendeeMap.get("email"));
			}
		}
	}


	public class EndNotificationReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			finishAndRemoveTask();
		}
	}
}