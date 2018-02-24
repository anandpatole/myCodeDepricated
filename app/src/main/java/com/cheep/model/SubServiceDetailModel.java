package com.cheep.model;

import android.support.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by bhavesh on 28/4/17.
 */
@Keep
public class SubServiceDetailModel implements Serializable {
    @SerializedName("cat_id")
    public int catId;

    @SerializedName("sub_cat_id")
    public int sub_cat_id;

    @SerializedName("name")
    public String name;

    //    public String monthlyPrice = "120";
    @SerializedName("min_unit")
    public String minUnit;
    @SerializedName("max_unit")
    public String maxUnit;
    @SerializedName("unit_price")
    public String unitPrice;

    public boolean isSelected = false;

    public List<SubServiceDetailModel> subServiceList;

    public int qty;
}
