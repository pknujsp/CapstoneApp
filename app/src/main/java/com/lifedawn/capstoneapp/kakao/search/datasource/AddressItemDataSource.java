package com.lifedawn.capstoneapp.kakao.search.datasource;

import androidx.annotation.NonNull;
import androidx.paging.PositionalDataSource;

import com.lifedawn.capstoneapp.retrofits.Queries;
import com.lifedawn.capstoneapp.retrofits.RetrofitClient;
import com.lifedawn.capstoneapp.retrofits.parameters.LocalApiPlaceParameter;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.address.AddressResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddressItemDataSource extends PositionalDataSource<AddressResponse.Documents> {
	private Queries queries;
	private AddressResponse.Meta addressMeta;
	private final LocalApiPlaceParameter localApiPlaceParameter;
	
	public AddressItemDataSource(LocalApiPlaceParameter localApiParameter) {
		this.localApiPlaceParameter = localApiParameter;
	}
	
	@Override
	public void loadInitial(@NonNull LoadInitialParams params, @NonNull LoadInitialCallback<AddressResponse.Documents> callback) {
		queries = RetrofitClient.getApiService(RetrofitClient.KAKAO_LOCAL);
		Map<String, String> queryMap = localApiPlaceParameter.getParameterMap();
		Call<AddressResponse> call = queries.getAddress(queryMap);
		
		call.enqueue(new Callback<AddressResponse>() {
			@Override
			public void onResponse(Call<AddressResponse> call, Response<AddressResponse> response) {
				List<AddressResponse.Documents> addressDocuments = null;
				
				if (response.body() == null) {
					addressDocuments = new ArrayList<>();
					addressMeta = new AddressResponse.Meta();
				} else {
					addressDocuments = response.body().getDocumentsList();
					addressMeta = response.body().getMeta();
				}
				callback.onResult(addressDocuments, 0, addressDocuments.size());
			}
			
			@Override
			public void onFailure(Call<AddressResponse> call, Throwable t) {
				List<AddressResponse.Documents> addressDocuments = new ArrayList<>();
				addressMeta = new AddressResponse.Meta();
				callback.onResult(addressDocuments, 0, addressDocuments.size());
			}
		});
	}
	
	@Override
	public void loadRange(@NonNull LoadRangeParams params, @NonNull LoadRangeCallback<AddressResponse.Documents> callback) {
		queries = RetrofitClient.getApiService(RetrofitClient.KAKAO_LOCAL);
		
		if (!addressMeta.isEnd()) {
			localApiPlaceParameter.setPage(Integer.toString(Integer.parseInt(localApiPlaceParameter.getPage()) + 1));
			Map<String, String> queryMap = localApiPlaceParameter.getParameterMap();
			Call<AddressResponse> call = queries.getAddress(queryMap);
			
			call.enqueue(new Callback<AddressResponse>() {
				@Override
				public void onResponse(Call<AddressResponse> call, Response<AddressResponse> response) {
					List<AddressResponse.Documents> addressDocuments = response.body().getDocumentsList();
					addressMeta = response.body().getMeta();
					callback.onResult(addressDocuments);
					
				}
				
				@Override
				public void onFailure(Call<AddressResponse> call, Throwable t) {
					List<AddressResponse.Documents> addressDocuments = new ArrayList<>();
					callback.onResult(addressDocuments);
					
				}
			});
		} else {
			callback.onResult(new ArrayList<AddressResponse.Documents>(0));
		}
	}
}
