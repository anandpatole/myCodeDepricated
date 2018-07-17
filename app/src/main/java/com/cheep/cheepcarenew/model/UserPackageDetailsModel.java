package com.cheep.cheepcarenew.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by majid on 12-07-2018.
 */

public class UserPackageDetailsModel implements Serializable {

    @SerializedName("title")
    public String title;

    @SerializedName("subtitle")
    public String subtitle;

    @SerializedName("type")
    public String type;

    @SerializedName("old_price")
    public String old_price;

    @SerializedName("new_price")
    public String new_price;


}
