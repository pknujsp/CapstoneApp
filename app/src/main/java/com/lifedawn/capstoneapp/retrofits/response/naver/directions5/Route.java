package com.lifedawn.capstoneapp.retrofits.response.naver.directions5;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
public class Route{
    @SerializedName("traoptimal")
    @Expose
    public ArrayList<Traoptimal> traoptimal;

    public ArrayList<Traoptimal> getTraoptimal() {
        return traoptimal;
    }

    public void setTraoptimal(ArrayList<Traoptimal> traoptimal) {
        this.traoptimal = traoptimal;
    }
}
