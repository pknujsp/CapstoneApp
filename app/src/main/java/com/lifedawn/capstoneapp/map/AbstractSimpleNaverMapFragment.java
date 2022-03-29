package com.lifedawn.capstoneapp.map;

import android.content.SharedPreferences;
import android.graphics.PointF;
import android.location.Location;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.lifedawn.capstoneapp.common.constants.SharedPreferenceConstant;
import com.lifedawn.capstoneapp.databinding.FragmentAbstractNaverMapBinding;
import com.lifedawn.capstoneapp.map.places.AroundPlacesFragment;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.NaverMapOptions;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.LocationOverlay;

import org.jetbrains.annotations.NotNull;

public abstract class AbstractSimpleNaverMapFragment extends Fragment implements OnMapReadyCallback, NaverMap.OnMapClickListener,
		NaverMap.OnCameraIdleListener, CameraUpdate.FinishCallback, NaverMap.OnLocationChangeListener, NaverMap.OnMapLongClickListener {


	protected FragmentAbstractNaverMapBinding binding;
	protected NaverMap naverMap;
	protected MapFragment mapFragment;

	protected Integer markerWidth;
	protected Integer markerHeight;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		markerWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24f, getResources().getDisplayMetrics());
		markerHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32f, getResources().getDisplayMetrics());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentAbstractNaverMapBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		binding.naverMapButtonsLayout.zoomInButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				naverMap.moveCamera(CameraUpdate.zoomIn());
			}
		});

		binding.naverMapButtonsLayout.zoomOutButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				naverMap.moveCamera(CameraUpdate.zoomOut());
			}
		});

		binding.naverMapButtonsLayout.gpsButton.setVisibility(View.GONE);

		loadMap();
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
	}


	protected void loadMap() {
		if (mapFragment == null) {
			NaverMapOptions naverMapOptions = new NaverMapOptions();

			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
			LatLng lastLatLng = new LatLng(
					Double.parseDouble(preferences.getString(SharedPreferenceConstant.LAST_LATITUDE.name(), "37.6076585")),
					Double.parseDouble(preferences.getString(SharedPreferenceConstant.LAST_LONGITUDE.name(), "127.0965492")));

			naverMapOptions.scaleBarEnabled(true).locationButtonEnabled(false).compassEnabled(false).zoomControlEnabled(
					false).rotateGesturesEnabled(false).mapType(NaverMap.MapType.Basic).camera(new CameraPosition(lastLatLng, 11));

			mapFragment = MapFragment.newInstance(naverMapOptions);
			getChildFragmentManager().beginTransaction().add(binding.naveMapFragment.getId(), mapFragment,
					MapFragment.class.getName()).commitNow();
		}
		mapFragment.getMapAsync(this);
	}

	@Override
	public void onMapReady(@NonNull NaverMap naverMap) {
		this.naverMap = naverMap;

		NaverMap.MapType currentMapType = NaverMap.MapType.Basic;
		naverMap.setMapType(currentMapType);
		naverMap.addOnLocationChangeListener(this);
		naverMap.addOnCameraIdleListener(this);
		naverMap.setOnMapClickListener(this);
		naverMap.setOnMapLongClickListener(this);
		naverMap.getUiSettings().setZoomControlEnabled(false);

		LocationOverlay locationOverlay = naverMap.getLocationOverlay();
		locationOverlay.setVisible(false);
	}

	@Override
	public void onCameraUpdateFinish() {

	}

	@Override
	public void onCameraIdle() {

	}

	@Override
	public void onLocationChange(@NonNull Location location) {

	}

	@Override
	public void onMapClick(@NonNull PointF pointF, @NonNull LatLng latLng) {

	}

	@Override
	public void onMapLongClick(@NonNull PointF pointF, @NonNull LatLng latLng) {

	}


}
