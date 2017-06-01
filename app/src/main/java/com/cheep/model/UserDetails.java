package com.cheep.model;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by bhavesh on 14/10/16.
 */

public class UserDetails {

    @SerializedName("user_id")
    public String UserID;

    @SerializedName("user_name")
    public String UserName;

    @SerializedName("email_address")
    public String Email;


    @SerializedName("user_info")
    public String UserInfo;

    @SerializedName("profile_img")
    public String ProfileImg;

    @SerializedName("profile_banner")
    public String ProfileBanner;

    @SerializedName("phone_number")
    public String PhoneNumber;

    @SerializedName("locality")
    public String locality;

    public String getLocality() {
        return TextUtils.isEmpty(locality) ? CityName : locality;
    }

    @SerializedName("city_id")
    public String CityID;

    @SerializedName("city_name")
    public String CityName;

    @SerializedName("user_lat")
    public String Latitude;

    @SerializedName("user_lng")
    public String Longitude;

    @SerializedName("login_with")
    public String LoginWith;

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

}
