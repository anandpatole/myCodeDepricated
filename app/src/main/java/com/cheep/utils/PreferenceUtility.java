package com.cheep.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.cheep.model.UserDetails;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by bhavesh on 13/10/16.
 */

public class PreferenceUtility {
    private static final String TAG = "PreferenceUtility";
    SharedPreferences mSharedPreferences;
    private Context context;
    private static PreferenceUtility mPreferenceUtility;
    private static UserDetails mUserDetails;

    private static final String PREF_X_API_KEY = "com.cheep.xapikey";
    private static final String PREF_FCM_TOKEN = "com.cheep.fcm.tokem";
    private static final String PREF_USER_INFO = "com.cheep.fcm.userinfo";
    private static final String PREF_NOTIFICATION_COUNTER = "com.cheep.notification_counter";

    private static final String PREF_INTRO_SCREEN_STATUS="com.cheep.intro.screen.status";

    private PreferenceUtility(Context mContext) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        context = mContext;
    }

    public static PreferenceUtility getInstance(Context mContext) {
        if (mPreferenceUtility == null)
            mPreferenceUtility = new PreferenceUtility(mContext);
        return mPreferenceUtility;
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

    public String getFCMRegID() {
        if (mSharedPreferences.contains(PREF_FCM_TOKEN)) {
            return mSharedPreferences.getString(PREF_FCM_TOKEN, null);
        }
        return null;
    }

    public void saveUserDetails(JSONObject jsonData) {
        mUserDetails = (UserDetails) Utility.getObjectFromJsonString(jsonData.toString(), UserDetails.class);
        Log.d(TAG, "saveUserDetails() called with: jsonData = [" + jsonData + "]");
        mSharedPreferences.edit().putString(PREF_USER_INFO, jsonData.toString()).apply();

        //updating hotline profile
        HotlineHelper.getInstance(context).updateUserInfo(mUserDetails,context);
    }

    /**
     * It may be usefull in future so it is remain as private
     *
     * @param model
     */
    public void saveUserDetails(UserDetails model) {
        Log.d(TAG, "saveUserDetails() called with: model = [" + model + "]");
        mSharedPreferences.edit().putString(PREF_USER_INFO, Utility.getJsonStringFromObject(model)).apply();
        mUserDetails = model;
        //updating hotline profile
        HotlineHelper.getInstance(context).updateUserInfo(mUserDetails,context);
    }

    public UserDetails getUserDetails() {
        if (mUserDetails != null) {
            return mUserDetails;
        }
        if (mSharedPreferences.contains(PREF_USER_INFO)) {
            try {
                JSONObject jsonObject = new JSONObject(mSharedPreferences.getString(PREF_USER_INFO, null));
                mUserDetails = (UserDetails) Utility.getObjectFromJsonString(jsonObject.toString(), UserDetails.class);
                return mUserDetails;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void updateIntroScreenStatus(boolean status)
    {
        mSharedPreferences.edit().putBoolean(PREF_INTRO_SCREEN_STATUS,status).apply();
    }

    public boolean getIntroScreenStatus()
    {
        if (mSharedPreferences.contains(PREF_INTRO_SCREEN_STATUS))
        {
            return  mSharedPreferences.getBoolean(PREF_INTRO_SCREEN_STATUS, false);
        }
        return false;
    }

    /**
     * This will be needs to call when User logged out from application
     */
    public void onUserLogout() {
        //updating hotline profile
        HotlineHelper.getInstance(context).clearUser(context);

        mUserDetails = null;
        mSharedPreferences.edit().remove(PREF_USER_INFO).apply();
        mSharedPreferences.edit().remove(PREF_X_API_KEY).apply();
        mSharedPreferences.edit().remove(PREF_NOTIFICATION_COUNTER).apply();
    }


    //For Notification Counter
    public void incrementUnreadNotificationCounter() {
        int counter = mSharedPreferences.getInt(PREF_NOTIFICATION_COUNTER, 0);
        mSharedPreferences.edit().putInt(PREF_NOTIFICATION_COUNTER, ++counter).commit();
    }

    public int getUnreadNotificationCounter() {
        return mSharedPreferences.getInt(PREF_NOTIFICATION_COUNTER, 0);
    }

    public void clearUnreadNotificationCounter() {
        mSharedPreferences.edit().putInt(PREF_NOTIFICATION_COUNTER, 0).commit();
    }
}
