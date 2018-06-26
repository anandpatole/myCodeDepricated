package com.cheep.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
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
import com.cheep.firebase.FirebaseHelper;
import com.cheep.firebase.FirebaseUtils;
import com.cheep.firebase.model.ChatTaskModel;
import com.cheep.fragment.EnterTaskDetailFragment;
import com.cheep.fragment.SelectSubCategoryFragment;
import com.cheep.model.AddressModel;
import com.cheep.model.JobCategoryModel;
import com.cheep.model.MessageEvent;
import com.cheep.model.SubServiceDetailModel;
import com.cheep.model.TaskDetailModel;
import com.cheep.model.UserDetails;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.GsonUtility;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;
import com.google.android.gms.common.api.Status;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.cheep.network.NetworkUtility.TAGS.CAT_ID;
import static com.cheep.network.NetworkUtility.TAGS.CAT_SLUG;

/**
 * Created by bhavesh on 26/4/17.
 */
public class TaskCreationActivity extends BaseAppCompatActivity {
    private static final String TAG = TaskCreationActivity.class.getSimpleName();
    private ActivityTaskCreateBinding mActivityTaskCreateBinding;
    public JobCategoryModel mJobCategoryModel;
    public TaskCreationPagerAdapter mTaskCreationPagerAdapter;
    Map<String, Object> mTaskCreationParams;
    CustomLoadingDialog mDialog;
    public ArrayList<SubServiceDetailModel> allSubCategoryList;
    public ArrayList<SubServiceDetailModel.PackageData> pestControlPackageDataList;
    String additionalChargeReason;

    public static void getInstance(Context mContext, JobCategoryModel model) {
        Intent intent = new Intent(mContext, TaskCreationActivity.class);
        Log.e(TAG, "getInstance: " + GsonUtility.getJsonStringFromObject(model));
        intent.putExtra(Utility.Extra.DATA, GsonUtility.getJsonStringFromObject(model));
        mContext.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityTaskCreateBinding = DataBindingUtil.setContentView(this, R.layout.activity_task_create);
        initiateUI();
        setListeners();
        EventBus.getDefault().register(this);
        registerReceiver(mBR_OnLoginSuccess, new IntentFilter(Utility.BR_ON_LOGIN_SUCCESS));
    }

    @Override
    protected void initiateUI() {

        mActivityTaskCreateBinding.progressBar.setVisibility(View.GONE);
        if (getIntent().getExtras() != null) {
            // Fetch JobCategory Model
            mJobCategoryModel = (JobCategoryModel) GsonUtility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA), JobCategoryModel.class);
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

        // Change description
        mActivityTaskCreateBinding.textStepDesc.setText(getString(R.string.step_1_desc));

        fetchListOfSubCategory(mJobCategoryModel.catId, mJobCategoryModel.catSlug);

    }

    @Override
    protected void setListeners() {

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

        mActivityTaskCreateBinding.textPostTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Make the status Verified
                        mTaskCreationPagerAdapter.mSelectSubCategoryFragment.isVerified = true;

                        //Alert The activity that step one is been verified.
                        setTaskState(TaskCreationActivity.STEP_ONE_NORMAL);
                        gotoStep(TaskCreationActivity.STAGE_2);
                    }
                }, 500);


            }
        });
    }


    /**
     * This will setup the viewpager and tabs as well
     *
     * @param pager
     * @param title
     * @param subTitle
     */
    private void setupViewPager(ViewPager pager, String title, String subTitle) {
        mTaskCreationPagerAdapter = new TaskCreationPagerAdapter(getSupportFragmentManager());
        mTaskCreationPagerAdapter.addFragment(SelectSubCategoryFragment.TAG);
        mTaskCreationPagerAdapter.addFragment(EnterTaskDetailFragment.TAG);
        pager.setAdapter(mTaskCreationPagerAdapter);

        // set layout cheep tip
        mTaskCreationPagerAdapter.mSelectSubCategoryFragment.setCheepTipUI(title, subTitle);

        // Set the default step
        if (allSubCategoryList.isEmpty()) {
            setTaskState(STEP_TWO_NORMAL);
            gotoStep(STAGE_2);
            mActivityTaskCreateBinding.textStep1.setEnabled(false);
        } else {
            setTaskState(STEP_ONE_NORMAL);
        }
    }

    /**
     * Below would manage the state of Step while creating task creation
     */
    public static final int STEP_ONE_NORMAL = 1;
    public static final int STEP_TWO_NORMAL = 4;
    public int mCurrentStep = -1;

    public void setTaskState(int step_state) {
        mCurrentStep = step_state;
        switch (step_state) {
            case STEP_ONE_NORMAL:
                mActivityTaskCreateBinding.textStep1.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_unverified));
                mActivityTaskCreateBinding.textStep1.setTextColor(ContextCompat.getColor(mContext, R.color.splash_gradient_end));

                mActivityTaskCreateBinding.textStep2.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_normal));
                mActivityTaskCreateBinding.textStep2.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mActivityTaskCreateBinding.textStep3.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_normal));
                mActivityTaskCreateBinding.textStep3.setTextColor(ContextCompat.getColor(mContext, R.color.white));


                break;
            case STEP_TWO_NORMAL:
                mActivityTaskCreateBinding.textStep1.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_normal));
                mActivityTaskCreateBinding.textStep1.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mActivityTaskCreateBinding.textStep2.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_unverified));
                mActivityTaskCreateBinding.textStep2.setTextColor(ContextCompat.getColor(mContext, R.color.splash_gradient_end));

                mActivityTaskCreateBinding.textStep3.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_normal));
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
        if (mActivityTaskCreateBinding.viewpager.getCurrentItem() == 1 && (allSubCategoryList != null && allSubCategoryList.size() > 0)) {
            gotoStep(STAGE_1);
            return;
        }
        super.onBackPressed();
    }

    public void showPostTaskButton(final boolean needsToShow, boolean isEnabled) {

        mActivityTaskCreateBinding.textPostTask.post(new Runnable() {
            @Override
            public void run() {
                if (needsToShow) {
                    mActivityTaskCreateBinding.textPostTask.setVisibility(View.VISIBLE);
                } else {
                    mActivityTaskCreateBinding.textPostTask.setVisibility(View.GONE);
                }
            }
        });

        if (isEnabled) {
            mActivityTaskCreateBinding.textPostTask.setSelected(true);
            mActivityTaskCreateBinding.textPostTask.setEnabled(true);
            mActivityTaskCreateBinding.textPostTask.setBackgroundColor(ContextCompat.getColor(mContext, R.color.splash_gradient_end));
        } else {
            mActivityTaskCreateBinding.textPostTask.setSelected(false);
            mActivityTaskCreateBinding.textPostTask.setEnabled(false);
            mActivityTaskCreateBinding.textPostTask.setBackgroundColor(ContextCompat.getColor(mContext, R.color.grey_varient_12));
        }

    }

    public int getPostButtonHeight() {
        return mActivityTaskCreateBinding.textPostTask.getHeight();
    }

    /*public void setSelectedSubService(SubServiceDetailModel subServiceDetailModel) {
        this.mSelectedSubServiceDetailModel = subServiceDetailModel;
    }

    public SubServiceDetailModel getSelectedSubService() {
        return mSelectedSubServiceDetailModel;
    }
*/
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

    /**
     * this is new method of instabook flow from here user wil navigated to booking confirmation screen
     */
    public void onInstaBookClickedNew() {
        if (PreferenceUtility.getInstance(mContext).getUserDetails() == null) {
            LoginActivity.newInstance(mContext);
            return;
        }

        TaskDetailModel taskDetailModel = new TaskDetailModel();

//                    taskDetailModel.categoryName = mJobCategoryModel.catName;

        taskDetailModel.additionalChargeReason = mTaskCreationPagerAdapter.mEnterTaskDetailFragment.additionalChargeReason;
        taskDetailModel.catPrice = mJobCategoryModel.catPrice;

//                    taskDetailModel.categoryName = mJobCategoryModel.cat
        if (!getSubCatList().isEmpty()) {

            taskDetailModel.subCategoryName = getSubCatList().get(0).name;

            taskDetailModel.subCategoryID = String.valueOf(getSubCatList().get(0).sub_cat_id);
            taskDetailModel.subCatList = mTaskCreationPagerAdapter.mSelectSubCategoryFragment.getSubCatList();
        } else {
            taskDetailModel.subCategoryName = "";
            taskDetailModel.subCategoryID = "";
            taskDetailModel.subCatList = null;
        }

        if (taskDetailModel.taskAddress == null)
            taskDetailModel.taskAddress = new AddressModel();
        taskDetailModel.taskAddress.address = mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mSelectedAddress.address;
        taskDetailModel.taskAddress.address_id = mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mSelectedAddress.address_id;
//        taskDetailModel.taskAddressId = mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mSelectedAddressModel.address_id;
//                    taskDetailModel.categoryId = mJobCategoryModel.catId;
//        taskDetailModel.taskDesc = mTaskCreationPagerAdapter.mEnterTaskDetailFragment.getTaskDescription();
//                    taskDetailModel.catImage = mJobCategoryModel.catImage;
        taskDetailModel.categoryModel = mJobCategoryModel;
        taskDetailModel.taskStartdate = String.valueOf(mTaskCreationPagerAdapter.mEnterTaskDetailFragment.superCalendar.getCalendar().getTimeInMillis());


//                    model.taskImage = mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mCurrentPhotoPath;
//        taskDetailModel.mMediaModelList = mTaskCreationPagerAdapter.mEnterTaskDetailFragment.getMediaList();

        taskDetailModel.subCatList = mTaskCreationPagerAdapter.mSelectSubCategoryFragment.getSubCatList();

//                    model.taskImage = mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mCurrentPhotoPath;
//        taskDetailModel.mMediaModelList = mTaskCreationPagerAdapter.mEnterTaskDetailFragment.getMediaList();
        if (mJobCategoryModel.isSubscribed.equalsIgnoreCase(Utility.BOOLEAN.YES)) {
            if (mJobCategoryModel.catSlug.equalsIgnoreCase(Utility.CAT_SLUG_TYPES.PEST_CONTROL)) {
                if (taskDetailModel.taskAddress.is_subscribe.equalsIgnoreCase(Utility.ADDRESS_SUBSCRIPTION_TYPE.PREMIUM)) {
                    taskDetailModel.taskType = Utility.TASK_TYPE.SUBSCRIBED;
                } else {
                    taskDetailModel.taskType = Utility.TASK_TYPE.NORMAL;
                }
            } else {
                taskDetailModel.taskType = Utility.TASK_TYPE.SUBSCRIBED;
            }
        } else {
            taskDetailModel.taskType = Utility.TASK_TYPE.NORMAL;
        }

        taskDetailModel.taskStatus = Utility.TASK_STATUS.PENDING;

        BookingConfirmationInstaActivity.newInstance(TaskCreationActivity.this, taskDetailModel, mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mSelectedAddress);
    }

/*
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
            Utility.showSnackBar(getString(R.string.can_only_start_task_after_3_hours, "3"), mActivityTaskCreateBinding.getRoot());
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
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, userDetails.userID);
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());

        // Add Params
        Map<String, Object> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.CITY_ID, userDetails.CityID);
        mParams.put(NetworkUtility.TAGS.CAT_ID, mJobCategoryModel.catId);
        if (Integer.parseInt(mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mSelectedAddressModel.address_id) > 0) {
            mParams.put(NetworkUtility.TAGS.ADDRESS_ID, mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mSelectedAddressModel.address_id);
        } else {
            // In case its nagative then provide other address information
            */
/*
             public String address_initials;
             public String address;
             public String category; //comes from NetworkUtility.TAGS.ADDRESS_TYPE.
             public String lat;
             public String lng;
             *//*

            mParams = NetworkUtility.addGuestAddressParams(mParams, mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mSelectedAddressModel);

        }
        mParams.put(NetworkUtility.TAGS.SUBCATEGORY_ID, String.valueOf(mSelectedSubServiceDetailModel.sub_cat_id));

        mTaskCreationParams = new HashMap<>();
        mTaskCreationParams.put(NetworkUtility.TAGS.CITY_ID, userDetails.CityID);
        mTaskCreationParams.put(NetworkUtility.TAGS.CAT_ID, mJobCategoryModel.catId);
        if (Integer.parseInt(mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mSelectedAddressModel.address_id) > 0) {
            mParams.put(NetworkUtility.TAGS.ADDRESS_ID, mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mSelectedAddressModel.address_id);
        } else {
            // In case its nagative then provide other address information
            */
/*
             public String address_initials;
             public String address;
             public String category; //comes from NetworkUtility.TAGS.ADDRESS_TYPE.
             public String lat;
             public String lng;
             *//*

            mTaskCreationParams = NetworkUtility.addGuestAddressParams(mTaskCreationParams, mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mSelectedAddressModel);
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
*/

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
//                        TaskDetailModel taskDetailModel = (TaskDetailModel) GsonUtility.getObjectFromJsonString(jsonObject.optString(NetworkUtility.TAGS.DATA), TaskDetailModel.class);
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


    /**
     * This method would going to call when task completed successfully
     */
    private void onSuccessfullTaskCompletion(JSONObject jsonObject) {
        final TaskDetailModel taskDetailModel = (TaskDetailModel) GsonUtility.getObjectFromJsonString(jsonObject.optString(NetworkUtility.TAGS.DATA), TaskDetailModel.class);
        if (taskDetailModel != null) {
            /* * Add new task detail on firebase
             * @Sanjay 20 Feb 2016
             */
            ChatTaskModel chatTaskModel = new ChatTaskModel();
            chatTaskModel.taskId = FirebaseUtils.getPrefixTaskId(taskDetailModel.taskId);
            chatTaskModel.taskDesc = taskDetailModel.taskDesc;
            chatTaskModel.categoryId = taskDetailModel.categoryModel.catId;
            chatTaskModel.categoryName = taskDetailModel.categoryModel.catName;
            chatTaskModel.selectedSPId = "";
            UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
            chatTaskModel.userId = FirebaseUtils.getPrefixUserId(userDetails.userID);
            FirebaseHelper.getTaskRef(chatTaskModel.taskId).setValue(chatTaskModel);
        }

        String message = mContext.getString(R.string.desc_task_creation_acknowledgement
                , PreferenceUtility.getInstance(mContext).getUserDetails().userName);
        String title = mContext.getString(R.string.label_your_task_is_posted);
        AcknowledgementDialogWithoutProfilePic mAcknowledgementDialogWithoutProfilePic = AcknowledgementDialogWithoutProfilePic.newInstance(R.drawable.dialog_top_bird_heart, title, message, new AcknowledgementInteractionListener() {

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
                    intent.putExtra(Utility.Extra.DATA, GsonUtility.getJsonStringFromObject(taskDetailModel));
                    Log.d(TAG, "onAcknowledgementAccepted: prefed >>>>> " + taskDetailModel.isPrefedQuote);
                    intent.putExtra(Utility.Extra.IS_INSTA_BOOKING_TASK, Utility.BOOLEAN.NO.equalsIgnoreCase(taskDetailModel.isPrefedQuote));
                    sendBroadcast(intent);
                } else {
                    HomeActivity.newInstance(mContext, null);
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
        callWSForPrefedQuotes(taskDetailModel.taskId, taskDetailModel.taskAddress.address_id);


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
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, userDetails.userID);
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
            unregisterReceiver(mBR_OnLoginSuccess);
            EventBus.getDefault().unregister(this);

        } catch (Exception e) {
            Log.i(TAG, "onDestroy: ");
        }

        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        if (event.BROADCAST_ACTION == Utility.BROADCAST_TYPE.TASK_PAID_FOR_INSTA_BOOKING) {
            // br for finished task creation activity
            finish();
        }

    }


    /**
     * BroadCast that would restart the screen once login has been done.
     */
    private BroadcastReceiver mBR_OnLoginSuccess = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Utility.hideKeyboard(mContext);
            // check here for user guest has selected insta booked or get quots
//                onInstaBookClicked();
            onInstaBookClickedNew();

            /**
             * As User is currently logged in, we need to add FullAddressModel to existing addresslist.
             */
            UserDetails mUserDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
            if (mUserDetails != null) {
                if (mUserDetails.addressList.isEmpty()) {
                    mUserDetails.addressList = new ArrayList<>();
                }

                // Add additional selected addressmodel here.
                mUserDetails.addressList.add(mTaskCreationPagerAdapter.mEnterTaskDetailFragment.mSelectedAddress);

                // Save the user now.
                PreferenceUtility.getInstance(mContext).saveUserDetails(mUserDetails);
            }
        }
    };

    public ArrayList<SubServiceDetailModel> getSubCatList() {
        return mTaskCreationPagerAdapter.mSelectSubCategoryFragment.getSubCatList();

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////// Fetch SubService Listing[START] /////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    private void fetchListOfSubCategory(String catId, String catSlug) {
        Log.d(TAG, "fetchListOfSubCategory() called with: catId = [" + catId + "]");
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityTaskCreateBinding.getRoot());
            return;
        }

        //Add Header parameters
        mActivityTaskCreateBinding.progressBar.setVisibility(View.VISIBLE);

        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        if (PreferenceUtility.getInstance(mContext).getUserDetails() != null) {
            mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().userID);
        }

        //Add Params
        Map<String, String> mParams = new HashMap<>();
        mParams.put(CAT_ID, catId);
        mParams.put(CAT_SLUG, catSlug);

        VolleyNetworkRequest mVolleyNetworkRequestForCategoryList = new VolleyNetworkRequest(NetworkUtility.WS.FETCH_SUB_SERVICE_LIST
                , mCallFetchSubServiceListingWSErrorListener
                , mCallFetchSubServiceListingWSResponseListener
                , mHeaderParams
                , mParams
                , null);

        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForCategoryList, NetworkUtility.WS.FETCH_SUB_SERVICE_LIST);
    }


    Response.Listener mCallFetchSubServiceListingWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {
            mActivityTaskCreateBinding.progressBar.setVisibility(View.GONE);
            Log.d(TAG, "onResponse() called with: response = [" + response + "]");
            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
                        JSONObject jsonObject1 = jsonObject.optJSONObject(NetworkUtility.TAGS.DATA);
                        JSONArray jsonArray = jsonObject1.optJSONArray(NetworkUtility.TAGS.SUB_CATS);
                        JSONObject cheepTip = jsonObject1.optJSONObject(NetworkUtility.TAGS.CATEGORY_TIP);
                        JSONArray packageData = jsonObject1.optJSONArray(NetworkUtility.TAGS.PACKAGE_DATA);

                        String title = cheepTip.optString(NetworkUtility.TAGS.TITLE);
                        String subTitle = cheepTip.optString(NetworkUtility.TAGS.SUBTITLE);

                        allSubCategoryList = GsonUtility.getObjectListFromJsonString(jsonArray.toString(), SubServiceDetailModel[].class);
                        pestControlPackageDataList = GsonUtility.getObjectListFromJsonString(packageData.toString(), SubServiceDetailModel.PackageData[].class);
                        if (mJobCategoryModel.catSlug.equalsIgnoreCase(Utility.CAT_SLUG_TYPES.PEST_CONTROL)) {
                            SubServiceDetailModel additional = new SubServiceDetailModel();
                            additional.catId = "-1";
                            additional.isSelected = false;
                            additional.name = Utility.NEED_HELP;
                            additional.subSubCatModels = null;
                            allSubCategoryList.add(additional);
                        }
                        // Setting viewpager
                        setupViewPager(mActivityTaskCreateBinding.viewpager, title, subTitle);


                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityTaskCreateBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);

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
                mCallFetchSubServiceListingWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }

        }
    };


    Response.ErrorListener mCallFetchSubServiceListingWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityTaskCreateBinding.getRoot());
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////// Fetch SubService Listing[END] ////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

}
