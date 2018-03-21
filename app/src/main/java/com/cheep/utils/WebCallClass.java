package com.cheep.utils;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.R;
import com.cheep.cheepcare.model.AdminSettingModel;
import com.cheep.cheepcare.model.CityDetail;
import com.cheep.cheepcare.model.CityLandingPageModel;
import com.cheep.cheepcare.model.PackageDetail;
import com.cheep.cheepcare.model.SubscribedTaskDetailModel;
import com.cheep.model.AddressModel;
import com.cheep.model.JobCategoryModel;
import com.cheep.model.NotificationModel;
import com.cheep.model.SubServiceDetailModel;
import com.cheep.model.UserDetails;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.cheep.network.NetworkUtility.TAGS.ADDRESS_ID;
import static com.cheep.network.NetworkUtility.TAGS.CARE_CITY_SLUG;
import static com.cheep.network.NetworkUtility.TAGS.CARE_PACKAGE_ID;
import static com.cheep.network.NetworkUtility.TAGS.DATA;
import static com.cheep.network.NetworkUtility.TAGS.FINAL_EXTRA_CHARGE;

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

                                CityDetail cityDetail = (CityDetail) GsonUtility.getObjectFromJsonString(
                                        jsonData.getString(NetworkUtility.TAGS.CITY_DETAIL)
                                        , CityDetail.class);

                                List<PackageDetail> subscribedList = GsonUtility.getObjectListFromJsonString(
                                        jsonData.getString(NetworkUtility.TAGS.USER_PACKAGE_DETAIL)
                                        , PackageDetail[].class);

                                List<PackageDetail> allPackageList = GsonUtility.getObjectListFromJsonString(
                                        jsonData.getString(NetworkUtility.TAGS.PACKAGE_DETAIL)
                                        , PackageDetail[].class);

                                AdminSettingModel adminSettingModel = (AdminSettingModel) GsonUtility.getObjectFromJsonString(
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
                    commonListener.showSpecificMessage(mContext.getString(R.string.label_something_went_wrong));
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
        mParams.put(NetworkUtility.TAGS.CARE_CITY_SLUG, careCitySlug);

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
                            ArrayList<SubServiceDetailModel> freeCatList = GsonUtility.getObjectListFromJsonString(object.getString(NetworkUtility.TAGS.FREE_SERVICE), SubServiceDetailModel[].class);
                            ArrayList<SubServiceDetailModel> paidCatList = GsonUtility.getObjectListFromJsonString(object.getString(NetworkUtility.TAGS.PAID_SERVICE), SubServiceDetailModel[].class);
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
        mParams.put(NetworkUtility.TAGS.CAT_ID, mJobCategoryModel.catId);
        mParams.put(NetworkUtility.TAGS.PACKAGE_TYPE, mPackageType);
        mParams.put(NetworkUtility.TAGS.ADDRESS_ID, mAddressModel.address_id);

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

    public interface SuccessOfTaskCreationResponseListener {
        void onSuccessOfTaskCreate();
    }

    public static void createTask(final Context mContext, SubscribedTaskDetailModel subscribedTaskDetailModel,
                                  final CommonResponseListener commonListener,
                                  final SuccessOfTaskCreationResponseListener successListener) {

        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        if (PreferenceUtility.getInstance(mContext).getUserDetails() != null) {
            mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().userID);
        }

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
                            successListener.onSuccessOfTaskCreate();
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
                    commonListener.showSpecificMessage(mContext.getString(R.string.label_something_went_wrong));
                    e.printStackTrace();
                }
            }
        };
        //Add Params
        Map<String, String> mParams = new HashMap<>();

        //0
        mParams.put(NetworkUtility.TAGS.CARE_PACKAGE_ID, subscribedTaskDetailModel.taskType.equalsIgnoreCase(Utility.TASK_TYPE.NORMAL) ? Utility.ZERO_STRING : subscribedTaskDetailModel.carePackageId);

        mParams.put(NetworkUtility.TAGS.CAT_ID, subscribedTaskDetailModel.jobCategoryModel.catId);
        String freeServicejson = new Gson().toJson(subscribedTaskDetailModel.freeServiceList);

        mParams.put(NetworkUtility.TAGS.FREE_SERVICE, freeServicejson);

        String paidServicejson = new Gson().toJson(subscribedTaskDetailModel.paidServiceList);

        mParams.put(NetworkUtility.TAGS.PAID_SERVICE, paidServicejson);

        mParams.put(NetworkUtility.TAGS.ADDRESS_ID, subscribedTaskDetailModel.addressModel.address_id);

        mParams.put(NetworkUtility.TAGS.TOTAL_AMOUNT, String.valueOf(subscribedTaskDetailModel.total));

        mParams.put(NetworkUtility.TAGS.PAYABLE_AMOUNT, subscribedTaskDetailModel.paybleAmount);

        mParams.put(NetworkUtility.TAGS.START_DATETIME, subscribedTaskDetailModel.startDateTime);

        mParams.put(NetworkUtility.TAGS.IS_REFER_CODE, Utility.BOOLEAN.NO);

        mParams.put(NetworkUtility.TAGS.TASK_TYPE, subscribedTaskDetailModel.taskType);

//        mParams.put(NetworkUtility.TAGS.TASK_DESC, subscribedTaskDetailModel.taskDesc);

        mParams.put(NetworkUtility.TAGS.MEDIA_FILE, Utility.getSelectedMediaJsonString(subscribedTaskDetailModel.mediaFileList));

        mParams.put(NetworkUtility.TAGS.PROMOCODE_PRICE, Utility.ZERO_STRING);


        mParams.put(NetworkUtility.TAGS.USED_WALLET_BALANCE, Utility.ZERO_STRING);

        mParams.put(NetworkUtility.TAGS.CHEEPCODE, Utility.EMPTY_STRING);

        // PENDING
        mParams.put(NetworkUtility.TAGS.PAYMENT_METHOD, subscribedTaskDetailModel.taskType.equalsIgnoreCase(Utility.TASK_TYPE.NORMAL) ? Utility.TASK_STATUS.PENDING : subscribedTaskDetailModel.paymentMethod);


        mParams.put(NetworkUtility.TAGS.PAYMENT_LOG, subscribedTaskDetailModel.paymentLog);

        mParams.put(NetworkUtility.TAGS.CHARGE_SPECIFIC_TIME, String.valueOf(subscribedTaskDetailModel.nonWorkingHourFees));

        mParams.put(NetworkUtility.TAGS.CHARGE_EXCEED_LIMIT, String.valueOf(subscribedTaskDetailModel.taskExcessLimitFees));

        Log.d(TAG, "VolleyNetworkRequest() called with: url = [" + "url" + "], errorListener = [" + "errorListener" + "]" +
                ", listener = [" + "listener" + "], headers = [" + mHeaderParams + "], stringData = [" + mParams + "], fileParam = [" + null + "]");

        //noinspection unchecked
        VolleyNetworkRequest mVolleyNetworkRequestForCategoryList = new VolleyNetworkRequest(NetworkUtility.WS.CARE_CREATE_TASK
                , errorListener
                , responseListener
                , mHeaderParams
                , mParams
                , null);

        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForCategoryList, NetworkUtility.WS.CARE_CREATE_TASK);
    }
    //////////////////////////Create task Cheep care call end//////////////////////////


    ////////////////////////// Get Profile call start     //////////////////////////
    public interface GetProfileDetailResponseListener {

        void getUserDetails(UserDetails userDetails, JSONArray jsonEmergencyContacts, ArrayList<AddressModel> addressList);
    }

    public static void getProfileDetail(final Context mContext, final CommonResponseListener commonListener
            , final GetProfileDetailResponseListener successListener) {

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

                                UserDetails userDetails = (UserDetails) GsonUtility.getObjectFromJsonString(jsonData.toString(), UserDetails.class);
                                PreferenceUtility.getInstance(mContext).saveUserDetails(jsonData);

                                JSONArray jsonEmergencyContacts = jsonData.optJSONArray(NetworkUtility.TAGS.EMERGENCY_DATA);
                                ArrayList<AddressModel> addressList = GsonUtility.getObjectListFromJsonString(jsonData.optJSONArray(NetworkUtility.TAGS.ADDRESS).toString(), AddressModel[].class);

                                successListener.getUserDetails(userDetails, jsonEmergencyContacts, addressList);

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
                    commonListener.showSpecificMessage(mContext.getString(R.string.label_something_went_wrong));
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

        //noinspection unchecked
        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.PROFILE
                , errorListener
                , responseListener
                , mHeaderParams
                , mParams
                , null);

        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest, NetworkUtility.WS.PROFILE);
    }
    ////////////////////////// Get Profile call end     //////////////////////////

    ////////////////////////// Get city available for cheep care call start     //////////////////////////
    public interface CityAvailableCheepCareListener {

        void getCityDetails(CityDetail cityDetail);
    }

    public static void isCityAvailableForCare(final Context mContext, String addressId, final CommonResponseListener commonListener
            , final CityAvailableCheepCareListener successListener) {

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
                                JSONObject dataObj = jsonObject.optJSONObject(NetworkUtility.TAGS.DATA);
                                CityDetail cityDetail = new CityDetail();
                                cityDetail.cityName = dataObj.optString(NetworkUtility.TAGS.CARE_CITY_NAME);
                                cityDetail.id = dataObj.optString(NetworkUtility.TAGS.CARE_CITY_ID);
                                cityDetail.citySlug = dataObj.optString(NetworkUtility.TAGS.CARE_CITY_SLUG);
                                successListener.getCityDetails(cityDetail);
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
                    commonListener.showSpecificMessage(mContext.getString(R.string.label_something_went_wrong));
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
        mParams.put(NetworkUtility.TAGS.ADDRESS_ID, addressId);

        //noinspection unchecked
        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.IS_CITY_AVAILABLE_FOR_CARE
                , errorListener
                , responseListener
                , mHeaderParams
                , mParams
                , null);

        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest, NetworkUtility.WS.IS_CITY_AVAILABLE_FOR_CARE);
    }
    ////////////////////////// Get city available for cheep care call  end     //////////////////////////


    ////////////////////////// Get city care Data call start     //////////////////////////
    public interface GetCityCareDataListener {

        void getCityCareData(CityLandingPageModel cityLandingPageModel);
    }

    public static void getCityCareDetail(final Context mContext, String citySlug, final CommonResponseListener commonListener
            , final GetCityCareDataListener successListener) {

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
                            CityLandingPageModel mCityLandingPageModel = (CityLandingPageModel) GsonUtility.getObjectFromJsonString(jsonObject.optString(DATA), CityLandingPageModel.class);
                            successListener.getCityCareData(mCityLandingPageModel);
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
                    commonListener.showSpecificMessage(mContext.getString(R.string.label_something_went_wrong));
                    e.printStackTrace();
                }
            }
        };

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());

        //Add Params
        Map<String, String> mParams = new HashMap<>();
        mParams.put(CARE_CITY_SLUG, citySlug);


        //noinspection unchecked
        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.GET_CITY_CARE_DETAIL
                , errorListener
                , responseListener
                , mHeaderParams
                , mParams
                , null);

        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest, NetworkUtility.WS.GET_CITY_CARE_DETAIL);
    }
    ////////////////////////// Get city care Data call end     //////////////////////////

    ////////////////////////// Get notification list call start     //////////////////////////
    public interface GetNotificationListListener {

        void getNotificationList(ArrayList<NotificationModel> list, String pageNumber);
    }

    public static void getNotificationList(final Context mContext, final String nextPageId
            , final CommonResponseListener commonListener
            , final GetNotificationListListener successListener) {

        Log.d(TAG, "getNotificationList() called with: mContext = [" + mContext + "], nextPageId = [" + nextPageId +
                "], commonListener = [" + commonListener + "], successListener = [" + successListener + "]");

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
                            ArrayList<NotificationModel> list;
                            try {
                                list = GsonUtility.getObjectListFromJsonString(jsonObject.optString(NetworkUtility.TAGS.DATA), NotificationModel[].class);
                            } catch (Exception e) {
                                Log.i(TAG, "onResponse: Error" + e.toString());
                                list = new ArrayList<>();
                            }

                            successListener.getNotificationList(list, jsonObject.optString(NetworkUtility.TAGS.PAGE_NUM));

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
                    commonListener.showSpecificMessage(mContext.getString(R.string.label_something_went_wrong));
                    e.printStackTrace();
                }
            }
        };

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        if (PreferenceUtility.getInstance(mContext).getUserDetails() != null) {
            mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().userID);
        }

        //Add Params
        Map<String, String> mParams = new HashMap<>();
        if (!TextUtils.isEmpty(nextPageId)) {
            mParams.put(NetworkUtility.TAGS.PAGE_NUM, nextPageId);
        }

        //noinspection unchecked
        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.NOTIFICATION_LIST
                , errorListener
                , responseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest, NetworkUtility.WS.NOTIFICATION_LIST);
    }
    ////////////////////////// Get notification list call end     //////////////////////////


    ////////////////////////// GgetExtraChargeAfterExceedLimit call start     //////////////////////////
    public interface GetExtraChargeAfterExceedLimitListener {

        void getFinalExtraCharge(String finalExtraCharge);
    }

    public static void getExtraChargeAfterExceedLimit(final Context mContext, String care_package_id, String address_id, final CommonResponseListener commonListener
            , final GetExtraChargeAfterExceedLimitListener successListener) {

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
                            String mCityLandingPageModel = jsonObject.optJSONObject(DATA).optString(FINAL_EXTRA_CHARGE);
                            successListener.getFinalExtraCharge(mCityLandingPageModel);
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
                    commonListener.showSpecificMessage(mContext.getString(R.string.label_something_went_wrong));
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
        mParams.put(CARE_PACKAGE_ID, care_package_id);
        mParams.put(ADDRESS_ID, address_id);


        //noinspection unchecked
        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.GET_EXTRA_CHARGE_AFTER_EXCEED_LIMIT
                , errorListener
                , responseListener
                , mHeaderParams
                , mParams
                , null);

        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest, NetworkUtility.WS.GET_EXTRA_CHARGE_AFTER_EXCEED_LIMIT);
    }
    ////////////////////////// getExtraChargeAfterExceedLimit call end     //////////////////////////

    ////////////////////////// getAdminSettings call start    //////////////////////////
    public static void getAdminSettings(final Context mContext, final CommonResponseListener commonListener) {

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
                            AdminSettingModel adminSettingModel = (AdminSettingModel) GsonUtility.getObjectFromJsonString(jsonObject.getString(DATA)
                                    , AdminSettingModel.class);
                            PreferenceUtility.getInstance(mContext).setAdminSettings(adminSettingModel);
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
                    commonListener.showSpecificMessage(mContext.getString(R.string.label_something_went_wrong));
                    e.printStackTrace();
                }
            }
        };

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        if (PreferenceUtility.getInstance(mContext).getUserDetails() != null)
            mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().userID);

        //noinspection unchecked
        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(
                Request.Method.GET
                , NetworkUtility.WS.GET_ADMIN_SETTINGS
                , errorListener
                , responseListener
                , mHeaderParams
                , null
                , null);

        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest, NetworkUtility.WS.GET_ADMIN_SETTINGS);
    }

    ////////////////////////// getAdminSettings call end     //////////////////////////
}