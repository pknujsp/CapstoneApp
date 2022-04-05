package com.lifedawn.capstoneapp.weather;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.common.interfaces.BackgroundCallback;
import com.lifedawn.capstoneapp.databinding.FragmentWeatherInfoBinding;
import com.lifedawn.capstoneapp.map.LocationDto;
import com.lifedawn.capstoneapp.retrofits.MultipleRestApiDownloader;
import com.lifedawn.capstoneapp.retrofits.RetrofitClient;
import com.lifedawn.capstoneapp.retrofits.response.kma.KmaCurrentConditions;
import com.lifedawn.capstoneapp.retrofits.response.kma.KmaDailyForecast;
import com.lifedawn.capstoneapp.retrofits.response.kma.KmaHourlyForecast;
import com.lifedawn.capstoneapp.weather.model.CurrentConditionsDto;
import com.lifedawn.capstoneapp.weather.model.DailyForecastDto;
import com.lifedawn.capstoneapp.weather.model.HourlyForecastDto;
import com.lifedawn.capstoneapp.weather.request.WeatherRequest;
import com.lifedawn.capstoneapp.weather.response.KmaResponseProcessor;
import com.lifedawn.capstoneapp.weather.util.WeatherUtil;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class WeatherInfoFragment extends DialogFragment {
	private FragmentWeatherInfoBinding binding;
	private LocationDto locationDto;
	private Bundle bundle;
	private ZonedDateTime promiseDateTime;
	private static MultipleRestApiDownloader weatherMultipleRestApiDownloader;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			bundle = getArguments();
		} else {
			bundle = savedInstanceState;
		}

		locationDto = (LocationDto) bundle.getSerializable("locationDto");
		promiseDateTime = (ZonedDateTime) bundle.getSerializable("promiseDateTime");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		binding = FragmentWeatherInfoBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		binding.todayandpromise.promiseDayCurrentWeather.title.setText(R.string.promiseDayCurrentWeather);

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
								}
							});
						}
					}
				});
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putAll(bundle);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
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
					binding.todayandpromise.todayCurrentWeather.amPm.setVisibility(View.GONE);
					binding.todayandpromise.todayCurrentWeather.leftWeatherIcon.setImageResource(currentConditionsDto.getWeatherIcon());
					binding.todayandpromise.todayCurrentWeather.rightWeatherIcon.setVisibility(View.GONE);
					binding.todayandpromise.todayCurrentWeather.weatherDescription.setText(currentConditionsDto.getWeatherDescription());
					binding.todayandpromise.todayCurrentWeather.temperature.setText(currentConditionsDto.getTemp());

					if (promiseWeatherDataType == null) {
						binding.todayandpromise.promiseDayCurrentWeather.getRoot().setVisibility(View.GONE);
					} else {
						if (finalPromiseDayHourlyDto != null) {
							binding.todayandpromise.promiseDayCurrentWeather.rightWeatherIcon.setVisibility(View.GONE);

							binding.todayandpromise.promiseDayCurrentWeather.leftWeatherIcon.setImageResource(finalPromiseDayHourlyDto.getWeatherIcon());
							binding.todayandpromise.promiseDayCurrentWeather.temperature.setText(finalPromiseDayHourlyDto.getTemp());
							binding.todayandpromise.promiseDayCurrentWeather.weatherDescription.setText(finalPromiseDayHourlyDto.getWeatherDescription());
						} else {
							binding.todayandpromise.promiseDayCurrentWeather.temperature.setText(new String(finalPromiseDayDailyDto.getMinTemp() + " / " +
									finalPromiseDayDailyDto.getMaxTemp()));

							if (finalPromiseDayDailyDto.isSingle()) {
								binding.todayandpromise.promiseDayCurrentWeather.rightWeatherIcon.setVisibility(View.GONE);

								binding.todayandpromise.promiseDayCurrentWeather.leftWeatherIcon.setImageResource(finalPromiseDayDailyDto.getSingleValues().getWeatherIcon());
								binding.todayandpromise.promiseDayCurrentWeather.weatherDescription.setText(finalPromiseDayHourlyDto.getWeatherDescription());
							} else {
								binding.todayandpromise.promiseDayCurrentWeather.rightWeatherIcon.setVisibility(View.VISIBLE);

								binding.todayandpromise.promiseDayCurrentWeather.leftWeatherIcon.setImageResource(finalPromiseDayDailyDto.getAmValues().getWeatherIcon());
								binding.todayandpromise.promiseDayCurrentWeather.rightWeatherIcon.setImageResource(finalPromiseDayDailyDto.getPmValues().getWeatherIcon());
								binding.todayandpromise.promiseDayCurrentWeather.weatherDescription.setText(finalPromiseDayDailyDto.getAmValues().getWeatherDescription()
										+ " / " + finalPromiseDayDailyDto.getPmValues().getWeatherDescription());
							}
						}
					}

				}
			});
		} else {
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
				}
			});
		}
	}
}