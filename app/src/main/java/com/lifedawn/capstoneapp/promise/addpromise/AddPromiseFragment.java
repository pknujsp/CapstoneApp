package com.lifedawn.capstoneapp.promise.addpromise;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.common.constants.Constant;
import com.lifedawn.capstoneapp.common.interfaces.HttpCallback;
import com.lifedawn.capstoneapp.common.interfaces.OnFragmentCallback;
import com.lifedawn.capstoneapp.friends.invitation.InvitationFriendFragment;
import com.lifedawn.capstoneapp.map.LocationDto;
import com.lifedawn.capstoneapp.map.adapters.LocationItemViewPagerAbstractAdapter;
import com.lifedawn.capstoneapp.promise.abstractfragment.AbstractPromiseFragment;
import com.lifedawn.capstoneapp.reminder.RemindersFragment;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.KakaoLocalDocument;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.TimeZone;

public class AddPromiseFragment extends AbstractPromiseFragment {
	private ArrayList<EventAttendee> newEventAttendeeList = new ArrayList<>();
	private ArrayList<EventReminder> newRemindersList = new ArrayList<>();
	private LocalDate promiseDate = LocalDate.now();
	private LocalTime promiseTime = LocalTime.now();
	private Calendar calendarService;
	private LocationDto locationDto;

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
		binding.toolbar.fragmentTitle.setText(R.string.new_promise);

		ZonedDateTime now = ZonedDateTime.now();

		binding.date.setText(now.format(START_DATE_FORMATTER));
		binding.date.setTag(now.toLocalDate());
		binding.time.setText(now.format(START_TIME_FORMATTER));
		binding.time.setTag(now.toLocalTime());
		setAccount(accountViewModel.getUsingAccountType(), accountViewModel.getLastSignInAccount());

		binding.saveBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//값이 제대로 입력되었는지 확인후 저장
				Event event = new Event();
				event.setSummary(binding.titleEditText.getText().toString()).setDescription(
						binding.descriptionEditText.getText().toString());

				if (locationDto != null) {
					event.setLocation(locationDto.toString());
				}

				final String timeZoneId = TimeZone.getDefault().getID();

				final DateTime dateTime = getStartDateTime();
				EventDateTime start = new EventDateTime();
				EventDateTime end = new EventDateTime();
				start.setDateTime(dateTime).setTimeZone(timeZoneId);
				end.setDateTime(dateTime).setTimeZone(timeZoneId);

				event.setStart(start).setEnd(end);

				//알림
				if (newRemindersList.size() > 0) {
					Event.Reminders reminders = new Event.Reminders();
					reminders.setOverrides(newRemindersList).setUseDefault(false);
					event.setReminders(reminders);
				}

				//참석자
				if (newEventAttendeeList.size() > 0) {
					event.setAttendees(newEventAttendeeList);
				}


				//캘린더ID 지정
				googleAccountLifeCycleObserver.setInstanceUserRecoverableAuthIntentActivityResultCallback(
						new ActivityResultCallback<ActivityResult>() {
							@Override
							public void onActivityResult(ActivityResult result) {
								calendarService = calendarViewModel.getCalendarService();
								saveNewEvent(event);
							}
						});
				calendarService = calendarViewModel.getCalendarService();
				saveNewEvent(event);
			}
		});
	}

	private void saveNewEvent(Event event) {
		calendarViewModel.saveEvent(calendarService, event, new HttpCallback<Event>() {
			@Override
			public void onResponseSuccessful(Event result) {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (result == null) {
							Toast.makeText(getContext(), R.string.failed_add_new_promise, Toast.LENGTH_SHORT).show();
						} else {
							getParentFragmentManager().popBackStack();
						}
					}
				});
			}

			@Override
			public void onResponseFailed(Exception e) {

			}
		});
	}

	@Override
	protected void onClickedInviteFriendChip() {
		InvitationFriendFragment invitationFriendFragment = new InvitationFriendFragment();
		invitationFriendFragment.setOnFragmentCallback(new OnFragmentCallback<ArrayList<EventAttendee>>() {
			@Override
			public void onResult(ArrayList<EventAttendee> e) {
				newEventAttendeeList = e;
				initAttendeesView(newEventAttendeeList);
			}
		});

		invitationFriendFragment.setEventAttendees(newEventAttendeeList);

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
			newEventAttendeeList.remove(index - 1);
			initAttendeesView(newEventAttendeeList);
		} else {
			//친구 정보 다이얼로그로 표시
		}
	}

	@Override
	protected void onClickedReminderChip(EventReminder eventReminder, int index, boolean remove) {
		if (remove) {
			newRemindersList.remove(index - 1);
			initRemindersView(newRemindersList);
		} else {
			Bundle bundle = new Bundle();
			bundle.putInt("previousMinutes", eventReminder.getMinutes());
			bundle.putSerializable("requestCode", RemindersFragment.RequestType.EDIT);

			RemindersFragment eventReminderFragment = new RemindersFragment();
			eventReminderFragment.setOnEventReminderResultListener(new RemindersFragment.OnEventReminderResultListener() {
				@Override
				public void onResultModifiedReminder(EventReminder reminder, int previousMinutes) {
					newRemindersList.add(reminder);
					initRemindersView(newRemindersList);
				}

				@Override
				public void onResultAddedReminder(EventReminder reminder) {

				}

				@Override
				public void onResultRemovedReminder(int previousMinutes) {
					for (int i = 0; i < newRemindersList.size(); i++) {
						if (newRemindersList.get(i).getMinutes() == previousMinutes) {
							newRemindersList.remove(i);
							break;
						}
					}
					initRemindersView(newRemindersList);
				}
			});
			eventReminderFragment.setArguments(bundle);
			getParentFragmentManager().beginTransaction().hide(AddPromiseFragment.this).add(R.id.fragmentContainerView,
					eventReminderFragment, RemindersFragment.class.getName()).addToBackStack(RemindersFragment.class.getName()).commit();
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
									onSelectedLocation(locationDto);
								}
							});
							locationDto = null;
							mapFragment.replaceLocation(null);
							binding.placeName.setText(R.string.no_promise_location);
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
	protected void onSelectedLocation(LocationDto locationDto) {
		super.onSelectedLocation(locationDto);
	}
}
