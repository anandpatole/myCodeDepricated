package com.cheep.activity;

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
import com.cheep.R;
import com.cheep.cheepcare.adapter.SelectedSubServicePriceAdapter;
import com.cheep.databinding.ActivityBookingConfirmationCcBinding;
import com.cheep.model.AddressModel;
import com.cheep.model.MessageEvent;
import com.cheep.model.SubServiceDetailModel;
import com.cheep.model.TaskDetailModel;
import com.cheep.model.UserDetails;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.CalendarUtility;
import com.cheep.utils.GlideUtility;
import com.cheep.utils.GsonUtility;
import com.cheep.utils.LogUtils;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;
import com.cheep.utils.WebCallClass;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class BookingConfirmationInstaActivity extends BaseAppCompatActivity {


    private ActivityBookingConfirmationCcBinding mBinding;
    private TaskDetailModel taskDetailModel;
    private AddressModel mSelectedAddressModel;
    private double total;
    private double subTotal;
    private static final String TAG = LogUtils.makeLogTag(BookingConfirmationInstaActivity.class);
    //    private String mCategoryId;

    public static void newInstance(Context context, TaskDetailModel taskDetailModel, AddressModel mSelectedAddressModel) {
        Intent intent = new Intent(context, BookingConfirmationInstaActivity.class);
        intent.putExtra(Utility.Extra.DATA, GsonUtility.getJsonStringFromObject(taskDetailModel));
        intent.putExtra(Utility.Extra.SELECTED_ADDRESS_MODEL, GsonUtility.getJsonStringFromObject(mSelectedAddressModel));
        context.startActivity(intent);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_booking_confirmation_cc);

        // add event bus listener
        EventBus.getDefault().register(this);
        initiateUI();
        setListeners();
    }

    @Override
    protected void initiateUI() {


        if (getIntent().hasExtra(Utility.Extra.DATA)) {
            //This is only when provider profile view for specific task (provider gives quote to specific task)
            taskDetailModel = (TaskDetailModel) GsonUtility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA), TaskDetailModel.class);
        }

        if (getIntent().hasExtra(Utility.Extra.SELECTED_ADDRESS_MODEL)) {
            mSelectedAddressModel = (AddressModel) GsonUtility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.SELECTED_ADDRESS_MODEL), AddressModel.class);
        }

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

        // address and time UI
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(getSpannableString(getString(R.string.msg_task_description), ContextCompat.getColor(this, R.color.splash_gradient_end), true));
        if (!TextUtils.isEmpty(taskDetailModel.taskStartdate)) {
            spannableStringBuilder.append(getSpannableString(getString(R.string.label_on), ContextCompat.getColor(this, R.color.grey_varient_8), false));
            String datetime = CalendarUtility.getDate(Long.parseLong(taskDetailModel.taskStartdate), Utility.DATE_FORMAT_DD_MMMM) + ", " + CalendarUtility.get2HourTimeSlots(taskDetailModel.taskStartdate);
            spannableStringBuilder.append(getSpannableString(datetime, ContextCompat.getColor(this, R.color.splash_gradient_end), true));
        }
        spannableStringBuilder.append(getSpannableString(getString(R.string.label_at), ContextCompat.getColor(this, R.color.grey_varient_8), false));
        spannableStringBuilder.append(getSpannableString(mSelectedAddressModel.getAddressWithInitials(), ContextCompat.getColor(this, R.color.splash_gradient_end), true));

        mBinding.tvTaskDescription.setText(spannableStringBuilder);

        // banner image of cat
        GlideUtility.loadImageView(mContext, mBinding.imgService, taskDetailModel.categoryModel.catImageExtras.original, R.drawable.gradient_black);


        // free service list

        // paid service list
        if (!taskDetailModel.subCatList.isEmpty()) {
            mBinding.recyclerViewPaid.setAdapter(new SelectedSubServicePriceAdapter(taskDetailModel.subCatList, Utility.SERVICE_TYPE.PAID));
        } else {
            mBinding.tvPaidServices.setVisibility(View.GONE);
            mBinding.recyclerViewPaid.setVisibility(View.GONE);
        }

        //
        mBinding.ivTermsTick.setSelected(true);

        setPayButtonSelection();


        mBinding.llNonWorkingHourFree.setVisibility(View.GONE);
        mBinding.llExcessLimitFee.setVisibility(View.GONE);

        mBinding.tvAdditionalCharges.setVisibility(View.GONE);
        mBinding.viewLine1.setVisibility(View.GONE);
        mBinding.viewLine1.setVisibility(View.GONE);


        double subServiceTotal = 0;
        for (SubServiceDetailModel subServiceDetailModel : taskDetailModel.subCatList) {
            subServiceTotal += subServiceDetailModel.selected_unit * Double.parseDouble(subServiceDetailModel.unitPrice);
        }
        subTotal = subServiceTotal;
        total = subTotal;

        mBinding.tvSubTotal.setText(getString(R.string.rupee_symbol_x, String.valueOf(subTotal)));

        mBinding.tvTotal.setText(getString(R.string.rupee_symbol_x, String.valueOf(total)));

        mBinding.tvBookAndPay.setVisibility(View.GONE);
        mBinding.lnPayLaterPayNowButtons.setVisibility(View.VISIBLE);

    }

    @Override
    protected void setListeners() {
        mBinding.rlPayNow.setOnClickListener(onPayNowClickListener);
        mBinding.rlPayLater.setOnClickListener(onPayLaterClickListener);

        mBinding.ivTermsTick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBinding.ivTermsTick.setSelected(!mBinding.ivTermsTick.isSelected());

                // Changes are per new flow pay now/later: 15/11/17
                setPayButtonSelection();
            }
        });


    }

    private void setPayButtonSelection() {
        mBinding.rlPayLater.setSelected(mBinding.ivTermsTick.isSelected());
        mBinding.rlPayNow.setSelected(mBinding.ivTermsTick.isSelected());
        mBinding.rlPayLater.setEnabled(mBinding.ivTermsTick.isSelected());
        mBinding.rlPayNow.setEnabled(mBinding.ivTermsTick.isSelected());
        mBinding.tvBookAndPay.setSelected(mBinding.ivTermsTick.isSelected());
        mBinding.tvBookAndPay.setEnabled(mBinding.ivTermsTick.isSelected());
    }

    View.OnClickListener onPayLaterClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            callCreateInstaTaskBooking();
        }

    };
    View.OnClickListener onPayNowClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            openPaymentChoiceActivity();
        }
    };


    /**
     * TODO: Add apps flyer events for task creation and also add event for coupan dunia code
     */

    private void callCreateInstaTaskBooking() {

        // Check Internet connection
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mBinding.getRoot());
            return;
        }

        showProgressDialog();

        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();

        // Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, userDetails.userID);
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());

        // Add Params
        Map<String, Object> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.TASK_DESC, taskDetailModel.taskDesc);
        if (Integer.parseInt(mSelectedAddressModel.address_id) > 0) {
            mParams.put(NetworkUtility.TAGS.ADDRESS_ID, mSelectedAddressModel.address_id);
        } else {
            // In case its negative then provide other address information
            mParams = NetworkUtility.addGuestAddressParams(mParams, mSelectedAddressModel);

        }
        mParams.put(NetworkUtility.TAGS.CITY_ID, userDetails.CityID);
        mParams.put(NetworkUtility.TAGS.CAT_ID, taskDetailModel.categoryModel.catId);
        mParams.put(NetworkUtility.TAGS.START_DATETIME, taskDetailModel.taskStartdate);
//        mParams.put(NetworkUtility.TAGS.SUBCATEGORY_ID, taskDetailModel.subCategoryID);
        String selectedServices = new Gson().toJson(taskDetailModel.subCatList);
        mParams.put(NetworkUtility.TAGS.TASK_SUB_CATEGORIES, selectedServices);
        //        mParams.put(NetworkUtility.TAGS.SP_USER_ID, providerModel.providerId);
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
        mParams.put(NetworkUtility.TAGS.IS_REFER_CODE, Utility.BOOLEAN.NO);
//        mParams.put(NetworkUtility.TAGS.QUOTE_AMOUNT, TextUtils.isEmpty(cheepCode) ? providerModel.quotePrice : providerModel.actualQuotePrice);
        mParams.put(NetworkUtility.TAGS.QUOTE_AMOUNT, total);
        mParams.put(NetworkUtility.TAGS.PAYABLE_AMOUNT, total);
        mParams.put(NetworkUtility.TAGS.USED_WALLET_BALANCE, Utility.ZERO_STRING);
        String media_file = Utility.getSelectedMediaJsonString(taskDetailModel.mMediaModelList);
        mParams.put(NetworkUtility.TAGS.MEDIA_FILE, media_file);
        mParams.put(NetworkUtility.TAGS.TASK_TYPE, Utility.TASK_TYPE.INSTA_BOOK);

        mParams.put(NetworkUtility.TAGS.PAYMENT_LOG, Utility.EMPTY_STRING);
        mParams.put(NetworkUtility.TAGS.PAYMENT_METHOD, NetworkUtility.PAYMENT_METHOD_TYPE.PAY_LATER);
        mParams.put(NetworkUtility.TAGS.PAYMENT_STATUS, Utility.PAYMENT_STATUS.COMPLETED);


        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.CREATE_TASK
                , mCallBookProForNormalTaskWSErrorListener
                , mCallCreateInstaTaskWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest);
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
//                        AppsFlyerLib.getInstance().trackEvent(mContext, NetworkUtility.TAGS.APPSFLYER_CUSTOM_TRACK_EVENTS.TASK_CREATE, mTaskCreationParams);
//                        if (!TextUtils.isEmpty(taskDetailModel.cheepCode) && taskDetailModel.cheepCode.startsWith(Utility.COUPON_DUNIA_CODE_PREFIX))
//                            if (!BuildConfig.BUILD_TYPE.equalsIgnoreCase("release"))
//                                AppsFlyerLib.getInstance().trackEvent(mContext, NetworkUtility.TAGS.APPSFLYER_CUSTOM_TRACK_EVENTS.COUPON_DUNIA_TASK_DEBUG, mTaskCreationParams);
//                            else
//                                AppsFlyerLib.getInstance().trackEvent(mContext, NetworkUtility.TAGS.APPSFLYER_CUSTOM_TRACK_EVENTS.COUPON_DUNIA_TASK_LIVE, mTaskCreationParams);
//                        Utility.onSuccessfulInstaBookingTaskCompletion(PaymentDetailsActivity.this, jsonObject, providerModel);

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

    private void openPaymentChoiceActivity() {
//        PaymentChoiceCheepCareActivity.newInstance(BookingConfirmationInstaActivity.this, subscribedTaskDetailModel);
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
        LogUtils.LOGE(TAG, "onMessageEvent: " + event.BROADCAST_ACTION);
        if (event.BROADCAST_ACTION == Utility.BROADCAST_TYPE.SUBSCRIBED_TASK_CREATE_SUCCESSFULLY) {
            finish();
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
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

    private final WebCallClass.CommonResponseListener mErrorListnerForExcessLimitFees =
            new WebCallClass.CommonResponseListener() {
                @Override
                public void volleyError(VolleyError error) {
                    Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");
                    hideProgressDialog();
                    Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
                    mBinding.tvBookAndPay.setEnabled(false);
                }

                @Override
                public void showSpecificMessage(String message) {
                    hideProgressDialog();
                    // Show message
                    Utility.showSnackBar(message, mBinding.getRoot());
                    mBinding.tvBookAndPay.setEnabled(false);
                }

                @Override
                public void forceLogout() {
                    hideProgressDialog();
                    finish();
                }
            };


}
