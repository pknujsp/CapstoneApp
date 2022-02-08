package com.lifedawn.capstoneapp.kakao.search.searchresult;

import android.os.Bundle;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.lifedawn.capstoneapp.common.constants.Constant;
import com.lifedawn.capstoneapp.common.interfaces.HttpCallback;
import com.lifedawn.capstoneapp.common.interfaces.OnClickedListItemListener;
import com.lifedawn.capstoneapp.databinding.FragmentSearchResultMainBinding;
import com.lifedawn.capstoneapp.kakao.search.LocalParameterUtil;
import com.lifedawn.capstoneapp.kakao.search.SearchResultChecker;
import com.lifedawn.capstoneapp.retrofits.parameters.LocalApiPlaceParameter;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.KakaoLocalResponse;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.address.AddressResponse;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.place.PlaceResponse;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class LocationSearchResultMainFragment extends Fragment {
	private FragmentSearchResultMainBinding binding;
	
	private SearchResultListAdapter searchResultListAdapter;
	private String query;
	
	private OnPageCallback onPageCallback;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		query = getArguments().getString("query");
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
	
	public void searchLocation() {
		final LocalApiPlaceParameter addressParameter = LocalParameterUtil.getAddressParameter(query, "1",
				LocalApiPlaceParameter.DEFAULT_PAGE);
		final LocalApiPlaceParameter placeParameter = LocalParameterUtil.getPlaceParameter(query, null, null, "1",
				LocalApiPlaceParameter.DEFAULT_PAGE, LocalApiPlaceParameter.SEARCH_CRITERIA_SORT_TYPE_ACCURACY);
		
		SearchResultChecker.checkExisting(addressParameter, placeParameter, new HttpCallback<List<KakaoLocalResponse>>() {
			@Override
			public void onResponseSuccessful(List<KakaoLocalResponse> resultList) {
				List<Fragment> fragments = new ArrayList<>();
				
				for (KakaoLocalResponse kakaoLocalResponse : resultList) {
					
					if (kakaoLocalResponse instanceof PlaceResponse) {
						SearchResultPlaceListFragment searchResultPlaceListFragment = new SearchResultPlaceListFragment(query,
								new OnClickedListItemListener<PlaceResponse.Documents>() {
									@Override
									public void onClicked(PlaceResponse.Documents e) {
									
									}
								});
						fragments.add(searchResultPlaceListFragment);
					} else if (kakaoLocalResponse instanceof AddressResponse) {
						SearchResultAddressListFragment addressesListFragment = new SearchResultAddressListFragment(query,
								new OnClickedListItemListener<AddressResponse.Documents>() {
									@Override
									public void onClicked(AddressResponse.Documents e) {
									
									}
								});
						fragments.add(addressesListFragment);
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
		super.onDestroy();
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
	
	class OnPageCallback extends ViewPager2.OnPageChangeCallback {
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