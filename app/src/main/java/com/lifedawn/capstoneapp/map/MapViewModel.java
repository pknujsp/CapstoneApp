package com.lifedawn.capstoneapp.map;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.lifedawn.capstoneapp.map.interfaces.BottomSheetController;
import com.lifedawn.capstoneapp.map.interfaces.IMapData;
import com.lifedawn.capstoneapp.map.interfaces.MarkerOnClickListener;
import com.naver.maps.geometry.LatLng;

public class MapViewModel extends AndroidViewModel {
	private IMapPoint iMapPoint;
	private IMapData iMapData;
	private BottomSheetController bottomSheetController;
	private MarkerOnClickListener markerOnClickListener;
	
	public MapViewModel setiMapPoint(IMapPoint iMapPoint) {
		this.iMapPoint = iMapPoint;
		return this;
	}
	
	public MapViewModel(@NonNull Application application) {
		super(application);
	}
	
	public LatLng getMapCenterPoint() {
		return iMapPoint.getCenterPoint();
	}
	
	public IMapPoint getiMapPoint() {
		return iMapPoint;
	}
	
	public IMapData getiMapData() {
		return iMapData;
	}
	
	public MapViewModel setiMapData(IMapData iMapData) {
		this.iMapData = iMapData;
		return this;
	}
	
	
	public interface IMapPoint {
		LatLng getCenterPoint();
	}
	
	public BottomSheetController getBottomSheetController() {
		return bottomSheetController;
	}
	
	public MapViewModel setBottomSheetController(BottomSheetController bottomSheetController) {
		this.bottomSheetController = bottomSheetController;
		return this;
	}
	
	public MarkerOnClickListener getMarkerOnClickListener() {
		return markerOnClickListener;
	}
	
	public MapViewModel setMarkerOnClickListener(MarkerOnClickListener markerOnClickListener) {
		this.markerOnClickListener = markerOnClickListener;
		return this;
	}
}
