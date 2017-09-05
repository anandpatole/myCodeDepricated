package com.cheep.strategicpartner;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;

import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.databinding.ActivityPaymentDetailStartegicPartnerBinding;
import com.cheep.model.TaskDetailModel;
import com.cheep.utils.SuperCalendar;
import com.cheep.utils.Utility;


public class PaymentsStepStrategicPartnerActivity extends BaseAppCompatActivity {

    public static void newInstance(Context context, TaskDetailModel taskDetailModel) {
        Intent intent = new Intent(context, PaymentsStepStrategicPartnerActivity.class);
        intent.putExtra(Utility.Extra.DATA, Utility.getJsonStringFromObject(taskDetailModel));
        context.startActivity(intent);
    }


    private static final String TAG = "PaymentsStepActivity";
    private TaskDetailModel taskDetailModel;
    private ActivityPaymentDetailStartegicPartnerBinding mActivityPaymentDetailBinding;


    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
          when the device runing out of memory we dont want the user to restart the payment. rather we close it and redirect them to previous activity.
         */
        mActivityPaymentDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_payment_detail_startegic_partner);

        initiateUI();
        setListeners();
    }

    @Override
    protected void initiateUI() {
        setSupportActionBar(mActivityPaymentDetailBinding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(Utility.EMPTY_STRING);
            mActivityPaymentDetailBinding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Utility.hideKeyboard(mContext);
                    onBackPressed();
                }
            });
        }

        // Enable Step Three Unverified state

        if (getIntent().hasExtra(Utility.Extra.DATA)) {
            //This is only when provider profile view for specific task (provider gives quote to specific task)
            taskDetailModel = (TaskDetailModel) Utility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA), TaskDetailModel.class);
        }

        if (taskDetailModel != null) {
            Utility.loadImageView(mContext, mActivityPaymentDetailBinding.imgService, taskDetailModel.catImage, R.drawable.gradient_black);
            mActivityPaymentDetailBinding.recycleSelectedService.setLayoutManager(new LinearLayoutManager(this));
            if (taskDetailModel.taskSelectedSubCategoryList != null)
                mActivityPaymentDetailBinding.recycleSelectedService.setAdapter(new PaymentSummaryAdapter(taskDetailModel.taskSelectedSubCategoryList));
            Utility.loadImageView(mContext, mActivityPaymentDetailBinding.imgService, taskDetailModel.catImage, R.drawable.gradient_black);
            Utility.showCircularImageViewWithColorBorder(mContext, TAG, mActivityPaymentDetailBinding.imgProfile, taskDetailModel.catImage, R.drawable.icon_profile_img_solid, R.color.dark_blue_variant_1, true);
            String dateTime = "";
            if (!TextUtils.isEmpty(taskDetailModel.taskStartdate)) {
                dateTime = Utility.getDate(Long.parseLong(taskDetailModel.taskStartdate), "dd MMMM, HH:mm a");
                dateTime = dateTime.replace("AM", "am").replace("PM", "pm");
            }
            SuperCalendar superStartDateTimeCalendar = SuperCalendar.getInstance();
            superStartDateTimeCalendar.setTimeZone(SuperCalendar.SuperTimeZone.GMT.GMT);
            superStartDateTimeCalendar.setTimeInMillis(Long.parseLong(taskDetailModel.taskStartdate));
            superStartDateTimeCalendar.setLocaleTimeZone();
            String selectedDate = superStartDateTimeCalendar.format(Utility.DATE_FORMAT_DD_MMM_YYYY);
            String selectedTime = superStartDateTimeCalendar.format(Utility.DATE_FORMAT_HH_MM_AM);
            // String description = "You are booking "+providerModel.userName + " to "+taskDetailModel.subCategoryName + " on "+dateTime+ " at "+taskDetailModel.taskAddress;
            // set details of partner name user selected date time and address
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
            spannableStringBuilder.append(getSpannableString("Your order with ", ContextCompat.getColor(this, R.color.grey_varient_8), false));
            spannableStringBuilder.append(getSpannableString(taskDetailModel.categoryName, ContextCompat.getColor(this, R.color.splash_gradient_end), true));
            spannableStringBuilder.append(getSpannableString(getString(R.string.label_on), ContextCompat.getColor(this, R.color.grey_varient_8), false));
            spannableStringBuilder.append(getSpannableString(selectedDate + ", " + selectedTime
                    , ContextCompat.getColor(this, R.color.splash_gradient_end), true));
            spannableStringBuilder.append(getSpannableString(getString(R.string.label_at), ContextCompat.getColor(this, R.color.grey_varient_8), false));
            spannableStringBuilder.append(getSpannableString(taskDetailModel.taskAddress, ContextCompat.getColor(this, R.color.splash_gradient_end), true));
            spannableStringBuilder.append(getSpannableString(".", ContextCompat.getColor(this, R.color.splash_gradient_end), true));


            mActivityPaymentDetailBinding.txtdesc.setText(spannableStringBuilder);

//            double promocodeValue = 0;
//            if (!TextUtils.isEmpty(taskDetailModel.task_total_amount)) {
//                double task_total_amount = 0;
//                double taskPaidAmountTotal = 0;
//                if (!TextUtils.isEmpty(taskDetailModel.taskPaidAmount)) {
//                    taskPaidAmountTotal = getQuotePriceInInteger(taskDetailModel.taskPaidAmount);
//                }
//                task_total_amount = getQuotePriceInInteger(taskDetailModel.task_total_amount);
//                promocodeValue = task_total_amount - taskPaidAmountTotal;
//
//            }

//            double taskPaidAmount = getQuotePriceInInteger(taskDetailModel.task_total_amount);
//            double totalPayment = taskPaidAmount - promocodeValue;

            double taskQuoteAmount = getQuotePriceInInteger(taskDetailModel.selectedProvider.quotePrice);
            double taskPaidAmount = getQuotePriceInInteger(taskDetailModel.taskPaidAmount);
            double additionalPaidAmount = 0;
            if (!TextUtils.isEmpty(taskDetailModel.additional_paid_amount)) {
                additionalPaidAmount = getQuotePriceInInteger(taskDetailModel.additional_paid_amount);
            }
            double subTotal = (taskQuoteAmount + additionalPaidAmount);
            double promocodeValue = getQuotePriceInInteger(taskDetailModel.taskDiscountAmount);

            mActivityPaymentDetailBinding.txtsubtotal.setText(getString(R.string.ruppe_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf(subTotal))));
            mActivityPaymentDetailBinding.txttotal.setText(getString(R.string.ruppe_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf(taskPaidAmount))));
            mActivityPaymentDetailBinding.txtpromocode.setText(getString(R.string.ruppe_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf((promocodeValue)))));
            mActivityPaymentDetailBinding.lnPromoCodeDisclaimer.setVisibility(promocodeValue == 0 ? View.GONE : View.VISIBLE);
        }
        mActivityPaymentDetailBinding.textLabelTotalPaid.setText(getString(R.string.label_total_paid));
        mActivityPaymentDetailBinding.textpromocodelabel.setEnabled(false);
        mActivityPaymentDetailBinding.textpromocodelabel.setText(getString(R.string.label_promocode_apply));
        mActivityPaymentDetailBinding.textpromocodelabel.setTextColor(ContextCompat.getColor(this, R.color.black));
        mActivityPaymentDetailBinding.lnstep.setVisibility(View.GONE);
        mActivityPaymentDetailBinding.textStepDesc.setVisibility(View.GONE);


        mActivityPaymentDetailBinding.textpromocodelabel.setEnabled(false);


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

    public SpannableStringBuilder getSpannableString(String fullstring, int color, boolean isBold) {
        SpannableStringBuilder text = new SpannableStringBuilder(fullstring);
        text.setSpan(new ForegroundColorSpan(color), 0, fullstring.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (isBold) {
            text.setSpan(new StyleSpan(Typeface.BOLD), 0, fullstring.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return text;
    }


    @Override
    protected void setListeners() {
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}
