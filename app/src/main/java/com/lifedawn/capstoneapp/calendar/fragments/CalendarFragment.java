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

import com.kizitonwose.calendarview.model.CalendarDay;
import com.kizitonwose.calendarview.model.CalendarMonth;
import com.kizitonwose.calendarview.model.DayOwner;
import com.kizitonwose.calendarview.ui.DayBinder;
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder;
import com.kizitonwose.calendarview.ui.ViewContainer;
import com.lifedawn.capstoneapp.databinding.CalendarDayLayoutBinding;
import com.lifedawn.capstoneapp.databinding.CalendarMonthHeaderLayoutBinding;
import com.lifedawn.capstoneapp.databinding.FragmentCalendarBinding;

import org.jetbrains.annotations.NotNull;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.Locale;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class CalendarFragment extends Fragment {
	private FragmentCalendarBinding binding;
	private final DayOfWeek FIRST_DAY_OF_WEEK = WeekFields.of(Locale.getDefault()).getFirstDayOfWeek();

	YearMonth currentMonth = YearMonth.now(ZoneId.systemDefault());
	YearMonth firstMonth = currentMonth.minusMonths(10);
	YearMonth lastMonth = currentMonth.plusMonths(10);
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
			
			@NonNull
			@Override
			public MonthHeaderViewContainer create(@NonNull View view) {
				return new MonthHeaderViewContainer(view);
			}
			
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
					viewContainer.binding.promiseCount.setTextColor(Color.BLUE);
				} else {
					viewContainer.binding.calendarDayText.setTextColor(Color.LTGRAY);
					viewContainer.binding.promiseCount.setTextColor(Color.BLUE);
				}


				
				viewContainer.binding.calendarDayText.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Toast.makeText(getContext(), String.valueOf(calendarDay.getDate().getDayOfMonth()), Toast.LENGTH_SHORT).show();
						//Toast.makeText(getContext(), String.valueOf(promiseCount()), Toast.LENGTH_SHORT).show();

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
	
	private class DayViewContainer extends ViewContainer {
		private CalendarDayLayoutBinding binding;
		
		public DayViewContainer(@NonNull View view) {
			super(view);
			binding = CalendarDayLayoutBinding.bind(view);

		}
		
	}
	
	private class MonthHeaderViewContainer extends ViewContainer {
		private CalendarMonthHeaderLayoutBinding binding;
		
		public MonthHeaderViewContainer(@NonNull View view) {
			super(view);
			this.binding = CalendarMonthHeaderLayoutBinding.bind(view);
		}
	}
	

}