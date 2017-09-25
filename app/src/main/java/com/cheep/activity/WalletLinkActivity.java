package com.cheep.activity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
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
import android.view.ViewGroup;
import android.widget.LinearLayout;

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
import com.cheep.utils.PaytmUtility;
import com.cheep.utils.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class WalletLinkActivity extends BaseAppCompatActivity implements View.OnClickListener,
        PaytmUtility.SendOtpResponseListener,
        PaytmUtility.VerifyOtpResponseListener,
        PaytmUtility.CheckBalanceResponseListener,
        PaytmUtility.GetChecksumResponseListener{

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

    //returned in response of verify otp api
    private String mAccessToken;
    private long mExpires;
    private String mResourceOwnerCustomerId;
    private String amount;

    //returned in response of check balance api
    private String requestGuid;
    private String orderId;
    private double totalBalance;
    private double paytmWalletBalance;
    private String ownerGuid;
    private String walletGrade;
    private String ssoId;

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
                if (mActivityWalletLinkBinding.tvSendOtp.getText().toString().equalsIgnoreCase(getString(R.string.label_send_otp))) {
                    BTN_WHICH = BTN_IS_SEND_OTP;
                } else if (mActivityWalletLinkBinding.tvSendOtp.getText().toString().equalsIgnoreCase(getString(R.string.label_proceed))) {
                    BTN_WHICH = BTN_IS_PROCEED;
                } else if (mActivityWalletLinkBinding.tvSendOtp.getText().toString().equalsIgnoreCase(getString(R.string.label_add_amount))) {
                    BTN_WHICH = BTN_IS_ADD_AMOUNT;
                } else if (mActivityWalletLinkBinding.tvSendOtp.getText().toString().equalsIgnoreCase(getString(R.string.label_confirm))) {
                    BTN_WHICH = BTN_IS_CONFIRM;
                }
                mEtText = mActivityWalletLinkBinding.etMobileNumber.getText().toString();
                if (isValidated()) {
                    if (BTN_WHICH == BTN_IS_SEND_OTP) {
                        sendOTP();
                    } else if (BTN_WHICH == BTN_IS_PROCEED) {
                        verifyOTP();
                    } else if (BTN_WHICH == BTN_IS_ADD_AMOUNT) {
                        addMoney();
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
        Utility.showSnackBar(getString(R.string.validate_phone_number_length), mActivityWalletLinkBinding.getRoot());
        hideProgressDialog();
    }

    @Override
    public void paytmAccountBlocked() {
        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityWalletLinkBinding.getRoot());
        hideProgressDialog();
    }

    @Override
    public void volleyError() {
        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityWalletLinkBinding.getRoot());
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

        PaytmUtility.sendOTP(mContext, mEtText, this);
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
         * do not hideProgressDialog as we need to call 2 (would be 3 in case we call getUserDetails API) APIs back to back
         */
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
            orderId = jsonObject.getString(NetworkUtility.PAYTM.PARAMETERS.orderId);

            JSONObject responseParamJson = jsonObject.getJSONObject(NetworkUtility.PAYTM.PARAMETERS.response);
            totalBalance = responseParamJson.getDouble(NetworkUtility.PAYTM.PARAMETERS.totalBalance);
            paytmWalletBalance = responseParamJson.getDouble(NetworkUtility.PAYTM.PARAMETERS.paytmWalletBalance);
            ownerGuid = responseParamJson.getString(NetworkUtility.PAYTM.PARAMETERS.ownerGuid);
            walletGrade = responseParamJson.getString(NetworkUtility.PAYTM.PARAMETERS.walletGrade);
            ssoId = responseParamJson.getString(NetworkUtility.PAYTM.PARAMETERS.ssoId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (paytmWalletBalance < Double.parseDouble(amount)) {
            BTN_WHICH = BTN_IS_ADD_AMOUNT;
        } else {
            BTN_WHICH = BTN_IS_CONFIRM;
        }
        callgetChecksum();
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
    private void addMoney() {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityWalletLinkBinding.getRoot());
            return;
        }

        //Show Progress
        showProgressDialog();

        Map<String, String> bodyParams = new HashMap<>();
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.MID, BuildConfig.SANDBOX_MERCHANT_ID);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.REQUEST_TYPE, Utility.ADD_MONEY);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.ORDER_ID, orderId);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.CUST_ID, mResourceOwnerCustomerId);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.TXN_AMOUNT, mEtText);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.CHANNEL_ID, BuildConfig.CHANNEL_ID);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.INDUSTRY_TYPE_ID, BuildConfig.INDUSTRY_TYPE_ID);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.WEBSITE, BuildConfig.WEBSITE);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.SSO_TOKEN, mAccessToken);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.CHECKSUMHASH, mChecksumHash);

        final String requestString = new JSONObject(bodyParams).toString();

        PaytmNetworkRequest paytmNetworkRequest = new PaytmNetworkRequest(
                Request.Method.POST,
                NetworkUtility.PAYTM.WALLET_APIS.ADD_MONEY,
                mAddMoneyResponseListener,
                mAddMoneyErrorListener,
                null,
                requestString);
        Volley.getInstance(mContext).addToRequestQueue(paytmNetworkRequest, NetworkUtility.PAYTM.WALLET_APIS.ADD_MONEY);
    }

    Response.ErrorListener mAddMoneyErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            LogUtils.LOGD(TAG, "onErrorResponse() called with: error = [" + error + "]");

            //hide ProgressDialog
            hideProgressDialog();

            // Show Toast
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityWalletLinkBinding.getRoot());
        }
    };

    Response.Listener<String> mAddMoneyResponseListener = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            LogUtils.LOGD(TAG, "onResponse() of add money called with: response = [" + response + "]");
            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(response);
//                String responseCode = jsonObject.getString(NetworkUtility.PAYTM.PARAMETERS.responseCode);
//                switch (responseCode) {
//                    case NetworkUtility.PAYTM.RESPONSE_CODES.LOGIN:
//                    case NetworkUtility.PAYTM.RESPONSE_CODES.REGISTER:
//                        mState = jsonObject.getString(NetworkUtility.PAYTM.PARAMETERS.state);
//                        updateUI();
//                        break;
//                    case NetworkUtility.PAYTM.RESPONSE_CODES.INVALID_AUTHORIZATION:
//                    case NetworkUtility.PAYTM.RESPONSE_CODES.BAD_REQUEST:
//                    case NetworkUtility.PAYTM.RESPONSE_CODES.LOGIN_FAILED:
//                        // Show Toast
//                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityWalletLinkBinding.getRoot());
//                        break;
//                    //invalid email not handled as email is not mandatory and we are not sending email
//                    case NetworkUtility.PAYTM.RESPONSE_CODES.INVALID_EMAIL:
//                        break;
//                    case NetworkUtility.PAYTM.RESPONSE_CODES.INVALID_MOBILE:
//                        // Show Toast
//                        Utility.showSnackBar(getString(R.string.validate_phone_number_length), mActivityWalletLinkBinding.getRoot());
//                        break;
//                    case NetworkUtility.PAYTM.RESPONSE_CODES.ACCOUNT_BLOCKED:
//                        //TODO: snackbar message to be changed in case required. now displaying generalized message of something went wrong
//                        // Show Toast
//                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityWalletLinkBinding.getRoot());
//                        break;
//                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            hideProgressDialog();
        }
    };
    ///////////////////////////////////////////////////////////Paytm Add Money API call ends///////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////Volley Get Checksum Hash Web call starts///////////////////////////////////////////////////////////
    @Override
    public void volleyGetChecksumSuccessResponse(String checksumHash) {
        mChecksumHash = checksumHash;
        hideProgressDialog();
    }

    @Override
    public void showSpecificErrorMessage(String errorMessage) {
        Utility.showSnackBar(errorMessage, mActivityWalletLinkBinding.getRoot());
        hideProgressDialog();
    }

    private void callgetChecksum() {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityWalletLinkBinding.getRoot());
            return;
        }

        showProgressDialog();

       PaytmUtility.getChecksum(mContext, String.valueOf(0), mResourceOwnerCustomerId, this);
    }
    ///////////////////////////////////////////////////////////Volley Get Checksum Hash Web call ends///////////////////////////////////////////////////////////

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
        super.onDestroy();
    }
}
