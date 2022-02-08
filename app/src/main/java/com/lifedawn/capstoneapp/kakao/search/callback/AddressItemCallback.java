package com.lifedawn.capstoneapp.kakao.search.callback;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.address.AddressResponse;

public class AddressItemCallback extends DiffUtil.ItemCallback<AddressResponse.Documents> {
	@Override
	public boolean areItemsTheSame(@NonNull AddressResponse.Documents oldItem, @NonNull AddressResponse.Documents newItem) {
		return oldItem.getAddressName().equals(newItem.getAddressName());
	}
	
	@Override
	public boolean areContentsTheSame(@NonNull AddressResponse.Documents oldItem, @NonNull AddressResponse.Documents newItem) {
		return oldItem.getAddressName().equals(newItem.getAddressName());
	}
}