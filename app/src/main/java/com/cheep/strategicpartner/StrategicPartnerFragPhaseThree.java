package com.cheep.strategicpartner;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.appsflyer.AppsFlyerLib;
import com.cheep.BootstrapConstant;
import com.cheep.BuildConfig;
import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.activity.PaymentsActivity;
import com.cheep.custom_view.BottomAlertDialog;
import com.cheep.databinding.FragmentStrategicPartnerPhaseThreeBinding;
import com.cheep.dialogs.AcknowledgementDialogWithProfilePic;
import com.cheep.dialogs.AcknowledgementDialogWithoutProfilePic;
import com.cheep.dialogs.AcknowledgementInteractionListener;
import com.cheep.firebase.FirebaseHelper;
import com.cheep.firebase.FirebaseUtils;
import com.cheep.firebase.model.ChatTaskModel;
import com.cheep.fragment.BaseFragment;
import com.cheep.model.MessageEvent;
import com.cheep.model.TaskDetailModel;
import com.cheep.model.UserDetails;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
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
    private String addressId = "";
    private String payableAmount;
    private String start_datetime = "";
    private EditText edtCheepCode;
    private BottomAlertDialog cheepCodeDialog;
    private boolean isVerified = false;
    @Nullable
    private String cheepCode;

    // After Posting Task, this would hold the details for AppsFlyer
    Map<String, Object> mTaskCreationParams;

    @SuppressWarnings("unused")
    public static StrategicPartnerFragPhaseThree newInstance() {
        return new StrategicPartnerFragPhaseThree();
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
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.d(TAG, "setUserVisibleHint() called with: isVisibleToUser = [" + isVisibleToUser + "]");
        if (!isVisibleToUser || mStrategicPartnerTaskCreationAct == null) {
            return;
        }
        mStrategicPartnerTaskCreationAct.setTaskState(
                isVerified ?
                        StrategicPartnerTaskCreationAct.STEP_THREE_VERIFIED :
                        StrategicPartnerTaskCreationAct.STEP_THREE_NORMAL);


        // force to scroll up the view for strategic partner logo
        mFragmentStrategicPartnerPhaseThreeBinding.scrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mFragmentStrategicPartnerPhaseThreeBinding.scrollView.fullScroll(View.FOCUS_UP);
            }
        }, 5);

        // get date time and selected address from Questions (phase 2)
        for (int i = 0; i < mStrategicPartnerTaskCreationAct.getQuestionsList().size(); i++) {
            QueAnsModel queAnsModel = mStrategicPartnerTaskCreationAct.getQuestionsList().get(i);
            if (queAnsModel.answerType.equalsIgnoreCase(Utility.TEMPLATE_TIME_PICKER)) {
                start_datetime = queAnsModel.answer;
            }
            if (queAnsModel.answerType.equalsIgnoreCase(Utility.TEMPLATE_LOCATION)) {
                addressId = queAnsModel.answer;
            }
        }
        // set details of partner name user selected date time and address
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(getSpannableString("Your order with ", ContextCompat.getColor(mStrategicPartnerTaskCreationAct, R.color.grey_varient_8), false));
        spannableStringBuilder.append(getSpannableString(mStrategicPartnerTaskCreationAct.mBannerImageModel.name, ContextCompat.getColor(mStrategicPartnerTaskCreationAct, R.color.splash_gradient_end), true));
        spannableStringBuilder.append(getSpannableString(getString(R.string.label_on), ContextCompat.getColor(mStrategicPartnerTaskCreationAct, R.color.grey_varient_8), false));
        spannableStringBuilder.append(getSpannableString(mStrategicPartnerTaskCreationAct.date + ", " + mStrategicPartnerTaskCreationAct.time
                , ContextCompat.getColor(mStrategicPartnerTaskCreationAct, R.color.splash_gradient_end), true));
        spannableStringBuilder.append(getSpannableString(getString(R.string.label_at), ContextCompat.getColor(mStrategicPartnerTaskCreationAct, R.color.grey_varient_8), false));
        spannableStringBuilder.append(getSpannableString(mStrategicPartnerTaskCreationAct.address, ContextCompat.getColor(mStrategicPartnerTaskCreationAct, R.color.splash_gradient_end), true));
        spannableStringBuilder.append(getSpannableString(".", ContextCompat.getColor(mStrategicPartnerTaskCreationAct, R.color.splash_gradient_end), true));

        mFragmentStrategicPartnerPhaseThreeBinding.txtdesc.setText(spannableStringBuilder);


        // get list of selected services from phase 1
        if (mStrategicPartnerTaskCreationAct.getSelectedSubService() != null)
            mFragmentStrategicPartnerPhaseThreeBinding.recycleSelectedService.setAdapter(new PaymentSummaryAdapter(mStrategicPartnerTaskCreationAct.getSelectedSubService()));

        // set total and sub total details
        mFragmentStrategicPartnerPhaseThreeBinding.txttotal.setText(getString(R.string.ruppe_symbol_x, "" + Utility.getQuotePriceInInteger(mStrategicPartnerTaskCreationAct.total)));
        mFragmentStrategicPartnerPhaseThreeBinding.txtsubtotal.setText(getString(R.string.ruppe_symbol_x, "" + Utility.getQuotePriceInInteger(mStrategicPartnerTaskCreationAct.total)));
        mFragmentStrategicPartnerPhaseThreeBinding.textPay.setText("Pay " + getString(R.string.ruppe_symbol_x, String.valueOf(Utility.getQuotePriceInInteger(mStrategicPartnerTaskCreationAct.total))));

        // handle clicks for create task web api and payment flow
        mFragmentStrategicPartnerPhaseThreeBinding.textPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Initiating the payment now
                payNow();
            }
        });


        // Enter promo code UI
        mFragmentStrategicPartnerPhaseThreeBinding.textpromocodelabel.setTextColor(ContextCompat.getColor(mStrategicPartnerTaskCreationAct, R.color.splash_gradient_end));
        mFragmentStrategicPartnerPhaseThreeBinding.textpromocodelabel.setText(getResources().getString(R.string.label_enter_promocode));

        mFragmentStrategicPartnerPhaseThreeBinding.textpromocodelabel.setEnabled(true);
        mFragmentStrategicPartnerPhaseThreeBinding.textpromocodelabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCheepCodeDialog();
            }
        });
        mFragmentStrategicPartnerPhaseThreeBinding.imgCheepCodeClose.setVisibility(View.GONE);
        mFragmentStrategicPartnerPhaseThreeBinding.txtpromocode.setText(getString(R.string.ruppe_symbol_x, "" + 0.0));
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
    }


    /**
     * clear promo code details and set original total values
     */
    private void resetPromoCodeValue() {
        mFragmentStrategicPartnerPhaseThreeBinding.textpromocodelabel.setTextColor(ContextCompat.getColor(mStrategicPartnerTaskCreationAct, R.color.splash_gradient_end));
        mFragmentStrategicPartnerPhaseThreeBinding.textpromocodelabel.setText(getResources().getString(R.string.label_enter_promocode));
        mFragmentStrategicPartnerPhaseThreeBinding.txtsubtotal.setText(getString(R.string.ruppe_symbol_x, "" + Utility.getQuotePriceInInteger(mStrategicPartnerTaskCreationAct.total)));
        mFragmentStrategicPartnerPhaseThreeBinding.txttotal.setText(getString(R.string.ruppe_symbol_x, "" + Utility.getQuotePriceInInteger(mStrategicPartnerTaskCreationAct.total)));
        mFragmentStrategicPartnerPhaseThreeBinding.textPay.setText(getString(R.string.label_pay_fee_v1, "" + Utility.getQuotePriceInInteger(mStrategicPartnerTaskCreationAct.total)));
        mFragmentStrategicPartnerPhaseThreeBinding.txtpromocode.setText(getString(R.string.ruppe_symbol_x, "" + Utility.getQuotePriceInInteger("0")));
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
        cheepCodeDialog.setTitle(getString(R.string.label_cheepcode));
        cheepCodeDialog.setCustomView(view);
        cheepCodeDialog.showDialog();
    }


    private void validateCheepCode(String s) {
        cheepCode = s;
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(getString(R.string.no_internet), mFragmentStrategicPartnerPhaseThreeBinding.getRoot());
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
        mParams.put(NetworkUtility.TAGS.QUOTE_AMOUNT, mStrategicPartnerTaskCreationAct.total);
        mParams.put(NetworkUtility.TAGS.CHEEPCODE, cheepCode);
        mParams.put(NetworkUtility.TAGS.CAT_ID, mStrategicPartnerTaskCreationAct.mBannerImageModel.cat_id);
        mParams.put(NetworkUtility.TAGS.ADDRESS_ID, addressId);

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
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;
                hideProgressDialog();
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:

                        if (edtCheepCode != null) {
                            cheepCode = edtCheepCode.getText().toString().trim();
                            cheepCodeDialog.dismiss();

                            mStrategicPartnerTaskCreationAct.total = jsonObject.optString(NetworkUtility.TAGS.QUOTE_AMOUNT);

                            String discount = jsonObject.optString(NetworkUtility.TAGS.DISCOUNT_AMOUNT);
                            String payable = jsonObject.optString(NetworkUtility.TAGS.PAYABLE_AMOUNT);
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
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");
            Utility.showToast(mContext, getString(R.string.label_something_went_wrong));

        }

    };


    private void updatePaymentDetails(String discount, String payable) {
        payableAmount = payable;
        mFragmentStrategicPartnerPhaseThreeBinding.txtpromocode.setText(getString(R.string.ruppe_symbol_x, "" + Utility.getQuotePriceInInteger(discount)));
        mFragmentStrategicPartnerPhaseThreeBinding.txttotal.setText(getString(R.string.ruppe_symbol_x, "" + Utility.getQuotePriceInInteger(payable)));
        mFragmentStrategicPartnerPhaseThreeBinding.textPay.setText(getString(R.string.label_pay_fee_v1, "" + Utility.getQuotePriceInInteger(payable)));
        mFragmentStrategicPartnerPhaseThreeBinding.textpromocodelabel.setEnabled(false);
        mFragmentStrategicPartnerPhaseThreeBinding.textpromocodelabel.setText(cheepCode);
        mFragmentStrategicPartnerPhaseThreeBinding.imgCheepCodeClose.setVisibility(View.VISIBLE);
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
    public void initiateUI() {
        Log.d(TAG, "initiateUI() called");

        mFragmentStrategicPartnerPhaseThreeBinding.recycleSelectedService.setLayoutManager(new LinearLayoutManager(mStrategicPartnerTaskCreationAct));
        if (mStrategicPartnerTaskCreationAct.getSelectedSubService() != null)
            mFragmentStrategicPartnerPhaseThreeBinding.recycleSelectedService.setAdapter(new PaymentSummaryAdapter(mStrategicPartnerTaskCreationAct.getSelectedSubService()));
    }

    @Override
    public void setListener() {
        Log.d(TAG, "setListener() called");
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
    }


    /***********************************************************************************************
     *********************** [Generate Hash Code For Payment] [Start] *************************************
     ***********************************************************************************************/
    /**
     * Used for payment
     */
    private void payNow() {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(getString(R.string.no_internet), mFragmentStrategicPartnerPhaseThreeBinding.getRoot());
            return;
        }

        showProgressDialog();

        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();

        //Add Params
        Map<String, Object> mParams;// = new HashMap<String, Object>();
        mParams = getPaymentTransactionFields(userDetails);

        // We do not need to pass PROID and TaskID in Strategicpartner as it still not finalized
//        mParams.put(NetworkUtility.TAGS.SP_USER_ID, providerModel.providerId);
//        mParams.put(NetworkUtility.TAGS.TASK_ID, taskDetailModel.taskId);

        if (!TextUtils.isEmpty(cheepCode))
            mParams.put(NetworkUtility.TAGS.CHEEPCODE, cheepCode);
        else
            mParams.put(NetworkUtility.TAGS.CHEEPCODE, Utility.EMPTY_STRING);

        //Create Asynctask that will do the encryption and afterwords call webservice
        AsyncFetchEnryptedString asyncFetchEnryptedString = new AsyncFetchEnryptedString();
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
    @SuppressWarnings("unchecked")
    private class AsyncFetchEnryptedString extends AsyncTask<String, Void, String> {

        public AsyncFetchEnryptedString() {
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
            getPaymentUrl();

            //Url is based on condition if address id is greater then 0 then it means we need to update the existing address
            VolleyNetworkRequest mVolleyNetworkRequestForSPList = new VolleyNetworkRequest(NetworkUtility.WS.GET_PAYMENT_HASH_FOR_STRATEGIC_PARTNER
                    , mCallPaymentWSErrorListener
                    , mCallPaymentWSResponseListener
                    , mHeaderParams
                    , mFinalParams
                    , null);
            Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForSPList);
        }
    }

    // Constants
    public static final String TXN_ID = "txnid";
    public static final String DEVICE_TYPE = "device_type";
    public static final String ISMOBILEVIEW = "ismobileview";
    public static final String PRODUCTINFO = "productinfo";
    public static final String USER_CREDENTIALS = "user_credentials";
    public static final String KEY = "key";
    public static final String INSTRUMENT_TYPE = "instrument_type";
    public static final String SURL = "surl";
    public static final String FURL = "furl";
    public static final String INSTRUMENT_ID = "instrument_id";
    public static final String FIRSTNAME = "firstname";
    public static final String EMAIL = "email";
    public static final String PHONE = "phone";
    public static final String AMOUNT = "amount";
    public static final String UDF1 = "udf1";
    public static final String UDF2 = "udf2";
    public static final String UDF3 = "udf3";
    public static final String UDF4 = "udf4";
    public static final String UDF5 = "udf5";
    public static final String HASH = "hash";

    StringBuilder postData = new StringBuilder();

    private String getPaymentUrl() {
        for (String key : mTransactionFieldsParams.keySet()) {
            postData = postData.append("&").append(key).append("=").append(mTransactionFieldsParams.get(key));
        }
        Log.d(TAG, "getPaymentUrl() returned: " + postData.toString());
        return postData.toString();
    }

    String transaction_Id;
    HashMap<String, Object> mTransactionFieldsParams;

    private Map<String, Object> getPaymentTransactionFields(UserDetails userDetails) {

        mTransactionFieldsParams = new HashMap<>();
        // Create Unique Transaction ID
        transaction_Id = Utility.getUniqueTransactionId();

        mTransactionFieldsParams.put(TXN_ID, transaction_Id);
        mTransactionFieldsParams.put(DEVICE_TYPE, "1");
        mTransactionFieldsParams.put(ISMOBILEVIEW, "1");
        mTransactionFieldsParams.put(PRODUCTINFO, userDetails.UserID);
        mTransactionFieldsParams.put(USER_CREDENTIALS, BuildConfig.PAYUBIZ_HDFC_KEY + ":" + userDetails.Email);
        mTransactionFieldsParams.put(KEY, BuildConfig.PAYUBIZ_HDFC_KEY);
        mTransactionFieldsParams.put(INSTRUMENT_TYPE, PreferenceUtility.getInstance(mContext).getFCMRegID());
        mTransactionFieldsParams.put(SURL, BuildConfig.PAYUBIZ_SUCCESS_URL);
        mTransactionFieldsParams.put(FURL, BuildConfig.PAYUBIZ_FAIL_URL);
        mTransactionFieldsParams.put(INSTRUMENT_ID, "7dd17561243c202");

        // User Details
        mTransactionFieldsParams.put(FIRSTNAME, userDetails.UserName);
        mTransactionFieldsParams.put(EMAIL, userDetails.Email);
        mTransactionFieldsParams.put(PHONE, userDetails.PhoneNumber);
        // Total Amount
        mTransactionFieldsParams.put(AMOUNT, TextUtils.isEmpty(cheepCode) ? mStrategicPartnerTaskCreationAct.total : payableAmount);
        // Start DateTime(In Milliseconds- Timestamp)
        mTransactionFieldsParams.put(UDF1, "Task Start Date : " + start_datetime);
        // We don't have Provider ID so pass it empty.
        mTransactionFieldsParams.put(UDF2, Utility.EMPTY_STRING);

        // Platform
        mTransactionFieldsParams.put(UDF3, NetworkUtility.TAGS.PLATFORMTYPE.ANDROID);

        mTransactionFieldsParams.put(UDF4, Utility.EMPTY_STRING);
        mTransactionFieldsParams.put(UDF5, Utility.EMPTY_STRING);
        mTransactionFieldsParams.put(HASH, Utility.EMPTY_STRING);
        return mTransactionFieldsParams;
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
                            callTaskCreationWebServiceForStratgicPartner(true, "Payment has been bypassed for development");
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
                            Intent intent = new Intent(mStrategicPartnerTaskCreationAct, PaymentsActivity.class);
                            intent.putExtra("url", BuildConfig.PAYUBIZ_HDFC_URL);
                            intent.putExtra("postData", postData.toString().replaceAll("hash=", "hash=" + jsonObject.optString("hash_string")));
                            startActivityForResult(intent, Utility.REQUEST_START_PAYMENT);
                        }
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mFragmentStrategicPartnerPhaseThreeBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
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
                mCallPaymentWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
        }
    };

    Response.ErrorListener mCallPaymentWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(final VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
//            hideProgressDialog();

            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mFragmentStrategicPartnerPhaseThreeBinding.getRoot());

        }
    };

    /***********************************************************************************************
     *********************** [Generate Hash Code For Payment] [End] *************************************
     ***********************************************************************************************/

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Utility.REQUEST_START_PAYMENT) {
//            Toast.makeText(mContext, "OnActivityResult called with resultCode:" + resultCode + ", requestCode:" + requestCode, Toast.LENGTH_SHORT).show();
            if (resultCode == mStrategicPartnerTaskCreationAct.RESULT_OK) {
                mStrategicPartnerTaskCreationAct.setTaskState(mStrategicPartnerTaskCreationAct.STEP_THREE_VERIFIED);
                //success
                if (data != null) {
                    Log.d(TAG, "onActivityResult() called with success: result= [" + data.getStringExtra("result") + "]");
                    //Call update payment service from here with all the response come from service
                    callTaskCreationWebServiceForStratgicPartner(true, data.getStringExtra("result"));
                }
            }
            if (resultCode == mStrategicPartnerTaskCreationAct.RESULT_CANCELED) {
                mStrategicPartnerTaskCreationAct.setTaskState(mStrategicPartnerTaskCreationAct.STEP_THREE_UNVERIFIED);
                //failed
                if (data != null) {
                    Log.d(TAG, "onActivityResult() called with failed: result= [" + data.getStringExtra("result") + "]");
                    //Call update payment service from here with all the response come from service
                    callTaskCreationWebServiceForStratgicPartner(false, data.getStringExtra("result"));
                    Utility.showSnackBar(getString(R.string.msg_payment_failed), mFragmentStrategicPartnerPhaseThreeBinding.getRoot());
                }
            }
        }
    }

    /***********************************************************************************************
     *********************** [Task Creation API Call ] [Start] ******************************
     **********************************************************************************************/

    /**
     * @param isPaymentSuccess
     * @param paymentGatewaySummary
     */
    @SuppressWarnings("unchecked")
    private void callTaskCreationWebServiceForStratgicPartner(boolean isPaymentSuccess, String paymentGatewaySummary) {

        // Check Internet connection
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(getString(R.string.no_internet), mFragmentStrategicPartnerPhaseThreeBinding.getRoot());
            return;
        }

        //Show Progress
        showProgressDialog();

        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();

        // Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, userDetails.UserID);
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());

        // Add Params
        String subCategoryDetail = getSelectedServicesJsonString().toString();


        ArrayList<QueAnsModel> mList = mStrategicPartnerTaskCreationAct.getQuestionsList();
        String question_detail = getQuestionAnswerDetailsJsonString(mList).toString();
        Map<String, String> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.ADDRESS_ID, addressId);
        mParams.put(NetworkUtility.TAGS.CAT_ID, mStrategicPartnerTaskCreationAct.mBannerImageModel.cat_id);
        mParams.put(NetworkUtility.TAGS.START_DATETIME, start_datetime);
        mParams.put(NetworkUtility.TAGS.SUB_CATEGORY_DETAIL, subCategoryDetail);
        mParams.put(NetworkUtility.TAGS.QUESTION_DETAIL, question_detail);
        mParams.put(NetworkUtility.TAGS.QUOTE_AMOUNT, mStrategicPartnerTaskCreationAct.total);

        mParams.put(NetworkUtility.TAGS.CHEEPCODE, TextUtils.isEmpty(cheepCode) ? Utility.EMPTY_STRING : cheepCode);
        mParams.put(NetworkUtility.TAGS.PAYABLE_AMOUNT, TextUtils.isEmpty(cheepCode) ? mStrategicPartnerTaskCreationAct.total : payableAmount);
        mParams.put(NetworkUtility.TAGS.TRANSACTION_ID, transaction_Id);


        // Create Params for AppsFlyer event track
        mTaskCreationParams = new HashMap<>();
        mTaskCreationParams.put(NetworkUtility.TAGS.ADDRESS_ID, addressId);
        mTaskCreationParams.put(NetworkUtility.TAGS.CAT_ID, mStrategicPartnerTaskCreationAct.mBannerImageModel.cat_id);
        mTaskCreationParams.put(NetworkUtility.TAGS.START_DATETIME, start_datetime);
        mTaskCreationParams.put(NetworkUtility.TAGS.SUB_CATEGORY_DETAIL, subCategoryDetail);
        mTaskCreationParams.put(NetworkUtility.TAGS.QUESTION_DETAIL, question_detail);
        mTaskCreationParams.put(NetworkUtility.TAGS.QUOTE_AMOUNT, mStrategicPartnerTaskCreationAct.total + "");
        mTaskCreationParams.put(NetworkUtility.TAGS.CHEEPCODE, cheepCode);
        mTaskCreationParams.put(NetworkUtility.TAGS.PAYABLE_AMOUNT, payableAmount);
        mTaskCreationParams.put(NetworkUtility.TAGS.TRANSACTION_ID, transaction_Id);

        // Add Params
        HashMap<String, File> mFileParams = new HashMap<>();
        // TODO : This needs to be improved after words.

        ArrayList<MediaModel> mFileList = null;
        for (QueAnsModel model : mList)
            if (model.answerType.equalsIgnoreCase(Utility.TEMPLATE_UPLOAD)) {
                mFileList = model.medialList;
                break;
            }

        if (mFileList != null && !mFileList.isEmpty())
            for (int i = 0; i < mFileList.size(); i++) {
                MediaModel mediaModel = mFileList.get(i);
                if (!TextUtils.isEmpty(mediaModel.path) && new File(mediaModel.path).exists()) {
                    Log.e(TAG, "callTaskCreationWebServiceForStratgicPartner: path " + mediaModel.path + "");
                    mFileParams.put("media_file[" + i + "]", new File(mediaModel.path));
                }
            }

        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.TASK_CREATE_STRATEGIC_PARTNER
                , mCallCreateTaskWSErrorListener
                , mCallCreateTaskWSResponseListener
                , mHeaderParams
                , mParams
                , mFileParams);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest);
    }

    Response.Listener mCallCreateTaskWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {

            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
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

                        String title = mContext.getString(R.string.label_great_choice_x, PreferenceUtility.getInstance(mContext).getUserDetails().UserName);
                        String message = "Your task is confirmed and a PRO from " + mStrategicPartnerTaskCreationAct.mBannerImageModel.name + ", will be there at your location on " +
                                mStrategicPartnerTaskCreationAct.date + " at " + mStrategicPartnerTaskCreationAct.time;

                        final AcknowledgementDialogWithProfilePic mAcknowledgementDialogWithProfilePic = AcknowledgementDialogWithProfilePic.newInstance(
                                mContext,
                                R.drawable.ic_acknowledgement_dialog_header_background,
                                title,
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
                                });
                        mAcknowledgementDialogWithProfilePic.setCancelable(false);
                        mAcknowledgementDialogWithProfilePic.show(mStrategicPartnerTaskCreationAct.getSupportFragmentManager(), AcknowledgementDialogWithProfilePic.TAG);


                        // Finish the current activity
//                        mStrategicPartnerTaskCreationAct.finish();

                        //Sending Broadcast to the HomeScreen Screen.
                        Intent intent = new Intent(Utility.BR_ON_TASK_CREATED_FOR_STRATEGIC_PARTNER);
                        mStrategicPartnerTaskCreationAct.sendBroadcast(intent);

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

    /**
     * This method would going to call when task created successfully
     */
    private void onSuccessfullTaskCreated(JSONObject jsonObject) {
        TaskDetailModel taskDetailModel = (TaskDetailModel) Utility.getObjectFromJsonString(jsonObject.optString(NetworkUtility.TAGS.DATA), TaskDetailModel.class);
        if (taskDetailModel != null) {
            /* * Add new task detail on firebase
             * @Sanjay 20 Feb 2016
             */
            ChatTaskModel chatTaskModel = new ChatTaskModel();
            chatTaskModel.taskId = FirebaseUtils.getPrefixTaskId(taskDetailModel.taskId);
            chatTaskModel.taskDesc = taskDetailModel.taskDesc;
            chatTaskModel.categoryId = taskDetailModel.categoryId;
            chatTaskModel.categoryName = taskDetailModel.categoryName;
            chatTaskModel.selectedSPId = "";
            UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
            chatTaskModel.userId = FirebaseUtils.getPrefixUserId(userDetails.UserID);
            FirebaseHelper.getTaskRef(chatTaskModel.taskId).setValue(chatTaskModel);
        }

        // Update the name of User
       /* mDialogFragmentTaskCreationBinding.textTaskCreationAcknowledgment
                .setText(mDialogFragmentTaskCreationBinding.getRoot().getContext().getString(R.string.desc_task_creation_acknowledgement, mUserName));
        */
        String message = mContext.getString(R.string.desc_task_creation_acknowledgement
                , PreferenceUtility.getInstance(mContext).getUserDetails().UserName);
        String title = mContext.getString(R.string.label_your_task_is_posted);
        AcknowledgementDialogWithoutProfilePic mAcknowledgementDialogWithoutProfilePic = AcknowledgementDialogWithoutProfilePic.newInstance(R.drawable.ic_bird_with_heart_illustration, title, message, new AcknowledgementInteractionListener() {

            @Override
            public void onAcknowledgementAccepted() {
                // Finish the current activity
                mStrategicPartnerTaskCreationAct.finish();

                //Sending Broadcast to the HomeScreen Screen.
                Intent intent = new Intent(Utility.BR_ON_TASK_CREATED_FOR_STRATEGIC_PARTNER);
                mStrategicPartnerTaskCreationAct.sendBroadcast(intent);
            }
        });
        mAcknowledgementDialogWithoutProfilePic.setCancelable(false);
        mAcknowledgementDialogWithoutProfilePic.show(getActivity().getSupportFragmentManager(), AcknowledgementDialogWithoutProfilePic.TAG);
    }


    /**
     * Create Dialog which would going to show on error completion
     */
    Response.ErrorListener mCallCreateTaskWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            hideProgressDialog();

            // Show Toast
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mFragmentStrategicPartnerPhaseThreeBinding.getRoot());
        }
    };

    private JsonArray getQuestionAnswerDetailsJsonString(ArrayList<QueAnsModel> mList) {
        JsonArray quesArray = new JsonArray();
        for (int i = 0; i < mList.size(); i++) {
            QueAnsModel queAnsModel = mList.get(i);
            if (!queAnsModel.answerType.equalsIgnoreCase(Utility.TEMPLATE_DATE_PICKER)
                    && !queAnsModel.answerType.equalsIgnoreCase(Utility.TEMPLATE_TIME_PICKER)
                    && !queAnsModel.answerType.equalsIgnoreCase(Utility.TEMPLATE_LOCATION)
                    && !queAnsModel.answerType.equalsIgnoreCase(Utility.TEMPLATE_UPLOAD)) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("question_id", queAnsModel.questionId);
                if (queAnsModel.answer != null)
                    jsonObject.addProperty("answer", queAnsModel.answer);
                else
                    jsonObject.addProperty("answer", Utility.EMPTY_STRING);
                quesArray.add(jsonObject);
            }
        }
        return quesArray;
    }

    private JsonArray getSelectedServicesJsonString() {
        JsonArray selectedServiceArray = new JsonArray();
        ArrayList<StrategicPartnerServiceModel> list = mStrategicPartnerTaskCreationAct.getSelectedSubService();
        for (int i = 0; i < list.size(); i++) {
            StrategicPartnerServiceModel model = list.get(i);
            for (int j = 0; j < model.allSubSubCats.size(); j++) {
                StrategicPartnerServiceModel.AllSubSubCat allSubSubCat = model.allSubSubCats.get(j);
                JsonObject obj = new JsonObject();
                obj.addProperty("subcategory_id", model.catId);
                obj.addProperty("sub_sub_cat_id", allSubSubCat.subSubCatId);
                obj.addProperty("price", allSubSubCat.price);
                selectedServiceArray.add(obj);
            }
        }
        return selectedServiceArray;
    }

    /***********************************************************************************************
     *********************** [Task Creation API Call ] [End] ********************************
     ***********************************************************************************************/


}
