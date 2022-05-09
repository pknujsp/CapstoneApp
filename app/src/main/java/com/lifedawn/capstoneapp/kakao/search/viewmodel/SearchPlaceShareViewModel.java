package com.lifedawn.capstoneapp.kakao.search.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.lifedawn.capstoneapp.map.LocationDto;
import com.lifedawn.capstoneapp.retrofits.parameters.LocalApiPlaceParameter;

public class SearchPlaceShareViewModel extends AndroidViewModel {
	private LocationDto promiseLocationDto;
	private LocationDto mapCenterLocationDto;
	private int criteriaType;

	public SearchPlaceShareViewModel(@NonNull Application application) {
		super(application);
	}

	public SearchPlaceShareViewModel setMapCenterLocationDto(LocationDto mapCenterLocationDto) {
		this.mapCenterLocationDto = mapCenterLocationDto;
		return this;
	}


	public SearchPlaceShareViewModel setCriteriaType(int criteriaType) {
		this.criteriaType = criteriaType;
		return this;
	}

	public SearchPlaceShareViewModel setPromiseLocationDto(LocationDto promiseLocationDto) {
		this.promiseLocationDto = promiseLocationDto;
		return this;
	}

	public LocationDto getPromiseLocationDto() {
		return promiseLocationDto;
	}

	public LocationDto getMapCenterLocationDto() {
		return mapCenterLocationDto;
	}

	public LocationDto getCriteriaLocationDto() {
		return criteriaType == LocalApiPlaceParameter.SEARCH_CRITERIA_MAP_POINT_CURRENT_LOCATION ? promiseLocationDto :
				mapCenterLocationDto;
	}
}
