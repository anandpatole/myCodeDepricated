package com.cheep.strategicpartner;

import android.support.annotation.Keep;

import com.cheep.custom_view.expandablerecycleview.Parent;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by giteeka on 21/7/17.
 * Strategic partner services Json class model
 */

@Keep
public class StrategicPartnerServiceModel implements Parent<AllSubSubCat> {


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

}
