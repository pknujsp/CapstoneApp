package com.lifedawn.capstoneapp.kakao.search.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.lifedawn.capstoneapp.model.firestore.PlaceDto;
import com.lifedawn.capstoneapp.retrofits.parameters.LocalApiPlaceParameter;

public class SearchPlaceShareViewModel extends AndroidViewModel {
	private PlaceDto promisePlaceDto;
	private PlaceDto mapCenterPlaceDto;
	private int criteriaType;

	public SearchPlaceShareViewModel(@NonNull Application application) {
		super(application);
	}

	public SearchPlaceShareViewModel setMapCenterLocationDto(PlaceDto mapCenterPlaceDto) {
		this.mapCenterPlaceDto = mapCenterPlaceDto;
		return this;
	}


	public SearchPlaceShareViewModel setCriteriaType(int criteriaType) {
		this.criteriaType = criteriaType;
		return this;
	}

	public SearchPlaceShareViewModel setPromiseLocationDto(PlaceDto promisePlaceDto) {
		this.promisePlaceDto = promisePlaceDto;
		return this;
	}

	public PlaceDto getPromiseLocationDto() {
		return promisePlaceDto;
	}

	public PlaceDto getMapCenterLocationDto() {
		return mapCenterPlaceDto;
	}

	public PlaceDto getCriteriaLocationDto() {
		return criteriaType == LocalApiPlaceParameter.SEARCH_CRITERIA_MAP_POINT_CURRENT_LOCATION ? promisePlaceDto :
				mapCenterPlaceDto;
	}
}
