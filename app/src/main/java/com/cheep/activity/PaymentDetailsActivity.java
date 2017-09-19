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
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.R;
import com.cheep.custom_view.BottomAlertDialog;
import com.cheep.custom_view.CFEditTextRegular;
import com.cheep.databinding.ActivityPaymentDetailBinding;
import com.cheep.model.AddressModel;
import com.cheep.model.MessageEvent;
import com.cheep.model.ProviderModel;
import com.cheep.model.TaskDetailModel;
import com.cheep.model.UserDetails;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.cheep.utils.Utility.Extra.PAYMENT_VIEW_IS_ADDITIONAL_CHARGE;


public class PaymentDetailsActivity extends BaseAppCompatActivity {


    public static void newInstance(Context context, TaskDetailModel taskDetailModel, ProviderModel providerModel, int additional) {
        Intent intent = new Intent(context, PaymentDetailsActivity.class);
        intent.putExtra(Utility.Extra.DATA, Utility.getJsonStringFromObject(providerModel));
        intent.putExtra(Utility.Extra.DATA_2, Utility.getJsonStringFromObject(taskDetailModel));
        intent.putExtra(PAYMENT_VIEW_IS_ADDITIONAL_CHARGE, additional);
        context.startActivity(intent);
    }

    public static void newInstance(Context context, TaskDetailModel taskDetailModel, ProviderModel providerModel, int additional, boolean isInstaBooking, AddressModel mSelectedAddressModel) {
        Intent intent = new Intent(context, PaymentDetailsActivity.class);
        intent.putExtra(Utility.Extra.DATA, Utility.getJsonStringFromObject(providerModel));
        intent.putExtra(Utility.Extra.DATA_2, Utility.getJsonStringFromObject(taskDetailModel));
        intent.putExtra(Utility.Extra.SELECTED_ADDRESS_MODEL, Utility.getJsonStringFromObject(mSelectedAddressModel));
        intent.putExtra(Utility.Extra.TASK_TYPE_IS_INSTA, isInstaBooking);
        intent.putExtra(PAYMENT_VIEW_IS_ADDITIONAL_CHARGE, additional);
        context.startActivity(intent);
    }

    public static void newInstance(Context context, TaskDetailModel taskDetailModel, ProviderModel providerModel, boolean viewonly) {
        Intent intent = new Intent(context, PaymentDetailsActivity.class);
        intent.putExtra(Utility.Extra.DATA, Utility.getJsonStringFromObject(providerModel));
        intent.putExtra(Utility.Extra.DATA_2, Utility.getJsonStringFromObject(taskDetailModel));
        intent.putExtra(Utility.Extra.PAYMENT_VIEW, viewonly);
        context.startActivity(intent);
    }

    private static final String TAG = "PaymentsStepActivity";
    private ActivityPaymentDetailBinding mActivityPaymentDetailBinding;
    Bundle bundle;
    private ProviderModel providerModel;
    private TaskDetailModel taskDetailModel;
    //ForInsta Booking
    private boolean isInstaBooking = false;
    private String actualQuotePrice;
    private AddressModel mSelectedAddressModelForInsta;


    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
          when the device runing out of memory we dont want the user to restart the payment. rather we close it and redirect them to previous activity.
         */
        mActivityPaymentDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_payment_detail);
        // add event bus listener
        EventBus.getDefault().register(this);

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
        mActivityPaymentDetailBinding.textMaterialDisclaimer.setVisibility(View.VISIBLE);

        if (getIntent().hasExtra(Utility.Extra.PAYMENT_VIEW_IS_ADDITIONAL_CHARGE)) {
            if (taskDetailModel != null) {
                mActivityPaymentDetailBinding.textTitle.setText(taskDetailModel.categoryName);
            }
            int isAdditional = getIntent().getIntExtra(Utility.Extra.PAYMENT_VIEW_IS_ADDITIONAL_CHARGE, 0);
            if (isAdditional == 0) {
                if (taskDetailModel != null) {
                    resetPromocodeValue();
                }
            } else {
                if (taskDetailModel != null) {
                    setAdditionalPayment();
                }
            }
            mActivityPaymentDetailBinding.textpromocodelabel.setEnabled(true);
            mActivityPaymentDetailBinding.lnPromoCodeDisclaimer.setVisibility(View.GONE);

        } else if (getIntent().hasExtra(Utility.Extra.PAYMENT_VIEW)) {
            boolean viewonly = getIntent().getBooleanExtra(Utility.Extra.PAYMENT_VIEW, false);
            if (viewonly) {
                if (taskDetailModel != null) {
                    resetPromocodeValuePreview();
                    mActivityPaymentDetailBinding.lnDesclaimer.setVisibility(View.GONE);
                    mActivityPaymentDetailBinding.textMaterialDisclaimer.setVisibility(View.GONE);
                    Utility.loadImageView(mContext, mActivityPaymentDetailBinding.imgService, taskDetailModel.catImage, R.drawable.gradient_black);
                }
                mActivityPaymentDetailBinding.textLabelTotalPaid.setText(getString(R.string.label_total_paid));
                mActivityPaymentDetailBinding.textPay.setVisibility(View.GONE);
                mActivityPaymentDetailBinding.textpromocodelabel.setEnabled(false);
                mActivityPaymentDetailBinding.textpromocodelabel.setText(getString(R.string.label_promocode_apply));
                mActivityPaymentDetailBinding.textpromocodelabel.setTextColor(ContextCompat.getColor(this, R.color.black));
                mActivityPaymentDetailBinding.lnstep.setVisibility(View.GONE);
                mActivityPaymentDetailBinding.textStepDesc.setVisibility(View.GONE);

            }
            mActivityPaymentDetailBinding.textpromocodelabel.setEnabled(false);
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

            mActivityPaymentDetailBinding.txtdesc.setText(spannableStringBuilder);
        }

        // Add Desclaimer
        mActivityPaymentDetailBinding.imgCheepCodeClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActivityPaymentDetailBinding.textpromocodelabel.setEnabled(true);

                cheepCode = null;
                if (!TextUtils.isEmpty(actualQuotePrice)) {
                    providerModel.quotePrice = actualQuotePrice;
                }
                if (taskDetailModel != null) {
                    taskDetailModel.cheepCode = Utility.EMPTY_STRING;
                    taskDetailModel.taskDiscountAmount = Utility.ZERO_STRING;

                }
                actualQuotePrice = null;
                mActivityPaymentDetailBinding.imgCheepCodeClose.setVisibility(View.GONE);
                resetPromocodeValue();
            }
        });

    }

    /**
     * Used for Reset Payment Values after applying promocode OR Removing promociod
     */
    public void resetPromocodeValue() {
        mActivityPaymentDetailBinding.textpromocodelabel.setTextColor(ContextCompat.getColor(this, R.color.splash_gradient_end));
        mActivityPaymentDetailBinding.textpromocodelabel.setText(getResources().getString(R.string.label_enter_promocode));
        mActivityPaymentDetailBinding.lnPromoCodeDisclaimer.setVisibility(View.GONE);
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
            mActivityPaymentDetailBinding.txtprofee.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf(taskPaidAmount))));
            mActivityPaymentDetailBinding.txtadditionalcharge.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf(additionalCharges))));
            mActivityPaymentDetailBinding.txtsubtotal.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf(subTotal))));
            mActivityPaymentDetailBinding.txttotal.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf(totalPayment))));
            mActivityPaymentDetailBinding.textPay.setText(getString(R.string.label_pay_fee_v1, "" + Utility.getQuotePriceFormatter(String.valueOf(totalPayment))));
            mActivityPaymentDetailBinding.txtpromocode.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf(promocodeValue))));

            mActivityPaymentDetailBinding.textpromocodelabel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showCheepCodeDialog();
                }
            });
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
        mActivityPaymentDetailBinding.textPay.setText(getString(R.string.label_pay_fee_v1, "" + Utility.getQuotePriceFormatter(String.valueOf(taskPaidAmount))));

        mActivityPaymentDetailBinding.txtpromocode.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf(taskDetailModel.taskDiscountAmount))));
        mActivityPaymentDetailBinding.lnPromoCodeDisclaimer.setVisibility(promocodeValue == 0 ? View.GONE : View.VISIBLE);
        mActivityPaymentDetailBinding.textpromocodelabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCheepCodeDialog();
            }
        });
    }

    /**
     * Used for Additional payment
     */
    public void setAdditionalPayment() {
        int taskPaidAmount = 0;
        mActivityPaymentDetailBinding.txtprofee.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(Utility.ZERO_STRING)));
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
        mActivityPaymentDetailBinding.rlprofee.setAlpha(0.5f);
        mActivityPaymentDetailBinding.rlpromocode.setAlpha(0.5f);
        mActivityPaymentDetailBinding.textpromocodelabel.setEnabled(false);
        mActivityPaymentDetailBinding.textpromocodelabel.setText(R.string.label_enter_promocode);
        mActivityPaymentDetailBinding.txtpromocode.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(Utility.ZERO_STRING)));

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
            int isAdditionalPayment = getIntent().getIntExtra(Utility.Extra.PAYMENT_VIEW_IS_ADDITIONAL_CHARGE, 0);
            // Open payment method activity on payment as per new flow
            PaymentChoiceActivity.newInstance(mContext, taskDetailModel, providerModel, isAdditionalPayment, isInstaBooking, mSelectedAddressModelForInsta);
        }
    };

    CFEditTextRegular edtCheepcode;
    BottomAlertDialog cheepCodeDialog;

    private void showCheepCodeDialog() {

        View view = View.inflate(mContext, R.layout.dialog_add_promocode, null);
        edtCheepcode = view.findViewById(R.id.edit_cheepcode);
        cheepCodeDialog = new BottomAlertDialog(mContext);
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
        EventBus.getDefault().unregister(this);
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
            mParams.put(NetworkUtility.TAGS.QUOTE_AMOUNT, providerModel.quotePriceWithOutGST);
            mParams.put(NetworkUtility.TAGS.CHEEPCODE, cheepCode);
            mParams.put(NetworkUtility.TAGS.CAT_ID, taskDetailModel.categoryId);
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
    Response.Listener mCallValidateCheepCodeWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {

            String strResponse = (String) response;
            try {
                Utility.hideKeyboard(PaymentDetailsActivity.this, edtCheepcode);
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

        taskDetailModel.cheepCode = cheepCode;
        taskDetailModel.taskDiscountAmount = discount;

//        mActivityJobSummaryBinding.btnPay.setText(getString(R.string.label_pay_X_X_X, total, discount, payable));
//        @change only need to show payable amount
        mActivityPaymentDetailBinding.txtpromocode.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(discount)));
        mActivityPaymentDetailBinding.txttotal.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(payable)));
        mActivityPaymentDetailBinding.textPay.setText(getString(R.string.label_pay_fee_v1, Utility.getQuotePriceFormatter(payable)));
        mActivityPaymentDetailBinding.textpromocodelabel.setEnabled(false);
//        mActivityPaymentDetailBinding.textpromocodelabel.setText(cheepCode);
        mActivityPaymentDetailBinding.textpromocodelabel.setText(getString(R.string.label_promocode_apply));
        mActivityPaymentDetailBinding.textpromocodelabel.setTextColor(ContextCompat.getColor(this, R.color.black));

        mActivityPaymentDetailBinding.imgCheepCodeClose.setVisibility(View.VISIBLE);
    }

    Response.ErrorListener mCallValidateCheepCodeWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(final VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
//            hideProgressDialog();

//            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityJobSummaryBinding.getRoot());
            Utility.hideKeyboard(PaymentDetailsActivity.this, edtCheepcode);
            Utility.showToast(mContext, getString(R.string.label_something_went_wrong));

        }

    };


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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        if (event.BROADCAST_ACTION == Utility.BROADCAST_TYPE.TASK_PAID
                || event.BROADCAST_ACTION == Utility.BROADCAST_TYPE.PAYMENT_COMPLETED_NEED_TO_REDIRECT_TO_MY_TASK_SCREEN) {
            finish();
        } else if (event.BROADCAST_ACTION == Utility.BROADCAST_TYPE.TASK_PAID_FOR_INSTA_BOOKING) {
            finish();
        } else if (event.BROADCAST_ACTION == Utility.BROADCAST_TYPE.TASK_PROCESSING) {
            finish();
        }
    }


}
