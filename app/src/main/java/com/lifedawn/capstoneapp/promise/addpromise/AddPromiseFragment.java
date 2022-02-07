package com.lifedawn.capstoneapp.promise.addpromise;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventReminder;
import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.common.interfaces.OnFragmentCallback;
import com.lifedawn.capstoneapp.friends.invitation.InvitationFriendFragment;
import com.lifedawn.capstoneapp.map.PromiseNaverMapFragment;
import com.lifedawn.capstoneapp.promise.abstractfragment.AbstractPromiseFragment;
import com.lifedawn.capstoneapp.reminder.RemindersFragment;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;

public class AddPromiseFragment extends AbstractPromiseFragment {
	private ArrayList<EventAttendee> newEvenAttendeeList = new ArrayList<>();
	private ArrayList<EventReminder> newRemindersList = new ArrayList<>();
	private LocalDate promiseDate = LocalDate.now();
	private LocalTime promiseTime = LocalTime.now();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		ZonedDateTime now = ZonedDateTime.now();
		
		binding.date.setText(now.format(START_DATE_FORMATTER));
		binding.time.setText(now.format(START_TIME_FORMATTER));
		setAccount(accountViewModel.getUsingAccountType(), accountViewModel.getConnectedGoogleAccount());
		
		binding.saveBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//값이 제대로 입력되었는지 확인후 저장
				
			}
		});
	}
	
	@Override
	protected void onClickedInviteFriendChip() {
		InvitationFriendFragment invitationFriendFragment = new InvitationFriendFragment();
		invitationFriendFragment.setOnFragmentCallback(new OnFragmentCallback<ArrayList<EventAttendee>>() {
			@Override
			public void onResult(ArrayList<EventAttendee> e) {
				newEvenAttendeeList.clear();
				newEvenAttendeeList.addAll(e);
				
				initAttendeesView(newEvenAttendeeList);
			}
		});
		
		invitationFriendFragment.setEventAttendees(newEvenAttendeeList);
		
		getParentFragmentManager().beginTransaction().hide(AddPromiseFragment.this).add(R.id.fragmentContainerView,
				invitationFriendFragment, InvitationFriendFragment.class.getName()).addToBackStack(
				InvitationFriendFragment.class.getName()).commit();
	}
	
	@Override
	protected void onClickedAddReminderChip() {
		RemindersFragment remindersFragment = new RemindersFragment();
		remindersFragment.setOnEventReminderResultListener(new RemindersFragment.OnEventReminderResultListener() {
			@Override
			public void onResultModifiedReminder(EventReminder reminder, int previousMinutes) {
			
			}
			
			@Override
			public void onResultAddedReminder(EventReminder reminder) {
				//중복검사
				if (!isDuplicate(newRemindersList, reminder)) {
					newRemindersList.add(reminder);
					initRemindersView(newRemindersList);
				} else {
					Toast.makeText(getContext(), R.string.existing_Value, Toast.LENGTH_SHORT).show();
				}
			}
			
			@Override
			public void onResultRemovedReminder(int previousMinutes) {
			
			}
		});
		Bundle bundle = new Bundle();
		bundle.putSerializable("requestType", RemindersFragment.RequestType.ADD);
		remindersFragment.setArguments(bundle);
		
		getParentFragmentManager().beginTransaction().hide(AddPromiseFragment.this).add(R.id.fragmentContainerView, remindersFragment,
				RemindersFragment.class.getName()).addToBackStack(RemindersFragment.class.getName()).commit();
	}
	
	@Override
	protected void onClickedFriendChip(EventAttendee eventAttendee, int index, boolean remove) {
		if (remove) {
			newEvenAttendeeList.remove(index - 1);
			initAttendeesView(newEvenAttendeeList);
		} else {
			//친구 정보 다이얼로그로 표시
		}
	}
	
	@Override
	protected void onClickedReminderChip(EventReminder eventReminder, int index, boolean remove) {
		if (remove) {
			newRemindersList.remove(index - 1);
			initRemindersView(newRemindersList);
		}
	}
	
	@Override
	protected void onResultTime(LocalTime time) {
		this.promiseTime = time;
		
	}
	
	@Override
	protected void onResultDate(LocalDate date) {
		this.promiseDate = date;
	}
	
	@Override
	protected LocalDate onClickedDate() {
		return promiseDate;
	}
	
	@Override
	protected LocalTime onClickedTime() {
		return promiseTime;
	}
	
	@Override
	protected void onClickedAccount() {
	
	}
	
	@Override
	protected void onClickedMap() {
		PromiseNaverMapFragment promiseNaverMapFragment = new PromiseNaverMapFragment();
		getParentFragmentManager().beginTransaction().hide(AddPromiseFragment.this).add(R.id.fragmentContainerView,
				promiseNaverMapFragment, PromiseNaverMapFragment.class.getName()).addToBackStack(
				PromiseNaverMapFragment.class.getName()).commit();
	}
	
}
