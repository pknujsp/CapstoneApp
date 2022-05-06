package com.lifedawn.capstoneapp.retrofits.response.naver.directions5;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
public class Traoptimal{
    @SerializedName("summary")
    @Expose
    public Summary summary;

    @SerializedName("path")
    @Expose
    public ArrayList<ArrayList<Double>> path;

    @SerializedName("section")
    @Expose
    public ArrayList<Section> section;

    @SerializedName("guide")
    @Expose
    public ArrayList<Guide> guide;
}
