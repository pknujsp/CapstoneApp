package com.lifedawn.capstoneapp.promise.promiseinfo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.chip.Chip;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.common.constants.Constant;
import com.lifedawn.capstoneapp.common.util.ReminderUtil;
import com.lifedawn.capstoneapp.databinding.FragmentPromiseInfoBinding;
import com.lifedawn.capstoneapp.map.LocationDto;
import com.lifedawn.capstoneapp.map.SelectedLocationSimpleMapFragment;
import com.lifedawn.capstoneapp.promise.abstractfragment.AbstractPromiseFragment;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class PromiseInfoFragment extends Fragment {
	private FragmentPromiseInfoBinding binding;
	private Event originalEvent;
	private LocationDto locationDto;
	private SelectedLocationSimpleMapFragment mapFragment;

	protected final DateTimeFormatter START_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy M/d E a h:mm");


	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			Bundle bundle = getArguments();
			originalEvent = new Event();
			HashMap<String, Object> map = (HashMap<String, Object>) bundle.getSerializable("map");

			Set<String> keySet = map.keySet();
			for (String key : keySet) {
				originalEvent.set(key, map.get(key));
			}

			if (originalEvent.getLocation() != null) {
				locationDto = LocationDto.toLocationDto(originalEvent.getLocation());
			}
		}

	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		binding = FragmentPromiseInfoBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mapFragment = new SelectedLocationSimpleMapFragment();
		getChildFragmentManager().beginTransaction().add(binding.naverMap.getId(), mapFragment).commit();

		binding.title.setText(originalEvent.getSummary());

		EventDateTime eventDateTime = originalEvent.getStart();
		ZonedDateTime start = ZonedDateTime.parse(eventDateTime.getDateTime().toString());
		start = start.withZoneSameInstant(ZoneId.of(eventDateTime.getTimeZone()));

		binding.dateTime.setText(start.format(START_DATETIME_FORMATTER));
		binding.description.setText(originalEvent.getDescription());

		//초대받은 사람들
		initAttendeesView(originalEvent.getAttendees());

		//장소
		if (locationDto != null) {
			mapFragment.replaceLocation(locationDto);

			if (locationDto != null) {
				binding.placeName.setText(locationDto.getLocationType() == Constant.PLACE ? locationDto.getPlaceName() : locationDto.getAddressName());
				binding.naverMap.setVisibility(View.VISIBLE);
			}
		} else {
			binding.placeName.setText(R.string.no_promise_location);
			binding.naverMap.setVisibility(View.GONE);
		}
		//알림
		Event.Reminders reminders = originalEvent.getReminders();
		if (reminders != null) {
			initRemindersView(reminders.getOverrides());
		}

	}

	protected void initAttendeesView(List<EventAttendee> attendeeList) {
		if (attendeeList != null) {
			for (EventAttendee eventAttendee : attendeeList) {
				Chip chip = (Chip) getLayoutInflater().inflate(R.layout.event_attendee_chip, null);
				chip.setText(eventAttendee.getDisplayName());
				chip.setCloseIconVisible(false);
				binding.attendeeChipGroup.addView(chip);
			}
		}
	}

	protected void initRemindersView(List<EventReminder> eventReminderList) {
		if (eventReminderList != null) {
			int index = 1;

			for (EventReminder eventReminder : eventReminderList) {
				Chip chip = (Chip) getLayoutInflater().inflate(R.layout.event_reminder_chip, null);
				chip.setText(ReminderUtil.makeReminderText(ReminderUtil.make(eventReminder.getMinutes()), getContext()));
				int finalIndex = index;
				chip.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
					}
				});
				chip.setOnCloseIconClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
					}
				});

				index++;
				binding.reminderChipGroup.addView(chip);
			}
		}
	}
}
