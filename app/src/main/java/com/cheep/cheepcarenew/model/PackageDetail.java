package com.cheep.cheepcarenew.model;

import android.support.annotation.Nullable;

import com.cheep.custom_view.expandablerecycleview.Parent;
import com.cheep.model.AddressModel;
import com.cheep.model.JobCategoryModel;
import com.cheep.network.NetworkUtility;
import com.cheep.utils.Utility;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class PackageDetail implements Parent<JobCategoryModel>, Serializable {

    @SerializedName("care_package_id")
    public String id;

    @SerializedName("title")
    public String title;

    @SerializedName("subtitle")
    public String subtitle;

    @SerializedName("price")
    public String price;

    @SerializedName("package_type")
    public String packageType;

    @SerializedName("package_slug")
    public String packageSlug;

    @SerializedName("image")
    public String packageImage;

    @SerializedName("type")
    public String type;


    @SerializedName("live_lable_arr")
    public List<String> live_lable_arr;

    private final static long serialVersionUID = -4061560571461215516L;
    @Nullable
    public ArrayList<PackageOption> packageOptionList;
    public boolean isSelected = false;

    @Nullable
    @SerializedName("address")
    public List<AddressModel> mSelectedAddressList;

    @Nullable
    @SerializedName("categoryBannerData")
    public List<JobCategoryModel> categoryList;
    public int rowType = 0;

    public double monthlyPrice = 0;
    public double yearlyPrice = 0;
    public double halfYearlyPrice = 0;


    @Override
    public List<JobCategoryModel> getChildList() {
        return categoryList;
    }

    @Override
    public boolean isInitiallyExpanded() {
        return !packageSlug.equalsIgnoreCase(NetworkUtility.CARE_PACKAGE_SLUG.APPLIANCE_CARE) && !packageSlug.equalsIgnoreCase(NetworkUtility.CARE_PACKAGE_SLUG.TECH_CARE);
    }

    public String getDaysLeft(String stringDate) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Utility.DATE_FORMAT_YYYY_MM_DD, Locale.US);
        SimpleDateFormat dateFormat = new SimpleDateFormat(Utility.DATE_FORMAT_DD_MM_YY, Locale.US);
        String todayString = dateFormat.format(Calendar.getInstance().getTime());
        Date todayDate = null;
        Date date = null;
        try {
            date = simpleDateFormat.parse(stringDate);
            todayDate = dateFormat.parse(todayString);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (date != null && todayDate != null) {
            long currentTimeMillis = /*Calendar.getInstance().getTimeInMillis()*/todayDate.getTime();
            long expirationTimeMillis = date.getTime();
            System.out.println("Days: " + TimeUnit.DAYS.convert((expirationTimeMillis - currentTimeMillis), TimeUnit.MILLISECONDS));
            return String.valueOf((expirationTimeMillis - currentTimeMillis) / (1000 * 60 * 60 * 24));
        }
        return Utility.EMPTY_STRING;
    }


}