package com.cheep.cheepcare.model;

import com.cheep.custom_view.expandablerecycleview.Parent;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by pankaj on 12/26/17.
 */

public class CheepCarePackageServicesModel implements Parent<PackageOption>, Serializable {

    @SerializedName("package_id")
    @Expose
    public String packageId;

    @SerializedName("package_option_title")
    @Expose
    public String packageOptionTitle;

    @SerializedName("selection_type")
    @Expose
    public String selectionType;

    @SerializedName("package_suboption")
    @Expose
    public List<PackageOption> packageOptionList = null;
    public boolean isSelected = false;

    public interface SELECTION_TYPE {
        String RADIO = "radio";
        String CHECK_BOX = "checkbox";
    }


    @Override
    public List<PackageOption> getChildList() {
        return packageOptionList;
    }

    @Override
    public boolean isInitiallyExpanded() {
        return false;
    }
}