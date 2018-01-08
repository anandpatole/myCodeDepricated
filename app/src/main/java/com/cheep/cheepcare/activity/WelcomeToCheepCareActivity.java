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
import com.cheep.cheepcare.adapter.ExpandableCareRecyclerAdapter;
import com.cheep.cheepcare.model.CheepCarePackageModel;
import com.cheep.databinding.ActivityWelcomeToCheepCareBinding;
import com.cheep.utils.Utility;

import java.util.List;

/**
 * Created by pankaj on 12/28/17.
 */

public class WelcomeToCheepCareActivity extends BaseAppCompatActivity {

    private static final String TAG = WelcomeToCheepCareActivity.class.getSimpleName();
    private ActivityWelcomeToCheepCareBinding mBinding;
    private String mCityName;
    private List<AnimatorSet> animators;

    public static void newInstance(Context mContext, String cityName) {
        Intent intent = new Intent(mContext, WelcomeToCheepCareActivity.class);
        intent.putExtra(Utility.Extra.CITY_NAME, cityName);
        mContext.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_welcome_to_cheep_care);
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
                = new SpannableStringBuilder(getString(R.string.msg_welcome_x, "Nikita"));
        spannableStringBuilder.append(Utility.ONE_CHARACTER_SPACE).append(Utility.ONE_CHARACTER_SPACE);
        ImageSpan span = new ImageSpan(getBaseContext(), R.drawable.ic_smiley_folded_hands_big, ImageSpan.ALIGN_BASELINE);
        spannableStringBuilder.setSpan(span, spannableStringBuilder.length() - 1
                , spannableStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        mBinding.tvWelcomeText.setText(spannableStringBuilder);

        mBinding.tvInfoText.setText(getString(R.string.msg_welcoming_on_subscription));

        mBinding.recyclerView.setNestedScrollingEnabled(false);
        ExpandableCareRecyclerAdapter adapter = new ExpandableCareRecyclerAdapter(CheepCarePackageModel.getCheepCarePackages(), true);
        mBinding.recyclerView.setAdapter(adapter);

//        addCareItems();

       /* mBinding.recyclerView.setNestedScrollingEnabled(false);
        ExpandableCareRecyclerAdapter adapter = new ExpandableCareRecyclerAdapter(CheepCarePackageModel.getCheepCarePackages(), true);
        mBinding.recyclerView.setAdapter(adapter);*/

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

    /*private void addCareItems() {
        LayoutInflater inflater = LayoutInflater.from(mContext);

        List<CheepCarePackageModel> packages = CheepCarePackageModel.getCheepCarePackages();
        CardView cardView = null;
        LinearLayout linearLayout = null;
        RowPackageCareItemBinding careItemBinding;

        for (int i = 0; i < packages.size(); i++) {

            //declare and initialize the card view to add
            cardView = new CardView(mContext);
            cardView.setCardElevation(Utility.convertDpToPixel(4, mContext));
            CardView.LayoutParams cardParams = new CardView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                    , ViewGroup.LayoutParams.WRAP_CONTENT);
            cardParams.topMargin = (int) Utility.convertDpToPixel(12, mContext);
            cardParams.leftMargin = (int) Utility.convertDpToPixel(8, mContext);
            cardParams.rightMargin = (int) Utility.convertDpToPixel(8, mContext);
            cardView.setLayoutParams(cardParams);

            //declare and initialize the linear layout to add inside cardView
            linearLayout = new LinearLayout(mContext);
            LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                    , ViewGroup.LayoutParams.MATCH_PARENT);
            linearLayout.setLayoutParams(linearLayoutParams);
            linearLayout.setOrientation(LinearLayout.VERTICAL);

            //declare and initialize the package care item to add and add it to linear layout
            careItemBinding = DataBindingUtil.inflate(inflater, R.layout.row_package_care_item, cardView, false);
            bindCareItem(careItemBinding, packages.get(i));
            linearLayout.addView(careItemBinding.getRoot());

            RowPackageCareSubItemBinding careSubItemBinding = null;

            for (int j = 0; j < packages.get(i).subItems.size(); j++) {
                careSubItemBinding =
                        DataBindingUtil.inflate(inflater, R.layout.row_package_care_sub_item, cardView, false);
                bindSubCareItem(careSubItemBinding, packages.get(i).subItems.get(j));
                linearLayout.addView(careSubItemBinding.getRoot());
            }
            cardView.addView(linearLayout);
            mBinding.llCareItemsContainer.addView(cardView);
        }
    }

    private void bindCareItem(RowPackageCareItemBinding careItemBinding, CheepCarePackageModel model) {
        careItemBinding.tvCareName.setText(model.packageTitle);
        careItemBinding.tvDescription.setText(model.packageDescription);
        if (model.subItems == null || model.subItems.size() == 0) {
            careItemBinding.tvDescription.setCompoundDrawablesWithIntrinsicBounds(0
                    , 0
                    , R.drawable.ic_right_arrow_in_white_circle
                    , 0);
        } else {
            careItemBinding.tvDescription.setCompoundDrawablesWithIntrinsicBounds(0
                    , 0
                    , R.drawable.ic_white_arrow_filled_blue
                    , 0);
        }
        if (model.daysLeft != null *//*&& model.daysLeft.length() != 0*//*) {
            switch (model.daysLeft.length()) {
                case 0:
                    careItemBinding.tvDaysLeft1.setVisibility(View.GONE);
                    careItemBinding.tvDaysLeft2.setVisibility(View.GONE);
                    careItemBinding.tvDaysLeft3.setVisibility(View.GONE);
                    careItemBinding.tvLeftDays.setVisibility(View.GONE);
                    break;
                case 1:
                    careItemBinding.tvDaysLeft1.setVisibility(View.VISIBLE);
                    careItemBinding.tvDaysLeft2.setVisibility(View.VISIBLE);
                    careItemBinding.tvDaysLeft3.setVisibility(View.VISIBLE);

                    careItemBinding.tvDaysLeft1.setText("0");
                    careItemBinding.tvDaysLeft2.setText("0");
                    careItemBinding.tvDaysLeft3.setText(model.daysLeft);
                    break;
                case 2:
                    careItemBinding.tvDaysLeft1.setVisibility(View.VISIBLE);
                    careItemBinding.tvDaysLeft2.setVisibility(View.VISIBLE);
                    careItemBinding.tvDaysLeft3.setVisibility(View.VISIBLE);

                    careItemBinding.tvDaysLeft1.setText("0");
                    careItemBinding.tvDaysLeft2.setText(model.daysLeft.subSequence(0, 1));
                    careItemBinding.tvDaysLeft3.setText(model.daysLeft.subSequence(1, 2));
                    break;
                case 3:
                    careItemBinding.tvDaysLeft1.setVisibility(View.VISIBLE);
                    careItemBinding.tvDaysLeft2.setVisibility(View.VISIBLE);
                    careItemBinding.tvDaysLeft3.setVisibility(View.VISIBLE);

                    careItemBinding.tvDaysLeft1.setText(model.daysLeft.subSequence(0, 1));
                    careItemBinding.tvDaysLeft2.setText(model.daysLeft.subSequence(1, 2));
                    careItemBinding.tvDaysLeft3.setText(model.daysLeft.subSequence(2, 3));
                    break;
            }
        } else {
            careItemBinding.tvDaysLeft1.setVisibility(View.GONE);
            careItemBinding.tvDaysLeft2.setVisibility(View.GONE);
            careItemBinding.tvDaysLeft3.setVisibility(View.GONE);
            careItemBinding.tvLeftDays.setVisibility(View.GONE);
        }
    }

    private void bindSubCareItem(final RowPackageCareSubItemBinding careSubItemBinding, final JobCategoryModel model) {
        //Background image
        Utility.loadImageView(mContext, careSubItemBinding.imgCategoryBackground, model.catImage, R.drawable.gradient_black);
        Utility.loadImageView(mContext, careSubItemBinding.imgCategoryBackground, model.catImageExtras.thumb, R.drawable.gradient_black);

        //Category Icon
        Utility.loadImageView(mContext, careSubItemBinding.imgCategoryIcon, model.catIcon, 0);
        careSubItemBinding.imgCategoryIcon.setVisibility(View.GONE);
//        careSubItemBinding.textJobsCount.setText(String.valueOf(model.spCount));
        careSubItemBinding.textCategoryName.setText(String.valueOf(model.catName));

        // Start LIVE tracking and Text changes
        final int liveFeedCounter = model.live_lable_arr != null ? model.live_lable_arr.size() : 0;
        final Map<String, Integer> mOfferIndexMap = new HashMap<>();
        if (liveFeedCounter > 0) {
            careSubItemBinding.ivLiveAnimated.setVisibility(View.VISIBLE);
            careSubItemBinding.tvLiveFeed.setVisibility(View.VISIBLE);

            // Live Icon offset
            int offset = careSubItemBinding.getRoot().getResources().getDimensionPixelSize(R.dimen.scale_5dp);
            final int mLiveIconOffset = careSubItemBinding.getRoot().getResources().getDimensionPixelSize(R.dimen.icon_live_width) + offset;

            // Start live image animations
            careSubItemBinding.ivLiveAnimated.setBackgroundResource(R.drawable.ic_live);
            ((AnimationDrawable) careSubItemBinding.ivLiveAnimated.getBackground()).start();

            AnimatorSet offerAnimation = loadBannerScrollAnimation(careSubItemBinding.tvLiveFeed, 2000, 100, new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    int offerIndex = mOfferIndexMap.containsKey(model.catId) ? mOfferIndexMap.get(model.catId) : 0;
                    SpannableString labelOffer = new SpannableString(model.live_lable_arr.get(offerIndex));
                    labelOffer.setSpan(new LeadingMarginSpan.Standard(mLiveIconOffset, 0), 0, labelOffer.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    careSubItemBinding.tvLiveFeed.setText(labelOffer);
                    offerIndex = (offerIndex == (liveFeedCounter - 1) ? 0 : offerIndex + 1);
                    mOfferIndexMap.put(model.catId, offerIndex);
                }
            });
            offerAnimation.start();
//            removeAnimations();
//            addAnimator(offerAnimation);
            int offerIndex = mOfferIndexMap.containsKey(model.catId) ? mOfferIndexMap.get(model.catId) : 0;
            SpannableString labelOffer = new SpannableString(model.live_lable_arr.get(offerIndex));
            labelOffer.setSpan(new LeadingMarginSpan.Standard(mLiveIconOffset, 0), 0, labelOffer.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            careSubItemBinding.tvLiveFeed.setText(labelOffer);
            offerIndex = (offerIndex == (liveFeedCounter - 1) ? 0 : offerIndex + 1);
            mOfferIndexMap.put(model.catId, offerIndex);
        } else {
            careSubItemBinding.ivLiveAnimated.setVisibility(View.GONE);
            careSubItemBinding.tvLiveFeed.setVisibility(View.GONE);
        }

        // LIVE Pro stacks
        updateLIVEProStackImages(careSubItemBinding, (ArrayList<String>) model.proImagesPerCategory, mContext);
    }

    private AnimatorSet loadBannerScrollAnimation(View view, int offset, int distance, AnimatorListenerAdapter midEndListener) {
        ObjectAnimator moveOut = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, 0, (-1 * distance));
        if (midEndListener != null) {
            moveOut.addListener(midEndListener);
        }
        ObjectAnimator moveIn = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, distance, 0);
        final AnimatorSet set = new AnimatorSet();
        set.setDuration(1000);
        set.setStartDelay(offset);
        set.playSequentially(moveOut, moveIn);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                set.start();
            }
        });
        return set;
    }

    public void addAnimator(AnimatorSet animator) {
        if (animator != null) {
            animators.add(animator);
        }
    }

    public void removeAnimations() {
        for (AnimatorSet animatorSet : animators) {
            for (Animator child : animatorSet.getChildAnimations()) {
                child.removeAllListeners();
            }
            animatorSet.removeAllListeners();
            animatorSet.end();
            animatorSet.cancel();
        }
        animators.clear();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////Reload SP Listing based on Availability [START]///////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    *//**
     * This method would going to update the SP list of images
     *
     * @param list of Providers available for particular category
     *//*
    private void updateLIVEProStackImages(RowPackageCareSubItemBinding mBinding, ArrayList<String> list, Context context) {

        for (int i = 0; i < 5; i++) {
            switch (i) {
                case 0:
                    if (list.size() > 0 && list.get(i) != null) {
                        Utility.showCircularImageView(context, TAG, mBinding.img1, list.get(i), R.drawable.ic_cheep_circular_icon, true, 0.5f);
                        mBinding.img1.setVisibility(View.VISIBLE);
                    } else {
                        mBinding.img1.setVisibility(View.GONE);
                    }
                    break;
                case 1:
                    if (list.size() > 1 && list.get(i) != null) {
                        Utility.showCircularImageView(context, TAG, mBinding.img2, list.get(i), R.drawable.ic_cheep_circular_icon, true, 0.5f);
                        mBinding.img2.setVisibility(View.VISIBLE);
                    } else {
                        mBinding.img2.setVisibility(View.GONE);
                    }
                    break;
                case 2:
                    if (list.size() > 2 && list.get(i) != null) {
                        Utility.showCircularImageView(context, TAG, mBinding.img3, list.get(i), R.drawable.ic_cheep_circular_icon, true, 0.5f);
                        mBinding.img3.setVisibility(View.VISIBLE);
                    } else {
                        mBinding.img3.setVisibility(View.GONE);
                    }
                    break;
                *//*case 3:
                    if (list.size() > 3 && list.get(i) != null) {
                        Utility.showCircularImageView(context, TAG, mBinding.img4, list.get(i).profileUrl, R.drawable.ic_cheep_circular_icon, true);
                        mBinding.img4.setVisibility(View.VISIBLE);
                    } else {
                        mBinding.img4.setVisibility(View.GONE);
                    }
                    break;
                case 4:
                    if (list.size() > 4 && list.get(i) != null) {
                        Utility.showCircularImageView(context, TAG, mActivityTaskSummaryBinding.img5, list.get(i).profileUrl, R.drawable.ic_cheep_circular_icon, true);
                        mBinding.img5.setVisibility(View.VISIBLE);
                    } else {
                        mBinding.img5.setVisibility(View.GONE);
                    }
                    break;*//*
            }
        }

        // Check if list size is more than 3
        if (list.size() > 3) {
            int extra_count = list.size() - 3;
            // Set size of textview based on number of digits, to make sure it looks proper in
            // In all devices(specifically for hdpi) devices.
            int digit_length = String.valueOf(extra_count).length();
            switch (digit_length) {
                case 1:
                    mBinding.extraProCount.setTextSize(
                            TypedValue.COMPLEX_UNIT_PX,
                            context.getResources().getDimension(R.dimen.home_screen_extra_category_count_text_size_one_digits));
                    break;
                case 2:
                    mBinding.extraProCount.setTextSize(
                            TypedValue.COMPLEX_UNIT_PX,
                            context.getResources().getDimension(R.dimen.home_screen_extra_category_count_text_size_two_digits));
                    break;
                default:
                    mBinding.extraProCount.setTextSize(
                            TypedValue.COMPLEX_UNIT_PX,
                            context.getResources().getDimension(R.dimen.home_screen_extra_category_count_text_size_three_digits));
                    break;
            }

            mBinding.extraProCount.setVisibility(View.VISIBLE);
            mBinding.extraProCount.setText("+" + String.valueOf(extra_count));
        } else {
            mBinding.extraProCount.setVisibility(View.GONE);
        }

        // Awaiting Response
        if (list.size() == 0) {
            mBinding.lnProsAvailable.setVisibility(View.VISIBLE);
            // Set margin as Zero
            mBinding.textTaskResponseStatus.setPadding((int) Utility.convertDpToPixel(0, context), 0, 0, 0);
            mBinding.textTaskResponseStatus.setText(mBinding.lnProsAvailable.getResources().getString(R.string.no_pros_in_this_category));
        } else {
            // Set margin as Zero
            mBinding.textTaskResponseStatus.setPadding((int) Utility.convertDpToPixel(5, context), 0, 0, 0);
            mBinding.lnProsAvailable.setVisibility(View.VISIBLE);
            mBinding.textTaskResponseStatus.setText(mBinding.lnProsAvailable.getResources().getString(R.string.label_pros_available));
        }

    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////Reload SP Listing based on AddressID [END]//////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////*/
}