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

import com.android.volley.VolleyError;
import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.cheepcare.adapter.SelectedSubServicePriceAdapter;
import com.cheep.cheepcare.dialogs.TaskConfirmedCCInstaBookDialog;
import com.cheep.cheepcare.model.SubscribedTaskDetailModel;
import com.cheep.databinding.ActivityBookingConfirmationCcBinding;
import com.cheep.model.MessageEvent;
import com.cheep.model.SubServiceDetailModel;
import com.cheep.network.NetworkUtility;
import com.cheep.utils.CalendarUtility;
import com.cheep.utils.GlideUtility;
import com.cheep.utils.GsonUtility;
import com.cheep.utils.LogUtils;
import com.cheep.utils.SuperCalendar;
import com.cheep.utils.Utility;
import com.cheep.utils.WebCallClass;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.math.BigDecimal;


public class BookingConfirmationCcActivity extends BaseAppCompatActivity {


    private ActivityBookingConfirmationCcBinding mBinding;
    //    private String mCategoryId;
    private SubscribedTaskDetailModel subscribedTaskDetailModel;
    boolean isTaskExcessLimitFeesApplied = false;

    public static void newInstance(Context context, SubscribedTaskDetailModel subscribedTaskDetailModel) {
        Intent intent = new Intent(context, BookingConfirmationCcActivity.class);
        intent.putExtra(Utility.Extra.DATA, GsonUtility.getJsonStringFromObject(subscribedTaskDetailModel));
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

        // get data of task specification screen
//        if (getIntent().getExtras() != null && getIntent().hasExtra(Utility.Extra.SELECTED_PACKAGE_ID))
//            mCarePackageId = getIntent().getExtras().getString(Utility.Extra.SELECTED_PACKAGE_ID);
//        if (getIntent().getExtras() != null && getIntent().hasExtra(Utility.Extra.CATEGORY_DATA))
//            jobCategoryModel = (JobCategoryModel) GsonUtility.getObjectFromJsonString(getIntent().getExtras().getString(Utility.Extra.CATEGORY_DATA), JobCategoryModel.class);
//        if (getIntent().getExtras() != null && getIntent().hasExtra(Utility.Extra.DATA))
//            freeList = (ArrayList<SubServiceDetailModel>) getIntent().getExtras().getSerializable(Utility.Extra.DATA);
//        if (getIntent().getExtras() != null && getIntent().hasExtra(Utility.Extra.DATA_2))
//            paidList = (ArrayList<SubServiceDetailModel>) getIntent().getExtras().getSerializable(Utility.Extra.DATA_2);
//        if (getIntent().getExtras() != null && getIntent().hasExtra(Utility.Extra.SELECTED_ADDRESS_MODEL))
//            mAddressModel = (AddressModel) getIntent().getSerializableExtra(Utility.Extra.SELECTED_ADDRESS_MODEL);
//        if (getIntent().getExtras() != null && getIntent().hasExtra(Utility.Extra.START_DATETIME))
//            startDateTime = getIntent().getExtras().getString(Utility.Extra.START_DATETIME);
//        if (getIntent().getExtras() != null && getIntent().hasExtra(Utility.Extra.DATA_3))
//            taskDes = getIntent().getExtras().getString(Utility.Extra.DATA_3);
//        if (getIntent().getExtras() != null && getIntent().hasExtra(Utility.Extra.TASK_TYPE))
//            taskType = getIntent().getExtras().getString(Utility.Extra.TASK_TYPE);
//        if (getIntent().getExtras() != null && getIntent().hasExtra(Utility.Extra.ADMIN_SETTING))
//            adminSettingModel = (AdminSettingModel) GsonUtility.getObjectFromJsonString(getIntent().getExtras().getString(Utility.Extra.ADMIN_SETTING), AdminSettingModel.class);

        if (getIntent().getExtras() != null && getIntent().hasExtra(Utility.Extra.DATA))
            subscribedTaskDetailModel = (SubscribedTaskDetailModel) GsonUtility.getObjectFromJsonString(getIntent().getExtras().getString(Utility.Extra.DATA), SubscribedTaskDetailModel.class);


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
        if (!TextUtils.isEmpty(subscribedTaskDetailModel.startDateTime)) {
            spannableStringBuilder.append(getSpannableString(getString(R.string.label_on), ContextCompat.getColor(this, R.color.grey_varient_8), false));
            String datetime = CalendarUtility.getDate(Long.parseLong(subscribedTaskDetailModel.startDateTime), Utility.DATE_FORMAT_DD_MMMM) + ", " + CalendarUtility.get2HourTimeSlots(subscribedTaskDetailModel.startDateTime);
            spannableStringBuilder.append(getSpannableString(datetime, ContextCompat.getColor(this, R.color.splash_gradient_end), true));
        }
        spannableStringBuilder.append(getSpannableString(getString(R.string.label_at), ContextCompat.getColor(this, R.color.grey_varient_8), false));
        spannableStringBuilder.append(getSpannableString(subscribedTaskDetailModel.addressModel.getAddressWithInitials(), ContextCompat.getColor(this, R.color.splash_gradient_end), true));

        mBinding.tvTaskDescription.setText(spannableStringBuilder);

        // banner image of cat
        GlideUtility.loadImageView(mContext, mBinding.imgService, subscribedTaskDetailModel.jobCategoryModel.catImageExtras.original, R.drawable.gradient_black);


        // free service list
        if (!subscribedTaskDetailModel.freeServiceList.isEmpty()) {
            mBinding.recyclerViewFree.setAdapter(new SelectedSubServicePriceAdapter(subscribedTaskDetailModel.freeServiceList
                    , subscribedTaskDetailModel.taskType.equalsIgnoreCase(Utility.TASK_TYPE.SUBSCRIBED) ?
                    Utility.SERVICE_TYPE.FREE : Utility.SERVICE_TYPE.PAID));
        } else {
            mBinding.tvFreeCc.setVisibility(View.GONE);
            mBinding.recyclerViewFree.setVisibility(View.GONE);
        }


        // paid service list
        if (!subscribedTaskDetailModel.paidServiceList.isEmpty()) {
            mBinding.recyclerViewPaid.setAdapter(new SelectedSubServicePriceAdapter(subscribedTaskDetailModel.paidServiceList, Utility.SERVICE_TYPE.PAID));
        } else {
            mBinding.tvPaidServices.setVisibility(View.GONE);
            mBinding.recyclerViewPaid.setVisibility(View.GONE);
        }

        //
        mBinding.ivTermsTick.setSelected(true);
        setPayButtonSelection();


        // calculation of non working hour fees & task excess limit count;

        /*
         when its normal task non working hour fees will be applicable only when user has selected time beyound given slot like from 10 am to 7 pm.
        this time slot will come from backed (cms)
        for cheep care time non working hour fees will be applicable only when user has selected any time. (there will be no limitation of time slot)
        */

        boolean isNonWorkingHourFeesApplied = false;
        if (subscribedTaskDetailModel.taskType.equalsIgnoreCase(Utility.TASK_TYPE.NORMAL)) {
            long timeStamp = Long.parseLong(subscribedTaskDetailModel.startDateTime);
            SuperCalendar superCalendar = SuperCalendar.getInstance();
            superCalendar.setTimeInMillis(timeStamp);
            String time = superCalendar.format(SuperCalendar.SuperFormatter.FULL_DATE);
            boolean isWorkingTime = superCalendar.isWorkingHour(subscribedTaskDetailModel.adminSettingModel.starttime,
                    subscribedTaskDetailModel.adminSettingModel.endtime);
            LogUtils.LOGE(TAG, "initiateUI: time " + time + "\n" + isWorkingTime);

            LogUtils.LOGE(TAG, "initiateUI: isWorkingTime " + isWorkingTime);


            if (isWorkingTime) {
                subscribedTaskDetailModel.nonWorkingHourFees = 0;
                mBinding.llNonWorkingHourFree.setVisibility(View.GONE);
            } else {
                isNonWorkingHourFeesApplied = true;
                mBinding.llNonWorkingHourFree.setVisibility(View.VISIBLE);
                subscribedTaskDetailModel.nonWorkingHourFees = Double.valueOf(subscribedTaskDetailModel.adminSettingModel.additionalChargeForSelectingSpecificTime);
                mBinding.tvNonWorkingHourCharges.setText(getString(R.string.rupee_symbol_x, new BigDecimal(subscribedTaskDetailModel.nonWorkingHourFees).toString()));
            }
        } else {
            if (!TextUtils.isEmpty(subscribedTaskDetailModel.startDateTime)) {
                isNonWorkingHourFeesApplied = true;
                mBinding.llNonWorkingHourFree.setVisibility(View.VISIBLE);
                subscribedTaskDetailModel.nonWorkingHourFees = Double.valueOf(subscribedTaskDetailModel.adminSettingModel.additionalChargeForSelectingSpecificTime);
                mBinding.tvNonWorkingHourCharges.setText(getString(R.string.rupee_symbol_x, new BigDecimal(subscribedTaskDetailModel.nonWorkingHourFees).toString()));
            } else {
                subscribedTaskDetailModel.nonWorkingHourFees = 0;
                mBinding.llNonWorkingHourFree.setVisibility(View.GONE);
            }
        }

        int limitCount = 0;
        try {
            limitCount = Integer.valueOf(subscribedTaskDetailModel.addressModel.limit_cnt);
            LogUtils.LOGE(TAG, "initiateUI: limit_cnt" + limitCount);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (subscribedTaskDetailModel.taskType.equalsIgnoreCase(Utility.TASK_TYPE.SUBSCRIBED) && limitCount < 1) {
            WebCallClass.getExtraChargeAfterExceedLimit(this, subscribedTaskDetailModel.carePackageId, subscribedTaskDetailModel.addressModel.address_id, mCommonResponseListener, new WebCallClass.GetExtraChargeAfterExceedLimitListener() {
                @Override
                public void getFinalExtraCharge(String finalExtraCharge) {
                    subscribedTaskDetailModel.taskExcessLimitFees = Double.valueOf(finalExtraCharge);
                    mBinding.llExcessLimitFee.setVisibility(View.VISIBLE);
                    mBinding.tvTaskExcessLimitCharges.setText(getString(R.string.rupee_symbol_x, new BigDecimal(subscribedTaskDetailModel.taskExcessLimitFees).toString()));
                    isTaskExcessLimitFeesApplied = true;
                }
            });

        } else {
            subscribedTaskDetailModel.taskExcessLimitFees = 0;
            mBinding.llExcessLimitFee.setVisibility(View.GONE);
        }

        mBinding.tvAdditionalCharges.setVisibility(isNonWorkingHourFeesApplied || isTaskExcessLimitFeesApplied ? View.VISIBLE : View.GONE);
        mBinding.viewLine1.setVisibility(isNonWorkingHourFeesApplied || isTaskExcessLimitFeesApplied ? View.VISIBLE : View.GONE);
        mBinding.viewLine1.setVisibility(isNonWorkingHourFeesApplied || isTaskExcessLimitFeesApplied ? View.VISIBLE : View.GONE);

        if (!subscribedTaskDetailModel.taskType.equalsIgnoreCase(Utility.TASK_TYPE.SUBSCRIBED)) {
            subscribedTaskDetailModel.freeServiceTotal = 0;
            for (SubServiceDetailModel subServiceDetailModel : subscribedTaskDetailModel.freeServiceList) {
                subscribedTaskDetailModel.freeServiceTotal += subServiceDetailModel.selected_unit * Double.parseDouble(subServiceDetailModel.unitPrice);
            }
        }

        subscribedTaskDetailModel.paidServiceTotal = 0;
        for (SubServiceDetailModel subServiceDetailModel : subscribedTaskDetailModel.paidServiceList) {
            subscribedTaskDetailModel.paidServiceTotal += subServiceDetailModel.selected_unit * Double.parseDouble(subServiceDetailModel.unitPrice);
        }

        subscribedTaskDetailModel.subtotal = subscribedTaskDetailModel.freeServiceTotal
                + subscribedTaskDetailModel.paidServiceTotal
                + subscribedTaskDetailModel.nonWorkingHourFees
                + subscribedTaskDetailModel.taskExcessLimitFees;
        subscribedTaskDetailModel.total = subscribedTaskDetailModel.subtotal;

        mBinding.tvSubTotal.setText(getString(R.string.rupee_symbol_x, String.valueOf(subscribedTaskDetailModel.subtotal)));

        mBinding.tvTotal.setText(getString(R.string.rupee_symbol_x, String.valueOf(subscribedTaskDetailModel.total)));

        if (subscribedTaskDetailModel.taskType.equalsIgnoreCase(Utility.TASK_TYPE.SUBSCRIBED)) {
            mBinding.tvBookAndPay.setVisibility(View.VISIBLE);
            mBinding.lnPayLaterPayNowButtons.setVisibility(View.GONE);
        } else {
            mBinding.tvBookAndPay.setVisibility(View.GONE);
            mBinding.lnPayLaterPayNowButtons.setVisibility(View.VISIBLE);
        }

    }

    @Override
    protected void setListeners() {
        mBinding.tvBookAndPay.setOnClickListener(onPayClickListener);
        mBinding.rlPayNow.setOnClickListener(onPayClickListener);
        mBinding.rlPayLater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callCreateSubscribedTask();
            }
        });


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

    View.OnClickListener onPayClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (subscribedTaskDetailModel.total == 0) {
                callCreateSubscribedTask();
            } else {
                openPaymentChoiceActivity();
            }
        }
    };

    private void openPaymentChoiceActivity() {
        PaymentChoiceCheepCareActivity.newInstance(BookingConfirmationCcActivity.this, subscribedTaskDetailModel);
    }

    private void callCreateSubscribedTask() {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mBinding.getRoot());
            return;
        }
        showProgressDialog();
        subscribedTaskDetailModel.paymentMethod = NetworkUtility.PAYMENT_METHOD_TYPE.FREE;

        final String message;
        if (!TextUtils.isEmpty(subscribedTaskDetailModel.startDateTime)) {
            String datetime = CalendarUtility.getDate(Long.parseLong(subscribedTaskDetailModel.startDateTime), Utility.DATE_FORMAT_DD_MMMM) + getString(R.string.label_between) + CalendarUtility.get2HourTimeSlots(subscribedTaskDetailModel.startDateTime);
            message = getString(R.string.msg_task_confirmed_cheep_care, datetime, "3");
        } else {
            message = getString(R.string.msg_task_confirmed_cheep_care_no_time_specified);
        }

        WebCallClass.createTask(mContext, subscribedTaskDetailModel, mCommonResponseListener, new WebCallClass.SuccessOfTaskCreationResponseListener() {
            @Override
            public void onSuccessOfTaskCreate() {
                hideProgressDialog();
                LogUtils.LOGE(TAG, "onSuccessOfTaskCreate: ");
                TaskConfirmedCCInstaBookDialog taskConfirmedCCInstaBookDialog = TaskConfirmedCCInstaBookDialog.newInstance(
                        new TaskConfirmedCCInstaBookDialog.TaskConfirmActionListener() {
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
                        }, message);
                taskConfirmedCCInstaBookDialog.show(getSupportFragmentManager(), TaskConfirmedCCInstaBookDialog.TAG);
            }
        });
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

    private static final String TAG = "BookingConfirmationCcAc";

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
