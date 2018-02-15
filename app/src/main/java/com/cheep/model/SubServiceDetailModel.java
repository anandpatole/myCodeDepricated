package com.cheep.model;

import android.support.annotation.Keep;

import com.cheep.custom_view.expandablerecycleview.Parent;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by bhavesh on 28/4/17.
 */
@Keep
public class SubServiceDetailModel implements Parent<SubServiceDetailModel> {
    @SerializedName("cat_id")
    public int catId;

    @SerializedName("sub_cat_id")
    public int sub_cat_id;

    @SerializedName("name")
    public String name;

    public String monthlyPrice;

    public boolean isSelected = false;

    public List<SubServiceDetailModel> subServiceList;

    @Override
    public List<SubServiceDetailModel> getChildList() {
        return subServiceList;
    }

    @Override
    public boolean isInitiallyExpanded() {
        return false;
    }
}
