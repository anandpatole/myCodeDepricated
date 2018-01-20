package com.cheep.cheepcare.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by giteeka on 5/1/18.
 */

public class CityLandingPageModel {

    public class CityDetail implements Serializable {

        @SerializedName("id")
        @Expose
        public String id;
        @SerializedName("city_name")
        @Expose
        public String cityName;
        @SerializedName("city_slug")
        @Expose
        public String citySlug;
        @SerializedName("greeting_message")
        @Expose
        public String greetingMessage;
        @SerializedName("description")
        @Expose
        public String description;
        @SerializedName("city_tutorials")
        @Expose
        public List<CityTutorials> cityTutorials = null;

    }

    @SerializedName("cityDetail")
    @Expose
    public CityDetail cityDetail;


    @SerializedName("packageDetail")
    @Expose
    public List<PackageDetail> packageDetailList = null;


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
