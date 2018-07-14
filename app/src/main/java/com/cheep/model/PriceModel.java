package com.cheep.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class PriceModel implements Serializable {

    @SerializedName("month_cost_for_3")
    @Expose
    public String monthCostFor3;
    @SerializedName("gst_for_3")
    @Expose
    public String gstFor3;
    @SerializedName("month_cost_for_6")
    @Expose
    public String monthCostFor6;
    @SerializedName("gst_for_6")
    @Expose
    public String gstFor6;
    @SerializedName("month_cost_for_12")
    @Expose
    public String monthCostFor12;
    @SerializedName("gst_for_12")
    @Expose
    public String gstFor12;
    @SerializedName("old_new_difference")
    @Expose
    public String oldNewDifference;

}