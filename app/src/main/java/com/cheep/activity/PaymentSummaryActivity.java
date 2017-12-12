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
import com.cheep.databinding.ActivityPaymentSummaryBinding;
import com.cheep.model.PaymentSummaryModel;
import com.cheep.model.TaskDetailModel;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class PaymentSummaryActivity extends BaseAppCompatActivity {

    private static final String TAG = "PaymentSummaryActivity";
    private ActivityPaymentSummaryBinding mActivityPaymentDetailBinding;
    private TaskDetailModel taskDetailModel;
    private PaymentSummaryModel paymentSummaryModel;

    /**
     * view summary
     *
     * @param context         context
     * @param taskDetailModel task detail for summary
     */
    public static void newInstance(Context context, TaskDetailModel taskDetailModel) {
        Intent intent = new Intent(context, PaymentSummaryActivity.class);
        intent.putExtra(Utility.Extra.DATA_2, Utility.getJsonStringFromObject(taskDetailModel));
        context.startActivity(intent);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
          when the device runing out of memory we dont want the user to restart the payment. rather we close it and redirect them to previous activity.
         */
        mActivityPaymentDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_payment_summary);

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


        if (getIntent().hasExtra(Utility.Extra.DATA_2)) {
            //This is only when provider profile view for specific task (provider gives quote to specific task)
            taskDetailModel = (TaskDetailModel) Utility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA_2), TaskDetailModel.class);
        }

        if (taskDetailModel != null) {
            callPaymentSummaryWS();
            Utility.loadImageView(mContext, mActivityPaymentDetailBinding.imgService, taskDetailModel.catImage, R.drawable.gradient_black);
            if (taskDetailModel.selectedProvider != null) {
                Utility.showCircularImageViewWithColorBorder(mContext, TAG, mActivityPaymentDetailBinding.imgProfile, taskDetailModel.selectedProvider.profileUrl, Utility.DEFAULT_CHEEP_LOGO, R.color.dark_blue_variant_1, true);
                String dateTime = "";
                if (!TextUtils.isEmpty(taskDetailModel.taskStartdate)) {
                    dateTime = Utility.getDate(Long.parseLong(taskDetailModel.taskStartdate), Utility.DATE_TIME_DD_MMMM_HH_MM);
                    dateTime = dateTime.replace(getString(R.string.label_am_caps), getString(R.string.label_am_small)).replace(getString(R.string.label_pm_caps), getString(R.string.label_pm_small));
                }

                SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
                spannableStringBuilder.append(getSpannableString(taskDetailModel.subCategoryName, ContextCompat.getColor(this, R.color.splash_gradient_end), true));
                spannableStringBuilder.append(getSpannableString(getString(R.string.label_by), ContextCompat.getColor(this, R.color.grey_varient_8), false));
                spannableStringBuilder.append(getSpannableString(taskDetailModel.selectedProvider.userName, ContextCompat.getColor(this, R.color.splash_gradient_end), true));
                spannableStringBuilder.append(getSpannableString(getString(R.string.label_on), ContextCompat.getColor(this, R.color.grey_varient_8), false));
                spannableStringBuilder.append(getSpannableString(dateTime, ContextCompat.getColor(this, R.color.splash_gradient_end), true));
                spannableStringBuilder.append(getSpannableString(getString(R.string.label_at), ContextCompat.getColor(this, R.color.grey_varient_8), false));
                spannableStringBuilder.append(getSpannableString(taskDetailModel.taskAddress, ContextCompat.getColor(this, R.color.splash_gradient_end), true));
                mActivityPaymentDetailBinding.txtdesc.setText(spannableStringBuilder);
            }
        }

    }


    /**
     * Used for Set Payment VAlue When User only Preview.]
     * Payment summary screen for task summary
     */

    public void viewPaymentDetails() {
        if (paymentSummaryModel != null) {
            if (paymentSummaryModel.totalAmountStatus.equalsIgnoreCase(Utility.TASK_STATUS.PENDING))
                mActivityPaymentDetailBinding.textLabelTotalPaid.setText(getString(R.string.label_total_pay));
            else
                mActivityPaymentDetailBinding.textLabelTotalPaid.setText(getString(R.string.label_total_paid));

            mActivityPaymentDetailBinding.txtprofee.setText(getRuppeAmount(paymentSummaryModel.proPaymentAmount));
            mActivityPaymentDetailBinding.txtadditionalcharge.setText(getRuppeAmount(paymentSummaryModel.additionalPaidAmount));
            mActivityPaymentDetailBinding.txtsubtotal.setText(getRuppeAmount(paymentSummaryModel.subTotalAmount));
            mActivityPaymentDetailBinding.txttotal.setText(getRuppeAmount(paymentSummaryModel.totalAmount));
            mActivityPaymentDetailBinding.txtreferraldiscount.setText(getRuppeAmount(paymentSummaryModel.walletBalanceUsed));
            mActivityPaymentDetailBinding.txtpromocode.setText(getRuppeAmount(paymentSummaryModel.promoCodePrice));

            if (getQuotePriceInInteger(paymentSummaryModel.walletBalanceUsed) > 0) {
                mActivityPaymentDetailBinding.lnPromoCodeDisclaimer.setVisibility(View.VISIBLE);
                mActivityPaymentDetailBinding.txtPromoCodeDisclaimer.setText(R.string.disclaimer_referral);
            } else if (getQuotePriceInInteger(paymentSummaryModel.promoCodePrice) > 0) {
                mActivityPaymentDetailBinding.lnPromoCodeDisclaimer.setVisibility(View.VISIBLE);
                mActivityPaymentDetailBinding.txtPromoCodeDisclaimer.setText(R.string.disclaimer_promo_code);
            } else {
                mActivityPaymentDetailBinding.lnPromoCodeDisclaimer.setVisibility(View.GONE);
            }

        }
    }

    private String getRuppeAmount(String proPaymentAmount) {
        return getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(proPaymentAmount));
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
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////// Payment Detail Detail Service[Start] ////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Call Task Detail web service
     */
    private void callPaymentSummaryWS() {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityPaymentDetailBinding.getRoot());
            return;
        }

        showProgressBar(true);

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);

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
                        paymentSummaryModel = (PaymentSummaryModel) Utility.getObjectFromJsonString(jsonObject.optString(NetworkUtility.TAGS.DATA), PaymentSummaryModel.class);
                        viewPaymentDetails();
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
            showProgressBar(false);

            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityPaymentDetailBinding.getRoot());

        }
    };

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////// Payment Detail WS[END] ///////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////

    private void showProgressBar(boolean flag) {
        mActivityPaymentDetailBinding.progress.setVisibility(flag ? View.VISIBLE : View.GONE);
        mActivityPaymentDetailBinding.lnRoot.setVisibility(flag ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.GET_PAYMENT_SUMMARY);
        super.onDestroy();
    }
}
