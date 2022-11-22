package com.lifedawn.capstoneapp.map.places.content;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lifedawn.capstoneapp.map.MarkerType;
import com.lifedawn.capstoneapp.map.places.parent.AbstractSearchContentViewPagerFragment;


public class AroundPlacesContentsViewPagerFragment extends AbstractSearchContentViewPagerFragment {

	public AroundPlacesContentsViewPagerFragment() {
		super(MarkerType.AROUND_PLACE);
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