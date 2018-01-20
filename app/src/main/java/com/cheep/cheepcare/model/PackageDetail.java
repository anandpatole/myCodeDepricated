package com.cheep.cheepcare.model;

import android.support.annotation.Nullable;

import com.cheep.model.AddressModel;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class PackageDetail implements Serializable {

    @SerializedName("id")
    @Expose
    public String id;
    @SerializedName("title")
    @Expose
    public String title;
    @SerializedName("subtitle")
    @Expose
    public String subtitle;

    @SerializedName("price")
    @Expose
    public String price;


    @SerializedName("package_slug")
    @Expose
    public String packageSlug;
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
