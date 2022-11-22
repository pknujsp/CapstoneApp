package com.lifedawn.capstoneapp.promise.receivedinvitation;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.calendar.fragments.SyncCalendarCallback;
import com.lifedawn.capstoneapp.common.constants.Constant;
import com.lifedawn.capstoneapp.common.interfaces.IRefreshCalendar;
import com.lifedawn.capstoneapp.common.interfaces.OnClickPromiseItemListener;
import com.lifedawn.capstoneapp.common.interfaces.BackgroundCallback;
import com.lifedawn.capstoneapp.common.repository.CalendarRepository;
import com.lifedawn.capstoneapp.common.util.AttendeeUtil;
import com.lifedawn.capstoneapp.common.util.PermissionsLifeCycleObserver;
import com.lifedawn.capstoneapp.common.view.RecyclerViewItemDecoration;
import com.lifedawn.capstoneapp.common.viewmodel.AccountViewModel;
import com.lifedawn.capstoneapp.common.viewmodel.CalendarViewModel;
import com.lifedawn.capstoneapp.common.viewmodel.FriendViewModel;
import com.lifedawn.capstoneapp.databinding.FragmentReceivedInvitationBinding;
import com.lifedawn.capstoneapp.databinding.ItemViewInvitedPromiseBinding;
import com.lifedawn.capstoneapp.main.MainTransactionFragment;
import com.lifedawn.capstoneapp.main.MyApplication;
import com.lifedawn.capstoneapp.model.firestore.PlaceDto;
import com.lifedawn.capstoneapp.promise.promiseinfo.PromiseInfoFragment;

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
	private PermissionsLifeCycleObserver permissionsLifeCycleObserver;
	private FriendViewModel friendViewModel;
	private OnResultInvitedPromiseListener onResultInvitedPromiseListener;

	private String myEmail;

	private boolean initializing = true;
	private boolean successfulResponse;

	public static final String TAG = "ReceivedInvitationFragment";

	public ReceivedInvitationFragment setOnResultInvitedPromiseListener(OnResultInvitedPromiseListener onResultInvitedPromiseListener) {
		this.onResultInvitedPromiseListener = onResultInvitedPromiseListener;
		return this;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		googleAccountLifeCycleObserver = new GoogleAccountLifeCycleObserver(requireActivity().getActivityResultRegistry(),
				requireActivity());
		getLifecycle().addObserver(googleAccountLifeCycleObserver);
		permissionsLifeCycleObserver = new PermissionsLifeCycleObserver(requireActivity());
		getLifecycle().addObserver(permissionsLifeCycleObserver);
		accountViewModel = new ViewModelProvider(requireActivity()).get(AccountViewModel.class);
		calendarViewModel = new ViewModelProvider(requireActivity()).get(CalendarViewModel.class);
		friendViewModel = new ViewModelProvider(requireActivity()).get(FriendViewModel.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentReceivedInvitationBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onDestroy() {
		onResultInvitedPromiseListener.onResult(successfulResponse);
		super.onDestroy();
	}

	private void showResponseDialog(String msg, DialogInterface.OnClickListener onClickListener) {
		new MaterialAlertDialogBuilder(requireActivity()).setTitle(R.string.response)
				.setMessage(msg).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).setPositiveButton(R.string.ok, onClickListener).create().show();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
		binding.recyclerView.addItemDecoration(new RecyclerViewItemDecoration(getContext()));

		adapter = new RecyclerViewAdapter(getContext());
		adapter.setOnClickPromiseItemListener(new OnClickPromiseItemListener() {
			@Override
			public void onClickedEdit(CalendarRepository.EventObj event, int position) {

			}

			@Override
			public void onClickedRemoveEvent(CalendarRepository.EventObj event, int position) {
				CalendarRepository.removeEvent(getContext(), event.getEvent(), new BackgroundCallback<ContentValues>() {
					@Override
					public void onResultSuccessful(ContentValues e) {
						binding.refreshLayout.setRefreshing(true);
						refreshEvents();
					}

					@Override
					public void onResultFailed(Exception e) {

					}
				});
			}

			@Override
			public void onClickedEvent(CalendarRepository.EventObj event, int position) {
				PromiseInfoFragment promiseInfoFragment = new PromiseInfoFragment();

				Bundle bundle = new Bundle();
				bundle.putString("eventId", event.getEvent().getAsString(CalendarContract.Events._ID));
				promiseInfoFragment.setArguments(bundle);

				FragmentManager fragmentManager = getParentFragment().getParentFragment().getParentFragmentManager();
				fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag(MainTransactionFragment.class.getName())).add(
						R.id.fragmentContainerView, promiseInfoFragment, PromiseInfoFragment.class.getName()).addToBackStack(
						PromiseInfoFragment.class.getName()).commit();
			}

			@Override
			public void onClickedRefusal(CalendarRepository.EventObj event, int position) {
				showResponseDialog(getString(R.string.msg_refusal_response_to_promise), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						responseToInvitationEvent(event.getEvent().getAsString("_sync_id"), false);
						dialog.dismiss();
					}
				});
			}

			@Override
			public void onClickedAcceptance(CalendarRepository.EventObj event, int position) {
				showResponseDialog(getString(R.string.msg_accept_response_to_promise), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						responseToInvitationEvent(event.getEvent().getAsString("_sync_id"), true);
						dialog.dismiss();
					}
				});
			}
		});
		adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
			@Override
			public void onChanged() {
				super.onChanged();

				if (adapter.getItemCount() > 0) {
					binding.warningLayout.getRoot().setVisibility(View.GONE);
				} else {
					binding.warningLayout.getRoot().setVisibility(View.VISIBLE);
					binding.warningLayout.warningText.setText(R.string.empty_received_invitation_promises);
					binding.warningLayout.btn.setVisibility(View.GONE);
				}
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


		if (permissionsLifeCycleObserver.checkCalendarPermissions()) {
			binding.refreshLayout.setRefreshing(true);
			refreshEvents();

			calendarViewModel.getSyncCalendarLiveData().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
				@Override
				public void onChanged(Boolean aBoolean) {
					refreshEvents();
				}
			});
		} else {
			binding.warningLayout.btn.setVisibility(View.VISIBLE);
			binding.warningLayout.btn.setText(R.string.check_permissions);

			binding.warningLayout.getRoot().setVisibility(View.VISIBLE);

			final ActivityResultCallback<Boolean> activityResultCallback = new ActivityResultCallback<Boolean>() {
				@Override
				public void onActivityResult(Boolean result) {
					if (result) {
						binding.warningLayout.getRoot().setVisibility(View.GONE);
						binding.refreshLayout.setRefreshing(true);
						refreshEvents();
					} else {
						//권한 거부됨
						binding.warningLayout.warningText.setText(R.string.needs_calendar_permission);
						ActivityResultCallback<Boolean> activityResultCallback = this;
						binding.warningLayout.btn.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								permissionsLifeCycleObserver.launchCalendarPermissionsLauncher(activityResultCallback);
							}
						});
					}
				}
			};

			permissionsLifeCycleObserver.launchCalendarPermissionsLauncher(activityResultCallback);
		}
	}

	private void responseToInvitationEvent(String eventId, boolean acceptance) {
		MyApplication.EXECUTOR_SERVICE.execute(new Runnable() {
			@Override
			public void run() {
				final Calendar calendarService = calendarViewModel.getCalendarService();
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
							binding.refreshLayout.setRefreshing(true);
							syncCalendars();
							Toast.makeText(getContext(), acceptance ? R.string.response_accept_to_invited_event : R.string.response_decline_to_invited_event,
									Toast.LENGTH_SHORT).show();
							successfulResponse = true;
						}
					});
				}
			}
		});
	}


	@Override
	public void syncCalendars() {
		calendarViewModel.syncCalendars(accountViewModel.getCurrentSignInAccount(), new SyncCalendarCallback<Boolean>() {
			@Override
			public void onResultSuccessful(Boolean e) {
				refreshEvents();

				if (e) {
					if (getActivity() != null) {
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Toast.makeText(getContext(), R.string.succeed_update_event, Toast.LENGTH_SHORT).show();
							}
						});
					}
				}
			}

			@Override
			public void onResultFailed(Exception e) {
				if (getActivity() != null) {
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(getContext(), R.string.failed_sync_calendar, Toast.LENGTH_SHORT).show();
							binding.refreshLayout.setRefreshing(false);

						}
					});
				}
			}

			@Override
			public void onSyncStarted() {
				super.onSyncStarted();
				Toast.makeText(getContext(), R.string.start_update_event, Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onAlreadySyncing() {
				Toast.makeText(getContext(), R.string.already_syncing, Toast.LENGTH_SHORT).show();
				if (!syncing) {
					binding.refreshLayout.setRefreshing(false);
				}
			}
		});
	}

	@Override
	public void refreshEvents() {
		myEmail = accountViewModel.getLastSignInAccountName();
		CalendarRepository.loadCalendar(getContext(), myEmail, new BackgroundCallback<ContentValues>() {
			@Override
			public void onResultSuccessful(ContentValues e) {
				CalendarRepository.loadReceivedInvitationEvents(getContext(), myEmail,
						new BackgroundCallback<List<CalendarRepository.EventObj>>() {
							@Override
							public void onResultSuccessful(List<CalendarRepository.EventObj> eventObjList) {
								if (getActivity() == null) {
									return;
								}

								List<Integer> removeIndexList = new ArrayList<>();

								for (int i = eventObjList.size() - 1; i >= 0; i--) {
									List<ContentValues> attendeeList = eventObjList.get(i).getAttendeeList();
									boolean invited = false;
									for (ContentValues attendee : attendeeList) {
										if (attendee.getAsString(CalendarContract.Attendees.ATTENDEE_EMAIL).equals(myEmail)) {
											invited = true;
											break;
										}
									}

									if (!invited) {
										removeIndexList.add(i);
									}
								}

								for (int index : removeIndexList) {
									eventObjList.remove(index);
								}

								getActivity().runOnUiThread(new Runnable() {
									@Override
									public void run() {
										binding.refreshLayout.setRefreshing(false);
										adapter.setEvents(eventObjList);
										adapter.notifyDataSetChanged();

										if (adapter.getItemCount() > 0) {
											binding.warningLayout.getRoot().setVisibility(View.GONE);
										} else {
											binding.warningLayout.getRoot().setVisibility(View.VISIBLE);
											binding.warningLayout.warningText.setText(R.string.empty_received_invitation_promises);
											binding.warningLayout.btn.setVisibility(View.GONE);
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
			public void onResultFailed(Exception e) {
				if (getActivity() != null) {
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							binding.refreshLayout.setRefreshing(false);
						}
					});
				}

			}
		});
	}

	private final class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
		private List<CalendarRepository.EventObj> events = new ArrayList<>();
		private OnClickPromiseItemListener onClickPromiseItemListener;
		private DateTimeFormatter DATE_TIME_FORMATTER;
		private String signInAccountEmail;

		public RecyclerViewAdapter(Context context) {
			DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(context.getString(R.string.promiseDateTimeFormat));
			signInAccountEmail = accountViewModel.getLastSignInAccountName();
		}

		public void setOnClickPromiseItemListener(OnClickPromiseItemListener onClickPromiseItemListener) {
			this.onClickPromiseItemListener = onClickPromiseItemListener;
		}

		public void setEvents(List<CalendarRepository.EventObj> events) {
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

				CalendarRepository.EventObj eventObj = events.get(getBindingAdapterPosition());

				final ContentValues event = eventObj.getEvent();
				String dtStart = event.getAsString(CalendarContract.Events.DTSTART);
				String eventTimeZone = event.getAsString(CalendarContract.Events.EVENT_TIMEZONE);
				ZonedDateTime start = ZonedDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(dtStart)), ZoneId.of(eventTimeZone));

				binding.dateTime.setText(start.format(DATE_TIME_FORMATTER));
				binding.description.setText(event.getAsString(CalendarContract.Events.DESCRIPTION) == null ||
						event.getAsString(CalendarContract.Events.DESCRIPTION).isEmpty() ?
						getContext().getString(R.string.noDescription) :
						event.getAsString(CalendarContract.Events.DESCRIPTION));
				binding.title.setText(event.getAsString(CalendarContract.Events.TITLE) == null ? getString(R.string.no_title) :
						event.getAsString(CalendarContract.Events.TITLE));

				if (event.getAsString(CalendarContract.Events.EVENT_LOCATION) != null) {
					if (event.getAsString(CalendarContract.Events.EVENT_LOCATION).isEmpty()) {
						binding.location.setText(getContext().getString(R.string.no_promise_location));
					} else {
						PlaceDto placeDto = PlaceDto.toLocationDto(event.getAsString(CalendarContract.Events.EVENT_LOCATION));
						if (placeDto != null) {
							binding.location.setText(
									placeDto.getLocationType() == Constant.ADDRESS ? placeDto.getAddressName() : placeDto.getPlaceName());
						} else {
							binding.location.setText(event.getAsString(CalendarContract.Events.EVENT_LOCATION));
						}
					}
				} else {
					binding.location.setText(getContext().getString(R.string.no_promise_location));
				}

				List<String> attendeeNameList = new ArrayList<>();

				for (ContentValues attendee : eventObj.getAttendeeList()) {
					if (attendee.getAsString(CalendarContract.Attendees.ATTENDEE_EMAIL).equals(signInAccountEmail)) {
						attendeeNameList.add(getString(R.string.me));
					} else {
						attendeeNameList.add(friendViewModel.getName(attendee));
					}
				}

				String people = AttendeeUtil.toListString(attendeeNameList);
				binding.people.setText(people.isEmpty() ? getString(R.string.no_attendee) : people);

				binding.invitee.setText(friendViewModel.getName(event));

				List<ContentValues> attendeeList = eventObj.getAttendeeList();
				for (ContentValues attendee : attendeeList) {
					if (attendee.getAsString(CalendarContract.Attendees.ATTENDEE_EMAIL).equals(myEmail)) {
						if (attendee.getAsInteger(CalendarContract.Attendees.ATTENDEE_STATUS).equals(CalendarContract.Attendees.ATTENDEE_STATUS_ACCEPTED)) {
							binding.status.setText(getString(R.string.response_accept_to_invited_event));
							binding.acceptanceBtn.setVisibility(View.GONE);
							binding.refusalBtn.setVisibility(View.VISIBLE);
							break;
						} else if (attendee.getAsInteger(CalendarContract.Attendees.ATTENDEE_STATUS).equals(CalendarContract.Attendees.ATTENDEE_STATUS_DECLINED)) {
							binding.status.setText(getString(R.string.response_decline_to_invited_event));
							binding.acceptanceBtn.setVisibility(View.VISIBLE);
							binding.refusalBtn.setVisibility(View.GONE);
							break;
						} else {
							binding.status.setText(getString(R.string.no_response_to_invited_event));
							binding.acceptanceBtn.setVisibility(View.VISIBLE);
							binding.refusalBtn.setVisibility(View.VISIBLE);
							break;
						}
					}
				}


			}
		}
	}

	public interface OnResultInvitedPromiseListener {
		void onResult(boolean changed);
	}
}