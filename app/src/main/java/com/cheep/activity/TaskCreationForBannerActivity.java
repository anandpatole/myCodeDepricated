package com.cheep.activity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

import com.cheep.R;
import com.cheep.adapter.TaskCreationForBannerPagerAdapter;
import com.cheep.databinding.ActivityTaskCreateForBannerBinding;
import com.cheep.fragment.SelectBannerServicesFragment;
import com.cheep.fragment.ServiceDetailsFragment;
import com.cheep.fragment.ServiceQuestionsFragment;
import com.cheep.model.BannerImageModel;
import com.cheep.model.SubServiceDetailModel;
import com.cheep.utils.Utility;
import com.google.android.gms.common.api.Status;

/**
 * Created by bhavesh on 26/4/17.
 */
public class TaskCreationForBannerActivity extends BaseAppCompatActivity {
    private static final String TAG = "TaskCreationForBannerActivity";
    private ActivityTaskCreateForBannerBinding mActivityTaskCreateBinding;
    public BannerImageModel mBannerImageModel;
    TaskCreationForBannerPagerAdapter mTaskCreationPagerAdapter;
    private SubServiceDetailModel mSelectedSubServiceDetailModel;

    public static void getInstance(Context mContext, BannerImageModel model) {
        Intent intent = new Intent(mContext, TaskCreationForBannerActivity.class);
        intent.putExtra(Utility.Extra.DATA, Utility.getJsonStringFromObject(model));
        mContext.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityTaskCreateBinding = DataBindingUtil.setContentView(this, R.layout.activity_task_create_for_banner);
        initiateUI();
//        setListeners();
    }

    @Override
    protected void initiateUI() {
        if (getIntent().getExtras() != null) {
            // Fetch banner Model
            Log.e(TAG, " data " + getIntent().getStringExtra(Utility.Extra.DATA));
            mBannerImageModel = (BannerImageModel) Utility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA), BannerImageModel.class);
        }
        setupViewPager(mActivityTaskCreateBinding.viewpager);
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

    }


    @Override
    protected void setListeners() {

    }


    /**
     * This will setup the viewpager and tabs as well
     *
     * @param pager
     */
    private void setupViewPager(ViewPager pager) {
        mTaskCreationPagerAdapter = new TaskCreationForBannerPagerAdapter(getSupportFragmentManager());
        mTaskCreationPagerAdapter.addFragment(SelectBannerServicesFragment.TAG);
        mTaskCreationPagerAdapter.addFragment(ServiceQuestionsFragment.TAG);
        mTaskCreationPagerAdapter.addFragment(ServiceDetailsFragment.TAG);
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
        if (needsToShow) {
            mActivityTaskCreateBinding.textPostTask.setVisibility(View.VISIBLE);
        } else {
            mActivityTaskCreateBinding.textPostTask.setVisibility(View.GONE);
        }

        /*
          If shown make the color change accordingly
         */
        if (isEnabled) {
            mActivityTaskCreateBinding.textPostTask.setSelected(true);
            mActivityTaskCreateBinding.textPostTask.setBackgroundColor(ContextCompat.getColor(mContext, R.color.splash_gradient_end));
        } else {
            mActivityTaskCreateBinding.textPostTask.setSelected(false);
            mActivityTaskCreateBinding.textPostTask.setBackgroundColor(ContextCompat.getColor(mContext, R.color.grey_varient_12));
        }
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
//        mTaskCreationPagerAdapter.mServiceDetailsFragment.showPlacePickerDialog(true);
    }

    @Override
    public void onLocationSettingsDialogNeedToBeShow(Status locationRequest) {
        super.onLocationSettingsDialogNeedToBeShow(locationRequest);
        // Location settings are not satisfied, but this can be fixed
        // by showing the user a dialog.
        try {
            // Show the dialog by calling startResolutionForResult(),
            // and check the result in onActivityResult().
            locationRequest.startResolutionForResult(TaskCreationForBannerActivity.this, Utility.REQUEST_CODE_CHECK_LOCATION_SETTINGS);
        } catch (IntentSender.SendIntentException e) {
            // Ignore the error.
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Utility.REQUEST_CODE_CHECK_LOCATION_SETTINGS) {
            if (resultCode == RESULT_OK) {
                mLocationTrackService.requestLocationUpdate();
            }
        }
    }
}
