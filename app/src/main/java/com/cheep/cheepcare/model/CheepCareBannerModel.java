package com.cheep.cheepcare.model;

import android.support.annotation.Keep;

import com.cheep.utils.Utility;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Keep
public class CheepCareBannerModel {

    @SerializedName("id")
    @Expose
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

    @SerializedName("is_subscribed")
    @Expose
    public String isSubscribed = Utility.BOOLEAN.NO;

}