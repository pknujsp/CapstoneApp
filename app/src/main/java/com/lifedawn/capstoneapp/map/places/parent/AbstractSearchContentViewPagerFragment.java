package com.lifedawn.capstoneapp.map.places.parent;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.lifedawn.capstoneapp.common.constants.Constant;
import com.lifedawn.capstoneapp.databinding.FragmentAbstractSearchContentBinding;
import com.lifedawn.capstoneapp.kakao.search.viewmodel.SearchPlaceShareViewModel;
import com.lifedawn.capstoneapp.map.MapViewModel;
import com.lifedawn.capstoneapp.map.MarkerType;
import com.lifedawn.capstoneapp.map.interfaces.IMap;
import com.lifedawn.capstoneapp.map.interfaces.OnExtraListDataListener;
import com.lifedawn.capstoneapp.map.places.content.ContentViewPagerAdapter;
import com.lifedawn.capstoneapp.map.places.interfaces.IConnectContents;
import com.naver.maps.geometry.LatLng;

import java.util.List;

public abstract class AbstractSearchContentViewPagerFragment extends Fragment implements OnExtraListDataListener<Constant>, IConnectContents {
	protected FragmentAbstractSearchContentBinding binding;
	protected ContentViewPagerAdapter viewPagerAdapter;
	protected IMap iMap;
	protected SearchPlaceShareViewModel searchPlaceShareViewModel;
	protected MapViewModel mapViewModel;
	protected MarkerType markerType;

	protected ViewPager2.OnPageChangeCallback onPageChangeCallback;


	public AbstractSearchContentViewPagerFragment(MarkerType markerType) {
		this.markerType = markerType;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		searchPlaceShareViewModel = new ViewModelProvider(requireActivity()).get(SearchPlaceShareViewModel.class);
		mapViewModel = new ViewModelProvider(requireActivity()).get(MapViewModel.class);
		iMap = mapViewModel.getiMapData();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		binding = FragmentAbstractSearchContentBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}


	@Override
	public void loadExtraListData(Constant e, RecyclerView.AdapterDataObserver adapterDataObserver) {

	}

	@Override
	public void loadExtraListData(RecyclerView.AdapterDataObserver adapterDataObserver) {

	}

	@Override
	public void loadPlaces(int tabPosition) {

	}

	@Override
	public void loadExtraData(int tabPosition, RecyclerView.AdapterDataObserver adapterDataObserver) {
		viewPagerAdapter.getFragment(tabPosition).loadExtraListData(adapterDataObserver);
	}

	@Override
	public void setViewPager(List<? extends AbstractSearchContentViewPagerItemFragment> fragmentList) {
		viewPagerAdapter = new ContentViewPagerAdapter(this, fragmentList);
		binding.viewPager.setAdapter(viewPagerAdapter);

		if (onPageChangeCallback != null) {
			binding.viewPager.unregisterOnPageChangeCallback(onPageChangeCallback);
			onPageChangeCallback = null;
		}

		onPageChangeCallback = new ViewPager2.OnPageChangeCallback() {
			int lastPosition;
			boolean initializing = true;
			AbstractSearchContentViewPagerItemFragment fragment;

			@Override
			public void onPageSelected(int position) {
				fragment = viewPagerAdapter.getFragment(position);

				if (!initializing) {
					if (fragment.adapter != null) {
						Log.e("뷰페이저 체크", fragment.query + " : adapter is not null");
						iMap.removeMarkers(markerType);
						iMap.createMarkers(fragment.adapter.getCurrentList().snapshot(), markerType, fragment.markerOnClickListener);
						iMap.showMarkers(markerType);
					} else {
						Log.e("뷰페이저 체크", fragment.query + " : adapter is null");
						fragment.getLifecycle().addObserver(new DefaultLifecycleObserver() {
							@Override
							public void onStart(@NonNull LifecycleOwner owner) {
								Log.e("뷰페이저 체크", fragment.query + " : onStart");
								DefaultLifecycleObserver.super.onStart(owner);
								fragment.requestQuery();
							}
						});
					}

				} else {
					fragment.getLifecycle().addObserver(new DefaultLifecycleObserver() {
						@Override
						public void onStart(@NonNull LifecycleOwner owner) {
							DefaultLifecycleObserver.super.onStart(owner);
							fragment.requestQuery();
						}
					});
				}

				initializing = false;
				lastPosition = position;
			}
		};

		binding.viewPager.registerOnPageChangeCallback(onPageChangeCallback);
	}

	@Override
	public ViewPager2 getViewPager2() {
		return binding.viewPager;
	}

}