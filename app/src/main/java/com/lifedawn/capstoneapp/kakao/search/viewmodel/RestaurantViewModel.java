package com.lifedawn.capstoneapp.kakao.search.viewmodel;

import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.lifedawn.capstoneapp.kakao.search.datasourcefactory.RestaurantDataSourceFactory;
import com.lifedawn.capstoneapp.retrofits.parameters.LocalApiPlaceParameter;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.place.PlaceResponse;

import java.util.concurrent.Executors;

public class RestaurantViewModel extends KakaoLocalApiViewModel<PlaceResponse.Documents> {

	@Override
	public void init(LocalApiPlaceParameter placeParameter, PagedList.BoundaryCallback<PlaceResponse.Documents> boundaryCallback) {
		dataSourceFactory = new RestaurantDataSourceFactory(placeParameter);
		dataSourceMutableLiveData = dataSourceFactory.getLiveData();

		config = (new PagedList.Config.Builder())
				.setEnablePlaceholders(false)
				.setInitialLoadSizeHint(Integer.parseInt(LocalApiPlaceParameter.DEFAULT_SIZE) * 2)
				.setPageSize(15)
				.setPrefetchDistance(4)
				.build();

		pagedListLiveData = new LivePagedListBuilder<Integer, PlaceResponse.Documents>(dataSourceFactory, config)
				.setBoundaryCallback(boundaryCallback)
				.setFetchExecutor(Executors.newSingleThreadExecutor())
				.build();
	}


}