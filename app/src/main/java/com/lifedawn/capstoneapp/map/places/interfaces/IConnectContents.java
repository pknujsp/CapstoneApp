package com.lifedawn.capstoneapp.map.places.interfaces;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.lifedawn.capstoneapp.map.places.parent.AbstractSearchContentViewPagerItemFragment;

import java.util.List;

public interface IConnectContents {
	void setViewPager(List<? extends AbstractSearchContentViewPagerItemFragment> fragmentList);

	ViewPager2 getViewPager2();

	void loadExtraData(int tabPosition, RecyclerView.AdapterDataObserver adapterDataObserver);

	void loadPlaces(int tabPosition);
}
