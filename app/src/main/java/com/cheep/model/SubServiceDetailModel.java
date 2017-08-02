package com.cheep.model;

import android.support.annotation.Keep;

import com.google.gson.annotations.SerializedName;

/**
 * Created by bhavesh on 28/4/17.
 */
@Keep
public class SubServiceDetailModel {
    @SerializedName("cat_id")
    public int catId;

    @SerializedName("sub_cat_id")
    public int sub_cat_id;

    @SerializedName("name")
    public String name;

    public boolean isSelected = false;
}
