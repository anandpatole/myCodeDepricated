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
import android.view.View;

import com.cheep.R;
import com.cheep.databinding.ActivityPaymentDetailBinding;
import com.cheep.model.ProviderModel;
import com.cheep.model.TaskDetailModel;
import com.cheep.utils.Utility;


public class PaymentSummaryActivity extends BaseAppCompatActivity {

    private static final String TAG = "PaymentSummaryActivity";
    private ActivityPaymentDetailBinding mActivityPaymentDetailBinding;
    Bundle bundle;
    private ProviderModel providerModel;
    private TaskDetailModel taskDetailModel;

    /**
     * view summary
     *
     * @param context
     * @param taskDetailModel
     * @param providerModel
     */
    public static void newInstance(Context context, TaskDetailModel taskDetailModel, ProviderModel providerModel, boolean viewonly) {
        Intent intent = new Intent(context, PaymentSummaryActivity.class);
        intent.putExtra(Utility.Extra.DATA, Utility.getJsonStringFromObject(providerModel));
        intent.putExtra(Utility.Extra.DATA_2, Utility.getJsonStringFromObject(taskDetailModel));
        intent.putExtra(Utility.Extra.PAYMENT_VIEW, viewonly);
        context.startActivity(intent);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
          when the device runing out of memory we dont want the user to restart the payment. rather we close it and redirect them to previous activity.
         */
        mActivityPaymentDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_payment_detail);

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


        mActivityPaymentDetailBinding.ivTermsTick.setSelected(true);
        mActivityPaymentDetailBinding.textPay.setSelected(true);
        mActivityPaymentDetailBinding.textName.setVisibility(View.GONE);
        mActivityPaymentDetailBinding.providerRating.setVisibility(View.GONE);
        mActivityPaymentDetailBinding.textExperience.setVisibility(View.GONE);

        if (getIntent().hasExtra(Utility.Extra.DATA)) {
            providerModel = (ProviderModel) Utility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA), ProviderModel.class);
            //This is only when provider profile view for specific task (provider gives quote to specific task)
            taskDetailModel = (TaskDetailModel) Utility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA_2), TaskDetailModel.class);
        }


        mActivityPaymentDetailBinding.textVerified.setVisibility(View.GONE);
        mActivityPaymentDetailBinding.textCategory.setVisibility(View.GONE);
        mActivityPaymentDetailBinding.lnDesclaimer.setVisibility(View.VISIBLE);
        if (getIntent().hasExtra(Utility.Extra.PAYMENT_VIEW)) {
            boolean viewonly = getIntent().getBooleanExtra(Utility.Extra.PAYMENT_VIEW, false);
            if (viewonly) {
                if (taskDetailModel != null) {
                    viewPaymentDetails();
                    mActivityPaymentDetailBinding.lnDesclaimer.setVisibility(View.GONE);
//                    mActivityPaymentDetailBinding.textMaterialDisclaimer.setVisibility(View.GONE);
                    Utility.loadImageView(mContext, mActivityPaymentDetailBinding.imgService, taskDetailModel.catImage, R.drawable.gradient_black);

                    if (taskDetailModel.taskStatus.equalsIgnoreCase(Utility.TASK_STATUS.COMPLETION_CONFIRM))
                        mActivityPaymentDetailBinding.textLabelTotalPaid.setText(getString(R.string.label_total_paid));
                    else
                        mActivityPaymentDetailBinding.textLabelTotalPaid.setText(getString(R.string.label_total_pay));

                    mActivityPaymentDetailBinding.textPay.setVisibility(View.GONE);
                    mActivityPaymentDetailBinding.textpromocodelabel.setText(getString(R.string.label_promocode_apply));
                    mActivityPaymentDetailBinding.textpromocodelabel.setTextColor(ContextCompat.getColor(this, R.color.black));
                    mActivityPaymentDetailBinding.textreferraldiscountlabel.setTextColor(ContextCompat.getColor(this, R.color.black));
                    mActivityPaymentDetailBinding.lnstep.setVisibility(View.GONE);
                    mActivityPaymentDetailBinding.textStepDesc.setVisibility(View.GONE);
                    mActivityPaymentDetailBinding.lltermsandcondition.setVisibility(View.GONE);
                    mActivityPaymentDetailBinding.ivreferraldiscount.setVisibility(View.INVISIBLE);
                    mActivityPaymentDetailBinding.ivpromocode.setVisibility(View.INVISIBLE);
                }


            }
        }

        if (taskDetailModel != null) {
            Utility.loadImageView(mContext, mActivityPaymentDetailBinding.imgService, taskDetailModel.catImage, R.drawable.gradient_black);
        }
        if (providerModel != null) {
            Utility.showCircularImageViewWithColorBorder(mContext, TAG, mActivityPaymentDetailBinding.imgProfile, providerModel.profileUrl, Utility.DEFAULT_CHEEP_LOGO, R.color.dark_blue_variant_1, true);
            String dateTime = "";
            if (!TextUtils.isEmpty(taskDetailModel.taskStartdate)) {
                dateTime = Utility.getDate(Long.parseLong(taskDetailModel.taskStartdate), Utility.DATE_TIME_DD_MMMM_HH_MM);
                dateTime = dateTime.replace(getString(R.string.label_am_caps), getString(R.string.label_am_small)).replace(getString(R.string.label_pm_caps), getString(R.string.label_pm_small));
            }


            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
            spannableStringBuilder.append(getSpannableString(taskDetailModel.subCategoryName, ContextCompat.getColor(this, R.color.splash_gradient_end), true));
            spannableStringBuilder.append(getSpannableString(getString(R.string.label_by), ContextCompat.getColor(this, R.color.grey_varient_8), false));
            spannableStringBuilder.append(getSpannableString(providerModel.userName, ContextCompat.getColor(this, R.color.splash_gradient_end), true));
            spannableStringBuilder.append(getSpannableString(getString(R.string.label_on), ContextCompat.getColor(this, R.color.grey_varient_8), false));
            spannableStringBuilder.append(getSpannableString(dateTime, ContextCompat.getColor(this, R.color.splash_gradient_end), true));
            spannableStringBuilder.append(getSpannableString(getString(R.string.label_at), ContextCompat.getColor(this, R.color.grey_varient_8), false));
            spannableStringBuilder.append(getSpannableString(taskDetailModel.taskAddress, ContextCompat.getColor(this, R.color.splash_gradient_end), true));
            mActivityPaymentDetailBinding.txtdesc.setText(spannableStringBuilder);
        }
    }


    /**
     * Used for Set Payment VAlue When User only Preview.]
     * Payment summary screen for task summary
     */

    public void viewPaymentDetails() {

        mActivityPaymentDetailBinding.textpromocodelabel.setTextColor(ContextCompat.getColor(this, R.color.splash_gradient_end));
        mActivityPaymentDetailBinding.textpromocodelabel.setText(getResources().getString(R.string.label_enter_promocode));

        double taskQuoteAmount = getQuotePriceInInteger(providerModel.quotePrice);
        double taskPaidAmount = getQuotePriceInInteger(taskDetailModel.taskPaidAmount);
        double additionalPaidAmount = 0;
        if (!TextUtils.isEmpty(taskDetailModel.additional_paid_amount)) {
            additionalPaidAmount = getQuotePriceInInteger(taskDetailModel.additional_paid_amount);
        }
        double subTotal = (taskQuoteAmount + additionalPaidAmount);
        double promocodeValue = getQuotePriceInInteger(taskDetailModel.taskDiscountAmount);
        mActivityPaymentDetailBinding.txtprofee.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf(taskQuoteAmount))));
        mActivityPaymentDetailBinding.txtadditionalcharge.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf(additionalPaidAmount))));
        mActivityPaymentDetailBinding.txtsubtotal.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf(subTotal))));
        mActivityPaymentDetailBinding.txttotal.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf(taskPaidAmount))));
//        mActivityPaymentDetailBinding.textPay.setText(getString(R.string.label_book_now_for_rupees, "" + Utility.getQuotePriceFormatter(String.valueOf(taskPaidAmount))));
        mActivityPaymentDetailBinding.textreferraldiscountlabel.setText(getString(R.string.label_referral_discount_paymentsumaary));
        mActivityPaymentDetailBinding.textreferraldiscountlabel.setTextColor(ContextCompat.getColor(this, R.color.black));
        mActivityPaymentDetailBinding.llclaimreferral.setEnabled(false);
        mActivityPaymentDetailBinding.llpromocode.setEnabled(false);

        mActivityPaymentDetailBinding.txtpromocode.setSelected(true);
        mActivityPaymentDetailBinding.txtreferraldiscount.setSelected(true);

        mActivityPaymentDetailBinding.llpromocode.setEnabled(false);
        mActivityPaymentDetailBinding.llclaimreferral.setEnabled(false);

        try {
            if (promocodeValue != 0) {
                if (taskDetailModel.isReferCode.equalsIgnoreCase(Utility.BOOLEAN.YES) || taskDetailModel.isPromoCode.equalsIgnoreCase(Utility.BOOLEAN.YES)) {
                    mActivityPaymentDetailBinding.txtpromocode.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf(promocodeValue))));
                    mActivityPaymentDetailBinding.txtreferraldiscount.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(Utility.ZERO_STRING)));
                    mActivityPaymentDetailBinding.lnPromoCodeDisclaimer.setVisibility(View.VISIBLE);
                    mActivityPaymentDetailBinding.txtPromoCodeDisclaimer.setText(R.string.disclaimer_promo_code);
                } else if (taskDetailModel.isWalletUsed.equalsIgnoreCase(Utility.BOOLEAN.YES)) {
                    mActivityPaymentDetailBinding.txtreferraldiscount.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf(promocodeValue))));
                    mActivityPaymentDetailBinding.txtpromocode.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(Utility.ZERO_STRING)));
                    mActivityPaymentDetailBinding.lnPromoCodeDisclaimer.setVisibility(View.VISIBLE);
                    mActivityPaymentDetailBinding.txtPromoCodeDisclaimer.setText(R.string.disclaimer_referral);
                } else {
                    mActivityPaymentDetailBinding.txtreferraldiscount.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(Utility.ZERO_STRING)));
                    mActivityPaymentDetailBinding.txtpromocode.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(Utility.ZERO_STRING)));
                    mActivityPaymentDetailBinding.lnPromoCodeDisclaimer.setVisibility(View.GONE);
                }
            } else {
                mActivityPaymentDetailBinding.txtreferraldiscount.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(Utility.ZERO_STRING)));
                mActivityPaymentDetailBinding.txtpromocode.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(Utility.ZERO_STRING)));
                mActivityPaymentDetailBinding.lnPromoCodeDisclaimer.setVisibility(View.GONE);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
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
    }

}
