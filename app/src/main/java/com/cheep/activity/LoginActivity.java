package com.cheep.activity;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.BuildConfig;
import com.cheep.R;
import com.cheep.databinding.ActivityLoginBinding;
import com.cheep.facebook.FacebookHelper;
import com.cheep.firebase.FierbaseChatService;
import com.cheep.firebase.FirebaseHelper;
import com.cheep.firebase.FirebaseUtils;
import com.cheep.firebase.model.ChatUserModel;
import com.cheep.fragment.BaseFragment;
import com.cheep.fragment.InfoFragment;
import com.cheep.interfaces.DrawerLayoutInteractionListener;
import com.cheep.model.LocationInfo;
import com.cheep.model.UserDetails;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.FetchLocationInfoUtility;
import com.cheep.utils.GsonUtility;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by Bhavesh Patadiya on 9/26/16.
 */
public class LoginActivity extends BaseAppCompatActivity implements
        DrawerLayoutInteractionListener,
        FacebookHelper.FacebookCallbacks {
    private static final String TAG = LoginActivity.class.getSimpleName();
    private ActivityLoginBinding mActivityLoginBinding;

    //Temporary variables for storing email and Username in case of social media signup
    private String TEMP_EMAIL;
    private String TEMP_NAME;
    private String TEMP_LOGIN_WITH;
    private String TEMP_PHONE_NUMBER;
    private String TEMP_FB_APP_ID;
    private String TEMP_TWITTER_APP_ID;
    private String TEMP_GOOGLE_PLUS_APP_ID;
    private SharedPreferences mSharedPreferences;

    // Twitter
    TwitterAuthClient mTwitterAuthClient;

    // Facebook CallbackManager
    CallbackManager mCallbackManager;
    FacebookHelper mFacebookHelper;

    // GoogleAPIclient for using
    GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 9001;

    // Location Info
    private LocationInfo mSelectedLocationInfo;
    private boolean isDesclaimerEnabled = false;

    public static void newInstance(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityLoginBinding = DataBindingUtil.setContentView(this, R.layout.activity_login);

        initiateUI();
        setListeners();

        configureGooglePlusSignIn();

        //Showing expire dialog based on parameters
        if (getIntent().getBooleanExtra(Utility.Extra.SESSION_EXPIRE, false)) {
            showSessionExpireDialog();
        }


        // Register Broadcast app
        registerReceiver(mBR_OnLoginSuccess, new IntentFilter(Utility.BR_ON_LOGIN_SUCCESS));
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called");

        checkVersionOfApp();

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() called");
        hideProgressDialog();
    }


    @Override
    protected void initiateUI() {
        getWindow().setBackgroundDrawableResource(R.drawable.login_bg_blur);

        //Setting toolbar
        setSupportActionBar(mActivityLoginBinding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(Utility.EMPTY_STRING);
            mActivityLoginBinding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Utility.hideKeyboard(mContext);
                    onBackPressed();
                }
            });
        }

        setTermAndCondition();

    }

    @Override
    protected void setListeners() {
        mActivityLoginBinding.imgLoginSubmit.setOnClickListener(onClickListener);
//        mActivityLoginBinding.layoutSignup.setOnClickListener(onClickListener);
//        mActivityLoginBinding.textForgotPassword.setOnClickListener(onClickListener);

        //Twitter Button Login
        mActivityLoginBinding.ivTwitter.setOnClickListener(onClickListener);

        //Facebook Button Login
        mActivityLoginBinding.ivFb.setOnClickListener(onClickListener);

        //Google SignInButton
        mActivityLoginBinding.ivGplus.setOnClickListener(onClickListener);
    }


    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                /*case R.id.btn_sign_in:
                    onClickOnSignIn();
                    break;*/
                case R.id.img_login_submit:
                    onClickOnSignIn();
                    break;
//                case R.id.layout_signup:
//                    redirectUserToSignUp(null, null, NetworkUtility.TAGS.LOGINWITHTYPE.EMAIL);
//                    break;
               /* case R.id.text_forgot_password:
//                    showForgotPasswordDialog();
                    break;*/
                case R.id.iv_twitter:
                    onClickOfTwitter();
                    break;
                case R.id.iv_fb:
                    onClickOfFacebook();
                    break;
                case R.id.iv_gplus:
                    onClickOfGoogle();
                    break;

            }
        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleGoogleSignInResult(result);
        }

        if (requestCode == Utility.REQUEST_CODE_CHECK_LOCATION_SETTINGS) {
            if (resultCode == RESULT_OK) {
                mLocationTrackService.requestLocationUpdate();
            }
        }

        //Calling below is the key to get callback from Twitter API, Facebook API
        if (mTwitterAuthClient != null)
            mTwitterAuthClient.onActivityResult(requestCode, resultCode, data);

        //For Facebook
        if (mCallbackManager != null)
            mCallbackManager.onActivityResult(requestCode, resultCode, data);


    }

    @Override
    protected void onLocationNotAvailable() {
        Log.d(TAG, "onLocationNotAvailable() called");
    }

    @Override
    protected void onLocationFetched(Location mLocation) {
        Log.d(TAG, "onLocationFetched() called with: mLocation = [" + mLocation + "]");
    }


    @Override
    public void onLocationSettingsDialogNeedToBeShow(Status status) {
        super.onLocationSettingsDialogNeedToBeShow(status);
        // Location settings are not satisfied, but this can be fixed
        // by showing the user a dialog.
        try {
            // Show the dialog by calling startResolutionForResult(),
            // and check the result in onActivityResult().
            status.startResolutionForResult(this, Utility.REQUEST_CODE_CHECK_LOCATION_SETTINGS);
        } catch (IntentSender.SendIntentException e) {
            // Ignore the error.
        }
    }

    @Override
    public void onBindLocationTrackService() {
        Log.i(TAG, "onBindLocationTrackService: ");
        /*
          Check if Location service is enabled or not, if not ask for user to accept it and stop the ongoing service
         */
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Let the activity know that location permission not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION) &&
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, Utility.REQUEST_CODE_PERMISSION_LOCATION);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, Utility.REQUEST_CODE_PERMISSION_LOCATION);
            }
        } else {
            requestLocationUpdateFromService();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Utility.REQUEST_CODE_PERMISSION_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED ) {
                Log.i(TAG, "onRequestPermissionsResult: Permission Granted");
                //So, ask service to fetch the location now
                requestLocationUpdateFromService();
            } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Log.i(TAG, "onRequestPermissionsResult: Permission Denied");
                Snackbar.make(mActivityLoginBinding.getRoot(), getString(R.string.permission_denied_location), 3000).show();
            }
        }
    }
    /**
     * Logout Confirmation Dialog
     */
    private void showSessionExpireDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
        builder.setCancelable(false);
        builder.setTitle(getString(R.string.label_force_logout));
        if (getIntent().getIntExtra(Utility.Extra.ACTION, NetworkUtility.TAGS.STATUSCODETYPE.FORCE_LOGOUT_REQUIRED) == NetworkUtility.TAGS.STATUSCODETYPE.USER_DELETED) {
            builder.setMessage(getString(R.string.desc_user_deleted));
        } else {
            builder.setMessage(getString(R.string.desc_force_logout));
        }
        builder.setPositiveButton(getString(R.string.label_Ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d(TAG, "onClick() called with: dialogInterface = [" + dialogInterface + "], i = [" + i + "]");
            }
        });
        builder.show();

    }
    public void loadFragment(String tag, BaseFragment baseFragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.content, baseFragment, tag).addToBackStack(InfoFragment.TAG).commitAllowingStateLoss();
    }

    private void setTermAndCondition(){
        SpannableStringBuilder mSpannableStringBuilder = new SpannableStringBuilder(getString(R.string.terms_and_condition));

        mActivityLoginBinding.tvDesclaimer.setText(mSpannableStringBuilder);
        mActivityLoginBinding.tvDesclaimer.setMovementMethod(LinkMovementMethod.getInstance());

        mActivityLoginBinding.tvDesclaimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment mFragment = getSupportFragmentManager().findFragmentByTag(InfoFragment.TAG + "_" + NetworkUtility.TAGS.PAGEID_TYPE.TERMS);
                if (mFragment == null) {
                    loadFragment(InfoFragment.TAG + "_" + NetworkUtility.TAGS.PAGEID_TYPE.TERMS, InfoFragment.newInstance(NetworkUtility.TAGS.PAGEID_TYPE.TERMS));
                }
            }
        });


        mActivityLoginBinding.imgCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDesclaimerEnabled = !isDesclaimerEnabled;
                enableDesclaimerCheckMark(isDesclaimerEnabled);
            }
        });
    }
    private void enableDesclaimerCheckMark(boolean flag) {
        if (flag) {
            mActivityLoginBinding.imgCheckbox.setImageResource(R.drawable.ic_checkbox_icon_checked);
            mActivityLoginBinding.imgCheckbox.setSelected(flag);
        } else {
            mActivityLoginBinding.imgCheckbox.setImageResource(R.drawable.ic_checkbox_icon_unchecked);
            mActivityLoginBinding.imgCheckbox.setSelected(flag);
        }
    }


    //DrawerLayoutInteractionListener,
    @Override
    public void setUpDrawerLayoutWithToolBar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(Utility.EMPTY_STRING);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Utility.hideKeyboard(mContext);
                    if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                        getSupportFragmentManager().popBackStack();
                    }else {
                        onBackPressed();
                    }
                }
            });
        }
    }

    @Override
    public void profileUpdated() {

    }



    /**************************************************************************************************************
     * *****************************************Facebook SignIn[Start]*******************************************************
     ************************************************************************************************************/
    private void onClickOfFacebook() {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityLoginBinding.getRoot());
            return;
        }
        showProgressDialog();
        mCallbackManager = CallbackManager.Factory.create();
        mFacebookHelper = new FacebookHelper(mContext, mCallbackManager);
        mFacebookHelper.login();
    }

    /**
     * Facebook Login Callbacks
     *
     * @param loginResult :returned the logged in user details
     */
    @Override
    public void onFBLoginSuccessCalled(LoginResult loginResult) {
        Log.d(TAG, "onFBLoginSuccessCalled() called with: loginResult = [" + loginResult + "]");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                fetchUserInfo();
            }
        }, 500);
    }

    /**
     * Called if Facebool Login cancelled
     */
    @Override
    public void onFBLoginCancelledCalled() {
        Log.d(TAG, "onFBLoginCancelledCalled() called");
        hideProgressDialog();
    }

    /**
     * Called when facebook login completed with an error
     *
     * @param e : error message
     */
    @Override
    public void onFBLoginonErrorCalled(FacebookException e) {
        Log.d(TAG, "onFBLoginonErrorCalled() called with: e = [" + e.toString() + "]");
        hideProgressDialog();
        Utility.showSnackBar(e.getMessage(), mActivityLoginBinding.getRoot());
    }

    private void fetchUserInfo() {
        final AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken != null) {
            GraphRequest request = GraphRequest.newMeRequest(
                    accessToken, new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(JSONObject me, GraphResponse response) {
                            Log.i(TAG, "Facebook-User Details : " + me);
                            //HideProgress
                            hideProgressDialog();

                            //Check if we found a valid email address from Facebook, if not NOT let user to go ahaed by showing snackbar
                            if (!me.has(Utility.FACEBOOK_EMAIL_KEY)) {
//                                Utility.showSnackBar(getString(R.string.social_email_not_found), mActivityLoginBinding.getRoot());
                                Toast.makeText(mContext, getString(R.string.social_email_not_found), Toast.LENGTH_LONG).show();
                                return;
                            }

                            //Set temporary address
                            TEMP_EMAIL = me.optString(Utility.FACEBOOK_EMAIL_KEY);
                            TEMP_NAME = me.optString(Utility.FACEBOOK_NAME_KEY);
                            TEMP_PHONE_NUMBER = Utility.EMPTY_STRING;
                            TEMP_LOGIN_WITH = NetworkUtility.TAGS.LOGINWITHTYPE.FACEBOOK;
                            TEMP_FB_APP_ID = me.optString("id");
                            TEMP_TWITTER_APP_ID = Utility.EMPTY_STRING;
                            TEMP_GOOGLE_PLUS_APP_ID = Utility.EMPTY_STRING;
                            callLoginWS(NetworkUtility.TAGS.LOGINWITHTYPE.FACEBOOK, me.optString("id"));

                            //Logout from Facebook Now
                            mFacebookHelper.logout();
                        }
                    });
            Bundle bundle = new Bundle();
            bundle.putString(Utility.FACEBOOK_FIELDS_KEY, (Utility.FACEBOOK_EMAIL_KEY + Utility.COMMA + Utility.FACEBOOK_NAME_KEY));
            request.setParameters(bundle);
            GraphRequest.executeBatchAsync(request);
        }
    }
    /*************************************************************************************************************
     *****************************************Facebook SignIn[End]*******************************************************
     */


    /**************************************************************************************************************
     * *****************************************Twitter SignIn[Start]*******************************************************
     ************************************************************************************************************/

    private void onClickOfTwitter() {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityLoginBinding.getRoot());
            return;
        }

        showProgressDialog();
        mTwitterAuthClient = new TwitterAuthClient();
        mTwitterAuthClient.authorize((Activity) mContext, mTwitterSessionCallback);
    }

    /**
     * Twitter Callback
     */
    private Callback<TwitterSession> mTwitterSessionCallback = new Callback<TwitterSession>() {
        @Override
        public void success(final Result<TwitterSession> result) {
            Log.d(TAG, "success() called with: result = [" + result.data.getUserName() + "]");
            /*
              This will request added permission
             */
            TwitterAuthClient authClient = new TwitterAuthClient();
            authClient.requestEmail(result.data, new Callback<String>() {
                @Override
                public void success(Result<String> emailResult) {
                    Log.d(TAG, "success() called with: result FetchEmail= [" + emailResult.data + "]");
                    hideProgressDialog();

                    // Do something with the result, which provides the email address
                    TEMP_EMAIL = emailResult.data;
                    TEMP_NAME = result.data.getUserName();
                    TEMP_PHONE_NUMBER = Utility.EMPTY_STRING;
                    TEMP_LOGIN_WITH = NetworkUtility.TAGS.LOGINWITHTYPE.TWITTER;
                    TEMP_FB_APP_ID = Utility.EMPTY_STRING;
                    TEMP_TWITTER_APP_ID = String.valueOf(result.data.getUserId());
                    TEMP_GOOGLE_PLUS_APP_ID = Utility.EMPTY_STRING;

                    callLoginWS(NetworkUtility.TAGS.LOGINWITHTYPE.TWITTER, String.valueOf(result.data.getUserId()));
                }

                @Override
                public void failure(TwitterException exception) {
                    Log.d(TAG, "failure() called with: exception FetchEmail= [" + exception + "]");

                    // Do something on failure
                    hideProgressDialog();

                    //We couldnt able to fetch valid email address so, display proper message to user.
//                    Utility.showSnackBar(getString(R.string.social_email_not_found), mActivityLoginBinding.getRoot());
                    Toast.makeText(mContext, getString(R.string.social_email_not_found), Toast.LENGTH_LONG).show();
                }
            });
        }

        @Override
        public void failure(TwitterException exception) {
            hideProgressDialog();
            Log.d(TAG, "failure() called with: exception = [" + exception.toString() + "]");
        }
    };

    /*************************************************************************************************************
     Twitter SignIn[End]*******************************************************
     */

    /**************************************************************************************************************
     * ******************************************Google SignIn[Start]*******************************************************
     ************************************************************************************************************/
    private void onClickOfGoogle() {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityLoginBinding.getRoot());
            return;
        }
        showProgressDialog();
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void configureGooglePlusSignIn() {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, mOnConnectionFailedListener /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        Log.d(TAG, "onConnected() called with: bundle = [" + bundle + "]");
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Log.d(TAG, "onConnectionSuspended() called with: i = [" + i + "]");
                    }
                })
                .build();
    }

    private GoogleApiClient.OnConnectionFailedListener mOnConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

            Log.d(TAG, "onConnectionFailed() called with: connectionResult = [" + connectionResult + "]");

        }
    };

    private void handleGoogleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        hideProgressDialog();
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            if (acct != null) {
                Log.i(TAG, "handleGoogleSignInResult: Display Name" + acct.getDisplayName() + " ,email: " + acct.getEmail());
                //Now Signout from google
                signOutFromGoogle();

                //Set temporary address
                TEMP_LOGIN_WITH = NetworkUtility.TAGS.LOGINWITHTYPE.GOOGLEPLUS;
                TEMP_PHONE_NUMBER = Utility.EMPTY_STRING;
                TEMP_EMAIL = acct.getEmail();
                TEMP_NAME = acct.getDisplayName();
                TEMP_FB_APP_ID = Utility.EMPTY_STRING;
                TEMP_TWITTER_APP_ID = Utility.EMPTY_STRING;
                TEMP_GOOGLE_PLUS_APP_ID = acct.getId();
                //Call Login Webservice now.
                callLoginWS(NetworkUtility.TAGS.LOGINWITHTYPE.GOOGLEPLUS, acct.getId());
            }
        }

    }

    private void signOutFromGoogle() {
        Log.d(TAG, "signOutFromGoogle() called");
        if (!mGoogleApiClient.isConnected()) {
            return;
        }
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                Log.d(TAG, "onResult() signOutFromGoogle called with: status = [" + status + "]");
            }
        });
    }

    private void revokeAccessFromGoogle() {
        if (!mGoogleApiClient.isConnected()) {
            return;
        }
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        Log.d(TAG, "onResult() revokeAccessFromGoogle called with: status = [" + status + "]");
                    }
                });
    }
    /*************************************************************************************************************
     *****************************************Google SignIn[End]*******************************************************
     */


    /**************************************************************************************************************
     * *************************************************************************************************************
     * *****************************************Webservice Integration [Start]**************************************
     * *************************************************************************************************************
     ************************************************************************************************************/
    private void onClickOnSignIn() {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityLoginBinding.getRoot());
            return;
        }

        if (isValidate()) {
            TEMP_LOGIN_WITH = NetworkUtility.TAGS.LOGINWITHTYPE.MOBILE;
            TEMP_PHONE_NUMBER = mActivityLoginBinding.editUserMobileNumber.getText().toString().trim();
            TEMP_EMAIL = Utility.EMPTY_STRING;
            TEMP_NAME = Utility.EMPTY_STRING;
            TEMP_FB_APP_ID = Utility.EMPTY_STRING;
            TEMP_TWITTER_APP_ID = Utility.EMPTY_STRING;
            TEMP_GOOGLE_PLUS_APP_ID = Utility.EMPTY_STRING;
            //hidding keyboard
            Utility.hideKeyboard(mContext);
            /*Utility.hideKeyboard(mContext, mActivityLoginBinding.editUsername);
            Utility.hideKeyboard(mContext, mActivityLoginBinding.editPassword);*/
            callLoginWS(NetworkUtility.TAGS.LOGINWITHTYPE.MOBILE
                    , mActivityLoginBinding.editUserMobileNumber.getText().toString().trim());
        }
    }

    /**
     * To validate all user entered fields
     */
    public boolean isValidate() {

       /* //email address
        if (TextUtils.isEmpty(mActivityLoginBinding.editUsername.getText())) {
            Utility.showSnackBar(getString(R.string.validate_empty_email), mActivityLoginBinding.getRoot());
            return false;
        }

        //Check for valid email address
        if (!Utility.isValidEmail(mActivityLoginBinding.editUsername.getText().toString())) {
            Utility.showSnackBar(getString(R.string.validate_pattern_email), mActivityLoginBinding.getRoot());
            return false;
        }

        // Password
        if (TextUtils.isEmpty(mActivityLoginBinding.editPassword.getText())) {
            Utility.showSnackBar(getString(R.string.validate_empty_password), mActivityLoginBinding.getRoot());
            return false;
        }

        //Length of password Feild must be atleast 6 characters
        if (mActivityLoginBinding.editPassword.getText().length() < Utility.PASSWORD_MIN_LENGTH) {
            Utility.showSnackBar(getString(R.string.validate_password_length), mActivityLoginBinding.getRoot());
            return false;
        }*/

        if (TextUtils.isEmpty(mActivityLoginBinding.editUserMobileNumber.getText())) {
            Utility.showSnackBar(getString(R.string.validate_phone_number), mActivityLoginBinding.getRoot());
            return false;
        }

        //Length of phone number must bhi 10 in length
        if (!Utility.isValidPhoneNumber(mActivityLoginBinding.editUserMobileNumber.getText().toString().trim())) {
            Utility.showSnackBar(getString(R.string.validate_phone_number_length), mActivityLoginBinding.getRoot());
            return false;
        }

        if(!isDesclaimerEnabled){
            Utility.hideKeyboard(mContext);
            Utility.showSnackBarWithTextCenter(getString(R.string.validate_terms), mActivityLoginBinding.getRoot());
            return false;
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.LOGIN);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.FORG0T_PASSWORD);
        try {
            unregisterReceiver(mBR_OnLoginSuccess);
        } catch (Exception e) {

        }
        super.onDestroy();
    }

    /**
     * Call Login WS Key Webservice
     */
    private void callLoginWS(final String loginWithTag, final String extraInfoBasedOnTag) {

        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityLoginBinding.getRoot());
            return;
        }

        //Show Progress
        showProgressDialog();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, BuildConfig.X_API_KEY);

        //Add Params
        Map<String, String> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.LOGINWITH, loginWithTag);
        if (NetworkUtility.TAGS.LOGINWITHTYPE.MOBILE.equals(loginWithTag)) {
            mParams.put(NetworkUtility.TAGS.PHONE_NUMBER, extraInfoBasedOnTag);
        } else if (NetworkUtility.TAGS.LOGINWITHTYPE.FACEBOOK.equals(loginWithTag)) {
            mParams.put(NetworkUtility.TAGS.FB_APP_ID, extraInfoBasedOnTag);
        } else if (NetworkUtility.TAGS.LOGINWITHTYPE.TWITTER.equals(loginWithTag)) {
            mParams.put(NetworkUtility.TAGS.TWITTER_APP_ID, extraInfoBasedOnTag);
        } else if (NetworkUtility.TAGS.LOGINWITHTYPE.GOOGLEPLUS.equals(loginWithTag)) {
            mParams.put(NetworkUtility.TAGS.GOOGLE_PLUS_APP_ID, extraInfoBasedOnTag);
        }

        if (mLocationTrackService != null && mLocationTrackService.mLocation != null && mLocationTrackService.mLocation.getLatitude() != 0 && mLocationTrackService.mLocation.getLongitude() != 0) {
            if (mSelectedLocationInfo == null) {
                //Location found so first fetch information and then go ahead.
                FetchLocationInfoUtility mFetchLocationInfoUtility = new FetchLocationInfoUtility(mContext,
                        new FetchLocationInfoUtility.FetchLocationInfoCallBack() {
                            @Override
                            public void onLocationInfoAvailable(LocationInfo mLocationIno) {
                                mSelectedLocationInfo = mLocationIno;
                                callLoginWS(loginWithTag, extraInfoBasedOnTag);
                            }

                            @Override
                            public void internetConnectionNotFound() {
                                Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityLoginBinding.getRoot());
                            }
                        },
                        false);
                mFetchLocationInfoUtility.getLocationInfo(String.valueOf(mLocationTrackService.mLocation.getLatitude()), String.valueOf(mLocationTrackService.mLocation.getLongitude()));
                return;
            } else {
                mParams.put(NetworkUtility.TAGS.LAT, mSelectedLocationInfo.lat);
                mParams.put(NetworkUtility.TAGS.LNG, mSelectedLocationInfo.lng);
                mParams.put(NetworkUtility.TAGS.COUNTRY, mSelectedLocationInfo.Country);
                mParams.put(NetworkUtility.TAGS.STATE, mSelectedLocationInfo.State);
                mParams.put(NetworkUtility.TAGS.CITY_NAME, mSelectedLocationInfo.City);
                mParams.put(NetworkUtility.TAGS.LOCALITY, TextUtils.isEmpty(mSelectedLocationInfo.Locality) ? Utility.EMPTY_STRING : mSelectedLocationInfo.Locality);
            }
        } else {
            mParams.put(NetworkUtility.TAGS.LAT, Utility.EMPTY_STRING);
            mParams.put(NetworkUtility.TAGS.LNG, Utility.EMPTY_STRING);
        }

        mParams.put(NetworkUtility.TAGS.PLATFORM, NetworkUtility.TAGS.PLATFORMTYPE.ANDROID);
        mParams.put(NetworkUtility.TAGS.DEVICE_TOKEN, PreferenceUtility.getInstance(mContext).getFCMRegID());

        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.LOGIN
                , mCallLoginWSErrorListener
                , mCallLoginWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(this).addToRequestQueue(mVolleyNetworkRequest, NetworkUtility.WS.LOGIN);

    }

    /**
     * Listeners for tracking Webservice calls
     */
    Response.Listener mCallLoginWSResponseListener = new Response.Listener()
    {
        @Override
        public void onResponse(Object response) {

            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:

                        final UserDetails userDetails = (UserDetails) GsonUtility.getObjectFromJsonString(jsonObject.getJSONObject(NetworkUtility.TAGS.DATA).toString(), UserDetails.class);

                        // If User is trying to login through MOBILE, App needs to verify Mobile number before going to Homescreen,
                        // Rest of the case we can directly redirect the user to login screen
                        if (NetworkUtility.TAGS.LOGINWITHTYPE.MOBILE.equals(userDetails.loginWith)) {
                            //Redirect user to Home Screen
                            VerificationActivity.newInstance(mContext, jsonObject.getJSONObject(NetworkUtility.TAGS.DATA).toString(), Utility.ACTION_LOGIN);
                        } else {
                            // Save the User detail information
                            if (jsonObject.has(NetworkUtility.TAGS.DATA)) {
                                PreferenceUtility.getInstance(mContext).saveUserDetails(jsonObject.getJSONObject(NetworkUtility.TAGS.DATA));
                                PreferenceUtility.getInstance(mContext).setXAPIKey(jsonObject.getJSONObject(NetworkUtility.TAGS.DATA).getString(NetworkUtility.TAGS.WS_ACCESS_KEY));
                                if (userDetails != null) {
                                /*
                                * Create new register user in fierbase
                                * @Sanjay 20 Feb 2016
                                * */
                                    ChatUserModel chatUserModel = new ChatUserModel();
                                    chatUserModel.setUserId(FirebaseUtils.getPrefixUserId(userDetails.userID));
                                    chatUserModel.setUserName(userDetails.userName);
                                    chatUserModel.setProfileImg(userDetails.profileImg);
                                    FirebaseHelper.getUsersRef(chatUserModel.getUserId()).setValue(chatUserModel);

                                /*
                                * Start fierbase chat service
                                * @Sanjay 20 Feb 2016
                                * */
                                    startService(new Intent(LoginActivity.this, FierbaseChatService.class));
                                }

                                // Send Broadcast
                                sendBroadcast(new Intent(Utility.BR_ON_LOGIN_SUCCESS));

                                // Finish the activity
                                finish();

//                                HomeActivity.newInstance(mContext);
//                                finish();
                            }
                        }
                        // PreferenceUtility.REFER_CODE = jsonObject.getJSONObject(NetworkUtility.TAGS.DATA).getString(NetworkUtility.TAGS.REFER_CODE);
                        //PreferenceUtility.getInstance(mContext).setReferenceCode(jsonObject.getJSONObject(NetworkUtility.TAGS.DATA).getString(NetworkUtility.TAGS.REFER_CODE));
                        //Log.e(TAG, jsonObject.getJSONObject(NetworkUtility.TAGS.DATA).getString(NetworkUtility.TAGS.REFER_CODE) );
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityLoginBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        String error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mActivityLoginBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.SIGNUP_REQUIRED:
                        switch (TEMP_LOGIN_WITH) {
                            case NetworkUtility.TAGS.LOGINWITHTYPE.MOBILE:
                                redirectUserToSignUp(TEMP_EMAIL,
                                        TEMP_NAME,
                                        TEMP_LOGIN_WITH,
                                        jsonObject.getString(NetworkUtility.TAGS.PHONE_NUMBER),
                                        Utility.EMPTY_STRING,
                                        Utility.EMPTY_STRING,
                                        Utility.EMPTY_STRING);
                                break;
                            case NetworkUtility.TAGS.LOGINWITHTYPE.FACEBOOK:
                                redirectUserToSignUp(TEMP_EMAIL,
                                        TEMP_NAME,
                                        TEMP_LOGIN_WITH,
                                        Utility.EMPTY_STRING,
                                        TEMP_FB_APP_ID,
                                        Utility.EMPTY_STRING,
                                        Utility.EMPTY_STRING);
                                break;
                            case NetworkUtility.TAGS.LOGINWITHTYPE.TWITTER:
                                redirectUserToSignUp(TEMP_EMAIL,
                                        TEMP_NAME,
                                        TEMP_LOGIN_WITH,
                                        Utility.EMPTY_STRING,
                                        Utility.EMPTY_STRING,
                                        TEMP_TWITTER_APP_ID,
                                        Utility.EMPTY_STRING);
                                break;
                            case NetworkUtility.TAGS.LOGINWITHTYPE.GOOGLEPLUS:
                                redirectUserToSignUp(TEMP_EMAIL,
                                        TEMP_NAME,
                                        TEMP_LOGIN_WITH,
                                        Utility.EMPTY_STRING,
                                        Utility.EMPTY_STRING,
                                        Utility.EMPTY_STRING,
                                        TEMP_GOOGLE_PLUS_APP_ID);
                                break;
                        }
//                        if (!TEMP_LOGIN_WITH.equals(NetworkUtility.TAGS.LOGINWITHTYPE.MOBILE))
//                        else
//                            Utility.showSnackBar(message, mActivityLoginBinding.getRoot());

                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mCallLoginWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
            hideProgressDialog();
        }
    };

    private void redirectUserToSignUp(String email, String name, String loginwith, String phoneNumber, String FB_APP_ID, String TWITTER_APP_ID, String GOOGLE_PLUS_APP_ID) {
        // Go to SignUp Activity with the fetched details
        UserDetails mUserDetails = new UserDetails();
        mUserDetails.email = email;
        mUserDetails.userName = name;
        mUserDetails.loginWith = loginwith;
        mUserDetails.phoneNumber = phoneNumber;
        mUserDetails.fb_app_id = FB_APP_ID;
        mUserDetails.tw_app_id = TWITTER_APP_ID;
        mUserDetails.gp_app_id = GOOGLE_PLUS_APP_ID;
        SignupActivity.newInstance(mContext, mUserDetails);
    }

    Response.ErrorListener mCallLoginWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            hideProgressDialog();

            // Show Toast
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityLoginBinding.getRoot());
        }
    };



    /**
     * Check Version number of application
     */
    //////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////Check Version API [Starts]///////////////////////
    //////////////////////////////////////////////////////////////////////////////////
    private void checkVersionOfApp() {

        if (!Utility.isConnected(mContext)) {
//            Utility.showSnackBar(getString(R.string.no_internet), mActivityHomeBinding.getRoot());
            return;
        }

        //Add Header parameters
        Map<String, String> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.vVERSION, BuildConfig.VERSION_NAME);
        mParams.put(NetworkUtility.TAGS.eUSERTYPE, BuildConfig.USER_TYPE);
        mParams.put(NetworkUtility.TAGS.ePLATFORM, NetworkUtility.TAGS.PLATFORMTYPE.ANDROID);

        VolleyNetworkRequest mVolleyNetworkRequestForCategoryList = new VolleyNetworkRequest(NetworkUtility.WS.CHECK_APP_VERSION
                , mCheckVersionNumberWSErrorListener
                , mCheckVersionNumberWSResponseListener
                , null
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForCategoryList);
    }

    Response.Listener mCheckVersionNumberWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {
            Log.d(TAG, "onResponse() called with: response = [" + response + "]");

            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                hideProgressDialog();
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
                        JSONObject jObjData = jsonObject.getJSONObject(NetworkUtility.TAGS.DATA);
                        int vVersionType = Integer.parseInt(jObjData.optString(NetworkUtility.TAGS.vVERSION_TYPE));
                        String message = jObjData.optString(NetworkUtility.TAGS.VERSION_DESC);
                        switch (vVersionType) {
                            case NetworkUtility.TAGS.VERSION_CHANGE_TYPE.NORMAL:
                                // Do nothing as user is using latest update.
                                break;
                            case NetworkUtility.TAGS.VERSION_CHANGE_TYPE.RECOMMENDED_TO_UPGRADE:
                                // We need to recommend user to update the app, however not forcefully as its not compulsary
                                break;
                            case NetworkUtility.TAGS.VERSION_CHANGE_TYPE.FORCE_UPGARDE_REQUIRED:
                                // We need to forcefully ask user to update the application.
                                showForceUpgradeAppDialog(message);
                                break;

                        }
//                        Utility.showSnackBar(jsonObject.getString(NetworkUtility.TAGS.MESSAGE), mActivityHomeBinding.getRoot());
                        break;
                }

            } catch (JSONException e) {
                e.printStackTrace();
                mCheckVersionNumberWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
        }
    };

    private void showForceUpgradeAppDialog(String message) {
        Log.d(TAG, "showForceUpgradeAppDialog() called with: message = [" + message + "]");
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setCancelable(false);
        builder.setMessage(message)
                .setNegativeButton(R.string.label_update, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Utility.redirectUserToPlaystore(mContext);
                    }
                });
        // Create the AlertDialog object and return it
        builder.create();

        builder.show();
    }

    Response.ErrorListener mCheckVersionNumberWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");
        }
    };

    //////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////Check Version API [Ends]/////////////////////////
    //////////////////////////////////////////////////////////////////////////////////

    /*************************************************************************************************************
     *************************************************************************************************************
     *****************************************Webservice Integration [End]**************************************
     *************************************************************************************************************
     */


    /**
     * BroadCast that would restart the screen once login has been done.
     */
    private BroadcastReceiver mBR_OnLoginSuccess = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Do nothing, just restart the activity
            finish();
        }
    };

}
