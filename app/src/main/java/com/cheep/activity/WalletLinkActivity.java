package com.cheep.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
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
import android.widget.LinearLayout;

import com.cheep.R;
import com.cheep.databinding.ActivityWalletLinkBinding;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.utils.LogUtils;
import com.cheep.utils.PaytmUtility;
import com.cheep.utils.Utility;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import static com.cheep.network.NetworkUtility.PAYTM.PARAMETERS.orderId;
import static com.cheep.network.NetworkUtility.PAYTM.PARAMETERS.response;


public class WalletLinkActivity extends BaseAppCompatActivity implements View.OnClickListener,
        PaytmUtility.SendOtpResponseListener,
        PaytmUtility.VerifyOtpResponseListener,
        PaytmUtility.CheckBalanceResponseListener,
        PaytmUtility.GetChecksumResponseListener,
        PaytmUtility.AddMoneyResponseListener,
        PaytmUtility.SavePaytmUserResponseListener,
        PaytmUtility.WithdrawMoneyResponseListener {

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
    private String paytmReturnedOrderId;
    private double totalBalance;
    private double paytmWalletBalance;
    private String ownerGuid;
    private String walletGrade;
    private String ssoId;
    private String generatedOrderId;
    private String mMobileNumber;

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
                        sendOTP();
                    } else if (BTN_WHICH == BTN_IS_PROCEED) {
                        verifyOTP();
                    } else if (BTN_WHICH == BTN_IS_ADD_AMOUNT) {
                        addMoney();
                    } else if (BTN_WHICH == BTN_IS_CONFIRM) {
                        withdrawMoney();
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

    @Override
    public void paytmAddMoneySuccessResponse(String htmlResponse) {
        mActivityWalletLinkBinding.webView.loadData(htmlResponse, "", "");
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

        PaytmUtility.addMoney(mContext, generatedOrderId, mAccessToken, mEtText, mChecksumHash, mResourceOwnerCustomerId, mMobileNumber, this);
    }
    ///////////////////////////////////////////////////////////Paytm Add Money API call ends///////////////////////////////////////////////////////////


    ///////////////////////////////////////////////////////////Paytm Withdraw Money API call starts///////////////////////////////////////////////////////////
    @Override
    public void paytmWithdrawMoneySuccessResponse(String htmlResponse) {
        mActivityWalletLinkBinding.webView.loadData(htmlResponse, "", "");
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

    private void withdrawMoney() {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityWalletLinkBinding.getRoot());
            return;
        }

        //Show Progress
        showProgressDialog();

        PaytmUtility.withdrawMoney(mContext, generatedOrderId, mAccessToken, mEtText, mChecksumHash, mResourceOwnerCustomerId, mMobileNumber, this);
    }
    ///////////////////////////////////////////////////////////Paytm Withdraw Money API call ends///////////////////////////////////////////////////////////


    ///////////////////////////////////////////////////////////Volley Get Checksum Hash Web call starts///////////////////////////////////////////////////////////
    @Override
    public void volleyGetChecksumSuccessResponse(String checksumHash) {
        mChecksumHash = checksumHash;
        hideProgressDialog();
    }

    private void callgetChecksum() {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityWalletLinkBinding.getRoot());
            return;
        }

        showProgressDialog();

        generatedOrderId = PaytmUtility.getChecksum(mContext, String.valueOf(0), mResourceOwnerCustomerId, this);
    }
    ///////////////////////////////////////////////////////////Volley Get Checksum Hash Web call ends///////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////Volley save paytm user Web call starts///////////////////////////////////////////////////////////

    @Override
    public void volleySavePaytmUserSuccessResponse() {
        Log.d(TAG, "volleySavePaytmUserSuccessResponse: user successfully saved");
        hideProgressDialog();
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
        } else if (BTN_WHICH == BTN_IS_CONFIRM){
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
}
