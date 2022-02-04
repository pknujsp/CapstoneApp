package com.lifedawn.capstoneapp.promise.abstractfragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.chip.Chip;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventReminder;
import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.common.ReminderUtil;
import com.lifedawn.capstoneapp.databinding.FragmentEditPromiseBinding;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.List;


public abstract class AbstractPromiseFragment extends Fragment {
    protected FragmentEditPromiseBinding binding;
    protected EditType editType;
    protected final DateTimeFormatter START_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy M/d E a h:mm");

    public enum EditType implements Serializable {
        ADD, EDIT
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Bundle bundle = getArguments();
            editType = (EditType) bundle.getSerializable("editType");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEditPromiseBinding.inflate(inflater);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    protected void initAttendeesView(List<EventAttendee> attendeeList) {
        if (attendeeList != null) {
            binding.attendeeChipGroup.removeAllViews();
            for (EventAttendee eventAttendee : attendeeList) {
                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.event_attendee_chip, null);
                chip.setText(eventAttendee.getEmail());

                binding.attendeeChipGroup.addView(chip);
            }
        }
    }

    protected void initRemindersView(List<EventReminder> eventReminderList) {
        if (eventReminderList != null) {
            binding.reminderChipGroup.removeAllViews();
            for (EventReminder eventReminder : eventReminderList) {
                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.event_reminder_chip, null);
                chip.setText(ReminderUtil.makeReminderText(ReminderUtil.make(eventReminder.getMinutes()), getContext()));

                binding.reminderChipGroup.addView(chip);
            }
        }
    }
}