package com.lifedawn.capstoneapp.kakao;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.lifedawn.capstoneapp.common.constants.BundleConstant;
import com.lifedawn.capstoneapp.common.interfaces.OnDbQueryCallback;
import com.lifedawn.capstoneapp.common.view.SearchHistoryFragment;
import com.lifedawn.capstoneapp.common.viewmodel.SearchHistoryViewModel;
import com.lifedawn.capstoneapp.databinding.FragmentSearchBinding;
import com.lifedawn.capstoneapp.kakao.search.searchresult.LocationSearchResultMainFragment;
import com.lifedawn.capstoneapp.room.dto.SearchHistoryDto;

public class SearchFragment extends Fragment implements SearchHistoryFragment.OnClickedHistoryItemListener {
	private FragmentSearchBinding binding;
	private SearchHistoryViewModel searchHistoryViewModel;
	
	private final OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
		@Override
		public void handleOnBackPressed() {
			if (!getChildFragmentManager().popBackStackImmediate()) {
				getParentFragmentManager().popBackStack();
			}
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActivity().getOnBackPressedDispatcher().addCallback(onBackPressedCallback);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentSearchBinding.inflate(inflater);
		return binding.getRoot();
	}
	
	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		Bundle bundle = new Bundle();
		bundle.putSerializable(BundleConstant.SEARCH_HISTORY_TYPE.name(), SearchHistoryDto.SearchHistoryType.MAP);
		SearchHistoryFragment searchHistoryFragment = new SearchHistoryFragment();
		searchHistoryFragment.setOnClickedHistoryItemListener(this);
		searchHistoryFragment.setArguments(bundle);
		
		getChildFragmentManager().beginTransaction().replace(binding.fragmentContainerView.getId(), searchHistoryFragment,
				SearchHistoryFragment.class.getName()).commit();
		
		searchHistoryViewModel = new ViewModelProvider(requireActivity()).get(SearchHistoryViewModel.class);
		
		binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				if (query.isEmpty()) {
					return false;
				}
				
				searchHistoryViewModel.contains(query, SearchHistoryDto.SearchHistoryType.MAP.name(), new OnDbQueryCallback<Boolean>() {
					@Override
					public void onResult(Boolean e) {
						if (!e) {
							SearchHistoryDto searchHistoryDto = new SearchHistoryDto();
							searchHistoryDto.setSearchHistoryType(SearchHistoryDto.SearchHistoryType.MAP);
							searchHistoryDto.setValue(query);
							searchHistoryViewModel.insert(searchHistoryDto, null);
						}
					}
				});
				// 검색 진행
				search(query);
				return true;
			}
			
			@Override
			public boolean onQueryTextChange(String newText) {
				return false;
			}
		});
	}
	
	@Override
	public void onDestroy() {
		onBackPressedCallback.remove();
		super.onDestroy();
	}
	
	
	private void search(String query) {
		//검색 진행
		LocationSearchResultMainFragment locationSearchResultMainFragment = new LocationSearchResultMainFragment();
		Bundle bundle = new Bundle();
		bundle.putString("query", query);
		locationSearchResultMainFragment.setArguments(bundle);
		
		FragmentManager fragmentManager = getChildFragmentManager();
		
		if (fragmentManager.findFragmentByTag(LocationSearchResultMainFragment.class.getName()) != null) {
			fragmentManager.popBackStackImmediate();
		}
		FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
		fragmentTransaction.hide(getChildFragmentManager().findFragmentByTag(SearchHistoryFragment.class.getName())).add(
				binding.fragmentContainerView.getId(), locationSearchResultMainFragment,
				LocationSearchResultMainFragment.class.getName()).addToBackStack(LocationSearchResultMainFragment.class.getName()).commit();
	}
	
	@Override
	public void onClickedValue(SearchHistoryDto searchHistoryDto, int position) {
		search(searchHistoryDto.getValue());
	}
	
	@Override
	public void onClickedRemove(SearchHistoryDto searchHistoryDto, int position) {
	
	}
}