package com.cheep.activity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.cheep.BuildConfig;
import com.cheep.R;
import com.cheep.databinding.ActivitySendOtpBinding;
import com.cheep.model.MessageEvent;
import com.cheep.model.UserDetails;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.utils.LogUtils;
import com.cheep.utils.PaytmUtility;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;
import com.mixpanel.android.java_websocket.util.Base64;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static com.cheep.network.NetworkUtility.PAYTM.PARAMETERS.orderId;
import static com.cheep.network.NetworkUtility.PAYTM.PARAMETERS.response;


public class SendOtpActivity extends BaseAppCompatActivity implements View.OnClickListener,
        PaytmUtility.SendOtpResponseListener,
        PaytmUtility.VerifyOtpResponseListener,
        PaytmUtility.CheckBalanceResponseListener,
        PaytmUtility.SavePaytmUserResponseListener {

    private static final String TAG = LogUtils.makeLogTag(SendOtpActivity.class);
    private ActivitySendOtpBinding mActivitySendOtpBinding;
    private boolean isPaytm;
    private String mEtText;
    private TextWatcher mobileNumberTextWatcher;
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

        }

        @Override
        public void showGeneralizedErrorMessage() {

        }

        @Override
        public void volleyError() {

        }
    };


    public static void newInstance(Context context, boolean isPaytm, String amount) {
        Intent intent = new Intent(context, SendOtpActivity.class);
        intent.putExtra(Utility.Extra.DATA, isPaytm);
        intent.putExtra(Utility.Extra.AMOUNT, amount);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivitySendOtpBinding = DataBindingUtil.setContentView(this, R.layout.activity_send_otp);
        initiateUI();
        setListeners();
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
        Intent intent = getIntent();
        if (intent.hasExtra(Utility.Extra.DATA)) {
            isPaytm = intent.getExtras().getBoolean(Utility.Extra.DATA);
        }
        if (intent.hasExtra(Utility.Extra.AMOUNT)) {
            amount = intent.getExtras().getString(Utility.Extra.AMOUNT);
        }
        mobileNumberTextWatcher = new TextWatcher() {
            public EditText ET = mActivitySendOtpBinding.etMobileNumber;
            //we need to know if the user is erasing or inputing some new character
            private boolean backspacingFlag = false;
            //we need to block the :afterTextChanges method to be called again after we just replaced the EditText text
            private boolean editedFlag = false;
            //we need to mark the cursor position and restore it after the edition
            private int cursorComplement;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //we store the cursor local relative to the end of the string in the EditText before the edition
                cursorComplement = s.length() - ET.getSelectionStart();
                //we check if the user ir inputing or erasing a character
                backspacingFlag = count > after;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // nothing to do here =D
            }

            @Override
            public void afterTextChanged(Editable s) {
                String string = s.toString();
                String phone = string.replaceAll("[^\\d]", "");
                //what matters are the phone digits beneath the mask, so we always work with a raw string with only digits

                //if the text was just edited, :afterTextChanged is called another time... so we need to verify the flag of edition
                //if the flag is false, this is a original user-typed entry. so we go on and do some magic
                if (!editedFlag) {

                    //we start verifying the worst case, many characters mask need to be added
                    //example: 999999999 <- 6+ digits already typed
                    // masked: (999) 999-999
                    if (phone.length() >= 5 && !backspacingFlag) {
                        //we will edit. next call on this textWatcher will be ignored
                        phone.replace(" ", "");
                        editedFlag = true;
                        //here is the core. we substring the raw digits and add the mask as convenient
                        String ans = phone.substring(0, 5) + " " + phone.substring(5);
                        ET.setText(ans);
                        //we deliver the cursor to its original position relative to the end of the string
                        ET.setSelection(ET.getText().length() - cursorComplement);

                        //we end at the most simple case, when just one character mask is needed
                        //example: 99999 <- 3+ digits already typed
                        // masked: (999) 99
                    }
                    // We just edited the field, ignoring this cicle of the watcher and getting ready for the next
                } else {
                    editedFlag = false;
                }
            }
        };
        mActivitySendOtpBinding.etMobileNumber.addTextChangedListener(mobileNumberTextWatcher);

        if (isPaytm) {
            mActivitySendOtpBinding.tvEnterNoLinkXAccount.setText(getString(R.string.label_enter_number_link_account, getString(R.string.label_paytm)));
            mActivitySendOtpBinding.tvWeCreateXWallet.setText(getString(R.string.label_we_create_wallet, getString(R.string.label_paytm)));
        } else {
            mActivitySendOtpBinding.tvEnterNoLinkXAccount.setText(getString(R.string.label_enter_number_link_account, getString(R.string.label_mobikwik)));
            mActivitySendOtpBinding.tvWeCreateXWallet.setText(getString(R.string.label_we_create_wallet, getString(R.string.label_mobikwik)));
        }
    }

    @Override
    protected void setListeners() {
        mActivitySendOtpBinding.tvSendOtp.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_send_otp:
                mEtText = mActivitySendOtpBinding.etMobileNumber.getText().toString();
                if (mActivitySendOtpBinding.tvSendOtp.getText().toString().equalsIgnoreCase(getString(R.string.label_send_otp))) {
                    BTN_WHICH = BTN_IS_SEND_OTP;
                    mMobileNumber = mEtText.replace(" ", "");
                } else if (mActivitySendOtpBinding.tvSendOtp.getText().toString().equalsIgnoreCase(getString(R.string.label_proceed))) {
                    BTN_WHICH = BTN_IS_PROCEED;
                } else if (mActivitySendOtpBinding.tvSendOtp.getText().toString().equalsIgnoreCase(getString(R.string.label_add_amount))) {
                    BTN_WHICH = BTN_IS_ADD_AMOUNT;
                } else if (mActivitySendOtpBinding.tvSendOtp.getText().toString().equalsIgnoreCase(getString(R.string.label_confirm))) {
                    BTN_WHICH = BTN_IS_CONFIRM;
                }
                if (isValidated()) {
                    switch (BTN_WHICH) {
                        case BTN_IS_SEND_OTP:
                        /*
                         * Hide the Keyboard if it opened
                         */
                            Utility.hideKeyboard(this);
                            sendOTP(false);
                            break;
                        case BTN_IS_PROCEED:
                        /*
                         * Hide the Keyboard if it opened
                         */
                            Utility.hideKeyboard(this);
                            verifyOTP();
                            break;
                        case BTN_IS_ADD_AMOUNT:
                        /*
                         * Hide the Keyboard if it opened
                         */
                            Utility.hideKeyboard(this);
                            callgetChecksumForAddMoney();
//                        addMoney();
                            break;
                        case BTN_IS_CONFIRM:
                        /*
                         * Hide the Keyboard if it opened
                         */
                            Utility.hideKeyboard(this);
                            callgetChecksumForWithdrawMoney();

                            break;
                    }
                }
                break;
        }
    }


    private void updateUI() {
        switch (BTN_WHICH) {
            case BTN_IS_SEND_OTP:
                /*String sendOTPString = getString(R.string.label_send_otp_again);
                mActivitySendOtpBinding.tvEnterNoLinkXAccount.setText(getString(R.string.label_enter_otp_sent_on_x, mActivitySendOtpBinding.etMobileNumber.getText()));
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
                        mActivitySendOtpBinding.tvSendOtp.setOnClickListener(charSequence.length() > 0 ? SendOtpActivity.this : null);
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
                mActivitySendOtpBinding.etMobileNumber.removeTextChangedListener(mobileNumberTextWatcher);
                mActivitySendOtpBinding.etMobileNumber.addTextChangedListener(textWatcher);
                mActivitySendOtpBinding.ivMobile.setVisibility(View.GONE);
                mActivitySendOtpBinding.tvDefaultCountryCode.setVisibility(View.GONE);
                mActivitySendOtpBinding.etMobileNumber.setGravity(Gravity.CENTER);
                mActivitySendOtpBinding.etMobileNumber.setText(Utility.EMPTY_STRING);
                mActivitySendOtpBinding.etMobileNumber.setTextColor(ContextCompat.getColor(mContext, R.color.grey_varient_8));
                mActivitySendOtpBinding.etMobileNumber.setHint(getString(R.string.label_enter_otp));
          *//*  if (BuildConfig.BUILD_TYPE.equalsIgnoreCase(Utility.DEBUG)) {
                mActivitySendOtpBinding.etMobileNumber.setText(BootstrapConstant.PAYTM_STAGING_MOBILE_NUMBER);
            }*//*
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
                mActivitySendOtpBinding.tvWeCreateXWallet.setMovementMethod(LinkMovementMethod.getInstance());*/

                break;
            case BTN_IS_PROCEED:
                Log.d(TAG, "updateUI: BTN_IS_PROCEED");

                break;
            case BTN_IS_ADD_AMOUNT: {
                LinearLayout.LayoutParams tvEnterNoLinkXAccountLayoutParams =
                        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                tvEnterNoLinkXAccountLayoutParams.setMargins(0, 0, 0, 0);

                mActivitySendOtpBinding.tvEnterNoLinkXAccount.setLayoutParams(tvEnterNoLinkXAccountLayoutParams);
                mActivitySendOtpBinding.tvEnterNoLinkXAccount.setText(getString(R.string.label_amount_payable));
                mActivitySendOtpBinding.tvAmount.setText(getString(R.string.rupee_symbol_x, amount));
                mActivitySendOtpBinding.tvAmount.setVisibility(View.VISIBLE);
                mActivitySendOtpBinding.tvLowBalance.setVisibility(View.VISIBLE);
                mActivitySendOtpBinding.etMobileNumber.setText(Utility.EMPTY_STRING);
                mActivitySendOtpBinding.etMobileNumber.setTextColor(ContextCompat.getColor(mContext, R.color.splash_gradient_end));
                mActivitySendOtpBinding.etMobileNumber.setHint(getString(R.string.label_enter_amount));
                mActivitySendOtpBinding.etMobileNumber.setText(String.valueOf(payableAmount));
                mActivitySendOtpBinding.etMobileNumber.setEnabled(true);
                mActivitySendOtpBinding.etMobileNumber.setGravity(Gravity.CENTER);
                mActivitySendOtpBinding.tvSendOtp.setText(getString(R.string.label_add_amount));
                mActivitySendOtpBinding.tvSendOtp.setOnClickListener(this);
                mActivitySendOtpBinding.tvSendOtp.setEnabled(true);
//                mActivitySendOtpBinding.etMobileNumber.removeTextChangedListener(textWatcher);
                mActivitySendOtpBinding.tvWeCreateXWallet.setText(getString(R.string.label_current_balance, Utility.getQuotePriceFormatter(String.valueOf(paytmWalletBalance))));
                mActivitySendOtpBinding.tvWeCreateXWallet.setGravity(Gravity.CENTER);

                break;
            }
            case BTN_IS_CONFIRM: {
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
                mActivitySendOtpBinding.tvSendOtp.setOnClickListener(this);
                mActivitySendOtpBinding.tvSendOtp.setEnabled(true);
                break;
            }
        }
    }

    ///////////////////////////////////////////////////////////Paytm API call starts///////////////////////////////////////////////////////////

    //////////////////////////////////////////////Paytm API call Generalized response methods starts//////////////////////////////////////////////
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

    //This method is called when access token expires early due to some reason and we need to do whole OAuth process again
    @Override
    public void paytmInvalidAuthorization() {
        //TODO: implement that if accessToken is valid i.e. 1 month is not due directly call checkBalance API.
    }

    @Override
    public void volleyError() {
        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivitySendOtpBinding.getRoot());
        hideProgressDialog();
    }

    @Override
    public void showSpecificErrorMessage(String errorMessage) {
        Utility.showSnackBar(errorMessage, mActivitySendOtpBinding.getRoot());
        hideProgressDialog();
    }
    //////////////////////////////////////////////Paytm API call Generalized response methods ends//////////////////////////////////////////////

    ///////////////////////////////////////////////////////////Paytm Send OTP API call starts///////////////////////////////////////////////////////////
    @Override
    public void paytmSendOtpSuccessResponse(String state, boolean isRegenerated) {
        mState = state;
        /*if (!isRegenerated)
            updateUI();
        else
            timer.start();*/
        VerifyOtpActivity.newInstance(mContext, mMobileNumber, mState, isPaytm, amount);
        hideProgressDialog();
    }

    private void sendOTP(boolean isRegenerated) {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivitySendOtpBinding.getRoot());
            return;
        }
        //Show Progress
        showProgressDialog();

        PaytmUtility.sendOTP(mContext, mMobileNumber, this, isRegenerated);
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
        /*timer.cancel();*/
        savePaytmUserDetails();
        checkBalance();
    }

    @Override
    public void paytmVerifyOtpInvalidOtp() {
        Utility.showSnackBar(getString(R.string.label_invalid_otp), mActivitySendOtpBinding.getRoot());
        hideProgressDialog();
    }

    private void verifyOTP() {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivitySendOtpBinding.getRoot());
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
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivitySendOtpBinding.getRoot());
            return;
        }

        //Show Progress
        showProgressDialog();

        PaytmUtility.checkBalance(mContext, mAccessToken, this);
    }
    ///////////////////////////////////////////////////////////Paytm Check Balance API call ends///////////////////////////////////////////////////////////


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
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.THEME, Utility.MERCHANT);

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
    ///////////////////////////////////////////////////////////Paytm Withdraw Money API call ends///////////////////////////////////////////////////////////


    ///////////////////////////////////////////////////////////Volley Get Checksum Hash Web call starts///////////////////////////////////////////////////////////

    private void callgetChecksumForAddMoney() {


        double edtAmount = Double.parseDouble(mEtText);
        if (edtAmount < payableAmount) {
            Utility.showToast(mContext, getString(R.string.enter_min_amount, payableAmount));
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
    ///////////////////////////////////////////////////////////Volley Get Checksum Hash Web call ends///////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////Volley save paytm user Web call starts///////////////////////////////////////////////////////////
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
                    UserDetails userDetails = PreferenceUtility.getInstance(SendOtpActivity.this).getUserDetails();
                    userDetails.mPaytmUserDetail = (UserDetails.PaytmUserDetail) Utility.getObjectFromJsonString(paytmData, UserDetails.PaytmUserDetail.class);
                    PreferenceUtility.getInstance(SendOtpActivity.this).saveUserDetails(userDetails);
                    break;
                case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                    Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivitySendOtpBinding.getRoot());
                    break;
                case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                    Utility.showSnackBar(jsonObject.getString(NetworkUtility.TAGS.MESSAGE), mActivitySendOtpBinding.getRoot());
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void savePaytmUserDetails() {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivitySendOtpBinding.getRoot());
            return;
        }

        showProgressDialog();

        PaytmUtility.savePaytmUserDetails(mContext, mResourceOwnerCustomerId, mAccessToken, mMobileNumber, this, NetworkUtility.PAYMENT_METHOD_TYPE.PAYTM, String.valueOf(mExpires));
    }
    ///////////////////////////////////////////////////////////Volley save paytm user Web call ends///////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////Paytm API call ends///////////////////////////////////////////////////////////

    private boolean isValidated() {
        LogUtils.LOGD(TAG, "isValidated() which button clicked " + BTN_WHICH);
        if (BTN_WHICH == BTN_IS_SEND_OTP) {
            if (TextUtils.isEmpty(mActivitySendOtpBinding.etMobileNumber.getText())) {
                Utility.showSnackBar(getString(R.string.validate_phone_number_wallet), mActivitySendOtpBinding.getRoot());
                return false;
            }

            //Length of phone number must bhi 10 in length
            if (!Utility.isValidPhoneNumber(mActivitySendOtpBinding.etMobileNumber.getText().toString().trim().replace(" ", ""))) {
                Utility.showSnackBar(getString(R.string.validate_phone_number_length), mActivitySendOtpBinding.getRoot());
                return false;
            }
            return true;
        } else if (BTN_WHICH == BTN_IS_PROCEED) {
            if (TextUtils.isEmpty(mActivitySendOtpBinding.etMobileNumber.getText())) {
                Utility.showSnackBar(getString(R.string.validate_otp_empty), mActivitySendOtpBinding.getRoot());
                return false;
            }
            return true;
        } else if (BTN_WHICH == BTN_IS_ADD_AMOUNT) {
            if (TextUtils.isEmpty(mActivitySendOtpBinding.etMobileNumber.getText())) {
                Utility.showSnackBar(getString(R.string.validate_empty_amount), mActivitySendOtpBinding.getRoot());
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
        try {
            EventBus.getDefault().unregister(this);

        } catch (Exception e) {

        }
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
     * Event Bus Callbacks
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        Log.d(TAG, "onMessageEvent() called with: event = [" + event.BROADCAST_ACTION + "]");
        if (event.BROADCAST_ACTION == Utility.BROADCAST_TYPE.PAYTM_RESPONSE) {
            // We need to finish this activity regardless of the response is success or failure
            finish();
        }
        if (event.BROADCAST_ACTION == Utility.BROADCAST_TYPE.PAYTM_LINKED) {
            // when paytm data is linked successfully
            finish();
        }
    }

}
