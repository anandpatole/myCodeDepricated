package com.cheep.strategicpartner;

import com.cheep.custom_view.expandablerecycleview.Parent;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by giteeka on 21/7/17.
 */

public class StrategicPartnerSubCategoryModel implements Parent<StrategicPartnerSubCategoryModel.AllSubSubCat> {


    @SerializedName("cat_id")
    public int catId;

    @SerializedName("sub_cat_id")
    public int sub_cat_id;

    @SerializedName("name")
    public String name;

    @SerializedName("all_sub_sub_cats")
    public List<AllSubSubCat> allSubSubCats = null;

    public boolean isSelected = false;

    @Override
    public List<AllSubSubCat> getChildList() {
        return allSubSubCats;
    }

    @Override
    public boolean isInitiallyExpanded() {
        return false;
    }

    public class AllSubSubCat {

        @SerializedName("sub_sub_cat_name")
        public String subSubCatName;

        @SerializedName("price")
        public String price;

        @SerializedName("sub_sub_cat_id")
        public String subSubCatId;

        public boolean isSelected = false;

    }
}
