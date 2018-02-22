package com.cheep.cheepcare.activity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

        String submitReviewString  = getString(R.string.msg_submit_review);
        int excellentEndIndex = submitReviewString.indexOf("Your");
        int endIndex = submitReviewString.length() - 1;

        ImageSpan smileSpan = new ImageSpan(mContext,R.drawable.emoji_folded_hands);

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
                    break;
            }
        }
    };
}
