package com.cheep.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.BootstrapConstant;
import com.cheep.BuildConfig;
import com.cheep.R;
import com.cheep.databinding.ActivityPaymentChoiceBinding;
import com.cheep.dialogs.PayByCashDialog;
import com.cheep.model.MessageEvent;
import com.cheep.model.TaskDetailModel;
import com.cheep.model.UserDetails;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.HDFCPaymentUtility;
import com.cheep.utils.LogUtils;
import com.cheep.utils.PaytmUtility;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.cheep.network.NetworkUtility.PAYTM.PARAMETERS.orderId;
import static com.cheep.network.NetworkUtility.PAYTM.PARAMETERS.response;


public class PaymentChoiceActivity extends BaseAppCompatActivity implements View.OnClickListener {

    private static final String TAG = LogUtils.makeLogTag(PaymentChoiceActivity.class);
    private ActivityPaymentChoiceBinding mActivityPaymentChoiceBinding;
    private TaskDetailModel taskDetailModel;
    private String paymentMethod;
    private Map<String, String> mTransactionParams;
    private String amount;
    public static final int PAYTM_SEND_OTP = 0;
    public static final int PAYTM_ADD_MONEY = 1;
    public static final int PAYTM_WITHDRAW = 2;

    private int PAYTM_STEP = -1;


    public static void newInstance(Context context, TaskDetailModel taskDetailModel) {
        Intent intent = new Intent(context, PaymentChoiceActivity.class);
        intent.putExtra(Utility.Extra.DATA, Utility.getJsonStringFromObject(taskDetailModel));
        context.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityPaymentChoiceBinding = DataBindingUtil.setContentView(this, R.layout.activity_payment_choice);
        initiateUI();
        setListeners();

        // Register and Event Buss to get callback from various payment gateways
        EventBus.getDefault().register(this);
    }

    @Override
    protected void initiateUI() {
        if (getIntent().hasExtra(Utility.Extra.DATA)) {
            taskDetailModel = (TaskDetailModel) Utility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA), TaskDetailModel.class);
        }

        mActivityPaymentChoiceBinding.tvPaytmLinkAccount.setText(getString(R.string.label_link_x, getString(R.string.label_account)));
        setupActionbar();

    }

    private void setupActionbar() {
        if (taskDetailModel.taskType.equalsIgnoreCase(Utility.TASK_TYPE.STRATEGIC)) {
            if (taskDetailModel.cheepCode.isEmpty())
                amount = Utility.getQuotePriceFormatter(taskDetailModel.totalStrategicPartner);
            else
                amount = Utility.getQuotePriceFormatter(taskDetailModel.payableAmountStrategicPartner);
        } else {
            amount = Utility.getQuotePriceFormatter(taskDetailModel.taskPaidAmount);
        }
        mActivityPaymentChoiceBinding.textTitle.setText(getString(R.string.label_please_pay_x, amount));
        setSupportActionBar(mActivityPaymentChoiceBinding.toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(Utility.EMPTY_STRING);
        actionBar.setDisplayHomeAsUpEnabled(true);
        //no inspection RestrictedApi
//        actionBar.setDefaultDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void setListeners() {
        mActivityPaymentChoiceBinding.rlCard.setOnClickListener(this);
        mActivityPaymentChoiceBinding.rlNetbanking.setOnClickListener(this);
        mActivityPaymentChoiceBinding.rlPaytm.setOnClickListener(this);
        mActivityPaymentChoiceBinding.rlCashPayment.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_card:
            case R.id.rl_netbanking:
                paymentMethod = NetworkUtility.PAYMENT_METHOD_TYPE.PAYU;
                LogUtils.LOGD(TAG, "onClick: of HDFC PAYMENT");
                if (taskDetailModel.taskType.equalsIgnoreCase(Utility.TASK_TYPE.STRATEGIC)) {
                    // Go for HDFC/payu payment gateway strategic partner
                    generateHashForStrategicPartner();
                } else {
                    // Go for HDFC/payu payment gateway
                    generateHashForNormalTask();
                }
                break;
            case R.id.rl_paytm:
                paymentMethod = NetworkUtility.PAYMENT_METHOD_TYPE.PAYTM;
                UserDetails userDetails = PreferenceUtility.getInstance(PaymentChoiceActivity.this).getUserDetails();
                UserDetails.PaytmUserDetail paytmUserDetail = userDetails.mPaytmUserDetail;

                switch (PAYTM_STEP) {
                    case PAYTM_SEND_OTP:
                        SendOtpActivity.newInstance(mContext, true, amount);
                        break;
                    case PAYTM_ADD_MONEY:
                        AddMoneyActivity.newInstance(mContext, amount, payableAmount, paytmUserDetail.paytmAccessToken,
                                paytmUserDetail.paytmphoneNumber, paytmUserDetail.paytmCustId, paytmWalletBalance);
                        break;
                    case PAYTM_WITHDRAW:
                        WithdrawMoneyActivity.newInstance(mContext, amount, payableAmount, paytmUserDetail.paytmAccessToken,
                                paytmUserDetail.paytmphoneNumber, paytmUserDetail.paytmCustId, paytmWalletBalance, true);
                        break;
                }

                break;

            case R.id.rl_cash_payment:
                paymentMethod = NetworkUtility.PAYMENT_METHOD_TYPE.COD;

                PayByCashDialog payByCashDialog = PayByCashDialog.newInstance(taskDetailModel.selectedProvider.userName, Utility.getQuotePriceFormatter(taskDetailModel.taskPaidAmount), new PayByCashDialog.PayByCashDoneListener() {
                    @Override
                    public void onDoneClick() {
                        callPaymentForNormalTaskWS(Utility.EMPTY_STRING);
                    }
                });
                payByCashDialog.show(getSupportFragmentManager(), PayByCashDialog.TAG);

                break;
        }
    }


///////////////////////////////////////////////////////    NORMAL TASK PAYMENT METHOD [START] ///////////////////////////////////////////////////////

    /**
     * Used for payment
     */
    private void generateHashForNormalTask() {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityPaymentChoiceBinding.getRoot());
            return;
        }

        showProgressDialog();

        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();

        //Add Params
        // = new HashMap<String, Object>();

        mTransactionParams = HDFCPaymentUtility.getPaymentTransactionFieldsForNormalTask(
                PreferenceUtility.getInstance(this).getFCMRegID(), userDetails, taskDetailModel);

        new HDFCPaymentUtility.AsyncFetchEncryptedString(new HDFCPaymentUtility.EncryptTransactionParamsListener() {
            @Override
            public void onPostOfEncryption(String encryptedData) {
                UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
                //Add Header parameters
                Map<String, String> mHeaderParams = new HashMap<>();
                mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
                mHeaderParams.put(NetworkUtility.TAGS.USER_ID, userDetails.UserID);

                Map<String, Object> mFinalParams = new HashMap<>();
                mFinalParams.put(NetworkUtility.TAGS.DATA, encryptedData);

//                getPaymentUrl(userDetails, isForAdditionalQuote);
                String url;
                // if payment is done using insta feature then
                // post data will be generated like strategic partner feature
                // call startegic generate hash for payment
                url = taskDetailModel.taskType.equalsIgnoreCase(Utility.TASK_TYPE.NORMAL) ? NetworkUtility.WS.GET_PAYMENT_HASH : NetworkUtility.WS.GET_PAYMENT_HASH_FOR_STRATEGIC_PARTNER;
                //Url is based on condition if address id is greater then 0 then it means we need to update the existing address
                VolleyNetworkRequest mVolleyNetworkRequestForSPList = new VolleyNetworkRequest(url
                        , mCallGenerateHashWSErrorListener
                        , mCallGenerateHashWSResponseListener
                        , mHeaderParams
                        , mFinalParams
                        , null);
                Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForSPList);


            }
        }).execute(new JSONObject(mTransactionParams).toString());


    }

    Response.Listener mCallGenerateHashWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {

            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                LogUtils.LOGE(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;
                hideProgressDialog();
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:

                        /*
                         * Changes @Bhavesh : 7thJuly,2017
                         * In case we have to bypass the payment
                         */
                        if (BuildConfig.NEED_TO_BYPASS_PAYMENT) {
//                            PLEASE NOTE: THIS IS JUST TO BYPPASS THE PAYMENT GATEWAY. THIS IS NOT
//                            GOING TO RUN IN LIVE ENVIRONMENT BUILDS
                            // Direct bypass the things
//                            updatePaymentStatus(true, getString(R.string.message_payment_bypassed), false);
                            callPaymentForNormalTaskWS(getString(R.string.message_payment_bypassed));
                        } else {
                            //TODO: Remove this when release and it is saving cc detail in clipboard only
                            if ("debug".equalsIgnoreCase(BuildConfig.BUILD_TYPE)) {
                                //Copy dummy creditcard detail in clipboard
                                try {
                                    Utility.setClipboard(mContext, BootstrapConstant.CC_DETAILS);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            HDFCPaymentGatewayActivity.newInstance(
                                    PaymentChoiceActivity.this,
                                    HDFCPaymentUtility.getPaymentUrl(mTransactionParams, jsonObject.optString(NetworkUtility.TAGS.HASH_STRING)),
                                    Utility.REQUEST_START_PAYMENT);

                        }
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityPaymentChoiceBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mActivityPaymentChoiceBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.USER_DELETED:
                    case NetworkUtility.TAGS.STATUSCODETYPE.FORCE_LOGOUT_REQUIRED:
                        //Logout and finish the current activity
                        Utility.logout(mContext, true, statusCode);
                        finish();
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mCallGenerateHashWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
        }
    };

    Response.ErrorListener mCallGenerateHashWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(final VolleyError error) {
            hideProgressDialog();
            LogUtils.LOGD(TAG, "onErrorResponse() called with: error = [" + error + "]");
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityPaymentChoiceBinding.getRoot());

        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Utility.REQUEST_START_PAYMENT:
//            Toast.makeText(mContext, "OnActivityResult called with resultCode:" + resultCode + ", requestCode:" + requestCode, Toast.LENGTH_SHORT).show();
                if (resultCode == RESULT_OK) {
                    //success
                    if (data != null) {
                        LogUtils.LOGE(TAG, "onActivityResult() called with success: result= [" + data.getStringExtra(Utility.Extra.PAYU_RESPONSE) + "]");
                        callPaymentForNormalTaskWS(data.getStringExtra(Utility.Extra.PAYU_RESPONSE));
                    }
                }
                if (resultCode == RESULT_CANCELED) {
                    //failed
                    if (data != null) {
                        LogUtils.LOGE(TAG, "onActivityResult() called with failed: result= [" + data.getStringExtra(Utility.Extra.PAYU_RESPONSE) + "]");
                        Utility.showSnackBar(getString(R.string.msg_payment_failed), mActivityPaymentChoiceBinding.getRoot());
                    }
                }
                break;
            case Utility.REQUEST_START_PAYMENT_FOR_STRATEGIC_PARTNER:
                if (resultCode == Activity.RESULT_OK) {
                    // success
                    if (data != null) {
                        LogUtils.LOGE(TAG, "onActivityResult() called with success: result= [" + data.getStringExtra(Utility.Extra.PAYU_RESPONSE) + "]");
                        // Call update payment service from here with all the response come from service
                        LogUtils.LOGD(TAG, "onActivityResult: payment of strategic partner");
//                        callCreateStrategicPartnerTaskWS(data.getStringExtra(Utility.Extra.PAYU_RESPONSE));
                    }
                }
                if (resultCode == Activity.RESULT_CANCELED) {
                    if (data != null) {
                        LogUtils.LOGE(TAG, "onActivityResult() called with failed: result= [" + data.getStringExtra(Utility.Extra.PAYU_RESPONSE) + "]");
                        //Call update payment service from here with all the response come from service
//                    callTaskCreationWebServiceForStratgicPartner(false, data.getStringExtra("result"));
                        Utility.showSnackBar(getString(R.string.msg_payment_failed), mActivityPaymentChoiceBinding.getRoot());
                    }
                }
                break;
        }
    }

    private void callPaymentForNormalTaskWS(String paymentLog) {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityPaymentChoiceBinding.getRoot());
            return;
        }
        showProgressDialog();

        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
        //Add Header parameters

        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, userDetails.UserID);

        //Add Params
        Map<String, Object> mParams = new HashMap<>();
        if (mTransactionParams == null)
            mParams.put(NetworkUtility.TAGS.TRANSACTION_ID, Utility.getUniqueTransactionId());
        else
            mParams.put(NetworkUtility.TAGS.TRANSACTION_ID, mTransactionParams.get(HDFCPaymentUtility.TXN_ID));
        mParams.put(NetworkUtility.TAGS.PAYABLE_AMOUNT, taskDetailModel.taskPaidAmount);
        mParams.put(NetworkUtility.TAGS.PAYMENT_LOG, paymentLog);
        mParams.put(NetworkUtility.TAGS.PAYMENT_STATUS, Utility.PAYMENT_STATUS.COMPLETED);
        mParams.put(NetworkUtility.TAGS.PAYMENT_METHOD, paymentMethod);
        mParams.put(NetworkUtility.TAGS.TASK_ID, taskDetailModel.taskId);
        // Url is based on condition if address id is greater then 0 then it means we need to update the existing address
        VolleyNetworkRequest mVolleyNetworkRequestForSPList = new VolleyNetworkRequest(NetworkUtility.WS.PAY_TASK_PAYMENT
                , mCallCompleteTaskWSErrorListener
                , mCallCompleteTaskWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForSPList);
    }


    Response.Listener mCallCompleteTaskWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {

            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
                        Utility.showSnackBar(getString(R.string.msg_thanks_for_confirmation), mActivityPaymentChoiceBinding.getRoot());

                                /*
                                  Update the UI Accordingly.
                                 */

                        //Refresh UI for Paid status

                        // Notify the Home Screen to check for ongoing task counter.
                        MessageEvent messageEvent = new MessageEvent();
                        messageEvent.BROADCAST_ACTION = Utility.BROADCAST_TYPE.TASK_PAID_SUCCESSFULLY;
                        EventBus.getDefault().post(messageEvent);
                        finish();

                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityPaymentChoiceBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        String error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mActivityPaymentChoiceBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.USER_DELETED:
                    case NetworkUtility.TAGS.STATUSCODETYPE.FORCE_LOGOUT_REQUIRED:
                        //Logout and finish the current activity
                        Utility.logout(mContext, true, statusCode);
                        finish();
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mCallCompleteTaskWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
            hideProgressDialog();
        }
    };

    Response.ErrorListener mCallCompleteTaskWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            hideProgressDialog();

            // Show Toast
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityPaymentChoiceBinding.getRoot());
        }
    };
//////////////////////////////////////////////////////////////////    NORMAL TASK PAYMENT METHOD [END] ///////////////////////////////////////////////////////

    /**
     * Used for payment
     */
    private void generateHashForStrategicPartner() {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityPaymentChoiceBinding.getRoot());
            return;
        }

        if (PreferenceUtility.getInstance(mContext).getUserDetails() == null) {
            LoginActivity.newInstance(mContext);
            return;
        }

        showProgressDialog();

        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();

        //Add Params
        mTransactionParams = HDFCPaymentUtility.getPaymentTransactionFieldsForStrategicPartner(PreferenceUtility.getInstance(this).getFCMRegID(),
                userDetails,
                taskDetailModel.cheepCode,
                taskDetailModel.totalStrategicPartner,
                taskDetailModel.payableAmountStrategicPartner,
                taskDetailModel.taskStartdate);

        // We do not need to pass PROID and TaskID in Strategic partner as it still not finalized
        //mParams.put(NetworkUtility.TAGS.SP_USER_ID, providerModel.providerId);
        //mParams.put(NetworkUtility.TAGS.TASK_ID, taskDetailModel.taskId);

        if (!TextUtils.isEmpty(taskDetailModel.cheepCode)) {
            mTransactionParams.put(NetworkUtility.TAGS.CHEEPCODE, taskDetailModel.cheepCode);
            mTransactionParams.put(NetworkUtility.TAGS.PROMOCODE_PRICE, taskDetailModel.taskDiscountAmount);
        } else {
            mTransactionParams.put(NetworkUtility.TAGS.CHEEPCODE, Utility.EMPTY_STRING);
            mTransactionParams.put(NetworkUtility.TAGS.PROMOCODE_PRICE, Utility.ZERO_STRING);
        }

        new HDFCPaymentUtility.AsyncFetchEncryptedString(new HDFCPaymentUtility.EncryptTransactionParamsListener() {
            @Override
            public void onPostOfEncryption(String encryptedData) {
                UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();

                //Add Header parameters
                Map<String, String> mHeaderParams = new HashMap<>();
                mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
                mHeaderParams.put(NetworkUtility.TAGS.USER_ID, userDetails.UserID);

                Map<String, Object> mFinalParams = new HashMap<>();
                mFinalParams.put(NetworkUtility.TAGS.DATA, encryptedData);

                //calling this to create post data
                //Url is based on condition if address id is greater then 0 then it means we need to update the existing address
                @SuppressWarnings("unchecked")
                VolleyNetworkRequest mVolleyNetworkRequestForSPList = new VolleyNetworkRequest(NetworkUtility.WS.GET_PAYMENT_HASH_FOR_STRATEGIC_PARTNER
                        , mCallGenerateHashWSErrorListener
                        , mCallGenerateHashWSResponseListener
                        , mHeaderParams
                        , mFinalParams
                        , null);
                Volley.getInstance(mContext).

                        addToRequestQueue(mVolleyNetworkRequestForSPList);
            }
        }).execute(new JSONObject(mTransactionParams).toString());
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
        // Unregister the listerners you register in OnCreate method
        try {
            Volley.getInstance(PaymentChoiceActivity.this).getRequestQueue().cancelAll(NetworkUtility.WS.PAY_TASK_PAYMENT);
            EventBus.getDefault().unregister(this);
        } catch (Exception e) {

        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // check paytm access token is still valid and show linked account balance
        UserDetails userDetails = PreferenceUtility.getInstance(PaymentChoiceActivity.this).getUserDetails();
        if (userDetails != null && userDetails.mPaytmUserDetail != null) {
//        if (false) {
            try {
                long accessTokenExpiresTimeStamp = Long.parseLong(userDetails.mPaytmUserDetail.accessTokenExpiresTimestamp);
                if (accessTokenExpiresTimeStamp < System.currentTimeMillis()) {
                    // access token has been expired
                    userDetails.mPaytmUserDetail = null;
                    PreferenceUtility.getInstance(PaymentChoiceActivity.this).saveUserDetails(userDetails);
                    mActivityPaymentChoiceBinding.tvPaytmLinkAccount.setText(getString(R.string.label_link_x, getString(R.string.label_account)));
                    mActivityPaymentChoiceBinding.tvPaytmLinkAccount.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_link_blue, 0, 0, 0);
                    PAYTM_STEP = PAYTM_SEND_OTP;

                } else {
                    // show linked account balace
                    mActivityPaymentChoiceBinding.tvPaytmLinkAccount.setVisibility(View.GONE);

                    checkBalance(userDetails.mPaytmUserDetail.paytmAccessToken);
                }
            } catch (NumberFormatException e) {
                mActivityPaymentChoiceBinding.tvPaytmLinkAccount.setText(getString(R.string.label_link_x, getString(R.string.label_account)));
                mActivityPaymentChoiceBinding.tvPaytmLinkAccount.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_link_blue, 0, 0, 0);
                PAYTM_STEP = PAYTM_SEND_OTP;
            }
        } else {
            mActivityPaymentChoiceBinding.tvPaytmLinkAccount.setText(getString(R.string.label_link_x, getString(R.string.label_account)));
            mActivityPaymentChoiceBinding.tvPaytmLinkAccount.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_link_blue, 0, 0, 0);
            PAYTM_STEP = PAYTM_SEND_OTP;
        }
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
                // show dialog
//                Utility.showToast(mContext, "Paytm Puru have giteeka na hawale vatan sathio!!!!! :) ");
//                TODO: Need to start the task from here

                if (taskDetailModel.taskType.equalsIgnoreCase(Utility.TASK_TYPE.NORMAL)) {
                    callPaymentForNormalTaskWS(event.paytmResponse.ResponsePayLoad);
                }

            } else {
                Utility.showToast(mContext, getString(R.string.msg_payment_failed));
            }
        }
    }

    ///////////////////////////////////////////////////////////Paytm Check Balance API call starts///////////////////////////////////////////////////////////

    private void checkBalance(String mAccessToken) {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityPaymentChoiceBinding.getRoot());
            return;
        }

        //Show Progress
        mActivityPaymentChoiceBinding.progress.setVisibility(View.VISIBLE);
        PaytmUtility.checkBalance(mContext, mAccessToken, mCheckBalanceResponseListener);
    }

    double paytmWalletBalance;
    private double payableAmount = 0;
    private final PaytmUtility.CheckBalanceResponseListener mCheckBalanceResponseListener = new PaytmUtility.CheckBalanceResponseListener() {
        @Override
        public void paytmCheckBalanceSuccessResponse(JSONObject jsonObject) {
            try {
//                requestGuid = jsonObject.getString(NetworkUtility.PAYTM.PARAMETERS.requestGuid);
                String paytmReturnedOrderId = jsonObject.getString(orderId);
                JSONObject responseParamJson = jsonObject.getJSONObject(response);
                double totalBalance = responseParamJson.getDouble(NetworkUtility.PAYTM.PARAMETERS.totalBalance);
                paytmWalletBalance = responseParamJson.getDouble(NetworkUtility.PAYTM.PARAMETERS.paytmWalletBalance);
                String ownerGuid = responseParamJson.getString(NetworkUtility.PAYTM.PARAMETERS.ownerGuid);
                String walletGrade = responseParamJson.getString(NetworkUtility.PAYTM.PARAMETERS.walletGrade);
                String ssoId = responseParamJson.getString(NetworkUtility.PAYTM.PARAMETERS.ssoId);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (amount.contains(Utility.COMMA)) {
                amount = amount.replace(Utility.COMMA, Utility.EMPTY_STRING);
            }
            boolean isLowBalance = paytmWalletBalance < Double.parseDouble(amount);
            payableAmount = Double.parseDouble(amount) - paytmWalletBalance;
            mActivityPaymentChoiceBinding.tvPaytmBalance.setVisibility(View.VISIBLE);

            mActivityPaymentChoiceBinding.tvPaytmBalance.setText("(" + getString(R.string.rupee_symbol_x, String.valueOf(paytmWalletBalance)) + ")");
            mActivityPaymentChoiceBinding.tvPaytmLinkAccount.setVisibility(View.VISIBLE);
            mActivityPaymentChoiceBinding.tvPaytmLinkAccount.setText(Utility.EMPTY_STRING);
            mActivityPaymentChoiceBinding.tvPaytmLinkAccount.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_right_arrow_blue, 0, 0, 0);
            if (isLowBalance) {
//            BTN_WHICH = BTN_IS_ADD_AMOUNT;
                //TODO: add amount
                mActivityPaymentChoiceBinding.tvLowBalancePaytm.setVisibility(View.VISIBLE);
                mActivityPaymentChoiceBinding.tvLowBalancePaytm.setText("Low balance. You need " +
                        getString(R.string.rupee_symbol_x, Utility.getQuotePriceFormatter(String.valueOf(payableAmount))) /*+ "..."*/);
                PAYTM_STEP = PAYTM_ADD_MONEY;

            } else {
//            BTN_WHICH = BTN_IS_CONFIRM;
                //TODO: withdraw money
                PAYTM_STEP = PAYTM_WITHDRAW;
            }

            mActivityPaymentChoiceBinding.progress.setVisibility(View.GONE);

        }


        //This method is called when access token expires early due to some reason and we need to do whole OAuth process again
        @Override
        public void paytmInvalidAuthorization() {
            //TODO: implement that if accessToken is valid i.e. 1 month is not due directly call checkBalance API.
        }

        @Override
        public void showGeneralizedErrorMessage() {
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityPaymentChoiceBinding.getRoot());
            mActivityPaymentChoiceBinding.progress.setVisibility(View.GONE);
        }

        @Override
        public void paytmInvalidMobileNumber() {
            mActivityPaymentChoiceBinding.progress.setVisibility(View.GONE);
            Utility.showSnackBar(getString(R.string.validate_phone_number), mActivityPaymentChoiceBinding.getRoot());
        }

        @Override
        public void paytmAccountBlocked() {
            mActivityPaymentChoiceBinding.progress.setVisibility(View.GONE);
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityPaymentChoiceBinding.getRoot());
        }

        @Override
        public void volleyError() {
            mActivityPaymentChoiceBinding.progress.setVisibility(View.GONE);
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityPaymentChoiceBinding.getRoot());
        }
    };


}