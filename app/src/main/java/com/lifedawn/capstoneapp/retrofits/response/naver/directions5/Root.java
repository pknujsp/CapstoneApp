package com.lifedawn.capstoneapp.retrofits.response.naver.directions5;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.meta.Exclusive;

public class Root{
    @SerializedName("code")
    @Expose
    public String code;

    @SerializedName("message")
    @Expose
    public String message;

    @SerializedName("currentDateTime")
    @Expose
    public String currentDateTime;

    @SerializedName("route")
    @Expose
    public Route route;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCurrentDateTime() {
        return currentDateTime;
    }

    public void setCurrentDateTime(String currentDateTime) {
        this.currentDateTime = currentDateTime;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }
}
