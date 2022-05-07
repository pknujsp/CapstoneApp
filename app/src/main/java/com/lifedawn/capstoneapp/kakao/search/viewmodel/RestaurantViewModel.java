package com.lifedawn.capstoneapp.kakao.search.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.lifedawn.capstoneapp.kakao.search.datasource.RestaurantDataSource;
import com.lifedawn.capstoneapp.kakao.search.datasourcefactory.RestaurantDataSourceFactory;
import com.lifedawn.capstoneapp.retrofits.parameters.LocalApiPlaceParameter;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.place.PlaceResponse;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class RestaurantViewModel extends ViewModel {
	private LiveData<PagedList<PlaceResponse.Documents>> pagedListLiveData;
	private RestaurantDataSourceFactory dataSourceFactory;
	private MutableLiveData<RestaurantDataSource> dataSourceMutableLiveData;
	private Executor executor;
	private PagedList.Config config;

	public RestaurantViewModel() {
		executor = Executors.newSingleThreadExecutor();
		//pagedListLiveData = new MutableLiveData<>();
	}

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
				.setFetchExecutor(executor)
				.build();
	}

	public LiveData<PagedList<PlaceResponse.Documents>> getPagedListMutableLiveData() {
		return pagedListLiveData;
	}

}