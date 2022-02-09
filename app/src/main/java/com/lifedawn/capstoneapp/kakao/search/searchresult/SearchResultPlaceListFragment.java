package com.lifedawn.capstoneapp.kakao.search.searchresult;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.LocationResult;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.common.constants.Constant;
import com.lifedawn.capstoneapp.common.interfaces.OnClickedListItemListener;
import com.lifedawn.capstoneapp.common.util.FusedLocation;
import com.lifedawn.capstoneapp.databinding.FragmentLocationSearchResultBinding;
import com.lifedawn.capstoneapp.databinding.PlaceRecyclerViewItemBinding;
import com.lifedawn.capstoneapp.kakao.search.LocalParameterUtil;
import com.lifedawn.capstoneapp.kakao.search.callback.PlaceItemCallback;
import com.lifedawn.capstoneapp.kakao.search.util.MapUtil;
import com.lifedawn.capstoneapp.kakao.search.viewmodel.PlacesViewModel;
import com.lifedawn.capstoneapp.map.MapViewModel;
import com.lifedawn.capstoneapp.map.MarkerType;
import com.lifedawn.capstoneapp.map.interfaces.IMapData;
import com.lifedawn.capstoneapp.map.interfaces.OnExtraListDataListener;
import com.lifedawn.capstoneapp.retrofits.parameters.LocalApiPlaceParameter;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.place.PlaceResponse;
import com.naver.maps.geometry.LatLng;

public class SearchResultPlaceListFragment extends Fragment implements OnExtraListDataListener<Constant> {
	private final String QUERY;
	private final OnClickedListItemListener<PlaceResponse.Documents> placeDocumentsOnClickedListItem;
	
	private int currentSearchMapPointCriteria = LocalApiPlaceParameter.SEARCH_CRITERIA_MAP_POINT_MAP_CENTER;
	private int currentSearchSortTypeCriteria = LocalApiPlaceParameter.SEARCH_CRITERIA_SORT_TYPE_ACCURACY;
	
	private FragmentLocationSearchResultBinding binding;
	private PlacesViewModel viewModel;
	
	private ArrayAdapter<CharSequence> spinnerAdapter;
	private PlacesAdapter adapter;
	
	private Location currentLocation;
	private FusedLocation fusedLocation;
	private MapViewModel mapViewModel;
	private IMapData iMapData;
	
	public SearchResultPlaceListFragment(String query, OnClickedListItemListener<PlaceResponse.Documents> placeDocumentsOnClickedListItem) {
		this.QUERY = query;
		this.placeDocumentsOnClickedListItem = placeDocumentsOnClickedListItem;
	}
	
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		fusedLocation = FusedLocation.getInstance(getContext());
		
		mapViewModel = new ViewModelProvider(getActivity()).get(MapViewModel.class);
		iMapData = mapViewModel.getiMapData();
	}
	
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		binding = FragmentLocationSearchResultBinding.inflate(inflater);
		return binding.getRoot();
	}
	
	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.searchResultType.setText(getString(R.string.result_place));
		
		spinnerAdapter = ArrayAdapter.createFromResource(getContext(), R.array.map_search_result_sort_spinner,
				android.R.layout.simple_spinner_item);
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		binding.searchSortSpinner.setAdapter(spinnerAdapter);
		binding.searchSortSpinner.setSelection(
				currentSearchSortTypeCriteria == LocalApiPlaceParameter.SEARCH_CRITERIA_SORT_TYPE_ACCURACY ? 1 : 0, false);
		binding.searchSortSpinner.setOnItemSelectedListener(onItemSelectedListener);
		viewModel = new ViewModelProvider(this).get(PlacesViewModel.class);
		
		binding.searchResultRecyclerview.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
		binding.searchResultRecyclerview.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
		
		binding.searchCriteriaToggleGroup.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
			@Override
			public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
				if (isChecked) {
					fusedLocation.cancel();
					
					switch (checkedId) {
						case R.id.search_around_current_location:
							currentSearchMapPointCriteria = LocalApiPlaceParameter.SEARCH_CRITERIA_MAP_POINT_CURRENT_LOCATION;
							requestPlacesByGps(QUERY);
							break;
						case R.id.search_around_map_center:
							currentSearchMapPointCriteria = LocalApiPlaceParameter.SEARCH_CRITERIA_MAP_POINT_MAP_CENTER;
							requestPlaces(QUERY);
							break;
					}
				}
			}
		});
		
		binding.searchCriteriaToggleGroup.check(R.id.search_around_map_center);
		
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	private final AdapterView.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
			switch (index) {
				case 0:
					//거리 순서
					currentSearchSortTypeCriteria = LocalApiPlaceParameter.SEARCH_CRITERIA_SORT_TYPE_DISTANCE;
					break;
				case 1:
					//정확도 순서
					currentSearchSortTypeCriteria = LocalApiPlaceParameter.SEARCH_CRITERIA_SORT_TYPE_ACCURACY;
					break;
			}
			
			if (currentSearchMapPointCriteria == LocalApiPlaceParameter.SEARCH_CRITERIA_MAP_POINT_CURRENT_LOCATION) {
				requestPlacesByGps(QUERY);
			} else {
				requestPlaces(QUERY);
			}
		}
		
		@Override
		public void onNothingSelected(AdapterView<?> adapterView) {
		
		}
	};
	
	private void requestPlacesByGps(String query) {
		fusedLocation.startLocationUpdates(new FusedLocation.MyLocationCallback() {
			@Override
			public void onSuccessful(LocationResult locationResult) {
				currentLocation = locationResult.getLocations().get(0);
				requestPlaces(query);
			}
			
			@Override
			public void onFailed(Fail fail) {
				Toast.makeText(getContext(), R.string.failed_find_current_location, Toast.LENGTH_SHORT).show();
				binding.searchCriteriaToggleGroup.check(R.id.search_around_map_center);
			}
		}, false);
		
	}
	
	private void requestPlaces(String query) {
		String latitude = null;
		String longitude = null;
		
		if (currentSearchMapPointCriteria == LocalApiPlaceParameter.SEARCH_CRITERIA_MAP_POINT_CURRENT_LOCATION) {
			latitude = String.valueOf(currentLocation.getLatitude());
			longitude = String.valueOf(currentLocation.getLongitude());
		} else {
			LatLng latLng = mapViewModel.getMapCenterPoint();
			latitude = String.valueOf(latLng.latitude);
			longitude = String.valueOf(latLng.longitude);
		}
		
		LocalApiPlaceParameter parameter = LocalParameterUtil.getPlaceParameter(query, latitude, longitude,
				LocalApiPlaceParameter.DEFAULT_SIZE, LocalApiPlaceParameter.DEFAULT_PAGE, currentSearchSortTypeCriteria);
		
		adapter = new PlacesAdapter(getContext(), placeDocumentsOnClickedListItem);
		adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
			@Override
			public void onItemRangeInserted(int positionStart, int itemCount) {
				super.onItemRangeInserted(positionStart, itemCount);
				if (positionStart > 0) {
					iMapData.addExtraMarkers(adapter.getCurrentList().snapshot(), MarkerType.SEARCH_RESULT_PLACE);
				} else {
					if (itemCount > 0) {
						iMapData.createMarkers(adapter.getCurrentList().snapshot(), MarkerType.SEARCH_RESULT_PLACE);
					}
				}
			}
		});
		binding.searchResultRecyclerview.setAdapter(adapter);
		
		viewModel.init(parameter, new PagedList.BoundaryCallback<PlaceResponse.Documents>() {
			@Override
			public void onZeroItemsLoaded() {
				super.onZeroItemsLoaded();
			}
		});
		viewModel.getPagedListMutableLiveData().observe(getViewLifecycleOwner(), new Observer<PagedList<PlaceResponse.Documents>>() {
			@Override
			public void onChanged(PagedList<PlaceResponse.Documents> placeDocuments) {
				adapter.submitList(placeDocuments);
			}
		});
	}
	
	@Override
	public void loadExtraListData(Constant e, RecyclerView.AdapterDataObserver adapterDataObserver) {
	
	}
	
	@Override
	public void loadExtraListData(RecyclerView.AdapterDataObserver adapterDataObserver) {
		adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
			@Override
			public void onItemRangeInserted(int positionStart, int itemCount) {
				super.onItemRangeInserted(positionStart, itemCount);
				adapterDataObserver.onItemRangeInserted(positionStart, itemCount);
				adapter.unregisterAdapterDataObserver(this);
			}
		});
		binding.searchResultRecyclerview.scrollBy(0, 10000);
	}
	
	
	private static class PlacesAdapter extends PagedListAdapter<PlaceResponse.Documents, PlacesAdapter.ItemViewHolder> {
		private Context context;
		private final OnClickedListItemListener<PlaceResponse.Documents> onClickedListItem;
		
		public PlacesAdapter(Context context, OnClickedListItemListener<PlaceResponse.Documents> onClickedListItem) {
			super(new PlaceItemCallback());
			this.context = context;
			this.onClickedListItem = onClickedListItem;
		}
		
		private class ItemViewHolder extends RecyclerView.ViewHolder {
			private PlaceRecyclerViewItemBinding binding;
			
			public ItemViewHolder(View view) {
				super(view);
				binding = PlaceRecyclerViewItemBinding.bind(view);
			}
			
			public void bind() {
				PlaceResponse.Documents item = getItem(getBindingAdapterPosition());
				
				binding.placeName.setText(item.getPlaceName());
				binding.placeIndex.setText(String.valueOf(getBindingAdapterPosition() + 1));
				binding.placeCategory.setText(item.getCategoryName());
				binding.placeAddressName.setText(item.getAddressName());
				binding.placeDistance.setText(MapUtil.convertMeterToKm(Double.parseDouble(item.getDistance())));
				
				itemView.getRootView().setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						onClickedListItem.onClicked(getItem(getBindingAdapterPosition()));
					}
				});
			}
		}
		
		
		@NonNull
		@Override
		public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			return new PlacesAdapter.ItemViewHolder(
					LayoutInflater.from(parent.getContext()).inflate(R.layout.place_recycler_view_item, parent, false));
		}
		
		@Override
		public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
			holder.bind();
		}
		
		
	}
}
