package com.cheep.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CoverImageModel {
    @SerializedName("img_key")
    @Expose
    public String imgKey;

    @SerializedName("img_url")
    @Expose
    public String imgUrl;
}