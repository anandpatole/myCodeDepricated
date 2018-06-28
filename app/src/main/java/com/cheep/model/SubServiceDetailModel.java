package com.cheep.model;

import android.support.annotation.Keep;

import com.cheep.custom_view.expandablerecycleview.Parent;
import com.cheep.strategicpartner.model.SubSubCatModel;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by bhavesh on 28/4/17.
 */
@Keep
public class SubServiceDetailModel implements Parent<SubSubCatModel>, Serializable {
    @SerializedName("cat_id")
    public String catId;

    @SerializedName("sub_cat_id")
    public String sub_cat_id;

    @SerializedName("sub_cat_name")
    public String name;

    @SerializedName("sub_cat_price")
    public String subCatPrice;


    public boolean isSelected = false;
    @SerializedName("all_sub_sub_cats")
    public List<SubSubCatModel> subSubCatModels = null;
    @SerializedName("packageData")
    public List<PackageData> packageData = null;

    @Override
    public List<SubSubCatModel> getChildList() {
        return subSubCatModels;
    }

    @Override
    public boolean isInitiallyExpanded() {
        return false;
    }


    public class PackageData {
        // remaining count out of 4 service
        @SerializedName("pestcontrol_cnt")
        public String pestcontrolCnt;

        @SerializedName("address_id")
        public String address_id;
    }
}
