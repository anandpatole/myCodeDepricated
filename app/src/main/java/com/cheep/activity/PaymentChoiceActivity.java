package com.cheep.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.appsflyer.AppsFlyerLib;
import com.cheep.BootstrapConstant;
import com.cheep.BuildConfig;
import com.cheep.R;
import com.cheep.databinding.ActivityPaymentChoiceBinding;
import com.cheep.dialogs.AcknowledgementDialogWithProfilePic;
import com.cheep.dialogs.AcknowledgementInteractionListener;
import com.cheep.firebase.FirebaseHelper;
import com.cheep.firebase.FirebaseUtils;
import com.cheep.firebase.model.ChatTaskModel;
import com.cheep.firebase.model.TaskChatModel;
import com.cheep.fragment.BaseFragment;
import com.cheep.model.AddressModel;
import com.cheep.model.MessageEvent;
import com.cheep.model.ProviderModel;
import com.cheep.model.TaskDetailModel;
import com.cheep.model.UserDetails;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.HDFCPaymentUtility;
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

import java.util.HashMap;
import java.util.Map;


public class PaymentChoiceActivity extends BaseAppCompatActivity {

    private ActivityPaymentChoiceBinding mActivityPaymentChoiceBinding;
    private static final String TAG = "PaymentChoiceActivity";
    private ProviderModel providerModel;
    private TaskDetailModel taskDetailModel;
    private boolean isInstaBooking = false;
    private boolean isStrategicPartner = false;
    private int isAdditional;
    private AddressModel mSelectedAddressModelForInsta;
    Map<String, Object> mTaskCreationParams;
    Map<String, String> mTransactionParams;

    public static void newInstance(Context context, TaskDetailModel taskDetailModel, ProviderModel providerModel, int isAdditionalPayment, boolean isInstaBooking, AddressModel mSelectedAddressModel) {
        Intent intent = new Intent(context, PaymentChoiceActivity.class);
        intent.putExtra(Utility.Extra.DATA, Utility.getJsonStringFromObject(providerModel));
        intent.putExtra(Utility.Extra.DATA_2, Utility.getJsonStringFromObject(taskDetailModel));
        intent.putExtra(Utility.Extra.TASK_TYPE_IS_INSTA, isInstaBooking);
        intent.putExtra(Utility.Extra.PAYMENT_VIEW_IS_ADDITIONAL_CHARGE, isAdditionalPayment);
        intent.putExtra(Utility.Extra.SELECTED_ADDRESS_MODEL, Utility.getJsonStringFromObject(mSelectedAddressModel));
        context.startActivity(intent);
    }

    public static void newInstance(BaseFragment baseFragment, TaskDetailModel taskDetailModel, boolean isStrategicPartner, int resultCode) {
        Intent intent = new Intent(baseFragment.getActivity(), PaymentChoiceActivity.class);
        intent.putExtra(Utility.Extra.DATA_2, Utility.getJsonStringFromObject(taskDetailModel));
        intent.putExtra(Utility.Extra.TASK_TYPE_IS_STRATEGIC, isStrategicPartner);
        baseFragment.startActivityForResult(intent, resultCode);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityPaymentChoiceBinding = DataBindingUtil.setContentView(this, R.layout.activity_payment_choice);
        initiateUI();
        setListeners();
    }

    @Override
    protected void initiateUI() {
        if (getIntent().hasExtra(Utility.Extra.DATA)) {
            providerModel = (ProviderModel) Utility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA), ProviderModel.class);
            //This is only when provider profile view for specific task (provider gives quote to specific task)
        }
        if (getIntent().hasExtra(Utility.Extra.DATA_2)) {
            taskDetailModel = (TaskDetailModel) Utility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.DATA_2), TaskDetailModel.class);
        }
        if (getIntent().hasExtra(Utility.Extra.TASK_TYPE_IS_INSTA)) {
            isInstaBooking = getIntent().getBooleanExtra(Utility.Extra.TASK_TYPE_IS_INSTA, false);
            mSelectedAddressModelForInsta = (AddressModel) Utility.getObjectFromJsonString(getIntent().getStringExtra(Utility.Extra.SELECTED_ADDRESS_MODEL), AddressModel.class);
        }

        if (getIntent().hasExtra(Utility.Extra.PAYMENT_VIEW_IS_ADDITIONAL_CHARGE)) {
            isAdditional = getIntent().getIntExtra(Utility.Extra.PAYMENT_VIEW_IS_ADDITIONAL_CHARGE, 0);
        }

        if (getIntent().hasExtra(Utility.Extra.TASK_TYPE_IS_STRATEGIC)) {
            isStrategicPartner = getIntent().getBooleanExtra(Utility.Extra.TASK_TYPE_IS_STRATEGIC, false);
        }
    }

    @Override
    protected void setListeners() {
        mActivityPaymentChoiceBinding.tvCard.setOnClickListener(OnCardNetBankingPayListener);
        mActivityPaymentChoiceBinding.tvNetbanking.setOnClickListener(OnCardNetBankingPayListener);
    }

    View.OnClickListener OnCardNetBankingPayListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            LogUtils.LOGE(TAG, "onClick: of HDFC PAYMENT");
            if (isStrategicPartner) {
                // Go for regular payment gateway strategic partner
                payNowForStrategicPartner();
            } else if (isAdditional == 0) {
                // Go for regular payment gateway
                payNowForNormalTask(false);
            } else {
                // Go for regular payment gateway
                payNowForNormalTask(true);
            }


        }
    };

//////////////////////////////////////////////////////////////////    NORMAL TASK PAYMENT METHOD [START] ///////////////////////////////////////////////////////

    /**
     * Used for payment
     */
    private void payNowForNormalTask(final boolean isForAdditionalQuote) {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityPaymentChoiceBinding.getRoot());
            return;
        }

        showProgressDialog();

        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();

        //Add Params
        // = new HashMap<String, Object>();

        mTransactionParams = HDFCPaymentUtility.getPaymentTransactionFieldsForNormalTask(PreferenceUtility.getInstance(this).getFCMRegID(), userDetails, isForAdditionalQuote, isInstaBooking, taskDetailModel, providerModel);

        new HDFCPaymentUtility.AsyncFetchEncryptedString(new HDFCPaymentUtility.EncryptTransactionParamsListener() {
            @Override
            public void onPostOfEncryption(String encryptedData) {
                UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
                //Add Header parameters
                Map<String, String> mHeaderParams = new HashMap<>();
                mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
                mHeaderParams.put(NetworkUtility.TAGS.USER_ID, userDetails.UserID);

                Map<String, Object> mFinalParams = new HashMap<>();
                mFinalParams.put(NetworkUtility.TAGS.DATA, encryptedData);

//                getPaymentUrl(userDetails, isForAdditionalQuote);
                String url;
                // if payment is done using insta feature then
                // post data will be generated like strategic partner feature
                // call startegic generate hash for payment
                url = isInstaBooking ? NetworkUtility.WS.GET_PAYMENT_HASH_FOR_STRATEGIC_PARTNER : NetworkUtility.WS.GET_PAYMENT_HASH;
                //Url is based on condition if address id is greater then 0 then it means we need to update the existing address
                VolleyNetworkRequest mVolleyNetworkRequestForSPList = new VolleyNetworkRequest(url
                        , mCallGenerateHashWSErrorListener
                        , mCallGenerateHashWSResponseListener
                        , mHeaderParams
                        , mFinalParams
                        , null);
                Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForSPList);


            }
        }).execute(new JSONObject(mTransactionParams).toString());


    }


    // if payment is done using insta feature then
    // post data will be generated like strategic partner feature
    // as task id will be null && and additional payment will be null


 /*   private Map<String, Object> getPaymentTransactionFields(UserDetails userDetails, boolean isForAdditionalQuote) {

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
    } */

    Response.Listener mCallGenerateHashWSResponseListener = new Response.Listener() {
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

                        /*
                         * Changes @Bhavesh : 7thJuly,2017
                         * In case we have to bypass the payment
                         */
                        if (BuildConfig.NEED_TO_BYPASS_PAYMENT) {
//                            PLEASE NOTE: THIS IS JUST TO BYPPASS THE PAYMENT GATEWAY. THIS IS NOT
//                            GOING TO RUN IN LIVE ENVIRONMENT BUILDS
                            // Direct bypass the things
                            if (!isInstaBooking && jsonObject.getString(NetworkUtility.TAGS.IS_FOR_ADDITIONAL_QUOTE).equalsIgnoreCase(getString(R.string.label_yes))) {
                                //Call update payment service from here with all the response come from service
                                updatePaymentStatus(true, getString(R.string.message_payment_bypassed), true);
                            } else {
                                //Call update payment service from here with all the response come from service
                                if (isInstaBooking)
                                    callCreateInstaBookingTaskWS(getString(R.string.message_payment_bypassed));
                                else if (isStrategicPartner)
                                    onSuccessPaymentOfStrategicPartner(getString(R.string.message_payment_bypassed));
                                else
                                    updatePaymentStatus(true, getString(R.string.message_payment_bypassed), false);
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

                            if (isStrategicPartner) {
                                HDFCPaymentGatewayActivity.newInstance(
                                        PaymentChoiceActivity.this,
                                        HDFCPaymentUtility.getPaymentUrl(mTransactionParams, jsonObject.optString(NetworkUtility.TAGS.HASH_STRING)),
                                        Utility.REQUEST_START_PAYMENT_FOR_STRATEGIC_PARTNER);

                            } else if (!isInstaBooking && jsonObject.getString(NetworkUtility.TAGS.IS_FOR_ADDITIONAL_QUOTE).equalsIgnoreCase(getString(R.string.label_yes))) {
                                HDFCPaymentGatewayActivity.newInstance(
                                        PaymentChoiceActivity.this,
                                        HDFCPaymentUtility.getPaymentUrl(mTransactionParams, jsonObject.optString(NetworkUtility.TAGS.HASH_STRING)),
                                        Utility.ADDITIONAL_REQUEST_START_PAYMENT);
                            } else {
                                HDFCPaymentGatewayActivity.newInstance(
                                        PaymentChoiceActivity.this,
                                        HDFCPaymentUtility.getPaymentUrl(mTransactionParams, jsonObject.optString(NetworkUtility.TAGS.HASH_STRING)),
                                        Utility.REQUEST_START_PAYMENT);
                            }

                        }
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityPaymentChoiceBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mActivityPaymentChoiceBinding.getRoot());
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
                mCallGenerateHashWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
        }
    };

    Response.ErrorListener mCallGenerateHashWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(final VolleyError error) {
            hideProgressDialog();
            LogUtils.LOGE(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
//            hideProgressDialog();

            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityPaymentChoiceBinding.getRoot());

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
        final UserDetails userDetails = PreferenceUtility.getInstance(PaymentChoiceActivity.this).getUserDetails();
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

    private void callCreateInstaBookingTaskWS(String response) {
        //        TASK_CREATE_INSTA_BOOKING

//        Required Params => task_desc,address_id,city_id,cat_id,start_datetime,
// media_file,subcategory_id,spUserId,txnid,cheepcode,quote_amount,payable_amount
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityPaymentChoiceBinding.getRoot());
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
            /*
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
        mParams.put(NetworkUtility.TAGS.TRANSACTION_ID, mTransactionParams.get(HDFCPaymentUtility.TXN_ID));
        mParams.put(NetworkUtility.TAGS.PAYMENT_STATUS, Utility.PAYMENT_STATUS.COMPLETED);
        mParams.put(NetworkUtility.TAGS.PAYMENT_LOG, response);
        LogUtils.LOGE(TAG, "payNow: cheepCode " + taskDetailModel.cheepCode);
        LogUtils.LOGE(TAG, "payNow: dicount " + taskDetailModel.taskDiscountAmount);

        if (!TextUtils.isEmpty(taskDetailModel.cheepCode)) {
            mParams.put(NetworkUtility.TAGS.CHEEPCODE, taskDetailModel.cheepCode);
            mParams.put(NetworkUtility.TAGS.PROMOCODE_PRICE, taskDetailModel.taskDiscountAmount);
        } else {
            mParams.put(NetworkUtility.TAGS.CHEEPCODE, Utility.EMPTY_STRING);
            mParams.put(NetworkUtility.TAGS.PROMOCODE_PRICE, Utility.ZERO_STRING);
        }

        mParams.put(NetworkUtility.TAGS.QUOTE_AMOUNT, providerModel.quotePriceWithOutGST);
        mParams.put(NetworkUtility.TAGS.PAYABLE_AMOUNT, providerModel.quotePrice);
        // For AppsFlyer
        mTaskCreationParams = new HashMap<>();
        mTaskCreationParams.put(NetworkUtility.TAGS.TASK_DESC, taskDetailModel.taskDesc);
//        mParams.put(NetworkUtility.TAGS.ADDRESS_ID, taskDetailModel.taskAddressId);
        if (Integer.parseInt(mSelectedAddressModelForInsta.address_id) > 0) {
            mTaskCreationParams.put(NetworkUtility.TAGS.ADDRESS_ID, mSelectedAddressModelForInsta.address_id);
        } else {
            // In case its nagative then provide other address information
            /*
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
        mTaskCreationParams.put(NetworkUtility.TAGS.PAYMENT_STATUS, Utility.PAYMENT_STATUS.COMPLETED);
        mTaskCreationParams.put(NetworkUtility.TAGS.PAYMENT_LOG, response);
        mTaskCreationParams.put(NetworkUtility.TAGS.SUBCATEGORY_ID, taskDetailModel.subCategoryID);
        mTaskCreationParams.put(NetworkUtility.TAGS.SP_USER_ID, providerModel.providerId);
        mTaskCreationParams.put(NetworkUtility.TAGS.TRANSACTION_ID, mTransactionParams.get(HDFCPaymentUtility.TXN_ID));

        LogUtils.LOGE(TAG, "payNow: cheepCode " + taskDetailModel.cheepCode);
        LogUtils.LOGE(TAG, "payNow: dicount " + taskDetailModel.taskDiscountAmount);

        if (!TextUtils.isEmpty(taskDetailModel.cheepCode)) {
            mTaskCreationParams.put(NetworkUtility.TAGS.CHEEPCODE, taskDetailModel.cheepCode);
            mTaskCreationParams.put(NetworkUtility.TAGS.PROMOCODE_PRICE, taskDetailModel.taskDiscountAmount);


        } else {
            mTaskCreationParams.put(NetworkUtility.TAGS.CHEEPCODE, Utility.EMPTY_STRING);
            mTaskCreationParams.put(NetworkUtility.TAGS.PROMOCODE_PRICE, Utility.ZERO_STRING);
        }

        mTaskCreationParams.put(NetworkUtility.TAGS.QUOTE_AMOUNT, providerModel.quotePriceWithOutGST);
        mTaskCreationParams.put(NetworkUtility.TAGS.PAYABLE_AMOUNT, providerModel.quotePrice);

        // Url is based on condition if address id is greater then 0 then it means we need to update the existing address
        VolleyNetworkRequest mVolleyNetworkRequestForSPList = new VolleyNetworkRequest(NetworkUtility.WS.TASK_CREATE_INSTA_BOOKING
                , mCallUpdatePaymentStatusWSErrorListener
                , mCallCreateInstaTaskWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForSPList);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Utility.REQUEST_START_PAYMENT:
//            Toast.makeText(mContext, "OnActivityResult called with resultCode:" + resultCode + ", requestCode:" + requestCode, Toast.LENGTH_SHORT).show();
                if (resultCode == RESULT_OK) {
                    //success
                    if (data != null) {
                        LogUtils.LOGE(TAG, "onActivityResult() called with success: result= [" + data.getStringExtra("payu_response") + "]");
                        //Call update payment service from here with all the response come from service
                        // check is task is from insta booking or not
                        if (isInstaBooking)
                            callCreateInstaBookingTaskWS(data.getStringExtra("payu_response"));
                        else
                            updatePaymentStatus(true, data.getStringExtra("payu_response"), false);
                    }
                }
                if (resultCode == RESULT_CANCELED) {
                    //failed
                    if (data != null) {
                        LogUtils.LOGE(TAG, "onActivityResult() called with failed: result= [" + data.getStringExtra("payu_response") + "]");
                        //Call update payment service from here with all the response come from service
                        // check is task is from insta booking or not
                        if (isInstaBooking)
                            Utility.showSnackBar(getString(R.string.msg_payment_failed), mActivityPaymentChoiceBinding.getRoot());
                        else
                            updatePaymentStatus(false, data.getStringExtra("payu_response"), false);
                        Utility.showSnackBar(getString(R.string.msg_payment_failed), mActivityPaymentChoiceBinding.getRoot());
                    }
                }
                break;
            case Utility.ADDITIONAL_REQUEST_START_PAYMENT:
//            Toast.makeText(mContext, "OnActivityResult called with resultCode:" + resultCode + ", requestCode:" + requestCode, Toast.LENGTH_SHORT).show();
                if (resultCode == RESULT_OK) {
                    //success
                    if (data != null) {
                        LogUtils.LOGE(TAG, "onActivityResult() called with success: result= [" + data.getStringExtra("payu_response") + "]");
                        //Call update payment service from here with all the response come from service
                        updatePaymentStatus(true, data.getStringExtra("payu_response"), true);
                    }
                }
                if (resultCode == RESULT_CANCELED) {
                    //failed
                    if (data != null) {
                        LogUtils.LOGE(TAG, "onActivityResult() called with failed: result= [" + data.getStringExtra("payu_response") + "]");
                        //Call update payment service from here with all the response come from service
                        updatePaymentStatus(false, data.getStringExtra("payu_response"), true);
                        Utility.showSnackBar(getString(R.string.msg_payment_failed), mActivityPaymentChoiceBinding.getRoot());
                    }
                }
                break;
            case Utility.REQUEST_START_PAYMENT_FOR_STRATEGIC_PARTNER:
                if (resultCode == Activity.RESULT_OK) {
                    // success
                    if (data != null) {
                        LogUtils.LOGE(TAG, "onActivityResult() called with success: result= [" + data.getStringExtra("payu_response") + "]");
                        // Call update payment service from here with all the response come from service
                        onSuccessPaymentOfStrategicPartner(data.getStringExtra("payu_response"));
                    }
                }
                if (resultCode == Activity.RESULT_CANCELED) {
                    if (data != null) {
                        LogUtils.LOGE(TAG, "onActivityResult() called with failed: result= [" + data.getStringExtra("payu_response") + "]");
                        //Call update payment service from here with all the response come from service
//                    callTaskCreationWebServiceForStratgicPartner(false, data.getStringExtra("result"));
                        Utility.showSnackBar(getString(R.string.msg_payment_failed), mActivityPaymentChoiceBinding.getRoot());
                    }
                }
                break;
        }
    }

    public void onSuccessPaymentOfStrategicPartner(String payuResponse) {
        Intent intent = new Intent();
        intent.putExtra(Utility.Extra.IS_PAYMENT_SUCCESSFUL, true);
        intent.putExtra(Utility.Extra.PAYU_RESPONSE, payuResponse);
        intent.putExtra(Utility.Extra.TRANSACTION_ID, mTransactionParams.get(HDFCPaymentUtility.TXN_ID));
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * Used for payment
     */
    private void updatePaymentStatus(boolean isSuccess, String response, boolean isAdditionalPayment) {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityPaymentChoiceBinding.getRoot());
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
        mParams.put(NetworkUtility.TAGS.TRANSACTION_ID, mTransactionParams.get(HDFCPaymentUtility.TXN_ID));
        mParams.put(NetworkUtility.TAGS.PAYMENT_STATUS, isSuccess ? Utility.PAYMENT_STATUS.COMPLETED : Utility.PAYMENT_STATUS.FAILED);
        mParams.put(NetworkUtility.TAGS.PAYMENT_LOG, response);
        mParams.put(NetworkUtility.TAGS.IS_FOR_ADDITIONAL_QUOTE, isAdditionalPayment
                ? getString(R.string.label_yes).toLowerCase() :
                getString(R.string.label_no).toLowerCase());

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
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityPaymentChoiceBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mActivityPaymentChoiceBinding.getRoot());
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
//                        Utility.showToast(PaymentsStepActivity.this, jsonObject.getString(NetworkUtility.TAGS.MESSAGE));

                        // Send Event tracking for AppsFlyer
                        AppsFlyerLib.getInstance().trackEvent(mContext, NetworkUtility.TAGS.APPSFLYER_CUSTOM_TRACK_EVENTS.TASK_CREATE, mTaskCreationParams);

                        onSuccessfullInstaBookingTaskCompletion(jsonObject);
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityPaymentChoiceBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mActivityPaymentChoiceBinding.getRoot());
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

        MessageEvent messageEvent = new MessageEvent();
        messageEvent.BROADCAST_ACTION = Utility.BROADCAST_TYPE.TASK_PAID_FOR_INSTA_BOOKING;
        EventBus.getDefault().post(messageEvent);

        // finish current activity
        finish();
        // br for finished task creation activity
//        Intent intent = new Intent(Utility.BR_ON_TASK_CREATED_FOR_INSTA_BOOKING);
//        sendBroadcast(intent);
    }

    private String fetchMessageFromDateOfMonth(int day, SuperCalendar superStartDateTimeCalendar) {
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
            LogUtils.LOGE(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            hideProgressDialog();

            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityPaymentChoiceBinding.getRoot());

        }
    };


//////////////////////////////////////////////////////////////////    NORMAL TASK PAYMENT METHOD [END] ///////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////    Strategic partner PAYMENT METHOD [Start] ///////////////////////////////////////////////////////

 /* **********************************************************************************************
     *********************** [Generate Hash Code For Payment] [Start] *************************************
     ***********************************************************************************************/

    /**
     * Used for payment
     */
    private void payNowForStrategicPartner() {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mActivityPaymentChoiceBinding.getRoot());
            return;
        }

        if (PreferenceUtility.getInstance(mContext).getUserDetails() == null) {
            LoginActivity.newInstance(mContext);
            return;
        }

        showProgressDialog();

        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();

        //Add Params
        mTransactionParams = HDFCPaymentUtility.getPaymentTransactionFieldsForStrategicPartner(PreferenceUtility.getInstance(this).getFCMRegID(),
                userDetails,
                taskDetailModel.cheepCode,
                taskDetailModel.totalStrategicPartner,
                taskDetailModel.payableAmountStrategicPartner,
                taskDetailModel.taskStartdate);

        // We do not need to pass PROID and TaskID in Strategic partner as it still not finalized
        //mParams.put(NetworkUtility.TAGS.SP_USER_ID, providerModel.providerId);
        //mParams.put(NetworkUtility.TAGS.TASK_ID, taskDetailModel.taskId);

        if (!TextUtils.isEmpty(taskDetailModel.cheepCode)) {
            mTransactionParams.put(NetworkUtility.TAGS.CHEEPCODE, taskDetailModel.cheepCode);
            mTransactionParams.put(NetworkUtility.TAGS.PROMOCODE_PRICE, taskDetailModel.taskDiscountAmount);
        } else {
            mTransactionParams.put(NetworkUtility.TAGS.CHEEPCODE, Utility.EMPTY_STRING);
            mTransactionParams.put(NetworkUtility.TAGS.PROMOCODE_PRICE, Utility.ZERO_STRING);
        }

        new HDFCPaymentUtility.AsyncFetchEncryptedString(new HDFCPaymentUtility.EncryptTransactionParamsListener() {
            @Override
            public void onPostOfEncryption(String encryptedData) {
                UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();

                //Add Header parameters
                Map<String, String> mHeaderParams = new HashMap<>();
                mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
                mHeaderParams.put(NetworkUtility.TAGS.USER_ID, userDetails.UserID);

                Map<String, Object> mFinalParams = new HashMap<>();
                mFinalParams.put(NetworkUtility.TAGS.DATA, encryptedData);

                //calling this to create post data
                //Url is based on condition if address id is greater then 0 then it means we need to update the existing address
                VolleyNetworkRequest mVolleyNetworkRequestForSPList = new VolleyNetworkRequest(NetworkUtility.WS.GET_PAYMENT_HASH_FOR_STRATEGIC_PARTNER
                        , mCallGenerateHashWSErrorListener
                        , mCallGenerateHashWSResponseListener
                        , mHeaderParams
                        , mFinalParams
                        , null);
                Volley.getInstance(mContext).

                        addToRequestQueue(mVolleyNetworkRequestForSPList);
            }
        }).execute(new JSONObject(mTransactionParams).toString());
    }


}
