package com.lifedawn.capstoneapp.kakao.search.searchresult.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lifedawn.capstoneapp.R;

public class PlacesAdapter extends PagedListAdapter<PlaceDocuments, PlacesAdapter.ItemViewHolder> {
	private Context context;
	private final OnClickedListItem<PlaceDocuments> onClickedListItem;
	
	public PlacesAdapter(Context context, OnClickedListItem<PlaceDocuments> onClickedListItem) {
		super(new PlaceItemCallback());
		this.context = context;
		this.onClickedListItem = onClickedListItem;
	}
	
	class ItemViewHolder extends RecyclerView.ViewHolder {
		private TextView placeName;
		private TextView placeIndex;
		private TextView placeCategory;
		private TextView placeAddressName;
		private TextView placeDistance;
		
		public ItemViewHolder(View view) {
			super(view);
			placeName = (TextView) view.findViewById(R.id.place_name);
			placeIndex = (TextView) view.findViewById(R.id.place_index);
			placeCategory = (TextView) view.findViewById(R.id.place_category);
			placeAddressName = (TextView) view.findViewById(R.id.place_address_name);
			placeDistance = (TextView) view.findViewById(R.id.place_distance);
		}
		
		public void bind() {
			PlaceDocuments item = getItem(getBindingAdapterPosition());
			
			placeName.setText(item.getPlaceName());
			placeIndex.setText(String.valueOf(getBindingAdapterPosition() + 1));
			placeCategory.setText(item.getCategoryName());
			placeAddressName.setText(item.getAddressName());
			placeDistance.setText(LocationUtil.convertMeterToKm(item.getDistance()));
			
			
			itemView.getRootView().setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					onClickedListItem.onClickedListItem(getItem(getBindingAdapterPosition()), getBindingAdapterPosition());
				}
			});
		}
	}
	
	
	@NonNull
	@Override
	public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new PlacesAdapter.ItemViewHolder(
				LayoutInflater.from(parent.getContext()).inflate(R.layout.place_recycler_view_item, parent, false));
	}
	
	@Override
	public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
		holder.bind();
	}
	
	
}