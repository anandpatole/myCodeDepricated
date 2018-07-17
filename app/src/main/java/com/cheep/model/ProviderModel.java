package com.cheep.model;

import android.support.annotation.Keep;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by pankaj on 10/6/16.
 */
@Keep
public class ProviderModel implements Serializable {

    @SerializedName("sp_user_id")
    public String providerId;

    @SerializedName("sp_distance")
    public String distance;

    @SerializedName("sp_time")
    public String time;

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

    @SerializedName(value = "sp_verified", alternate = "is_verified")
    public String isVerified; // yes or no

    @SerializedName("sp_favourite")
    public String isFavourite;

    @SerializedName("sp_jobs_count")
    public String jobsCount;

    @SerializedName("sp_ratings_count")
    public String rating;

    @SerializedName("sp_reviews_count")
    public String reviews;

//   1   "task_id": "6223",
//              "sp_user_id": "398",
//          2    "cat_name": "Cook",
//              "sp_user_name": "Test1",
//              "sp_profile_image": "https://s3.ap-south-1.amazonaws.com/cheepapp/service_provider/profile/thumb/1521713902_5ab382eec68af_image.jpg",
//           3   "is_verified": "no",
//              "sp_favourite": "no",
//              "pro_level": "4"

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

    @SerializedName("happy_home_count")
    public String happyHomeCount;

    @SerializedName("sp_per_off")
    public String discount;

    @SerializedName("pro_level")
    public String pro_level;

    @SerializedName("sp_without_gst_quote_price")
    public String spWithoutGstQuotePrice;

    /**
     * for pay now
     */
    public String actualQuotePrice = "";

    @SerializedName("live_lable_arr")
    public List<String> offerList;


    @SerializedName("sp_categories")
    public List<String> categories;

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



































