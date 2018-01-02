package com.cheep.model;

import android.support.annotation.Keep;
import android.util.Log;

import com.cheep.utils.Utility;
import com.google.gson.annotations.SerializedName;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by pankaj on 10/12/16.
 */
@Keep
public class HistoryModel {
    private static final String TAG = HistoryModel.class.getSimpleName();
    @SerializedName("sp_user_id")
    public String sp_user_id;

    @SerializedName("sp_user_name")
    public String sp_user_name;

    @SerializedName("sp_profile_image")
    public String sp_profile_image;

    @SerializedName("paid_amount")
    public String paid_amount;

    @SerializedName("saved_amount")
    public String saved_amount;

    @SerializedName("payment_date")
    public String payment_date;

    @SerializedName("task_category")
    public String task_category;

    public String getPaymentDate() {
        Log.i(TAG, "getPaymentDate: Initial Date: " + payment_date);
        SimpleDateFormat serverSDF = new SimpleDateFormat(Utility.DATE_TIME_FORMAT_SERVICE_YEAR/*, Locale.US*/);
        serverSDF.setTimeZone(TimeZone.getTimeZone(Utility.UTC));
        SimpleDateFormat outputSDF = new SimpleDateFormat(Utility.DATE_TIME_FORMAT_SERVICE_YEAR/*, Locale.US*/);
        Date date = null;
        try {
            date = serverSDF.parse(payment_date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return outputSDF.format(date);
    }
}
