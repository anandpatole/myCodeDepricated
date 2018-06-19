package com.cheep.utils;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.appsflyer.AppsFlyerLib;
import com.cheep.BuildConfig;
import com.cheep.R;
import com.cheep.cheepcare.model.AdminSettingModel;
import com.cheep.cheepcare.model.CareCityDetail;
import com.cheep.cheepcare.model.CityLandingPageModel;
import com.cheep.cheepcare.model.PackageDetail;
import com.cheep.cheepcare.model.RatingModel;
import com.cheep.cheepcare.model.SubscribedTaskDetailModel;
import com.cheep.model.AddressModel;
import com.cheep.model.CityModel;
import com.cheep.model.HistoryModel;
import com.cheep.model.JobCategoryModel;
import com.cheep.model.NotificationModel;
import com.cheep.model.ProviderModel;
import com.cheep.model.RateAndReviewModel;
import com.cheep.model.SubServiceDetailModel;
import com.cheep.model.TaskDetailModel;
import com.cheep.model.UserDetails;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.google.gson.Gson;
import com.google.gson.JsonArray;

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
import static com.cheep.network.NetworkUtility.TAGS.DETAIL;
import static com.cheep.network.NetworkUtility.TAGS.FINAL_EXTRA_CHARGE;
import static com.cheep.network.NetworkUtility.TAGS.HOME;
import static com.cheep.network.NetworkUtility.TAGS.OFFICE;
import static com.cheep.network.NetworkUtility.TAGS.START_DATETIME;

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

        void getSubscribedCarePackageSuccessResponse(CareCityDetail careCityDetail
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

                                CareCityDetail careCityDetail = (CareCityDetail) GsonUtility.getObjectFromJsonString(
                                        jsonData.getString(NetworkUtility.TAGS.CITY_DETAIL)
                                        , CareCityDetail.class);

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
                                        careCityDetail
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
        void onSuccessOfTaskCreate(String startdateTimeTimeStamp);
    }

    public static void createCheepCareTask(final Context mContext, SubscribedTaskDetailModel subscribedTaskDetailModel,
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
                            String startdateTimeTimeStamp = jsonObject.optJSONObject(DATA).optString(START_DATETIME);
                            successListener.onSuccessOfTaskCreate(startdateTimeTimeStamp);
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
///////Need Help ///////
    public interface GetNeedHelpResponseListener
    {

        void getNeedHelp();
    }
    public static void getNeedHelp(final Context mContext, final CommonResponseListener commonListener
            , final GetNeedHelpResponseListener successListener,String cat_id) {

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
                            successListener.getNeedHelp();


//                                UserDetails userDetails = (UserDetails) GsonUtility.getObjectFromJsonString(jsonData.toString(), UserDetails.class);
//                                PreferenceUtility.getInstance(mContext).saveUserDetails(jsonData);
//
//                                JSONArray jsonEmergencyContacts = jsonData.optJSONArray(NetworkUtility.TAGS.EMERGENCY_DATA);
//                                ArrayList<AddressModel> addressList = GsonUtility.getObjectListFromJsonString(jsonData.optJSONArray(NetworkUtility.TAGS.ADDRESS).toString(), AddressModel[].class);
//
//                                successListener.getUserDetails(userDetails, jsonEmergencyContacts, addressList);


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
mParams.put(NetworkUtility.TAGS.CAT_ID,cat_id);
        //noinspection unchecked
        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.NEED_HELP
                , errorListener
                , responseListener
                , mHeaderParams
                , mParams
                , null);

        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest, NetworkUtility.WS.NEED_HELP);
    }
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
////////////////////////////////GetRelationShipList/////////////////////
    public interface GetRelationShipResponseListener {

        void getRelationShipList( JSONArray relationshipList);
    }
public interface  UpdateEmergencyContactResponseListener{
        void getUpdateEmergencyContactResponse(JSONArray emergency_contact);
}
    public static void getRelationShipListDetail(final Context mContext, final CommonResponseListener commonListener
            , final GetRelationShipResponseListener successListener) {

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

                               // JSONObject jsonData = jsonObject.getJSONObject(NetworkUtility.TAGS.DATA);



                                JSONArray relationshipList = jsonObject.optJSONArray(NetworkUtility.TAGS.DATA);

                                //ArrayList<AddressModel> addressList = GsonUtility.getObjectListFromJsonString(jsonData.optJSONArray(NetworkUtility.TAGS.ADDRESS).toString(), AddressModel[].class);

                                successListener.getRelationShipList(relationshipList);

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
        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.GET_RELATIONSHIP_LIST
                , errorListener
                , responseListener
                , mHeaderParams
                , mParams
                , null);

        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest, NetworkUtility.WS.GET_RELATIONSHIP_LIST);
    }
///
public static void updateEmergencyContactDetail(final Context mContext, final CommonResponseListener commonListener
        , final UpdateEmergencyContactResponseListener successListener,JSONArray emergencycontact) {

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

                            // JSONObject jsonData = jsonObject.getJSONObject(NetworkUtility.TAGS.DATA);


                            JSONArray jsonEmergencyContacts = jsonObject.getJSONArray(NetworkUtility.TAGS.DATA);


                            //ArrayList<AddressModel> addressList = GsonUtility.getObjectListFromJsonString(jsonData.optJSONArray(NetworkUtility.TAGS.ADDRESS).toString(), AddressModel[].class);

                            successListener.getUpdateEmergencyContactResponse(jsonEmergencyContacts);

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
    Map<String, Object> mParams = new HashMap<>();
    mParams.put(NetworkUtility.TAGS.EMERGENCY_DATA, emergencycontact);
    //noinspection unchecked
    VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.UPDATE_EMERGENCY_CONTACTS
            , errorListener
            , responseListener
            , mHeaderParams
            , mParams
            , null);

    Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest, NetworkUtility.WS.UPDATE_EMERGENCY_CONTACTS);
}
    ////////////////////////// Get city available for cheep care call start     //////////////////////////
    public interface CityAvailableCheepCareListener {

        void getCityDetails(CareCityDetail careCityDetail);
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
                                CareCityDetail careCityDetail = new CareCityDetail();
                                careCityDetail.cityName = dataObj.optString(NetworkUtility.TAGS.CARE_CITY_NAME);
                                careCityDetail.id = dataObj.optString(NetworkUtility.TAGS.CARE_CITY_ID);
                                careCityDetail.citySlug = dataObj.optString(NetworkUtility.TAGS.CARE_CITY_SLUG);
                                successListener.getCityDetails(careCityDetail);
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
    ////////////////////////// getExtraChargeAfterExceedLimit call end     //////////////////////////]


    ////////////////////////// Create insta task booking call start     //////////////////////////
    public interface InstaBookTaskCreationListener {

        void successOfInstaBookTaskCreation();
    }

    public static void createInstaBookingTask(final Context mContext, final TaskDetailModel taskDetailModel, AddressModel mSelectedAddressModel, String quoteAmount, String payableAmount, String paymentMethod, String paymentLog, String txnId, final CommonResponseListener commonListener
            , final InstaBookTaskCreationListener successListener) {
        Map<String, Object> mTaskCreationParams = new HashMap<>();

        final Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");
                commonListener.volleyError(error);
            }
        };

        final Map<String, Object> finalMTaskCreationParams = mTaskCreationParams;
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
                            LogUtils.LOGE(TAG, "onResponse:finalMTaskCreationParams " + finalMTaskCreationParams);

                            AppsFlyerLib.getInstance().trackEvent(mContext, NetworkUtility.TAGS.APPSFLYER_CUSTOM_TRACK_EVENTS.TASK_CREATE, finalMTaskCreationParams);
                            if (!TextUtils.isEmpty(taskDetailModel.cheepCode) && taskDetailModel.cheepCode.startsWith(Utility.COUPON_DUNIA_CODE_PREFIX))
                                if (!BuildConfig.BUILD_TYPE.equalsIgnoreCase("release"))
                                    AppsFlyerLib.getInstance().trackEvent(mContext, NetworkUtility.TAGS.APPSFLYER_CUSTOM_TRACK_EVENTS.COUPON_DUNIA_TASK_DEBUG, finalMTaskCreationParams);
                                else
                                    AppsFlyerLib.getInstance().trackEvent(mContext, NetworkUtility.TAGS.APPSFLYER_CUSTOM_TRACK_EVENTS.COUPON_DUNIA_TASK_LIVE, finalMTaskCreationParams);
                            Utility.onSuccessfulInstaBookingTaskCompletion(mContext, jsonObject);
                            successListener.successOfInstaBookTaskCreation();
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

        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();

        //Add Params
        Map<String, Object> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.TASK_DESC, taskDetailModel.taskDesc);
        if (Integer.parseInt(mSelectedAddressModel.address_id) > 0) {
            mParams.put(NetworkUtility.TAGS.ADDRESS_ID, mSelectedAddressModel.address_id);
        } else {
            // In case its negative then provide other address information
            mParams = NetworkUtility.addGuestAddressParams(mParams, mSelectedAddressModel);

        }
        mParams.put(NetworkUtility.TAGS.CITY_ID, userDetails.CityID);
        mParams.put(NetworkUtility.TAGS.CAT_ID, taskDetailModel.categoryModel.catId);
        mParams.put(NetworkUtility.TAGS.START_DATETIME, taskDetailModel.taskStartdate);
        String selectedServices = new Gson().toJson(taskDetailModel.subCatList);
        mParams.put(NetworkUtility.TAGS.TASK_SUB_CATEGORIES, selectedServices);
        mParams.put(NetworkUtility.TAGS.PAYMENT_STATUS, Utility.PAYMENT_STATUS.COMPLETED);

        if (!TextUtils.isEmpty(taskDetailModel.cheepCode)) {
            mParams.put(NetworkUtility.TAGS.CHEEPCODE, taskDetailModel.cheepCode);
            mParams.put(NetworkUtility.TAGS.PROMOCODE_PRICE, taskDetailModel.taskDiscountAmount);
        } else {
            mParams.put(NetworkUtility.TAGS.CHEEPCODE, Utility.EMPTY_STRING);
            mParams.put(NetworkUtility.TAGS.PROMOCODE_PRICE, Utility.ZERO_STRING);
        }

        mParams.put(NetworkUtility.TAGS.QUOTE_AMOUNT, quoteAmount);// this is total of selected sub categories price which are with gst
        mParams.put(NetworkUtility.TAGS.PAYABLE_AMOUNT, payableAmount);
        mParams.put(NetworkUtility.TAGS.IS_REFER_CODE, taskDetailModel.isReferCode);
        mParams.put(NetworkUtility.TAGS.USED_WALLET_BALANCE, taskDetailModel.usedWalletAmount);
        String media_file = Utility.getSelectedMediaJsonString(taskDetailModel.mMediaModelList);
        mParams.put(NetworkUtility.TAGS.MEDIA_FILE, media_file);
        mParams.put(NetworkUtility.TAGS.TASK_TYPE, Utility.TASK_TYPE.INSTA_BOOK);

        if (!TextUtils.isEmpty(txnId))
            mParams.put(NetworkUtility.TAGS.TRANSACTION_ID, txnId);

        mParams.put(NetworkUtility.TAGS.PAYMENT_LOG, paymentLog);
        mParams.put(NetworkUtility.TAGS.PAYMENT_METHOD, paymentMethod);
        mParams.put(NetworkUtility.TAGS.PAYMENT_STATUS, Utility.PAYMENT_STATUS.COMPLETED);

        //noinspection unchecked
        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.CREATE_TASK
                , errorListener
                , responseListener
                , mHeaderParams
                , mParams
                , null);

        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest, NetworkUtility.WS.CREATE_TASK);

        mTaskCreationParams.put(NetworkUtility.TAGS.TASK_DESC, taskDetailModel.taskDesc);
        if (Integer.parseInt(mSelectedAddressModel.address_id) > 0) {
            mTaskCreationParams.put(NetworkUtility.TAGS.ADDRESS_ID, mSelectedAddressModel.address_id);
        } else {
            // In case its negative then provide other address information
            mTaskCreationParams = NetworkUtility.addGuestAddressParams(mParams, mSelectedAddressModel);

        }
        mTaskCreationParams.put(NetworkUtility.TAGS.CITY_ID, userDetails.CityID);
        mTaskCreationParams.put(NetworkUtility.TAGS.CAT_ID, taskDetailModel.categoryModel.catId);
        mTaskCreationParams.put(NetworkUtility.TAGS.START_DATETIME, taskDetailModel.taskStartdate);
        mTaskCreationParams.put(NetworkUtility.TAGS.TASK_SUB_CATEGORIES, selectedServices);

        if (!TextUtils.isEmpty(taskDetailModel.cheepCode)) {
            mTaskCreationParams.put(NetworkUtility.TAGS.CHEEPCODE, taskDetailModel.cheepCode);
            mTaskCreationParams.put(NetworkUtility.TAGS.PROMOCODE_PRICE, taskDetailModel.taskDiscountAmount);
        } else {
            mTaskCreationParams.put(NetworkUtility.TAGS.CHEEPCODE, Utility.EMPTY_STRING);
            mTaskCreationParams.put(NetworkUtility.TAGS.PROMOCODE_PRICE, Utility.ZERO_STRING);
        }
        mTaskCreationParams.put(NetworkUtility.TAGS.IS_REFER_CODE, taskDetailModel.isReferCode);
        mTaskCreationParams.put(NetworkUtility.TAGS.QUOTE_AMOUNT, quoteAmount);
        mTaskCreationParams.put(NetworkUtility.TAGS.PAYABLE_AMOUNT, payableAmount);
        mTaskCreationParams.put(NetworkUtility.TAGS.TRANSACTION_ID, mParams.get(NetworkUtility.TAGS.TRANSACTION_ID));
        mTaskCreationParams.put(NetworkUtility.TAGS.TASK_TYPE, Utility.TASK_TYPE.INSTA_BOOK);
        mTaskCreationParams.put(NetworkUtility.TAGS.MEDIA_FILE, media_file);
        mTaskCreationParams.put(NetworkUtility.TAGS.USED_WALLET_BALANCE, taskDetailModel.usedWalletAmount);
        mTaskCreationParams.put(NetworkUtility.TAGS.PAYMENT_LOG, paymentLog);
        mTaskCreationParams.put(NetworkUtility.TAGS.PAYMENT_METHOD, paymentMethod);
        mTaskCreationParams.put(NetworkUtility.TAGS.PAYMENT_STATUS, Utility.PAYMENT_STATUS.COMPLETED);
    }
    ////////////////////////// Create insta task booking call end     //////////////////////////

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
                NetworkUtility.WS.GET_ADMIN_SETTINGS
                , errorListener
                , responseListener
                , mHeaderParams
                , null
                , null);

        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest, NetworkUtility.WS.GET_ADMIN_SETTINGS);
    }

    ////////////////////////// getAdminSettings call end     //////////////////////////

    ////////////////////////// get rating review types and data ws start    //////////////////////////

    public interface RateAndReviewDataListener {

        void getRatingTypeList(ArrayList<RatingModel> ratingList);

        void getSubmittedRateAndReviewData();
    }

    public static void getRateAndReviewWS(final Context mContext, String taskId, final RateAndReviewDataListener rateAndReviewDataListener, final CommonResponseListener commonListener) {

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
                            JSONObject jsonData = jsonObject.optJSONObject(DATA);
                            ArrayList<RatingModel> ratingModels = GsonUtility.getObjectListFromJsonString(jsonData.optString(DETAIL), RatingModel[].class);
                            rateAndReviewDataListener.getRatingTypeList(ratingModels);

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
        Map<String, Object> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.TASK_ID, taskId);

        //noinspection unchecked
        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(
                NetworkUtility.WS.GET_TASK_REVIEW
                , errorListener
                , responseListener
                , mHeaderParams
                , mParams
                , null);

        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest, NetworkUtility.WS.GET_TASK_REVIEW);
    }

    ////////////////////////// get rating review types and data ws call end     //////////////////////////


    ////////////////////////// submit rating review types ws call start         //////////////////////////
    public interface SubmitRateAndReviewListener {
        void onSuccessOfRateAndReviewSubmit();
    }

    public static void submitReviewWS(final Context mContext, String taskId, String providerId, String rating, String message, String ratingList, final SubmitRateAndReviewListener rateAndReviewDataListener, final CommonResponseListener commonListener) {

        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
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
                            rateAndReviewDataListener.onSuccessOfRateAndReviewSubmit();
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
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, userDetails.userID);
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());

        //Add Params
        Map<String, String> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.SP_USER_ID, providerId);
        mParams.put(NetworkUtility.TAGS.TASK_ID, taskId);
        mParams.put(NetworkUtility.TAGS.RATINGS, String.valueOf(rating));
        mParams.put(NetworkUtility.TAGS.DETAIL, ratingList);
        if (!TextUtils.isEmpty(message)) {
            mParams.put(NetworkUtility.TAGS.MESSAGE, message);
        } else {
            mParams.put(NetworkUtility.TAGS.MESSAGE, Utility.EMPTY_STRING);
        }

        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.ADD_REVIEW
                , errorListener
                , responseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest);

    }

    ////////////////////////// submit rating review types ws call end            //////////////////////////

    ////////////////////////// pay pending task payment ws call start ///////////////////////////////////
    public interface PayPendingTaskPaymentListener {
        void onSuccessOfPendingTaskPaid(String taskStatus);
    }

    public static void payPendingTaskPaymentWS(final Context mContext, String txnId, String paymentLog, String paymentMethod, TaskDetailModel taskDetailModel, final CommonResponseListener commonListener, final PayPendingTaskPaymentListener pendingTaskPaymentListener) {

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
                            String taskStatus = jsonObject.optJSONObject(NetworkUtility.TAGS.DATA).optString(NetworkUtility.TAGS.TASK_STATUS);
                            pendingTaskPaymentListener.onSuccessOfPendingTaskPaid(taskStatus);
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

        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
        //Add Header parameters

        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, userDetails.userID);

        Map<String, Object> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.TRANSACTION_ID, txnId);
        mParams.put(NetworkUtility.TAGS.PAYABLE_AMOUNT, taskDetailModel.taskTotalPendingAmount);
        mParams.put(NetworkUtility.TAGS.PAYMENT_LOG, paymentLog);
        mParams.put(NetworkUtility.TAGS.PAYMENT_STATUS, Utility.PAYMENT_STATUS.COMPLETED);
        mParams.put(NetworkUtility.TAGS.PAYMENT_METHOD, paymentMethod);
        mParams.put(NetworkUtility.TAGS.TASK_ID, taskDetailModel.taskId);

        mParams.put(NetworkUtility.TAGS.PRO_PAYMENT_STATUS, taskDetailModel.paymentSummaryModel.proPaymentStatus);
        mParams.put(NetworkUtility.TAGS.ADDITIONAL_PENDING_AMOUNT,
                TextUtils.isEmpty(taskDetailModel.paymentSummaryModel.additionalPendingAmount)
                        ? Utility.ZERO_STRING :
                        taskDetailModel.paymentSummaryModel.additionalPendingAmount);

        // Url is based on condition if address id is greater then 0 then it means we need to update the existing address
        VolleyNetworkRequest mVolleyNetworkRequestForSPList = new VolleyNetworkRequest(NetworkUtility.WS.PAY_TASK_PAYMENT
                , errorListener
                , responseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForSPList);

    }

    ////////////////////////// pay pending task payment ws call ends ///////////////////////////////////

    ////////////////////////// Get payment history list call start //////////////////////////
    public interface GetPaymentHistoryListListener {

        void getPaymentHistoryList(ArrayList<HistoryModel> list, String pageNumber, String monthlyTotalPrice);
    }

    public static void getPaymentHistoryList(final Context mContext, final String nextPageId, String monthYear
            , final CommonResponseListener commonListener
            , final GetPaymentHistoryListListener successListener) {

        Log.d(TAG, "getPaymentHistoryList() called with: mContext = [" + mContext + "], nextPageId = [" + nextPageId +
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
                            ArrayList<HistoryModel> list;
                            try {
                                list = GsonUtility.getObjectListFromJsonString(jsonObject.optString(NetworkUtility.TAGS.DATA), HistoryModel[].class);
                            } catch (Exception e) {
                                Log.i(TAG, "onResponse: Error" + e.toString());
                                list = new ArrayList<>();
                            }

                            successListener.getPaymentHistoryList(list
                                    , jsonObject.optString(NetworkUtility.TAGS.PAGE_NUM)
                                    , jsonObject.optString(NetworkUtility.TAGS.MONTHLY_TOTAL));

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
        mParams.put(NetworkUtility.TAGS.LAST_ID, nextPageId);
        mParams.put(NetworkUtility.TAGS.MONTH_YEAR, monthYear);

        //noinspection unchecked
        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.PAYMENT_HISTORY
                , errorListener
                , responseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest, NetworkUtility.WS.PAYMENT_HISTORY);
    }
    ////////////////////////// Get payment history list call end //////////////////////////

    ////////////////////////// getTaskForPendingReview call start //////////////////////////
    public interface GetTaskForPendingReviewListener {
        void getTaskForPendingReviewResponse(String taskId, String catName, ProviderModel providerModel);
    }

    public static void getTaskForPendingReview(final Context mContext, final CommonResponseListener commonListener
            , final GetTaskForPendingReviewListener successResponseListener) {

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
                            int nameValuePairs = jsonObject.getJSONObject(DATA).length();
                            if (nameValuePairs > 0) {
                                ProviderModel providerModel = (ProviderModel) GsonUtility.getObjectFromJsonString(jsonObject.getString(DATA)
                                        , ProviderModel.class);
                                successResponseListener.getTaskForPendingReviewResponse(
                                        jsonObject.getJSONObject(DATA).getString(NetworkUtility.TAGS.TASK_ID)
                                        , jsonObject.getJSONObject(DATA).getString(NetworkUtility.TAGS.CAT_NAME)
                                        , providerModel);
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
        if (PreferenceUtility.getInstance(mContext).getUserDetails() != null)
            mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().userID);

        //noinspection unchecked
        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(
                NetworkUtility.WS.GET_TASK_FOR_PENDING_REVIEW
                , errorListener
                , responseListener
                , mHeaderParams
                , null
                , null);

        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest, NetworkUtility.WS.GET_TASK_FOR_PENDING_REVIEW);
    }

    ////////////////////////// getTaskForPendingReview call end     //////////////////////////


    ////////////////////////// Get User Review list call start     //////////////////////////
    public interface GetUserReviewListListener {

        void getUserReviewList(RateAndReviewModel model, String pageNumber);
    }

    public static void getUserReviewList(final Context mContext, final String nextPageId, String userId, String reviewByMe
            , final CommonResponseListener commonListener
            , final GetUserReviewListListener successListener) {

        Log.d(TAG, "getPaymentHistoryList() called with: mContext = [" + mContext + "], nextPageId = [" + nextPageId +
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
                            RateAndReviewModel model;
                            try {
                                model = (RateAndReviewModel) GsonUtility.getObjectFromJsonString(jsonObject.optString(NetworkUtility.TAGS.DATA), RateAndReviewModel.class);
                            } catch (Exception e) {
                                Log.i(TAG, "onResponse: Error" + e.toString());
                                model = new RateAndReviewModel();
                            }

                            successListener.getUserReviewList(model
                                    , model.lastId);
                            Log.e(TAG, "onResponse: " + model.lastId);

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
        mParams.put(NetworkUtility.TAGS.LAST_ID, nextPageId);
        mParams.put(NetworkUtility.TAGS.REVIEW_BY_ME, reviewByMe);

        //noinspection unchecked
        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.GET_USER_REVIEW_LIST
                , errorListener
                , responseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest, NetworkUtility.WS.GET_USER_REVIEW_LIST);
    }
    ////////////////////////// Get User Review List call end     //////////////////////////


    ////////////////////////// Set FreshChat Restore Id call start     //////////////////////////

    public static void setFreshChatRestoreId(final Context mContext, final String restoreId
            , final CommonResponseListener commonListener) {

        Log.d(TAG, "getPaymentHistoryList() called with: mContext = [" + mContext + "]" +
                ", commonListener = [" + commonListener + "],");

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
                            UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
                            userDetails.restoreId = restoreId;
                            PreferenceUtility.getInstance(mContext).saveUserDetails(userDetails);
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
        mParams.put(NetworkUtility.TAGS.RESTORE_ID, restoreId);

        //noinspection unchecked
        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.FRESHCHAT_RESTORE_ID
                , errorListener
                , responseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest, NetworkUtility.WS.FRESHCHAT_RESTORE_ID);
    }
    ////////////////////////// Fresh Chat RestoreId call end     //////////////////////////

    ////////////////////////// get city list for vote call start //////////////////////////
    public interface GetCityListListener {
        void getCityNames(ArrayList<CityModel> list);
    }

    public static void getCity(final Context mContext, final CommonResponseListener commonListener
            , final GetCityListListener successResponseListener) {

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
                            ArrayList<CityModel> cityModels = GsonUtility.getObjectListFromJsonString(jsonObject.getString(DATA), CityModel[].class);
                            successResponseListener.getCityNames(cityModels);
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

        Map<String, String> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.SEARCH_TEXT, Utility.EMPTY_STRING);

        //noinspection unchecked
        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(
                NetworkUtility.WS.SEARCH_CITY
                , errorListener
                , responseListener
                , mHeaderParams
                , mParams
                , null);

        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest, NetworkUtility.WS.SEARCH_CITY);
    }

    ////////////////////////// get city list for vote call end //////////////////////////

    ////////////////////////// add vote of city call start //////////////////////////
    public interface AddVoteForCheepCareCityListListener {
        void onSuccessOfVote();
    }

    public static void voteCityForCheepCare(final Context mContext, String phoneNumber, String cityId, final CommonResponseListener commonListener
            , final AddVoteForCheepCareCityListListener successResponseListener) {

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
                            successResponseListener.onSuccessOfVote();
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

        Map<String, String> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.CITY_ID, cityId);
        mParams.put(NetworkUtility.TAGS.PHONE_NUMBER, phoneNumber);

        //noinspection unchecked
        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(
                NetworkUtility.WS.VOTE_CITY_FOR_CHEEP_CARE
                , errorListener
                , responseListener
                , mHeaderParams
                , mParams
                , null);

        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest, NetworkUtility.WS.VOTE_CITY_FOR_CHEEP_CARE);
    }

    ////////////////////////// add vote of city call end //////////////////////////


    ////////////////////////// get address size list call start //////////////////////////

    public static void getAddressAssetSizeWS(final Context mContext, final CommonResponseListener commonListener) {

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
                            JSONObject jsonData = jsonObject.getJSONObject(DATA);
                            JSONArray homeArray = jsonData.getJSONArray(HOME);
                            JSONArray officeArray = jsonData.getJSONArray(OFFICE);
                            PreferenceUtility.getInstance(mContext).setHomeAddressSize(homeArray.toString());
                            PreferenceUtility.getInstance(mContext).setOfficeAddressSize(officeArray.toString());
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


        //noinspection unchecked
        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(
                NetworkUtility.WS.GET_ASSET_AREA
                , errorListener
                , responseListener
                , mHeaderParams
                , null
                , null);

        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest, NetworkUtility.WS.GET_ASSET_AREA);
    }

    ////////////////////////// get address size call end //////////////////////////
}