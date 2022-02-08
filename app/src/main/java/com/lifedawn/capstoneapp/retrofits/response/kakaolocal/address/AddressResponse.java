package com.lifedawn.capstoneapp.retrofits.response.kakaolocal.address;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.KakaoLocalResponse;

import java.util.List;

public class AddressResponse extends KakaoLocalResponse {
	@SerializedName("meta")
	@Expose
	private Meta meta;
	
	@SerializedName("documents")
	@Expose
	private List<Documents> documentsList;
	
	public Meta getMeta() {
		return meta;
	}
	
	public void setMeta(Meta meta) {
		this.meta = meta;
	}
	
	public List<Documents> getDocumentsList() {
		return documentsList;
	}
	
	public void setDocumentsList(List<Documents> documentsList) {
		this.documentsList = documentsList;
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
	}
	
	public static class Documents {
		@SerializedName("address_name")
		@Expose
		private String addressName;
		
		@SerializedName("address_type")
		@Expose
		private String addressType;
		
		@SerializedName("x")
		@Expose
		private String x;
		
		@SerializedName("y")
		@Expose
		private String y;
		
		@SerializedName("address")
		@Expose
		private Address addressResponseAddress;
		
		@SerializedName("road_address")
		@Expose
		private RoadAddress addressResponseRoadAddress;
		
		public String getAddressName() {
			return addressName;
		}
		
		public void setAddressName(String addressName) {
			this.addressName = addressName;
		}
		
		public String getAddressType() {
			return addressType;
		}
		
		public void setAddressType(String addressType) {
			this.addressType = addressType;
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
		
		public Address getAddressResponseAddress() {
			return addressResponseAddress;
		}
		
		public void setAddressResponseAddress(Address addressResponseAddress) {
			this.addressResponseAddress = addressResponseAddress;
		}
		
		public RoadAddress getAddressResponseRoadAddress() {
			return addressResponseRoadAddress;
		}
		
		public void setAddressResponseRoadAddress(RoadAddress addressResponseRoadAddress) {
			this.addressResponseRoadAddress = addressResponseRoadAddress;
		}
		
		public static class Address {
			@SerializedName("address_name")
			@Expose
			private String addressName;
			
			@SerializedName("region_1depth_name")
			@Expose
			private String region1DepthName;
			
			@SerializedName("region_2depth_name")
			@Expose
			private String region2DepthName;
			
			@SerializedName("region_3depth_name")
			@Expose
			private String region3DepthName;
			
			@SerializedName("region_3depth_h_name")
			@Expose
			private String region3DepthHName;
			
			@SerializedName("h_code")
			@Expose
			private String hCode;
			
			@SerializedName("b_code")
			@Expose
			private String bCode;
			
			@SerializedName("mountain_yn")
			@Expose
			private String mountainYn;
			
			@SerializedName("main_address_no")
			@Expose
			private String mainAddressNo;
			
			@SerializedName("sub_address_no")
			@Expose
			private String subAddressNo;
			
			@SerializedName("zip_code")
			@Expose
			private String zipCode;
			
			@SerializedName("x")
			@Expose
			private double x;
			
			@SerializedName("y")
			@Expose
			private double y;
			
			public String getAddressName() {
				return addressName;
			}
			
			public void setAddressName(String addressName) {
				this.addressName = addressName;
			}
			
			public String getRegion1DepthName() {
				return region1DepthName;
			}
			
			public void setRegion1DepthName(String region1DepthName) {
				this.region1DepthName = region1DepthName;
			}
			
			public String getRegion2DepthName() {
				return region2DepthName;
			}
			
			public void setRegion2DepthName(String region2DepthName) {
				this.region2DepthName = region2DepthName;
			}
			
			public String getRegion3DepthName() {
				return region3DepthName;
			}
			
			public void setRegion3DepthName(String region3DepthName) {
				this.region3DepthName = region3DepthName;
			}
			
			public String getRegion3DepthHName() {
				return region3DepthHName;
			}
			
			public void setRegion3DepthHName(String region3DepthHName) {
				this.region3DepthHName = region3DepthHName;
			}
			
			public String gethCode() {
				return hCode;
			}
			
			public void sethCode(String hCode) {
				this.hCode = hCode;
			}
			
			public String getbCode() {
				return bCode;
			}
			
			public void setbCode(String bCode) {
				this.bCode = bCode;
			}
			
			public String getMountainYn() {
				return mountainYn;
			}
			
			public void setMountainYn(String mountainYn) {
				this.mountainYn = mountainYn;
			}
			
			public String getMainAddressNo() {
				return mainAddressNo;
			}
			
			public void setMainAddressNo(String mainAddressNo) {
				this.mainAddressNo = mainAddressNo;
			}
			
			public String getSubAddressNo() {
				return subAddressNo;
			}
			
			public void setSubAddressNo(String subAddressNo) {
				this.subAddressNo = subAddressNo;
			}
			
			public String getZipCode() {
				return zipCode;
			}
			
			public void setZipCode(String zipCode) {
				this.zipCode = zipCode;
			}
			
			public double getX() {
				return x;
			}
			
			public void setX(double x) {
				this.x = x;
			}
			
			public double getY() {
				return y;
			}
			
			public void setY(double y) {
				this.y = y;
			}
		}
		
		public static class RoadAddress {
			@SerializedName("address_name")
			@Expose
			private String addressName;
			
			@SerializedName("region_1depth_name")
			@Expose
			private String region1DepthName;
			
			@SerializedName("region_2depth_name")
			@Expose
			private String region2DepthName;
			
			@SerializedName("region_3depth_name")
			@Expose
			private String region3DepthName;
			
			@SerializedName("road_name")
			@Expose
			private String roadName;
			
			@SerializedName("underground_yn")
			@Expose
			private String undergroundYn;
			
			@SerializedName("main_building_no")
			@Expose
			private String mainBuildingNo;
			
			@SerializedName("sub_building_no")
			@Expose
			private String subBuildingNo;
			
			@SerializedName("building_name")
			@Expose
			private String buildingName;
			
			@SerializedName("zone_no")
			@Expose
			private String zoneNo;
			
			@SerializedName("x")
			@Expose
			private double x;
			
			@SerializedName("y")
			@Expose
			private double y;
			
			public String getAddressName() {
				return addressName;
			}
			
			public void setAddressName(String addressName) {
				this.addressName = addressName;
			}
			
			public String getRegion1DepthName() {
				return region1DepthName;
			}
			
			public void setRegion1DepthName(String region1DepthName) {
				this.region1DepthName = region1DepthName;
			}
			
			public String getRegion2DepthName() {
				return region2DepthName;
			}
			
			public void setRegion2DepthName(String region2DepthName) {
				this.region2DepthName = region2DepthName;
			}
			
			public String getRegion3DepthName() {
				return region3DepthName;
			}
			
			public void setRegion3DepthName(String region3DepthName) {
				this.region3DepthName = region3DepthName;
			}
			
			public String getRoadName() {
				return roadName;
			}
			
			public void setRoadName(String roadName) {
				this.roadName = roadName;
			}
			
			public String getUndergroundYn() {
				return undergroundYn;
			}
			
			public void setUndergroundYn(String undergroundYn) {
				this.undergroundYn = undergroundYn;
			}
			
			public String getMainBuildingNo() {
				return mainBuildingNo;
			}
			
			public void setMainBuildingNo(String mainBuildingNo) {
				this.mainBuildingNo = mainBuildingNo;
			}
			
			public String getSubBuildingNo() {
				return subBuildingNo;
			}
			
			public void setSubBuildingNo(String subBuildingNo) {
				this.subBuildingNo = subBuildingNo;
			}
			
			public String getBuildingName() {
				return buildingName;
			}
			
			public void setBuildingName(String buildingName) {
				this.buildingName = buildingName;
			}
			
			public String getZoneNo() {
				return zoneNo;
			}
			
			public void setZoneNo(String zoneNo) {
				this.zoneNo = zoneNo;
			}
			
			public double getX() {
				return x;
			}
			
			public void setX(double x) {
				this.x = x;
			}
			
			public double getY() {
				return y;
			}
			
			public void setY(double y) {
				this.y = y;
			}
		}
	}
}
