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

import com.lifedawn.capstoneapp.model.firestore.PlaceDto;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.overlay.Marker;

import org.jetbrains.annotations.NotNull;

public class SelectedLocationSimpleMapFragment extends AbstractSimpleNaverMapFragment {
	private Marker selectedLocationMarker;
	private PlaceDto placeDto;
	private Bundle bundle;

	public void replaceLocation(PlaceDto placeDto) {
		this.placeDto = placeDto;
		if (mapFragment != null) {
			showMarkerOfSelectedLocation();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		bundle = savedInstanceState != null ? savedInstanceState : getArguments();
		if (bundle != null && bundle.containsKey("locationDto")) {
			placeDto = (PlaceDto) bundle.getSerializable("locationDto");
		}
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.headerFragmentContainer.setVisibility(View.GONE);
		binding.funcChipGroup.setVisibility(View.GONE);
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
		replaceLocation(placeDto);
		showMarkerOfSelectedLocation();
	}



	private void showMarkerOfSelectedLocation() {
		if (placeDto == null) {
			if (selectedLocationMarker != null) {
				selectedLocationMarker.setMap(null);
			}
		} else {
			selectedLocationMarker = new Marker();
			String caption = null;

			if (placeDto.getLocationType() == ADDRESS) {
				caption = placeDto.getAddressName();
			} else if (placeDto.getLocationType() == PLACE) {
				caption = placeDto.getPlaceName();
			}

			selectedLocationMarker.setCaptionText(caption);
			selectedLocationMarker.setCaptionColor(Color.BLACK);
			selectedLocationMarker.setPosition(
					new LatLng(Double.parseDouble(placeDto.getLatitude()), Double.parseDouble(placeDto.getLongitude())));

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
