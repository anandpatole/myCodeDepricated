package com.cheep.cheepcare.model;

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
    @SerializedName("package_slug")
    @Expose
    public String packageSlug;
    private final static long serialVersionUID = -4061560571461215516L;
    public ArrayList<CheepCarePackageServicesModel> packageOptionList;
    public boolean isSelected = false;
    public AddressModel mSelectedAddress;
    public int rowType = 0;
}
