package com.lifedawn.capstoneapp.weather.request;

import android.content.Context;
import android.util.ArrayMap;

import com.lifedawn.capstoneapp.common.interfaces.BackgroundCallback;
import com.lifedawn.capstoneapp.main.MyApplication;
import com.lifedawn.capstoneapp.retrofits.MultipleRestApiDownloader;
import com.lifedawn.capstoneapp.retrofits.RetrofitClient;
import com.lifedawn.capstoneapp.weather.DataProviderType;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WeatherRequest {
	private WeatherRequest() {
	}

	public static MultipleRestApiDownloader requestWeatherData(Context context, Double latitude, Double longitude,
	                                                           BackgroundCallback<WeatherResponseResult> callback) {
		//메인 날씨 제공사만 요청

		final Set<DataProviderType> dataProviderTypeSet = new HashSet<>();
		dataProviderTypeSet.add(DataProviderType.KMA_WEB);

		ArrayMap<DataProviderType, RequestWeatherSource> requestWeatherSources = new ArrayMap<>();
		setRequestWeatherSourcesWithProvider(dataProviderTypeSet, requestWeatherSources);

		final WeatherResponseResult weatherResponseResult = new WeatherResponseResult(dataProviderTypeSet,
				requestWeatherSources, DataProviderType.KMA_WEB);

		final MultipleRestApiDownloader multipleRestApiDownloader = new MultipleRestApiDownloader() {
			@Override
			public void onResult() {
				weatherResponseResult.multipleRestApiDownloader = this;

				Set<Map.Entry<DataProviderType, ArrayMap<RetrofitClient.ServiceType, MultipleRestApiDownloader.ResponseResult>>> entrySet = weatherResponseResult.multipleRestApiDownloader.getResponseMap().entrySet();
				//메인 날씨 제공사의 데이터가 정상이면 메인 날씨 제공사의 프래그먼트들을 설정하고 값을 표시한다.
				//메인 날씨 제공사의 응답이 불량이면 재 시도, 취소 중 택1 다이얼로그 표시
				for (Map.Entry<DataProviderType, ArrayMap<RetrofitClient.ServiceType, MultipleRestApiDownloader.ResponseResult>> entry : entrySet) {

					for (MultipleRestApiDownloader.ResponseResult responseResult : entry.getValue().values()) {
						if (!responseResult.isSuccessful()) {
							//failed
							callback.onResultFailed(new Exception("날씨 데이터 불러오기 실패"));
							return;
						}
					}

				}
				//응답 성공 하면
				final WeatherResponseResult weatherResponseObj = new WeatherResponseResult(
						weatherResponseResult.multipleRestApiDownloader,
						weatherResponseResult.dataProviderTypeSet, weatherResponseResult.mainDataProviderType);

				weatherResponseObj.setLatitude(latitude).setLongitude(longitude);

				callback.onResultSuccessful(weatherResponseObj);
			}

			@Override
			public void onCanceled() {

			}
		};

		int totalRequestCount = 0;
		for (RequestWeatherSource requestWeatherSource : requestWeatherSources.values()) {
			totalRequestCount += requestWeatherSource.getRequestServiceTypes().size();
		}
		multipleRestApiDownloader.setRequestCount(totalRequestCount);
		MyApplication.EXECUTOR_SERVICE.execute(new Runnable() {
			@Override
			public void run() {
				if (requestWeatherSources.containsKey(DataProviderType.KMA_WEB)) {
					KmaProcessing.requestWeatherDataAsWEB(context, latitude, longitude, (RequestKma) requestWeatherSources.get(DataProviderType.KMA_WEB),
							multipleRestApiDownloader);
				}

			}
		});

		return multipleRestApiDownloader;
	}

	private static void setRequestWeatherSourcesWithProvider(Set<DataProviderType> dataProviderTypeSet,
	                                                         ArrayMap<DataProviderType, RequestWeatherSource> newRequestWeatherSources) {
		if (dataProviderTypeSet.contains(DataProviderType.KMA_WEB)) {
			RequestKma requestKma = new RequestKma();
			requestKma.addRequestServiceType(RetrofitClient.ServiceType.KMA_WEB_CURRENT_CONDITIONS).addRequestServiceType(
					RetrofitClient.ServiceType.KMA_WEB_FORECASTS);
			newRequestWeatherSources.put(DataProviderType.KMA_WEB, requestKma);
		}

	}


	public static class WeatherResponseResult implements Serializable {
		MultipleRestApiDownloader multipleRestApiDownloader;
		Set<DataProviderType> dataProviderTypeSet;
		DataProviderType mainDataProviderType;
		Double latitude;
		Double longitude;
		ArrayMap<DataProviderType, RequestWeatherSource> requestWeatherSources;

		public WeatherResponseResult(Set<DataProviderType> dataProviderTypeSet,
		                             ArrayMap<DataProviderType, RequestWeatherSource> requestWeatherSources, DataProviderType mainDataProviderType) {
			this.dataProviderTypeSet = dataProviderTypeSet;
			this.requestWeatherSources = requestWeatherSources;
			this.mainDataProviderType = mainDataProviderType;
		}

		public WeatherResponseResult(MultipleRestApiDownloader multipleRestApiDownloader, Set<DataProviderType> dataProviderTypeSet, DataProviderType mainDataProviderType) {
			this.multipleRestApiDownloader = multipleRestApiDownloader;
			this.dataProviderTypeSet = dataProviderTypeSet;
			this.mainDataProviderType = mainDataProviderType;
		}

		public Double getLatitude() {
			return latitude;
		}

		public WeatherResponseResult setLatitude(Double latitude) {
			this.latitude = latitude;
			return this;
		}

		public Double getLongitude() {
			return longitude;
		}

		public WeatherResponseResult setLongitude(Double longitude) {
			this.longitude = longitude;
			return this;
		}

		public MultipleRestApiDownloader getMultipleRestApiDownloader() {
			return multipleRestApiDownloader;
		}

		public Set<DataProviderType> getWeatherProviderTypeSet() {
			return dataProviderTypeSet;
		}

		public DataProviderType getMainWeatherProviderType() {
			return mainDataProviderType;
		}

		public ArrayMap<DataProviderType, RequestWeatherSource> getRequestWeatherSources() {
			return requestWeatherSources;
		}
	}
}
