package com.lifedawn.capstoneapp.kakao.restaurant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.tabs.TabLayout;
import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.common.interfaces.OnDbQueryCallback;
import com.lifedawn.capstoneapp.common.repository.CustomPlaceCategoryRepository;
import com.lifedawn.capstoneapp.kakao.search.viewmodel.RestaurantViewModel;
import com.lifedawn.capstoneapp.map.LocationDto;
import com.lifedawn.capstoneapp.map.places.AbstractSearchHeaderFragment;
import com.lifedawn.capstoneapp.map.places.AroundPlacesContentsFragment;
import com.lifedawn.capstoneapp.map.places.AroundPlacesHeaderFragment;
import com.lifedawn.capstoneapp.retrofits.parameters.LocalApiPlaceParameter;
import com.lifedawn.capstoneapp.room.dto.CustomPlaceCategoryDto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RestaurantHeaderFragment extends AbstractSearchHeaderFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (bundle.containsKey("locationDto")) {
			currentSearchMapPointCriteria = LocalApiPlaceParameter.SEARCH_CRITERIA_MAP_POINT_CURRENT_LOCATION;
		} else {
			currentSearchMapPointCriteria = LocalApiPlaceParameter.SEARCH_CRITERIA_MAP_POINT_MAP_CENTER;
		}
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

						createTabs(fragmentList, placeCategoryList);
					}
				});
			}
		});

	}

}
