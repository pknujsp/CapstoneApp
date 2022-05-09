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

public class KakaoLocalApiDataSource<T> extends PositionalDataSource<T> {
	protected Queries queries;
	protected LocalApiPlaceParameter localApiPlaceParameter;

	public KakaoLocalApiDataSource(LocalApiPlaceParameter localApiParameter) {
		this.localApiPlaceParameter = localApiParameter;
	}

	@Override
	public void loadInitial(@NonNull PositionalDataSource.LoadInitialParams params, @NonNull LoadInitialCallback<T> callback) {
		queries = RetrofitClient.getApiService(RetrofitClient.ServiceType.KAKAO_LOCAL);
	}

	@Override
	public void loadRange(@NonNull LoadRangeParams params, @NonNull LoadRangeCallback<T> callback) {
		queries = RetrofitClient.getApiService(RetrofitClient.ServiceType.KAKAO_LOCAL);
	}

}