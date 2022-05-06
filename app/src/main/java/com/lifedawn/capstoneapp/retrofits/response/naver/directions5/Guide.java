package com.lifedawn.capstoneapp.retrofits.response.naver.directions5;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Guide{


    @SerializedName("pointIndex")
    @Expose
    public String pointIndex;

    @SerializedName("type")
    @Expose
    public String type;

    @SerializedName("instructions")
    @Expose
    public String instructions;

    @SerializedName("distance")
    @Expose
    public String distance;

    @SerializedName("duration")
    @Expose
    public String duration;

    public String getPointIndex() {
        return pointIndex;
    }

    public void setPointIndex(String pointIndex) {
        this.pointIndex = pointIndex;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
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
}
