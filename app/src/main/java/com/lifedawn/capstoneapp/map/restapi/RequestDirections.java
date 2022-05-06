package com.lifedawn.capstoneapp.map.restapi;

import com.google.gson.Gson;
import com.lifedawn.capstoneapp.retrofits.JsonDownloader;
import com.lifedawn.capstoneapp.retrofits.MultipleRestApiDownloader;
import com.lifedawn.capstoneapp.retrofits.Queries;
import com.lifedawn.capstoneapp.retrofits.RetrofitClient;
import com.lifedawn.capstoneapp.retrofits.response.naver.directions.DirectionsResponse;
import com.lifedawn.capstoneapp.retrofits.response.naver.directions5.Root;
import com.lifedawn.capstoneapp.weather.DataProviderType;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestDirections {

	private RequestDirections() {
	}

	public static void requestDirections(Point start, Point goal, MultipleRestApiDownloader multipleRestApiDownloader) {
		Queries queries = RetrofitClient.getApiService(RetrofitClient.ServiceType.NAVER_DIRECTIONS);
		Call<String> call = queries.getDirections(start.toString(), goal.toString());

		multipleRestApiDownloader.getCallMap().put(RetrofitClient.ServiceType.NAVER_DIRECTIONS, call);
		multipleRestApiDownloader.setRequestCount(1);
		multipleRestApiDownloader.setResponseCount(0);

		JsonDownloader callback = new JsonDownloader() {
			@Override
			public void onResponseResult(Response<?> response, Object responseObj, String responseText) {
				multipleRestApiDownloader.processResult(DataProviderType.NAVER_DIRECTIONS, null,
						RetrofitClient.ServiceType.NAVER_DIRECTIONS,
						response, responseObj, responseText);
			}

			@Override
			public void onResponseResult(Throwable t) {
				multipleRestApiDownloader.processResult(DataProviderType.NAVER_DIRECTIONS, null,
						RetrofitClient.ServiceType.NAVER_DIRECTIONS, t);
			}
		};

		call.enqueue(new Callback<String>() {
			@Override
			public void onResponse(Call<String> call, Response<String> response) {
				if (response.body() != null) {
					Root directionsResponse = new Gson().fromJson(response.body().toString()
							, Root.class);
					callback.onResponseResult(response, directionsResponse, response.body());
				} else {
					callback.onResponseResult(new Exception());
				}
			}

			@Override
			public void onFailure(Call<String> call, Throwable t) {
				callback.onResponseResult(t);
			}
		});


	}

	public static class Point extends Object {
		public final Float latitude;
		public final Float longitude;

		public Point(Float latitude, Float longitude) {
			this.latitude = latitude;
			this.longitude = longitude;
		}

		@Override
		public String toString() {
			return longitude.toString() + "," + latitude.toString();
		}
	}
}
