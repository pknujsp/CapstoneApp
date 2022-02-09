package com.lifedawn.capstoneapp.map.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.map.MarkerType;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.KakaoLocalDocument;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.address.AddressResponse;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.place.PlaceResponse;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class LocationItemViewPagerAdapter extends LocationItemViewPagerAbstractAdapter {
	private List<KakaoLocalDocument> kakaoLocalDocumentList = new ArrayList<>();
	
	public LocationItemViewPagerAdapter(Context context, MarkerType MARKER_TYPE) {
		super(context, MARKER_TYPE);
	}
	
	public void setLocalDocumentsList(List<? extends KakaoLocalDocument> localDocumentsList) {
		this.kakaoLocalDocumentList.clear();
		this.kakaoLocalDocumentList.addAll(localDocumentsList);
	}
	
	public List<KakaoLocalDocument> getLocalDocumentsList() {
		return kakaoLocalDocumentList;
	}
	
	@NonNull
	@Override
	public LocationItemInMapViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new LocationItemInMapViewHolder(
				LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_places_item, parent, false));
	}
	
	@Override
	public void onBindViewHolder(@NonNull @NotNull PlaceItemInMapViewHolder holder, int position) {
		holder.bind();
	}
	
	@Override
	public int getItemCount() {
		return kakaoLocalDocumentList.size();
	}
	
	@Override
	public KakaoLocalDocument getLocalItem(int position) {
		return kakaoLocalDocumentList.get(position);
	}
	
	@Override
	public int getLocalItemPosition(KakaoLocalDocument kakaoLocalDocument) {
		int i = 0;
		for (; i < kakaoLocalDocumentList.size(); i++) {
			if (kakaoLocalDocumentList.get(i).equals(kakaoLocalDocument)) {
				break;
			}
		}
		return i;
	}
	
	@Override
	public int getItemsCount() {
		return kakaoLocalDocumentList.size();
	}
	
	public int getItemPosition(KakaoLocalDocument kakaoLocalDocument) {
		int position = 0;
		
		if (kakaoLocalDocument instanceof PlaceResponse.Documents) {
			String placeId = ((PlaceResponse.Documents) kakaoLocalDocument).getId();
			
			for (KakaoLocalDocument document : kakaoLocalDocumentList) {
				if (((PlaceResponse.Documents) document).getId().equals(placeId)) {
					break;
				}
				position++;
			}
		} else if (kakaoLocalDocument instanceof AddressResponse.Documents) {
			String x = ((AddressResponse.Documents) kakaoLocalDocument).getX();
			String y = ((AddressResponse.Documents) kakaoLocalDocument).getY();
			
			AddressResponse.Documents addressResponseDocument = null;
			for (KakaoLocalDocument document : kakaoLocalDocumentList) {
				addressResponseDocument = (AddressResponse.Documents) document;
				
				if (addressResponseDocument.getX().equals(x) &&
						addressResponseDocument.getY().equals(y)) {
					break;
				}
				position++;
			}
		}
		
		return position;
	}
	
	class LocationItemInMapViewHolder extends PlaceItemInMapViewHolder {
		public LocationItemInMapViewHolder(@NonNull View view) {
			super(view);
		}
		
		@Override
		KakaoLocalDocument getKakaoLocalDocument(int position) {
			return kakaoLocalDocumentList.get(position);
		}
		
		@Override
		public void bind() {
			setDataView(getKakaoLocalDocument(getBindingAdapterPosition()));
		}
		
	}
}