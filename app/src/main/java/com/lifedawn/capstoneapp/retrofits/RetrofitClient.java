package com.lifedawn.capstoneapp.retrofits;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
	public static final String KAKAO_APP_KEY = "KakaoAK 7c9ce45e6c29183f85f43ad31833c902";
	private static final String KAKAO_LOCAL_API_URL = "https://dapi.kakao.com/v2/local/";
	
	public static final String DATATYPE = "JSON";
	
	public static final int KAKAO_LOCAL = 0;
	
	private static OkHttpClient client = new OkHttpClient.Builder().connectTimeout(3, TimeUnit.SECONDS).readTimeout(
			Duration.ofSeconds(5)).build();
	
	
	public static synchronized Queries getApiService(int serviceType) {
		switch (serviceType) {
			case KAKAO_LOCAL: {
				Gson gson = new GsonBuilder().setLenient().create();
				
				Retrofit kakaoInstance = new Retrofit.Builder().client(client).addConverterFactory(
						GsonConverterFactory.create(gson)).baseUrl(KAKAO_LOCAL_API_URL).build();
				
				return kakaoInstance.create(Queries.class);
			}
			
			default:
				return null;
			
		}
	}
	
}