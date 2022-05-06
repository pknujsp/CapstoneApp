package com.lifedawn.capstoneapp.retrofits;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitClient {
	public static final String KAKAO_APP_KEY = "KakaoAK 7c9ce45e6c29183f85f43ad31833c902";
	public static final String KAKAO_LOCAL_API_URL = "https://dapi.kakao.com/v2/local/";
	public static final String KMA_CURRENT_CONDITIONS_AND_HOURLY_AND_DAILY_FORECAST_URL = "https://www.weather.go.kr/w/wnuri-fct2021/main/";
	public static final String NAVER_DIRECTIONS_URL = "https://naveropenapi.apigw.ntruss.com/map-direction/v1/";
	public static final String X_NCP_APIGW_API_KEY = "QEYTkiogo2sREZMXir3XP4eKGrrIAZFE5WdmhMUh";
	public static final String X_NCP_APIGW_API_KEY_ID = "90mygyptxv";

	private static OkHttpClient client = new OkHttpClient.Builder().connectTimeout(3, TimeUnit.SECONDS).readTimeout(
			Duration.ofSeconds(5)).build();

	public enum ServiceType {
		KMA_WEB_CURRENT_CONDITIONS, KMA_WEB_FORECASTS, KAKAO_LOCAL, NAVER_DIRECTIONS
	}

	public static synchronized Queries getApiService(ServiceType serviceType) {
		switch (serviceType) {
			case KAKAO_LOCAL: {
				Gson gson = new GsonBuilder().setLenient().create();

				Retrofit kakaoInstance = new Retrofit.Builder().client(client).addConverterFactory(
						GsonConverterFactory.create(gson)).baseUrl(KAKAO_LOCAL_API_URL).build();

				return kakaoInstance.create(Queries.class);
			}

			case KMA_WEB_CURRENT_CONDITIONS:
			case KMA_WEB_FORECASTS:
				Retrofit kmaHtmlInstance = new Retrofit.Builder().client(client).addConverterFactory(ScalarsConverterFactory.create())
						.baseUrl(KMA_CURRENT_CONDITIONS_AND_HOURLY_AND_DAILY_FORECAST_URL).build();
				return kmaHtmlInstance.create(Queries.class);

			case NAVER_DIRECTIONS:
				Retrofit directionsInstance = new Retrofit.Builder().client(client).addConverterFactory(ScalarsConverterFactory.create())
						.baseUrl(NAVER_DIRECTIONS_URL).build();
				return directionsInstance.create(Queries.class);

			default:
				return null;

		}
	}

}