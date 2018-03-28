package com.cheep.cheepcare.activity;

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
import com.cheep.activity.AddMoneyActivity;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.activity.HDFCPaymentGatewayActivity;
import com.cheep.activity.SendOtpActivity;
import com.cheep.activity.WithdrawMoneyActivity;
import com.cheep.cheepcare.dialogs.PaymentFailedDialog;
import com.cheep.cheepcare.dialogs.TaskConfirmedCCInstaBookDialog;
import com.cheep.cheepcare.model.CareCityDetail;
import com.cheep.cheepcare.model.CheepCarePaymentDataModel;
import com.cheep.cheepcare.model.SubscribedTaskDetailModel;
import com.cheep.databinding.ActivityPaymentChoiceCheepCareBinding;
import com.cheep.dialogs.AcknowledgementInteractionListener;
import com.cheep.model.AddressModel;
import com.cheep.model.MessageEvent;
import com.cheep.model.UserDetails;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.CalendarUtility;
import com.cheep.utils.GsonUtility;
import com.cheep.utils.HDFCPaymentUtility;
import com.cheep.utils.LogUtils;
import com.cheep.utils.PaytmUtility;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;
import com.cheep.utils.WebCallClass;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.cheep.network.NetworkUtility.PAYTM.PARAMETERS.orderId;
import static com.cheep.network.NetworkUtility.PAYTM.PARAMETERS.response;


public class PaymentChoiceCheepCareActivity extends BaseAppCompatActivity implements View.OnClickListener {

    private static final String TAG = LogUtils.makeLogTag(PaymentChoiceCheepCareActivity.class);
    private ActivityPaymentChoiceCheepCareBinding mBinding;
    private Map<String, String> mTransactionParams;
    public static final int PAYTM_SEND_OTP = 0;
    public static final int PAYTM_ADD_MONEY = 1;
    public static final int PAYTM_WITHDRAW = 2;
    private String cartDetail = "";
    private CheepCarePaymentDataModel paymentDataModel;
    private Map<String, Object> mTaskCreationParams;

    private SubscribedTaskDetailModel subscribedTaskDetailModel;
    private int PAYTM_STEP = -1;
    private String paymentMethod;
    private double paytmPayableAmount;
    private double payableAmount;
    private CareCityDetail careCityDetail;
    private String paymentFor;

    public static final String PAYMENT_FOR_SUBSCRIPTION = "payment_for_subscription";
    public static final String PAYMENT_FOR_TASK_CREATION = "payment_for_task_creation";


    /*
         cart details - selected packages json array string
         payment data model - payment info
         city detail - selected city data for which user is purchasing subscription
     */
    public static void newInstance(Context context, String cartDetails, CheepCarePaymentDataModel paymentDataModel, CareCityDetail mCareCityDetail) {
        Intent intent = new Intent(context, PaymentChoiceCheepCareActivity.class);
        intent.putExtra(Utility.Extra.DATA, cartDetails);
        intent.putExtra(Utility.Extra.DATA_2, GsonUtility.getJsonStringFromObject(paymentDataModel));
        intent.putExtra(Utility.Extra.DATA_3, GsonUtility.getJsonStringFromObject(mCareCityDetail));
        intent.putExtra(Utility.Extra.PAYMENT_VIEW, PAYMENT_FOR_SUBSCRIPTION);
        context.startActivity(intent);
    }

    /*
    this is for payment of subscribed task with paid service payment or addtional charge of non working hours and excess limit of task bookinf
    also if user selects non-subscribed address then user has to pay for free services as well
     */
    public static void newInstance(Context context, SubscribedTaskDetailModel subscribedTaskDetailModel) {
        Intent intent = new Intent(context, PaymentChoiceCheepCareActivity.class);
        intent.putExtra(Utility.Extra.DATA, GsonUtility.getJsonStringFromObject(subscribedTaskDetailModel));
        intent.putExtra(Utility.Extra.PAYMENT_VIEW, PAYMENT_FOR_TASK_CREATION);
        context.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_payment_choice_cheep_care);

        initiateUI();
        setListeners();

        // Register and Event Buss to get callback from various payment gateways
        EventBus.getDefault().register(this);
    }

    @Override
    protected void initiateUI() {

        paymentFor = getIntent().getStringExtra(Utility.Extra.PAYMENT_VIEW);
        if (paymentFor.equalsIgnoreCase(PAYMENT_FOR_SUBSCRIPTION)) {
            cartDetail = getIntent().getStringExtra(Utility.Extra.DATA);
            paymentDataModel = (CheepCarePaymentDataModel) GsonUtility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA_2),
                    CheepCarePaymentDataModel.class);
            careCityDetail = (CareCityDetail) GsonUtility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA_3), CareCityDetail.class);
            payableAmount = paymentDataModel.payableAmount;
            LogUtils.LOGE(TAG, "initiateUI: paymentDataModel \n============\n" + paymentDataModel);
        } else {
            subscribedTaskDetailModel = (SubscribedTaskDetailModel) GsonUtility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA), SubscribedTaskDetailModel.class);
            payableAmount = subscribedTaskDetailModel.total;
        }
        setupActionbar();
    }

    private void setupActionbar() {


        mBinding.textTitle.setText(getString(R.string.label_please_pay_x, Utility.getQuotePriceFormatter(String.valueOf(payableAmount))));
        setSupportActionBar(mBinding.toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(Utility.EMPTY_STRING);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void setListeners() {
        mBinding.rlCard.setOnClickListener(this);
        mBinding.rlNetbanking.setOnClickListener(this);
        mBinding.rlPaytm.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_card:
            case R.id.rl_netbanking:
                paymentMethod = NetworkUtility.PAYMENT_METHOD_TYPE.PAYU;
                doPaymentOfNetBanking();
                break;
            case R.id.rl_paytm:
                paymentMethod = NetworkUtility.PAYMENT_METHOD_TYPE.PAYTM;
                doPaymentOfPaytm();

                break;

            case R.id.rl_cash_payment:

                break;
        }
    }


    ///////////////////////////////////////////////////////    Cheep care task payment METHOD [START] ///////////////////////////////////////////////////////

    /**
     * Used for payment
     */
    private void generateHashForCheepCarePackagePurchase() {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mBinding.getRoot());
            return;
        }

        showProgressDialog();

        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();

        //Add Params
        // = new HashMap<String, Object>();
        String startDateTime;
        if (paymentFor.equalsIgnoreCase(PAYMENT_FOR_SUBSCRIPTION)) {
            startDateTime = Utility.EMPTY_STRING;
        } else {
            startDateTime = subscribedTaskDetailModel.startDateTime;
        }
        mTransactionParams = HDFCPaymentUtility.getPaymentTransactionFieldsForCheepCare(
                PreferenceUtility.getInstance(this).getFCMRegID(),
                userDetails,
                String.valueOf(payableAmount),
                startDateTime);

        new HDFCPaymentUtility.AsyncFetchEncryptedString(new HDFCPaymentUtility.EncryptTransactionParamsListener() {
            @Override
            public void onPostOfEncryption(String encryptedData) {
                UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
                //Add Header parameters
                Map<String, String> mHeaderParams = new HashMap<>();
                mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
                mHeaderParams.put(NetworkUtility.TAGS.USER_ID, userDetails.userID);

                Map<String, Object> mFinalParams = new HashMap<>();
                mFinalParams.put(NetworkUtility.TAGS.DATA, encryptedData);

//                getPaymentUrl(userDetails, isForAdditionalQuote);
                String url;
                // if payment is done using insta feature then
                // post data will be generated like strategic partner feature
                // call startegic generate hash for payment
                url = NetworkUtility.WS.GENERATE_HASH_FOR_CHEEP_CARE;
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
                            onSuccessOfPayment(getString(R.string.message_payment_bypassed));
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

                            HDFCPaymentGatewayActivity.newInstance(PaymentChoiceCheepCareActivity.this,
                                    HDFCPaymentUtility.getPaymentUrl(mTransactionParams, jsonObject.optString(NetworkUtility.TAGS.HASH_STRING)),
                                    Utility.REQUEST_START_PAYMENT_CHEEP_CARE);


                        }
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mBinding.getRoot());
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

    private void onSuccessOfPayment(String paymentLog) {
        if (paymentFor.equalsIgnoreCase(PAYMENT_FOR_SUBSCRIPTION)) {
            callCreateCheepCarePackageWS(paymentLog);
        } else {
            callCreateSubscribedTaskWS(paymentLog);
        }
    }

    private void callCreateSubscribedTaskWS(String paymentLog) {

        subscribedTaskDetailModel.paymentMethod = paymentMethod;
        subscribedTaskDetailModel.paymentLog = paymentLog;
        subscribedTaskDetailModel.paybleAmount = String.valueOf(payableAmount);

        WebCallClass.createCheepCareTask(mContext, subscribedTaskDetailModel, mCommonResponseListener, new WebCallClass.SuccessOfTaskCreationResponseListener() {
            @Override
            public void onSuccessOfTaskCreate(String startdateTimeTimeStamp) {
                LogUtils.LOGE(TAG, "onSuccessOfTaskCreate: ");
                hideProgressDialog();
                subscribedTaskDetailModel.startDateTime = startdateTimeTimeStamp;
                String datetime = "";
                if (!TextUtils.isEmpty(subscribedTaskDetailModel.startDateTime)) {
                    datetime = CalendarUtility.getDate(Long.parseLong(subscribedTaskDetailModel.startDateTime), Utility.DATE_FORMAT_DD_MMMM) + getString(R.string.label_between) + CalendarUtility.get2HourTimeSlots(subscribedTaskDetailModel.startDateTime);
                }


                TaskConfirmedCCInstaBookDialog taskConfirmedCCInstaBookDialog = TaskConfirmedCCInstaBookDialog.newInstance(new TaskConfirmedCCInstaBookDialog.TaskConfirmActionListener() {
                    @Override
                    public void onAcknowledgementAccepted() {
                        MessageEvent messageEvent = new MessageEvent();
                        messageEvent.BROADCAST_ACTION = Utility.BROADCAST_TYPE.SUBSCRIBED_TASK_CREATE_SUCCESSFULLY;
                        EventBus.getDefault().post(messageEvent);
                        finish();
                    }

                    @Override
                    public void rescheduleTask() {

                    }
                }, false, datetime);
                taskConfirmedCCInstaBookDialog.show(getSupportFragmentManager(), TaskConfirmedCCInstaBookDialog.TAG);
            }
        });
    }

    private final WebCallClass.CommonResponseListener mCommonResponseListener =
            new WebCallClass.CommonResponseListener() {
                @Override
                public void volleyError(VolleyError error) {
                    Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");
                    hideProgressDialog();
                    Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
                }

                @Override
                public void showSpecificMessage(String message) {
                    hideProgressDialog();
                    // Show message
                    Utility.showSnackBar(message, mBinding.getRoot());
                }

                @Override
                public void forceLogout() {
                    hideProgressDialog();
                    finish();
                }
            };

    private void callCreateCheepCarePackageWS(String paymentLog) {
        LogUtils.LOGE(TAG, "callCreateCheepCarePackageWS: paymentLog \n" + paymentLog);


        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mBinding.getRoot());
            return;
        }
        showProgressDialog();

        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
        //Add Header parameters

        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, userDetails.userID);

        //Add Params
        Map<String, String> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.TOTAL_AMOUNT, String.valueOf(paymentDataModel.totalAmount));
        mParams.put(NetworkUtility.TAGS.PROMOCODE, paymentDataModel.promocode);
        mParams.put(NetworkUtility.TAGS.PROMOCODE_PRICE, String.valueOf(paymentDataModel.promocodePrice));
        mParams.put(NetworkUtility.TAGS.PAYABLE_AMOUNT, String.valueOf(paymentDataModel.payableAmount));
        mParams.put(NetworkUtility.TAGS.TAX_AMOUNT, String.valueOf(paymentDataModel.taxAmount));
        mParams.put(NetworkUtility.TAGS.IS_ANNUALLY, String.valueOf(paymentDataModel.isAnnually));
        mParams.put(NetworkUtility.TAGS.CARE_CITY_ID, String.valueOf(careCityDetail.id));
        mParams.put(NetworkUtility.TAGS.PAYMENT_METHOD, paymentMethod);
        mParams.put(NetworkUtility.TAGS.PAYMENT_LOG, paymentLog);
        mParams.put(NetworkUtility.TAGS.DSA_CODE, paymentDataModel.dsaCode);
        mParams.put(NetworkUtility.TAGS.BUNDLE_DISCOUNT_PERCENT, String.valueOf(paymentDataModel.bundlediscountPercent));
        mParams.put(NetworkUtility.TAGS.BUNDLE_DISCOUNT_PRICE, String.valueOf(paymentDataModel.bundlediscountPrice));
        mParams.put(NetworkUtility.TAGS.CART_DETAIL, cartDetail);

        LogUtils.LOGE(TAG, "callBookProAndPayForNormalTaskWS: mParams " + mParams);

        // Url is based on condition if address id is greater then 0 then it means we need to update the existing address
        VolleyNetworkRequest mVolleyNetworkRequestForSPList = new VolleyNetworkRequest(NetworkUtility.WS.PURCHASE_CARE_PACKAGE
                , mCallGenerateHashWSErrorListener
                , mCallCreateCheepCareTaskWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForSPList);
    }

    Response.ErrorListener mCallGenerateHashWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(final VolleyError error) {
            hideProgressDialog();
            LogUtils.LOGD(TAG, "onErrorResponse() called with: error = [" + error + "]");
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Utility.REQUEST_START_PAYMENT_CHEEP_CARE:
//            Toast.makeText(mContext, "OnActivityResult called with resultCode:" + resultCode + ", requestCode:" + requestCode, Toast.LENGTH_SHORT).show();
                if (resultCode == RESULT_OK) {
                    //success
                    if (data != null) {
                        LogUtils.LOGE(TAG, "onActivityResult() called with success: result= [" + data.getStringExtra(Utility.Extra.PAYU_RESPONSE) + "]");
                        onSuccessOfPayment(data.getStringExtra(Utility.Extra.PAYU_RESPONSE));

                    }
                }
                if (resultCode == RESULT_CANCELED) {
                    //failed
                    if (data != null) {
                        LogUtils.LOGE(TAG, "onActivityResult() called with failed: result= [" + data.getStringExtra(Utility.Extra.PAYU_RESPONSE) + "]");
//                        Utility.showSnackBar(getString(R.string.msg_payment_failed), mBinding.getRoot());
                        showPaymentFailedDialog();
                    }
                }
                break;
        }
    }


    Response.ErrorListener mCallErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            callProfileWsforUpdatedAddressList();
            // Close Progressbar
            hideProgressDialog();

            // Show Toast
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
        }
    };
//////////////////////////////////////////////////////////////////    NORMAL TASK PAYMENT METHOD [END] ///////////////////////////////////////////////////////


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
            Volley.getInstance(PaymentChoiceCheepCareActivity.this).getRequestQueue().cancelAll(NetworkUtility.WS.PURCHASE_CARE_PACKAGE);
            EventBus.getDefault().unregister(this);
        } catch (Exception e) {

        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // check paytm access token is still valid and show linked account balance
        UserDetails userDetails = PreferenceUtility.getInstance(PaymentChoiceCheepCareActivity.this).getUserDetails();
        if (userDetails != null && userDetails.mPaytmUserDetail != null) {
//        if (false) {
            try {
                long accessTokenExpiresTimeStamp = Long.parseLong(userDetails.mPaytmUserDetail.accessTokenExpiresTimestamp);
                if (accessTokenExpiresTimeStamp < System.currentTimeMillis()) {
                    // access token has been expired
                    userDetails.mPaytmUserDetail = null;
                    PreferenceUtility.getInstance(PaymentChoiceCheepCareActivity.this).saveUserDetails(userDetails);
                    mBinding.tvPaytmLinkAccount.setText(getString(R.string.label_link_x, getString(R.string.label_account)));
                    mBinding.tvPaytmLinkAccount.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_link_blue, 0, 0, 0);
                    PAYTM_STEP = PAYTM_SEND_OTP;

                } else {
                    // show linked account balace
                    mBinding.tvPaytmLinkAccount.setVisibility(View.GONE);

                    checkBalance(userDetails.mPaytmUserDetail.paytmAccessToken);
                }
            } catch (NumberFormatException e) {
                mBinding.tvPaytmLinkAccount.setText(getString(R.string.label_link_x, getString(R.string.label_account)));
                mBinding.tvPaytmLinkAccount.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_link_blue, 0, 0, 0);
                PAYTM_STEP = PAYTM_SEND_OTP;
            }
        } else {
            mBinding.tvPaytmLinkAccount.setText(getString(R.string.label_link_x, getString(R.string.label_account)));
            mBinding.tvPaytmLinkAccount.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_link_blue, 0, 0, 0);
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
                    onSuccessOfPayment(event.paytmResponse.ResponsePayLoad);
                } else {
//                    Utility.showToast(mContext, getString(R.string.msg_payment_failed));
                    showPaymentFailedDialog();
                }
                break;
            case Utility.BROADCAST_TYPE.PAYMENT_COMPLETED_NEED_TO_REDIRECT_TO_MY_TASK_SCREEN:

                break;
        }

    }
    ///////////////////////////////////////////////////////////Paytm Check Balance API call starts///////////////////////////////////////////////////////////

    private void checkBalance(String mAccessToken) {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mBinding.getRoot());
            return;
        }

        //Show Progress
        mBinding.progress.setVisibility(View.VISIBLE);
        PaytmUtility.checkBalance(mContext, mAccessToken, mCheckBalanceResponseListener);
    }

    double paytmWalletBalance;

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

            boolean isLowBalance;
            if (paymentDataModel != null) {
                isLowBalance = paytmWalletBalance < paymentDataModel.payableAmount;
                paytmPayableAmount = paymentDataModel.payableAmount - paytmWalletBalance;
            } else {
                isLowBalance = paytmWalletBalance < subscribedTaskDetailModel.total;
                paytmPayableAmount = subscribedTaskDetailModel.total - paytmWalletBalance;
            }
            mBinding.tvPaytmBalance.setVisibility(View.VISIBLE);

            mBinding.tvPaytmBalance.setText("(" + getString(R.string.rupee_symbol_x, String.valueOf(paytmWalletBalance)) + ")");
            mBinding.tvPaytmLinkAccount.setVisibility(View.VISIBLE);
            mBinding.tvPaytmLinkAccount.setText(Utility.EMPTY_STRING);
            mBinding.tvPaytmLinkAccount.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_right_arrow_blue, 0, 0, 0);
            if (isLowBalance) {
//            BTN_WHICH = BTN_IS_ADD_AMOUNT;
                //TODO: add amount
                mBinding.tvLowBalancePaytm.setVisibility(View.VISIBLE);
                mBinding.tvLowBalancePaytm.setText("Low balance. You need " +
                        getString(R.string.rupee_symbol_x, Utility.getQuotePriceFormatter(String.valueOf(paytmPayableAmount))) /*+ "..."*/);
                PAYTM_STEP = PAYTM_ADD_MONEY;

            } else {
//            BTN_WHICH = BTN_IS_CONFIRM;
                //TODO: withdraw money
                PAYTM_STEP = PAYTM_WITHDRAW;
            }

            mBinding.progress.setVisibility(View.GONE);

        }


        //This method is called when access token expires early due to some reason and we need to do whole OAuth process again
        @Override
        public void paytmInvalidAuthorization() {
            //TODO: implement that if accessToken is valid i.e. 1 month is not due directly call checkBalance API.
            mBinding.progress.setVisibility(View.GONE);
            mBinding.tvPaytmLinkAccount.setVisibility(View.VISIBLE);
            mBinding.tvPaytmLinkAccount.setText(getString(R.string.label_link_x, getString(R.string.label_account)));
            mBinding.tvPaytmLinkAccount.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_link_blue, 0, 0, 0);
            PAYTM_STEP = PAYTM_SEND_OTP;
        }

        @Override
        public void showGeneralizedErrorMessage() {
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
            mBinding.progress.setVisibility(View.GONE);
        }

        @Override
        public void paytmInvalidMobileNumber() {
            mBinding.progress.setVisibility(View.GONE);
            Utility.showSnackBar(getString(R.string.validate_phone_number), mBinding.getRoot());
        }

        @Override
        public void paytmAccountBlocked() {
            mBinding.progress.setVisibility(View.GONE);
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
        }

        @Override
        public void volleyError() {
            mBinding.progress.setVisibility(View.GONE);
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
        }
    };


    Response.Listener mCallCreateCheepCareTaskWSResponseListener = new Response.Listener() {
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
                        ManageSubscriptionActivity.newInstance(PaymentChoiceCheepCareActivity.this, careCityDetail
                                , false, Utility.EMPTY_STRING);
                        MessageEvent messageEvent = new MessageEvent();
                        messageEvent.id = careCityDetail.id;
                        messageEvent.BROADCAST_ACTION = Utility.BROADCAST_TYPE.PACKAGE_SUBSCRIBED_SUCCESSFULLY;

                        EventBus.getDefault().post(messageEvent);
                        callProfileWsforUpdatedAddressList();

                        finish();


                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
                        callProfileWsforUpdatedAddressList();
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mBinding.getRoot());
                        callProfileWsforUpdatedAddressList();
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
                mCallErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }


        }
    };

    private void callProfileWsforUpdatedAddressList() {
        WebCallClass.getProfileDetail(mContext, new WebCallClass.CommonResponseListener() {
            @Override
            public void volleyError(VolleyError error) {

            }

            @Override
            public void showSpecificMessage(String message) {

            }

            @Override
            public void forceLogout() {

            }
        }, new WebCallClass.GetProfileDetailResponseListener() {
            @Override
            public void getUserDetails(UserDetails userDetails, JSONArray jsonEmergencyContacts, ArrayList<AddressModel> addressList) {

            }
        });
    }


    private void showPaymentFailedDialog() {
        LogUtils.LOGE(TAG, "showPaymentFailedDialog: ");
        PaymentFailedDialog paymentFailedDialog = PaymentFailedDialog.newInstance(
                new AcknowledgementInteractionListener() {

                    @Override
                    public void onAcknowledgementAccepted() {
                        // Finish the current activity
                        LogUtils.LOGE(TAG, "show: ------------------ ");

//                        if (paymentMethod.equalsIgnoreCase(NetworkUtility.PAYMENT_METHOD_TYPE.PAYU)) {
//                            doPaymentOfNetBanking();
//                        } else {
//                            doPaymentOfPaytm();
//                        }
                    }
                });
        paymentFailedDialog.setCancelable(true);
        paymentFailedDialog.show(getSupportFragmentManager(), PaymentFailedDialog.TAG);
    }

    private void doPaymentOfPaytm() {

        paymentMethod = NetworkUtility.PAYMENT_METHOD_TYPE.PAYTM;
        UserDetails userDetails = PreferenceUtility.getInstance(PaymentChoiceCheepCareActivity.this).getUserDetails();
        UserDetails.PaytmUserDetail paytmUserDetail = userDetails.mPaytmUserDetail;

        switch (PAYTM_STEP) {
            case PAYTM_SEND_OTP:
                SendOtpActivity.newInstance(mContext, true, String.valueOf(payableAmount));
                break;
            case PAYTM_ADD_MONEY:
                AddMoneyActivity.newInstance(mContext, String.valueOf(payableAmount), paytmPayableAmount, paytmUserDetail.paytmAccessToken,
                        paytmUserDetail.paytmphoneNumber, paytmUserDetail.paytmCustId, paytmWalletBalance);
                break;
            case PAYTM_WITHDRAW:
                WithdrawMoneyActivity.newInstance(mContext, String.valueOf(payableAmount), paytmPayableAmount, paytmUserDetail.paytmAccessToken,
                        paytmUserDetail.paytmphoneNumber, paytmUserDetail.paytmCustId, paytmWalletBalance, true);
                break;
        }
    }

    private void doPaymentOfNetBanking() {
        paymentMethod = NetworkUtility.PAYMENT_METHOD_TYPE.PAYU;
        generateHashForCheepCarePackagePurchase();
    }


}