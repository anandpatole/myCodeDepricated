package com.cheep.utils;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.cheep.cheepcarenew.model.AdminSettingModel;
import com.cheep.cheepcarenew.model.CityLandingPageModel;
import com.cheep.model.AddressModel;
import com.cheep.model.ComparisionChart.ComparisionChartModel;
import com.cheep.model.GuestUserDetails;
import com.cheep.model.UserDetails;

import org.json.JSONException;
import org.json.JSONObject;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by bhavesh on 13/10/16.
 */
public class PreferenceUtility {
    private static final String TAG = "PreferenceUtility";
    private SharedPreferences mSharedPreferences;
    private Context context;
    //    private static PreferenceUtility mPreferenceUtility;
    private static UserDetails mUserDetails;
    private ComparisionChartModel comparisionChartModel;
    private CityLandingPageModel cityLandingPageModel;
    private static GuestUserDetails mGuestUserDetails;

    private static final String PREF_X_API_KEY = "com.cheep.xapikey";
    private static final String PREF_FCM_TOKEN = "com.cheep.fcm.tokem";
    private static final String PREF_USER_INFO = "com.cheep.fcm.userinfo";
    private static final String PREF_ADMIN_SETTINGS = "com.cheep.adminSettings";
    private static final String PREF_NOTIFICATION_COUNTER = "com.cheep.notification_counter";
    private static final String PREF_INTRO_SCREEN_STATUS = "com.cheep.intro.screen.status";
    private static final String PREF_HOME_SCREEN_VISIBLE = "com.cheep.homescreen.available";

    //Guest User Pref
    private static final String PREF_FILE_GUEST = "com.cheep.guest";
    private static final String PREF_GUEST_USER_INFO = "com.cheep.guest.userinfo";
    private static final String CITY_DATA = "com.cheep.guest.citydata";
    private static final String HOME_ADDRESS_SIZE = "com.cheep.homeaddresssize";
    private static final String OFFICE_ADDRESS_SIZE = "com.cheep.officeaddresssize";
    private SharedPreferences mGuestSharedPreferences;
    private static AdminSettingModel mAdminSettings;


//    private static final String PREF_SAVE_COMPARISON_CHART = "ComparisionChartFragmentDialog";
//    private static final String PREF_SAVE_CITY_LANDING_MODEl = "CityLandingPageModel";
//    private static final String PREF_TYPE_OF_PACKAGE = "Type";
    private static  String PREF_ADDRESS_MODEL="Address";
    public AddressModel addressModel=null;

    private PreferenceUtility(Context mContext) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mGuestSharedPreferences = mContext.getSharedPreferences(PREF_FILE_GUEST, Context.MODE_PRIVATE);
        context = mContext;
    }

    public static PreferenceUtility getInstance(Context mContext) {
        return new PreferenceUtility(mContext);
    }

    public void setXAPIKey(String value) {
        mSharedPreferences.edit().putString(PREF_X_API_KEY, value).apply();
    }

    public String getXAPIKey() {
        if (mSharedPreferences.contains(PREF_X_API_KEY)) {
            return mSharedPreferences.getString(PREF_X_API_KEY, null);
        }
        return null;
    }

    public void setFCMRegID(String token) {
        mSharedPreferences.edit().putString(PREF_FCM_TOKEN, token).apply();
    }

    public void setHomeAddressSize(String homeArrayList) {
        mSharedPreferences.edit().putString(HOME_ADDRESS_SIZE, homeArrayList).apply();
    }

    public void setOfficeAddressSize(String officeArrayList) {
        mSharedPreferences.edit().putString(OFFICE_ADDRESS_SIZE, officeArrayList).apply();
    }

    public String getHomeAddressSize() {
        if (mSharedPreferences.contains(HOME_ADDRESS_SIZE)) {
            return mSharedPreferences.getString(HOME_ADDRESS_SIZE, "");
        }
        return "";
    }

    public String getOfficeAddressSize() {
        if (mSharedPreferences.contains(OFFICE_ADDRESS_SIZE)) {
            return mSharedPreferences.getString(OFFICE_ADDRESS_SIZE, "");
        }
        return "";
    }

    public String getFCMRegID() {
        if (mSharedPreferences.contains(PREF_FCM_TOKEN)) {
            return mSharedPreferences.getString(PREF_FCM_TOKEN, null);
        }
        return null;
    }

    public void saveUserDetails(JSONObject jsonData) {
        mUserDetails = (UserDetails) GsonUtility.getObjectFromJsonString(jsonData.toString(), UserDetails.class);
        Log.d(TAG, "saveUserDetails() called with: jsonData = [" + jsonData + "]");
        mSharedPreferences.edit().putString(PREF_USER_INFO, jsonData.toString()).apply();

        //updating hotline profile
        // HotlineHelper.getInstance(context).updateUserInfo(mUserDetails, context);
        FreshChatHelper.getInstance(context).updateUserInfo(mUserDetails, context);
    }

    /**
     * It may be useful in future so it is remain as private
     *
     * @param model
     */
    public void saveUserDetails(UserDetails model) {
        Log.d(TAG, "saveUserDetails() called with: model = [" + model + "]");
        mSharedPreferences.edit().putString(PREF_USER_INFO, GsonUtility.getJsonStringFromObject(model)).apply();
        mUserDetails = model;
        //updating hotline profile
        //  HotlineHelper.getInstance(context).updateUserInfo(mUserDetails, context);
        FreshChatHelper.getInstance(context).updateUserInfo(mUserDetails, context);
    }

    public UserDetails getUserDetails() {
        if (mUserDetails != null) {
            return mUserDetails;
        }
        if (mSharedPreferences.contains(PREF_USER_INFO)) {
            try {
                JSONObject jsonObject = new JSONObject(mSharedPreferences.getString(PREF_USER_INFO, null));
                mUserDetails = (UserDetails) GsonUtility.getObjectFromJsonString(jsonObject.toString(), UserDetails.class);
                return mUserDetails;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

//    public void saveComparisonChatDetails(ComparisionChartModel model) {
//        Log.d(TAG, "saveComparisonChatDetails() called with: model = [" + model + "]");
//        mSharedPreferences.edit().putString(PREF_SAVE_COMPARISON_CHART, GsonUtility.getJsonStringFromObject(model)).apply();
//
//    }
//    public ComparisionChartModel getComparisonChatDetails() {
//        if (mSharedPreferences.contains(PREF_SAVE_COMPARISON_CHART)) {
//            try {
//                JSONObject jsonObject = new JSONObject(mSharedPreferences.getString(PREF_SAVE_COMPARISON_CHART, null));
//                comparisionChartModel = (ComparisionChartModel) GsonUtility.getObjectFromJsonString(jsonObject.toString(), ComparisionChartModel.class);
//                return comparisionChartModel;
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//        return null;
//    }
//
//    public void saveCityLandingPageModel(CityLandingPageModel model) {
//        Log.d(TAG, "saveCityLandingPageModel() called with: model = [" + model + "]");
//        mSharedPreferences.edit().putString(PREF_SAVE_CITY_LANDING_MODEl, GsonUtility.getJsonStringFromObject(model)).apply();
//
//    }
//    public CityLandingPageModel getCityLandingPageModel() {
//        if (mSharedPreferences.contains(PREF_SAVE_CITY_LANDING_MODEl)) {
//            try {
//                JSONObject jsonObject = new JSONObject(mSharedPreferences.getString(PREF_SAVE_CITY_LANDING_MODEl, null));
//                cityLandingPageModel = (CityLandingPageModel) GsonUtility.getObjectFromJsonString(jsonObject.toString(), CityLandingPageModel.class);
//                return cityLandingPageModel;
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//        return null;
//    }
//
//    public void saveTypeOfPackage(String value) {
//        mSharedPreferences.edit().putString(PREF_TYPE_OF_PACKAGE, value).apply();
//    }
//
//    public String getTypeOfPackage()
//    {
//        if (mSharedPreferences.contains(PREF_TYPE_OF_PACKAGE)) {
//            return mSharedPreferences.getString(PREF_TYPE_OF_PACKAGE, null);
//        }
//        return null;
//    }

    public void setAddressModel(AddressModel model) {
        Log.d(TAG, "setAdminSettings() called with: model = [" + model + "]");
        mSharedPreferences.edit().putString(PREF_ADDRESS_MODEL, GsonUtility.getJsonStringFromObject(model)).apply();
    }

    public AddressModel getAddressModel() {
        if (mSharedPreferences.contains(PREF_ADDRESS_MODEL)) {
            try {
                JSONObject jsonObject = new JSONObject(mSharedPreferences.getString(PREF_ADDRESS_MODEL, null));
                addressModel = (AddressModel) GsonUtility.getObjectFromJsonString(jsonObject.toString(), AddressModel.class);
                return addressModel;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * It may be useful in future so it is remain as private
     *
     * @param model
     */
    public void setAdminSettings(AdminSettingModel model) {
        Log.d(TAG, "setAdminSettings() called with: model = [" + model + "]");
        mSharedPreferences.edit().putString(PREF_ADMIN_SETTINGS, GsonUtility.getJsonStringFromObject(model)).apply();
        mAdminSettings = model;
    }

    public AdminSettingModel getAdminSettings() {
        if (mAdminSettings != null) {
            return mAdminSettings;
        }
        if (mSharedPreferences.contains(PREF_ADMIN_SETTINGS)) {
            try {
                JSONObject jsonObject = new JSONObject(mSharedPreferences.getString(PREF_ADMIN_SETTINGS, null));
                mAdminSettings = (AdminSettingModel) GsonUtility.getObjectFromJsonString(jsonObject.toString(), AdminSettingModel.class);
                return mAdminSettings;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void updateIntroScreenStatus(boolean status) {
        mSharedPreferences.edit().putBoolean(PREF_INTRO_SCREEN_STATUS, status).apply();
    }

    public boolean getIntroScreenStatus() {
        return mSharedPreferences.contains(PREF_INTRO_SCREEN_STATUS) && mSharedPreferences.getBoolean(PREF_INTRO_SCREEN_STATUS, false);
    }

    /**
     * This will be needs to call when User logged out from application
     */
    public void onUserLogout() {
        //updating hotline profile
        // HotlineHelper.getInstance(context).clearUser(context);
        FreshChatHelper.getInstance(context).resetUser(context);

        // Clear All notification if raised
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();

        mUserDetails = null;
        mSharedPreferences.edit().remove(PREF_USER_INFO).apply();
        mSharedPreferences.edit().remove(PREF_X_API_KEY).apply();


        // Clear Unread otification counter
        mSharedPreferences.edit().remove(PREF_NOTIFICATION_COUNTER).apply();

        // Clear cart detail of all cities
        String[] ciryArray = mSharedPreferences.getString(CITY_DATA, Utility.EMPTY_STRING).split(",");
        LogUtils.LOGE(TAG, "onUserLogout: " + mSharedPreferences.getString(CITY_DATA, Utility.EMPTY_STRING));
        if (ciryArray.length > 0) {
            for (String s : ciryArray) {
                removeCityCartDetail(s);
            }
            mSharedPreferences.edit().remove(CITY_DATA).apply();
        }


        // Clear all Guest User Details if any
        mGuestUserDetails = null;
        mGuestSharedPreferences.edit().remove(PREF_GUEST_USER_INFO).apply();

        Log.d(TAG, "onUserLogout() finished");
    }

    //For Notification Counter
    public void incrementUnreadNotificationCounter() {
        int counter = mSharedPreferences.getInt(PREF_NOTIFICATION_COUNTER, 0);
        mSharedPreferences.edit().putInt(PREF_NOTIFICATION_COUNTER, ++counter).apply();
    }

    public int getUnreadNotificationCounter() {
        return mSharedPreferences.getInt(PREF_NOTIFICATION_COUNTER, 0);
    }

    public void clearUnreadNotificationCounter() {
        mSharedPreferences.edit().putInt(PREF_NOTIFICATION_COUNTER, 0).apply();
    }


    /**
     * Guest user Info
     */
    public GuestUserDetails getGuestUserDetails() {
        if (mGuestUserDetails != null) {
            return mGuestUserDetails;
        }
        if (mGuestSharedPreferences.contains(PREF_GUEST_USER_INFO)) {
            try {
                JSONObject jsonObject = new JSONObject(mGuestSharedPreferences.getString(PREF_GUEST_USER_INFO, null));
                mGuestUserDetails = (GuestUserDetails) GsonUtility.getObjectFromJsonString(jsonObject.toString(), GuestUserDetails.class);
                return mGuestUserDetails;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return mGuestUserDetails == null ? new GuestUserDetails() : mGuestUserDetails;
    }

    public void saveGuestUserDetails(GuestUserDetails model) {
        Log.d(TAG, "saveUserDetails() called with: model = [" + model + "]");
        mGuestSharedPreferences.edit().putString(PREF_GUEST_USER_INFO, GsonUtility.getJsonStringFromObject(model)).apply();
        mGuestUserDetails = model;
    }

    /**
     * Below would manage HomeScreen availability to properly manage Guest flow
     */

    public void setHomeScreenVisibility(boolean flag) {
        mSharedPreferences.edit().putBoolean(PREF_HOME_SCREEN_VISIBLE, flag).apply();
    }

    public boolean isHomeScreenVisible() {
        return !mSharedPreferences.contains(PREF_HOME_SCREEN_VISIBLE) || mSharedPreferences.getBoolean(PREF_HOME_SCREEN_VISIBLE, true);
    }


    /**
     * @param citySlug    slug of cart city
     * @param cartDetails it will have whole care package list with its sub services data, selected address, payment data
     */
    public void setCityCartDetail(String citySlug, String cartDetails) {
        mSharedPreferences.edit().putString(citySlug, cartDetails).apply();

        String s = getCityData();
        if (s != null && !TextUtils.isEmpty(s)) {
            if (!s.contains(citySlug))
                mSharedPreferences.edit().putString(CITY_DATA, s + "," + citySlug).apply();
        } else {
            mSharedPreferences.edit().putString(CITY_DATA, citySlug).apply();
        }
        LogUtils.LOGE(TAG, "setCityCartDetail: " + mSharedPreferences.getString(CITY_DATA, Utility.EMPTY_STRING));
    }

    public String getCityCartDetail(String citySlug) {
        return mSharedPreferences.getString(citySlug, Utility.EMPTY_STRING);
    }

    public void removeCityCartDetail(String citySlug) {
        LogUtils.LOGE(TAG, "removeCityCartDetail() called with: citySlug = [" + citySlug + "]");
        mSharedPreferences.edit().remove(citySlug).apply();
    }


    public String getCityData() {
        return mSharedPreferences.getString(CITY_DATA, Utility.EMPTY_STRING);
    }
}
