package com.lifedawn.capstoneapp.kakao.search.datasourcefactory;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;

import com.lifedawn.capstoneapp.kakao.search.datasource.AddressItemDataSource;
import com.lifedawn.capstoneapp.retrofits.parameters.LocalApiPlaceParameter;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.address.AddressResponse;

public class AddressItemDataSourceFactory extends DataSource.Factory<Integer, AddressResponse.Documents> {
	private AddressItemDataSource dataSource;
	private MutableLiveData<AddressItemDataSource> liveData;
	private final LocalApiPlaceParameter addressParameter;
	
	public AddressItemDataSourceFactory(LocalApiPlaceParameter addressParameter) {
		liveData = new MutableLiveData<>();
		this.addressParameter = addressParameter;
	}
	
	@NonNull
	@Override
	public DataSource<Integer, AddressResponse.Documents> create() {
		dataSource = new AddressItemDataSource(addressParameter);
		liveData.postValue(dataSource);
		return dataSource;
	}
	
	public MutableLiveData<AddressItemDataSource> getLiveData() {
		return liveData;
	}
}