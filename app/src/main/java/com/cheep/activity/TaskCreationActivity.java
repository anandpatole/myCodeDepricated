package com.cheep.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.appsflyer.AppsFlyerLib;
import com.cheep.R;
import com.cheep.adapter.TaskCreationPagerAdapter;
import com.cheep.databinding.ActivityTaskCreateBinding;
import com.cheep.dialogs.AcknowledgementDialogWithoutProfilePic;
import com.cheep.dialogs.AcknowledgementInteractionListener;
import com.cheep.dialogs.CustomLoadingDialog;
import com.cheep.dialogs.InstaBookProDialog;
import com.cheep.firebase.FirebaseHelper;
import com.cheep.firebase.FirebaseUtils;
import com.cheep.firebase.model.ChatTaskModel;
import com.cheep.fragment.EnterTaskDetailFragment;
import com.cheep.fragment.SelectSubCategoryFragment;
import com.cheep.model.InstaBookingProDetail;
import com.cheep.model.JobCategoryModel;
import com.cheep.model.ProviderModel;
import com.cheep.model.SubServiceDetailModel;
import com.cheep.model.TaskDetailModel;
import com.cheep.model.UserDetails;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;
import com.google.android.gms.common.api.Status;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by bhavesh on 26/4/17.
 */
public class TaskCreationActivity extends BaseAppCompatActivity {
    private static final String TAG = "TaskCreationActivity";
    private ActivityTaskCreateBinding mActivityTaskCreateBinding;
    public JobCategoryModel mJobCategoryModel;
    TaskCreationPagerAdapter mTaskCreationPagerAdapter;
    private SubServiceDetailModel mSelectedSubServiceDetailModel;
    Map<String, Object> mTaskCreationParams;
    CustomLoadingDialog mDialog;

    public static void getInstance(Context mContext, JobCategoryModel model) {
        Intent intent = new Intent(mContext, TaskCreationActivity.class);
        intent.putExtra(Utility.Extra.DATA, Utility.getJsonStringFromObject(model));
        mContext.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityTaskCreateBinding = DataBindingUtil.setContentView(this, R.layout.activity_task_create);
        mContext.registerReceiver(mBR_OnInstaTaskCreated, new IntentFilter(Utility.BR_ON_TASK_CREATED_FOR_INSTA_BOOKING));
        initiateUI();
        setListeners();
    }

    @Override
    protected void initiateUI() {
        if (getIntent().getExtras() != null) {
            // Fetch JobCategory Model
            mJobCategoryModel = (JobCategoryModel) Utility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA), JobCategoryModel.class);
        }

        // Setting up Toolbar
        setSupportActionBar(mActivityTaskCreateBinding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(Utility.EMPTY_STRING);
            mActivityTaskCreateBinding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Utility.hideKeyboard(mContext);
                    onBackPressed();
                }
            });
        }

        // Set category
        mActivityTaskCreateBinding.textTitle.setText(mJobCategoryModel.catName != null ? mJobCategoryModel.catName : Utility.EMPTY_STRING);

        // Set up image
        Utility.loadImageView(mContext, mActivityTaskCreateBinding.imgService, mJobCategoryModel.catImage, R.drawable.gradient_black);
        Utility.loadImageView(mContext, mActivityTaskCreateBinding.imgService, mJobCategoryModel.catImageExtras.thumb, R.drawable.gradient_black);

        // Setting viewpager
        setupViewPager(mActivityTaskCreateBinding.viewpager);

        // Change description
        mActivityTaskCreateBinding.textStepDesc.setText(getString(R.string.step_1_desc));

        // Set the default step
        setTaskState(STEP_ONE_NORMAL);

        // Manage Click events of TaskCreation steps
        mActivityTaskCreateBinding.textStep1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoStep(STAGE_1);
            }
        });
        mActivityTaskCreateBinding.textStep2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                  Need to check whether first step is verified or not.
                 */
                if (mCurrentStep > 2) {
                    gotoStep(STAGE_2);
                } else {
                    Utility.showSnackBar(getString(R.string.step_1_desc), mActivityTaskCreateBinding.getRoot());
                }

            }
        });


    }

    private BroadcastReceiver mBR_OnInstaTaskCreated = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive() called with: context = [" + context + "], intent = [" + intent + "]");
            /*
              Redirect the user to MYTask Screen
             */
            finish();

            //Sending Broadcast to the HomeScreen Screen.
            Intent newIntent = new Intent(Utility.BR_ON_TASK_CREATED);
            sendBroadcast(newIntent);
        }
    };

    @Override
    protected void setListeners() {

    }


    /**
     * This will setup the viewpager and tabs as well
     *
     * @param pager
     */
    private void setupViewPager(ViewPager pager) {
        mTaskCreationPagerAdapter = new TaskCreationPagerAdapter(getSupportFragmentManager());
        mTaskCreationPagerAdapter.addFragment(SelectSubCategoryFragment.TAG);
        mTaskCreationPagerAdapter.addFragment(EnterTaskDetailFragment.TAG);
        pager.setAdapter(mTaskCreationPagerAdapter);
    }

    /**
     * Below would manage the state of Step while creating task creation
     */
    public static final int STEP_ONE_NORMAL = 1;
    public static final int STEP_ONE_UNVERIFIED = 2;
    public static final int STEP_ONE_VERIFIED = 3;
    public static final int STEP_TWO_NORMAL = 4;
    public static final int STEP_TWO_UNVERIFIED = 5;
    public static final int STEP_TWO_VERIFIED = 6;
    public static final int STEP_THREE_NORMAL = 7;
    public static final int STEP_THREE_UNVERIFIED = 8;
    public static final int STEP_THREE_VERIFIED = 9;
    public int mCurrentStep = -1;

    public void setTaskState(int step_state) {
        mCurrentStep = step_state;
        switch (step_state) {
            case STEP_ONE_NORMAL:
                mActivityTaskCreateBinding.textStep1.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_normal));
                mActivityTaskCreateBinding.textStep1.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mActivityTaskCreateBinding.textStep2.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_normal));
                mActivityTaskCreateBinding.textStep2.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mActivityTaskCreateBinding.textStep3.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_normal));
                mActivityTaskCreateBinding.textStep3.setTextColor(ContextCompat.getColor(mContext, R.color.white));


                break;
            case STEP_ONE_UNVERIFIED:
                mActivityTaskCreateBinding.textStep1.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_unverified));
                mActivityTaskCreateBinding.textStep1.setTextColor(ContextCompat.getColor(mContext, R.color.splash_gradient_end));

                mActivityTaskCreateBinding.textStep2.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_normal));
                mActivityTaskCreateBinding.textStep2.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mActivityTaskCreateBinding.textStep3.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_normal));
                mActivityTaskCreateBinding.textStep3.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                break;
            case STEP_ONE_VERIFIED:
            case STEP_TWO_NORMAL:
                mActivityTaskCreateBinding.textStep1.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_verified));
                mActivityTaskCreateBinding.textStep1.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mActivityTaskCreateBinding.textStep2.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_normal));
                mActivityTaskCreateBinding.textStep2.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mActivityTaskCreateBinding.textStep3.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_normal));
                mActivityTaskCreateBinding.textStep3.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                break;
            case STEP_TWO_UNVERIFIED:
                mActivityTaskCreateBinding.textStep1.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_verified));
                mActivityTaskCreateBinding.textStep1.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mActivityTaskCreateBinding.textStep2.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_unverified));
                mActivityTaskCreateBinding.textStep2.setTextColor(ContextCompat.getColor(mContext, R.color.splash_gradient_end));

                mActivityTaskCreateBinding.textStep3.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_normal));
                mActivityTaskCreateBinding.textStep3.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                break;
            case STEP_TWO_VERIFIED:
            case STEP_THREE_NORMAL:
                mActivityTaskCreateBinding.textStep1.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_verified));
                mActivityTaskCreateBinding.textStep1.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mActivityTaskCreateBinding.textStep2.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_verified));
                mActivityTaskCreateBinding.textStep2.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mActivityTaskCreateBinding.textStep3.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_normal));
                mActivityTaskCreateBinding.textStep3.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                break;
            case STEP_THREE_UNVERIFIED:
                mActivityTaskCreateBinding.textStep1.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_verified));
                mActivityTaskCreateBinding.textStep1.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mActivityTaskCreateBinding.textStep2.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_verified));
                mActivityTaskCreateBinding.textStep2.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mActivityTaskCreateBinding.textStep3.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_unverified));
                mActivityTaskCreateBinding.textStep3.setTextColor(ContextCompat.getColor(mContext, R.color.splash_gradient_end));

                break;
            case STEP_THREE_VERIFIED:
                mActivityTaskCreateBinding.textStep1.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_verified));
                mActivityTaskCreateBinding.textStep1.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mActivityTaskCreateBinding.textStep2.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_verified));
                mActivityTaskCreateBinding.textStep2.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mActivityTaskCreateBinding.textStep3.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_verified));
                mActivityTaskCreateBinding.textStep3.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                break;
        }
    }

    public static final int STAGE_1 = 0;
    public static final int STAGE_2 = 1;

    public void gotoStep(int step) {
        switch (step) {
            case STAGE_1:
                mActivityTaskCreateBinding.viewpager.setCurrentItem(0);
                // Change description
                mActivityTaskCreateBinding.textStepDesc.setText(getString(R.string.step_1_desc));
                break;
            case STAGE_2:
                mActivityTaskCreateBinding.viewpager.setCurrentItem(1);
                // Change description
                mActivityTaskCreateBinding.textStepDesc.setText(getString(R.string.step_2_desc));
                break;
        }
    }


    @Override
    public void onBackPressed() {
        if (mActivityTaskCreateBinding.viewpager.getCurrentItem() == 1) {
            gotoStep(STAGE_1);
            return;
        }
        super.onBackPressed();
    }

    public void showPostTaskButton(boolean needsToShow, boolean isEnabled) {

        /*if (needsToShow) {
            mActivityTaskCreateBinding.textPostTask.setVisibility(View.GONE);
        } else {
            mActivityTaskCreateBinding.textPostTask.setVisibility(View.GONE);
        }


        if (isEnabled) {
            mActivityTaskCreateBinding.textPostTask.setSelected(true);
            mActivityTaskCreateBinding.textPostTask.setBackgroundColor(ContextCompat.getColor(mContext, R.color.splash_gradient_end));
        } else {
            mActivityTaskCreateBinding.textPostTask.setSelected(false);
            mActivityTaskCreateBinding.textPostTask.setBackgroundColor(ContextCompat.getColor(mContext, R.color.grey_varient_12));
        }*/
    }

    public int getPostButtonHeight() {
        return mActivityTaskCreateBinding.textPostTask.getHeight();
    }

    public void setSelectedSubService(SubServiceDetailModel subServiceDetailModel) {
        this.mSelectedSubServiceDetailModel = subServiceDetailModel;
    }

    public SubServiceDetailModel getSelectedSubService() {
        return mSelectedSubServiceDetailModel;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Utility.REQUEST_CODE_READ_EXTERNAL_STORAGE_ADD_COVER) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "onRequestPermissionsResult: Permission Granted");
                //startIntentFileChooser(Utility.REQUEST_CODE_GET_FILE_ADD_COVER);
            } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Log.i(TAG, "onRequestPermissionsResult: Permission Denied");
                Utility.showSnackBar(getString(R.string.permission_denied_read), mActivityTaskCreateBinding.getRoot());
            }
        } else if (requestCode == Utility.REQUEST_CODE_READ_EXTERNAL_STORAGE_ADD_PROFILE_GALLERY) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "onRequestPermissionsResult: Permission Granted");
                // startIntentFileChooser(Utility.REQUEST_CODE_GET_FILE_ADD_PROFILE_GALLERY);
            } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Log.i(TAG, "onRequestPermissionsResult: Permission Denied");
                Utility.showSnackBar(getString(R.string.permission_denied_read), mActivityTaskCreateBinding.getRoot());
            }
        } else if (requestCode == Utility.REQUEST_CODE_ADD_PROFILE_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "onRequestPermissionsResult: Permission Granted");
                //  startCameraCaptureChooser(Utility.REQUEST_CODE_IMAGE_CAPTURE_ADD_PROFILE);
            } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Log.i(TAG, "onRequestPermissionsResult: Permission Denied Camera");
                Utility.showSnackBar(getString(R.string.permission_denied_camera), mActivityTaskCreateBinding.getRoot());
            }
        }
    }

    @Override
    public void gpsEnabled() {
        super.gpsEnabled();
        // Show placepicker activity
        mTaskCreationPagerAdapter.mEnterTaskDetailFragment.showPlacePickerDialog(true);
    }

    @Override
    public void onLocationSettingsDialogNeedToBeShow(Status locationRequest) {
        super.onLocationSettingsDialogNeedToBeShow(locationRequest);
        // Location settings are not satisfied, but this can be fixed
        // by showing the user a dialog.
        try {
            // Show the dialog by calling startResolutionForResult(),
            // and check the result in onActivityResult().
            locationRequest.startResolutionForResult(TaskCreationActivity.this, Utility.REQUEST_CODE_CHECK_LOCATION_SETTINGS);
        } catch (IntentSender.SendIntentException e) {
            // Ignore the error.
        }
    }


    public void onInstaBookClicked() {
        if (!isValidationCompleted()) {
            return;
        }

        // Check Internet connection
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(getString(R.string.no_internet), mActivityTaskCreateBinding.getRoot());
            return;
        }

        if (mTaskCreationPagerAdapter.mEnterTaskDetailFragment.superCalendar == null) {
            Utility.showSnackBar(getString(R.string.can_only_start_task_after_3_hours), mActivityTaskCreateBinding.getRoot());
            return;
        }

        mDialog = new CustomLoadingDialog();
        mDialog.setCancelable(false);
        mDialog.show(getSupportFragmentManager(), "loading");


        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();

        // Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, userDetails.UserID);
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());

        // Add Params
        Map<String, String> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.CITY_ID, userDetails.CityID);
        mParams.put(NetworkUtility.TAGS.CAT_ID, mJobCategoryModel.catId);
        mParams.put(NetworkUtility.TAGS.ADDRESS_ID, mTaskCreationPagerAdapter.mEnterTaskDetailFragment.addressId);
        mParams.put(NetworkUtility.TAGS.SUBCATEGORY_ID, String.valueOf(mSelectedSubServiceDetailModel.sub_cat_id));

        mTaskCreationParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.CITY_ID, userDetails.CityID);
        mParams.put(NetworkUtility.TAGS.CAT_ID, mJobCategoryModel.catId);
        mParams.put(NetworkUtility.TAGS.ADDRESS_ID, mTaskCreationPagerAdapter.mEnterTaskDetailFragment.addressId);
        mParams.put(NetworkUtility.TAGS.SUBCATEGORY_ID, String.valueOf(mSelectedSubServiceDetailModel.sub_cat_id));
        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.GET_PRO_FOR_INSTA_BOOKING
                , mCallInstaBookErrorListener
                , mCallInstaBookWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////Post Task [Start] /////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    public void onGetQuoteClicked() {
        Log.d(TAG, "onPostTaskClicked() called");
        if (!isValidationCompleted()) {
            return;
        }

        // Check Internet connection
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(getString(R.string.no_internet), mActivityTaskCreateBinding.getRoot());
            return;
        }

        if (mTaskCreationPagerAdapter.mEnterTaskDetailFragment.superCalendar == null) {
            Utility.showSnackBar(getString(R.string.can_only_start_task_after_3_hours), mActivityTaskCreateBinding.getRoot());
            return;
        }

       /* SuperCalendar superCalendar = SuperCalendar.getInstance();
        superCalendar.setTimeInMillis(mTaskCreationPagerAdapter.mEnterTaskDetailFragment.startDateTimeSuperCalendar.getTimeInMillis());
        superCalendar.setTimeZone(SuperCalendar.SuperTimeZone.GMT.GMT);

        // Get date-time for next 3 hours
        SuperCalendar calAfter3Hours = SuperCalendar.getInstance().getNext3HoursTime();

        if (superCalendar.getTimeInMillis() < calAfter3Hours.getTimeInMillis()) {
            Utility.showSnackBar(getString(R.string.can_only_start_task_after_3_hours), mActivityTaskCreateBinding.getRoot());
            return;
        }*/

        //Show Progress
        showProgressDialog();

        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();

        // Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, userDetails.UserID);
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());

        // Add Params
        Map<String, String> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.TASK_DESC, mTaskCreationPagerAdapter.mEnterTaskDetailFragment.getTaskDescription());
        mParams.put(NetworkUtility.TAGS.ADDRESS_ID, mTaskCreationPagerAdapter.mEnterTaskDetailFragment.addressId);
        mParams.put(NetworkUtility.TAGS.CITY_ID, userDetails.CityID);
        mParams.put(NetworkUtility.TAGS.CAT_ID, mJobCategoryModel.catId);
        mParams.put(NetworkUtility.TAGS.SUBCATEGORY_ID, String.valueOf(mSelectedSubServiceDetailModel.sub_cat_id));
        mParams.put(NetworkUtility.TAGS.START_DATETIME, String.valueOf(mTaskCreationPagerAdapter.mEnterTaskDetailFragment.superCalendar.getTimeInMillis()));

        // Create Params for AppsFlyer event track
        mTaskCreationParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.TASK_DESC, mTaskCreationPagerAdapter.mEnterTaskDetailFragment.getTaskDescription());
        mParams.put(NetworkUtility.TAGS.ADDRESS_ID, mTaskCreationPagerAdapter.mEnterTaskDetailFragment.addressId);
        mParams.put(NetworkUtility.TAGS.CITY_ID, userDetails.CityID);
        mParams.put(NetworkUtility.TAGS.CAT_ID, mJobCategoryModel.catId);
        mParams.put(NetworkUtility.TAGS.SUBCATEGORY_ID, String.valueOf(mSelectedSubServiceDetailModel.sub_cat_id));
        mParams.put(NetworkUtility.TAGS.START_DATETIME, String.valueOf(mTaskCreationPagerAdapter.mEnterTaskDetailFragment.superCalendar.getTimeInMillis()));


        // Add Params
        HashMap<String, File> mFileParams = new HashMap<>();
        if (!TextUtils.isEmpty(mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mCurrentPhotoPath)
                && new File(mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mCurrentPhotoPath).exists()) {
            mFileParams.put(NetworkUtility.TAGS.TASK_IMAGE, new File(mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mCurrentPhotoPath));
        }

        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.CREATE_TASK
                , mCallCreateTaskWSErrorListener
                , mCallCreateTaskWSResponseListener
                , mHeaderParams
                , mParams
                , mFileParams);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest);
    }

    Response.Listener mCallCreateTaskWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {

            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:

                        /*
                          Below was older approach when app needs to update the same task page.
                         */
//                        TaskDetailModel taskDetailModel = (TaskDetailModel) Utility.getObjectFromJsonString(jsonObject.optString(NetworkUtility.TAGS.DATA), TaskDetailModel.class);
//                        getIntent().putExtra(Utility.Extra.DATA, jsonObject.optString(NetworkUtility.TAGS.DATA));
//                        getIntent().putExtra(Utility.Extra.IS_FIRST_TIME, true);
//                        getIntent().setAction(Utility.ACTION_HIRE_PROVIDER);
//                        initiateUI();
//                        setListeners();

                        // Send Event tracking for AppsFlyer
                        AppsFlyerLib.getInstance().trackEvent(mContext, NetworkUtility.TAGS.APPSFLYER_CUSTOM_TRACK_EVENTS.TASK_CREATE, mTaskCreationParams);


                        /*
                          Now according to the new flow, once task created
                          app will be redirected to MyTask Detail screen.
                         */
                        onSuccessfullTaskCompletion(jsonObject);
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityTaskCreateBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        String error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mActivityTaskCreateBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.USER_DELETED:
                    case NetworkUtility.TAGS.STATUSCODETYPE.FORCE_LOGOUT_REQUIRED:
                        //Logout and finish the current activity
                        Utility.logout(mContext, true, statusCode);
                        finish();
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mCallCreateTaskWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
            hideProgressDialog();
        }
    };


    Response.Listener mCallInstaBookWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {

            if (!isFinishing() && mDialog != null) {
                mDialog.dismiss();
            }
            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:

                        /*
                          Below was older approach when app needs to update the same task page.
                         */
//                        TaskDetailModel taskDetailModel = (TaskDetailModel) Utility.getObjectFromJsonString(jsonObject.optString(NetworkUtility.TAGS.DATA), TaskDetailModel.class);
//                        getIntent().putExtra(Utility.Extra.DATA, jsonObject.optString(NetworkUtility.TAGS.DATA));
//                        getIntent().putExtra(Utility.Extra.IS_FIRST_TIME, true);
//                        getIntent().setAction(Utility.ACTION_HIRE_PROVIDER);
//                        initiateUI();
//                        setListeners();

                        // Send Event tracking for AppsFlyer
                        AppsFlyerLib.getInstance().trackEvent(mContext, NetworkUtility.TAGS.APPSFLYER_CUSTOM_TRACK_EVENTS.INSTA_BOOK, mTaskCreationParams);


                        /*
                          Now according to the new flow, once task created
                          app will be redirected to MyTask Detail screen.
                         */
                        final InstaBookingProDetail taskDetailModel = (InstaBookingProDetail) Utility.getObjectFromJsonString(jsonObject.optString(NetworkUtility.TAGS.DATA), InstaBookingProDetail.class);
                        if (taskDetailModel != null && taskDetailModel.spId != null)
                            onSuccessInstaBooking(taskDetailModel);
                        else
                            Utility.showToast(TaskCreationActivity.this, getString(R.string.alert_no_pro_found));
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityTaskCreateBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        String error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mActivityTaskCreateBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.USER_DELETED:
                    case NetworkUtility.TAGS.STATUSCODETYPE.FORCE_LOGOUT_REQUIRED:
                        //Logout and finish the current activity
                        Utility.logout(mContext, true, statusCode);
                        finish();
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mCallInstaBookErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }

        }
    };


    /**
     * This method would going to call when task completed successfully
     */
    private void onSuccessInstaBooking(final InstaBookingProDetail taskDetailModel) {
        if (taskDetailModel != null) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(mTaskCreationPagerAdapter.mEnterTaskDetailFragment.superCalendar.getCalendar().getTimeInMillis());
            c.setTimeZone(TimeZone.getDefault());

            Date d = c.getTime();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM", Locale.getDefault());
            String date = simpleDateFormat.format(d);
            int fromHour = c.get(Calendar.HOUR);
            if (fromHour == 0) {
                fromHour = 12;
            }
            c.add(Calendar.HOUR, 1);
            int toHour = c.get(Calendar.HOUR);
            InstaBookProDialog dialog = InstaBookProDialog.newInstance(this, taskDetailModel, date + " between " + fromHour + "-" + toHour, new AcknowledgementInteractionListener() {
                @Override
                public void onAcknowledgementAccepted() {


                    TaskDetailModel model = new TaskDetailModel();

                    model.categoryName = mJobCategoryModel.catName;
                    model.subCategoryName = mSelectedSubServiceDetailModel.name;
                    model.taskAddress = mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mAddress;
                    model.taskAddressId = mTaskCreationPagerAdapter.mEnterTaskDetailFragment.addressId;
                    model.task_total_amount = taskDetailModel.rate;
                    model.categoryId = mJobCategoryModel.catId;
                    model.taskDesc = mTaskCreationPagerAdapter.mEnterTaskDetailFragment.getTaskDescription();
                    model.catImage = mJobCategoryModel.catImage;
                    model.taskStartdate = String.valueOf(mTaskCreationPagerAdapter.mEnterTaskDetailFragment.superCalendar.getCalendar().getTimeInMillis());
                    model.subCategoryID = String.valueOf(mSelectedSubServiceDetailModel.sub_cat_id);
                    ProviderModel pModel = new ProviderModel();
                    pModel.userName = taskDetailModel.userName;
                    pModel.profileUrl = taskDetailModel.profileImg;
                    pModel.providerId = taskDetailModel.spId;
                    pModel.quotePrice = taskDetailModel.rateGST;
                    pModel.quotePriceWithOutGST = taskDetailModel.rate;

                    PaymentsStepActivity.newInstance(TaskCreationActivity.this, model, pModel, 0, true);

                    //Log.i("myLog", "tasks:"+mJobCategoryModel.catName+"::"+mSelectedSubServiceDetailModel.name+"::"+mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mAddress);

                }
            });
            dialog.setCancelable(true);
            dialog.show(getSupportFragmentManager(), "loading");
        } else {
        }
    }


    /**
     * This method would going to call when task completed successfully
     */
    private void onSuccessfullTaskCompletion(JSONObject jsonObject) {
        TaskDetailModel taskDetailModel = (TaskDetailModel) Utility.getObjectFromJsonString(jsonObject.optString(NetworkUtility.TAGS.DATA), TaskDetailModel.class);
        if (taskDetailModel != null) {
            /* * Add new task detail on firebase
             * @Sanjay 20 Feb 2016
             */
            ChatTaskModel chatTaskModel = new ChatTaskModel();
            chatTaskModel.taskId = FirebaseUtils.getPrefixTaskId(taskDetailModel.taskId);
            chatTaskModel.taskDesc = taskDetailModel.taskDesc;
            chatTaskModel.categoryId = taskDetailModel.categoryId;
            chatTaskModel.categoryName = taskDetailModel.categoryName;
            chatTaskModel.selectedSPId = "";
            UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
            chatTaskModel.userId = FirebaseUtils.getPrefixUserId(userDetails.UserID);
            FirebaseHelper.getTaskRef(chatTaskModel.taskId).setValue(chatTaskModel);
        }

        // Update the name of User
       /* mDialogFragmentTaskCreationBinding.textTaskCreationAcknowledgment
                .setText(mDialogFragmentTaskCreationBinding.getRoot().getContext().getString(R.string.desc_task_creation_acknowledgement, mUserName));
        */
        String message = mContext.getString(R.string.desc_task_creation_acknowledgement
                , PreferenceUtility.getInstance(mContext).getUserDetails().UserName);
        String title = mContext.getString(R.string.label_your_task_is_posted);
        AcknowledgementDialogWithoutProfilePic mAcknowledgementDialogWithoutProfilePic = AcknowledgementDialogWithoutProfilePic.newInstance(R.drawable.ic_bird_with_heart_illustration, title, message, new AcknowledgementInteractionListener() {

            @Override
            public void onAcknowledgementAccepted() {
                // Finish the current activity
                finish();

                //Sending Broadcast to the HomeScreen Screen.
                Intent intent = new Intent(Utility.BR_ON_TASK_CREATED);
                sendBroadcast(intent);
            }
        });
        mAcknowledgementDialogWithoutProfilePic.setCancelable(false);
        mAcknowledgementDialogWithoutProfilePic.show(getSupportFragmentManager(), AcknowledgementDialogWithoutProfilePic.TAG);
    }


    /**
     * Create Dialog which would going to show on successful completion
     */


    Response.ErrorListener mCallCreateTaskWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            hideProgressDialog();

            // Show Toast
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityTaskCreateBinding.getRoot());
        }
    };


    Response.ErrorListener mCallInstaBookErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            if (mDialog != null) {
                mDialog.dismiss();
            }

            // Show Toast
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityTaskCreateBinding.getRoot());
        }
    };

    public boolean isValidationCompleted() {
        // Task Description
        if (!mTaskCreationPagerAdapter.mEnterTaskDetailFragment.isTaskDescriptionVerified) {
            Utility.showSnackBar(getString(R.string.validate_task_desc), mActivityTaskCreateBinding.getRoot());
            return false;
        }

        // Date-Time of Task
        if (!mTaskCreationPagerAdapter.mEnterTaskDetailFragment.isTaskWhenVerified) {
            Utility.showSnackBar(getString(R.string.validate_date), mActivityTaskCreateBinding.getRoot());
            return false;
        }

        // place of Task
        if (!mTaskCreationPagerAdapter.mEnterTaskDetailFragment.isTaskWhereVerified) {
            Utility.showSnackBar(getString(R.string.validate_address_new_task), mActivityTaskCreateBinding.getRoot());
            return false;
        }

        return true;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////Post Task [End] /////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Utility.REQUEST_CODE_CHECK_LOCATION_SETTINGS) {
            if (resultCode == RESULT_OK) {
                mLocationTrackService.requestLocationUpdate();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mContext.unregisterReceiver(mBR_OnInstaTaskCreated);

    }
}
