package com.cheep.cheepcare.model;

import android.support.annotation.Nullable;

import com.cheep.model.AddressModel;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PackageDetail implements Serializable {

    @SerializedName("care_package_id")
    public String id;
    @SerializedName("title")
    public String title;
    @SerializedName("subtitle")
    public String subtitle;

    @SerializedName("price")
    public String price;


    @SerializedName("package_slug")
    public String packageSlug;

    @SerializedName("package_image")
    public String packageImage;


    @SerializedName("live_lable_arr")
    public List<String> live_lable_arr;

    private final static long serialVersionUID = -4061560571461215516L;
    @Nullable
    public ArrayList<PackageOption> packageOptionList;
    public boolean isSelected = false;
    @Nullable
    public AddressModel mSelectedAddress;
    public int rowType = 0;

    public double monthlyPrice = 0;
    public double yearlyPrice = 0;
    public double halfYearlyPrice = 0;
}
