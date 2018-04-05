package com.cheep.model;

import android.support.annotation.Keep;

import com.cheep.cheepcare.model.AdminSettingModel;
import com.cheep.utils.Utility;

import java.util.List;

@Keep
public class MessageEvent {
    public int BROADCAST_ACTION;
    public String id;
    public String isFav;
    public String commentCount;
    public String taskStartdate;
    public String taskStatus;

    // When TASK_PAID && TASK_PROCESSING as Broadcast type
    public String taskPaidAmount;

    // Used for QUOTE_REQUEST Notification Type
    public String max_quote_price;
    public String sp_counts;
    public String quoted_sp_image_url;

    // used for REQUEST_FOR_DETAIL Notification Type
    public String request_detail_status;
    public String spUserId;

    // Used for additional payment requested
    public String additional_quote_amount;

    // Used for Making Alert Enable/Disable
    public String total_ongoing_task;

    //for re-opening TaskCreationCCActivity when user selects
    public JobCategoryModel jobCategoryModel;
    public AddressModel addressModel;
    public String packageType;
    public String packageId;
    public List<AddressModel> selectedAddressList;
    public AdminSettingModel adminSettingModel;
    //for re-opening TaskCreationCCActivity when user selects

    // class that would going to store the details we will get from Paytm Response
    public PaytmResponse paytmResponse;
    public String taskRating;

    /**
     * Hold the data for Paytm Transaction response
     */
    public static class PaytmResponse {
        // Decide whether the transaction is true or false
        public boolean isSuccess;

        // Response code in interger "01", "02",..
        public String ResponseCode;

        // This would be in JSON Formate
        public String ResponsePayLoad; // In JSON Format

        public String subsId = Utility.EMPTY_STRING;
        public String isSubscription = Utility.BOOLEAN.NO;
    }
}

