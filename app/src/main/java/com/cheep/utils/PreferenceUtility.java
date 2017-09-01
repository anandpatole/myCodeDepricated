package com.cheep.utils;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

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
    private static GuestUserDetails mGuestUserDetails;

    private static final String PREF_X_API_KEY = "com.cheep.xapikey";
    private static final String PREF_FCM_TOKEN = "com.cheep.fcm.tokem";
    private static final String PREF_USER_INFO = "com.cheep.fcm.userinfo";
    private static final String PREF_NOTIFICATION_COUNTER = "com.cheep.notification_counter";
    private static final String PREF_INTRO_SCREEN_STATUS = "com.cheep.intro.screen.status";
    private static final String PREF_HOME_SCREEN_VISIBLE = "com.cheep.homescreen.available";

    //Guest User Pref
    private static final String PREF_FILE_GUEST = "com.cheep.guest";
    private static final String PREF_GUEST_USER_INFO = "com.cheep.guest.userinfo";
    private SharedPreferences mGuestSharedPreferences;

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
        HotlineHelper.getInstance(context).updateUserInfo(mUserDetails, context);
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
        HotlineHelper.getInstance(context).updateUserInfo(mUserDetails, context);
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

    public void updateIntroScreenStatus(boolean status) {
        mSharedPreferences.edit().putBoolean(PREF_INTRO_SCREEN_STATUS, status).apply();
    }

    public boolean getIntroScreenStatus() {
        if (mSharedPreferences.contains(PREF_INTRO_SCREEN_STATUS)) {
            return mSharedPreferences.getBoolean(PREF_INTRO_SCREEN_STATUS, false);
        }
        return false;
    }

    /**
     * This will be needs to call when User logged out from application
     */
    public void onUserLogout() {
        //updating hotline profile
        HotlineHelper.getInstance(context).clearUser(context);

        // Clear All notification if raised
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();

        mUserDetails = null;
        mSharedPreferences.edit().remove(PREF_USER_INFO).apply();
        mSharedPreferences.edit().remove(PREF_X_API_KEY).apply();

        // Clear Unread otification counter
        mSharedPreferences.edit().remove(PREF_NOTIFICATION_COUNTER).apply();

        // Clear all Guest User Details if any
        mGuestSharedPreferences.edit().remove(PREF_GUEST_USER_INFO).apply();
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
                mGuestUserDetails = (GuestUserDetails) Utility.getObjectFromJsonString(jsonObject.toString(), GuestUserDetails.class);
                return mGuestUserDetails;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return mGuestUserDetails == null ? new GuestUserDetails() : mGuestUserDetails;
    }

    public void saveGuestUserDetails(GuestUserDetails model) {
        Log.d(TAG, "saveUserDetails() called with: model = [" + model + "]");
        mGuestSharedPreferences.edit().putString(PREF_GUEST_USER_INFO, Utility.getJsonStringFromObject(model)).apply();
        mGuestUserDetails = model;
    }


    /**
     * Below would manage HomeScreen availability to properly manage Guest flow
     */

    public void setHomeScreenVisibility(boolean flag) {
        mSharedPreferences.edit().putBoolean(PREF_HOME_SCREEN_VISIBLE, flag).apply();
    }

    public boolean isHomeScreenVisible() {
        if (mSharedPreferences.contains(PREF_HOME_SCREEN_VISIBLE)) {
            return mSharedPreferences.getBoolean(PREF_HOME_SCREEN_VISIBLE, true);
        }
        return true;
    }
}
