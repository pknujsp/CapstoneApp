package com.lifedawn.capstoneapp.kakao.search.searchresult.adapter;

import android.util.ArrayMap;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SearchResultListAdapter extends FragmentStateAdapter {
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