package com.cheep.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.cheepcare.model.AdminSettingModel;
import com.cheep.cheepcare.model.CityDetail;
import com.cheep.cheepcare.model.PackageDetail;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.cheep.network.NetworkUtility.TAGS.CARE_CITY_SLUG;

/**
 * Created by bhavesh on 24/1/18.
 */

public class WebCallClass {
    private static final String TAG = WebCallClass.class.getSimpleName();

    //////////////////////////generic interface for common responses starts//////////////////////////
    public interface CommonResponseListener {
        void volleyError();

        void showSpecificMessage(String message);

        void forceLogout();
    }
    //////////////////////////generic interface for common responses ends//////////////////////////

    //////////////////////////Get Subscribed Care Package call start//////////////////////////
    public interface GetSubscribedCarePackageResponseListener {

        void getSubscribedCarePackageSuccessResponse(CityDetail cityDetail
                , List<PackageDetail> subscribedList
                , List<PackageDetail> allPackageList, AdminSettingModel adminSettingModel);
    }

    public static void getSubscribedCarePackage(final Context mContext, final String careCitySlug
            , final CommonResponseListener commonListener
            , final GetSubscribedCarePackageResponseListener successListener) {

        final Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");
                commonListener.volleyError();
            }
        };

        final Response.Listener responseListener = new Response.Listener() {
            @Override
            public void onResponse(Object response) {
                Log.d(TAG, "onResponse() called with: response = [" + response + "]");
                try {
                    JSONObject jsonObject = new JSONObject(response.toString());
                    int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);

                    String error_message;
                    switch (statusCode) {
                        case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
                            if (!TextUtils.isEmpty(jsonObject.getString(NetworkUtility.TAGS.DATA))) {

                                JSONObject jsonData = jsonObject.getJSONObject(NetworkUtility.TAGS.DATA);

                                CityDetail cityDetail = (CityDetail) Utility.getObjectFromJsonString(
                                        jsonData.getString(NetworkUtility.TAGS.CITY_DETAIL)
                                        , CityDetail.class);

                                List<PackageDetail> subscribedList = Utility.getObjectListFromJsonString(
                                        jsonData.getString(NetworkUtility.TAGS.USER_PACKAGE_DETAIL)
                                        , PackageDetail[].class);

                                List<PackageDetail> allPackageList = Utility.getObjectListFromJsonString(
                                        jsonData.getString(NetworkUtility.TAGS.PACKAGE_DETAIL)
                                        , PackageDetail[].class);

                                AdminSettingModel adminSettingModel = (AdminSettingModel) Utility.getObjectFromJsonString(
                                        jsonData.getString(NetworkUtility.TAGS.ADMIN_SETTING)
                                        , AdminSettingModel.class);

                                successListener.getSubscribedCarePackageSuccessResponse(
                                        cityDetail
                                        , subscribedList
                                        , allPackageList,
                                        adminSettingModel);
                                ;
                            }
                            break;
                        case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                            // Show Toast
                            commonListener.volleyError();
                            break;
                        case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                            error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                            // Show message
                            commonListener.showSpecificMessage(error_message);
                            break;
                        case NetworkUtility.TAGS.STATUSCODETYPE.USER_DELETED:
                        case NetworkUtility.TAGS.STATUSCODETYPE.FORCE_LOGOUT_REQUIRED:
                            //Logout and finish the current activity
                            Utility.logout(mContext, true, statusCode);
                            commonListener.forceLogout();
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().userID);

        //Add Params
        Map<String, String> mParams = new HashMap<>();
        mParams.put(CARE_CITY_SLUG, careCitySlug);

        //noinspection unchecked
        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.GET_USER_SUBSCRIBED_CARE_PACKAGE
                , errorListener
                , responseListener
                , mHeaderParams
                , mParams
                , null);

        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest, NetworkUtility.WS.GET_USER_SUBSCRIBED_CARE_PACKAGE);
    }
    //////////////////////////Get Subscribed User Care Package call end//////////////////////////

}
