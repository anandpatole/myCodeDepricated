package com.cheep.cheepcarenew.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by majid on 27-06-2018.
 */

public class UserPackageDataModel implements Serializable {

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

}
