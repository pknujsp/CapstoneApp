package com.lifedawn.capstoneapp.retrofits.response.naver.directions5;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Summary {
	@SerializedName("start")
	@Expose
	public Start start;

	@SerializedName("goal")
	@Expose
	public Goal goal;

	@SerializedName("distance")
	@Expose
	public String distance;

	@SerializedName("duration")
	@Expose
	public String duration;

	@SerializedName("etaServiceType")
	@Expose
	public String etaServiceType;

	@SerializedName("departureTime")
	@Expose
	public String departureTime;

	@SerializedName("bbox")
	@Expose
	public ArrayList<ArrayList<Double>> bbox;

	@SerializedName("tollFare")
	@Expose
	public String tollFare;

	@SerializedName("taxiFare")
	@Expose
	public String taxiFare;

	@SerializedName("fuelPrice")
	@Expose
	public String fuelPrice;

	public Start getStart() {
		return start;
	}

	public void setStart(Start start) {
		this.start = start;
	}

	public Goal getGoal() {
		return goal;
	}

	public void setGoal(Goal goal) {
		this.goal = goal;
	}

	public String getDistance() {
		return distance;
	}

	public void setDistance(String distance) {
		this.distance = distance;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getEtaServiceType() {
		return etaServiceType;
	}

	public void setEtaServiceType(String etaServiceType) {
		this.etaServiceType = etaServiceType;
	}

	public String getDepartureTime() {
		return departureTime;
	}

	public void setDepartureTime(String departureTime) {
		this.departureTime = departureTime;
	}

	public ArrayList<ArrayList<Double>> getBbox() {
		return bbox;
	}

	public void setBbox(ArrayList<ArrayList<Double>> bbox) {
		this.bbox = bbox;
	}

	public String getTollFare() {
		return tollFare;
	}

	public void setTollFare(String tollFare) {
		this.tollFare = tollFare;
	}

	public String getTaxiFare() {
		return taxiFare;
	}

	public void setTaxiFare(String taxiFare) {
		this.taxiFare = taxiFare;
	}

	public String getFuelPrice() {
		return fuelPrice;
	}

	public void setFuelPrice(String fuelPrice) {
		this.fuelPrice = fuelPrice;
	}
}
