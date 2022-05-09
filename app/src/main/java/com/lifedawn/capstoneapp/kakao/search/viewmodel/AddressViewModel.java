package com.lifedawn.capstoneapp.kakao.search.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.lifedawn.capstoneapp.kakao.search.datasource.AddressItemDataSource;
import com.lifedawn.capstoneapp.kakao.search.datasourcefactory.AddressItemDataSourceFactory;
import com.lifedawn.capstoneapp.retrofits.parameters.LocalApiPlaceParameter;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.address.AddressResponse;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AddressViewModel extends KakaoLocalApiViewModel<AddressResponse.Documents> {

	@Override
	public void init(LocalApiPlaceParameter placeParameter, PagedList.BoundaryCallback<AddressResponse.Documents> boundaryCallback) {
		dataSourceFactory = new AddressItemDataSourceFactory(placeParameter);
		dataSourceMutableLiveData = dataSourceFactory.getLiveData();

		PagedList.Config config = (new PagedList.Config.Builder()).setEnablePlaceholders(false).setInitialLoadSizeHint(
				Integer.parseInt(LocalApiPlaceParameter.DEFAULT_SIZE) * 2).setPageSize(15).setPrefetchDistance(4).build();
		pagedListLiveData = new LivePagedListBuilder<Integer, AddressResponse.Documents>(dataSourceFactory, config).setFetchExecutor(
				Executors.newSingleThreadExecutor()).build();
	}

}