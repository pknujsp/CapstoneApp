package com.lifedawn.capstoneapp.map.places.content;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.common.interfaces.OnClickedListItemListener;
import com.lifedawn.capstoneapp.databinding.PlaceRecyclerViewItemBinding;
import com.lifedawn.capstoneapp.kakao.search.util.MapUtil;
import com.lifedawn.capstoneapp.map.places.parent.SearchResultAdapter;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.place.PlaceResponse;

public class PlacesSearchResultAdapter extends SearchResultAdapter<PlaceResponse.Documents, PlacesSearchResultAdapter.ItemViewHolder> {

	public PlacesSearchResultAdapter(Context context, OnClickedListItemListener<PlaceResponse.Documents> onClickedListItem) {
		super(context, onClickedListItem);
	}

	@NonNull
	@Override
	public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new ItemViewHolder(
				LayoutInflater.from(parent.getContext()).inflate(R.layout.place_recycler_view_item, parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
		holder.bind();
	}

	protected class ItemViewHolder extends RecyclerView.ViewHolder {
		private PlaceRecyclerViewItemBinding binding;
		protected PlaceResponse.Documents item;

		public ItemViewHolder(View view) {
			super(view);
			binding = PlaceRecyclerViewItemBinding.bind(view);
		}

		public void bind() {
			item = getItem(getBindingAdapterPosition());

			binding.placeName.setText(item.getPlaceName());
			binding.placeIndex.setText(String.valueOf(getBindingAdapterPosition() + 1));
			binding.placeCategory.setText(item.getCategoryName());
			binding.placeAddressName.setText(item.getAddressName());
			binding.placeDistance.setText(MapUtil.convertMeterToKm(Double.parseDouble(item.getDistance())));

			itemView.getRootView().setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					onClickedListItem.onClicked(getItem(getBindingAdapterPosition()));
				}
			});
		}

	}
}
