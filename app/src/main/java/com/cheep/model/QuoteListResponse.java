package com.cheep.model;

import android.support.annotation.Keep;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Anurag on 08-06-2017.
 */
@Keep
public class QuoteListResponse {
    @SerializedName("data")
    @Expose
    public List<ProviderModel> quoteList;

    @SerializedName("task_startdate")
    @Expose
    public String taskStartDate;

    @SerializedName("message")
    @Expose
    public String message;

    @SerializedName("status")
    @Expose
    public String status;

    @SerializedName("status_code")
    @Expose
    public int statusCode;

    public boolean isSuccess() {
        return statusCode == 200;
    }
}
