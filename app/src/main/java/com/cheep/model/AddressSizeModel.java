package com.cheep.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by giteeka on 11/6/18.
 */
public class AddressSizeModel {

    @SerializedName("id")
    @Expose
    public String id;
    @SerializedName("value")
    @Expose
    public String value;
    public boolean isSelected = false;
}
