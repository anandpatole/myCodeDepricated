package com.cheep.cheepcarenew.activities;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.R;
import com.cheep.activity.AddMoneyActivity;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.activity.SendOtpActivity;
import com.cheep.activity.WithdrawMoneyActivity;
import com.cheep.cheepcarenew.dialogs.AcknowledgementPopupDialog;
import com.cheep.cheepcarenew.dialogs.PaymentFailedDialog;
import com.cheep.cheepcarenew.model.CareCityDetail;
import com.cheep.cheepcarenew.model.CheepCarePaymentDataModel;
import com.cheep.cheepcarenew.model.UserRenewSubscriptionModel;
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
import com.cheep.utils.LogUtils;
import com.cheep.utils.PaytmUtility;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.SuperCalendar;
import com.cheep.utils.Utility;
import com.cheep.utils.WebCallClass;
import com.google.gson.Gson;
import com.paynimo.android.payment.PaymentActivity;
import com.paynimo.android.payment.PaymentModesActivity;
import com.paynimo.android.payment.model.Checkout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
    public static final int PAYTM_SUBSCRIPTION = 3;
    private String cartDetail = "";
    private CheepCarePaymentDataModel paymentDataModel;
    private UserRenewSubscriptionModel renewSubscriptionModel;
    private Map<String, Object> mTaskCreationParams;

    //    private SubscribedTaskDetailModel subscribedTaskDetailModel;
    private int PAYTM_STEP = -1;
    private String paymentMethod;
    private double paytmPayableAmount;
    private double payableAmount;
    private CareCityDetail careCityDetail;
    private AddressModel addressModel;
    private String paymentFor;

    public static final String PAYMENT_FOR_SUBSCRIPTION = "payment_for_subscription";
    public static final String PAYMENT_FOR_TASK_CREATION = "payment_for_task_creation";
    public static final String PAYMENT_FOR_RENEW_PACKAGE = "payment_for_renew_package";
    private AcknowledgementPopupDialog acknowledgementPopupDialog;


    /*
         cart details - selected packages json array string
         payment data model - payment info
         city detail - selected city data for which user is purchasing subscription
     */
    public static void newInstance(Context context, String cartDetails, CheepCarePaymentDataModel paymentDataModel, CareCityDetail mCareCityDetail, AddressModel addressModel) {
        Intent intent = new Intent(context, PaymentChoiceCheepCareActivity.class);
        intent.putExtra(Utility.Extra.DATA, cartDetails);
        intent.putExtra(Utility.Extra.DATA_2, GsonUtility.getJsonStringFromObject(paymentDataModel));
        intent.putExtra(Utility.Extra.DATA_3, GsonUtility.getJsonStringFromObject(mCareCityDetail));
        intent.putExtra(Utility.Extra.SELECTED_ADDRESS_MODEL, GsonUtility.getJsonStringFromObject(addressModel));
        intent.putExtra(Utility.Extra.PAYMENT_VIEW, PAYMENT_FOR_SUBSCRIPTION);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void newInstance(Context context, UserRenewSubscriptionModel userRenewSubscriptionModel) {
        Intent intent = new Intent(context, PaymentChoiceCheepCareActivity.class);
        intent.putExtra(Utility.Extra.DATA, userRenewSubscriptionModel);
        intent.putExtra(Utility.Extra.PAYMENT_VIEW, PAYMENT_FOR_RENEW_PACKAGE);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /*
    this is for payment of subscribed task with paid service payment or addtional charge of non working hours and excess limit of task bookinf
    also if user selects non-subscribed address then user has to pay for free services as well
     *//*
    public static void newInstance(Context context, SubscribedTaskDetailModel subscribedTaskDetailModel) {
        Intent intent = new Intent(context, PaymentChoiceCheepCareActivity.class);
        intent.putExtra(Utility.Extra.DATA, GsonUtility.getJsonStringFromObject(subscribedTaskDetailModel));
        intent.putExtra(Utility.Extra.PAYMENT_VIEW, PAYMENT_FOR_TASK_CREATION);
        context.startActivity(intent);
    }
*/

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

        mBinding.cartAutoRenewSwitch.setSelected(false);
        mBinding.paytmAutoRenewSwitch.setSelected(false);
        mBinding.tvCardAutoRenewal.setText(R.string.label_auto_renew_activated);
        mBinding.tvPaytmAutoRenewal.setText(R.string.label_auto_renew_activated);
        paymentFor = getIntent().getStringExtra(Utility.Extra.PAYMENT_VIEW);
        if (paymentFor.equalsIgnoreCase(PAYMENT_FOR_SUBSCRIPTION)) {
            cartDetail = getIntent().getStringExtra(Utility.Extra.DATA);
            paymentDataModel = (CheepCarePaymentDataModel) GsonUtility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA_2),
                    CheepCarePaymentDataModel.class);
            careCityDetail = (CareCityDetail) GsonUtility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA_3), CareCityDetail.class);
            addressModel = (AddressModel) GsonUtility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.SELECTED_ADDRESS_MODEL), AddressModel.class);
            payableAmount = paymentDataModel.paidAmount;
            LogUtils.LOGE(TAG, "initiateUI: paymentDataModel \n============\n" + paymentDataModel);

            // get next year date
            SuperCalendar superCalendar = SuperCalendar.getInstance();
            superCalendar.getCalendar().add(Calendar.MONTH, +Integer.parseInt(paymentDataModel.packageDuration));
            String day = String.valueOf(superCalendar.getCalendar().get(Calendar.DATE));
            String month = String.valueOf(superCalendar.getCalendar().get(Calendar.MONTH));
            String year = String.valueOf(superCalendar.getCalendar().get(Calendar.YEAR));
            String date = day + "-" + month + "-" + year;
            String oneDayMinusFromDate = CalendarUtility.getOneDayMinusDateFromPassingDate(date);

            mBinding.tvAutoRenewalMsg.setText(getString(R.string.label_with_payment_desc_cheep_care, oneDayMinusFromDate));
        } else if (paymentFor.equalsIgnoreCase(PAYMENT_FOR_RENEW_PACKAGE)) {
            renewSubscriptionModel = (UserRenewSubscriptionModel) getIntent().getSerializableExtra(Utility.Extra.DATA);
            String date = CalendarUtility.getFutureDate(renewSubscriptionModel.endDate,renewSubscriptionModel.packageDuration);
            mBinding.tvAutoRenewalMsg.setText(getString(R.string.label_with_payment_desc_cheep_care, date));
        }
        setupActionbar();
    }

    private void setupActionbar() {

        DecimalFormat formatter = new DecimalFormat("#,###");
        if (paymentFor.equalsIgnoreCase(PAYMENT_FOR_SUBSCRIPTION)) {
            mBinding.textTitle.setText(getString(R.string.label_please_pay_x, formatter.format(Double.valueOf(paymentDataModel.paidAmount))));

        } else if (paymentFor.equalsIgnoreCase(PAYMENT_FOR_RENEW_PACKAGE)) {
            //remove value after dot
            double x = Double.valueOf(renewSubscriptionModel.paidAmount);
            int paidAmount = (int) x;
            //String paidAmount = new DecimalFormat("##").format(renewSubscriptionModel.paidAmount);
            mBinding.textTitle.setText(getString(R.string.label_please_pay_x, formatter.format(Double.valueOf(paidAmount))));
        }
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
        mBinding.cartAutoRenewSwitch.setOnClickListener(this);
        mBinding.paytmAutoRenewSwitch.setOnClickListener(this);
    }

    // open show Acknowledgement Popup Dialog
    private void showAcknowledgementPopupDialog() {
        if (acknowledgementPopupDialog != null) {
            acknowledgementPopupDialog.dismissAllowingStateLoss();
            acknowledgementPopupDialog = null;
        }
        acknowledgementPopupDialog = AcknowledgementPopupDialog.newInstance(new AcknowledgementPopupDialog.AcknowledgementListener() {
            @Override
            public void onClickOfThanks() {

                MessageEvent messageEvent = new MessageEvent();
                if (paymentFor.equalsIgnoreCase(PAYMENT_FOR_SUBSCRIPTION)){
                    messageEvent.id = careCityDetail.id;
                    messageEvent.BROADCAST_ACTION = Utility.BROADCAST_TYPE.PACKAGE_SUBSCRIBED_SUCCESSFULLY;
                }else if(paymentFor.equalsIgnoreCase(PAYMENT_FOR_RENEW_PACKAGE)) {
                    messageEvent.id = renewSubscriptionModel.id;
                    messageEvent.BROADCAST_ACTION = Utility.BROADCAST_TYPE.PACKAGE_SUBSCRIBED_RENEW_SUCCESSFULLY;
                }
                EventBus.getDefault().post(messageEvent);

                callProfileWsforUpdatedAddressList();

                finish();
            }
        });
        acknowledgementPopupDialog.show(getSupportFragmentManager(), TAG);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_card:
                initCheckout();
                break;
            case R.id.rl_netbanking:
                paymentMethod = NetworkUtility.PAYMENT_METHOD_TYPE.PAYU;
                doPaymentOfNetBanking();
//                doPaymentOfNetBanking();
                break;
            case R.id.rl_paytm:
                paymentMethod = NetworkUtility.PAYMENT_METHOD_TYPE.PAYTM;
                doPaymentOfPaytm();

                break;

            case R.id.rl_cash_payment:
                break;
            case R.id.cartAutoRenewSwitch:
                view.setSelected(!view.isSelected());
                mBinding.tvCardAutoRenewal.setText(view.isSelected() ? R.string.label_auto_renew_activated : R.string.label_auto_renew_deactivated);
                break;
            case R.id.paytmAutoRenewSwitch:
                view.setSelected(!view.isSelected());
                mBinding.tvPaytmAutoRenewal.setText(view.isSelected() ? R.string.label_auto_renew_activated : R.string.label_auto_renew_deactivated);
                break;
        }
    }


    private void onSuccessOfPayment(String paymentLog, String subsId, String isSubscription) {
        if (paymentFor.equalsIgnoreCase(PAYMENT_FOR_SUBSCRIPTION)) {
            callCreateCheepCarePackageWS(paymentLog, subsId, isSubscription);
        }
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

    private void callCreateCheepCarePackageWS(String paymentLog, String subsId, String isSubscription) {
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
        Map<String, Object> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.TOTAL_AMOUNT, String.valueOf(paymentDataModel.totalAmount));
        mParams.put(NetworkUtility.TAGS.PROMOCODE, paymentDataModel.promocode);
        mParams.put(NetworkUtility.TAGS.DISCOUNT_AMOUNT, String.valueOf(paymentDataModel.discountAmount));
        mParams.put(NetworkUtility.TAGS.PAID_AMOUNT, String.valueOf(paymentDataModel.paidAmount));
        mParams.put(NetworkUtility.TAGS.TAX_AMOUNT, String.valueOf(paymentDataModel.taxAmount));
        mParams.put(NetworkUtility.TAGS.PACKAGE_TYPE, String.valueOf(paymentDataModel.packageType));
        mParams.put(NetworkUtility.TAGS.PACKAGE_DURATION, String.valueOf(paymentDataModel.packageDuration));
        mParams.put(NetworkUtility.TAGS.DSA_CODE, paymentDataModel.dsaCode);
        mParams.put(NetworkUtility.TAGS.CARE_CITY_ID, String.valueOf(careCityDetail.id));
        mParams.put(NetworkUtility.TAGS.PACKAGE_ID, String.valueOf(paymentDataModel.packageId));
        mParams.put(NetworkUtility.TAGS.PACKAGE_TITLE, String.valueOf(paymentDataModel.packageTitle));
        int addressId = 0;
        try {
            addressId = Integer.parseInt(addressModel.address_id);
        } catch (Exception e) {
            addressId = 0;
        }
        if (addressId <= 0) {
            NetworkUtility.addGuestAddressParams(mParams, addressModel);

        } else {
            mParams.put(NetworkUtility.TAGS.ADDRESS_ID, String.valueOf(addressId));
        }
        mParams.put(NetworkUtility.TAGS.ASSET_TYPE_ID, String.valueOf(paymentDataModel.addressAssetTypeId));
        //mParams.put(NetworkUtility.TAGS.PAYABLE_AMOUNT, String.valueOf(paymentDataModel.payableAmount));
        //mParams.put(NetworkUtility.TAGS.IS_ANNUALLY, String.valueOf(paymentDataModel.isAnnually));
        // mParams.put(NetworkUtility.TAGS.BUNDLE_DISCOUNT_PRICE, String.valueOf(paymentDataModel.bundlediscountPrice));
        //mParams.put(NetworkUtility.TAGS.BUNDLE_DISCOUNT_PERCENT, String.valueOf(paymentDataModel.bundlediscountPercent));

        mParams.put(NetworkUtility.TAGS.PAYMENT_METHOD, paymentMethod);
        mParams.put(NetworkUtility.TAGS.PAYMENT_LOG, paymentLog);
        // mParams.put(NetworkUtility.TAGS.CART_DETAIL, cartDetail);
        //mParams.put(NetworkUtility.TAGS.SUBS_ID.toLowerCase(), subsId);
        mParams.put(NetworkUtility.TAGS.IS_RENEW, isSubscription);

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
            case PaymentActivity.REQUEST_CODE:
                onResultOfPayNimo(resultCode, data);
                break;
            case Utility.REQUEST_START_PAYMENT_CHEEP_CARE:

                if (resultCode == RESULT_OK) {
                    //success

                    if (data != null) {
                        LogUtils.LOGE(TAG, "onActivityResult() called with success: result= [" + data.getStringExtra(Utility.Extra.PAYU_RESPONSE) + "]");
                        onSuccessOfPayment(data.getStringExtra(Utility.Extra.PAYU_RESPONSE), Utility.EMPTY_STRING, Utility.BOOLEAN.NO);
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
                    if (mBinding.paytmAutoRenewSwitch.isSelected())
                        PAYTM_STEP = PAYTM_SUBSCRIPTION;

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
                    LogUtils.LOGE(TAG, "onMessageEvent:isSubscription " + event.paytmResponse.isSubscription);
                    LogUtils.LOGE(TAG, "onMessageEvent:subsId " + event.paytmResponse.subsId);
                    onSuccessOfPayment(event.paytmResponse.ResponsePayLoad, event.paytmResponse.subsId, event.paytmResponse.isSubscription);
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

            boolean isLowBalance = false;
            if (paymentDataModel != null) {
                isLowBalance = paytmWalletBalance < paymentDataModel.payableAmount;
                paytmPayableAmount = paymentDataModel.payableAmount - paytmWalletBalance;
            } /*else {
                isLowBalance = paytmWalletBalance < subscribedTaskDetailModel.total;
                paytmPayableAmount = subscribedTaskDetailModel.total - paytmWalletBalance;
            }*/
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

                        /*MessageEvent messageEvent = new MessageEvent();
                        messageEvent.id = careCityDetail.id;
                        messageEvent.BROADCAST_ACTION = Utility.BROADCAST_TYPE.PACKAGE_SUBSCRIBED_SUCCESSFULLY;

                        EventBus.getDefault().post(messageEvent);

                        callProfileWsforUpdatedAddressList();*/

                        /*finish();*/

                        showAcknowledgementPopupDialog();


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

        if (PAYTM_STEP == PAYTM_SEND_OTP) {
            SendOtpActivity.newInstance(mContext, true, String.valueOf(payableAmount), mBinding.paytmAutoRenewSwitch.isSelected(), Utility.BROADCAST_TYPE.PAYTM_RESPONSE);
        }
        //this is commented because subscription is remove temporary -giteeka
        /*else if (mBinding.paytmAutoRenewSwitch.isSelected()) {
            SubscriptionActivity.newInstance(mContext, String.valueOf(payableAmount), paytmUserDetail.paytmAccessToken,
                    paytmUserDetail.paytmphoneNumber, paytmUserDetail.paytmCustId);
        }*/
        else {
            switch (PAYTM_STEP) {
                case PAYTM_ADD_MONEY:
                    AddMoneyActivity.newInstance(mContext, String.valueOf(payableAmount), paytmPayableAmount, paytmUserDetail.paytmAccessToken,
                            paytmUserDetail.paytmphoneNumber, paytmUserDetail.paytmCustId, paytmWalletBalance, Utility.BROADCAST_TYPE.PAYTM_RESPONSE);
                    break;
                case PAYTM_WITHDRAW:
                    WithdrawMoneyActivity.newInstance(mContext, String.valueOf(payableAmount), paytmPayableAmount, paytmUserDetail.paytmAccessToken,
                            paytmUserDetail.paytmphoneNumber, paytmUserDetail.paytmCustId, paytmWalletBalance, true, Utility.BROADCAST_TYPE.PAYTM_RESPONSE);
                    break;
            }
        }
    }

   /* private void doPaymentOfNetBanking() {
        paymentMethod = NetworkUtility.PAYMENT_METHOD_TYPE.PAYU;
        generateHashForCheepCarePackagePurchase();
    }*/

    private void doPaymentOfNetBanking() {
        paymentMethod = NetworkUtility.PAYMENT_METHOD_TYPE.PAYU;
        Checkout objCheckOut = new Checkout();

        objCheckOut.setMerchantIdentifier("T144071");
        objCheckOut.setTransactionIdentifier("T007");
        objCheckOut.setTransactionReference("testReference");
        objCheckOut.setTransactionType("SALE");
        objCheckOut.setTransactionSubType("DEBIT");
        objCheckOut.setTransactionCurrency("INR");
        objCheckOut.setTransactionAmount(String.valueOf(payableAmount));
        objCheckOut.setTransactionAmount("4");
        Calendar calendar = SuperCalendar.getInstance().getCalendar();
        String date = CalendarUtility.getDate(calendar.getTimeInMillis(), Utility.DATE_FORMAT_DD_MM_YYYY);
        objCheckOut.setTransactionDateTime(date);
        objCheckOut.setConsumerIdentifier("test");
        objCheckOut.setConsumerEmailID("test@gmail.com");
        objCheckOut.setConsumerMobileNumber("8238864762");
        objCheckOut.setConsumerAccountNo("");
//        objCheckOut.addCartItem("TEST", String.valueOf(payableAmount), "0.00", "", "Pkg1", "", "", "0.0");
        objCheckOut.addCartItem("TEST", "4", "0.00", "", "Pkg1", "", "", "0.0");

        Log.d("Checkout Request Object", new Gson().toJson(objCheckOut.getMerchantRequestPayload())/*objCheckOut.getMerchantRequestPayload().toString()*/);

        Intent authIntent = new Intent(this, PaymentModesActivity.class);
        authIntent.putExtra(PaymentActivity.ARGUMENT_DATA_CHECKOUT, objCheckOut);
        authIntent.putExtra(PaymentActivity.EXTRA_PUBLIC_KEY, "1234-6666-6789-56");
        authIntent.putExtra(PaymentActivity.EXTRA_REQUESTED_PAYMENT_MODE, PaymentActivity.PAYMENT_METHOD_NETBANKING);
        startActivityForResult(authIntent, PaymentActivity.REQUEST_CODE);
    }

    private void initCheckout() {
        paymentMethod = NetworkUtility.PAYMENT_METHOD_TYPE.PAYU;
        Checkout objCheckOut = new Checkout();
        objCheckOut.setMerchantIdentifier("T144071");
        objCheckOut.setTransactionIdentifier("T007");
        objCheckOut.setTransactionReference("testReference");
        objCheckOut.setTransactionType("SALE");
        objCheckOut.setTransactionSubType("DEBIT");
        objCheckOut.setTransactionCurrency("INR");
        objCheckOut.setTransactionAmount("1");
        Calendar calendar = SuperCalendar.getInstance().getCalendar();
        String date = CalendarUtility.getDate(calendar.getTimeInMillis(), Utility.DATE_FORMAT_DD_MM_YYYY);
        objCheckOut.setTransactionDateTime(date);
        objCheckOut.setConsumerIdentifier("test");
        objCheckOut.setConsumerEmailID("test@gmail.com");
        objCheckOut.setConsumerMobileNumber("8238864762");
        objCheckOut.setConsumerAccountNo("");

//        objCheckOut.addCartItem("TEST", "4.00", "0.00", "", "Pkg1", "", "", "0.0");
        objCheckOut.addCartItem("TEST", "1", "0.00", "", "Pkg1", "", "", "0.0");
        if (mBinding.cartAutoRenewSwitch.isSelected()) {
            objCheckOut.setTransactionMerchantInitiated("N");
            objCheckOut.setPaymentInstructionAction("Y");
            objCheckOut.setPaymentInstructionType("F");
            objCheckOut.setPaymentInstructionAmount("1");
            objCheckOut.setPaymentInstructionLimit("1");
            objCheckOut.setPaymentInstructionFrequency("DAIL");
            Calendar calendar1 = (Calendar) calendar.clone();
            calendar1.add(Calendar.DATE, 60);
            String endDate = CalendarUtility.getDate(calendar1.getTimeInMillis(), Utility.DATE_FORMAT_DD_MM_YYYY);
            objCheckOut.setPaymentInstructionStartDateTime(date);
            objCheckOut.setPaymentInstructionEndDateTime(endDate);
        }
        Log.d("Checkout Request Object", new Gson().toJson(objCheckOut.getMerchantRequestPayload())/*objCheckOut.getMerchantRequestPayload().toString()*/);

        Intent authIntent = new Intent(this, PaymentModesActivity.class);
        authIntent.putExtra(PaymentActivity.ARGUMENT_DATA_CHECKOUT, objCheckOut);
        authIntent.putExtra(PaymentActivity.EXTRA_PUBLIC_KEY, "1234-6666-6789-56");
        authIntent.putExtra(PaymentActivity.EXTRA_REQUESTED_PAYMENT_MODE, PaymentActivity.PAYMENT_METHOD_CARDS);
        startActivityForResult(authIntent, PaymentActivity.REQUEST_CODE);
    }

    private void onResultOfPayNimo(int resultCode, Intent data) {
        switch (resultCode) {
            case PaymentActivity.RESULT_OK:
                Log.d(TAG, "Result Code :" + RESULT_OK);
                if (data != null) {
                    LogUtils.LOGE(TAG, "data= [" + data + "]");
                    try {
                        Checkout checkoutObj = (Checkout) data.getSerializableExtra(PaymentActivity.ARGUMENT_DATA_CHECKOUT);
                        Log.d("Checkout Response Obj", checkoutObj.getMerchantResponsePayload().toString());

                        String paymentlog = printResult(checkoutObj);
                        if (checkoutObj.getMerchantResponsePayload().getPaymentMethod().getPaymentTransaction().getStatusCode().equalsIgnoreCase(
                                PaymentActivity.TRANSACTION_STATUS_SALES_DEBIT_SUCCESS)) {
                            Toast.makeText(getApplicationContext(), "Transaction Status - Success", Toast.LENGTH_SHORT).show();
                            Log.v("TRANSACTION STATUS=>", "SUCCESS");

                            // TRANSACTION STATUS - SUCCESS (status code  0300 means success), NOW MERCHANT CAN PERFORM ANY OPERATION OVER SUCCESS RESULT

                            if (checkoutObj.getMerchantResponsePayload()
                                    .getPaymentMethod()
                                    .getPaymentTransaction()
                                    .getInstruction()
                                    .getStatusCode().equalsIgnoreCase("")) {
                                //  SI TRANSACTION STATUS - SUCCESS (status code 0300 means success)
                                // failure
                                // todo :: >> this is our final success
                                String siMandateId = checkoutObj.getMerchantResponsePayload().getPaymentMethod().getPaymentTransaction().getInstruction().getId();
                                LogUtils.LOGE(TAG, "onResultOfPayNimo:siMandateId " + siMandateId);
                                if (paymentFor.equalsIgnoreCase(PAYMENT_FOR_RENEW_PACKAGE)){
                                    callRenewUserPackageWS(paymentlog, Utility.EMPTY_STRING, Utility.BOOLEAN.NO);
                                }else {
                                    callCreateCheepCarePackageWS(paymentlog, Utility.EMPTY_STRING, Utility.BOOLEAN.NO);
                                }
                                Log.v("TRANSACTION SI STATUS=>", "SI Transaction not Initiated");

                            } else if (checkoutObj.getMerchantResponsePayload()
                                    .getPaymentMethod()
                                    .getPaymentTransaction()
                                    .getInstruction()
                                    .getStatusCode().equalsIgnoreCase(PaymentActivity.TRANSACTION_STATUS_SALES_DEBIT_SUCCESS)) {

                                //SI TRANSACTION STATUS - SUCCESS (status code 0300 means success)
                                Log.e("TRANSACTION SI STATUS=>", "SUCCESS");
                                String siMandateId = checkoutObj.getMerchantResponsePayload().getPaymentMethod().getPaymentTransaction().getInstruction().getId();
                                LogUtils.LOGE(TAG, "onResultOfPayNimo:siMandateId " + siMandateId);
                                // todo :: >> this is our final success
                                if (paymentFor.equalsIgnoreCase(PAYMENT_FOR_RENEW_PACKAGE)){
                                    callRenewUserPackageWS(paymentlog, Utility.EMPTY_STRING, Utility.BOOLEAN.NO);
                                }else {
                                    callCreateCheepCarePackageWS(paymentlog, Utility.EMPTY_STRING, Utility.BOOLEAN.NO);
                                }

                            } else {
                                //SI TRANSACTION STATUS - Failure (status code OTHER THAN 0300 means failure)
                                Log.v("TRANSACTION SI STATUS=>", "FAILURE");
                                showPaymentFailedDialog();
                            }
                        } // Transaction Completed and Got FAILURE
                        else {
                            // some error from bank side
                            LogUtils.LOGW("TRANSACTION STATUS=>", "FAILURE");
                            Toast.makeText(getApplicationContext(), "Transaction Status - Failure", Toast.LENGTH_SHORT).show();
                            showPaymentFailedDialog();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                break;
            case PaymentActivity.RESULT_ERROR:
                Log.d(TAG, "got an error");
                if (data.hasExtra(PaymentActivity.RETURN_ERROR_CODE) && data.hasExtra(PaymentActivity.RETURN_ERROR_DESCRIPTION)) {
                    String error_code = data.getStringExtra(PaymentActivity.RETURN_ERROR_CODE);
                    String error_desc = data.getStringExtra(PaymentActivity.RETURN_ERROR_DESCRIPTION);
                    Toast.makeText(getApplicationContext(), " Got error :" + error_code + "--- " + error_desc, Toast.LENGTH_SHORT).show();
                    LogUtils.LOGW(TAG + " Code=>", error_code);
                    LogUtils.LOGW(TAG + " Desc=>", error_desc);
                }
                showPaymentFailedDialog();
                break;
            case PaymentActivity.RESULT_CANCELED:
                //Toast.makeText(getApplicationContext(), "Transaction Aborted by User",Toast.LENGTH_SHORT).show();
                LogUtils.LOGW(TAG, "User pressed back button");

                break;
        }
    }

    private String printResult(Checkout checkoutObj) {
        String transactionType = checkoutObj.getMerchantRequestPayload().getTransaction().getType();
        String transactionSubType = checkoutObj.getMerchantRequestPayload().getTransaction().getSubType();
        String result = "StatusCode : " + checkoutObj
                .getMerchantResponsePayload().getPaymentMethod()
                .getPaymentTransaction().getStatusCode()
                + "\nStatusMessage : " + checkoutObj
                .getMerchantResponsePayload().getPaymentMethod()
                .getPaymentTransaction().getStatusMessage()
                + "\nErrorMessage : " + checkoutObj
                .getMerchantResponsePayload().getPaymentMethod()
                .getPaymentTransaction().getErrorMessage()
                + "\nAmount : " + checkoutObj
                .getMerchantResponsePayload().getPaymentMethod().getPaymentTransaction().getAmount()
                + "\nDateTime : " + checkoutObj.
                getMerchantResponsePayload().getPaymentMethod()
                .getPaymentTransaction().getDateTime()
                + "\nMerchantTransactionIdentifier : "
                + checkoutObj.getMerchantResponsePayload()
                .getMerchantTransactionIdentifier()
                + "\nIdentifier : " + checkoutObj
                .getMerchantResponsePayload().getPaymentMethod()
                .getPaymentTransaction().getIdentifier()
                + "\nBankSelectionCode : " + checkoutObj
                .getMerchantResponsePayload().getPaymentMethod()
                .getBankSelectionCode()
                + "\nBankReferenceIdentifier : " + checkoutObj
                .getMerchantResponsePayload().getPaymentMethod()
                .getPaymentTransaction().getBankReferenceIdentifier()
                + "\nRefundIdentifier : " + checkoutObj
                .getMerchantResponsePayload().getPaymentMethod()
                .getPaymentTransaction().getRefundIdentifier()
                + "\nBalanceAmount : " + checkoutObj
                .getMerchantResponsePayload().getPaymentMethod()
                .getPaymentTransaction().getBalanceAmount()
                + "\nInstrumentAliasName : " + checkoutObj
                .getMerchantResponsePayload().getPaymentMethod()
                .getInstrumentAliasName()
                + "\nSI Mandate Id : " + checkoutObj
                .getMerchantResponsePayload().getPaymentMethod()
                .getPaymentTransaction().getInstruction().getId()
                + "\nSI Mandate Status : " + checkoutObj
                .getMerchantResponsePayload().getPaymentMethod()
                .getPaymentTransaction().getInstruction().getStatusCode()
                + "\nSI Mandate Error Code : " + checkoutObj
                .getMerchantResponsePayload().getPaymentMethod()
                .getPaymentTransaction().getInstruction().getErrorcode();
        Log.e(TAG, "onActivityResult: " + result);
        return result;
    }

    ////////////////////////////////////////////////////////////////////  RENEW USER PACKAGE ///////////////////////////////////////////////////////

    private void callRenewUserPackageWS(String paymentLog, String subsId, String isSubscription) {
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
        Map<String, Object> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.USER_PACKAGE_ID, String.valueOf(renewSubscriptionModel.userPackageId));
        mParams.put(NetworkUtility.TAGS.TOTAL_AMOUNT, String.valueOf(renewSubscriptionModel.totalAmount));
        mParams.put(NetworkUtility.TAGS.DISCOUNT_AMOUNT, String.valueOf(renewSubscriptionModel.discountAmount));
        mParams.put(NetworkUtility.TAGS.PAID_AMOUNT, String.valueOf(renewSubscriptionModel.paidAmount));
        mParams.put(NetworkUtility.TAGS.TAX_AMOUNT, String.valueOf(renewSubscriptionModel.taxAmount));

        mParams.put(NetworkUtility.TAGS.PAYMENT_METHOD, paymentMethod);
        mParams.put(NetworkUtility.TAGS.PAYMENT_LOG, paymentLog);

        LogUtils.LOGE(TAG, "callBookProAndPayForNormalTaskWS: mParams " + mParams);

        // Url is based on condition if address id is greater then 0 then it means we need to update the existing address
        VolleyNetworkRequest mVolleyNetworkRequestForSPList = new VolleyNetworkRequest(NetworkUtility.WS.CARE_PACKAGE_RENEW
                , mCallGenerateHashWSErrorListenerRenew
                , mCallCreateCheepCareTaskWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForSPList);
    }

    Response.ErrorListener mCallGenerateHashWSErrorListenerRenew = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(final VolleyError error) {
            hideProgressDialog();
            LogUtils.LOGD(TAG, "onErrorResponse() called with: error = [" + error + "]");
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
        }
    };
}