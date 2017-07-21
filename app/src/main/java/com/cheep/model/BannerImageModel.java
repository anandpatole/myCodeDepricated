package com.cheep.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BannerImageModel {
    @SerializedName("cat_image")
    @Expose
    public String imgCatImageUrl;
    @SerializedName("cat_id")
    @Expose
    public String cat_id;

}