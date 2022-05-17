package com.lifedawn.capstoneapp.map;

import android.os.Bundle;
import android.view.View;

import com.lifedawn.capstoneapp.map.adapters.LocationItemViewPagerAbstractAdapter;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.KakaoLocalDocument;

public class NewPromiseLocationNaverMapFragment extends AbstractNaverMapFragment {
	private LocationItemViewPagerAbstractAdapter.OnClickedLocationBtnListener onClickedLocationBtnListener;

	public void setOnClickedLocationBtnListener(LocationItemViewPagerAbstractAdapter.OnClickedLocationBtnListener onClickedLocationBtnListener) {
		this.onClickedLocationBtnListener = onClickedLocationBtnListener;
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setPlaceBottomSheetSelectBtnVisibility(View.VISIBLE);
		setPlaceBottomSheetUnSelectBtnVisibility(View.GONE);
	}

	@Override
	protected LocationDto getPromiseLocationDto() {
		return null;
	}


	@Override
	public void onSelected(KakaoLocalDocument kakaoLocalDocument, boolean remove) {
		getParentFragmentManager().popBackStack();
		onClickedLocationBtnListener.onSelected(kakaoLocalDocument, remove);
	}

}
