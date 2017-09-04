package com.cheep.activity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.appsflyer.AppsFlyerLib;
import com.cheep.BuildConfig;
import com.cheep.R;
import com.cheep.databinding.ActivityVerificationBinding;
import com.cheep.firebase.FierbaseChatService;
import com.cheep.firebase.FirebaseHelper;
import com.cheep.firebase.FirebaseUtils;
import com.cheep.firebase.model.ChatUserModel;
import com.cheep.model.UserDetails;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by pankaj on 9/26/16.
 */

public class VerificationActivity extends BaseAppCompatActivity {
    private static final String TAG = "VerificationActivity";
    private ActivityVerificationBinding mActivityVerificationBinding;

    private UserDetails mUserDetails;
    //    private String password;
    private String selectedImagePath;
//    private String correctOTP;

    public static void newInstance(Context context, UserDetails userDetails, String password, String selectedImagePath, String correctOTP) {
        Intent intent = new Intent(context, VerificationActivity.class);
        intent.putExtra(Utility.Extra.USER_DETAILS, Utility.getJsonStringFromObject(userDetails));
//        intent.putExtra(Utility.Extra.PASSWORD, password);
        intent.putExtra(Utility.Extra.SELECTED_IMAGE_PATH, selectedImagePath);
        intent.putExtra(Utility.Extra.PHONE_NUMBER, userDetails.PhoneNumber);
//        intent.putExtra(Utility.Extra.CORRECT_OTP, correctOTP);
        intent.putExtra(Utility.Extra.INFO_TYPE, Utility.ACTION_REGISTER);
        context.startActivity(intent);
    }

    // While Changing Mobile number
    public static void newInstance(Context context, UserDetails userDetails, String phoneNumber, String action) {
        Intent intent = new Intent(context, VerificationActivity.class);
        intent.putExtra(Utility.Extra.USER_DETAILS, Utility.getJsonStringFromObject(userDetails));
        intent.putExtra(Utility.Extra.PHONE_NUMBER, phoneNumber);
        intent.putExtra(Utility.Extra.INFO_TYPE, action);
        context.startActivity(intent);
    }

    // For Login With MOBILE
    public static void newInstance(Context context, String userDetails, String action) {
        Intent intent = new Intent(context, VerificationActivity.class);
        intent.putExtra(Utility.Extra.USER_DETAILS, userDetails);
        final UserDetails details = (UserDetails) Utility.getObjectFromJsonString(userDetails, UserDetails.class);
        intent.putExtra(Utility.Extra.PHONE_NUMBER, details.PhoneNumber);
        intent.putExtra(Utility.Extra.INFO_TYPE, action);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityVerificationBinding = DataBindingUtil.setContentView(this, R.layout.activity_verification);
        initiateUI();
        setListeners();
    }

    @Override
    protected void initiateUI() {

        getWindow().setBackgroundDrawableResource(R.drawable.login_bg_blur);
        //Check if we got any user details that need to be updated
        if (getIntent().hasExtra(Utility.Extra.USER_DETAILS)) {
            mUserDetails = (UserDetails) Utility.getObjectFromJsonString(getIntent().getExtras().getString(Utility.Extra.USER_DETAILS), UserDetails.class);
        }

        //Check if we got password
        /*if (getIntent().hasExtra(Utility.Extra.PASSWORD)) {
            password = getIntent().getExtras().getString(Utility.Extra.PASSWORD);
        }*/

        //Check if we got selectedImagePath
        if (getIntent().hasExtra(Utility.Extra.SELECTED_IMAGE_PATH)) {
            selectedImagePath = getIntent().getExtras().getString(Utility.Extra.SELECTED_IMAGE_PATH);
        }

        //Check if we got OTP from backend team
        /*if (getIntent().hasExtra(Utility.Extra.CORRECT_OTP)) {
            correctOTP = getIntent().getExtras().getString(Utility.Extra.CORRECT_OTP);
        }*/

        //Setting toolbar
        setSupportActionBar(mActivityVerificationBinding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(Utility.EMPTY_STRING);
            mActivityVerificationBinding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Utility.hideKeyboard(mContext);
                    onBackPressed();
                }
            });
        }

        //checking if user comes from change password screen or register screen
        if (Utility.ACTION_CHANGE_PHONE_NUMBER.equalsIgnoreCase(getIntent().getStringExtra(Utility.Extra.INFO_TYPE))) {
            mActivityVerificationBinding.btnNearlyThere.setAllCaps(false);
            mActivityVerificationBinding.btnNearlyThere.setText(getString(R.string.label_verify));
        } else if (Utility.ACTION_REGISTER.equalsIgnoreCase(getIntent().getStringExtra(Utility.Extra.INFO_TYPE))
                || Utility.ACTION_LOGIN.equalsIgnoreCase(getIntent().getStringExtra(Utility.Extra.INFO_TYPE))) {
            mActivityVerificationBinding.btnNearlyThere.setAllCaps(false);
            mActivityVerificationBinding.btnNearlyThere.setText(getString(R.string.label_brilliant_its_time_to_cheep));
        }

    }

    private ArrayList<EditText> sequenceList;

    @Override
    protected void setListeners() {

        //Setting sequence
        sequenceList = new ArrayList<EditText>(Arrays.asList(
                mActivityVerificationBinding.editOtp1,
                mActivityVerificationBinding.editOtp2,
                mActivityVerificationBinding.editOtp3,
                mActivityVerificationBinding.editOtp4)
        );

        //Adding listener to focus on next edittext when fill
        mActivityVerificationBinding.editOtp1.addTextChangedListener(new MyTextWatcher(mActivityVerificationBinding.editOtp1));
        mActivityVerificationBinding.editOtp2.addTextChangedListener(new MyTextWatcher(mActivityVerificationBinding.editOtp2));
        mActivityVerificationBinding.editOtp3.addTextChangedListener(new MyTextWatcher(mActivityVerificationBinding.editOtp3));
        mActivityVerificationBinding.editOtp4.addTextChangedListener(new MyTextWatcher(mActivityVerificationBinding.editOtp4));

        //Adding Backpress key listener to request focus to previous edittext;
        mActivityVerificationBinding.editOtp1.setOnKeyListener(new CKeyListener(null));
        mActivityVerificationBinding.editOtp2.setOnKeyListener(new CKeyListener(mActivityVerificationBinding.editOtp1));
        mActivityVerificationBinding.editOtp3.setOnKeyListener(new CKeyListener(mActivityVerificationBinding.editOtp2));
        mActivityVerificationBinding.editOtp4.setOnKeyListener(new CKeyListener(mActivityVerificationBinding.editOtp3));

        //Click Listener
        mActivityVerificationBinding.btnNearlyThere.setOnClickListener(onClickListener);
        mActivityVerificationBinding.tvResendCode.setOnClickListener(onClickListener);
    }


    /**
     * Listener to manage clear button, when user clicks on clear button on keyboard we have to move focus to previous EditText
     */
    private class CKeyListener implements View.OnKeyListener {

        private EditText editText;

        public CKeyListener(EditText edt) {
            this.editText = edt;
        }

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {

            if (editText != null && event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    // this is for backspace
                    String txt = ((EditText) v).getText().toString();
                    if (txt.length() <= 0) {
                        editText.setText("");
                        editText.requestFocus();
                        return true;
                    }
                }
            }
            return false;
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_nearly_there:
                    //checking if user comes from change password screen or register screen
                    if (Utility.ACTION_CHANGE_PHONE_NUMBER.equalsIgnoreCase(getIntent().getStringExtra(Utility.Extra.INFO_TYPE))) {
                        callVerifyOTPWS(getIntent().getStringExtra(Utility.Extra.PHONE_NUMBER));
                    } else if (Utility.ACTION_REGISTER.equalsIgnoreCase(getIntent().getStringExtra(Utility.Extra.INFO_TYPE))) {
                        //calling verify otp to check online from web server then call proceedWithSignUp
                        if (isValidate()) {
                            callVerifyOTPWS(getIntent().getStringExtra(Utility.Extra.PHONE_NUMBER));
                        }
//                        proceedWithSignUp();
                    } else if (Utility.ACTION_LOGIN.equalsIgnoreCase(getIntent().getStringExtra(Utility.Extra.INFO_TYPE))) {
                        if (isValidate()) {
                            callVerifyOTPWS(getIntent().getStringExtra(Utility.Extra.PHONE_NUMBER));
                        }
                    }
                    break;
                case R.id.tv_resend_code:

                    if (Utility.ACTION_CHANGE_PHONE_NUMBER.equalsIgnoreCase(getIntent().getStringExtra(Utility.Extra.INFO_TYPE))) {
                        callResendOTPWSForChangePassword(getIntent().getStringExtra(Utility.Extra.PHONE_NUMBER));
                    } else if (Utility.ACTION_REGISTER.equalsIgnoreCase(getIntent().getStringExtra(Utility.Extra.INFO_TYPE))
                            || Utility.ACTION_LOGIN.equalsIgnoreCase(getIntent().getStringExtra(Utility.Extra.INFO_TYPE))) {
                        callResendOTPWS();
                    }

                    break;
            }
        }
    };

    /**
     * Call Login WS Key Webservice
     */
    private void callVerifyOTPWS(String phoneNumber) {

        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(getString(R.string.no_internet), mActivityVerificationBinding.getRoot());
            return;
        }

        //Check if we are having proper userdetails
        if (mUserDetails == null) {
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityVerificationBinding.getRoot());
            return;
        }

        //Show Progress
        showProgressDialog();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, BuildConfig.X_API_KEY);

        String webService = NetworkUtility.WS.VERIFY_OTP;
        //Checking this condition because this screen opens from register time (There are no user details in PreferenceUtility) and change phone number from ProfileTabFragment (There is user detail in PreferenceUtils)
        if (PreferenceUtility.getInstance(mContext).getUserDetails() != null) {
            mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
            mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);
            webService = NetworkUtility.WS.VERIFY_OTP_CODE;
        }

        //Add Params
        Map<String, String> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.PHONE_NUMBER, phoneNumber);
        mParams.put(NetworkUtility.TAGS.OTP_CODE, fetchEnteredOTP());

        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(webService
                , mCallVerifyOTPCodeWSErrorListener
                , mCallVerifyOTPCodeResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(this).addToRequestQueue(mVolleyNetworkRequest);

    }

    /**
     * Listeners for tracking Webservice calls
     */
    Response.Listener mCallVerifyOTPCodeResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {

            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:

                        if (Utility.ACTION_CHANGE_PHONE_NUMBER.equalsIgnoreCase(getIntent().getStringExtra(Utility.Extra.INFO_TYPE))) {
                            //Save the User detail information
                            if (jsonObject.has(NetworkUtility.TAGS.DATA)) {
                                PreferenceUtility.getInstance(mContext).saveUserDetails(jsonObject.getJSONObject(NetworkUtility.TAGS.DATA));
                                PreferenceUtility.getInstance(mContext).setXAPIKey(jsonObject.getJSONObject(NetworkUtility.TAGS.DATA).getString(NetworkUtility.TAGS.WS_ACCESS_KEY));
                            }
                            finish();
                        } else if (Utility.ACTION_REGISTER.equalsIgnoreCase(getIntent().getStringExtra(Utility.Extra.INFO_TYPE))) {
                            proceedWithSignUp();
                        } else if (Utility.ACTION_LOGIN.equalsIgnoreCase(getIntent().getStringExtra(Utility.Extra.INFO_TYPE))) {
                            proceedWithLogin();
                        }

                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityVerificationBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        String error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mActivityVerificationBinding.getRoot());
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mCallVerifyOTPCodeWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
            hideProgressDialog();
        }
    };

    Response.ErrorListener mCallVerifyOTPCodeWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            hideProgressDialog();

            // Show Toast
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityVerificationBinding.getRoot());
        }
    };


    /**
     * Listener to change focus to next EditText
     */
    class MyTextWatcher implements TextWatcher {
        EditText editText;

        public MyTextWatcher(EditText editText) {
            this.editText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            int position = sequenceList.indexOf(editText);
            if (position < sequenceList.size() - 1 && charSequence.length() > 0) {

                sequenceList.get(position + 1).requestFocus();
            }
            editText.setSelected(charSequence.length() > 0);

        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    }

    /**
     * Method of Login Redirection
     */
    private void proceedWithLogin() {
        // Save the User detail information
        if (getIntent().getExtras().getString(Utility.Extra.USER_DETAILS) != null) {
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(getIntent().getExtras().getString(Utility.Extra.USER_DETAILS));
                PreferenceUtility.getInstance(mContext).saveUserDetails(jsonObject);
                PreferenceUtility.getInstance(mContext).setXAPIKey(jsonObject.getString(NetworkUtility.TAGS.WS_ACCESS_KEY));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (mUserDetails != null) {
                                /*
                                * Create new register user in fierbase
                                * @Sanjay 20 Feb 2016
                                * */
                ChatUserModel chatUserModel = new ChatUserModel();
                chatUserModel.setUserId(FirebaseUtils.getPrefixUserId(mUserDetails.UserID));
                chatUserModel.setUserName(mUserDetails.UserName);
                chatUserModel.setProfileImg(mUserDetails.ProfileImg);
                FirebaseHelper.getUsersRef(chatUserModel.getUserId()).setValue(chatUserModel);
                                /*
                                * Start fierbase chat service
                                * @Sanjay 20 Feb 2016
                                * */
                startService(new Intent(mContext, FierbaseChatService.class));
            }
            // redirect to Home Screen
//            HomeActivity.newInstance(mContext);
            sendBroadcast(new Intent(Utility.BR_ON_LOGIN_SUCCESS));
            finish();
        }
    }

    /**************************************************************************************************************
     * *************************************************************************************************************
     * *****************************************Webservice Integration [Start]**************************************
     * *************************************************************************************************************
     ************************************************************************************************************/
    private void proceedWithSignUp() {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(getString(R.string.no_internet), mActivityVerificationBinding.getRoot());
            return;
        }

        if (isValidate()) {
            callSignUpWS();
        }
    }


    /**
     * To validate all user entered fields
     */
    public boolean isValidate() {

        //Check if User entered all four digits or not
        if (fetchEnteredOTP().length() < 4) {
            Utility.showSnackBar(getString(R.string.validate_otp_empty), mActivityVerificationBinding.getRoot());
            return false;
        }

        //No Need to check otp locally as we are checking otp with webservice

        /*//Checking locally with webservice response otp
        if (!fetchEnteredOTP().equals(correctOTP)) {
            Utility.showSnackBar(getString(R.string.validate_otp_incorrect), mActivityVerificationBinding.getRoot());
            return false;
        }*/

        return true;
    }

    /**
     * Call Login WS Key Webservice
     */
    private void callSignUpWS() {

        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(getString(R.string.no_internet), mActivityVerificationBinding.getRoot());
            return;
        }

        //Check if we are having proper userdetails
        if (mUserDetails == null) {
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityVerificationBinding.getRoot());
            return;
        }

        //Show Progress
        showProgressDialog();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, BuildConfig.X_API_KEY);

        //Add Params
        Map<String, String> mParams = fetchKeyValuesForRegisteration();

        //Add File Params
        HashMap<String, File> mFileParams = new HashMap<>();
        if (selectedImagePath != null && (!TextUtils.isEmpty(selectedImagePath))) {
            mFileParams.put(NetworkUtility.TAGS.PROFILE_IMAGE, new File(selectedImagePath));
        }

        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.SIGNUP
                , mCallSignUpWSErrorListener
                , mCallSignUpWSResponseListener
                , mHeaderParams
                , mParams
                , mFileParams);
        Volley.getInstance(this).addToRequestQueue(mVolleyNetworkRequest);

    }

    /**
     * This method will return HashMap in <String,String>
     *
     * @return
     */
    private Map<String, String> fetchKeyValuesForRegisteration() {
        Map<String, String> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.LOGINWITH, mUserDetails.LoginWith);
        mParams.put(NetworkUtility.TAGS.USERNAME, mUserDetails.UserName);
        mParams.put(NetworkUtility.TAGS.EMAIL_ADDRESS, mUserDetails.Email);
        mParams.put(NetworkUtility.TAGS.PHONE_NUMBER, mUserDetails.PhoneNumber);

        if (!TextUtils.isEmpty(mUserDetails.fb_app_id)) {
            mParams.put(NetworkUtility.TAGS.FB_APP_ID, mUserDetails.fb_app_id);
        }
        if (!TextUtils.isEmpty(mUserDetails.tw_app_id)) {
            mParams.put(NetworkUtility.TAGS.TWITTER_APP_ID, mUserDetails.tw_app_id);
        }
        if (!TextUtils.isEmpty(mUserDetails.gp_app_id)) {
            mParams.put(NetworkUtility.TAGS.GOOGLE_PLUS_APP_ID, mUserDetails.gp_app_id);
        }

//        mParams.put(NetworkUtility.TAGS.PASSWORD, password);

        if (mLocationTrackService.mLocation != null && mLocationTrackService.mLocation.getLatitude() != 0 && mLocationTrackService.mLocation.getLongitude() != 0) {
            mParams.put(NetworkUtility.TAGS.LAT, String.valueOf(mLocationTrackService.mLocation.getLatitude()));
            mParams.put(NetworkUtility.TAGS.LNG, String.valueOf(mLocationTrackService.mLocation.getLongitude()));
        } else {
            mParams.put(NetworkUtility.TAGS.LAT, Utility.EMPTY_STRING);
            mParams.put(NetworkUtility.TAGS.LNG, Utility.EMPTY_STRING);
        }

        /*mParams.put(NetworkUtility.TAGS.LAT, mLocationTrackService.mLocation != null ? String.valueOf(mLocationTrackService.mLocation.getLatitude()) : BootstrapConstant.LAT);
        mParams.put(NetworkUtility.TAGS.LNG, mLocationTrackService.mLocation != null ? String.valueOf(mLocationTrackService.mLocation.getLongitude()) : BootstrapConstant.LNG);*/

        mParams.put(NetworkUtility.TAGS.PLATFORM, NetworkUtility.TAGS.PLATFORMTYPE.ANDROID);
        mParams.put(NetworkUtility.TAGS.DEVICE_TOKEN, PreferenceUtility.getInstance(mContext).getFCMRegID());

        return mParams;
    }

    /**
     * This method will return HashMap in <String,Object>
     *
     * @return
     */
    private Map<String, Object> fetchKeyValuesForRegisterationWithObject() {
        Map<String, Object> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.LOGINWITH, mUserDetails.LoginWith);
        mParams.put(NetworkUtility.TAGS.USERNAME, mUserDetails.UserName);
        mParams.put(NetworkUtility.TAGS.EMAIL_ADDRESS, mUserDetails.Email);
        mParams.put(NetworkUtility.TAGS.PHONE_NUMBER, mUserDetails.PhoneNumber);

        if (!TextUtils.isEmpty(mUserDetails.fb_app_id)) {
            mParams.put(NetworkUtility.TAGS.FB_APP_ID, mUserDetails.fb_app_id);
        }
        if (!TextUtils.isEmpty(mUserDetails.tw_app_id)) {
            mParams.put(NetworkUtility.TAGS.TWITTER_APP_ID, mUserDetails.tw_app_id);
        }
        if (!TextUtils.isEmpty(mUserDetails.gp_app_id)) {
            mParams.put(NetworkUtility.TAGS.GOOGLE_PLUS_APP_ID, mUserDetails.gp_app_id);
        }

//        mParams.put(NetworkUtility.TAGS.PASSWORD, password);

        if (mLocationTrackService.mLocation != null && mLocationTrackService.mLocation.getLatitude() != 0 && mLocationTrackService.mLocation.getLongitude() != 0) {
            mParams.put(NetworkUtility.TAGS.LAT, String.valueOf(mLocationTrackService.mLocation.getLatitude()));
            mParams.put(NetworkUtility.TAGS.LNG, String.valueOf(mLocationTrackService.mLocation.getLongitude()));
        } else {
            mParams.put(NetworkUtility.TAGS.LAT, Utility.EMPTY_STRING);
            mParams.put(NetworkUtility.TAGS.LNG, Utility.EMPTY_STRING);
        }

        /*mParams.put(NetworkUtility.TAGS.LAT, mLocationTrackService.mLocation != null ? String.valueOf(mLocationTrackService.mLocation.getLatitude()) : BootstrapConstant.LAT);
        mParams.put(NetworkUtility.TAGS.LNG, mLocationTrackService.mLocation != null ? String.valueOf(mLocationTrackService.mLocation.getLongitude()) : BootstrapConstant.LNG);*/

        mParams.put(NetworkUtility.TAGS.PLATFORM, NetworkUtility.TAGS.PLATFORMTYPE.ANDROID);
        mParams.put(NetworkUtility.TAGS.DEVICE_TOKEN, PreferenceUtility.getInstance(mContext).getFCMRegID());

        return mParams;
    }


    /**
     * Listeners for tracking Webservice calls
     */
    Response.Listener mCallSignUpWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {

            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:

                        //Save the User detail information
                        if (jsonObject.has(NetworkUtility.TAGS.DATA)) {
                            PreferenceUtility.getInstance(mContext).saveUserDetails(jsonObject.getJSONObject(NetworkUtility.TAGS.DATA));
                            PreferenceUtility.getInstance(mContext).setXAPIKey(jsonObject.getJSONObject(NetworkUtility.TAGS.DATA).getString(NetworkUtility.TAGS.WS_ACCESS_KEY));

                            UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
                            if (userDetails != null) {
                                /*
                                * Create new register user in fierbase
                                * @Sanjay 20 Feb 2016
                                * */
                                ChatUserModel chatUserModel = new ChatUserModel();
                                chatUserModel.setUserId(FirebaseUtils.getPrefixUserId(userDetails.UserID));
                                chatUserModel.setUserName(userDetails.UserName);
                                chatUserModel.setProfileImg(userDetails.ProfileImg);
                                FirebaseHelper.getUsersRef(chatUserModel.getUserId()).setValue(chatUserModel);

                                /*
                                * Start fierbase chat service
                                * @Sanjay 20 Feb 2016
                                * */
                                startService(new Intent(VerificationActivity.this, FierbaseChatService.class));
                            }

                            // Send Custom AppsFlyer Event Tracking
                            if (NetworkUtility.TAGS.LOGINWITHTYPE.MOBILE.equals(mUserDetails.LoginWith)) {
//                                AppsFlyerLib.getInstance().set
                                AppsFlyerLib.getInstance().trackEvent(mContext
                                        , NetworkUtility.TAGS.APPSFLYER_CUSTOM_TRACK_EVENTS.REG_MOBILE
                                        , fetchKeyValuesForRegisterationWithObject());
                            } else if (NetworkUtility.TAGS.LOGINWITHTYPE.FACEBOOK.equals(mUserDetails.LoginWith)) {
                                AppsFlyerLib.getInstance().trackEvent(mContext
                                        , NetworkUtility.TAGS.APPSFLYER_CUSTOM_TRACK_EVENTS.REG_FB
                                        , fetchKeyValuesForRegisterationWithObject());
                            } else if (NetworkUtility.TAGS.LOGINWITHTYPE.GOOGLEPLUS.equals(mUserDetails.LoginWith)) {
                                AppsFlyerLib.getInstance().trackEvent(mContext
                                        , NetworkUtility.TAGS.APPSFLYER_CUSTOM_TRACK_EVENTS.REG_GOOGLE
                                        , fetchKeyValuesForRegisterationWithObject());
                            } else if (NetworkUtility.TAGS.LOGINWITHTYPE.TWITTER.equals(mUserDetails.LoginWith)) {
                                AppsFlyerLib.getInstance().trackEvent(mContext
                                        , NetworkUtility.TAGS.APPSFLYER_CUSTOM_TRACK_EVENTS.REG_TWITTER
                                        , fetchKeyValuesForRegisterationWithObject());
                            }
                        }

                        // Send Broadcast
                        sendBroadcast(new Intent(Utility.BR_ON_LOGIN_SUCCESS));
                        finish();

                        /*new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // Send Broadcast
                                sendBroadcast(new Intent(Utility.BR_ON_LOGIN_SUCCESS));
                                finish();
                            }
                        }, 1500);*/

//                        Utility.showToast(mContext, getString(R.string.label_welcome_message));
                        /*String message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show Toast
                        Utility.showToast(mContext, message);

                        //Redirect User to Login Activity
                        LoginActivity.newInstance(mContext);*/
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityVerificationBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        String error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mActivityVerificationBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.SIGNUP_REQUIRED:
//                        redirectUserToSignUp(TEMP_EMAIL, TEMP_NAME, TEMP_LOGIN_WITH);
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mCallSignUpWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
            hideProgressDialog();
        }
    };

    Response.ErrorListener mCallSignUpWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            hideProgressDialog();

            // Show Toast
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityVerificationBinding.getRoot());
        }
    };

    /**
     * Call Verify OTP WS Key Webservice
     */
    private void callResendOTPWSForChangePassword(String phoneNumber) {

        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(getString(R.string.no_internet), mActivityVerificationBinding.getRoot());
            return;
        }

        //Check if we are having proper userdetails
        if (mUserDetails == null) {
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityVerificationBinding.getRoot());
            return;
        }

        //Show Progress
        showProgressDialog();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);

        //Add Params
        Map<String, String> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.PHONE_NUMBER, phoneNumber);

        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.EDIT_PHONE_NUMBER
                , mCallReSendOTPWSErrorListener
                , mCallReSendOTPWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(this).addToRequestQueue(mVolleyNetworkRequest);

    }

    @Override
    protected void onDestroy() {
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.SEND_OTP);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.EDIT_PHONE_NUMBER);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.SIGNUP);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.VERIFY_OTP);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.VERIFY_OTP_CODE);

        super.onDestroy();
    }

    /**
     * Call Verify OTP WS Key Webservice
     */
    private void callResendOTPWS() {

        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(getString(R.string.no_internet), mActivityVerificationBinding.getRoot());
            return;
        }

        //Check if we are having proper userdetails
        if (mUserDetails == null) {
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityVerificationBinding.getRoot());
            return;
        }

        //Show Progress
        showProgressDialog();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, BuildConfig.X_API_KEY);

        //Add Params
        Map<String, String> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.EMAIL_ADDRESS, mUserDetails.Email);
        mParams.put(NetworkUtility.TAGS.PHONE_NUMBER, mUserDetails.PhoneNumber);

        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.SEND_OTP
                , mCallReSendOTPWSErrorListener
                , mCallReSendOTPWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(this).addToRequestQueue(mVolleyNetworkRequest);

    }

    /**
     * Listeners for tracking Webservice calls
     */
    Response.Listener mCallReSendOTPWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {

            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
//                        Change the Update OTP as well
//                        correctOTP = jsonObject.getString(NetworkUtility.TAGS.OTP_CODE);
                        String messasge = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(messasge, mActivityVerificationBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityVerificationBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        String error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mActivityVerificationBinding.getRoot());
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mCallReSendOTPWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
            hideProgressDialog();
        }
    };

    Response.ErrorListener mCallReSendOTPWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            hideProgressDialog();

            // Show Toast
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityVerificationBinding.getRoot());
        }
    };

    /**************************************************************************************************************
     * *************************************************************************************************************
     * *******************************************Webservice Integration [End]**************************************
     * **************************************************************************************************************
     ************************************************************************************************************/
    private String fetchEnteredOTP() {
        String stringBuilder = mActivityVerificationBinding.editOtp1.getText().toString() +
                mActivityVerificationBinding.editOtp2.getText().toString() +
                mActivityVerificationBinding.editOtp3.getText().toString() +
                mActivityVerificationBinding.editOtp4.getText().toString();
        return stringBuilder;
    }
}
