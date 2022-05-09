package com.lifedawn.capstoneapp.map.places.header;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.common.interfaces.OnDbQueryCallback;
import com.lifedawn.capstoneapp.common.repository.CustomPlaceCategoryRepository;
import com.lifedawn.capstoneapp.map.LocationDto;
import com.lifedawn.capstoneapp.map.places.content.AroundPlacesContentsViewPagerFragment;
import com.lifedawn.capstoneapp.map.places.content.AroundPlacesSearchContentViewPagerItemFragment;
import com.lifedawn.capstoneapp.map.places.parent.AbstractSearchHeaderFragment;
import com.lifedawn.capstoneapp.retrofits.parameters.LocalApiPlaceParameter;
import com.lifedawn.capstoneapp.room.dto.CustomPlaceCategoryDto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class AroundPlacesHeaderFragment extends AbstractSearchHeaderFragment {
	private CustomPlaceCategoryRepository customPlaceCategoryRepository;
	private LocationDto promiseLocationDto;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		customPlaceCategoryRepository = new CustomPlaceCategoryRepository(getContext());

		if (bundle.containsKey("promiseLocationDto")) {
			currentSearchMapPointCriteria = LocalApiPlaceParameter.SEARCH_CRITERIA_MAP_POINT_CURRENT_LOCATION;
			searchPlaceShareViewModel.setPromiseLocationDto((LocationDto) bundle.getSerializable("promiseLocationDto"));
		} else {
			binding.searchAroundPromiseLocation.setVisibility(View.GONE);
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
		init();
	}

	@Override
	protected void init() {
		super.init();

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
						AroundPlacesSearchContentViewPagerItemFragment placeFragment = null;
						List<AroundPlacesSearchContentViewPagerItemFragment> fragmentList = new ArrayList<>();

						for (String name : placeCategoryList) {
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

						createTabs(fragmentList, placeCategoryList);
					}
				});
			}
		});

	}


}