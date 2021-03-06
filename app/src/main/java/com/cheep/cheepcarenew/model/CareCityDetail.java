package com.cheep.cheepcarenew.model;

import android.support.annotation.Keep;

import com.cheep.utils.Utility;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

@Keep
public class CareCityDetail implements Serializable {

    @SerializedName("landing_screen_title_1")
    @Expose
    public String landingScreenTitle1;
    @SerializedName("landing_screen_title_2")
    @Expose
    public String landingScreenTitle2;


    @SerializedName("greeting_message")
    @Expose
    public String greetingMessage;
    @SerializedName("description")
    @Expose
    public String description;
    @SerializedName("city_tutorials")
    @Expose
    public List<CityLandingPageModel.CityTutorials> cityTutorials = null;

    @SerializedName("care_city_id")
    public String id;

    @SerializedName("care_city_name")
    @Expose
    public String cityName;

    @SerializedName("care_city_slug")
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


    @SerializedName("old_price")
    @Expose
    public String oldPrice;
    @SerializedName("new_price")
    @Expose
    public String newPrice;


}