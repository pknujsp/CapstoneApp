package com.lifedawn.capstoneapp.map;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.lifedawn.capstoneapp.map.interfaces.BottomSheetController;
import com.lifedawn.capstoneapp.map.interfaces.IMap;
import com.lifedawn.capstoneapp.map.interfaces.OnPoiItemClickListener;
import com.naver.maps.geometry.LatLng;

public class MapViewModel extends AndroidViewModel {
	private IMapPoint iMapPoint;
	private IMap iMap;
	private BottomSheetController bottomSheetController;
	private OnPoiItemClickListener onPoiItemClickListener;

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
	
	public IMap getiMapData() {
		return iMap;
	}
	
	public MapViewModel setiMapData(IMap iMap) {
		this.iMap = iMap;
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

	public void setPoiItemOnClickListener(OnPoiItemClickListener onPoiItemClickListener) {
		this.onPoiItemClickListener = onPoiItemClickListener;
	}

	public OnPoiItemClickListener getPoiItemOnClickListener() {
		return onPoiItemClickListener;
	}
}
