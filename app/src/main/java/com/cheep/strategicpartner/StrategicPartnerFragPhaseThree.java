package com.cheep.strategicpartner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.appsflyer.AppsFlyerLib;
import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.activity.LoginActivity;
import com.cheep.activity.PaymentChoiceActivity;
import com.cheep.custom_view.BottomAlertDialog;
import com.cheep.custom_view.CFEditTextRegular;
import com.cheep.databinding.FragmentStrategicPartnerPhaseThreeBinding;
import com.cheep.dialogs.AcknowledgementDialogWithProfilePic;
import com.cheep.dialogs.AcknowledgementInteractionListener;
import com.cheep.fragment.BaseFragment;
import com.cheep.model.MessageEvent;
import com.cheep.model.ProviderModel;
import com.cheep.model.TaskDetailModel;
import com.cheep.model.UserDetails;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.strategicpartner.model.MediaModel;
import com.cheep.strategicpartner.model.QueAnsModel;
import com.cheep.utils.LogUtils;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.SuperCalendar;
import com.cheep.utils.Utility;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Giteeka on 28/7/17.
 * This Fragment is Third step of Strategic partner screen.
 * Payment summary
 * Task time and address details
 * Selection service total amount
 * Enter promo code logic
 * and payment flow
 */
public class StrategicPartnerFragPhaseThree extends BaseFragment {
    public static final String TAG = "StrategicPartnerFragPha";
    private FragmentStrategicPartnerPhaseThreeBinding mFragmentStrategicPartnerPhaseThreeBinding;
    private StrategicPartnerTaskCreationAct mStrategicPartnerTaskCreationAct;
    private String payableAmount = "";
    private String promocode_price = "";
    private String start_datetime = "";
    private String date = "";
    private String time = "";
    private CFEditTextRegular edtCheepCode;
    private BottomAlertDialog cheepCodeDialog;
//    TaskDetailModel taskDetailModel;

    @Nullable
    private String cheepCode;
    private String tempTotalBasePrice = "";
    // After Posting Task, this would hold the details for AppsFlyer
    Map<String, Object> mTaskCreationParams;

    @SuppressWarnings("unused")
    public static StrategicPartnerFragPhaseThree newInstance() {
        return new StrategicPartnerFragPhaseThree();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext.registerReceiver(mBR_OnLoginSuccess, new IntentFilter(Utility.BR_ON_LOGIN_SUCCESS));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mFragmentStrategicPartnerPhaseThreeBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_strategic_partner_phase_three, container, false);
        return mFragmentStrategicPartnerPhaseThreeBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initiateUI();
        setListener();
    }

    @Override
    public void initiateUI() {
        LogUtils.LOGD(TAG, "initiateUI() called");
        mFragmentStrategicPartnerPhaseThreeBinding.textDescPayNow.setText(getString(R.string.description_pay_now) + " " + new String(Character.toChars(0x1F499)));

        mFragmentStrategicPartnerPhaseThreeBinding.recycleSelectedService.setLayoutManager(new LinearLayoutManager(mStrategicPartnerTaskCreationAct));
        mFragmentStrategicPartnerPhaseThreeBinding.recycleSelectedService.setNestedScrollingEnabled(false);
        if (mStrategicPartnerTaskCreationAct.getSelectedSubService() != null)
            mFragmentStrategicPartnerPhaseThreeBinding.recycleSelectedService.setAdapter(new PaymentDetailAdapter(mStrategicPartnerTaskCreationAct.getSelectedSubService()));
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        LogUtils.LOGD(TAG, "setUserVisibleHint() called with: isVisibleToUser = [" + isVisibleToUser + "]");
        if (!isVisibleToUser || mStrategicPartnerTaskCreationAct == null) {
            return;
        }
        mStrategicPartnerTaskCreationAct.setTaskState(StrategicPartnerTaskCreationAct.STEP_THREE_NORMAL);

        // force to scroll up the view for strategic partner logo
        mFragmentStrategicPartnerPhaseThreeBinding.scrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mFragmentStrategicPartnerPhaseThreeBinding.scrollView.fullScroll(View.FOCUS_UP);
            }
        }, 5);

        // get date time and selected address from Questions (phase 2)
        if (mStrategicPartnerTaskCreationAct.getQuestionsList() != null)
            for (int i = 0; i < mStrategicPartnerTaskCreationAct.getQuestionsList().size(); i++) {
                QueAnsModel queAnsModel = mStrategicPartnerTaskCreationAct.getQuestionsList().get(i);
                if (queAnsModel.answerType.equalsIgnoreCase(Utility.TEMPLATE_TIME_PICKER)) {
                    start_datetime = queAnsModel.answer;
                    SuperCalendar superCalendar = SuperCalendar.getInstance();
                    superCalendar.setTimeInMillis(Long.parseLong(start_datetime));
                    time = superCalendar.format(Utility.DATE_FORMAT_HH_MM_AM);
                    date = superCalendar.format(Utility.DATE_FORMAT_DD_MMM_YYYY);
                }
            }
        // set details of partner name user selected date time and address
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(getSpannableString(mContext.getString(R.string.label_your_order_with), ContextCompat.getColor(mStrategicPartnerTaskCreationAct, R.color.grey_varient_8), false));
        spannableStringBuilder.append(getSpannableString(mStrategicPartnerTaskCreationAct.mBannerImageModel.name, ContextCompat.getColor(mStrategicPartnerTaskCreationAct, R.color.splash_gradient_end), true));
        spannableStringBuilder.append(getSpannableString(getString(R.string.label_on), ContextCompat.getColor(mStrategicPartnerTaskCreationAct, R.color.grey_varient_8), false));
        spannableStringBuilder.append(getSpannableString(date + ", " + time
                , ContextCompat.getColor(mStrategicPartnerTaskCreationAct, R.color.splash_gradient_end), true));
        spannableStringBuilder.append(getSpannableString(getString(R.string.label_at), ContextCompat.getColor(mStrategicPartnerTaskCreationAct, R.color.grey_varient_8), false));
        if (mStrategicPartnerTaskCreationAct.mSelectedAddressModel != null)
            spannableStringBuilder.append(getSpannableString(mStrategicPartnerTaskCreationAct.mSelectedAddressModel.address, ContextCompat.getColor(mStrategicPartnerTaskCreationAct, R.color.splash_gradient_end), true));
        spannableStringBuilder.append(getSpannableString(".", ContextCompat.getColor(mStrategicPartnerTaskCreationAct, R.color.splash_gradient_end), true));

        mFragmentStrategicPartnerPhaseThreeBinding.txtdesc.setText(spannableStringBuilder);

        // get list of selected services from phase 1
        if (mStrategicPartnerTaskCreationAct.getSelectedSubService() != null)
            mFragmentStrategicPartnerPhaseThreeBinding.recycleSelectedService.setAdapter(new PaymentDetailAdapter(mStrategicPartnerTaskCreationAct.getSelectedSubService()));

        // set total and sub total details
        mFragmentStrategicPartnerPhaseThreeBinding.txttotal.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(mStrategicPartnerTaskCreationAct.totalOfGSTPrice)));
        mFragmentStrategicPartnerPhaseThreeBinding.txtsubtotal.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(mStrategicPartnerTaskCreationAct.totalOfGSTPrice)));
//        mFragmentStrategicPartnerPhaseThreeBinding.textPay.setText(getString(R.string.label_book_now_for_rupees, String.valueOf(Utility.getQuotePriceFormatter(mStrategicPartnerTaskCreationAct.totalOfGSTPrice))));

        // handle clicks for create task web api and payment flow
        mFragmentStrategicPartnerPhaseThreeBinding.rlPayLater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mStrategicPartnerTaskCreationAct.isPayNow = false;

                if (!Utility.isConnected(mContext)) {
                    Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mFragmentStrategicPartnerPhaseThreeBinding.getRoot());
                    return;
                }

                if (PreferenceUtility.getInstance(mContext).getUserDetails() == null) {
                    LoginActivity.newInstance(mContext);
                    return;
                }

                callCreateStrategicPartnerTaskWS();
            }
        });
        mFragmentStrategicPartnerPhaseThreeBinding.rlPayNow.setSelected(true);
        mFragmentStrategicPartnerPhaseThreeBinding.rlPayLater.setSelected(true);
        mFragmentStrategicPartnerPhaseThreeBinding.rlPayNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mStrategicPartnerTaskCreationAct.isPayNow = true;
                if (!Utility.isConnected(mContext)) {
                    Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mFragmentStrategicPartnerPhaseThreeBinding.getRoot());
                    return;
                }

                if (PreferenceUtility.getInstance(mContext).getUserDetails() == null) {
                    LoginActivity.newInstance(mContext);
                    return;
                }

                TaskDetailModel taskDetailModel = new TaskDetailModel();
                taskDetailModel.mQuesList = mStrategicPartnerTaskCreationAct.getQuestionsList();
                taskDetailModel.taskDesc = getTaskDescription(mStrategicPartnerTaskCreationAct.getQuestionsList());
                taskDetailModel.taskSelectedSubCategoryList = mStrategicPartnerTaskCreationAct.getSelectedSubService();
                for (QueAnsModel model : mStrategicPartnerTaskCreationAct.getQuestionsList())
                    if (model.answerType.equalsIgnoreCase(Utility.TEMPLATE_UPLOAD)) {
                        taskDetailModel.mMediaModelList = model.medialList;
                        break;
                    }
                taskDetailModel.taskStartdate = start_datetime;
                taskDetailModel.categoryId = mStrategicPartnerTaskCreationAct.mBannerImageModel.cat_id;
                taskDetailModel.taskType = Utility.TASK_TYPE.STRATEGIC;

                taskDetailModel.quoteAmountStrategicPartner = mStrategicPartnerTaskCreationAct.totalOfBasePrice;
                taskDetailModel.cheepCode = cheepCode;
                taskDetailModel.taskDiscountAmount = promocode_price;
                taskDetailModel.taskStatus = Utility.TASK_STATUS.PENDING;
                taskDetailModel.taskPaidAmount = TextUtils.isEmpty(taskDetailModel.cheepCode) ? mStrategicPartnerTaskCreationAct.totalOfGSTPrice
                        : payableAmount;

                ProviderModel provider = new ProviderModel();
                provider.providerId = mStrategicPartnerTaskCreationAct.spUserId;
                taskDetailModel.selectedProvider = provider;
                taskDetailModel.categoryName = mStrategicPartnerTaskCreationAct.mBannerImageModel.name;
                taskDetailModel.catImage = mStrategicPartnerTaskCreationAct.mBannerImageModel.imgCatImageUrl;

                PaymentChoiceActivity.newInstance(StrategicPartnerFragPhaseThree.this, taskDetailModel, mStrategicPartnerTaskCreationAct.mSelectedAddressModel);


            }
        });


        // Enter promo code UI
        mFragmentStrategicPartnerPhaseThreeBinding.textpromocodelabel.setTextColor(ContextCompat.getColor(mStrategicPartnerTaskCreationAct, R.color.splash_gradient_end));
        mFragmentStrategicPartnerPhaseThreeBinding.textpromocodelabel.setText(getResources().getString(R.string.label_enter_promocode));
        mFragmentStrategicPartnerPhaseThreeBinding.lnPromoCodeDisclaimer.setVisibility(View.GONE);

        mFragmentStrategicPartnerPhaseThreeBinding.textpromocodelabel.setEnabled(true);
        mFragmentStrategicPartnerPhaseThreeBinding.textpromocodelabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCheepCodeDialog();
            }
        });
        mFragmentStrategicPartnerPhaseThreeBinding.imgCheepCodeClose.setVisibility(View.GONE);
        mFragmentStrategicPartnerPhaseThreeBinding.txtpromocode.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(Utility.ZERO_STRING)));
        mFragmentStrategicPartnerPhaseThreeBinding.imgCheepCodeClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cheepCode = null;
                mFragmentStrategicPartnerPhaseThreeBinding.textpromocodelabel.setEnabled(true);
                mFragmentStrategicPartnerPhaseThreeBinding.imgCheepCodeClose.setVisibility(View.GONE);
                resetPromoCodeValue();
            }
        });

        // reset the values fo Payable Amount
        cheepCode = Utility.EMPTY_STRING;
        payableAmount = Utility.EMPTY_STRING;
        promocode_price = Utility.EMPTY_STRING;
    }

    /**
     * clear promo code details and set original total values
     */
    private void resetPromoCodeValue() {
        mFragmentStrategicPartnerPhaseThreeBinding.textpromocodelabel.setTextColor(ContextCompat.getColor(mStrategicPartnerTaskCreationAct, R.color.splash_gradient_end));
        mFragmentStrategicPartnerPhaseThreeBinding.textpromocodelabel.setText(getResources().getString(R.string.label_enter_promocode));
        mFragmentStrategicPartnerPhaseThreeBinding.txtsubtotal.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(mStrategicPartnerTaskCreationAct.totalOfGSTPrice)));
        mFragmentStrategicPartnerPhaseThreeBinding.txttotal.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(mStrategicPartnerTaskCreationAct.totalOfGSTPrice)));
//        mFragmentStrategicPartnerPhaseThreeBinding.textPay.setText(getString(R.string.label_book_now_for_rupees, "" + Utility.getQuotePriceFormatter(mStrategicPartnerTaskCreationAct.totalOfGSTPrice)));
        mFragmentStrategicPartnerPhaseThreeBinding.txtpromocode.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(Utility.ZERO_STRING)));
        mFragmentStrategicPartnerPhaseThreeBinding.lnPromoCodeDisclaimer.setVisibility(View.GONE);
    }

    private void showCheepCodeDialog() {

        View view = View.inflate(mContext, R.layout.dialog_add_promocode, null);
        edtCheepCode = view.findViewById(R.id.edit_cheepcode);
        cheepCodeDialog = new BottomAlertDialog(mContext);
        view.findViewById(R.id.btn_apply).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(edtCheepCode.getText().toString())) {
                    Utility.showToast(mContext, getString(R.string.validate_cheepcode));
                    return;
                }
                validateCheepCode(edtCheepCode.getText().toString());
            }
        });
        edtCheepCode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_DONE:
                        if (TextUtils.isEmpty(edtCheepCode.getText().toString())) {
                            Utility.showToast(mContext, getString(R.string.validate_cheepcode));
                            break;
                        }
                        validateCheepCode(edtCheepCode.getText().toString());
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


    private void validateCheepCode(String s) {
        cheepCode = s;
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mFragmentStrategicPartnerPhaseThreeBinding.getRoot());
            return;
        }
        showProgressDialog();
        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();


        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        if (userDetails != null)
            mHeaderParams.put(NetworkUtility.TAGS.USER_ID, userDetails.UserID);

        //Add Params
        Map<String, Object> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.QUOTE_AMOUNT, mStrategicPartnerTaskCreationAct.totalOfBasePrice);
        mParams.put(NetworkUtility.TAGS.CHEEPCODE, cheepCode);
        mParams.put(NetworkUtility.TAGS.CAT_ID, mStrategicPartnerTaskCreationAct.mBannerImageModel.cat_id);
        mParams.put(NetworkUtility.TAGS.IS_INSTA_BOOKING, Utility.BOOLEAN.NO);

        int addressId = 0;
        try {
            if (mStrategicPartnerTaskCreationAct.mSelectedAddressModel != null)
                addressId = Integer.parseInt(mStrategicPartnerTaskCreationAct.mSelectedAddressModel.address_id);
        } catch (Exception e) {
            addressId = 0;
        }
        if (addressId <= 0) {
            NetworkUtility.addGuestAddressParams(mTaskCreationParams, mStrategicPartnerTaskCreationAct.mSelectedAddressModel);
        } else {
            mParams.put(NetworkUtility.TAGS.ADDRESS_ID, addressId);
        }

        //Url is based on condition if address id is greater then 0 then it means we need to update the existing address
        VolleyNetworkRequest mVolleyNetworkRequestForSPList = new VolleyNetworkRequest(NetworkUtility.WS.CHECK_CHEEPCODE_FOR_STRATEGIC_PARTNER
                , mCallValidateCheepCodeWSErrorListener
                , mCallValidateCheepCodeWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForSPList);
    }

    private Response.Listener mCallValidateCheepCodeWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {

            String strResponse = (String) response;
            try {
                Utility.hideKeyboard(mStrategicPartnerTaskCreationAct, edtCheepCode);
                JSONObject jsonObject = new JSONObject(strResponse);
                LogUtils.LOGI(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;
                hideProgressDialog();
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:

                        if (edtCheepCode != null) {
                            cheepCode = edtCheepCode.getText().toString().trim();
                            cheepCodeDialog.dismiss();
                            tempTotalBasePrice = mStrategicPartnerTaskCreationAct.totalOfBasePrice;
                            mStrategicPartnerTaskCreationAct.totalOfBasePrice = jsonObject.optString(NetworkUtility.TAGS.QUOTE_AMOUNT);
                            String discount = jsonObject.optString(NetworkUtility.TAGS.DISCOUNT_AMOUNT);
                            String payable = jsonObject.optString(NetworkUtility.TAGS.PAYABLE_AMOUNT);
                            mFragmentStrategicPartnerPhaseThreeBinding.lnPromoCodeDisclaimer.setVisibility(View.VISIBLE);
                            updatePaymentDetails(discount, payable);

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

                        cheepCode = "";
                        if (!TextUtils.isEmpty(tempTotalBasePrice))
                            mStrategicPartnerTaskCreationAct.totalOfBasePrice = tempTotalBasePrice;

                        Utility.showToast(mContext, error_message);
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.USER_DELETED:
                    case NetworkUtility.TAGS.STATUSCODETYPE.FORCE_LOGOUT_REQUIRED:
                        //Logout and finish the current activity
                        Utility.logout(mContext, true, statusCode);
                        mStrategicPartnerTaskCreationAct.finish();
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mCallValidateCheepCodeWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
        }
    };
    private Response.ErrorListener mCallValidateCheepCodeWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(final VolleyError error) {
            LogUtils.LOGD(TAG, "onErrorResponse() called with: error = [" + error + "]");
            Utility.hideKeyboard(mStrategicPartnerTaskCreationAct, edtCheepCode);
            Utility.showToast(mContext, getString(R.string.label_something_went_wrong));
        }

    };

    private void updatePaymentDetails(String discount, String payable) {
        payableAmount = payable;

        mFragmentStrategicPartnerPhaseThreeBinding.txtpromocode.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(discount)));
        mFragmentStrategicPartnerPhaseThreeBinding.txttotal.setText(getString(R.string.rupee_symbol_x, "" + Utility.getQuotePriceFormatter(payable)));

        promocode_price = discount;


//        mFragmentStrategicPartnerPhaseThreeBinding.textPay.setText(getString(R.string.label_book_now_for_rupees, "" + Utility.getQuotePriceFormatter(payable)));
        mFragmentStrategicPartnerPhaseThreeBinding.textpromocodelabel.setEnabled(false);
        mFragmentStrategicPartnerPhaseThreeBinding.textpromocodelabel.setText(getString(R.string.label_promocode_apply));
        mFragmentStrategicPartnerPhaseThreeBinding.textpromocodelabel.setTextColor(ContextCompat.getColor(mStrategicPartnerTaskCreationAct, R.color.black));
        mFragmentStrategicPartnerPhaseThreeBinding.imgCheepCodeClose.setVisibility(View.GONE);
    }

    private SpannableStringBuilder getSpannableString(String string, int color, boolean isBold) {
        SpannableStringBuilder text = new SpannableStringBuilder(string);
        text.setSpan(new ForegroundColorSpan(color), 0, string.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (isBold) {
            text.setSpan(new StyleSpan(Typeface.BOLD), 0, string.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return text;
    }

    @Override
    public void setListener() {
        LogUtils.LOGD(TAG, "setListener() called");
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        BaseAppCompatActivity activity = (BaseAppCompatActivity) context;
        if (activity instanceof StrategicPartnerTaskCreationAct) {
            mStrategicPartnerTaskCreationAct = (StrategicPartnerTaskCreationAct) activity;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.FETCH_SUB_SERVICE_LIST);
        try {
            mContext.unregisterReceiver(mBR_OnLoginSuccess);
        } catch (Exception e) {
            LogUtils.LOGI(TAG, "onDestroy: ");
        }
    }

    /**
     * BroadCast that would restart the screen once login has been done.
     */
    private BroadcastReceiver mBR_OnLoginSuccess = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtils.LOGD(TAG, "onReceive() called with: context = [" + context + "], intent = [" + intent + "]");
            Utility.hideKeyboard(mContext);
            // Initiating the payment now

            // As User is currently logged in, we need to add FullAddressModel to existing addresslist.
            UserDetails mUserDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
            if (mUserDetails != null) {
                if (mUserDetails.addressList.isEmpty()) {
                    mUserDetails.addressList = new ArrayList<>();
                }

                // Add additional selected addressmodel here.
                mUserDetails.addressList.add(mStrategicPartnerTaskCreationAct.mSelectedAddressModel);

                // Save the user now.
                PreferenceUtility.getInstance(mContext).saveUserDetails(mUserDetails);
            }

//            payNow();
        }
    };

    //////////////////////////////////////////////////////////////////    Strategic partner task create [Start] ///////////////////////////////////////////////////////
    // TODO : commented on 4 nove as per pay later flow
    private void callCreateStrategicPartnerTaskWS() {

        // Check Internet connection
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mFragmentStrategicPartnerPhaseThreeBinding.getRoot());
            return;
        }

        ArrayList<MediaModel> mMediaModelList = new ArrayList<>();
        for (QueAnsModel model : mStrategicPartnerTaskCreationAct.getQuestionsList())
            if (model.answerType.equalsIgnoreCase(Utility.TEMPLATE_UPLOAD)) {
                mMediaModelList = model.medialList;
                break;
            }

        //Show Progress
        showProgressDialog();

        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();

        // Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, userDetails.UserID);
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());

        // Add Params
        ArrayList<QueAnsModel> mList = mStrategicPartnerTaskCreationAct.getQuestionsList();
        String subCategoryDetail = Utility.getSelectedServicesJsonString(mStrategicPartnerTaskCreationAct.getSelectedSubService());
        String task_desc = getTaskDescription(mStrategicPartnerTaskCreationAct.getQuestionsList());
        String question_detail = Utility.getQuestionAnswerDetailsJsonString(mList);
        String media_file = Utility.getSelectedMediaJsonString(mMediaModelList);
        LogUtils.LOGE(TAG, "start dat time " + start_datetime);
        SuperCalendar superCalendar = SuperCalendar.getInstance();
        superCalendar.setTimeInMillis(Long.parseLong(start_datetime));
        superCalendar.setTimeZone(SuperCalendar.SuperTimeZone.GMT.GMT);

        LogUtils.LOGE(TAG, "gmt time " + String.valueOf(superCalendar.getTimeInMillis()));
        LogUtils.LOGE(TAG, "Payment method type" + String.valueOf(superCalendar.getTimeInMillis()));

        Map<String, Object> mParams = new HashMap<>();
        if (mStrategicPartnerTaskCreationAct.mSelectedAddressModel != null)
            if (Integer.parseInt(mStrategicPartnerTaskCreationAct.mSelectedAddressModel.address_id) > 0) {
                mParams.put(NetworkUtility.TAGS.ADDRESS_ID, mStrategicPartnerTaskCreationAct.mSelectedAddressModel.address_id);
                mParams.put(NetworkUtility.TAGS.ADDRESS_ID, userDetails.CityID);
            } else {
                mParams = NetworkUtility.addGuestAddressParams(mParams, mStrategicPartnerTaskCreationAct.mSelectedAddressModel);
            }

        mParams.put(NetworkUtility.TAGS.CAT_ID, mStrategicPartnerTaskCreationAct.mBannerImageModel.cat_id);
        mParams.put(NetworkUtility.TAGS.START_DATETIME, String.valueOf(superCalendar.getTimeInMillis()));
        mParams.put(NetworkUtility.TAGS.SUB_CATEGORY_DETAIL, subCategoryDetail);
        mParams.put(NetworkUtility.TAGS.QUESTION_DETAIL, question_detail);
        mParams.put(NetworkUtility.TAGS.QUOTE_AMOUNT, mStrategicPartnerTaskCreationAct.totalOfBasePrice + "");

        mParams.put(NetworkUtility.TAGS.CHEEPCODE, TextUtils.isEmpty(cheepCode) ? Utility.EMPTY_STRING : cheepCode);
        mParams.put(NetworkUtility.TAGS.PAYABLE_AMOUNT, TextUtils.isEmpty(cheepCode) ? mStrategicPartnerTaskCreationAct.totalOfGSTPrice
                : payableAmount);
        mParams.put(NetworkUtility.TAGS.TASK_DESC, task_desc);
        mParams.put(NetworkUtility.TAGS.SP_USER_ID, mStrategicPartnerTaskCreationAct.spUserId);
        mParams.put(NetworkUtility.TAGS.PROMOCODE_PRICE, TextUtils.isEmpty(cheepCode) ? Utility.ZERO_STRING : promocode_price);
        // new amazon s3 uploaded file names
        mParams.put(NetworkUtility.TAGS.MEDIA_FILE, media_file);
        mParams.put(NetworkUtility.TAGS.PAYMENT_STATUS, Utility.PAYMENT_STATUS.COMPLETED);
        mParams.put(NetworkUtility.TAGS.PAYMENT_METHOD, NetworkUtility.PAYMENT_METHOD_TYPE.PAY_LATER);
        mParams.put(NetworkUtility.TAGS.PAYMENT_LOG, Utility.EMPTY_STRING);
        mParams.put(NetworkUtility.TAGS.TASK_TYPE, Utility.TASK_TYPE.STRATEGIC);
        mParams.put(NetworkUtility.TAGS.IS_REFER_CODE, Utility.BOOLEAN.NO);
        mParams.put(NetworkUtility.TAGS.USED_WALLET_BALANCE, Utility.ZERO_STRING);

        LogUtils.LOGE(TAG, "subCategoryDetail = [ " + subCategoryDetail + " ] ");
        LogUtils.LOGE(TAG, "question_detail = [ " + question_detail + " ] ");
        LogUtils.LOGE(TAG, "start_datetime = [ " + start_datetime + " ] ");
        LogUtils.LOGE(TAG, "total = [ " + mStrategicPartnerTaskCreationAct.totalOfBasePrice + " ] ");
        LogUtils.LOGE(TAG, "task_desc= [ " + task_desc + " ] ");
        LogUtils.LOGE(TAG, "media_file= [ " + media_file + " ] ");
        LogUtils.LOGE(TAG, "SP_USER_ID= [ " + mStrategicPartnerTaskCreationAct.spUserId + " ] ");
        LogUtils.LOGE(TAG, "cat_id = [ " + mStrategicPartnerTaskCreationAct.mBannerImageModel.cat_id + " ] ");

        // Create Params for AppsFlyer event track
        mTaskCreationParams = new HashMap<>();
        if (mStrategicPartnerTaskCreationAct.mSelectedAddressModel != null)
            if (Integer.parseInt(mStrategicPartnerTaskCreationAct.mSelectedAddressModel.address_id) > 0) {
                mTaskCreationParams.put(NetworkUtility.TAGS.ADDRESS_ID, mStrategicPartnerTaskCreationAct.mSelectedAddressModel.address_id);
            } else {
                // In case its Nagative then provide other address information
            /*
             public String address_initials;
             public String address;
             public String category; //comes from NetworkUtility.TAGS.ADDRESS_TYPE.
             public String lat;
             public String lng;
             */
                mTaskCreationParams = NetworkUtility.addGuestAddressParams(mTaskCreationParams, mStrategicPartnerTaskCreationAct.mSelectedAddressModel);
            }
        mTaskCreationParams.put(NetworkUtility.TAGS.CAT_ID, mStrategicPartnerTaskCreationAct.mBannerImageModel.cat_id);
        mTaskCreationParams.put(NetworkUtility.TAGS.START_DATETIME, start_datetime);
        mTaskCreationParams.put(NetworkUtility.TAGS.SUB_CATEGORY_DETAIL, subCategoryDetail);
        mTaskCreationParams.put(NetworkUtility.TAGS.QUESTION_DETAIL, question_detail);
        mTaskCreationParams.put(NetworkUtility.TAGS.QUOTE_AMOUNT, mStrategicPartnerTaskCreationAct.totalOfBasePrice + "");
        mTaskCreationParams.put(NetworkUtility.TAGS.PAYABLE_AMOUNT, TextUtils.isEmpty(cheepCode) ? mStrategicPartnerTaskCreationAct.totalOfGSTPrice
                : payableAmount);
        mTaskCreationParams.put(NetworkUtility.TAGS.MEDIA_FILE, media_file);
        mTaskCreationParams.put(NetworkUtility.TAGS.SP_USER_ID, mStrategicPartnerTaskCreationAct.spUserId);
        mTaskCreationParams.put(NetworkUtility.TAGS.CHEEPCODE, cheepCode);
        mTaskCreationParams.put(NetworkUtility.TAGS.PROMOCODE_PRICE, TextUtils.isEmpty(cheepCode) ? Utility.ZERO_STRING : promocode_price);
        mTaskCreationParams.put(NetworkUtility.TAGS.PAYMENT_LOG, Utility.EMPTY_STRING);
        mTaskCreationParams.put(NetworkUtility.TAGS.PAYMENT_METHOD, NetworkUtility.PAYMENT_METHOD_TYPE.PAY_LATER);
        mTaskCreationParams.put(NetworkUtility.TAGS.PAYMENT_STATUS, Utility.PAYMENT_STATUS.COMPLETED);


        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.CREATE_TASK
                , mCallCreateTaskWSErrorListener
                , mCallCreateTaskWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest);
    }


    /**
     * Create Dialog which would going to show on error completion
     */
    Response.ErrorListener mCallCreateTaskWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            LogUtils.LOGD(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            hideProgressDialog();

            // Show Toast
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mFragmentStrategicPartnerPhaseThreeBinding.getRoot());
        }
    };


    Response.Listener mCallCreateTaskWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {

            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                LogUtils.LOGI(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:

                        // Send Event tracking for AppsFlyer
                        AppsFlyerLib.getInstance().trackEvent(mContext, NetworkUtility.TAGS.APPSFLYER_CUSTOM_TRACK_EVENTS.TASK_CREATE, mTaskCreationParams);

                        /*
                          Now according to the new flow, once task created
                          app will be redirected to MyTask Detail screen.
                         */
//                        TODO:This needs to be updated.
//                        onSuccessfullTaskCreated(jsonObject);
//                        Utility.showToast(mContext, "Task Created Successfully!!");

                        /*String title = "Brilliant";*/

                        SuperCalendar superCalendar = SuperCalendar.getInstance();
                        superCalendar.setTimeInMillis(Long.parseLong(start_datetime));
                        String time = superCalendar.format(Utility.DATE_FORMAT_HH_MM_AM);
                        String date = superCalendar.format(Utility.DATE_FORMAT_DD_MMM_YYYY);

                        String message = getString(R.string.label_strategic_task_confirmed, mStrategicPartnerTaskCreationAct.mBannerImageModel.name) +
                                date + getString(R.string.label_at) + time;

                        final AcknowledgementDialogWithProfilePic mAcknowledgementDialogWithProfilePic = AcknowledgementDialogWithProfilePic.newInstance(
                                mContext,
                                R.drawable.ic_acknowledgement_dialog_header_background,
                                getString(R.string.label_brilliant),
                                message,
                                mStrategicPartnerTaskCreationAct.mBannerImageModel.imgCatImageUrl,
                                new AcknowledgementInteractionListener() {

                                    @Override
                                    public void onAcknowledgementAccepted() {
                                        // Finish the activity
                                        mStrategicPartnerTaskCreationAct.finish();


                                        // Payment is been done now, so broadcast this specific case to relavent activities
                                        MessageEvent messageEvent = new MessageEvent();
                                        messageEvent.BROADCAST_ACTION = Utility.BROADCAST_TYPE.PAYMENT_COMPLETED_NEED_TO_REDIRECT_TO_MY_TASK_SCREEN;
                                        EventBus.getDefault().post(messageEvent);
                                    }
                                }, -1);
                        mAcknowledgementDialogWithProfilePic.setCancelable(false);
                        mAcknowledgementDialogWithProfilePic.show(mStrategicPartnerTaskCreationAct.getSupportFragmentManager(), AcknowledgementDialogWithProfilePic.TAG);


                        // Finish the current activity
//                        mStrategicPartnerTaskCreationAct.finish();

                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mFragmentStrategicPartnerPhaseThreeBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        String error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mFragmentStrategicPartnerPhaseThreeBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.USER_DELETED:
                    case NetworkUtility.TAGS.STATUSCODETYPE.FORCE_LOGOUT_REQUIRED:
                        //Logout and finish the current activity
                        Utility.logout(mContext, true, statusCode);
                        mStrategicPartnerTaskCreationAct.finish();
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mCallCreateTaskWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
            hideProgressDialog();
        }
    };

    private String getTaskDescription(ArrayList<QueAnsModel> mList) {
        for (int i = 0; i < mList.size(); i++) {
            QueAnsModel queAnsModel = mList.get(i);
            if (queAnsModel.answerType.equalsIgnoreCase(Utility.TEMPLATE_TEXT_FIELD)) {
                return queAnsModel.answer == null || queAnsModel.answer.equalsIgnoreCase("") ? "" : queAnsModel.answer;
            }
        }
        return "";
    }


}
