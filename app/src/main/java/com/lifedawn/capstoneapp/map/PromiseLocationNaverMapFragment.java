package com.lifedawn.capstoneapp.map;

import android.graphics.Color;
import android.graphics.PointF;
import android.location.Location;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.common.constants.Constant;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.KakaoLocalDocument;
import com.lifedawn.capstoneapp.weather.WeatherInfoFragment;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;

import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;

public class PromiseLocationNaverMapFragment extends AbstractNaverMapFragment {
	private LocationDto selectedLocationDtoInEvent;
	private Marker selectedLocationInEventMarker;
	private InfoWindow selectedLocationInEventInfoWindow;
	private Bundle bundle;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			bundle = savedInstanceState;
		} else {
			bundle = getArguments();
		}

		selectedLocationDtoInEvent = (LocationDto) bundle.getSerializable("locationDto");
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putAll(bundle);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		binding.weatherChip.setVisibility(View.VISIBLE);
		binding.weatherChip.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//날씨 표현
				WeatherInfoFragment weatherInfoFragment = new WeatherInfoFragment();

				Bundle infoBundle = new Bundle();
				infoBundle.putSerializable("locationDto", selectedLocationDtoInEvent);
				infoBundle.putSerializable("promiseDateTime", bundle.getSerializable("promiseDateTime"));
				weatherInfoFragment.setArguments(infoBundle);

				weatherInfoFragment.show(getChildFragmentManager(), WeatherInfoFragment.class.getName());
			}
		});

		binding.promiseLocationChip.setVisibility(View.VISIBLE);
		binding.promiseLocationChip.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				moveCameraToPromiseLocation();
			}
		});
	}

	@Override
	protected LocationDto getPromiseLocationDto() {
		return selectedLocationDtoInEvent;
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
		createSelectedLocationMarker();
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

	@Override
	public void onClickedPlaceBottomSheet(KakaoLocalDocument kakaoLocalDocument) {

	}


	private void createSelectedLocationMarker() {
		LatLng latLng = new LatLng(Double.parseDouble(selectedLocationDtoInEvent.getLatitude()),
				Double.parseDouble(selectedLocationDtoInEvent.getLongitude()));

		final int markerWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36f, getResources().getDisplayMetrics());
		final int markerHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 42f, getResources().getDisplayMetrics());

		if (selectedLocationInEventMarker != null) {
			selectedLocationInEventMarker.setMap(null);
		}

		selectedLocationInEventMarker = new Marker(latLng);
		selectedLocationInEventMarker.setMap(naverMap);
		selectedLocationInEventMarker.setWidth(markerWidth);
		selectedLocationInEventMarker.setHeight(markerHeight);
		//selectedLocationInEventMarker.setIcon(OverlayImage.fromResource(R.drawable.current_location_icon));
		selectedLocationInEventMarker.setForceShowIcon(true);
		selectedLocationInEventMarker.setCaptionColor(Color.BLUE);
		selectedLocationInEventMarker.setCaptionHaloColor(Color.rgb(200, 255, 200));
		selectedLocationInEventMarker.setCaptionTextSize(12f);


		selectedLocationInEventMarker.setOnClickListener(new Overlay.OnClickListener() {
			@Override
			public boolean onClick(@NonNull Overlay overlay) {
				if (selectedLocationInEventInfoWindow.getMarker() == null) {
					selectedLocationInEventInfoWindow.open(selectedLocationInEventMarker);
					selectedLocationInEventMarker.setCaptionText(getString(R.string.message_click_marker_to_delete));
				} else {
					selectedLocationInEventInfoWindow.close();
					selectedLocationInEventMarker.setCaptionText("");
				}
				return true;
			}
		});

		selectedLocationInEventInfoWindow = new InfoWindow();
		selectedLocationInEventInfoWindow.setAdapter(new InfoWindow.DefaultTextAdapter(getContext()) {
			@NonNull
			@Override
			public CharSequence getText(@NonNull InfoWindow infoWindow) {
				StringBuilder stringBuilder = new StringBuilder();
				stringBuilder.append(getString(R.string.promise_location));
				stringBuilder.append("\n");
				if (selectedLocationDtoInEvent.getLocationType() == Constant.PLACE) {
					stringBuilder.append(getString(R.string.place));
					stringBuilder.append(" : ");
					stringBuilder.append(selectedLocationDtoInEvent.getPlaceName());
					stringBuilder.append("\n");
					stringBuilder.append(getString(R.string.address));
					stringBuilder.append(" : ");
					stringBuilder.append(selectedLocationDtoInEvent.getAddressName());
				} else {
					stringBuilder.append(getString(R.string.address));
					stringBuilder.append(" : ");
					stringBuilder.append(selectedLocationDtoInEvent.getAddressName());
				}
				return stringBuilder.toString();
			}
		});

		selectedLocationInEventMarker.performClick();
		moveCameraToPromiseLocation();
	}

	private void moveCameraToPromiseLocation() {
		LatLng latLng = new LatLng(Double.parseDouble(selectedLocationDtoInEvent.getLatitude()),
				Double.parseDouble(selectedLocationDtoInEvent.getLongitude()));
		CameraUpdate cameraUpdate = CameraUpdate.scrollTo(latLng);
		naverMap.moveCamera(cameraUpdate);
	}
}
