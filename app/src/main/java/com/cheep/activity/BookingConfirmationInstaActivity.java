package com.cheep.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.R;
import com.cheep.adapter.SelectedSubServiceAdapter;
import com.cheep.custom_view.BottomAlertDialog;
import com.cheep.custom_view.CFEditTextRegular;
import com.cheep.databinding.ActivityBookingConfirmationInstaBinding;
import com.cheep.model.AddressModel;
import com.cheep.model.MessageEvent;
import com.cheep.model.TaskDetailModel;
import com.cheep.model.UserDetails;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.CalendarUtility;
import com.cheep.utils.GsonUtility;
import com.cheep.utils.LogUtils;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;
import com.cheep.utils.WebCallClass;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static com.cheep.utils.Utility.getSpannableString;


public class BookingConfirmationInstaActivity extends BaseAppCompatActivity {


    private ActivityBookingConfirmationInstaBinding mBinding;
    private TaskDetailModel taskDetailModel;
    private AddressModel mSelectedAddressModel;
    private double total;
    private double totalWithGST;
    private double subTotal;
    private static final String TAG = LogUtils.makeLogTag(BookingConfirmationInstaActivity.class);
    private Map<String, Object> mTaskCreationParams;


    private double usedWalletBalance = 0;

    private String referralBalance;
    private String maxReferDiscount;

    private CFEditTextRegular edtCheepcode;
    private BottomAlertDialog cheepCodeDialog;
    private String cheepCode;
    private String payableAmount;


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
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_booking_confirmation_insta);

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
        mBinding.textStepDesc.setText("Please check details below and book");
        // Enable Step Three Unverified state
        setTaskState(STEP_THREE_UNVERIFIED);

        double subTotal = 0;
        double subServiceTotal = 0;
        double subServiceTotalWithGST = 0;
        double additionalCharge = 0;
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
        mBinding.tvLabelCategory.setText(taskDetailModel.categoryModel.catName);
        if (mSelectedAddressModel.is_subscribe.equalsIgnoreCase(Utility.ADDRESS_SUBSCRIPTION_TYPE.PREMIUM) || mSelectedAddressModel.is_subscribe.equalsIgnoreCase(Utility.ADDRESS_SUBSCRIPTION_TYPE.NORMAL)) {
            if (taskDetailModel.additionalChargeReason.equalsIgnoreCase(Utility.DIALOG_TYPE.NONE)) {

                mBinding.lnPayLaterPayNowButtons.setVisibility(View.GONE);
                mBinding.tvGotcha.setVisibility(View.VISIBLE);

            } else {
                mBinding.lnPayLaterPayNowButtons.setVisibility(View.VISIBLE);
                mBinding.tvGotcha.setVisibility(View.GONE);
            }
            mBinding.tvLabelCategoryPrices.setText(getString(R.string.free));
            subServiceTotal = 0;
        } else {
            mBinding.lnPayLaterPayNowButtons.setVisibility(View.VISIBLE);
            mBinding.tvGotcha.setVisibility(View.GONE);
            mBinding.tvLabelCategoryPrices.setText(getString(R.string.rupee_symbol_x, Utility.getQuotePriceFormatter(taskDetailModel.catPrice)));
            subServiceTotal = Double.valueOf(taskDetailModel.catPrice);
        }
        if (taskDetailModel.additionalChargeReason.equalsIgnoreCase(Utility.DIALOG_TYPE.NONE)) {
            mBinding.rlAdditionalCharges.setVisibility(View.GONE);
            additionalCharge = 0;
            mBinding.viewLine2.setVisibility(View.GONE);
        } else {
            mBinding.viewLine2.setVisibility(View.VISIBLE);
            mBinding.rlAdditionalCharges.setVisibility(View.VISIBLE);
            // mBinding.tvLabelAdditionalCharge.setText(taskDetailModel.additionalChargeReason);
            mBinding.tvAdditionalChargeReason.setText(taskDetailModel.additionalChargeReason);
            if (taskDetailModel.additionalChargeReason.equalsIgnoreCase(Utility.DIALOG_TYPE.OUT_OF_OFFICE_HOURS)) {
                mBinding.tvAdditionalChargeSubreason.setText(getString(R.string.out_of_off_info));
            } else {
                mBinding.tvAdditionalChargeSubreason.setText(getString(R.string.urgent_booking_info));
            }

            mBinding.tvAdditionalCharge.setText(getString(R.string.rupee_symbol_x, Utility.getQuotePriceFormatter(PreferenceUtility.getInstance(mContext).getAdminSettings().additionalChargeForSelectingSpecificTime)));
            additionalCharge = Double.valueOf(PreferenceUtility.getInstance(mContext).getAdminSettings().additionalChargeForSelectingSpecificTime);
        }
        // banner image of cat
        //GlideUtility.loadImageView(mContext, mBinding.imgService, taskDetailModel.categoryModel.catImageExtras.original, R.drawable.gradient_black);

        if (!taskDetailModel.subCatList.isEmpty()) {
            mBinding.recyclerViewPaid.setVisibility(View.VISIBLE);
            mBinding.recyclerViewPaid.setLayoutManager(new LinearLayoutManager(this));
            mBinding.recyclerViewPaid.setAdapter(new SelectedSubServiceAdapter(taskDetailModel.subCatList));
            mBinding.viewLine1.setVisibility(View.VISIBLE);
            mBinding.categoryTick.setVisibility(View.GONE);
        } else {
            mBinding.categoryTick.setVisibility(View.VISIBLE);
            mBinding.recyclerViewPaid.setVisibility(View.GONE);
            mBinding.viewLine1.setVisibility(View.GONE);
        }

        //
        mBinding.ivTermsTick.setSelected(true);

        setPayButtonSelection();

//        for (SubServiceDetailModel subServiceDetailModel : taskDetailModel.subCatList) {
//            subServiceTotal += subServiceDetailModel.selected_unit * Double.parseDouble(subServiceDetailModel.unitPrice);
//            LogUtils.LOGE(TAG, "initiateUI:unitPrice " + subServiceDetailModel.unitPrice);
//            subServiceTotalWithGST += subServiceDetailModel.selected_unit * Double.parseDouble(subServiceDetailModel.unitPriceWithGST);
//            LogUtils.LOGE(TAG, "initiateUI:unitPriceWithGST " + subServiceDetailModel.unitPriceWithGST);
//        }
        subTotal = subServiceTotal + additionalCharge;
        total = new BigDecimal(subServiceTotal).doubleValue();
        LogUtils.LOGE(TAG, "initiateUI:total " + total);
        subTotal = new BigDecimal(subTotal).doubleValue();
        LogUtils.LOGE(TAG, "initiateUI:subTotal " + subTotal);
        totalWithGST = new BigDecimal(subTotal).doubleValue();
        payableAmount = String.valueOf(totalWithGST);
        LogUtils.LOGE(TAG, "initiateUI:totalWithGST " + totalWithGST);

        mBinding.tvSubTotal.setText(getString(R.string.rupee_symbol_x, Utility.getQuotePriceFormatter(String.valueOf(subTotal))));

        mBinding.tvTotal.setText(getString(R.string.rupee_symbol_x, Utility.getQuotePriceFormatter(String.valueOf(subTotal))));

        // mBinding.lnPayNow.setVisibility(View.GONE);
        //mBinding.lnPayLaterPayNowButtons.setVisibility(View.VISIBLE);

        //initUIForReferDiscountAndPromoCode();

    }

    private void initUIForReferDiscountAndPromoCode() {
        resetPromoCodeValue();
        callGetReferBalance();
        mBinding.lnPromoCodeDisclaimer.setVisibility(View.GONE);

        mBinding.ivreferraldiscount.setVisibility(View.VISIBLE);
        mBinding.textreferraldiscountlabel.setVisibility(View.VISIBLE);
        mBinding.textreferraldiscountApplied.setVisibility(View.GONE);

        mBinding.ivpromocode.setVisibility(View.VISIBLE);
        mBinding.textpromocodelabel.setVisibility(View.VISIBLE);
        mBinding.textpromocodeApplied.setVisibility(View.GONE);
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
        mBinding.llclaimreferral.setOnClickListener(OnClaimOfReferCodeClickListener);
        mBinding.llpromocode.setOnClickListener(onPromoCodeClickListener);
        // Add Declaimer
        mBinding.imgCheepCodeClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickOfClosePromoCode();
            }
        });
    }

    View.OnClickListener onPromoCodeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mBinding.ivpromocode.setVisibility(View.VISIBLE);
            mBinding.ivpromocode.setSelected(false);
            mBinding.txtpromocode.setSelected(false);
            mBinding.imgCheepCodeClose.setVisibility(View.GONE);
            mBinding.llpromocode.setEnabled(true);
            resetPromoCodeValue();

            mBinding.ivpromocode.setSelected(!mBinding.ivpromocode.isSelected());
            mBinding.txtpromocode.setSelected(mBinding.ivpromocode.isSelected());
            mBinding.lnPromoCodeDisclaimer.setVisibility(View.GONE);

            if (mBinding.ivreferraldiscount.isSelected()) {
                mBinding.txtreferraldiscount.setSelected(false);
                mBinding.ivreferraldiscount.setSelected(false);
            }

            if (mBinding.ivpromocode.isSelected()) {

                showCheepCodeDialog();
                usedWalletBalance = 0;
                mBinding.txtreferraldiscount.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(referralBalance)));
                mBinding.tvTotal.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf(totalWithGST))));
//                    mBinding.textPay.setText(getString(R.string.label_book_now_for_rupees, Utility.getQuotePriceFormatter(providerModel.quotePrice)));
            }

        }
    };

    private void showCheepCodeDialog() {
        View view = View.inflate(mContext, R.layout.dialog_add_promocode, null);
        edtCheepcode = view.findViewById(R.id.edit_cheepcode);
        cheepCodeDialog = new BottomAlertDialog(mContext);
        mBinding.ivpromocode.setSelected(false);
        mBinding.txtpromocode.setSelected(false);
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

    /**
     * Used for payment
     */
    private void validateCheepCode(String cheepCode) {
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

        mParams.put(NetworkUtility.TAGS.QUOTE_AMOUNT, total);
        mParams.put(NetworkUtility.TAGS.CHEEPCODE, cheepCode);
        mParams.put(NetworkUtility.TAGS.CAT_ID, taskDetailModel.categoryModel.catId);
        mParams.put(NetworkUtility.TAGS.IS_INSTA_BOOKING, Utility.BOOLEAN.YES);
        int addressId;
        try {
            addressId = Integer.parseInt(mSelectedAddressModel.address_id);
        } catch (Exception e) {
            addressId = 0;
        }
        if (addressId <= 0) {
            NetworkUtility.addGuestAddressParams(mTaskCreationParams, mSelectedAddressModel);

        } else {
            mParams.put(NetworkUtility.TAGS.ADDRESS_ID, addressId);
        }
        //Url is based on condition if address id is greater then 0 then it means we need to update the existing address
        String url = NetworkUtility.WS.CHECK_CHEEPCODE_FOR_STRATEGIC_PARTNER;
        VolleyNetworkRequest mVolleyNetworkRequestForSPList = new VolleyNetworkRequest(url
                , mCallValidateCheepCodeWSErrorListener
                , mCallValidateCheepCodeWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForSPList);
    }


    private Response.ErrorListener mCallValidateCheepCodeWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(final VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            hideProgressDialog();

//            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityJobSummaryBinding.getRoot());
            Utility.hideKeyboard(BookingConfirmationInstaActivity.this, edtCheepcode);
            Utility.showToast(mContext, getString(R.string.label_something_went_wrong));

        }

    };

    private String discount;
    Response.Listener mCallValidateCheepCodeWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {

            String strResponse = (String) response;
            try {
                Utility.hideKeyboard(BookingConfirmationInstaActivity.this, edtCheepcode);
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

                            total = Double.parseDouble(jsonObject.optString(NetworkUtility.TAGS.QUOTE_AMOUNT));
                            discount = jsonObject.optString(NetworkUtility.TAGS.DISCOUNT_AMOUNT);
                            // new field for refer and earn functionality
                            taskDetailModel.isReferCode = jsonObject.optString(NetworkUtility.TAGS.IS_REFER_CODE);

                            payableAmount = jsonObject.optString(NetworkUtility.TAGS.PAYABLE_AMOUNT);
                            updatePaymentBtn();
                            mBinding.lnPromoCodeDisclaimer.setVisibility(View.VISIBLE);
                            mBinding.txtPromoCodeDisclaimer.setText(R.string.disclaimer_promo_code);

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
    private void updatePaymentBtn() {
        // setting payable amount as quote price to pay.
        taskDetailModel.taskDiscountAmount = discount;
        taskDetailModel.cheepCode = cheepCode;
        usedWalletBalance = 0;
//        mActivityJobSummaryBinding.btnPay.setText(getString(R.string.label_pay_X_X_X, total, discount, payable));
//        @change only need to show payable amount
        mBinding.txtpromocode.setSelected(true);
        mBinding.txtpromocode.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(discount)));
        mBinding.tvTotal.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(payableAmount)));
//        mBinding.textPay.setText(getString(R.string.label_book_now_for_rupees, Utility.getQuotePriceFormatter(payable)));
        mBinding.textpromocodelabel.setText(getString(R.string.label_promocode_apply));
        mBinding.textpromocodelabel.setTextColor(ContextCompat.getColor(this, R.color.black));

        mBinding.imgCheepCodeClose.setVisibility(View.VISIBLE);
        mBinding.llpromocode.setEnabled(false);
        mBinding.ivpromocode.setVisibility(View.INVISIBLE);
    }

    View.OnClickListener OnClaimOfReferCodeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mBinding.ivreferraldiscount.setSelected(!mBinding.ivreferraldiscount.isSelected());
            mBinding.txtreferraldiscount.setSelected(mBinding.ivreferraldiscount.isSelected());
            mBinding.llpromocode.setEnabled(true);
            onClickOfClosePromoCode();
            if (mBinding.ivreferraldiscount.isSelected()) {
                taskDetailModel.isReferCode = Utility.BOOLEAN.NO;

                try {
                    double quoteAmount = total;
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

                    // todo : remove commented (logic pending)
                    getAmountWithGstWS(String.valueOf(usedWalletBalance));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                usedWalletBalance = 0;
                mBinding.tvTotal.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf(totalWithGST))));
                mBinding.txtreferraldiscount.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(referralBalance)));
                mBinding.lnPromoCodeDisclaimer.setVisibility(View.GONE);
            }
        }
    };

    private void getAmountWithGstWS(String discountAmout) {
        showProgressDialog();
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().userID);
        Map<String, String> mParams = new HashMap<>();

        mParams.put(NetworkUtility.TAGS.QUOTE_AMOUNT, String.valueOf(payableAmount));
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
                Utility.hideKeyboard(BookingConfirmationInstaActivity.this, edtCheepcode);
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
                            mBinding.txtreferraldiscount.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf(balance - discountAmount))));
                        }
                        // new field for refer and earn functionality
                        taskDetailModel.isReferCode = jsonObject.optString(NetworkUtility.TAGS.IS_REFER_CODE);

                        String payable;
                        payable = jsonObject.optString(NetworkUtility.TAGS.PAYABLE_AMOUNT);
                        mBinding.lnPromoCodeDisclaimer.setVisibility(View.VISIBLE);
                        mBinding.txtPromoCodeDisclaimer.setText(R.string.disclaimer_referral);
                        // setting payable amount as quote price to pay.
                        payableAmount = payable;
                        mBinding.tvTotal.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(payable)));

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

    private void onClickOfClosePromoCode() {
        mBinding.ivpromocode.setVisibility(View.VISIBLE);
        mBinding.ivpromocode.setSelected(false);
        mBinding.txtpromocode.setSelected(false);
        cheepCode = null;
        if (taskDetailModel != null) {
            taskDetailModel.cheepCode = Utility.EMPTY_STRING;
            taskDetailModel.taskDiscountAmount = Utility.ZERO_STRING;
        }
        mBinding.imgCheepCodeClose.setVisibility(View.GONE);
        mBinding.llpromocode.setEnabled(true);
        resetPromoCodeValue();
    }

    /**
     * Used for Reset Payment Values after applying promocode OR Removing promociod
     */
    public void resetPromoCodeValue() {
        mBinding.textpromocodelabel.setTextColor(ContextCompat.getColor(this, R.color.splash_gradient_end));
        mBinding.textpromocodelabel.setText(getResources().getString(R.string.label_enter_promocode));
        mBinding.lnPromoCodeDisclaimer.setVisibility(View.GONE);
        taskDetailModel.isReferCode = Utility.BOOLEAN.NO;
        mBinding.tvTotal.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf(totalWithGST))));
        mBinding.txtpromocode.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf(0))));
    }

    private void setPayButtonSelection() {
        mBinding.rlPayLater.setSelected(mBinding.ivTermsTick.isSelected());
        mBinding.rlPayNow.setSelected(mBinding.ivTermsTick.isSelected());
        mBinding.rlPayLater.setEnabled(mBinding.ivTermsTick.isSelected());
        mBinding.rlPayNow.setEnabled(mBinding.ivTermsTick.isSelected());
        mBinding.tvPayNow.setSelected(mBinding.ivTermsTick.isSelected());
        mBinding.tvPayNow.setEnabled(mBinding.ivTermsTick.isSelected());
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

        WebCallClass.createInstaBookingTask(BookingConfirmationInstaActivity.this,
                taskDetailModel, mSelectedAddressModel, String.valueOf(total), payableAmount, NetworkUtility.PAYMENT_METHOD_TYPE.PAY_LATER, Utility.EMPTY_STRING, Utility.EMPTY_STRING, new WebCallClass.CommonResponseListener() {
                    @Override
                    public void volleyError(VolleyError error) {

                    }

                    @Override
                    public void showSpecificMessage(String message) {

                    }

                    @Override
                    public void forceLogout() {

                    }
                }, new WebCallClass.InstaBookTaskCreationListener() {
                    @Override
                    public void successOfInstaBookTaskCreation() {

                    }
                });
    }


    private void openPaymentChoiceActivity() {
        PaymentChoiceActivity.newInstance(BookingConfirmationInstaActivity.this, taskDetailModel, null, String.valueOf(total), payableAmount, mSelectedAddressModel);
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////// Payment Detail Detail Service[Start] ////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////


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
        if (event.BROADCAST_ACTION == Utility.BROADCAST_TYPE.TASK_PAID_FOR_INSTA_BOOKING) {
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

    private void callGetReferBalance() {
        showProgressDialog();
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().userID);
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
                        mBinding.txtreferraldiscount.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(referralBalance)));
                        Log.i(TAG, "onResponse: " + jsonObject.toString());
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
}
