package com.lifedawn.capstoneapp.map.places.content;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.lifedawn.capstoneapp.map.places.parent.AbstractSearchContentViewPagerItemFragment;

import java.util.ArrayList;
import java.util.List;

public class ContentViewPagerAdapter extends FragmentStateAdapter {
	private final List<AbstractSearchContentViewPagerItemFragment> fragmentList = new ArrayList<>();

	public ContentViewPagerAdapter(@NonNull Fragment fragment, List<? extends AbstractSearchContentViewPagerItemFragment> fragmentList) {
		super(fragment);
		this.fragmentList.addAll(fragmentList);
	}

	public AbstractSearchContentViewPagerItemFragment getFragment(int position) {
		return fragmentList.get(position);
	}

	@NonNull
	@Override
	public Fragment createFragment(int position) {
		return fragmentList.get(position);
	}

	@Override
	public int getItemCount() {
		return fragmentList.size();
	}

	public List<AbstractSearchContentViewPagerItemFragment> getFragmentList() {
		return fragmentList;
	}
}