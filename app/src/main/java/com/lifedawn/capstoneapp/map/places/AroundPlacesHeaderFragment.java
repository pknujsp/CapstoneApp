package com.lifedawn.capstoneapp.map.places;

import android.content.Context;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.common.constants.Constant;
import com.lifedawn.capstoneapp.common.interfaces.OnDbQueryCallback;
import com.lifedawn.capstoneapp.common.repository.CustomPlaceCategoryRepository;
import com.lifedawn.capstoneapp.databinding.FragmentAroundPlacesHeaderBinding;
import com.lifedawn.capstoneapp.map.BottomSheetType;
import com.lifedawn.capstoneapp.map.LocationDto;
import com.lifedawn.capstoneapp.map.MapViewModel;
import com.lifedawn.capstoneapp.map.MarkerType;
import com.lifedawn.capstoneapp.map.interfaces.BottomSheetController;
import com.lifedawn.capstoneapp.map.interfaces.IMap;
import com.lifedawn.capstoneapp.map.interfaces.MarkerOnClickListener;
import com.lifedawn.capstoneapp.map.interfaces.OnExtraListDataListener;
import com.lifedawn.capstoneapp.map.interfaces.OnPoiItemClickListener;
import com.lifedawn.capstoneapp.retrofits.parameters.LocalApiPlaceParameter;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.KakaoLocalDocument;
import com.lifedawn.capstoneapp.room.dto.CustomPlaceCategoryDto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class AroundPlacesHeaderFragment extends Fragment implements OnExtraListDataListener<Constant>, IConnectHeader {
	private FragmentAroundPlacesHeaderBinding binding;
	private CustomPlaceCategoryRepository customPlaceCategoryRepository;
	private LocationDto promiseLocationDto;
	private IMap iMap;
	private MapViewModel mapViewModel;
	private BottomSheetController bottomSheetController;
	private OnPoiItemClickListener mapOnPoiItemClickListener;
	private IConnectContents iConnectContents;
	private OnExtraListDataListener<Constant> onExtraListDataListener;

	private static int currentSearchMapPointCriteria = LocalApiPlaceParameter.SEARCH_CRITERIA_MAP_POINT_CURRENT_LOCATION;
	private static int currentSearchSortTypeCriteria = LocalApiPlaceParameter.SEARCH_CRITERIA_SORT_TYPE_ACCURACY;

	private final OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
		@Override
		public void handleOnBackPressed() {
			if (bottomSheetController.getStateOfBottomSheet(BottomSheetType.AROUND_PLACES) == BottomSheetBehavior.STATE_COLLAPSED) {
				bottomSheetController.collapseAllExpandedBottomSheets();
				bottomSheetController.setStateOfBottomSheet(BottomSheetType.AROUND_PLACES, BottomSheetBehavior.STATE_EXPANDED);
			} else {
				bottomSheetController.setStateOfBottomSheet(BottomSheetType.AROUND_PLACES, BottomSheetBehavior.STATE_COLLAPSED);
				getParentFragmentManager().popBackStack();
			}
		}
	};

	private final MarkerOnClickListener markerOnClickListener = new MarkerOnClickListener() {
		@Override
		public void onClickedMarker() {
			bottomSheetController.setStateOfBottomSheet(BottomSheetType.AROUND_PLACES, BottomSheetBehavior.STATE_COLLAPSED);
		}
	};

	private final OnPoiItemClickListener onPoiItemClickListener = new OnPoiItemClickListener() {
		@Override
		public void onPOIItemSelectedByList(KakaoLocalDocument kakaoLocalDocument, MarkerType markerType, MarkerOnClickListener markerOnClickListener) {
			bottomSheetController.setStateOfBottomSheet(BottomSheetType.AROUND_PLACES, BottomSheetBehavior.STATE_COLLAPSED);
			mapOnPoiItemClickListener.onPOIItemSelectedByList(kakaoLocalDocument, markerType, AroundPlacesHeaderFragment.this.markerOnClickListener);
		}

		@Override
		public void onPOIItemSelectedByBottomSheet(int position, MarkerType markerType, MarkerOnClickListener markerOnClickListener) {

		}
	};

	public void setiConnectContents(IConnectContents iConnectContents) {
		this.iConnectContents = iConnectContents;
	}

	public void setOnExtraListDataListener(OnExtraListDataListener<Constant> onExtraListDataListener) {
		this.onExtraListDataListener = onExtraListDataListener;
	}

	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);
		requireActivity().getOnBackPressedDispatcher().addCallback(onBackPressedCallback);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		customPlaceCategoryRepository = new CustomPlaceCategoryRepository(getContext());
		mapViewModel = new ViewModelProvider(requireActivity()).get(MapViewModel.class);
		iMap = mapViewModel.getiMapData();
		bottomSheetController = mapViewModel.getBottomSheetController();
		mapOnPoiItemClickListener = mapViewModel.getPoiItemOnClickListener();

		Bundle bundle = getArguments();
		if (bundle != null && bundle.containsKey("locationDto")) {
			currentSearchMapPointCriteria = LocalApiPlaceParameter.SEARCH_CRITERIA_MAP_POINT_CURRENT_LOCATION;
			promiseLocationDto = (LocationDto) getArguments().getSerializable("locationDto");
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		binding = FragmentAroundPlacesHeaderBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		binding.header.fragmentTitle.setText(R.string.around_place);
		binding.header.backBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				bottomSheetController.setStateOfBottomSheet(BottomSheetType.AROUND_PLACES, BottomSheetBehavior.STATE_COLLAPSED);
				getParentFragmentManager().popBackStack();
			}
		});

		if (promiseLocationDto == null) {
			binding.searchAroundPromiseLocation.setVisibility(View.GONE);
		}

		init();
	}

	@Override
	public void onDestroy() {
		onBackPressedCallback.remove();
		super.onDestroy();
	}

	private void init() {
		customPlaceCategoryRepository.getAll(new OnDbQueryCallback<List<CustomPlaceCategoryDto>>() {
			@Override
			public void onResult(List<CustomPlaceCategoryDto> customPlaceCategoryDtoList) {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						List<String> placeCategoryList = new ArrayList<>();
						String[] placeCategoryArr = getResources().getStringArray(R.array.KakaoLocationitems);
						placeCategoryList.addAll(Arrays.asList(placeCategoryArr));

						for (CustomPlaceCategoryDto dto : customPlaceCategoryDtoList) {
							placeCategoryList.add(dto.getName());
						}
						Bundle bundle = null;
						AroundPlacesContentsFragment.PlaceFragment placeFragment = null;
						List<AroundPlacesContentsFragment.PlaceFragment> fragmentList = new ArrayList<>();

						for (String name : placeCategoryList) {
							TabLayout.Tab tab = binding.categoryTabLayout.newTab();
							tab.setContentDescription(name);
							tab.setText(name);

							binding.categoryTabLayout.addTab(tab);

							bundle = new Bundle();
							bundle.putString("category", name);
							bundle.putSerializable("locationDto", promiseLocationDto);

							placeFragment = new AroundPlacesContentsFragment.PlaceFragment(markerOnClickListener, onPoiItemClickListener,
									AroundPlacesHeaderFragment.this);

							placeFragment.setArguments(bundle);
							fragmentList.add(placeFragment);
						}

						iConnectContents.setViewPager(fragmentList);

						new TabLayoutMediator(binding.categoryTabLayout, iConnectContents.getViewPager2(), new TabLayoutMediator.TabConfigurationStrategy() {
							@Override
							public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
								tab.setText(placeCategoryList.get(position));
							}
						}).attach();

						binding.searchCriteriaToggleGroup.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
							@Override
							public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
								if (isChecked) {
									switch (checkedId) {
										case R.id.search_around_promise_location:
											currentSearchMapPointCriteria = LocalApiPlaceParameter.SEARCH_CRITERIA_MAP_POINT_CURRENT_LOCATION;
											break;
										case R.id.search_around_map_center:
											currentSearchMapPointCriteria = LocalApiPlaceParameter.SEARCH_CRITERIA_MAP_POINT_MAP_CENTER;
											break;
									}

								}
							}
						});
						binding.searchCriteriaToggleGroup.check(R.id.search_around_promise_location);
					}
				});
			}
		});

	}


	@Override
	public void loadExtraListData(Constant e, RecyclerView.AdapterDataObserver adapterDataObserver) {

	}

	@Override
	public void loadExtraListData(RecyclerView.AdapterDataObserver adapterDataObserver) {
		int position = binding.categoryTabLayout.getSelectedTabPosition();
		iConnectContents.loadExtraData(position, adapterDataObserver);
	}

	@Override
	public int getSearchMapPointCriteria() {
		return currentSearchMapPointCriteria;
	}

	@Override
	public int getSearchSortCriteria() {
		return currentSearchSortTypeCriteria;
	}

}