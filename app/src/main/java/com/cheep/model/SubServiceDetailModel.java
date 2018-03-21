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

    @SerializedName("min_unit")
    public String minUnit;

    @SerializedName("max_unit")
    public String maxUnit;

    @SerializedName("unit_price")
    public String unitPrice;

    @SerializedName("unit_price_with_gst")
    public String unitPriceWithGST;

    @SerializedName("charge_type")
    public String chargeType;

    public boolean isSelected = false;

    public int selected_unit = 1;

    @SerializedName("all_sub_sub_cats")
    public List<SubSubCatModel> subSubCatModels = null;

    @Override
    public List<SubSubCatModel> getChildList() {
        return subSubCatModels;
    }

    @Override
    public boolean isInitiallyExpanded() {
        return false;
    }
}
