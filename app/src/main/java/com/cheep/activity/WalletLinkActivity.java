package com.cheep.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
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
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import com.cheep.BuildConfig;
import com.cheep.R;
import com.cheep.databinding.ActivityWalletLinkBinding;
import com.cheep.model.MessageEvent;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.utils.LogUtils;
import com.cheep.utils.PaytmUtility;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;
import com.mixpanel.android.java_websocket.util.Base64;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static com.cheep.network.NetworkUtility.PAYTM.PARAMETERS.orderId;
import static com.cheep.network.NetworkUtility.PAYTM.PARAMETERS.response;


public class WalletLinkActivity extends BaseAppCompatActivity implements View.OnClickListener,
        PaytmUtility.SendOtpResponseListener,
        PaytmUtility.VerifyOtpResponseListener,
        PaytmUtility.CheckBalanceResponseListener,
        PaytmUtility.AddMoneyResponseListener,
        PaytmUtility.SavePaytmUserResponseListener {

    private static final String TAG = LogUtils.makeLogTag(WalletLinkActivity.class);
    private ActivityWalletLinkBinding mActivityWalletLinkBinding;
    private boolean isPaytm;
    private String mEtText;
    private TextWatcher textWatcher;
    private String mChecksumHash;

    private final int BTN_IS_SEND_OTP = 0;
    private final int BTN_IS_PROCEED = 1;
    private final int BTN_IS_ADD_AMOUNT = 2;
    private final int BTN_IS_CONFIRM = 3;
    private int BTN_WHICH = -1;

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
    private String mMobileNumber;
    private boolean isLowBalance;
    private double payableAmount;

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

        }

        @Override
        public void showGeneralizedErrorMessage() {

        }

        @Override
        public void volleyError() {

        }
    };

    // Add Money Checksum Callback
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

        }

        @Override
        public void showGeneralizedErrorMessage() {

        }

        @Override
        public void volleyError() {

        }
    };


    public static void newInstance(Context context, boolean isPaytm, String amount) {
        Intent intent = new Intent(context, WalletLinkActivity.class);
        intent.putExtra(Utility.Extra.DATA, isPaytm);
        intent.putExtra(Utility.Extra.AMOUNT, amount);
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
        if (isPaytm)
            mActivityWalletLinkBinding.textTitle.setText(getString(R.string.label_link_x, getString(R.string.label_paytm)));
        else
            mActivityWalletLinkBinding.textTitle.setText(getString(R.string.label_link_x, getString(R.string.label_mobikwik)));
        setSupportActionBar(mActivityWalletLinkBinding.toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(Utility.EMPTY_STRING);
        actionBar.setDisplayHomeAsUpEnabled(true);
        //noinspection RestrictedApi
        actionBar.setDefaultDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void initiateUI() {
        Intent intent = getIntent();
        if (intent.hasExtra(Utility.Extra.DATA)) {
            isPaytm = intent.getExtras().getBoolean(Utility.Extra.DATA);
        }
        if (intent.hasExtra(Utility.Extra.AMOUNT)) {
            amount = intent.getExtras().getString(Utility.Extra.AMOUNT);
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
                mEtText = mActivityWalletLinkBinding.etMobileNumber.getText().toString();
                if (mActivityWalletLinkBinding.tvSendOtp.getText().toString().equalsIgnoreCase(getString(R.string.label_send_otp))) {
                    BTN_WHICH = BTN_IS_SEND_OTP;
                    mMobileNumber = mEtText;
                } else if (mActivityWalletLinkBinding.tvSendOtp.getText().toString().equalsIgnoreCase(getString(R.string.label_proceed))) {
                    BTN_WHICH = BTN_IS_PROCEED;
                } else if (mActivityWalletLinkBinding.tvSendOtp.getText().toString().equalsIgnoreCase(getString(R.string.label_add_amount))) {
                    BTN_WHICH = BTN_IS_ADD_AMOUNT;
                } else if (mActivityWalletLinkBinding.tvSendOtp.getText().toString().equalsIgnoreCase(getString(R.string.label_confirm))) {
                    BTN_WHICH = BTN_IS_CONFIRM;
                }
                if (isValidated()) {
                    if (BTN_WHICH == BTN_IS_SEND_OTP) {
                        /**
                         * Hide the Keyboard if it opened
                         */
                        Utility.hideKeyboard(this);
                        sendOTP();
                    } else if (BTN_WHICH == BTN_IS_PROCEED) {
                        /**
                         * Hide the Keyboard if it opened
                         */
                        Utility.hideKeyboard(this);
                        verifyOTP();
                    } else if (BTN_WHICH == BTN_IS_ADD_AMOUNT) {
                        /**
                         * Hide the Keyboard if it opened
                         */
                        Utility.hideKeyboard(this);
                        callgetChecksumForAddMoney();
//                        addMoney();
                    } else if (BTN_WHICH == BTN_IS_CONFIRM) {
                        /**
                         * Hide the Keyboard if it opened
                         */
                        Utility.hideKeyboard(this);
                        callgetChecksumForWithdrawMoney();

                    }
                }
                break;
        }
    }

    private void updateUI() {
        if (BTN_WHICH == BTN_IS_SEND_OTP) {
            String sendOTPString = getString(R.string.label_send_otp_again);
            mActivityWalletLinkBinding.tvEnterNoLinkXAccount.setText(getString(R.string.label_enter_otp_sent_on_x, mActivityWalletLinkBinding.etMobileNumber.getText()));
            mActivityWalletLinkBinding.tvSendOtp.setText(getString(R.string.label_proceed));
            mActivityWalletLinkBinding.tvSendOtp.setEnabled(false);

            textWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    mActivityWalletLinkBinding.tvSendOtp.setEnabled(charSequence.length() > 3);
                    mActivityWalletLinkBinding.tvSendOtp.setOnClickListener(charSequence.length() > 3 ? WalletLinkActivity.this : null);
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            };

            mActivityWalletLinkBinding.etMobileNumber.addTextChangedListener(textWatcher);
            mActivityWalletLinkBinding.ivMobile.setVisibility(View.GONE);
            mActivityWalletLinkBinding.tvDefaultCountryCode.setVisibility(View.GONE);
            mActivityWalletLinkBinding.etMobileNumber.setGravity(Gravity.CENTER);
            mActivityWalletLinkBinding.etMobileNumber.setText(Utility.EMPTY_STRING);
            mActivityWalletLinkBinding.etMobileNumber.setTextColor(ContextCompat.getColor(mContext, R.color.grey_varient_8));
            mActivityWalletLinkBinding.etMobileNumber.setHint(getString(R.string.label_enter_otp));
          /*  if (BuildConfig.BUILD_TYPE.equalsIgnoreCase(Utility.DEBUG)) {
                mActivityWalletLinkBinding.etMobileNumber.setText(BootstrapConstant.PAYTM_STAGING_MOBILE_NUMBER);
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
                    sendOTP();
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    ds.setUnderlineText(false);
                }
            }, clickIndex, sendOTPSpannableStringBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            sendOTPSpannableStringBuilder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.splash_gradient_end)),
                    clickIndex, sendOTPSpannableStringBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            mActivityWalletLinkBinding.tvWeCreateXWallet.setText(sendOTPSpannableStringBuilder);
            mActivityWalletLinkBinding.tvWeCreateXWallet.setGravity(Gravity.START);
            mActivityWalletLinkBinding.tvWeCreateXWallet.setMovementMethod(LinkMovementMethod.getInstance());

        } else if (BTN_WHICH == BTN_IS_PROCEED) {
            Log.d(TAG, "updateUI: BTN_IS_PROCEED");

        } else if (BTN_WHICH == BTN_IS_ADD_AMOUNT) {
            LinearLayout.LayoutParams tvEnterNoLinkXAccountLayoutParams =
                    new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tvEnterNoLinkXAccountLayoutParams.setMargins(0, 0, 0, 0);

            mActivityWalletLinkBinding.tvEnterNoLinkXAccount.setLayoutParams(tvEnterNoLinkXAccountLayoutParams);
            mActivityWalletLinkBinding.tvEnterNoLinkXAccount.setText(getString(R.string.label_amount_payable));

            mActivityWalletLinkBinding.tvAmount.setText(getString(R.string.rupee_symbol_x, amount));
            mActivityWalletLinkBinding.tvAmount.setVisibility(View.VISIBLE);
            mActivityWalletLinkBinding.tvLowBalance.setVisibility(View.VISIBLE);
            mActivityWalletLinkBinding.etMobileNumber.setText(Utility.EMPTY_STRING);
            mActivityWalletLinkBinding.etMobileNumber.setTextColor(ContextCompat.getColor(mContext, R.color.splash_gradient_end));
            mActivityWalletLinkBinding.etMobileNumber.setHint(getString(R.string.label_enter_amount));
            mActivityWalletLinkBinding.etMobileNumber.setText(String.valueOf(payableAmount));
            mActivityWalletLinkBinding.etMobileNumber.setEnabled(false);
            mActivityWalletLinkBinding.etMobileNumber.setGravity(Gravity.CENTER);
            mActivityWalletLinkBinding.tvSendOtp.setText(getString(R.string.label_add_amount));
            mActivityWalletLinkBinding.tvSendOtp.setOnClickListener(this);
            mActivityWalletLinkBinding.tvSendOtp.setEnabled(true);
            mActivityWalletLinkBinding.etMobileNumber.removeTextChangedListener(textWatcher);
            mActivityWalletLinkBinding.tvWeCreateXWallet.setText(getString(R.string.label_current_balance, String.valueOf(paytmWalletBalance)));
            mActivityWalletLinkBinding.tvWeCreateXWallet.setGravity(Gravity.CENTER);

        } else if (BTN_WHICH == BTN_IS_CONFIRM) {
            LinearLayout.LayoutParams tvEnterNoLinkXAccountLayoutParams =
                    new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tvEnterNoLinkXAccountLayoutParams.setMargins((int) (Utility.convertDpToPixel(34f, mContext))
                    , (int) (Utility.convertDpToPixel(6f, mContext))
                    , (int) (Utility.convertDpToPixel(34f, mContext))
                    , (int) (Utility.convertDpToPixel(34f, mContext)));
            mActivityWalletLinkBinding.tvEnterNoLinkXAccount.setLayoutParams(tvEnterNoLinkXAccountLayoutParams);
            mActivityWalletLinkBinding.tvEnterNoLinkXAccount.setTextSize(14);
            if (isPaytm)
                mActivityWalletLinkBinding.tvEnterNoLinkXAccount.setText(getString(R.string.label_notify_paying_by_wallet, getString(R.string.label_paytm)));
            else
                mActivityWalletLinkBinding.tvEnterNoLinkXAccount.setText(getString(R.string.label_notify_paying_by_wallet, getString(R.string.label_mobikwik)));

            LinearLayout.LayoutParams tvAmountLayoutParams =
                    new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tvAmountLayoutParams.setMargins(0, 0, 0, (int) (Utility.convertDpToPixel(87f, mContext)));
            mActivityWalletLinkBinding.tvAmount.setLayoutParams(tvAmountLayoutParams);
            mActivityWalletLinkBinding.tvAmount.setTextSize(32);
            mActivityWalletLinkBinding.tvAmount.setText(getString(R.string.rupee_symbol_x, amount));

            mActivityWalletLinkBinding.tvAmount.setVisibility(View.VISIBLE);
            mActivityWalletLinkBinding.llEtContainer.setVisibility(View.GONE);
            mActivityWalletLinkBinding.tvWeCreateXWallet.setVisibility(View.GONE);
            mActivityWalletLinkBinding.etMobileNumber.removeTextChangedListener(textWatcher);
            mActivityWalletLinkBinding.tvSendOtp.setText(getString(R.string.label_confirm));
            mActivityWalletLinkBinding.tvSendOtp.setOnClickListener(this);
            mActivityWalletLinkBinding.tvSendOtp.setEnabled(true);
        }
    }

    ///////////////////////////////////////////////////////////Paytm API call starts///////////////////////////////////////////////////////////

    //////////////////////////////////////////////Paytm API call Generalized response methods starts//////////////////////////////////////////////
    @Override
    public void showGeneralizedErrorMessage() {
        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityWalletLinkBinding.getRoot());
        hideProgressDialog();
    }

    @Override
    public void paytmInvalidMobileNumber() {
        Utility.showSnackBar(getString(R.string.validate_phone_number), mActivityWalletLinkBinding.getRoot());
        hideProgressDialog();
    }

    @Override
    public void paytmAccountBlocked() {
        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityWalletLinkBinding.getRoot());
        hideProgressDialog();
    }

    //This method is called when access token expires early due to some reason and we need to do whole OAuth process again
    @Override
    public void paytmInvalidAuthorization() {
        //TODO: implement that if accessToken is valid i.e. 1 month is not due directly call checkBalance API.
    }

    @Override
    public void volleyError() {
        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityWalletLinkBinding.getRoot());
        hideProgressDialog();
    }

    @Override
    public void showSpecificErrorMessage(String errorMessage) {
        Utility.showSnackBar(errorMessage, mActivityWalletLinkBinding.getRoot());
        hideProgressDialog();
    }
    //////////////////////////////////////////////Paytm API call Generalized response methods ends//////////////////////////////////////////////

    ///////////////////////////////////////////////////////////Paytm Send OTP API call starts///////////////////////////////////////////////////////////
    @Override
    public void paytmSendOtpSuccessResponse(String state) {
        mState = state;
        updateUI();
        hideProgressDialog();
    }

    private void sendOTP() {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityWalletLinkBinding.getRoot());
            return;
        }
        //Show Progress
        showProgressDialog();

        PaytmUtility.sendOTP(mContext, mMobileNumber, this);
    }
    ///////////////////////////////////////////////////////////Paytm Send OTP API call ends///////////////////////////////////////////////////////////


    ///////////////////////////////////////////////////////////Paytm Verify OTP API call starts///////////////////////////////////////////////////////////
    @Override
    public void paytmVerifyOtpSuccessResponse(String accessToken, long expires, String resourceOwnerCustomerId) {
        mAccessToken = accessToken;
        mExpires = expires;
        mResourceOwnerCustomerId = resourceOwnerCustomerId;
        /**
         * have not called getUserDetails API as whatever this API returns, we already get it in response of this API
         * do not hideProgressDialog as we need to call 3 (would be 4 in case we call getUserDetails API) APIs back to back
         */
        savePaytmUserDetails();
        checkBalance();
    }

    @Override
    public void paytmVerifyOtpInvalidOtp() {
        Utility.showSnackBar(getString(R.string.label_invalid_otp), mActivityWalletLinkBinding.getRoot());
        hideProgressDialog();
    }

    private void verifyOTP() {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityWalletLinkBinding.getRoot());
            return;
        }

        //Show Progress
        showProgressDialog();

        PaytmUtility.verifyOtp(mContext, mEtText, mState, this);
    }
    ///////////////////////////////////////////////////////////Paytm Verify OTP API call ends///////////////////////////////////////////////////////////


    ///////////////////////////////////////////////////////////Paytm Check Balance API call starts///////////////////////////////////////////////////////////
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
            BTN_WHICH = BTN_IS_ADD_AMOUNT;
            payableAmount = Math.ceil(Double.parseDouble(amount) - paytmWalletBalance);
        } else {
            BTN_WHICH = BTN_IS_CONFIRM;
        }
        updateUI();
        hideProgressDialog();
    }

    private void checkBalance() {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityWalletLinkBinding.getRoot());
            return;
        }

        //Show Progress
        showProgressDialog();

        PaytmUtility.checkBalance(mContext, mAccessToken, this);
    }
    ///////////////////////////////////////////////////////////Paytm Check Balance API call ends///////////////////////////////////////////////////////////


    ///////////////////////////////////////////////////////////Paytm Add Money API call starts///////////////////////////////////////////////////////////

    @Override
    public void paytmAddMoneySuccessResponse(String htmlResponse) {

        mActivityWalletLinkBinding.webView.setWebChromeClient(new WebChromeClient() {

        });
        mActivityWalletLinkBinding.webView.setWebViewClient(new WebViewClient() {
            /*@Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }*/

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Log.d(TAG, "shouldOverrideUrlLoading() called with: view = [" + view + "], request = [" + request + "]");
                view.loadUrl(view.getUrl());
                return true;
            }
        });
        mActivityWalletLinkBinding.webView.getSettings().setJavaScriptEnabled(true);
        mActivityWalletLinkBinding.webView.loadDataWithBaseURL(NetworkUtility.PAYTM.WALLET_APIS.ADD_MONEY, htmlResponse, "text/html", "UTF-8", null);
//        mActivityWalletLinkBinding.webView.loadData(htmlResponse, "text/html", "UTF-8");
        mActivityWalletLinkBinding.svMainLayout.setVisibility(View.GONE);
        mActivityWalletLinkBinding.webView.setVisibility(View.VISIBLE);

        Document document = Jsoup.parse(htmlResponse);
        Log.d(TAG, "paytmAddMoneySuccessResponse: document :: " + document);
        Elements inputElements = document.getElementsByTag("INPUT");
        for (int i = 0; i < inputElements.size(); i++) {
            Element element = inputElements.get(i);

            Attributes attributes = element.attributes();

            if (attributes.hasKeyIgnoreCase("NAME")) {
                String value = attributes.getIgnoreCase("VALUE");
                switch (attributes.getIgnoreCase("NAME")) {
                    case NetworkUtility.PAYTM.PARAMETERS.RESPCODE:
                        Log.d("RESPCODE", value);
                        if (value.equals("802")) {
                            Utility.showSnackBar(document.getElementsByAttributeValue("NAME", "RESPMSG").get(0).attr("VALUE"), mActivityWalletLinkBinding.getRoot());
//                            mActivityWalletLinkBinding.webView.loadData(document.getElementsByAttributeValue("NAME", "STATUS").get(0).attr("VALUE"),"","");
                            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                            builder.setTitle("Transaction status");
                            builder.setMessage(document.getElementsByAttributeValue("NAME", "STATUS").get(0).attr("VALUE"));
                            builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    finish();
                                }
                            });
                            builder.create().show();
                        }
                        break;
                    case NetworkUtility.PAYTM.PARAMETERS.RESPMSG:
                        Log.d("RESPMSG", value);
                        break;
                    case NetworkUtility.PAYTM.PARAMETERS.STATUS:
                        Log.d("STATUS", value);
                        break;
                    case NetworkUtility.PAYTM.PARAMETERS.MID:
                        Log.d("MID", value);
                        break;
                    case NetworkUtility.PAYTM.PARAMETERS.TXNAMOUNT:
                        Log.d("TXNAMOUNT", value);
                        break;
                    case NetworkUtility.PAYTM.PARAMETERS.ORDERID:
                        Log.d("ORDERID", value);
                        break;
                }
            }
        }
        hideProgressDialog();
    }

    private void addMoney() {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityWalletLinkBinding.getRoot());
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
            mActivityWalletLinkBinding.webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        mActivityWalletLinkBinding.webView.setWebViewClient(mWebViewClient);
        mActivityWalletLinkBinding.webView.getSettings().setJavaScriptEnabled(true);
        mActivityWalletLinkBinding.webView.postUrl(NetworkUtility.PAYTM.WALLET_APIS.ADD_MONEY, postData.getBytes());

        // Show the webview
        mActivityWalletLinkBinding.svMainLayout.setVisibility(View.GONE);
        mActivityWalletLinkBinding.webView.setVisibility(View.VISIBLE);
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
    ///////////////////////////////////////////////////////////Paytm Add Money API call ends///////////////////////////////////////////////////////////


    ///////////////////////////////////////////////////////////Paytm Withdraw Money API call starts///////////////////////////////////////////////////////////
    private void withdrawMoney() {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityWalletLinkBinding.getRoot());
            return;
        }

        //Show Progress
        showProgressDialog();

        PaytmUtility.withdrawMoney(mContext, generatedOrderId, mAccessToken, amount, mChecksumHash, mResourceOwnerCustomerId, mMobileNumber, mWithdrawMoneyResponseListener);
    }
    ///////////////////////////////////////////////////////////Paytm Withdraw Money API call ends///////////////////////////////////////////////////////////


    ///////////////////////////////////////////////////////////Volley Get Checksum Hash Web call starts///////////////////////////////////////////////////////////

    private void callgetChecksumForAddMoney() {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityWalletLinkBinding.getRoot());
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

    private void callgetChecksumForWithdrawMoney() {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityWalletLinkBinding.getRoot());
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
    ///////////////////////////////////////////////////////////Volley Get Checksum Hash Web call ends///////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////Volley save paytm user Web call starts///////////////////////////////////////////////////////////

    @Override
    public void volleySavePaytmUserSuccessResponse() {
        Log.d(TAG, "volleySavePaytmUserSuccessResponse: user successfully saved");
    }

    private void savePaytmUserDetails() {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityWalletLinkBinding.getRoot());
            return;
        }

        showProgressDialog();

        PaytmUtility.savePaytmUserDetails(mContext, mResourceOwnerCustomerId, mAccessToken, mMobileNumber, this);
    }
    ///////////////////////////////////////////////////////////Volley save paytm user Web call ends///////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////Paytm API call ends///////////////////////////////////////////////////////////

    private boolean isValidated() {
        LogUtils.LOGD(TAG, "isValidated() which button clicked " + BTN_WHICH);
        if (BTN_WHICH == BTN_IS_SEND_OTP) {
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
        } else if (BTN_WHICH == BTN_IS_PROCEED) {
            if (TextUtils.isEmpty(mActivityWalletLinkBinding.etMobileNumber.getText())) {
                Utility.showSnackBar(getString(R.string.validate_otp_empty), mActivityWalletLinkBinding.getRoot());
                return false;
            }
            return true;
        } else if (BTN_WHICH == BTN_IS_ADD_AMOUNT) {
            if (TextUtils.isEmpty(mActivityWalletLinkBinding.etMobileNumber.getText())) {
                Utility.showSnackBar(getString(R.string.validate_empty_amount), mActivityWalletLinkBinding.getRoot());
                return false;
            }
            return true;
        } else if (BTN_WHICH == BTN_IS_CONFIRM) {
            return true;
        }
        return false;
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
        /**
         * cancel all callbacks
         */
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.PAYTM.OAUTH_APIS.SEND_OTP);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.PAYTM.OAUTH_APIS.GET_ACCESS_TOKEN_SENDING_OTP);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.PAYTM.CHECK_BALANCE_API);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.GET_CHECKSUM_HASH);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.PAYTM.WALLET_APIS.ADD_MONEY);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.SAVE_PAYTM_USER_DETAILS);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.PAYTM.WALLET_APIS.WITHDRAW_MONEY);
        super.onDestroy();
    }

    /**
     * Customized webview client for Payment Webview
     */
    private WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.d(TAG, "onPageStarted() called with: view = [" + view + "], url = [" + url + "], favicon = [" + favicon + "]");
            super.onPageStarted(view, url, favicon);
            mActivityWalletLinkBinding.progress.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            Log.d(TAG, "onPageFinished() called with: view = [" + view + "], url = [" + url + "]");
            mActivityWalletLinkBinding.progress.setVisibility(View.GONE);
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


}
