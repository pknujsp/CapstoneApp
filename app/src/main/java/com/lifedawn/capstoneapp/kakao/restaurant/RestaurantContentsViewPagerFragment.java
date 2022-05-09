package com.lifedawn.capstoneapp.kakao.restaurant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lifedawn.capstoneapp.map.MarkerType;
import com.lifedawn.capstoneapp.map.places.parent.AbstractSearchContentViewPagerFragment;

public class RestaurantContentsViewPagerFragment extends AbstractSearchContentViewPagerFragment {

	public RestaurantContentsViewPagerFragment() {
		super(MarkerType.RESTAURANT);
	}

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
	}

}