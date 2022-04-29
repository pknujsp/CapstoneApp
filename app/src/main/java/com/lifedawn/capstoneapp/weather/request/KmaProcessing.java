package com.lifedawn.capstoneapp.weather.request;

import android.content.Context;
import android.util.Log;

import com.lifedawn.capstoneapp.common.interfaces.OnDbQueryCallback;
import com.lifedawn.capstoneapp.retrofits.JsonDownloader;
import com.lifedawn.capstoneapp.retrofits.MultipleRestApiDownloader;
import com.lifedawn.capstoneapp.retrofits.Queries;
import com.lifedawn.capstoneapp.retrofits.RetrofitClient;
import com.lifedawn.capstoneapp.retrofits.parameters.kma.KmaCurrentConditionsParameters;
import com.lifedawn.capstoneapp.retrofits.parameters.kma.KmaForecastsParameters;
import com.lifedawn.capstoneapp.retrofits.response.kma.KmaCurrentConditions;
import com.lifedawn.capstoneapp.retrofits.response.kma.KmaDailyForecast;
import com.lifedawn.capstoneapp.retrofits.response.kma.KmaHourlyForecast;
import com.lifedawn.capstoneapp.room.dto.KmaAreaCodeDto;
import com.lifedawn.capstoneapp.weather.DataProviderType;
import com.lifedawn.capstoneapp.weather.response.KmaResponseProcessor;
import com.lifedawn.capstoneapp.weather.response.KmaWebParser;
import com.lifedawn.capstoneapp.weather.util.KmaAreaCodesRepository;
import com.lifedawn.capstoneapp.weather.util.LocationDistance;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class KmaProcessing {
	private KmaProcessing() {
	}

	/**
	 * 현재 날씨 web
	 */
	public static Call<String> getCurrentConditionsData(KmaCurrentConditionsParameters parameter, JsonDownloader callback) {
		Queries queries = RetrofitClient.getApiService(RetrofitClient.ServiceType.KMA_WEB_CURRENT_CONDITIONS);
		Call<String> call = queries.getKmaCurrentConditions(parameter.getParametersMap());
		call.enqueue(new Callback<String>() {
			@Override
			public void onResponse(Call<String> call, Response<String> response) {
				if (response.body() != null) {
					final Document currentConditionsDocument = Jsoup.parse(response.body());
					KmaCurrentConditions kmaCurrentConditions = KmaWebParser.parseCurrentConditions(currentConditionsDocument,
							ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toString());
					callback.onResponseResult(response, kmaCurrentConditions, response.body());
					Log.e("kma", "kma current conditions 성공");
				} else {
					callback.onResponseResult(new Exception());
					Log.e("kma", "kma current conditions 실패");
				}
			}

			@Override
			public void onFailure(Call<String> call, Throwable t) {
				callback.onResponseResult(t);
				Log.e("kma", "kma current conditions 실패");
			}
		});

		return call;
	}

	/**
	 * 시간별, 일별 web
	 */
	public static Call<String> getForecastsData(KmaForecastsParameters parameter, JsonDownloader callback) {
		Queries queries = RetrofitClient.getApiService(RetrofitClient.ServiceType.KMA_WEB_FORECASTS);
		Call<String> call = queries.getKmaHourlyAndDailyForecast(parameter.getParametersMap());
		call.enqueue(new Callback<String>() {
			@Override
			public void onResponse(Call<String> call, Response<String> response) {
				if (response.body() != null) {
					final Document forecastsDocument = Jsoup.parse(response.body());
					List<KmaHourlyForecast> kmaHourlyForecasts = KmaWebParser.parseHourlyForecasts(forecastsDocument);
					List<KmaDailyForecast> kmaDailyForecasts = KmaWebParser.parseDailyForecasts(forecastsDocument);
					KmaWebParser.makeExtendedDailyForecasts(kmaHourlyForecasts, kmaDailyForecasts);
					Object[] lists = new Object[]{kmaHourlyForecasts, kmaDailyForecasts};

					callback.onResponseResult(response, lists, response.body());
					Log.e("kma", "kma forecasts 성공");
				} else {
					callback.onResponseResult(new Exception());
					Log.e("kma", "kma forecasts 실패");
				}
			}

			@Override
			public void onFailure(Call<String> call, Throwable t) {
				callback.onResponseResult(t);
				Log.e("kma", "kma forecasts 실패");
			}
		});

		return call;
	}


	public static String getTmFc(ZonedDateTime dateTime) {
		final int hour = dateTime.getHour();
		final int minute = dateTime.getMinute();
		DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd");

		if (hour >= 18 && minute >= 1) {
			dateTime = dateTime.withHour(18);
			return dateTime.format(yyyyMMdd) + "1800";
		} else if (hour >= 6 && minute >= 1) {
			dateTime = dateTime.withHour(6);
			return dateTime.format(yyyyMMdd) + "0600";
		} else {
			dateTime = dateTime.minusDays(1).withHour(18);
			return dateTime.format(yyyyMMdd) + "1800";
		}
	}


	public static void requestWeatherDataAsWEB(Context context, Double latitude, Double longitude,
	                                           RequestKma requestKma,
	                                           MultipleRestApiDownloader multipleRestApiDownloader) {
		KmaAreaCodesRepository kmaAreaCodesRepository = new KmaAreaCodesRepository(context);
		kmaAreaCodesRepository.getAreaCodes(latitude, longitude,
				new OnDbQueryCallback<List<KmaAreaCodeDto>>() {

					@Override
					public void onResult(List<KmaAreaCodeDto> result) {
						if (result.isEmpty()) {
							Exception exception = new Exception("not found lat,lon");
							Set<RetrofitClient.ServiceType> requestTypeSet = requestKma.getRequestServiceTypes();

							if (requestTypeSet.contains(RetrofitClient.ServiceType.KMA_WEB_CURRENT_CONDITIONS)) {
								multipleRestApiDownloader.processResult(DataProviderType.KMA_WEB, null,
										RetrofitClient.ServiceType.KMA_WEB_CURRENT_CONDITIONS, exception);
							}
							if (requestTypeSet.contains(RetrofitClient.ServiceType.KMA_WEB_FORECASTS)) {
								multipleRestApiDownloader.processResult(DataProviderType.KMA_WEB, null,
										RetrofitClient.ServiceType.KMA_WEB_FORECASTS, exception);
							}
							return;
						}

						final double[] criteriaLatLng = {latitude, longitude};
						double minDistance = Double.MAX_VALUE;
						double distance = 0;
						double[] compLatLng = new double[2];
						KmaAreaCodeDto nearbyKmaAreaCodeDto = null;

						for (KmaAreaCodeDto weatherAreaCodeDTO : result) {
							compLatLng[0] = Double.parseDouble(weatherAreaCodeDTO.getLatitudeSecondsDivide100());
							compLatLng[1] = Double.parseDouble(weatherAreaCodeDTO.getLongitudeSecondsDivide100());

							distance = LocationDistance.distance(criteriaLatLng[0], criteriaLatLng[1], compLatLng[0], compLatLng[1],
									LocationDistance.Unit.METER);
							if (distance < minDistance) {
								minDistance = distance;
								nearbyKmaAreaCodeDto = weatherAreaCodeDTO;
							}
						}
						ZonedDateTime koreaLocalDateTime = ZonedDateTime.now(KmaResponseProcessor.getZoneId());
						multipleRestApiDownloader.put("koreaLocalDateTime", koreaLocalDateTime.toString());

						final String tmFc = getTmFc(koreaLocalDateTime);
						multipleRestApiDownloader.put("tmFc", tmFc);
						Set<RetrofitClient.ServiceType> requestTypeSet = requestKma.getRequestServiceTypes();

						final String code = nearbyKmaAreaCodeDto.getAdministrativeAreaCode();

						if (requestTypeSet.contains(RetrofitClient.ServiceType.KMA_WEB_CURRENT_CONDITIONS)) {
							final KmaCurrentConditionsParameters parameters = new KmaCurrentConditionsParameters(code);
							parameters.setLatitude(latitude).setLongitude(longitude);

							Call<String> currentConditionsCall = getCurrentConditionsData(parameters,
									new JsonDownloader() {
										@Override
										public void onResponseResult(Response<?> response, Object responseObj, String responseText) {
											multipleRestApiDownloader.processResult(DataProviderType.KMA_WEB, parameters,
													RetrofitClient.ServiceType.KMA_WEB_CURRENT_CONDITIONS, response, responseObj, responseText);
										}

										@Override
										public void onResponseResult(Throwable t) {
											multipleRestApiDownloader.processResult(DataProviderType.KMA_WEB, parameters,
													RetrofitClient.ServiceType.KMA_WEB_CURRENT_CONDITIONS, t);
										}
									});
							multipleRestApiDownloader.getCallMap().put(RetrofitClient.ServiceType.KMA_WEB_CURRENT_CONDITIONS, currentConditionsCall);

						}

						if (requestTypeSet.contains(RetrofitClient.ServiceType.KMA_WEB_FORECASTS)) {
							final KmaForecastsParameters parameters = new KmaForecastsParameters(code);
							parameters.setLatitude(latitude).setLongitude(longitude);

							Call<String> forecastsCall = getForecastsData(parameters,
									new JsonDownloader() {
										@Override
										public void onResponseResult(Response<?> response, Object responseObj, String responseText) {
											multipleRestApiDownloader.processResult(DataProviderType.KMA_WEB, parameters,
													RetrofitClient.ServiceType.KMA_WEB_FORECASTS, response, responseObj, responseText);
										}

										@Override
										public void onResponseResult(Throwable t) {
											multipleRestApiDownloader.processResult(DataProviderType.KMA_WEB, parameters,
													RetrofitClient.ServiceType.KMA_WEB_FORECASTS, t);
										}
									});
							multipleRestApiDownloader.getCallMap().put(RetrofitClient.ServiceType.KMA_WEB_FORECASTS, forecastsCall);

						}
					}


				});
	}

}
