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

    public Summary getSummary() {
        return summary;
    }

    public Traoptimal setSummary(Summary summary) {
        this.summary = summary;
        return this;
    }

    public ArrayList<ArrayList<Double>> getPath() {
        return path;
    }

    public Traoptimal setPath(ArrayList<ArrayList<Double>> path) {
        this.path = path;
        return this;
    }

    public ArrayList<Section> getSection() {
        return section;
    }

    public Traoptimal setSection(ArrayList<Section> section) {
        this.section = section;
        return this;
    }

    public ArrayList<Guide> getGuide() {
        return guide;
    }

    public Traoptimal setGuide(ArrayList<Guide> guide) {
        this.guide = guide;
        return this;
    }
}
