package com.cheep.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.appsflyer.AppsFlyerLib;
import com.cheep.BuildConfig;
import com.cheep.R;
import com.cheep.custom_view.BottomAlertDialog;
import com.cheep.custom_view.CFEditTextRegular;
import com.cheep.databinding.ActivityPaymentDetailBinding;
import com.cheep.dialogs.AcknowledgementDialogWithProfilePic;
import com.cheep.dialogs.AcknowledgementInteractionListener;
import com.cheep.model.AddressModel;
import com.cheep.model.MessageEvent;
import com.cheep.model.PaymentSummaryModel;
import com.cheep.model.ProviderModel;
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
import com.cheep.utils.SuperCalendar;
import com.cheep.utils.Utility;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class PaymentDetailsActivity extends BaseAppCompatActivity {


    private static final String TAG = "PaymentDetailsActivity";
    private ActivityPaymentDetailBinding mActivityPaymentDetailBinding;
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

    /**
     * used while user is booking task and selects pay now/later buttons
     */
    private boolean isPayNow = false;

    /**
     * used while user taps on View Payment summary and tries to do payment in on going task
     */
    private boolean payPendingAmount = false;

    /**
     * payment summary for task (Booking confirmation screen for booking get quotes)
     *
     * @param context
     * @param taskDetailModel
     * @param providerModel
     */
    public static void newInstance(Context context, TaskDetailModel taskDetailModel, ProviderModel providerModel, AddressModel mSelectedAddressModel) {
        Intent intent = new Intent(context, PaymentDetailsActivity.class);
        intent.putExtra(Utility.Extra.DATA, GsonUtility.getJsonStringFromObject(providerModel));
        intent.putExtra(Utility.Extra.DATA_2, GsonUtility.getJsonStringFromObject(taskDetailModel));
        intent.putExtra(Utility.Extra.SELECTED_ADDRESS_MODEL, GsonUtility.getJsonStringFromObject(mSelectedAddressModel));
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
        Intent intent = new Intent(mContext, PaymentDetailsActivity.class);
        intent.putExtra(Utility.Extra.DATA, GsonUtility.getJsonStringFromObject(taskDetailModel.selectedProvider));
        intent.putExtra(Utility.Extra.DATA_2, GsonUtility.getJsonStringFromObject(taskDetailModel));
        intent.putExtra(Utility.Extra.DATA_3, true);
        mContext.startActivity(intent);

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
        if (getIntent().hasExtra(Utility.Extra.DATA_3)) {
            if (getIntent().getBooleanExtra(Utility.Extra.DATA_3, false))
//                setUpDetailsForPayLater();
                callPaymentSummaryWS();
            else
                setUpDetailsForBooking();
        } else {
            setUpDetailsForBooking();
        }


    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////// Payment Detail Detail Service[Start] ////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Call Payment Detail web service
     */
    private void callPaymentSummaryWS() {

        if (getIntent().hasExtra(Utility.Extra.DATA_2)) {
            //This is only when provider profile view for specific task (provider gives quote to specific task)
            taskDetailModel = (TaskDetailModel) GsonUtility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA_2), TaskDetailModel.class);
            providerModel = (ProviderModel) GsonUtility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA), ProviderModel.class);
        }

        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityPaymentDetailBinding.getRoot());
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
                        paymentSummaryModel = (PaymentSummaryModel) GsonUtility.getObjectFromJsonString(jsonObject.optString(NetworkUtility.TAGS.DATA), PaymentSummaryModel.class);
                        setUpDetailsForPayLater();
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
            mActivityPaymentDetailBinding.lnPayLaterPayNowButtons.setVisibility(View.GONE);
            mActivityPaymentDetailBinding.lnPayNow.setEnabled(false);
            mActivityPaymentDetailBinding.rlPayNow.setEnabled(false);
            mActivityPaymentDetailBinding.rlPayLater.setEnabled(false);
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityPaymentDetailBinding.getRoot());

        }
    };

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////// Payment Detail WS[END] ///////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////

    private void setUpDetailsForPayLater() {
        // Changes are per new flow pay now/later: 4/12/17
        mActivityPaymentDetailBinding.lnPayLaterPayNowButtons.setVisibility(View.GONE);

        mActivityPaymentDetailBinding.rlreferraldiscount.setEnabled(false);
        mActivityPaymentDetailBinding.rlpromocode.setEnabled(false);

        mActivityPaymentDetailBinding.llpromocode.setEnabled(false);
        mActivityPaymentDetailBinding.llclaimreferral.setEnabled(false);

        mActivityPaymentDetailBinding.lnPayNow.setVisibility(View.VISIBLE);
        mActivityPaymentDetailBinding.textPayNow.setSelected(true);

        mActivityPaymentDetailBinding.lnDesclaimer.setVisibility(View.GONE);

        if (taskDetailModel != null && providerModel != null) {

            mActivityPaymentDetailBinding.textTitle.setText(getString(R.string.title_booking_confimation));
            mActivityPaymentDetailBinding.textCategory.setVisibility(View.VISIBLE);
            mActivityPaymentDetailBinding.textCategory.setText(taskDetailModel.categoryModel.catName);

            // top header image
            GlideUtility.loadImageView(mContext, mActivityPaymentDetailBinding.imgService, taskDetailModel.categoryModel.catImageExtras.original, R.drawable.gradient_black);
            GlideUtility.showCircularImageViewWithColorBorder(mContext, TAG, mActivityPaymentDetailBinding.imgProfile, providerModel.profileUrl, Utility.DEFAULT_CHEEP_LOGO, R.color.dark_blue_variant_1, true);
            String datetime = "";

            // pro name
            mActivityPaymentDetailBinding.textName.setText(providerModel.userName);
            // set date & time
            if (!TextUtils.isEmpty(taskDetailModel.taskStartdate)) {
                datetime = CalendarUtility.getDate(Long.parseLong(taskDetailModel.taskStartdate), Utility.DATE_FORMAT_DD_MMMM) + ", " + CalendarUtility.get2HourTimeSlots(taskDetailModel.taskStartdate);
//                dateTime = dateTime.replace(getString(R.string.label_am_caps), getString(R.string.label_am_small)).replace(getString(R.string.label_pm_caps), getString(R.string.label_pm_small));

            }

            // pro experience
            if (!TextUtils.isEmpty(providerModel.experience))
                if (Utility.ZERO_STRING.equals(providerModel.experience)) {
                    mActivityPaymentDetailBinding.textExperience.setText(Utility.checkNonNullAndSet(mContext.getString(R.string.label_experience_zero_one_line)));
                } else {
//                    mActivityPaymentDetailBinding.textExperience.setText(this.getResources().getQuantityString(R.plurals.getExperienceStringOneLine, Integer.parseInt(providerModel.experience), providerModel.experience));
                    mActivityPaymentDetailBinding.textExperience.setText(Utility.getExperienceString(providerModel.experience, Utility.EMPTY_STRING));
                }


            // set pro rating
            Utility.showRating(providerModel.rating, mActivityPaymentDetailBinding.providerRating);

            //badge
            int badgeResID = Utility.getProLevelBadge(providerModel.pro_level);
            GlideUtility.showCircularImageViewWithColorBorder(mContext, TAG, mActivityPaymentDetailBinding.ivBadge, badgeResID, R.color.splash_gradient_end, true);

            // pro verified text
            mActivityPaymentDetailBinding.textVerified.setVisibility(providerModel.isVerified.equalsIgnoreCase(Utility.BOOLEAN.YES) ? View.VISIBLE : View.GONE);

            // set task description date time and place
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
            spannableStringBuilder.append(getSpannableString(taskDetailModel.subCategoryName, ContextCompat.getColor(this, R.color.splash_gradient_end), true));
            spannableStringBuilder.append(getSpannableString(getString(R.string.label_on), ContextCompat.getColor(this, R.color.grey_varient_8), false));
            spannableStringBuilder.append(getSpannableString(datetime, ContextCompat.getColor(this, R.color.splash_gradient_end), true));
            spannableStringBuilder.append(getSpannableString(getString(R.string.label_at), ContextCompat.getColor(this, R.color.grey_varient_8), false));
            spannableStringBuilder.append(getSpannableString(taskDetailModel.taskAddress, ContextCompat.getColor(this, R.color.splash_gradient_end), true));


            mActivityPaymentDetailBinding.txtdesc.setText(spannableStringBuilder);

            mActivityPaymentDetailBinding.textProChargesLabel.setSelected(paymentSummaryModel.proPaymentStatus.equalsIgnoreCase(Utility.TASK_STATUS.PENDING));
            mActivityPaymentDetailBinding.txtprofee.setSelected(paymentSummaryModel.proPaymentStatus.equalsIgnoreCase(Utility.TASK_STATUS.PENDING));

            mActivityPaymentDetailBinding.txtprofee.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf(paymentSummaryModel.proPaymentAmount))));
            mActivityPaymentDetailBinding.txtadditionalcharge.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(paymentSummaryModel.additionalPendingAmount)));
            mActivityPaymentDetailBinding.txtsubtotal.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf(paymentSummaryModel.subTotalAmount))));

            mActivityPaymentDetailBinding.ivreferraldiscount.setVisibility(View.INVISIBLE);
            mActivityPaymentDetailBinding.textreferraldiscountlabel.setVisibility(View.GONE);
            mActivityPaymentDetailBinding.textreferraldiscountApplied.setVisibility(View.VISIBLE);

            mActivityPaymentDetailBinding.ivpromocode.setVisibility(View.INVISIBLE);
            mActivityPaymentDetailBinding.textpromocodelabel.setVisibility(View.GONE);
            mActivityPaymentDetailBinding.textpromocodeApplied.setVisibility(View.VISIBLE);

            mActivityPaymentDetailBinding.txtreferraldiscount.setText(getRuppeAmount(paymentSummaryModel.walletBalanceUsed));
            mActivityPaymentDetailBinding.txtpromocode.setText(getRuppeAmount(paymentSummaryModel.promoCodePrice));
            mActivityPaymentDetailBinding.txttotal.setText(getRuppeAmount(paymentSummaryModel.totalAmount));

            double walletBalanceUsed = 0;
            double promocodePrice = 0;
            try {
                walletBalanceUsed = Double.parseDouble(paymentSummaryModel.walletBalanceUsed);
                promocodePrice = Double.parseDouble(paymentSummaryModel.promoCodePrice);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            if (walletBalanceUsed > 0) {
                mActivityPaymentDetailBinding.lnPromoCodeDisclaimer.setVisibility(View.VISIBLE);
                mActivityPaymentDetailBinding.txtPromoCodeDisclaimer.setText(R.string.disclaimer_referral);
            } else if (promocodePrice > 0) {
                mActivityPaymentDetailBinding.lnPromoCodeDisclaimer.setVisibility(View.VISIBLE);
                mActivityPaymentDetailBinding.txtPromoCodeDisclaimer.setText(R.string.disclaimer_promo_code);
            } else {
                mActivityPaymentDetailBinding.lnPromoCodeDisclaimer.setVisibility(View.GONE);
            }

            mActivityPaymentDetailBinding.textPayNow.setOnClickListener(new View.OnClickListener() {
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
        // Changes are per new flow pay now/later: 15/11/17

        mActivityPaymentDetailBinding.rlPayNow.setSelected(true);
        mActivityPaymentDetailBinding.rlPayLater.setSelected(true);

        mActivityPaymentDetailBinding.lnPayLaterPayNowButtons.setVisibility(View.VISIBLE);

        mActivityPaymentDetailBinding.lnPayNow.setVisibility(View.GONE);

        mActivityPaymentDetailBinding.lnDesclaimer.setVisibility(View.GONE);
        mActivityPaymentDetailBinding.lnPromoCodeDisclaimer.setVisibility(View.GONE);

        String text = getString(R.string.description_pay_now);
        StringBuilder description = new StringBuilder(text);
        // appending two space for two smiley at the end of description
        description.append("  ");
        Spannable span = new SpannableString(description);
        Drawable img = ContextCompat.getDrawable(this, R.drawable.emoji_blue_heart);
        img.setBounds(0, 0, img.getIntrinsicWidth(), img.getIntrinsicHeight());
        ImageSpan image = new ImageSpan(img, ImageSpan.ALIGN_BOTTOM);
        span.setSpan(image, span.length() - 1, span.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        mActivityPaymentDetailBinding.textDescPayNow.setText(span);

//        mActivityPaymentDetailBinding.textDescPayNow.setText(getString(R.string.description_pay_now) + " " + new String(Character.toChars(0x1F499)));

        if (getIntent().hasExtra(Utility.Extra.DATA_2)) {
            //This is only when provider profile view for specific task (provider gives quote to specific task)
            taskDetailModel = (TaskDetailModel) GsonUtility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA_2), TaskDetailModel.class);
            providerModel = (ProviderModel) GsonUtility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA), ProviderModel.class);
            isInstaBooking = taskDetailModel.taskType.equalsIgnoreCase(Utility.TASK_TYPE.INSTA_BOOK);
        }

        if (getIntent().hasExtra(Utility.Extra.SELECTED_ADDRESS_MODEL)) {
            mSelectedAddressModelForInsta = (AddressModel) GsonUtility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.SELECTED_ADDRESS_MODEL), AddressModel.class);
        }

        if (taskDetailModel != null && providerModel != null) {
            mActivityPaymentDetailBinding.textTitle.setText(getString(R.string.title_booking_confimation));
            resetPromoCodeValue();
            callGetReferBalance();

            mActivityPaymentDetailBinding.textProChargesLabel.setSelected(true);
            mActivityPaymentDetailBinding.txtprofee.setSelected(true);
            mActivityPaymentDetailBinding.txtprofee.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf(providerModel.quotePrice))));
            mActivityPaymentDetailBinding.txtadditionalcharge.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf(0))));
            mActivityPaymentDetailBinding.txtsubtotal.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf(providerModel.quotePrice))));
            mActivityPaymentDetailBinding.textCategory.setVisibility(View.VISIBLE);
            mActivityPaymentDetailBinding.textCategory.setText(taskDetailModel.categoryModel.catName);

            // top header image
            GlideUtility.loadImageView(mContext, mActivityPaymentDetailBinding.imgService, taskDetailModel.categoryModel.catImageExtras.original, R.drawable.gradient_black);
            GlideUtility.showCircularImageViewWithColorBorder(mContext, TAG, mActivityPaymentDetailBinding.imgProfile, providerModel.profileUrl, Utility.DEFAULT_CHEEP_LOGO, R.color.dark_blue_variant_1, true);
            String datetime = "";

            // pro name
            mActivityPaymentDetailBinding.textName.setText(providerModel.userName);
            // set date & time
            if (!TextUtils.isEmpty(taskDetailModel.taskStartdate)) {
                datetime = CalendarUtility.getDate(Long.parseLong(taskDetailModel.taskStartdate), Utility.DATE_FORMAT_DD_MMMM) + ", " + CalendarUtility.get2HourTimeSlots(taskDetailModel.taskStartdate);
//                dateTime = dateTime.replace(getString(R.string.label_am_caps), getString(R.string.label_am_small)).replace(getString(R.string.label_pm_caps), getString(R.string.label_pm_small));

            }

            // pro experience
            if (!TextUtils.isEmpty(providerModel.experience))
                if (Utility.ZERO_STRING.equals(providerModel.experience)) {
                    mActivityPaymentDetailBinding.textExperience.setText(Utility.checkNonNullAndSet(mContext.getString(R.string.label_experience_zero_one_line)));
                } else {
//                    mActivityPaymentDetailBinding.textExperience.setText(this.getResources().getQuantityString(R.plurals.getExperienceStringOneLine, Integer.parseInt(providerModel.experience), providerModel.experience));
                    mActivityPaymentDetailBinding.textExperience.setText(Utility.getExperienceString(providerModel.experience, Utility.EMPTY_STRING));

                }


            // set pro rating
            Utility.showRating(providerModel.rating, mActivityPaymentDetailBinding.providerRating);

            mActivityPaymentDetailBinding.ivreferraldiscount.setVisibility(View.VISIBLE);
            mActivityPaymentDetailBinding.textreferraldiscountlabel.setVisibility(View.VISIBLE);
            mActivityPaymentDetailBinding.textreferraldiscountApplied.setVisibility(View.GONE);

            mActivityPaymentDetailBinding.ivpromocode.setVisibility(View.VISIBLE);
            mActivityPaymentDetailBinding.textpromocodelabel.setVisibility(View.VISIBLE);
            mActivityPaymentDetailBinding.textpromocodeApplied.setVisibility(View.GONE);


            //badge
            int badgeResID = Utility.getProLevelBadge(providerModel.pro_level);
            GlideUtility.showCircularImageViewWithColorBorder(mContext, TAG, mActivityPaymentDetailBinding.ivBadge, badgeResID, R.color.splash_gradient_end, true);

            // pro verified text
            mActivityPaymentDetailBinding.textVerified.setVisibility(providerModel.isVerified.equalsIgnoreCase(Utility.BOOLEAN.YES) ? View.VISIBLE : View.GONE);

            // set task description date time and place
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
            spannableStringBuilder.append(getSpannableString(taskDetailModel.subCategoryName, ContextCompat.getColor(this, R.color.splash_gradient_end), true));
            spannableStringBuilder.append(getSpannableString(getString(R.string.label_on), ContextCompat.getColor(this, R.color.grey_varient_8), false));
            spannableStringBuilder.append(getSpannableString(datetime, ContextCompat.getColor(this, R.color.splash_gradient_end), true));
            spannableStringBuilder.append(getSpannableString(getString(R.string.label_at), ContextCompat.getColor(this, R.color.grey_varient_8), false));
            spannableStringBuilder.append(getSpannableString(taskDetailModel.taskAddress, ContextCompat.getColor(this, R.color.splash_gradient_end), true));
            mActivityPaymentDetailBinding.txtdesc.setText(spannableStringBuilder);
        }
    }


    @Override
    protected void setListeners() {
        mActivityPaymentDetailBinding.rlPayNow.setOnClickListener(onPayClickListener);
        mActivityPaymentDetailBinding.rlPayLater.setOnClickListener(onBookOnlyClickListener);
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

                // Changes are per new flow pay now/later: 15/11/17
                mActivityPaymentDetailBinding.rlPayLater.setSelected(mActivityPaymentDetailBinding.ivTermsTick.isSelected());
                mActivityPaymentDetailBinding.rlPayNow.setSelected(mActivityPaymentDetailBinding.ivTermsTick.isSelected());
                mActivityPaymentDetailBinding.rlPayLater.setEnabled(mActivityPaymentDetailBinding.ivTermsTick.isSelected());
                mActivityPaymentDetailBinding.rlPayNow.setEnabled(mActivityPaymentDetailBinding.ivTermsTick.isSelected());
                mActivityPaymentDetailBinding.textPayNow.setSelected(mActivityPaymentDetailBinding.ivTermsTick.isSelected());
                mActivityPaymentDetailBinding.textPayNow.setEnabled(mActivityPaymentDetailBinding.ivTermsTick.isSelected());
            }
        });

    }

    // Changes are per new flow pay now/later: 15/11/17
    View.OnClickListener onPayClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            isPayNow = true;
            setTaskState(STEP_THREE_VERIFIED);
            taskDetailModel.usedWalletAmount = String.valueOf(usedWalletBalance);

            PaymentChoiceActivity.newInstance(mContext, taskDetailModel, providerModel, mSelectedAddressModelForInsta);

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
    View.OnClickListener onPromoCodeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mActivityPaymentDetailBinding.ivpromocode.setVisibility(View.VISIBLE);
            mActivityPaymentDetailBinding.ivpromocode.setSelected(false);
            mActivityPaymentDetailBinding.txtpromocode.setSelected(false);
            mActivityPaymentDetailBinding.imgCheepCodeClose.setVisibility(View.GONE);
            mActivityPaymentDetailBinding.llpromocode.setEnabled(true);
            resetPromoCodeValue();

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
                if (!TextUtils.isEmpty(providerModel.actualQuotePrice)) {
                    providerModel.quotePrice = providerModel.actualQuotePrice;
                }
                providerModel.actualQuotePrice = "";
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

                try {
                    providerModel.actualQuotePrice = providerModel.quotePrice;
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

                if (!TextUtils.isEmpty(providerModel.actualQuotePrice)) {
                    providerModel.quotePrice = providerModel.actualQuotePrice;
                }
                providerModel.actualQuotePrice = "";
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
        if (!TextUtils.isEmpty(providerModel.actualQuotePrice)) {
            providerModel.quotePrice = providerModel.actualQuotePrice;
        }
        if (taskDetailModel != null) {
            taskDetailModel.cheepCode = Utility.EMPTY_STRING;
            taskDetailModel.taskDiscountAmount = Utility.ZERO_STRING;
        }
        providerModel.actualQuotePrice = "";
        mActivityPaymentDetailBinding.imgCheepCodeClose.setVisibility(View.GONE);
        mActivityPaymentDetailBinding.llpromocode.setEnabled(true);
        resetPromoCodeValue();
    }

    /**
     * Used for Reset Payment Values after applying promocode OR Removing promociod
     */
    public void resetPromoCodeValue() {
        mActivityPaymentDetailBinding.textpromocodelabel.setTextColor(ContextCompat.getColor(this, R.color.splash_gradient_end));
        mActivityPaymentDetailBinding.textpromocodelabel.setText(getResources().getString(R.string.label_enter_promocode));
        mActivityPaymentDetailBinding.lnPromoCodeDisclaimer.setVisibility(View.GONE);
        taskDetailModel.isReferCode = Utility.BOOLEAN.NO;
        mActivityPaymentDetailBinding.txttotal.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf(providerModel.quotePrice))));
        mActivityPaymentDetailBinding.txtpromocode.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf(0))));
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

        providerModel.actualQuotePrice = providerModel.quotePrice;


        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, userDetails.userID);

        //Add Params
        Map<String, Object> mParams = new HashMap<>();

        if (isInstaBooking) {
            mParams.put(NetworkUtility.TAGS.QUOTE_AMOUNT, providerModel.spWithoutGstQuotePrice);
            mParams.put(NetworkUtility.TAGS.CHEEPCODE, cheepCode);
            mParams.put(NetworkUtility.TAGS.CAT_ID, taskDetailModel.categoryModel.catId);
            mParams.put(NetworkUtility.TAGS.IS_INSTA_BOOKING, Utility.BOOLEAN.YES);
            int addressId;
            try {
                addressId = Integer.parseInt(mSelectedAddressModelForInsta.address_id);
            } catch (Exception e) {
                addressId = 0;
            }
            if (addressId <= 0) {
                NetworkUtility.addGuestAddressParams(mTaskCreationParams, mSelectedAddressModelForInsta);

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
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().userID);
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
        mActivityPaymentDetailBinding.textTitle.setText(getString(R.string.label_payment_summary));
        mActivityPaymentDetailBinding.textName.setVisibility(View.GONE);
        mActivityPaymentDetailBinding.lnRateExp.setVisibility(View.GONE);
        mActivityPaymentDetailBinding.textVerified.setVisibility(View.INVISIBLE);
        mActivityPaymentDetailBinding.textCategory.setVisibility(View.INVISIBLE);
        mActivityPaymentDetailBinding.textStepDesc.setVisibility(View.INVISIBLE);
        mActivityPaymentDetailBinding.lnstep.setVisibility(View.INVISIBLE);
        mActivityPaymentDetailBinding.lltermsandcondition.setVisibility(View.GONE);
        mActivityPaymentDetailBinding.lnPayNow.setVisibility(View.GONE);
        mActivityPaymentDetailBinding.textLabelTotalPaid.setText(getString(R.string.label_total_paid));
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


    private void callBookProForNormalTaskWS() {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityPaymentDetailBinding.getRoot());
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
                            String message = CalendarUtility.fetchMessageFromDateOfMonth(mContext, onlydate, superStartDateTimeCalendar, providerModel);

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
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityPaymentDetailBinding.getRoot());
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
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityPaymentDetailBinding.getRoot());
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
        mParams.put(NetworkUtility.TAGS.CAT_ID, taskDetailModel.categoryModel.catId);
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
        mTaskCreationParams.put(NetworkUtility.TAGS.CAT_ID, taskDetailModel.categoryModel.catId);
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
                        Utility.onSuccessfulInstaBookingTaskCompletion(PaymentDetailsActivity.this, jsonObject, providerModel);
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
                mCallBookProForNormalTaskWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }

        }
    };


    // check is task is from insta booking or not

//    Blue Heart Emoji (U+1F499) - iEmoji.com
}
