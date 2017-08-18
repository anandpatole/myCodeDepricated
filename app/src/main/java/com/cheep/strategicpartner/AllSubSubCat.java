package com.cheep.strategicpartner;

import android.support.annotation.Keep;

import com.google.gson.annotations.SerializedName;

@Keep
public class AllSubSubCat {

    @SerializedName("sub_sub_cat_name")
    public String subSubCatName;

    @SerializedName("price")
    public String price;

    @SerializedName("sub_sub_cat_id")
    public String subSubCatId;

    @SerializedName("package_description")
    public String package_description;

    @SerializedName("sub_category_name")
    public String subCategoryName;

    public boolean isSelected = false;

}
