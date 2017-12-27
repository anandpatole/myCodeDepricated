package com.cheep.cheepcare.model;

import com.cheep.custom_view.expandablerecycleview.Parent;

import java.io.Serializable;
import java.util.List;

/**
 * Created by pankaj on 12/26/17.
 */

public class CheepCarePackageServicesModel implements Parent<CheepCarePackageSubServicesModel>, Serializable {

    public int catId;
    public int sub_cat_id;
    public String name;
    public String description;
    public List<CheepCarePackageSubServicesModel> subServices = null;
    public boolean isSelected = false;

    @Override
    public List<CheepCarePackageSubServicesModel> getChildList() {
        return subServices;
    }

    @Override
    public boolean isInitiallyExpanded() {
        return false;
    }
}