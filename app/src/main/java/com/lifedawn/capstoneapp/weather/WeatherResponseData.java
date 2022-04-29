package com.lifedawn.capstoneapp.weather;

import android.content.Context;
import android.util.ArrayMap;

import com.lifedawn.capstoneapp.retrofits.MultipleRestApiDownloader;
import com.lifedawn.capstoneapp.retrofits.RetrofitClient;
import com.lifedawn.capstoneapp.retrofits.response.kma.KmaCurrentConditions;
import com.lifedawn.capstoneapp.retrofits.response.kma.KmaDailyForecast;
import com.lifedawn.capstoneapp.retrofits.response.kma.KmaHourlyForecast;
import com.lifedawn.capstoneapp.weather.model.CurrentConditionsDto;
import com.lifedawn.capstoneapp.weather.model.DailyForecastDto;
import com.lifedawn.capstoneapp.weather.model.HourlyForecastDto;
import com.lifedawn.capstoneapp.weather.response.KmaResponseProcessor;
import com.lifedawn.capstoneapp.weather.util.WeatherUtil;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class WeatherResponseData {

	private WeatherResponseData() {
	}

	private static Map<String, WeatherResponseObj> map = new HashMap<>();

	public static WeatherResponseObj getWeatherResponse(Double latitude, Double longitude) {
		//30분 이상 지난 데이터 인 경우 삭제하고 null반환
		final String key = latitude.toString() + longitude.toString();
		if (map.containsKey(key)) {
			if (TimeUnit.SECONDS.toMinutes(ZonedDateTime.now().getSecond()) - TimeUnit.SECONDS.toMinutes(map.get(key).multipleRestApiDownloader.getRequestDateTime().getSecond()) >= 30L) {
				map.remove(key);
				return null;
			} else {
				return map.get(key);
			}
		} else {
			return null;
		}

	}

	public static WeatherResponseObj addWeatherResponse(Context context, Double latitude, Double longitude,
	                                                    MultipleRestApiDownloader multipleRestApiDownloader) {
		Map<DataProviderType, ArrayMap<RetrofitClient.ServiceType, MultipleRestApiDownloader.ResponseResult>> responseMap =
				multipleRestApiDownloader.getResponseMap();
		ArrayMap<RetrofitClient.ServiceType, MultipleRestApiDownloader.ResponseResult> arrayMap = responseMap.get(DataProviderType.KMA_WEB);

		if (arrayMap.get(RetrofitClient.ServiceType.KMA_WEB_FORECASTS).isSuccessful() &&
				arrayMap.get(RetrofitClient.ServiceType.KMA_WEB_CURRENT_CONDITIONS).isSuccessful()) {
			KmaCurrentConditions kmaCurrentConditions = (KmaCurrentConditions) arrayMap.get(RetrofitClient.ServiceType.KMA_WEB_CURRENT_CONDITIONS).getResponseObj();
			Object[] forecasts = (Object[]) arrayMap.get(RetrofitClient.ServiceType.KMA_WEB_FORECASTS).getResponseObj();

			ArrayList<KmaHourlyForecast> kmaHourlyForecasts = (ArrayList<KmaHourlyForecast>) forecasts[0];
			ArrayList<KmaDailyForecast> kmaDailyForecasts = (ArrayList<KmaDailyForecast>) forecasts[1];

			List<HourlyForecastDto> hourlyForecastDtoList = KmaResponseProcessor.makeHourlyForecastDtoListOfWEB(context,
					kmaHourlyForecasts, latitude, longitude);
			List<DailyForecastDto> dailyForecastDtoList = KmaResponseProcessor.makeDailyForecastDtoListOfWEB(kmaDailyForecasts);
			CurrentConditionsDto currentConditionsDto = KmaResponseProcessor.makeCurrentConditionsDtoOfWEB(context,
					kmaCurrentConditions, kmaHourlyForecasts.get(0), latitude, longitude);


			map.put(latitude.toString() + longitude.toString(), new WeatherResponseObj(currentConditionsDto, hourlyForecastDtoList,
					dailyForecastDtoList, multipleRestApiDownloader));
			return map.get(latitude.toString() + longitude.toString());
		} else {
			return null;
		}

	}

	public static PromiseWeatherForecast getPromiseDayHourlyForecast(ZonedDateTime promiseDateTime,
	                                                                 List<HourlyForecastDto> hourlyForecastDtoList, List<DailyForecastDto> dailyForecastDtoList) {
		final WeatherDataType promiseWeatherDataType = WeatherUtil.findPromiseWeatherDataType(promiseDateTime.toLocalDateTime(),
				hourlyForecastDtoList, dailyForecastDtoList);

		PromiseWeatherForecast promiseWeatherForecast = new PromiseWeatherForecast(promiseWeatherDataType);

		if (promiseWeatherDataType != null) {
			if (promiseWeatherDataType == WeatherDataType.hourlyForecast) {
				promiseWeatherForecast.hourlyForecastDto = WeatherUtil.getPromiseDayWeatherByHourly(promiseDateTime.toLocalDateTime(),
						hourlyForecastDtoList);
			} else {
				promiseWeatherForecast.dailyForecastDto = WeatherUtil.getPromiseDayWeatherByDaily(promiseDateTime.toLocalDateTime(),
						dailyForecastDtoList);
			}
		}
		return promiseWeatherForecast;
	}


	public static class WeatherResponseObj {
		public final CurrentConditionsDto currentConditionsDto;
		public final List<HourlyForecastDto> hourlyForecastDtoList;
		public final List<DailyForecastDto> dailyForecastDtoList;
		public final MultipleRestApiDownloader multipleRestApiDownloader;

		public WeatherResponseObj(CurrentConditionsDto currentConditionsDto, List<HourlyForecastDto> hourlyForecastDtoList, List<DailyForecastDto> dailyForecastDtoList, MultipleRestApiDownloader multipleRestApiDownloader) {
			this.currentConditionsDto = currentConditionsDto;
			this.hourlyForecastDtoList = hourlyForecastDtoList;
			this.dailyForecastDtoList = dailyForecastDtoList;
			this.multipleRestApiDownloader = multipleRestApiDownloader;
		}
	}

	public static class PromiseWeatherForecast {
		final WeatherDataType weatherDataType;
		public HourlyForecastDto hourlyForecastDto;
		public DailyForecastDto dailyForecastDto;

		public PromiseWeatherForecast(WeatherDataType weatherDataType) {
			this.weatherDataType = weatherDataType;
		}


	}
}
