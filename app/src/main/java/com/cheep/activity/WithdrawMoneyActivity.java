package com.cheep.activity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.cheep.R;
import com.cheep.databinding.ActivitySendOtpBinding;
import com.cheep.model.MessageEvent;
import com.cheep.network.NetworkUtility;
import com.cheep.utils.PaytmUtility;
import com.cheep.utils.Utility;
import com.mixpanel.android.java_websocket.util.Base64;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class WithdrawMoneyActivity extends BaseAppCompatActivity {

    private static final String TAG = WithdrawMoneyActivity.class.getSimpleName();
    private ActivitySendOtpBinding mActivitySendOtpBinding;

    private String amount;
    private double payableAmount;

    private String mAccessToken;
    private String mMobileNumber;
    private double paytmWalletBalance;
    private boolean isPaytm;
    private String mEtText;
    private String mChecksumHash;

    //returned in response of check balance api
    private String generatedOrderId;

    // returned in response of verify otp api
    private String mResourceOwnerCustomerId;

    public static void newInstance(Context context, String amount, double payableAmount, String accessToken, String mobileNumber
            , String resourceOwnerCustomerId, double paytmWalletBalance, boolean isPaytm) {
        Intent intent = new Intent(context, WithdrawMoneyActivity.class);
        intent.putExtra(Utility.Extra.AMOUNT, amount);
        intent.putExtra(Utility.Extra.PAYABLE_AMOUNT, payableAmount);
        intent.putExtra(Utility.Extra.ACCESS_TOKEN, accessToken);
        intent.putExtra(Utility.Extra.MOBILE_NUMBER, mobileNumber);
        intent.putExtra(Utility.Extra.CUST_ID, resourceOwnerCustomerId);
        intent.putExtra(Utility.Extra.PAYTM_WALLET_BALANCE, paytmWalletBalance);
        intent.putExtra(Utility.Extra.DATA, isPaytm);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivitySendOtpBinding = DataBindingUtil.setContentView(this, R.layout.activity_send_otp);
        initiateUI();
        setupActionbar();
    }

    @Override
    protected void initiateUI() {
        if (getIntent().hasExtra(Utility.Extra.AMOUNT)) {
            amount = getIntent().getExtras().getString(Utility.Extra.AMOUNT);
            payableAmount = getIntent().getExtras().getDouble(Utility.Extra.PAYABLE_AMOUNT);
            mAccessToken = getIntent().getExtras().getString(Utility.Extra.ACCESS_TOKEN);
            mMobileNumber = getIntent().getExtras().getString(Utility.Extra.MOBILE_NUMBER);
            mResourceOwnerCustomerId = getIntent().getExtras().getString(Utility.Extra.CUST_ID);
            paytmWalletBalance = getIntent().getExtras().getDouble(Utility.Extra.PAYTM_WALLET_BALANCE);
            isPaytm = getIntent().getExtras().getBoolean(Utility.Extra.DATA);

        }

        mActivitySendOtpBinding.ivMobile.setVisibility(View.GONE);
        mActivitySendOtpBinding.tvDefaultCountryCode.setVisibility(View.GONE);

        LinearLayout.LayoutParams tvEnterNoLinkXAccountLayoutParams =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvEnterNoLinkXAccountLayoutParams.setMargins((int) (Utility.convertDpToPixel(34f, mContext))
                , (int) (Utility.convertDpToPixel(6f, mContext))
                , (int) (Utility.convertDpToPixel(34f, mContext))
                , (int) (Utility.convertDpToPixel(34f, mContext)));
        mActivitySendOtpBinding.tvEnterNoLinkXAccount.setLayoutParams(tvEnterNoLinkXAccountLayoutParams);
        mActivitySendOtpBinding.tvEnterNoLinkXAccount.setTextSize(14);
        if (isPaytm)
            mActivitySendOtpBinding.tvEnterNoLinkXAccount.setText(getString(R.string.label_notify_paying_by_wallet, getString(R.string.label_paytm)));
        else
            mActivitySendOtpBinding.tvEnterNoLinkXAccount.setText(getString(R.string.label_notify_paying_by_wallet, getString(R.string.label_mobikwik)));

        LinearLayout.LayoutParams tvAmountLayoutParams =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvAmountLayoutParams.setMargins(0, 0, 0, (int) (Utility.convertDpToPixel(87f, mContext)));
        mActivitySendOtpBinding.tvAmount.setLayoutParams(tvAmountLayoutParams);
        mActivitySendOtpBinding.tvAmount.setTextSize(32);
        mActivitySendOtpBinding.tvAmount.setText(getString(R.string.rupee_symbol_x, amount));

        mActivitySendOtpBinding.tvAmount.setVisibility(View.VISIBLE);
        mActivitySendOtpBinding.llEtContainer.setVisibility(View.GONE);
        mActivitySendOtpBinding.tvWeCreateXWallet.setVisibility(View.GONE);
//                mActivitySendOtpBinding.etMobileNumber.removeTextChangedListener(textWatcher);
        mActivitySendOtpBinding.tvSendOtp.setText(getString(R.string.label_confirm));
        mActivitySendOtpBinding.tvSendOtp.setOnClickListener(mOnClickListener);
        mActivitySendOtpBinding.tvSendOtp.setEnabled(true);
    }

    @Override
    protected void setListeners() {

    }

    private void setupActionbar() {
        mActivitySendOtpBinding.textTitle.setText(getString(R.string.label_confirm_payment));
        setSupportActionBar(mActivitySendOtpBinding.toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(Utility.EMPTY_STRING);
        actionBar.setDisplayHomeAsUpEnabled(true);
        //noinspection RestrictedApi
    }

    ///////////////////////////////////////////////////////////Volley Get Checksum Hash Web call starts///////////////////////////////////////////////////////////
    private void callgetChecksumForWithdrawMoney() {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivitySendOtpBinding.getRoot());
            return;
        }

        showProgressDialog();

        generatedOrderId = PaytmUtility.getChecksumForWithdrawMoney(mContext,
                amount,
                mAccessToken,
                mMobileNumber,
                mResourceOwnerCustomerId,
                mGetChecksumResponseListenerForWithdrawMoney);
    }

    // Withdraw Money Checksum Callback
    PaytmUtility.GetChecksumResponseListener mGetChecksumResponseListenerForWithdrawMoney = new PaytmUtility.GetChecksumResponseListener() {
        @Override
        public void volleyGetChecksumSuccessResponse(String checksumHash) {
            Log.d(TAG, "volleyGetChecksumSuccessResponse() called with: checksumHash = [" + checksumHash + "]");
            try {
                mChecksumHash = new String(Base64.decode(checksumHash));
                withdrawMoney();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
    ///////////////////////////////////////////////////////////Volley Get Checksum Hash Web call ends///////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////Paytm Withdraw Money API call starts///////////////////////////////////////////////////////////
    private void withdrawMoney() {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivitySendOtpBinding.getRoot());
            return;
        }

        //Show Progress
        showProgressDialog();

        PaytmUtility.withdrawMoney(mContext, generatedOrderId, mAccessToken, amount, mChecksumHash, mResourceOwnerCustomerId, mMobileNumber, mWithdrawMoneyResponseListener);
    }

    /**
     * Callbacks (Verify Transaction) for Add Money
     */
    private final PaytmUtility.WithdrawMoneyResponseListener mWithdrawMoneyResponseListener = new PaytmUtility.WithdrawMoneyResponseListener() {
        @Override
        public void paytmWithdrawMoneySuccessResponse(String responseInJsonOrInHTML) {
            // Hide the progresDialog
            hideProgressDialog();

            /*
             * If its empty, we just need to redirect the user with failure message
             */
            if (TextUtils.isEmpty(responseInJsonOrInHTML)) {
                // Create the message event and sent the broadcast to @PaymentChoiceActivity
                MessageEvent messageEvent = new MessageEvent();
                messageEvent.BROADCAST_ACTION = Utility.BROADCAST_TYPE.PAYTM_RESPONSE;
                MessageEvent.PaytmResponse paytmResponse = new MessageEvent.PaytmResponse();
                paytmResponse.isSuccess = false;
                messageEvent.paytmResponse = paytmResponse;

                // Send the event
                EventBus.getDefault().post(messageEvent);
            }


            // Parse the data and revert back with response accordingly
            try {
                final JSONObject mRoot = new JSONObject(responseInJsonOrInHTML);
                String responseCode = mRoot.optString(NetworkUtility.PAYTM.PARAMETERS.ResponseCode);

                MessageEvent.PaytmResponse paytmResponse = new MessageEvent.PaytmResponse();
                paytmResponse.ResponseCode = responseCode;
                paytmResponse.ResponsePayLoad = responseInJsonOrInHTML;
                // Close the screen and pass the success message to @com.cheep.activity.PaymentChoiceActivity
// Close the screen and pass the failure message to @com.cheep.activity.PaymentChoiceActivity
                paytmResponse.isSuccess = !TextUtils.isEmpty(responseCode) && responseCode.equalsIgnoreCase(NetworkUtility.PAYTM.RESPONSE_CODES.LOGIN);

                // Create the message event and sent the broadcast to @PaymentChoiceActivity
                MessageEvent messageEvent = new MessageEvent();
                messageEvent.BROADCAST_ACTION = Utility.BROADCAST_TYPE.PAYTM_RESPONSE;
                messageEvent.paytmResponse = paytmResponse;

                // Send the event
                EventBus.getDefault().post(messageEvent);

            } catch (JSONException e) {
                e.printStackTrace();

                // Create the message event and sent the broadcast to @PaymentChoiceActivity
                MessageEvent messageEvent = new MessageEvent();
                messageEvent.BROADCAST_ACTION = Utility.BROADCAST_TYPE.PAYTM_RESPONSE;
                MessageEvent.PaytmResponse paytmResponse = new MessageEvent.PaytmResponse();
                paytmResponse.isSuccess = false;
                messageEvent.paytmResponse = paytmResponse;

                // Send the event
                EventBus.getDefault().post(messageEvent);
            }

            // Finish the activity at the end
            finish();
        }

        @Override
        public void volleyError() {
            Log.d(TAG, "volleyError() called");
            hideProgressDialog();

            // Create the message event and sent the broadcast to @PaymentChoiceActivity
            MessageEvent messageEvent = new MessageEvent();
            messageEvent.BROADCAST_ACTION = Utility.BROADCAST_TYPE.PAYTM_RESPONSE;
            MessageEvent.PaytmResponse paytmResponse = new MessageEvent.PaytmResponse();
            paytmResponse.isSuccess = false;
            messageEvent.paytmResponse = paytmResponse;

            // Send the event
            EventBus.getDefault().post(messageEvent);

            // Finish the activity at the end
            finish();
        }
    };
    ///////////////////////////////////////////////////////////Paytm Withdraw Money API call ends///////////////////////////////////////////////////////////

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mEtText = mActivitySendOtpBinding.etMobileNumber.getText().toString();
            switch (v.getId()) {
                case R.id.tv_send_otp:
                    /*
                     * Hide the Keyboard if it opened
                     */
                    Utility.hideKeyboard(WithdrawMoneyActivity.this);
                    callgetChecksumForWithdrawMoney();
                    break;
            }
        }
    };

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
}