package com.cheep.cheepcarenew.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by majid on 17-07-2018.
 */

public class ManagesSubscriptionModel implements Serializable {

    @SerializedName("user_package_data")
    public UserPackageDataModel userPackageData;

    @SerializedName("package_detail")
    public UserPackageDetailsModel userPackageDetail;

    @SerializedName("city_care_detail")
    public UserCityCareDetail userCityCareDetail;

    @SerializedName("PRIOR_PACKAGE_RENEW_NOTFICATION_DAY")
    public String PRIOR_PACKAGE_RENEW_NOTIFICATION_DAY;

}
