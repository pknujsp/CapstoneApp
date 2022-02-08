package com.lifedawn.capstoneapp.retrofits.response.kakaolocal.place;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.KakaoLocalResponse;

import java.util.List;

public class PlaceResponse extends KakaoLocalResponse {
	@SerializedName("meta")
	@Expose
	private Meta placeMeta;
	
	@SerializedName("documents")
	@Expose
	private List<Documents> placeDocuments;
	
	public Meta getPlaceMeta() {
		return placeMeta;
	}
	
	public void setPlaceMeta(Meta placeMeta) {
		this.placeMeta = placeMeta;
	}
	
	public List<Documents> getPlaceDocuments() {
		return placeDocuments;
	}
	
	public void setPlaceDocuments(List<Documents> placeDocuments) {
		this.placeDocuments = placeDocuments;
	}
	
	public static class Meta {
		@SerializedName("total_count")
		@Expose
		private int totalCount;
		
		@SerializedName("pageable_count")
		@Expose
		private int pageableCount;
		
		@SerializedName("is_end")
		@Expose
		private boolean isEnd;
		
		@SerializedName("same_name")
		@Expose
		private PlaceSameName placeSameName;
		
		public static class PlaceSameName {
			@SerializedName("region")
			@Expose
			private String[] region;
			
			@SerializedName("keyword")
			@Expose
			private String keyword;
			
			@SerializedName("selected_region")
			@Expose
			private String selectedRegion;
			
			public String[] getRegion() {
				return region;
			}
			
			public void setRegion(String[] region) {
				this.region = region;
			}
			
			public String getKeyword() {
				return keyword;
			}
			
			public void setKeyword(String keyword) {
				this.keyword = keyword;
			}
			
			public String getSelectedRegion() {
				return selectedRegion;
			}
			
			public void setSelectedRegion(String selectedRegion) {
				this.selectedRegion = selectedRegion;
			}
		}
		
		public int getTotalCount() {
			return totalCount;
		}
		
		public void setTotalCount(int totalCount) {
			this.totalCount = totalCount;
		}
		
		public int getPageableCount() {
			return pageableCount;
		}
		
		public void setPageableCount(int pageableCount) {
			this.pageableCount = pageableCount;
		}
		
		public boolean isEnd() {
			return isEnd;
		}
		
		public void setEnd(boolean end) {
			isEnd = end;
		}
		
		public PlaceSameName getPlaceSameName() {
			return placeSameName;
		}
		
		public void setPlaceSameName(PlaceSameName placeSameName) {
			this.placeSameName = placeSameName;
		}
	}
	
	public static class Documents {
		@SerializedName("id")
		@Expose
		private String id;
		
		@SerializedName("place_name")
		@Expose
		private String placeName;
		
		@SerializedName("category_name")
		@Expose
		private String categoryName;
		
		@SerializedName("category_group_code")
		@Expose
		private String categoryGroupCode;
		
		@SerializedName("category_group_name")
		@Expose
		private String categoryGroupName;
		
		@SerializedName("phone")
		@Expose
		private String phone;
		
		@SerializedName("address_name")
		@Expose
		private String addressName;
		
		@SerializedName("road_address_name")
		@Expose
		private String roadAddressName;
		
		@SerializedName("x")
		@Expose
		private String x;
		
		@SerializedName("y")
		@Expose
		private String y;
		
		@SerializedName("place_url")
		@Expose
		private String placeUrl;
		
		@SerializedName("distance")
		@Expose
		private String distance;
		
		public String getId() {
			return id;
		}
		
		public void setId(String id) {
			this.id = id;
		}
		
		public String getPlaceName() {
			return placeName;
		}
		
		public void setPlaceName(String placeName) {
			this.placeName = placeName;
		}
		
		public String getCategoryName() {
			return categoryName;
		}
		
		public void setCategoryName(String categoryName) {
			this.categoryName = categoryName;
		}
		
		public String getCategoryGroupCode() {
			return categoryGroupCode;
		}
		
		public void setCategoryGroupCode(String categoryGroupCode) {
			this.categoryGroupCode = categoryGroupCode;
		}
		
		public String getCategoryGroupName() {
			return categoryGroupName;
		}
		
		public void setCategoryGroupName(String categoryGroupName) {
			this.categoryGroupName = categoryGroupName;
		}
		
		public String getPhone() {
			return phone;
		}
		
		public void setPhone(String phone) {
			this.phone = phone;
		}
		
		public String getAddressName() {
			return addressName;
		}
		
		public void setAddressName(String addressName) {
			this.addressName = addressName;
		}
		
		public String getRoadAddressName() {
			return roadAddressName;
		}
		
		public void setRoadAddressName(String roadAddressName) {
			this.roadAddressName = roadAddressName;
		}
		
		public String getX() {
			return x;
		}
		
		public void setX(String x) {
			this.x = x;
		}
		
		public String getY() {
			return y;
		}
		
		public void setY(String y) {
			this.y = y;
		}
		
		public String getPlaceUrl() {
			return placeUrl;
		}
		
		public void setPlaceUrl(String placeUrl) {
			this.placeUrl = placeUrl;
		}
		
		public String getDistance() {
			return distance;
		}
		
		public void setDistance(String distance) {
			this.distance = distance;
		}
	}
}
