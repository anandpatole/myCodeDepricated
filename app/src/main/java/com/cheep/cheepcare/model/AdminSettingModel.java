package com.cheep.cheepcare.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by giteeka on 23/1/18.
 */

public class AdminSettingModel {
    @SerializedName("GST_RATE")
    @Expose
    public String gstRate;
}
