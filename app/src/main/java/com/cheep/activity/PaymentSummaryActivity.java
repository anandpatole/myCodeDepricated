package com.cheep.activity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.R;
import com.cheep.adapter.SelectedSubServiceAdapter;
import com.cheep.cheepcare.model.AdminSettingModel;
import com.cheep.databinding.ActivityPaymentSummaryBinding;
import com.cheep.model.MessageEvent;
import com.cheep.model.PaymentSummaryModel;
import com.cheep.model.TaskDetailModel;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.CalendarUtility;
import com.cheep.utils.GsonUtility;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.cheep.utils.Utility.getSpannableString;


public class PaymentSummaryActivity extends BaseAppCompatActivity {

    private static final String TAG = "PaymentSummaryCheepCareActivity";
    private ActivityPaymentSummaryBinding mBinding;
    private TaskDetailModel taskDetailModel;
    private PaymentSummaryModel paymentSummaryModel;
    private String startDateTime = Utility.EMPTY_STRING;
    private String rescheduleReason = Utility.EMPTY_STRING;

    /**
     * view summary
     *
     * @param context         context
     * @param taskDetailModel task detail for summary
     */
    public static void newInstance(Context context, TaskDetailModel taskDetailModel, String startDateTime, String rescheduleReason) {
        Intent intent = new Intent(context, PaymentSummaryActivity.class);
        intent.putExtra(Utility.Extra.DATA, GsonUtility.getJsonStringFromObject(taskDetailModel));
        intent.putExtra(Utility.Extra.DATA_2, startDateTime);
        intent.putExtra(Utility.Extra.DATA_3, rescheduleReason);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
          when the device runing out of memory we dont want the user to restart the payment. rather we close it and redirect them to previous activity.
         */
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_payment_summary);
        EventBus.getDefault().register(this);
        initiateUI();
        setListeners();
    }

    //    {
//        "data": {
//        "additional_paid_amount": "0",
//        "additional_pending_amount": "0",
//        "pro_payment_amount": "250.0",
//        "pro_payment_status": "pending",
//        "wallet_balance_used": "0.00",
//        "promocode_price": "0",
//        "non_office_hours_charge": "0.00",
//        "urgent_booking_charge": "0.00",
//        "extra_charge_status": "pending",
//        "total_amount": "250",
//        "sub_total_amount": "250",
//        "total_amount_status": "pending"
//    },
//        "message": "Task Summary fetched successfully.",
//            "status": "success",
//            "status_code": 200
//    }
    @Override
    protected void initiateUI() {

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


        if (getIntent().hasExtra(Utility.Extra.DATA)) {
            //This is only when provider profile view for specific task (provider gives quote to specific task)
            taskDetailModel = (TaskDetailModel) GsonUtility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA), TaskDetailModel.class);
        }
        if (getIntent().hasExtra(Utility.Extra.DATA_2)) {
            //This is only when provider profile view for specific task (provider gives quote to specific task)
            startDateTime = getIntent().getStringExtra(Utility.Extra.DATA_2);
        }
        if (getIntent().hasExtra(Utility.Extra.DATA_3)) {
            //This is only when provider profile view for specific task (provider gives quote to specific task)
            rescheduleReason = getIntent().getStringExtra(Utility.Extra.DATA_3);
        }


        if (taskDetailModel != null) {
            mBinding.tvCatName.setText(taskDetailModel.categoryModel.catName);
            mBinding.recyclerViewPaid.setAdapter(new SelectedSubServiceAdapter(taskDetailModel.subCatList));

            callPaymentSummaryWS();
            if (startDateTime.equalsIgnoreCase(Utility.EMPTY_STRING) && taskDetailModel.selectedProvider != null) {
                mBinding.textTitle.setText(R.string.label_payment_summary);
                String dateTime = "";
                if (!TextUtils.isEmpty(startDateTime)) {
                    dateTime = CalendarUtility.getDate(Long.parseLong(startDateTime), Utility.DATE_TIME_DD_MMMM_HH_MM);
                    dateTime = dateTime.replace(getString(R.string.label_am_caps), getString(R.string.label_am_small)).replace(getString(R.string.label_pm_caps), getString(R.string.label_pm_small));
                }

                SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
                spannableStringBuilder.append(getSpannableString(taskDetailModel.subCategoryName, ContextCompat.getColor(this, R.color.splash_gradient_end), true));
                spannableStringBuilder.append(getSpannableString(getString(R.string.label_by), ContextCompat.getColor(this, R.color.grey_varient_8), false));
                spannableStringBuilder.append(getSpannableString(taskDetailModel.selectedProvider.userName, ContextCompat.getColor(this, R.color.splash_gradient_end), true));
                spannableStringBuilder.append(getSpannableString(getString(R.string.label_on), ContextCompat.getColor(this, R.color.grey_varient_8), false));
                spannableStringBuilder.append(getSpannableString(dateTime, ContextCompat.getColor(this, R.color.splash_gradient_end), true));
                spannableStringBuilder.append(getSpannableString(getString(R.string.label_at), ContextCompat.getColor(this, R.color.grey_varient_8), false));
                spannableStringBuilder.append(getSpannableString(taskDetailModel.taskAddress.address, ContextCompat.getColor(this, R.color.splash_gradient_end), true));
                mBinding.txtdesc.setText(spannableStringBuilder);
            } else {
                // address and time UI
                mBinding.textTitle.setText(R.string.booking_confirmation);
                SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
                spannableStringBuilder.append(getSpannableString(getString(R.string.msg_task_description), ContextCompat.getColor(this, R.color.splash_gradient_end), true));
                if (!TextUtils.isEmpty(startDateTime)) {
                    spannableStringBuilder.append(getSpannableString(getString(R.string.label_on), ContextCompat.getColor(this, R.color.grey_varient_8), false));
                    String datetime = CalendarUtility.getDate(Long.parseLong(startDateTime), Utility.DATE_FORMAT_DD_MMMM) + ", " + CalendarUtility.get2HourTimeSlots(startDateTime);
                    spannableStringBuilder.append(getSpannableString(datetime, ContextCompat.getColor(this, R.color.splash_gradient_end), true));
                }
                spannableStringBuilder.append(getSpannableString(getString(R.string.label_at), ContextCompat.getColor(this, R.color.grey_varient_8), false));
                spannableStringBuilder.append(getSpannableString(taskDetailModel.taskAddress.getAddressWithInitials(), ContextCompat.getColor(this, R.color.splash_gradient_end), true));


                mBinding.txtdesc.setText(spannableStringBuilder);
            }
        }

    }


    /**
     * Used for Set Payment VAlue When User only Preview.]
     * Payment summary screen for task summary
     */

    public void viewPaymentDetails() {

        AdminSettingModel adminSettingModel = PreferenceUtility.getInstance(this).getAdminSettings();
        double catPrice = 0;
        double additionalCharges = 0;
        double nonWorkingHourCharges = 0;
        double urgentBookingcharges = 0;
        double subTotal = 0;
        double total = 0;
        if (paymentSummaryModel != null) {
            if (!startDateTime.equalsIgnoreCase(Utility.EMPTY_STRING)) {


                try {
                    catPrice = Double.valueOf(taskDetailModel.categoryModel.catNewPrice);

                    mBinding.txtadditionalcharge.setText(getRuppeAmount(paymentSummaryModel.additionalPaidAmount));
                    mBinding.textLabelTotalPaid.setText(getString(R.string.label_total_pay));
                    mBinding.txtprofee.setText(getRuppeAmount(taskDetailModel.categoryModel.catNewPrice));

                    if (rescheduleReason.equalsIgnoreCase(Utility.ADDITION_CHARGES_DIALOG_TYPE.OUT_OF_OFFICE_HOURS)) {
                        mBinding.rladditionalOutOfOffice.setVisibility(View.VISIBLE);
                        mBinding.viewOutOfOffice.setVisibility(View.VISIBLE);
                        mBinding.rladditionalUrgentBooking.setVisibility(View.GONE);
                        mBinding.viewUrgentBooking.setVisibility(View.GONE);
                        mBinding.txtOutOfOfficeCharges.setText(getRuppeAmount(adminSettingModel.additionalChargeForSelectingSpecificTime));
                        nonWorkingHourCharges = Double.valueOf(adminSettingModel.additionalChargeForSelectingSpecificTime);
                        paymentSummaryModel.nonOfficeHoursCharge = String.valueOf(nonWorkingHourCharges);
                    } else {
                        mBinding.rladditionalOutOfOffice.setVisibility(View.GONE);
                        mBinding.viewOutOfOffice.setVisibility(View.GONE);
                        mBinding.rladditionalUrgentBooking.setVisibility(View.VISIBLE);
                        mBinding.viewUrgentBooking.setVisibility(View.VISIBLE);
                        mBinding.txtUrgentBookingCharges.setText(getRuppeAmount(adminSettingModel.additionalChargeForSelectingSpecificTime));
                        urgentBookingcharges = Double.valueOf(adminSettingModel.additionalChargeForSelectingSpecificTime);
                        paymentSummaryModel.urgentBookingCharge = String.valueOf(urgentBookingcharges);
                    }
                    mBinding.txtadditionalcharge.setText(getRuppeAmount(paymentSummaryModel.additionalPaidAmount));

                    if (paymentSummaryModel.proPaymentStatus.equalsIgnoreCase(Utility.PAYMENT_STATUS.PENDING)) {
                        subTotal += catPrice;
                    }
                    subTotal += additionalCharges;
                    subTotal += nonWorkingHourCharges;
                    subTotal += urgentBookingcharges;
                    total = subTotal;

                    mBinding.txtsubtotal.setText(getRuppeAmount(String.valueOf(subTotal)));
                    mBinding.txttotal.setText(getRuppeAmount(String.valueOf(total)));
                    paymentSummaryModel.subTotalAmount = String.valueOf(subTotal);
                    paymentSummaryModel.totalAmount = String.valueOf(total);

                    mBinding.tvPaynow.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                mBinding.txtprofee.setText(getRuppeAmount(taskDetailModel.categoryModel.catNewPrice));
                mBinding.txtadditionalcharge.setText(getRuppeAmount(paymentSummaryModel.additionalPaidAmount));

                nonWorkingHourCharges = Double.parseDouble(paymentSummaryModel.nonOfficeHoursCharge);
                urgentBookingcharges = Double.parseDouble(paymentSummaryModel.urgentBookingCharge);

                if (nonWorkingHourCharges > 0) {
                    mBinding.rladditionalOutOfOffice.setVisibility(View.VISIBLE);
                    mBinding.viewOutOfOffice.setVisibility(View.VISIBLE);
                    mBinding.rladditionalUrgentBooking.setVisibility(View.GONE);
                    mBinding.viewUrgentBooking.setVisibility(View.GONE);
                    mBinding.txtOutOfOfficeCharges.setText(getRuppeAmount(paymentSummaryModel.nonOfficeHoursCharge));
                } else {
                    mBinding.rladditionalOutOfOffice.setVisibility(View.GONE);
                    mBinding.viewOutOfOffice.setVisibility(View.GONE);
                    mBinding.rladditionalUrgentBooking.setVisibility(View.VISIBLE);
                    mBinding.viewUrgentBooking.setVisibility(View.VISIBLE);
                    mBinding.txtUrgentBookingCharges.setText(getRuppeAmount(paymentSummaryModel.urgentBookingCharge));
                }
                if (paymentSummaryModel.proPaymentStatus.equalsIgnoreCase(Utility.PAYMENT_STATUS.PENDING)) {
                    subTotal += catPrice;
                }
                if (paymentSummaryModel.extraChargeStatus.equalsIgnoreCase(Utility.PAYMENT_STATUS.PENDING)) {
                    subTotal += nonWorkingHourCharges;
                    subTotal += urgentBookingcharges;
                }
                subTotal += additionalCharges;
                subTotal += nonWorkingHourCharges;
                subTotal += urgentBookingcharges;
                total = subTotal;

                if (paymentSummaryModel.totalAmountStatus.equalsIgnoreCase(Utility.TASK_STATUS.PENDING)) {
                    mBinding.txtsubtotal.setText(getRuppeAmount(String.valueOf(subTotal)));
                    mBinding.txttotal.setText(getRuppeAmount(String.valueOf(total)));
                    mBinding.textLabelTotalPaid.setText(getString(R.string.label_total_pay));
                    mBinding.tvPaynow.setVisibility(View.VISIBLE);

                } else {

                    mBinding.txtsubtotal.setText(getRuppeAmount(paymentSummaryModel.subTotalAmount));
                    mBinding.txttotal.setText(getRuppeAmount(paymentSummaryModel.totalAmount));
                    mBinding.textLabelTotalPaid.setText(getString(R.string.label_total_paid));

                    paymentSummaryModel.subTotalAmount = String.valueOf(subTotal);
                    paymentSummaryModel.totalAmount = String.valueOf(total);
                    mBinding.tvPaynow.setVisibility(View.GONE);
                }

//            mBinding.txtreferraldiscount.setText(getRuppeAmount(paymentSummaryModel.walletBalanceUsed));
//            mBinding.txtpromocode.setText(getRuppeAmount(paymentSummaryModel.promoCodePrice));

//            if (getQuotePriceInInteger(paymentSummaryModel.walletBalanceUsed) > 0) {
//                mBinding.lnPromoCodeDisclaimer.setVisibility(View.VISIBLE);
//                mBinding.txtPromoCodeDisclaimer.setText(R.string.disclaimer_referral);
//            } else if (getQuotePriceInInteger(paymentSummaryModel.promoCodePrice) > 0) {
//                mBinding.lnPromoCodeDisclaimer.setVisibility(View.VISIBLE);
//                mBinding.txtPromoCodeDisclaimer.setText(R.string.disclaimer_promo_code);
//            } else {
//                mBinding.lnPromoCodeDisclaimer.setVisibility(View.GONE);
//            }

            }
        }
    }

    private String getRuppeAmount(String proPaymentAmount) {
        return getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(proPaymentAmount));
    }

    public Double getQuotePriceInInteger(String quotePrice) {
        if (quotePrice == null) {
            return -1.0;
        }
        try {
            return Double.parseDouble(quotePrice);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    @Override
    protected void setListeners() {
        mBinding.tvPaynow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                taskDetailModel.paymentSummaryModel = paymentSummaryModel;
                taskDetailModel.taskTotalPendingAmount = paymentSummaryModel.totalAmount;
                String startDatetime = !startDateTime.equalsIgnoreCase(Utility.EMPTY_STRING) ? startDateTime : Utility.ZERO_STRING;
                PaymentChoiceActivity.newInstance(mContext, taskDetailModel, startDatetime);
            }
        });
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////// Payment Detail Detail Service[Start] ////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Call Task Detail web service
     */
    private void callPaymentSummaryWS() {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mBinding.getRoot());
            return;
        }

        showProgressBar(true);

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

    Response.Listener mCallPaymentSummaryWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {

            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;
                showProgressBar(false);
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
                        paymentSummaryModel = (PaymentSummaryModel) GsonUtility.getObjectFromJsonString(jsonObject.optString(NetworkUtility.TAGS.DATA), PaymentSummaryModel.class);
                        viewPaymentDetails();
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
            showProgressBar(false);

            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());

        }
    };

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////// Payment Detail WS[END] ///////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////

    private void showProgressBar(boolean flag) {
        mBinding.progress.setVisibility(flag ? View.VISIBLE : View.GONE);
        mBinding.lnRoot.setVisibility(flag ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.GET_PAYMENT_SUMMARY);
        super.onDestroy();
    }

    /**
     * Event Bus Callbacks
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        Log.d(TAG, "onMessageEvent() called with: event = [" + event.BROADCAST_ACTION + "]");
        switch (event.BROADCAST_ACTION) {
            case Utility.BROADCAST_TYPE.TASK_PAID_FOR_INSTA_BOOKING:
                finish();
                break;
        }
    }
}
