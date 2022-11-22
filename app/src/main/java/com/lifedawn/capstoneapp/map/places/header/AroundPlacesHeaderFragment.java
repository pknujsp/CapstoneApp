package com.lifedawn.capstoneapp.map.places.header;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.tabs.TabLayout;
import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.common.interfaces.OnDbQueryCallback;
import com.lifedawn.capstoneapp.common.repository.CustomPlaceCategoryRepository;
import com.lifedawn.capstoneapp.map.BottomSheetType;
import com.lifedawn.capstoneapp.model.firestore.PlaceDto;
import com.lifedawn.capstoneapp.map.MarkerType;
import com.lifedawn.capstoneapp.map.places.content.AroundPlacesSearchContentViewPagerItemFragment;
import com.lifedawn.capstoneapp.map.places.parent.AbstractSearchHeaderFragment;
import com.lifedawn.capstoneapp.retrofits.parameters.LocalApiPlaceParameter;
import com.lifedawn.capstoneapp.room.dto.CustomPlaceCategoryDto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class AroundPlacesHeaderFragment extends AbstractSearchHeaderFragment {
	private CustomPlaceCategoryRepository customPlaceCategoryRepository;

	public AroundPlacesHeaderFragment() {
		super(BottomSheetType.AROUND_PLACES);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		customPlaceCategoryRepository = new CustomPlaceCategoryRepository(getContext());

		if (bundle != null) {
			if (bundle.containsKey("locationDto")) {
				currentSearchMapPointCriteria = LocalApiPlaceParameter.SEARCH_CRITERIA_MAP_POINT_CURRENT_LOCATION;
				searchPlaceShareViewModel.setPromiseLocationDto((PlaceDto) bundle.getSerializable("locationDto"));
			}
		} else {
			currentSearchMapPointCriteria = LocalApiPlaceParameter.SEARCH_CRITERIA_MAP_POINT_MAP_CENTER;
		}


		searchPlaceShareViewModel.setCriteriaType(currentSearchMapPointCriteria);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		binding.header.fragmentTitle.setText(R.string.around_place);

		if (!bundle.containsKey("locationDto")) {
			binding.searchAroundPromiseLocation.setVisibility(View.GONE);
		}
		init(null);
	}

	@Override
	protected void init(Integer lastPositionOnTab) {
		if (lastPositionOnTab == null) {
			customPlaceCategoryRepository.getAll(new OnDbQueryCallback<List<CustomPlaceCategoryDto>>() {
				@Override
				public void onResult(List<CustomPlaceCategoryDto> customPlaceCategoryDtoList) {
					if (getActivity() == null) {
						return;
					}

					tabNameList.clear();
					String[] placeCategoryArr = getResources().getStringArray(R.array.KakaoLocationitems);
					tabNameList.addAll(Arrays.asList(placeCategoryArr));

					for (CustomPlaceCategoryDto dto : customPlaceCategoryDtoList) {
						tabNameList.add(dto.getName());
					}

					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							createTabs(tabNameList);
							binding.searchCriteriaToggleGroup.check(currentSearchMapPointCriteria == LocalApiPlaceParameter.SEARCH_CRITERIA_MAP_POINT_MAP_CENTER ?
									R.id.search_around_map_center : R.id.search_around_promise_location);
						}
					});

				}
			});

		} else {
			iMap.removeMarkers(MarkerType.AROUND_PLACE);
			bottomSheetController.setStateOfBottomSheet(BottomSheetType.LOCATION_ITEM, BottomSheetBehavior.STATE_COLLAPSED);

			if (binding.categoryTabLayout.getTabCount() > 0) {
				binding.categoryTabLayout.clearOnTabSelectedListeners();
				binding.categoryTabLayout.removeAllTabs();
			}

			createTabs(tabNameList);
			binding.categoryTabLayout.selectTab(binding.categoryTabLayout.getTabAt(lastPositionOnTab), true);
		}
	}

	@Override
	protected void createTabs(List<String> tabNamesList) {
		Bundle bundle = null;
		AroundPlacesSearchContentViewPagerItemFragment placeFragment = null;
		List<AroundPlacesSearchContentViewPagerItemFragment> fragmentList = new ArrayList<>();

		if (binding.categoryTabLayout.getTabCount() > 0) {
			binding.categoryTabLayout.removeAllTabs();
		}

		for (String name : tabNamesList) {
			TabLayout.Tab tab = binding.categoryTabLayout.newTab();
			tab.setContentDescription(name);
			tab.setText(name);

			binding.categoryTabLayout.addTab(tab);

			bundle = new Bundle();
			bundle.putString("query", name);

			placeFragment = new AroundPlacesSearchContentViewPagerItemFragment(markerOnClickListener, onPoiItemClickListener,
					AroundPlacesHeaderFragment.this);

			placeFragment.setArguments(bundle);
			fragmentList.add(placeFragment);
		}

		iConnectContents.setViewPager(fragmentList);
		super.createTabs(tabNamesList);
	}

	@Override
	public void onDestroy() {
		iMap.removeMarkers(MarkerType.AROUND_PLACE);
		super.onDestroy();
	}

}