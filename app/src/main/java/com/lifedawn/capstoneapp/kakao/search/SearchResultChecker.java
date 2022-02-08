package com.lifedawn.capstoneapp.kakao.search;

import com.lifedawn.capstoneapp.common.interfaces.HttpCallback;
import com.lifedawn.capstoneapp.retrofits.Queries;
import com.lifedawn.capstoneapp.retrofits.RetrofitClient;
import com.lifedawn.capstoneapp.retrofits.parameters.LocalApiPlaceParameter;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.KakaoLocalResponse;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.address.AddressResponse;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.place.PlaceResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchResultChecker {
	private SearchResultChecker() {
	}
	
	public static void checkAddress(LocalApiPlaceParameter localApiPlaceParameter, HttpCallback<KakaoLocalResponse> callback) {
		Queries queries = RetrofitClient.getApiService(RetrofitClient.KAKAO_LOCAL);
		Map<String, String> queryMap = localApiPlaceParameter.getParameterMap();
		Call<AddressResponse> call = queries.getAddress(queryMap);
		
		call.enqueue(new Callback<AddressResponse>() {
			@Override
			public void onResponse(Call<AddressResponse> call, Response<AddressResponse> response) {
				callback.processResult(response);
			}
			
			@Override
			public void onFailure(Call<AddressResponse> call, Throwable t) {
				callback.processResult(t);
			}
		});
	}
	
	public static void checkPlace(LocalApiPlaceParameter localApiPlaceParameter, HttpCallback<KakaoLocalResponse> callback) {
		Queries queries = RetrofitClient.getApiService(RetrofitClient.KAKAO_LOCAL);
		Map<String, String> queryMap = localApiPlaceParameter.getParameterMap();
		Call<PlaceResponse> call = queries.getPlaceKeyword(queryMap);
		
		call.enqueue(new Callback<PlaceResponse>() {
			@Override
			public void onResponse(Call<PlaceResponse> call, Response<PlaceResponse> response) {
				callback.processResult(response);
			}
			
			@Override
			public void onFailure(Call<PlaceResponse> call, Throwable t) {
				callback.processResult(t);
			}
		});
	}
	
	
	public static void checkExisting(LocalApiPlaceParameter addressParameter, LocalApiPlaceParameter placeParameter,
			HttpCallback<List<KakaoLocalResponse>> callback) {
		
		final int requestCount = 2;
		HttpCallback<KakaoLocalResponse> primaryCallback = new HttpCallback<KakaoLocalResponse>() {
			int responseCount = 0;
			List<KakaoLocalResponse> kakaoLocalResponseList = new ArrayList<>();
			List<Exception> exceptionList = new ArrayList<>();
			
			@Override
			public void onResponseSuccessful(KakaoLocalResponse result) {
				++responseCount;
				kakaoLocalResponseList.add(result);
				onCompleted();
			}
			
			@Override
			public void onResponseFailed(Exception e) {
				++responseCount;
				exceptionList.add(e);
				onCompleted();
			}
			
			private void onCompleted() {
				if (requestCount == responseCount) {
					if (!exceptionList.isEmpty()) {
						callback.onResponseFailed(new Exception());
					} else {
						int succeed = 0;
						
						for (KakaoLocalResponse kakaoLocalResponse : kakaoLocalResponseList) {
							if (kakaoLocalResponse instanceof PlaceResponse) {
								PlaceResponse placeResponse = (PlaceResponse) kakaoLocalResponse;
								if (!placeResponse.getPlaceDocuments().isEmpty()) {
									succeed++;
								}
							} else if (kakaoLocalResponse instanceof AddressResponse) {
								AddressResponse addressResponse = (AddressResponse) kakaoLocalResponse;
								if (!addressResponse.getDocumentsList().isEmpty()) {
									succeed++;
								}
							}
							
						}
						
						if (succeed >= 1) {
							callback.onResponseSuccessful(kakaoLocalResponseList);
						} else {
							callback.onResponseFailed(new Exception());
						}
					}
				}
			}
		};
		
		checkAddress(addressParameter, primaryCallback);
		checkPlace(placeParameter, primaryCallback);
	}
}
