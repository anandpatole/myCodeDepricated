package com.cheep.model;

import android.support.annotation.Keep;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

/**
 * Created by Bhavesh V Patadiya on 10/13/16.
 */
@Keep
public class AddressModel implements Serializable {

    public String address_id;
    public String address_initials;
    public String address;
    public String category; //comes from NetworkUtility.TAGS.ADDRESS_TYPE.
    public String lat;
    public String lng;
    public String landmark;
    public String pincode;
    public String nickname;
    public String end_date;
    /**
     * This would only be useful in case of Guest
     */
    public String cityName;
    public String countryName;
    public String stateName;

    public boolean isSubscribedAddress = false;
    public boolean isSelected = false;

    public LatLng getLatLng() {
        try {
            return new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LatLng(0, 0);
    }
}

