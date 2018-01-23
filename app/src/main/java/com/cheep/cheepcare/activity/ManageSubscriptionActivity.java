package com.cheep.cheepcare.activity;

import android.animation.AnimatorSet;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.cheep.cheepcare.adapter.ExpandableBoughtPackagesRecyclerAdapter;
import com.cheep.cheepcare.adapter.ManageSubscriptionAddPackageAdapter;
import com.cheep.cheepcare.model.CheepCareBannerModel;
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
    private CheepCareBannerModel mCity;
    private List<AnimatorSet> animators;
    private int activityType;

    public interface ACTIVITY_TYPES {
        int WELCOME_TO_CC_ACTIVITY = 0;
        int MANAGE_SUBSCRIPTION_ACTIVITY = 1;
    }

    public static void newInstance(Context context, CheepCareBannerModel city, int activityType) {
        Intent intent = new Intent(context, ManageSubscriptionActivity.class);
        intent.putExtra(Utility.Extra.CITY_DETAIL, Utility.getJsonStringFromObject(city));
        intent.putExtra(Utility.Extra.ACTIVITY_TYPE, Utility.getJsonStringFromObject(city));
        context.startActivity(intent);
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
        if (getIntent().hasExtra(Utility.Extra.CITY_DETAIL)) {
            mCity = (CheepCareBannerModel) Utility.getObjectFromJsonString(getIntent().getExtras().getString(Utility.Extra.CITY_DETAIL), CheepCareBannerModel.class);
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

        mBinding.tvCityName.setText(mCity.cityName);

        if (activityType == ACTIVITY_TYPES.WELCOME_TO_CC_ACTIVITY) {
            SpannableStringBuilder spannableStringBuilder
                    = new SpannableStringBuilder(getString(R.string.msg_welcome_x, "Nikita"));
            spannableStringBuilder.append(Utility.ONE_CHARACTER_SPACE).append(Utility.ONE_CHARACTER_SPACE);
            ImageSpan span = new ImageSpan(getBaseContext(), R.drawable.ic_smiley_folded_hands_big, ImageSpan.ALIGN_BASELINE);
            spannableStringBuilder.setSpan(span, spannableStringBuilder.length() - 1
                    , spannableStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            mBinding.tvWelcomeText.setText(spannableStringBuilder);
            mBinding.tvInfoText.setText(getString(R.string.msg_welcoming_on_subscription));
        } else {
            mBinding.tvInfoText.setText(getString(R.string.cheep_care_work_flow_desc, "Nikita"));
        }

        mBinding.rvBoughtPackages.setNestedScrollingEnabled(false);
        ExpandableBoughtPackagesRecyclerAdapter boughtAdapter =
                new ExpandableBoughtPackagesRecyclerAdapter(CheepCarePackageModel.getCheepCareBoughtPackages(), true);
        mBinding.rvBoughtPackages.setAdapter(boughtAdapter);

        mBinding.rvAddPackage.setNestedScrollingEnabled(false);
        ManageSubscriptionAddPackageAdapter addPackageAdapter =
                new ManageSubscriptionAddPackageAdapter(addPackageInteractionListener
                        , CheepCarePackageModel.getManageSubscriptionAddPackageList());
        mBinding.rvAddPackage.setAdapter(addPackageAdapter);

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

    private final ManageSubscriptionAddPackageAdapter.AddPackageInteractionListener addPackageInteractionListener =
            new ManageSubscriptionAddPackageAdapter.AddPackageInteractionListener() {
                @Override
                public void onPackageItemClick(CheepCarePackageModel model) {

                }
            };
}