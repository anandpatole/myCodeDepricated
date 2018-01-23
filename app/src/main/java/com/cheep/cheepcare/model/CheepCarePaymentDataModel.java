package com.cheep.cheepcare.model;

import android.support.annotation.Keep;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by giteeka on 23/1/18.
 */

@Keep
public class CheepCarePaymentDataModel {

    @SerializedName("total_amount")
    @Expose
    public double totalAmount;
    @SerializedName("promocode")
    @Expose
    public String promocode;
    @SerializedName("promocode_price")
    @Expose
    public double promocodePrice;
    @SerializedName("payable_amount")
    @Expose
    public double payableAmount;
    @SerializedName("tax_amount")
    @Expose
    public String taxAmount;
    @SerializedName("is_annually")
    @Expose
    public String isAnnually;
    @SerializedName("care_city_id")
    @Expose
    public String careCityId;
    @SerializedName("payment_type")
    @Expose
    public String paymentType;
    @SerializedName("payment_log")
    @Expose
    public String paymentLog;
    @SerializedName("dsaCode")
    @Expose
    public String dsaCode;
    @SerializedName("bundlediscount_percent")
    @Expose
    public double bundlediscountPercent;
    @SerializedName("bundlediscount_price")
    @Expose
    public double bundlediscountPrice;

    @Override
    public String toString() {
        return " totalAmount :: " + totalAmount + "\n " +
                " promocode :: " + promocode + "\n " +
                " payable_amount :: " + payableAmount + "\n " +
                " taxAmount :: " + taxAmount + "\n " +
                " isAnnually :: " + isAnnually + "\n " +
                " careCityId :: " + careCityId + "\n " +
                "  paymentType :: " + paymentType + "\n " +
                "  dsaCode :: " + dsaCode + "\n " +
                "  bundlediscountPercent :: " + bundlediscountPercent + "\n " +
                "  bundlediscountPrice :: " + bundlediscountPrice + "\n ";
    }
}
