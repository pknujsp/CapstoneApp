package com.lifedawn.capstoneapp.retrofits.response.naver.directions5;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
public class Goal{
    @SerializedName("location")
    @Expose
    public ArrayList<Double> location;

    @SerializedName("dir")
    @Expose
    public String dir;

    public ArrayList<Double> getLocation() {
        return location;
    }

    public void setLocation(ArrayList<Double> location) {
        this.location = location;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }
}
