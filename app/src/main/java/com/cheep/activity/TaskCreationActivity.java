package com.cheep.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.cheep.utils.SuperCalendar;
import com.cheep.utils.Utility;
import com.google.android.gms.common.api.Status;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
    private boolean isInstaBooking = false;

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

        registerReceiver(mBR_OnLoginSuccess, new IntentFilter(Utility.BR_ON_LOGIN_SUCCESS));
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
            newIntent.putExtra(Utility.Extra.IS_INSTA_BOOKING_TASK, true);
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
        switch (requestCode) {
            case Utility.REQUEST_CODE_READ_EXTERNAL_STORAGE_ADD_COVER:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "onRequestPermissionsResult: Permission Granted");
                    //startIntentFileChooser(Utility.REQUEST_CODE_GET_FILE_ADD_COVER);
                } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Log.i(TAG, "onRequestPermissionsResult: Permission Denied");
                    Utility.showSnackBar(getString(R.string.permission_denied_read), mActivityTaskCreateBinding.getRoot());
                }
                break;
            case Utility.REQUEST_CODE_READ_EXTERNAL_STORAGE_ADD_PROFILE_GALLERY:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "onRequestPermissionsResult: Permission Granted");
                    // startIntentFileChooser(Utility.REQUEST_CODE_GET_FILE_ADD_PROFILE_GALLERY);
                } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Log.i(TAG, "onRequestPermissionsResult: Permission Denied");
                    Utility.showSnackBar(getString(R.string.permission_denied_read), mActivityTaskCreateBinding.getRoot());
                }
                break;
            case Utility.REQUEST_CODE_ADD_PROFILE_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "onRequestPermissionsResult: Permission Granted");
                    //  startCameraCaptureChooser(Utility.REQUEST_CODE_IMAGE_CAPTURE_ADD_PROFILE);
                } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Log.i(TAG, "onRequestPermissionsResult: Permission Denied Camera");
                    Utility.showSnackBar(getString(R.string.permission_denied_camera), mActivityTaskCreateBinding.getRoot());
                }
                break;
        }
    }

    @Override
    public void gpsEnabled() {
        super.gpsEnabled();
//        Log.d(TAG, "gpsEnabled() called");
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
        } catch (Exception e) {
            // Ignore the error.
        }
    }


    public void onInstaBookClicked() {
        if (!isValidationCompleted()) {
            return;
        }

        // Check Internet connection
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityTaskCreateBinding.getRoot());
            return;
        }

        if (mTaskCreationPagerAdapter.mEnterTaskDetailFragment.superCalendar == null) {
            Utility.showSnackBar(getString(R.string.can_only_start_task_after_3_hours), mActivityTaskCreateBinding.getRoot());
            return;
        }
        if (PreferenceUtility.getInstance(mContext).getUserDetails() == null) {
            isInstaBooking = true;
            LoginActivity.newInstance(mContext);
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
        if (Integer.parseInt(mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mSelectedAddressModel.address_id) > 0) {
            mParams.put(NetworkUtility.TAGS.ADDRESS_ID, mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mSelectedAddressModel.address_id);
        } else {
            // In case its nagative then provide other address information
            /*
             public String address_initials;
             public String address;
             public String category; //comes from NetworkUtility.TAGS.ADDRESS_TYPE.
             public String lat;
             public String lng;
             */
            mParams.put(NetworkUtility.TAGS.ADDRESS_INITIALS, mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mSelectedAddressModel.address_initials);
            mParams.put(NetworkUtility.TAGS.ADDRESS, mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mSelectedAddressModel.address);
            mParams.put(NetworkUtility.TAGS.CATEGORY, mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mSelectedAddressModel.category);
            mParams.put(NetworkUtility.TAGS.LAT, mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mSelectedAddressModel.lat);
            mParams.put(NetworkUtility.TAGS.LNG, mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mSelectedAddressModel.lng);
            mParams.put(NetworkUtility.TAGS.CITY_NAME, mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mSelectedAddressModel.cityName);
            mParams.put(NetworkUtility.TAGS.COUNTRY, mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mSelectedAddressModel.countryName);
            mParams.put(NetworkUtility.TAGS.STATE, mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mSelectedAddressModel.stateName);
        }
        mParams.put(NetworkUtility.TAGS.SUBCATEGORY_ID, String.valueOf(mSelectedSubServiceDetailModel.sub_cat_id));

        mTaskCreationParams = new HashMap<>();
        mTaskCreationParams.put(NetworkUtility.TAGS.CITY_ID, userDetails.CityID);
        mTaskCreationParams.put(NetworkUtility.TAGS.CAT_ID, mJobCategoryModel.catId);
        if (Integer.parseInt(mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mSelectedAddressModel.address_id) > 0) {
            mParams.put(NetworkUtility.TAGS.ADDRESS_ID, mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mSelectedAddressModel.address_id);
        } else {
            // In case its nagative then provide other address information
            /*
             public String address_initials;
             public String address;
             public String category; //comes from NetworkUtility.TAGS.ADDRESS_TYPE.
             public String lat;
             public String lng;
             */
            mTaskCreationParams.put(NetworkUtility.TAGS.ADDRESS_INITIALS, mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mSelectedAddressModel.address_initials);
            mTaskCreationParams.put(NetworkUtility.TAGS.ADDRESS, mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mSelectedAddressModel.address);
            mTaskCreationParams.put(NetworkUtility.TAGS.CATEGORY, mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mSelectedAddressModel.category);
            mTaskCreationParams.put(NetworkUtility.TAGS.LAT, mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mSelectedAddressModel.lat);
            mTaskCreationParams.put(NetworkUtility.TAGS.LNG, mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mSelectedAddressModel.lng);
            mTaskCreationParams.put(NetworkUtility.TAGS.CITY_NAME, mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mSelectedAddressModel.cityName);
            mTaskCreationParams.put(NetworkUtility.TAGS.COUNTRY, mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mSelectedAddressModel.countryName);
            mTaskCreationParams.put(NetworkUtility.TAGS.STATE, mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mSelectedAddressModel.stateName);
        }
        mTaskCreationParams.put(NetworkUtility.TAGS.SUBCATEGORY_ID, String.valueOf(mSelectedSubServiceDetailModel.sub_cat_id));

        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.GET_PRO_FOR_INSTA_BOOKING
                , mCallGetProInstaBookErrorListener
                , mCallGetProInstaBookWSResponseListener
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
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityTaskCreateBinding.getRoot());
            return;
        }

        if (mTaskCreationPagerAdapter.mEnterTaskDetailFragment.superCalendar == null) {
            Utility.showSnackBar(getString(R.string.can_only_start_task_after_3_hours), mActivityTaskCreateBinding.getRoot());
            return;
        }

        if (PreferenceUtility.getInstance(mContext).getUserDetails() == null) {
            isInstaBooking = false;
            LoginActivity.newInstance(mContext);
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

        // Show Progress
        showProgressDialog();

        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();

        // Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, userDetails.UserID);
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());

        // Add Params
        Map<String, String> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.TASK_DESC, mTaskCreationPagerAdapter.mEnterTaskDetailFragment.getTaskDescription());
        if (Integer.parseInt(mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mSelectedAddressModel.address_id) > 0) {
            mParams.put(NetworkUtility.TAGS.ADDRESS_ID, mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mSelectedAddressModel.address_id);
        } else {
            // In case its nagative then provide other address information
            /*
             public String address_initials;
             public String address;
             public String category; //comes from NetworkUtility.TAGS.ADDRESS_TYPE.
             public String lat;
             public String lng;
             */
            mParams.put(NetworkUtility.TAGS.ADDRESS_INITIALS, mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mSelectedAddressModel.address_initials);
            mParams.put(NetworkUtility.TAGS.ADDRESS, mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mSelectedAddressModel.address);
            mParams.put(NetworkUtility.TAGS.CATEGORY, mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mSelectedAddressModel.category);
            mParams.put(NetworkUtility.TAGS.LAT, mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mSelectedAddressModel.lat);
            mParams.put(NetworkUtility.TAGS.LNG, mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mSelectedAddressModel.lng);
            mParams.put(NetworkUtility.TAGS.CITY_NAME, mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mSelectedAddressModel.cityName);
            mParams.put(NetworkUtility.TAGS.COUNTRY, mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mSelectedAddressModel.countryName);
            mParams.put(NetworkUtility.TAGS.STATE, mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mSelectedAddressModel.stateName);
        }
        if (userDetails != null) {
            mParams.put(NetworkUtility.TAGS.CITY_ID, userDetails.CityID);
        }
        mParams.put(NetworkUtility.TAGS.CAT_ID, mJobCategoryModel.catId);
        mParams.put(NetworkUtility.TAGS.SUBCATEGORY_ID, String.valueOf(mSelectedSubServiceDetailModel.sub_cat_id));
        mParams.put(NetworkUtility.TAGS.START_DATETIME, String.valueOf(mTaskCreationPagerAdapter.mEnterTaskDetailFragment.superCalendar.getTimeInMillis()));

        // Create Params for AppsFlyer event track
        mTaskCreationParams = new HashMap<>();
        mTaskCreationParams.put(NetworkUtility.TAGS.TASK_DESC, mTaskCreationPagerAdapter.mEnterTaskDetailFragment.getTaskDescription());
        if (Integer.parseInt(mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mSelectedAddressModel.address_id) > 0) {
            mTaskCreationParams.put(NetworkUtility.TAGS.ADDRESS_ID, mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mSelectedAddressModel.address_id);
        } else {
            // In case its nagative then provide other address information
            /**
             * public String address_initials;
             public String address;
             public String category; //comes from NetworkUtility.TAGS.ADDRESS_TYPE.
             public String lat;
             public String lng;
             public String cityName;
             public String countryName;
             public String stateName;
             */
            mTaskCreationParams.put(NetworkUtility.TAGS.ADDRESS_INITIALS, mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mSelectedAddressModel.address_initials);
            mTaskCreationParams.put(NetworkUtility.TAGS.ADDRESS, mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mSelectedAddressModel.address);
            mTaskCreationParams.put(NetworkUtility.TAGS.CATEGORY, mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mSelectedAddressModel.category);
            mTaskCreationParams.put(NetworkUtility.TAGS.LAT, mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mSelectedAddressModel.lat);
            mTaskCreationParams.put(NetworkUtility.TAGS.LNG, mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mSelectedAddressModel.lng);
            mTaskCreationParams.put(NetworkUtility.TAGS.CITY_NAME, mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mSelectedAddressModel.cityName);
            mTaskCreationParams.put(NetworkUtility.TAGS.COUNTRY, mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mSelectedAddressModel.countryName);
            mTaskCreationParams.put(NetworkUtility.TAGS.STATE, mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mSelectedAddressModel.stateName);
        }
//        mTaskCreationParams.put(NetworkUtility.TAGS.CITY_ID, userDetails.CityID);
        mTaskCreationParams.put(NetworkUtility.TAGS.CAT_ID, mJobCategoryModel.catId);
        mTaskCreationParams.put(NetworkUtility.TAGS.SUBCATEGORY_ID, String.valueOf(mSelectedSubServiceDetailModel.sub_cat_id));
        mTaskCreationParams.put(NetworkUtility.TAGS.START_DATETIME, String.valueOf(mTaskCreationPagerAdapter.mEnterTaskDetailFragment.superCalendar.getTimeInMillis()));

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
                        // Logout and finish the current activity
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


    Response.Listener mCallGetProInstaBookWSResponseListener = new Response.Listener() {
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

                        /*
                          Now according to the new flow, once task created
                          app will be redirected to MyTask Detail screen.
                         */
                        final InstaBookingProDetail taskDetailModel = (InstaBookingProDetail) Utility.getObjectFromJsonString(jsonObject.optString(NetworkUtility.TAGS.DATA), InstaBookingProDetail.class);
                        if (taskDetailModel != null && taskDetailModel.spId != null)
                            onSuccessOfGetProForInstaBooking(taskDetailModel);
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
                mCallGetProInstaBookErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }

        }
    };


    /**
     * This method would going to call when task completed successfully
     */
    private void onSuccessOfGetProForInstaBooking(final InstaBookingProDetail taskDetailModel) {
        if (taskDetailModel != null) {

            // create calendar for time and date for dialog text
            SuperCalendar superCalendar = SuperCalendar.getInstance();
            superCalendar.setTimeInMillis(mTaskCreationPagerAdapter.mEnterTaskDetailFragment.superCalendar.getCalendar().getTimeInMillis());

            Date d = superCalendar.getCalendar().getTime();

            // set time format 24 hours
            final String timeFormat = SuperCalendar.SuperFormatter.HOUR_24_HOUR + ":" + SuperCalendar.SuperFormatter.MINUTE + "";

            // set date format
            final String dateFormat = SuperCalendar.SuperFormatter.DATE + " " + SuperCalendar.SuperFormatter.MONTH_JAN;

            // formatter for date and time
            SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);
            SimpleDateFormat timeFormatter = new SimpleDateFormat(timeFormat);

            String date = dateFormatter.format(d);

            //time will from selected hour + 1 hour added like 12.11 - 13.11 hrs
            String fromHour = timeFormatter.format(d);

            // add two hour slot (changed on 13 sept 2017)
            superCalendar.getCalendar().add(Calendar.HOUR_OF_DAY, 2);

            Date toDate = superCalendar.getCalendar().getTime();

            // +1 hour
            String toHour = timeFormatter.format(toDate);

            InstaBookProDialog dialog = InstaBookProDialog.newInstance(this, taskDetailModel, date + getString(R.string.label_between) + fromHour + " - " + toHour + getString(R.string.label_hrs), new AcknowledgementInteractionListener() {
                @Override
                public void onAcknowledgementAccepted() {

                    TaskDetailModel detailModel = new TaskDetailModel();
                    detailModel.categoryName = mJobCategoryModel.catName;
                    detailModel.subCategoryName = mSelectedSubServiceDetailModel.name;
                    detailModel.taskAddress = mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mSelectedAddressModel.address;
                    detailModel.taskAddressId = mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mSelectedAddressModel.address_id;
                    detailModel.task_total_amount = taskDetailModel.rate;
                    detailModel.categoryId = mJobCategoryModel.catId;
                    detailModel.taskDesc = mTaskCreationPagerAdapter.mEnterTaskDetailFragment.getTaskDescription();
                    detailModel.catImage = mJobCategoryModel.catImage;
                    detailModel.taskStartdate = String.valueOf(mTaskCreationPagerAdapter.mEnterTaskDetailFragment.superCalendar.getCalendar().getTimeInMillis());
                    detailModel.subCategoryID = String.valueOf(mSelectedSubServiceDetailModel.sub_cat_id);
                    detailModel.taskImage= mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mCurrentPhotoPath;
                    ProviderModel providerModel = new ProviderModel();
                    providerModel.userName = taskDetailModel.userName;
                    providerModel.profileUrl = taskDetailModel.profileImg;
                    providerModel.providerId = taskDetailModel.spId;
                    providerModel.quotePrice = taskDetailModel.rateGST;
                    providerModel.quotePriceWithOutGST = taskDetailModel.rate;

                    PaymentsStepActivity.newInstance(TaskCreationActivity.this, detailModel, providerModel, 0, true, mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mSelectedAddressModel);

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
        final TaskDetailModel taskDetailModel = (TaskDetailModel) Utility.getObjectFromJsonString(jsonObject.optString(NetworkUtility.TAGS.DATA), TaskDetailModel.class);
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


        if (Utility.BOOLEAN.NO.equalsIgnoreCase(taskDetailModel.isPrefedQuote)) {

            /**
             * If HomeScreen is not available, create new instance and redirect
             * to Mytask screen, if yes, we just need to broadcast the same.
             */
//            if (PreferenceUtility.getInstance(mContext).isHomeScreenVisible()) {
//                //Sending Broadcast to the HomeScreen Screen.
//                Intent intent = new Intent(Utility.BR_ON_TASK_CREATED);
//                intent.putExtra(Utility.Extra.DATA, Utility.getJsonStringFromObject(taskDetailModel));
//                Log.d(TAG, "onAcknowledgementAccepted: prefed >>>>> " + taskDetailModel.isPrefedQuote);
//                intent.putExtra(Utility.Extra.IS_INSTA_BOOKING_TASK, Utility.BOOLEAN.NO.equalsIgnoreCase(taskDetailModel.isPrefedQuote));
//                sendBroadcast(intent);
//            } else {
//                HomeActivity.newInstance(mContext);
//            }
//            finish();

            /**
             * Below can be uncommented in case we need to show dialog.
             */
            /*final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
            builder.setCancelable(false);
            builder.setTitle(getString(R.string.no_pro_available_title));
            builder.setMessage(getString(R.string.no_pro_available_description));
            builder.setPositiveButton(getString(R.string.label_Ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Log.d(TAG, "onClick() called with: dialogInterface = [" + dialogInterface + "], i = [" + i + "]");
                if (PreferenceUtility.getInstance(mContext).isHomeScreenVisible()) {
                //Sending Broadcast to the HomeScreen Screen.
                Intent intent = new Intent(Utility.BR_ON_TASK_CREATED);
                intent.putExtra(Utility.Extra.DATA, Utility.getJsonStringFromObject(taskDetailModel));
                Log.d(TAG, "onAcknowledgementAccepted: prefed >>>>> " + taskDetailModel.isPrefedQuote);
                intent.putExtra(Utility.Extra.IS_INSTA_BOOKING_TASK, Utility.BOOLEAN.NO.equalsIgnoreCase(taskDetailModel.isPrefedQuote));
                sendBroadcast(intent);
            } else {
                HomeActivity.newInstance(mContext);
            }
            dialogInterface.dismiss();
            finish();
                }
            });
            builder.show();*/
        }

        // Update the name of User
       /* mDialogFragmentTaskCreationBinding.textTaskCreationAcknowledgment
                .setText(mDialogFragmentTaskCreationBinding.getRoot().getContext().getString(R.string.desc_task_creation_acknowledgement, mUserName));
        */
//        else {
        String message = mContext.getString(R.string.desc_task_creation_acknowledgement
                , PreferenceUtility.getInstance(mContext).getUserDetails().UserName);
        String title = mContext.getString(R.string.label_your_task_is_posted);
        AcknowledgementDialogWithoutProfilePic mAcknowledgementDialogWithoutProfilePic = AcknowledgementDialogWithoutProfilePic.newInstance(R.drawable.ic_bird_with_heart_illustration, title, message, new AcknowledgementInteractionListener() {

            @Override
            public void onAcknowledgementAccepted() {
                // Finish the current activity
                finish();

                /**
                 * If HomeScreen is not available, create new instance and redirect
                 * to Mytask screen, if yes, we just need to broadcast the same.
                 */
                if (PreferenceUtility.getInstance(mContext).isHomeScreenVisible()) {
                    //Sending Broadcast to the HomeScreen Screen.
                    Intent intent = new Intent(Utility.BR_ON_TASK_CREATED);
                    intent.putExtra(Utility.Extra.DATA, Utility.getJsonStringFromObject(taskDetailModel));
                    Log.d(TAG, "onAcknowledgementAccepted: prefed >>>>> " + taskDetailModel.isPrefedQuote);
                    intent.putExtra(Utility.Extra.IS_INSTA_BOOKING_TASK, Utility.BOOLEAN.NO.equalsIgnoreCase(taskDetailModel.isPrefedQuote));
                    sendBroadcast(intent);
                } else {
                    HomeActivity.newInstance(mContext);
                }
            }
        });
        mAcknowledgementDialogWithoutProfilePic.setCancelable(false);
        mAcknowledgementDialogWithoutProfilePic.show(getSupportFragmentManager(), AcknowledgementDialogWithoutProfilePic.TAG);

        /**
         * @Changes: 2ndAug2017 by Bhavesh
         * Once any task is created, App is having feature of sending Prefed quotes
         * so, we will initate once webservice call BUT we will not track the response as
         * it would be asynchronously managed.
         */
        callWSForPrefedQuotes(taskDetailModel.taskId, taskDetailModel.taskAddressId);

//        }

    }

    /**
     * Initiating Prefed Quotes related Webservice
     *
     * @param taskId        Task Id of Method
     * @param taskAddressId AddressID from which Task is Initiated
     */
    @SuppressWarnings("unchecked")
    private void callWSForPrefedQuotes(String taskId, String taskAddressId) {
        Log.d(TAG, "callWSForPrefedQuotes() called with: taskId = [" + taskId + "], taskAddressId = [" + taskAddressId + "]");

        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();

        // Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, userDetails.UserID);
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());

        // Add Params
        Map<String, String> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.TASK_ID, taskId);
        mParams.put(NetworkUtility.TAGS.ADDRESS_ID, taskAddressId);

        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.CURL_NOTIFICATION_TO_SP
                , new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.d(TAG, "onErrorResponse() called with: volleyError = [" + volleyError + "]");
            }
        }
                , new Response.Listener() {
            @Override
            public void onResponse(Object o) {
                Log.d(TAG, "onResponse() called with: o = [" + o + "]");
            }
        }
                , mHeaderParams
                , mParams
                , null);

        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest, NetworkUtility.WS.CURL_NOTIFICATION_TO_SP);
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


    Response.ErrorListener mCallGetProInstaBookErrorListener = new Response.ErrorListener() {
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
        Log.d(TAG, "onDestroy() called");
        try {
            mContext.unregisterReceiver(mBR_OnInstaTaskCreated);
            unregisterReceiver(mBR_OnLoginSuccess);
        } catch (Exception e) {
            Log.i(TAG, "onDestroy: ");
        }
        super.onDestroy();
    }

    /**
     * Location [END]
     */

    /**
     * BroadCast that would restart the screen once login has been done.
     */
    private BroadcastReceiver mBR_OnLoginSuccess = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive() called with: context = [" + context + "], intent = [" + intent + "]");
            Utility.hideKeyboard(mContext);
            // TODO : check here for user guest has selected insta booked or get quots
            Log.e(TAG, "isInstaBooking :: " + isInstaBooking);
            if (isInstaBooking)
                onInstaBookClicked();
            else
                onGetQuoteClicked();

            /**
             * As User is currently logged in, we need to add FullAddressModel to existing addresslist.
             */
            UserDetails mUserDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
            if (mUserDetails != null) {
                if (mUserDetails.addressList.isEmpty()) {
                    mUserDetails.addressList = new ArrayList<>();
                }

                // Add additional selected addressmodel here.
                mUserDetails.addressList.add(mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mSelectedAddressModel);

                // Save the user now.
                PreferenceUtility.getInstance(mContext).saveUserDetails(mUserDetails);
            }
        }
    };
}
