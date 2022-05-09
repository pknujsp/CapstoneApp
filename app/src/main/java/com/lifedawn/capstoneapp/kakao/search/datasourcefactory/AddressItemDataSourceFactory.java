package com.lifedawn.capstoneapp.kakao.search.datasourcefactory;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;

import com.lifedawn.capstoneapp.kakao.search.datasource.AddressItemDataSource;
import com.lifedawn.capstoneapp.retrofits.parameters.LocalApiPlaceParameter;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.address.AddressResponse;

public class AddressItemDataSourceFactory extends KakaoLocalApiDataSourceFactory< AddressResponse.Documents> {

	public AddressItemDataSourceFactory(LocalApiPlaceParameter placeParameter) {
		super(placeParameter);
	}

	@NonNull
	@Override
	public DataSource<Integer, AddressResponse.Documents> create() {
		dataSource = new AddressItemDataSource(placeParameter);
		liveData.postValue(dataSource);
		return dataSource;
	}
}