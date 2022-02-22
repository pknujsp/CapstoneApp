package com.lifedawn.capstoneapp.map;

import static com.lifedawn.capstoneapp.common.constants.Constant.ADDRESS;
import static com.lifedawn.capstoneapp.common.constants.Constant.PLACE;

import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.overlay.Marker;

import org.jetbrains.annotations.NotNull;

public class SelectedLocationSimpleMapFragment extends AbstractSimpleNaverMapFragment {
	private Marker selectedLocationMarker;
	private LocationDto locationDto;


	public void replaceLocation(LocationDto locationDto) {
		this.locationDto = locationDto;
		if (mapFragment != null) {
			showMarkerOfSelectedLocation();
		}
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.headerFragmentContainer.setVisibility(View.GONE);
	}

	@Override
	public void onMapReady(@NonNull NaverMap naverMap) {
		super.onMapReady(naverMap);
		mapFragment.getMapView().setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				return true;
			}
		});
		showMarkerOfSelectedLocation();
	}

	private void showMarkerOfSelectedLocation() {
		if (locationDto == null) {
			if (selectedLocationMarker != null) {
				selectedLocationMarker.setMap(null);
			}
		} else {
			selectedLocationMarker = new Marker();
			String caption = null;

			if (locationDto.getLocationType() == ADDRESS) {
				caption = locationDto.getAddressName();
			} else if (locationDto.getLocationType() == PLACE) {
				caption = locationDto.getPlaceName();
			}

			selectedLocationMarker.setCaptionText(caption);
			selectedLocationMarker.setCaptionColor(Color.BLACK);
			selectedLocationMarker.setPosition(
					new LatLng(Double.parseDouble(locationDto.getLatitude()), Double.parseDouble(locationDto.getLongitude())));

			int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32f, getResources().getDisplayMetrics());
			int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 42f, getResources().getDisplayMetrics());
			selectedLocationMarker.setWidth(width);
			selectedLocationMarker.setHeight(height);

			selectedLocationMarker.setMap(naverMap);
			CameraUpdate cameraUpdate = CameraUpdate.scrollAndZoomTo(selectedLocationMarker.getPosition(), 13);
			naverMap.moveCamera(cameraUpdate);
		}
	}
}
