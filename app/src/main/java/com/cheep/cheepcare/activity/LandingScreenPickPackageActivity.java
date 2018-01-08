package com.cheep.cheepcare.activity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.cheepcare.adapter.CheepCareFeatureAdapter;
import com.cheep.cheepcare.adapter.CheepCarePackageAdapter;
import com.cheep.cheepcare.model.CheepCareFeatureModel;
import com.cheep.cheepcare.model.CheepCarePackageModel;
import com.cheep.databinding.ActivityLandingScreenPickPackageBinding;
import com.cheep.utils.Utility;

/**
 * Created by pankaj on 12/21/17.
 */

public class LandingScreenPickPackageActivity extends BaseAppCompatActivity {

    private ActivityLandingScreenPickPackageBinding mBinding;
    private CheepCareFeatureAdapter mFeatureAdapter;
    private CheepCarePackageAdapter mPackageAdapter;
    private String mCityName;

    public static void newInstance(Context context, String cityName) {
        Intent intent = new Intent(context, LandingScreenPickPackageActivity.class);
        intent.putExtra(Utility.Extra.CITY_NAME, cityName);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding
                = DataBindingUtil.setContentView(this, R.layout.activity_landing_screen_pick_package);
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

        SpannableStringBuilder spannableStringBuilder
                = new SpannableStringBuilder(getString(R.string.dummy_good_morning_mumbai));
        spannableStringBuilder.append(Utility.ONE_CHARACTER_SPACE).append(Utility.ONE_CHARACTER_SPACE);
        ImageSpan span = new ImageSpan(mContext, R.drawable.ic_mic, ImageSpan.ALIGN_BASELINE);
        spannableStringBuilder.setSpan(span, spannableStringBuilder.length() - 1
                , spannableStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        mBinding.tvGoodMorningText.setText(spannableStringBuilder);

        mBinding.tvInfoText.setText(getString(R.string.landing_page_info_text));

        mBinding.recyclerViewCheepCareFeature.setNestedScrollingEnabled(false);
        mFeatureAdapter = new CheepCareFeatureAdapter();
        mFeatureAdapter.addFeatureList(CheepCareFeatureModel.getCheepCareFeatures());
        mBinding.recyclerViewCheepCareFeature.setLayoutManager(new LinearLayoutManager(
                mContext
                , LinearLayoutManager.VERTICAL
                , false
        ));
        mBinding.recyclerViewCheepCareFeature.setAdapter(mFeatureAdapter);

        mBinding.recyclerViewCheepCarePackages.setNestedScrollingEnabled(false);
        mPackageAdapter = new CheepCarePackageAdapter(mPackageItemClickListener);
        mPackageAdapter.addPackageList(CheepCarePackageModel.getCheepCarePackages());
        mBinding.recyclerViewCheepCarePackages.setLayoutManager(new LinearLayoutManager(
                mContext
                , LinearLayoutManager.VERTICAL
                , false
        ));
        mBinding.recyclerViewCheepCarePackages.setAdapter(mPackageAdapter);

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

    private final CheepCarePackageAdapter.PackageItemClickListener mPackageItemClickListener
            = new CheepCarePackageAdapter.PackageItemClickListener() {
        @Override
        public void onPackageItemClick(int position, CheepCarePackageModel packageModel) {
            PackageCustomizationActivity.newInstance(mContext, position, packageModel, mCityName);
        }
    };
}
