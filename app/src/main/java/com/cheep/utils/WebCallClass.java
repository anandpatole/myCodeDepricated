package com.cheep.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.cheepcare.model.CityDetail;
import com.cheep.cheepcare.model.PackageDetail;
import com.cheep.network.NetworkUtility;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by bhavesh on 24/1/18.
 */

public class WebCallClass {
    private static final String TAG = WebCallClass.class.getSimpleName();

    //////////////////////////generic interface for common responses starts//////////////////////////
    public interface CommonResponseListener {
        void volleyError();

        void showSpecificMessage(String message);
    }
    //////////////////////////generic interface for common responses ends//////////////////////////

    //////////////////////////Get Subscribed Care Package call start//////////////////////////
    public interface GetSubscribedCarePackageResponseListener {

        void getSubscribedCarePackageSuccessResponse(CityDetail cityDetail
                , ArrayList<PackageDetail> subscribedlist
                , ArrayList<PackageDetail> allPackagelist);
    }

    public static void getSubscribedCarePackage(Context context, String careCitySlug
            , final CommonResponseListener commonListener
            , final GetSubscribedCarePackageResponseListener successListener) {

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");
                commonListener.volleyError();
            }
        };

        Response.Listener responseListener = new Response.Listener() {
            @Override
            public void onResponse(Object response) {
                Log.d(TAG, "onResponse() called with: response = [" + response + "]");
                try {
                    JSONObject jsonObject = new JSONObject(response.toString());
                    if (!TextUtils.isEmpty(jsonObject.getString(NetworkUtility.TAGS.DATA))) {
                        successListener.getSubscribedCarePackageSuccessResponse(jsonObject.getJSONObject(NetworkUtility.TAGS.DATA)
                                .getJSONObject(NetworkUtility.TAGS.SUB_DATA).getString(NetworkUtility.TAGS.DESCRIPTION));
                    } else {
                        commonListener.volleyError();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String a = PreferenceUtility.getInstance(context).getUserDetails().userID;
            }
        };
    }
    //////////////////////////Get Subscribed User Care Package call end//////////////////////////

}
