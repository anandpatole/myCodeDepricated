package com.cheep.model;

import android.content.Context;
import android.support.annotation.Keep;
import android.text.TextUtils;

import com.cheep.utils.Utility;
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
    public String is_subscribe = Utility.BOOLEAN.NO;
    public String limit_cnt;

    /**
     * This would only be useful in case of Guest
     */
    public String cityName;
    public String countryName;
    public String stateName;

    public boolean isSelected = false;

    public LatLng getLatLng() {
        try {
            return new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LatLng(0, 0);
    }


    public String getAddressWithInitials() {
        if (TextUtils.isEmpty(address_initials))
            return address;
        else
            return address_initials + ", " + address;
    }

    public String getNicknameString(Context context) {
        return !TextUtils.isEmpty(nickname) ? nickname : context.getString(Utility.getAddressCategoryString(category));
    }
}

