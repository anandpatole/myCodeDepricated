package com.cheep.strategicpartner;

import android.support.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by giteeka on 17/8/17.
 */
@Keep
public class ServiceTaskDetailModel {

    @SerializedName("sub_category_name")
    public String subCategoryName;
    @SerializedName("price")
    public String price;
    @SerializedName("sub_sub_cat_name")
    public String subSubCatName;
    @SerializedName("sub_cat_id")
    public String subCatId;
    @SerializedName("name")
    public String name;
    @SerializedName("all_sub_sub_cats")
    public List<SubSubCat> allSubSubCats = null;

    @Keep
    public class SubSubCat {

        @SerializedName("price")
        public String price;
        @SerializedName("sub_sub_cat_name")
        public String subSubCatName;

    }
}
