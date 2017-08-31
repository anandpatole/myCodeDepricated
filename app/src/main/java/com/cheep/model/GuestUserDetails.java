package com.cheep.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by bhavesh on 25/8/17.
 */

public class GuestUserDetails {
    public String mLat;
    public String mLon;
    public String mCityID;
    public String mCityName;
    public String mCountryName;
    public String mStateName;

    @SerializedName("address")
    public ArrayList<AddressModel> addressList;
}
