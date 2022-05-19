package com.lifedawn.capstoneapp.kakao.search.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.lifedawn.capstoneapp.kakao.search.datasource.KakaoLocalApiDataSource;
import com.lifedawn.capstoneapp.kakao.search.datasource.PlaceItemDataSource;
import com.lifedawn.capstoneapp.kakao.search.datasourcefactory.KakaoLocalApiDataSourceFactory;
import com.lifedawn.capstoneapp.kakao.search.datasourcefactory.PlaceItemDataSourceFactory;
import com.lifedawn.capstoneapp.retrofits.parameters.LocalApiPlaceParameter;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.place.PlaceResponse;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public abstract class KakaoLocalApiViewModel<T> extends ViewModel {
	protected LiveData<PagedList<T>> pagedListLiveData = new MutableLiveData<>();
	protected KakaoLocalApiDataSourceFactory<T> dataSourceFactory;
	protected MutableLiveData<KakaoLocalApiDataSource<T>> dataSourceMutableLiveData;
	protected PagedList.Config config;
	protected Executor executor = Executors.newFixedThreadPool(5);

	@Override
	protected void onCleared() {
		super.onCleared();
	}

	public abstract void init(LocalApiPlaceParameter placeParameter, PagedList.BoundaryCallback<T> boundaryCallback);

	public final LiveData<PagedList<T>> getPagedListMutableLiveData() {
		return pagedListLiveData;
	}
}
