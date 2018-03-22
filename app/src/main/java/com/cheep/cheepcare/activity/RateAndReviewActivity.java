package com.cheep.cheepcare.activity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.view.View;

import com.android.volley.VolleyError;
import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.cheepcare.adapter.RatingAdapter;
import com.cheep.cheepcare.model.RatingModel;
import com.cheep.databinding.ActivityRateAndReviewBinding;
import com.cheep.model.MessageEvent;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.utils.GsonUtility;
import com.cheep.utils.Utility;
import com.cheep.utils.WebCallClass;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

/**
 * Created by bhavesh on 21/2/18.
 */

public class RateAndReviewActivity extends BaseAppCompatActivity {

    private ActivityRateAndReviewBinding mBinding;
    private RatingAdapter adapter;
    private String taskId, providerId;

    public static void newInstance(Context context, String taskId, String providerID) {
        Intent intent = new Intent(context, RateAndReviewActivity.class);
        intent.putExtra(Utility.Extra.TASK_ID, taskId);
        intent.putExtra(Utility.Extra.SP_USER_ID, providerID);
        context.startActivity(intent);
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
        mBinding.groupRatingSubmitted.setVisibility(View.GONE);
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
        taskId = getIntent().getStringExtra(Utility.Extra.TASK_ID);
        providerId = getIntent().getStringExtra(Utility.Extra.SP_USER_ID);

        adapter = new RatingAdapter();
        mBinding.rvRating.setNestedScrollingEnabled(false);
        mBinding.rvRating.setLayoutManager(new LinearLayoutManager(RateAndReviewActivity.this, LinearLayoutManager.VERTICAL, false));
        mBinding.rvRating.setAdapter(adapter);
        WebCallClass.getRateAndReviewWS(this, taskId, rateAndReviewListener, errorListener);
    }

    private WebCallClass.RateAndReviewDataListener rateAndReviewListener = new WebCallClass.RateAndReviewDataListener() {
        @Override
        public void getRatingTypeList(ArrayList<RatingModel> ratingList) {
            hideProgressDialog();
            adapter.setList(ratingList);
        }

        @Override
        public void getSubmittedRateAndReviewData() {
            hideProgressDialog();
        }
    };
    private WebCallClass.CommonResponseListener errorListener = new WebCallClass.CommonResponseListener() {
        @Override
        public void volleyError(VolleyError error) {
            hideProgressDialog();
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
        }

        @Override
        public void showSpecificMessage(String message) {
            hideProgressDialog();
            Utility.showSnackBar(message, mBinding.getRoot());
        }

        @Override
        public void forceLogout() {
            hideProgressDialog();
            finish();
        }
    };

    @Override
    protected void setListeners() {
        mBinding.textBottomAction.setOnClickListener(mOnClickListener);
        mBinding.tvYes.setOnClickListener(mOnClickListener);
        mBinding.tvNo.setOnClickListener(mOnClickListener);
    }

    float avgRating = 0;
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
                    adapter.setIsIndicator(true);
                    mBinding.etReview.setEnabled(false);

                    for (RatingModel ratingModel : adapter.getList()) {
                        try {
                            avgRating += Float.parseFloat(ratingModel.reviewTypeRating);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        avgRating = avgRating / adapter.getItemCount();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    mBinding.groupRatingSubmitted.setVisibility(View.VISIBLE);
                    mBinding.tvRating.setText(String.valueOf(avgRating));

                    break;
                case R.id.tv_no:
                case R.id.tv_yes:
                    callSubmitReviewWS();
                    break;


            }
        }
    };

    private void callSubmitReviewWS() {
        //Validation
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mBinding.getRoot());
            return;
        }

        //Show Progress
        showProgressDialog();

        String message = mBinding.etReview.getText().toString();
        WebCallClass.submitReviewWS(this, taskId, providerId, String.valueOf(avgRating), message, GsonUtility.getJsonStringFromObject(adapter.getList()), new WebCallClass.SubmitRateAndReviewListener() {
            @Override
            public void onSuccessOfRateAndReviewSubmit() {
                hideProgressDialog();
                MessageEvent event = new MessageEvent();
                event.taskRating = String.valueOf(avgRating);
                event.BROADCAST_ACTION = Utility.BROADCAST_TYPE.TASK_RATED;
                EventBus.getDefault().post(event);
                finish();
            }
        }, errorListener);


    }

    @Override
    protected void onDestroy() {
        hideProgressDialog();
        Volley.getInstance(this).getRequestQueue().cancelAll(NetworkUtility.WS.ADD_REVIEW);
        Volley.getInstance(this).getRequestQueue().cancelAll(NetworkUtility.WS.GET_TASK_REVIEW);
        super.onDestroy();
    }
}
