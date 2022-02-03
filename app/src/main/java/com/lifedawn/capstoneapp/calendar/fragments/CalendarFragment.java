package com.lifedawn.capstoneapp.calendar.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.kizitonwose.calendarview.model.CalendarDay;
import com.kizitonwose.calendarview.ui.DayBinder;
import com.kizitonwose.calendarview.ui.ViewContainer;
import com.lifedawn.capstoneapp.databinding.CalendarDayLayoutBinding;
import com.lifedawn.capstoneapp.databinding.FragmentCalendarBinding;

import org.jetbrains.annotations.NotNull;

import java.time.DayOfWeek;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.WeekFields;
import java.util.Locale;

public class CalendarFragment extends Fragment {
	private FragmentCalendarBinding binding;
	
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
		
		binding.calendarView.setDayBinder(new DayBinder<DayViewContainer>() {
			@NonNull
			@Override
			public DayViewContainer create(@NonNull View view) {
				return new DayViewContainer(view);
			}
			
			@Override
			public void bind(@NonNull DayViewContainer viewContainer, @NonNull CalendarDay calendarDay) {
				viewContainer.setText(String.valueOf(calendarDay.getDate().getDayOfMonth()));
			}
		});
		
		/*
		val currentMonth = YearMonth.now()
		val firstMonth = currentMonth.minusMonths(10)
		val lastMonth = currentMonth.plusMonths(10)
		val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
		calendarView.setup(firstMonth, lastMonth, firstDayOfWeek)
		calendarView.scrollToMonth(currentMonth)
		 */
		
		final YearMonth currentMonth = YearMonth.now(ZoneId.systemDefault());
		final YearMonth firstMonth = currentMonth.minusMonths(10);
		final YearMonth lastMonth = currentMonth.plusMonths(10);
		final DayOfWeek firstDayOfWeek = WeekFields.of(Locale.getDefault()).getFirstDayOfWeek();
		
		binding.calendarView.setup(firstMonth, lastMonth, firstDayOfWeek);
		binding.calendarView.scrollToMonth(currentMonth);
	}
	
	private class DayViewContainer extends ViewContainer {
		private CalendarDayLayoutBinding binding;
		
		public DayViewContainer(@NonNull View view) {
			super(view);
			binding = CalendarDayLayoutBinding.bind(view);
		}
		
		public void setText(String text) {
			binding.calendarDayText.setText(text);
		}
		
	}
}