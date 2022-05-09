package com.lifedawn.capstoneapp.kakao.search.datasource;

import androidx.annotation.NonNull;

import com.lifedawn.capstoneapp.retrofits.parameters.LocalApiPlaceParameter;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.place.PlaceResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RestaurantDataSource extends KakaoLocalApiDataSource<PlaceResponse.Documents> {
	private final String NECESSARY_CATEGORY_NAME = "음식점";

	private PlaceResponse.Meta placeMeta;
	private String categoryName;

	public RestaurantDataSource(LocalApiPlaceParameter localApiParameter) {
		super(localApiParameter);
	}


	@Override
	public void loadInitial(@NonNull LoadInitialParams params, @NonNull LoadInitialCallback<PlaceResponse.Documents> callback) {
		super.loadInitial(params, callback);

		Map<String, String> queryMap = localApiPlaceParameter.getParameterMap();
		Call<PlaceResponse> call = queries.getPlaceKeyword(queryMap);

		call.enqueue(new Callback<PlaceResponse>() {
			@Override
			public void onResponse(Call<PlaceResponse> call, Response<PlaceResponse> response) {
				List<PlaceResponse.Documents> placeDocuments = null;
				if (response.body() == null) {
					placeDocuments = new ArrayList<>();
					placeMeta = new PlaceResponse.Meta();
				} else {
					placeDocuments = response.body().getPlaceDocuments();
					placeMeta = response.body().getPlaceMeta();
				}
				List<PlaceResponse.Documents> classifiedList = classifyRestaurants(placeDocuments);
				callback.onResult(classifiedList, 0, classifiedList.size());

			}

			@Override
			public void onFailure(Call<PlaceResponse> call, Throwable t) {
				List<PlaceResponse.Documents> placeDocuments = new ArrayList<>();
				placeMeta = new PlaceResponse.Meta();
				callback.onResult(placeDocuments, 0, placeDocuments.size());
			}
		});
	}

	@Override
	public void loadRange(@NonNull LoadRangeParams params, @NonNull LoadRangeCallback<PlaceResponse.Documents> callback) {
		super.loadRange(params, callback);

		if (!placeMeta.isEnd()) {
			localApiPlaceParameter.setPage(Integer.toString(Integer.parseInt(localApiPlaceParameter.getPage()) + 1));
			Map<String, String> queryMap = localApiPlaceParameter.getParameterMap();
			Call<PlaceResponse> call = queries.getPlaceKeyword(queryMap);

			call.enqueue(new Callback<PlaceResponse>() {
				@Override
				public void onResponse(Call<PlaceResponse> call, Response<PlaceResponse> response) {
					List<PlaceResponse.Documents> placeDocuments = response.body().getPlaceDocuments();
					placeMeta = response.body().getPlaceMeta();

					List<PlaceResponse.Documents> classifiedList = classifyRestaurants(placeDocuments);
					callback.onResult(classifiedList);
				}

				@Override
				public void onFailure(Call<PlaceResponse> call, Throwable t) {
					List<PlaceResponse.Documents> placeDocuments = new ArrayList<>();
					callback.onResult(placeDocuments);
				}
			});
		} else {
			callback.onResult(new ArrayList<PlaceResponse.Documents>(0));
		}
	}

	private List<PlaceResponse.Documents> classifyRestaurants(List<PlaceResponse.Documents> placeDocumentsList) {
		List<PlaceResponse.Documents> classifiedList = new ArrayList<>();

		for (PlaceResponse.Documents placeDocument : placeDocumentsList) {
			categoryName = placeDocument.getCategoryName().split(" > ")[0];
			if (categoryName.equals(NECESSARY_CATEGORY_NAME)) {
				classifiedList.add(placeDocument);
			}
		}

		return classifiedList;
	}
}
