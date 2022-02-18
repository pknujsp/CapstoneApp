package com.lifedawn.capstoneapp.calendar.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.kizitonwose.calendarview.model.CalendarDay;
import com.kizitonwose.calendarview.model.CalendarMonth;
import com.kizitonwose.calendarview.model.DayOwner;
import com.kizitonwose.calendarview.ui.DayBinder;
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder;
import com.kizitonwose.calendarview.ui.ViewContainer;
import com.lifedawn.capstoneapp.common.interfaces.BackgroundCallback;
import com.lifedawn.capstoneapp.common.viewmodel.AccountViewModel;
import com.lifedawn.capstoneapp.common.viewmodel.CalendarViewModel;
import com.lifedawn.capstoneapp.databinding.CalendarDayLayoutBinding;
import com.lifedawn.capstoneapp.databinding.CalendarMonthHeaderLayoutBinding;
import com.lifedawn.capstoneapp.databinding.FragmentCalendarBinding;
import com.lifedawn.capstoneapp.main.MyApplication;

import org.jetbrains.annotations.NotNull;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class CalendarFragment extends Fragment {
    private FragmentCalendarBinding binding;
    private AccountViewModel accountViewModel;
    private CalendarViewModel calendarViewModel;
    private final DayOfWeek FIRST_DAY_OF_WEEK = WeekFields.of(Locale.getDefault()).getFirstDayOfWeek();
    private final Map<String, List<Event>> eventsMap = new HashMap<>();

    private YearMonth currentMonth = YearMonth.now(ZoneId.systemDefault());
    private YearMonth firstMonth = currentMonth.minusMonths(10);
    private YearMonth lastMonth = currentMonth.plusMonths(10);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        accountViewModel = new ViewModelProvider(getActivity()).get(AccountViewModel.class);
        calendarViewModel = new ViewModelProvider(getActivity()).get(CalendarViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCalendarBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.calendarView.setup(firstMonth, lastMonth, FIRST_DAY_OF_WEEK);

        binding.calendarView.setMonthHeaderBinder(new MonthHeaderFooterBinder<MonthHeaderViewContainer>() {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy M");
            ZonedDateTime zonedDateTime = ZonedDateTime.now();
            final DayOfWeek[] dayOfWeeks = new DayOfWeek[]{DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY};

            ZonedDateTime firstDateTime;
            ZonedDateTime endDateTime;
            final ZoneId zoneId = ZoneId.systemDefault();

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

                firstDateTime = ZonedDateTime.of(calendarMonth.getYear(), calendarMonth.getMonth(), 1, 0, 0, 0, 0, zoneId);
                endDateTime = firstDateTime.plusMonths(1);

                DateTime timeMin = new DateTime(firstDateTime.toInstant().getEpochSecond() * 1000L);
                DateTime timeMax = new DateTime(endDateTime.toInstant().getEpochSecond() * 1000L);
                loadEvents(timeMin, timeMax, new BackgroundCallback<List<Event>>() {
                    @Override
                    public void onResultSuccessful(List<Event> events) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                }
                            });
                        }
                    }

                    @Override
                    public void onResultFailed(Exception e) {

                    }
                });
            }
        });

        // 날짜표시와 밑에 파란색으로 약속 카운팅도 하기
        binding.calendarView.setDayBinder(new DayBinder<DayViewContainer>() {

            @NonNull
            @Override
            public DayViewContainer create(@NonNull View view) {
                return new DayViewContainer(view);
            }

            @Override
            public void bind(@NonNull DayViewContainer viewContainer, @NonNull CalendarDay calendarDay) {
                viewContainer.binding.calendarDayText.setText(String.valueOf(calendarDay.getDate().getDayOfMonth()));
                if (calendarDay.getOwner() == DayOwner.THIS_MONTH) {
                    viewContainer.binding.calendarDayText.setTextColor(Color.BLACK);
                } else {
                    viewContainer.binding.calendarDayText.setTextColor(Color.LTGRAY);
                }

                viewContainer.binding.calendarDayText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getContext(), String.valueOf(calendarDay.getDate().getDayOfMonth()), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        binding.calendarView.setMonthScrollListener(new Function1<CalendarMonth, Unit>() {
            @Override
            public Unit invoke(CalendarMonth calendarMonth) {
                if ((calendarMonth.getYearMonth().compareTo(firstMonth) <= 3) || (calendarMonth.getYearMonth().compareTo(
                        lastMonth) >= -3)) {
                    firstMonth = firstMonth.minusMonths(10);
                    lastMonth = lastMonth.plusMonths(10);
                    binding.calendarView.updateMonthRangeAsync(firstMonth, lastMonth);
                }
                return null;
            }
        });

        binding.calendarView.scrollToMonth(currentMonth);
    }

    private void loadEvents(DateTime timeMin, DateTime timeMax, BackgroundCallback<List<Event>> callback) {
        MyApplication.EXECUTOR_SERVICE.execute(new Runnable() {
            @Override
            public void run() {
                final Calendar calendarService = calendarViewModel.getCalendarService();
                final List<Event> eventList = new ArrayList<>();
                String pageToken = null;

                final String[] calendarIds = new String[]{calendarViewModel.getMainCalendarId(), "primary"};

                try {
                    for (String calendarId : calendarIds) {
                        pageToken = null;
                        do {
                            Events events = calendarService.events().list(calendarId).setPageToken(pageToken).setTimeMin(timeMin)
                                    .setTimeMax(timeMax).execute();
                            eventList.addAll(events.getItems());

                            pageToken = events.getNextPageToken();
                        } while (pageToken != null);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                final String date = timeMin.toStringRfc3339();
                eventsMap.put(date, eventList);
                callback.onResultSuccessful(eventList);
            }
        });

    }

    // 일 보여주는 컨테이너 바인딩
    private class DayViewContainer extends ViewContainer {
        private CalendarDayLayoutBinding binding;

        public DayViewContainer(@NonNull View view) {
            super(view);
            binding = CalendarDayLayoutBinding.bind(view);

        }

    }

    // 달 보여주는 컨테이너 바인딩
    private class MonthHeaderViewContainer extends ViewContainer {
        private CalendarMonthHeaderLayoutBinding binding;

        public MonthHeaderViewContainer(@NonNull View view) {
            super(view);
            this.binding = CalendarMonthHeaderLayoutBinding.bind(view);
        }
    }


}