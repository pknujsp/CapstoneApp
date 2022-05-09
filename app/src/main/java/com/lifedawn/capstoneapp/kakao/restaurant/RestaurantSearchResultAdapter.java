package com.lifedawn.capstoneapp.kakao.restaurant;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.common.interfaces.OnClickedListItemListener;
import com.lifedawn.capstoneapp.databinding.PlaceRecyclerViewItemBinding;
import com.lifedawn.capstoneapp.databinding.RestaurantItemviewBinding;
import com.lifedawn.capstoneapp.kakao.search.util.MapUtil;
import com.lifedawn.capstoneapp.map.places.parent.SearchResultAdapter;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.place.PlaceResponse;

public class RestaurantSearchResultAdapter extends SearchResultAdapter<PlaceResponse.Documents, RestaurantSearchResultAdapter.ItemViewHolder> {

	public RestaurantSearchResultAdapter(Context context, OnClickedListItemListener<PlaceResponse.Documents> onClickedListItem) {
		super(context, onClickedListItem);
	}

	@NonNull
	@Override
	public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new ItemViewHolder(
				LayoutInflater.from(context).inflate(R.layout.restaurant_itemview, parent, false));
	}

	@Override
	public void onViewRecycled(@NonNull ItemViewHolder holder) {
		super.onViewRecycled(holder);
	}

	@Override
	public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
		holder.bind();
	}

	protected class ItemViewHolder extends RecyclerView.ViewHolder {
		private RestaurantItemviewBinding binding;
		protected PlaceResponse.Documents item;

		public ItemViewHolder(View view) {
			super(view);
			binding = RestaurantItemviewBinding.bind(view);
		}

		public void bind() {
			item = getItem(getBindingAdapterPosition());

			binding.restaurantName.setText(item.getPlaceName());
			binding.restaurantAddress.setText(item.getAddressName());
			binding.restaurantCategory.setText(item.getCategoryName());

			itemView.getRootView().setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					onClickedListItem.onClicked(getItem(getBindingAdapterPosition()));
				}
			});
		}

	}
}
