package com.cheep.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by majid on 08-06-2018.
 */

public class PackageTip {

    @SerializedName("title")
    @Expose
    public String title;

    @SerializedName("subtitle")
    @Expose
    public String subtitle;

}
