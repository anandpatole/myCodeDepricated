package com.cheep.strategicpartner;

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
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.adapter.TaskCreationForStrategicPartnerPagerAdapter;
import com.cheep.databinding.ActivityTaskCreationForStrategicPartnerBinding;
import com.cheep.model.BannerImageModel;
import com.cheep.utils.Utility;
import com.google.android.gms.common.api.Status;

/**
 * Created by bhavesh on 26/4/17.
 */
public class StrategicPartnerTaskCreationAct extends BaseAppCompatActivity {
    private static final String TAG = "TaskCreationForStrategi";
    private ActivityTaskCreationForStrategicPartnerBinding mActivityTaskCreationForStrategicPartnerBinding;
    public BannerImageModel mBannerImageModel;
    TaskCreationForStrategicPartnerPagerAdapter mTaskCreationPagerAdapter;
    private String value;
    private String mSelectedQuestions;
    public boolean isSingleSelection = false;

    public static void getInstance(Context mContext, BannerImageModel model) {
        Intent intent = new Intent(mContext, StrategicPartnerTaskCreationAct.class);
        intent.putExtra(Utility.Extra.DATA, Utility.getJsonStringFromObject(model));
        mContext.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityTaskCreationForStrategicPartnerBinding = DataBindingUtil.setContentView(this, R.layout.activity_task_creation_for_strategic_partner);
        initiateUI();
    }

    @Override
    protected void initiateUI() {
        /**
         * Fetch data from Home Screen(Includes details about strategic Partners
         */
        if (getIntent().getExtras() != null) {
            // Fetch banner Model
            Log.e(TAG, " data " + getIntent().getStringExtra(Utility.Extra.DATA));
            mBannerImageModel = (BannerImageModel) Utility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA), BannerImageModel.class);
            if (mBannerImageModel != null) {
                Utility.loadImageView(mContext, mActivityTaskCreationForStrategicPartnerBinding.imgService, mBannerImageModel.imgCatImageUrl, R.drawable.gradient_black);
                isSingleSelection = mBannerImageModel.minimum_selection.equalsIgnoreCase("1");
                mActivityTaskCreationForStrategicPartnerBinding.textTitle.setText(mBannerImageModel.name != null ? mBannerImageModel.name : Utility.EMPTY_STRING);
            }
        }

        mActivityTaskCreationForStrategicPartnerBinding.textStepDesc.setText(getString(R.string.step_1_desc_for_strategic_partner));


        // manage Viewpager
        setupViewPager(mActivityTaskCreationForStrategicPartnerBinding.viewpager);

        // Setting up Toolbar
        setSupportActionBar(mActivityTaskCreationForStrategicPartnerBinding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(Utility.EMPTY_STRING);
            mActivityTaskCreationForStrategicPartnerBinding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Utility.hideKeyboard(mContext);
                    onBackPressed();
                }
            });
        }

        mActivityTaskCreationForStrategicPartnerBinding.textStep1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoStep(STAGE_1);
            }
        });
        mActivityTaskCreationForStrategicPartnerBinding.textStep2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                  Need to check whether first step is verified or not.
                 */
                if (mCurrentStep > 2) {
                    gotoStep(STAGE_2);
                } else {
                    Utility.showSnackBar(getString(R.string.step_1_desc_for_strategic_partner), mActivityTaskCreationForStrategicPartnerBinding.getRoot());
                }

            }
        });
        mActivityTaskCreationForStrategicPartnerBinding.textStep3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                  Need to check whether first step is verified or not.
                 */
                if (mCurrentStep > 5) {
                    gotoStep(STAGE_3);
                } else {
                    if (mCurrentStep < STEP_ONE_UNVERIFIED)
                        Utility.showSnackBar(getString(R.string.step_1_desc_for_strategic_partner), mActivityTaskCreationForStrategicPartnerBinding.getRoot());
                    else
                        Utility.showSnackBar(getString(R.string.step_2_desc_for_strategic_partner), mActivityTaskCreationForStrategicPartnerBinding.getRoot());
                }

            }
        });


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
        mTaskCreationPagerAdapter = new TaskCreationForStrategicPartnerPagerAdapter(getSupportFragmentManager());
        mTaskCreationPagerAdapter.addFragment(StrategicPartnerFragPhaseOne.TAG);
        mTaskCreationPagerAdapter.addFragment(StrategicPartnerFragPhaseTwo.TAG);
        mTaskCreationPagerAdapter.addFragment(StrategicPartnerFragPhaseThree.TAG);
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
                mActivityTaskCreationForStrategicPartnerBinding.textStep1.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_normal));
                mActivityTaskCreationForStrategicPartnerBinding.textStep1.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mActivityTaskCreationForStrategicPartnerBinding.textStep2.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_normal));
                mActivityTaskCreationForStrategicPartnerBinding.textStep2.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mActivityTaskCreationForStrategicPartnerBinding.textStep3.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_normal));
                mActivityTaskCreationForStrategicPartnerBinding.textStep3.setTextColor(ContextCompat.getColor(mContext, R.color.white));


                break;
            case STEP_ONE_UNVERIFIED:
                mActivityTaskCreationForStrategicPartnerBinding.textStep1.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_unverified));
                mActivityTaskCreationForStrategicPartnerBinding.textStep1.setTextColor(ContextCompat.getColor(mContext, R.color.splash_gradient_end));

                mActivityTaskCreationForStrategicPartnerBinding.textStep2.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_normal));
                mActivityTaskCreationForStrategicPartnerBinding.textStep2.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mActivityTaskCreationForStrategicPartnerBinding.textStep3.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_normal));
                mActivityTaskCreationForStrategicPartnerBinding.textStep3.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                break;
            case STEP_ONE_VERIFIED:
            case STEP_TWO_NORMAL:
                mActivityTaskCreationForStrategicPartnerBinding.textStep1.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_verified));
                mActivityTaskCreationForStrategicPartnerBinding.textStep1.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mActivityTaskCreationForStrategicPartnerBinding.textStep2.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_normal));
                mActivityTaskCreationForStrategicPartnerBinding.textStep2.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mActivityTaskCreationForStrategicPartnerBinding.textStep3.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_normal));
                mActivityTaskCreationForStrategicPartnerBinding.textStep3.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                break;
            case STEP_TWO_UNVERIFIED:
                mActivityTaskCreationForStrategicPartnerBinding.textStep1.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_verified));
                mActivityTaskCreationForStrategicPartnerBinding.textStep1.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mActivityTaskCreationForStrategicPartnerBinding.textStep2.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_unverified));
                mActivityTaskCreationForStrategicPartnerBinding.textStep2.setTextColor(ContextCompat.getColor(mContext, R.color.splash_gradient_end));

                mActivityTaskCreationForStrategicPartnerBinding.textStep3.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_normal));
                mActivityTaskCreationForStrategicPartnerBinding.textStep3.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                break;
            case STEP_TWO_VERIFIED:
            case STEP_THREE_NORMAL:
                mActivityTaskCreationForStrategicPartnerBinding.textStep1.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_verified));
                mActivityTaskCreationForStrategicPartnerBinding.textStep1.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mActivityTaskCreationForStrategicPartnerBinding.textStep2.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_verified));
                mActivityTaskCreationForStrategicPartnerBinding.textStep2.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mActivityTaskCreationForStrategicPartnerBinding.textStep3.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_normal));
                mActivityTaskCreationForStrategicPartnerBinding.textStep3.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                break;
            case STEP_THREE_UNVERIFIED:
                mActivityTaskCreationForStrategicPartnerBinding.textStep1.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_verified));
                mActivityTaskCreationForStrategicPartnerBinding.textStep1.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mActivityTaskCreationForStrategicPartnerBinding.textStep2.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_verified));
                mActivityTaskCreationForStrategicPartnerBinding.textStep2.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mActivityTaskCreationForStrategicPartnerBinding.textStep3.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_unverified));
                mActivityTaskCreationForStrategicPartnerBinding.textStep3.setTextColor(ContextCompat.getColor(mContext, R.color.splash_gradient_end));

                break;
            case STEP_THREE_VERIFIED:
                mActivityTaskCreationForStrategicPartnerBinding.textStep1.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_verified));
                mActivityTaskCreationForStrategicPartnerBinding.textStep1.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mActivityTaskCreationForStrategicPartnerBinding.textStep2.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_verified));
                mActivityTaskCreationForStrategicPartnerBinding.textStep2.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mActivityTaskCreationForStrategicPartnerBinding.textStep3.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_verified));
                mActivityTaskCreationForStrategicPartnerBinding.textStep3.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                break;
        }
    }

    public static final int STAGE_1 = 0;
    public static final int STAGE_2 = 1;
    public static final int STAGE_3 = 2;

    public void gotoStep(int step) {
        switch (step) {
            case STAGE_1:
                mActivityTaskCreationForStrategicPartnerBinding.viewpager.setCurrentItem(0);
                // Change description
                mActivityTaskCreationForStrategicPartnerBinding.textStepDesc.setText(getString(R.string.step_1_desc_for_strategic_partner));
                break;
            case STAGE_2:
                mActivityTaskCreationForStrategicPartnerBinding.viewpager.setCurrentItem(1);
                // Change description
                mActivityTaskCreationForStrategicPartnerBinding.textStepDesc.setText(getString(R.string.step_2_desc_for_strategic_partner));
                break;
            case STAGE_3:
                mActivityTaskCreationForStrategicPartnerBinding.viewpager.setCurrentItem(2);
                // Change description
                mActivityTaskCreationForStrategicPartnerBinding.textStepDesc.setText(getString(R.string.step_3_desc_for_strategic_partner));
                break;
        }
    }


    @Override
    public void onBackPressed() {
        if (mActivityTaskCreationForStrategicPartnerBinding.viewpager.getCurrentItem() == 1) {
            gotoStep(STAGE_1);
            return;
        }
        super.onBackPressed();
    }


    public void setSelectedSubService(String value) {
        this.value = value;
        Log.e(TAG, " on continue click");
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
                Utility.showSnackBar(getString(R.string.permission_denied_read), mActivityTaskCreationForStrategicPartnerBinding.getRoot());
            }
        } else if (requestCode == Utility.REQUEST_CODE_READ_EXTERNAL_STORAGE_ADD_PROFILE_GALLERY) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "onRequestPermissionsResult: Permission Granted");
                // startIntentFileChooser(Utility.REQUEST_CODE_GET_FILE_ADD_PROFILE_GALLERY);
            } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Log.i(TAG, "onRequestPermissionsResult: Permission Denied");
                Utility.showSnackBar(getString(R.string.permission_denied_read), mActivityTaskCreationForStrategicPartnerBinding.getRoot());
            }
        } else if (requestCode == Utility.REQUEST_CODE_ADD_PROFILE_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "onRequestPermissionsResult: Permission Granted");
                //  startCameraCaptureChooser(Utility.REQUEST_CODE_IMAGE_CAPTURE_ADD_PROFILE);
            } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Log.i(TAG, "onRequestPermissionsResult: Permission Denied Camera");
                Utility.showSnackBar(getString(R.string.permission_denied_camera), mActivityTaskCreationForStrategicPartnerBinding.getRoot());
            }
        }
    }

    @Override
    public void gpsEnabled() {
        super.gpsEnabled();
        // Show placepicker activity
//        mTaskCreationPagerAdapter.mStrategicPartnerFragPhaseThree.showPlacePickerDialog(true);
    }

    @Override
    public void onLocationSettingsDialogNeedToBeShow(Status locationRequest) {
        super.onLocationSettingsDialogNeedToBeShow(locationRequest);
        // Location settings are not satisfied, but this can be fixed
        // by showing the user a dialog.
        try {
            // Show the dialog by calling startResolutionForResult(),
            // and check the result in onActivityResult().
            locationRequest.startResolutionForResult(StrategicPartnerTaskCreationAct.this, Utility.REQUEST_CODE_CHECK_LOCATION_SETTINGS);
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

    public String getSelectedSubService() {
        return value;
    }

    public void setSelectedQuestions(String selectedQuestions) {
        mSelectedQuestions = selectedQuestions;
    }

    public String getSelectedQuestions() {
        return mSelectedQuestions;
    }
}
