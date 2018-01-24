package com.cheep.model;

import android.support.annotation.Keep;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by bhavesh on 14/10/16.
 */
@Keep
public class UserDetails {

    @SerializedName("user_id")
    public String userID;

    @SerializedName("user_name")
    public String userName;

    @SerializedName("email_address")
    public String email;

    @SerializedName("user_info")
    public String userInfo;

    @SerializedName("profile_img")
    public String profileImg;

    @SerializedName("profile_banner")
    public String profileBanner;

    @SerializedName("phone_number")
    public String phoneNumber;

    public String getDisplayLocationName() {
        return TextUtils.isEmpty(mLocality) ? mCityName : mLocality;
    }

    @SerializedName("login_with")
    public String loginWith;

    @SerializedName("address")
    public ArrayList<AddressModel> addressList;

//    @SerializedName("ws_access_key")
//    public String WSAccessKey;

    @SerializedName("language")
    public String language;


    @SerializedName("fb_app_id")
    public String fb_app_id;

    @SerializedName("tw_app_id")
    public String tw_app_id;

    @SerializedName("gp_app_id")
    public String gp_app_id;


    /**
     * Location Tracking for User
     */
    public String mCountry;
    public String mStateName;

    @SerializedName("city_id")
    public String CityID;

    @SerializedName("city_name")
    public String mCityName;

    @SerializedName("user_lat")
    public String mLat;

    @SerializedName("user_lng")
    public String mLng;

    @SerializedName("locality")
    public String mLocality;

    @SerializedName("refer_code")
    public String refer_code;

    @SerializedName("paytmData")
    public PaytmUserDetail mPaytmUserDetail;


    @Keep
    public static class PaytmUserDetail {

        @SerializedName("paytm_access_token")
        public String paytmAccessToken;

        @SerializedName("paytm_phone_number")
        public String paytmphoneNumber;

        @SerializedName("paytm_cust_id")
        public String paytmCustId;

        @SerializedName("access_token_expires_timestamp")
        public String accessTokenExpiresTimestamp;

    }
}
