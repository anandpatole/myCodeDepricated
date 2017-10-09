package com.cheep.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.appsflyer.AppsFlyerLib;
import com.cheep.BootstrapConstant;
import com.cheep.BuildConfig;
import com.cheep.R;
import com.cheep.custom_view.BottomAlertDialog;
import com.cheep.custom_view.CFEditTextRegular;
import com.cheep.databinding.ActivityPaymentDetailBinding;
import com.cheep.dialogs.AcknowledgementDialogWithProfilePic;
import com.cheep.dialogs.AcknowledgementInteractionListener;
import com.cheep.firebase.FirebaseHelper;
import com.cheep.firebase.FirebaseUtils;
import com.cheep.firebase.model.ChatTaskModel;
import com.cheep.firebase.model.TaskChatModel;
import com.cheep.model.AddressModel;
import com.cheep.model.MessageEvent;
import com.cheep.model.ProviderModel;
import com.cheep.model.TaskDetailModel;
import com.cheep.model.UserDetails;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.LogUtils;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.SuperCalendar;
import com.cheep.utils.Utility;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static com.cheep.utils.Utility.Extra.PAYMENT_VIEW_IS_ADDITIONAL_CHARGE;


public class PaymentsStepActivity extends BaseAppCompatActivity {


    private double usedWalletBalance = 0;

    public static void newInstance(Context context, TaskDetailModel taskDetailModel, ProviderModel providerModel, int additional) {
        Intent intent = new Intent(context, PaymentsStepActivity.class);
        intent.putExtra(Utility.Extra.DATA, Utility.getJsonStringFromObject(providerModel));
        intent.putExtra(Utility.Extra.DATA_2, Utility.getJsonStringFromObject(taskDetailModel));
        intent.putExtra(PAYMENT_VIEW_IS_ADDITIONAL_CHARGE, additional);
        context.startActivity(intent);
    }

    public static void newInstance(Context context, TaskDetailModel taskDetailModel, ProviderModel providerModel, int additional, boolean isInstaBooking, AddressModel mSelectedAddressModel) {
        Intent intent = new Intent(context, PaymentsStepActivity.class);
        intent.putExtra(Utility.Extra.DATA, Utility.getJsonStringFromObject(providerModel));
        intent.putExtra(Utility.Extra.DATA_2, Utility.getJsonStringFromObject(taskDetailModel));
        intent.putExtra(Utility.Extra.SELECTED_ADDRESS_MODEL, Utility.getJsonStringFromObject(mSelectedAddressModel));
        intent.putExtra(Utility.Extra.TASK_TYPE_IS_INSTA, isInstaBooking);
        intent.putExtra(PAYMENT_VIEW_IS_ADDITIONAL_CHARGE, additional);
        context.startActivity(intent);
    }

    public static void newInstance(Context context, TaskDetailModel taskDetailModel, ProviderModel providerModel, boolean viewonly) {
        Intent intent = new Intent(context, PaymentsStepActivity.class);
        intent.putExtra(Utility.Extra.DATA, Utility.getJsonStringFromObject(providerModel));
        intent.putExtra(Utility.Extra.DATA_2, Utility.getJsonStringFromObject(taskDetailModel));
        intent.putExtra(Utility.Extra.PAYMENT_VIEW, viewonly);
        context.startActivity(intent);
    }

    private static final String TAG = "PaymentsStepActivity";
    private ProviderModel providerModel;
    private TaskDetailModel taskDetailModel;
    Bundle bundle;
    private String actualQuotePrice;
    private String promocode_price;
    private ActivityPaymentDetailBinding mActivityPaymentDetailBinding;

    //ForInsta Booking
    private boolean isInstaBooking = false;
    private AddressModel mSelectedAddressModelForInsta;
    Map<String, Object> mTaskCreationParams;


    private int isAdditional;

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

        // Enable Step Three Unverified state
        setTaskState(STEP_THREE_UNVERIFIED);

        mActivityPaymentDetailBinding.ivTermsTick.setSelected(true);
        mActivityPaymentDetailBinding.textPay.setSelected(true);

        if (getIntent().hasExtra(Utility.Extra.DATA)) {
            providerModel = (ProviderModel) Utility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA), ProviderModel.class);
            //This is only when provider profile view for specific task (provider gives quote to specific task)
            taskDetailModel = (TaskDetailModel) Utility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA_2), TaskDetailModel.class);
        }

        if (getIntent().hasExtra(Utility.Extra.TASK_TYPE_IS_INSTA)) {
            isInstaBooking = getIntent().getBooleanExtra(Utility.Extra.TASK_TYPE_IS_INSTA, false);
            mSelectedAddressModelForInsta = (AddressModel) Utility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.SELECTED_ADDRESS_MODEL), AddressModel.class);
        }
        mActivityPaymentDetailBinding.lnDesclaimer.setVisibility(View.VISIBLE);

        if (getIntent().hasExtra(Utility.Extra.PAYMENT_VIEW_IS_ADDITIONAL_CHARGE)) {
            if (taskDetailModel != null) {
                mActivityPaymentDetailBinding.textTitle.setText(getString(R.string.title_booking_confimation));
            }
            isAdditional = getIntent().getIntExtra(Utility.Extra.PAYMENT_VIEW_IS_ADDITIONAL_CHARGE, 0);
            if (isAdditional == 0) {
                if (taskDetailModel != null) {
                    resetPromocodeValue();
                    callGetReferBalance();

                }
            } else {
                if (taskDetailModel != null) {
                    setAdditionalPayment();
                }
            }
            mActivityPaymentDetailBinding.lnPromoCodeDisclaimer.setVisibility(View.GONE);


        } else if (getIntent().hasExtra(Utility.Extra.PAYMENT_VIEW)) {
            boolean viewonly = getIntent().getBooleanExtra(Utility.Extra.PAYMENT_VIEW, false);
            if (viewonly) {
                if (taskDetailModel != null) {
                    resetPromocodeValuePreview();
                    mActivityPaymentDetailBinding.lnDesclaimer.setVisibility(View.GONE);
                    Utility.loadImageView(mContext, mActivityPaymentDetailBinding.imgService, taskDetailModel.catImage, R.drawable.gradient_black);
                }
                mActivityPaymentDetailBinding.textLabelTotalPaid.setText(getString(R.string.label_total_paid));
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

        if (taskDetailModel != null) {
            Utility.loadImageView(mContext, mActivityPaymentDetailBinding.imgService, taskDetailModel.catImage, R.drawable.gradient_black);
        }
        if (providerModel != null) {

//            Utility.loadImageView(mContext, mActivityPaymentDetailBinding.imgProfile, providerModel.profileUrl, Utility.DEFAULT_PROFILE_SRC);
            Utility.showCircularImageViewWithColorBorder(mContext, TAG, mActivityPaymentDetailBinding.imgProfile, providerModel.profileUrl, Utility.DEFAULT_CHEEP_LOGO, R.color.dark_blue_variant_1, true);
            String dateTime = "";
            if (!TextUtils.isEmpty(taskDetailModel.taskStartdate)) {
                dateTime = Utility.getDate(Long.parseLong(taskDetailModel.taskStartdate), Utility.DATE_TIME_DD_MMMM_HH_MM);
                dateTime = dateTime.replace(getString(R.string.label_am_caps), getString(R.string.label_am_small)).replace(getString(R.string.label_pm_caps), getString(R.string.label_pm_small));
            }

            // String description = "You are booking "+providerModel.userName + " to "+taskDetailModel.subCategoryName + " on "+dateTime+ " at "+taskDetailModel.taskAddress;

            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
            spannableStringBuilder.append(getSpannableString(taskDetailModel.subCategoryName, ContextCompat.getColor(this, R.color.splash_gradient_end), true));
            spannableStringBuilder.append(getSpannableString(getString(R.string.label_by), ContextCompat.getColor(this, R.color.grey_varient_8), false));
            spannableStringBuilder.append(getSpannableString(providerModel.userName, ContextCompat.getColor(this, R.color.splash_gradient_end), true));
            spannableStringBuilder.append(getSpannableString(getString(R.string.label_on), ContextCompat.getColor(this, R.color.grey_varient_8), false));
            spannableStringBuilder.append(getSpannableString(dateTime, ContextCompat.getColor(this, R.color.splash_gradient_end), true));
            spannableStringBuilder.append(getSpannableString(getString(R.string.label_at), ContextCompat.getColor(this, R.color.grey_varient_8), false));
            spannableStringBuilder.append(getSpannableString(taskDetailModel.taskAddress, ContextCompat.getColor(this, R.color.splash_gradient_end), true));

           /* SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
            spannableStringBuilder.append(getSpannableString(getString(R.string.label_booking), ContextCompat.getColor(this, R.color.grey_varient_8), false));
            spannableStringBuilder.append(getSpannableString(providerModel.userName, ContextCompat.getColor(this, R.color.splash_gradient_end), true));
            spannableStringBuilder.append(getSpannableString(getString(R.string.label_to), ContextCompat.getColor(this, R.color.grey_varient_8), false));
            spannableStringBuilder.append(getSpannableString(taskDetailModel.subCategoryName, ContextCompat.getColor(this, R.color.splash_gradient_end), true));
            spannableStringBuilder.append(getSpannableString(getString(R.string.label_on), ContextCompat.getColor(this, R.color.grey_varient_8), false));
            spannableStringBuilder.append(getSpannableString(dateTime, ContextCompat.getColor(this, R.color.splash_gradient_end), true));
            spannableStringBuilder.append(getSpannableString(getString(R.string.label_at), ContextCompat.getColor(this, R.color.grey_varient_8), false));
            spannableStringBuilder.append(getSpannableString(taskDetailModel.taskAddress, ContextCompat.getColor(this, R.color.splash_gradient_end), true));*/
            double taskPaidAmount = getQuotePriceInInteger(providerModel.quotePrice);
            double additionalCharges = 0;
            double promocodeValue = 0;
            double additionalPaidAmount = 0;

            if (isInstaBooking) {
                promocodeValue = 0;
            } else {

                if (!TextUtils.isEmpty(taskDetailModel.additionalQuoteAmount)) {
                    additionalCharges = getQuotePriceInInteger(taskDetailModel.additionalQuoteAmount);
                }

                if (!TextUtils.isEmpty(taskDetailModel.task_total_amount)) {
                    double task_total_amount = 0;
                    double taskPaidAmountTotal = 0;
                    if (!TextUtils.isEmpty(taskDetailModel.taskPaidAmount)) {
                        taskPaidAmountTotal = getQuotePriceInInteger(taskDetailModel.taskPaidAmount);
                    }
                    task_total_amount = getQuotePriceInInteger(taskDetailModel.task_total_amount);
                    promocodeValue = task_total_amount - taskPaidAmountTotal;

                }
            }

            double subTotal = (taskPaidAmount + additionalCharges);
            double totalPayment = (taskPaidAmount + additionalCharges) - promocodeValue;
            mActivityPaymentDetailBinding.txtdesc.setText(spannableStringBuilder);
            mActivityPaymentDetailBinding.txtprofee.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf(taskPaidAmount))));
            mActivityPaymentDetailBinding.txtadditionalcharge.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf(additionalCharges))));
            mActivityPaymentDetailBinding.txtsubtotal.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf(subTotal))));
        }

        //Referrral discount image click listener
        mActivityPaymentDetailBinding.llclaimreferral.setOnClickListener(OnClaimOfRefereCodeClickListener);


        //Promo code image onclick listener
        mActivityPaymentDetailBinding.llpromocode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActivityPaymentDetailBinding.ivpromocode.setVisibility(View.VISIBLE);
                mActivityPaymentDetailBinding.ivpromocode.setSelected(false);
                mActivityPaymentDetailBinding.txtpromocode.setSelected(false);
                mActivityPaymentDetailBinding.imgCheepCodeClose.setVisibility(View.GONE);
                mActivityPaymentDetailBinding.llpromocode.setEnabled(true);
                resetPromocodeValue();

                mActivityPaymentDetailBinding.ivpromocode.setSelected(!mActivityPaymentDetailBinding.ivpromocode.isSelected());
                mActivityPaymentDetailBinding.txtpromocode.setSelected(mActivityPaymentDetailBinding.ivpromocode.isSelected());
                mActivityPaymentDetailBinding.lnPromoCodeDisclaimer.setVisibility(View.GONE);

                if (mActivityPaymentDetailBinding.ivreferraldiscount.isSelected()) {
                    mActivityPaymentDetailBinding.txtreferraldiscount.setSelected(false);
                    mActivityPaymentDetailBinding.ivreferraldiscount.setSelected(false);
                }

                if (mActivityPaymentDetailBinding.ivpromocode.isSelected()) {

                    showCheepCodeDialog();
                    usedWalletBalance = 0;
                    mActivityPaymentDetailBinding.txtreferraldiscount.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(referralBalance)));
                    if (!TextUtils.isEmpty(actualQuotePrice)) {
                        providerModel.quotePrice = actualQuotePrice;
                    }
                    actualQuotePrice = null;
                    mActivityPaymentDetailBinding.txttotal.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(providerModel.quotePrice)));
                    mActivityPaymentDetailBinding.textPay.setText(getString(R.string.label_book_now_for_rupees, Utility.getQuotePriceFormatter(providerModel.quotePrice)));
                }

            }
        });


        // Add Desclaimer
        mActivityPaymentDetailBinding.imgCheepCodeClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickOfClosePromoCode();
            }
        });

        //terms and condition click listener
        mActivityPaymentDetailBinding.ivTermsTick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActivityPaymentDetailBinding.ivTermsTick.setSelected(!mActivityPaymentDetailBinding.ivTermsTick.isSelected());
                mActivityPaymentDetailBinding.textPay.setSelected(mActivityPaymentDetailBinding.ivTermsTick.isSelected());
                mActivityPaymentDetailBinding.textPay.setEnabled(mActivityPaymentDetailBinding.ivTermsTick.isSelected());
            }
        });


    }

    View.OnClickListener OnClaimOfRefereCodeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mActivityPaymentDetailBinding.ivreferraldiscount.setSelected(!mActivityPaymentDetailBinding.ivreferraldiscount.isSelected());
            mActivityPaymentDetailBinding.txtreferraldiscount.setSelected(mActivityPaymentDetailBinding.ivreferraldiscount.isSelected());
            mActivityPaymentDetailBinding.llpromocode.setEnabled(true);
                onClickOfClosePromoCode();


            if (mActivityPaymentDetailBinding.ivreferraldiscount.isSelected()) {
                isReferCode = Utility.BOOLEAN.NO;
                Log.e(TAG, "  providerModel.spWithoutGstQuotePrice  :: " + providerModel.spWithoutGstQuotePrice);
                Log.e(TAG, "  maxReferDiscount  :: " + maxReferDiscount);
                Log.e(TAG, " referralBalance  :: " + referralBalance);

                try {
                    actualQuotePrice = providerModel.quotePrice;
                    double quoteAmount = Double.parseDouble(providerModel.spWithoutGstQuotePrice);
                    double maxDiscountAmt = Double.parseDouble(maxReferDiscount);
                    double referralBal = Double.parseDouble(referralBalance);
//                double payableAmount = 0;
                    usedWalletBalance = 0;


                    if (referralBal > maxDiscountAmt) {
                        if (quoteAmount >= maxDiscountAmt) {
//                        payableAmount = quoteAmount - maxDiscountAmt;
                            usedWalletBalance = maxDiscountAmt;
                        } else {
//                        payableAmount = 0;
                            usedWalletBalance = quoteAmount;
                        }
                    } else {
                        if (quoteAmount >= referralBal) {
//                        payableAmount = quoteAmount - referralBal;
                            usedWalletBalance = referralBal;
                        } else {
//                        payableAmount = 0;
                            usedWalletBalance = quoteAmount;
                        }
                    }

                    getAmountWithGstWS(String.valueOf(usedWalletBalance));
                } catch (Exception e) {
                    e.printStackTrace();
                }

//                providerModel.quotePrice = payableAmount + "";
//                mActivityPaymentDetailBinding.txttotal.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(payableAmount + "")));
//                mActivityPaymentDetailBinding.textPay.setText(getString(R.string.label_pay_fee_v1, Utility.getQuotePriceFormatter(payableAmount + "")));

            } else {
                usedWalletBalance = 0;

                if (!TextUtils.isEmpty(actualQuotePrice)) {
                    providerModel.quotePrice = actualQuotePrice;
                }
                actualQuotePrice = null;
                mActivityPaymentDetailBinding.txttotal.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(providerModel.quotePrice)));
                mActivityPaymentDetailBinding.txtreferraldiscount.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(referralBalance)));
                mActivityPaymentDetailBinding.lnPromoCodeDisclaimer.setVisibility(View.GONE);
                mActivityPaymentDetailBinding.textPay.setText(getString(R.string.label_book_now_for_rupees, Utility.getQuotePriceFormatter(providerModel.quotePrice)));
            }
        }
    };

    private void onClickOfClosePromoCode() {
        mActivityPaymentDetailBinding.ivpromocode.setVisibility(View.VISIBLE);
        mActivityPaymentDetailBinding.ivpromocode.setSelected(false);
        mActivityPaymentDetailBinding.txtpromocode.setSelected(false);
        cheepCode = null;
        if (!TextUtils.isEmpty(actualQuotePrice)) {
            providerModel.quotePrice = actualQuotePrice;
        }
        actualQuotePrice = null;
        mActivityPaymentDetailBinding.imgCheepCodeClose.setVisibility(View.GONE);
        mActivityPaymentDetailBinding.llpromocode.setEnabled(true);
        resetPromocodeValue();
    }

    /**
     * Used for Reset Payment Values after applying promocode OR Removing promo code
     */
    public void resetPromocodeValue() {
        mActivityPaymentDetailBinding.textpromocodelabel.setTextColor(ContextCompat.getColor(this, R.color.splash_gradient_end));
        mActivityPaymentDetailBinding.textpromocodelabel.setText(getResources().getString(R.string.label_enter_promocode));
        mActivityPaymentDetailBinding.lnPromoCodeDisclaimer.setVisibility(View.GONE);
        isReferCode = Utility.BOOLEAN.NO;

        if (!TextUtils.isEmpty(providerModel.quotePrice)) {
            double taskPaidAmount = getQuotePriceInInteger(providerModel.quotePrice);
            double additionalCharges = 0;
            double promocodeValue = 0;
            double additionalPaidAmount = 0;

            if (isInstaBooking) {
                promocodeValue = 0;
            } else {

                if (!TextUtils.isEmpty(taskDetailModel.additionalQuoteAmount)) {
                    additionalCharges = getQuotePriceInInteger(taskDetailModel.additionalQuoteAmount);
                }

                if (!TextUtils.isEmpty(taskDetailModel.task_total_amount)) {
                    double task_total_amount = 0;
                    double taskPaidAmountTotal = 0;
                    if (!TextUtils.isEmpty(taskDetailModel.taskPaidAmount)) {
                        taskPaidAmountTotal = getQuotePriceInInteger(taskDetailModel.taskPaidAmount);
                    }
                    task_total_amount = getQuotePriceInInteger(taskDetailModel.task_total_amount);
                    promocodeValue = task_total_amount - taskPaidAmountTotal;

                }
            }

            double subTotal = (taskPaidAmount + additionalCharges);
            double totalPayment = (taskPaidAmount + additionalCharges) - promocodeValue;
       //     mActivityPaymentDetailBinding.txtprofee.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf(taskPaidAmount))));
        //    mActivityPaymentDetailBinding.txtadditionalcharge.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf(additionalCharges))));
        //    mActivityPaymentDetailBinding.txtsubtotal.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf(subTotal))));
            mActivityPaymentDetailBinding.txttotal.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf(totalPayment))));
            mActivityPaymentDetailBinding.textPay.setText(getString(R.string.label_book_now_for_rupees, "" + Utility.getQuotePriceFormatter(String.valueOf(totalPayment))));
            mActivityPaymentDetailBinding.txtpromocode.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf(promocodeValue))));

        }
    }

    /**
     * Used for Set Payment VAlue When User only Preview.
     */

    public void resetPromocodeValuePreview() {
        mActivityPaymentDetailBinding.textpromocodelabel.setTextColor(ContextCompat.getColor(this, R.color.splash_gradient_end));
        mActivityPaymentDetailBinding.textpromocodelabel.setText(getResources().getString(R.string.label_enter_promocode));
//        if (!TextUtils.isEmpty(providerModel.quotePrice)) {
        double taskQuoteAmount = getQuotePriceInInteger(providerModel.quotePrice);
        double taskPaidAmount = getQuotePriceInInteger(taskDetailModel.taskPaidAmount);
//            double promocodeValue = 0;
        double additionalPaidAmount = 0;
        if (!TextUtils.isEmpty(taskDetailModel.additional_paid_amount)) {
            additionalPaidAmount = getQuotePriceInInteger(taskDetailModel.additional_paid_amount);
        }
//
//            if (!TextUtils.isEmpty(taskDetailModel.task_total_amount)) {
//                double task_total_amount;
//                double taskPaidAmountTotal = 0;
//                if (!TextUtils.isEmpty(taskDetailModel.taskPaidAmount)) {
//                    taskPaidAmountTotal = getQuotePriceInInteger(taskDetailModel.taskPaidAmount);
//                }
//                task_total_amount = getQuotePriceInInteger(taskDetailModel.task_total_amount);
//                promocodeValue = task_total_amount - taskPaidAmountTotal;
//
//            }
//
        double subTotal = (taskQuoteAmount + additionalPaidAmount);
        double promocodeValue = getQuotePriceInInteger(taskDetailModel.taskDiscountAmount);
//            double totalPayment = (taskPaidAmount + additionalPaidAmount) - promocodeValue;
        mActivityPaymentDetailBinding.txtprofee.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf(taskQuoteAmount))));
        mActivityPaymentDetailBinding.txtadditionalcharge.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf(additionalPaidAmount))));
        mActivityPaymentDetailBinding.txtsubtotal.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf(subTotal))));
        mActivityPaymentDetailBinding.txttotal.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf(taskPaidAmount))));
        mActivityPaymentDetailBinding.textPay.setText(getString(R.string.label_book_now_for_rupees, "" + Utility.getQuotePriceFormatter(String.valueOf(taskPaidAmount))));
        mActivityPaymentDetailBinding.textreferraldiscountlabel.setText(getString(R.string.label_referral_discount_paymentsumaary));

        mActivityPaymentDetailBinding.txtpromocode.setSelected(true);
        mActivityPaymentDetailBinding.txtreferraldiscount.setSelected(true);

        try {
            if (promocodeValue != 0) {
                if (taskDetailModel.isReferCode.equalsIgnoreCase(Utility.BOOLEAN.YES) || taskDetailModel.isPromoCode.equalsIgnoreCase(Utility.BOOLEAN.YES)) {
                    mActivityPaymentDetailBinding.txtpromocode.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf(promocodeValue))));
                    mActivityPaymentDetailBinding.txtreferraldiscount.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(Utility.ZERO_STRING)));
                    mActivityPaymentDetailBinding.lnPromoCodeDisclaimer.setVisibility(View.VISIBLE);
                } else if (taskDetailModel.isWalletUsed.equalsIgnoreCase(Utility.BOOLEAN.YES)) {
                    {
                        mActivityPaymentDetailBinding.txtreferraldiscount.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf(promocodeValue))));
                        mActivityPaymentDetailBinding.txtpromocode.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(Utility.ZERO_STRING)));
                        mActivityPaymentDetailBinding.lnPromoCodeDisclaimer.setVisibility(View.VISIBLE);
                    }
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

    /**
     * Used for Additional payment
     */
    public void setAdditionalPayment() {
        int taskPaidAmount = 0;
        mActivityPaymentDetailBinding.txtprofee.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter("0")));
        double additionalCharges = 0;
        if (!TextUtils.isEmpty(taskDetailModel.additionalQuoteAmount)) {
            additionalCharges = getQuotePriceInInteger(taskDetailModel.additionalQuoteAmount);
        }
        mActivityPaymentDetailBinding.txtadditionalcharge.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf(additionalCharges))));

        double subTotal = taskPaidAmount + additionalCharges;
        mActivityPaymentDetailBinding.txtsubtotal.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf(subTotal))));
        mActivityPaymentDetailBinding.txttotal.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf(subTotal))));
        mActivityPaymentDetailBinding.textPay.setText(getString(R.string.label_pay_fee_v1, "" + Utility.getQuotePriceFormatter(String.valueOf(subTotal))));
        mActivityPaymentDetailBinding.rlprofee.setAlpha(0.5f);
        mActivityPaymentDetailBinding.rlreferraldiscount.setAlpha(0.5f);
        mActivityPaymentDetailBinding.rlpromocode.setAlpha(0.5f);
        mActivityPaymentDetailBinding.rlpromocode.setAlpha(0.5f);
        mActivityPaymentDetailBinding.llpromocode.setEnabled(false);
        mActivityPaymentDetailBinding.llclaimreferral.setEnabled(false);
        mActivityPaymentDetailBinding.textpromocodelabel.setText(R.string.label_enter_promocode);
        mActivityPaymentDetailBinding.txtpromocode.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(Utility.ZERO_STRING)));
        mActivityPaymentDetailBinding.txtreferraldiscount.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(Utility.ZERO_STRING)));
        mActivityPaymentDetailBinding.textPay.setSelected(mActivityPaymentDetailBinding.ivTermsTick.isSelected());
        /*mActivityPaymentDetailBinding.devicerpromocode.setVisibility(View.GONE);
        mActivityPaymentDetailBinding.rlpromocode.setVisibility(View.GONE);*/

    }

    public SpannableStringBuilder getSpannableString(String fullstring, int color, boolean isBold) {
        SpannableStringBuilder text = new SpannableStringBuilder(fullstring);
        text.setSpan(new ForegroundColorSpan(color), 0, fullstring.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (isBold) {
            text.setSpan(new StyleSpan(Typeface.BOLD), 0, fullstring.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return text;
    }

    View.OnClickListener onPayClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            setTaskState(STEP_THREE_VERIFIED);

            try {
                double payableAmount = Double.parseDouble(providerModel.quotePrice);
                if (payableAmount == 0) {
                    if (isInstaBooking) {
                        callCreateInstaBookingTaskWS(true, Utility.EMPTY_STRING, NetworkUtility.TAGS.PAYMENT_METHOD_FREE);
                    } else {
                        updatePaymentStatus(true, Utility.EMPTY_STRING, isAdditional != 0, NetworkUtility.TAGS.PAYMENT_METHOD_FREE);
                    }
                } else
                    payNow(isAdditional != 0);
            } catch (NumberFormatException e) {
                e.printStackTrace();

            }

        }
    };

    CFEditTextRegular edtCheepcode;
    BottomAlertDialog cheepCodeDialog;

    private void showCheepCodeDialog() {

        View view = View.inflate(mContext, R.layout.dialog_add_promocode, null);
        edtCheepcode = view.findViewById(R.id.edit_cheepcode);
        cheepCodeDialog = new BottomAlertDialog(mContext);
        mActivityPaymentDetailBinding.ivpromocode.setSelected(false);
        mActivityPaymentDetailBinding.txtpromocode.setSelected(false);
        view.findViewById(R.id.btn_apply).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(edtCheepcode.getText().toString())) {
                    Utility.showToast(mContext, getString(R.string.validate_cheepcode));
                    return;
                }
                validateCheepCode(edtCheepcode.getText().toString());
            }
        });
        edtCheepcode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_DONE:
                        if (TextUtils.isEmpty(edtCheepcode.getText().toString())) {
                            Utility.showToast(mContext, getString(R.string.validate_cheepcode));
                            break;
                        }
                        validateCheepCode(edtCheepcode.getText().toString());
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        cheepCodeDialog.setTitle(getString(R.string.label_cheepcode));
        cheepCodeDialog.setCustomView(view);
        cheepCodeDialog.showDialog();
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
        mActivityPaymentDetailBinding.textPay.setOnClickListener(onPayClickListener);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * Used for payment
     */
    private void validateCheepCode(String cheepCode) {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityPaymentDetailBinding.getRoot());
            return;
        }

        showProgressDialog();

        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();

        actualQuotePrice = providerModel.quotePrice;


        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, userDetails.UserID);

        //Add Params
        Map<String, Object> mParams = new HashMap<>();

        if (isInstaBooking) {
            mParams.put(NetworkUtility.TAGS.QUOTE_AMOUNT, providerModel.spWithoutGstQuotePrice);
            mParams.put(NetworkUtility.TAGS.CHEEPCODE, cheepCode);
            mParams.put(NetworkUtility.TAGS.CAT_ID, taskDetailModel.categoryId);
            mParams.put(NetworkUtility.TAGS.IS_INSTA_BOOKING, Utility.BOOLEAN.YES);
            int addressId;
            try {
                addressId = Integer.parseInt(mSelectedAddressModelForInsta.address_id);
            } catch (Exception e) {
                addressId = 0;
            }
            if (addressId <= 0) {
                mParams.put(NetworkUtility.TAGS.ADDRESS, mSelectedAddressModelForInsta.address);
                mParams.put(NetworkUtility.TAGS.ADDRESS_INITIALS, mSelectedAddressModelForInsta.address_initials);
                mParams.put(NetworkUtility.TAGS.CATEGORY, mSelectedAddressModelForInsta.category);
                mParams.put(NetworkUtility.TAGS.LAT, mSelectedAddressModelForInsta.lat);
                mParams.put(NetworkUtility.TAGS.LNG, mSelectedAddressModelForInsta.lng);
                mParams.put(NetworkUtility.TAGS.COUNTRY, mSelectedAddressModelForInsta.countryName);
                mParams.put(NetworkUtility.TAGS.STATE, mSelectedAddressModelForInsta.stateName);
                mParams.put(NetworkUtility.TAGS.CITY_NAME, mSelectedAddressModelForInsta.cityName);
            } else {
                mParams.put(NetworkUtility.TAGS.ADDRESS_ID, addressId);
            }
        } else {
            mParams.put(NetworkUtility.TAGS.QUOTE_AMOUNT, providerModel.quotePrice);
            mParams.put(NetworkUtility.TAGS.TASK_ID, taskDetailModel.taskId);
            mParams.put(NetworkUtility.TAGS.CHEEPCODE, cheepCode);
        }
        //Url is based on condition if address id is greater then 0 then it means we need to update the existing address
        String url = isInstaBooking ? NetworkUtility.WS.CHECK_CHEEPCODE_FOR_STRATEGIC_PARTNER : NetworkUtility.WS.VALIDATE_CHEEP_CODE;
        VolleyNetworkRequest mVolleyNetworkRequestForSPList = new VolleyNetworkRequest(url
                , mCallValidateCheepCodeWSErrorListener
                , mCallValidateCheepCodeWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForSPList);
    }


    private String cheepCode;
    private String isReferCode = Utility.BOOLEAN.NO;
    Response.Listener mCallValidateCheepCodeWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {

            String strResponse = (String) response;
            try {
                Utility.hideKeyboard(PaymentsStepActivity.this, edtCheepcode);
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;
                hideProgressDialog();
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:

                        if (edtCheepcode != null) {
                            cheepCode = edtCheepcode.getText().toString().trim();
                            cheepCodeDialog.dismiss();

                            String total = jsonObject.optString(NetworkUtility.TAGS.QUOTE_AMOUNT);
                            String discount = jsonObject.optString(NetworkUtility.TAGS.DISCOUNT_AMOUNT);
                            // new field for refer and earn functionality
                            isReferCode = jsonObject.optString(NetworkUtility.TAGS.IS_REFER_CODE);

                            String payable;
                            payable = jsonObject.optString(NetworkUtility.TAGS.PAYABLE_AMOUNT);
                            updatePaymentBtn(total, discount, payable);
                            mActivityPaymentDetailBinding.lnPromoCodeDisclaimer.setVisibility(View.VISIBLE);

                        }

                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
//                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityJobSummaryBinding.getRoot());
                        Utility.showToast(mContext, getString(R.string.label_something_went_wrong));
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
//                        Utility.showSnackBar(error_message, mActivityJobSummaryBinding.getRoot());
                        Utility.showToast(mContext, error_message);
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
                mCallPaymentWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
        }
    };


    Response.Listener mCallGetAmoutWithGSTWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {
            hideProgressDialog();
            String strResponse = (String) response;
            try {
                Utility.hideKeyboard(PaymentsStepActivity.this, edtCheepcode);
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;
                hideProgressDialog();
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:


                        String total = jsonObject.optString(NetworkUtility.TAGS.QUOTE_AMOUNT);
                        String discount = jsonObject.optString(NetworkUtility.TAGS.DISCOUNT_AMOUNT);
                        double discountAmount = 0;
                        double balance = 0;
                        if (!TextUtils.isEmpty(discount) && !TextUtils.isEmpty(discount)) {
                            discountAmount = Double.parseDouble(discount);
                            balance = Double.parseDouble(referralBalance);
                            mActivityPaymentDetailBinding.txtreferraldiscount.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf(balance - discountAmount))));
                        }
                        // new field for refer and earn functionality
                        isReferCode = jsonObject.optString(NetworkUtility.TAGS.IS_REFER_CODE);

                        String payable;
                        payable = jsonObject.optString(NetworkUtility.TAGS.PAYABLE_AMOUNT);
                        mActivityPaymentDetailBinding.lnPromoCodeDisclaimer.setVisibility(View.VISIBLE);

                        // setting payable amount as quote price to pay.
                        providerModel.quotePrice = payable;
                        mActivityPaymentDetailBinding.txttotal.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(payable)));

                        mActivityPaymentDetailBinding.textPay.setText(getString(R.string.label_book_now_for_rupees, Utility.getQuotePriceFormatter(payable)));
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
//                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityJobSummaryBinding.getRoot());
                        Utility.showToast(mContext, getString(R.string.label_something_went_wrong));
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
//                        Utility.showSnackBar(error_message, mActivityJobSummaryBinding.getRoot());
                        Utility.showToast(mContext, error_message);
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
                mCallValidateCheepCodeWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
        }
    };


    /**
     * Updating payemnt btn text to show the discounted price and payable amount
     */
    private void updatePaymentBtn(String total, String discount, String payable) {
        // setting payable amount as quote price to pay.
        providerModel.quotePrice = payable;
        promocode_price = discount;
        usedWalletBalance = 0;
//        mActivityJobSummaryBinding.btnPay.setText(getString(R.string.label_pay_X_X_X, total, discount, payable));
//        @change only need to show payable amount
        mActivityPaymentDetailBinding.txtpromocode.setSelected(true);
        mActivityPaymentDetailBinding.txtpromocode.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(discount)));
        mActivityPaymentDetailBinding.txttotal.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(payable)));
        mActivityPaymentDetailBinding.textPay.setText(getString(R.string.label_book_now_for_rupees, Utility.getQuotePriceFormatter(payable)));
        mActivityPaymentDetailBinding.textpromocodelabel.setText(getString(R.string.label_promocode_apply));
        mActivityPaymentDetailBinding.textpromocodelabel.setTextColor(ContextCompat.getColor(this, R.color.black));

        mActivityPaymentDetailBinding.imgCheepCodeClose.setVisibility(View.VISIBLE);
        mActivityPaymentDetailBinding.llpromocode.setEnabled(false);
        mActivityPaymentDetailBinding.ivpromocode.setVisibility(View.INVISIBLE);
    }

    Response.ErrorListener mCallValidateCheepCodeWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(final VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            hideProgressDialog();

//            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityJobSummaryBinding.getRoot());
            Utility.hideKeyboard(PaymentsStepActivity.this, edtCheepcode);
            Utility.showToast(mContext, getString(R.string.label_something_went_wrong));

        }

    };


    /**
     * Used for payment
     */
    private void payNow(boolean isForAdditionalQuote) {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityPaymentDetailBinding.getRoot());
            return;
        }

        showProgressDialog();

        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();

        //Add Params
        Map<String, Object> mParams;// = new HashMap<String, Object>();

        mParams = getPaymentTransactionFields(userDetails, isForAdditionalQuote);
        if (!isInstaBooking) {
            mParams.put(NetworkUtility.TAGS.SP_USER_ID, providerModel.providerId);
            mParams.put(NetworkUtility.TAGS.TASK_ID, taskDetailModel.taskId);
        }

        if (!TextUtils.isEmpty(cheepCode)) {
            mParams.put(NetworkUtility.TAGS.CHEEPCODE, cheepCode);
            mParams.put(NetworkUtility.TAGS.PROMOCODE_PRICE, promocode_price);
        } else {
            mParams.put(NetworkUtility.TAGS.CHEEPCODE, Utility.EMPTY_STRING);
            mParams.put(NetworkUtility.TAGS.PROMOCODE_PRICE, "0");
        }
        mParams.put(NetworkUtility.TAGS.IS_REFER_CODE, isReferCode);


        //Create Asynctask that will do the encryption and afterwords call webservice
        AsyncFetchEnryptedString asyncFetchEnryptedString = new AsyncFetchEnryptedString(isForAdditionalQuote);
        asyncFetchEnryptedString.execute(new JSONObject(mParams).toString());

//        String encryptedText = Utility.encryptUsingRNCryptorNative(new JSONObject(mParams).toString());


    }


    /**
     * Asynctask that will do encryption
     *
     * @Dated : 6th Feb 2017
     * input: String that needs to be converted
     * output: String after Encryption completed
     */
    private class AsyncFetchEnryptedString extends AsyncTask<String, Void, String> {
        boolean isForAdditionalQuote;

        public AsyncFetchEnryptedString(boolean isForAdditionalQuote) {
            this.isForAdditionalQuote = isForAdditionalQuote;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                String result = Utility.applyAESEncryption(new JSONObject(params[0]).toString());
                return result;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String encryptedData) {
            super.onPostExecute(encryptedData);

            UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();

            //Add Header parameters
            Map<String, String> mHeaderParams = new HashMap<>();
            mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
            mHeaderParams.put(NetworkUtility.TAGS.USER_ID, userDetails.UserID);

            Map<String, Object> mFinalParams = new HashMap<>();
            mFinalParams.put(NetworkUtility.TAGS.DATA, encryptedData);

            //calling this to create post data
            getPaymentUrl(userDetails, isForAdditionalQuote);
            String url = "";
            // if payment is done using insta feature then
            // post data will be generated like strategic partner feature
            // call startegic generate hash for payment
            url = isInstaBooking ? NetworkUtility.WS.GET_PAYMENT_HASH_FOR_STRATEGIC_PARTNER : NetworkUtility.WS.GET_PAYMENT_HASH;
            //Url is based on condition if address id is greater then 0 then it means we need to update the existing address
            VolleyNetworkRequest mVolleyNetworkRequestForSPList = new VolleyNetworkRequest(url
                    , mCallPaymentWSErrorListener
                    , mCallPaymentWSResponseListener
                    , mHeaderParams
                    , mFinalParams
                    , null);
            Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForSPList);
        }

    }

    // if payment is done using insta feature then
    // post data will be generated like strategic partner feature
    // as task id will be null && and additional payment will be null
    private String getPaymentUrl(UserDetails userDetails, boolean isAdditionalPayment) {

        postData = "&txnid=" + transaction_Id +
                "&device_type=1" +
                "&ismobileview=1" +
                "&productinfo=" + (isInstaBooking ? userDetails.UserID : taskDetailModel.taskId) +
                "&user_credentials=" + userDetails.Email +
                "&key=" + BuildConfig.PAYUBIZ_HDFC_KEY +
                "&instrument_type=" + PreferenceUtility.getInstance(mContext).getFCMRegID() +
                "&surl=" + BuildConfig.PAYUBIZ_SUCCESS_URL +
                "&furl=" + BuildConfig.PAYUBIZ_FAIL_URL + "" +
                "&instrument_id=7dd17561243c202" +
                "&firstname=" + userDetails.UserName +
                "&email=" + userDetails.Email +
                "&phone=" + userDetails.PhoneNumber +
                "&amount=" + (isAdditionalPayment ? taskDetailModel.additionalQuoteAmount : providerModel.quotePrice) +
//                "&bankcode=PAYUW" + //for PayU Money
//                "&pg=WALLET"+//for PayU Money
                "&udf1=Task Start Date : " + taskDetailModel.taskStartdate +
                "&udf2=" + (isInstaBooking ? Utility.EMPTY_STRING : "Provider Id : " + providerModel.providerId) +
                "&udf3=" + NetworkUtility.TAGS.PLATFORMTYPE.ANDROID +
                "&udf4=" + (isAdditionalPayment ? Utility.TASK_STATUS.ADDITIONAL_PAYMENT_REQUESTED : "") +
                "&udf5=" +
                "&hash=";

        return postData;
    }

    String postData;
    String transaction_Id;

    // if payment is done using insta feature then
    // post data will be generated like strategic partner feature
    // as task id will be null && and additional payment will be null
    private Map<String, Object> getPaymentTransactionFields(UserDetails userDetails, boolean isForAdditionalQuote) {

        Map<String, Object> mParams = new HashMap<>();

        transaction_Id = System.currentTimeMillis() + "";
        mParams.put("key", BuildConfig.PAYUBIZ_HDFC_KEY);
        mParams.put("amount", isForAdditionalQuote ? taskDetailModel.additionalQuoteAmount : providerModel.quotePrice);
        mParams.put("txnid", transaction_Id);
        mParams.put("email", userDetails.Email);
        mParams.put("productinfo", isInstaBooking ? userDetails.UserID : taskDetailModel.taskId);
        mParams.put("firstname", userDetails.UserName);
        mParams.put("udf1", "Task Start Date : " + taskDetailModel.taskStartdate);
        mParams.put("udf2", isInstaBooking ? Utility.EMPTY_STRING : "Provider Id : " + providerModel.providerId);
        mParams.put("udf3", NetworkUtility.TAGS.PLATFORMTYPE.ANDROID);
        mParams.put("udf4", isForAdditionalQuote ? Utility.TASK_STATUS.ADDITIONAL_PAYMENT_REQUESTED : "");
        mParams.put("udf5", "");
        mParams.put("user_credentials", BuildConfig.PAYUBIZ_HDFC_KEY + ":" + userDetails.Email);

        return mParams;
    }


    Response.Listener mCallPaymentWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {

            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;
                hideProgressDialog();
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:

                        /**
                         * Changes @Bhavesh : 7thJuly,2017
                         * In case we have to bypass the payment
                         */
                        if (BuildConfig.NEED_TO_BYPASS_PAYMENT) {
//                            PLEASE NOTE: THIS IS JUST TO BYPPASS THE PAYMENT GATEWAY. THIS IS NOT
//                            GOING TO RUN IN LIVE ENVIRONMENT BUILDS
                            // Direct bypass the things
                            if (!isInstaBooking && jsonObject.getString(NetworkUtility.TAGS.IS_FOR_ADDITIONAL_QUOTE).equalsIgnoreCase(getString(R.string.label_yes))) {
                                //Call update payment service from here with all the response come from service
                                updatePaymentStatus(true, getString(R.string.message_payment_bypassed), true, NetworkUtility.TAGS.PAYMENT_METHOD_PAYU);
                            } else {
                                //Call update payment service from here with all the response come from service
                                if (isInstaBooking)
                                    callCreateInstaBookingTaskWS(true, "Payment has been bypassed for development", NetworkUtility.TAGS.PAYMENT_METHOD_PAYU);
                                else
                                    updatePaymentStatus(true, getString(R.string.message_payment_bypassed), false, NetworkUtility.TAGS.PAYMENT_METHOD_PAYU);
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
                            Intent intent = new Intent(PaymentsStepActivity.this, PaymentsActivity.class);
                            intent.putExtra("url", BuildConfig.PAYUBIZ_HDFC_URL);
                            intent.putExtra("postData", postData.replaceAll("hash=", "hash=" + jsonObject.optString("hash_string")));
                            // if task is generated from insta booking feature then addition payment field will not come in response
                            if (!isInstaBooking && jsonObject.getString(NetworkUtility.TAGS.IS_FOR_ADDITIONAL_QUOTE).equalsIgnoreCase(getString(R.string.label_yes))) {
                                startActivityForResult(intent, Utility.ADDITIONAL_REQUEST_START_PAYMENT);
                            } else {
                                startActivityForResult(intent, Utility.REQUEST_START_PAYMENT);
                            }

                        }
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityPaymentDetailBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mActivityPaymentDetailBinding.getRoot());
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
                mCallPaymentWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
        }
    };

    Response.ErrorListener mCallPaymentWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(final VolleyError error) {
            hideProgressDialog();
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
//            hideProgressDialog();

            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityPaymentDetailBinding.getRoot());

        }
    };

    /*
       * Update finalized sp id on firebase.
       * @Sanjay 20 Feb 2016
       * */
    private void updateSelectedSpOnFirebase(final TaskDetailModel taskDetailModel, final ProviderModel providerModel) {
        String formattedTaskId = FirebaseUtils.getPrefixTaskId(taskDetailModel.taskId);
        String formattedSpId = FirebaseUtils.getPrefixSPId(providerModel.providerId);
        String formattedUserId = "";
        final UserDetails userDetails = PreferenceUtility.getInstance(PaymentsStepActivity.this).getUserDetails();
        if (userDetails != null) {
            formattedUserId = FirebaseUtils.getPrefixUserId(userDetails.UserID);
        }
        FirebaseHelper.getRecentChatRef(formattedUserId).child(formattedTaskId).removeValue();
        if (!TextUtils.isEmpty(formattedTaskId) && !TextUtils.isEmpty(formattedSpId)) {
            FirebaseHelper.getTaskRef(formattedTaskId).child(FirebaseHelper.KEY_SELECTEDSPID).setValue(formattedSpId);
        }

        final String formattedId = FirebaseUtils.get_T_SP_U_FormattedId(formattedTaskId, formattedSpId, formattedUserId);
        final String finalFormattedUserId = formattedUserId;
        FirebaseHelper.getTaskChatRef(formattedTaskId).child(formattedId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {
                    TaskChatModel taskChatModel = dataSnapshot.getValue(TaskChatModel.class);
                    taskChatModel.chatId = formattedId;
                    FirebaseHelper.getRecentChatRef(finalFormattedUserId).child(taskChatModel.chatId).setValue(taskChatModel);

                    if (isInstaBooking) {
        /* * Add new task detail on firebase
         * @Giteeka sep 7 2017 for insta booking
         */
                        ChatTaskModel chatTaskModel = new ChatTaskModel();
                        chatTaskModel.taskId = FirebaseUtils.getPrefixTaskId(taskDetailModel.taskId);
                        chatTaskModel.taskDesc = taskDetailModel.taskDesc;
                        chatTaskModel.categoryId = taskDetailModel.categoryId;
                        chatTaskModel.categoryName = taskDetailModel.categoryName;
                        chatTaskModel.selectedSPId = providerModel.providerId;
                        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
                        chatTaskModel.userId = FirebaseUtils.getPrefixUserId(userDetails.UserID);
                        FirebaseHelper.getTaskRef(chatTaskModel.taskId).setValue(chatTaskModel);
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void callCreateInstaBookingTaskWS(boolean isSuccess, String response, String paymentMethod) {
        //        TASK_CREATE_INSTA_BOOKING

//        Required Params => task_desc,address_id,city_id,cat_id,start_datetime,
// media_file,subcategory_id,spUserId,txnid,cheepcode,quote_amount,payable_amount
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityPaymentDetailBinding.getRoot());
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
//        mParams.put(NetworkUtility.TAGS.ADDRESS_ID, taskDetailModel.taskAddressId);
        if (Integer.parseInt(mSelectedAddressModelForInsta.address_id) > 0) {
            mParams.put(NetworkUtility.TAGS.ADDRESS_ID, mSelectedAddressModelForInsta.address_id);
        } else {
            // In case its nagative then provide other address information
            /**
             * public String address_initials;
             public String address;
             public String category; //comes from NetworkUtility.TAGS.ADDRESS_TYPE.
             public String lat;
             public String lng;
             */
            mParams.put(NetworkUtility.TAGS.ADDRESS_INITIALS, mSelectedAddressModelForInsta.address_initials);
            mParams.put(NetworkUtility.TAGS.ADDRESS, mSelectedAddressModelForInsta.address);
            mParams.put(NetworkUtility.TAGS.CATEGORY, mSelectedAddressModelForInsta.category);
            mParams.put(NetworkUtility.TAGS.LAT, mSelectedAddressModelForInsta.lat);
            mParams.put(NetworkUtility.TAGS.LNG, mSelectedAddressModelForInsta.lng);
            mParams.put(NetworkUtility.TAGS.CITY_NAME, mSelectedAddressModelForInsta.cityName);
            mParams.put(NetworkUtility.TAGS.COUNTRY, mSelectedAddressModelForInsta.countryName);
            mParams.put(NetworkUtility.TAGS.STATE, mSelectedAddressModelForInsta.stateName);
        }
        mParams.put(NetworkUtility.TAGS.CITY_ID, userDetails.CityID);
        mParams.put(NetworkUtility.TAGS.CAT_ID, taskDetailModel.categoryId);
        mParams.put(NetworkUtility.TAGS.START_DATETIME, taskDetailModel.taskStartdate);
        mParams.put(NetworkUtility.TAGS.SUBCATEGORY_ID, taskDetailModel.subCategoryID);
        mParams.put(NetworkUtility.TAGS.SP_USER_ID, providerModel.providerId);
        mParams.put(NetworkUtility.TAGS.PAYMENT_STATUS, isSuccess ? Utility.PAYMENT_STATUS.COMPLETED : Utility.PAYMENT_STATUS.FAILED);
        mParams.put(NetworkUtility.TAGS.PAYMENT_LOG, response);
        mParams.put(NetworkUtility.TAGS.PAYMENT_METHOD, paymentMethod);
        mParams.put(NetworkUtility.TAGS.TRANSACTION_ID, TextUtils.isEmpty(transaction_Id) ? Utility.ZERO_STRING : transaction_Id);
        if (!TextUtils.isEmpty(cheepCode)) {
            mParams.put(NetworkUtility.TAGS.CHEEPCODE, cheepCode);
            mParams.put(NetworkUtility.TAGS.PAYABLE_AMOUNT, providerModel.quotePrice);
            mParams.put(NetworkUtility.TAGS.PROMOCODE_PRICE, promocode_price);
            // new field for refer and earn functionality
            mParams.put(NetworkUtility.TAGS.IS_REFER_CODE, isReferCode);
        } else {
            mParams.put(NetworkUtility.TAGS.CHEEPCODE, Utility.EMPTY_STRING);
            mParams.put(NetworkUtility.TAGS.PAYABLE_AMOUNT, providerModel.quotePrice);
            mParams.put(NetworkUtility.TAGS.PROMOCODE_PRICE, "0");
            // new field for refer and earn functionality
            mParams.put(NetworkUtility.TAGS.IS_REFER_CODE, Utility.BOOLEAN.NO);
        }
        mParams.put(NetworkUtility.TAGS.QUOTE_AMOUNT, providerModel.spWithoutGstQuotePrice);
        LogUtils.LOGE(TAG, "Quote Amout : " + mParams.get(NetworkUtility.TAGS.QUOTE_AMOUNT));
        mParams.put(NetworkUtility.TAGS.USED_WALLET_BALANCE, usedWalletBalance);

        // For AppsFlyer
        mTaskCreationParams = new HashMap<>();
        mTaskCreationParams.put(NetworkUtility.TAGS.TASK_DESC, taskDetailModel.taskDesc);
//        mParams.put(NetworkUtility.TAGS.ADDRESS_ID, taskDetailModel.taskAddressId);
        if (Integer.parseInt(mSelectedAddressModelForInsta.address_id) > 0) {
            mTaskCreationParams.put(NetworkUtility.TAGS.ADDRESS_ID, mSelectedAddressModelForInsta.address_id);
        } else {
            // In case its nagative then provide other address information
            /**
             * public String address_initials;
             public String address;
             public String category; //comes from NetworkUtility.TAGS.ADDRESS_TYPE.
             public String lat;
             public String lng;
             */
            mTaskCreationParams.put(NetworkUtility.TAGS.ADDRESS_INITIALS, mSelectedAddressModelForInsta.address_initials);
            mTaskCreationParams.put(NetworkUtility.TAGS.ADDRESS, mSelectedAddressModelForInsta.address);
            mTaskCreationParams.put(NetworkUtility.TAGS.CATEGORY, mSelectedAddressModelForInsta.category);
            mTaskCreationParams.put(NetworkUtility.TAGS.LAT, mSelectedAddressModelForInsta.lat);
            mTaskCreationParams.put(NetworkUtility.TAGS.LNG, mSelectedAddressModelForInsta.lng);
            mTaskCreationParams.put(NetworkUtility.TAGS.CITY_NAME, mSelectedAddressModelForInsta.cityName);
            mTaskCreationParams.put(NetworkUtility.TAGS.COUNTRY, mSelectedAddressModelForInsta.countryName);
            mTaskCreationParams.put(NetworkUtility.TAGS.STATE, mSelectedAddressModelForInsta.stateName);
        }
        mTaskCreationParams.put(NetworkUtility.TAGS.CITY_ID, userDetails.CityID);
        mTaskCreationParams.put(NetworkUtility.TAGS.CAT_ID, taskDetailModel.categoryId);
        mTaskCreationParams.put(NetworkUtility.TAGS.START_DATETIME, taskDetailModel.taskStartdate);
        mTaskCreationParams.put(NetworkUtility.TAGS.PAYMENT_STATUS, isSuccess ? Utility.PAYMENT_STATUS.COMPLETED : Utility.PAYMENT_STATUS.FAILED);
        mTaskCreationParams.put(NetworkUtility.TAGS.PAYMENT_LOG, response);
        mTaskCreationParams.put(NetworkUtility.TAGS.SUBCATEGORY_ID, taskDetailModel.subCategoryID);
        mTaskCreationParams.put(NetworkUtility.TAGS.SP_USER_ID, providerModel.providerId);
        mTaskCreationParams.put(NetworkUtility.TAGS.TRANSACTION_ID, TextUtils.isEmpty(transaction_Id) ? Utility.ZERO_STRING : transaction_Id);
        if (!TextUtils.isEmpty(cheepCode)) {
            mTaskCreationParams.put(NetworkUtility.TAGS.CHEEPCODE, cheepCode);
            mTaskCreationParams.put(NetworkUtility.TAGS.PAYABLE_AMOUNT, providerModel.quotePrice);
            mTaskCreationParams.put(NetworkUtility.TAGS.PROMOCODE_PRICE, promocode_price);


        } else {
            mTaskCreationParams.put(NetworkUtility.TAGS.CHEEPCODE, Utility.EMPTY_STRING);
            mTaskCreationParams.put(NetworkUtility.TAGS.PAYABLE_AMOUNT, providerModel.quotePrice);
            mTaskCreationParams.put(NetworkUtility.TAGS.PROMOCODE_PRICE, "0");
        }
        mTaskCreationParams.put(NetworkUtility.TAGS.USED_WALLET_BALANCE, usedWalletBalance);
        mTaskCreationParams.put(NetworkUtility.TAGS.QUOTE_AMOUNT, providerModel.spWithoutGstQuotePrice);

        HashMap<String, File> mFileParams = new HashMap<>();
        if (!TextUtils.isEmpty(taskDetailModel.taskImage)
                && new File(taskDetailModel.taskImage).exists()) {
            mFileParams.put(NetworkUtility.TAGS.MEDIA_FILE, new File(taskDetailModel.taskImage));
        }

        // Url is based on condition if address id is greater then 0 then it means we need to update the existing address
        VolleyNetworkRequest mVolleyNetworkRequestForSPList = new VolleyNetworkRequest(NetworkUtility.WS.TASK_CREATE_INSTA_BOOKING
                , mCallUpdatePaymentStatusWSErrorListener
                , mCallCreateInstaTaskWSResponseListener
                , mHeaderParams
                , mParams
                , mFileParams);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForSPList);

    }

    /**
     * Used for payment
     */
    private void updatePaymentStatus(boolean isSuccess, String response, boolean isAdditionalPayment, String paymentMethod) {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityPaymentDetailBinding.getRoot());
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
        if (!TextUtils.isEmpty(cheepCode)) {
            mParams.put(NetworkUtility.TAGS.CHEEPCODE, cheepCode);
            mParams.put(NetworkUtility.TAGS.PROMOCODE_PRICE, promocode_price);
            mParams.put(NetworkUtility.TAGS.IS_REFER_CODE, isReferCode);
        } else {
            mParams.put(NetworkUtility.TAGS.CHEEPCODE, Utility.EMPTY_STRING);
            mParams.put(NetworkUtility.TAGS.PROMOCODE_PRICE, Utility.ZERO_STRING);
            mParams.put(NetworkUtility.TAGS.IS_REFER_CODE, Utility.BOOLEAN.NO);
        }
        mParams.put(NetworkUtility.TAGS.TRANSACTION_ID, TextUtils.isEmpty(transaction_Id) ? Utility.ZERO_STRING : transaction_Id);
        mParams.put(NetworkUtility.TAGS.PAYMENT_STATUS, isSuccess ? Utility.PAYMENT_STATUS.COMPLETED : Utility.PAYMENT_STATUS.FAILED);
        mParams.put(NetworkUtility.TAGS.PAYMENT_LOG, response);
        mParams.put(NetworkUtility.TAGS.IS_FOR_ADDITIONAL_QUOTE, isAdditionalPayment
                ? getString(R.string.label_yes).toLowerCase() :
                getString(R.string.label_no).toLowerCase());
        mParams.put(NetworkUtility.TAGS.USED_WALLET_BALANCE, usedWalletBalance);

        mParams.put(NetworkUtility.TAGS.PAYMENT_METHOD, paymentMethod);

        // Url is based on condition if address id is greater then 0 then it means we need to update the existing address
        VolleyNetworkRequest mVolleyNetworkRequestForSPList = new VolleyNetworkRequest(NetworkUtility.WS.PAYMENT
                , mCallUpdatePaymentStatusWSErrorListener
                , mCallUpdatePaymentStatusWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForSPList);
    }

    Response.Listener mCallUpdatePaymentStatusWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {

            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;
                hideProgressDialog();
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:

                        JSONObject jsonData = jsonObject.optJSONObject(NetworkUtility.TAGS.DATA);
                        String taskStatus = jsonData.optString(NetworkUtility.TAGS.TASK_STATUS);

//                        callTaskDetailWS();

                        if (Utility.TASK_STATUS.PAID.equalsIgnoreCase(taskStatus)) {
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
                                    updateSelectedSpOnFirebase(taskDetailModel, providerModel);
                                }
                            }

                            //  Refresh UI for Paid status
                            //  FillProviderDetails(providerModel);

                            MessageEvent messageEvent = new MessageEvent();
                            messageEvent.BROADCAST_ACTION = Utility.BROADCAST_TYPE.TASK_PAID;
                            messageEvent.id = taskDetailModel.taskId;
                            EventBus.getDefault().post(messageEvent);

                             /*
                             *  @Changes : 7th July, 2017 :- Bhavesh Patadiya
                             *  Need to show Model Dialog once Payment has been made successful. Once
                             *  User clicks on OK. we will finish of the activity.
                             */
                            String title = mContext.getString(R.string.label_great_choice_x, PreferenceUtility.getInstance(mContext).getUserDetails().UserName);
                            final SuperCalendar superStartDateTimeCalendar = SuperCalendar.getInstance();
                            superStartDateTimeCalendar.setTimeZone(SuperCalendar.SuperTimeZone.GMT.GMT);
                            superStartDateTimeCalendar.setTimeInMillis(Long.parseLong(taskDetailModel.taskStartdate));
                            superStartDateTimeCalendar.setLocaleTimeZone();

                            int onlydate = Integer.parseInt(superStartDateTimeCalendar.format("dd"));
                            String message = fetchMessageFromDateOfMonth(onlydate, superStartDateTimeCalendar);

//                            final UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
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
                                    });
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
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityPaymentDetailBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mActivityPaymentDetailBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.USER_DELETED:
                    case NetworkUtility.TAGS.STATUSCODETYPE.FORCE_LOGOUT_REQUIRED:
                        //Logout and finish the current activity
                        Utility.logout(mContext, true, statusCode);
                        ;
                        finish();
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mCallUpdatePaymentStatusWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }

        }
    };


    Response.Listener mCallCreateInstaTaskWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {

            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;
                hideProgressDialog();
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
//                        Utility.showToast(PaymentsStepActivity.this, jsonObject.getString(NetworkUtility.TAGS.MESSAGE));

                        // Send Event tracking for AppsFlyer
                        AppsFlyerLib.getInstance().trackEvent(mContext, NetworkUtility.TAGS.APPSFLYER_CUSTOM_TRACK_EVENTS.TASK_CREATE, mTaskCreationParams);

                        onSuccessfullInstaBookingTaskCompletion(jsonObject);
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityPaymentDetailBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mActivityPaymentDetailBinding.getRoot());
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
                mCallUpdatePaymentStatusWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }

        }
    };
    // check is task is from insta booking or not

    private void onSuccessfullInstaBookingTaskCompletion(JSONObject jsonObject) {
        TaskDetailModel taskDetailModel = (TaskDetailModel) Utility.getObjectFromJsonString(jsonObject.optString(NetworkUtility.TAGS.DATA), TaskDetailModel.class);

        if (providerModel != null) {
            // add task and pro entry for firebase
            updateSelectedSpOnFirebase(taskDetailModel, providerModel);
        }


        // finish current activity
        finish();
        // br for finished task creation activity
        Intent intent = new Intent(Utility.BR_ON_TASK_CREATED_FOR_INSTA_BOOKING);
        sendBroadcast(intent);
    }


    private String fetchMessageFromDateOfMonth(int day, SuperCalendar superStartDateTimeCalendar) {
        String date = Utility.EMPTY_STRING;
        String DATE_FORMAT_TASK_HAS_BEEN_PAID_DATE_TH = SuperCalendar.SuperFormatter.DATE + getString(R.string.label_th_date) + SuperCalendar.SuperFormatter.MONTH_JAN;
        String DATE_FORMAT_TASK_HAS_BEEN_PAID_DATE_ST = SuperCalendar.SuperFormatter.DATE + getString(R.string.label_st_date) + SuperCalendar.SuperFormatter.MONTH_JAN;
        String DATE_FORMAT_TASK_HAS_BEEN_PAID_DATE_RD = SuperCalendar.SuperFormatter.DATE + getString(R.string.label_rd_date) + SuperCalendar.SuperFormatter.MONTH_JAN;
        String DATE_FORMAT_TASK_HAS_BEEN_PAID_DATE_ND = SuperCalendar.SuperFormatter.DATE + getString(R.string.label_nd_date) + SuperCalendar.SuperFormatter.MONTH_JAN;

        if (day >= 11 && day <= 13) {
            date = superStartDateTimeCalendar.format(DATE_FORMAT_TASK_HAS_BEEN_PAID_DATE_TH);
        } else {
            switch (day % 10) {
                case 1:
                    date = superStartDateTimeCalendar.format(DATE_FORMAT_TASK_HAS_BEEN_PAID_DATE_ST);
                    break;
                case 2:
                    date = superStartDateTimeCalendar.format(DATE_FORMAT_TASK_HAS_BEEN_PAID_DATE_ND);
                    break;
                case 3:
                    date = superStartDateTimeCalendar.format(DATE_FORMAT_TASK_HAS_BEEN_PAID_DATE_RD);
                    break;
                default:
                    date = superStartDateTimeCalendar.format(DATE_FORMAT_TASK_HAS_BEEN_PAID_DATE_TH);
                    break;
            }
        }
        // as per  24 hour format 13 spt 2017
//        String DATE_FORMAT_TASK_HAS_BEEN_PAID_TIME = SuperCalendar.SuperFormatter.HOUR_12_HOUR_2_DIGIT + ":" + SuperCalendar.SuperFormatter.MINUTE + "' '" + SuperCalendar.SuperFormatter.AM_PM;
        String time = superStartDateTimeCalendar.format(Utility.DATE_FORMAT_HH_MM_AM);
        String message = mContext.getString(R.string.desc_task_payment_done_acknowledgement
                , providerModel.userName, date + getString(R.string.label_at) + time);
        message = message.replace(".", "");
        message = message.replace(getString(R.string.label_am_caps), getString(R.string.label_am_small)).replace(getString(R.string.label_pm_caps), getString(R.string.label_pm_small));
        return message + ".";
    }

    Response.ErrorListener mCallUpdatePaymentStatusWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(final VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            hideProgressDialog();

            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityPaymentDetailBinding.getRoot());

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Utility.REQUEST_START_PAYMENT) {
//            Toast.makeText(mContext, "OnActivityResult called with resultCode:" + resultCode + ", requestCode:" + requestCode, Toast.LENGTH_SHORT).show();
            if (resultCode == RESULT_OK) {
                setTaskState(STEP_THREE_VERIFIED);
                //success
                if (data != null) {
                    Log.d(TAG, "onActivityResult() called with success: result= [" + data.getStringExtra("payu_response") + "]");
                    //Call update payment service from here with all the response come from service
                    // check is task is from insta booking or not
                    if (isInstaBooking)
                        callCreateInstaBookingTaskWS(true, data.getStringExtra("payu_response"), NetworkUtility.TAGS.PAYMENT_METHOD_PAYU);
                    else
                        updatePaymentStatus(true, data.getStringExtra("payu_response"), false, NetworkUtility.TAGS.PAYMENT_METHOD_PAYU);
                }
            }
            if (resultCode == RESULT_CANCELED) {
                setTaskState(STEP_THREE_UNVERIFIED);
                //failed
                if (data != null) {
                    Log.d(TAG, "onActivityResult() called with failed: result= [" + data.getStringExtra("payu_response") + "]");
                    //Call update payment service from here with all the response come from service
                    // check is task is from insta booking or not
                    if (isInstaBooking)
                        Utility.showSnackBar(getString(R.string.msg_payment_failed), mActivityPaymentDetailBinding.getRoot());
                    else
                        updatePaymentStatus(false, data.getStringExtra("payu_response"), false, NetworkUtility.TAGS.PAYMENT_METHOD_PAYU);
                    Utility.showSnackBar(getString(R.string.msg_payment_failed), mActivityPaymentDetailBinding.getRoot());
                }
            }
        } else if (requestCode == Utility.ADDITIONAL_REQUEST_START_PAYMENT) {
//            Toast.makeText(mContext, "OnActivityResult called with resultCode:" + resultCode + ", requestCode:" + requestCode, Toast.LENGTH_SHORT).show();
            if (resultCode == RESULT_OK) {
                setTaskState(STEP_THREE_VERIFIED);
                //success
                if (data != null) {
                    Log.d(TAG, "onActivityResult() called with success: result= [" + data.getStringExtra("payu_response") + "]");
                    //Call update payment service from here with all the response come from service
                    updatePaymentStatus(true, data.getStringExtra("payu_response"), true, NetworkUtility.TAGS.PAYMENT_METHOD_PAYU);
                }
            }
            if (resultCode == RESULT_CANCELED) {
                setTaskState(STEP_THREE_UNVERIFIED);
                //failed
                if (data != null) {
                    Log.d(TAG, "onActivityResult() called with failed: result= [" + data.getStringExtra("payu_response") + "]");
                    //Call update payment service from here with all the response come from service
                    updatePaymentStatus(false, data.getStringExtra("payu_response"), true, NetworkUtility.TAGS.PAYMENT_METHOD_PAYU);
                    Utility.showSnackBar(getString(R.string.msg_payment_failed), mActivityPaymentDetailBinding.getRoot());
                }
            }
        }
    }


    /**
     * Below would manage the state of Step while creating task creation
     */
    public static final int STEP_ONE_NORMAL = 1;
    public static final int STEP_ONE_UNVERIFIED = 2;
    public static final int STEP_ONE_VERIFIED = 3;
    public static final int STEP_TWO_NORMAL = 4;
    public static final int STEP_TWO_UNVERIFIED = 5;
    public static final int STEP_TWO_VERIFIED = 6;
    public static final int STEP_THREE_NORMAL = 7;
    public static final int STEP_THREE_UNVERIFIED = 8;
    public static final int STEP_THREE_VERIFIED = 9;
    public int mCurrentStep = -1;

    public void setTaskState(int step_state) {
        mCurrentStep = step_state;
        switch (step_state) {
            case STEP_ONE_NORMAL:
                mActivityPaymentDetailBinding.textStep1.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_normal));
                mActivityPaymentDetailBinding.textStep1.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mActivityPaymentDetailBinding.textStep2.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_normal));
                mActivityPaymentDetailBinding.textStep2.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mActivityPaymentDetailBinding.textStep3.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_normal));
                mActivityPaymentDetailBinding.textStep3.setTextColor(ContextCompat.getColor(mContext, R.color.white));


                break;
            case STEP_ONE_UNVERIFIED:
                mActivityPaymentDetailBinding.textStep1.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_unverified));
                mActivityPaymentDetailBinding.textStep1.setTextColor(ContextCompat.getColor(mContext, R.color.splash_gradient_end));

                mActivityPaymentDetailBinding.textStep2.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_normal));
                mActivityPaymentDetailBinding.textStep2.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mActivityPaymentDetailBinding.textStep3.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_normal));
                mActivityPaymentDetailBinding.textStep3.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                break;
            case STEP_ONE_VERIFIED:
            case STEP_TWO_NORMAL:
                mActivityPaymentDetailBinding.textStep1.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_verified));
                mActivityPaymentDetailBinding.textStep1.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mActivityPaymentDetailBinding.textStep2.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_normal));
                mActivityPaymentDetailBinding.textStep2.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mActivityPaymentDetailBinding.textStep3.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_normal));
                mActivityPaymentDetailBinding.textStep3.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                break;
            case STEP_TWO_UNVERIFIED:
                mActivityPaymentDetailBinding.textStep1.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_verified));
                mActivityPaymentDetailBinding.textStep1.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mActivityPaymentDetailBinding.textStep2.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_unverified));
                mActivityPaymentDetailBinding.textStep2.setTextColor(ContextCompat.getColor(mContext, R.color.splash_gradient_end));

                mActivityPaymentDetailBinding.textStep3.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_normal));
                mActivityPaymentDetailBinding.textStep3.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                break;
            case STEP_TWO_VERIFIED:
            case STEP_THREE_NORMAL:
                mActivityPaymentDetailBinding.textStep1.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_verified));
                mActivityPaymentDetailBinding.textStep1.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mActivityPaymentDetailBinding.textStep2.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_verified));
                mActivityPaymentDetailBinding.textStep2.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mActivityPaymentDetailBinding.textStep3.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_normal));
                mActivityPaymentDetailBinding.textStep3.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                break;
            case STEP_THREE_UNVERIFIED:
                mActivityPaymentDetailBinding.textStep1.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_verified));
                mActivityPaymentDetailBinding.textStep1.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mActivityPaymentDetailBinding.textStep2.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_verified));
                mActivityPaymentDetailBinding.textStep2.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mActivityPaymentDetailBinding.textStep3.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_unverified));
                mActivityPaymentDetailBinding.textStep3.setTextColor(ContextCompat.getColor(mContext, R.color.splash_gradient_end));

                break;
            case STEP_THREE_VERIFIED:
                mActivityPaymentDetailBinding.textStep1.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_verified));
                mActivityPaymentDetailBinding.textStep1.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mActivityPaymentDetailBinding.textStep2.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_verified));
                mActivityPaymentDetailBinding.textStep2.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                mActivityPaymentDetailBinding.textStep3.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_steps_verified));
                mActivityPaymentDetailBinding.textStep3.setTextColor(ContextCompat.getColor(mContext, R.color.white));

                break;
        }
    }

    private void getAmountWithGstWS(String discountAmout) {
        showProgressDialog();
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);
        Map<String, String> mParams = new HashMap<>();

        mParams.put(NetworkUtility.TAGS.QUOTE_AMOUNT, providerModel.quotePrice);
        mParams.put(NetworkUtility.TAGS.DISCOUNT_AMOUNT, discountAmout);
        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.GET_AMOUNT_WITH_GST
                , mCallValidateCheepCodeWSErrorListener
                , mCallGetAmoutWithGSTWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest, NetworkUtility.WS.GET_AMOUNT_WITH_GST);
    }

    private void callGetReferBalance() {
        showProgressDialog();
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);
        Map<String, String> mParams = new HashMap<>();
        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.REFER_BALANCE
                , mCallGetReferBalanceErrorListener
                , mCallGetReferBalanceWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest, NetworkUtility.WS.REFER_BALANCE);

    }

    private String referralBalance;
    private String maxReferDiscount;
    Response.Listener mCallGetReferBalanceWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {
            hideProgressDialog();
            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;
                hideProgressDialog();
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
                        referralBalance = (jsonObject.optJSONObject(NetworkUtility.TAGS.DATA)).optString(NetworkUtility.TAGS.WALLET_BALANCE);
                        maxReferDiscount = (jsonObject.optJSONObject(NetworkUtility.TAGS.DATA)).optString(NetworkUtility.TAGS.MAX_REFER_DISCOUNT);
                        mActivityPaymentDetailBinding.txtreferraldiscount.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(referralBalance)));
                        Log.i(TAG, "onResponse: " + jsonObject.toString());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityPaymentDetailBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mActivityPaymentDetailBinding.getRoot());
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
                mCallUpdatePaymentStatusWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }

        }
    };
    Response.ErrorListener mCallGetReferBalanceErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");
            hideProgressDialog();
        }
    };

}
