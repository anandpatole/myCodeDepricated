package com.cheep.model;

import android.text.TextUtils;

public class LocationInfo {
    public String lat; // lattitude
    public String lng; // mLng
    public String Country; // country
    public String State; // administrative_area_level_1
    public String City; // mLocality, administrative_area_level_2
    public String Locality; // sublocality_level_1

    public String getDisplayLocationName() {
        return TextUtils.isEmpty(Locality) ? City : Locality;
    }
}