package com.lifedawn.capstoneapp.map;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.naver.maps.geometry.LatLng;

public class MapViewModel extends AndroidViewModel {
	private IMapPoint iMapPoint;
	
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
	
	
	public interface IMapPoint {
		LatLng getCenterPoint();
	}
}
