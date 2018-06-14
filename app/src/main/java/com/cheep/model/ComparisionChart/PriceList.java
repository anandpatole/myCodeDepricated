package com.cheep.model.ComparisionChart;


import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by majid on 13-06-2018.
 */

public class PriceList implements Serializable{

    @SerializedName("type")
    public String type;

    @SerializedName("old_price")
    public String oldPrice;

    @SerializedName("new_price")
    public String newPrice;

}
