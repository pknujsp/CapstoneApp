package com.lifedawn.capstoneapp.kakao.search;

import com.lifedawn.capstoneapp.retrofits.parameters.LocalApiPlaceParameter;

public class LocalParameterUtil {
	public static LocalApiPlaceParameter getPlaceParameter(String searchWord, String latitude, String longitude, String size, String page,
	                                                       Integer sortCriteria, String range) {
		LocalApiPlaceParameter parameter = new LocalApiPlaceParameter();
		
		parameter.setY(latitude).setX(longitude).setSize(size).setPage(page);
		
		switch (sortCriteria) {
			case LocalApiPlaceParameter.SEARCH_CRITERIA_SORT_TYPE_ACCURACY:
				parameter.setSort(LocalApiPlaceParameter.SORT_ACCURACY);
				break;
			case LocalApiPlaceParameter.SEARCH_CRITERIA_SORT_TYPE_DISTANCE:
				parameter.setSort(LocalApiPlaceParameter.SORT_DISTANCE);
				break;
		}

		parameter.setRadius(range);
		parameter.setQuery(searchWord);
		
		return parameter;
	}
	
	public static LocalApiPlaceParameter getPlaceParameterForSpecific(String searchWord, String latitude, String longitude) {
		LocalApiPlaceParameter parameter = new LocalApiPlaceParameter();
		
		parameter.setY(latitude).setX(longitude).setSize("5").setPage("1");
		
		parameter.setSort(LocalApiPlaceParameter.SORT_ACCURACY);
		
		parameter.setQuery(searchWord);
		
		parameter.setRadius("100");
		
		return parameter;
	}
	
	public static LocalApiPlaceParameter getAddressParameter(String searchWord, String size, String page) {
		LocalApiPlaceParameter parameter = new LocalApiPlaceParameter();
		parameter.setQuery(searchWord).setSize(size).setPage(page);
		return parameter;
	}
}