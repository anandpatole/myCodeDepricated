package com.cheep.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class RateCardModel implements Serializable {

    @SerializedName("description")
    public String description;
    @SerializedName("labour_rate")
    public String labourRate;
    @SerializedName("add_unit")
    public String addUnit;

}
