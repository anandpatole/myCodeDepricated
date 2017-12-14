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
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.activity.PaymentChoiceActivity;
import com.cheep.databinding.ActivityPaymentDetailStartegicPartnerNewBinding;
import com.cheep.databinding.RowPaymentSummaryBinding;
import com.cheep.model.MessageEvent;
import com.cheep.model.PaymentSummaryModel;
import com.cheep.model.TaskDetailModel;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
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
import java.util.List;
import java.util.Map;


public class PaymentsSummaryStrategicPartnerActivity extends BaseAppCompatActivity {

    public static void newInstance(Context context, TaskDetailModel taskDetailModel) {
        Intent intent = new Intent(context, PaymentsSummaryStrategicPartnerActivity.class);
        intent.putExtra(Utility.Extra.DATA, Utility.getJsonStringFromObject(taskDetailModel));
        context.startActivity(intent);
    }

    PaymentSummaryModel paymentSummaryModel;
    private static final String TAG = "PaymentsSummaryStrategi";
    private TaskDetailModel taskDetailModel;
    private ActivityPaymentDetailStartegicPartnerNewBinding mActivityPaymentDetailBinding;
    /**
     * used while user taps on View Payment summary and tries to do payment in on going task
     */
    private boolean isPayNow = true;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
          when the device runing out of memory we dont want the user to restart the payment. rather we close it and redirect them to previous activity.
         */
        mActivityPaymentDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_payment_detail_startegic_partner_new);
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

        if (getIntent().hasExtra(Utility.Extra.DATA)) {
            //This is only when provider profile view for specific task (provider gives quote to specific task)
            taskDetailModel = (TaskDetailModel) Utility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA), TaskDetailModel.class);
        }

        if (taskDetailModel != null) {
            mActivityPaymentDetailBinding.recycleSelectedService.setLayoutManager(new LinearLayoutManager(this));

            ViewTreeObserver mViewTreeObserver = mActivityPaymentDetailBinding.frameBannerImage.getViewTreeObserver();
            mViewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mActivityPaymentDetailBinding.frameBannerImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    int width = mActivityPaymentDetailBinding.frameBannerImage.getMeasuredWidth();
                    ViewGroup.LayoutParams params = mActivityPaymentDetailBinding.frameBannerImage.getLayoutParams();

                    params.height = Utility.getHeightFromWidthForTwoOneRatio(width);
                    mActivityPaymentDetailBinding.frameBannerImage.setLayoutParams(params);
                    // Load the image now.
                    Utility.loadImageView(mContext, mActivityPaymentDetailBinding.imgService, taskDetailModel.bannerImage, R.drawable.gradient_black);
                }
            });

            if (taskDetailModel.isAnyAmountPending.equalsIgnoreCase(Utility.BOOLEAN.YES)) {
                mActivityPaymentDetailBinding.textTitle.setText(R.string.title_booking_confimation);
            } else {
                mActivityPaymentDetailBinding.textTitle.setText(R.string.label_payment_summary);

            }

            callPaymentSummaryWS();

            Utility.showCircularImageViewWithColorBorder(mContext, TAG, mActivityPaymentDetailBinding.imgLogo, taskDetailModel.catImage, Utility.DEFAULT_CHEEP_LOGO, R.color.dark_blue_variant_1, true);
            String dateTime = "";
            if (!TextUtils.isEmpty(taskDetailModel.taskStartdate)) {
                dateTime = Utility.getDate(Long.parseLong(taskDetailModel.taskStartdate), Utility.DATE_TIME_DD_MMMM_HH_MM);
                dateTime = dateTime.replace(getString(R.string.label_am_caps), getString(R.string.label_am_small)).replace(getString(R.string.label_pm_caps), getString(R.string.label_pm_small));
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
            spannableStringBuilder.append(getSpannableString(getString(R.string.label_your_order_with), ContextCompat.getColor(this, R.color.grey_varient_8), false));
            spannableStringBuilder.append(getSpannableString(taskDetailModel.categoryName, ContextCompat.getColor(this, R.color.splash_gradient_end), true));
            spannableStringBuilder.append(getSpannableString(getString(R.string.label_on), ContextCompat.getColor(this, R.color.grey_varient_8), false));
            spannableStringBuilder.append(getSpannableString(selectedDate + ", " + selectedTime
                    , ContextCompat.getColor(this, R.color.splash_gradient_end), true));
            spannableStringBuilder.append(getSpannableString(getString(R.string.label_at), ContextCompat.getColor(this, R.color.grey_varient_8), false));
            spannableStringBuilder.append(getSpannableString(taskDetailModel.taskAddress, ContextCompat.getColor(this, R.color.splash_gradient_end), true));
            spannableStringBuilder.append(getSpannableString(".", ContextCompat.getColor(this, R.color.splash_gradient_end), true));


            mActivityPaymentDetailBinding.txtdesc.setText(spannableStringBuilder);

        }

    }

    private void setPaymentData() {
        if (paymentSummaryModel != null) {

            if (paymentSummaryModel.totalAmountStatus.equalsIgnoreCase(Utility.TASK_STATUS.PENDING)) {
                mActivityPaymentDetailBinding.lnPayNow.setVisibility(View.VISIBLE);
                mActivityPaymentDetailBinding.textPayNow.setSelected(true);
                mActivityPaymentDetailBinding.textStepDesc.setVisibility(View.VISIBLE);
                mActivityPaymentDetailBinding.textTitle.setText(R.string.title_booking_confimation);
            } else {
                mActivityPaymentDetailBinding.lnPayNow.setVisibility(View.GONE);
                mActivityPaymentDetailBinding.textStepDesc.setVisibility(View.GONE);
                mActivityPaymentDetailBinding.textTitle.setText(R.string.label_payment_summary);
            }

            mActivityPaymentDetailBinding.txtsubtotal.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf(paymentSummaryModel.subTotalAmount))));
            mActivityPaymentDetailBinding.txttotal.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf(paymentSummaryModel.totalAmount))));
            mActivityPaymentDetailBinding.txtpromocode.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(String.valueOf((paymentSummaryModel.promoCodePrice)))));

            double promocodeValue = getQuotePriceInInteger(paymentSummaryModel.promoCodePrice);

            mActivityPaymentDetailBinding.lnPromoCodeDisclaimer.setVisibility(promocodeValue == 0 ? View.GONE : View.VISIBLE);
            if (paymentSummaryModel.totalAmountStatus.equalsIgnoreCase(Utility.TASK_STATUS.PENDING))
                mActivityPaymentDetailBinding.textLabelTotalPaid.setText(getString(R.string.label_total_pay));
            else
                mActivityPaymentDetailBinding.textLabelTotalPaid.setText(getString(R.string.label_total_paid));

            mActivityPaymentDetailBinding.recycleSelectedService.setAdapter(new PaymentSummaryAdapter(paymentSummaryModel.taskUserCategory));

        }
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
//        showProgressDialog();
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
//            hideProgressDialog();
            showProgressBar(false);/**/
            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
                        paymentSummaryModel = (PaymentSummaryModel) Utility.getObjectFromJsonString(jsonObject.optString(NetworkUtility.TAGS.DATA), PaymentSummaryModel.class);
                        setPaymentData();
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
//            hideProgressDialog();

            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityPaymentDetailBinding.getRoot());

        }
    };

    private void showProgressBar(boolean flag) {
        mActivityPaymentDetailBinding.progress.setVisibility(flag ? View.VISIBLE : View.GONE);
        mActivityPaymentDetailBinding.lnTop.setVisibility(flag ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void setListeners() {
        mActivityPaymentDetailBinding.textPayNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPayNow = true;
                PaymentChoiceActivity.newInstance(mContext, taskDetailModel);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.GET_PAYMENT_SUMMARY);
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        LogUtils.LOGD(TAG, "onMessageEvent() called with: event = [" + event.BROADCAST_ACTION + "]");
        switch (event.BROADCAST_ACTION) {
            case Utility.BROADCAST_TYPE.TASK_PAID_SUCCESSFULLY:

                //Refresh UI for complete status
                if (isPayNow) {
//                    paymentSummaryModel.totalAmountStatus = Utility.TASK_STATUS.PAID;
//                    setPaymentData();
                    finish();
                }
                break;
        }
    }


    class PaymentSummaryAdapter extends RecyclerView.Adapter<PaymentSummaryAdapter.MyViewHolder> {
        private List<PaymentSummaryModel.TaskUserCategory> mList;

        PaymentSummaryAdapter(List<PaymentSummaryModel.TaskUserCategory> mList) {
            this.mList = mList;
        }


        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RowPaymentSummaryBinding rowPaymentSummaryBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_payment_summary, parent, false);
            return new MyViewHolder(rowPaymentSummaryBinding);

        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            PaymentSummaryModel.TaskUserCategory taskUserCategory = mList.get(position);
            holder.rowPastTaskBinding.textServiceName.setText(taskUserCategory.userCategory);

            // calculate selected sub services amount and set total
            holder.rowPastTaskBinding.textServiceSubService.setSelected(true);
            holder.rowPastTaskBinding.textServiceSubService.setText(taskUserCategory.userSubCategory);
            holder.rowPastTaskBinding.textServiceRate.setText(
                    holder.rowPastTaskBinding.textServiceRate.getContext().getString(R.string.rupee_symbol_x, String.valueOf(Utility.getQuotePriceFormatter(taskUserCategory.userCategoryPrice))));
        }


        @Override
        public int getItemCount() {
            return mList.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            final RowPaymentSummaryBinding rowPastTaskBinding;

            MyViewHolder(RowPaymentSummaryBinding binding) {
                super(binding.getRoot());
                rowPastTaskBinding = binding;
            }
        }
    }
}
