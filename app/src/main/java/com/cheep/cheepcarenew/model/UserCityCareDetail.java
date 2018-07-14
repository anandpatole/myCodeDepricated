package com.cheep.cheepcarenew.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by majid on 12-07-2018.
 */

public class UserCityCareDetail implements Serializable {

    @SerializedName("id")
    public String id;

    @SerializedName("title")
    public String title;

    @SerializedName("subtitle")
    public String subtitle;

    @SerializedName("city_slug")
    public String citySlug;

    @SerializedName("city_name")
    public String cityName;

}
