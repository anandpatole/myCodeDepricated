package com.cheep.model;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by bhavesh on 25/8/17.
 */

public class GuestUserDetails {

    public String mLat;
    public String mLng;
    public String mCityID;
    public String mCityName;
    public String mCountryName;
    public String mStateName;
    public String mLocality;

    @SerializedName("address")
    public ArrayList<AddressModel> addressList;

    public String getDisplayLocationName() {
        return TextUtils.isEmpty(mLocality) ? mCityName : mLocality;
    }
}
