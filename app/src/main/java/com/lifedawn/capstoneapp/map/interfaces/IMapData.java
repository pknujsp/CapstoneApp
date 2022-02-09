package com.lifedawn.capstoneapp.map.interfaces;

import com.lifedawn.capstoneapp.map.MarkerType;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.KakaoLocalDocument;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IMapData {
	void createMarkers(@NotNull List<? extends KakaoLocalDocument> kakaoLocalDocuments, @NotNull MarkerType markerType);
	
	void addExtraMarkers(@NotNull List<? extends KakaoLocalDocument> kakaoLocalDocuments, @NotNull MarkerType markerType);
	
	void removeMarkers(MarkerType... markerTypes);
	
	void removeMarker(MarkerType markerType, int index);
	
	void removeAllMarkers();
	
	void showMarkers(MarkerType... markerTypes);
	
	void showMarkers(MarkerType markerType, boolean isShow);
	
	void deselectMarker();
	
}