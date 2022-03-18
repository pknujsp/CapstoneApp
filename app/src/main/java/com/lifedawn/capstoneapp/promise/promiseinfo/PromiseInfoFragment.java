package com.lifedawn.capstoneapp.promise.promiseinfo;

import android.content.ContentValues;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.chip.Chip;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.common.constants.Constant;
import com.lifedawn.capstoneapp.common.constants.ValueUnits;
import com.lifedawn.capstoneapp.common.interfaces.BackgroundCallback;
import com.lifedawn.capstoneapp.common.repository.CalendarRepository;
import com.lifedawn.capstoneapp.common.repositoryinterface.ICalendarRepository;
import com.lifedawn.capstoneapp.common.util.ReminderUtil;
import com.lifedawn.capstoneapp.databinding.FragmentPromiseInfoBinding;
import com.lifedawn.capstoneapp.main.MyApplication;
import com.lifedawn.capstoneapp.map.LocationDto;
import com.lifedawn.capstoneapp.map.SelectedLocationSimpleMapFragment;
import com.lifedawn.capstoneapp.promise.abstractfragment.AbstractPromiseFragment;
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

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class PromiseInfoFragment extends Fragment {
	private FragmentPromiseInfoBinding binding;
	private String eventId;
	private ContentValues originalEvent;
	private LocationDto locationDto;
	private SelectedLocationSimpleMapFragment mapFragment;

	private final DateTimeFormatter START_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy M/d E a h:mm");


	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = getArguments();

		eventId = bundle.getString("eventId");
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

		binding.weatherLayout.setVisibility(View.GONE);

		binding.toolbar.backBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getParentFragmentManager().popBackStack();
			}
		});
		binding.toolbar.fragmentTitle.setText(R.string.promise_info);


		CalendarRepository.loadEvent(getContext(), eventId,
				new BackgroundCallback<List<CalendarRepository.EventObj>>() {
					@Override
					public void onResultSuccessful(List<CalendarRepository.EventObj> e) {
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								final CalendarRepository.EventObj eventObj = e.get(0);
								originalEvent = eventObj.getEvent();
								binding.title.setText(originalEvent.getAsString(CalendarContract.Events.TITLE));

								String dtStart = originalEvent.getAsString(CalendarContract.Events.DTSTART);
								String eventTimeZone = originalEvent.getAsString(CalendarContract.Events.EVENT_TIMEZONE);
								ZonedDateTime start = ZonedDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(dtStart)), ZoneId.of(eventTimeZone));

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

										WeatherRequest.requestWeatherData(getContext(),
												Double.parseDouble(locationDto.getLatitude()),
												Double.parseDouble(locationDto.getLongitude()),
												new BackgroundCallback<WeatherRequest.WeatherResponseResult>() {
													@Override
													public void onResultSuccessful(WeatherRequest.WeatherResponseResult e) {
														onResultWeather(e);
													}

													@Override
													public void onResultFailed(Exception e) {
														getActivity().runOnUiThread(new Runnable() {
															@Override
															public void run() {
																Toast.makeText(getContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
															}
														});
													}
												});
									}
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

	protected void initAttendeesView(List<ContentValues> attendeeList) {
		if (attendeeList != null) {
			ContentValues organizer = new ContentValues();
			organizer.put(CalendarContract.Attendees.ATTENDEE_EMAIL, originalEvent.getAsString(CalendarContract.Events.ORGANIZER));
			attendeeList.add(organizer);

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
		} else {
		}
	}

	private void onResultWeather(WeatherRequest.WeatherResponseResult weatherResponseResult) {
		Set<WeatherProviderType> weatherProviderTypeSet = weatherResponseResult.getWeatherProviderTypeSet();
		MultipleRestApiDownloader multipleRestApiDownloader = weatherResponseResult.getMultipleRestApiDownloader();
		Double latitude = weatherResponseResult.getLatitude();
		Double longitude = weatherResponseResult.getLongitude();

		Map<WeatherProviderType, ArrayMap<RetrofitClient.ServiceType, MultipleRestApiDownloader.ResponseResult>> responseMap = multipleRestApiDownloader.getResponseMap();
		ArrayMap<RetrofitClient.ServiceType, MultipleRestApiDownloader.ResponseResult> arrayMap = null;

		CurrentConditionsDto currentConditionsDto = null;
		List<HourlyForecastDto> hourlyForecastDtoList = null;
		List<DailyForecastDto> dailyForecastDtoList = null;


		String currentConditionsWeatherVal = null;
		ZoneId zoneId = null;

		if (weatherProviderTypeSet.contains(WeatherProviderType.KMA_WEB)) {
			arrayMap = responseMap.get(WeatherProviderType.KMA_WEB);

			KmaCurrentConditions kmaCurrentConditions = (KmaCurrentConditions) arrayMap.get(RetrofitClient.ServiceType.KMA_WEB_CURRENT_CONDITIONS).getResponseObj();
			Object[] forecasts = (Object[]) arrayMap.get(RetrofitClient.ServiceType.KMA_WEB_FORECASTS).getResponseObj();

			ArrayList<KmaHourlyForecast> kmaHourlyForecasts = (ArrayList<KmaHourlyForecast>) forecasts[0];
			ArrayList<KmaDailyForecast> kmaDailyForecasts = (ArrayList<KmaDailyForecast>) forecasts[1];

			currentConditionsDto = KmaResponseProcessor.makeCurrentConditionsDtoOfWEB(getContext(),
					kmaCurrentConditions, kmaHourlyForecasts.get(0), latitude, longitude);

			hourlyForecastDtoList = KmaResponseProcessor.makeHourlyForecastDtoListOfWEB(getContext(),
					kmaHourlyForecasts, latitude, longitude);

			dailyForecastDtoList = KmaResponseProcessor.makeDailyForecastDtoListOfWEB(kmaDailyForecasts);

			String pty = kmaCurrentConditions.getPty();

			currentConditionsWeatherVal = pty.isEmpty() ? kmaHourlyForecasts.get(0).getWeatherDescription() : pty;
			zoneId = KmaResponseProcessor.getZoneId();
		}

		CurrentConditionsDto finalCurrentConditionsDto = currentConditionsDto;
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				binding.weatherLayout.setVisibility(View.VISIBLE);
				binding.weatherIcon.setImageResource(finalCurrentConditionsDto.getWeatherIcon());
				binding.weatherDescription.setText(finalCurrentConditionsDto.getWeatherDescription());
				binding.temperature.setText(finalCurrentConditionsDto.getTemp());
			}
		});
	}
}
