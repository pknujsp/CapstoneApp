package com.lifedawn.capstoneapp.kakao.search.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.lifedawn.capstoneapp.kakao.search.datasource.PlaceItemDataSource;
import com.lifedawn.capstoneapp.kakao.search.datasourcefactory.PlaceItemDataSourceFactory;
import com.lifedawn.capstoneapp.retrofits.parameters.LocalApiPlaceParameter;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.place.PlaceResponse;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PlacesViewModel extends ViewModel {
	private LiveData<PagedList<PlaceResponse.Documents>> pagedListLiveData;
	private PlaceItemDataSourceFactory dataSourceFactory;
	private MutableLiveData<PlaceItemDataSource> dataSourceMutableLiveData;
	private Executor executor;
	private PagedList.Config config;
	
	public PlacesViewModel() {
		executor = Executors.newSingleThreadExecutor();
		pagedListLiveData = new MutableLiveData<>();
	}
	
	public void init(LocalApiPlaceParameter placeParameter, PagedList.BoundaryCallback<PlaceResponse.Documents> boundaryCallback) {
		dataSourceFactory = new PlaceItemDataSourceFactory(placeParameter);
		dataSourceMutableLiveData = dataSourceFactory.getLiveData();
		
		config = (new PagedList.Config.Builder()).setEnablePlaceholders(false).setInitialLoadSizeHint(
				Integer.parseInt(LocalApiPlaceParameter.DEFAULT_SIZE) * 2).setPageSize(15).setPrefetchDistance(4).build();
		
		pagedListLiveData = new LivePagedListBuilder<Integer, PlaceResponse.Documents>(dataSourceFactory, config).setBoundaryCallback(
				boundaryCallback).setFetchExecutor(executor).build();
	}
	
	public LiveData<PagedList<PlaceResponse.Documents>> getPagedListMutableLiveData() {
		return pagedListLiveData;
	}
	
}
