package com.lifedawn.capstoneapp.promise.receivedinvitation;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
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
import com.lifedawn.capstoneapp.common.interfaces.OnClickPromiseItemListener;
import com.lifedawn.capstoneapp.common.interfaces.BackgroundCallback;
import com.lifedawn.capstoneapp.common.util.AttendeeUtil;
import com.lifedawn.capstoneapp.common.view.ProgressDialog;
import com.lifedawn.capstoneapp.common.view.RecyclerViewItemDecoration;
import com.lifedawn.capstoneapp.common.viewmodel.AccountViewModel;
import com.lifedawn.capstoneapp.common.viewmodel.CalendarViewModel;
import com.lifedawn.capstoneapp.databinding.FragmentReceivedInvitationBinding;
import com.lifedawn.capstoneapp.databinding.ItemViewInvitedPromiseBinding;
import com.lifedawn.capstoneapp.main.MyApplication;
import com.lifedawn.capstoneapp.map.LocationDto;

import org.jetbrains.annotations.NotNull;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ReceivedInvitationFragment extends Fragment {
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
			public void onClickedEdit(Event event, int position) {

			}

			@Override
			public void onClickedEvent(Event event, int position) {

			}

			@Override
			public void onClickedRefusal(Event event, int position) {
				final Calendar calendarService = calendarViewModel.getCalendarService();
				final String myEmail = accountViewModel.lastSignInAccount().getEmail();

				calendarViewModel.sendResponseForInvitedPromise(calendarService, "primary", myEmail, event, false,
						new BackgroundCallback<Boolean>() {
							@Override
							public void onResultSuccessful(Boolean e) {
								getActivity().runOnUiThread(new Runnable() {
									@Override
									public void run() {
										if (e) {
											Toast.makeText(getContext(), R.string.response_decline_to_invited_event, Toast.LENGTH_SHORT).show();
											binding.refreshLayout.post(new Runnable() {
												@Override
												public void run() {
													binding.refreshLayout.setRefreshing(true);
													refresh();

												}
											});
										} else {
											Toast.makeText(getContext(), R.string.failed_response_for_invitied_event, Toast.LENGTH_SHORT).show();
										}
									}
								});
							}

							@Override
							public void onResultFailed(Exception e) {

							}
						});
			}

			@Override
			public void onClickedAcceptance(Event event, int position) {
				final Calendar calendarService = calendarViewModel.getCalendarService();
				final String myEmail = accountViewModel.lastSignInAccount().getEmail();

				calendarViewModel.sendResponseForInvitedPromise(calendarService, "primary", myEmail, event, true,
						new BackgroundCallback<Boolean>() {
							@Override
							public void onResultSuccessful(Boolean e) {
								getActivity().runOnUiThread(new Runnable() {
									@Override
									public void run() {
										if (e) {
											Toast.makeText(getContext(), R.string.response_accept_to_invited_event, Toast.LENGTH_SHORT).show();
											binding.refreshLayout.post(new Runnable() {
												@Override
												public void run() {
													binding.refreshLayout.setRefreshing(true);
													refresh();

												}
											});

										} else {
											Toast.makeText(getContext(), R.string.failed_response_for_invitied_event, Toast.LENGTH_SHORT).show();
										}
									}
								});
							}

							@Override
							public void onResultFailed(Exception e) {

							}
						});
			}
		});
		binding.recyclerView.setAdapter(adapter);

		binding.refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				refresh();
			}
		});

		if (calendarViewModel.getCalendarService() == null) {
			if (accountViewModel.getUsingAccountType() == Constant.ACCOUNT_GOOGLE) {
				calendarViewModel.createCalendarService(accountViewModel.getGoogleAccountCredential(), googleAccountLifeCycleObserver, new BackgroundCallback<Calendar>() {
					@Override
					public void onResultSuccessful(Calendar e) {
						if (calendarViewModel.getMainCalendarId() == null) {
							calendarViewModel.existingPromiseCalendar(e, new BackgroundCallback<CalendarListEntry>() {
								@Override
								public void onResultSuccessful(CalendarListEntry e) {
									binding.refreshLayout.post(new Runnable() {
										@Override
										public void run() {
											binding.refreshLayout.setRefreshing(true);
											refresh();

										}
									});

								}

								@Override
								public void onResultFailed(Exception e) {

								}
							});
						}
					}

					@Override
					public void onResultFailed(Exception e) {

					}
				});
			}
		} else {
			binding.refreshLayout.post(new Runnable() {
				@Override
				public void run() {
					binding.refreshLayout.setRefreshing(true);
					refresh();

				}
			});

		}
		initializing = false;
	}

	private void refresh() {


		MyApplication.EXECUTOR_SERVICE.execute(new Runnable() {
			@Override
			public void run() {
				final Calendar calendarService = calendarViewModel.getCalendarService();
				final List<Event> invitedEventList = new ArrayList<>();
				String pageToken = null;

				final String myEmail = accountViewModel.lastSignInAccount().getEmail();
				final String needsAction = "needsAction";
				final String eTag = "promise";

				try {
					do {
						Events events = calendarService.events().list("primary").setPageToken(pageToken).execute();
						List<Event> eventList = events.getItems();

						for (Event event : eventList) {
							if (event.getAttendees() != null && event.getEtag().equals(eTag)) {
								for (EventAttendee eventAttendee : event.getAttendees()) {
									if (eventAttendee.getEmail().equals(myEmail) && eventAttendee.getResponseStatus().equals(needsAction)) {
										invitedEventList.add(event);
										break;
									}
								}
							}
						}

						pageToken = events.getNextPageToken();
					} while (pageToken != null);
				} catch (Exception e) {

				}


				if (getActivity() != null) {
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							binding.refreshLayout.setRefreshing(false);
							adapter.setEvents(invitedEventList);
							adapter.notifyDataSetChanged();
						}
					});
				}
			}
		});
	}

	private class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
		private List<Event> events = new ArrayList<>();
		private OnClickPromiseItemListener onClickPromiseItemListener;
		private DateTimeFormatter DATE_TIME_FORMATTER;

		public RecyclerViewAdapter(Context context) {
			DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(context.getString(R.string.promiseDateTimeFormat));
		}

		public void setOnClickPromiseItemListener(OnClickPromiseItemListener onClickPromiseItemListener) {
			this.onClickPromiseItemListener = onClickPromiseItemListener;
		}

		public void setEvents(List<Event> events) {
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

				final Event event = events.get(getBindingAdapterPosition());
				EventDateTime eventDateTime = event.getStart();
				ZonedDateTime start = ZonedDateTime.parse(eventDateTime.getDateTime().toString());
				start = start.withZoneSameInstant(ZoneId.of(eventDateTime.getTimeZone()));

				binding.dateTime.setText(start.format(DATE_TIME_FORMATTER));
				binding.description.setText(event.getDescription() == null ? getContext().getString(R.string.noDescription) :
						event.getDescription());
				binding.title.setText(event.getSummary());

				LocationDto locationDto = new LocationDto();
				if (event.getLocation() != null) {
					locationDto = LocationDto.toLocationDto(event.getLocation());

					if (locationDto != null) {
						binding.location.setText(
								locationDto.getLocationType() == Constant.ADDRESS ? locationDto.getAddressName() : locationDto.getPlaceName());
					} else {
						binding.location.setTag(event.getLocation());
					}
				} else {
					binding.location.setText(getContext().getString(R.string.no_promise_location));
				}

				List<EventAttendee> attendeeList = event.getAttendees();
				if (attendeeList != null) {
					binding.people.setText(AttendeeUtil.toListString(attendeeList));
				}

				binding.invitee.setText(event.getCreator().getEmail());
			}
		}
	}
}