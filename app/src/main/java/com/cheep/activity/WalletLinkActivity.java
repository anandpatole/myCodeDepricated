package com.cheep.activity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.BuildConfig;
import com.cheep.R;
import com.cheep.databinding.ActivityWalletLinkBinding;
import com.cheep.network.NetworkUtility;
import com.cheep.network.PaytmNetworkRequest;
import com.cheep.network.Volley;
import com.cheep.utils.LogUtils;
import com.cheep.utils.Utility;

import java.util.HashMap;
import java.util.Map;


public class WalletLinkActivity extends BaseAppCompatActivity implements View.OnClickListener {

    private static final String TAG = LogUtils.makeLogTag(WalletLinkActivity.class);
    private ActivityWalletLinkBinding mActivityWalletLinkBinding;
    private boolean isPaytm;

    public static void newInstance(Context context, boolean isPaytm) {
        Intent intent = new Intent(context, WalletLinkActivity.class);
        intent.putExtra(Utility.Extra.DATA, isPaytm);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityWalletLinkBinding = DataBindingUtil.setContentView(this, R.layout.activity_wallet_link);
        initiateUI();
        setListeners();
        setupActionbar();
    }

    private void setupActionbar() {
        mActivityWalletLinkBinding.textTitle.setText(getString(R.string.label_link_x, getString(R.string.label_paytm)));
        setSupportActionBar(mActivityWalletLinkBinding.toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(Utility.EMPTY_STRING);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDefaultDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void initiateUI() {
        Intent intent = getIntent();
        if (intent.hasExtra(Utility.Extra.DATA)) {
            isPaytm = intent.getExtras().getBoolean(Utility.Extra.DATA);
        }
        if (isPaytm) {
            mActivityWalletLinkBinding.tvEnterNoLinkXAccount.setText(getString(R.string.label_enter_number_link_account, getString(R.string.label_paytm)));
            mActivityWalletLinkBinding.tvWeCreateXWallet.setText(getString(R.string.label_we_create_wallet, getString(R.string.label_paytm)));
        } else {
            mActivityWalletLinkBinding.tvEnterNoLinkXAccount.setText(getString(R.string.label_enter_number_link_account, getString(R.string.label_mobikwik)));
            mActivityWalletLinkBinding.tvWeCreateXWallet.setText(getString(R.string.label_we_create_wallet, getString(R.string.label_mobikwik)));
        }
    }

    @Override
    protected void setListeners() {
        mActivityWalletLinkBinding.tvSendOtp.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_send_otp:
                if (isValidated(true)) {
                    String mobileNumber = mActivityWalletLinkBinding.etMobileNumber.getText().toString();
//                    sendOTP(mobileNumber);
                    updateUI(mobileNumber);
                }
                break;
        }
    }

    private void updateUI(final String mobileNumber) {
        String sendOTPString = getString(R.string.label_send_otp_again);
        mActivityWalletLinkBinding.tvEnterNoLinkXAccount.setText(getString(R.string.label_enter_otp_sent_on_x, mActivityWalletLinkBinding.etMobileNumber.getText()));
        mActivityWalletLinkBinding.tvSendOtp.setText(getString(R.string.label_proceed));
        mActivityWalletLinkBinding.ivMobile.setVisibility(View.GONE);
        mActivityWalletLinkBinding.tvDefaultCountryCode.setVisibility(View.GONE);
        mActivityWalletLinkBinding.etMobileNumber.setGravity(Gravity.CENTER);
        mActivityWalletLinkBinding.etMobileNumber.setText(Utility.EMPTY_STRING);
        mActivityWalletLinkBinding.etMobileNumber.setHint(getString(R.string.label_enter_otp));
        SpannableStringBuilder sendOTPSpannableStringBuilder = new SpannableStringBuilder(sendOTPString);

        int clickIndex = sendOTPString.indexOf(Utility.CLICK);

        if (clickIndex < 0) {
            LogUtils.LOGD(TAG, "\"click\" case changed in strings.xml");
            return;
        }

        LogUtils.LOGD(TAG, "clickIndex: " + clickIndex);

        sendOTPSpannableStringBuilder.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View view) {
                sendOTP(mobileNumber);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setUnderlineText(false);
            }
        }, clickIndex, sendOTPSpannableStringBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sendOTPSpannableStringBuilder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.splash_gradient_end)),
                clickIndex, sendOTPSpannableStringBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        mActivityWalletLinkBinding.tvWeCreateXWallet.setText(sendOTPSpannableStringBuilder);
        mActivityWalletLinkBinding.tvWeCreateXWallet.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void sendOTP(String mobileNumber) {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityWalletLinkBinding.getRoot());
            return;
        }

        //Show Progress
        showProgressDialog();

        Map<String, String> mParams = new HashMap<>();

//        mParams.put("email", "parekhkruti26@gmail.com");
        mParams.put("phone", mobileNumber);
        mParams.put("clientId", BuildConfig.CLIENT_ID);
        mParams.put("scope", "wallet");
        mParams.put("responseType", "token");

        PaytmNetworkRequest paytmNetworkRequest = new PaytmNetworkRequest(
                Request.Method.POST,
                NetworkUtility.PAYTM.OAUTH_APIS.SEND_OTP,
                mSendOTPResponseListener,
                mSendOTPErrorListener,
                null,
                mParams);
        Volley.getInstance(mContext).addToRequestQueue(paytmNetworkRequest, NetworkUtility.PAYTM.OAUTH_APIS.SEND_OTP);
    }

    Response.ErrorListener mSendOTPErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            LogUtils.LOGD(TAG, "onErrorResponse() called with: error = [" + error + "]");
            hideProgressDialog();

            // Show Toast
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityWalletLinkBinding.getRoot());
        }
    };

    Response.Listener<String> mSendOTPResponseListener = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            LogUtils.LOGD(TAG, "onResponse() called with: response = [" + response + "]");

            hideProgressDialog();
        }
    };

    private boolean isValidated(boolean isSendOTP) {

        if (isSendOTP) {
            if (TextUtils.isEmpty(mActivityWalletLinkBinding.etMobileNumber.getText())) {
                Utility.showSnackBar(getString(R.string.validate_phone_number), mActivityWalletLinkBinding.getRoot());
                return false;
            }

            //Length of phone number must bhi 10 in length
            if (!Utility.isValidPhoneNumber(mActivityWalletLinkBinding.etMobileNumber.getText().toString().trim())) {
                Utility.showSnackBar(getString(R.string.validate_phone_number_length), mActivityWalletLinkBinding.getRoot());
                return false;
            }
            return true;
        } else {
            if (TextUtils.isEmpty(mActivityWalletLinkBinding.etMobileNumber.getText())) {
                Utility.showSnackBar(getString(R.string.validate_otp_empty), mActivityWalletLinkBinding.getRoot());
                return false;
            }
            return true;
        }
    }

}
