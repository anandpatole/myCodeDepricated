package com.cheep.activity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;

import com.cheep.R;
import com.cheep.databinding.ActivitySendOtpBinding;
import com.cheep.model.MessageEvent;
import com.cheep.network.NetworkUtility;
import com.cheep.utils.LogUtils;
import com.cheep.utils.PaytmUtility;
import com.cheep.utils.Utility;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import static com.cheep.network.NetworkUtility.PAYTM.PARAMETERS.orderId;
import static com.cheep.network.NetworkUtility.PAYTM.PARAMETERS.response;

public class VerifyOtpActivity extends BaseAppCompatActivity {

    private static final String TAG = VerifyOtpActivity.class.getSimpleName();
    private ActivitySendOtpBinding mActivitySendOtpBinding;
    private CountDownTimer timer;
    boolean isTimerOnGoing = false;
    long currentMilliSeconds = 0;
    private TextWatcher textWatcher;
    private boolean isPaytm;
    private String mEtText;

    private String mMobileNumber;

    //returned in response of send otp api
    private String mState;

    // returned in response of verify otp api
    private String mAccessToken;
    private long mExpires;
    private String mResourceOwnerCustomerId;

    private String amount;

    //returned in response of check balance api
    private String requestGuid;
    private String paytmReturnedOrderId;
    private double totalBalance;
    private double paytmWalletBalance;
    private String ownerGuid;
    private String walletGrade;
    private String ssoId;
    private String generatedOrderId;
    private boolean isLowBalance;

    private double payableAmount;

    public static void newInstance(Context context, String mobileNumber, String state, boolean isPaytm, String amount) {
        Intent intent = new Intent(context, VerifyOtpActivity.class);
        intent.putExtra(Utility.Extra.MOBILE_NUMBER, mobileNumber);
        intent.putExtra(Utility.Extra.STATE, state);
        intent.putExtra(Utility.Extra.DATA, isPaytm);
        intent.putExtra(Utility.Extra.AMOUNT, amount);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivitySendOtpBinding = DataBindingUtil.setContentView(this, R.layout.activity_send_otp);
        initiateUI();
        setupActionbar();
        EventBus.getDefault().register(this);

    }

    private void setupActionbar() {
        if (isPaytm)
            mActivitySendOtpBinding.textTitle.setText(getString(R.string.label_link_x, getString(R.string.label_paytm)));
        else
            mActivitySendOtpBinding.textTitle.setText(getString(R.string.label_link_x, getString(R.string.label_mobikwik)));
        setSupportActionBar(mActivitySendOtpBinding.toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(Utility.EMPTY_STRING);
        actionBar.setDisplayHomeAsUpEnabled(true);
        //noinspection RestrictedApi
        actionBar.setDefaultDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void initiateUI() {

        if (getIntent().hasExtra(Utility.Extra.MOBILE_NUMBER)) {
            mMobileNumber = getIntent().getExtras().getString(Utility.Extra.MOBILE_NUMBER);
            mState = getIntent().getExtras().getString(Utility.Extra.STATE);
            isPaytm = getIntent().getExtras().getBoolean(Utility.Extra.DATA);
            amount = getIntent().getExtras().getString(Utility.Extra.AMOUNT);
        }

        String sendOTPString = getString(R.string.label_send_otp_again);
        mActivitySendOtpBinding.tvEnterNoLinkXAccount.setText(getString(R.string.label_enter_otp_sent_on_x, mMobileNumber));
//                mActivitySendOtpBinding.tvSendOtp.setText(getString(R.string.label_proceed));
        mActivitySendOtpBinding.tvSendOtp.setEnabled(false);
        mActivitySendOtpBinding.tvWeCreateXWallet.setVisibility(View.INVISIBLE);
        timer = new CountDownTimer(60000, 1000) {

            public void onTick(long millisUntilFinished) {
                currentMilliSeconds = millisUntilFinished;
                isTimerOnGoing = true;
                if (!mActivitySendOtpBinding.etMobileNumber.getText().toString().isEmpty()) {
                    mActivitySendOtpBinding.tvSendOtp.setSelected(true);
                    mActivitySendOtpBinding.tvSendOtp.setText(getString(R.string.label_proceed));
                } else {
                    mActivitySendOtpBinding.tvSendOtp.setSelected(false);
                    mActivitySendOtpBinding.tvSendOtp.setText(String.format("00:" + "%02d", (int) millisUntilFinished / 1000));
                }
            }

            public void onFinish() {
                isTimerOnGoing = false;
                if (!mActivitySendOtpBinding.etMobileNumber.getText().toString().isEmpty()) {
                    mActivitySendOtpBinding.tvSendOtp.setSelected(true);
                    mActivitySendOtpBinding.tvSendOtp.setText(getString(R.string.label_proceed));
                } else {
                    mActivitySendOtpBinding.tvSendOtp.setSelected(false);
                    mActivitySendOtpBinding.tvSendOtp.setText(String.format("00:" + "%02d", 0));
                }
                mActivitySendOtpBinding.tvWeCreateXWallet.setVisibility(View.VISIBLE);
            }

        }.start();

        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mActivitySendOtpBinding.tvSendOtp.setEnabled(charSequence.length() > 0);
                mActivitySendOtpBinding.tvSendOtp.setOnClickListener(charSequence.length() > 0 ? mOnClickListener : null);
                if (!mActivitySendOtpBinding.etMobileNumber.getText().toString().isEmpty()) {
                    mActivitySendOtpBinding.tvSendOtp.setSelected(true);
                    mActivitySendOtpBinding.tvSendOtp.setText(getString(R.string.label_proceed));
                } else if (!isTimerOnGoing) {
                    mActivitySendOtpBinding.tvSendOtp.setSelected(false);
                    mActivitySendOtpBinding.tvSendOtp.setText(String.format("00:" + "%02d", 0));
                } else {
                    mActivitySendOtpBinding.tvSendOtp.setSelected(false);
                    mActivitySendOtpBinding.tvSendOtp.setText(String.format("00:" + "%02d", (int) currentMilliSeconds / 1000));

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };

        mActivitySendOtpBinding.etMobileNumber.addTextChangedListener(textWatcher);
        mActivitySendOtpBinding.ivMobile.setVisibility(View.GONE);
        mActivitySendOtpBinding.tvDefaultCountryCode.setVisibility(View.GONE);
        mActivitySendOtpBinding.etMobileNumber.setGravity(Gravity.CENTER);
        mActivitySendOtpBinding.etMobileNumber.setText(Utility.EMPTY_STRING);
        mActivitySendOtpBinding.etMobileNumber.setTextColor(ContextCompat.getColor(mContext, R.color.grey_varient_8));
        mActivitySendOtpBinding.etMobileNumber.setHint(getString(R.string.label_enter_otp));
          /*  if (BuildConfig.BUILD_TYPE.equalsIgnoreCase(Utility.DEBUG)) {
                mActivitySendOtpBinding.etMobileNumber.setText(BootstrapConstant.PAYTM_STAGING_MOBILE_NUMBER);
            }*/
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
                sendOTP(true);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setUnderlineText(false);
            }
        }, clickIndex, sendOTPSpannableStringBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sendOTPSpannableStringBuilder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.splash_gradient_end)),
                clickIndex, sendOTPSpannableStringBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        mActivitySendOtpBinding.tvWeCreateXWallet.setText(sendOTPSpannableStringBuilder);
        mActivitySendOtpBinding.tvWeCreateXWallet.setGravity(Gravity.CENTER);
        mActivitySendOtpBinding.tvWeCreateXWallet.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    protected void setListeners() {

    }

    ///////////////////////////////////////////////////////////Paytm Send OTP API call starts///////////////////////////////////////////////////////////
    /*@Override
    public void paytmSendOtpSuccessResponse(String state, boolean isRegenerated) {
        mState = state;
        *//*if (!isRegenerated)
            updateUI();
        else
            timer.start();*//*
        hideProgressDialog();
    }*/

    private void sendOTP(boolean isRegenerated) {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivitySendOtpBinding.getRoot());
            return;
        }
        //Show Progress
        showProgressDialog();

        PaytmUtility.sendOTP(mContext, mMobileNumber, mSendOtpResponseListener, isRegenerated);
    }

    private final PaytmUtility.SendOtpResponseListener mSendOtpResponseListener = new PaytmUtility.SendOtpResponseListener() {
        @Override
        public void paytmSendOtpSuccessResponse(String state, boolean isRegenerated) {
            mState = state;
        /*if (!isRegenerated)
            updateUI();
        else
            timer.start();*/
            hideProgressDialog();
        }

        @Override
        public void showGeneralizedErrorMessage() {
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivitySendOtpBinding.getRoot());
            hideProgressDialog();
        }

        @Override
        public void paytmInvalidMobileNumber() {
            Utility.showSnackBar(getString(R.string.validate_phone_number), mActivitySendOtpBinding.getRoot());
            hideProgressDialog();
        }

        @Override
        public void paytmAccountBlocked() {
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivitySendOtpBinding.getRoot());
            hideProgressDialog();
        }

        @Override
        public void volleyError() {
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivitySendOtpBinding.getRoot());
            hideProgressDialog();
        }
    };
    ///////////////////////////////////////////////////////////Paytm Send OTP API call ends///////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////Paytm Verify OTP API call starts///////////////////////////////////////////////////////////
   /* @Override
    public void paytmVerifyOtpSuccessResponse(String accessToken, long expires, String resourceOwnerCustomerId) {
        mAccessToken = accessToken;
        mExpires = expires;
        mResourceOwnerCustomerId = resourceOwnerCustomerId;
        */

    /**
     * have not called getUserDetails API as whatever this API returns, we already get it in response of this API
     * do not hideProgressDialog as we need to call 3 (would be 4 in case we call getUserDetails API) APIs back to back
     *//*
        *//*timer.cancel();*//*
//        savePaytmUserDetails();
//        checkBalance();
    }

    @Override
    public void paytmVerifyOtpInvalidOtp() {
        Utility.showSnackBar(getString(R.string.label_invalid_otp), mActivitySendOtpBinding.getRoot());
        hideProgressDialog();
    }*/
    private void verifyOTP() {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivitySendOtpBinding.getRoot());
            return;
        }

        //Show Progress
        showProgressDialog();

        PaytmUtility.verifyOtp(mContext, mEtText, mState, mVerifyOtpResponseListener);
    }

    private final PaytmUtility.VerifyOtpResponseListener mVerifyOtpResponseListener = new PaytmUtility.VerifyOtpResponseListener() {
        @Override
        public void paytmVerifyOtpSuccessResponse(String accessToken, long expires, String resourceOwnerCustomerId) {
            mAccessToken = accessToken;
            mExpires = expires;
            mResourceOwnerCustomerId = resourceOwnerCustomerId;
            /**
             * have not called getUserDetails API as whatever this API returns, we already get it in response of this API
             * do not hideProgressDialog as we need to call 3 (would be 4 in case we call getUserDetails API) APIs back to back
             */
        /*timer.cancel();*/
            savePaytmUserDetails();
            checkBalance();
        }

        @Override
        public void showGeneralizedErrorMessage() {
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivitySendOtpBinding.getRoot());
            hideProgressDialog();
        }

        @Override
        public void paytmVerifyOtpInvalidOtp() {
            Utility.showSnackBar(getString(R.string.label_invalid_otp), mActivitySendOtpBinding.getRoot());
            hideProgressDialog();
        }

        @Override
        public void volleyError() {
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivitySendOtpBinding.getRoot());
            hideProgressDialog();
        }
    };
    ///////////////////////////////////////////////////////////Paytm Verify OTP API call ends///////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////Volley save paytm user Web call starts///////////////////////////////////////////////////////////

    /*@Override
    public void volleySavePaytmUserSuccessResponse() {
        Log.d(TAG, "volleySavePaytmUserSuccessResponse: user successfully saved");
        //TODO: save paytm data in userDetails class
    }*/

    private void savePaytmUserDetails() {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivitySendOtpBinding.getRoot());
            return;
        }

        showProgressDialog();

        PaytmUtility.savePaytmUserDetails(mContext, mResourceOwnerCustomerId, mAccessToken, mMobileNumber
                , mSavePaytmUserResponseListener, NetworkUtility.TAGS.PAYMENT_METHOD_TYPE.PAYTM);
    }

    private final PaytmUtility.SavePaytmUserResponseListener mSavePaytmUserResponseListener = new PaytmUtility.SavePaytmUserResponseListener() {
        @Override
        public void volleySavePaytmUserSuccessResponse() {
            Log.d(TAG, "volleySavePaytmUserSuccessResponse: user successfully saved");
            //TODO: save paytm data in userDetails class
        }

        @Override
        public void showSpecificErrorMessage(String errorMessage) {
            Utility.showSnackBar(errorMessage, mActivitySendOtpBinding.getRoot());
            hideProgressDialog();
        }

        @Override
        public void showGeneralizedErrorMessage() {
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivitySendOtpBinding.getRoot());
            hideProgressDialog();
        }

        @Override
        public void volleyError() {
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivitySendOtpBinding.getRoot());
            hideProgressDialog();
        }
    };
    ///////////////////////////////////////////////////////////Volley save paytm user Web call ends///////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////Paytm Check Balance API call starts///////////////////////////////////////////////////////////

    private void checkBalance() {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivitySendOtpBinding.getRoot());
            return;
        }

        //Show Progress
        showProgressDialog();

        PaytmUtility.checkBalance(mContext, mAccessToken, mCheckBalanceResponseListener);
    }

    private final PaytmUtility.CheckBalanceResponseListener mCheckBalanceResponseListener = new PaytmUtility.CheckBalanceResponseListener() {
        @Override
        public void paytmCheckBalanceSuccessResponse(JSONObject jsonObject) {
            try {
                requestGuid = jsonObject.getString(NetworkUtility.PAYTM.PARAMETERS.requestGuid);
                paytmReturnedOrderId = jsonObject.getString(orderId);
                JSONObject responseParamJson = jsonObject.getJSONObject(response);
                totalBalance = responseParamJson.getDouble(NetworkUtility.PAYTM.PARAMETERS.totalBalance);
                paytmWalletBalance = responseParamJson.getDouble(NetworkUtility.PAYTM.PARAMETERS.paytmWalletBalance);
                ownerGuid = responseParamJson.getString(NetworkUtility.PAYTM.PARAMETERS.ownerGuid);
                walletGrade = responseParamJson.getString(NetworkUtility.PAYTM.PARAMETERS.walletGrade);
                ssoId = responseParamJson.getString(NetworkUtility.PAYTM.PARAMETERS.ssoId);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (amount.contains(Utility.COMMA)) {
                amount = amount.replace(Utility.COMMA, Utility.EMPTY_STRING);
            }
            isLowBalance = paytmWalletBalance < Double.parseDouble(amount);
            if (isLowBalance) {
//            BTN_WHICH = BTN_IS_ADD_AMOUNT;
                //TODO: add amount
                payableAmount = Math.ceil(Double.parseDouble(amount) - paytmWalletBalance);
                AddMoneyActivity.newInstance(mContext, amount, payableAmount, mAccessToken, mMobileNumber, mResourceOwnerCustomerId, paytmWalletBalance);
            } else {
//            BTN_WHICH = BTN_IS_CONFIRM;
                //TODO: withdraw money
            }
            hideProgressDialog();
        }

        //This method is called when access token expires early due to some reason and we need to do whole OAuth process again
        @Override
        public void paytmInvalidAuthorization() {
            //TODO: implement that if accessToken is valid i.e. 1 month is not due directly call checkBalance API.
        }

        @Override
        public void showGeneralizedErrorMessage() {
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivitySendOtpBinding.getRoot());
            hideProgressDialog();
        }

        @Override
        public void paytmInvalidMobileNumber() {
            Utility.showSnackBar(getString(R.string.validate_phone_number), mActivitySendOtpBinding.getRoot());
            hideProgressDialog();
        }

        @Override
        public void paytmAccountBlocked() {
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivitySendOtpBinding.getRoot());
            hideProgressDialog();
        }

        @Override
        public void volleyError() {
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivitySendOtpBinding.getRoot());
            hideProgressDialog();
        }
    };
    ///////////////////////////////////////////////////////////Paytm Check Balance API call ends///////////////////////////////////////////////////////////

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mEtText = mActivitySendOtpBinding.etMobileNumber.getText().toString();
            if (isValidated()) {
                switch (v.getId()) {
                    case R.id.tv_send_otp:
                    /*
                         * Hide the Keyboard if it opened
                         */
                        Utility.hideKeyboard(VerifyOtpActivity.this);
                        verifyOTP();
                        break;
                }
            }
        }
    };

    private boolean isValidated() {
        LogUtils.LOGD(TAG, "isValidated() ");

        if (TextUtils.isEmpty(mActivitySendOtpBinding.etMobileNumber.getText())) {
            Utility.showSnackBar(getString(R.string.validate_otp_empty), mActivitySendOtpBinding.getRoot());
            return false;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void onDestroy() {
        try {
            EventBus.getDefault().unregister(this);
        } catch (Exception e) {

        }
        super.onDestroy();

    }

    /**
     * Event Bus Callbacks
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        Log.d(TAG, "onMessageEvent() called with: event = [" + event.BROADCAST_ACTION + "]");
        if (event.BROADCAST_ACTION == Utility.BROADCAST_TYPE.PAYTM_RESPONSE) {
            // Check the response <code></code>
            if (event.paytmResponse.isSuccess) {
                finish();
            }
        }
    }
}
