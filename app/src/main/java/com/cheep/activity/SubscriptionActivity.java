package com.cheep.activity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.cheep.BuildConfig;
import com.cheep.R;
import com.cheep.databinding.ActivitySubscriptionBinding;
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
import java.util.HashMap;
import java.util.Map;

import static com.cheep.utils.LogUtils.makeLogTag;

public class SubscriptionActivity extends BaseAppCompatActivity {

    ActivitySubscriptionBinding binding;
    private String mChecksumHash;
    private static final String TAG = makeLogTag(SubscriptionActivity.class);


    private String generatedOrderId;
    private String accessToken;
    private String phoneNo;
    private String custId;
    private String amount;
//    private String amount = "1000";

    @Override
    protected void initiateUI() {
        if (getIntent().hasExtra(Utility.Extra.AMOUNT)) {
            amount = getIntent().getExtras().getString(Utility.Extra.AMOUNT);
            accessToken = getIntent().getExtras().getString(Utility.Extra.ACCESS_TOKEN);
            phoneNo = getIntent().getExtras().getString(Utility.Extra.MOBILE_NUMBER);
            custId = getIntent().getExtras().getString(Utility.Extra.CUST_ID);
            callsubcrition();
        }

    }

    @Override
    protected void setListeners() {
     /*   binding.btnPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callsubcrition();
            }
        });*/

      /*  binding.btnRenew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callRenew();
            }
        });*/
    }

    private void callRenew() {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, binding.getRoot());
            return;
        }


        showProgressDialog();

        generatedOrderId = PaytmUtility.autoRenewal(mContext, amount, "304359", new PaytmUtility.GetChecksumResponseListener() {
            @Override
            public void volleyGetChecksumSuccessResponse(String checksumhash) {
                Log.d(TAG, "volleyGetChecksumSuccessResponse() called with: checksumHash = [" + checksumhash + "]");
                // encode the checksum
                try {
                    mChecksumHash = new String(Base64.decode(checksumhash));
                    renew();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                hideProgressDialog();
                Log.i(TAG, "volleyGetChecksumSuccessResponse: Output: " + mChecksumHash);
            }

            @Override
            public void showSpecificErrorMessage(String errorMessage) {
                Utility.showSnackBar(errorMessage, binding.getRoot());
                hideProgressDialog();
            }

            @Override
            public void showGeneralizedErrorMessage() {
                Utility.showSnackBar(getString(R.string.label_something_went_wrong), binding.getRoot());
                hideProgressDialog();
            }

            @Override
            public void volleyError() {
                Utility.showSnackBar(getString(R.string.label_something_went_wrong), binding.getRoot());
                hideProgressDialog();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_subscription);
        initiateUI();
        setListeners();
    }

    private void callsubcrition() {

        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, binding.getRoot());
            return;
        }


        showProgressDialog();

        generatedOrderId = PaytmUtility.getChecksumForSubscription(mContext,
                amount,
                accessToken,
                phoneNo,
                custId,
                mGetChecksumResponseListenerForAddMoney);
    }

    private PaytmUtility.GetChecksumResponseListener mGetChecksumResponseListenerForAddMoney = new PaytmUtility.GetChecksumResponseListener() {
        @Override
        public void volleyGetChecksumSuccessResponse(String checksumhash) {
            Log.d(TAG, "volleyGetChecksumSuccessResponse() called with: checksumHash = [" + checksumhash + "]");
            // encode the checksum
            try {
                mChecksumHash = new String(Base64.decode(checksumhash));
                subscription();
            } catch (IOException e) {
                e.printStackTrace();
            }
            hideProgressDialog();
            Log.i(TAG, "volleyGetChecksumSuccessResponse: Output: " + mChecksumHash);
        }

        @Override
        public void showSpecificErrorMessage(String errorMessage) {
            Utility.showSnackBar(errorMessage, binding.getRoot());
            hideProgressDialog();
        }

        @Override
        public void showGeneralizedErrorMessage() {
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), binding.getRoot());
            hideProgressDialog();
        }

        @Override
        public void volleyError() {
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), binding.getRoot());
            hideProgressDialog();
        }
    };


    ///////////////////////////////////////////////////////////Paytm Add Money API call starts///////////////////////////////////////////////////////////
    private void subscription() {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, binding.getRoot());
            return;
        }

        //Show Progress
        showProgressDialog();
        /**
         * We need to call Webview with provided POST datas
         */
        Map<String, Object> bodyParams = new HashMap<>();
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.CHANNEL_ID, BuildConfig.CHANNEL_ID);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.WEBSITE, BuildConfig.WEBSITE);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.ORDER_ID, generatedOrderId);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.INDUSTRY_TYPE_ID, BuildConfig.INDUSTRY_TYPE_ID);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.CALLBACK_URL, NetworkUtility.WS.VERIFY_CHECKSUM);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.SSO_TOKEN, accessToken);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.REQUEST_TYPE, Utility.SUBSCRIBE);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.TXN_AMOUNT, amount);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.MID, BuildConfig.SANDBOX_MERCHANT_ID);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.CUST_ID, custId);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.MOBILE_NO, phoneNo);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.EMAIL, PreferenceUtility.getInstance(mContext).getUserDetails().email);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.THEME, Utility.MERCHANT);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.SUBS_SERVICE_ID, "123");
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.SUBS_AMOUNT_TYPE, "FIX");
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.SUBS_FREQUENCY, 1);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.SUBS_FREQUENCY_UNIT, "DAY");
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.SUBS_EXPIRY_DATE, "2028-03-30");
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.SUBS_ENABLE_RETRY, 1);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.CHECKSUMHASH, mChecksumHash);

        String postData = generatePostDataString(bodyParams); //"username=" + URLEncoder.encode(my_username, "UTF-8") + "&password=" + URLEncoder.encode(my_password, "UTF-8");

        if (Build.VERSION.SDK_INT >= 21) {
            binding.webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        binding.webView.setWebViewClient(mWebViewClient);
        binding.webView.getSettings().setJavaScriptEnabled(true);
        binding.webView.postUrl(NetworkUtility.PAYTM.WALLET_APIS.ADD_MONEY + "?orderid=" + generatedOrderId, postData.getBytes());

//         Show the webView
//        binding.btnPay.setVisibility(View.GONE);
        binding.webView.setVisibility(View.VISIBLE);
    }

    private void renew() {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, binding.getRoot());
            return;
        }

        //Show Progress
        showProgressDialog();
        /**
         * We need to call Webview with provided POST datas
         */
        Map<String, Object> bodyParams = new HashMap<>();
        bodyParams.put(NetworkUtility.TAGS.ORDER_ID, generatedOrderId);
        bodyParams.put(NetworkUtility.TAGS.TXN_AMOUNT, amount);
        bodyParams.put(NetworkUtility.TAGS.SUBS_ID, "304359");
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.CHECKSUMHASH, mChecksumHash);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.MID, BuildConfig.SANDBOX_MERCHANT_ID);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.REQUEST_TYPE, Utility.RENEW_SUBSCRIPTION);

        String postData = generatePostDataString(bodyParams); //"username=" + URLEncoder.encode(my_username, "UTF-8") + "&password=" + URLEncoder.encode(my_password, "UTF-8");

        if (Build.VERSION.SDK_INT >= 21) {
            binding.webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        binding.webView.setWebViewClient(mWebViewClient);
        binding.webView.getSettings().setJavaScriptEnabled(true);
        binding.webView.postUrl(NetworkUtility.PAYTM.WALLET_APIS.ADD_MONEY, postData.getBytes());

        // Show the webView
//        binding.btnPay.setVisibility(View.GONE);
        binding.webView.setVisibility(View.VISIBLE);
    }

    /* private String generatePostDataString(Map<String, String> bodyParams,int s) {
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
     }*/
    private String generatePostDataString(Map<String, ?> bodyParams) {
        StringBuilder postData = new StringBuilder();
        if (bodyParams == null) {
            return postData.toString();
        }
        for (String key : bodyParams.keySet()) {
            if (!postData.toString().isEmpty()) {
                postData = postData.append("&");
            }
            try {
                postData = postData.append(key).append("=").append(URLEncoder.encode(bodyParams.get(key).toString(), "UTF-8"));
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
            binding.progress.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            Log.d(TAG, "onPageFinished() called with: view = [" + view + "], url = [" + url + "]");
            binding.progress.setVisibility(View.GONE);
            /*
            Check if the callback url comes and go ahead
             */
            if (url.equalsIgnoreCase(NetworkUtility.WS.VERIFY_CHECKSUM)) {
                showProgressDialog();
                ///Call Webservice from here
                PaytmUtility.callVerifyOrderTransaction(mContext, generatedOrderId, mVerifyTransactionSubscriptionResponseListener);
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
    private final PaytmUtility.VerifyTransactionMoneyResponseListener mVerifyTransactionSubscriptionResponseListener = new PaytmUtility.VerifyTransactionMoneyResponseListener() {
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
                paytmResponse.subsId = mCallbackResponse.optString(NetworkUtility.TAGS.SUBS_ID);
                paytmResponse.isSubscription = Utility.BOOLEAN.YES;
                if (!TextUtils.isEmpty(responseCode) && responseCode.equalsIgnoreCase(NetworkUtility.PAYTM.RESPONSE_CODES.LOGIN)) {
                    // Close the screen and pass the success message to @com.cheep.activity.PaymentChoiceActivity
                    paytmResponse.isSuccess = true;

                    // TODO : Here we need to call subscribed api

//                    TODO : Once withdraw api called, need to remove below code
                    // Create the message event and sent the broadcast to @PaymentChoiceActivity
                    MessageEvent messageEvent = new MessageEvent();
                    messageEvent.BROADCAST_ACTION = Utility.BROADCAST_TYPE.PAYTM_RESPONSE;
                    messageEvent.paytmResponse = paytmResponse;

                    // Send the event
                    EventBus.getDefault().post(messageEvent);

                    // Finish the activity at the end
                    finish();

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

    public static void newInstance(Context mContext, String amount, String accessToken, String mobileNumber, String resourceOwnerCustomerId) {
        Intent intent = new Intent(mContext, SubscriptionActivity.class);
        intent.putExtra(Utility.Extra.AMOUNT, amount);
        intent.putExtra(Utility.Extra.ACCESS_TOKEN, accessToken);
        intent.putExtra(Utility.Extra.MOBILE_NUMBER, mobileNumber);
        intent.putExtra(Utility.Extra.CUST_ID, resourceOwnerCustomerId);
        mContext.startActivity(intent);

    }
    ///////////////////////////////////////////////////////////Paytm Add Money API call ends///////////////////////////////////////////////////////////


}
