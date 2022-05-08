package com.lifedawn.capstoneapp.map;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.location.Location;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.common.constants.BundleConstant;
import com.lifedawn.capstoneapp.common.constants.SharedPreferenceConstant;
import com.lifedawn.capstoneapp.common.interfaces.OnClickedListItemListener;
import com.lifedawn.capstoneapp.common.util.FusedLocation;
import com.lifedawn.capstoneapp.common.util.LocationLifeCycleObserver;
import com.lifedawn.capstoneapp.common.view.SearchHistoryFragment;
import com.lifedawn.capstoneapp.databinding.FragmentAbstractNaverMapBinding;
import com.lifedawn.capstoneapp.kakao.SearchBarFragment;
import com.lifedawn.capstoneapp.kakao.search.searchresult.LocationSearchResultMainFragment;
import com.lifedawn.capstoneapp.map.adapters.LocationItemViewPagerAbstractAdapter;
import com.lifedawn.capstoneapp.map.adapters.LocationItemViewPagerAdapter;
import com.lifedawn.capstoneapp.map.interfaces.BottomSheetController;
import com.lifedawn.capstoneapp.map.interfaces.IMap;
import com.lifedawn.capstoneapp.map.interfaces.MarkerOnClickListener;
import com.lifedawn.capstoneapp.map.interfaces.OnClickedBottomSheetListener;
import com.lifedawn.capstoneapp.map.interfaces.OnPoiItemClickListener;
import com.lifedawn.capstoneapp.map.places.AroundPlacesContentsFragment;
import com.lifedawn.capstoneapp.map.places.AroundPlacesHeaderFragment;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.KakaoLocalDocument;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.address.AddressResponse;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.place.PlaceResponse;
import com.lifedawn.capstoneapp.room.dto.SearchHistoryDto;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.geometry.LatLngBounds;
import com.naver.maps.map.CameraAnimation;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.NaverMapOptions;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.Projection;
import com.naver.maps.map.overlay.LocationOverlay;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.util.CameraUtils;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.util.MarkerIcons;

import org.jetbrains.annotations.NotNull;
import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractNaverMapFragment extends Fragment implements LocationItemViewPagerAbstractAdapter.OnClickedLocationBtnListener, OnMapReadyCallback, NaverMap.OnMapClickListener, NaverMap.OnCameraIdleListener, CameraUpdate.FinishCallback, NaverMap.OnLocationChangeListener, NaverMap.OnMapLongClickListener, OnPoiItemClickListener, BottomSheetController, IMap, OnClickedBottomSheetListener {
	private static final int PERMISSION_REQUEST_CODE = 1;
	private static final int REQUEST_CODE_LOCATION = 2;
	protected final Map<MarkerType, List<Marker>> MARKERS_MAP = new HashMap<>();
	protected final Map<MarkerType, LocationItemViewPagerAbstractAdapter> VIEW_PAGER_ADAPTER_MAP = new HashMap<>();
	protected final Map<BottomSheetType, BottomSheetBehavior> bottomSheetBehaviorMap = new HashMap<>();
	protected final Map<BottomSheetType, Fragment> bottomSheetFragmentMap = new HashMap<>();
	protected final Map<BottomSheetType, LinearLayout> bottomSheetViewMap = new HashMap<>();

	protected Integer DEFAULT_HEIGHT_OF_BOTTOMSHEET;
	protected Integer HIGH_HEIGHT_OF_BOTTOMSHEET;
	protected Integer MAX_HEIGHT_OF_BOTTOMSHEET;
	protected Integer MEDIUM_HEIGHT_OF_BOTTOMSHEET;
	protected Integer SMALL_HEIGHT_OF_BOTTOMSHEET;

	protected float mapTranslationLength;

	protected FragmentAbstractNaverMapBinding binding;
	protected NaverMap naverMap;
	protected MapFragment mapFragment;
	protected FusedLocationSource fusedLocationSource;
	protected LocationLifeCycleObserver locationLifeCycleObserver;
	protected FusedLocation fusedLocation;
	protected MapViewModel mapViewModel;

	protected Integer markerWidth;
	protected Integer markerHeight;

	private ViewPager2 locationItemBottomSheetViewPager;

	private int placeBottomSheetSelectBtnVisibility = View.GONE;
	private int placeBottomSheetUnSelectBtnVisibility = View.GONE;


	protected final OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
		@Override
		public void handleOnBackPressed() {
			if (!getChildFragmentManager().popBackStackImmediate()) {
				getParentFragmentManager().popBackStackImmediate();
			}
		}
	};


	protected final FragmentManager.FragmentLifecycleCallbacks fragmentLifecycleCallbacks = new FragmentManager.FragmentLifecycleCallbacks() {
		@Override
		public void onFragmentAttached(@NonNull @NotNull FragmentManager fm, @NonNull @NotNull Fragment f,
		                               @NonNull @NotNull Context context) {
			super.onFragmentAttached(fm, f, context);
			if (f instanceof SearchHistoryFragment) {
				binding.headerLayout.setVisibility(View.GONE);
			} else if (f instanceof AroundPlacesContentsFragment) {
				binding.headerLayout.setVisibility(View.GONE);
				binding.anotherFragmentContainer.setVisibility(View.VISIBLE);
			}
		}

		@Override
		public void onFragmentCreated(@NonNull @NotNull FragmentManager fm, @NonNull @NotNull Fragment f,
		                              @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
			super.onFragmentCreated(fm, f, savedInstanceState);

		}

		@Override
		public void onFragmentDestroyed(@NonNull @NotNull FragmentManager fm, @NonNull @NotNull Fragment f) {
			super.onFragmentDestroyed(fm, f);
			if (f instanceof SearchHistoryFragment) {
				setStateOfBottomSheet(BottomSheetType.SEARCH_LOCATION, BottomSheetBehavior.STATE_COLLAPSED);
				binding.headerLayout.setVisibility(View.VISIBLE);

			} else if (f instanceof LocationSearchResultMainFragment) {
				removeMarkers(MarkerType.SEARCH_RESULT_ADDRESS, MarkerType.SEARCH_RESULT_PLACE);
			} else if (f instanceof AroundPlacesContentsFragment) {
				binding.anotherFragmentContainer.setVisibility(View.GONE);
				binding.headerLayout.setVisibility(View.VISIBLE);
				removeAllMarkers();
				setStateOfBottomSheet(BottomSheetType.LOCATION_ITEM, BottomSheetBehavior.STATE_COLLAPSED);
			}
		}

	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getChildFragmentManager().registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, true);
		getActivity().getOnBackPressedDispatcher().addCallback(onBackPressedCallback);

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
		mapViewModel.setiMapData(this);
		mapViewModel.setBottomSheetController(this);
		mapViewModel.setPoiItemOnClickListener(this);

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

		setLocationItemsBottomSheet();
		setLocationSearchBottomSheet();
		setAroundPlacesBottomSheet();



		binding.placeChip.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//주변 장소 표시
				collapseAllExpandedBottomSheets();

				FragmentManager childFragmentManager = getChildFragmentManager();
				int backStackCount = childFragmentManager.getBackStackEntryCount();

				for (int count = 0; count < backStackCount; count++) {
					childFragmentManager.popBackStack();
				}

				AroundPlacesHeaderFragment headerFragment = new AroundPlacesHeaderFragment();
				AroundPlacesContentsFragment aroundPlacesContentsFragment = new AroundPlacesContentsFragment();
				headerFragment.setiConnectContents(aroundPlacesContentsFragment);
				headerFragment.setOnExtraListDataListener(aroundPlacesContentsFragment);

				if (getPromiseLocationDto() != null) {
					Bundle bundle = new Bundle();
					bundle.putSerializable("locationDto", getPromiseLocationDto());
					headerFragment.setArguments(bundle);
				}

				childFragmentManager.beginTransaction().add(binding.aroundPlacesBottomSheet.fragmentContainerView.getId(),
						aroundPlacesContentsFragment, AroundPlacesContentsFragment.class.getName())
						.add(binding.anotherFragmentContainer.getId(), headerFragment,
								AroundPlacesHeaderFragment.class.getName())
						.addToBackStack(AroundPlacesContentsFragment.class.getName()).commitAllowingStateLoss();

				setStateOfBottomSheet(BottomSheetType.AROUND_PLACES, BottomSheetBehavior.STATE_EXPANDED);
			}
		});

		binding.restaurantsChip.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//주변 장소 표시
				collapseAllExpandedBottomSheets();

				FragmentManager childFragmentManager = getChildFragmentManager();
				int backStackCount = childFragmentManager.getBackStackEntryCount();

				for (int count = 0; count < backStackCount; count++) {
					childFragmentManager.popBackStack();
				}

				AroundPlacesHeaderFragment headerFragment = new AroundPlacesHeaderFragment();
				AroundPlacesContentsFragment aroundPlacesContentsFragment = new AroundPlacesContentsFragment();
				headerFragment.setiConnectContents(aroundPlacesContentsFragment);
				headerFragment.setOnExtraListDataListener(aroundPlacesContentsFragment);

				if (getPromiseLocationDto() != null) {
					Bundle bundle = new Bundle();
					bundle.putSerializable("locationDto", getPromiseLocationDto());
					headerFragment.setArguments(bundle);
				}

				childFragmentManager.beginTransaction().add(binding.aroundPlacesBottomSheet.fragmentContainerView.getId(),
						aroundPlacesContentsFragment, AroundPlacesContentsFragment.class.getName())
						.add(binding.anotherFragmentContainer.getId(), headerFragment,
								AroundPlacesHeaderFragment.class.getName())
						.addToBackStack(AroundPlacesContentsFragment.class.getName()).commitAllowingStateLoss();

				setStateOfBottomSheet(BottomSheetType.AROUND_PLACES, BottomSheetBehavior.STATE_EXPANDED);
			}
		});


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

		binding.naverMapFragmentRootLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				binding.naverMapFragmentRootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

				//search bottom sheet 크기 조정
				final int headerBarHeight = binding.headerLayout.getHeight() - binding.funcChipGroup.getHeight();
				final int headerBarTopMargin = (int) getResources().getDimension(R.dimen.map_header_bar_top_margin);
				final int headerBarMargin = (int) (headerBarTopMargin * 1.5f);
				final int fullHeight = binding.naverMapFragmentRootLayout.getHeight();

				final int margin32 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32f, getResources().getDisplayMetrics());

				DEFAULT_HEIGHT_OF_BOTTOMSHEET = fullHeight - (int) getResources().getDimension(
						R.dimen.map_header_bar_height) - headerBarMargin;
				HIGH_HEIGHT_OF_BOTTOMSHEET = fullHeight - margin32;

				final int searchBottomSheetHeight = binding.naverMapFragmentRootLayout.getHeight() - headerBarHeight - headerBarMargin;

				LinearLayout locationSearchBottomSheet = bottomSheetViewMap.get(BottomSheetType.SEARCH_LOCATION);

				locationSearchBottomSheet.getLayoutParams().height = searchBottomSheetHeight;
				locationSearchBottomSheet.requestLayout();

				BottomSheetBehavior locationSearchBottomSheetBehavior = bottomSheetBehaviorMap.get(BottomSheetType.SEARCH_LOCATION);
				locationSearchBottomSheetBehavior.onLayoutChild(binding.naverMapFragmentRootLayout, locationSearchBottomSheet,
						ViewCompat.LAYOUT_DIRECTION_LTR);

			}
		});

		binding.headerFragmentContainer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//expand search location bottomsheet
				collapseAllExpandedBottomSheets();

				FragmentManager childFragmentManager = getChildFragmentManager();
				int backStackCount = childFragmentManager.getBackStackEntryCount();

				for (int count = 0; count < backStackCount; count++) {
					childFragmentManager.popBackStack();
				}

				final String tag = SearchHistoryFragment.class.getName();
				final Bundle bundle = new Bundle();
				bundle.putSerializable(BundleConstant.SEARCH_HISTORY_TYPE.name(), SearchHistoryDto.SearchHistoryType.MAP);

				SearchHistoryFragment searchHistoryFragment = new SearchHistoryFragment();
				searchHistoryFragment.setOnClickedHistoryItemListener(
						(SearchHistoryFragment.OnClickedHistoryItemListener) childFragmentManager.findFragmentByTag(
								SearchBarFragment.class.getName()));
				searchHistoryFragment.setArguments(bundle);

				childFragmentManager.beginTransaction().add(binding.locationSearchBottomSheet.searchFragmentContainer.getId(),
						searchHistoryFragment, tag).addToBackStack(tag).commit();

				setStateOfBottomSheet(BottomSheetType.SEARCH_LOCATION, BottomSheetBehavior.STATE_EXPANDED);
			}
		});


		loadMap();
	}

	private void moveMap(boolean recovery, int height) {
		if (recovery) {
			PointF movePoint = new PointF(0f, mapTranslationLength);
			CameraUpdate cameraUpdate = CameraUpdate.scrollBy(movePoint);
			naverMap.moveCamera(cameraUpdate);
		} else {

			Projection projection = naverMap.getProjection();
			LatLng latLng = naverMap.getContentBounds().getCenter();
			LatLng northEast = latLng.offset(2000, 2000);
			LatLng southWest = latLng.offset(-2000, 2000);
			LatLngBounds latLngBounds = new LatLngBounds(southWest, northEast);

			int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32f, getResources().getDisplayMetrics());
			CameraUpdate cameraUpdateZoom = CameraUpdate.zoomTo(CameraUtils.getFittableZoom(naverMap, latLngBounds, padding));
			naverMap.moveCamera(cameraUpdateZoom);

			PointF point = projection.toScreenLocation(latLng);

			final int newMapViewContentHeight =
					binding.naverMapViewLayout.getHeight() - height - (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 350f,
							getResources().getDisplayMetrics());
			mapTranslationLength = (float) (point.y - (binding.naverMapViewLayout.getHeight() / 2 - newMapViewContentHeight / 2));

			PointF movePoint = new PointF(0f, -mapTranslationLength);
			CameraUpdate cameraUpdate = CameraUpdate.scrollBy(movePoint);
			naverMap.moveCamera(cameraUpdate);

		}
	}


	protected abstract LocationDto getPromiseLocationDto();

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
		onBackPressedCallback.remove();
		super.onDestroy();
	}

	protected final void onClickedBottomNav() {
		if (getStateOfBottomSheet(BottomSheetType.LOCATION_ITEM) == BottomSheetBehavior.STATE_EXPANDED) {
			setStateOfBottomSheet(BottomSheetType.LOCATION_ITEM, BottomSheetBehavior.STATE_COLLAPSED);
		}
	}

	private void setLocationSearchBottomSheet() {
		LinearLayout locationSearchBottomSheet = binding.locationSearchBottomSheet.locationSearchBottomsheet;

		BottomSheetBehavior locationSearchBottomSheetBehavior = BottomSheetBehavior.from(locationSearchBottomSheet);
		locationSearchBottomSheetBehavior.setDraggable(false);
		locationSearchBottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
			@Override
			public void onStateChanged(@NonNull View bottomSheet, int newState) {

			}

			@Override
			public void onSlide(@NonNull View bottomSheet, float slideOffset) {

			}
		});
		locationSearchBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

		bottomSheetViewMap.put(BottomSheetType.SEARCH_LOCATION, locationSearchBottomSheet);
		bottomSheetBehaviorMap.put(BottomSheetType.SEARCH_LOCATION, locationSearchBottomSheetBehavior);

		SearchBarFragment searchBarFragment = new SearchBarFragment();
		getChildFragmentManager().beginTransaction().replace(binding.locationSearchBottomSheet.searchBarFragmentContainer.getId(),
				searchBarFragment, SearchBarFragment.class.getName()).commit();
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
		deselectMarker();
	}

	@Override
	public void onMapLongClick(@NonNull PointF pointF, @NonNull LatLng latLng) {

	}

	protected void createPlaceMarker(MarkerType markerType, PlaceResponse.Documents item, MarkerOnClickListener onMarkerClickListener) {
		Marker marker = new Marker();
		marker.setWidth(markerWidth);
		marker.setHeight(markerHeight);
		marker.setPosition(new LatLng(Double.parseDouble(item.getY()), Double.parseDouble(item.getX())));
		marker.setMap(naverMap);
		marker.setCaptionText(item.getPlaceName());
		marker.setOnClickListener(new Overlay.OnClickListener() {
			@Override
			public boolean onClick(@NonNull Overlay overlay) {
				onMarkerClickListener.onClickedMarker();
				onClickedMarkerByTouch((Marker) overlay);
				return true;
			}
		});

		if (markerType == MarkerType.RESTAURANT) {
			marker.setIcon(MarkerIcons.BLUE);
		}

		marker.setTag(new MarkerHolder(item, markerType));
		MARKERS_MAP.get(markerType).add(marker);
	}

	protected void createAddressMarker(MarkerType markerType, AddressResponse.Documents item, MarkerOnClickListener onMarkerClickListener) {
		Marker marker = new Marker();
		marker.setWidth(markerWidth);
		marker.setHeight(markerHeight);
		marker.setPosition(new LatLng(Double.parseDouble(item.getY()), Double.parseDouble(item.getX())));
		marker.setMap(naverMap);
		marker.setCaptionText(item.getAddressName());
		marker.setOnClickListener(new Overlay.OnClickListener() {
			@Override
			public boolean onClick(@NonNull Overlay overlay) {
				onMarkerClickListener.onClickedMarker();
				onClickedMarkerByTouch((Marker) overlay);
				return true;
			}
		});

		marker.setTag(new MarkerHolder(item, markerType));
		MARKERS_MAP.get(markerType).add(marker);
	}


	protected void onClickedMarkerByTouch(Marker marker) {
		//poiitem을 직접 선택한 경우 호출
		CameraUpdate cameraUpdate = CameraUpdate.scrollTo(marker.getPosition());
		cameraUpdate.animate(CameraAnimation.Easing, 160);
		naverMap.moveCamera(cameraUpdate);

		MarkerHolder markerHolder = (MarkerHolder) marker.getTag();
		LocationItemViewPagerAbstractAdapter adapter = VIEW_PAGER_ADAPTER_MAP.get(markerHolder.markerType);
		int itemPosition = 0;

		if (markerHolder.markerType == MarkerType.LONG_CLICKED_MAP) {
			itemPosition = 0;
		} else {
			itemPosition = ((LocationItemViewPagerAdapter) adapter).getItemPosition(markerHolder.kakaoLocalDocument);
		}

		//선택된 마커의 아이템 리스트내 위치 파악 후 뷰 페이저 이동
		locationItemBottomSheetViewPager.setTag(markerHolder.markerType);
		locationItemBottomSheetViewPager.setAdapter(adapter);
		locationItemBottomSheetViewPager.setCurrentItem(itemPosition, false);

		setStateOfBottomSheet(BottomSheetType.LOCATION_ITEM, BottomSheetBehavior.STATE_EXPANDED);
	}

	protected final OnClickedListItemListener<PlaceResponse.Documents> ON_CLICK_PLACE_ITEM_IN_LIST_LISTENER = new OnClickedListItemListener<PlaceResponse.Documents>() {
		@Override
		public void onClicked(PlaceResponse.Documents e) {

		}
	};

	protected final OnClickedListItemListener<AddressResponse.Documents> ON_CLICK_ADDRESS_ITEM_IN_LIST_LISTENER = new OnClickedListItemListener<AddressResponse.Documents>() {
		@Override
		public void onClicked(AddressResponse.Documents e) {

		}
	};


	public void onPageSelectedLocationItemBottomSheetViewPager(int position, MarkerType markerType) {

		switch (markerType) {

			case SEARCH_RESULT_ADDRESS:
				LocationSearchResultMainFragment locationSearchResultFragmentForAddress = (LocationSearchResultMainFragment) getChildFragmentManager().findFragmentByTag(
						LocationSearchResultMainFragment.class.getName());
				locationSearchResultFragmentForAddress.loadExtraListData(new RecyclerView.AdapterDataObserver() {
					@Override
					public void onItemRangeInserted(int positionStart, int itemCount) {
						super.onItemRangeInserted(positionStart, itemCount);
					}
				});
				return;

			case SEARCH_RESULT_PLACE:
				LocationSearchResultMainFragment locationSearchResultFragmentForPlace = (LocationSearchResultMainFragment) getChildFragmentManager().findFragmentByTag(
						LocationSearchResultMainFragment.class.getName());
				locationSearchResultFragmentForPlace.loadExtraListData(new RecyclerView.AdapterDataObserver() {
					@Override
					public void onItemRangeInserted(int positionStart, int itemCount) {
						super.onItemRangeInserted(positionStart, itemCount);
					}
				});
				return;

			case AROUND_PLACE:
				AroundPlacesHeaderFragment fragment =
						(AroundPlacesHeaderFragment) getChildFragmentManager().findFragmentByTag(
								AroundPlacesHeaderFragment.class.getName());

				fragment.loadExtraListData(new RecyclerView.AdapterDataObserver() {
					@Override
					public void onItemRangeInserted(int positionStart, int itemCount) {
						super.onItemRangeInserted(positionStart, itemCount);
					}
				});
				return;

		}
	}

	protected void setLocationItemsBottomSheet() {
		LinearLayout locationItemBottomSheet = binding.placeslistBottomSheet.placesBottomsheet;
		locationItemBottomSheetViewPager = (ViewPager2) locationItemBottomSheet.findViewById(R.id.place_items_viewpager);

		locationItemBottomSheetViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
			MarkerType markerType;

			@Override
			public void onPageSelected(int position) {
				super.onPageSelected(position);
				if (getStateOfBottomSheet(BottomSheetType.LOCATION_ITEM) == BottomSheetBehavior.STATE_EXPANDED) {
					markerType = (MarkerType) locationItemBottomSheetViewPager.getTag();
					onPOIItemSelectedByBottomSheet(position, markerType, new MarkerOnClickListener() {
						@Override
						public void onClickedMarker() {

						}
					});

					int count = VIEW_PAGER_ADAPTER_MAP.get(markerType).getItemCount();

					if (count >= 15 && position == count - 1) {
						onPageSelectedLocationItemBottomSheetViewPager(position, markerType);
					} else {

					}

				}
			}
		});
		locationItemBottomSheetViewPager.setOffscreenPageLimit(2);

		BottomSheetBehavior locationItemBottomSheetBehavior = BottomSheetBehavior.from(locationItemBottomSheet);
		locationItemBottomSheetBehavior.setDraggable(false);
		locationItemBottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
			float differenceY;

			@Override
			public void onStateChanged(@NonNull View bottomSheet, int newState) {
				if (newState == BottomSheetBehavior.STATE_EXPANDED) {
					binding.naverMapButtonsLayout.getRoot().setY(binding.getRoot().getHeight() - bottomSheet.getHeight());
				} else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
					binding.naverMapButtonsLayout.getRoot().setY(binding.getRoot().getHeight() - binding.naverMapButtonsLayout.getRoot().getHeight());
				}
			}

			@Override
			public void onSlide(@NonNull View bottomSheet, float slideOffset) {
				//expanded일때 offset == 1.0, collapsed일때 offset == 0.0
				//offset에 따라서 버튼들이 이동하고, 지도의 좌표가 변경되어야 한다.
				differenceY = bottomSheet.getHeight();
				float translationValue = -differenceY * slideOffset;
				binding.naverMapButtonsLayout.getRoot().animate().translationY(translationValue);
			}
		});

		bottomSheetViewMap.put(BottomSheetType.LOCATION_ITEM, locationItemBottomSheet);
		bottomSheetBehaviorMap.put(BottomSheetType.LOCATION_ITEM, locationItemBottomSheetBehavior);
	}

	protected void setRestaurantsBottomSheet() {
		LinearLayout restaurantsBottomSheet = binding.restaurantsBottomSheet.restaurantsBottomSheet;

		BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(restaurantsBottomSheet);
		bottomSheetBehavior.setDraggable(false);
		bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
			float differenceY;

			@Override
			public void onStateChanged(@NonNull View bottomSheet, int newState) {

			}

			@Override
			public void onSlide(@NonNull View bottomSheet, float slideOffset) {
				//expanded일때 offset == 1.0, collapsed일때 offset == 0.0
				//offset에 따라서 버튼들이 이동하고, 지도의 좌표가 변경되어야 한다.
				differenceY = bottomSheet.getHeight();
				float translationValue = -differenceY * slideOffset;
				binding.naverMapButtonsLayout.getRoot().animate().translationY(translationValue);
			}
		});

		bottomSheetViewMap.put(BottomSheetType.RESTAURANT, restaurantsBottomSheet);
		bottomSheetBehaviorMap.put(BottomSheetType.RESTAURANT, bottomSheetBehavior);
	}

	protected void setAroundPlacesBottomSheet() {
		LinearLayout aroundPlacesBottomSheet = binding.aroundPlacesBottomSheet.aroundPlacesBottomSheet;


		BottomSheetBehavior aroundPlacesBottomSheetBehavior = BottomSheetBehavior.from(aroundPlacesBottomSheet);
		aroundPlacesBottomSheetBehavior.setDraggable(false);
		aroundPlacesBottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
			float differenceY;

			@Override
			public void onStateChanged(@NonNull View bottomSheet, int newState) {
				if (newState == BottomSheetBehavior.STATE_EXPANDED) {
					Projection projection = naverMap.getProjection();
					LatLng latLng = naverMap.getContentBounds().getCenter();

					PointF point = projection.toScreenLocation(latLng);

					final int newMapViewContentHeight =
							binding.naverMapViewLayout.getHeight() - bottomSheet.getHeight();
					mapTranslationLength = (float) (point.y - (binding.naverMapViewLayout.getHeight() / 2 - newMapViewContentHeight / 2));

					PointF movePoint = new PointF(0f, -mapTranslationLength);
					CameraUpdate cameraUpdate = CameraUpdate.scrollBy(movePoint);
					naverMap.moveCamera(cameraUpdate);

				}
			}

			@Override
			public void onSlide(@NonNull View bottomSheet, float slideOffset) {
				//expanded일때 offset == 1.0, collapsed일때 offset == 0.0
				//offset에 따라서 버튼들이 이동하고, 지도의 좌표가 변경되어야 한다.
				differenceY = bottomSheet.getHeight();
				float translationValue = -differenceY * slideOffset;
				binding.naverMapButtonsLayout.getRoot().animate().translationY(translationValue);
			}
		});

		bottomSheetViewMap.put(BottomSheetType.AROUND_PLACES, aroundPlacesBottomSheet);
		bottomSheetBehaviorMap.put(BottomSheetType.AROUND_PLACES, aroundPlacesBottomSheetBehavior);
	}

	public void setPlaceBottomSheetSelectBtnVisibility(int placeBottomSheetSelectBtnVisibility) {
		this.placeBottomSheetSelectBtnVisibility = placeBottomSheetSelectBtnVisibility;
	}

	public void setPlaceBottomSheetUnSelectBtnVisibility(int placeBottomSheetUnSelectBtnVisibility) {
		this.placeBottomSheetUnSelectBtnVisibility = placeBottomSheetUnSelectBtnVisibility;
	}


	@Override
	public void createMarkers(@NotNull List<? extends KakaoLocalDocument> kakaoLocalDocuments, @NotNull MarkerType markerType,
	                          MarkerOnClickListener markerOnClickListener) {
		if (!MARKERS_MAP.containsKey(markerType)) {
			MARKERS_MAP.put(markerType, new ArrayList<>());
		} else {
			removeMarkers(markerType);
		}

		LocationItemViewPagerAbstractAdapter adapter = new LocationItemViewPagerAdapter(getContext(), markerType);
		adapter.setOnClickedLocationBtnListener(this);
		adapter.setOnClickedBottomSheetListener(this);
		adapter.setVisibleSelectBtn(placeBottomSheetSelectBtnVisibility);
		adapter.setVisibleUnSelectBtn(placeBottomSheetUnSelectBtnVisibility);
		((LocationItemViewPagerAdapter) adapter).setLocalDocumentsList(kakaoLocalDocuments);
		adapter.notifyDataSetChanged();

		VIEW_PAGER_ADAPTER_MAP.put(markerType, adapter);

		if (!kakaoLocalDocuments.isEmpty()) {
			if (kakaoLocalDocuments.get(0) instanceof PlaceResponse.Documents) {
				List<PlaceResponse.Documents> placeDocuments = (List<PlaceResponse.Documents>) kakaoLocalDocuments;

				for (PlaceResponse.Documents document : placeDocuments) {
					createPlaceMarker(markerType, document, markerOnClickListener);
				}
			} else if (kakaoLocalDocuments.get(0) instanceof AddressResponse.Documents) {
				List<AddressResponse.Documents> addressDocuments = (List<AddressResponse.Documents>) kakaoLocalDocuments;

				for (AddressResponse.Documents document : addressDocuments) {
					createAddressMarker(markerType, document, markerOnClickListener);
				}
			}
		}
	}

	@Override
	public void addExtraMarkers(@NotNull List<? extends KakaoLocalDocument> kakaoLocalDocuments, @NotNull MarkerType markerType,
	                            MarkerOnClickListener markerOnClickListener) {
		if (!kakaoLocalDocuments.isEmpty()) {
			LocationItemViewPagerAdapter adapter = (LocationItemViewPagerAdapter) VIEW_PAGER_ADAPTER_MAP.get(markerType);
			final int lastIndex = adapter.getItemsCount() - 1;

			List<KakaoLocalDocument> currentList = adapter.getLocalDocumentsList();
			List<? extends KakaoLocalDocument> subList = (List<? extends KakaoLocalDocument>) kakaoLocalDocuments.subList(lastIndex + 1,
					kakaoLocalDocuments.size());
			currentList.addAll(subList);

			if (kakaoLocalDocuments.get(0) instanceof PlaceResponse.Documents) {
				List<PlaceResponse.Documents> placeDocuments = (List<PlaceResponse.Documents>) subList;

				for (PlaceResponse.Documents document : placeDocuments) {
					createPlaceMarker(markerType, document, markerOnClickListener);
				}
			} else if (kakaoLocalDocuments.get(0) instanceof AddressResponse.Documents) {
				List<AddressResponse.Documents> addressDocuments = (List<AddressResponse.Documents>) subList;

				for (AddressResponse.Documents document : addressDocuments) {
					createAddressMarker(markerType, document, markerOnClickListener);
				}
			}

			adapter.notifyDataSetChanged();
		}
	}

	@Override
	public void removeMarker(MarkerType markerType, int index) {
		if (MARKERS_MAP.containsKey(markerType)) {
			MARKERS_MAP.get(markerType).get(index).setMap(null);
			MARKERS_MAP.get(markerType).remove(index);
		}
	}

	@Override
	public void removeMarkers(MarkerType... markerTypes) {
		for (MarkerType markerType : markerTypes) {
			if (MARKERS_MAP.containsKey(markerType)) {
				List<Marker> markerList = MARKERS_MAP.get(markerType);
				for (Marker marker : markerList) {
					marker.setMap(null);
				}

				markerList.clear();
			}
		}
	}


	@Override
	public void removeAllMarkers() {
		Set<MarkerType> keySet = MARKERS_MAP.keySet();
		for (MarkerType markerType : keySet) {
			List<Marker> markerList = MARKERS_MAP.get(markerType);
			for (Marker marker : markerList) {
				marker.setMap(null);
			}

			markerList.clear();
		}
	}


	@Override
	public void showMarkers(MarkerType... markerTypes) {
		List<LatLng> latLngList = new ArrayList<>();

		for (MarkerType markerType : markerTypes) {
			List<Marker> markerList = MARKERS_MAP.get(markerType);

			for (Marker marker : markerList) {
				latLngList.add(marker.getPosition());
			}
		}

		if (!latLngList.isEmpty()) {
			LatLngBounds latLngBounds = LatLngBounds.from(latLngList);

			int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20f, getResources().getDisplayMetrics());
			double fittableZoom = CameraUtils.getFittableZoom(naverMap, latLngBounds, padding);
			if (fittableZoom >= 16) {
				fittableZoom = 16;
			}

			CameraUpdate cameraUpdate = CameraUpdate.scrollAndZoomTo(latLngBounds.getCenter(), fittableZoom);
			naverMap.moveCamera(cameraUpdate);
		}
	}

	@Override
	public void deselectMarker() {
		setStateOfBottomSheet(BottomSheetType.LOCATION_ITEM, BottomSheetBehavior.STATE_COLLAPSED);
	}


	//POI => point of interest
	@Override
	public void onPOIItemSelectedByList(KakaoLocalDocument kakaoLocalDocument, MarkerType markerType, MarkerOnClickListener markerOnClickListener) {
		//bottomsheet가 아닌 list에서 아이템을 선택한 경우 호출
		//adapter -> poiitem생성 -> select poiitem -> bottomsheet열고 정보 표시
		List<Marker> markerList = MARKERS_MAP.get(markerType);
		MarkerHolder markerHolder = null;
		Marker selectedMarker = null;

		LocationItemViewPagerAbstractAdapter adapter = VIEW_PAGER_ADAPTER_MAP.get(markerType);
		final int position = adapter.getLocalItemPosition(kakaoLocalDocument);

		if (kakaoLocalDocument instanceof PlaceResponse.Documents) {
			String placeId = ((PlaceResponse.Documents) kakaoLocalDocument).getId();
			for (Marker marker : markerList) {
				markerHolder = (MarkerHolder) marker.getTag();
				if (((PlaceResponse.Documents) markerHolder.kakaoLocalDocument).getId().equals(placeId)) {
					selectedMarker = marker;
					break;
				}
			}
		} else if (kakaoLocalDocument instanceof AddressResponse.Documents) {
			String addressName = ((AddressResponse.Documents) kakaoLocalDocument).getAddressName();
			for (Marker marker : markerList) {
				markerHolder = (MarkerHolder) marker.getTag();
				if (((AddressResponse.Documents) markerHolder.kakaoLocalDocument).getAddressName().equals(addressName)) {
					selectedMarker = marker;
					break;
				}
			}
		}
		CameraUpdate cameraUpdate = CameraUpdate.scrollTo(selectedMarker.getPosition());
		cameraUpdate.animate(CameraAnimation.Easing, 150);
		naverMap.moveCamera(cameraUpdate);

		//선택된 마커의 아이템 리스트내 위치 파악 후 뷰 페이저 이동
		locationItemBottomSheetViewPager.setTag(markerType);
		locationItemBottomSheetViewPager.setAdapter(adapter);
		locationItemBottomSheetViewPager.setCurrentItem(position, false);

		setStateOfBottomSheet(BottomSheetType.LOCATION_ITEM, BottomSheetBehavior.STATE_EXPANDED);
	}


	LatLng lastTarget = new LatLng(0, 0);

	@Override
	public void onPOIItemSelectedByBottomSheet(int position, MarkerType markerType, MarkerOnClickListener markerOnClickListener) {
		//bottomsheet에서 스크롤 하는 경우 호출
		LatLng target = null;

		LocationItemViewPagerAbstractAdapter adapter = (LocationItemViewPagerAbstractAdapter) locationItemBottomSheetViewPager.getAdapter();
		KakaoLocalDocument kakaoLocalDocument = adapter.getLocalItem(position);

		if (kakaoLocalDocument instanceof PlaceResponse.Documents) {
			target = new LatLng(Double.parseDouble(((PlaceResponse.Documents) kakaoLocalDocument).getY()),
					Double.parseDouble(((PlaceResponse.Documents) kakaoLocalDocument).getX()));
		} else if (kakaoLocalDocument instanceof AddressResponse.Documents) {
			target = new LatLng(Double.parseDouble(((AddressResponse.Documents) kakaoLocalDocument).getY()),
					Double.parseDouble(((AddressResponse.Documents) kakaoLocalDocument).getX()));
		}


		if (!lastTarget.equals(target)) {
			CameraUpdate cameraUpdate = CameraUpdate.scrollTo(target);
			cameraUpdate.animate(CameraAnimation.Easing, 150);
			naverMap.moveCamera(cameraUpdate);
		}

		if (target == null) {
			target = new LatLng(0, 0);
		}
		lastTarget = target;
	}


	@Override
	public BottomSheetBehavior getBottomSheetBehavior(BottomSheetType bottomSheetType) {
		return bottomSheetBehaviorMap.get(bottomSheetType);
	}


	@Override
	public List<BottomSheetBehavior> getBottomSheetBehaviorOfExpanded(BottomSheetBehavior currentBottomSheetBehavior) {
		Set<BottomSheetType> keySet = bottomSheetBehaviorMap.keySet();
		List<BottomSheetBehavior> bottomSheetBehaviors = new ArrayList<>();

		for (BottomSheetType bottomSheetType : keySet) {
			if (bottomSheetBehaviorMap.get(bottomSheetType).getState() == BottomSheetBehavior.STATE_EXPANDED) {

				if (currentBottomSheetBehavior != null) {
					if (!bottomSheetBehaviorMap.get(bottomSheetType).equals(currentBottomSheetBehavior)) {
						bottomSheetBehaviors.add(bottomSheetBehaviorMap.get(bottomSheetType));
					}
				}
			}
		}
		return bottomSheetBehaviors;
	}

	@Override
	public void collapseAllExpandedBottomSheets() {
		Set<BottomSheetType> keySet = bottomSheetBehaviorMap.keySet();

		for (BottomSheetType bottomSheetType : keySet) {
			if (getStateOfBottomSheet(bottomSheetType) == BottomSheetBehavior.STATE_EXPANDED) {
				setStateOfBottomSheet(bottomSheetType, BottomSheetBehavior.STATE_COLLAPSED);
			}
		}
	}

	@Override
	public void setStateOfBottomSheet(BottomSheetType bottomSheetType, int state) {
		bottomSheetBehaviorMap.get(bottomSheetType).setState(state);
	}

	@Override
	public int getStateOfBottomSheet(BottomSheetType bottomSheetType) {
		return bottomSheetBehaviorMap.get(bottomSheetType).getState();
	}


	@Override
	public void showMarkers(MarkerType markerType, boolean isShow) {
		if (MARKERS_MAP.containsKey(markerType)) {
			List<Marker> markers = MARKERS_MAP.get(markerType);
			for (Marker marker : markers) {
				marker.setMap(isShow ? naverMap : null);
			}
		}
	}

	protected final Object[] createBottomSheet(int fragmentContainerViewId) {
		XmlPullParser parser = getResources().getXml(R.xml.persistent_bottom_sheet_default_attrs);
		try {
			parser.next();
			parser.nextTag();
		} catch (Exception e) {
			e.printStackTrace();
		}

		AttributeSet attr = Xml.asAttributeSet(parser);
		LinearLayout bottomSheetView = new LinearLayout(getContext(), attr);

		CoordinatorLayout.LayoutParams layoutParams = new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);
		layoutParams.setBehavior(new BottomSheetBehavior());
		bottomSheetView.setLayoutParams(layoutParams);
		bottomSheetView.setClickable(true);
		bottomSheetView.setOrientation(LinearLayout.VERTICAL);

		binding.naverMapFragmentRootLayout.addView(bottomSheetView);

		//fragmentcontainerview 추가
		FragmentContainerView fragmentContainerView = new FragmentContainerView(getContext());
		fragmentContainerView.setId(fragmentContainerViewId);
		fragmentContainerView.setLayoutParams(
				new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		bottomSheetView.addView(fragmentContainerView);

		BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView);
		bottomSheetBehavior.setDraggable(false);
		bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
		bottomSheetBehavior.setHideable(false);
		bottomSheetBehavior.setPeekHeight(0);

		return new Object[]{bottomSheetView, bottomSheetBehavior};
	}

	@Override
	public void onSelected(KakaoLocalDocument kakaoLocalDocument, boolean remove) {

	}

	protected final void openRestaurantFragment() {

	}


	protected static class MarkerHolder {
		final KakaoLocalDocument kakaoLocalDocument;
		final MarkerType markerType;

		public MarkerHolder(MarkerType markerType) {
			this.kakaoLocalDocument = null;
			this.markerType = markerType;
		}

		public MarkerHolder(KakaoLocalDocument kakaoLocalDocument, MarkerType markerType) {
			this.kakaoLocalDocument = kakaoLocalDocument;
			this.markerType = markerType;
		}

	}
}