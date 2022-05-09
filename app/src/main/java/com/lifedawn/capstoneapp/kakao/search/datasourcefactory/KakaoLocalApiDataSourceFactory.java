package com.lifedawn.capstoneapp.kakao.search.datasourcefactory;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;

import com.lifedawn.capstoneapp.kakao.search.datasource.KakaoLocalApiDataSource;
import com.lifedawn.capstoneapp.kakao.search.datasource.PlaceItemDataSource;
import com.lifedawn.capstoneapp.retrofits.parameters.LocalApiPlaceParameter;

public class KakaoLocalApiDataSourceFactory<T> extends DataSource.Factory<Integer, T> {
	protected KakaoLocalApiDataSource<T> dataSource;
	protected MutableLiveData<KakaoLocalApiDataSource<T>> liveData;
	protected LocalApiPlaceParameter placeParameter;

	public KakaoLocalApiDataSourceFactory(LocalApiPlaceParameter placeParameter) {
		liveData = new MutableLiveData<>();
		this.placeParameter = placeParameter;
	}


	public final MutableLiveData<KakaoLocalApiDataSource<T>> getLiveData() {
		return liveData;
	}

	@NonNull
	@Override
	public DataSource<Integer, T> create() {
		return null;
	}
}