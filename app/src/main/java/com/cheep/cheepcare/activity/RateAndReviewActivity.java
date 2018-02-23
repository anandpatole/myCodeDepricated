package com.cheep.cheepcare.activity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.view.View;

import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.databinding.ActivityRateAndReviewBinding;
import com.cheep.utils.Utility;

/**
 * Created by bhavesh on 21/2/18.
 */

public class RateAndReviewActivity extends BaseAppCompatActivity {

    private ActivityRateAndReviewBinding mBinding;

    public static void newInstance(Context context) {
        context.startActivity(new Intent(context, RateAndReviewActivity.class));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_rate_and_review);
        initiateUI();
        setListeners();
    }

    @Override
    protected void initiateUI() {
        // initialize indexes where image spans are to be put
        String submitReviewString = getString(R.string.msg_submit_review);
        int excellentEndIndex = "Excellent".length();
        int endIndex = submitReviewString.length() - 1;

        //initialize variables for setting image spans
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(submitReviewString);
        ImageSpan smileSpan = new ImageSpan(mContext, R.drawable.emoji_smile_blush, DynamicDrawableSpan.ALIGN_BASELINE);
        ImageSpan handsSpan = new ImageSpan(mContext, R.drawable.emoji_folded_hands, DynamicDrawableSpan.ALIGN_BASELINE);

        // set image spans in spannableStringBuilder
        spannableStringBuilder.setSpan(smileSpan, excellentEndIndex, excellentEndIndex + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.setSpan(handsSpan, endIndex, endIndex + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // set spannableStringBuilder text in submitReview textView
        mBinding.tvSubmitReviewDescription.setText(spannableStringBuilder);

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
        mBinding.textBottomAction.setOnClickListener(mOnClickListener);
    }

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.text_bottom_action:
                    mBinding.groupRatingSubmitted.setVisibility(View.VISIBLE);
                    mBinding.textBottomAction.setVisibility(View.GONE);
                    mBinding.etReview.setSelected(true);
                    mBinding.tvSubmitReviewDescription.setSelected(true);
                    mBinding.tvSubmitReviewDescription.setText(getString(R.string.msg_submitted_review));
                    mBinding.ratingCommunication.setIsIndicator(true);
                    mBinding.ratingPunctuality.setIsIndicator(true);
                    mBinding.ratingValueForMoney.setIsIndicator(true);
                    mBinding.ratingQualityOfJob.setIsIndicator(true);
                    mBinding.ratingPresentation.setIsIndicator(true);
                    mBinding.etReview.setEnabled(false);
                    break;
            }
        }
    };
}
