package com.lifedawn.capstoneapp.calendar.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.kizitonwose.calendarview.model.CalendarDay;
import com.kizitonwose.calendarview.model.CalendarMonth;
import com.kizitonwose.calendarview.model.DayOwner;
import com.kizitonwose.calendarview.ui.DayBinder;
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder;
import com.kizitonwose.calendarview.ui.ViewContainer;
import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.common.classes.AlreadySyncingException;
import com.lifedawn.capstoneapp.common.interfaces.BackgroundCallback;
import com.lifedawn.capstoneapp.common.interfaces.IRefreshCalendar;
import com.lifedawn.capstoneapp.common.interfaces.OnClickPromiseItemListener;
import com.lifedawn.capstoneapp.common.interfaces.OnFragmentCallback;
import com.lifedawn.capstoneapp.common.repository.CalendarRepository;
import com.lifedawn.capstoneapp.common.util.PermissionsLifeCycleObserver;
import com.lifedawn.capstoneapp.common.viewmodel.AccountViewModel;
import com.lifedawn.capstoneapp.common.viewmodel.CalendarViewModel;
import com.lifedawn.capstoneapp.databinding.CalendarDayLayoutBinding;
import com.lifedawn.capstoneapp.databinding.CalendarMonthHeaderLayoutBinding;
import com.lifedawn.capstoneapp.databinding.EventDialogFragmentBinding;
import com.lifedawn.capstoneapp.databinding.FragmentCalendarBinding;
import com.lifedawn.capstoneapp.databinding.ItemViewEventListBinding;
import com.lifedawn.capstoneapp.databinding.ViewEventDialogBinding;
import com.lifedawn.capstoneapp.main.MainTransactionFragment;
import com.lifedawn.capstoneapp.promise.editpromise.EditPromiseFragment;
import com.lifedawn.capstoneapp.promise.promiseinfo.PromiseInfoFragment;

import org.jetbrains.annotations.NotNull;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class CalendarFragment extends Fragment implements IRefreshCalendar {
	private final String TAG = "CalendarFragment";
	private final DayOfWeek FIRST_DAY_OF_WEEK = WeekFields.of(Locale.getDefault()).getFirstDayOfWeek();
	private final Map<String, List<CalendarRepository.EventObj>> eventsMap = new HashMap<>();
	private final ZoneId ZONEID = ZoneId.systemDefault();

	public static String myEmail;


	private FragmentCalendarBinding binding;
	private AccountViewModel accountViewModel;
	private CalendarViewModel calendarViewModel;
	private PermissionsLifeCycleObserver permissionsLifeCycleObserver;

	private YearMonth currentCalendarYearMonth;
	private YearMonth beginEventsYearMonth;
	private YearMonth endEventsYearMonth;

	private ContentValues calendar;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		permissionsLifeCycleObserver = new PermissionsLifeCycleObserver(requireActivity());
		getLifecycle().addObserver(permissionsLifeCycleObserver);
		accountViewModel = new ViewModelProvider(getActivity()).get(AccountViewModel.class);
		calendarViewModel = new ViewModelProvider(getActivity()).get(CalendarViewModel.class);
		myEmail = accountViewModel.getLastSignInAccount().getEmail();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentCalendarBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		binding.calendarView.setMonthHeaderBinder(new MonthHeaderFooterBinder<MonthHeaderViewContainer>() {
			final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy M");
			final DayOfWeek[] dayOfWeeks = new DayOfWeek[]{DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
					DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY};
			ZonedDateTime zonedDateTime = ZonedDateTime.now();

			@NonNull
			@Override
			public MonthHeaderViewContainer create(@NonNull View view) {
				return new MonthHeaderViewContainer(view);
			}

			// 달력 상단 바인딩
			@Override
			public void bind(@NonNull MonthHeaderViewContainer monthHeaderViewContainer, @NonNull CalendarMonth calendarMonth) {
				zonedDateTime = zonedDateTime.withYear(calendarMonth.getYear());
				zonedDateTime = zonedDateTime.withMonth(calendarMonth.getMonth());
				monthHeaderViewContainer.binding.headerTextView.setText(zonedDateTime.format(dateTimeFormatter));

				if (monthHeaderViewContainer.binding.days.getTag() == null) {
					monthHeaderViewContainer.binding.days.setTag(Boolean.TRUE);
					int childCount = monthHeaderViewContainer.binding.days.getChildCount();
					for (int child = 0; child < childCount; child++) {
						((TextView) monthHeaderViewContainer.binding.days.getChildAt(child)).setText(
								dayOfWeeks[child].getDisplayName(TextStyle.SHORT, Locale.getDefault()));
					}
				}

			}
		});

		// 날짜표시와 밑에 파란색으로 약속 카운팅도 하기
		binding.calendarView.setDayBinder(new DayBinder<DayViewContainer>() {
			String dateText;
			List<CalendarRepository.EventObj> eventObjList;
			int acceptedCount;
			int eventCount;

			@NonNull
			@Override
			public DayViewContainer create(@NonNull View view) {
				return new DayViewContainer(view);
			}

			@Override
			public void bind(@NonNull DayViewContainer viewContainer, @NonNull CalendarDay calendarDay) {
				acceptedCount = 0;
				eventCount = 0;
				dateText = calendarDay.getDate().toString();

				viewContainer.binding.calendarDayText.setText(String.valueOf(calendarDay.getDate().getDayOfMonth()));
				if (calendarDay.getOwner() == DayOwner.THIS_MONTH) {
					viewContainer.binding.calendarDayText.setTextColor(Color.BLACK);
				} else {
					viewContainer.binding.calendarDayText.setTextColor(Color.LTGRAY);
				}

				final LocalDate date = calendarDay.getDate();
				viewContainer.binding.getRoot().setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Bundle bundle = new Bundle();
						bundle.putSerializable("criteriaDate", date);

						EventDialogFragment eventDialogFragment =
								new EventDialogFragment();
						eventDialogFragment.setArguments(bundle);
						eventDialogFragment.setOnClickPromiseItemListener(new OnClickPromiseItemListener() {
							@Override
							public void onClickedEdit(CalendarRepository.EventObj event, int position) {
								EditPromiseFragment editPromiseFragment = new EditPromiseFragment();
								editPromiseFragment.setOnFragmentCallback(new OnFragmentCallback<Boolean>() {
									@Override
									public void onResult(Boolean e) {
										refreshEvents();
									}
								});
								Bundle bundle = new Bundle();
								bundle.putString("eventId", event.getEvent().getAsString("_sync_id"));

								editPromiseFragment.setArguments(bundle);

								FragmentManager fragmentManager =
										getParentFragment().getParentFragment().getParentFragmentManager();
								fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag(MainTransactionFragment.class.getName())).add(
										R.id.fragmentContainerView, editPromiseFragment, EditPromiseFragment.class.getName()).addToBackStack(
										EditPromiseFragment.class.getName()).commit();
							}

							@Override
							public void onClickedEvent(CalendarRepository.EventObj event, int position) {

							}

							@Override
							public void onClickedRefusal(CalendarRepository.EventObj event, int position) {

							}

							@Override
							public void onClickedAcceptance(CalendarRepository.EventObj event, int position) {

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
						});
						eventDialogFragment.setEventsMap(eventsMap);
						eventDialogFragment.show(getChildFragmentManager(), EventDialogFragment.class.getName());
					}
				});

				if (eventsMap.containsKey(dateText)) {
					eventObjList = eventsMap.get(dateText);
					eventCount = eventObjList.size();

					for (CalendarRepository.EventObj eventObj : eventObjList) {
						boolean fixed = true;

						for (ContentValues attendee : eventObj.getAttendeeList()) {
							if (attendee.containsKey(CalendarContract.Attendees.ATTENDEE_STATUS) &&
									attendee.getAsInteger(CalendarContract.Attendees.ATTENDEE_STATUS)
											!= CalendarContract.Attendees.ATTENDEE_STATUS_ACCEPTED) {
								fixed = false;
								break;
							}
						}

						if (fixed) {
							acceptedCount++;
						}

					}
				}

				viewContainer.binding.acceptedPromiseCount.setText(acceptedCount > 0 ? String.valueOf(acceptedCount) : null);
				viewContainer.binding.eventCount.setText(eventCount > 0 ? String.valueOf(eventCount) : null);
			}
		});

		binding.calendarView.setMonthScrollListener(new Function1<CalendarMonth, Unit>() {

			@Override
			public Unit invoke(CalendarMonth calendarMonth) {
				currentCalendarYearMonth = currentCalendarYearMonth.withYear(calendarMonth.getYear()).withMonth(calendarMonth.getMonth());

				Log.e(TAG, "currentCalendarYearMonth : " + currentCalendarYearMonth.toString());
				Log.e(TAG, "beginEventsYearMonth : " + beginEventsYearMonth.toString());
				Log.e(TAG, "endEventsYearMonth : " + endEventsYearMonth.toString());
				Log.e(TAG, "----------------------------------------");


				if ((currentCalendarYearMonth.compareTo(beginEventsYearMonth)) <= 0 || (currentCalendarYearMonth.compareTo(endEventsYearMonth)) >= 0) {
					binding.progressCircular.setVisibility(View.VISIBLE);
					beginEventsYearMonth = currentCalendarYearMonth.minusYears(1);
					endEventsYearMonth = currentCalendarYearMonth.plusYears(1);
					binding.calendarView.updateMonthRangeAsync(beginEventsYearMonth, endEventsYearMonth);
					refreshEvents();
				}
				return null;
			}
		});

		binding.refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				binding.progressCircular.setVisibility(View.VISIBLE);
				syncCalendars();
			}
		});

		currentCalendarYearMonth = YearMonth.now();
		beginEventsYearMonth = YearMonth.of(currentCalendarYearMonth.getYear(), currentCalendarYearMonth.getMonthValue());
		endEventsYearMonth = currentCalendarYearMonth.plusYears(1);
		binding.calendarView.setup(currentCalendarYearMonth, endEventsYearMonth, FIRST_DAY_OF_WEEK);

		if (permissionsLifeCycleObserver.checkCalendarPermissions()) {
			binding.refreshLayout.setRefreshing(true);
			loadCalendar();

			calendarViewModel.getSyncCalendarLiveData().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
				@Override
				public void onChanged(Boolean aBoolean) {
					loadCalendar();
				}
			});
		} else {
			binding.warningLayout.btn.setText(R.string.check_permissions);
			binding.warningLayout.getRoot().setVisibility(View.VISIBLE);

			final ActivityResultCallback<Boolean> activityResultCallback = new ActivityResultCallback<Boolean>() {
				@Override
				public void onActivityResult(Boolean result) {
					if (result) {
						binding.warningLayout.getRoot().setVisibility(View.GONE);
						binding.refreshLayout.setRefreshing(true);
						loadCalendar();
					} else {
						//권한 거부됨
						binding.warningLayout.warningText.setText(R.string.needs_calendar_permission);
						final ActivityResultCallback<Boolean> activityResultCallback = this;

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

	private void loadCalendar() {
		CalendarRepository.loadCalendar(getContext(), accountViewModel.getLastSignInAccountName(), new BackgroundCallback<ContentValues>() {
			@Override
			public void onResultSuccessful(ContentValues e) {
				calendar = e;
				refreshEvents();
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
	public void syncCalendars() {
		calendarViewModel.syncCalendars(accountViewModel.getCurrentSignInAccount(), new SyncCalendarCallback<Boolean>() {
			@Override
			public void onResultSuccessful(Boolean e) {
				loadCalendar();
			}

			@Override
			public void onResultFailed(Exception e) {
				Toast.makeText(getContext(), R.string.failed_sync_calendar, Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onSyncStarted() {
				super.onSyncStarted();
			}

			@Override
			public void onAlreadySyncing() {
				Toast.makeText(getContext(), R.string.already_syncing, Toast.LENGTH_SHORT).show();
			}
		});
	}


	@Override
	public void refreshEvents() {
		ZonedDateTime criteriaDateTime = ZonedDateTime.of(currentCalendarYearMonth.getYear(), currentCalendarYearMonth.getMonthValue(), 1, 0, 0, 0, 0, ZONEID);
		ZonedDateTime begin = criteriaDateTime.withYear(beginEventsYearMonth.getYear()).withMonth(beginEventsYearMonth.getMonthValue());
		ZonedDateTime end = criteriaDateTime.withYear(endEventsYearMonth.getYear()).withMonth(endEventsYearMonth.getMonthValue());

		CalendarRepository.loadEvents(getContext(), calendar.getAsString(CalendarContract.Calendars._ID), begin, end
				, new BackgroundCallback<List<CalendarRepository.EventObj>>() {

					@Override
					public void onResultSuccessful(List<CalendarRepository.EventObj> e) {
						eventsMap.clear();

						String dateText = null;
						ZonedDateTime eventDateTime = null;
						final ZoneId zoneId = begin.getZone();

						for (CalendarRepository.EventObj eventObj : e) {
							ContentValues event = eventObj.getEvent();
							eventDateTime =
									ZonedDateTime.ofInstant(Instant.ofEpochMilli(event.getAsLong(CalendarContract.Events.DTSTART)), zoneId);
							dateText = eventDateTime.toLocalDate().toString();
							eventObj.setDate(dateText);

							if (!eventsMap.containsKey(dateText)) {
								eventsMap.put(dateText, new ArrayList<>());
							}
							eventsMap.get(dateText).add(eventObj);
						}

						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								binding.calendarView.notifyCalendarChanged();
								binding.refreshLayout.setRefreshing(false);
								binding.progressCircular.setVisibility(View.GONE);
							}
						});
					}

					@Override
					public void onResultFailed(Exception e) {

					}
				});

	}


	// 일 보여주는 컨테이너 바인딩
	private static class DayViewContainer extends ViewContainer {
		private CalendarDayLayoutBinding binding;

		public DayViewContainer(@NonNull View view) {
			super(view);
			binding = CalendarDayLayoutBinding.bind(view);
		}
	}

	// 달 보여주는 컨테이너 바인딩
	private static class MonthHeaderViewContainer extends ViewContainer {
		private CalendarMonthHeaderLayoutBinding binding;

		public MonthHeaderViewContainer(@NonNull View view) {
			super(view);
			this.binding = CalendarMonthHeaderLayoutBinding.bind(view);
		}
	}

	public static class EventDialogFragment extends DialogFragment {
		private static final int FIRST_POSITION = Integer.MAX_VALUE / 2;
		private final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/M/d E");
		private final DateTimeFormatter EVENT_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy/M/d E a hh:mm");
		private EventDialogFragmentBinding binding;
		private CompositePageTransformer compositePageTransformer;
		private LocalDate criteriaDate;
		private Bundle bundle;
		private Map<String, List<CalendarRepository.EventObj>> eventsMap;
		private int acceptedEventColor;
		private int eventColor;


		private OnClickPromiseItemListener onClickPromiseItemListener;

		public void setOnClickPromiseItemListener(OnClickPromiseItemListener onClickPromiseItemListener) {
			this.onClickPromiseItemListener = onClickPromiseItemListener;
		}

		public EventDialogFragment setEventsMap(Map<String, List<CalendarRepository.EventObj>> eventsMap) {
			this.eventsMap = eventsMap;
			return this;
		}

		@Override
		public void onAttach(@NonNull @NotNull Context context) {
			super.onAttach(context);
		}

		@Override
		public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			bundle = savedInstanceState != null ? savedInstanceState : getArguments();
			criteriaDate = (LocalDate) bundle.getSerializable("criteriaDate");
			acceptedEventColor = ContextCompat.getColor(getContext(), R.color.design_default_color_primary);
			eventColor = ContextCompat.getColor(getContext(), R.color.white);
		}

		@Override
		public void onSaveInstanceState(@NonNull Bundle outState) {
			super.onSaveInstanceState(outState);
			outState.putAll(bundle);
		}

		@NonNull
		@Override
		public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
			return new Dialog(getContext(), R.style.DialogTransparent);
		}

		@Nullable
		@org.jetbrains.annotations.Nullable
		@Override
		public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
			binding = EventDialogFragmentBinding.inflate(inflater);
			return binding.getRoot();
		}

		@Override
		public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
			super.onViewCreated(view, savedInstanceState);
			final int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24f, getResources().getDisplayMetrics());

			binding.viewPager.setOffscreenPageLimit(2);
			binding.viewPager.getChildAt(0).setOverScrollMode(View.OVER_SCROLL_NEVER);

			compositePageTransformer = new CompositePageTransformer();
			compositePageTransformer.addTransformer(new MarginPageTransformer(margin));
			compositePageTransformer.addTransformer(new ViewPager2.PageTransformer() {
				@Override
				public void transformPage(@NonNull View page, float position) {
					float r = 1 - Math.abs(position);
					page.setScaleY(0.8f + r * 0.2f);
				}
			});
			binding.viewPager.setPageTransformer(compositePageTransformer);
			binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
				@Override
				public void onPageSelected(int position) {
					super.onPageSelected(position);
				}
			});

			final EventViewPagerAdapter adapter = new EventViewPagerAdapter(criteriaDate);
			adapter.setOnClickPromiseItemListener(new OnClickPromiseItemListener() {
				@Override
				public void onClickedRemoveEvent(CalendarRepository.EventObj event, int position) {
					eventsMap.get(event.getDate()).remove(event);
					adapter.notifyDataSetChanged();

					onClickPromiseItemListener.onClickedRemoveEvent(event, position);
				}

				@Override
				public void onClickedEdit(CalendarRepository.EventObj event, int position) {
					onClickPromiseItemListener.onClickedEdit(event, position);
					dismiss();
				}

				@Override
				public void onClickedEvent(CalendarRepository.EventObj event, int position) {
					PromiseInfoFragment promiseInfoFragment = new PromiseInfoFragment();

					Bundle bundle = new Bundle();
					bundle.putString("eventId", event.getEvent().getAsString(CalendarContract.Events._ID));
					promiseInfoFragment.setArguments(bundle);

					FragmentManager fragmentManager =
							getParentFragment().getParentFragment().getParentFragment().getParentFragmentManager();
					fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag(MainTransactionFragment.class.getName())).add(
							R.id.fragmentContainerView, promiseInfoFragment, PromiseInfoFragment.class.getName()).addToBackStack(
							PromiseInfoFragment.class.getName()).commit();
					dismiss();
				}

				@Override
				public void onClickedRefusal(CalendarRepository.EventObj event, int position) {

				}

				@Override
				public void onClickedAcceptance(CalendarRepository.EventObj event, int position) {

				}
			});

			binding.viewPager.setAdapter(adapter);
			binding.viewPager.setCurrentItem(FIRST_POSITION, false);
			binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
				private int lastPosition = FIRST_POSITION;

				@Override
				public void onPageSelected(int position) {
					super.onPageSelected(position);
					lastPosition = position;
				}
			});

			binding.goToTodayBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					LocalDate criteria = LocalDate.parse(criteriaDate.toString());
					LocalDate now = LocalDate.now();

					int newPos = FIRST_POSITION - (int) (criteria.toEpochDay() - now.toEpochDay());
					// 10 ,9 -> -1
					// 9, 10 -> +1
					binding.viewPager.setCurrentItem(newPos, true);
				}
			});

			binding.goToSelectedPositionBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					binding.viewPager.setCurrentItem(FIRST_POSITION, true);
				}
			});
		}

		@Override
		public void onResume() {
			super.onResume();
			Window window = getDialog().getWindow();
			window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		}

		@Override
		public void onDestroy() {
			super.onDestroy();
		}

		private class EventViewPagerAdapter extends RecyclerView.Adapter<EventViewPagerAdapter.ViewHolder> {
			private LayoutInflater layoutInflater;
			private final LocalDate criteriaDate;
			private OnClickPromiseItemListener onClickPromiseItemListener;

			public EventViewPagerAdapter setOnClickPromiseItemListener(OnClickPromiseItemListener onClickPromiseItemListener) {
				this.onClickPromiseItemListener = onClickPromiseItemListener;
				return this;
			}

			public EventViewPagerAdapter(LocalDate criteriaDate) {
				this.criteriaDate = criteriaDate;
				layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			}

			@NonNull
			@NotNull
			@Override
			public CalendarFragment.EventDialogFragment.EventViewPagerAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
				return new ViewHolder(layoutInflater.inflate(R.layout.view_event_dialog, parent, false));
			}

			@Override
			public void onViewRecycled(@NonNull ViewHolder holder) {
				holder.clear();
				super.onViewRecycled(holder);
			}

			@Override
			public void onBindViewHolder(@NonNull @NotNull CalendarFragment.EventDialogFragment.EventViewPagerAdapter.ViewHolder holder, int position) {
				holder.onBind(position);
			}

			@Override
			public int getItemCount() {
				return Integer.MAX_VALUE;
			}

			private class ViewHolder extends RecyclerView.ViewHolder {
				private ViewEventDialogBinding binding;
				private LocalDate date;
				private EventListAdapter adapter;


				public ViewHolder(@NonNull @NotNull View itemView) {
					super(itemView);
					binding = ViewEventDialogBinding.bind(itemView);
					binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),
							RecyclerView.VERTICAL, false));
				}

				public void clear() {
					binding.recyclerView.setAdapter(null);
				}

				private void setEmptyInfo() {
					if (adapter != null) {
						if (adapter.getItemCount() > 0) {
							binding.warningLayout.getRoot().setVisibility(View.GONE);
						} else {
							binding.warningLayout.btn.setVisibility(View.GONE);
							binding.warningLayout.warningText.setText(R.string.empty_promises);
							binding.warningLayout.getRoot().setVisibility(View.VISIBLE);
						}
					} else {
						binding.warningLayout.btn.setVisibility(View.GONE);
						binding.warningLayout.warningText.setText(R.string.empty_promises);
						binding.warningLayout.getRoot().setVisibility(View.VISIBLE);
					}
				}

				public void onBind(int position) {
					int dateAmount = position - FIRST_POSITION;
					Log.e("position", position + ", " + FIRST_POSITION);

					date = criteriaDate.plusDays(dateAmount);

					binding.date.setText(date.format(DATE_FORMATTER));
					String dateText = date.toString();

					if (eventsMap.containsKey(dateText)) {
						adapter = new EventListAdapter(eventsMap.get(dateText));
						adapter.setOnClickPromiseItemListener(onClickPromiseItemListener);
						adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
							@Override
							public void onChanged() {
								super.onChanged();
								setEmptyInfo();
							}
						});

						binding.recyclerView.setAdapter(adapter);
					}
					setEmptyInfo();
				}


				private class EventListAdapter extends RecyclerView.Adapter<EventListAdapter.EventViewHolder> {
					private List<CalendarRepository.EventObj> eventList;
					private OnClickPromiseItemListener onClickPromiseItemListener;
					private List<ContentValues> attendeeList;

					public EventListAdapter setOnClickPromiseItemListener(OnClickPromiseItemListener onClickPromiseItemListener) {
						this.onClickPromiseItemListener = onClickPromiseItemListener;
						return this;
					}

					public EventListAdapter(List<CalendarRepository.EventObj> eventList) {
						this.eventList = eventList;
					}

					@NonNull
					@Override
					public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
						ItemViewEventListBinding binding = ItemViewEventListBinding.inflate(LayoutInflater.from(parent.getContext()));
						binding.getRoot().setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
								ViewGroup.LayoutParams.WRAP_CONTENT));

						return new EventViewHolder(binding);
					}

					@Override
					public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
						holder.onBind();
					}

					@Override
					public int getItemCount() {
						return eventList.size();
					}

					private class EventViewHolder extends RecyclerView.ViewHolder {
						private ItemViewEventListBinding binding;

						public EventViewHolder(ItemViewEventListBinding binding) {
							super(binding.getRoot());
							this.binding = binding;
						}

						public void onBind() {
							int position = getBindingAdapterPosition();

							binding.getRoot().setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									onClickPromiseItemListener.onClickedEvent(eventList.get(position), position);
								}
							});
							binding.getRoot().setOnLongClickListener(new View.OnLongClickListener() {
								@Override
								public boolean onLongClick(View v) {
									CalendarRepository.EventObj eventObj = eventList.get(position);
									if (eventObj.getEvent().getAsString(CalendarContract.Events.ACCOUNT_NAME).equals(
											CalendarFragment.myEmail)) {
										PopupMenu popupMenu = new PopupMenu(getContext(), binding.eventTitle);
										popupMenu.getMenuInflater().inflate(R.menu.event_menu, popupMenu.getMenu());
										popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
											@Override
											public boolean onMenuItemClick(MenuItem item) {
												if (item.getItemId() == R.id.action_edit) {
													//수정
													onClickPromiseItemListener.onClickedEdit(eventObj, position);
												} else if (item.getItemId() == R.id.action_delete) {
													onClickPromiseItemListener.onClickedRemoveEvent(eventObj, position);
													//삭제
												}
												return false;
											}
										});
										popupMenu.show();
									}

									return true;
								}
							});

							ContentValues event = eventList.get(position).getEvent();
							String title = event.containsKey(CalendarContract.Events.TITLE) ?
									event.getAsString(CalendarContract.Events.TITLE) : "";
							binding.eventTitle.setText(new String((position + 1) + " - " + (title.isEmpty() ?
									getString(R.string.no_title) : title)));

							ZonedDateTime start = ZonedDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(event.getAsString(CalendarContract.Events.DTSTART))),
									ZoneId.of(event.getAsString(CalendarContract.Events.EVENT_TIMEZONE)));
							binding.dateTime.setText(EVENT_DATE_TIME_FORMATTER.format(start));
						}

					}

				}

			}

		}

	}

}