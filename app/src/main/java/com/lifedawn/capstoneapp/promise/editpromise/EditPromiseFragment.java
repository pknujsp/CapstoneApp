package com.lifedawn.capstoneapp.promise.editpromise;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
import com.lifedawn.capstoneapp.main.MyApplication;
import com.lifedawn.capstoneapp.model.firestore.PlaceDto;
import com.lifedawn.capstoneapp.map.SelectedLocationSimpleMapFragment;
import com.lifedawn.capstoneapp.map.adapters.LocationItemViewPagerAbstractAdapter;
import com.lifedawn.capstoneapp.promise.abstractfragment.AbstractPromiseFragment;
import com.lifedawn.capstoneapp.reminder.RemindersFragment;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.KakaoLocalDocument;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class EditPromiseFragment extends AbstractPromiseFragment {
	private Event originalEvent;
	private Event editEvent;
	private PlaceDto placeDto;
	private Calendar calendarService;
	private String eventId;
	private OnFragmentCallback<Boolean> onFragmentCallback;
	private Bundle bundle;

	private boolean initializing = true;

	private boolean editSummary;
	private boolean editDescription;
	private boolean editAttendees;
	private boolean editReminders;
	private boolean editLocation;

	public EditPromiseFragment setOnFragmentCallback(OnFragmentCallback<Boolean> onFragmentCallback) {
		this.onFragmentCallback = onFragmentCallback;
		return this;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		bundle = getArguments() != null ? getArguments() : savedInstanceState;
		eventId = bundle.getString("eventId");
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putAll(bundle);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.toolbar.fragmentTitle.setText(R.string.edit_promise);

		MyApplication.EXECUTOR_SERVICE.execute(new Runnable() {
			@Override
			public void run() {
				final Calendar calendarService = calendarViewModel.getCalendarService();

				try {
					originalEvent = calendarService.events().get("primary", eventId).execute();
				} catch (Exception e) {
					e.printStackTrace();
				}

				editEvent = originalEvent.clone();
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						init();
					}
				});
			}
		});

		binding.saveBtn.setText(R.string.update);
		binding.saveBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new MaterialAlertDialogBuilder(requireActivity())
						.setTitle(R.string.edit_promise)
						.setMessage(R.string.msg_edit_promise)
						.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								googleAccountLifeCycleObserver.setInstanceUserRecoverableAuthIntentActivityResultCallback(
										new ActivityResultCallback<ActivityResult>() {
											@Override
											public void onActivityResult(ActivityResult result) {
												calendarService = calendarViewModel.getCalendarService();
												updateEvent();
											}
										});
								calendarService = calendarViewModel.getCalendarService();
								updateEvent();
							}
						}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).create().show();


			}
		});

		binding.titleEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				if (!initializing) {
					editSummary = true;
				}
			}
		});

		binding.descriptionEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				if (!initializing) {
					editDescription = true;
				}
			}
		});
	}

	private void updateEvent() {
		if (editEvent.getReminders() != null) {
			editEvent.getReminders().setUseDefault(false);
		}
		if (editSummary) {
			editEvent.setSummary(binding.titleEditText.getText().toString());
		}
		if (editDescription) {
			editEvent.setDescription(binding.descriptionEditText.getText().toString());
		}

		calendarViewModel.updateEvent(calendarService, editEvent, new HttpCallback<Event>() {
			@Override
			public void onResponseSuccessful(Event result) {
				if (getActivity() != null) {
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							onFragmentCallback.onResult(true);
							getParentFragmentManager().popBackStack();
						}
					});
				}
			}

			@Override
			public void onResponseFailed(Exception e) {

			}
		});
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
		if (placeDto != null) {
			String message = placeDto.getLocationType() == Constant.ADDRESS ? placeDto.getAddressName() : placeDto.getPlaceName() + getString(
					R.string.message_change_location);

			new MaterialAlertDialogBuilder(getActivity()).setTitle(
					R.string.title_asking_to_change_location).setMessage(message).setPositiveButton(R.string.ok,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							showMap(new LocationItemViewPagerAbstractAdapter.OnClickedLocationBtnListener() {
								@Override
								public void onSelected(KakaoLocalDocument kakaoLocalDocument, boolean remove) {
									placeDto = PlaceDto.toLocationDto(kakaoLocalDocument);
									editEvent.setLocation(placeDto.toString());
									onSelectedLocation(placeDto);
									editLocation = true;
								}
							});
							editLocation = true;
							mapFragment.replaceLocation(null);
							binding.placeName.setText(R.string.no_promise_location);
							placeDto = null;
							editEvent.setLocation(null);
							dialogInterface.dismiss();
						}
					}).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					dialogInterface.dismiss();
				}
			}).create().show();
		} else {
			showMap(new LocationItemViewPagerAbstractAdapter.OnClickedLocationBtnListener() {
				@Override
				public void onSelected(KakaoLocalDocument kakaoLocalDocument, boolean remove) {
					placeDto = PlaceDto.toLocationDto(kakaoLocalDocument);
					editEvent.setLocation(placeDto.toString());
					onSelectedLocation(placeDto);
				}
			});
		}

	}

	@Override
	protected void onClickedInviteFriendChip() {
		InvitationFriendFragment invitationFriendFragment = new InvitationFriendFragment();
		invitationFriendFragment.setOnFragmentCallback(new OnFragmentCallback<ArrayList<EventAttendee>>() {
			@Override
			public void onResult(ArrayList<EventAttendee> e) {
				//삭제,추가된 사람만 변경, 기존 사람은 그대로 데이터 유지
				if (e.isEmpty()) {
					editEvent.setAttendees(null);
				} else {
					if (editEvent.getAttendees() != null) {
						ArrayList<EventAttendee> lastList = (ArrayList<EventAttendee>) editEvent.getAttendees();

						for (int i = 0; i < e.size(); i++) {
							for (int j = 0; j < lastList.size(); j++) {
								if (e.get(i).getEmail().equals(lastList.get(j).getEmail())) {
									e.remove(j);
									e.add(lastList.get(j));
									break;
								}
							}
						}
					}
					editEvent.setAttendees(e);
				}
				editAttendees = true;
				initAttendeesView(editEvent.getAttendees());
			}
		});

		ArrayList<EventAttendee> eventAttendees = null;
		if (editEvent.getAttendees() != null) {
			eventAttendees = (ArrayList<EventAttendee>) editEvent.getAttendees();
		} else {
			eventAttendees = new ArrayList<>();
		}

		invitationFriendFragment.setEventAttendees(eventAttendees);

		getParentFragmentManager().beginTransaction().hide(EditPromiseFragment.this).add(R.id.fragmentContainerView,
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
				if (editEvent.getReminders() == null || editEvent.getReminders().getOverrides() == null) {
					Event.Reminders reminders = new Event.Reminders();
					reminders.setOverrides(new ArrayList<>());
					editEvent.setReminders(reminders);
				}

				if (!isDuplicate(editEvent.getReminders().getOverrides(), reminder)) {
					editReminders = true;
					editEvent.getReminders().getOverrides().add(reminder);
					initRemindersView(editEvent.getReminders().getOverrides());
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

		getParentFragmentManager().beginTransaction().hide(EditPromiseFragment.this).add(R.id.fragmentContainerView, remindersFragment,
				RemindersFragment.class.getName()).addToBackStack(RemindersFragment.class.getName()).commit();
	}

	@Override
	protected void onClickedFriendChip(EventAttendee eventAttendee, int index, boolean remove) {
		if (remove) {
			editEvent.getAttendees().remove(index - 1);
			initAttendeesView(editEvent.getAttendees());
			if (editEvent.getAttendees().isEmpty()) {
				editEvent.setAttendees(null);
			}
		} else {
			//친구 정보 다이얼로그로 표시
		}
	}

	@Override
	protected void onClickedReminderChip(EventReminder eventReminder, int index, boolean remove) {
		if (remove) {
			editEvent.getReminders().getOverrides().remove(index - 1);
			initRemindersView(editEvent.getReminders().getOverrides());
			if (editEvent.getReminders().getOverrides().isEmpty()) {
				editEvent.setReminders(null);
			}
		} else {
			Bundle bundle = new Bundle();
			bundle.putInt("previousMinutes", eventReminder.getMinutes());
			bundle.putSerializable("requestType", RemindersFragment.RequestType.EDIT);

			RemindersFragment eventReminderFragment = new RemindersFragment();
			eventReminderFragment.setOnEventReminderResultListener(new RemindersFragment.OnEventReminderResultListener() {
				@Override
				public void onResultModifiedReminder(EventReminder reminder, int previousMinutes) {
					editEvent.getReminders().getOverrides().add(reminder);
					initRemindersView(editEvent.getReminders().getOverrides());
					if (editEvent.getReminders().getOverrides().isEmpty()) {
						editEvent.setReminders(null);
					}
				}

				@Override
				public void onResultAddedReminder(EventReminder reminder) {

				}

				@Override
				public void onResultRemovedReminder(int previousMinutes) {
					List<EventReminder> reminders = editEvent.getReminders().getOverrides();
					for (int i = 0; i < reminders.size(); i++) {
						if (reminders.get(i).getMinutes() == previousMinutes) {
							reminders.remove(i);
							break;
						}
					}

					initRemindersView(editEvent.getReminders().getOverrides());
					if (editEvent.getReminders().getOverrides().isEmpty()) {
						editEvent.setReminders(null);
					}
				}
			});
			eventReminderFragment.setArguments(bundle);
			getParentFragmentManager().beginTransaction().hide(EditPromiseFragment.this).add(R.id.fragmentContainerView,
					eventReminderFragment, RemindersFragment.class.getName()).addToBackStack(RemindersFragment.class.getName()).commit();
		}
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

		mapFragment = new SelectedLocationSimpleMapFragment();
		Bundle bundle = new Bundle();

		//장소
		final String locationValue = originalEvent.getLocation();
		if (locationValue != null) {
			placeDto = PlaceDto.toLocationDto(locationValue);
			bundle.putSerializable("locationDto", placeDto);

			binding.placeName.setText(placeDto.getLocationType() == Constant.PLACE ? placeDto.getPlaceName() : placeDto.getAddressName());
			binding.naverMap.setVisibility(View.VISIBLE);
		}
		//알림
		Event.Reminders reminders = originalEvent.getReminders();
		if (reminders != null) {
			initRemindersView(reminders.getOverrides());
		}

		binding.account.setText(originalEvent.getOrganizer().getDisplayName());

		mapFragment.setArguments(bundle);
		getChildFragmentManager().beginTransaction().add(binding.naverMap.getId(), mapFragment).commitAllowingStateLoss();

		initializing = false;
		binding.progressLayout.onSuccessful();
	}

}
