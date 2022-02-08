package com.lifedawn.capstoneapp.kakao.search.datasource;

import androidx.annotation.NonNull;
import androidx.paging.PositionalDataSource;

import com.lifedawn.capstoneapp.retrofits.Queries;
import com.lifedawn.capstoneapp.retrofits.RetrofitClient;
import com.lifedawn.capstoneapp.retrofits.parameters.LocalApiPlaceParameter;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.place.PlaceResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlaceItemDataSource extends PositionalDataSource<PlaceResponse.Documents> {
	private Queries queries;
	private PlaceResponse.Meta placeMeta;
	private LocalApiPlaceParameter localApiPlaceParameter;
	
	public PlaceItemDataSource(LocalApiPlaceParameter localApiParameter) {
		this.localApiPlaceParameter = localApiParameter;
	}
	
	@Override
	public void loadInitial(@NonNull LoadInitialParams params, @NonNull LoadInitialCallback<PlaceResponse.Documents> callback) {
		queries = RetrofitClient.getApiService(RetrofitClient.KAKAO_LOCAL);
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
				callback.onResult(placeDocuments, 0, placeDocuments.size());
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
		queries = RetrofitClient.getApiService(RetrofitClient.KAKAO_LOCAL);
		
		if (!placeMeta.isEnd()) {
			localApiPlaceParameter.setPage(Integer.toString(Integer.parseInt(localApiPlaceParameter.getPage()) + 1));
			Map<String, String> queryMap = localApiPlaceParameter.getParameterMap();
			Call<PlaceResponse> call = queries.getPlaceKeyword(queryMap);
			
			call.enqueue(new Callback<PlaceResponse>() {
				@Override
				public void onResponse(Call<PlaceResponse> call, Response<PlaceResponse> response) {
					List<PlaceResponse.Documents> placeDocuments = response.body().getPlaceDocuments();
					placeMeta = response.body().getPlaceMeta();
					
					callback.onResult(placeDocuments);
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
	
}