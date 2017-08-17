package com.cheep.model;

import android.support.annotation.Keep;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Keep
public class BannerImageModel {
    @SerializedName("cat_image")
    @Expose
    public String imgCatImageUrl;
    @SerializedName("banner_image")
    @Expose
    public String bannerImage;
    @SerializedName("cat_id")
    @Expose
    public String cat_id;
    @SerializedName("title")
    @Expose
    public String name;
    @SerializedName("minimum_selection")
    @Expose
    public String minimum_selection;

}