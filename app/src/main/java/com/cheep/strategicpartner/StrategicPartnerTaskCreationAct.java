package com.cheep.strategicpartner;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.activity.ZoomImageActivity;
import com.cheep.adapter.TaskCreationForStrategicPartnerPagerAdapter;
import com.cheep.cheepcare.adapter.MediaFullScreenAdapter;
import com.cheep.databinding.ActivityTaskCreationForStrategicPartnerBinding;
import com.cheep.model.AddressModel;
import com.cheep.model.BannerImageModel;
import com.cheep.model.MessageEvent;
import com.cheep.model.SubServiceDetailModel;
import com.cheep.strategicpartner.model.SubSubCatModel;
import com.cheep.model.MediaModel;
import com.cheep.strategicpartner.model.QueAnsModel;
import com.cheep.utils.AmazonUtils;
import com.cheep.utils.GlideUtility;
import com.cheep.utils.GsonUtility;
import com.cheep.utils.LogUtils;
import com.cheep.utils.Utility;
import com.google.android.gms.common.api.Status;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

/**
 * Created by Giteeka on 20/7/17.
 * This activity is Specifically for Strategic partner feature
 * This includes 3 Step
 * Phase 1 - service selection
 * Phase 2 - Questionnary
 * Phase 3 - Payment summary
 * logic to update status of step number in header
 * location services for address
 */
public class StrategicPartnerTaskCreationAct extends BaseAppCompatActivity {
    private static final String TAG = "StrategicPartnerTaskCre";

    private ActivityTaskCreationForStrategicPartnerBinding mActivityTaskCreationForStrategicPartnerBinding;
    public BannerImageModel mBannerImageModel;
    private ArrayList<QueAnsModel> mQuestionsList;
    private ArrayList<SubServiceDetailModel> mSelectedServicesList;
    public boolean isSingleSelection = false;
    public String spUserId = "";
    public boolean isPayNow = false;

    @Nullable
    public AddressModel mSelectedAddressModel;
    //
    public String totalOfGSTPrice = "";
    public String totalOfBasePrice = "";
    private TaskCreationForStrategicPartnerPagerAdapter taskCreationPagerAdapter;

    //variables for add media//
    private float itemWidth;
    //    private float padding;
    private float allPixels;
    private MediaFullScreenAdapter mediaFullScreenAdapter;
    private int expectedPosition;
    //variables for add media//

    public static void getInstance(Context mContext, BannerImageModel model) {
        Intent intent = new Intent(mContext, StrategicPartnerTaskCreationAct.class);
        intent.putExtra(Utility.Extra.DATA, GsonUtility.getJsonStringFromObject(model));
        mContext.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityTaskCreationForStrategicPartnerBinding = DataBindingUtil.setContentView(this, R.layout.activity_task_creation_for_strategic_partner);
        EventBus.getDefault().register(this);
        initiateUI();
        setListeners();
    }

    @Override
    protected void initiateUI() {
        /*
          Fetch data from Home Screen(Includes details about strategic Partners
         */

        setTaskState(STEP_ONE_UNVERIFIED);


        if (getIntent().getExtras() != null) {
            // Fetch banner Model
            LogUtils.LOGE(TAG, " data " + getIntent().getStringExtra(Utility.Extra.DATA));
            mBannerImageModel = (BannerImageModel) GsonUtility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA), BannerImageModel.class);
            if (mBannerImageModel != null) {
                // Load PRO image
                GlideUtility.showCircularImageViewWithColorBorder(mContext, TAG, mActivityTaskCreationForStrategicPartnerBinding.imgLogo, mBannerImageModel.imgCatImageUrl, Utility.DEFAULT_CHEEP_LOGO, R.color.dark_blue_variant_1, true);

                isSingleSelection = mBannerImageModel.minimum_selection.equalsIgnoreCase("1");
                mActivityTaskCreationForStrategicPartnerBinding.textTitle.setText(mBannerImageModel.name != null ? mBannerImageModel.name : Utility.EMPTY_STRING);
            }
        }

        mActivityTaskCreationForStrategicPartnerBinding.imgLogo.setVisibility(View.GONE);
//        GlideUtility.loadImageView(this, mActivityTaskCreationForStrategicPartnerBinding.imgLogo, mBannerImageModel.imgCatImageUrl, R.drawable.icon_profile_img);
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
//                gotoStep(STAGE_1);
            }
        });
        mActivityTaskCreationForStrategicPartnerBinding.textStep2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                  Need to check whether first step is verified or not.
                 */
//                if (mCurrentStep > 2) {
//                    gotoStep(STAGE_2);
//                } else {
//                    Utility.showSnackBar(getString(R.string.step_1_desc_for_strategic_partner), mActivityTaskCreationForStrategicPartnerBinding.getRoot());
//                }

            }
        });
        mActivityTaskCreationForStrategicPartnerBinding.textStep3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                  Need to check whether first step is verified or not.
                 */
//                if (mCurrentStep > 5) {
//                    gotoStep(STAGE_3);
//                } else {
//                    if (mCurrentStep < STEP_ONE_UNVERIFIED)
//                        Utility.showSnackBar(getString(R.string.step_1_desc_for_strategic_partner), mActivityTaskCreationForStrategicPartnerBinding.getRoot());
//                    else
//                        Utility.showSnackBar(getString(R.string.step_2_desc_for_strategic_partner), mActivityTaskCreationForStrategicPartnerBinding.getRoot());
//                }

            }
        });



        // Calculat Pager Height and Width
        ViewTreeObserver mViewTreeObserver = mActivityTaskCreationForStrategicPartnerBinding.frameBannerImage.getViewTreeObserver();
        mViewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mActivityTaskCreationForStrategicPartnerBinding.frameBannerImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int width = mActivityTaskCreationForStrategicPartnerBinding.frameBannerImage.getMeasuredWidth();
                ViewGroup.LayoutParams params = mActivityTaskCreationForStrategicPartnerBinding.frameBannerImage.getLayoutParams();
                params.height = Utility.getHeightFromWidthForTwoOneRatio(width);
                mActivityTaskCreationForStrategicPartnerBinding.frameBannerImage.setLayoutParams(params);

                // Load the image now.
                GlideUtility.loadImageView(mContext, mActivityTaskCreationForStrategicPartnerBinding.imgService, mBannerImageModel.bannerImage, R.drawable.gradient_black);
            }
        });


        initMediaRecyclerView();

    }
    private void initMediaRecyclerView() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        itemWidth = getResources().getDimension(R.dimen.item_width);
//        padding = (size.x - itemWidth) / 2;

        allPixels = 0;

        LinearLayoutManager shopItemslayoutManager = new LinearLayoutManager(getApplicationContext());
        shopItemslayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mActivityTaskCreationForStrategicPartnerBinding.addMedia.itemList.setLayoutManager(shopItemslayoutManager);

        mActivityTaskCreationForStrategicPartnerBinding.addMedia.itemList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                synchronized (this) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        calculatePositionAndScroll(recyclerView);
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                allPixels += dx;
            }
        });

        mediaFullScreenAdapter = new MediaFullScreenAdapter(mediaInteractionListener);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        mActivityTaskCreationForStrategicPartnerBinding.addMedia.itemList.setLayoutManager(layoutManager);
        mActivityTaskCreationForStrategicPartnerBinding.addMedia.itemList.setAdapter(mediaFullScreenAdapter);
    }
    private void calculatePositionAndScroll(RecyclerView recyclerView) {
        expectedPosition = Math.round((allPixels/* + padding*/) / itemWidth);
        scrollListToPosition(recyclerView, expectedPosition);
        mActivityTaskCreationForStrategicPartnerBinding.addMedia.tvNumberOfMedia.setText(getString(R.string.number_of_media, String.valueOf(expectedPosition + 1/* + 1*/)
                , String.valueOf(mediaFullScreenAdapter.getListSize())));
    }

    private void scrollListToPosition(RecyclerView recyclerView, int expectedPosition) {
        float targetScrollPos = expectedPosition * itemWidth/* - padding*/;
        float missingPx = targetScrollPos - allPixels;
        if (missingPx != 0) {
            recyclerView.smoothScrollBy((int) missingPx, 0);
        }
    }

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.iv_hide_media_ui:
                    mActivityTaskCreationForStrategicPartnerBinding.addMedia.getRoot().setVisibility(View.GONE);
                    break;
                case R.id.iv_plus:
                    taskCreationPagerAdapter.mStrategicPartnerFragPhaseTwo.showMediaChooserDialog();
                    break;
            }
        }
    };
    @Override
    protected void setListeners() {
        mActivityTaskCreationForStrategicPartnerBinding.addMedia.ivHideMediaUi.setOnClickListener(mOnClickListener);
        mActivityTaskCreationForStrategicPartnerBinding.addMedia.ivPlus.setOnClickListener(mOnClickListener);
    }


    /**
     * This will setup the viewpager and tabs as well
     *
     * @param pager view pager for 3 steps
     */
    private void setupViewPager(ViewPager pager) {
        taskCreationPagerAdapter = new TaskCreationForStrategicPartnerPagerAdapter(getSupportFragmentManager());
        taskCreationPagerAdapter.addFragment(StrategicPartnerFragPhaseOne.TAG);
        taskCreationPagerAdapter.addFragment(StrategicPartnerFragPhaseTwo.TAG);
        taskCreationPagerAdapter.addFragment(StrategicPartnerFragPhaseThree.TAG);
        pager.setOffscreenPageLimit(2);
        pager.setAdapter(taskCreationPagerAdapter);
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

    private static final int STAGE_1 = 0;
    public static final int STAGE_2 = 1;
    public static final int STAGE_3 = 2;

    public void gotoStep(int step) {
        switch (step) {
            case STAGE_1:
                mActivityTaskCreationForStrategicPartnerBinding.viewpager.setCurrentItem(0);
                setTaskState(STEP_ONE_UNVERIFIED);
                // Change description
                mActivityTaskCreationForStrategicPartnerBinding.imgLogo.setVisibility(View.GONE);
                mActivityTaskCreationForStrategicPartnerBinding.textStepDesc.setText(getString(R.string.step_1_desc_for_strategic_partner));
                break;
            case STAGE_2:
                mActivityTaskCreationForStrategicPartnerBinding.viewpager.setCurrentItem(1);
                setTaskState(STEP_TWO_UNVERIFIED);
                // Change description
                mActivityTaskCreationForStrategicPartnerBinding.imgLogo.setVisibility(View.GONE);
                mActivityTaskCreationForStrategicPartnerBinding.textStepDesc.setText(getString(R.string.step_2_desc_for_strategic_partner));
                break;
            case STAGE_3:
                mActivityTaskCreationForStrategicPartnerBinding.viewpager.setCurrentItem(2);
                setTaskState(STEP_THREE_UNVERIFIED);
                // Change description
                mActivityTaskCreationForStrategicPartnerBinding.textStepDesc.setText(getString(R.string.step_3_desc_for_strategic_partner));
                mActivityTaskCreationForStrategicPartnerBinding.imgLogo.setVisibility(View.VISIBLE);
                break;
        }
    }


    @Override
    public void onBackPressed() {
        if (mActivityTaskCreationForStrategicPartnerBinding.addMedia.getRoot().getVisibility() == View.VISIBLE) {
            mActivityTaskCreationForStrategicPartnerBinding.addMedia.ivHideMediaUi.performClick();
            return;
        }

        if (mActivityTaskCreationForStrategicPartnerBinding.viewpager.getCurrentItem() == 1) {
            gotoStep(STAGE_1);
            return;
        }
        if (mActivityTaskCreationForStrategicPartnerBinding.viewpager.getCurrentItem() == 2) {
            gotoStep(STAGE_2);
            return;
        }

        if (getQuestionsList() != null) {
            for (QueAnsModel model : getQuestionsList())
                if (model.answerType.equalsIgnoreCase(Utility.TEMPLATE_UPLOAD)) {
                    ArrayList<MediaModel> mediaModelArrayList = model.medialList;
                    if (mediaModelArrayList != null && !mediaModelArrayList.isEmpty()) {
                        for (MediaModel mediaModel : mediaModelArrayList) {
                            LogUtils.LOGD(TAG, "onBackPressed() delete");
                            LogUtils.LOGD(TAG, "onBackPressed() " + mediaModel.mediaName);
                            LogUtils.LOGD(TAG, "onBackPressed() " + mediaModel.mediaThumbName);
                            LogUtils.LOGD(TAG, "onBackPressed() ============");
                            AmazonUtils.deleteFiles(this, mediaModel.mediaName, mediaModel.mediaThumbName);
                        }
                        break;
                    }
                }
        }
        super.onBackPressed();
    }


    public void setSelectedSubService(ArrayList<SubServiceDetailModel> mSelectedServicesList) {
        this.mSelectedServicesList = mSelectedServicesList;
        LogUtils.LOGE(TAG, " on continue click");
        for (SubServiceDetailModel model : mSelectedServicesList) {
            LogUtils.LOGE(TAG, " Item Name " + model.name);
            for (SubSubCatModel subSubCatModel : model.subSubCatModels) {
                LogUtils.LOGE(TAG, " Item  sub name " + subSubCatModel.subSubCatName);
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Utility.REQUEST_CODE_READ_EXTERNAL_STORAGE_ADD_COVER:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    LogUtils.LOGI(TAG, "onRequestPermissionsResult: Permission Granted");
                    //startIntentFileChooser(Utility.REQUEST_CODE_GET_FILE_ADD_COVER);
                } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    LogUtils.LOGI(TAG, "onRequestPermissionsResult: Permission Denied");
                    Utility.showSnackBar(getString(R.string.permission_denied_read), mActivityTaskCreationForStrategicPartnerBinding.getRoot());
                }
                break;
            case Utility.REQUEST_CODE_READ_EXTERNAL_STORAGE_ADD_PROFILE_GALLERY:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    LogUtils.LOGI(TAG, "onRequestPermissionsResult: Permission Granted");
                    // startIntentFileChooser(Utility.REQUEST_CODE_GET_FILE_ADD_PROFILE_GALLERY);
                } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    LogUtils.LOGI(TAG, "onRequestPermissionsResult: Permission Denied");
                    Utility.showSnackBar(getString(R.string.permission_denied_read), mActivityTaskCreationForStrategicPartnerBinding.getRoot());
                }
                break;
            case Utility.REQUEST_CODE_ADD_PROFILE_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    LogUtils.LOGI(TAG, "onRequestPermissionsResult: Permission Granted");
                    //  startCameraCaptureChooser(Utility.REQUEST_CODE_IMAGE_CAPTURE_ADD_PROFILE);
                } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    LogUtils.LOGI(TAG, "onRequestPermissionsResult: Permission Denied Camera");
                    Utility.showSnackBar(getString(R.string.permission_denied_camera), mActivityTaskCreationForStrategicPartnerBinding.getRoot());
                }
                break;
        }
    }

    @Override
    public void gpsEnabled() {
        super.gpsEnabled();
//        // Show place picker activity
//        mTaskCreationPagerAdapter.mStrategicPartnerFragPhaseTwo.showPlacePickerDialog(true);
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

    public ArrayList<SubServiceDetailModel> getSelectedSubService() {
        return mSelectedServicesList;
    }

    public void setQuestionsList(ArrayList<QueAnsModel> questionsList) {
        mQuestionsList = questionsList;
    }

    public ArrayList<QueAnsModel> getQuestionsList() {
        return mQuestionsList;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        Log.d(TAG, "onMessageEvent() called with: event = [" + event.BROADCAST_ACTION + "]");
        switch (event.BROADCAST_ACTION) {
            case Utility.BROADCAST_TYPE.TASK_PAID_SUCCESSFULLY:
                // Finish this activity
                if (isPayNow)
                    finish();
                break;
            case Utility.BROADCAST_TYPE.PAYMENT_COMPLETED_NEED_TO_REDIRECT_TO_MY_TASK_SCREEN:
                // Finish this activity
                if (isPayNow)
                    finish();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    public void showMediaUI(ArrayList<MediaModel> mediaList) {
        Log.d(TAG, "showMediaUI() called with: mediaList = [" + mediaList + "]");
        mActivityTaskCreationForStrategicPartnerBinding.addMedia.getRoot().setVisibility(View.VISIBLE);
        mediaFullScreenAdapter.addAll(mediaList);
        mActivityTaskCreationForStrategicPartnerBinding.addMedia.tvNumberOfMedia.setText(getString(R.string.number_of_media, String.valueOf(1)
                , String.valueOf(mediaList.size())));
    }

    public void addMedia(MediaModel media) {
        Log.d(TAG, "addMedia() called with: media = [" + media + "]");
        mediaFullScreenAdapter.add(media);
        mActivityTaskCreationForStrategicPartnerBinding.addMedia.tvNumberOfMedia.setText(getString(R.string.number_of_media, String.valueOf(expectedPosition + 1/* + 1*/)
                , String.valueOf(mediaFullScreenAdapter.getListSize())));
    }

    public void shouldAddMediaClickListener(boolean addMediaClickListener) {
        mActivityTaskCreationForStrategicPartnerBinding.addMedia.ivPlus.setOnClickListener(addMediaClickListener ? mOnClickListener : null);
    }

    private final MediaFullScreenAdapter.MediaInteractionListener mediaInteractionListener = new MediaFullScreenAdapter.MediaInteractionListener() {
        @Override
        public void onItemClick(int position, MediaModel model) {
            ZoomImageActivity.newInstance(mContext, null, model.localFilePath);
        }

        @Override
        public void onRemoveClick(int position, MediaModel model) {
            if (mediaFullScreenAdapter.getListSize() == 3) {
                shouldAddMediaClickListener(true);
            }
            if (mediaFullScreenAdapter.getListSize() == 1) {
                mActivityTaskCreationForStrategicPartnerBinding.addMedia.ivHideMediaUi.performClick();
            }
            mediaFullScreenAdapter.removeItem(StrategicPartnerTaskCreationAct.this, position, model);
            calculatePositionAndScroll(mActivityTaskCreationForStrategicPartnerBinding.addMedia.itemList);
            taskCreationPagerAdapter.mStrategicPartnerFragPhaseTwo.removeMediaItem(position, model);
        }
    };
}
