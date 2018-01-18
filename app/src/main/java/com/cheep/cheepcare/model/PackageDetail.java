package com.cheep.cheepcare.model;

import android.support.annotation.Nullable;

import com.cheep.model.AddressModel;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class PackageDetail implements Serializable {

    @SerializedName("id")
    public String id;
    @SerializedName("title")
    public String title;
    @SerializedName("subtitle")
    public String subtitle;

    @SerializedName("price")
    public String price;


    @SerializedName("package_slug")
    public String packageSlug;
    private final static long serialVersionUID = -4061560571461215516L;
    @Nullable
    public ArrayList<PackageOption> packageOptionList;
    public boolean isSelected = false;
    @Nullable
    public AddressModel mSelectedAddress;
    public int rowType = 0;

    public double calculatedPackagePrice = 0;
}
