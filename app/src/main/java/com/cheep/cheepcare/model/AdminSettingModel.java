package com.cheep.cheepcare.model;

import com.cheep.utils.Utility;
import com.google.gson.annotations.SerializedName;

/**
 * Created by giteeka on 23/1/18.
 */

public class AdminSettingModel {
    @SerializedName("GST_RATE")
    public String gstRate;

    @SerializedName("ADDITIONAL_CHARGE_FOR_SELECTING_SPECIFIC_TIME")
    public String additionalChargeForSelectingSpecificTime;

    @SerializedName("WORKING_START_HOUR")
    public String starttime = Utility.EMPTY_STRING;

    @SerializedName("WORKING_END_HOUR")
    public String endtime = Utility.EMPTY_STRING;

}
