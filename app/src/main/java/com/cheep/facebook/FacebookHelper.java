package com.cheep.facebook;

import android.app.Activity;
import android.content.Context;

import com.cheep.utils.Utility;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by bhavesh on 9/4/15.
 */
public class FacebookHelper {
    private static final String TAG = FacebookHelper.class.getSimpleName();
    private Activity mActivity;
    private LoginManager mLoginManager;
    private LoginResult mLoginResult;
    private Profile mFacebookProfile;

//    public static final String EMAIL = "email";
//    public static final String PUBLIC_PROFILE = "public_profile";
//    public static final String BIRTH_DATE = "user_birthday";
//    public static final String AGE_RANGE = "age_range";

    private Collection<String> READ_PERMISSIONS = Arrays.asList(Utility.EMAIL, Utility.PUBLIC_PROFILE);

    public interface FacebookCallbacks {
        void onFBLoginSuccessCalled(LoginResult loginResult);

        void onFBLoginCancelledCalled();

        void onFBLoginonErrorCalled(FacebookException e);
    }

    private FacebookCallbacks mFacebookCallbacks;

    public FacebookHelper(Context context, CallbackManager mCallbackManager) {
        this.mActivity = (Activity) context;
        mFacebookCallbacks = (FacebookCallbacks) context;
        FacebookSdk.sdkInitialize(mActivity.getApplicationContext());
        mLoginManager = LoginManager.getInstance();
        FacebookCallback<LoginResult> mLoginFacebookCallback = new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                mFacebookCallbacks.onFBLoginSuccessCalled(loginResult);
            }

            @Override
            public void onCancel() {
                mFacebookCallbacks.onFBLoginCancelledCalled();
            }

            @Override
            public void onError(FacebookException e) {
                mFacebookCallbacks.onFBLoginonErrorCalled(e);
            }
        };
        mLoginManager.registerCallback(mCallbackManager, mLoginFacebookCallback);
    }

    public void login() {
        mLoginManager.logInWithReadPermissions(mActivity, READ_PERMISSIONS);
    }


    public void logout() {
        mLoginManager.logOut();
    }
}
