package com.lifedawn.capstoneapp.promise.abstractfragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.chip.Chip;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.account.GoogleAccountLifeCycleObserver;
import com.lifedawn.capstoneapp.common.constants.Constant;
import com.lifedawn.capstoneapp.common.util.ReminderUtil;
import com.lifedawn.capstoneapp.common.viewmodel.AccountViewModel;
import com.lifedawn.capstoneapp.common.viewmodel.CalendarViewModel;
import com.lifedawn.capstoneapp.databinding.FragmentEditPromiseBinding;
import com.lifedawn.capstoneapp.map.LocationDto;
import com.lifedawn.capstoneapp.map.NewPromiseLocationNaverMapFragment;
import com.lifedawn.capstoneapp.map.SelectedLocationSimpleMapFragment;
import com.lifedawn.capstoneapp.map.adapters.LocationItemViewPagerAbstractAdapter;
import com.lifedawn.capstoneapp.promise.editpromise.EditPromiseFragment;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;


public abstract class AbstractPromiseFragment extends Fragment {
	protected FragmentEditPromiseBinding binding;
	protected EditType editType;
	protected AccountViewModel accountViewModel;
	protected CalendarViewModel calendarViewModel;
	protected GoogleAccountLifeCycleObserver googleAccountLifeCycleObserver;
	protected SelectedLocationSimpleMapFragment mapFragment;

	protected final DateTimeFormatter START_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy M/d E a h:mm");
	protected final DateTimeFormatter START_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy M/d E");
	protected final DateTimeFormatter START_TIME_FORMATTER = DateTimeFormatter.ofPattern("a h:mm");

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

		googleAccountLifeCycleObserver = new GoogleAccountLifeCycleObserver(requireActivity().getActivityResultRegistry(),
				requireActivity());
		getLifecycle().addObserver(googleAccountLifeCycleObserver);

		accountViewModel = new ViewModelProvider(getActivity()).get(AccountViewModel.class);
		calendarViewModel = new ViewModelProvider(getActivity()).get(CalendarViewModel.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentEditPromiseBinding.inflate(inflater);
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

		binding.invite.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickedInviteFriendChip();
			}
		});

		binding.addReminderChip.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickedAddReminderChip();
			}
		});

		binding.date.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final LocalDate localDate = onClickedDate();
				MaterialDatePicker datePicker = MaterialDatePicker.Builder.datePicker().setTitleText(R.string.promiseDate).setInputMode(
						MaterialDatePicker.INPUT_MODE_CALENDAR).setSelection(TimeUnit.DAYS.toMillis(localDate.toEpochDay())).build();

				datePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener() {
					@Override
					public void onPositiveButtonClick(Object selection) {
						final Long newDateLong = (Long) selection;
						final LocalDate newDate = LocalDate.ofEpochDay(TimeUnit.MILLISECONDS.toDays(newDateLong));
						binding.date.setText(newDate.format(START_DATE_FORMATTER));
						binding.date.setTag(newDate);
						onResultDate(newDate);
					}
				});
				datePicker.addOnNegativeButtonClickListener(view -> {
					datePicker.dismiss();
				});

				datePicker.show(getChildFragmentManager(), datePicker.toString());
			}
		});

		binding.time.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final LocalTime localTime = onClickedTime();
				MaterialTimePicker.Builder builder = new MaterialTimePicker.Builder();
				MaterialTimePicker timePicker = builder.setTitleText(R.string.promiseTime).setTimeFormat(TimeFormat.CLOCK_12H).setHour(
						localTime.getHour()).setMinute(localTime.getMinute()).setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK).build();

				timePicker.addOnPositiveButtonClickListener(view -> {
					final LocalTime newTime = LocalTime.of(timePicker.getHour(), timePicker.getMinute());
					binding.time.setTag(newTime);
					binding.time.setText(newTime.format(START_TIME_FORMATTER));
					onResultTime(newTime);
				});
				timePicker.addOnNegativeButtonClickListener(view -> {
					timePicker.dismiss();
				});
				timePicker.show(getChildFragmentManager(), timePicker.toString());
			}
		});

		binding.account.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickedAccount();
			}
		});

		binding.placeName.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickedMap();
			}
		});

		mapFragment = new SelectedLocationSimpleMapFragment();
		getChildFragmentManager().beginTransaction().add(binding.naverMap.getId(), mapFragment).commit();
	}

	@Override
	public void onDestroy() {
		getLifecycle().removeObserver(googleAccountLifeCycleObserver);
		super.onDestroy();
	}

	protected void initAttendeesView(List<EventAttendee> attendeeList) {
		if (binding.attendeeChipGroup.getChildCount() >= 2) {
			binding.attendeeChipGroup.removeViews(1, binding.attendeeChipGroup.getChildCount() - 1);
		}
		if (attendeeList != null) {
			int index = 1;
			for (EventAttendee eventAttendee : attendeeList) {
				Chip chip = (Chip) getLayoutInflater().inflate(R.layout.event_attendee_chip, null);
				chip.setText(eventAttendee.getEmail());

				int finalIndex = index;
				chip.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onClickedFriendChip(eventAttendee, finalIndex, false);
					}
				});
				chip.setOnCloseIconClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onClickedFriendChip(eventAttendee, finalIndex, true);
					}
				});

				index++;
				binding.attendeeChipGroup.addView(chip);
			}
		}
	}

	protected void initRemindersView(List<EventReminder> eventReminderList) {
		if (binding.reminderChipGroup.getChildCount() >= 2) {
			binding.reminderChipGroup.removeViews(1, binding.reminderChipGroup.getChildCount() - 1);
		}
		if (eventReminderList != null) {
			int index = 1;

			for (EventReminder eventReminder : eventReminderList) {
				Chip chip = (Chip) getLayoutInflater().inflate(R.layout.event_reminder_chip, null);
				chip.setText(ReminderUtil.makeReminderText(ReminderUtil.make(eventReminder.getMinutes()), getContext()));
				int finalIndex = index;
				chip.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onClickedReminderChip(eventReminder, finalIndex, false);
					}
				});
				chip.setOnCloseIconClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onClickedReminderChip(eventReminder, finalIndex, true);
					}
				});
				index++;
				binding.reminderChipGroup.addView(chip);
			}
		}
	}

	protected void setAccount(Constant accountType, @Nullable GoogleSignInAccount account) {
		if (accountType == Constant.ACCOUNT_GOOGLE) {
			binding.account.setText(account.getDisplayName());
		} else {
			binding.account.setText(getString(R.string.local));
		}
	}

	//
	protected final boolean isDuplicate(List<EventReminder> eventReminderList, EventReminder eventReminder) {
		if (eventReminderList != null) {
			for (EventReminder compEventReminder : eventReminderList) {
				if (compEventReminder.getMinutes().equals(eventReminder.getMinutes())) {
					return true;
				}
			}
		}
		return false;
	}

	protected DateTime getStartDateTime() {
		LocalDate localDate = (LocalDate) binding.date.getTag();
		LocalTime localTime = (LocalTime) binding.time.getTag();

		ZonedDateTime zonedDateTime = ZonedDateTime.of(localDate, localTime, ZoneId.systemDefault());
		Calendar calendar = Calendar.getInstance(TimeZone.getDefault());

		calendar.set(zonedDateTime.getYear(), zonedDateTime.getMonthValue() - 1, zonedDateTime.getDayOfMonth(), zonedDateTime.getHour(),
				zonedDateTime.getMinute());
		DateTime dateTime = new DateTime(calendar.getTime());
		return dateTime;
	}

	protected void showMap(LocationItemViewPagerAbstractAdapter.OnClickedLocationBtnListener onClickedLocationBtnListener) {
		NewPromiseLocationNaverMapFragment newPromiseLocationNaverMapFragment = new NewPromiseLocationNaverMapFragment();
		newPromiseLocationNaverMapFragment.setOnClickedLocationBtnListener(onClickedLocationBtnListener);

		newPromiseLocationNaverMapFragment.setPlaceBottomSheetSelectBtnVisibility(View.VISIBLE);
		getParentFragmentManager().beginTransaction().hide(this).add(R.id.fragmentContainerView, newPromiseLocationNaverMapFragment,
				NewPromiseLocationNaverMapFragment.class.getName()).addToBackStack(
				NewPromiseLocationNaverMapFragment.class.getName()).commit();
	}

	protected void onSelectedLocation(LocationDto locationDto) {
		binding.placeName.setText(
				locationDto.getLocationType() == Constant.PLACE ? locationDto.getPlaceName() : locationDto.getAddressName());
		binding.naverMap.setVisibility(View.VISIBLE);
		mapFragment.replaceLocation(locationDto);
	}

	protected abstract void onResultDate(LocalDate date);

	protected abstract void onResultTime(LocalTime time);

	protected abstract LocalDate onClickedDate();

	protected abstract LocalTime onClickedTime();

	protected abstract void onClickedAccount();

	protected abstract void onClickedMap();

	protected abstract void onClickedInviteFriendChip();

	protected abstract void onClickedAddReminderChip();

	protected abstract void onClickedFriendChip(EventAttendee eventAttendee, int index, boolean remove);

	protected abstract void onClickedReminderChip(EventReminder eventReminder, int index, boolean remove);
}