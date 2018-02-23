package com.cheep.cheepcare.model;

import com.cheep.custom_view.expandablerecycleview.Parent;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by pankaj on 12/26/17.
 */

public class PackageOption implements Parent<PackageSubOption>, Serializable {

    @SerializedName("package_option_id")
    public String packageId;

    @SerializedName("package_option_title")
    public String packageOptionTitle;

    @SerializedName("selection_type")
    public String selectionType;

    @SerializedName("package_suboption")
    public List<PackageSubOption> packageOptionList = null;
    public boolean isSelected = false;

    public interface SELECTION_TYPE {
        String RADIO = "radio";
        String CHECK_BOX = "checkbox";
    }


    @Override
    public List<PackageSubOption> getChildList() {
        return packageOptionList;
    }

    @Override
    public boolean isInitiallyExpanded() {
        return false;
    }
}