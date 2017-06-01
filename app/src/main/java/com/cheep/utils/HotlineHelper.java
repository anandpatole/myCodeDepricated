package com.cheep.utils;

import android.content.Context;

import com.cheep.BuildConfig;
import com.cheep.model.UserDetails;
import com.freshdesk.hotline.Hotline;
import com.freshdesk.hotline.HotlineConfig;
import com.freshdesk.hotline.HotlineUser;
import com.freshdesk.hotline.exception.HotlineInvalidUserPropertyException;

/**
 * Created by pankaj on 1/4/17.
 */

public class HotlineHelper {

    private static HotlineHelper hotlineHelper;

    private HotlineHelper(Context context) {

        HotlineConfig hlConfig = new HotlineConfig(BuildConfig.HOTLINE_APP_ID, BuildConfig.HOTLINE_APP_KEY);

        hlConfig.setVoiceMessagingEnabled(true);
        hlConfig.setCameraCaptureEnabled(true);
        hlConfig.setPictureMessagingEnabled(true);

        Hotline.getInstance(context).init(hlConfig);
    }

    public static HotlineHelper getInstance(Context context) {

        if (hotlineHelper == null) {
            hotlineHelper = new HotlineHelper(context);
        }
        return hotlineHelper;
    }

    public void updateUserInfo(UserDetails userDetails, Context mContext) {

        /**
         * Updating fcm registration id (Device token)
         */
        Hotline.getInstance(mContext).updateGcmRegistrationToken(PreferenceUtility.getInstance(mContext).getFCMRegID());

        //Get the user object for the current installation
        HotlineUser hlUser = Hotline.getInstance(mContext).getUser();

        if (hlUser != null) {
            hlUser.setName(userDetails.UserName);
            hlUser.setEmail(userDetails.Email);
            hlUser.setPhone("", userDetails.PhoneNumber);
            hlUser.setExternalId(userDetails.UserName + "." + userDetails.UserID);

            /*try {
                Hotline.getInstance(mContext).updateUserProperty("id", "1");
            } catch (Exception e) {
                e.printStackTrace();
            }*/

            //Call updateUser so that the user information is synced with Hotline's servers
            Hotline.getInstance(mContext).updateUser(hlUser);

        }
    }

    public void clearUser(Context mContext) {
        Hotline.clearUserData(mContext);
    }

    public void showConversation(Context context) {
        Hotline.showConversations(context);
    }
}
