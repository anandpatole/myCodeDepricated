package com.cheep.model.ComparisionChart;


import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by majid on 13-06-2018.
 */

public class FeatureList implements Serializable {

    @SerializedName("feature")
    public String feature;

    @SerializedName("premium")
    public String premium;

    @SerializedName("normal")
    public String normal;

}
