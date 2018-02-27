package com.cheep.cheepcare.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.appsflyer.AppsFlyerLib;
import com.cheep.BuildConfig;
import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.activity.PaymentChoiceActivity;
import com.cheep.activity.TaskSummaryForMultiCatActivity;
import com.cheep.cheepcare.adapter.SelectedSubServicePriceAdapter;
import com.cheep.custom_view.BottomAlertDialog;
import com.cheep.custom_view.CFEditTextRegular;
import com.cheep.databinding.ActivityBookingConfirmationCcBinding;
import com.cheep.dialogs.AcknowledgementDialogWithProfilePic;
import com.cheep.dialogs.AcknowledgementInteractionListener;
import com.cheep.model.AddressModel;
import com.cheep.model.MessageEvent;
import com.cheep.model.PaymentSummaryModel;
import com.cheep.model.ProviderModel;
import com.cheep.model.SubServiceDetailModel;
import com.cheep.model.TaskDetailModel;
import com.cheep.model.UserDetails;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.LogUtils;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.SuperCalendar;
import com.cheep.utils.Utility;
import com.cheep.utils.WebCallClass;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class BookingConfirmationCcActivity extends BaseAppCompatActivity {


    private static final String TAG = BookingConfirmationCcActivity.class.getSimpleName();
    private ActivityBookingConfirmationCcBinding mBinding;
    Bundle bundle;
    private ProviderModel providerModel;
    private TaskDetailModel taskDetailModel;
    //ForInsta Booking
    private boolean isInstaBooking = false;
    //    private String actualQuotePrice;
    private AddressModel mSelectedAddressModelForInsta;
    private double usedWalletBalance = 0;

    private String referralBalance;
    private String maxReferDiscount;

    private CFEditTextRegular edtCheepcode;
    private BottomAlertDialog cheepCodeDialog;
    private String cheepCode;

    //added by bhavesh 26/2/18
    private String mCarePackageId;
    private String mCategoryId;
    private ArrayList<SubServiceDetailModel> freeList;
    private ArrayList<SubServiceDetailModel> paidList;
    private AddressModel mAddressModel;
    private String startDateTime;
    //added by bhavesh 26/2/18

    /**
     * used while user is booking task and selects pay now/later buttons
     */
    private boolean isPayNow = false;

    /**
     * used while user taps on View Payment summary and tries to do payment in on going task
     */
    private boolean payPendingAmount = false;

    /**
     * payment summary for task
     *
     * @param context
     * @param taskDetailModel
     * @param providerModel
     */
    public static void newInstance(Context context, TaskDetailModel taskDetailModel, ProviderModel providerModel, AddressModel mSelectedAddressModel) {
        Intent intent = new Intent(context, BookingConfirmationCcActivity.class);
        intent.putExtra(Utility.Extra.DATA, Utility.getJsonStringFromObject(providerModel));
        intent.putExtra(Utility.Extra.DATA_2, Utility.getJsonStringFromObject(taskDetailModel));
        intent.putExtra(Utility.Extra.SELECTED_ADDRESS_MODEL, Utility.getJsonStringFromObject(mSelectedAddressModel));
        intent.putExtra(Utility.Extra.DATA_3, false);
        context.startActivity(intent);
    }

    /**
     * payment summary on completion of task
     * if user has choose pay later option or additional payments are pending then this instance will be called
     *
     * @param mContext
     * @param taskDetailModel
     */
    public static void newInstance(Context mContext, TaskDetailModel taskDetailModel) {
        Intent intent = new Intent(mContext, BookingConfirmationCcActivity.class);
        if (taskDetailModel != null)
            intent.putExtra(Utility.Extra.DATA, Utility.getJsonStringFromObject(taskDetailModel.selectedProvider));
        intent.putExtra(Utility.Extra.DATA_2, Utility.getJsonStringFromObject(taskDetailModel));
        intent.putExtra(Utility.Extra.DATA_3, true);
        mContext.startActivity(intent);

    }

    //TODO: method to be removed
    public static void newInstance(Context context, String carePackageId, String catId, ArrayList<SubServiceDetailModel> freeList
            , ArrayList<SubServiceDetailModel> paidList, AddressModel addressModel, String startDateTime) {
        Intent intent = new Intent(context, BookingConfirmationCcActivity.class);
        intent.putExtra(Utility.Extra.SELECTED_PACKAGE_ID, carePackageId);
        intent.putExtra(Utility.Extra.CATEGORY_ID, catId);
        intent.putExtra("Utility.Extra.DATA", freeList);
        intent.putExtra("Utility.Extra.DATA_2", paidList);
        intent.putExtra(Utility.Extra.SELECTED_ADDRESS_MODEL, addressModel);
        intent.putExtra("startDateTime", startDateTime);
        context.startActivity(intent);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
          when the device runing out of memory we dont want the user to restart the payment. rather we close it and redirect them to previous activity.
         */
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_booking_confirmation_cc);
        // add event bus listener
        EventBus.getDefault().register(this);
        initiateUI();
        setListeners();
    }

    @Override
    protected void initiateUI() {

        if (getIntent().getExtras() != null && getIntent().hasExtra(Utility.Extra.SELECTED_PACKAGE_ID))
            mCarePackageId = getIntent().getExtras().getString(Utility.Extra.SELECTED_PACKAGE_ID);
        if (getIntent().getExtras() != null && getIntent().hasExtra(Utility.Extra.CATEGORY_ID))
            mCategoryId = getIntent().getExtras().getString(Utility.Extra.CATEGORY_ID);
        if (getIntent().getExtras() != null && getIntent().hasExtra("Utility.Extra.DATA"))
            freeList = (ArrayList<SubServiceDetailModel>) getIntent().getExtras().getSerializable("Utility.Extra.DATA");
        if (getIntent().getExtras() != null && getIntent().hasExtra("Utility.Extra.DATA_2"))
            paidList = (ArrayList<SubServiceDetailModel>) getIntent().getExtras().getSerializable("Utility.Extra.DATA_2");
        if (getIntent().getExtras() != null && getIntent().hasExtra(Utility.Extra.SELECTED_ADDRESS_MODEL))
            mAddressModel = (AddressModel) getIntent().getSerializableExtra(Utility.Extra.SELECTED_ADDRESS_MODEL);
        if (getIntent().getExtras() != null && getIntent().hasExtra("startDateTime"))
            startDateTime = getIntent().getExtras().getString("startDateTime");

        setSupportActionBar(mBinding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(Utility.EMPTY_STRING);
            mBinding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Utility.hideKeyboard(mContext);
                    onBackPressed();
                }
            });
        }

        // Enable Step Three Unverified state
        setTaskState(STEP_THREE_UNVERIFIED);

//        String taskDescription = getString(R.string.msg_task_description, "25th April", "1100", "1400"
//                , );
//        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(taskDescription);
//
//        ForegroundColorSpan colorSpan = new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.black));
//        spannableStringBuilder.setSpan(colorSpan, taskDescription.indexOf("at")
//                , (taskDescription.indexOf("at") + 2), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//
//        ForegroundColorSpan colorSpan1 = new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.black));
//        spannableStringBuilder.setSpan(colorSpan1, taskDescription.indexOf("on")
//                , (taskDescription.indexOf("on") + 2), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);


        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(getSpannableString(getString(R.string.msg_task_description), ContextCompat.getColor(this, R.color.splash_gradient_end), true));
        spannableStringBuilder.append(getSpannableString(getString(R.string.label_on), ContextCompat.getColor(this, R.color.grey_varient_8), false));
        spannableStringBuilder.append(getSpannableString("25th April, 1100 hrs - 1400 hrs", ContextCompat.getColor(this, R.color.splash_gradient_end), true));
        spannableStringBuilder.append(getSpannableString(getString(R.string.label_at), ContextCompat.getColor(this, R.color.grey_varient_8), false));
        spannableStringBuilder.append(getSpannableString("1401/1402 Ghanshyam Enclave, Lalji Pada, ICICI Bank, New Link Road, Kandivali West, Mumbai, Maharashtra", ContextCompat.getColor(this, R.color.splash_gradient_end), true));


        mBinding.tvTaskDescription.setText(spannableStringBuilder);

        if (!freeList.isEmpty()) {
            mBinding.recyclerViewFree.setAdapter(new SelectedSubServicePriceAdapter(freeList));
        } else {
            mBinding.tvFreeCc.setVisibility(View.GONE);
            mBinding.recyclerViewFree.setVisibility(View.GONE);
        }

        if (!paidList.isEmpty()) {
            mBinding.recyclerViewPaid.setAdapter(new SelectedSubServicePriceAdapter(paidList));
        } else {
            mBinding.tvPaidServices.setVisibility(View.GONE);
            mBinding.recyclerViewPaid.setVisibility(View.GONE);
        }

        mBinding.ivTermsTick.setSelected(true);
        if (getIntent().hasExtra(Utility.Extra.DATA_3)) {
            if (getIntent().getBooleanExtra(Utility.Extra.DATA_3, false)) ;
//                setUpDetailsForPayLater();
//                callPaymentSummaryWS();
            else
                setUpDetailsForBooking();
        } else {
            setUpDetailsForBooking();
        }


    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////// Payment Detail Detail Service[Start] ////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public SpannableStringBuilder getSpannableString(String fullstring, int color, boolean isBold) {
        SpannableStringBuilder text = new SpannableStringBuilder(fullstring);
        text.setSpan(new ForegroundColorSpan(color), 0, fullstring.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (isBold) {
            text.setSpan(new StyleSpan(Typeface.BOLD), 0, fullstring.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return text;
    }

    /**
     * Call Payment Detail web service
     */
    private void callPaymentSummaryWS() {

        if (getIntent().hasExtra(Utility.Extra.DATA_2)) {
            //This is only when provider profile view for specific task (provider gives quote to specific task)
            taskDetailModel = (TaskDetailModel) Utility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA_2), TaskDetailModel.class);
            providerModel = (ProviderModel) Utility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA), ProviderModel.class);
        }

        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mBinding.getRoot());
            return;
        }
        showProgressDialog();
//        showProgressBar(true);

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().userID);

        //Add Params
        Map<String, Object> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.TASK_ID, taskDetailModel.taskId);
        mParams.put(NetworkUtility.TAGS.TASK_TYPE, taskDetailModel.taskType);

        //Url is based on condition if address id is greater then 0 then it means we need to update the existing address
        VolleyNetworkRequest mVolleyNetworkRequestForSPList = new VolleyNetworkRequest(NetworkUtility.WS.GET_PAYMENT_SUMMARY
                , mCallPaymentSummaryWSErrorListener
                , mCallPaymentSummaryWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForSPList, Utility.getUniqueTagForNetwork(this, NetworkUtility.WS.GET_PAYMENT_SUMMARY));
    }

    private PaymentSummaryModel paymentSummaryModel;
    Response.Listener mCallPaymentSummaryWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {

            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;
                hideProgressDialog();
//                showProgressBar(false);
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
                        paymentSummaryModel = (PaymentSummaryModel) Utility.getObjectFromJsonString(jsonObject.optString(NetworkUtility.TAGS.DATA), PaymentSummaryModel.class);
                        setUpDetailsForPayLater();
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
                mCallPaymentSummaryWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }

        }
    };

    Response.ErrorListener mCallPaymentSummaryWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(final VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
//            showProgressBar(false);
            hideProgressDialog();
            mBinding.lnPayNow.setEnabled(false);
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());

        }
    };

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////// Payment Detail WS[END] ///////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////

    private void setUpDetailsForPayLater() {

        mBinding.lnPayNow.setVisibility(View.VISIBLE);
        mBinding.tvBookAndPay.setSelected(true);

        if (taskDetailModel != null && providerModel != null) {

            mBinding.textTitle.setText(getString(R.string.title_booking_confimation));

            // top header image
            Utility.loadImageView(mContext, mBinding.imgService, taskDetailModel.catImage, R.drawable.gradient_black);

            mBinding.txtsubtotal.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf(paymentSummaryModel.subTotalAmount))));
            mBinding.txttotal.setText(getRuppeAmount(paymentSummaryModel.totalAmount));

            mBinding.lnPayNow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    payPendingAmount = true;
                    taskDetailModel.paymentSummaryModel = paymentSummaryModel;
                    PaymentChoiceActivity.newInstance(mContext, taskDetailModel);
                }
            });
        }
    }

    private String getRuppeAmount(String proPaymentAmount) {
        return getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(proPaymentAmount));
    }

    /**
     * When user selects book button
     */
    private void setUpDetailsForBooking() {
//        mBinding.lnPayNow.setVisibility(View.GONE);

        if (getIntent().hasExtra(Utility.Extra.DATA_2)) {
            //This is only when provider profile view for specific task (provider gives quote to specific task)
            taskDetailModel = (TaskDetailModel) Utility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA_2), TaskDetailModel.class);
            providerModel = (ProviderModel) Utility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA), ProviderModel.class);
            isInstaBooking = taskDetailModel.taskType.equalsIgnoreCase(Utility.TASK_TYPE.INSTA_BOOK);
        }

        if (getIntent().hasExtra(Utility.Extra.SELECTED_ADDRESS_MODEL)) {
            mSelectedAddressModelForInsta = (AddressModel) Utility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.SELECTED_ADDRESS_MODEL), AddressModel.class);
        }

        if (taskDetailModel != null && providerModel != null) {
            mBinding.textTitle.setText(getString(R.string.title_booking_confimation));

            mBinding.txtsubtotal.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf(providerModel.quotePrice))));

            // top header image
            Utility.loadImageView(mContext, mBinding.imgService, taskDetailModel.catImage, R.drawable.gradient_black);

        }
    }

    @Override
    protected void setListeners() {
        mBinding.ivTermsTick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBinding.ivTermsTick.setSelected(!mBinding.ivTermsTick.isSelected());

                // Changes are per new flow pay now/later: 15/11/17
                mBinding.tvBookAndPay.setSelected(mBinding.ivTermsTick.isSelected());
                mBinding.tvBookAndPay.setEnabled(mBinding.ivTermsTick.isSelected());
            }
        });

        mBinding.tvBookAndPay.setOnClickListener(onPayClickListener);

    }

    // Changes are per new flow pay now/later: 15/11/17
    View.OnClickListener onPayClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
//            isPayNow = true;
//            setTaskState(STEP_THREE_VERIFIED);
//            taskDetailModel.usedWalletAmount = String.valueOf(usedWalletBalance);
//
//            PaymentChoiceActivity.newInstance(mContext, taskDetailModel, providerModel, mSelectedAddressModelForInsta);
            double totalPrice;
            for (int i = 0; i < paidList.size(); i++) {

            }
            WebCallClass.createTask(mContext, mCarePackageId, mCategoryId, freeList, paidList, mAddressModel
                    , "0.00", "0.00", /*startDateTime*/"");
            TaskSummaryForMultiCatActivity.getInstance(mContext, Utility.EMPTY_STRING);
        }
    };
    // Changes are per new flow pay now/later: 15/11/17
    View.OnClickListener onBookOnlyClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            isPayNow = false;
            setTaskState(STEP_THREE_VERIFIED);

            taskDetailModel.usedWalletAmount = String.valueOf(usedWalletBalance);
            if (isInstaBooking) {
                callCreateInstaBookingTaskWS();
            } else {
                callBookProForNormalTaskWS();
            }

        }
    };

    /**
     * =======
     * >>>>>>> PaytmApiIntegrationCheepUserAndroid:app/src/main/java/com/cheep/activity/PaymentDetailsActivity.java
     * Below would manage the state of Step while creating task creation
     */
    public static final int STEP_THREE_UNVERIFIED = 8;
    public static final int STEP_THREE_VERIFIED = 9;
    public int mCurrentStep = -1;

    public void setTaskState(int step_state) {
        mCurrentStep = step_state;
        switch (step_state) {

            case STEP_THREE_UNVERIFIED:
                mBinding.textStep1.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_verified));
                mBinding.textStep1.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mBinding.textStep2.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_verified));
                mBinding.textStep2.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mBinding.textStep3.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_unverified));
                mBinding.textStep3.setTextColor(ContextCompat.getColor(mContext, R.color.splash_gradient_end));

                break;
            case STEP_THREE_VERIFIED:
                mBinding.textStep1.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_verified));
                mBinding.textStep1.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mBinding.textStep2.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_verified));
                mBinding.textStep2.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mBinding.textStep3.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_verified));
                mBinding.textStep3.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
//        if (event.BROADCAST_ACTION == Utility.BROADCAST_TYPE.TASK_PAID
//                || event.BROADCAST_ACTION == Utility.BROADCAST_TYPE.PAYMENT_COMPLETED_NEED_TO_REDIRECT_TO_MY_TASK_SCREEN) {
        switch (event.BROADCAST_ACTION) {
            case Utility.BROADCAST_TYPE.PAYMENT_COMPLETED_NEED_TO_REDIRECT_TO_MY_TASK_SCREEN:
//                finish();
                if (isPayNow) {
                    finish();
                }
                break;

            case Utility.BROADCAST_TYPE.TASK_PAID_SUCCESSFULLY:
                LogUtils.LOGE(TAG, "onMessageEvent: payPendingAmount " + payPendingAmount);
                if (payPendingAmount) {
//                    showPaymentSummary();
                    finish();
                }
                break;
            case Utility.BROADCAST_TYPE.TASK_PAID_FOR_INSTA_BOOKING:
                finish();
                break;
            case Utility.BROADCAST_TYPE.TASK_PROCESSING:
                finish();
                break;
        }
    }

    private void showPaymentSummary() {
        mBinding.textTitle.setText(getString(R.string.label_payment_summary));
        mBinding.textStepDesc.setVisibility(View.INVISIBLE);
        mBinding.lltermsandcondition.setVisibility(View.GONE);
        mBinding.lnPayNow.setVisibility(View.GONE);
        mBinding.textLabelTotalPaid.setText(getString(R.string.label_total_paid));
    }

    private void callBookProForNormalTaskWS() {
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
        mParams.put(NetworkUtility.TAGS.PAYABLE_AMOUNT, providerModel.quotePrice);
        mParams.put(NetworkUtility.TAGS.USED_WALLET_BALANCE, taskDetailModel.usedWalletAmount);
        LogUtils.LOGE(TAG, "callBookProForNormalTaskWS: quote amount" + providerModel.spWithoutGstQuotePrice);
        mParams.put(NetworkUtility.TAGS.QUOTE_AMOUNT, providerModel.spWithoutGstQuotePrice);

        if (!TextUtils.isEmpty(taskDetailModel.cheepCode)) {
            mParams.put(NetworkUtility.TAGS.CHEEPCODE, taskDetailModel.cheepCode);
            mParams.put(NetworkUtility.TAGS.PROMOCODE_PRICE, taskDetailModel.taskDiscountAmount);
        } else {
            mParams.put(NetworkUtility.TAGS.CHEEPCODE, Utility.EMPTY_STRING);
            mParams.put(NetworkUtility.TAGS.PROMOCODE_PRICE, Utility.ZERO_STRING);
        }

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
                        if (!TextUtils.isEmpty(taskDetailModel.cheepCode) && taskDetailModel.cheepCode.startsWith(Utility.COUPON_DUNIA_CODE_PREFIX)) {
                            LogUtils.LOGE(TAG, "onResponse: Appsflyer for coupon dunia*************");
                            if (!BuildConfig.BUILD_TYPE.equalsIgnoreCase("release"))
                                AppsFlyerLib.getInstance().trackEvent(mContext, NetworkUtility.TAGS.APPSFLYER_CUSTOM_TRACK_EVENTS.COUPON_DUNIA_TASK_DEBUG, mTaskCreationParams);
                            else
                                AppsFlyerLib.getInstance().trackEvent(mContext, NetworkUtility.TAGS.APPSFLYER_CUSTOM_TRACK_EVENTS.COUPON_DUNIA_TASK_LIVE, mTaskCreationParams);
                        }

//                        callTaskDetailWS();

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
                                    Utility.updateSelectedSpOnFirebase(mContext, taskDetailModel, providerModel, isInstaBooking);
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


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();

    }

    //        TASK_CREATE_INSTA_BOOKING

    private Map<String, Object> mTaskCreationParams;

    private void callCreateInstaBookingTaskWS() {

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
        mParams.put(NetworkUtility.TAGS.TASK_DESC, taskDetailModel.taskDesc);
        if (Integer.parseInt(mSelectedAddressModelForInsta.address_id) > 0) {
            mParams.put(NetworkUtility.TAGS.ADDRESS_ID, mSelectedAddressModelForInsta.address_id);
        } else {
            // In case its negative then provide other address information
            mParams = NetworkUtility.addGuestAddressParams(mParams, mSelectedAddressModelForInsta);

        }
        mParams.put(NetworkUtility.TAGS.CITY_ID, userDetails.CityID);
        mParams.put(NetworkUtility.TAGS.CAT_ID, taskDetailModel.categoryId);
        mParams.put(NetworkUtility.TAGS.START_DATETIME, taskDetailModel.taskStartdate);
        mParams.put(NetworkUtility.TAGS.SUBCATEGORY_ID, taskDetailModel.subCategoryID);
        mParams.put(NetworkUtility.TAGS.SP_USER_ID, providerModel.providerId);
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
//        mParams.put(NetworkUtility.TAGS.QUOTE_AMOUNT, TextUtils.isEmpty(cheepCode) ? providerModel.quotePrice : providerModel.actualQuotePrice);
        mParams.put(NetworkUtility.TAGS.QUOTE_AMOUNT, providerModel.spWithoutGstQuotePrice);
        mParams.put(NetworkUtility.TAGS.PAYABLE_AMOUNT, providerModel.quotePrice);
        mParams.put(NetworkUtility.TAGS.USED_WALLET_BALANCE, taskDetailModel.usedWalletAmount);
        String media_file = Utility.getSelectedMediaJsonString(taskDetailModel.mMediaModelList);
        mParams.put(NetworkUtility.TAGS.MEDIA_FILE, media_file);
        mParams.put(NetworkUtility.TAGS.TASK_TYPE, Utility.TASK_TYPE.INSTA_BOOK);

        mParams.put(NetworkUtility.TAGS.PAYMENT_LOG, Utility.EMPTY_STRING);
        mParams.put(NetworkUtility.TAGS.PAYMENT_METHOD, NetworkUtility.PAYMENT_METHOD_TYPE.PAY_LATER);
        mParams.put(NetworkUtility.TAGS.PAYMENT_STATUS, Utility.PAYMENT_STATUS.COMPLETED);

        // For AppsFlyer
        mTaskCreationParams = new HashMap<>();
        mTaskCreationParams.put(NetworkUtility.TAGS.TASK_DESC, taskDetailModel.taskDesc);
//        mParams.put(NetworkUtility.TAGS.ADDRESS_ID, taskDetailModel.taskAddressId);
        if (Integer.parseInt(mSelectedAddressModelForInsta.address_id) > 0) {
            mTaskCreationParams.put(NetworkUtility.TAGS.ADDRESS_ID, mSelectedAddressModelForInsta.address_id);
        } else {
            // In case its nagative then provide other address information
            NetworkUtility.addGuestAddressParams(mTaskCreationParams, mSelectedAddressModelForInsta);
        }
        mTaskCreationParams.put(NetworkUtility.TAGS.CITY_ID, userDetails.CityID);
        mTaskCreationParams.put(NetworkUtility.TAGS.CAT_ID, taskDetailModel.categoryId);
        mTaskCreationParams.put(NetworkUtility.TAGS.START_DATETIME, taskDetailModel.taskStartdate);
        mTaskCreationParams.put(NetworkUtility.TAGS.PAYMENT_STATUS, Utility.PAYMENT_STATUS.COMPLETED);
        mTaskCreationParams.put(NetworkUtility.TAGS.SUBCATEGORY_ID, taskDetailModel.subCategoryID);
        mTaskCreationParams.put(NetworkUtility.TAGS.SP_USER_ID, providerModel.providerId);
        mTaskCreationParams.put(NetworkUtility.TAGS.USED_WALLET_BALANCE, taskDetailModel.usedWalletAmount);
        mTaskCreationParams.put(NetworkUtility.TAGS.MEDIA_FILE, media_file);
        mTaskCreationParams.put(NetworkUtility.TAGS.TASK_TYPE, Utility.TASK_TYPE.INSTA_BOOK);
        mTaskCreationParams.put(NetworkUtility.TAGS.PAYMENT_STATUS, Utility.PAYMENT_STATUS.COMPLETED);

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
                        if (!TextUtils.isEmpty(taskDetailModel.cheepCode) && taskDetailModel.cheepCode.startsWith(Utility.COUPON_DUNIA_CODE_PREFIX))
                            if (!BuildConfig.BUILD_TYPE.equalsIgnoreCase("release"))
                                AppsFlyerLib.getInstance().trackEvent(mContext, NetworkUtility.TAGS.APPSFLYER_CUSTOM_TRACK_EVENTS.COUPON_DUNIA_TASK_DEBUG, mTaskCreationParams);
                            else
                                AppsFlyerLib.getInstance().trackEvent(mContext, NetworkUtility.TAGS.APPSFLYER_CUSTOM_TRACK_EVENTS.COUPON_DUNIA_TASK_LIVE, mTaskCreationParams);
                        Utility.onSuccessfulInstaBookingTaskCompletion(BookingConfirmationCcActivity.this, jsonObject, providerModel);
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

    // check is task is from insta booking or not

//    Blue Heart Emoji (U+1F499) - iEmoji.com
}
