package com.cheep.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.appsflyer.AppsFlyerLib;
import com.cheep.BootstrapConstant;
import com.cheep.BuildConfig;
import com.cheep.R;
import com.cheep.cheepcare.dialogs.TaskConfirmedCCInstaBookDialog;
import com.cheep.cheepcare.model.AdminSettingModel;
import com.cheep.cheepcare.model.SubscribedTaskDetailModel;
import com.cheep.databinding.ActivityPaymentChoiceBinding;
import com.cheep.dialogs.AcknowledgementDialogWithProfilePic;
import com.cheep.dialogs.AcknowledgementInteractionListener;
import com.cheep.dialogs.OutOfOfficeHoursDialog;
import com.cheep.dialogs.PayByCashDialog;
import com.cheep.dialogs.UrgentBookingDialog;
import com.cheep.fragment.BaseFragment;
import com.cheep.fragment.EnterTaskDetailFragment;
import com.cheep.model.AddressModel;
import com.cheep.model.MessageEvent;
import com.cheep.model.ProviderModel;
import com.cheep.model.TaskDetailModel;
import com.cheep.model.UserDetails;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.strategicpartner.model.QueAnsModel;
import com.cheep.utils.CalendarUtility;
import com.cheep.utils.GsonUtility;
import com.cheep.utils.HDFCPaymentUtility;
import com.cheep.utils.LogUtils;
import com.cheep.utils.PaytmUtility;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.SuperCalendar;
import com.cheep.utils.Utility;
import com.cheep.utils.WebCallClass;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static com.cheep.network.NetworkUtility.PAYTM.PARAMETERS.orderId;
import static com.cheep.network.NetworkUtility.PAYTM.PARAMETERS.response;


public class PaymentChoiceActivity extends BaseAppCompatActivity implements View.OnClickListener {

    private static final String TAG = LogUtils.makeLogTag(PaymentChoiceActivity.class);
    private ActivityPaymentChoiceBinding mBinding;
    private ProviderModel providerModel;
    private TaskDetailModel taskDetailModel;
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
    private double quoteAmount;
    double paytmWalletBalance;
    private double payableAmountForPaytm = 0;
    private double payableAmountForTask = 0;
    private SubscribedTaskDetailModel subscribedTaskDetailModel;
    boolean isSubscribedTask = false;
    private SuperCalendar startDateTimeSuperCalendar = SuperCalendar.getInstance();
    private SuperCalendar superCalendar;
    private String additionalChargeReason = Utility.EMPTY_STRING;
    private UrgentBookingDialog ugent_dialog;

    /**
     * this is for payment for pending amount or pay later task
     *
     * @param context         context of activity
     * @param taskDetailModel task detail model class
     */
    public static void newInstance(Context context, TaskDetailModel taskDetailModel) {
        Intent intent = new Intent(context, PaymentChoiceActivity.class);
        intent.putExtra(Utility.Extra.DATA, GsonUtility.getJsonStringFromObject(taskDetailModel));
        intent.putExtra(Utility.Extra.IS_PAY_NOW, false);
        context.startActivity(intent);
    }

    /**
     * this is for payment for subscribed cheep care task
     *
     * @param context                   context of activity
     * @param subscribedTaskDetailModel task detail model class
     */
    public static void newInstance(Context context, SubscribedTaskDetailModel subscribedTaskDetailModel) {
        Intent intent = new Intent(context, PaymentChoiceActivity.class);
        intent.putExtra(Utility.Extra.DATA_2, GsonUtility.getJsonStringFromObject(subscribedTaskDetailModel));
        context.startActivity(intent);
    }

    /**
     * When user selects pay now for normal & insta task payment
     *
     * @param context               context of activity
     * @param taskDetailModel       task detail model class
     * @param providerModel         pro detail model class
     * @param mSelectedAddressModel selected address by user for task booking
     */
    public static void newInstance(Context context, TaskDetailModel taskDetailModel, ProviderModel providerModel, String total, String payableAmount, AddressModel mSelectedAddressModel) {
        Intent intent = new Intent(context, PaymentChoiceActivity.class);
        intent.putExtra(Utility.Extra.DATA, GsonUtility.getJsonStringFromObject(taskDetailModel));
        intent.putExtra(Utility.Extra.DATA_2, GsonUtility.getJsonStringFromObject(providerModel));
        intent.putExtra(Utility.Extra.PAYABLE_AMOUNT, payableAmount);
        intent.putExtra(Utility.Extra.QUOTE_AMOUNT, total);
        intent.putExtra(Utility.Extra.SELECTED_ADDRESS_MODEL, GsonUtility.getJsonStringFromObject(mSelectedAddressModel));
        intent.putExtra(Utility.Extra.IS_PAY_NOW, true);
        context.startActivity(intent);
    }

    /**
     * hen user selects pay now for  strategic partner payment
     *
     * @param baseFragment          startegic partner third fragment
     * @param taskDetailModel       task detail model class
     * @param mSelectedAddressModel selected address by user for task booking
     */
    public static void newInstance(BaseFragment baseFragment, TaskDetailModel taskDetailModel, AddressModel mSelectedAddressModel) {
        Intent intent = new Intent(baseFragment.getActivity(), PaymentChoiceActivity.class);
        intent.putExtra(Utility.Extra.DATA, GsonUtility.getJsonStringFromObject(taskDetailModel));
        intent.putExtra(Utility.Extra.IS_PAY_NOW, true);
        intent.putExtra(Utility.Extra.SELECTED_ADDRESS_MODEL, GsonUtility.getJsonStringFromObject(mSelectedAddressModel));
        baseFragment.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_payment_choice);
        initiateUI();
        setListeners();

        // Register and Event Buss to get callback from various payment gateways
        EventBus.getDefault().register(this);
    }

    @Override
    protected void initiateUI() {
        // TODO: Changes are per new flow pay now/later: 16/11/17

        if (getIntent().hasExtra(Utility.Extra.DATA)) {
            isSubscribedTask = false;
            taskDetailModel = (TaskDetailModel) GsonUtility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA), TaskDetailModel.class);
            if (getIntent().hasExtra(Utility.Extra.DATA_2)) {
                providerModel = (ProviderModel) GsonUtility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA_2), ProviderModel.class);
            } else {
                providerModel = taskDetailModel.selectedProvider;
            }
            if (getIntent().hasExtra(Utility.Extra.SELECTED_ADDRESS_MODEL)) {
                mSelectedAddressModel = (AddressModel) GsonUtility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.SELECTED_ADDRESS_MODEL), AddressModel.class);
            }

            if (getIntent().hasExtra(Utility.Extra.QUOTE_AMOUNT)) {
                payableAmountForTask = Double.parseDouble(getIntent().getStringExtra(Utility.Extra.PAYABLE_AMOUNT));
                quoteAmount = Double.parseDouble(getIntent().getStringExtra(Utility.Extra.QUOTE_AMOUNT));
            }
            isPayNow = getIntent().getBooleanExtra(Utility.Extra.IS_PAY_NOW, false);

            if (taskDetailModel != null && taskDetailModel.taskStatus.equalsIgnoreCase(Utility.TASK_STATUS.PENDING))
                mBinding.llCashPayment.setVisibility(View.GONE);
            else
                mBinding.llCashPayment.setVisibility(View.VISIBLE);

            mBinding.tvPaytmLinkAccount.setText(getString(R.string.label_link_x, getString(R.string.label_account)));
        } else {
            isSubscribedTask = true;
            subscribedTaskDetailModel = (SubscribedTaskDetailModel) GsonUtility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA_2), SubscribedTaskDetailModel.class);
            mBinding.llCashPayment.setVisibility(View.GONE);
//            payableAmount = subscribedTaskDetailModel.total;
        }
        setupActionbar();

    }

    private void setupActionbar() {

        // set final payment amount which user going to pay

        if (isSubscribedTask) {
            amount = String.valueOf(subscribedTaskDetailModel.total);
        } else if (isPayNow) {
            if (taskDetailModel.taskType.equalsIgnoreCase(Utility.TASK_TYPE.STRATEGIC)) {
                amount = Utility.getQuotePriceFormatter(taskDetailModel.taskPaidAmount);
            } /*else if (isAdditional != 0) {
                amount = Utility.getQuotePriceFormatter(taskDetailModel.additionalQuoteAmount);
            } */ else {
                amount = String.valueOf(payableAmountForTask);
            }
        } else {
            amount = Utility.getQuotePriceFormatter(taskDetailModel.taskTotalPendingAmount);
        }
        mBinding.textTitle.setText(getString(R.string.label_please_pay_x, amount));


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
        mBinding.rlCashPayment.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_card:
            case R.id.rl_netbanking:
                paymentMethod = NetworkUtility.PAYMENT_METHOD_TYPE.PAYU;
                if (isSubscribedTask) {
                    generateHashForCheepCarePackagePurchase();
                } else if (taskDetailModel.taskType.equalsIgnoreCase(Utility.TASK_TYPE.STRATEGIC)) {
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
                        SendOtpActivity.newInstance(mContext, true, amount, false);
                        break;
                    case PAYTM_ADD_MONEY:
                        AddMoneyActivity.newInstance(mContext, amount, payableAmountForPaytm, paytmUserDetail.paytmAccessToken,
                                paytmUserDetail.paytmphoneNumber, paytmUserDetail.paytmCustId, paytmWalletBalance);
                        break;
                    case PAYTM_WITHDRAW:
                        WithdrawMoneyActivity.newInstance(mContext, amount, payableAmountForPaytm, paytmUserDetail.paytmAccessToken,
                                paytmUserDetail.paytmphoneNumber, paytmUserDetail.paytmCustId, paytmWalletBalance, true);
                        break;
                }

                break;

            case R.id.rl_cash_payment:
                paymentMethod = NetworkUtility.PAYMENT_METHOD_TYPE.COD;

                String proName = taskDetailModel.taskType.equalsIgnoreCase(Utility.TASK_TYPE.STRATEGIC) ? taskDetailModel.categoryModel.catName : providerModel.userName;
                PayByCashDialog payByCashDialog = PayByCashDialog.newInstance(proName, amount, new PayByCashDialog.PayByCashDoneListener() {
                    @Override
                    public void onDoneClick() {
                        onSuccessOfAnyPaymentMode(Utility.EMPTY_STRING);
                    }
                });
                payByCashDialog.show(getSupportFragmentManager(), PayByCashDialog.TAG);

                break;
        }
    }

    private void onSuccessOfAnyPaymentMode(String paymentLog) {
        if (isSubscribedTask) {
            callCreateSubscribedTaskWS(paymentLog);
        } else if (isPayNow) {

            callCreateInstaTaskBooking(paymentLog);
            // callBookProAndPayForNormalTaskWS(paymentLog);
//            else if (taskDetailModel.taskType.equalsIgnoreCase(Utility.TASK_TYPE.INSTA_BOOK)) {
//                callCreateInstaTaskBooking(paymentLog);
//            } else {
//                callCreateStrategicPartnerTaskWS(paymentLog);
//            }
        } else
            callCreateInstaTaskBooking(paymentLog);
        //callPayTaskPaymentWS(paymentLog);
    }


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
        mTransactionParams = HDFCPaymentUtility.getPaymentTransactionFieldsForCheepCare(
                PreferenceUtility.getInstance(this).getFCMRegID(),
                userDetails,
                String.valueOf(subscribedTaskDetailModel.total),
                subscribedTaskDetailModel.startDateTime);

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
                        , mCallGenerateHashForCCWSResponseListener
                        , mHeaderParams
                        , mFinalParams
                        , null);
                Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForSPList);


            }
        }).execute(new JSONObject(mTransactionParams).toString());


    }

    Response.Listener mCallGenerateHashForCCWSResponseListener = new Response.Listener() {
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
                            onSuccessOfAnyPaymentMode(getString(R.string.message_payment_bypassed));
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

                            HDFCPaymentGatewayActivity.newInstance(PaymentChoiceActivity.this,
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
///////////////////////////////////////////////////////    NORMAL TASK PAYMENT METHOD [START] ///////////////////////////////////////////////////////

    /**
     * Used for payment
     */
    private void generateHashForNormalTask() {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mBinding.getRoot());
            return;
        }

        showProgressDialog();

        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();

        //Add Params
        // = new HashMap<String, Object>();

        mTransactionParams = HDFCPaymentUtility.getPaymentTransactionFieldsForNormalTask(
                PreferenceUtility.getInstance(this).getFCMRegID(), userDetails, taskDetailModel, String.valueOf(payableAmountForTask), providerModel, isPayNow);

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
                //url = taskDetailModel.taskType.equalsIgnoreCase(Utility.TASK_TYPE.NORMAL) ? NetworkUtility.WS.GET_PAYMENT_HASH : NetworkUtility.WS.GET_PAYMENT_HASH_FOR_STRATEGIC_PARTNER;
                url = NetworkUtility.WS.GET_PAYMENT_HASH_FOR_STRATEGIC_PARTNER;
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
                            // Direct bypass the things
//                            updatePaymentStatus(true, getString(R.string.message_payment_bypassed), false);
                            onSuccessOfAnyPaymentMode(getString(R.string.message_payment_bypassed));
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
            case Utility.REQUEST_START_PAYMENT:
//            Toast.makeText(mContext, "OnActivityResult called with resultCode:" + resultCode + ", requestCode:" + requestCode, Toast.LENGTH_SHORT).show();
                if (resultCode == RESULT_OK) {
                    //success
                    if (data != null) {
                        LogUtils.LOGE(TAG, "onActivityResult() called with success: result= [" + data.getStringExtra(Utility.Extra.PAYU_RESPONSE) + "]");
                        callCreateInstaTaskBooking(Utility.Extra.PAYU_RESPONSE);
                        // onSuccessOfAnyPaymentMode(data.getStringExtra(Utility.Extra.PAYU_RESPONSE));
                    }
                }
                if (resultCode == RESULT_CANCELED) {
                    //failed
                    if (data != null) {
                        LogUtils.LOGE(TAG, "onActivityResult() called with failed: result= [" + data.getStringExtra(Utility.Extra.PAYU_RESPONSE) + "]");
                        Utility.showSnackBar(getString(R.string.msg_payment_failed), mBinding.getRoot());
                    }
                }
                break;
        }
    }

    private void callPayTaskPaymentWS(String paymentLog) {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mBinding.getRoot());
            return;
        }
        showProgressDialog();
        String txnId;
        if (mTransactionParams == null)

            txnId = Utility.getUniqueTransactionId();
        else
            txnId = mTransactionParams.get(HDFCPaymentUtility.TXN_ID);
        WebCallClass.payPendingTaskPaymentWS(this, txnId, paymentLog, paymentMethod, taskDetailModel, errorListener, new WebCallClass.PayPendingTaskPaymentListener() {
            @Override
            public void onSuccessOfPendingTaskPaid(String taskStatus) {
                // Utility.showSnackBar(getString(R.string.msg_thanks_for_confirmation), mBinding.getRoot());
                /*
                Update the UI Accordingly.
                */
                //Refresh UI for Paid status
                // Notify the Home Screen to check for ongoing task counter.
                if (taskDetailModel.taskStatus.equalsIgnoreCase(Utility.TASK_STATUS.COMPLETION_REQUEST)) {
                    callCompleteTaskWS(Utility.TASK_STATUS.COMPLETION_CONFIRM);
                } else {
                    MessageEvent messageEvent = new MessageEvent();
                    messageEvent.taskStatus = taskStatus;
                    messageEvent.BROADCAST_ACTION = Utility.BROADCAST_TYPE.TASK_PAID_SUCCESSFULLY;
                    EventBus.getDefault().post(messageEvent);
                    finish();
                }
            }
        });
       /* UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
        //Add Header parameters

        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, userDetails.userID);

        //Add Params
        Map<String, Object> mParams = new HashMap<>();
        if (mTransactionParams == null)
            mParams.put(NetworkUtility.TAGS.TRANSACTION_ID, Utility.getUniqueTransactionId());
        else
            mParams.put(NetworkUtility.TAGS.TRANSACTION_ID, mTransactionParams.get(HDFCPaymentUtility.TXN_ID));
        mParams.put(NetworkUtility.TAGS.PAYABLE_AMOUNT, taskDetailModel.taskTotalPendingAmount);
        mParams.put(NetworkUtility.TAGS.PAYMENT_LOG, paymentLog);
        mParams.put(NetworkUtility.TAGS.PAYMENT_STATUS, Utility.PAYMENT_STATUS.COMPLETED);
        mParams.put(NetworkUtility.TAGS.PAYMENT_METHOD, paymentMethod);
        mParams.put(NetworkUtility.TAGS.TASK_ID, taskDetailModel.taskId);

        mParams.put(NetworkUtility.TAGS.PRO_PAYMENT_STATUS, taskDetailModel.paymentSummaryModel.proPaymentStatus);
        mParams.put(NetworkUtility.TAGS.ADDITIONAL_PENDING_AMOUNT,
                TextUtils.isEmpty(taskDetailModel.paymentSummaryModel.additionalPendingAmount)
                        ? Utility.ZERO_STRING :
                        taskDetailModel.paymentSummaryModel.additionalPendingAmount);

        // Url is based on condition if address id is greater then 0 then it means we need to update the existing address
        VolleyNetworkRequest mVolleyNetworkRequestForSPList = new VolleyNetworkRequest(NetworkUtility.WS.PAY_TASK_PAYMENT
                , mCallCompleteTaskWSErrorListener
                , mCallPayTaskPaymentWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForSPList);*/
    }


    /* Response.Listener mCallPayTaskPaymentWSResponseListener = new Response.Listener() {
         @Override
         public void onResponse(Object response) {
             hideProgressDialog();
             String strResponse = (String) response;
             try {
                 JSONObject jsonObject = new JSONObject(strResponse);
                 Log.i(TAG, "onResponse: " + jsonObject.toString());
                 int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                 switch (statusCode) {
                     case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
 //                        Utility.showSnackBar(getString(R.string.msg_thanks_for_confirmation), mBinding.getRoot());

                                 *//*
                                  Update the UI Accordingly.
                                 *//*

                        //Refresh UI for Paid status

                        // Notify the Home Screen to check for ongoing task counter.
                        if (taskDetailModel.taskStatus.equalsIgnoreCase(Utility.TASK_STATUS.COMPLETION_REQUEST)) {
                            callCompleteTaskWS(Utility.TASK_STATUS.COMPLETION_CONFIRM);
                        } else {
                            MessageEvent messageEvent = new MessageEvent();
                            messageEvent.taskStatus = (jsonObject.optJSONObject(NetworkUtility.TAGS.DATA)).optString(NetworkUtility.TAGS.TASK_STATUS);
                            messageEvent.BROADCAST_ACTION = Utility.BROADCAST_TYPE.TASK_PAID_SUCCESSFULLY;
                            EventBus.getDefault().post(messageEvent);
                            finish();
                        }
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        String error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
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
                mCallCompleteTaskWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
        }
    };
*/
    Response.ErrorListener mCallCompleteTaskWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            hideProgressDialog();

            // Show Toast
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
        }
    };
//////////////////////////////////////////////////////////////////    NORMAL TASK PAYMENT METHOD [END] ///////////////////////////////////////////////////////

    /**
     * Used for payment
     */
    private void generateHashForStrategicPartner() {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mBinding.getRoot());
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
                isPayNow ? taskDetailModel.taskPaidAmount : taskDetailModel.taskTotalPendingAmount,
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
                mHeaderParams.put(NetworkUtility.TAGS.USER_ID, userDetails.userID);

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
            Volley.getInstance(PaymentChoiceActivity.this).getRequestQueue().cancelAll(NetworkUtility.WS.GET_PAYMENT_HASH_FOR_STRATEGIC_PARTNER);
            Volley.getInstance(PaymentChoiceActivity.this).getRequestQueue().cancelAll(NetworkUtility.WS.GENERATE_HASH_FOR_CHEEP_CARE);
            Volley.getInstance(PaymentChoiceActivity.this).getRequestQueue().cancelAll(NetworkUtility.WS.GET_PAYMENT_HASH);
            Volley.getInstance(PaymentChoiceActivity.this).getRequestQueue().cancelAll(NetworkUtility.WS.CHANGE_TASK_STATUS);
            Volley.getInstance(PaymentChoiceActivity.this).getRequestQueue().cancelAll(NetworkUtility.WS.CREATE_TASK);
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
                    // TODO: Need to start the task from here
                    onSuccessOfAnyPaymentMode(event.paytmResponse.ResponsePayLoad);
                } else {
                    Utility.showToast(mContext, getString(R.string.msg_payment_failed));
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
            payableAmountForPaytm = Double.parseDouble(amount) - paytmWalletBalance;
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
                        getString(R.string.rupee_symbol_x, Utility.getQuotePriceFormatter(String.valueOf(payableAmountForPaytm))) /*+ "..."*/);
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

    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////           Book and pay ws         [start]                               /////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////


    private void callBookProAndPayForNormalTaskWS(String paymentLog) {
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
        Map<String, Object> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.SP_USER_ID, providerModel.providerId);
        mParams.put(NetworkUtility.TAGS.TASK_ID, taskDetailModel.taskId);
        mParams.put(NetworkUtility.TAGS.IS_REFER_CODE, taskDetailModel.isReferCode);
        mParams.put(NetworkUtility.TAGS.PAYABLE_AMOUNT, payableAmountForTask);
        mParams.put(NetworkUtility.TAGS.USED_WALLET_BALANCE, taskDetailModel.usedWalletAmount);
        LogUtils.LOGE(TAG, "callBookProForNormalTaskWS:payableAmount" + payableAmountForTask);
        LogUtils.LOGE(TAG, "callBookProForNormalTaskWS: quote amount" + quoteAmount);
        mParams.put(NetworkUtility.TAGS.QUOTE_AMOUNT, quoteAmount);

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


        //Add Params
        mTaskCreationParams = new HashMap<>();
        mTaskCreationParams.put(NetworkUtility.TAGS.SP_USER_ID, providerModel.providerId);
        mTaskCreationParams.put(NetworkUtility.TAGS.TASK_ID, taskDetailModel.taskId);
        mTaskCreationParams.put(NetworkUtility.TAGS.IS_REFER_CODE, taskDetailModel.isReferCode);
        mTaskCreationParams.put(NetworkUtility.TAGS.PAYABLE_AMOUNT, providerModel.quotePrice);
        mTaskCreationParams.put(NetworkUtility.TAGS.USED_WALLET_BALANCE, taskDetailModel.usedWalletAmount);
        LogUtils.LOGE(TAG, "callBookProForNormalTaskWS: quote amount" + providerModel.spWithoutGstQuotePrice);
        mTaskCreationParams.put(NetworkUtility.TAGS.QUOTE_AMOUNT, providerModel.spWithoutGstQuotePrice);

        if (!TextUtils.isEmpty(taskDetailModel.cheepCode)) {
            mTaskCreationParams.put(NetworkUtility.TAGS.CHEEPCODE, taskDetailModel.cheepCode);
            mTaskCreationParams.put(NetworkUtility.TAGS.PROMOCODE_PRICE, taskDetailModel.taskDiscountAmount);
        } else {
            mTaskCreationParams.put(NetworkUtility.TAGS.CHEEPCODE, Utility.EMPTY_STRING);
            mTaskCreationParams.put(NetworkUtility.TAGS.PROMOCODE_PRICE, Utility.ZERO_STRING);
        }


        // TODO: Changes are per new flow pay now/later: 16/11/17
        // as per new flow if user selects pay now then book and pay will be done together
        // so setting task book pro params and payment params
        mTaskCreationParams.put(NetworkUtility.TAGS.TRANSACTION_ID, mParams.get(NetworkUtility.TAGS.TRANSACTION_ID));
        mTaskCreationParams.put(NetworkUtility.TAGS.PAYMENT_LOG, paymentLog);
        mTaskCreationParams.put(NetworkUtility.TAGS.PAYMENT_STATUS, Utility.PAYMENT_STATUS.COMPLETED);
        mTaskCreationParams.put(NetworkUtility.TAGS.PAYMENT_METHOD, paymentMethod);
        mTaskCreationParams.put(NetworkUtility.TAGS.TASK_ID, taskDetailModel.taskId);


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
                        if (!TextUtils.isEmpty(taskDetailModel.cheepCode) && taskDetailModel.cheepCode.startsWith(Utility.COUPON_DUNIA_CODE_PREFIX))
                            if (!BuildConfig.BUILD_TYPE.equalsIgnoreCase("release"))
                                AppsFlyerLib.getInstance().trackEvent(mContext, NetworkUtility.TAGS.APPSFLYER_CUSTOM_TRACK_EVENTS.COUPON_DUNIA_TASK_DEBUG, mTaskCreationParams);
                            else
                                AppsFlyerLib.getInstance().trackEvent(mContext, NetworkUtility.TAGS.APPSFLYER_CUSTOM_TRACK_EVENTS.COUPON_DUNIA_TASK_LIVE, mTaskCreationParams);


                        // AS PER new flow pay later task status will be pending
                        if (Utility.TASK_STATUS.PENDING.equalsIgnoreCase(taskStatus)) {
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
//                            String title = mContext.getString(R.string.label_great_choice_x, PreferenceUtility.getInstance(mContext).getUserDetails().userName);
                            String title = mContext.getString(R.string.label_brilliant) + "!";
                            final SuperCalendar superStartDateTimeCalendar = SuperCalendar.getInstance();
                            superStartDateTimeCalendar.setTimeZone(SuperCalendar.SuperTimeZone.GMT.GMT);
                            superStartDateTimeCalendar.setTimeInMillis(Long.parseLong(taskDetailModel.taskStartdate));
                            superStartDateTimeCalendar.setLocaleTimeZone();

                            int onlydate = Integer.parseInt(superStartDateTimeCalendar.format("dd"));
                            String message = CalendarUtility.fetchMessageFromDateOfMonth(mContext, onlydate, superStartDateTimeCalendar, providerModel);

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
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
        }
    };


    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////              Book and pay ws        [end]                               /////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////


    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////              Create Insta task + payment        [start]                 /////////////////
    ////////////////              commented code as this is changed as per new flow          /////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void callCreateInstaTaskBooking(String paymentLog) {

        // Check Internet connection
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mBinding.getRoot());
            return;
        }

        showProgressDialog();

        String txnId;
        if (mTransactionParams == null)
            txnId = Utility.getUniqueTransactionId();
        else
            txnId = mTransactionParams.get(HDFCPaymentUtility.TXN_ID);

        WebCallClass.createInstaBookingTask(PaymentChoiceActivity.this,
                taskDetailModel, mSelectedAddressModel, String.valueOf(quoteAmount), String.valueOf(payableAmountForTask), paymentMethod, paymentLog, txnId, errorListener, new WebCallClass.InstaBookTaskCreationListener() {
                    @Override
                    public void successOfInstaBookTaskCreation() {
                        hideProgressDialog();
                    }
                }, new TaskConfirmedCCInstaBookDialog.TaskConfirmActionListener() {
                    @Override
                    public void onAcknowledgementAccepted() {
                        MessageEvent messageEvent = new MessageEvent();
                        messageEvent.BROADCAST_ACTION = Utility.BROADCAST_TYPE.TASK_PAID_FOR_INSTA_BOOKING;
                        EventBus.getDefault().post(messageEvent);
                        finish();
                    }

                    @Override
                    public void rescheduleTask() {
                        // open time picker


                    }
                });
    }

    private void showDateTimePickerDialog() {
// Get Current Date
        final Calendar c = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(mContext, new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                if (view.isShown()) {
                    Log.d(TAG, "onDateSet() called with: view = [" + view + "], year = [" + year + "], monthOfYear = [" + monthOfYear + "], dayOfMonth = [" + dayOfMonth + "]");
                    startDateTimeSuperCalendar.set(Calendar.YEAR, year);
                    startDateTimeSuperCalendar.set(Calendar.MONTH, monthOfYear);
                    startDateTimeSuperCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    showTimePickerDialog();
                }
            }
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
        superCalendar = SuperCalendar.getInstance();
        datePickerDialog.getDatePicker().setMinDate(superCalendar.getTimeInMillis());
    }


    private void showTimePickerDialog() {
// Get Current Time
        final Calendar c = Calendar.getInstance();
        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(mContext,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        if (view.isShown()) {


                            Log.d(TAG, "onTimeSet() called with: view = [" + view + "], hourOfDay = [" + hourOfDay + "], minute = [" + minute + "]");

                            startDateTimeSuperCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                            startDateTimeSuperCalendar.set(Calendar.MINUTE, minute);

                            superCalendar = SuperCalendar.getInstance();
                            superCalendar.setTimeInMillis(startDateTimeSuperCalendar.getTimeInMillis());
                            superCalendar.setTimeZone(SuperCalendar.SuperTimeZone.GMT.GMT);

                            // Get date-time for next 3 hours
                            SuperCalendar calAfter3Hours = SuperCalendar.getInstance().getNext3HoursTime(false);

//                            TODO: This needs to Be UNCOMMENTED DO NOT FORGET
//                            if (!BuildConfig.BUILD_TYPE.equalsIgnoreCase(Utility.DEBUG)) {
                            AdminSettingModel model;
                            if (taskDetailModel.categoryModel.isSubscribed.equalsIgnoreCase(Utility.BOOLEAN.YES)) {
                                if (!(mSelectedAddressModel.is_subscribe.equalsIgnoreCase(Utility.ADDRESS_SUBSCRIPTION_TYPE.PREMIUM))) {
                                    if (System.currentTimeMillis() < startDateTimeSuperCalendar.getTimeInMillis()) {
                                        if (superCalendar.getTimeInMillis() < calAfter3Hours.getTimeInMillis()) {
                                            String selectedDateTime = startDateTimeSuperCalendar.format(Utility.DATE_FORMAT_DD_MMM)
                                                    + getString(R.string.label_between)
                                                    + CalendarUtility.get2HourTimeSlots(Long.toString(startDateTimeSuperCalendar.getTimeInMillis()));
                                            additionalChargeReason = Utility.ADDITION_CHARGES_DIALOG_TYPE.URGENT_BOOKING;
                                            model = PreferenceUtility.getInstance(mContext).getAdminSettings();
                                            ugent_dialog = UrgentBookingDialog.newInstance(model.additionalChargeForSelectingSpecificTime, new UrgentBookingDialog.UrgentBookingListener() {
                                                @Override
                                                public void onUrgentPayNow() {

                                                }

                                                @Override
                                                public void onUrgentCanWait() {

                                                }
                                            });
                                            ugent_dialog.show(getSupportFragmentManager(), Utility.ADDITION_CHARGES_DIALOG_TYPE.URGENT_BOOKING);
                                            ugent_dialog.setCancelable(false);
                                            return;
                                        }
                                    } else {
                                        additionalChargeReason = Utility.ADDITION_CHARGES_DIALOG_TYPE.NONE;
                                        Utility.showSnackBar(getString(R.string.validate_future_date), mBinding.getRoot());
                                        return;

                                    }
//                            }
                                    try {
                                        if (System.currentTimeMillis() < startDateTimeSuperCalendar.getTimeInMillis()) {
                                            if (isTimeBetweenTwoTime(startDateTimeSuperCalendar.format(Utility.DATE_FORMAT_HH_MM_SS))) {
                                                String selectedDateTime = startDateTimeSuperCalendar.format(Utility.DATE_FORMAT_DD_MMM)
                                                        + getString(R.string.label_between)
                                                        + CalendarUtility.get2HourTimeSlots(Long.toString(startDateTimeSuperCalendar.getTimeInMillis()));
                                                mFragmentEnterTaskDetailBinding.textTaskWhen.setText(selectedDateTime);
                                                mFragmentEnterTaskDetailBinding.textTaskWhen.setVisibility(View.VISIBLE);
                                                updateTaskVerificationFlags();
                                                additionalChargeReason = Utility.ADDITION_CHARGES_DIALOG_TYPE.OUT_OF_OFFICE_HOURS;
                                                out_of_office_dialog = OutOfOfficeHoursDialog.newInstance(model.additionalChargeForSelectingSpecificTime, EnterTaskDetailFragment.this);
                                                out_of_office_dialog.show(getFragmentManager(), Utility.ADDITION_CHARGES_DIALOG_TYPE.OUT_OF_OFFICE_HOURS);
                                                out_of_office_dialog.setCancelable(false);
                                                return;
                                            }
                                        } else {
                                            additionalChargeReason = Utility.ADDITION_CHARGES_DIALOG_TYPE.NONE;
                                            mFragmentEnterTaskDetailBinding.textTaskWhen.setText(Utility.EMPTY_STRING);
                                            mFragmentEnterTaskDetailBinding.textTaskWhen.setVisibility(View.GONE);
                                            Utility.showSnackBar(getString(R.string.validate_future_date), mFragmentEnterTaskDetailBinding.getRoot());
                                            updateTaskVerificationFlags();
                                            return;
                                        }
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    if (System.currentTimeMillis() < startDateTimeSuperCalendar.getTimeInMillis()) {
//                                       String selectedDateTime = startDateTimeSuperCalendar.format(Utility.DATE_FORMAT_DD_MMM)
//                                               + getString(R.string.label_at)
//                                               + startDateTimeSuperCalendar.format(Utility.DATE_FORMAT_HH_MM_AM);

                                        String selectedDateTime = startDateTimeSuperCalendar.format(Utility.DATE_FORMAT_DD_MMM)
                                                + getString(R.string.label_between)
                                                + CalendarUtility.get2HourTimeSlots(Long.toString(startDateTimeSuperCalendar.getTimeInMillis()));
                                        additionalChargeReason = Utility.ADDITION_CHARGES_DIALOG_TYPE.NONE;
                                        mFragmentEnterTaskDetailBinding.textTaskWhen.setText(selectedDateTime);
                                        mFragmentEnterTaskDetailBinding.textTaskWhen.setVisibility(View.VISIBLE);
                                        updateTaskVerificationFlags();
                                    } else {
                                        additionalChargeReason = Utility.ADDITION_CHARGES_DIALOG_TYPE.NONE;
                                        mFragmentEnterTaskDetailBinding.textTaskWhen.setText(Utility.EMPTY_STRING);
                                        mFragmentEnterTaskDetailBinding.textTaskWhen.setVisibility(View.GONE);
                                        Utility.showSnackBar(getString(R.string.validate_future_date), mFragmentEnterTaskDetailBinding.getRoot());
                                        updateTaskVerificationFlags();
                                    }
                                } else {
                                    if (System.currentTimeMillis() < startDateTimeSuperCalendar.getTimeInMillis()) {
                                        String selectedDateTime = startDateTimeSuperCalendar.format(Utility.DATE_FORMAT_DD_MMM)
                                                + getString(R.string.label_between)
                                                + CalendarUtility.get2HourTimeSlots(Long.toString(startDateTimeSuperCalendar.getTimeInMillis()));
                                        additionalChargeReason = Utility.ADDITION_CHARGES_DIALOG_TYPE.NONE;
                                        mFragmentEnterTaskDetailBinding.textTaskWhen.setText(selectedDateTime);
                                        mFragmentEnterTaskDetailBinding.textTaskWhen.setVisibility(View.VISIBLE);
                                        updateTaskVerificationFlags();
                                    } else {
                                        additionalChargeReason = Utility.ADDITION_CHARGES_DIALOG_TYPE.NONE;
                                        mFragmentEnterTaskDetailBinding.textTaskWhen.setText(Utility.EMPTY_STRING);
                                        mFragmentEnterTaskDetailBinding.textTaskWhen.setVisibility(View.GONE);
                                        Utility.showSnackBar(getString(R.string.validate_future_date), mFragmentEnterTaskDetailBinding.getRoot());
                                        updateTaskVerificationFlags();
                                    }
                                }
                            } else {

                                if (System.currentTimeMillis() < startDateTimeSuperCalendar.getTimeInMillis()) {
                                    if (superCalendar.getTimeInMillis() < calAfter3Hours.getTimeInMillis()) {


                                        //    Utility.showSnackBar(getString(R.string.can_only_start_task_after_3_hours, "3"), mFragmentEnterTaskDetailBinding.getRoot());
                                        // mFragmentEnterTaskDetailBinding.textTaskWhen.setText(Utility.EMPTY_STRING);
                                        // mFragmentEnterTaskDetailBinding.textTaskWhen.setVisibility(View.GONE);
                                        String selectedDateTime = startDateTimeSuperCalendar.format(Utility.DATE_FORMAT_DD_MMM)
                                                + getString(R.string.label_between)
                                                + CalendarUtility.get2HourTimeSlots(Long.toString(startDateTimeSuperCalendar.getTimeInMillis()));


                                        mFragmentEnterTaskDetailBinding.textTaskWhen.setText(selectedDateTime);
                                        mFragmentEnterTaskDetailBinding.textTaskWhen.setVisibility(View.VISIBLE);
                                        updateTaskVerificationFlags();
                                        additionalChargeReason = Utility.ADDITION_CHARGES_DIALOG_TYPE.URGENT_BOOKING;
                                        ugent_dialog = UrgentBookingDialog.newInstance(model.additionalChargeForSelectingSpecificTime, EnterTaskDetailFragment.this);
                                        ugent_dialog.show(getFragmentManager(), Utility.ADDITION_CHARGES_DIALOG_TYPE.URGENT_BOOKING);
                                        ugent_dialog.setCancelable(false);
                                        return;
                                    }
                                } else {
                                    additionalChargeReason = Utility.ADDITION_CHARGES_DIALOG_TYPE.NONE;
                                    mFragmentEnterTaskDetailBinding.textTaskWhen.setText(Utility.EMPTY_STRING);
                                    mFragmentEnterTaskDetailBinding.textTaskWhen.setVisibility(View.GONE);
                                    Utility.showSnackBar(getString(R.string.validate_future_date), mFragmentEnterTaskDetailBinding.getRoot());
                                    updateTaskVerificationFlags();
                                    return;

                                }
//                            }
                                try {
                                    if (System.currentTimeMillis() < startDateTimeSuperCalendar.getTimeInMillis()) {
                                        if (isTimeBetweenTwoTime(startDateTimeSuperCalendar.format(Utility.DATE_FORMAT_HH_MM_SS))) {
                                            String selectedDateTime = startDateTimeSuperCalendar.format(Utility.DATE_FORMAT_DD_MMM)
                                                    + getString(R.string.label_between)
                                                    + CalendarUtility.get2HourTimeSlots(Long.toString(startDateTimeSuperCalendar.getTimeInMillis()));
                                            mFragmentEnterTaskDetailBinding.textTaskWhen.setText(selectedDateTime);
                                            mFragmentEnterTaskDetailBinding.textTaskWhen.setVisibility(View.VISIBLE);
                                            updateTaskVerificationFlags();
                                            additionalChargeReason = Utility.ADDITION_CHARGES_DIALOG_TYPE.OUT_OF_OFFICE_HOURS;
                                            out_of_office_dialog = OutOfOfficeHoursDialog.newInstance(model.additionalChargeForSelectingSpecificTime, EnterTaskDetailFragment.this);
                                            out_of_office_dialog.show(getFragmentManager(), Utility.ADDITION_CHARGES_DIALOG_TYPE.OUT_OF_OFFICE_HOURS);
                                            out_of_office_dialog.setCancelable(false);
                                            return;
                                        }
                                    } else {
                                        additionalChargeReason = Utility.ADDITION_CHARGES_DIALOG_TYPE.NONE;
                                        mFragmentEnterTaskDetailBinding.textTaskWhen.setText(Utility.EMPTY_STRING);
                                        mFragmentEnterTaskDetailBinding.textTaskWhen.setVisibility(View.GONE);
                                        Utility.showSnackBar(getString(R.string.validate_future_date), mFragmentEnterTaskDetailBinding.getRoot());
                                        updateTaskVerificationFlags();
                                        return;
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                if (System.currentTimeMillis() < startDateTimeSuperCalendar.getTimeInMillis()) {
                                    String selectedDateTime = startDateTimeSuperCalendar.format(Utility.DATE_FORMAT_DD_MMM)
                                            + getString(R.string.label_between)
                                            + CalendarUtility.get2HourTimeSlots(Long.toString(startDateTimeSuperCalendar.getTimeInMillis()));
                                    additionalChargeReason = Utility.ADDITION_CHARGES_DIALOG_TYPE.NONE;
                                    mFragmentEnterTaskDetailBinding.textTaskWhen.setText(selectedDateTime);
                                    mFragmentEnterTaskDetailBinding.textTaskWhen.setVisibility(View.VISIBLE);
                                    updateTaskVerificationFlags();
                                } else {
                                    additionalChargeReason = Utility.ADDITION_CHARGES_DIALOG_TYPE.NONE;
                                    mFragmentEnterTaskDetailBinding.textTaskWhen.setText(Utility.EMPTY_STRING);
                                    mFragmentEnterTaskDetailBinding.textTaskWhen.setVisibility(View.GONE);
                                    Utility.showSnackBar(getString(R.string.validate_future_date), mFragmentEnterTaskDetailBinding.getRoot());
                                    updateTaskVerificationFlags();
                                }
                            }
                        }


                    }
                }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false);
        timePickerDialog.show();


    }

    private WebCallClass.CommonResponseListener errorListener = new WebCallClass.CommonResponseListener() {
        @Override
        public void volleyError(VolleyError error) {
            hideProgressDialog();
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
        }

        @Override
        public void showSpecificMessage(String message) {
            hideProgressDialog();
            Utility.showSnackBar(message, mBinding.getRoot());
        }

        @Override
        public void forceLogout() {
            hideProgressDialog();
            finish();
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
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mBinding.getRoot());
            return;
        }

        //Show Progress
        showProgressDialog();

        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();

        // Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, userDetails.userID);
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());

        // Add Params
        ArrayList<QueAnsModel> mList = taskDetailModel.mQuesList;
        String subCategoryDetail = Utility.getSelectedServicesJsonString(taskDetailModel.subCatList);
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

        Map<String, Object> mParams = new HashMap<>();
        if (mSelectedAddressModel != null)
            if (Integer.parseInt(mSelectedAddressModel.address_id) > 0) {
                mParams.put(NetworkUtility.TAGS.ADDRESS_ID, mSelectedAddressModel.address_id);
                mParams.put(NetworkUtility.TAGS.ADDRESS_ID, userDetails.CityID);
            } else {

                mParams = NetworkUtility.addGuestAddressParams(mParams, mSelectedAddressModel);

            }
        //new
        //  mParams.put(NetworkUtility.TAGS.CITY_ID, taskDetailModel.categoryModel.);

//        cat_id:17
//        start_datetime:1529753847374
//        task_subcategories:305,306
//        country:India
//        address:Mumbai, Maharashtra, India
//        lng:72.8776559
//        address_initials:123
//        city_name:Mumbai
//        state:Maharashtra
//        lat:19.0759837
//        category:home
//        total_amount:250.0
//        payment_method:pay_later
//        payment_status:completed
//        task_type:normal
//        cheepcode:
//        payable_amount:250.0
//        promocode_price:0
//        landmark:Sdfsdfsd
//        pincode:789789
//                *payment_log:
//        payment_log:(optional)
//                non_office_hours_charge:0.0
//        urgent_booking_charge:0.0 *
        mParams.put(NetworkUtility.TAGS.CAT_ID, taskDetailModel.categoryModel.catId);
        mParams.put(NetworkUtility.TAGS.START_DATETIME, String.valueOf(superCalendar.getTimeInMillis()));
        mParams.put(NetworkUtility.TAGS.TASK_SUB_CATEGORIES, subCategoryDetail);
        mParams.put(NetworkUtility.TAGS.QUESTION_DETAIL, question_detail);
        mParams.put(NetworkUtility.TAGS.QUOTE_AMOUNT, taskDetailModel.quoteAmountStrategicPartner + "");

        mParams.put(NetworkUtility.TAGS.CHEEPCODE, TextUtils.isEmpty(taskDetailModel.cheepCode) ? Utility.EMPTY_STRING : taskDetailModel.cheepCode);
        mParams.put(NetworkUtility.TAGS.PAYABLE_AMOUNT, taskDetailModel.taskPaidAmount);
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
        LogUtils.LOGE(TAG, "cat_id = [ " + taskDetailModel.categoryModel.catId + " ] ");

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
                mTaskCreationParams = NetworkUtility.addGuestAddressParams(mTaskCreationParams, mSelectedAddressModel);
            }
        mTaskCreationParams.put(NetworkUtility.TAGS.CAT_ID, taskDetailModel.categoryModel.catId);
        mTaskCreationParams.put(NetworkUtility.TAGS.START_DATETIME, taskDetailModel.taskStartdate);
        mTaskCreationParams.put(NetworkUtility.TAGS.SUB_CATEGORY_DETAIL, subCategoryDetail);
        mTaskCreationParams.put(NetworkUtility.TAGS.QUESTION_DETAIL, question_detail);
        mTaskCreationParams.put(NetworkUtility.TAGS.QUOTE_AMOUNT, taskDetailModel.quoteAmountStrategicPartner + "");
        mTaskCreationParams.put(NetworkUtility.TAGS.PAYABLE_AMOUNT, taskDetailModel.taskPaidAmount);
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
                        if (!TextUtils.isEmpty(taskDetailModel.cheepCode) && taskDetailModel.cheepCode.startsWith(Utility.COUPON_DUNIA_CODE_PREFIX))
                            if (!BuildConfig.BUILD_TYPE.equalsIgnoreCase("release"))
                                AppsFlyerLib.getInstance().trackEvent(mContext, NetworkUtility.TAGS.APPSFLYER_CUSTOM_TRACK_EVENTS.COUPON_DUNIA_TASK_DEBUG, mTaskCreationParams);
                            else
                                AppsFlyerLib.getInstance().trackEvent(mContext, NetworkUtility.TAGS.APPSFLYER_CUSTOM_TRACK_EVENTS.COUPON_DUNIA_TASK_LIVE, mTaskCreationParams);

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

                        String message = getString(R.string.label_strategic_task_confirmed, taskDetailModel.categoryModel.catName) +
                                date + getString(R.string.label_at) + time;

                        final AcknowledgementDialogWithProfilePic mAcknowledgementDialogWithProfilePic = AcknowledgementDialogWithProfilePic.newInstance(
                                mContext,
                                R.drawable.ic_acknowledgement_dialog_header_background,
                                getString(R.string.label_brilliant),
                                message,
                                taskDetailModel.categoryModel.catImageExtras.original,
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
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        String error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
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
                mCallBookProForNormalTaskWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
            hideProgressDialog();
        }
    };

    /**
     * Call Complete task
     */
    private void callCompleteTaskWS(String status) {

        //Validation
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mBinding.getRoot());
            return;
        }

        //Show Progress
        showProgressDialog();

        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, userDetails.userID);
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());

        SuperCalendar superCalendar = SuperCalendar.getInstance();
        superCalendar.setTimeZone(SuperCalendar.SuperTimeZone.GMT.GMT);

        //Add Params
        Map<String, String> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.TASK_ID, taskDetailModel.taskId);
        mParams.put(NetworkUtility.TAGS.STATUS, status);


        //Sending end datetime millis in GMT timezone
        mParams.put(NetworkUtility.TAGS.TASK_ENDDATE, String.valueOf(superCalendar.getTimeInMillis()));

        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.CHANGE_TASK_STATUS
                , mCallCompleteTaskWSErrorListener
                , mCallCompleteTaskWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest);

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
                        String taskStatus = jsonObject.getString(NetworkUtility.TAGS.TASK_STATUS);
                        if (!TextUtils.isEmpty(taskStatus)) {
                            if (taskStatus.equalsIgnoreCase(Utility.TASK_STATUS.COMPLETION_CONFIRM)) {
                                Utility.showSnackBar(getString(R.string.msg_thanks_for_confirmation), mBinding.getRoot());
                                MessageEvent messageEvent = new MessageEvent();
                                messageEvent.taskStatus = taskStatus;
                                messageEvent.BROADCAST_ACTION = Utility.BROADCAST_TYPE.TASK_PAID_SUCCESSFULLY;
                                EventBus.getDefault().post(messageEvent);
                                finish();
                            }
                        }

                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        String error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
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
                mCallCompleteTaskWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
            hideProgressDialog();
        }
    };

    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////           Create Strategic task + payment  [end]                        /////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    private void callCreateSubscribedTaskWS(String paymentLog) {

        subscribedTaskDetailModel.paymentMethod = paymentMethod;
        subscribedTaskDetailModel.paymentLog = paymentLog;
        subscribedTaskDetailModel.paybleAmount = String.valueOf(subscribedTaskDetailModel.total);

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

}