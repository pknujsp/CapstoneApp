package com.lifedawn.capstoneapp.map;

import com.lifedawn.capstoneapp.common.constants.Constant;

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
