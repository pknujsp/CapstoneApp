package com.lifedawn.capstoneapp.kakao.search.datasourcefactory;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;

import com.lifedawn.capstoneapp.kakao.search.datasource.RestaurantDataSource;
import com.lifedawn.capstoneapp.retrofits.parameters.LocalApiPlaceParameter;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.place.PlaceResponse;

public class RestaurantDataSourceFactory extends KakaoLocalApiDataSourceFactory<PlaceResponse.Documents> {

	public RestaurantDataSourceFactory(LocalApiPlaceParameter placeParameter) {
		super(placeParameter);
	}

	@NonNull
	@Override
	public DataSource<Integer, PlaceResponse.Documents> create() {
		dataSource = new RestaurantDataSource(placeParameter);
		liveData.postValue(dataSource);
		return dataSource;
	}
}