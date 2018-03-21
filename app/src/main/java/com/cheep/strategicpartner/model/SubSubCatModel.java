package com.cheep.strategicpartner.model;

import android.support.annotation.Keep;

import com.google.gson.annotations.SerializedName;


/* this model class is sub sub service under vlcc/hi care sub services
 Hi care / VLCC -> category
 cockroch control / waxing -> sub category
 1 bhk/ 2bhk / tip to toe / montly waxing -> sub sub category
*/

@Keep
public class SubSubCatModel {

    @SerializedName("sub_sub_cat_name")
    public String subSubCatName;

    @SerializedName("price")
    public String price;

    @SerializedName("base_price")
    public String basePrice;

    @SerializedName("sub_sub_cat_id")
    public String subSubCatId;

    @SerializedName("package_description")
    public String package_description;

    @SerializedName("sub_category_name")
    public String subCategoryName;

    public boolean isSelected = false;

}
