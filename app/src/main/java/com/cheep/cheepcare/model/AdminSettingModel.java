package com.cheep.cheepcare.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by giteeka on 23/1/18.
 */

public class AdminSettingModel {
    @SerializedName("GST_RATE")
    public String gstRate;

    @SerializedName("ADDITIONAL_CHARGE_FOR_SELECTING_SPECIFIC_TIME")
    public String additionalChargeForSelectingSpecificTime;

    @SerializedName("WORKING_SLOT_START_TIME")
    public String starttime;

    @SerializedName("WORKING_SLOT_END_TIME")
    public String endtime;

    @SerializedName("BUNDLE_DISCOUNT")
    public String bundleDiscount;

    @SerializedName("FIXED_TIME_SLOT_END_TIME")
    public String fixedTimeSlotEndTime;

    @SerializedName("BOOK_TASK_BEFORE_TIME_IN_WEEKDAYS_FOR_SUBSCRIBE_USER")
    public String bookTaskBeforeTimeInWeekdaysForSubscribeUser;

    @SerializedName("BOOK_TASK_BEFORE_TIME_IN_WEEKENDS_FOR_SUBSCRIBE_USER")
    public String bookTaskBeforeTimeInWeekendsForSubscribeUser;

    @SerializedName("BOOK_TASK_BEFORE_TIME_IN_WEEKDAYS_FOR_NORMAL_USER")
    public String bookTaskBeforeTimeInWeekdaysForNormalUser;

    @SerializedName("BOOK_TASK_BEFORE_TIME_IN_WEEKENDS_FOR_NORMAL_USER")
    public String bookTaskBeforeTimeInWeekendsForNormalUser;

}
