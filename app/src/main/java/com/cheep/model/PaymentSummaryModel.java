package com.cheep.model;

import android.support.annotation.Keep;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by giteeka on 20/11/17.
 */
@Keep
public class PaymentSummaryModel {

    @SerializedName("pro_payment_amount")
    @Expose
    public String proPaymentAmount;

    @SerializedName("promocode_price")
    @Expose
    public String promoCodePrice;
    @SerializedName("sub_total_amount")
    @Expose
    public String subTotalAmount;
    @SerializedName("additional_pending_amount")
    @Expose
    public String additionalPendingAmount;
    @SerializedName("total_amount")
    @Expose
    public String totalAmount;
    @SerializedName("wallet_balance_used")
    @Expose
    public String walletBalanceUsed;
    @SerializedName("task_user_category")
    @Expose
    public List<TaskUserCategory> taskUserCategory = null;

    @SerializedName("additional_paid_amount")
    @Expose
    public String additionalPaidAmount;

    @SerializedName("pro_payment_status")
    @Expose
    public String proPaymentStatus;


    @SerializedName("total_amount_status")
    @Expose
    public String totalAmountStatus;

    //    {
//        "data": {
//        "additional_paid_amount": "0",
//        "additional_pending_amount": "0",
//        "pro_payment_amount": "250.0",
//        "pro_payment_status": "pending",
//        "wallet_balance_used": "0.00",
//        "promocode_price": "0",
//        "non_office_hours_charge": "0.00",
//        "urgent_booking_charge": "0.00",
//        "extra_charge_status": "pending",
//        "total_amount": "250",
//        "sub_total_amount": "250",
//        "total_amount_status": "pending"
//    },
//        "message": "Task Summary fetched successfully.",
//            "status": "success",
//            "status_code": 200
//    }

    @SerializedName("non_office_hours_charge")
    @Expose
    public String nonOfficeHoursCharge;
    @SerializedName("urgent_booking_charge")
    @Expose
    public String urgentBookingCharge;
    @SerializedName("extra_charge_status")
    @Expose
    public String extraChargeStatus;


    public class TaskUserCategory {

        @SerializedName("user_category")
        @Expose
        public String userCategory;
        @SerializedName("user_sub_category")
        @Expose
        public String userSubCategory;
        @SerializedName("user_category_price")
        @Expose
        public String userCategoryPrice;

    }
}
