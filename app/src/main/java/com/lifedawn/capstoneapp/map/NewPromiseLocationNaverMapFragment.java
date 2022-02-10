package com.lifedawn.capstoneapp.map;

import android.os.Bundle;
import android.view.View;

import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.KakaoLocalDocument;

public class NewPromiseLocationNaverMapFragment extends AbstractNaverMapFragment {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setPlaceBottomSheetSelectBtnVisibility(View.VISIBLE);
		setPlaceBottomSheetUnSelectBtnVisibility(View.GONE);
	}
	
	@Override
	public void onClickedPlaceBottomSheet(KakaoLocalDocument kakaoLocalDocument) {
	
	}
}
