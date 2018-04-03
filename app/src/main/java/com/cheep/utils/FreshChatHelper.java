package com.cheep.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.volley.VolleyError;
import com.cheep.BuildConfig;
import com.cheep.model.UserDetails;
import com.freshchat.consumer.sdk.Freshchat;
import com.freshchat.consumer.sdk.FreshchatConfig;
import com.freshchat.consumer.sdk.FreshchatUser;
import com.freshdesk.hotline.ConversationOptions;
import com.freshdesk.hotline.Hotline;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by meet on 31/3/18.
 */

public class FreshChatHelper {

    private static FreshChatHelper freshChatHelper;

    private FreshChatHelper(Context context) {
        FreshchatConfig freshchatConfig = new FreshchatConfig(BuildConfig.HOTLINE_APP_ID, BuildConfig.HOTLINE_APP_KEY);
        Freshchat.getInstance(context).init(freshchatConfig);
    }


    public static FreshChatHelper getInstance(Context context) {

        if (freshChatHelper == null) {
            freshChatHelper = new FreshChatHelper(context);
        }
        return freshChatHelper;
    }

    public void updateUserInfo(UserDetails userDetails, Context mContext) {

        if (PreferenceUtility.getInstance(mContext).getUserDetails() != null) {
            if (PreferenceUtility.getInstance(mContext).getUserDetails().restoreId == null) {
                //Get the user object for the current installation
                FreshchatUser hlUser = Freshchat.getInstance(mContext).getUser();

                if (hlUser != null) {
                    hlUser.setFirstName(userDetails.userName);
                    hlUser.setEmail(userDetails.email);
                    hlUser.setPhone("", userDetails.phoneNumber);

            /*try {
                Hotline.getInstance(mContext).updateUserProperty("id", "1");
            } catch (Exception e) {
                e.printStackTrace();
            }*/

                    //Call updateUser so that the user information is synced with Hotline's servers
                    Freshchat.getInstance(mContext).setUser(hlUser);
                    String externalId = PreferenceUtility.getInstance(mContext).getUserDetails().userID;
                    Freshchat.getInstance(mContext).identifyUser(externalId, null);
                    IntentFilter intentFilter = new IntentFilter(Freshchat.FRESHCHAT_USER_RESTORE_ID_GENERATED);
                    LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(broadcastReceiver, intentFilter);
                }

            }


        }
    }

    // Listen
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String restoreId = Freshchat.getInstance(getApplicationContext()).getUser().getRestoreId();
            WebCallClass.setFreshChatRestoreId(context, restoreId, commonResponseListener);
            Log.e("Got the restore iddddd hurrayyy: ", restoreId);
            //Unregister
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(broadcastReceiver);
        }
    };

    WebCallClass.CommonResponseListener commonResponseListener = new WebCallClass.CommonResponseListener() {
        @Override
        public void volleyError(VolleyError error) {
        }

        @Override
        public void showSpecificMessage(String message) {
        }

        @Override
        public void forceLogout() {
        }
    };


    public void resetUser(Context mContext) {
        Freshchat.resetUser(mContext);
    }

    public void showConversation(Context context) {
        if (PreferenceUtility.getInstance(context).getUserDetails() != null && PreferenceUtility.getInstance(context).getUserDetails().restoreId != null) {
            Freshchat.getInstance(context).identifyUser(PreferenceUtility.getInstance(context).getUserDetails().userID,
                    PreferenceUtility.getInstance(context).getUserDetails().restoreId);
        }

        Freshchat.showConversations(context);
    }
}
