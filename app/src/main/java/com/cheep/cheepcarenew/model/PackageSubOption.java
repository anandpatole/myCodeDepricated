package com.cheep.cheepcarenew.model;

import android.support.annotation.Keep;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

@Keep
public class PackageSubOption implements Serializable {

    @SerializedName("package_suboption_id")
    @Expose
    public String packageOptionId;
    @SerializedName("package_suboption_title")
    @Expose
    public String packageSuboptionTitle;
    @SerializedName("unit_price")
    @Expose
    public String unitPrice;
    @SerializedName("monthly_price")
    @Expose
    public String monthlyPrice;
    @SerializedName("sixmonth_price")
    @Expose
    public String sixmonthPrice;
    @SerializedName("annual_price")
    @Expose
    public String annualPrice;



    //  "min_unit": "1",
//          "max_unit": "5"
    @SerializedName("min_unit")
    @Expose
    public String minUnit;

    @SerializedName("max_unit")
    @Expose
    public String maxUnit;

    public boolean isSelected = false;

    public int qty = 1;
}