package com.lifedawn.capstoneapp.promise.editpromise;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.lifedawn.capstoneapp.promise.abstractfragment.AbstractPromiseFragment;

import java.time.ZonedDateTime;
import java.util.List;

public class EditPromiseFragment extends AbstractPromiseFragment {
    private Event editEvent;

    public void setEditEvent(Event editEvent) {
        this.editEvent = editEvent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Bundle bundle = getArguments();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init();
    }

    private void init() {
        binding.titleEditText.setText(editEvent.getSummary());
        ZonedDateTime startDateTime = ZonedDateTime.parse(editEvent.getStart().toString());

        binding.dateTime.setText(startDateTime.format(START_DATETIME_FORMATTER));
        binding.descriptionEditText.setText(editEvent.getDescription());

        //초대받은 사람들
        initAttendeesView(editEvent.getAttendees());

        //장소
        final String location = editEvent.getLocation();

        //알림
        Event.Reminders reminders = editEvent.getReminders();
        if (reminders != null) {
            initRemindersView(reminders.getOverrides());
        }

        //계정 로컬인지, 구글 계정인지
        
    }


}
