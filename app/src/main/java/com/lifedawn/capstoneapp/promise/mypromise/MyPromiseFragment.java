package com.lifedawn.capstoneapp.promise.mypromise;

import android.accounts.Account;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
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
		} else {
			permissionsLifeCycleObserver.launchCalendarPermissionsLauncher(new ActivityResultCallback<Boolean>() {
				@Override
				public void onActivityResult(Boolean result) {
					if (result) {
						binding.refreshLayout.setRefreshing(true);
						refreshEvents();
					} else {
						//권한 거부됨
					}
				}
			});
		}


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
				CalendarRepository.loadMyEvents(getContext(), account, e.getAsString(CalendarContract.Calendars._ID),
						new BackgroundCallback<List<CalendarRepository.EventObj>>() {
							@Override
							public void onResultSuccessful(List<CalendarRepository.EventObj> e) {
								if (getActivity() != null) {
									getActivity().runOnUiThread(new Runnable() {
										@Override
										public void run() {
											binding.refreshLayout.setRefreshing(false);
											adapter.setEvents(e);
											adapter.notifyDataSetChanged();
										}
									});
								}
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
		private DateTimeFormatter DATE_TIME_FORMATTER;

		public RecyclerViewAdapter(Context context) {
			DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(context.getString(R.string.promiseDateTimeFormat));
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
						onClickPromiseItemListener.onClickedEdit(events.get(getBindingAdapterPosition()), getBindingAdapterPosition());
					}
				});

				CalendarRepository.EventObj eventObj = events.get(getBindingAdapterPosition());

				final ContentValues event = eventObj.getEvent();
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


				String people = AttendeeUtil.toListString(eventObj.getAttendeeList());
				binding.people.setText(people.isEmpty() ? getString(R.string.no_attendee) : people);
			}
		}
	}
}