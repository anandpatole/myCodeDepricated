package com.cheep.model;

import android.support.annotation.Keep;

import com.cheep.cheepcarenew.model.CareCityDetail;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by pankaj on 9/27/16.
 */
@Keep
public class JobCategoryModel implements Serializable {

    @SerializedName("cat_id")
    public String catId;
    @SerializedName("cat_name")
    public String catName;
    @SerializedName("cat_desc")
    public String catDesc;
    @SerializedName("cat_slug")
    public String catSlug;
    @SerializedName("cat_type")
    public String catType;
    @SerializedName("cat_icon")
    public String catIcon;

    @SerializedName("cat_new_price")
    public String catNewPrice;

   @SerializedName("cat_old_price")
    public String catOldPrice;

    @SerializedName("cat_jobs")
    public String catJobs;

    @SerializedName("cat_sp_count")
    public String spCount;

    @SerializedName("cat_image")
    public AttachmentModel catImageExtras;

    @SerializedName("is_favourite")
    @Expose
    public String isFavourite;
    @SerializedName("live_lable_arr")
    public List<String> live_lable_arr;

    @SerializedName("pro_image_per_cat")
    public List<String> proImagesPerCategory;

    @SerializedName("care_city_arr")
    public List<CareCityDetail> careCityData;
    // Newly added
    @SerializedName("is_subscribed")
    @Expose
    public String isSubscribed;

//    @SerializedName("cat_task_type")
//    public String catTaskType;


    /*public String categoryName;
    public int totalNoOfJobs;

    //Temp for resource
    public int iconRes;
    public int backgroundRes;

    public JobCategoryModel(String categoryName, int totalNoOfJobs, int iconRes, int backgroundRes) {
        this.categoryName = categoryName;
        this.totalNoOfJobs = totalNoOfJobs;
        this.iconRes = iconRes;
        this.backgroundRes = backgroundRes;
    }*/

    //Copy constructor
    /*public JobCategoryModel(JobCategoryModel jobCategoryModel) {
        this.categoryName = jobCategoryModel.categoryName;
        this.totalNoOfJobs = jobCategoryModel.totalNoOfJobs;
        this.iconRes = jobCategoryModel.iconRes;
        this.backgroundRes = jobCategoryModel.backgroundRes;
    }*/
}
