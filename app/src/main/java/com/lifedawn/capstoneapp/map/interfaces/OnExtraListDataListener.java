package com.lifedawn.capstoneapp.map.interfaces;

import androidx.recyclerview.widget.RecyclerView;

public interface OnExtraListDataListener<T>
{
	void loadExtraListData(T e, RecyclerView.AdapterDataObserver adapterDataObserver);
	
	void loadExtraListData(RecyclerView.AdapterDataObserver adapterDataObserver);
}