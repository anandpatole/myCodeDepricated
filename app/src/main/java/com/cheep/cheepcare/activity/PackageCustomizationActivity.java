package com.cheep.cheepcare.activity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.cheepcare.adapter.PackageCustomizationPagerAdapter;
import com.cheep.cheepcare.fragment.SelectPackageSpecificationsFragment;
import com.cheep.cheepcare.model.CheepCarePackageModel;
import com.cheep.databinding.ActivityPackageCustomizationBinding;
import com.cheep.utils.Utility;

/**
 * Created by pankaj on 12/22/17.
 */

public class PackageCustomizationActivity extends BaseAppCompatActivity {

    private ActivityPackageCustomizationBinding mBinding;
    private PackageCustomizationPagerAdapter mPackageCustomizationPagerAdapter;
    private CheepCarePackageModel mPackageModel;

    public static void newInstance(Context context, int position, CheepCarePackageModel model) {
        Intent intent = new Intent(context, PackageCustomizationActivity.class);
        intent.putExtra(Utility.Extra.POSITION, position);
        intent.putExtra(Utility.Extra.MODEL, model);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_package_customization);
        initiateUI();
    }

    @Override
    protected void initiateUI() {

        if (getIntent().hasExtra(Utility.Extra.MODEL)){
             mPackageModel = (CheepCarePackageModel) getIntent().getExtras().getSerializable(Utility.Extra.MODEL);
             /*position = getIntent().getExtras().getInt(Utility.Extra.POSITION);*/
        }

        // Calculate Pager Height and Width
        ViewTreeObserver mViewTreeObserver = mBinding.ivCityImage.getViewTreeObserver();
        mViewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mBinding.ivCityImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int width = mBinding.ivCityImage.getMeasuredWidth();
                ViewGroup.LayoutParams params = mBinding.ivCityImage.getLayoutParams();
                params.height = Utility.getHeightFromWidthForOneHalfIsToOneRatio(width);
                mBinding.ivCityImage.setLayoutParams(params);

                // Load the image now.
                Utility.loadImageView(mContext, mBinding.ivCityImage
                        , R.drawable.img_landing_screen_mumbai
                        , R.drawable.hotline_ic_image_loading_placeholder);
            }
        });

        Glide.with(mContext)
                .load(R.drawable.ic_home_with_heart_text)
                .asGif()
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(mBinding.ivCheepCareGif);

        // Set the default step
        setTaskState(STEP_ONE_UNVERIFIED);

        //Initiate description
        mBinding.textStepDesc.setText(getString(R.string.step_1_desc_cheep_care));

        //Initiate Button text
        mBinding.textContinue.setText(getString(R.string.select_x_package, mPackageModel.packageTitle));

        // Setting viewpager
        setupViewPager(mBinding.viewpager);

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
                if (mCurrentStep > 1) {
                    gotoStep(STAGE_2);
                } else {
                    Utility.showSnackBar(getString(R.string.step_1_desc_cheep_care), mBinding.getRoot());
                }

            }
        });
        mBinding.textStep3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                  Need to check whether first step is verified or not.
                 */
                if (mCurrentStep > 1) {
                    gotoStep(STAGE_3);
                } else {
                    Utility.showSnackBar(getString(R.string.step_1_desc_cheep_care), mBinding.getRoot());
                }

            }
        });

        //set phase description

        // Setting up Toolbar
        setSupportActionBar(mBinding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(Utility.EMPTY_STRING);
            mBinding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Utility.hideKeyboard(mContext);
                    onBackPressed();
                }
            });
        }
    }

    /**
     * This will setup the viewpager and tabs as well
     *
     * @param pager
     */
    private void setupViewPager(ViewPager pager) {
        mPackageCustomizationPagerAdapter = new PackageCustomizationPagerAdapter(getSupportFragmentManager());
        mPackageCustomizationPagerAdapter.addFragment(SelectPackageSpecificationsFragment.TAG);
        pager.setAdapter(mPackageCustomizationPagerAdapter);
    }

    /**
     * Below would manage the state of Step while creating task creation
     */
    /*public static final int STEP_ONE_NORMAL = 1;*/
    public static final int STEP_ONE_UNVERIFIED = 1;
    public static final int STEP_ONE_VERIFIED = 2;
    /*public static final int STEP_TWO_NORMAL = 4;*/
    public static final int STEP_TWO_UNVERIFIED = 3;
    public static final int STEP_TWO_VERIFIED = 4;
    /*public static final int STEP_THREE_NORMAL = 7;*/
    public static final int STEP_THREE_UNVERIFIED = 5;
    public static final int STEP_THREE_VERIFIED = 6;
    public int mCurrentStep = -1;

    public void setTaskState(int step_state) {
        mCurrentStep = step_state;
        switch (step_state) {
            /*case STEP_ONE_NORMAL:
                mBinding.textStep1.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_normal));
                mBinding.textStep1.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mBinding.textStep2.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_normal));
                mBinding.textStep2.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mBinding.textStep3.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_normal));
                mBinding.textStep3.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                break;*/
            case STEP_ONE_UNVERIFIED:
                mBinding.textStep1.setSelected(true);
                mBinding.textStep2.setSelected(false);
                mBinding.textStep3.setSelected(false);
                break;
                /*mBinding.textStep1.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_unverified));
                mBinding.textStep1.setTextColor(ContextCompat.getColor(mContext, R.color.splash_gradient_end));

                mBinding.textStep2.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_normal));
                mBinding.textStep2.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mBinding.textStep3.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_normal));
                mBinding.textStep3.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                break;*/
            case STEP_ONE_VERIFIED:
            /*case STEP_TWO_NORMAL:
                mBinding.textStep1.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_verified));
                mBinding.textStep1.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mBinding.textStep2.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_normal));
                mBinding.textStep2.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mBinding.textStep3.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_normal));
                mBinding.textStep3.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                break;*/
            case STEP_TWO_UNVERIFIED:
                mBinding.textStep1.setSelected(false);
                mBinding.textStep2.setSelected(true);
                mBinding.textStep3.setSelected(false);
                break;
                /*mBinding.textStep1.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_verified));
                mBinding.textStep1.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mBinding.textStep2.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_unverified));
                mBinding.textStep2.setTextColor(ContextCompat.getColor(mContext, R.color.splash_gradient_end));

                mBinding.textStep3.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_normal));
                mBinding.textStep3.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                break;*/
            case STEP_TWO_VERIFIED:
            /*case STEP_THREE_NORMAL:
                mBinding.textStep1.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_verified));
                mBinding.textStep1.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mBinding.textStep2.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_verified));
                mBinding.textStep2.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mBinding.textStep3.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_normal));
                mBinding.textStep3.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                break;*/
            case STEP_THREE_UNVERIFIED:
                mBinding.textStep1.setSelected(false);
                mBinding.textStep2.setSelected(false);
                mBinding.textStep3.setSelected(true);
                break;
                /*mBinding.textStep1.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_verified));
                mBinding.textStep1.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mBinding.textStep2.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_verified));
                mBinding.textStep2.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mBinding.textStep3.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_unverified));
                mBinding.textStep3.setTextColor(ContextCompat.getColor(mContext, R.color.splash_gradient_end));

                break;*/
            case STEP_THREE_VERIFIED:
                mBinding.textStep1.setSelected(false);
                mBinding.textStep2.setSelected(false);
                mBinding.textStep3.setSelected(true);
                break;
                /*mBinding.textStep1.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_verified));
                mBinding.textStep1.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mBinding.textStep2.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_verified));
                mBinding.textStep2.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mBinding.textStep3.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_verified));
                mBinding.textStep3.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                break;*/
        }
    }

    public static final int STAGE_1 = 0;
    public static final int STAGE_2 = 1;
    public static final int STAGE_3 = 2;

    public void gotoStep(int step) {
        switch (step) {
            case STAGE_1:
                mBinding.viewpager.setCurrentItem(0);
                // Change description
                mBinding.textStepDesc.setText(getString(R.string.step_1_desc_cheep_care));
                break;
            case STAGE_2:
                mBinding.viewpager.setCurrentItem(1);
                // Change description
                mBinding.textStepDesc.setText(getString(R.string.step_2_desc_cheep_care));
                break;
            case STAGE_3:
                mBinding.viewpager.setCurrentItem(2);
                // Change description
                mBinding.textStepDesc.setText(getString(R.string.step_3_desc_cheep_care));
                break;
        }
    }


    @Override
    public void onBackPressed() {
        if (mBinding.viewpager.getCurrentItem() == 2) {
            gotoStep(STAGE_2);
            return;
        } else if (mBinding.viewpager.getCurrentItem() == 1) {
            gotoStep(STAGE_1);
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void setListeners() {

    }
}
