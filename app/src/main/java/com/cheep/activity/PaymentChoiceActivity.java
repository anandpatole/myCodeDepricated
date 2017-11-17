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
import com.appsflyer.AppsFlyerLib;
import com.cheep.BootstrapConstant;
import com.cheep.BuildConfig;
import com.cheep.R;
import com.cheep.databinding.ActivityPaymentChoiceBinding;
import com.cheep.dialogs.AcknowledgementDialogWithProfilePic;
import com.cheep.dialogs.AcknowledgementInteractionListener;
import com.cheep.dialogs.PayByCashDialog;
import com.cheep.fragment.BaseFragment;
import com.cheep.model.AddressModel;
import com.cheep.model.MessageEvent;
import com.cheep.model.ProviderModel;
import com.cheep.model.TaskDetailModel;
import com.cheep.model.UserDetails;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.strategicpartner.model.MediaModel;
import com.cheep.strategicpartner.model.QueAnsModel;
import com.cheep.utils.HDFCPaymentUtility;
import com.cheep.utils.LogUtils;
import com.cheep.utils.PaytmUtility;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.SuperCalendar;
import com.cheep.utils.Utility;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.cheep.network.NetworkUtility.PAYTM.PARAMETERS.orderId;
import static com.cheep.network.NetworkUtility.PAYTM.PARAMETERS.response;


public class PaymentChoiceActivity extends BaseAppCompatActivity implements View.OnClickListener {

    private static final String TAG = LogUtils.makeLogTag(PaymentChoiceActivity.class);
    private ActivityPaymentChoiceBinding mActivityPaymentChoiceBinding;
    private TaskDetailModel taskDetailModel;
    private ProviderModel providerModel;
    private String paymentMethod;
    private Map<String, String> mTransactionParams;
    private String amount;
    public static final int PAYTM_SEND_OTP = 0;
    public static final int PAYTM_ADD_MONEY = 1;
    public static final int PAYTM_WITHDRAW = 2;
    private Map<String, Object> mTaskCreationParams;

    private int PAYTM_STEP = -1;
    private boolean isPayNow = false;
    private AddressModel mSelectedAddressModel;


    public static void newInstance(Context context, TaskDetailModel taskDetailModel) {
        Intent intent = new Intent(context, PaymentChoiceActivity.class);
        intent.putExtra(Utility.Extra.DATA, Utility.getJsonStringFromObject(taskDetailModel));
        intent.putExtra(Utility.Extra.IS_PAY_NOW, false);
        context.startActivity(intent);
    }

    public static void newInstance(Context context, TaskDetailModel taskDetailModel, ProviderModel providerModel, int isAdditionalPayment, AddressModel mSelectedAddressModel) {
        Intent intent = new Intent(context, PaymentChoiceActivity.class);
        intent.putExtra(Utility.Extra.DATA, Utility.getJsonStringFromObject(taskDetailModel));
        intent.putExtra(Utility.Extra.DATA_2, Utility.getJsonStringFromObject(providerModel));
        intent.putExtra(Utility.Extra.PAYMENT_VIEW_IS_ADDITIONAL_CHARGE, isAdditionalPayment);
        intent.putExtra(Utility.Extra.SELECTED_ADDRESS_MODEL, Utility.getJsonStringFromObject(mSelectedAddressModel));
        intent.putExtra(Utility.Extra.IS_PAY_NOW, true);
        context.startActivity(intent);
    }

    public static void newInstance(BaseFragment baseFragment, TaskDetailModel taskDetailModel, AddressModel mSelectedAddressModel) {
        Intent intent = new Intent(baseFragment.getActivity(), PaymentChoiceActivity.class);
        intent.putExtra(Utility.Extra.DATA, Utility.getJsonStringFromObject(taskDetailModel));
        intent.putExtra(Utility.Extra.IS_PAY_NOW, true);
        intent.putExtra(Utility.Extra.SELECTED_ADDRESS_MODEL, Utility.getJsonStringFromObject(mSelectedAddressModel));
        baseFragment.startActivity(intent);
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
        // TODO: Changes are per new flow pay now/later: 16/11/17

        if (getIntent().hasExtra(Utility.Extra.DATA)) {
            taskDetailModel = (TaskDetailModel) Utility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA), TaskDetailModel.class);
            if (getIntent().hasExtra(Utility.Extra.DATA_2)) {
                providerModel = (ProviderModel) Utility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA_2), ProviderModel.class);
            } else {
                providerModel = taskDetailModel.selectedProvider;
            }
            if (getIntent().hasExtra(Utility.Extra.SELECTED_ADDRESS_MODEL)) {
                mSelectedAddressModel = (AddressModel) Utility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.SELECTED_ADDRESS_MODEL), AddressModel.class);
            }
        }
        isPayNow = getIntent().getBooleanExtra(Utility.Extra.IS_PAY_NOW, false);
        mActivityPaymentChoiceBinding.tvPaytmLinkAccount.setText(getString(R.string.label_link_x, getString(R.string.label_account)));
        setupActionbar();

    }

    private void setupActionbar() {

        // set final payment amount which user going to pay

        if (isPayNow) {
            if (taskDetailModel.taskType.equalsIgnoreCase(Utility.TASK_TYPE.STRATEGIC)) {
                if (taskDetailModel.cheepCode != null && !taskDetailModel.cheepCode.isEmpty())
                    amount = Utility.getQuotePriceFormatter(taskDetailModel.payableAmountStrategicPartner);
                else
                    amount = Utility.getQuotePriceFormatter(taskDetailModel.totalStrategicPartner);
            } /*else if (isAdditional != 0) {
                amount = Utility.getQuotePriceFormatter(taskDetailModel.additionalQuoteAmount);
            } */ else {
                amount = Utility.getQuotePriceFormatter(providerModel.quotePrice);
            }
        } else {
            amount = Utility.getQuotePriceFormatter(taskDetailModel.taskPaidAmount);
        }
        mActivityPaymentChoiceBinding.textTitle.setText(getString(R.string.label_please_pay_x, amount));


        setSupportActionBar(mActivityPaymentChoiceBinding.toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(Utility.EMPTY_STRING);
        actionBar.setDisplayHomeAsUpEnabled(true);
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

                PayByCashDialog payByCashDialog = PayByCashDialog.newInstance(providerModel.userName, Utility.getQuotePriceFormatter(taskDetailModel.taskPaidAmount), new PayByCashDialog.PayByCashDoneListener() {
                    @Override
                    public void onDoneClick() {
                        callPaymentForNormalOrInstaTaskWS(Utility.EMPTY_STRING);
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
                PreferenceUtility.getInstance(this).getFCMRegID(), userDetails, taskDetailModel, providerModel);

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
                            if (isPayNow) {
                                if (taskDetailModel.taskType.equalsIgnoreCase(Utility.TASK_TYPE.NORMAL))
                                    callBookProAndPayForNormalTaskWS(getString(R.string.message_payment_bypassed));
                                else if (taskDetailModel.taskType.equalsIgnoreCase(Utility.TASK_TYPE.INSTA_BOOK)) {
                                    callCreateInstaBookingTaskWS(getString(R.string.message_payment_bypassed));
                                } else {
                                    LogUtils.LOGE(TAG, "onResponse: call staretegic partner task");
                                    callCreateStrategicPartnerTaskWS(getString(R.string.message_payment_bypassed));
                                }
                            } else {
                                callPaymentForNormalOrInstaTaskWS(getString(R.string.message_payment_bypassed));
                            }
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

                        if (isPayNow)
                            if (taskDetailModel.taskType.equalsIgnoreCase(Utility.TASK_TYPE.NORMAL))
                                callBookProAndPayForNormalTaskWS(data.getStringExtra(Utility.Extra.PAYU_RESPONSE));
                            else if (taskDetailModel.taskType.equalsIgnoreCase(Utility.TASK_TYPE.INSTA_BOOK)) {
                                callCreateInstaBookingTaskWS(data.getStringExtra(Utility.Extra.PAYU_RESPONSE));
                            } else {
                                LogUtils.LOGE(TAG, "onActivityResult: call startegic partner task");
                                callCreateStrategicPartnerTaskWS(data.getStringExtra(Utility.Extra.PAYU_RESPONSE));
                            }
                        else
                            callPaymentForNormalOrInstaTaskWS(data.getStringExtra(Utility.Extra.PAYU_RESPONSE));


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

    private void callPaymentForNormalOrInstaTaskWS(String paymentLog) {
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
        switch (event.BROADCAST_ACTION) {
            case Utility.BROADCAST_TYPE.PAYTM_RESPONSE:
                // Check the response <code></code>
                if (event.paytmResponse.isSuccess) {
                    // show dialog
//                TODO: Need to start the task from here
                    if (taskDetailModel.taskType.equalsIgnoreCase(Utility.TASK_TYPE.STRATEGIC)) {
                        callPaymentForStrategicTaskWS(event.paytmResponse.ResponsePayLoad);
                    } else {
                        if (isPayNow)
                            if (taskDetailModel.taskType.equalsIgnoreCase(Utility.TASK_TYPE.NORMAL))
                                callBookProAndPayForNormalTaskWS(event.paytmResponse.ResponsePayLoad);
                            else if (taskDetailModel.taskType.equalsIgnoreCase(Utility.TASK_TYPE.INSTA_BOOK)) {
                                callCreateInstaBookingTaskWS(event.paytmResponse.ResponsePayLoad);
                            } else {
                                callCreateStrategicPartnerTaskWS(event.paytmResponse.ResponsePayLoad);
                                LogUtils.LOGE(TAG, "onResponse: call staretegic partner task");
                            }
                        else {
                            callPaymentForNormalOrInstaTaskWS(event.paytmResponse.ResponsePayLoad);
                        }
                    }
                } else {
                    Utility.showToast(mContext, getString(R.string.msg_payment_failed));
                }
                break;
            case Utility.BROADCAST_TYPE.PAYMENT_COMPLETED_NEED_TO_REDIRECT_TO_MY_TASK_SCREEN:

                break;
        }

    }

    private void callPaymentForStrategicTaskWS(String responsePayLoad) {

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
            mActivityPaymentChoiceBinding.progress.setVisibility(View.GONE);
            mActivityPaymentChoiceBinding.tvPaytmLinkAccount.setVisibility(View.VISIBLE);
            mActivityPaymentChoiceBinding.tvPaytmLinkAccount.setText(getString(R.string.label_link_x, getString(R.string.label_account)));
            mActivityPaymentChoiceBinding.tvPaytmLinkAccount.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_link_blue, 0, 0, 0);
            PAYTM_STEP = PAYTM_SEND_OTP;
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

    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////           Book and pay ws         [start]                               /////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////


    private void callBookProAndPayForNormalTaskWS(String paymentLog) {
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
        mParams.put(NetworkUtility.TAGS.SP_USER_ID, providerModel.providerId);
        mParams.put(NetworkUtility.TAGS.TASK_ID, taskDetailModel.taskId);
        mParams.put(NetworkUtility.TAGS.IS_REFER_CODE, taskDetailModel.isReferCode);
        mParams.put(NetworkUtility.TAGS.PAYABLE_AMOUNT, providerModel.quotePrice);
        mParams.put(NetworkUtility.TAGS.USED_WALLET_BALANCE, taskDetailModel.usedWalletAmount);

        if (!TextUtils.isEmpty(taskDetailModel.cheepCode)) {
            mParams.put(NetworkUtility.TAGS.CHEEPCODE, taskDetailModel.cheepCode);
            mParams.put(NetworkUtility.TAGS.PROMOCODE_PRICE, taskDetailModel.taskDiscountAmount);
        } else {
            mParams.put(NetworkUtility.TAGS.CHEEPCODE, Utility.EMPTY_STRING);
            mParams.put(NetworkUtility.TAGS.PROMOCODE_PRICE, Utility.ZERO_STRING);
        }


        // TODO: Changes are per new flow pay now/later: 16/11/17
        // as per new flow if user selects pay now then book and pay will be done together
        // so setting task book pro params and payment params

        if (mTransactionParams == null)
            mParams.put(NetworkUtility.TAGS.TRANSACTION_ID, Utility.getUniqueTransactionId());
        else
            mParams.put(NetworkUtility.TAGS.TRANSACTION_ID, mTransactionParams.get(HDFCPaymentUtility.TXN_ID));
        mParams.put(NetworkUtility.TAGS.PAYMENT_LOG, paymentLog);
        mParams.put(NetworkUtility.TAGS.PAYMENT_STATUS, Utility.PAYMENT_STATUS.COMPLETED);
        mParams.put(NetworkUtility.TAGS.PAYMENT_METHOD, paymentMethod);
        mParams.put(NetworkUtility.TAGS.TASK_ID, taskDetailModel.taskId);

        LogUtils.LOGE(TAG, "callBookProAndPayForNormalTaskWS: mParams " + mParams);

        // Url is based on condition if address id is greater then 0 then it means we need to update the existing address
        VolleyNetworkRequest mVolleyNetworkRequestForSPList = new VolleyNetworkRequest(NetworkUtility.WS.BOOK_PRO_FOR_NORMAL_TASK
                , mCallBookProForNormalTaskWSErrorListener
                , mCallBookProForNormalTaskWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForSPList);
    }


    Response.Listener mCallBookProForNormalTaskWSResponseListener = new Response.Listener() {
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

                        JSONObject jsonData = jsonObject.optJSONObject(NetworkUtility.TAGS.DATA);
                        String taskStatus = jsonData.optString(NetworkUtility.TAGS.TASK_STATUS);


                        // AS PER new flow pay later task status will be pending
                        if (Utility.TASK_STATUS.PENDING.equalsIgnoreCase(taskStatus) ) {
                            //We are commenting it because from here we are intiating a payment flow and
                            // after that we need to call update payment status on server
                            String taskPaidAmount = jsonData.optString(NetworkUtility.TAGS.TASK_PAID_AMOUNT);
                            if (taskDetailModel != null) {
                                taskDetailModel.taskStatus = taskStatus;
                                if (!TextUtils.isEmpty(taskPaidAmount))
                                    taskDetailModel.taskPaidAmount = taskPaidAmount;
                                /*
                                * Update selected sp on firebase
                                * @Sanjay 20 Feb 2016
                                * */
                                if (providerModel != null) {
                                    Utility.updateSelectedSpOnFirebase(mContext, taskDetailModel, providerModel, taskDetailModel.taskType.equalsIgnoreCase(Utility.TASK_TYPE.INSTA_BOOK));
                                }
                            }

                            //  Refresh UI for Paid status
                            //  FillProviderDetails(providerModel);

                            MessageEvent messageEvent = new MessageEvent();
                            messageEvent.BROADCAST_ACTION = Utility.BROADCAST_TYPE.TASK_PRO_BOOKED;
                            messageEvent.id = taskDetailModel.taskId;
                            EventBus.getDefault().post(messageEvent);

                             /*
                             *  @Changes : 7th July, 2017 :- Bhavesh Patadiya
                             *  Need to show Model Dialog once Payment has been made successful. Once
                             *  User clicks on OK. we will finish of the activity.
                             */
//                            String title = mContext.getString(R.string.label_great_choice_x, PreferenceUtility.getInstance(mContext).getUserDetails().UserName);
                            String title = mContext.getString(R.string.label_brilliant) + "!";
                            final SuperCalendar superStartDateTimeCalendar = SuperCalendar.getInstance();
                            superStartDateTimeCalendar.setTimeZone(SuperCalendar.SuperTimeZone.GMT.GMT);
                            superStartDateTimeCalendar.setTimeInMillis(Long.parseLong(taskDetailModel.taskStartdate));
                            superStartDateTimeCalendar.setLocaleTimeZone();

                            int onlydate = Integer.parseInt(superStartDateTimeCalendar.format("dd"));
                            String message = Utility.fetchMessageFromDateOfMonth(mContext, onlydate, superStartDateTimeCalendar, providerModel);

//                            final UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
                            int badgeResId = Utility.getProLevelBadge(providerModel.pro_level);
                            AcknowledgementDialogWithProfilePic mAcknowledgementDialogWithProfilePic = AcknowledgementDialogWithProfilePic.newInstance(
                                    mContext,
                                    R.drawable.ic_acknowledgement_dialog_header_background,
                                    title,
                                    message,
                                    providerModel != null ? providerModel.profileUrl : null,
                                    new AcknowledgementInteractionListener() {

                                        @Override
                                        public void onAcknowledgementAccepted() {
                                            // Finish the activity
                                            finish();

                                            // Payment is been done now, so broadcast this specific case to relavent activities
                                            MessageEvent messageEvent = new MessageEvent();
                                            messageEvent.BROADCAST_ACTION = Utility.BROADCAST_TYPE.PAYMENT_COMPLETED_NEED_TO_REDIRECT_TO_MY_TASK_SCREEN;
                                            EventBus.getDefault().post(messageEvent);
                                        }
                                    }, badgeResId);
                            mAcknowledgementDialogWithProfilePic.setCancelable(false);
                            mAcknowledgementDialogWithProfilePic.show(getSupportFragmentManager(), AcknowledgementDialogWithProfilePic.TAG);

                        } else if (Utility.TASK_STATUS.PROCESSING.equalsIgnoreCase(taskStatus)) {
                            //We are commenting it because from here we are intiating a payment flow and after that we need to call update payment status on server
                            String taskPaidAmount = jsonData.optString(NetworkUtility.TAGS.TASK_PAID_AMOUNT);
                            if (taskDetailModel != null) {
                                taskDetailModel.taskStatus = taskStatus;
                                if (!TextUtils.isEmpty(taskPaidAmount))
                                    taskDetailModel.taskPaidAmount = taskPaidAmount;
                            }
                            //  Refresh UI for Paid status
                            //  FillProviderDetails(providerModel);
                            MessageEvent messageEvent = new MessageEvent();
                            messageEvent.BROADCAST_ACTION = Utility.BROADCAST_TYPE.TASK_PROCESSING;
                            messageEvent.id = taskDetailModel.taskId;
                            EventBus.getDefault().post(messageEvent);

                            // Finish the activity
                            finish();
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
                mCallBookProForNormalTaskWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }

        }
    };

    Response.ErrorListener mCallBookProForNormalTaskWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(final VolleyError error) {
            LogUtils.LOGE(TAG, "onErrorResponse() called with: error = [" + error + "]");
            // Close Progressbar
            hideProgressDialog();
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityPaymentChoiceBinding.getRoot());
        }
    };


    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////              Book and pay ws        [end]                               /////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////


    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////              Create Insta task + payment        [start]                 /////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    private void callCreateInstaBookingTaskWS(String paymentLog) {

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
        mParams.put(NetworkUtility.TAGS.TASK_DESC, taskDetailModel.taskDesc);
        String txnId = Utility.getUniqueTransactionId();
        if (Integer.parseInt(mSelectedAddressModel.address_id) > 0) {
            mParams.put(NetworkUtility.TAGS.ADDRESS_ID, mSelectedAddressModel.address_id);
        } else {
            // In case its negative then provide other address information
            mParams.put(NetworkUtility.TAGS.ADDRESS_INITIALS, mSelectedAddressModel.address_initials);
            mParams.put(NetworkUtility.TAGS.ADDRESS, mSelectedAddressModel.address);
            mParams.put(NetworkUtility.TAGS.CATEGORY, mSelectedAddressModel.category);
            mParams.put(NetworkUtility.TAGS.LAT, mSelectedAddressModel.lat);
            mParams.put(NetworkUtility.TAGS.LNG, mSelectedAddressModel.lng);
            mParams.put(NetworkUtility.TAGS.CITY_NAME, mSelectedAddressModel.cityName);
            mParams.put(NetworkUtility.TAGS.COUNTRY, mSelectedAddressModel.countryName);
            mParams.put(NetworkUtility.TAGS.STATE, mSelectedAddressModel.stateName);
        }
        mParams.put(NetworkUtility.TAGS.CITY_ID, userDetails.CityID);
        mParams.put(NetworkUtility.TAGS.CAT_ID, taskDetailModel.categoryId);
        mParams.put(NetworkUtility.TAGS.START_DATETIME, taskDetailModel.taskStartdate);
        mParams.put(NetworkUtility.TAGS.SUBCATEGORY_ID, taskDetailModel.subCategoryID);
        mParams.put(NetworkUtility.TAGS.SP_USER_ID, providerModel.providerId);
        mParams.put(NetworkUtility.TAGS.TRANSACTION_ID, txnId);
        mParams.put(NetworkUtility.TAGS.PAYMENT_STATUS, Utility.PAYMENT_STATUS.COMPLETED);
        LogUtils.LOGE(TAG, "payNow: cheepCode " + taskDetailModel.cheepCode);
        LogUtils.LOGE(TAG, "payNow: dicount " + taskDetailModel.taskDiscountAmount);

        if (!TextUtils.isEmpty(taskDetailModel.cheepCode)) {
            mParams.put(NetworkUtility.TAGS.CHEEPCODE, taskDetailModel.cheepCode);
            mParams.put(NetworkUtility.TAGS.PROMOCODE_PRICE, taskDetailModel.taskDiscountAmount);
        } else {
            mParams.put(NetworkUtility.TAGS.CHEEPCODE, Utility.EMPTY_STRING);
            mParams.put(NetworkUtility.TAGS.PROMOCODE_PRICE, Utility.ZERO_STRING);
        }
        mParams.put(NetworkUtility.TAGS.IS_REFER_CODE, taskDetailModel.isReferCode);
        mParams.put(NetworkUtility.TAGS.QUOTE_AMOUNT, providerModel.spWithoutGstQuotePrice);
        mParams.put(NetworkUtility.TAGS.PAYABLE_AMOUNT, providerModel.quotePrice);
        mParams.put(NetworkUtility.TAGS.PAYMENT_LOG, Utility.EMPTY_STRING);
        mParams.put(NetworkUtility.TAGS.PAYMENT_METHOD, NetworkUtility.PAYMENT_METHOD_TYPE.PAY_LATER);
        mParams.put(NetworkUtility.TAGS.USED_WALLET_BALANCE, taskDetailModel.usedWalletAmount);
        String media_file = Utility.getSelectedMediaJsonString(taskDetailModel.mMediaModelList);
        mParams.put(NetworkUtility.TAGS.MEDIA_FILE, media_file);
        mParams.put(NetworkUtility.TAGS.TASK_TYPE, Utility.TASK_TYPE.INSTA_BOOK);

        // TODO: Changes are per new flow pay now/later: 16/11/17
        // as per new flow if user selects pay now then book and pay will be done together
        // so setting task book pro params and payment params

        if (mTransactionParams == null)
            mParams.put(NetworkUtility.TAGS.TRANSACTION_ID, Utility.getUniqueTransactionId());
        else
            mParams.put(NetworkUtility.TAGS.TRANSACTION_ID, mTransactionParams.get(HDFCPaymentUtility.TXN_ID));
        mParams.put(NetworkUtility.TAGS.PAYMENT_LOG, paymentLog);
        mParams.put(NetworkUtility.TAGS.PAYMENT_STATUS, Utility.PAYMENT_STATUS.COMPLETED);
        mParams.put(NetworkUtility.TAGS.PAYMENT_METHOD, paymentMethod);
        mParams.put(NetworkUtility.TAGS.TASK_ID, taskDetailModel.taskId);


        // For AppsFlyer
        mTaskCreationParams = new HashMap<>();
        mTaskCreationParams.put(NetworkUtility.TAGS.TASK_DESC, taskDetailModel.taskDesc);
//        mParams.put(NetworkUtility.TAGS.ADDRESS_ID, taskDetailModel.taskAddressId);
        if (Integer.parseInt(mSelectedAddressModel.address_id) > 0) {
            mTaskCreationParams.put(NetworkUtility.TAGS.ADDRESS_ID, mSelectedAddressModel.address_id);
        } else {
            // In case its nagative then provide other address information
            mTaskCreationParams.put(NetworkUtility.TAGS.ADDRESS_INITIALS, mSelectedAddressModel.address_initials);
            mTaskCreationParams.put(NetworkUtility.TAGS.ADDRESS, mSelectedAddressModel.address);
            mTaskCreationParams.put(NetworkUtility.TAGS.CATEGORY, mSelectedAddressModel.category);
            mTaskCreationParams.put(NetworkUtility.TAGS.LAT, mSelectedAddressModel.lat);
            mTaskCreationParams.put(NetworkUtility.TAGS.LNG, mSelectedAddressModel.lng);
            mTaskCreationParams.put(NetworkUtility.TAGS.CITY_NAME, mSelectedAddressModel.cityName);
            mTaskCreationParams.put(NetworkUtility.TAGS.COUNTRY, mSelectedAddressModel.countryName);
            mTaskCreationParams.put(NetworkUtility.TAGS.STATE, mSelectedAddressModel.stateName);
        }
        mTaskCreationParams.put(NetworkUtility.TAGS.CITY_ID, userDetails.CityID);
        mTaskCreationParams.put(NetworkUtility.TAGS.CAT_ID, taskDetailModel.categoryId);
        mTaskCreationParams.put(NetworkUtility.TAGS.START_DATETIME, taskDetailModel.taskStartdate);
        mTaskCreationParams.put(NetworkUtility.TAGS.PAYMENT_STATUS, Utility.PAYMENT_STATUS.COMPLETED);
        mTaskCreationParams.put(NetworkUtility.TAGS.SUBCATEGORY_ID, taskDetailModel.subCategoryID);
        mTaskCreationParams.put(NetworkUtility.TAGS.SP_USER_ID, providerModel.providerId);
        mTaskCreationParams.put(NetworkUtility.TAGS.USED_WALLET_BALANCE, taskDetailModel.usedWalletAmount);
        mTaskCreationParams.put(NetworkUtility.TAGS.MEDIA_FILE, media_file);
        mTaskCreationParams.put(NetworkUtility.TAGS.TRANSACTION_ID, txnId);
        mTaskCreationParams.put(NetworkUtility.TAGS.TASK_TYPE, Utility.TASK_TYPE.INSTA_BOOK);

        if (!TextUtils.isEmpty(taskDetailModel.cheepCode)) {
            mTaskCreationParams.put(NetworkUtility.TAGS.CHEEPCODE, taskDetailModel.cheepCode);
            mTaskCreationParams.put(NetworkUtility.TAGS.PROMOCODE_PRICE, taskDetailModel.taskDiscountAmount);

        } else {
            mTaskCreationParams.put(NetworkUtility.TAGS.CHEEPCODE, Utility.EMPTY_STRING);
            mTaskCreationParams.put(NetworkUtility.TAGS.PROMOCODE_PRICE, Utility.ZERO_STRING);
        }

        mTaskCreationParams.put(NetworkUtility.TAGS.QUOTE_AMOUNT, providerModel.spWithoutGstQuotePrice);
        mTaskCreationParams.put(NetworkUtility.TAGS.PAYABLE_AMOUNT, providerModel.quotePrice);
        mTaskCreationParams.put(NetworkUtility.TAGS.PAYMENT_LOG, Utility.EMPTY_STRING);
        mTaskCreationParams.put(NetworkUtility.TAGS.PAYMENT_METHOD, NetworkUtility.PAYMENT_METHOD_TYPE.PAY_LATER);

        // Url is based on condition if address id is greater then 0 then it means we need to update the existing address
        VolleyNetworkRequest mVolleyNetworkRequestForSPList = new VolleyNetworkRequest(NetworkUtility.WS.CREATE_TASK
                , mCallBookProForNormalTaskWSErrorListener
                , mCallCreateInstaTaskWSResponseListener
                , mHeaderParams
                , mParams, null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForSPList);

    }

    Response.Listener mCallCreateInstaTaskWSResponseListener = new Response.Listener() {
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
                        // Send Event tracking for AppsFlyer
                        AppsFlyerLib.getInstance().trackEvent(mContext, NetworkUtility.TAGS.APPSFLYER_CUSTOM_TRACK_EVENTS.TASK_CREATE, mTaskCreationParams);
                        Utility.onSuccessfulInstaBookingTaskCompletion(PaymentChoiceActivity.this, jsonObject, providerModel);
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
                mCallBookProForNormalTaskWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }

        }
    };

    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////              Create Insta task + payment       [end]                    /////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////           Create Strategic task + payment  [start]                      /////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void callCreateStrategicPartnerTaskWS(String paymentLog) {

        // Check Internet connection
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityPaymentChoiceBinding.getRoot());
            return;
        }

        ArrayList<MediaModel> mMediaModelList = new ArrayList<>();
        for (QueAnsModel model : taskDetailModel.mQuesList)
            if (model.answerType.equalsIgnoreCase(Utility.TEMPLATE_UPLOAD)) {
                mMediaModelList = model.medialList;
                break;
            }

        //Show Progress
        showProgressDialog();

        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();

        // Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, userDetails.UserID);
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());

        // Add Params
        ArrayList<QueAnsModel> mList = taskDetailModel.mQuesList;
        String subCategoryDetail = Utility.getSelectedServicesJsonString(taskDetailModel.taskSelectedSubCategoryList);
        String task_desc = taskDetailModel.taskDesc;
        String question_detail = Utility.getQuestionAnswerDetailsJsonString(mList);
        String media_file = "";
        media_file = Utility.getSelectedMediaJsonString(taskDetailModel.mMediaModelList);
        LogUtils.LOGE(TAG, "start dat time " + taskDetailModel.taskStartdate);
        SuperCalendar superCalendar = SuperCalendar.getInstance();
        String txnid = Utility.getUniqueTransactionId();
        superCalendar.setTimeInMillis(Long.parseLong(taskDetailModel.taskStartdate));
        superCalendar.setTimeZone(SuperCalendar.SuperTimeZone.GMT.GMT);

        LogUtils.LOGE(TAG, "gmt time " + String.valueOf(superCalendar.getTimeInMillis()));
        LogUtils.LOGE(TAG, "Payment method type" + String.valueOf(superCalendar.getTimeInMillis()));

        Map<String, String> mParams = new HashMap<>();
        if (mSelectedAddressModel != null)
            if (Integer.parseInt(mSelectedAddressModel.address_id) > 0) {
                mParams.put(NetworkUtility.TAGS.ADDRESS_ID, mSelectedAddressModel.address_id);
                mParams.put(NetworkUtility.TAGS.ADDRESS_ID, userDetails.CityID);
            } else {
                mParams.put(NetworkUtility.TAGS.ADDRESS_INITIALS, mSelectedAddressModel.address_initials);
                mParams.put(NetworkUtility.TAGS.ADDRESS, mSelectedAddressModel.address);
                mParams.put(NetworkUtility.TAGS.CATEGORY, mSelectedAddressModel.category);
                mParams.put(NetworkUtility.TAGS.LAT, mSelectedAddressModel.lat);
                mParams.put(NetworkUtility.TAGS.LNG, mSelectedAddressModel.lng);
                mParams.put(NetworkUtility.TAGS.CITY_NAME, mSelectedAddressModel.cityName);
                mParams.put(NetworkUtility.TAGS.COUNTRY, mSelectedAddressModel.countryName);
                mParams.put(NetworkUtility.TAGS.STATE, mSelectedAddressModel.stateName);
            }

        mParams.put(NetworkUtility.TAGS.CAT_ID, taskDetailModel.categoryId);
        mParams.put(NetworkUtility.TAGS.START_DATETIME, String.valueOf(superCalendar.getTimeInMillis()));
        mParams.put(NetworkUtility.TAGS.SUB_CATEGORY_DETAIL, subCategoryDetail);
        mParams.put(NetworkUtility.TAGS.QUESTION_DETAIL, question_detail);
        mParams.put(NetworkUtility.TAGS.QUOTE_AMOUNT, taskDetailModel.quoteAmountStrategicPartner + "");

        mParams.put(NetworkUtility.TAGS.CHEEPCODE, TextUtils.isEmpty(taskDetailModel.cheepCode) ? Utility.EMPTY_STRING : taskDetailModel.cheepCode);
        mParams.put(NetworkUtility.TAGS.PAYABLE_AMOUNT, TextUtils.isEmpty(taskDetailModel.cheepCode) ? taskDetailModel.totalStrategicPartner
                : taskDetailModel.payableAmountStrategicPartner);
        mParams.put(NetworkUtility.TAGS.TRANSACTION_ID, txnid);
        mParams.put(NetworkUtility.TAGS.TASK_DESC, task_desc);
        mParams.put(NetworkUtility.TAGS.SP_USER_ID, taskDetailModel.selectedProvider.providerId);
        mParams.put(NetworkUtility.TAGS.PROMOCODE_PRICE, TextUtils.isEmpty(taskDetailModel.cheepCode) ? Utility.ZERO_STRING : taskDetailModel.taskDiscountAmount);
        // new amazon s3 uploaded file names
        mParams.put(NetworkUtility.TAGS.MEDIA_FILE, media_file);
        mParams.put(NetworkUtility.TAGS.TASK_TYPE, Utility.TASK_TYPE.STRATEGIC);
        mParams.put(NetworkUtility.TAGS.IS_REFER_CODE, Utility.BOOLEAN.NO);
        mParams.put(NetworkUtility.TAGS.USED_WALLET_BALANCE, Utility.ZERO_STRING);
        if (mTransactionParams == null)
            mParams.put(NetworkUtility.TAGS.TRANSACTION_ID, Utility.getUniqueTransactionId());
        else
            mParams.put(NetworkUtility.TAGS.TRANSACTION_ID, mTransactionParams.get(HDFCPaymentUtility.TXN_ID));
        mParams.put(NetworkUtility.TAGS.PAYMENT_LOG, paymentLog);
        mParams.put(NetworkUtility.TAGS.PAYMENT_STATUS, Utility.PAYMENT_STATUS.COMPLETED);
        mParams.put(NetworkUtility.TAGS.PAYMENT_METHOD, paymentMethod);


        LogUtils.LOGE(TAG, "subCategoryDetail = [ " + subCategoryDetail + " ] ");
        LogUtils.LOGE(TAG, "question_detail = [ " + question_detail + " ] ");
        LogUtils.LOGE(TAG, "start_datetime = [ " + taskDetailModel.taskStartdate + " ] ");
        LogUtils.LOGE(TAG, "total = [ " + taskDetailModel.quoteAmountStrategicPartner + " ] ");
        LogUtils.LOGE(TAG, "task_desc= [ " + task_desc + " ] ");
        LogUtils.LOGE(TAG, "media_file= [ " + media_file + " ] ");
        LogUtils.LOGE(TAG, "SP_USER_ID= [ " + taskDetailModel.selectedProvider.providerId + " ] ");
        LogUtils.LOGE(TAG, "cat_id = [ " + taskDetailModel.categoryId + " ] ");

        // Create Params for AppsFlyer event track
        mTaskCreationParams = new HashMap<>();
        if (mSelectedAddressModel != null)
            if (Integer.parseInt(mSelectedAddressModel.address_id) > 0) {
                mTaskCreationParams.put(NetworkUtility.TAGS.ADDRESS_ID, mSelectedAddressModel.address_id);
            } else {
                // In case its Nagative then provide other address information
            /*
             public String address_initials;
             public String address;
             public String category; //comes from NetworkUtility.TAGS.ADDRESS_TYPE.
             public String lat;
             public String lng;
             */
                mTaskCreationParams.put(NetworkUtility.TAGS.ADDRESS_INITIALS, mSelectedAddressModel.address_initials);
                mTaskCreationParams.put(NetworkUtility.TAGS.ADDRESS, mSelectedAddressModel.address);
                mTaskCreationParams.put(NetworkUtility.TAGS.CATEGORY, mSelectedAddressModel.category);
                mTaskCreationParams.put(NetworkUtility.TAGS.LAT, mSelectedAddressModel.lat);
                mTaskCreationParams.put(NetworkUtility.TAGS.LNG, mSelectedAddressModel.lng);
                mTaskCreationParams.put(NetworkUtility.TAGS.CITY_NAME, mSelectedAddressModel.cityName);
                mTaskCreationParams.put(NetworkUtility.TAGS.COUNTRY, mSelectedAddressModel.countryName);
                mTaskCreationParams.put(NetworkUtility.TAGS.STATE, mSelectedAddressModel.stateName);
            }
        mTaskCreationParams.put(NetworkUtility.TAGS.CAT_ID, taskDetailModel.categoryId);
        mTaskCreationParams.put(NetworkUtility.TAGS.START_DATETIME, taskDetailModel.taskStartdate);
        mTaskCreationParams.put(NetworkUtility.TAGS.SUB_CATEGORY_DETAIL, subCategoryDetail);
        mTaskCreationParams.put(NetworkUtility.TAGS.QUESTION_DETAIL, question_detail);
        mTaskCreationParams.put(NetworkUtility.TAGS.QUOTE_AMOUNT, taskDetailModel.quoteAmountStrategicPartner + "");
        mTaskCreationParams.put(NetworkUtility.TAGS.PAYABLE_AMOUNT, TextUtils.isEmpty(taskDetailModel.cheepCode) ? taskDetailModel.totalStrategicPartner
                : taskDetailModel.payableAmountStrategicPartner);
        if (mTransactionParams == null)
            mParams.put(NetworkUtility.TAGS.TRANSACTION_ID, Utility.getUniqueTransactionId());
        else
            mParams.put(NetworkUtility.TAGS.TRANSACTION_ID, mTransactionParams.get(HDFCPaymentUtility.TXN_ID));
        mTaskCreationParams.put(NetworkUtility.TAGS.MEDIA_FILE, media_file);
        mTaskCreationParams.put(NetworkUtility.TAGS.SP_USER_ID, taskDetailModel.selectedProvider.providerId);
        mTaskCreationParams.put(NetworkUtility.TAGS.CHEEPCODE, taskDetailModel.cheepCode);
        mTaskCreationParams.put(NetworkUtility.TAGS.PROMOCODE_PRICE, TextUtils.isEmpty(taskDetailModel.cheepCode) ? Utility.ZERO_STRING : taskDetailModel.taskDiscountAmount);

        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.CREATE_TASK
                , mCallBookProForNormalTaskWSErrorListener
                , mCallCreateTaskStrategicWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest);
    }

    Response.Listener mCallCreateTaskStrategicWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {

            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                LogUtils.LOGI(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:

                        // Send Event tracking for AppsFlyer
                        AppsFlyerLib.getInstance().trackEvent(mContext, NetworkUtility.TAGS.APPSFLYER_CUSTOM_TRACK_EVENTS.TASK_CREATE, mTaskCreationParams);

                        /*
                          Now according to the new flow, once task created
                          app will be redirected to MyTask Detail screen.
                         */
//                        TODO:This needs to be updated.
//                        onSuccessfullTaskCreated(jsonObject);
//                        Utility.showToast(mContext, "Task Created Successfully!!");

                        /*String title = "Brilliant";*/

                        SuperCalendar superCalendar = SuperCalendar.getInstance();
                        superCalendar.setTimeInMillis(Long.parseLong(taskDetailModel.taskStartdate));
                        String time = superCalendar.format(Utility.DATE_FORMAT_HH_MM_AM);
                        String date = superCalendar.format(Utility.DATE_FORMAT_DD_MMM_YYYY);

                        String message = getString(R.string.label_strategic_task_confirmed, taskDetailModel.categoryName) +
                                date + getString(R.string.label_at) + time;

                        final AcknowledgementDialogWithProfilePic mAcknowledgementDialogWithProfilePic = AcknowledgementDialogWithProfilePic.newInstance(
                                mContext,
                                R.drawable.ic_acknowledgement_dialog_header_background,
                                getString(R.string.label_brilliant),
                                message,
                                taskDetailModel.catImage,
                                new AcknowledgementInteractionListener() {

                                    @Override
                                    public void onAcknowledgementAccepted() {
                                        // Finish the activity
                                        finish();


                                        // Payment is been done now, so broadcast this specific case to relavent activities
                                        MessageEvent messageEvent = new MessageEvent();
                                        messageEvent.BROADCAST_ACTION = Utility.BROADCAST_TYPE.PAYMENT_COMPLETED_NEED_TO_REDIRECT_TO_MY_TASK_SCREEN;
                                        EventBus.getDefault().post(messageEvent);
                                    }
                                }, -1);
                        mAcknowledgementDialogWithProfilePic.setCancelable(false);
                        mAcknowledgementDialogWithProfilePic.show(getSupportFragmentManager(), AcknowledgementDialogWithProfilePic.TAG);


                        // Finish the current activity
//                        mStrategicPartnerTaskCreationAct.finish();

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
                mCallBookProForNormalTaskWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
            hideProgressDialog();
        }
    };
    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////           Create Strategic task + payment  [end]                        /////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////


}