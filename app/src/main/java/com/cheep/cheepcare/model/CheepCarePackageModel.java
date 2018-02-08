package com.cheep.cheepcare.model;

import com.cheep.cheepcare.adapter.PackageBundlingAdapter;
import com.cheep.custom_view.expandablerecycleview.Parent;
import com.cheep.model.JobCategoryModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pankaj on 12/21/17.
 */

public class CheepCarePackageModel implements Parent<JobCategoryModel>, Serializable {

    public String packageImage;
    public String packageTitle;
    public String packageDescription;
    public String price;
    public String subscribedDescription;
    public String daysLeft;
    public String catId;
    public boolean isSelected = false;

    public List<String> live_lable_arr;

    public List<JobCategoryModel> subItems = new ArrayList<>();
    public int rowType = PackageBundlingAdapter.ROW_PACKAGE_NOT_SELECTED;

    @Override
    public List<JobCategoryModel> getChildList() {
        return subItems;
    }

    @Override
    public boolean isInitiallyExpanded() {
        return true;
    }



}