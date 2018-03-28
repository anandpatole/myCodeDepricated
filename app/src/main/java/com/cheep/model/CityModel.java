package com.cheep.model;

import android.support.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by giteeka on 31/7/17.
 * Media model for Type image and video
 */

@Keep
public class CityModel implements Serializable {

    @SerializedName("city")
    public String city = "";

    @SerializedName("id")
    public String id = "";

}
