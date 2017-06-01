package com.cheep.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by pankaj on 9/27/16.
 */

public class JobCategoryModel {

    @SerializedName("cat_id")
    public String catId;
    @SerializedName("cat_name")
    public String catName;
    @SerializedName("cat_desc")
    public String catDesc;
    @SerializedName("cat_icon")
    public String catIcon;
    @SerializedName("cat_image")
    public String catImage;
    @SerializedName("cat_jobs")
    public String catJobs;
    @SerializedName("cat_sp_count")
    public String spCount;
    @SerializedName("cat_image_extra")
    public AttachmentModel catImageExtras;

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
