package com.lifedawn.capstoneapp.kakao;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.common.interfaces.OnDbQueryCallback;
import com.lifedawn.capstoneapp.common.view.SearchHistoryFragment;
import com.lifedawn.capstoneapp.common.viewmodel.SearchHistoryViewModel;
import com.lifedawn.capstoneapp.databinding.FragmentSearchBarBinding;
import com.lifedawn.capstoneapp.kakao.search.searchresult.LocationSearchResultMainFragment;
import com.lifedawn.capstoneapp.room.dto.SearchHistoryDto;

public class SearchBarFragment extends Fragment implements SearchHistoryFragment.OnClickedHistoryItemListener {
	private FragmentSearchBarBinding binding;
	private SearchHistoryViewModel searchHistoryViewModel;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentSearchBarBinding.inflate(inflater);
		return binding.getRoot();
	}
	
	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
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
		FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
		fragmentTransaction.hide(getParentFragmentManager().findFragmentByTag(SearchHistoryFragment.class.getName())).add(
				R.id.search_fragment_container, locationSearchResultMainFragment,
				LocationSearchResultMainFragment.class.getName()).addToBackStack(LocationSearchResultMainFragment.class.getName()).commit();
	}
	
	@Override
	public void onClickedValue(SearchHistoryDto searchHistoryDto, int position) {
		binding.searchView.setQuery(searchHistoryDto.getValue(), false);
		search(searchHistoryDto.getValue());
	}
	
	@Override
	public void onClickedRemove(SearchHistoryDto searchHistoryDto, int position) {
	
	}
}