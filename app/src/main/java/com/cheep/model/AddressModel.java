package com.cheep.model;

import android.support.annotation.Keep;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Bhavesh V Patadiya on 10/13/16.
 */
@Keep
public class AddressModel {

    public String address_id;
    public String address_initials;
    public String address;
    public String category; //comes from NetworkUtility.TAGS.ADDRESS_TYPE.
    public String lat;
    public String lng;

    /**
     * This would only be usefull in case of Guest
     */
    public String cityName;

    /*public AddressModel(String address_id, *//*String name,*//* String address, String address_initials, String category) {
        this.address_id = address_id;
        this.address = address;
        this.address_initials = address_initials;
        this.category = category;
        this.lat = "";
        this.lng = "";
    }*/

    public LatLng getLatLng() {
        try {
            return new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LatLng(0, 0);
    }
}

