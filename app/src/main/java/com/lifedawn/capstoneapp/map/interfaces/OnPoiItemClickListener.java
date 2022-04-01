package com.lifedawn.capstoneapp.map.interfaces;

import com.lifedawn.capstoneapp.map.MarkerType;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.KakaoLocalDocument;

public interface OnPoiItemClickListener {
	void onPOIItemSelectedByList(KakaoLocalDocument kakaoLocalDocument, MarkerType markerType, MarkerOnClickListener markerOnClickListener);

	void onPOIItemSelectedByBottomSheet(int position, MarkerType markerType, MarkerOnClickListener markerOnClickListener);
}
