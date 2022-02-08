package com.lifedawn.capstoneapp.kakao.search.callback;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.place.PlaceResponse;

public class PlaceItemCallback extends DiffUtil.ItemCallback<PlaceResponse.Documents> {
	@Override
	public boolean areItemsTheSame(@NonNull PlaceResponse.Documents oldItem, @NonNull PlaceResponse.Documents newItem) {
		return oldItem.getId().equals(newItem.getId());
	}
	
	@Override
	public boolean areContentsTheSame(@NonNull PlaceResponse.Documents oldItem, @NonNull PlaceResponse.Documents newItem) {
		return oldItem.getId().equals(newItem.getId());
	}
	
}
