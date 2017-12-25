package com.cheep.cheepcare.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
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

    private ActivityLandingScreenPickPackageBinding mActivityLandingScreenPickPackageBinding;
    private CheepCareFeatureAdapter mFeatureAdapter;
    private CheepCarePackageAdapter mPackageAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityLandingScreenPickPackageBinding
                = DataBindingUtil.setContentView(this, R.layout.activity_landing_screen_pick_package);
        initiateUI();
        setListeners();
    }

    @Override
    protected void initiateUI() {

        // Calculate Pager Height and Width
        ViewTreeObserver mViewTreeObserver = mActivityLandingScreenPickPackageBinding.ivCityImage.getViewTreeObserver();
        mViewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mActivityLandingScreenPickPackageBinding.ivCityImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int width = mActivityLandingScreenPickPackageBinding.ivCityImage.getMeasuredWidth();
                ViewGroup.LayoutParams params = mActivityLandingScreenPickPackageBinding.ivCityImage.getLayoutParams();
                params.height = Utility.getHeightFromWidthForTwoOneRatio(width);
                mActivityLandingScreenPickPackageBinding.ivCityImage.setLayoutParams(params);

                // Load the image now.
                Utility.loadImageView(mContext, mActivityLandingScreenPickPackageBinding.ivCityImage
                        , R.drawable.ic_landing_screen_mumbai
                        , R.drawable.hotline_ic_image_loading_placeholder);
            }
        });
        
        SpannableStringBuilder spannableStringBuilder
                = new SpannableStringBuilder(getString(R.string.dummy_good_morning_mumbai));
        spannableStringBuilder.append(Utility.ONE_CHARACTER_SPACE).append(Utility.ONE_CHARACTER_SPACE);
        ImageSpan span = new ImageSpan(getBaseContext(), R.drawable.ic_mic, ImageSpan.ALIGN_BASELINE);
        spannableStringBuilder.setSpan(span, spannableStringBuilder.length() - 1
                , spannableStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        mActivityLandingScreenPickPackageBinding.tvGoodMorningText.setText(spannableStringBuilder);

        Glide.with(mContext)
                .load(R.drawable.ic_home_with_heart_text)
                .asGif()
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(mActivityLandingScreenPickPackageBinding.ivCheepCareGif);

        mActivityLandingScreenPickPackageBinding.recyclerViewCheepCareFeature.setNestedScrollingEnabled(false);
        mFeatureAdapter = new CheepCareFeatureAdapter();
        mFeatureAdapter.addFeatureList(CheepCareFeatureModel.getCheepCareFeatures());
        mActivityLandingScreenPickPackageBinding.recyclerViewCheepCareFeature.setLayoutManager(new LinearLayoutManager(
                mContext
                , LinearLayoutManager.VERTICAL
                , false
        ));
        mActivityLandingScreenPickPackageBinding.recyclerViewCheepCareFeature.setAdapter(mFeatureAdapter);

        mActivityLandingScreenPickPackageBinding.recyclerViewCheepCarePackages.setNestedScrollingEnabled(false);
        mPackageAdapter = new CheepCarePackageAdapter();
        mPackageAdapter.addPakcageList(CheepCarePackageModel.getCheepCarePackages());
        mActivityLandingScreenPickPackageBinding.recyclerViewCheepCarePackages.setLayoutManager(new LinearLayoutManager(
                mContext
                , LinearLayoutManager.VERTICAL
                , false
        ));
        mActivityLandingScreenPickPackageBinding.recyclerViewCheepCarePackages.setAdapter(mPackageAdapter);
    }

    @Override
    protected void setListeners() {

    }
}
