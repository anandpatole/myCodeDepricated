package com.cheep.activity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import com.cheep.BuildConfig;
import com.cheep.R;
import com.cheep.databinding.ActivityAddMoneyBinding;
import com.cheep.model.MessageEvent;
import com.cheep.network.NetworkUtility;
import com.cheep.utils.PaytmUtility;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;
import com.mixpanel.android.java_websocket.util.Base64;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class AddMoneyActivity extends BaseAppCompatActivity {

    private static final String TAG = AddMoneyActivity.class.getSimpleName();
    private ActivityAddMoneyBinding mActivitySendOtpBinding;
    private String mEtText;

    private String amount;
    private double payableAmount;
    private String mChecksumHash;
    private String mAccessToken;
    private String mMobileNumber;
    private double paytmWalletBalance;

    //returned in response of check balance api
    private String generatedOrderId;

    // returned in response of verify otp api
    private String mResourceOwnerCustomerId;

    public static void newInstance(Context context, String amount, double payableAmount
            , String accessToken, String mobileNumber, String resourceOwnerCustomerId, double paytmWalletBalance) {
        Intent intent = new Intent(context, AddMoneyActivity.class);
        intent.putExtra(Utility.Extra.AMOUNT, amount);
        intent.putExtra(Utility.Extra.PAYABLE_AMOUNT, payableAmount);
        intent.putExtra(Utility.Extra.ACCESS_TOKEN, accessToken);
        intent.putExtra(Utility.Extra.MOBILE_NUMBER, mobileNumber);
        intent.putExtra(Utility.Extra.CUST_ID, resourceOwnerCustomerId);
        intent.putExtra(Utility.Extra.PAYTM_WALLET_BALANCE, paytmWalletBalance);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivitySendOtpBinding = DataBindingUtil.setContentView(this, R.layout.activity_add_money);
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
        }

        mActivitySendOtpBinding.ivMobile.setVisibility(View.GONE);
        mActivitySendOtpBinding.tvDefaultCountryCode.setVisibility(View.GONE);

        LinearLayout.LayoutParams tvEnterNoLinkXAccountLayoutParams =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvEnterNoLinkXAccountLayoutParams.setMargins(0, 0, 0, 0);

        mActivitySendOtpBinding.tvEnterNoLinkXAccount.setLayoutParams(tvEnterNoLinkXAccountLayoutParams);
        mActivitySendOtpBinding.tvEnterNoLinkXAccount.setText(getString(R.string.label_amount_payable));
        mActivitySendOtpBinding.tvAmount.setText(getString(R.string.rupee_symbol_x, amount));
        mActivitySendOtpBinding.tvAmount.setVisibility(View.VISIBLE);
        mActivitySendOtpBinding.tvLowBalance.setVisibility(View.VISIBLE);
        mActivitySendOtpBinding.etMobileNumber.setTextColor(ContextCompat.getColor(mContext, R.color.splash_gradient_end));
        mActivitySendOtpBinding.etMobileNumber.setHint(getString(R.string.label_enter_amount));
        mActivitySendOtpBinding.etMobileNumber.setText(formatAmount(String.valueOf(payableAmount)));
        mActivitySendOtpBinding.etMobileNumber.setEnabled(true);
        mActivitySendOtpBinding.etMobileNumber.setGravity(Gravity.CENTER);
        mActivitySendOtpBinding.etMobileNumber.setSelection(mActivitySendOtpBinding.etMobileNumber.getText().toString().length());

        mActivitySendOtpBinding.tvSendOtp.setText(getString(R.string.label_add_amount));
        mActivitySendOtpBinding.tvSendOtp.setOnClickListener(mOnClickListener);
        mActivitySendOtpBinding.tvSendOtp.setEnabled(true);
//                mActivitySendOtpBinding.etMobileNumber.removeTextChangedListener(textWatcher);
        mActivitySendOtpBinding.tvWeCreateXWallet.setText(getString(R.string.label_current_balance, formatAmount(String.valueOf(paytmWalletBalance))));
        mActivitySendOtpBinding.tvWeCreateXWallet.setGravity(Gravity.CENTER);
    }

    private String formatAmount(String amount) {
        return new DecimalFormat("##.##").format(amount);
    }

    @Override
    protected void setListeners() {

    }

    private void setupActionbar() {
        mActivitySendOtpBinding.textTitle.setText(getString(R.string.label_recharge_wallet));
        setSupportActionBar(mActivitySendOtpBinding.toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(Utility.EMPTY_STRING);
        actionBar.setDisplayHomeAsUpEnabled(true);
        //noinspection RestrictedApi
        actionBar.setDefaultDisplayHomeAsUpEnabled(true);
    }

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
                        Utility.hideKeyboard(AddMoneyActivity.this);
                        callgetChecksumForAddMoney();
//                        addMoney();
                        break;
                }
            }
        }
    };

    ///////////////////////////////////////////////////////////Volley Get Checksum Hash Web call starts///////////////////////////////////////////////////////////

    private void callgetChecksumForAddMoney() {


        double edtAmount = Double.parseDouble(mEtText);
        if (edtAmount < payableAmount) {
            Utility.showToast(mContext, "Please enter minimum " + payableAmount + " amount to proceed");
            return;
        }


        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivitySendOtpBinding.getRoot());
            return;
        }


        showProgressDialog();

        generatedOrderId = PaytmUtility.getChecksumForAddMoney(mContext,
                mEtText,
                mAccessToken,
                mMobileNumber,
                mResourceOwnerCustomerId,
                mGetChecksumResponseListenerForAddMoney);
    }


    // Add Money Checksum Callback
    PaytmUtility.GetChecksumResponseListener mGetChecksumResponseListenerForAddMoney = new PaytmUtility.GetChecksumResponseListener() {
        @Override
        public void volleyGetChecksumSuccessResponse(String checksumHash) {
            Log.d(TAG, "volleyGetChecksumSuccessResponse() called with: checksumHash = [" + checksumHash + "]");
            // encode the checksum
            try {
                mChecksumHash = new String(Base64.decode(checksumHash));
                addMoney();
            } catch (IOException e) {
                e.printStackTrace();
            }
            hideProgressDialog();
            Log.i(TAG, "volleyGetChecksumSuccessResponse: Output: " + mChecksumHash);
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

    ///////////////////////////////////////////////////////////Paytm Add Money API call starts///////////////////////////////////////////////////////////
    private void addMoney() {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivitySendOtpBinding.getRoot());
            return;
        }

        //Show Progress
        showProgressDialog();

        /**
         * We need to call Webview with provided POST datas
         */
        Map<String, String> bodyParams = new HashMap<>();
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.CHANNEL_ID, BuildConfig.CHANNEL_ID);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.WEBSITE, BuildConfig.WEBSITE);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.ORDER_ID, generatedOrderId);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.INDUSTRY_TYPE_ID, BuildConfig.INDUSTRY_TYPE_ID);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.CALLBACK_URL, NetworkUtility.WS.VERIFY_CHECKSUM);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.SSO_TOKEN, mAccessToken);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.REQUEST_TYPE, Utility.ADD_MONEY);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.TXN_AMOUNT, mEtText);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.CHECKSUMHASH, mChecksumHash);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.MID, BuildConfig.SANDBOX_MERCHANT_ID);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.CUST_ID, mResourceOwnerCustomerId);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.MOBILE_NO, mMobileNumber);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.EMAIL, PreferenceUtility.getInstance(mContext).getUserDetails().Email);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.THEME, "merchant");

        String postData = generatePostDataString(bodyParams); //"username=" + URLEncoder.encode(my_username, "UTF-8") + "&password=" + URLEncoder.encode(my_password, "UTF-8");

        if (Build.VERSION.SDK_INT >= 21) {
            mActivitySendOtpBinding.webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        mActivitySendOtpBinding.webView.setWebViewClient(mWebViewClient);
        mActivitySendOtpBinding.webView.getSettings().setJavaScriptEnabled(true);
        mActivitySendOtpBinding.webView.postUrl(NetworkUtility.PAYTM.WALLET_APIS.ADD_MONEY, postData.getBytes());

        // Show the webview
        mActivitySendOtpBinding.svMainLayout.setVisibility(View.GONE);
        mActivitySendOtpBinding.webView.setVisibility(View.VISIBLE);
    }

    private String generatePostDataString(Map<String, String> bodyParams) {
        StringBuilder postData = new StringBuilder();
        if (bodyParams == null) {
            return postData.toString();
        }
        for (String key : bodyParams.keySet()) {
            if (!postData.toString().isEmpty()) {
                postData = postData.append("&");
            }
            try {
                postData = postData.append(key).append("=").append(URLEncoder.encode(bodyParams.get(key), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "generatePostDataString() returned: " + postData.toString());
        return postData.toString();
    }

    /**
     * Customized webview client for Payment Webview
     */
    private WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.d(TAG, "onPageStarted() called with: view = [" + view + "], url = [" + url + "], favicon = [" + favicon + "]");
            super.onPageStarted(view, url, favicon);
            mActivitySendOtpBinding.progress.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            Log.d(TAG, "onPageFinished() called with: view = [" + view + "], url = [" + url + "]");
            mActivitySendOtpBinding.progress.setVisibility(View.GONE);
            /*
            Check if the callback url comes and go ahead
             */
            if (url.equalsIgnoreCase(NetworkUtility.WS.VERIFY_CHECKSUM)) {
                showProgressDialog();
                ///Call Webservice from here
                PaytmUtility.callVerifyOrderTransaction(mContext, generatedOrderId, mVerifyTransactionMoneyResponseListener);
            }
            super.onPageFinished(view, url);
        }

        @Override
        public void onPageCommitVisible(WebView view, String url) {
            Log.d(TAG, "onPageCommitVisible() called with: view = [" + view + "], url = [" + url + "]");
            super.onPageCommitVisible(view, url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Log.d(TAG, "shouldOverrideUrlLoading() called with: view = [" + view + "], request = [" + request + "]");
            return false;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d(TAG, "shouldOverrideUrlLoading() called with: view = [" + view + "], url = [" + url + "]");
            return false;
        }

    };

    /**
     * Callbacks (Verify Transaction) for Add Money
     */
    private final PaytmUtility.VerifyTransactionMoneyResponseListener mVerifyTransactionMoneyResponseListener = new PaytmUtility.VerifyTransactionMoneyResponseListener() {
        @Override
        public void paytmVerifyTransactionMoneyResponse(String responseInJson) {
            Log.d(TAG, "paytmVerifyTransactionMoneyResponse() called with: responseInJson = [" + responseInJson + "]");
            try {

                // Hide the progresDialog
                hideProgressDialog();

                /**
                 * If its empty, we just need to redirect the user with failure message
                 */
                if (TextUtils.isEmpty(responseInJson)) {
                    // Create the message event and sent the broadcast to @PaymentChoiceActivity
                    MessageEvent messageEvent = new MessageEvent();
                    messageEvent.BROADCAST_ACTION = Utility.BROADCAST_TYPE.PAYTM_RESPONSE;
                    MessageEvent.PaytmResponse paytmResponse = new MessageEvent.PaytmResponse();
                    paytmResponse.isSuccess = false;
                    messageEvent.paytmResponse = paytmResponse;

                    // Send the event
                    EventBus.getDefault().post(messageEvent);

                }

                final JSONObject mRoot = new JSONObject(responseInJson);
                JSONObject mData = mRoot.getJSONObject(NetworkUtility.TAGS.DATA);
                JSONObject mCallbackResponse = mData.getJSONObject(NetworkUtility.TAGS.CallbackResponse);
                String responseCode = mCallbackResponse.optString(NetworkUtility.PAYTM.PARAMETERS.RESPCODE);

                MessageEvent.PaytmResponse paytmResponse = new MessageEvent.PaytmResponse();
                paytmResponse.ResponseCode = responseCode;
                paytmResponse.ResponsePayLoad = mCallbackResponse.toString();
                if (!TextUtils.isEmpty(responseCode) && responseCode.equalsIgnoreCase(NetworkUtility.PAYTM.RESPONSE_CODES.LOGIN)) {
                    // Close the screen and pass the success message to @com.cheep.activity.PaymentChoiceActivity
                    paytmResponse.isSuccess = true;

                    // TODO : Here we need to call withdraw api
                    callgetChecksumForWithdrawMoney();

//                    TODO : Once withdraw api called, need to remove below code
                   /* // Create the message event and sent the broadcast to @PaymentChoiceActivity
                    MessageEvent messageEvent = new MessageEvent();
                    messageEvent.BROADCAST_ACTION = Utility.BROADCAST_TYPE.PAYTM_RESPONSE;
                    messageEvent.paytmResponse = paytmResponse;

                    // Send the event
                    EventBus.getDefault().post(messageEvent);

                    // Finish the activity at the end
                    finish();*/

                } else {
                    // Close the screen and pass the failure message to @com.cheep.activity.PaymentChoiceActivity
                    paytmResponse.isSuccess = false;

                    // Create the message event and sent the broadcast to @PaymentChoiceActivity
                    MessageEvent messageEvent = new MessageEvent();
                    messageEvent.BROADCAST_ACTION = Utility.BROADCAST_TYPE.PAYTM_RESPONSE;
                    messageEvent.paytmResponse = paytmResponse;

                    // Send the event
                    EventBus.getDefault().post(messageEvent);

                    // Finish the activity at the end
                    finish();
                }

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

                // Finish the activity at the end
                finish();
            }


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
    ///////////////////////////////////////////////////////////Paytm Add Money API call ends///////////////////////////////////////////////////////////

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
                if (!TextUtils.isEmpty(responseCode) && responseCode.equalsIgnoreCase(NetworkUtility.PAYTM.RESPONSE_CODES.LOGIN)) {
                    // Close the screen and pass the success message to @com.cheep.activity.PaymentChoiceActivity
                    paytmResponse.isSuccess = true;
                } else {
                    // Close the screen and pass the failure message to @com.cheep.activity.PaymentChoiceActivity
                    paytmResponse.isSuccess = false;
                }

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


    private boolean isValidated() {
        if (TextUtils.isEmpty(mActivitySendOtpBinding.etMobileNumber.getText())) {
            Utility.showSnackBar(getString(R.string.validate_empty_amount), mActivitySendOtpBinding.getRoot());
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
}