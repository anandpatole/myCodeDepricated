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
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.cheep.utils.Utility.Extra.PAYMENT_VIEW_IS_ADDITIONAL_CHARGE;


public class PaymentDetailsActivity extends BaseAppCompatActivity {


    private static final String TAG = "PaymentDetailsActivity";
    private ActivityPaymentDetailBinding mActivityPaymentDetailBinding;
    Bundle bundle;
    private ProviderModel providerModel;
    private TaskDetailModel taskDetailModel;
    //ForInsta Booking
    private boolean isInstaBooking = false;
    private String actualQuotePrice;
    private AddressModel mSelectedAddressModelForInsta;
    private double usedWalletBalance = 0;

    private String referralBalance;
    private String maxReferDiscount;

    private CFEditTextRegular edtCheepcode;
    private BottomAlertDialog cheepCodeDialog;
    private String cheepCode;

    /**
     * payment for normal task
     *
     * @param context
     * @param taskDetailModel
     * @param providerModel
     * @param additional
     */
    public static void newInstance(Context context, TaskDetailModel taskDetailModel, ProviderModel providerModel, int additional) {
        Intent intent = new Intent(context, PaymentDetailsActivity.class);
        intent.putExtra(Utility.Extra.DATA, Utility.getJsonStringFromObject(providerModel));
        intent.putExtra(Utility.Extra.DATA_2, Utility.getJsonStringFromObject(taskDetailModel));
        intent.putExtra(PAYMENT_VIEW_IS_ADDITIONAL_CHARGE, additional);
        context.startActivity(intent);
    }

    /**
     * payment summary for insta booking task
     *
     * @param context
     * @param taskDetailModel
     * @param providerModel
     * @param additional
     */
    public static void newInstance(Context context, TaskDetailModel taskDetailModel, ProviderModel providerModel, int additional, boolean isInstaBooking, AddressModel mSelectedAddressModel) {
        Intent intent = new Intent(context, PaymentDetailsActivity.class);
        intent.putExtra(Utility.Extra.DATA, Utility.getJsonStringFromObject(providerModel));
        intent.putExtra(Utility.Extra.DATA_2, Utility.getJsonStringFromObject(taskDetailModel));
        intent.putExtra(Utility.Extra.SELECTED_ADDRESS_MODEL, Utility.getJsonStringFromObject(mSelectedAddressModel));
        intent.putExtra(Utility.Extra.TASK_TYPE_IS_INSTA, isInstaBooking);
        intent.putExtra(PAYMENT_VIEW_IS_ADDITIONAL_CHARGE, additional);
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

        mActivityPaymentDetailBinding.ivTermsTick.setSelected(true);
        mActivityPaymentDetailBinding.textPay.setSelected(true);
        mActivityPaymentDetailBinding.lnDesclaimer.setVisibility(View.VISIBLE);

        if (getIntent().hasExtra(Utility.Extra.DATA)) {
            providerModel = (ProviderModel) Utility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA), ProviderModel.class);
            //This is only when provider profile view for specific task (provider gives quote to specific task)
            taskDetailModel = (TaskDetailModel) Utility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA_2), TaskDetailModel.class);
        }

        if (getIntent().hasExtra(Utility.Extra.TASK_TYPE_IS_INSTA)) {
            isInstaBooking = getIntent().getBooleanExtra(Utility.Extra.TASK_TYPE_IS_INSTA, false);
            mSelectedAddressModelForInsta = (AddressModel) Utility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.SELECTED_ADDRESS_MODEL), AddressModel.class);
        }

        if (getIntent().hasExtra(Utility.Extra.PAYMENT_VIEW_IS_ADDITIONAL_CHARGE)) {
            if (taskDetailModel != null) {
                mActivityPaymentDetailBinding.textTitle.setText(getString(R.string.title_booking_confimation));
            }
            int isAdditional = getIntent().getIntExtra(Utility.Extra.PAYMENT_VIEW_IS_ADDITIONAL_CHARGE, 0);
            if (isAdditional == 0) {
                if (taskDetailModel != null) {
                    resetPromocodeValue();
                    callGetReferBalance();
                    double taskPaidAmount = getQuotePriceInInteger(providerModel.quotePrice);
                    double additionalCharges = 0;
                    if (!TextUtils.isEmpty(taskDetailModel.additionalQuoteAmount)) {
                        additionalCharges = getQuotePriceInInteger(taskDetailModel.additionalQuoteAmount);
                    }
                    mActivityPaymentDetailBinding.txtprofee.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf(taskPaidAmount))));
                    mActivityPaymentDetailBinding.txtadditionalcharge.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf(additionalCharges))));
                    mActivityPaymentDetailBinding.txtsubtotal.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf(taskPaidAmount))));

                }
            } else {
                if (taskDetailModel != null) {
                    setAdditionalPayment();
                }
            }
            mActivityPaymentDetailBinding.lnPromoCodeDisclaimer.setVisibility(View.GONE);
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

    @Override
    protected void setListeners() {
        mActivityPaymentDetailBinding.textPay.setOnClickListener(onPayClickListener);
        mActivityPaymentDetailBinding.llclaimreferral.setOnClickListener(OnClaimOfReferCodeClickListener);
        mActivityPaymentDetailBinding.llpromocode.setOnClickListener(onPromoCodeClickListener);
        // Add Declaimer
        mActivityPaymentDetailBinding.imgCheepCodeClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickOfClosePromoCode();
            }
        });

        mActivityPaymentDetailBinding.ivTermsTick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActivityPaymentDetailBinding.ivTermsTick.setSelected(!mActivityPaymentDetailBinding.ivTermsTick.isSelected());
                mActivityPaymentDetailBinding.textPay.setSelected(mActivityPaymentDetailBinding.ivTermsTick.isSelected());
                mActivityPaymentDetailBinding.textPay.setEnabled(mActivityPaymentDetailBinding.ivTermsTick.isSelected());
            }
        });

    }

    View.OnClickListener onPayClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            setTaskState(STEP_THREE_VERIFIED);
            int isAdditionalPayment = getIntent().getIntExtra(Utility.Extra.PAYMENT_VIEW_IS_ADDITIONAL_CHARGE, 0);

            taskDetailModel.usedWalletAmount = String.valueOf(usedWalletBalance);
            if (isInstaBooking) {
                PaymentChoiceActivity.newInstance(mContext, taskDetailModel, providerModel, isAdditionalPayment, isInstaBooking, mSelectedAddressModelForInsta);
            } else {
                updatePaymentStatus(true, Utility.EMPTY_STRING, isAdditionalPayment != 0);
            }

        }
    };
    View.OnClickListener onPromoCodeClickListener = new View.OnClickListener() {
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
//                    mActivityPaymentDetailBinding.textPay.setText(getString(R.string.label_book_now_for_rupees, Utility.getQuotePriceFormatter(providerModel.quotePrice)));
            }

        }
    };
    View.OnClickListener OnClaimOfReferCodeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mActivityPaymentDetailBinding.ivreferraldiscount.setSelected(!mActivityPaymentDetailBinding.ivreferraldiscount.isSelected());
            mActivityPaymentDetailBinding.txtreferraldiscount.setSelected(mActivityPaymentDetailBinding.ivreferraldiscount.isSelected());
            mActivityPaymentDetailBinding.llpromocode.setEnabled(true);
            onClickOfClosePromoCode();
            if (mActivityPaymentDetailBinding.ivreferraldiscount.isSelected()) {
                taskDetailModel.isReferCode = Utility.BOOLEAN.NO;
                Log.e(TAG, "  providerModel.spWithoutGstQuotePrice  :: " + providerModel.spWithoutGstQuotePrice);
                Log.e(TAG, "  maxReferDiscount  :: " + maxReferDiscount);
                Log.e(TAG, " referralBalance  :: " + referralBalance);

                try {
                    actualQuotePrice = providerModel.quotePrice;
                    double quoteAmount = Double.parseDouble(providerModel.spWithoutGstQuotePrice);
                    double maxDiscountAmt = Double.parseDouble(maxReferDiscount);
                    double referralBal = Double.parseDouble(referralBalance);
                    usedWalletBalance = 0;

                    if (referralBal > maxDiscountAmt) {
                        if (quoteAmount >= maxDiscountAmt) {
                            usedWalletBalance = maxDiscountAmt;
                        } else {
                            usedWalletBalance = quoteAmount;
                        }
                    } else {
                        if (quoteAmount >= referralBal) {
                            usedWalletBalance = referralBal;
                        } else {
                            usedWalletBalance = quoteAmount;
                        }
                    }

                    getAmountWithGstWS(String.valueOf(usedWalletBalance));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                usedWalletBalance = 0;

                if (!TextUtils.isEmpty(actualQuotePrice)) {
                    providerModel.quotePrice = actualQuotePrice;
                }
                actualQuotePrice = null;
                mActivityPaymentDetailBinding.txttotal.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(providerModel.quotePrice)));
                mActivityPaymentDetailBinding.txtreferraldiscount.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(referralBalance)));
                mActivityPaymentDetailBinding.lnPromoCodeDisclaimer.setVisibility(View.GONE);
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
        if (taskDetailModel != null) {
            taskDetailModel.cheepCode = Utility.EMPTY_STRING;
            taskDetailModel.taskDiscountAmount = Utility.ZERO_STRING;
        }
        actualQuotePrice = null;
        mActivityPaymentDetailBinding.imgCheepCodeClose.setVisibility(View.GONE);
        mActivityPaymentDetailBinding.llpromocode.setEnabled(true);
        resetPromocodeValue();
    }

    /**
     * Used for Reset Payment Values after applying promocode OR Removing promociod
     */
    public void resetPromocodeValue() {
        mActivityPaymentDetailBinding.textpromocodelabel.setTextColor(ContextCompat.getColor(this, R.color.splash_gradient_end));
        mActivityPaymentDetailBinding.textpromocodelabel.setText(getResources().getString(R.string.label_enter_promocode));
        mActivityPaymentDetailBinding.lnPromoCodeDisclaimer.setVisibility(View.GONE);
        taskDetailModel.isReferCode = Utility.BOOLEAN.NO;

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
//            mActivityPaymentDetailBinding.textPay.setText(getString(R.string.label_book_now_for_rupees, "" + Utility.getQuotePriceFormatter(String.valueOf(totalPayment))));
            mActivityPaymentDetailBinding.txtpromocode.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf(promocodeValue))));

        }
    }


    /**
     * Used for Additional payment
     */
    public void setAdditionalPayment() {
        mActivityPaymentDetailBinding.txtprofee.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(providerModel.quotePrice)));
        double additionalCharges = 0;
        if (!TextUtils.isEmpty(taskDetailModel.additionalQuoteAmount)) {
            additionalCharges = getQuotePriceInInteger(taskDetailModel.additionalQuoteAmount);
        }
        mActivityPaymentDetailBinding.txtadditionalcharge.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf(additionalCharges))));

        double subTotal = additionalCharges;
        mActivityPaymentDetailBinding.txtsubtotal.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf(subTotal))));
        mActivityPaymentDetailBinding.txttotal.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf(subTotal))));
//        mActivityPaymentDetailBinding.textPay.setText(getString(R.string.label_pay_fee_v1, "" + Utility.getQuotePriceFormatter(String.valueOf(subTotal))));
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
    }

    public SpannableStringBuilder getSpannableString(String fullstring, int color, boolean isBold) {
        SpannableStringBuilder text = new SpannableStringBuilder(fullstring);
        text.setSpan(new ForegroundColorSpan(color), 0, fullstring.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (isBold) {
            text.setSpan(new StyleSpan(Typeface.BOLD), 0, fullstring.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return text;
    }


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
                            // new field for refer and earn functionality
                            taskDetailModel.isReferCode = jsonObject.optString(NetworkUtility.TAGS.IS_REFER_CODE);

                            String payable;
                            payable = jsonObject.optString(NetworkUtility.TAGS.PAYABLE_AMOUNT);
                            updatePaymentBtn(total, discount, payable);
                            mActivityPaymentDetailBinding.lnPromoCodeDisclaimer.setVisibility(View.VISIBLE);
                            mActivityPaymentDetailBinding.txtPromoCodeDisclaimer.setText(R.string.disclaimer_promo_code);

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
        taskDetailModel.taskDiscountAmount = discount;
        taskDetailModel.cheepCode = cheepCode;
        usedWalletBalance = 0;
//        mActivityJobSummaryBinding.btnPay.setText(getString(R.string.label_pay_X_X_X, total, discount, payable));
//        @change only need to show payable amount
        mActivityPaymentDetailBinding.txtpromocode.setSelected(true);
        mActivityPaymentDetailBinding.txtpromocode.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(discount)));
        mActivityPaymentDetailBinding.txttotal.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(payable)));
//        mActivityPaymentDetailBinding.textPay.setText(getString(R.string.label_book_now_for_rupees, Utility.getQuotePriceFormatter(payable)));
        mActivityPaymentDetailBinding.textpromocodelabel.setText(getString(R.string.label_promocode_apply));
        mActivityPaymentDetailBinding.textpromocodelabel.setTextColor(ContextCompat.getColor(this, R.color.black));

        mActivityPaymentDetailBinding.imgCheepCodeClose.setVisibility(View.VISIBLE);
        mActivityPaymentDetailBinding.llpromocode.setEnabled(false);
        mActivityPaymentDetailBinding.ivpromocode.setVisibility(View.INVISIBLE);
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

    Response.Listener mCallGetAmoutWithGSTWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {
            hideProgressDialog();
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
                        taskDetailModel.isReferCode = jsonObject.optString(NetworkUtility.TAGS.IS_REFER_CODE);

                        String payable;
                        payable = jsonObject.optString(NetworkUtility.TAGS.PAYABLE_AMOUNT);
                        mActivityPaymentDetailBinding.lnPromoCodeDisclaimer.setVisibility(View.VISIBLE);
                        mActivityPaymentDetailBinding.txtPromoCodeDisclaimer.setText(R.string.disclaimer_referral);
                        // setting payable amount as quote price to pay.
                        providerModel.quotePrice = payable;
                        mActivityPaymentDetailBinding.txttotal.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(payable)));

//                        mActivityPaymentDetailBinding.textPay.setText(getString(R.string.label_book_now_for_rupees, Utility.getQuotePriceFormatter(payable)));
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
                mCallGetReferBalanceErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
        }
    };

    Response.ErrorListener mCallValidateCheepCodeWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(final VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            hideProgressDialog();

//            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityJobSummaryBinding.getRoot());
            Utility.hideKeyboard(PaymentDetailsActivity.this, edtCheepcode);
            Utility.showToast(mContext, getString(R.string.label_something_went_wrong));

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
//        if (event.BROADCAST_ACTION == Utility.BROADCAST_TYPE.TASK_PAID
//                || event.BROADCAST_ACTION == Utility.BROADCAST_TYPE.PAYMENT_COMPLETED_NEED_TO_REDIRECT_TO_MY_TASK_SCREEN) {
        switch (event.BROADCAST_ACTION) {
            case Utility.BROADCAST_TYPE.PAYMENT_COMPLETED_NEED_TO_REDIRECT_TO_MY_TASK_SCREEN:
                finish();
                break;
            case Utility.BROADCAST_TYPE.TASK_PAID_FOR_INSTA_BOOKING:
                finish();
                break;
            case Utility.BROADCAST_TYPE.TASK_PROCESSING:
                finish();
                break;
        }
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
                mCallValidateCheepCodeWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
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

    /**
     * According to new flow Pay Later this WS used for book pro only.
     * Payment will be done after
     */
    private void updatePaymentStatus(boolean isSuccess, String response,
                                     boolean isAdditionalPayment) {
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
        if (!TextUtils.isEmpty(taskDetailModel.cheepCode)) {
            mParams.put(NetworkUtility.TAGS.CHEEPCODE, taskDetailModel.cheepCode);
            mParams.put(NetworkUtility.TAGS.PROMOCODE_PRICE, taskDetailModel.taskDiscountAmount);

        } else {
            mParams.put(NetworkUtility.TAGS.CHEEPCODE, Utility.EMPTY_STRING);
            mParams.put(NetworkUtility.TAGS.PROMOCODE_PRICE, Utility.ZERO_STRING);
        }
        mParams.put(NetworkUtility.TAGS.IS_REFER_CODE, taskDetailModel.isReferCode);
        mParams.put(NetworkUtility.TAGS.TRANSACTION_ID, Utility.getUniqueTransactionId());
        mParams.put(NetworkUtility.TAGS.IS_FOR_ADDITIONAL_QUOTE, isAdditionalPayment
                ? getString(R.string.label_yes).toLowerCase() :
                getString(R.string.label_no).toLowerCase());
        mParams.put(NetworkUtility.TAGS.PAYMENT_METHOD, NetworkUtility.PAYMENT_METHOD_TYPE.PAY_LATER);
//        mParams.put(NetworkUtility.TAGS.PAYMENT_STATUS, isSuccess ? Utility.PAYMENT_STATUS.COMPLETED : Utility.PAYMENT_STATUS.FAILED);
//        as per new pay later flow payment_status will be processing
        mParams.put(NetworkUtility.TAGS.PAYMENT_STATUS, Utility.PAYMENT_STATUS.PROCESSING);
        mParams.put(NetworkUtility.TAGS.PAYMENT_LOG, response);
        mParams.put(NetworkUtility.TAGS.USED_WALLET_BALANCE, taskDetailModel.usedWalletAmount);
        mParams.put(NetworkUtility.TAGS.PAYABLE_AMOUNT, isAdditionalPayment ? taskDetailModel.additionalQuoteAmount : providerModel.quotePrice);
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
                LogUtils.LOGE(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;
                hideProgressDialog();
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:

                        JSONObject jsonData = jsonObject.optJSONObject(NetworkUtility.TAGS.DATA);
                        String taskStatus = jsonData.optString(NetworkUtility.TAGS.TASK_STATUS);

//                        callTaskDetailWS();

// AS PER new flow pay later task status will be pending
                        if (Utility.TASK_STATUS.COD.equalsIgnoreCase(taskStatus)
                                || Utility.TASK_STATUS.PAID.equalsIgnoreCase(taskStatus) || Utility.TASK_STATUS.PAY_LATER.equalsIgnoreCase(taskStatus)) {
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
                            messageEvent.BROADCAST_ACTION = Utility.BROADCAST_TYPE.TASK_PRO_BOOKED;
                            messageEvent.id = taskDetailModel.taskId;
                            EventBus.getDefault().post(messageEvent);

                             /*
                             *  @Changes : 7th July, 2017 :- Bhavesh Patadiya
                             *  Need to show Model Dialog once Payment has been made successful. Once
                             *  User clicks on OK. we will finish of the activity.
                             */
//                            String title = mContext.getString(R.string.label_great_choice_x, PreferenceUtility.getInstance(mContext).getUserDetails().UserName);
                            String title = mContext.getString(R.string.label_brilliant) + "!";
                            final SuperCalendar superStartDateTimeCalendar = SuperCalendar.getInstance();
                            superStartDateTimeCalendar.setTimeZone(SuperCalendar.SuperTimeZone.GMT.GMT);
                            superStartDateTimeCalendar.setTimeInMillis(Long.parseLong(taskDetailModel.taskStartdate));
                            superStartDateTimeCalendar.setLocaleTimeZone();

                            int onlydate = Integer.parseInt(superStartDateTimeCalendar.format("dd"));
                            String message = fetchMessageFromDateOfMonth(onlydate, superStartDateTimeCalendar);

//                            final UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
                            int badgeResId = -1;
                            if (providerModel.pro_level != null) {
                                switch (providerModel.pro_level) {
                                    case Utility.PRO_LEVEL.PLATINUM:
                                        badgeResId = R.drawable.ic_badge_platinum;
                                        break;
                                    case Utility.PRO_LEVEL.GOLD:
                                        badgeResId = R.drawable.ic_badge_gold;
                                        break;
                                    case Utility.PRO_LEVEL.SILVER:
                                        badgeResId = R.drawable.ic_badge_silver;
                                        break;
                                    case Utility.PRO_LEVEL.BRONZE:
                                        badgeResId = R.drawable.ic_badge_bronze;
                                        break;
                                }
                            }
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

    Response.ErrorListener mCallUpdatePaymentStatusWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(final VolleyError error) {
            LogUtils.LOGE(TAG, "onErrorResponse() called with: error = [" + error + "]");
            // Close Progressbar
            hideProgressDialog();
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
        final UserDetails userDetails = PreferenceUtility.getInstance(PaymentDetailsActivity.this).getUserDetails();
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
                    if (taskChatModel != null) {
                        taskChatModel.chatId = formattedId;
                    }
                    if (taskChatModel != null) {
                        FirebaseHelper.getRecentChatRef(finalFormattedUserId).child(taskChatModel.chatId).setValue(taskChatModel);
                    }

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

    private String fetchMessageFromDateOfMonth(int day, SuperCalendar
            superStartDateTimeCalendar) {
        String date;
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
//        String time = superStartDateTimeCalendar.format(Utility.DATE_FORMAT_HH_MM_AM);

        // set time format 24 hours
        Date d = superStartDateTimeCalendar.getCalendar().getTime();

        SimpleDateFormat timeFormatter = new SimpleDateFormat(Utility.TIME_FORMAT_24HH_MM);
        String fromHour = timeFormatter.format(d);
        SuperCalendar superCalendar = SuperCalendar.getInstance();
        superCalendar.setTimeInMillis(superStartDateTimeCalendar.getCalendar().getTimeInMillis());
        superCalendar.getCalendar().add(Calendar.HOUR_OF_DAY, 2);

        Date toDate = superCalendar.getCalendar().getTime();
        String toHour = timeFormatter.format(toDate);

        String message = mContext.getString(R.string.desc_task_payment_done_acknowledgement
                , providerModel.userName, date + getString(R.string.label_between) + fromHour + " hrs - " + toHour + " hrs");
        message = message.replace(".", "");
//        message = message.replace(getString(R.string.label_am_caps), getString(R.string.label_am_small)).replace(getString(R.string.label_pm_caps), getString(R.string.label_pm_small));
        return message + ".";
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


}
