package com.lifedawn.capstoneapp.map.places;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.util.List;

public interface IConnectContents {
 void setViewPager(List<AroundPlacesContentsFragment.PlaceFragment> fragmentList);

 ViewPager2 getViewPager2();

 void loadExtraData(int tabPosition, RecyclerView.AdapterDataObserver adapterDataObserver);

}
