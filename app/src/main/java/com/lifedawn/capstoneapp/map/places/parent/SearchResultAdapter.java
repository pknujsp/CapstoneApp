package com.lifedawn.capstoneapp.map.places.parent;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.lifedawn.capstoneapp.common.interfaces.OnClickedListItemListener;
import com.lifedawn.capstoneapp.kakao.search.callback.PlaceItemCallback;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.KakaoLocalDocument;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.place.PlaceResponse;

public abstract class SearchResultAdapter<T extends KakaoLocalDocument, ItemViewHolder extends RecyclerView.ViewHolder> extends PagedListAdapter<T,
		ItemViewHolder> {
	protected Context context;
	protected OnClickedListItemListener<T> onClickedListItem;

	public SearchResultAdapter(Context context, OnClickedListItemListener<T> onClickedListItem) {
		super((DiffUtil.ItemCallback<T>) new PlaceItemCallback());
		this.context = context;
		this.onClickedListItem = onClickedListItem;
	}


	@NonNull
	@Override
	public abstract ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType);

	@Override
	public abstract void onBindViewHolder(@NonNull ItemViewHolder holder, int position);
}