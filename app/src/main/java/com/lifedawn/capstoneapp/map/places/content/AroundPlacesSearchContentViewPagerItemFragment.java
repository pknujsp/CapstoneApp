package com.lifedawn.capstoneapp.map.places.content;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.common.constants.Constant;
import com.lifedawn.capstoneapp.common.interfaces.OnClickedListItemListener;
import com.lifedawn.capstoneapp.databinding.PlaceRecyclerViewItemBinding;
import com.lifedawn.capstoneapp.kakao.search.callback.PlaceItemCallback;
import com.lifedawn.capstoneapp.kakao.search.util.MapUtil;
import com.lifedawn.capstoneapp.kakao.search.viewmodel.PlacesViewModel;
import com.lifedawn.capstoneapp.map.MarkerType;
import com.lifedawn.capstoneapp.map.interfaces.MarkerOnClickListener;
import com.lifedawn.capstoneapp.map.interfaces.OnPoiItemClickListener;
import com.lifedawn.capstoneapp.map.places.interfaces.IConnectHeader;
import com.lifedawn.capstoneapp.map.places.parent.AbstractSearchContentViewPagerItemFragment;
import com.lifedawn.capstoneapp.retrofits.parameters.LocalApiPlaceParameter;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.place.PlaceResponse;

public class AroundPlacesSearchContentViewPagerItemFragment extends AbstractSearchContentViewPagerItemFragment {
	private PlacesViewModel placesViewModel;

	public AroundPlacesSearchContentViewPagerItemFragment(MarkerOnClickListener markerOnClickListener, OnPoiItemClickListener onPoiItemClickListener, IConnectHeader iConnectHeader) {
		super(markerOnClickListener, onPoiItemClickListener, iConnectHeader);
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		placesViewModel = new ViewModelProvider(this).get(PlacesViewModel.class);
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}


	@Override
	public void requestQuery() {
		super.requestQuery();
	}

	@Override
	public void setAdapter(LocalApiPlaceParameter parameter) {
		adapter = new PlacesSearchResultAdapter(getContext(), new OnClickedListItemListener<PlaceResponse.Documents>() {
			@Override
			public void onClicked(PlaceResponse.Documents e) {
				onPoiItemClickListener.onPOIItemSelectedByList(e, MarkerType.AROUND_PLACE, null);
			}
		});

		adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
			@Override
			public void onItemRangeInserted(int positionStart, int itemCount) {
				super.onItemRangeInserted(positionStart, itemCount);
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						binding.progressLayout.onSuccessful();
					}
				});

				if (positionStart > 0) {
					iMap.addExtraMarkers(adapter.getCurrentList().snapshot(), MarkerType.AROUND_PLACE, markerOnClickListener);
				} else {
					if (itemCount > 0) {
						iMap.createMarkers(adapter.getCurrentList().snapshot(), MarkerType.AROUND_PLACE, markerOnClickListener);
						iMap.showMarkers(MarkerType.AROUND_PLACE);
					}
				}
			}
		});
		binding.recyclerView.setAdapter(adapter);

		placesViewModel.init(parameter, new PagedList.BoundaryCallback<PlaceResponse.Documents>() {
			@Override
			public void onZeroItemsLoaded() {
				super.onZeroItemsLoaded();
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						binding.progressLayout.onFailed(getString(R.string.empty_search_result));
					}
				});

			}
		});

		placesViewModel.getPagedListMutableLiveData().observe(getViewLifecycleOwner(), new Observer<PagedList<PlaceResponse.Documents>>() {
			@Override
			public void onChanged(PagedList<PlaceResponse.Documents> placeDocuments) {
				adapter.submitList(placeDocuments);
			}
		});

	}

}
