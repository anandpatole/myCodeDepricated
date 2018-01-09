package com.cheep.cheepcare.activity;

import android.animation.AnimatorSet;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.cheepcare.adapter.ExpandableBoughtPackagesRecyclerAdapter;
import com.cheep.cheepcare.model.CheepCarePackageModel;
import com.cheep.databinding.ActivityManageSubscriptionBinding;
import com.cheep.utils.Utility;

import java.util.List;

/**
 * Created by pankaj on 12/28/17.
 */

public class ManageSubscriptionActivity extends BaseAppCompatActivity {

    private static final String TAG = ManageSubscriptionActivity.class.getSimpleName();
    private ActivityManageSubscriptionBinding mBinding;
    private String mCityName;
    private List<AnimatorSet> animators;

    public static void newInstance(Context mContext, String cityName) {
        Intent intent = new Intent(mContext, ManageSubscriptionActivity.class);
        intent.putExtra(Utility.Extra.CITY_NAME, cityName);
        mContext.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_manage_subscription);
        initiateUI();
        setListeners();
    }

    @Override
    protected void initiateUI() {
        if (getIntent().hasExtra(Utility.Extra.CITY_NAME)) {
            mCityName = getIntent().getExtras().getString(Utility.Extra.CITY_NAME);
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
        /*// Start cheep care animations
        mBinding.ivCheepCareGif.setBackgroundResource(R.drawable.cheep_care_animation);
        ((AnimationDrawable) mBinding.ivCheepCareGif.getBackground()).start();*/

        mBinding.tvCityName.setText(mCityName);

        mBinding.tvInfoText.setText(getString(R.string.cheep_care_work_flow_desc, "Nikita"));

        mBinding.recyclerView.setNestedScrollingEnabled(false);
        ExpandableBoughtPackagesRecyclerAdapter adapter =
                new ExpandableBoughtPackagesRecyclerAdapter(CheepCarePackageModel.getCheepCareBoughtPackages(), true);
        mBinding.recyclerView.setAdapter(adapter);

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

    @Override
    protected void setListeners() {

    }
}