package com.lifedawn.capstoneapp.promise;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
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

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.account.GoogleAccountLifeCycleObserver;
import com.lifedawn.capstoneapp.calendar.fragments.SyncCalendarCallback;
import com.lifedawn.capstoneapp.common.constants.Constant;
import com.lifedawn.capstoneapp.common.interfaces.BackgroundCallback;
import com.lifedawn.capstoneapp.common.interfaces.IRefreshCalendar;
import com.lifedawn.capstoneapp.common.interfaces.OnClickPromiseItemListener;
import com.lifedawn.capstoneapp.common.interfaces.OnClickedExpandableListItemListener;
import com.lifedawn.capstoneapp.common.interfaces.OnFragmentCallback;
import com.lifedawn.capstoneapp.common.repository.CalendarRepository;
import com.lifedawn.capstoneapp.common.util.AttendeeUtil;
import com.lifedawn.capstoneapp.common.util.PermissionsLifeCycleObserver;
import com.lifedawn.capstoneapp.common.view.RecyclerViewItemDecoration;
import com.lifedawn.capstoneapp.common.viewmodel.AccountViewModel;
import com.lifedawn.capstoneapp.common.viewmodel.CalendarViewModel;
import com.lifedawn.capstoneapp.common.viewmodel.FriendViewModel;
import com.lifedawn.capstoneapp.databinding.FragmentPromiseTransactionBinding;
import com.lifedawn.capstoneapp.databinding.ItemViewPromiseBinding;
import com.lifedawn.capstoneapp.main.MainTransactionFragment;
import com.lifedawn.capstoneapp.map.LocationDto;
import com.lifedawn.capstoneapp.promise.addpromise.AddPromiseFragment;
import com.lifedawn.capstoneapp.promise.editpromise.EditPromiseFragment;
import com.lifedawn.capstoneapp.promise.fixedpromise.FixedPromiseFragment;
import com.lifedawn.capstoneapp.promise.promiseinfo.PromiseInfoFragment;
import com.lifedawn.capstoneapp.promise.receivedinvitation.ReceivedInvitationFragment;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PromiseMainFragment extends Fragment implements IRefreshCalendar {
	private FragmentPromiseTransactionBinding binding;
	private AccountViewModel accountViewModel;
	private CalendarViewModel calendarViewModel;
	private GoogleAccountLifeCycleObserver googleAccountLifeCycleObserver;
	private PermissionsLifeCycleObserver permissionsLifeCycleObserver;
	private FriendViewModel friendViewModel;
	private RecyclerViewAdapter adapter;
	private boolean initializing = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		googleAccountLifeCycleObserver = new GoogleAccountLifeCycleObserver(requireActivity().getActivityResultRegistry(),
				requireActivity());
		permissionsLifeCycleObserver = new PermissionsLifeCycleObserver(requireActivity());
		getLifecycle().addObserver(permissionsLifeCycleObserver);
		getLifecycle().addObserver(googleAccountLifeCycleObserver);

		accountViewModel = new ViewModelProvider(requireActivity()).get(AccountViewModel.class);
		calendarViewModel = new ViewModelProvider(requireActivity()).get(CalendarViewModel.class);
		friendViewModel = new ViewModelProvider(requireActivity()).get(FriendViewModel.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentPromiseTransactionBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onDestroy() {
		getLifecycle().removeObserver(googleAccountLifeCycleObserver);
		super.onDestroy();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		binding.invitedEventsBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (accountViewModel.getCurrentSignInAccount() == null) {
					Toast.makeText(getContext(), R.string.no_signin_account, Toast.LENGTH_SHORT).show();
					return;
				}
				ReceivedInvitationFragment receivedInvitationFragment = new ReceivedInvitationFragment();
				receivedInvitationFragment.setOnResultInvitedPromiseListener(new ReceivedInvitationFragment.OnResultInvitedPromiseListener() {
					@Override
					public void onResult(boolean changed) {
						if (changed) {
							refreshEvents();
						}
					}
				});

				getParentFragment().getParentFragmentManager().beginTransaction().hide(
						getParentFragment().getParentFragmentManager().findFragmentByTag(MainTransactionFragment.class.getName())).add(
						R.id.fragmentContainerView, receivedInvitationFragment, ReceivedInvitationFragment.class.getName()).addToBackStack(
						ReceivedInvitationFragment.class.getName()).commitAllowingStateLoss();
			}
		});

		binding.newPromiseBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (accountViewModel.getCurrentSignInAccount() == null) {
					Toast.makeText(getContext(), R.string.no_signin_account, Toast.LENGTH_SHORT).show();
					return;
				}
				AddPromiseFragment addPromiseFragment = new AddPromiseFragment();

				getParentFragment().getParentFragmentManager().beginTransaction().hide(
						getParentFragment().getParentFragmentManager().findFragmentByTag(MainTransactionFragment.class.getName())).add(
						R.id.fragmentContainerView, addPromiseFragment, AddPromiseFragment.class.getName()).addToBackStack(
						AddPromiseFragment.class.getName()).commitAllowingStateLoss();
			}
		});

		accountViewModel.getSignInLiveData().observe(getViewLifecycleOwner(), new Observer<GoogleSignInAccount>() {
			@Override
			public void onChanged(GoogleSignInAccount googleSignInAccount) {
				if (!initializing) {

				}
			}
		});


		binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
		binding.recyclerView.addItemDecoration(new RecyclerViewItemDecoration(getContext()));

		adapter = new RecyclerViewAdapter(getContext());
		adapter.setOnClickPromiseItemListener(new OnClickPromiseItemListener() {

			public void onClickedEdit(CalendarRepository.EventObj event, int position) {
				final EditPromiseFragment editPromiseFragment = new EditPromiseFragment();
				editPromiseFragment.setOnFragmentCallback(new OnFragmentCallback<Boolean>() {
					@Override
					public void onResult(Boolean e) {
						syncCalendars();
					}
				});
				Bundle bundle = new Bundle();
				bundle.putString("eventId", event.getEvent().getAsString("_sync_id"));

				editPromiseFragment.setArguments(bundle);

				FragmentManager fragmentManager = getParentFragment().getParentFragmentManager();
				fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag(MainTransactionFragment.class.getName())).add(
						R.id.fragmentContainerView, editPromiseFragment, EditPromiseFragment.class.getName()).addToBackStack(
						EditPromiseFragment.class.getName()).commitAllowingStateLoss();
			}

			@Override
			public void onClickedRemoveEvent(CalendarRepository.EventObj event, int position) {
				new MaterialAlertDialogBuilder(requireActivity()).setTitle(R.string.remove_event)
						.setMessage(R.string.msg_remove_event).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								CalendarRepository.removeEvent(getContext(), event.getEvent(), new BackgroundCallback<ContentValues>() {
									@Override
									public void onResultSuccessful(ContentValues e) {
										syncCalendars();
									}

									@Override
									public void onResultFailed(Exception e) {

									}
								});
								dialog.dismiss();
							}
						}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						}).create().show();

			}

			@Override
			public void onClickedEvent(CalendarRepository.EventObj event, int position) {
				final PromiseInfoFragment promiseInfoFragment = new PromiseInfoFragment();

				Bundle bundle = new Bundle();
				bundle.putString("eventId", event.getEvent().getAsString(CalendarContract.Events._ID));
				promiseInfoFragment.setArguments(bundle);

				FragmentManager fragmentManager = getParentFragment().getParentFragmentManager();
				fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag(MainTransactionFragment.class.getName())).add(
						R.id.fragmentContainerView, promiseInfoFragment, PromiseInfoFragment.class.getName()).addToBackStack(
						PromiseInfoFragment.class.getName()).commitAllowingStateLoss();
			}

			@Override
			public void onClickedRefusal(CalendarRepository.EventObj event, int position) {

			}

			@Override
			public void onClickedAcceptance(CalendarRepository.EventObj event, int position) {

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
					binding.warningLayout.warningText.setText(R.string.empty_fixed_promises);
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
		CalendarRepository.loadCalendar(getContext(), accountViewModel.getLastSignInAccountName(), new BackgroundCallback<ContentValues>() {
			@Override
			public void onResultSuccessful(ContentValues e) {
				loadInvitedEvents();

				CalendarRepository.loadEvents(getContext(), e.getAsString(CalendarContract.Calendars._ID),
						new BackgroundCallback<List<CalendarRepository.EventObj>>() {
							@Override
							public void onResultSuccessful(List<CalendarRepository.EventObj> eventObjList) {
								for (CalendarRepository.EventObj eventObj : eventObjList) {
									eventObj.setMyEvent(false);
								}

								CalendarRepository.loadMyEvents(getContext(), accountViewModel.getLastSignInAccountName(),
										e.getAsString(CalendarContract.Calendars._ID),
										new BackgroundCallback<List<CalendarRepository.EventObj>>() {
											@Override
											public void onResultSuccessful(List<CalendarRepository.EventObj> myEventObjList) {
												Set<Integer> myEventIdSet = new HashSet<>();
												for (CalendarRepository.EventObj eventObj : myEventObjList) {
													myEventIdSet.add(eventObj.getEvent().getAsInteger(CalendarContract.Events._ID));
												}

												for (CalendarRepository.EventObj eventObj : eventObjList) {
													if (myEventIdSet.contains(eventObj.getEvent().getAsInteger(CalendarContract.Events._ID))) {
														eventObj.setMyEvent(true);
													}
												}

												getActivity().runOnUiThread(new Runnable() {
													@Override
													public void run() {
														adapter.setEvents(eventObjList);
														binding.refreshLayout.setRefreshing(false);
														adapter.notifyDataSetChanged();
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

							@Override
							public void onResultFailed(Exception e) {
								getActivity().runOnUiThread(new Runnable() {
									@Override
									public void run() {
										binding.refreshLayout.setRefreshing(false);
									}
								});
							}
						});
			}

			@Override
			public void onResultFailed(Exception e) {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						binding.refreshLayout.setRefreshing(false);
					}
				});
			}
		});
	}

	private void loadInvitedEvents() {
		final String myEmail = accountViewModel.getLastSignInAccountName();
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
										String text = getString(R.string.received_invitation) + ": " + eventObjList.size();
										binding.invitedEventsBtn.setText(text);
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
		private List<CalendarRepository.EventObj> events = new ArrayList<>();
		private OnClickPromiseItemListener onClickPromiseItemListener;

		private final DateTimeFormatter DATE_TIME_FORMATTER;
		private final String SIGN_IN_ACCOUNT_NAME;

		public RecyclerViewAdapter(Context context) {
			DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(context.getString(R.string.promiseDateTimeFormat));
			SIGN_IN_ACCOUNT_NAME = accountViewModel.getLastSignInAccountName();
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
			return new RecyclerViewAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view_promise, null));
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
			private ItemViewPromiseBinding binding;

			public ViewHolder(@NonNull View itemView) {
				super(itemView);
				binding = ItemViewPromiseBinding.bind(itemView);
			}

			public void clear() {

			}

			public void onBind() {
				final int position = getBindingAdapterPosition();
				binding.editBtn.setVisibility(events.get(position).isMyEvent() ? View.VISIBLE : View.GONE);

				binding.getRoot().setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onClickPromiseItemListener.onClickedEvent(events.get(position), position);
					}
				});

				binding.editBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						PopupMenu popupMenu = new PopupMenu(getContext(), binding.editBtn);
						popupMenu.getMenuInflater().inflate(R.menu.event_menu, popupMenu.getMenu());
						popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
							@Override
							public boolean onMenuItemClick(MenuItem item) {
								if (item.getItemId() == R.id.action_edit) {
									//수정
									onClickPromiseItemListener.onClickedEdit(events.get(getBindingAdapterPosition()), getBindingAdapterPosition());
								} else if (item.getItemId() == R.id.action_delete) {
									onClickPromiseItemListener.onClickedRemoveEvent(events.get(getBindingAdapterPosition()), getBindingAdapterPosition());

									//삭제
								}
								return false;
							}
						});

						popupMenu.show();
					}
				});

				CalendarRepository.EventObj eventObj = events.get(getBindingAdapterPosition());

				final ContentValues event = eventObj.getEvent();
				String dtStart = event.getAsString(CalendarContract.Events.DTSTART);
				String eventTimeZone = event.getAsString(CalendarContract.Events.EVENT_TIMEZONE);
				ZonedDateTime start = ZonedDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(dtStart)), ZoneId.of(eventTimeZone));

				binding.dateTime.setText(start.format(DATE_TIME_FORMATTER));
				binding.description.setText(event.getAsString(CalendarContract.Events.DESCRIPTION) == null
						|| event.getAsString(CalendarContract.Events.DESCRIPTION).isEmpty() ?
						getContext().getString(R.string.noDescription) : event.getAsString(CalendarContract.Events.DESCRIPTION));
				binding.title.setText(event.getAsString(CalendarContract.Events.TITLE) == null ? getString(R.string.no_title) :
						event.getAsString(CalendarContract.Events.TITLE));

				if (event.getAsString(CalendarContract.Events.EVENT_LOCATION) != null) {
					if (event.getAsString(CalendarContract.Events.EVENT_LOCATION).isEmpty()) {
						binding.location.setText(getContext().getString(R.string.no_promise_location));
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
					binding.location.setText(getContext().getString(R.string.no_promise_location));
				}

				List<String> attendeeNameList = new ArrayList<>();
				attendeeNameList.add(event.getAsString(CalendarContract.Events.ORGANIZER).equals(SIGN_IN_ACCOUNT_NAME) ?
						getString(R.string.me) : friendViewModel.getName(event.getAsString(CalendarContract.Events.ORGANIZER)));

				for (ContentValues attendee : eventObj.getAttendeeList()) {
					if (attendee.getAsString(CalendarContract.Attendees.ATTENDEE_EMAIL).equals(SIGN_IN_ACCOUNT_NAME)) {
						attendeeNameList.add(getString(R.string.me));
					} else {
						attendeeNameList.add(friendViewModel.getName(attendee));
					}
				}

				String people = AttendeeUtil.toListString(attendeeNameList);
				binding.people.setText(people);
			}
		}
	}
}