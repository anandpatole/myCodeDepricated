package com.cheep.model;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

/**
 * Created by pankaj on 10/6/16.
 */
public class ProviderModel {

    @SerializedName("sp_id")
    public String providerId;

    @SerializedName("sp_distance")
    public String distance;

    @SerializedName("sp_user_name")
    public String userName;

    @SerializedName("sp_user_info")
    public String information;

    @SerializedName("sp_reported")
    public String spReported;

    @SerializedName("sp_task_count")
    public String taskCount;

    @SerializedName("sp_profile_image")
    public String profileUrl;

    @SerializedName("sp_verified")
    public String isVerified; // yes or no

    @SerializedName("sp_favourite")
    public String isFavourite;

    @SerializedName("sp_jobs_count")
    public String jobsCount;

    @SerializedName("sp_ratings_count")
    public String rating;

    @SerializedName("sp_reviews_count")
    public String reviews;

    @SerializedName("sp_request_type")
    public String requestType; // quote

    @SerializedName("sp_quote_price")
    public String quotePrice;

    @SerializedName("request_detail_status")
    public String request_detail_status;

    @SerializedName("experience")
    public String experience;

    @SerializedName("high_rating")
    public String high_rating;

    @SerializedName("low_price")
    public String low_price;

    @SerializedName("sp_locality")
    public String sp_locality;

    @SerializedName("sp_phone_number")
    public String sp_phone_number;

    @SerializedName("pro_level")
    public String pro_level="";


    public int getQuotePriceInInteger() {
        if (TextUtils.isEmpty(quotePrice)) {
            // Return lesser than 1
            return -1;
        } else {
            // Greater than one
            return (int) Double.parseDouble(quotePrice);
        }
        /*if (quotePrice == null) {
            return -1;
        }
        return Integer.parseInt(quotePrice);*/
    }

    /*public boolean isQuotePaid() {
        return false;
    }*/
}



































