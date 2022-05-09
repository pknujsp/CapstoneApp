package com.lifedawn.capstoneapp.kakao.restaurant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.tabs.TabLayout;
import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.common.interfaces.OnDbQueryCallback;
import com.lifedawn.capstoneapp.map.BottomSheetType;
import com.lifedawn.capstoneapp.map.LocationDto;
import com.lifedawn.capstoneapp.map.MarkerType;
import com.lifedawn.capstoneapp.map.places.content.AroundPlacesSearchContentViewPagerItemFragment;
import com.lifedawn.capstoneapp.map.places.header.AroundPlacesHeaderFragment;
import com.lifedawn.capstoneapp.map.places.parent.AbstractSearchHeaderFragment;
import com.lifedawn.capstoneapp.retrofits.parameters.LocalApiPlaceParameter;
import com.lifedawn.capstoneapp.room.dto.CustomPlaceCategoryDto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RestaurantHeaderFragment extends AbstractSearchHeaderFragment {

	public RestaurantHeaderFragment() {
		super(BottomSheetType.AROUND_PLACES);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (bundle.containsKey("locationDto")) {
			currentSearchMapPointCriteria = LocalApiPlaceParameter.SEARCH_CRITERIA_MAP_POINT_CURRENT_LOCATION;
			searchPlaceShareViewModel.setPromiseLocationDto((LocationDto) bundle.getSerializable("locationDto"));
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
		binding.header.fragmentTitle.setText(R.string.restaurant);

		if (!bundle.containsKey("locationDto")) {
			binding.searchAroundPromiseLocation.setVisibility(View.GONE);
		}

		init(null);
	}

	@Override
	protected void init(Integer lastPositionOnTab) {
		if (lastPositionOnTab == null) {
			tabNameList.clear();
			tabNameList.addAll(Arrays.asList(getResources().getStringArray(R.array.food_menu_list)));

			createTabs(tabNameList);
			binding.searchCriteriaToggleGroup.check(currentSearchMapPointCriteria == LocalApiPlaceParameter.SEARCH_CRITERIA_MAP_POINT_MAP_CENTER ?
					R.id.search_around_map_center : R.id.search_around_promise_location);
		} else {
			iMap.removeMarkers(MarkerType.RESTAURANT);
			bottomSheetController.setStateOfBottomSheet(BottomSheetType.LOCATION_ITEM, BottomSheetBehavior.STATE_COLLAPSED);

			if (binding.categoryTabLayout.getTabCount() > 0) {
				binding.categoryTabLayout.clearOnTabSelectedListeners();
				binding.categoryTabLayout.removeAllTabs();
			}

			createTabs(tabNameList);
			binding.categoryTabLayout.selectTab(binding.categoryTabLayout.getTabAt(lastPositionOnTab));
		}
	}


	@Override
	protected void createTabs(List<String> tabNamesList) {
		Bundle bundle = null;
		RestaurantContentViewPagerItemFragment fragment = null;
		List<RestaurantContentViewPagerItemFragment> fragmentList = new ArrayList<>();

		if (binding.categoryTabLayout.getTabCount() > 0) {
			binding.categoryTabLayout.removeAllTabs();
		}

		for (String name : tabNameList) {
			TabLayout.Tab tab = binding.categoryTabLayout.newTab();
			tab.setContentDescription(name);
			tab.setText(name);

			binding.categoryTabLayout.addTab(tab);

			bundle = new Bundle();
			bundle.putString("query", name);

			fragment = new RestaurantContentViewPagerItemFragment(markerOnClickListener, onPoiItemClickListener,
					RestaurantHeaderFragment.this);

			fragment.setArguments(bundle);
			fragmentList.add(fragment);
		}

		iConnectContents.setViewPager(fragmentList);
		super.createTabs(tabNamesList);
	}


	@Override
	public void onDestroy() {
		iMap.removeMarkers(MarkerType.RESTAURANT);
		super.onDestroy();
	}
}
