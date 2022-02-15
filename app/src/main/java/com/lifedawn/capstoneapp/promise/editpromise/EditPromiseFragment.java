package com.lifedawn.capstoneapp.promise.editpromise;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.common.constants.Constant;
import com.lifedawn.capstoneapp.map.LocationDto;
import com.lifedawn.capstoneapp.map.adapters.LocationItemViewPagerAbstractAdapter;
import com.lifedawn.capstoneapp.promise.abstractfragment.AbstractPromiseFragment;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.KakaoLocalDocument;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Set;
import java.util.TimeZone;

public class EditPromiseFragment extends AbstractPromiseFragment {
	private Event originalEvent;
	private Event editEvent;
	private LocationDto locationDto;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			Bundle bundle = getArguments();
			originalEvent = new Event();
			HashMap<String, Object> map = (HashMap<String, Object>) bundle.getSerializable("map");
			
			Set<String> keySet = map.keySet();
			for (String key : keySet) {
				originalEvent.set(key, map.get(key));
			}
			
			editEvent = originalEvent.clone();
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
	
	@Override
	protected void onResultDate(LocalDate date) {
		final DateTime dateTime = getStartDateTime();
		EventDateTime start = new EventDateTime();
		EventDateTime end = new EventDateTime();
		start.setDateTime(dateTime).setTimeZone(TimeZone.getDefault().getID());
		end.setDateTime(dateTime).setTimeZone(TimeZone.getDefault().getID());
		
		editEvent.setStart(start).setEnd(end);
	}
	
	@Override
	protected void onResultTime(LocalTime time) {
		final DateTime dateTime = getStartDateTime();
		EventDateTime start = new EventDateTime();
		EventDateTime end = new EventDateTime();
		start.setDateTime(dateTime).setTimeZone(TimeZone.getDefault().getID());
		end.setDateTime(dateTime).setTimeZone(TimeZone.getDefault().getID());
		
		editEvent.setStart(start).setEnd(end);
	}
	
	@Override
	protected LocalDate onClickedDate() {
		return (LocalDate) binding.date.getTag();
	}
	
	@Override
	protected LocalTime onClickedTime() {
		return (LocalTime) binding.time.getTag();
	}
	
	@Override
	protected void onClickedAccount() {
	
	
	}
	
	@Override
	protected void onClickedMap() {
		if (locationDto != null) {
			String message = locationDto.getLocationType() == Constant.ADDRESS ? locationDto.getAddressName() : locationDto.getPlaceName() + getString(
					R.string.message_change_location);
			
			AlertDialog dialog = new MaterialAlertDialogBuilder(getActivity()).setTitle(
					R.string.title_asking_to_change_location).setMessage(message).setPositiveButton(R.string.ok,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							showMap(new LocationItemViewPagerAbstractAdapter.OnClickedLocationBtnListener() {
								@Override
								public void onSelected(KakaoLocalDocument kakaoLocalDocument, boolean remove) {
									locationDto = LocationDto.toLocationDto(kakaoLocalDocument);
									editEvent.setLocation(locationDto.toString());
									onSelectedLocation(locationDto);
								}
							});
							binding.placeName.setText(R.string.placeName);
							binding.naverMap.setVisibility(View.GONE);
							locationDto = null;
							editEvent.setLocation(null);
							dialogInterface.dismiss();
						}
					}).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					dialogInterface.dismiss();
				}
			}).create();
			
			dialog.show();
		} else {
			showMap(new LocationItemViewPagerAbstractAdapter.OnClickedLocationBtnListener() {
				@Override
				public void onSelected(KakaoLocalDocument kakaoLocalDocument, boolean remove) {
					locationDto = LocationDto.toLocationDto(kakaoLocalDocument);
					onSelectedLocation(locationDto);
				}
			});
		}
	}
	
	@Override
	protected void onClickedInviteFriendChip() {
		binding.invite.getText();
		
	}
	
	@Override
	protected void onClickedAddReminderChip() {
		binding.addReminderChip.getText();
		
	}
	
	@Override
	protected void onClickedFriendChip(EventAttendee eventAttendee, int index, boolean remove) {
		eventAttendee.getEmail();
	}
	
	@Override
	protected void onClickedReminderChip(EventReminder eventReminder, int index, boolean remove) {
		eventReminder.getMinutes();
	}
	
	private void init() {
		binding.titleEditText.setText(originalEvent.getSummary());
		EventDateTime eventDateTime = originalEvent.getStart();
		ZonedDateTime start = ZonedDateTime.parse(eventDateTime.getDateTime().toString());
		start = start.withZoneSameInstant(ZoneId.of(eventDateTime.getTimeZone()));
		
		binding.date.setText(start.format(START_DATE_FORMATTER));
		binding.time.setText(start.format(START_TIME_FORMATTER));
		binding.date.setTag(start.toLocalDate());
		binding.time.setTag(start.toLocalTime());
		binding.descriptionEditText.setText(originalEvent.getDescription());
		
		binding.titleEditText.setText(originalEvent.getSummary());
		binding.descriptionEditText.setText(originalEvent.getDescription());
		
		//초대받은 사람들
		initAttendeesView(originalEvent.getAttendees());
		
		//장소
		final String location = originalEvent.getLocation();
		if (location != null) {
			locationDto = LocationDto.toLocationDto(location);
			mapFragment.replaceLocation(locationDto);
			
			if (locationDto != null) {
				binding.naverMap.setVisibility(View.VISIBLE);
			}
		}
		//알림
		Event.Reminders reminders = originalEvent.getReminders();
		if (reminders != null) {
			initRemindersView(reminders.getOverrides());
		}
		
		binding.account.setText(originalEvent.getOrganizer().getEmail());
		
	}
	
}
