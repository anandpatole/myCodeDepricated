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

    @SerializedName("ADDITIONAL_CHARGE_FOR_SELECTING_SPECIFIC_TIME")
    @Expose
    public String additionalChargeForSelectingSpecificTime;

    @SerializedName("WORKING_SLOT_START_TIME")
    @Expose
    public String starttime;

    @SerializedName("WORKING_SLOT_END_TIME")
    @Expose
    public String endtime;
}
