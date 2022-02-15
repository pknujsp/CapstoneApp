package com.lifedawn.capstoneapp.map;

import com.lifedawn.capstoneapp.common.constants.Constant;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.KakaoLocalDocument;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.address.AddressResponse;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.place.PlaceResponse;

import java.io.Serializable;

public class LocationDto implements Serializable {
	private long eventId;
	
	private String latitude;
	
	private String longitude;
	
	private String addressName;
	
	private String roadAddressName;
	
	private String placeId;
	
	private String placeName;
	
	private Constant locationType;
	
	/**
	 * "위도,경도,장소명,장소ID,주소명"
	 */
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(latitude).append(",").append(longitude).append(",").append(placeName == null ? "" : placeName).append(
				",").append(placeId).append(",").append(addressName);
		return stringBuilder.toString();
	}
	
	public static LocationDto toLocationDto(String str) {
		if (str == null) {
			return null;
		}
		String[] separatedStr = str.split(",");
		LocationDto locationDto = new LocationDto();
		locationDto.setLatitude(separatedStr[0]).setLongitude(separatedStr[1]).setPlaceName(separatedStr[2]).setPlaceId(
				separatedStr[3]).setAddressName(separatedStr[4]).setLocationType(
				locationDto.getPlaceName() == null ? Constant.ADDRESS : Constant.PLACE);
		return locationDto;
		
	}
	
	public static LocationDto toLocationDto(KakaoLocalDocument kakaoLocalDocument) {
		LocationDto location = new LocationDto();
		
		// 주소인지 장소인지를 구분한다.
		if (kakaoLocalDocument instanceof PlaceResponse.Documents) {
			PlaceResponse.Documents placeDocuments = (PlaceResponse.Documents) kakaoLocalDocument;
			location.setPlaceId(placeDocuments.getId());
			location.setPlaceName(placeDocuments.getPlaceName());
			location.setAddressName(placeDocuments.getAddressName());
			location.setRoadAddressName(placeDocuments.getRoadAddressName());
			location.setLatitude(placeDocuments.getY());
			location.setLongitude(placeDocuments.getX());
			location.setLocationType(Constant.PLACE);
		} else if (kakaoLocalDocument instanceof AddressResponse.Documents) {
			AddressResponse.Documents addressDocuments = (AddressResponse.Documents) kakaoLocalDocument;
			
			location.setAddressName(addressDocuments.getAddressName());
			location.setLatitude(addressDocuments.getY());
			location.setLongitude(addressDocuments.getX());
			location.setLocationType(Constant.ADDRESS);
			
			if (addressDocuments.getAddressResponseRoadAddress() != null) {
				location.setRoadAddressName(addressDocuments.getAddressResponseRoadAddress().getAddressName());
			}
		}
		return location;
	}
	
	public long getEventId() {
		return eventId;
	}
	
	public LocationDto setEventId(long eventId) {
		this.eventId = eventId;
		return this;
	}
	
	public String getLatitude() {
		return latitude;
	}
	
	public LocationDto setLatitude(String latitude) {
		this.latitude = latitude;
		return this;
	}
	
	public String getLongitude() {
		return longitude;
	}
	
	public LocationDto setLongitude(String longitude) {
		this.longitude = longitude;
		return this;
	}
	
	public String getAddressName() {
		return addressName;
	}
	
	public LocationDto setAddressName(String addressName) {
		this.addressName = addressName;
		return this;
	}
	
	public String getRoadAddressName() {
		return roadAddressName;
	}
	
	public LocationDto setRoadAddressName(String roadAddressName) {
		this.roadAddressName = roadAddressName;
		return this;
	}
	
	public String getPlaceId() {
		return placeId;
	}
	
	public LocationDto setPlaceId(String placeId) {
		this.placeId = placeId;
		return this;
	}
	
	public String getPlaceName() {
		return placeName;
	}
	
	public LocationDto setPlaceName(String placeName) {
		this.placeName = placeName;
		return this;
	}
	
	public Constant getLocationType() {
		return locationType;
	}
	
	public LocationDto setLocationType(Constant locationType) {
		this.locationType = locationType;
		return this;
	}
}
