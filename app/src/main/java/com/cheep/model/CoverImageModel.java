package com.cheep.model;

import android.support.annotation.Keep;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
@Keep
public class CoverImageModel {
    @SerializedName("img_key")
    @Expose
    public String imgKey;

    @SerializedName("img_url")
    @Expose
    public String imgUrl;
}