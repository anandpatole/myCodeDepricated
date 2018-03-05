package com.cheep.cheepcare.activity;

import android.content.Context;
import android.content.Intent;
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

import com.android.volley.VolleyError;
import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.cheepcare.adapter.TaskCreationPagerAdapter;
import com.cheep.cheepcare.fragment.FreeSubCategoryFragment;
import com.cheep.cheepcare.fragment.TaskCreationPhase2Fragment;
import com.cheep.cheepcare.model.AdminSettingModel;
import com.cheep.cheepcare.model.SubscribedTaskDetailModel;
import com.cheep.databinding.ActivityTaskCreateCcBinding;
import com.cheep.dialogs.CustomLoadingDialog;
import com.cheep.model.AddressModel;
import com.cheep.model.JobCategoryModel;
import com.cheep.model.MessageEvent;
import com.cheep.model.SubServiceDetailModel;
import com.cheep.utils.LogUtils;
import com.cheep.utils.Utility;
import com.cheep.utils.WebCallClass;
import com.google.android.gms.common.api.Status;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by bhavesh on 26/4/17.
 */
public class TaskCreationCCActivity extends BaseAppCompatActivity {
    private static final String TAG = TaskCreationCCActivity.class.getSimpleName();
    private ActivityTaskCreateCcBinding mBinding;
    public JobCategoryModel mJobCategoryModel;
    TaskCreationPagerAdapter mTaskCreationPagerAdapter;
    private List<SubServiceDetailModel> mSelectedSubServiceList;
    public ArrayList<AddressModel> mCareAddressList;
    Map<String, Object> mTaskCreationParams;
    CustomLoadingDialog mDialog;
    public AddressModel mAddressModel;
    public String mPackageType;
    public String mCarePackageId;
    public AdminSettingModel mAdminSettingModel;

    public static void getInstance(Context mContext, JobCategoryModel model, AddressModel addressModel, String packageType
            , String carePackageId, List<AddressModel> mSelectedAddressList, AdminSettingModel adminSettingModel) {
        Intent intent = new Intent(mContext, TaskCreationCCActivity.class);
        intent.putExtra(Utility.Extra.DATA, Utility.getJsonStringFromObject(model));
        intent.putExtra(Utility.Extra.DATA_2, packageType);
        intent.putExtra(Utility.Extra.DATA_3, Utility.getJsonStringFromObject(mSelectedAddressList));
        intent.putExtra(Utility.Extra.SELECTED_ADDRESS_MODEL, Utility.getJsonStringFromObject(addressModel));
        intent.putExtra(Utility.Extra.SELECTED_PACKAGE_ID, carePackageId);
        intent.putExtra(Utility.Extra.ADMIN_SETTING, Utility.getJsonStringFromObject(adminSettingModel));
//        ((Activity) mContext).startActivityForResult(intent, Utility.REQUEST_CODE_TASK_CREATION_CHEEP_CARE);
        mContext.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_task_create_cc);
        initiateUI();
        setListeners();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void initiateUI() {
        if (getIntent().getExtras() != null) {
            // Fetch JobCategory Model
            mJobCategoryModel = (JobCategoryModel) Utility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA), JobCategoryModel.class);
            mPackageType = getIntent().getStringExtra(Utility.Extra.DATA_2);
            mCarePackageId = getIntent().getStringExtra(Utility.Extra.SELECTED_PACKAGE_ID);
            mAddressModel = (AddressModel) Utility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.SELECTED_ADDRESS_MODEL), AddressModel.class);
            mAdminSettingModel = (AdminSettingModel) Utility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.ADMIN_SETTING), AdminSettingModel.class);
            mCareAddressList = Utility.getObjectListFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA_3), AddressModel[].class);
        }

        // Setting up Toolbar
        setSupportActionBar(mBinding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(null);
            mBinding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Utility.hideKeyboard(mContext);
                    onBackPressed();
                }
            });
        }

        showPostTaskButton(true, true);

        // Set category
        mBinding.textTitle.setText(mJobCategoryModel.catName != null ? mJobCategoryModel.catName : Utility.EMPTY_STRING);

        // Set up image
        Utility.loadImageView(mContext, mBinding.imgService, mJobCategoryModel.catImage, R.drawable.gradient_black);
//        Utility.loadImageView(mContext, mBinding.imgService, mJobCategoryModel.catImageExtras.thumb, R.drawable.gradient_black);

        // Setting viewpager
        setupViewPager(mBinding.viewpager);

        // Change description
        mBinding.textStepDesc.setText(getString(R.string.step_1_desc));

        // Set the default step
        setTaskState(STEP_ONE_NORMAL);

        // Manage Click events of TaskCreation steps
        mBinding.textStep1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoStep(STAGE_1);
            }
        });
        mBinding.textStep2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                  Need to check whether first step is verified or not.
                 */
                if (mCurrentStep > 2) {
                    gotoStep(STAGE_2);
                } else {
                    Utility.showSnackBar(getString(R.string.step_1_desc), mBinding.getRoot());
                }

            }
        });

    }

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == mBinding.textPostTask.getId()) {
                if (mBinding.viewpager.getCurrentItem() == mTaskCreationPagerAdapter.TASK_CREATION_PHASE_1_FRAGMENT) {
                    List<SubServiceDetailModel> list
                            = mTaskCreationPagerAdapter.mTaskCreationPhase1Fragment.getSelectedSubServices();
                    //lines to be uncommented
                    if (list != null && !list.isEmpty()) {
                        mSelectedSubServiceList = list;
                        gotoStep(STAGE_2);
                    } else {
                        Utility.showSnackBar(getString(R.string.step_1_desc), mBinding.getRoot());
                    }
                }
            }
        }
    };

    @Override
    protected void setListeners() {
        mBinding.textPostTask.setOnClickListener(mOnClickListener);
    }

    /**
     * This will setup the viewpager and tabs as well
     *
     * @param pager
     */
    private void setupViewPager(ViewPager pager) {
        mTaskCreationPagerAdapter = new TaskCreationPagerAdapter(getSupportFragmentManager());
        mTaskCreationPagerAdapter.addFragment(FreeSubCategoryFragment.TAG);
        mTaskCreationPagerAdapter.addFragment(TaskCreationPhase2Fragment.TAG);
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
                mBinding.textStep1.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_normal));
                mBinding.textStep1.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mBinding.textStep2.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_normal));
                mBinding.textStep2.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mBinding.textStep3.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_normal));
                mBinding.textStep3.setTextColor(ContextCompat.getColor(mContext, R.color.white));


                break;
            case STEP_ONE_UNVERIFIED:
                mBinding.textStep1.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_unverified));
                mBinding.textStep1.setTextColor(ContextCompat.getColor(mContext, R.color.splash_gradient_end));

                mBinding.textStep2.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_normal));
                mBinding.textStep2.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mBinding.textStep3.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_normal));
                mBinding.textStep3.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                break;
            case STEP_ONE_VERIFIED:
            case STEP_TWO_NORMAL:
                mBinding.textStep1.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_verified));
                mBinding.textStep1.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mBinding.textStep2.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_normal));
                mBinding.textStep2.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mBinding.textStep3.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_normal));
                mBinding.textStep3.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                break;
            case STEP_TWO_UNVERIFIED:
                mBinding.textStep1.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_verified));
                mBinding.textStep1.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mBinding.textStep2.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_unverified));
                mBinding.textStep2.setTextColor(ContextCompat.getColor(mContext, R.color.splash_gradient_end));

                mBinding.textStep3.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_normal));
                mBinding.textStep3.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                break;
            case STEP_TWO_VERIFIED:
            case STEP_THREE_NORMAL:
                mBinding.textStep1.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_verified));
                mBinding.textStep1.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mBinding.textStep2.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_verified));
                mBinding.textStep2.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mBinding.textStep3.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_normal));
                mBinding.textStep3.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                break;
            case STEP_THREE_UNVERIFIED:
                mBinding.textStep1.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_verified));
                mBinding.textStep1.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mBinding.textStep2.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_verified));
                mBinding.textStep2.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mBinding.textStep3.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_unverified));
                mBinding.textStep3.setTextColor(ContextCompat.getColor(mContext, R.color.splash_gradient_end));

                break;
            case STEP_THREE_VERIFIED:
                mBinding.textStep1.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_verified));
                mBinding.textStep1.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mBinding.textStep2.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_verified));
                mBinding.textStep2.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mBinding.textStep3.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_verified));
                mBinding.textStep3.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                break;
        }
    }

    public static final int STAGE_1 = 0;
    public static final int STAGE_2 = 1;

    public void gotoStep(int step) {
        switch (step) {
            case STAGE_1:
                mBinding.viewpager.setCurrentItem(0);
                // Change description
                mBinding.textStepDesc.setText(getString(R.string.step_1_desc));
                break;
            case STAGE_2:
                mBinding.viewpager.setCurrentItem(1);
                // Change description
                mBinding.textStepDesc.setText(getString(R.string.step_2_desc));
                break;
        }
    }


    @Override
    public void onBackPressed() {
        if (mBinding.viewpager.getCurrentItem() == 1) {
            gotoStep(STAGE_1);
            return;
        }
        super.onBackPressed();
    }

    public void showPostTaskButton(boolean needsToShow, boolean isEnabled) {

        if (needsToShow) {
            mBinding.textPostTask.setVisibility(View.VISIBLE);
        } else {
            mBinding.textPostTask.setVisibility(View.GONE);
        }


        if (isEnabled) {
            mBinding.textPostTask.setSelected(true);
//            mBinding.textPostTask.setBackgroundColor(ContextCompat.getColor(mContext, R.color.splash_gradient_end));
        } else {
            mBinding.textPostTask.setSelected(false);
//            mBinding.textPostTask.setBackgroundColor(ContextCompat.getColor(mContext, R.color.grey_varient_12));
        }
    }

    public int getPostButtonHeight() {
        return mBinding.textPostTask.getHeight();
    }

    public void setSelectedSubService(List<SubServiceDetailModel> subServiceList) {
        this.mSelectedSubServiceList = subServiceList;
    }

    public List<SubServiceDetailModel> getSelectedSubServices() {
        return mSelectedSubServiceList;
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
                    Utility.showSnackBar(getString(R.string.permission_denied_read), mBinding.getRoot());
                }
                break;
            case Utility.REQUEST_CODE_READ_EXTERNAL_STORAGE_ADD_PROFILE_GALLERY:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "onRequestPermissionsResult: Permission Granted");
                    // startIntentFileChooser(Utility.REQUEST_CODE_GET_FILE_ADD_PROFILE_GALLERY);
                } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Log.i(TAG, "onRequestPermissionsResult: Permission Denied");
                    Utility.showSnackBar(getString(R.string.permission_denied_read), mBinding.getRoot());
                }
                break;
            case Utility.REQUEST_CODE_ADD_PROFILE_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "onRequestPermissionsResult: Permission Granted");
                    //  startCameraCaptureChooser(Utility.REQUEST_CODE_IMAGE_CAPTURE_ADD_PROFILE);
                } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Log.i(TAG, "onRequestPermissionsResult: Permission Denied Camera");
                    Utility.showSnackBar(getString(R.string.permission_denied_camera), mBinding.getRoot());
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
            locationRequest.startResolutionForResult(TaskCreationCCActivity.this, Utility.REQUEST_CODE_CHECK_LOCATION_SETTINGS);
        } catch (Exception e) {
            // Ignore the error.
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////Post Task [Start] /////////////////////////////////////////
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
            EventBus.getDefault().unregister(this);

        } catch (Exception e) {
            Log.i(TAG, "onDestroy: ");
        }

        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        LogUtils.LOGE(TAG, "onMessageEvent: " + event.BROADCAST_ACTION);
        switch (event.BROADCAST_ACTION) {
            case Utility.BROADCAST_TYPE.TASK_PAID_FOR_INSTA_BOOKING:
                // br for finished task creation activity
                finish();
                break;
            case Utility.BROADCAST_TYPE.PACKAGE_SUBSCRIBED_SUCCESSFULLY:
                finish();
                break;
            case Utility.BROADCAST_TYPE.SUBSCRIBED_TASK_CREATE_SUCCESSFULLY:
                finish();
                break;
        }

    }

    private final WebCallClass.CommonResponseListener mCommonResponseListener =
            new WebCallClass.CommonResponseListener() {
                @Override
                public void volleyError(VolleyError error) {
                    Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");
                    hideProgressDialog();
                    Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
                }

                @Override
                public void showSpecificMessage(String message) {
                    hideProgressDialog();
                    // Show message
                    Utility.showSnackBar(message, mBinding.getRoot());
                }

                @Override
                public void forceLogout() {
                    hideProgressDialog();
                    finish();
                }
            };


    //TODO: to be removed
    public void startBookingConfirmationActivity() {


        if (mTaskCreationPagerAdapter.mTaskCreationPhase2Fragment.mSelectedAddress.is_subscribe.equalsIgnoreCase(Utility.BOOLEAN.NO) &&
                TextUtils.isEmpty(mTaskCreationPagerAdapter.mTaskCreationPhase2Fragment.getStartDateTime())) {
            Utility.showSnackBar(getString(R.string.validate_date), mBinding.getRoot());
            return;
        }

        SubscribedTaskDetailModel subscribedTaskDetailModel = new SubscribedTaskDetailModel();
        subscribedTaskDetailModel.addressModel = mTaskCreationPagerAdapter.mTaskCreationPhase2Fragment.mSelectedAddress;
        subscribedTaskDetailModel.jobCategoryModel = mJobCategoryModel;
        subscribedTaskDetailModel.adminSettingModel = mAdminSettingModel;
        subscribedTaskDetailModel.carePackageId = mCarePackageId;
        subscribedTaskDetailModel.freeServiceList = mTaskCreationPagerAdapter.mTaskCreationPhase1Fragment.getSelectedFreeServices();
        subscribedTaskDetailModel.paidServiceList = mTaskCreationPagerAdapter.mTaskCreationPhase1Fragment.getSelectedPaidServices();
        subscribedTaskDetailModel.startDateTime = mTaskCreationPagerAdapter.mTaskCreationPhase2Fragment.getStartDateTime();
        subscribedTaskDetailModel.taskDesc = mTaskCreationPagerAdapter.mTaskCreationPhase2Fragment.getTaskDescription();
        subscribedTaskDetailModel.taskType = subscribedTaskDetailModel.addressModel.is_subscribe.equalsIgnoreCase(Utility.BOOLEAN.YES) ? Utility.TASK_TYPE.SUBSCRIBED : Utility.TASK_TYPE.NORMAL;
        subscribedTaskDetailModel.mediaFileList = mTaskCreationPagerAdapter.mTaskCreationPhase2Fragment.mMediaRecycleAdapter.getList();


        BookingConfirmationCcActivity.newInstance(TaskCreationCCActivity.this, subscribedTaskDetailModel);
    }

    public void showSubscribedBadge(boolean show) {
        mBinding.imgSubscribed.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}

