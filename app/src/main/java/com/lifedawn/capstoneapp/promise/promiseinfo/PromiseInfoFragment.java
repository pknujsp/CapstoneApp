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
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.chip.Chip;
import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.chat.VoteMainFragment;
import com.lifedawn.capstoneapp.common.constants.Constant;
import com.lifedawn.capstoneapp.common.interfaces.BackgroundCallback;
import com.lifedawn.capstoneapp.common.repository.CalendarRepository;
import com.lifedawn.capstoneapp.common.util.ReminderUtil;
import com.lifedawn.capstoneapp.common.viewmodel.AccountViewModel;
import com.lifedawn.capstoneapp.common.viewmodel.FriendViewModel;
import com.lifedawn.capstoneapp.databinding.FragmentPromiseInfoBinding;
import com.lifedawn.capstoneapp.friends.AttendeeInfoDialog;
import com.lifedawn.capstoneapp.model.firestore.PlaceDto;
import com.lifedawn.capstoneapp.map.PromiseLocationNaverMapFragment;
import com.lifedawn.capstoneapp.map.SelectedLocationSimpleMapFragment;
import com.lifedawn.capstoneapp.retrofits.MultipleRestApiDownloader;
import com.lifedawn.capstoneapp.weather.WeatherResponseData;
import com.lifedawn.capstoneapp.weather.model.CurrentConditionsDto;
import com.lifedawn.capstoneapp.weather.model.DailyForecastDto;
import com.lifedawn.capstoneapp.weather.model.HourlyForecastDto;
import com.lifedawn.capstoneapp.weather.request.WeatherRequest;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PromiseInfoFragment extends Fragment {
	private FragmentPromiseInfoBinding binding;
	private String eventId;
	private ContentValues originalEvent;
	private PlaceDto placeDto;
	private SelectedLocationSimpleMapFragment mapFragment;
	private FriendViewModel friendViewModel;
	private AccountViewModel accountViewModel;
	private ZonedDateTime promiseDateTime;
	private MultipleRestApiDownloader multipleRestApiDownloader;
	private Bundle bundle;

	private final DateTimeFormatter START_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy M/d E a h:mm");


	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		bundle = getArguments() != null ? getArguments() : savedInstanceState;

		eventId = bundle.getString("eventId");
		friendViewModel = new ViewModelProvider(requireActivity()).get(FriendViewModel.class);
		accountViewModel = new ViewModelProvider(requireActivity()).get(AccountViewModel.class);
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putAll(bundle);
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

		binding.rootProgressLayout.setContentView(binding.scrollView);
		binding.rootProgressLayout.onStarted(null);

		binding.toolbar.backBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getParentFragmentManager().popBackStack();
			}
		});
		binding.toolbar.fragmentTitle.setText(R.string.promise_info);

		binding.weatherProgressLayout.setContentView(binding.weatherLayout);
		binding.weatherProgressLayout.onStarted(getString(R.string.loading_weather_data));
		binding.todayCurrentWeather.title.setText(R.string.todayCurrentWeather);
		binding.updateBtn.setVisibility(View.GONE);

		binding.votePlaces.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				VoteMainFragment voteMainFragment = new VoteMainFragment();

				FragmentManager fragmentManager = getParentFragmentManager();
				fragmentManager.beginTransaction().hide(PromiseInfoFragment.this).add(
						R.id.fragmentContainerView, voteMainFragment, VoteMainFragment.class.getName()).addToBackStack(
						VoteMainFragment.class.getName()).commitAllowingStateLoss();
			}
		});

		CalendarRepository.loadEvent(getContext(), eventId,
				new BackgroundCallback<List<CalendarRepository.EventObj>>() {
					@Override
					public void onResultSuccessful(List<CalendarRepository.EventObj> e) {
						final CalendarRepository.EventObj eventObj = e.get(0);
						originalEvent = eventObj.getEvent();

						String dtStart = originalEvent.getAsString(CalendarContract.Events.DTSTART);
						String eventTimeZone = originalEvent.getAsString(CalendarContract.Events.EVENT_TIMEZONE);
						ZonedDateTime start = ZonedDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(dtStart)), ZoneId.of(eventTimeZone));

						promiseDateTime = start;

						if (originalEvent.getAsString(CalendarContract.Events.EVENT_LOCATION) != null) {
							placeDto = PlaceDto.toLocationDto(originalEvent.getAsString(CalendarContract.Events.EVENT_LOCATION));
						}

						mapFragment = new SelectedLocationSimpleMapFragment();
						Bundle bundle = new Bundle();

						if (placeDto != null) {
							mapFragment.replaceLocation(placeDto);
							bundle.putSerializable("locationDto", placeDto);

							final Double latitude = placeDto.getLatitudeAsDouble();
							final Double longitude = placeDto.getLongitudeAsDouble();

							if (WeatherResponseData.getWeatherResponse(latitude, longitude) != null) {
								onResultWeather(WeatherResponseData.getWeatherResponse(latitude, longitude));
							} else {
								refreshWeatherData();
							}

							binding.placeName.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									PromiseLocationNaverMapFragment promiseLocationNaverMapFragment =
											new PromiseLocationNaverMapFragment();
									Bundle argument = new Bundle();
									argument.putSerializable("locationDto", placeDto);
									argument.putSerializable("promiseDateTime", promiseDateTime);

									promiseLocationNaverMapFragment.setArguments(argument);

									FragmentManager fragmentManager = getParentFragmentManager();
									fragmentManager.beginTransaction().hide(PromiseInfoFragment.this).add(
											R.id.fragmentContainerView, promiseLocationNaverMapFragment, PromiseLocationNaverMapFragment.class.getName()).addToBackStack(
											PromiseLocationNaverMapFragment.class.getName()).commitAllowingStateLoss();
								}
							});
						}
						mapFragment.setArguments(bundle);

						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								binding.title.setText(originalEvent.getAsString(CalendarContract.Events.TITLE) == null ?
										getString(R.string.no_title) : originalEvent.getAsString(CalendarContract.Events.TITLE));

								binding.dateTime.setText(start.format(START_DATETIME_FORMATTER));
								binding.description.setText(originalEvent.getAsString(CalendarContract.Events.DESCRIPTION));

								//장소
								if (placeDto != null) {
									binding.placeName.setText(placeDto.getLocationType() == Constant.PLACE ? placeDto.getPlaceName() : placeDto.getAddressName());
									binding.naverMap.setVisibility(View.VISIBLE);
								} else {
									binding.placeName.setText(R.string.no_promise_location);
									binding.naverMap.setVisibility(View.GONE);
									binding.weatherProgressLayout.onFailed(getString(R.string.no_weather_location));
								}
								getChildFragmentManager().beginTransaction().add(binding.naverMap.getId(), mapFragment).commitAllowingStateLoss();

								initAttendeesView(eventObj.getAttendeeList());
								initRemindersView(eventObj.getReminderList());

								binding.rootProgressLayout.onSuccessful();
							}
						});
					}

					@Override
					public void onResultFailed(Exception e) {
						binding.rootProgressLayout.onFailed(getString(R.string.failed_loading_event));
					}
				});


		binding.updateBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				binding.weatherProgressLayout.onStarted(getString(R.string.loading_weather_data));
				binding.updateBtn.setVisibility(View.GONE);

				refreshWeatherData();
			}
		});
	}

	void refreshWeatherData() {
		multipleRestApiDownloader = WeatherRequest.requestWeatherData(getContext(),
				placeDto.getLatitudeAsDouble(), placeDto.getLongitudeAsDouble(),
				new BackgroundCallback<WeatherRequest.WeatherResponseResult>() {
					@Override
					public void onResultSuccessful(WeatherRequest.WeatherResponseResult weatherResponseResult) {
						WeatherResponseData.addWeatherResponse(getContext(), weatherResponseResult.getLatitude(),
								weatherResponseResult.getLongitude(), weatherResponseResult.getMultipleRestApiDownloader());
						onResultWeather(WeatherResponseData.getWeatherResponse(placeDto.getLatitudeAsDouble(), placeDto.getLongitudeAsDouble()));
					}

					@Override
					public void onResultFailed(Exception e) {
						if (getActivity() != null) {
							getActivity().runOnUiThread(new Runnable() {
								@Override
								public void run() {
									binding.weatherProgressLayout.onFailed(getString(R.string.failed_loading_weather_data));
									binding.updateBtn.setVisibility(View.VISIBLE);
								}
							});
						}
					}
				});
	}

	@Override
	public void onDestroy() {
		if (multipleRestApiDownloader != null) {
			if (multipleRestApiDownloader.getCallMap().size() > 0) {
				multipleRestApiDownloader.cancel();
			}
		}
		super.onDestroy();
	}

	protected void initAttendeesView(List<ContentValues> attendeeList) {
		if (attendeeList != null) {
			ContentValues organizer = new ContentValues();
			organizer.put(CalendarContract.Attendees.ATTENDEE_EMAIL, originalEvent.getAsString(CalendarContract.Events.ORGANIZER));
			attendeeList.add(organizer);

			String status = null;
			String txt = null;
			boolean isOrganizer = false;

			List<Chip> chips = new ArrayList<>();

			for (ContentValues eventAttendee : attendeeList) {
				Chip chip = (Chip) getLayoutInflater().inflate(R.layout.event_attendee_chip, null);
				chips.add(chip);

				chip.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						String name =
								eventAttendee.getAsString(CalendarContract.Attendees.ATTENDEE_EMAIL).equals(accountViewModel.getLastSignInAccountName()) ? getString(R.string.me) :
										friendViewModel.getName(eventAttendee);
						String email = eventAttendee.getAsString(CalendarContract.Attendees.ATTENDEE_EMAIL);
						AttendeeInfoDialog.show(requireActivity(), name, email);
					}
				});

				isOrganizer =
						originalEvent.getAsString(CalendarContract.Events.ORGANIZER).equals(eventAttendee.getAsString(CalendarContract.Attendees.ATTENDEE_EMAIL));

				if (!isOrganizer && eventAttendee.containsKey(CalendarContract.Attendees.ATTENDEE_STATUS)) {
					switch (eventAttendee.getAsInteger(CalendarContract.Attendees.ATTENDEE_STATUS)) {
						case CalendarContract.Attendees.ATTENDEE_STATUS_ACCEPTED:
							status = getString(R.string.acceptance);
							break;
						case CalendarContract.Attendees.ATTENDEE_STATUS_DECLINED:
							status = getString(R.string.declined);
							break;
						default:
							status = getString(R.string.no_response);
					}
				} else {
					status = getString(R.string.invitee);
				}
				txt = status + " - " + (eventAttendee.getAsString(CalendarContract.Attendees.ATTENDEE_EMAIL).equals(accountViewModel.getLastSignInAccountName()) ? getString(R.string.me) :
						friendViewModel.getName(eventAttendee));
				chip.setText(txt);
				chip.setCloseIconVisible(false);
				chip.setCheckable(false);

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
				chip.setClickable(false);
				chip.setFocusable(false);
				binding.reminderChipGroup.addView(chip);
			}
		}
	}

	private void onResultWeather(WeatherResponseData.WeatherResponseObj weatherResponseObj) {
		if (getActivity() == null) {
			return;
		}

		if (weatherResponseObj != null) {
			final CurrentConditionsDto currentConditionsDto = weatherResponseObj.currentConditionsDto;
			final List<HourlyForecastDto> hourlyForecastDtoList = weatherResponseObj.hourlyForecastDtoList;
			final List<DailyForecastDto> dailyForecastDtoList = weatherResponseObj.dailyForecastDtoList;
			final WeatherResponseData.PromiseWeatherForecast promiseWeatherForecast =
					WeatherResponseData.getPromiseDayHourlyForecast(promiseDateTime, hourlyForecastDtoList, dailyForecastDtoList);

			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					DateTimeFormatter lastUpdateDateTimeFormatter = DateTimeFormatter.ofPattern("M.d E a hh:mm");
					binding.updateBtn.setText(weatherResponseObj.multipleRestApiDownloader.getRequestDateTime().format(lastUpdateDateTimeFormatter));

					binding.todayCurrentWeather.leftWeatherIcon.setImageResource(currentConditionsDto.getWeatherIcon());
					binding.todayCurrentWeather.rightWeatherIcon.setVisibility(View.GONE);
					binding.todayCurrentWeather.weatherDescription.setText(currentConditionsDto.getWeatherDescription());
					binding.todayCurrentWeather.temperature.setText(currentConditionsDto.getTemp());

					if (promiseWeatherForecast.dailyForecastDto == null && promiseWeatherForecast.hourlyForecastDto == null) {
						binding.promiseDayCurrentWeather.getRoot().setVisibility(View.GONE);
					} else {
						if (promiseWeatherForecast.hourlyForecastDto != null) {
							DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("d E HH시 예상");
							binding.promiseDayCurrentWeather.title.setText(promiseWeatherForecast.hourlyForecastDto.getHours().format(dateTimeFormatter));
							binding.promiseDayCurrentWeather.rightWeatherIcon.setVisibility(View.GONE);

							binding.promiseDayCurrentWeather.leftWeatherIcon.setImageResource(promiseWeatherForecast.hourlyForecastDto.getWeatherIcon());
							binding.promiseDayCurrentWeather.temperature.setText(promiseWeatherForecast.hourlyForecastDto.getTemp());
							binding.promiseDayCurrentWeather.weatherDescription.setText(promiseWeatherForecast.hourlyForecastDto.getWeatherDescription());
						} else {
							binding.promiseDayCurrentWeather.temperature.setText(new String(promiseWeatherForecast.dailyForecastDto.getMinTemp() + " /" +
									" " + promiseWeatherForecast.dailyForecastDto.getMaxTemp()));
							binding.promiseDayCurrentWeather.title.setText(R.string.promiseDayCurrentWeather);
							if (promiseWeatherForecast.dailyForecastDto.isSingle()) {
								binding.promiseDayCurrentWeather.rightWeatherIcon.setVisibility(View.GONE);

								binding.promiseDayCurrentWeather.leftWeatherIcon.setImageResource(promiseWeatherForecast.dailyForecastDto.getSingleValues().getWeatherIcon());
								binding.promiseDayCurrentWeather.weatherDescription.setText(promiseWeatherForecast.dailyForecastDto.getSingleValues().getWeatherDescription());
							} else {
								binding.promiseDayCurrentWeather.rightWeatherIcon.setVisibility(View.VISIBLE);

								binding.promiseDayCurrentWeather.leftWeatherIcon.setImageResource(promiseWeatherForecast.dailyForecastDto.getAmValues().getWeatherIcon());
								binding.promiseDayCurrentWeather.rightWeatherIcon.setImageResource(promiseWeatherForecast.dailyForecastDto.getPmValues().getWeatherIcon());
								binding.promiseDayCurrentWeather.weatherDescription.setText(new String(promiseWeatherForecast.dailyForecastDto.getAmValues().getWeatherDescription()
										+ " / " + promiseWeatherForecast.dailyForecastDto.getPmValues().getWeatherDescription()));
							}
						}
					}

					binding.weatherProgressLayout.onSuccessful();
					binding.updateBtn.setVisibility(View.VISIBLE);

				}
			});
		} else {
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					binding.weatherProgressLayout.onFailed(getString(R.string.failed_loading_weather_data));
					binding.updateBtn.setVisibility(View.VISIBLE);
				}
			});
		}
	}
}
