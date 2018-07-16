package com.cheep.cheepcarenew.model;

import com.cheep.model.PriceModel;
import com.google.gson.annotations.SerializedName;

public class PackageDetail {

    @SerializedName("care_package_id")
    public String id;

    @SerializedName("title")
    public String title;

    @SerializedName("subtitle")
    public String subtitle;

//    @SerializedName("price")
//    public String price;

    @SerializedName("package_slug")
    public String packageSlug;

    @SerializedName("image")
    public String packageImage;

    @SerializedName("type")
    public String type;

    public PriceModel priceModel;

}
