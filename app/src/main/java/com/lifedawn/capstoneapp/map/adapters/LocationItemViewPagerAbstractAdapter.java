package com.lifedawn.capstoneapp.map.adapters;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.databinding.CardViewPlacesItemBinding;
import com.lifedawn.capstoneapp.map.MarkerType;
import com.lifedawn.capstoneapp.map.interfaces.OnClickedBottomSheetListener;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.KakaoLocalDocument;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.address.AddressResponse;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.place.PlaceResponse;

public abstract class LocationItemViewPagerAbstractAdapter extends RecyclerView.Adapter<LocationItemViewPagerAbstractAdapter.PlaceItemInMapViewHolder> {
	protected final MarkerType MARKER_TYPE;
	private OnClickedLocationBtnListener onClickedLocationBtnListener;
	
	protected OnClickedBottomSheetListener onClickedBottomSheetListener;
	protected Context context;
	
	protected int isVisibleSelectBtn = View.GONE;
	protected int isVisibleUnSelectBtn = View.GONE;
	
	public LocationItemViewPagerAbstractAdapter(Context context, MarkerType MARKER_TYPE) {
		this.context = context;
		this.MARKER_TYPE = MARKER_TYPE;
	}
	
	public LocationItemViewPagerAbstractAdapter setOnClickedLocationBtnListener(OnClickedLocationBtnListener onClickedLocationBtnListener) {
		this.onClickedLocationBtnListener = onClickedLocationBtnListener;
		return this;
	}
	
	public final LocationItemViewPagerAbstractAdapter setVisibleSelectBtn(int visibleSelectBtn) {
		isVisibleSelectBtn = visibleSelectBtn;
		return this;
	}
	
	public final LocationItemViewPagerAbstractAdapter setVisibleUnSelectBtn(int visibleUnSelectBtn) {
		isVisibleUnSelectBtn = visibleUnSelectBtn;
		return this;
	}
	
	public final void setOnClickedBottomSheetListener(OnClickedBottomSheetListener onClickedBottomSheetListener) {
		this.onClickedBottomSheetListener = onClickedBottomSheetListener;
	}
	
	abstract public int getItemsCount();
	
	abstract public KakaoLocalDocument getLocalItem(int position);
	
	abstract public int getLocalItemPosition(KakaoLocalDocument kakaoLocalDocument);
	
	abstract class PlaceItemInMapViewHolder extends RecyclerView.ViewHolder {
		protected CardViewPlacesItemBinding binding;
		protected Integer favoriteLocationId;
		
		public PlaceItemInMapViewHolder(@NonNull View view) {
			super(view);
			binding = CardViewPlacesItemBinding.bind(view);
			binding.addressLayout.addressIndex.setVisibility(View.GONE);
			binding.rootLayout.setOnClickListener(itemOnClickListener);
			
			binding.selectThisPlaceButton.setVisibility(isVisibleSelectBtn);
			binding.unselectThisPlaceButton.setVisibility(isVisibleUnSelectBtn);
			
			binding.selectThisPlaceButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					onClickedLocationBtnListener.onSelected(getKakaoLocalDocument(getBindingAdapterPosition()), false);
				}
			});
			
			binding.unselectThisPlaceButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					onClickedLocationBtnListener.onSelected(getKakaoLocalDocument(getBindingAdapterPosition()), true);
				}
			});
			
		}
		
		abstract void bind();
		
		protected final void setDataView(KakaoLocalDocument kakaoLocalDocument) {
			final int position = getBindingAdapterPosition();
			String itemPosition = (position + 1) + " / " + getItemCount();
			binding.itemPosition.setText(itemPosition);
			
			PlaceResponse.Documents placeDocument = null;
			AddressResponse.Documents addressDocument = null;
			
			final ViewHolderData viewHolderData = new ViewHolderData(kakaoLocalDocument);
			binding.rootLayout.setTag(viewHolderData);
			
			if (kakaoLocalDocument instanceof PlaceResponse.Documents) {
				placeDocument = (PlaceResponse.Documents) kakaoLocalDocument;
				
				binding.placeLayout.placeItemName.setText(placeDocument.getPlaceName());
				binding.placeLayout.placeItemAddress.setText(placeDocument.getAddressName());
				binding.placeLayout.placeItemCategory.setText(placeDocument.getCategoryName());
				
				binding.placeLayout.getRoot().setVisibility(View.VISIBLE);
				binding.addressLayout.getRoot().setVisibility(View.GONE);
			} else if (kakaoLocalDocument instanceof AddressResponse.Documents) {
				addressDocument = (AddressResponse.Documents) kakaoLocalDocument;
				
				binding.addressLayout.addressName.setText(addressDocument.getAddressName());
				if (addressDocument.getAddressResponseRoadAddress() != null) {
					binding.addressLayout.anotherAddressType.setText(itemView.getContext().getString(R.string.road_addr));
					binding.addressLayout.anotherAddressName.setText(addressDocument.getAddressResponseRoadAddress().getAddressName());
				} else if (addressDocument.getAddressResponseAddress() != null) {
					binding.addressLayout.anotherAddressType.setText(itemView.getContext().getString(R.string.region_addr));
					binding.addressLayout.anotherAddressName.setText(addressDocument.getAddressResponseAddress().getAddressName());
				}
				
				binding.placeLayout.getRoot().setVisibility(View.GONE);
				binding.addressLayout.getRoot().setVisibility(View.VISIBLE);
			}
		}
		
		
		abstract KakaoLocalDocument getKakaoLocalDocument(int position);
	}
	
	static final class ViewHolderData {
		KakaoLocalDocument kakaoLocalDocument;
		
		public ViewHolderData(KakaoLocalDocument kakaoLocalDocument) {
			this.kakaoLocalDocument = kakaoLocalDocument;
		}
	}
	
	private final View.OnClickListener itemOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			ViewHolderData viewHolderData = (ViewHolderData) view.getTag();
			onClickedBottomSheetListener.onClickedPlaceBottomSheet(viewHolderData.kakaoLocalDocument);
		}
	};
	
	public interface OnClickedLocationBtnListener {
		void onSelected(KakaoLocalDocument kakaoLocalDocument, boolean remove);
	}
}
