package com.lifedawn.capstoneapp.promise.fixedpromise;

import android.Manifest;
import android.accounts.Account;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.account.GoogleAccountLifeCycleObserver;
import com.lifedawn.capstoneapp.common.constants.Constant;
import com.lifedawn.capstoneapp.common.interfaces.BackgroundCallback;
import com.lifedawn.capstoneapp.common.interfaces.OnClickPromiseItemListener;
import com.lifedawn.capstoneapp.common.util.AttendeeUtil;
import com.lifedawn.capstoneapp.common.view.ProgressDialog;
import com.lifedawn.capstoneapp.common.view.RecyclerViewItemDecoration;
import com.lifedawn.capstoneapp.common.viewmodel.AccountViewModel;
import com.lifedawn.capstoneapp.common.viewmodel.CalendarViewModel;
import com.lifedawn.capstoneapp.databinding.FragmentFixedPromiseBinding;
import com.lifedawn.capstoneapp.databinding.ItemViewPromiseBinding;
import com.lifedawn.capstoneapp.main.MainTransactionFragment;
import com.lifedawn.capstoneapp.main.MyApplication;
import com.lifedawn.capstoneapp.map.LocationDto;
import com.lifedawn.capstoneapp.promise.editpromise.EditPromiseFragment;
import com.lifedawn.capstoneapp.promise.promiseinfo.PromiseInfoFragment;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FixedPromiseFragment extends Fragment {
	private FragmentFixedPromiseBinding binding;
	private AccountViewModel accountViewModel;
	private CalendarViewModel calendarViewModel;
	private GoogleAccountLifeCycleObserver googleAccountLifeCycleObserver;
	private RecyclerViewAdapter adapter;
	private boolean initializing = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		googleAccountLifeCycleObserver = new GoogleAccountLifeCycleObserver(requireActivity().getActivityResultRegistry(),
				requireActivity());
		getLifecycle().addObserver(googleAccountLifeCycleObserver);
		accountViewModel = new ViewModelProvider(requireActivity()).get(AccountViewModel.class);
		calendarViewModel = new ViewModelProvider(requireActivity()).get(CalendarViewModel.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentFixedPromiseBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
		binding.recyclerView.addItemDecoration(new RecyclerViewItemDecoration(getContext()));

		adapter = new RecyclerViewAdapter(getContext());
		adapter.setOnClickPromiseItemListener(new OnClickPromiseItemListener() {
			@Override
			public void onClickedEdit(Event event, int position) {

			}

			@Override
			public void onClickedEvent(Event event, int position) {
				PromiseInfoFragment promiseInfoFragment = new PromiseInfoFragment();
				Map<String, Object> map = new HashMap<>();
				Set<String> keySet = event.keySet();
				for (String key : keySet) {
					map.put(key, event.get(key));
				}

				Bundle bundle = new Bundle();
				bundle.putSerializable("map", (Serializable) map);
				promiseInfoFragment.setArguments(bundle);

				FragmentManager fragmentManager = getParentFragment().getParentFragment().getParentFragmentManager();
				fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag(MainTransactionFragment.class.getName())).add(
						R.id.fragmentContainerView, promiseInfoFragment, PromiseInfoFragment.class.getName()).addToBackStack(
						PromiseInfoFragment.class.getName()).commit();
			}

			@Override
			public void onClickedRefusal(Event event, int position) {

			}

			@Override
			public void onClickedAcceptance(Event event, int position) {

			}
		});
		binding.recyclerView.setAdapter(adapter);

		binding.refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				refresh();
			}
		});

		ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_CALENDAR,
				Manifest.permission.WRITE_CALENDAR}, (int) System.currentTimeMillis());
		initializing = false;

		loadEvents();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	private void loadEvents() {
		binding.refreshLayout.setRefreshing(true);
		loadCalendar(new BackgroundCallback<ContentValues>() {
			@SuppressLint("Range")
			@Override
			public void onResultSuccessful(ContentValues calendar) {
				String[] selectionArgs = {calendar.getAsString(CalendarContract.Calendars._ID)};

				final String EVENT_QUERY = CalendarContract.Events.CALENDAR_ID + "=?";
				Cursor cursor = getContext().getContentResolver().query(CalendarContract.Events.CONTENT_URI, null, EVENT_QUERY, selectionArgs,
						null);

				List<ContentValues> eventList = new ArrayList<>();

				if (cursor != null) {
					while (cursor.moveToNext()) {
						ContentValues event = new ContentValues();
						String[] keys = cursor.getColumnNames();
						for (String key : keys) {
							event.put(key, cursor.getString(cursor.getColumnIndex(key)));
						}
						eventList.add(event);
					}
					cursor.close();
				}

				binding.refreshLayout.setRefreshing(false);
				adapter.setEvents(eventList);
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						adapter.notifyDataSetChanged();
					}
				});
			}

			@Override
			public void onResultFailed(Exception e) {

			}
		});
	}

	private void loadCalendar(BackgroundCallback<ContentValues> callback) {
		MyApplication.EXECUTOR_SERVICE.execute(new Runnable() {
			@SuppressLint("Range")
			@Override
			public void run() {
				Account account = accountViewModel.lastSignInAccount().getAccount();
				final String email = account.name;
				ContentResolver contentResolver = getContext().getContentResolver();
				Cursor cursor = contentResolver.query(CalendarContract.Calendars.CONTENT_URI, null, null, null, null);

				List<ContentValues> eventList = new ArrayList<>();

				if (cursor != null) {
					while (cursor.moveToNext()) {
						ContentValues calendar = new ContentValues();
						String[] keys = cursor.getColumnNames();
						for (String key : keys) {
							calendar.put(key, cursor.getString(cursor.getColumnIndex(key)));
						}
						eventList.add(calendar);
					}
					cursor.close();
				}

				for (ContentValues calendar : eventList) {
					if (calendar.getAsString(CalendarContract.Calendars.ACCOUNT_NAME).equals(email) &&
							calendar.getAsString(CalendarContract.Calendars.IS_PRIMARY).equals("1")) {
						callback.onResultSuccessful(calendar);
						break;
					}
				}
			}
		});
	}

	private void refresh() {
		calendarViewModel.syncCalendars(accountViewModel.lastSignInAccount().getAccount(), new BackgroundCallback<Boolean>() {
			@SuppressLint("Range")
			@Override
			public void onResultSuccessful(Boolean e) {
				binding.refreshLayout.setRefreshing(false);
				loadEvents();
			}

			@Override
			public void onResultFailed(Exception e) {

			}
		});
		/*
		MyApplication.EXECUTOR_SERVICE.execute(new Runnable() {
			@Override
			public void run() {
				final Calendar calendarService = calendarViewModel.getCalendarService();
				final List<Event> fixedEventList = new ArrayList<>();
				String pageToken = null;

				final String myEmail = accountViewModel.lastSignInAccount().getEmail();
				final String[] calendarIds = new String[]{"primary"};
				final String accepted = "accepted";

				try {
					for (String calendarId : calendarIds) {
						pageToken = null;
						do {
							Events events = calendarService.events().list(calendarId).setPageToken(pageToken).execute();
							List<Event> eventList = events.getItems();
							for (Event event : eventList) {
								if (event.getStart().getDateTime() == null) {
									continue;
								}

								if (event.getAttendees() != null) {
									for (EventAttendee eventAttendee : event.getAttendees()) {
										if (eventAttendee.getEmail().equals(myEmail) && eventAttendee.getResponseStatus().equals(accepted)) {
											fixedEventList.add(event);
											break;
										}
									}
								}

								if (event.getCreator().getEmail().equals(myEmail)) {
									fixedEventList.add(event);
								}
							}

							pageToken = events.getNextPageToken();
						} while (pageToken != null);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				adapter.setEvents(fixedEventList);

				if (getActivity() != null) {
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							binding.refreshLayout.setRefreshing(false);
							adapter.notifyDataSetChanged();
						}
					});
				}
			}
		});

		 */
	}

	@SuppressLint("Range")
	public Account getGoogleAccount() {
		if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
			final String[] PROJECTION = {
					CalendarContract.Calendars.ACCOUNT_NAME, CalendarContract.Calendars.OWNER_ACCOUNT, CalendarContract.Calendars.ACCOUNT_TYPE,
					CalendarContract.Calendars.IS_PRIMARY};
			ContentResolver contentResolver = getContext().getContentResolver();

			Cursor cursor = contentResolver.query(CalendarContract.Calendars.CONTENT_URI, PROJECTION, null, null, null);

			final String GOOGLE_SECONDARY_CALENDAR = "@group.calendar.google.com";
			List<ContentValues> accountList = new ArrayList<>();
			Set<String> ownerAccountSet = new HashSet<>();

			if (cursor != null) {
				while (cursor.moveToNext()) {
					if (cursor.getInt(3) == 1) {
						// another || google primary calendar
						if (!ownerAccountSet.contains(cursor.getString(1))) {
							ownerAccountSet.add(cursor.getString(1));
							ContentValues accountValues = new ContentValues();

							accountValues.put(CalendarContract.Calendars.ACCOUNT_NAME, cursor.getString(0));
							accountValues.put(CalendarContract.Calendars.OWNER_ACCOUNT, cursor.getString(1));
							accountValues.put(CalendarContract.Calendars.ACCOUNT_TYPE, cursor.getString(2));

							accountList.add(accountValues);
						}
					} else if (cursor.getString(cursor.getColumnIndex(CalendarContract.Calendars.OWNER_ACCOUNT)).contains(GOOGLE_SECONDARY_CALENDAR)) {
						if (!ownerAccountSet.contains(cursor.getString(1))) {
							ownerAccountSet.add(cursor.getString(1));
							ContentValues accountValues = new ContentValues();

							accountValues.put(CalendarContract.Calendars.ACCOUNT_NAME, cursor.getString(0));
							accountValues.put(CalendarContract.Calendars.OWNER_ACCOUNT, cursor.getString(1));
							accountValues.put(CalendarContract.Calendars.ACCOUNT_TYPE, cursor.getString(2));

							accountList.add(accountValues);
						}
					}
				}
				cursor.close();
			}
			Account googleSignInAccount = accountViewModel.lastSignInAccount().getAccount();

			for (ContentValues contentValues : accountList) {
				if (contentValues.getAsString(CalendarContract.Calendars.ACCOUNT_TYPE).equals("com.google")) {
					if (contentValues.getAsString(CalendarContract.Calendars.ACCOUNT_NAME).equals(googleSignInAccount.name)) {
						Account account = new Account(contentValues.getAsString(CalendarContract.Calendars.ACCOUNT_NAME)
								, contentValues.getAsString(CalendarContract.Calendars.ACCOUNT_TYPE));

						return account;
					}
				}
			}
		}
		return null;
	}

	private class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
		private List<ContentValues> events = new ArrayList<>();
		private OnClickPromiseItemListener onClickPromiseItemListener;
		private DateTimeFormatter DATE_TIME_FORMATTER;

		public RecyclerViewAdapter(Context context) {
			DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(context.getString(R.string.promiseDateTimeFormat));
		}

		public void setOnClickPromiseItemListener(OnClickPromiseItemListener onClickPromiseItemListener) {
			this.onClickPromiseItemListener = onClickPromiseItemListener;
		}

		public void setEvents(List<ContentValues> events) {
			this.events = events;
		}

		@NonNull
		@Override
		public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view_promise, null));
		}

		@Override
		public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
			holder.onBind();
		}

		@Override
		public void onViewRecycled(@NonNull ViewHolder holder) {
			holder.clear();
			super.onViewRecycled(holder);
		}

		@Override
		public int getItemCount() {
			return events.size();
		}

		private class ViewHolder extends RecyclerView.ViewHolder {
			private ItemViewPromiseBinding binding;

			public ViewHolder(@NonNull View itemView) {
				super(itemView);
				binding = ItemViewPromiseBinding.bind(itemView);
			}

			public void clear() {

			}

			public void onBind() {
				binding.editBtn.setVisibility(View.GONE);
				binding.getRoot().setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
					}
				});

				binding.editBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
					}
				});

				final ContentValues event = events.get(getBindingAdapterPosition());
				String dtStart = event.getAsString(CalendarContract.Events.DTSTART);
				String eventTimeZone = event.getAsString(CalendarContract.Events.EVENT_TIMEZONE);
				ZonedDateTime start = ZonedDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(dtStart)), ZoneId.of(eventTimeZone));
				start = start.withZoneSameInstant(start.getZone());

				binding.dateTime.setText(start.format(DATE_TIME_FORMATTER));
				binding.description.setText(event.getAsString(CalendarContract.Events.DESCRIPTION) == null ?
						getContext().getString(R.string.noDescription) :
						event.getAsString(CalendarContract.Events.DESCRIPTION));
				binding.title.setText(event.getAsString(CalendarContract.Events.TITLE));

				LocationDto locationDto = null;
				if (event.getAsString(CalendarContract.Events.EVENT_LOCATION) != null) {
					locationDto = LocationDto.toLocationDto(event.getAsString(CalendarContract.Events.EVENT_LOCATION));
					if (locationDto != null) {
						binding.location.setText(
								locationDto.getLocationType() == Constant.ADDRESS ? locationDto.getAddressName() : locationDto.getPlaceName());
					} else {
						binding.location.setTag(event.getAsString(CalendarContract.Events.EVENT_LOCATION));
					}
				} else {
					binding.location.setText(getContext().getString(R.string.no_promise_location));
				}

				/*
				List<EventAttendee> attendeeList = event.getAsString(CalendarContract.Events.ATT);
				if (attendeeList != null) {
					binding.people.setText(AttendeeUtil.toListString(attendeeList));
				}

				 */

			}
		}
	}
}