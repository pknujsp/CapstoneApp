package com.lifedawn.capstoneapp.promise.promiseinfo;

import android.content.ContentValues;
import android.os.Bundle;
import android.provider.CalendarContract;
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
import com.lifedawn.capstoneapp.common.interfaces.BackgroundCallback;
import com.lifedawn.capstoneapp.common.repository.CalendarRepository;
import com.lifedawn.capstoneapp.common.repositoryinterface.ICalendarRepository;
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
    private ContentValues originalEvent;
    private LocationDto locationDto;
    private SelectedLocationSimpleMapFragment mapFragment;

    private final DateTimeFormatter START_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy M/d E a h:mm");


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Bundle bundle = getArguments();
            originalEvent = bundle.getParcelable("event");

            if (originalEvent.getAsString(CalendarContract.Events.EVENT_LOCATION) != null) {
                locationDto = LocationDto.toLocationDto(originalEvent.getAsString(CalendarContract.Events.EVENT_LOCATION));
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
        binding.toolbar.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().popBackStack();
            }
        });
        binding.toolbar.fragmentTitle.setText(R.string.promise_info);
        mapFragment = new SelectedLocationSimpleMapFragment();
        getChildFragmentManager().beginTransaction().add(binding.naverMap.getId(), mapFragment).commit();

        binding.title.setText(originalEvent.getAsString(CalendarContract.Events.TITLE));

        Long eventDateTime = originalEvent.getAsLong(CalendarContract.Events.CALENDAR_TIME_ZONE);
        ZonedDateTime start = ZonedDateTime.parse(eventDateTime.toString());
        start = start.withZoneSameInstant(ZoneId.of(eventDateTime.toString()));

        binding.dateTime.setText(start.format(START_DATETIME_FORMATTER));
        binding.description.setText(originalEvent.getAsString(CalendarContract.Events.DESCRIPTION));

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

        //attendeelist 불러오기
        CalendarRepository.loadAttendees(getContext(), originalEvent.getAsLong(CalendarContract.Events._ID), new BackgroundCallback<List<ContentValues>>() {
            @Override
            public void onResultSuccessful(List<ContentValues> attendeeList) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initAttendeesView(attendeeList);
                    }
                });
            }

            @Override
            public void onResultFailed(Exception e) {

            }
        });

        //reminderlist 불러오기
        CalendarRepository.loadReminders(getContext(), originalEvent.getAsLong(CalendarContract.Events._ID), new BackgroundCallback<List<ContentValues>>() {
            @Override
            public void onResultSuccessful(List<ContentValues> e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initRemindersView(e);
                    }
                });
            }

            @Override
            public void onResultFailed(Exception e) {

            }
        });
    }

    protected void initAttendeesView(List<ContentValues> attendeeList) {
        if (attendeeList != null) {
            for (ContentValues eventAttendee : attendeeList) {
                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.event_attendee_chip, null);
                chip.setText(eventAttendee.getAsString(CalendarContract.Attendees.ATTENDEE_EMAIL));
                chip.setCloseIconVisible(false);
                binding.attendeeChipGroup.addView(chip);
            }
        }
    }

    protected void initRemindersView(List<ContentValues> eventReminderList) {
        if (eventReminderList != null) {
            for (ContentValues eventReminder : eventReminderList) {
                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.event_reminder_chip, null);
                chip.setText(ReminderUtil.makeReminderText(ReminderUtil.make(eventReminder.getAsInteger(CalendarContract.Reminders.MINUTES)), getContext()));
                chip.setCloseIconVisible(false);
                binding.reminderChipGroup.addView(chip);
            }
        }
    }
}
