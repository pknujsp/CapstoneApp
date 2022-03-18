package com.lifedawn.capstoneapp.retrofits;

import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.address.AddressResponse;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.place.PlaceResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.QueryMap;

public interface Queries {
	// kakao local
	@Headers({"Authorization: " + RetrofitClient.KAKAO_APP_KEY})
	@GET("search/address.json")
	Call<AddressResponse> getAddress(@QueryMap(encoded = true) Map<String, String> queryMap);

	@Headers({"Authorization: " + RetrofitClient.KAKAO_APP_KEY})
	@GET("search/keyword.json")
	Call<PlaceResponse> getPlaceKeyword(@QueryMap(encoded = true) Map<String, String> queryMap);

	// kma web html --------------------------------------------------------------------------------------------
	@GET("current-weather.do")
	Call<String> getKmaCurrentConditions(@QueryMap(encoded = true) Map<String, String> queryMap);

	@GET("digital-forecast.do")
	Call<String> getKmaHourlyAndDailyForecast(@QueryMap(encoded = true) Map<String, String> queryMap);

}
