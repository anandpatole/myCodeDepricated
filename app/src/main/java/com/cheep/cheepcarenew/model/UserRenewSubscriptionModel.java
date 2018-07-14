package com.cheep.cheepcarenew.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by majid on 13-07-2018.
 */

public class UserRenewSubscriptionModel implements Serializable {

    //user_package_data
    @SerializedName("user_package_id")
    public String userPackageId;

    @SerializedName("package_id")
    public String packageId;

    @SerializedName("package_type")
    public String packageType;

    @SerializedName("package_duration")
    public String packageDuration;

    @SerializedName("start_date")
    public String startDate;

    @SerializedName("end_date")
    public String endDate;

    @SerializedName("address_id")
    public String addressId;

    @SerializedName("is_renew")
    public String isRenew;

    @SerializedName("name")
    public String name;

    @SerializedName("address")
    public String address;

    @SerializedName("pincode")
    public String pincode;

    @SerializedName("category")
    public String category;

    @SerializedName("paid_amount")
    public String paidAmount;

    @SerializedName("payment_type")
    public String paymentType;


    //package_detail
    @SerializedName("title")
    public String title;

    @SerializedName("subtitle")
    public String subtitle;

    @SerializedName("type")
    public String type;

    @SerializedName("old_price")
    public String old_price;

    @SerializedName("new_price")
    public String new_price;


    //city_care_detail
    @SerializedName("id")
    public String id;

    @SerializedName("title")
    public String cityTitle;

    @SerializedName("subtitle")
    public String citySubtitle;

    @SerializedName("city_slug")
    public String citySlug;

    @SerializedName("city_name")
    public String cityName;


    @SerializedName("total_amount")
    @Expose
    public double totalAmount;

    @SerializedName("discountAmount")
    @Expose
    public double discountAmount;

    @SerializedName("taxAmount")
    @Expose
    public String taxAmount;





}
