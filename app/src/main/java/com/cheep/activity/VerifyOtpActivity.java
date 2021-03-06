package com.cheep.activity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
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
import com.cheep.databinding.ActivityVerifyOtpBinding;
import com.cheep.model.MessageEvent;
import com.cheep.model.UserDetails;
import com.cheep.network.NetworkUtility;
import com.cheep.utils.GsonUtility;
import com.cheep.utils.LogUtils;
import com.cheep.utils.PaytmUtility;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import static com.cheep.network.NetworkUtility.PAYTM.PARAMETERS.orderId;
import static com.cheep.network.NetworkUtility.PAYTM.PARAMETERS.response;

public class VerifyOtpActivity extends BaseAppCompatActivity {

    private static final String TAG = VerifyOtpActivity.class.getSimpleName();
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
    private boolean isSubscription = false;
    private int broadCastType = Utility.BROADCAST_TYPE.PAYTM_RESPONSE;

    private double payableAmount;
    private ActivityVerifyOtpBinding mActivityVerifyOtpBinding;
    // set timer for 2 min
    private static final long RESEND_OTP_TIMER = 2 * 60 * 1000;
    // set timer interval 1 sec
    private static final long RESEND_OTP_TIMER_INTERVAL = 1000;

    public static void newInstance(Context context, String mobileNumber, String state, boolean isPaytm, String amount, boolean isSubscription, int broadCastType) {
        Intent intent = new Intent(context, VerifyOtpActivity.class);
        intent.putExtra(Utility.Extra.MOBILE_NUMBER, mobileNumber);
        intent.putExtra(Utility.Extra.STATE, state);
        intent.putExtra(Utility.Extra.DATA, isPaytm);
        intent.putExtra(Utility.Extra.AMOUNT, amount);
        intent.putExtra(Utility.Extra.IS_SUBSCRIPTION, isSubscription);
        intent.putExtra(Utility.Extra.BROADCAST_TYPE, broadCastType);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityVerifyOtpBinding = DataBindingUtil.setContentView(this, R.layout.activity_verify_otp);
        initiateUI();
        setupActionbar();
        EventBus.getDefault().register(this);


    }

    private void setupActionbar() {
        if (isPaytm)
            mActivityVerifyOtpBinding.textTitle.setText(getString(R.string.label_link_x, getString(R.string.label_paytm)));
        else
            mActivityVerifyOtpBinding.textTitle.setText(getString(R.string.label_link_x, getString(R.string.label_mobikwik)));
        setSupportActionBar(mActivityVerifyOtpBinding.toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(Utility.EMPTY_STRING);
        actionBar.setDisplayHomeAsUpEnabled(true);
        //noinspection RestrictedApi
    }

    @Override
    protected void initiateUI() {

        if (getIntent().hasExtra(Utility.Extra.MOBILE_NUMBER)) {
            mMobileNumber = getIntent().getExtras().getString(Utility.Extra.MOBILE_NUMBER);
            mState = getIntent().getExtras().getString(Utility.Extra.STATE);
            isPaytm = getIntent().getExtras().getBoolean(Utility.Extra.DATA);
            amount = getIntent().getExtras().getString(Utility.Extra.AMOUNT);
            isSubscription = getIntent().getExtras().getBoolean(Utility.Extra.IS_SUBSCRIPTION, false);
            broadCastType = getIntent().getExtras().getInt(Utility.Extra.BROADCAST_TYPE);
        }


        String sendOTPString = getString(R.string.label_send_otp_again);
        String formattednumber = mMobileNumber.substring(0, 5) + " " + mMobileNumber.substring(5);
        mActivityVerifyOtpBinding.tvEnterNoLinkXAccount.setText(getString(R.string.label_enter_otp_sent_on_x, formattednumber));
//                mActivityVerifyOtpBinding.tvSendOtp.setText(getString(R.string.label_proceed));
        mActivityVerifyOtpBinding.tvSendOtp.setEnabled(false);
        mActivityVerifyOtpBinding.tvWeCreateXWallet.setVisibility(View.INVISIBLE);
        timer = new CountDownTimer(RESEND_OTP_TIMER, RESEND_OTP_TIMER_INTERVAL) {

            public void onTick(long millisUntilFinished) {
                currentMilliSeconds = millisUntilFinished;
                isTimerOnGoing = true;

                int minutes = (int) TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished);
                int seconds = (int) (TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)));

                if (!mActivityVerifyOtpBinding.etMobileNumber.getText().toString().isEmpty()) {
                    mActivityVerifyOtpBinding.tvSendOtp.setSelected(true);
                    mActivityVerifyOtpBinding.tvSendOtp.setText(getString(R.string.label_proceed));
                } else {
                    mActivityVerifyOtpBinding.tvSendOtp.setSelected(false);
                    mActivityVerifyOtpBinding.tvSendOtp.setText(String.format("%02d:%02d", minutes, seconds));
                }
            }

            public void onFinish() {
                isTimerOnGoing = false;
//                if (!mActivityVerifyOtpBinding.etMobileNumber.getText().toString().isEmpty()) {
//                    mActivityVerifyOtpBinding.tvSendOtp.setSelected(true);
//                    mActivityVerifyOtpBinding.tvSendOtp.setText(getString(R.string.label_proceed));
//                } else {
//                    mActivityVerifyOtpBinding.tvSendOtp.setSelected(false);
//                    mActivityVerifyOtpBinding.tvSendOtp.setText(String.format("00:%02d", 0));
//                }
                mActivityVerifyOtpBinding.tvSendOtp.setEnabled(!mActivityVerifyOtpBinding.etMobileNumber.getText().toString().isEmpty());
                mActivityVerifyOtpBinding.tvSendOtp.setText(getString(R.string.label_proceed));
                mActivityVerifyOtpBinding.tvWeCreateXWallet.setVisibility(View.VISIBLE);
                Utility.hideKeyboard(getApplicationContext());
            }

        }.start();

        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mActivityVerifyOtpBinding.tvSendOtp.setEnabled(charSequence.length() > 0);
                mActivityVerifyOtpBinding.tvSendOtp.setOnClickListener(charSequence.length() > 0 ? mOnClickListener : null);
                if (!mActivityVerifyOtpBinding.etMobileNumber.getText().toString().isEmpty()) {
                    mActivityVerifyOtpBinding.tvSendOtp.setSelected(true);
                    mActivityVerifyOtpBinding.tvSendOtp.setText(getString(R.string.label_proceed));
                } else if (!isTimerOnGoing) {
                    mActivityVerifyOtpBinding.tvSendOtp.setSelected(false);
                    mActivityVerifyOtpBinding.tvSendOtp.setText(getString(R.string.label_proceed));
                } else {
                    mActivityVerifyOtpBinding.tvSendOtp.setSelected(false);
                    int minutes = (int) TimeUnit.MILLISECONDS.toMinutes(currentMilliSeconds);
                    int seconds = (int) (TimeUnit.MILLISECONDS.toSeconds(currentMilliSeconds) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(currentMilliSeconds)));
                    mActivityVerifyOtpBinding.tvSendOtp.setText(String.format("%02d:%02d", minutes, seconds));

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (mActivityVerifyOtpBinding.etMobileNumber.getText().toString().trim().length() > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mActivityVerifyOtpBinding.etMobileNumber.setLetterSpacing(0.5f);
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mActivityVerifyOtpBinding.etMobileNumber.setLetterSpacing(0);
                    }
                }
            }
        };
        mActivityVerifyOtpBinding.etMobileNumber.addTextChangedListener(textWatcher);
        mActivityVerifyOtpBinding.ivMobile.setVisibility(View.GONE);
        mActivityVerifyOtpBinding.tvDefaultCountryCode.setVisibility(View.GONE);
        mActivityVerifyOtpBinding.etMobileNumber.setGravity(Gravity.CENTER);
        mActivityVerifyOtpBinding.etMobileNumber.setText(Utility.EMPTY_STRING);
        mActivityVerifyOtpBinding.etMobileNumber.setTextColor(ContextCompat.getColor(mContext, R.color.grey_varient_8));
        mActivityVerifyOtpBinding.etMobileNumber.setHint(getString(R.string.label_enter_otp));
          /*  if (BuildConfig.BUILD_TYPE.equalsIgnoreCase(Utility.DEBUG)) {
                mActivityVerifyOtpBinding.etMobileNumber.setText(BootstrapConstant.PAYTM_STAGING_MOBILE_NUMBER);
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

        mActivityVerifyOtpBinding.tvWeCreateXWallet.setText(sendOTPSpannableStringBuilder);
        mActivityVerifyOtpBinding.tvWeCreateXWallet.setGravity(Gravity.CENTER);
        mActivityVerifyOtpBinding.tvWeCreateXWallet.setMovementMethod(LinkMovementMethod.getInstance());
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
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityVerifyOtpBinding.getRoot());
            return;
        }
        //Show Progress
        showProgressDialog();
        PaytmUtility.sendOTP(mContext, mMobileNumber, mSendOtpResponseListener, isRegenerated);
    }

    private final PaytmUtility.SendOtpResponseListener mSendOtpResponseListener = new PaytmUtility.SendOtpResponseListener() {
        @Override
        public void paytmSendOtpSuccessResponse(String state, boolean isRegenerated) {
            mActivityVerifyOtpBinding.tvWeCreateXWallet.setVisibility(View.INVISIBLE);
            mState = state;
            timer.start();
            hideProgressDialog();
        }

        @Override
        public void showGeneralizedErrorMessage() {
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityVerifyOtpBinding.getRoot());
            hideProgressDialog();
        }

        @Override
        public void paytmInvalidMobileNumber() {
            Utility.showSnackBar(getString(R.string.validate_phone_number), mActivityVerifyOtpBinding.getRoot());
            hideProgressDialog();
        }

        @Override
        public void paytmAccountBlocked() {
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityVerifyOtpBinding.getRoot());
            hideProgressDialog();
        }

        @Override
        public void volleyError() {
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityVerifyOtpBinding.getRoot());
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
        Utility.showSnackBar(getString(R.string.label_invalid_otp), mActivityVerifyOtpBinding.getRoot());
        hideProgressDialog();
    }*/
    private void verifyOTP() {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityVerifyOtpBinding.getRoot());
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
            if (isSubscription) {
                SubscriptionActivity.newInstance(mContext, amount, accessToken, mMobileNumber, mResourceOwnerCustomerId);
            } else {
                checkBalance();
            }
        }

        @Override
        public void showGeneralizedErrorMessage() {
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityVerifyOtpBinding.getRoot());
            hideProgressDialog();
        }

        @Override
        public void paytmVerifyOtpInvalidOtp() {
            mActivityVerifyOtpBinding.etMobileNumber.setText(Utility.EMPTY_STRING);
            Utility.showSnackBar(getString(R.string.label_invalid_otp), mActivityVerifyOtpBinding.getRoot());
            hideProgressDialog();
        }

        @Override
        public void volleyError() {
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityVerifyOtpBinding.getRoot());
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
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityVerifyOtpBinding.getRoot());
            return;
        }

        showProgressDialog();

        PaytmUtility.savePaytmUserDetails(mContext, mResourceOwnerCustomerId, mAccessToken, mMobileNumber
                , mSavePaytmUserResponseListener, NetworkUtility.PAYMENT_METHOD_TYPE.PAYTM, String.valueOf(mExpires));
    }

    private final PaytmUtility.SavePaytmUserResponseListener mSavePaytmUserResponseListener = new PaytmUtility.SavePaytmUserResponseListener() {
        @Override
        public void volleySavePaytmUserSuccessResponse(String responseString) {
            Log.d(TAG, "volleySavePaytmUserSuccessResponse: user successfully saved");
            //TODO: save paytm data in userDetails class

            try {
                JSONObject jsonObject = new JSONObject(responseString);
                LogUtils.LOGD(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
                        // save paytm data in preferences user details
                        String paytmData = jsonObject.getJSONObject(NetworkUtility.TAGS.DATA).getJSONObject(NetworkUtility.TAGS.PAYMENT_GATEWAY_DATA).toString();
                        UserDetails userDetails = PreferenceUtility.getInstance(VerifyOtpActivity.this).getUserDetails();
                        userDetails.mPaytmUserDetail = (UserDetails.PaytmUserDetail) GsonUtility.getObjectFromJsonString(paytmData, UserDetails.PaytmUserDetail.class);
                        PreferenceUtility.getInstance(VerifyOtpActivity.this).saveUserDetails(userDetails);

                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityVerifyOtpBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        Utility.showSnackBar(jsonObject.getString(NetworkUtility.TAGS.MESSAGE), mActivityVerifyOtpBinding.getRoot());
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void showSpecificErrorMessage(String errorMessage) {
            Utility.showSnackBar(errorMessage, mActivityVerifyOtpBinding.getRoot());
            hideProgressDialog();
        }

        @Override
        public void showGeneralizedErrorMessage() {
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityVerifyOtpBinding.getRoot());
            hideProgressDialog();
        }

        @Override
        public void volleyError() {
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityVerifyOtpBinding.getRoot());
            hideProgressDialog();
        }
    };
    ///////////////////////////////////////////////////////////Volley save paytm user Web call ends///////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////Paytm Check Balance API call starts///////////////////////////////////////////////////////////

    private void checkBalance() {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityVerifyOtpBinding.getRoot());
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
                AddMoneyActivity.newInstance(mContext, amount, payableAmount, mAccessToken, mMobileNumber, mResourceOwnerCustomerId, paytmWalletBalance, broadCastType);
            } else {
//            BTN_WHICH = BTN_IS_CONFIRM;
                //TODO: withdraw money
                WithdrawMoneyActivity.newInstance(mContext, amount, payableAmount, mAccessToken, mMobileNumber, mResourceOwnerCustomerId, paytmWalletBalance, isPaytm, broadCastType);
            }
            // finish activity as now account is linked.
            // this event is fired to finish send otp and verify otp activity
            MessageEvent messageEvent = new MessageEvent();
            messageEvent.BROADCAST_ACTION = Utility.BROADCAST_TYPE.PAYTM_LINKED;
            EventBus.getDefault().post(messageEvent);

            finish();
            hideProgressDialog();
        }

        //This method is called when access token expires early due to some reason and we need to do whole OAuth process again
        @Override
        public void paytmInvalidAuthorization() {
            //TODO: implement that if accessToken is valid i.e. 1 month is not due directly call checkBalance API.
        }

        @Override
        public void showGeneralizedErrorMessage() {
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityVerifyOtpBinding.getRoot());
            hideProgressDialog();
        }

        @Override
        public void paytmInvalidMobileNumber() {
            Utility.showSnackBar(getString(R.string.validate_phone_number), mActivityVerifyOtpBinding.getRoot());
            hideProgressDialog();
        }

        @Override
        public void paytmAccountBlocked() {
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityVerifyOtpBinding.getRoot());
            hideProgressDialog();
        }

        @Override
        public void volleyError() {
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityVerifyOtpBinding.getRoot());
            hideProgressDialog();
        }
    };
    ///////////////////////////////////////////////////////////Paytm Check Balance API call ends///////////////////////////////////////////////////////////

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mEtText = mActivityVerifyOtpBinding.etMobileNumber.getText().toString();
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

        if (TextUtils.isEmpty(mActivityVerifyOtpBinding.etMobileNumber.getText())) {
            Utility.showSnackBar(getString(R.string.validate_otp_empty), mActivityVerifyOtpBinding.getRoot());
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
        if (event.BROADCAST_ACTION ==broadCastType) {
            // We need to finish this activity regardless of the response is success or failure
            finish();
        }
    }
}
