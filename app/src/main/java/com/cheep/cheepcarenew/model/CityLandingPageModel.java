package com.cheep.cheepcarenew.model;

import com.cheep.model.PackageTip;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by giteeka on 5/1/18.
 */

public class CityLandingPageModel {

    @SerializedName("cityDetail")
    @Expose
    public CareCityDetail careCityDetail;
    @SerializedName("admin_setting")
    @Expose
    public AdminSettingModel adminSetting;


    @SerializedName("packageDetail")
    @Expose
    public List<PackageDetail> packageDetailList = null;

    @SerializedName("packageTip")
    @Expose
    public PackageTip packageTip = null;


    public class CityTutorials {
        @SerializedName("image")
        @Expose
        public String image;

        @SerializedName("description")
        @Expose
        public String description;


        @SerializedName("title")
        @Expose
        public String title;
    }

}
