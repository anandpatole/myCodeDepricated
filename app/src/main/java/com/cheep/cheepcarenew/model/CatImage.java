package com.cheep.cheepcarenew.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by majid on 20-06-2018.
 */

public class CatImage implements Serializable {

    @SerializedName("thumb")
    public String thumb;

    @SerializedName("medium")
    public String medium;

    @SerializedName("original")
    public String original;


}
