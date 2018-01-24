package com.cheep.utils;

import android.content.Context;

import com.cheep.cheepcare.model.CityDetail;
import com.cheep.cheepcare.model.PackageDetail;

import java.util.ArrayList;

/**
 * Created by bhavesh on 24/1/18.
 */

public class WebCallClass {

    //////////////////////////generic interface for common responses starts//////////////////////////
    public interface CommonResponseListener {
        void volleyError();

        void showSpecificMessage(String messZage);
    }
    //////////////////////////generic interface for common responses ends//////////////////////////

    //////////////////////////Get Subscribed Care Package call start//////////////////////////
    public interface getSubscribedCarePackageResponseListener{
        void getSubscribedCarePackageSuccessResponse(CityDetail cityDetail
                , ArrayList<PackageDetail> subscribedlist
                , ArrayList<PackageDetail> allPackagelist);
    }

    public static void getSubscribedCarePackage(Context context, String careCitySlug) {
        String a = PreferenceUtility.getInstance(context).getUserDetails().userID;
    }
    //////////////////////////Get Subscribed User Care Package call end//////////////////////////
}
