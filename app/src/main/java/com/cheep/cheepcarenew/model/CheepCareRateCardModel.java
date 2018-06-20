package com.cheep.cheepcarenew.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by majid on 20-06-2018.
 */

public class CheepCareRateCardModel implements Serializable {

    @SerializedName("cat_id")
    public String catId;

    @SerializedName("cat_name")
    public String catName;

    @SerializedName("cat_desc")
    public String catDesc;

    @SerializedName("catSlug")
    public String cat_slug;

    @SerializedName("cat_icon")
    public String catIcon;

    @SerializedName("is_favourite")
    public String isFavourite;

    @SerializedName("is_subscribed")
    public String isSubscribed;

    @SerializedName("cat_image")
    public CatImage catImage;

}


