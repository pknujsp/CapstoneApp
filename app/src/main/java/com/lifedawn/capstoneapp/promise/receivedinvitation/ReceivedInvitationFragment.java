package com.lifedawn.capstoneapp.promise.receivedinvitation;

import android.accounts.Account;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.account.GoogleAccountLifeCycleObserver;
import com.lifedawn.capstoneapp.common.constants.Constant;
import com.lifedawn.capstoneapp.common.interfaces.IRefreshCalendar;
import com.lifedawn.capstoneapp.common.interfaces.OnClickPromiseItemListener;
import com.lifedawn.capstoneapp.common.interfaces.BackgroundCallback;
import com.lifedawn.capstoneapp.common.repository.CalendarRepository;
import com.lifedawn.capstoneapp.common.util.AttendeeUtil;
import com.lifedawn.capstoneapp.common.view.RecyclerViewItemDecoration;
import com.lifedawn.capstoneapp.common.viewmodel.AccountViewModel;
import com.lifedawn.capstoneapp.common.viewmodel.CalendarViewModel;
import com.lifedawn.capstoneapp.databinding.FragmentReceivedInvitationBinding;
import com.lifedawn.capstoneapp.databinding.ItemViewInvitedPromiseBinding;
import com.lifedawn.capstoneapp.main.MyApplication;
import com.lifedawn.capstoneapp.map.LocationDto;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ReceivedInvitationFragment extends Fragment implements IRefreshCalendar {
	private FragmentReceivedInvitationBinding binding;
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
		binding = FragmentReceivedInvitationBinding.inflate(inflater);
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
			public void onClickedEdit(ContentValues event, int position) {

			}

			@Override
			public void onClickedEvent(ContentValues event, int position) {

			}

			@Override
			public void onClickedRefusal(ContentValues event, int position) {
				responseToInvitationEvent(event.getAsString("_sync_id"), false);

			}

			@Override
			public void onClickedAcceptance(ContentValues event, int position) {
				responseToInvitationEvent(event.getAsString("_sync_id"), true);
			}
		});
		binding.recyclerView.setAdapter(adapter);

		binding.refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				syncCalendars();
			}
		});


		initializing = false;
		refreshEvents();
	}

	private void responseToInvitationEvent(String eventId, boolean acceptance) {
		MyApplication.EXECUTOR_SERVICE.execute(new Runnable() {
			@Override
			public void run() {
				final Calendar calendarService = calendarViewModel.getCalendarService();
				final String myEmail = accountViewModel.lastSignInAccount().getEmail();
				Event updatedEvent = null;

				try {
					Event event = calendarService.events().get("primary", eventId).execute();
					for (EventAttendee eventAttendee : event.getAttendees()) {
						if (eventAttendee.getEmail().equals(myEmail)) {
							eventAttendee.setResponseStatus(acceptance ? "accepted" : "declined");
							break;
						}
					}

					updatedEvent = calendarService.events().update("primary", event.getId(), event).execute();

				} catch (Exception e) {
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(getContext(), R.string.failed_response_for_invitied_event,
									Toast.LENGTH_SHORT).show();
						}
					});
				}

				if (updatedEvent != null) {
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							syncCalendars();
							Toast.makeText(getContext(), acceptance ? R.string.response_accept_to_invited_event : R.string.response_decline_to_invited_event,
									Toast.LENGTH_SHORT).show();
						}
					});
				}
			}
		});
	}


	@Override
	public void syncCalendars() {
		final Account account = accountViewModel.lastSignInAccount().getAccount();
		calendarViewModel.syncCalendars(account, new BackgroundCallback<Boolean>() {
			@SuppressLint("Range")
			@Override
			public void onResultSuccessful(Boolean e) {
				refreshEvents();
			}

			@Override
			public void onResultFailed(Exception e) {

			}
		});
	}

	@Override
	public void refreshEvents() {
		Account account = accountViewModel.lastSignInAccount().getAccount();
		CalendarRepository.loadCalendar(getContext(), account, new BackgroundCallback<ContentValues>() {
			@Override
			public void onResultSuccessful(ContentValues e) {
				CalendarRepository.loadReceivedInvitationEvents(getContext(), account, e.getAsString(CalendarContract.Calendars._ID), new BackgroundCallback<List<ContentValues>>() {
					@Override
					public void onResultSuccessful(List<ContentValues> e) {
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								binding.refreshLayout.setRefreshing(false);
								adapter.setEvents(e);
								adapter.notifyDataSetChanged();
							}
						});
					}

					@Override
					public void onResultFailed(Exception e) {

					}
				});
			}

			@Override
			public void onResultFailed(Exception e) {

			}
		});
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
		public RecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			return new RecyclerViewAdapter.ViewHolder(
					LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view_invited_promise, null));
		}

		@Override
		public void onBindViewHolder(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {
			holder.onBind();
		}

		@Override
		public void onViewRecycled(@NonNull RecyclerViewAdapter.ViewHolder holder) {
			holder.clear();
			super.onViewRecycled(holder);
		}

		@Override
		public int getItemCount() {
			return events.size();
		}

		private class ViewHolder extends RecyclerView.ViewHolder {
			private ItemViewInvitedPromiseBinding binding;

			public ViewHolder(@NonNull View itemView) {
				super(itemView);
				binding = ItemViewInvitedPromiseBinding.bind(itemView);
			}

			public void clear() {

			}

			public void onBind() {
				binding.getRoot().setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onClickPromiseItemListener.onClickedEvent(events.get(getBindingAdapterPosition()), getBindingAdapterPosition());
					}
				});

				binding.refusalBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onClickPromiseItemListener.onClickedRefusal(events.get(getBindingAdapterPosition()), getBindingAdapterPosition());
					}
				});

				binding.acceptanceBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onClickPromiseItemListener.onClickedAcceptance(events.get(getBindingAdapterPosition()),
								getBindingAdapterPosition());
					}
				});

				final ContentValues event = events.get(getBindingAdapterPosition());
				String dtStart = event.getAsString(CalendarContract.Events.DTSTART);
				String eventTimeZone = event.getAsString(CalendarContract.Events.EVENT_TIMEZONE);
				ZonedDateTime start = ZonedDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(dtStart)), ZoneId.of(eventTimeZone));

				binding.dateTime.setText(start.format(DATE_TIME_FORMATTER));
				binding.description.setText(event.getAsString(CalendarContract.Events.DESCRIPTION) == null ?
						getContext().getString(R.string.noDescription) :
						event.getAsString(CalendarContract.Events.DESCRIPTION));
				binding.title.setText(event.getAsString(CalendarContract.Events.TITLE));

				if (event.getAsString(CalendarContract.Events.EVENT_LOCATION) != null) {
					LocationDto locationDto = LocationDto.toLocationDto(event.getAsString(CalendarContract.Events.EVENT_LOCATION));
					if (locationDto != null) {
						binding.location.setText(
								locationDto.getLocationType() == Constant.ADDRESS ? locationDto.getAddressName() : locationDto.getPlaceName());
					} else {
						binding.location.setTag(event.getAsString(CalendarContract.Events.EVENT_LOCATION));
					}
				} else {
					binding.location.setText(getContext().getString(R.string.no_promise_location));
				}

			}
		}
	}
}