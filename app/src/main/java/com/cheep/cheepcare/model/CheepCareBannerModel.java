package com.cheep.cheepcare.model;

import android.support.annotation.Keep;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Keep
public class CheepCareBannerModel {

    @SerializedName("id")
    public String id;

    @SerializedName("city_name")
    @Expose
    public String cityName;

    @SerializedName("city_slug")
    @Expose
    public String citySlug;

    @SerializedName("title")
    @Expose
    public String title;

    @SerializedName("subtitle")
    @Expose
    public String subtitle;

}