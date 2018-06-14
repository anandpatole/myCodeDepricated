package com.cheep.model.ComparisionChart;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by majid on 06-06-2018.
 */

public class ComparisionChartModel implements Serializable {

    @SerializedName("feature_list")
    public ArrayList<FeatureList> featureLists;
    @SerializedName("price_list")
    public ArrayList<PriceList> priceLists;


}
