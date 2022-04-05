package com.lifedawn.capstoneapp.promise.promiseinfo;

import android.content.ContentValues;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.common.constants.Constant;
import com.lifedawn.capstoneapp.common.interfaces.BackgroundCallback;
import com.lifedawn.capstoneapp.common.repository.CalendarRepository;
import com.lifedawn.capstoneapp.common.util.ReminderUtil;
import com.lifedawn.capstoneapp.common.viewmodel.AccountViewModel;
import com.lifedawn.capstoneapp.common.viewmodel.FriendViewModel;
import com.lifedawn.capstoneapp.databinding.FragmentPromiseInfoBinding;
import com.lifedawn.capstoneapp.friends.AttendeeInfoDialog;
import com.lifedawn.capstoneapp.main.MainTransactionFragment;
import com.lifedawn.capstoneapp.map.LocationDto;
import com.lifedawn.capstoneapp.map.PromiseLocationNaverMapFragment;
import com.lifedawn.capstoneapp.map.SelectedLocationSimpleMapFragment;
import com.lifedawn.capstoneapp.retrofits.MultipleRestApiDownloader;
import com.lifedawn.capstoneapp.retrofits.RetrofitClient;
import com.lifedawn.capstoneapp.retrofits.response.kma.KmaCurrentConditions;
import com.lifedawn.capstoneapp.retrofits.response.kma.KmaDailyForecast;
import com.lifedawn.capstoneapp.retrofits.response.kma.KmaHourlyForecast;
import com.lifedawn.capstoneapp.weather.WeatherDataType;
import com.lifedawn.capstoneapp.weather.WeatherProviderType;
import com.lifedawn.capstoneapp.weather.model.CurrentConditionsDto;
import com.lifedawn.capstoneapp.weather.model.DailyForecastDto;
import com.lifedawn.capstoneapp.weather.model.HourlyForecastDto;
import com.lifedawn.capstoneapp.weather.request.WeatherRequest;
import com.lifedawn.capstoneapp.weather.response.KmaResponseProcessor;
import com.lifedawn.capstoneapp.weather.util.WeatherUtil;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PromiseInfoFragment extends Fragment {
	private FragmentPromiseInfoBinding binding;
	private String eventId;
	private ContentValues originalEvent;
	private LocationDto locationDto;
	private SelectedLocationSimpleMapFragment mapFragment;
	private FriendViewModel friendViewModel;
	private AccountViewModel accountViewModel;
	private ZonedDateTime promiseDateTime;

	private MultipleRestApiDownloader weatherMultipleRestApiDownloader;
	private final DateTimeFormatter START_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy M/d E a h:mm");


	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = getArguments();

		eventId = bundle.getString("eventId");
		friendViewModel = new ViewModelProvider(requireActivity()).get(FriendViewModel.class);
		accountViewModel = new ViewModelProvider(requireActivity()).get(AccountViewModel.class);
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

		binding.progressLayout.setVisibility(View.GONE);
		binding.todayCurrentWeather.title.setText(R.string.todayCurrentWeather);

		CalendarRepository.loadEvent(getContext(), eventId,
				new BackgroundCallback<List<CalendarRepository.EventObj>>() {
					@Override
					public void onResultSuccessful(List<CalendarRepository.EventObj> e) {
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								final CalendarRepository.EventObj eventObj = e.get(0);
								originalEvent = eventObj.getEvent();

								binding.title.setText(originalEvent.getAsString(CalendarContract.Events.TITLE) == null ?
										getString(R.string.no_title) : originalEvent.getAsString(CalendarContract.Events.TITLE));

								String dtStart = originalEvent.getAsString(CalendarContract.Events.DTSTART);
								String eventTimeZone = originalEvent.getAsString(CalendarContract.Events.EVENT_TIMEZONE);
								ZonedDateTime start = ZonedDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(dtStart)), ZoneId.of(eventTimeZone));

								promiseDateTime = start;

								DateTimeFormatter promiseDateTimeFormatter = DateTimeFormatter.ofPattern("M.d E");
								binding.promiseDayCurrentWeather.title.setText(new String(
										promiseDateTime.format(promiseDateTimeFormatter) + " " +
												getString(R.string.expected_forecast)));

								binding.dateTime.setText(start.format(START_DATETIME_FORMATTER));
								binding.description.setText(originalEvent.getAsString(CalendarContract.Events.DESCRIPTION));

								if (originalEvent.getAsString(CalendarContract.Events.EVENT_LOCATION) != null) {
									locationDto = LocationDto.toLocationDto(originalEvent.getAsString(CalendarContract.Events.EVENT_LOCATION));
								}

								mapFragment = new SelectedLocationSimpleMapFragment();
								Bundle bundle = new Bundle();

								//장소
								if (locationDto != null) {
									mapFragment.replaceLocation(locationDto);
									bundle.putSerializable("locationDto", locationDto);

									if (locationDto != null) {
										binding.placeName.setText(locationDto.getLocationType() == Constant.PLACE ? locationDto.getPlaceName() : locationDto.getAddressName());
										binding.naverMap.setVisibility(View.VISIBLE);
										binding.progressLayout.setVisibility(View.VISIBLE);

										weatherMultipleRestApiDownloader = WeatherRequest.requestWeatherData(getContext(),
												Double.parseDouble(locationDto.getLatitude()),
												Double.parseDouble(locationDto.getLongitude()),
												new BackgroundCallback<WeatherRequest.WeatherResponseResult>() {
													@Override
													public void onResultSuccessful(WeatherRequest.WeatherResponseResult e) {
														onResultWeather(e);
													}

													@Override
													public void onResultFailed(Exception e) {
														if (getActivity() != null) {
															getActivity().runOnUiThread(new Runnable() {
																@Override
																public void run() {
																	binding.progressCircular.setVisibility(View.GONE);
																	binding.progressMsg.setText(R.string.failed_loading_weather_data);
																}
															});
														}
													}
												});
									}

									binding.placeName.setOnClickListener(new View.OnClickListener() {
										@Override
										public void onClick(View v) {
											PromiseLocationNaverMapFragment promiseLocationNaverMapFragment =
													new PromiseLocationNaverMapFragment();
											Bundle argument = new Bundle();
											argument.putSerializable("locationDto", locationDto);
											argument.putSerializable("promiseDateTime", promiseDateTime);

											promiseLocationNaverMapFragment.setArguments(argument);

											FragmentManager fragmentManager = getParentFragmentManager();
											fragmentManager.beginTransaction().hide(PromiseInfoFragment.this).add(
													R.id.fragmentContainerView, promiseLocationNaverMapFragment, PromiseLocationNaverMapFragment.class.getName()).addToBackStack(
													PromiseLocationNaverMapFragment.class.getName()).commit();
										}
									});
								} else {
									binding.placeName.setText(R.string.no_promise_location);
									binding.naverMap.setVisibility(View.GONE);
								}
								mapFragment.setArguments(bundle);
								getChildFragmentManager().beginTransaction().add(binding.naverMap.getId(), mapFragment).commit();

								initAttendeesView(eventObj.getAttendeeList());
								initRemindersView(eventObj.getReminderList());
							}
						});
					}

					@Override
					public void onResultFailed(Exception e) {

					}
				});
	}

	@Override
	public void onDestroy() {
		if (weatherMultipleRestApiDownloader != null) {
			if (weatherMultipleRestApiDownloader.getCallMap().size() > 0) {
				weatherMultipleRestApiDownloader.cancel();
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

			for (ContentValues eventAttendee : attendeeList) {
				Chip chip = (Chip) getLayoutInflater().inflate(R.layout.event_attendee_chip, null);
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

	private void onResultWeather(WeatherRequest.WeatherResponseResult weatherResponseResult) {
		MultipleRestApiDownloader multipleRestApiDownloader = weatherResponseResult.getMultipleRestApiDownloader();
		Double latitude = weatherResponseResult.getLatitude();
		Double longitude = weatherResponseResult.getLongitude();

		Map<WeatherProviderType, ArrayMap<RetrofitClient.ServiceType, MultipleRestApiDownloader.ResponseResult>> responseMap = multipleRestApiDownloader.getResponseMap();
		ArrayMap<RetrofitClient.ServiceType, MultipleRestApiDownloader.ResponseResult> arrayMap = responseMap.get(WeatherProviderType.KMA_WEB);


		if (arrayMap.get(RetrofitClient.ServiceType.KMA_WEB_CURRENT_CONDITIONS).isSuccessful()) {

			KmaCurrentConditions kmaCurrentConditions = (KmaCurrentConditions) arrayMap.get(RetrofitClient.ServiceType.KMA_WEB_CURRENT_CONDITIONS).getResponseObj();
			Object[] forecasts = (Object[]) arrayMap.get(RetrofitClient.ServiceType.KMA_WEB_FORECASTS).getResponseObj();

			ArrayList<KmaHourlyForecast> kmaHourlyForecasts = (ArrayList<KmaHourlyForecast>) forecasts[0];
			ArrayList<KmaDailyForecast> kmaDailyForecasts = (ArrayList<KmaDailyForecast>) forecasts[1];

			final CurrentConditionsDto currentConditionsDto = KmaResponseProcessor.makeCurrentConditionsDtoOfWEB(getContext(),
					kmaCurrentConditions, kmaHourlyForecasts.get(0), latitude, longitude);

			final List<HourlyForecastDto> hourlyForecastDtoList = KmaResponseProcessor.makeHourlyForecastDtoListOfWEB(getContext(),
					kmaHourlyForecasts, latitude, longitude);

			final List<DailyForecastDto> dailyForecastDtoList = KmaResponseProcessor.makeDailyForecastDtoListOfWEB(kmaDailyForecasts);

			final WeatherDataType promiseWeatherDataType = WeatherUtil.findPromiseWeatherDataType(promiseDateTime.toLocalDateTime(),
					hourlyForecastDtoList,
					dailyForecastDtoList);

			HourlyForecastDto promiseDayHourlyDto = null;
			DailyForecastDto promiseDayDailyDto = null;

			if (promiseWeatherDataType != null) {
				if (promiseWeatherDataType == WeatherDataType.hourlyForecast) {
					promiseDayHourlyDto = WeatherUtil.getPromiseDayWeatherByHourly(promiseDateTime.toLocalDateTime(), hourlyForecastDtoList);
				} else {
					promiseDayDailyDto = WeatherUtil.getPromiseDayWeatherByDaily(promiseDateTime.toLocalDateTime(), dailyForecastDtoList);
				}
			}

			final HourlyForecastDto finalPromiseDayHourlyDto = promiseDayHourlyDto;
			final DailyForecastDto finalPromiseDayDailyDto = promiseDayDailyDto;

			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {

					binding.todayCurrentWeather.leftWeatherIcon.setImageResource(currentConditionsDto.getWeatherIcon());
					binding.todayCurrentWeather.rightWeatherIcon.setVisibility(View.GONE);
					binding.todayCurrentWeather.weatherDescription.setText(currentConditionsDto.getWeatherDescription());
					binding.todayCurrentWeather.temperature.setText(currentConditionsDto.getTemp());

					if (promiseWeatherDataType == null) {
						binding.promiseDayCurrentWeather.getRoot().setVisibility(View.GONE);
					} else {
						if (finalPromiseDayHourlyDto != null) {
							binding.promiseDayCurrentWeather.rightWeatherIcon.setVisibility(View.GONE);

							binding.promiseDayCurrentWeather.leftWeatherIcon.setImageResource(finalPromiseDayHourlyDto.getWeatherIcon());
							binding.promiseDayCurrentWeather.temperature.setText(finalPromiseDayHourlyDto.getTemp());
							binding.promiseDayCurrentWeather.weatherDescription.setText(finalPromiseDayHourlyDto.getWeatherDescription());
						} else {
							binding.promiseDayCurrentWeather.temperature.setText(new String(finalPromiseDayDailyDto.getMinTemp() + " / " +
									finalPromiseDayDailyDto.getMaxTemp()));

							if (finalPromiseDayDailyDto.isSingle()) {
								binding.promiseDayCurrentWeather.rightWeatherIcon.setVisibility(View.GONE);

								binding.promiseDayCurrentWeather.leftWeatherIcon.setImageResource(finalPromiseDayDailyDto.getSingleValues().getWeatherIcon());
								binding.promiseDayCurrentWeather.weatherDescription.setText(finalPromiseDayHourlyDto.getWeatherDescription());
							} else {
								binding.promiseDayCurrentWeather.rightWeatherIcon.setVisibility(View.VISIBLE);

								binding.promiseDayCurrentWeather.leftWeatherIcon.setImageResource(finalPromiseDayDailyDto.getAmValues().getWeatherIcon());
								binding.promiseDayCurrentWeather.rightWeatherIcon.setImageResource(finalPromiseDayDailyDto.getPmValues().getWeatherIcon());
								binding.promiseDayCurrentWeather.weatherDescription.setText(finalPromiseDayDailyDto.getAmValues().getWeatherDescription()
										+ " / " + finalPromiseDayDailyDto.getPmValues().getWeatherDescription());
							}
						}
					}

					binding.progressLayout.setVisibility(View.GONE);
				}
			});
		} else {
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					binding.progressCircular.setVisibility(View.GONE);
					binding.progressMsg.setText(R.string.failed_loading_weather_data);
				}
			});
		}
	}
}
