package com.cheep.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pankaj on 11/15/16.
 */

public class TaskDetailModel {
    @SerializedName("task_id")
    public String taskId;

    @SerializedName("task_desc")
    public String taskDesc;

    @SerializedName("task_startdate")
    public String taskStartdate;

    @SerializedName("task_image")
    public String taskImage;

    @SerializedName("task_address")
    public String taskAddress;

    @SerializedName("task_category")
    public String categoryName;


    @SerializedName("task_subcategory")
    public String subCategoryName;

    @SerializedName("task_subcategory_id")
    public String subCategoryID;

    @SerializedName("task_paid_amount")
    public String taskPaidAmount;

    @SerializedName("max_quote_price")
    public String maxQuotePrice;

    @SerializedName("task_category_id")
    public String categoryId;

    @SerializedName("task_status")
    public String taskStatus;

    @SerializedName("task_payment_status")
    public String taskPaymentStatus;

    @SerializedName("task_reviewed")
    public String ratingDone;

    @SerializedName("task_ratings")
    public String taskRatings;

    @SerializedName("task_cancel_reason")
    public String taskCancelReason;

    @SerializedName("sp_counts")
    public String providerCount;

    @SerializedName("task_reschedule_datetime")
    public String taskRescheduleDateTime;

    @SerializedName("additional_quote_amount")
    public String additionalQuoteAmount;

    @SerializedName("task_total_amount")
    public String task_total_amount;

    @SerializedName("additional_paid_amount")
    public String additional_paid_amount;

    // Added for New UI
    @SerializedName("task_address_id")
    public String taskAddressId;

    @SerializedName("cat_image")
    public String catImage;

    @SerializedName("cat_image_extra")
    public AttachmentModel catImageExtras;

    @SerializedName("profile_img_arr")
    public List<String> profile_img_arr;

    @SerializedName("live_lable_arr")
    public List<String> live_lable_arr=new ArrayList<>();

    // THis will be available in case Provider is Selected
    @SerializedName("sp_data")
    public ProviderModel selectedProvider;

    @SerializedName("quoted_sp_list")
    public ArrayList<ProviderModel> mQuotedSPList;

    public TaskDetailModel() {

    }

    public TaskDetailModel(String taskId, String taskDesc, String taskStartdate, String taskImage, String taskAddress, ProviderModel selectedProvider, String categoryName, String providerCount) {
        this.taskId = taskId;
        this.taskDesc = taskDesc;
        this.taskStartdate = taskStartdate;
        this.taskImage = taskImage;
        this.taskAddress = taskAddress;
        this.selectedProvider = selectedProvider;
        this.categoryName = categoryName;
        this.providerCount = providerCount;
    }


    /*//SP Selected
    public String sp_id;
    public String sp_name;
    public String profile_photo;
    public boolean isVerified;
    public String minToArrive;
    public String totalJobs;
    public String participateCounter;
    public String price;*/
}