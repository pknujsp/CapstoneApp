package com.lifedawn.capstoneapp.retrofits.response.naver.directions5;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Section{
    @SerializedName("pointIndex")
    @Expose
    public String pointIndex;

    @SerializedName("pointCount")
    @Expose
    public String pointCount;

    @SerializedName("distance")
    @Expose
    public String distance;

    @SerializedName("name")
    @Expose
    public String name;

    @SerializedName("congestion")
    @Expose
    public String congestion;

    @SerializedName("speed")
    @Expose
    public String speed;

    public String getPointIndex() {
        return pointIndex;
    }

    public void setPointIndex(String pointIndex) {
        this.pointIndex = pointIndex;
    }

    public String getPointCount() {
        return pointCount;
    }

    public void setPointCount(String pointCount) {
        this.pointCount = pointCount;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCongestion() {
        return congestion;
    }

    public void setCongestion(String congestion) {
        this.congestion = congestion;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }
}
