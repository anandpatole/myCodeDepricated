package com.cheep.utils;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.R;
import com.cheep.cheepcare.model.AdminSettingModel;
import com.cheep.cheepcare.model.CityDetail;
import com.cheep.cheepcare.model.PackageDetail;
import com.cheep.model.AddressModel;
import com.cheep.model.JobCategoryModel;
import com.cheep.model.SubServiceDetailModel;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.cheep.network.NetworkUtility.TAGS.ADDRESS_ID;
import static com.cheep.network.NetworkUtility.TAGS.CARE_CITY_SLUG;
import static com.cheep.network.NetworkUtility.TAGS.CARE_PACKAGE_ID;
import static com.cheep.network.NetworkUtility.TAGS.CAT_ID;
import static com.cheep.network.NetworkUtility.TAGS.FREE_SERVICE;
import static com.cheep.network.NetworkUtility.TAGS.PACKAGE_TYPE;
import static com.cheep.network.NetworkUtility.TAGS.PAID_SERVICE;
import static com.cheep.network.NetworkUtility.TAGS.PAYABLE_AMOUNT;
import static com.cheep.network.NetworkUtility.TAGS.START_DATETIME;
import static com.cheep.network.NetworkUtility.TAGS.TOTAL_PRICE;

/**
 * Created by bhavesh on 24/1/18.
 */

public class WebCallClass {
    private static final String TAG = WebCallClass.class.getSimpleName();

    //////////////////////////generic interface for common responses starts//////////////////////////
    public interface CommonResponseListener {
        void volleyError(VolleyError error);

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
                commonListener.volleyError(error);
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
                            commonListener.showSpecificMessage(mContext.getString(R.string.label_something_went_wrong));
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

    //////////////////////////Fetch List Of Sub Category call start//////////////////////////
    public interface FetchListOfSubCategoryResponseListener {
        void fetchListOfSubCategorySuccessResponse(ArrayList<SubServiceDetailModel> paidList
                , ArrayList<SubServiceDetailModel> freeList);
    }

    public static void fetchListOfSubCategory(final Context mContext, JobCategoryModel mJobCategoryModel
            , String mPackageType, AddressModel mAddressModel, final CommonResponseListener commonListener
            , final FetchListOfSubCategoryResponseListener successListener) {

        final Response.ErrorListener mCallFetchSubServiceListingWSErrorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");
                commonListener.volleyError(error);
            }
        };


        Response.Listener mCallFetchSubServiceListingWSResponseListener = new Response.Listener() {
            @Override
            public void onResponse(Object response) {
                Log.d(TAG, "onResponse() called with: response = [" + response + "]");
                String strResponse = (String) response;
                try {
                    JSONObject jsonObject = new JSONObject(strResponse);
                    Log.i(TAG, "onResponse: " + jsonObject.toString());
                    int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                    String error_message;
                    switch (statusCode) {
                        case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:

                            JSONObject object = jsonObject.optJSONObject(NetworkUtility.TAGS.DATA);
                            ArrayList<SubServiceDetailModel> freeCatList = Utility.getObjectListFromJsonString(object.getString(FREE_SERVICE), SubServiceDetailModel[].class);
                            ArrayList<SubServiceDetailModel> paidCatList = Utility.getObjectListFromJsonString(object.getString(PAID_SERVICE), SubServiceDetailModel[].class);
                            successListener.fetchListOfSubCategorySuccessResponse(freeCatList, paidCatList);
                            break;
                        case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                            commonListener.showSpecificMessage(mContext.getString(R.string.label_something_went_wrong));
                            break;
                        case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                            error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                            commonListener.showSpecificMessage(error_message);
                            break;
                        case NetworkUtility.TAGS.STATUSCODETYPE.USER_DELETED:
                        case NetworkUtility.TAGS.STATUSCODETYPE.FORCE_LOGOUT_REQUIRED:
                            //Logout and finish the current activity
                            Utility.logout(mContext, true, statusCode);
                            if (mContext != null)
                                ((FragmentActivity) mContext).finish();
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    mCallFetchSubServiceListingWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
                }

            }
        };


        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        if (PreferenceUtility.getInstance(mContext).getUserDetails() != null) {
            mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().userID);
        }

        //Add Params
        Map<String, String> mParams = new HashMap<>();
        mParams.put(CAT_ID, mJobCategoryModel.catId);
        mParams.put(PACKAGE_TYPE, mPackageType);
        mParams.put(ADDRESS_ID, mAddressModel.address_id);

        VolleyNetworkRequest mVolleyNetworkRequestForCategoryList = new VolleyNetworkRequest(NetworkUtility.WS.GET_CARE_FREE_PAID_SERVICES_FOR_CATEGORY
                , mCallFetchSubServiceListingWSErrorListener
                , mCallFetchSubServiceListingWSResponseListener
                , mHeaderParams
                , mParams
                , null);

        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForCategoryList, NetworkUtility.WS.GET_CARE_FREE_PAID_SERVICES_FOR_CATEGORY);
    }
    //////////////////////////Fetch List Of Sub Category call end//////////////////////////

    //////////////////////////Create task Cheep care call start//////////////////////////
    public static void createTask(Context mContext, String carePackageId, String catId
            , ArrayList<SubServiceDetailModel> freeList, ArrayList<SubServiceDetailModel> paidList, AddressModel mAddressModel
            , String totalPrice, String payableAmount, String startDateTime) {

        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        if (PreferenceUtility.getInstance(mContext).getUserDetails() != null) {
            mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().userID);
        }

        //Add Params
        Map<String, String> mParams = new HashMap<>();
        mParams.put(CARE_PACKAGE_ID, carePackageId);
        mParams.put(CAT_ID, catId);
        mParams.put(FREE_SERVICE, new Gson().toJson(freeList));
        mParams.put(PAID_SERVICE, new Gson().toJson(paidList));
        mParams.put(ADDRESS_ID, mAddressModel.address_id);
        mParams.put(TOTAL_PRICE, totalPrice);
        mParams.put(PAYABLE_AMOUNT, payableAmount);
        mParams.put(START_DATETIME, startDateTime);

        HashMap<String, File> mFileParams = new HashMap<>();

        Log.d(TAG, "VolleyNetworkRequest() called with: url = [" + "url" + "], errorListener = [" + "errorListener" + "]" +
                ", listener = [" + "listener" + "], headers = [" + mHeaderParams + "], stringData = [" + mParams + "], fileParam = [" + mFileParams + "]");
    }
    //////////////////////////Create task Cheep care call end//////////////////////////

}