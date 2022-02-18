package com.lifedawn.capstoneapp.promise.mypromise;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.account.GoogleAccountLifeCycleObserver;
import com.lifedawn.capstoneapp.common.constants.Constant;
import com.lifedawn.capstoneapp.common.interfaces.BackgroundCallback;
import com.lifedawn.capstoneapp.common.interfaces.OnClickPromiseItemListener;
import com.lifedawn.capstoneapp.common.view.ProgressDialog;
import com.lifedawn.capstoneapp.common.view.RecyclerViewItemDecoration;
import com.lifedawn.capstoneapp.common.viewmodel.AccountViewModel;
import com.lifedawn.capstoneapp.common.viewmodel.CalendarViewModel;
import com.lifedawn.capstoneapp.databinding.FragmentMyPromiseBinding;
import com.lifedawn.capstoneapp.databinding.ItemViewPromiseBinding;
import com.lifedawn.capstoneapp.main.MainTransactionFragment;
import com.lifedawn.capstoneapp.main.MyApplication;
import com.lifedawn.capstoneapp.map.LocationDto;
import com.lifedawn.capstoneapp.promise.editpromise.EditPromiseFragment;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class MyPromiseFragment extends Fragment {
	private FragmentMyPromiseBinding binding;
	private AccountViewModel accountViewModel;
	private GoogleAccountLifeCycleObserver googleAccountLifeCycleObserver;
	private CalendarViewModel calendarViewModel;
	private RecyclerViewAdapter adapter;
	private AlertDialog dialog;
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
			public void onClickedEdit(Event event, int position) {
				EditPromiseFragment editPromiseFragment = new EditPromiseFragment();
				Map<String, Object> map = new HashMap<>();
				Set<String> keySet = event.keySet();
				for (String key : keySet) {
					map.put(key, event.get(key));
				}

				Bundle bundle = new Bundle();
				bundle.putSerializable("map", (Serializable) map);
				editPromiseFragment.setArguments(bundle);

				FragmentManager fragmentManager = getParentFragment().getParentFragment().getParentFragmentManager();
				fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag(MainTransactionFragment.class.getName())).add(
						R.id.fragmentContainerView, editPromiseFragment, EditPromiseFragment.class.getName()).addToBackStack(
						EditPromiseFragment.class.getName()).commit();
			}

			@Override
			public void onClickedEvent(Event event, int position) {


			}

			@Override
			public void onClickedRefusal(Event event, int position) {


			}

			@Override
			public void onClickedAcceptance(Event event, int position) {


			}
		});
		binding.recyclerView.setAdapter(adapter);



		if (calendarViewModel.getCalendarService() == null) {
			if (accountViewModel.getUsingAccountType() == Constant.ACCOUNT_GOOGLE) {
				calendarViewModel.createCalendarService(accountViewModel.getGoogleAccountCredential(), googleAccountLifeCycleObserver, new BackgroundCallback<Calendar>() {
					@Override
					public void onResultSuccessful(Calendar e) {
						if (calendarViewModel.getMainCalendarId() == null) {
							calendarViewModel.existingPromiseCalendar(e, new BackgroundCallback<CalendarListEntry>() {
								@Override
								public void onResultSuccessful(CalendarListEntry e) {
									refresh();
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
			refresh();
		}
		initializing = false;
	}

	private void refresh() {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				dialog = ProgressDialog.showDialog(getActivity());
			}
		});

		MyApplication.EXECUTOR_SERVICE.execute(new Runnable() {
			@Override
			public void run() {
				final Calendar calendarService = calendarViewModel.getCalendarService();
				final String calendarId = calendarViewModel.getMainCalendarId();
				final List<Event> eventList = new ArrayList<>();
				String pageToken = null;

				try {
					do {
						Events events = calendarService.events().list(calendarId).setPageToken(pageToken).execute();
						eventList.addAll(events.getItems());
						pageToken = events.getNextPageToken();
					} while (pageToken != null);

					if (getActivity() != null) {
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								adapter.setEvents(eventList);
								adapter.notifyDataSetChanged();
								dialog.dismiss();
							}
						});
					}
				} catch (Exception e) {
					if (getActivity() != null) {
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								adapter.setEvents(eventList);
								adapter.notifyDataSetChanged();
								dialog.dismiss();
							}
						});
					}
				}


			}
		});
	}

	private static class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
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

				final Event event = events.get(getBindingAdapterPosition());
				EventDateTime eventDateTime = event.getStart();
				ZonedDateTime start = ZonedDateTime.parse(eventDateTime.getDateTime().toString());
				start = start.withZoneSameInstant(ZoneId.of(eventDateTime.getTimeZone()));

				binding.dateTime.setText(start.format(DATE_TIME_FORMATTER));
				binding.description.setText(event.getDescription());
				binding.title.setText(event.getSummary());

				LocationDto locationDto = new LocationDto();
				if (event.getLocation() != null) {
					locationDto = LocationDto.toLocationDto(event.getLocation());
					binding.location.setText(
							locationDto.getLocationType() == Constant.ADDRESS ? locationDto.getAddressName() : locationDto.getPlaceName());
				}
			}
		}
	}
}