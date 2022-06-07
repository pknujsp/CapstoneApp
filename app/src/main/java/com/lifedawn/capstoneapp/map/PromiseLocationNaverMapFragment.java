package com.lifedawn.capstoneapp.map;

import android.content.Context;
import android.graphics.Color;
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
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.common.constants.Constant;
import com.lifedawn.capstoneapp.map.findroute.FindRouteFragment;
import com.lifedawn.capstoneapp.retrofits.response.naver.directions5.Root;
import com.lifedawn.capstoneapp.weather.WeatherInfoFragment;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.geometry.LatLngBounds;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.overlay.PathOverlay;
import com.naver.maps.map.util.CameraUtils;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PromiseLocationNaverMapFragment extends AbstractNaverMapFragment {
	private LocationDto selectedLocationDtoInEvent;
	private Marker selectedLocationInEventMarker;
	private InfoWindow selectedLocationInEventInfoWindow;
	private Bundle bundle;

	private PathOverlay path;
	private Marker currentMarker;

	private FragmentManager.FragmentLifecycleCallbacks fragmentLifecycleCallbacks = new FragmentManager.FragmentLifecycleCallbacks() {

		@Override
		public void onFragmentAttached(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull Context context) {
			super.onFragmentAttached(fm, f, context);

			if (f instanceof FindRouteFragment) {
				binding.headerLayout.setVisibility(View.GONE);
			}
		}

		@Override
		public void onFragmentDestroyed(@NonNull FragmentManager fm, @NonNull Fragment f) {
			super.onFragmentDestroyed(fm, f);

			if (f instanceof FindRouteFragment) {
				removePath();
				binding.headerLayout.setVisibility(View.VISIBLE);
				setStateOfBottomSheet(BottomSheetType.FIND_ROUTES, BottomSheetBehavior.STATE_COLLAPSED);
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			bundle = savedInstanceState;
		} else {
			bundle = getArguments();
		}

		selectedLocationDtoInEvent = (LocationDto) bundle.getSerializable("locationDto");
		getChildFragmentManager().registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, false);
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
		setFindRoutesBottomSheet();

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

		binding.findRouteChip.setVisibility(View.VISIBLE);
		binding.findRouteChip.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
/*
				String uri = "geo:" +
						selectedLocationDtoInEvent.getLatitude() + "," + selectedLocationDtoInEvent.getLongitude() +
						"?q=" + selectedLocationDtoInEvent.getAddressName();

				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(uri));
				if (intent.resolveActivity(getContext().getPackageManager()) != null) {
					startActivity(intent);
				}

				*/

				binding.findRoutesBottomSheet.progressLayout.onStarted(getString(R.string.calculating_routes));

				FindRouteFragment findRouteFragment = new FindRouteFragment();

				findRouteFragment.setOnFindRouteListener(new FindRouteFragment.OnFindRouteListener() {
					@Override
					public void onResult(Location currentLocation, ArrayList<ArrayList<Double>> path, Root response, LocationDto startLocation, LocationDto goalLocation) {
						if (response == null) {
							binding.findRoutesBottomSheet.progressLayout.onFailed(getString(R.string.failed_finding_routes));
							removePath();
							return;
						}

						binding.findRoutesBottomSheet.departureLocation.setText(startLocation.getAddressName());
						binding.findRoutesBottomSheet.arrivalLocation.setText(goalLocation.getAddressName());

						float distance = Integer.parseInt(response.route.traoptimal.get(0).summary.distance) / 1000f;
						distance = (float) (Math.round(distance * 1000) / 1000.0);

						binding.findRoutesBottomSheet.distance.setText(new String(distance + "km"));

						long duration = Long.parseLong(response.route.traoptimal.get(0).summary.duration);
						duration = TimeUnit.MILLISECONDS.toMinutes(duration);

						ZonedDateTime now = ZonedDateTime.now();
						now = now.plusMinutes(duration);

						binding.findRoutesBottomSheet.time.setText(new String(duration + "분"));
						binding.findRoutesBottomSheet.progressLayout.onSuccessful();

						DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("a hh:mm");
						binding.findRoutesBottomSheet.estimatedArrivalTime.setText(now.format(dateTimeFormatter));

						createPath(currentLocation, path);
					}
				});

				Bundle infoBundle = new Bundle();
				infoBundle.putSerializable("goalLocationDto", selectedLocationDtoInEvent);
				findRouteFragment.setArguments(infoBundle);

				collapseAllExpandedBottomSheets();

				FragmentManager childFragmentManager = getChildFragmentManager();
				int backStackCount = childFragmentManager.getBackStackEntryCount();

				for (int count = 0; count < backStackCount; count++) {
					childFragmentManager.popBackStack();
				}

				binding.findRoutesBottomSheet.updateBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						binding.findRoutesBottomSheet.progressLayout.onStarted(getString(R.string.calculating_routes));
						findRouteFragment.findRoutes();
					}
				});

				binding.findRoutesBottomSheet.closeBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						getChildFragmentManager().popBackStack();
					}
				});

				childFragmentManager.beginTransaction().add(binding.findRoutesBottomSheet.fragmentContainerView.getId(),
								findRouteFragment, FindRouteFragment.class.getName())
						.addToBackStack(FindRouteFragment.class.getName()).commitAllowingStateLoss();

				setStateOfBottomSheet(BottomSheetType.FIND_ROUTES, BottomSheetBehavior.STATE_EXPANDED);
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
		getChildFragmentManager().unregisterFragmentLifecycleCallbacks(fragmentLifecycleCallbacks);
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

	private void createPath(Location currentLocation, ArrayList<ArrayList<Double>> pathList) {
		removePath();

		path = new PathOverlay();

		showMarkerOfCurrentLocation(currentLocation);
		List<LatLng> coords = new ArrayList<>();
		for (ArrayList<Double> path : pathList) {
			coords.add(new LatLng(path.get(1), path.get(0)));
		}

		path.setCoords(coords);
		path.setMap(naverMap);
		path.setColor(Color.GREEN);
		path.setWidth(15);
		path.setOutlineWidth(4);
		path.setOutlineColor(Color.BLUE);

		naverMap.setContentPadding(0, 0, 0, binding.findRoutesBottomSheet.getRoot().getHeight());

		List<Marker> markers = new ArrayList<>();
		markers.add(selectedLocationInEventMarker);
		markers.add(currentMarker);
		showMarkers(markers);
	}

	private void showMarkerOfCurrentLocation(Location currentLocation) {
		if (currentMarker == null) {
			currentMarker = new Marker();
			String caption = getString(R.string.current_location);

			currentMarker.setCaptionText(caption);
			currentMarker.setCaptionColor(Color.BLACK);

			int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32f, getResources().getDisplayMetrics());
			int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 42f, getResources().getDisplayMetrics());
			currentMarker.setWidth(width);
			currentMarker.setHeight(height);
		}

		currentMarker.setPosition(
				new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
		currentMarker.setMap(naverMap);
	}

	private void removePath() {
		if (path != null) {
			path.setMap(null);
		}

		if (currentMarker != null) {
			currentMarker.setMap(null);
		}

		naverMap.setContentPadding(0, 0, 0, 0);
	}
}
