package com.lifedawn.capstoneapp.kakao.search.searchresult;

import android.location.Location;
import android.os.Bundle;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.lifedawn.capstoneapp.common.constants.Constant;
import com.lifedawn.capstoneapp.common.interfaces.HttpCallback;
import com.lifedawn.capstoneapp.common.interfaces.OnClickedListItemListener;
import com.lifedawn.capstoneapp.databinding.FragmentSearchResultMainBinding;
import com.lifedawn.capstoneapp.kakao.search.LocalParameterUtil;
import com.lifedawn.capstoneapp.kakao.search.SearchResultChecker;
import com.lifedawn.capstoneapp.main.MyApplication;
import com.lifedawn.capstoneapp.map.BottomSheetType;
import com.lifedawn.capstoneapp.map.MapViewModel;
import com.lifedawn.capstoneapp.map.MarkerType;
import com.lifedawn.capstoneapp.map.interfaces.BottomSheetController;
import com.lifedawn.capstoneapp.map.interfaces.IMap;
import com.lifedawn.capstoneapp.map.interfaces.MarkerOnClickListener;
import com.lifedawn.capstoneapp.map.interfaces.OnExtraListDataListener;
import com.lifedawn.capstoneapp.map.interfaces.OnPoiItemClickListener;
import com.lifedawn.capstoneapp.retrofits.parameters.LocalApiPlaceParameter;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.KakaoLocalResponse;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.address.AddressResponse;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.place.PlaceResponse;
import com.naver.maps.geometry.LatLng;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class LocationSearchResultMainFragment extends Fragment implements OnExtraListDataListener<Constant> {
	private FragmentSearchResultMainBinding binding;

	private SearchResultListAdapter searchResultListAdapter;
	private String query;
	private MapViewModel mapViewModel;

	private OnPageCallback onPageCallback;
	private IMap iMap;
	private BottomSheetController bottomSheetController;
	private OnPoiItemClickListener onPoiItemClickListener;

	private OnExtraListDataListener<Constant> placesOnExtraListDataListener;
	private OnExtraListDataListener<Constant> addressesOnExtraListDataListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		query = getArguments().getString("query");

		mapViewModel = new ViewModelProvider(requireActivity()).get(MapViewModel.class);
		iMap = mapViewModel.getiMapData();
		bottomSheetController = mapViewModel.getBottomSheetController();
		onPoiItemClickListener = mapViewModel.getPoiItemOnClickListener();

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentSearchResultMainBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		searchResultListAdapter = new SearchResultListAdapter(LocationSearchResultMainFragment.this);
		onPageCallback = new OnPageCallback();
		binding.listViewPager.registerOnPageChangeCallback(onPageCallback);
		binding.listViewPager.setAdapter(searchResultListAdapter);

		searchLocation();
	}

	@Override
	public void loadExtraListData(Constant e, RecyclerView.AdapterDataObserver adapterDataObserver) {

	}


	@Override
	public void loadExtraListData(RecyclerView.AdapterDataObserver adapterDataObserver) {
		Constant currentResultType = getCurrentListType();

		if (currentResultType == Constant.ADDRESS) {
			addressesOnExtraListDataListener.loadExtraListData(adapterDataObserver);
		} else {
			placesOnExtraListDataListener.loadExtraListData(adapterDataObserver);
		}

	}

	public void searchLocation() {
		LocalApiPlaceParameter addressParameter = LocalParameterUtil.getAddressParameter(query, "1",
				LocalApiPlaceParameter.DEFAULT_PAGE);
		String latitude = null;
		String longitude = null;

		if (SearchResultPlaceListFragment.currentSearchMapPointCriteria == LocalApiPlaceParameter.SEARCH_CRITERIA_MAP_POINT_CURRENT_LOCATION) {
			latitude = String.valueOf(SearchResultPlaceListFragment.currentLocation.getLatitude());
			longitude = String.valueOf(SearchResultPlaceListFragment.currentLocation.getLongitude());
		} else {
			LatLng latLng = mapViewModel.getMapCenterPoint();
			latitude = String.valueOf(latLng.latitude);
			longitude = String.valueOf(latLng.longitude);
		}

		final LocalApiPlaceParameter placeParameter = LocalParameterUtil.getPlaceParameter(query, latitude, longitude,
				"1", LocalApiPlaceParameter.DEFAULT_PAGE,
				LocalApiPlaceParameter.SEARCH_CRITERIA_SORT_TYPE_ACCURACY, String.valueOf(MyApplication.MAP_SEARCH_RANGE * 1000));


		SearchResultChecker.checkExisting(addressParameter, placeParameter, new HttpCallback<List<KakaoLocalResponse>>() {
			@Override
			public void onResponseSuccessful(List<KakaoLocalResponse> resultList) {
				List<Fragment> fragments = new ArrayList<>();

				for (KakaoLocalResponse kakaoLocalResponse : resultList) {

					if (kakaoLocalResponse instanceof PlaceResponse) {
						PlaceResponse placeResponse = (PlaceResponse) kakaoLocalResponse;
						if (!placeResponse.getPlaceDocuments().isEmpty()) {
							SearchResultPlaceListFragment searchResultPlaceListFragment = new SearchResultPlaceListFragment(query,
									new OnClickedListItemListener<PlaceResponse.Documents>() {
										@Override
										public void onClicked(PlaceResponse.Documents e) {
											iMap.showMarkers(MarkerType.SEARCH_RESULT_PLACE);
											onPoiItemClickListener.onPOIItemSelectedByList(e, MarkerType.SEARCH_RESULT_PLACE, new MarkerOnClickListener() {
												@Override
												public void onClickedMarker() {

												}
											});
											bottomSheetController.setStateOfBottomSheet(BottomSheetType.SEARCH_LOCATION,
													BottomSheetBehavior.STATE_COLLAPSED);
											showMap();
										}
									});
							placesOnExtraListDataListener = searchResultPlaceListFragment;
							fragments.add(searchResultPlaceListFragment);
						}

					} else if (kakaoLocalResponse instanceof AddressResponse) {
						AddressResponse addressResponse = (AddressResponse) kakaoLocalResponse;
						if (!addressResponse.getDocumentsList().isEmpty()) {
							SearchResultAddressListFragment addressesListFragment = new SearchResultAddressListFragment(query,
									new OnClickedListItemListener<AddressResponse.Documents>() {
										@Override
										public void onClicked(AddressResponse.Documents e) {
											iMap.showMarkers(MarkerType.SEARCH_RESULT_ADDRESS);
											onPoiItemClickListener.onPOIItemSelectedByList(e, MarkerType.SEARCH_RESULT_ADDRESS, new MarkerOnClickListener() {
												@Override
												public void onClickedMarker() {

												}
											});
											bottomSheetController.setStateOfBottomSheet(BottomSheetType.SEARCH_LOCATION,
													BottomSheetBehavior.STATE_COLLAPSED);
											showMap();
										}
									});
							addressesOnExtraListDataListener = addressesListFragment;
							fragments.add(addressesListFragment);
						}

					}
				}

				requireActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						onPageCallback.lastPosition = 0;
						searchResultListAdapter.setFragments(fragments);
						searchResultListAdapter.notifyDataSetChanged();
					}
				});
			}

			@Override
			public void onResponseFailed(Exception e) {
				requireActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						binding.listViewPager.unregisterOnPageChangeCallback(onPageCallback);
						searchResultListAdapter.setFragments(new ArrayList<>());
						searchResultListAdapter.notifyDataSetChanged();
					}
				});
			}
		});
	}

	@Override
	public void onDestroy() {
		iMap.removeMarkers(MarkerType.SEARCH_RESULT_ADDRESS, MarkerType.SEARCH_RESULT_PLACE);

		super.onDestroy();
	}

	private void showMap() {
		bottomSheetController.setStateOfBottomSheet(BottomSheetType.SEARCH_LOCATION, BottomSheetBehavior.STATE_COLLAPSED);
		getParentFragmentManager().beginTransaction().hide(this).addToBackStack(
				LocationSearchResultMainFragment.class.getName() + "hide").commit();
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (hidden) {
			bottomSheetController.setStateOfBottomSheet(BottomSheetType.SEARCH_LOCATION, BottomSheetBehavior.STATE_COLLAPSED);
		} else {
			bottomSheetController.setStateOfBottomSheet(BottomSheetType.LOCATION_ITEM, BottomSheetBehavior.STATE_COLLAPSED);
			bottomSheetController.setStateOfBottomSheet(BottomSheetType.SEARCH_LOCATION, BottomSheetBehavior.STATE_EXPANDED);
		}
	}


	public Constant getCurrentListType() {
		int currentItem = binding.listViewPager.getCurrentItem();
		Fragment currentFragment = searchResultListAdapter.getFragment(currentItem);

		if (currentFragment instanceof SearchResultAddressListFragment) {
			return Constant.ADDRESS;
		} else {
			return Constant.PLACE;
		}
	}

	private static class OnPageCallback extends ViewPager2.OnPageChangeCallback {
		public int lastPosition = 0;

		@Override
		public void onPageSelected(int position) {
			super.onPageSelected(position);
			if (lastPosition != position) {
				lastPosition = position;
			}
		}
	}

	private static class SearchResultListAdapter extends FragmentStateAdapter {
		private ArrayMap<Long, Fragment> fragmentArrayMap = new ArrayMap<>();

		public SearchResultListAdapter(@NonNull @NotNull Fragment fragment) {
			super(fragment);
		}

		@NonNull
		@Override
		public Fragment createFragment(int position) {
			return fragmentArrayMap.valueAt(position);
		}

		@Override
		public int getItemCount() {
			return fragmentArrayMap.size();
		}

		public void setFragments(List<Fragment> fragments) {
			this.fragmentArrayMap.clear();
			for (Fragment fragment : fragments) {
				fragmentArrayMap.put((long) fragment.hashCode(), fragment);
			}
		}

		public Fragment getFragment(int position) {
			return fragmentArrayMap.valueAt(position);
		}

		@Override
		public long getItemId(int position) {
			return fragmentArrayMap.keyAt(position);
		}

		@Override
		public boolean containsItem(long itemId) {
			return fragmentArrayMap.containsKey(itemId);
		}
	}
}