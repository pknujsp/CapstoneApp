package com.lifedawn.capstoneapp.kakao.search.searchresult.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lifedawn.capstoneapp.R;

public class AddressesAdapter extends PagedListAdapter<AddressResponseDocuments, AddressesAdapter.ItemViewHolder> {
	private final Context context;
	private final OnClickedListItem<AddressResponseDocuments> onClickedListItem;
	
	public AddressesAdapter(Context context, OnClickedListItem<AddressResponseDocuments> onClickedListItem) {
		super(new AddressItemCallback());
		this.context = context;
		this.onClickedListItem = onClickedListItem;
	}
	
	
	class ItemViewHolder extends RecyclerView.ViewHolder {
		private final TextView addressName;
		private final TextView anotherAddressName;
		private final TextView anotherAddressType;
		private final TextView addressIndex;
		
		public ItemViewHolder(View view) {
			super(view);
			addressName = (TextView) view.findViewById(R.id.address_name);
			addressIndex = (TextView) view.findViewById(R.id.address_index);
			anotherAddressName = (TextView) view.findViewById(R.id.another_address_name);
			anotherAddressType = (TextView) view.findViewById(R.id.another_address_type);
		}
		
		public void bind(AddressResponseDocuments item) {
			addressName.setText(item.getAddressName());
			addressIndex.setText(String.valueOf(getBindingAdapterPosition() + 1));
			
			if (item.getAddressResponseRoadAddress() != null) {
				anotherAddressType.setText(context.getString(R.string.road_addr));
				anotherAddressName.setText(item.getAddressResponseRoadAddress().getAddressName());
			} else if (item.getAddressResponseAddress() != null) {
				anotherAddressType.setText(context.getString(R.string.region_addr));
				anotherAddressName.setText(item.getAddressResponseAddress().getAddressName());
			}
			
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
		return new ItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.address_recycler_view_item, parent, false));
	}
	
	@Override
	public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
		holder.bind(getItem(position));
	}
	
	@Override
	public void submitList(@Nullable PagedList<AddressResponseDocuments> pagedList) {
		super.submitList(pagedList);
	}
	
	@Override
	public void submitList(@Nullable PagedList<AddressResponseDocuments> pagedList, @Nullable Runnable commitCallback) {
		super.submitList(pagedList, commitCallback);
	}
}