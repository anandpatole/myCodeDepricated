package com.cheep.model;

import android.location.Address;
import android.support.annotation.Keep;

@Keep
public class GooglePlaceModel {

    public String description, placeid, reference, icon, name, distance, type, countryCode, country, mainString;
    public String lat, lng;
    public Address address;

}
