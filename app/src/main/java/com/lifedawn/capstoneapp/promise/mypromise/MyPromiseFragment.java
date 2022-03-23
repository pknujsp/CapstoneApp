package com.lifedawn.capstoneapp.promise.mypromise;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
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

import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.account.GoogleAccountLifeCycleObserver;
import com.lifedawn.capstoneapp.common.constants.Constant;
import com.lifedawn.capstoneapp.common.interfaces.BackgroundCallback;
import com.lifedawn.capstoneapp.common.interfaces.IRefreshCalendar;
import com.lifedawn.capstoneapp.common.interfaces.OnClickPromiseItemListener;
import com.lifedawn.capstoneapp.common.interfaces.OnFragmentCallback;
import com.lifedawn.capstoneapp.common.repository.CalendarRepository;
import com.lifedawn.capstoneapp.common.util.AttendeeUtil;
import com.lifedawn.capstoneapp.common.util.PermissionsLifeCycleObserver;
import com.lifedawn.capstoneapp.common.view.RecyclerViewItemDecoration;
import com.lifedawn.capstoneapp.common.viewmodel.AccountViewModel;
import com.lifedawn.capstoneapp.common.viewmodel.CalendarViewModel;
import com.lifedawn.capstoneapp.common.viewmodel.FriendViewModel;
import com.lifedawn.capstoneapp.databinding.FragmentMyPromiseBinding;
import com.lifedawn.capstoneapp.databinding.ItemViewPromiseBinding;
import com.lifedawn.capstoneapp.main.MainTransactionFragment;
import com.lifedawn.capstoneapp.map.LocationDto;
import com.lifedawn.capstoneapp.promise.editpromise.EditPromiseFragment;
import com.lifedawn.capstoneapp.promise.promiseinfo.PromiseInfoFragment;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class MyPromiseFragment extends Fragment implements IRefreshCalendar {
	private FragmentMyPromiseBinding binding;
	private AccountViewModel accountViewModel;
	private GoogleAccountLifeCycleObserver googleAccountLifeCycleObserver;
	private CalendarViewModel calendarViewModel;
	private RecyclerViewAdapter adapter;
	private PermissionsLifeCycleObserver permissionsLifeCycleObserver;
	private FriendViewModel friendViewModel;

	private boolean initializing = true;

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
		binding = FragmentMyPromiseBinding.inflate(inflater);
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
			public void onClickedEdit(CalendarRepository.EventObj event, int position) {
				EditPromiseFragment editPromiseFragment = new EditPromiseFragment();
				editPromiseFragment.setOnFragmentCallback(new OnFragmentCallback<Boolean>() {
					@Override
					public void onResult(Boolean e) {
						syncCalendars();
					}
				});
				Bundle bundle = new Bundle();
				bundle.putString("eventId", event.getEvent().getAsString("_sync_id"));

				editPromiseFragment.setArguments(bundle);

				FragmentManager fragmentManager = getParentFragment().getParentFragment().getParentFragmentManager();
				fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag(MainTransactionFragment.class.getName())).add(
						R.id.fragmentContainerView, editPromiseFragment, EditPromiseFragment.class.getName()).addToBackStack(
						EditPromiseFragment.class.getName()).commit();
			}

			@Override
			public void onClickedRemoveEvent(CalendarRepository.EventObj event, int position) {
				CalendarRepository.removeEvent(getContext(), event.getEvent(), new BackgroundCallback<ContentValues>() {
					@Override
					public void onResultSuccessful(ContentValues e) {
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


			}

			@Override
			public void onClickedAcceptance(CalendarRepository.EventObj event, int position) {


			}
		});
		binding.recyclerView.setAdapter(adapter);
		adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
			@Override
			public void onChanged() {
				super.onChanged();

				if (adapter.getItemCount() > 0) {
					binding.warningLayout.getRoot().setVisibility(View.GONE);
				} else {
					binding.warningLayout.getRoot().setVisibility(View.VISIBLE);
					binding.warningLayout.warningText.setText(R.string.empty_my_promises);
					binding.warningLayout.btn.setVisibility(View.GONE);
				}
			}
		});
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


	@Override
	public void syncCalendars() {
		calendarViewModel.syncCalendars(accountViewModel.getCurrentSignInAccount(), new BackgroundCallback<Boolean>() {
			@SuppressLint("Range")
			@Override
			public void onResultSuccessful(Boolean e) {
				refreshEvents();
			}

			@Override
			public void onResultFailed(Exception e) {
				binding.refreshLayout.setRefreshing(false);
				Toast.makeText(getContext(), R.string.failed_sync_calendar, Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	public void refreshEvents() {
		CalendarRepository.loadCalendar(getContext(), accountViewModel.getLastSignInAccountName(), new BackgroundCallback<ContentValues>() {
			@Override
			public void onResultSuccessful(ContentValues calendar) {
				CalendarRepository.loadMyEvents(getContext(), accountViewModel.getLastSignInAccountName(), calendar.getAsString(CalendarContract.Calendars._ID),
						new BackgroundCallback<List<CalendarRepository.EventObj>>() {
							@Override
							public void onResultSuccessful(List<CalendarRepository.EventObj> eventObjList) {
								if (getActivity() != null) {
									getActivity().runOnUiThread(new Runnable() {
										@Override
										public void run() {
											binding.refreshLayout.setRefreshing(false);
											adapter.setEvents(eventObjList);
											adapter.notifyDataSetChanged();
										}
									});
								}
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


	private class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
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
				binding.getRoot().setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onClickPromiseItemListener.onClickedEvent(events.get(getBindingAdapterPosition()), getBindingAdapterPosition());
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
						getContext().getString(R.string.noDescription) :
						event.getAsString(CalendarContract.Events.DESCRIPTION));
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
				attendeeNameList.add(event.getAsString(CalendarContract.Events.ORGANIZER).equals(signInAccountEmail) ?
						getString(R.string.me) : friendViewModel.getName(event.getAsString(CalendarContract.Events.ORGANIZER)));

				for (ContentValues attendee : eventObj.getAttendeeList()) {
					if (attendee.getAsString(CalendarContract.Attendees.ATTENDEE_EMAIL).equals(signInAccountEmail)) {
						attendeeNameList.add(getString(R.string.me));
					} else {
						attendeeNameList.add(friendViewModel.getName(attendee));
					}
				}

				String people = AttendeeUtil.toListString(attendeeNameList);
				binding.people.setText(people.isEmpty() ? getString(R.string.no_attendee) : people);
			}
		}
	}
}