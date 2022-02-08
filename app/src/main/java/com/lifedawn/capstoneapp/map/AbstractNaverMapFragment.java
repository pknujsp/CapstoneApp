package com.lifedawn.capstoneapp.map;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.common.constants.SharedPreferenceConstant;
import com.lifedawn.capstoneapp.common.util.FusedLocation;
import com.lifedawn.capstoneapp.common.util.LocationLifeCycleObserver;
import com.lifedawn.capstoneapp.databinding.FragmentAbstractNaverMapBinding;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.NaverMapOptions;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.LocationOverlay;
import com.naver.maps.map.util.FusedLocationSource;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public abstract class AbstractNaverMapFragment extends Fragment implements OnMapReadyCallback, NaverMap.OnMapClickListener, NaverMap.OnCameraIdleListener, CameraUpdate.FinishCallback, NaverMap.OnLocationChangeListener, NaverMap.OnMapLongClickListener {
	private static final int PERMISSION_REQUEST_CODE = 1;
	private static final int REQUEST_CODE_LOCATION = 2;
	protected FragmentAbstractNaverMapBinding binding;
	protected NaverMap naverMap;
	protected MapFragment mapFragment;
	protected FusedLocationSource fusedLocationSource;
	protected LocationLifeCycleObserver locationLifeCycleObserver;
	protected FusedLocation fusedLocation;
	protected MapViewModel mapViewModel;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		locationLifeCycleObserver = new LocationLifeCycleObserver(requireActivity().getActivityResultRegistry(), requireActivity());
		getLifecycle().addObserver(locationLifeCycleObserver);
		fusedLocation = FusedLocation.getInstance(getContext());
		mapViewModel = new ViewModelProvider(getActivity()).get(MapViewModel.class);
		mapViewModel.setiMapPoint(new MapViewModel.IMapPoint() {
			@Override
			public LatLng getCenterPoint() {
				return naverMap.getContentBounds().getCenter();
			}
		});
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
		
		binding.naverMapButtonsLayout.gpsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (naverMap.getLocationTrackingMode() == LocationTrackingMode.None) {
					//check permissions
					
					if (!fusedLocation.isOnGps()) {
						fusedLocation.onDisabledGps(getActivity(), locationLifeCycleObserver, new ActivityResultCallback<ActivityResult>() {
							@Override
							public void onActivityResult(ActivityResult result) {
								if (fusedLocation.isOnGps()) {
									binding.naverMapButtonsLayout.gpsButton.callOnClick();
								}
							}
						});
						return;
					}
					
					if (fusedLocation.checkDefaultPermissions()) {
						naverMap.setLocationSource(fusedLocationSource);
						naverMap.setLocationTrackingMode(LocationTrackingMode.NoFollow);
					} else {
						naverMap.setLocationSource(null);
						locationLifeCycleObserver.launchPermissionsLauncher(
								new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
								new ActivityResultCallback<Map<String, Boolean>>() {
									@Override
									public void onActivityResult(Map<String, Boolean> result) {
										if (!result.containsValue(false)) {
											fusedLocationSource.onRequestPermissionsResult(REQUEST_CODE_LOCATION,
													new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
															Manifest.permission.ACCESS_COARSE_LOCATION},
													new int[]{PackageManager.PERMISSION_GRANTED, PackageManager.PERMISSION_GRANTED});
											naverMap.setLocationSource(fusedLocationSource);
											naverMap.setLocationTrackingMode(LocationTrackingMode.NoFollow);
										} else {
											Toast.makeText(getActivity(), getString(R.string.message_needs_location_permission),
													Toast.LENGTH_SHORT).show();
										}
									}
								});
					}
				}
			}
		});
		
		loadMap();
	}
	
	@Override
	public void onDestroy() {
		if (naverMap != null) {
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
			SharedPreferences.Editor editor = preferences.edit();
			
			LatLng lastLatLng = naverMap.getCameraPosition().target;
			editor.putString(SharedPreferenceConstant.LAST_LONGITUDE.name(), String.valueOf(lastLatLng.longitude));
			editor.putString(SharedPreferenceConstant.LAST_LATITUDE.name(), String.valueOf(lastLatLng.latitude));
			editor.apply();
		}
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
			
			fusedLocationSource = new FusedLocationSource(this, PERMISSION_REQUEST_CODE);
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
		LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
		
		CameraUpdate cameraUpdate = CameraUpdate.scrollTo(latLng);
		naverMap.moveCamera(cameraUpdate);
		naverMap.setLocationSource(null);
		
		LocationOverlay locationOverlay = naverMap.getLocationOverlay();
		locationOverlay.setVisible(true);
		locationOverlay.setPosition(latLng);
	}
	
	@Override
	public void onMapClick(@NonNull PointF pointF, @NonNull LatLng latLng) {
	
	}
	
	@Override
	public void onMapLongClick(@NonNull PointF pointF, @NonNull LatLng latLng) {
	
	}
}