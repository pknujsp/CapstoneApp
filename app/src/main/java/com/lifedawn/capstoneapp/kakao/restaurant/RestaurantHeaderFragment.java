package com.lifedawn.capstoneapp.kakao.restaurant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.tabs.TabLayout;
import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.kakao.search.viewmodel.SearchPlaceShareViewModel;
import com.lifedawn.capstoneapp.map.LocationDto;
import com.lifedawn.capstoneapp.map.places.parent.AbstractSearchContentViewPagerItemFragment;
import com.lifedawn.capstoneapp.map.places.parent.AbstractSearchHeaderFragment;
import com.lifedawn.capstoneapp.map.places.content.AroundPlacesContentsViewPagerFragment;
import com.lifedawn.capstoneapp.retrofits.parameters.LocalApiPlaceParameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RestaurantHeaderFragment extends AbstractSearchHeaderFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.header.fragmentTitle.setText(R.string.restaurant);

		if (bundle.containsKey("promiseLocationDto")) {
			currentSearchMapPointCriteria = LocalApiPlaceParameter.SEARCH_CRITERIA_MAP_POINT_CURRENT_LOCATION;
			searchPlaceShareViewModel.setPromiseLocationDto((LocationDto) bundle.getSerializable("promiseLocationDto"));
		} else {
			binding.searchAroundPromiseLocation.setVisibility(View.GONE);
			currentSearchMapPointCriteria = LocalApiPlaceParameter.SEARCH_CRITERIA_MAP_POINT_MAP_CENTER;
		}

		searchPlaceShareViewModel.setCriteriaType(currentSearchMapPointCriteria);
		init();
	}

	@Override
	protected void init() {
		super.init();

		/*
		List<String> foodMenuList = new ArrayList<>();
		String[] foodMenuArr = getResources().getStringArray(R.array.food_menu_list);
		foodMenuList.addAll(Arrays.asList(foodMenuArr));

		Bundle bundle = null;
		AroundPlacesContentsViewPagerFragment.PlaceFragment fragment = null;
		List<AbstractSearchContentViewPagerItemFragment> fragmentList = new ArrayList<>();

		for (String name : foodMenuList) {
			TabLayout.Tab tab = binding.categoryTabLayout.newTab();
			tab.setContentDescription(name);
			tab.setText(name);

			binding.categoryTabLayout.addTab(tab);

			bundle = new Bundle();
			bundle.putString("foodMenuName", name);

			fragment = new AroundPlacesContentsViewPagerFragment.PlaceFragment(markerOnClickListener, onPoiItemClickListener,
					RestaurantHeaderFragment.this);

			fragment.setArguments(bundle);
			fragmentList.add(fragment);
		}

		createTabs(fragmentList, foodMenuList);

		 */
	}

}
