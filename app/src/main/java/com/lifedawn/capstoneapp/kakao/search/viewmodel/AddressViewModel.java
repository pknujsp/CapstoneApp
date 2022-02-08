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

public class AddressViewModel extends ViewModel {
	private LiveData<PagedList<AddressResponse.Documents>> pagedListLiveData;
	private AddressItemDataSourceFactory dataSourceFactory;
	private MutableLiveData<AddressItemDataSource> dataSourceMutableLiveData;
	private Executor executor;
	
	public AddressViewModel() {
	}
	
	public void init(LocalApiPlaceParameter addressParameter) {
		dataSourceFactory = new AddressItemDataSourceFactory(addressParameter);
		dataSourceMutableLiveData = dataSourceFactory.getLiveData();
		
		PagedList.Config config = (new PagedList.Config.Builder()).setEnablePlaceholders(false).setInitialLoadSizeHint(
				Integer.parseInt(LocalApiPlaceParameter.DEFAULT_SIZE) * 2).setPageSize(15).setPrefetchDistance(4).build();
		executor = Executors.newSingleThreadExecutor();
		pagedListLiveData = new LivePagedListBuilder<Integer, AddressResponse.Documents>(dataSourceFactory, config).setFetchExecutor(
				executor).build();
	}
	
	
	public LiveData<PagedList<AddressResponse.Documents>> getPagedListMutableLiveData() {
		return pagedListLiveData;
	}
}