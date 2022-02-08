package com.lifedawn.capstoneapp.map;

import android.graphics.PointF;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lifedawn.capstoneapp.kakao.SearchFragment;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.NaverMap;

import org.jetbrains.annotations.NotNull;

public class PromiseNaverMapFragment extends AbstractNaverMapFragment {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		binding.headerFragmentContainer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SearchFragment searchFragment = new SearchFragment();
				searchFragment.show(getChildFragmentManager(), SearchFragment.class.getName());
			}
		});
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	protected void loadMap() {
		super.loadMap();
	}
	
	@Override
	public void onMapReady(@NonNull NaverMap naverMap) {
		super.onMapReady(naverMap);
	}
	
	@Override
	public void onCameraUpdateFinish() {
		super.onCameraUpdateFinish();
	}
	
	@Override
	public void onCameraIdle() {
		super.onCameraIdle();
	}
	
	@Override
	public void onLocationChange(@NonNull Location location) {
		super.onLocationChange(location);
	}
	
	@Override
	public void onMapClick(@NonNull PointF pointF, @NonNull LatLng latLng) {
		super.onMapClick(pointF, latLng);
	}
	
	@Override
	public void onMapLongClick(@NonNull PointF pointF, @NonNull LatLng latLng) {
		super.onMapLongClick(pointF, latLng);
	}
}
