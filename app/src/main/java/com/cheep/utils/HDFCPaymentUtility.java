package com.cheep.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.cheep.BuildConfig;
import com.cheep.model.ProviderModel;
import com.cheep.model.TaskDetailModel;
import com.cheep.model.UserDetails;
import com.cheep.network.NetworkUtility;
import com.payu.india.Model.PaymentParams;
import com.payu.india.Model.PayuConfig;
import com.payu.india.Payu.PayuConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by giteeka on 19/9/17.
 */

public class HDFCPaymentUtility {

    private static final String TAG = HDFCPaymentUtility.class.getSimpleName();

    // Constants
    public static final String TXN_ID = "txnid";
    private static final String DEVICE_TYPE = "device_type";
    private static final String ISMOBILEVIEW = "ismobileview";
    private static final String USER_CREDENTIALS = "user_credentials";
    private static final String PRODUCTINFO = "productinfo";
    private static final String TASK_ID = "task_id";
    private static final String KEY = "key";
    private static final String INSTRUMENT_TYPE = "instrument_type";
    private static final String SURL = "surl";
    private static final String FURL = "furl";
    private static final String INSTRUMENT_ID = "instrument_id";
    private static final String FIRSTNAME = "firstname";
    private static final String EMAIL = "email";
    private static final String PHONE = "phone";
    private static final String AMOUNT = "amount";
    private static final String UDF1 = "udf1";
    private static final String UDF2 = "udf2";
    private static final String UDF3 = "udf3";
    private static final String UDF4 = "udf4";
    private static final String UDF5 = "udf5";
    private static final String HASH = "hash";

    private static final String PAYMENT_RELATED_DETAILS_FOR_MOBILE_SDK = "payment_related_details_for_mobile_sdk";
    private static final String VAS_FOR_MOBILE_SDK = "vas_for_mobile_sdk";
    private static final String GET_MERCHANT_IBIBO_CODES = "get_merchant_ibibo_codes";
    private static final String GET_USER_CARDS = "get_user_cards";
    private static final String SAVE_USER_CARD = "save_user_card";
    private static final String DELETE_USER_CARD = "delete_user_card";
    private static final String EDIT_USER_CARD = "edit_user_card";
    private static final String COMMAND = "command";

    public static Map<String, String> getPaymentTransactionFieldsForNormalTask(String fcmToken,
                                                                               UserDetails userDetails,
                                                                               TaskDetailModel taskDetailModel, String payableAmount
            , ProviderModel providerModel, boolean isPayNow) {
        //PaymentParams
        Map<String, String> mParams = new HashMap<>();
        String transaction_Id = Utility.getUniqueTransactionId();
        mParams.put(TXN_ID, transaction_Id);
        Log.e(TAG, "getPaymentTransactionFieldsForNormalTask: ----------------------" +transaction_Id);
        mParams.put(KEY, BuildConfig.PAYUBIZ_HDFC_KEY);
        mParams.put(EMAIL, userDetails.email);
        mParams.put(FIRSTNAME, userDetails.userName);
//        mParams.put(AMOUNT, isPayNow ? providerModel.quotePrice : taskDetailModel.taskTotalPendingAmount);
        mParams.put(AMOUNT, payableAmount);
//        mParams.put(PRODUCTINFO, taskDetailModel.taskId);
        if (taskDetailModel.taskId != null && !taskDetailModel.taskId.isEmpty()) {
            mParams.put(TASK_ID, taskDetailModel.taskId);
            mParams.put(PRODUCTINFO, taskDetailModel.taskId);
        } else {
            mParams.put(TASK_ID, Utility.EMPTY_STRING);
            mParams.put(PRODUCTINFO, userDetails.userID);
        }
        if (providerModel != null) {
            mParams.put(UDF2, providerModel.providerId);
        } else {
            mParams.put(UDF2, UDF2);
        }
        mParams.put(UDF1, "Task Start Date : " + taskDetailModel.taskStartdate);
        mParams.put(UDF3, NetworkUtility.TAGS.PLATFORMTYPE.ANDROID);
        mParams.put(UDF4, UDF4);
        mParams.put(UDF5, UDF5);
        mParams.put(USER_CREDENTIALS, BuildConfig.PAYUBIZ_HDFC_KEY + ":" + userDetails.email);
        mParams.put(DEVICE_TYPE, "1");
        mParams.put(ISMOBILEVIEW, "1");
        mParams.put(INSTRUMENT_TYPE, fcmToken);
        mParams.put(SURL, BuildConfig.PAYUBIZ_SUCCESS_URL);
        mParams.put(FURL, BuildConfig.PAYUBIZ_FAIL_URL);
        mParams.put(INSTRUMENT_ID, BuildConfig.PAYUBIZ_INSTRUMENT_ID);
        mParams.put(PHONE, userDetails.phoneNumber);
        mParams.put(HASH, Utility.EMPTY_STRING);

        LogUtils.LOGE(TAG, "getPaymentTransactionFieldsForNormalTask: mparams " + mParams);

        return mParams;
    }


    public static Map<String, String> getPaymentTransactionFieldsForStrategicPartner(String fcmToken,
                                                                                     UserDetails userDetails,
                                                                                     String payAmount,
                                                                                     String start_datetime) {

        Map<String, String> mTransactionFieldsParams;
        mTransactionFieldsParams = new HashMap<>();
        // Create Unique Transaction ID
        String transaction_Id = Utility.getUniqueTransactionId();

        mTransactionFieldsParams.put(TXN_ID, transaction_Id);
        mTransactionFieldsParams.put(DEVICE_TYPE, "1");
        mTransactionFieldsParams.put(ISMOBILEVIEW, "1");
        mTransactionFieldsParams.put(PRODUCTINFO, userDetails.userID);
        mTransactionFieldsParams.put(KEY, BuildConfig.PAYUBIZ_HDFC_KEY);
        mTransactionFieldsParams.put(USER_CREDENTIALS, BuildConfig.PAYUBIZ_HDFC_KEY + ":" + userDetails.email);
        mTransactionFieldsParams.put(INSTRUMENT_TYPE, fcmToken);
        mTransactionFieldsParams.put(SURL, BuildConfig.PAYUBIZ_SUCCESS_URL);
        mTransactionFieldsParams.put(FURL, BuildConfig.PAYUBIZ_FAIL_URL);
        mTransactionFieldsParams.put(INSTRUMENT_ID, BuildConfig.PAYUBIZ_INSTRUMENT_ID);

        // User Details
        mTransactionFieldsParams.put(FIRSTNAME, userDetails.userName);
        mTransactionFieldsParams.put(EMAIL, userDetails.email);
        mTransactionFieldsParams.put(PHONE, userDetails.phoneNumber);
        // Total Amount
        mTransactionFieldsParams.put(AMOUNT, payAmount);

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


    public static class AsyncFetchEncryptedString extends AsyncTask<String, Void, Map<String, Object>> {

        EncryptTransactionParamsListener mEncryptTransactionParamsListener;
        Context mContext;

        public AsyncFetchEncryptedString(Context context, EncryptTransactionParamsListener encryptTransactionParamsListener) {
            mContext = context;
            mEncryptTransactionParamsListener = encryptTransactionParamsListener;
        }

        @Override
        protected Map<String, Object> doInBackground(String... strings) {
            Map<String, Object> dataParam = new HashMap<>();
            JSONObject jsonObject = new JSONObject();
            try {
                UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
                jsonObject.put(NetworkUtility.TAGS.DATA_0, new JSONObject(strings[0]));
                dataParam.put(NetworkUtility.TAGS.DATA_0, Utility.applyAESEncryption(new JSONObject(strings[0]).toString()));
                //PayUHashes
                jsonObject.put(NetworkUtility.TAGS.DATA_1, getPayuHashJson(BuildConfig.PAYUBIZ_HDFC_KEY, BuildConfig.PAYUBIZ_HDFC_KEY + ":" + userDetails.email, PAYMENT_RELATED_DETAILS_FOR_MOBILE_SDK));
                dataParam.put(NetworkUtility.TAGS.DATA_1, Utility.applyAESEncryption(getPayuHashJson(BuildConfig.PAYUBIZ_HDFC_KEY, BuildConfig.PAYUBIZ_HDFC_KEY + ":" + userDetails.email, PAYMENT_RELATED_DETAILS_FOR_MOBILE_SDK).toString()));
                jsonObject.put(NetworkUtility.TAGS.DATA_2, getPayuHashJson(BuildConfig.PAYUBIZ_HDFC_KEY, PayuConstants.DEFAULT, VAS_FOR_MOBILE_SDK));
                dataParam.put(NetworkUtility.TAGS.DATA_2, Utility.applyAESEncryption(getPayuHashJson(BuildConfig.PAYUBIZ_HDFC_KEY, PayuConstants.DEFAULT, VAS_FOR_MOBILE_SDK).toString()));
                jsonObject.put(NetworkUtility.TAGS.DATA_3, getPayuHashJson(BuildConfig.PAYUBIZ_HDFC_KEY, PayuConstants.DEFAULT, GET_MERCHANT_IBIBO_CODES));
                dataParam.put(NetworkUtility.TAGS.DATA_3, Utility.applyAESEncryption(getPayuHashJson(BuildConfig.PAYUBIZ_HDFC_KEY, PayuConstants.DEFAULT, GET_MERCHANT_IBIBO_CODES).toString()));
                jsonObject.put(NetworkUtility.TAGS.DATA_4, getPayuHashJson(BuildConfig.PAYUBIZ_HDFC_KEY, BuildConfig.PAYUBIZ_HDFC_KEY + ":" + userDetails.email, GET_USER_CARDS));
                dataParam.put(NetworkUtility.TAGS.DATA_4, Utility.applyAESEncryption(getPayuHashJson(BuildConfig.PAYUBIZ_HDFC_KEY, BuildConfig.PAYUBIZ_HDFC_KEY + ":" + userDetails.email, GET_USER_CARDS).toString()));
                jsonObject.put(NetworkUtility.TAGS.DATA_5, getPayuHashJson(BuildConfig.PAYUBIZ_HDFC_KEY, BuildConfig.PAYUBIZ_HDFC_KEY + ":" + userDetails.email, SAVE_USER_CARD));
                dataParam.put(NetworkUtility.TAGS.DATA_5, Utility.applyAESEncryption(getPayuHashJson(BuildConfig.PAYUBIZ_HDFC_KEY, BuildConfig.PAYUBIZ_HDFC_KEY + ":" + userDetails.email, SAVE_USER_CARD).toString()));
                jsonObject.put(NetworkUtility.TAGS.DATA_6, getPayuHashJson(BuildConfig.PAYUBIZ_HDFC_KEY, BuildConfig.PAYUBIZ_HDFC_KEY + ":" + userDetails.email, DELETE_USER_CARD));
                dataParam.put(NetworkUtility.TAGS.DATA_6, Utility.applyAESEncryption(getPayuHashJson(BuildConfig.PAYUBIZ_HDFC_KEY, BuildConfig.PAYUBIZ_HDFC_KEY + ":" + userDetails.email, DELETE_USER_CARD).toString()));
                jsonObject.put(NetworkUtility.TAGS.DATA_7, getPayuHashJson(BuildConfig.PAYUBIZ_HDFC_KEY, BuildConfig.PAYUBIZ_HDFC_KEY + ":" + userDetails.email, EDIT_USER_CARD));
                dataParam.put(NetworkUtility.TAGS.DATA_7, Utility.applyAESEncryption(getPayuHashJson(BuildConfig.PAYUBIZ_HDFC_KEY, BuildConfig.PAYUBIZ_HDFC_KEY + ":" + userDetails.email, EDIT_USER_CARD).toString()));

                Log.e("REQUEST JSON :: ", jsonObject.toString());
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
            //return Utility.applyAESEncryption(new JSONObject(params[0]).toString());
            return dataParam;
        }

        @Override
        protected void onPostExecute(Map<String, Object> stringStringMap) {
            super.onPostExecute(stringStringMap);
            mEncryptTransactionParamsListener.onPostOfEncryption(stringStringMap);
        }
    }

    public static JSONObject getPayuHashJson(String key, String user_credentials, String command) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(KEY, key);
            jsonObject.put(USER_CREDENTIALS, user_credentials);
            jsonObject.put(COMMAND, command);

            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getPaymentUrl(Map<String, String> mTransactionFieldsParams, String hashString) {
        StringBuilder postData = new StringBuilder();
        for (String key : mTransactionFieldsParams.keySet()) {
            postData = postData.append("&").append(key).append("=").append(mTransactionFieldsParams.get(key));
        }
        LogUtils.LOGE(TAG, "getPaymentUrl() returned: " + postData.toString());
        return postData.toString().replaceAll("hash=", "hash=" + hashString);
    }


    /*  public static String getPaymentUrl(UserDetails userDetails, boolean isAdditionalPayment) {

          postData = "&txnid=" + transaction_Id +
                  "&device_type=1" +
                  "&ismobileview=1" +
                  "&productinfo=" + (isInstaBooking ? userDetails.userID : taskDetailModel.taskId) +
                  "&user_credentials=" + userDetails.email +
                  "&key=" + BuildConfig.PAYUBIZ_HDFC_KEY +
                  "&instrument_type=" + PreferenceUtility.getInstance(mContext).getFCMRegID() +
                  "&surl=" + BuildConfig.PAYUBIZ_SUCCESS_URL +
                  "&furl=" + BuildConfig.PAYUBIZ_FAIL_URL + "" +
                  "&instrument_id=7dd17561243c202" +
                  "&firstname=" + userDetails.userName +
                  "&email=" + userDetails.email +
                  "&phone=" + userDetails.phoneNumber +
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

  */
    public interface EncryptTransactionParamsListener {
        void onPostOfEncryption(Map<String, Object> encryptedData);
    }

    /**
     * This generate hash is for cheep care subscription purchase payment
     * and task creation of cheep care of subscribed task and paid task
     *
     * @param fcmToken    device token
     * @param userDetails preference user data
     * @param payAmount   final amount to pay
     * @return map params
     */
    public static Map<String, String> getPaymentTransactionFieldsForCheepCare(String fcmToken,
                                                                              UserDetails userDetails,
                                                                              String payAmount, String startDateTime) {

        Map<String, String> mTransactionFieldsParams;
        mTransactionFieldsParams = new HashMap<>();

        // Create Unique Transaction ID
        String transaction_Id = Utility.getUniqueTransactionId();

        mTransactionFieldsParams.put(TXN_ID, transaction_Id);
        mTransactionFieldsParams.put(DEVICE_TYPE, "1");
        mTransactionFieldsParams.put(ISMOBILEVIEW, "1");
        mTransactionFieldsParams.put(PRODUCTINFO, userDetails.userID);
        mTransactionFieldsParams.put(KEY, BuildConfig.PAYUBIZ_HDFC_KEY);
        mTransactionFieldsParams.put(USER_CREDENTIALS, BuildConfig.PAYUBIZ_HDFC_KEY + ":" + userDetails.email);
        mTransactionFieldsParams.put(INSTRUMENT_TYPE, fcmToken);
        mTransactionFieldsParams.put(SURL, BuildConfig.PAYUBIZ_SUCCESS_URL);
        mTransactionFieldsParams.put(FURL, BuildConfig.PAYUBIZ_FAIL_URL);
        mTransactionFieldsParams.put(INSTRUMENT_ID, BuildConfig.PAYUBIZ_INSTRUMENT_ID);

        // User Details
        mTransactionFieldsParams.put(FIRSTNAME, userDetails.userName);
        mTransactionFieldsParams.put(EMAIL, userDetails.email);
        mTransactionFieldsParams.put(PHONE, userDetails.phoneNumber);

        // Total Amount
        mTransactionFieldsParams.put(AMOUNT, payAmount);

        // Start DateTime(In Milliseconds- Timestamp)
        mTransactionFieldsParams.put(UDF1, "Task Start Date : " + (TextUtils.isEmpty(startDateTime) ? transaction_Id : startDateTime));

        // We don't have Provider ID so pass it empty.
        mTransactionFieldsParams.put(UDF2, Utility.EMPTY_STRING);

        // Platform
        mTransactionFieldsParams.put(UDF3, NetworkUtility.TAGS.PLATFORMTYPE.ANDROID);

        mTransactionFieldsParams.put(UDF4, Utility.EMPTY_STRING);
        mTransactionFieldsParams.put(UDF5, Utility.EMPTY_STRING);
        mTransactionFieldsParams.put(HASH, Utility.EMPTY_STRING);


        return mTransactionFieldsParams;
    }

    public static PaymentParams getPayUPaymentParams(Map<String, String> transactionParams, String hashString) {

        //TODO Below are mandatory params for hash genetation
        PaymentParams mPaymentParams = new PaymentParams();
        /**
         * For Test Environment, merchantKey = please contact mobile.integration@payu.in with your app name and registered email id

         */
        mPaymentParams.setKey(transactionParams.get(KEY));
        mPaymentParams.setAmount(transactionParams.get(AMOUNT));
        mPaymentParams.setProductInfo(transactionParams.get(PRODUCTINFO));
        mPaymentParams.setFirstName(transactionParams.get(FIRSTNAME));
        mPaymentParams.setEmail(transactionParams.get(EMAIL));
        mPaymentParams.setPhone(transactionParams.get(PHONE));
        /*
         * Transaction Id should be kept unique for each transaction.
         * */
        mPaymentParams.setTxnId(transactionParams.get(TXN_ID));
        /**
         * Surl --> Success url is where the transaction response is posted by PayU on successful transaction
         * Furl --> Failre url is where the transaction response is posted by PayU on failed transaction
         */
        mPaymentParams.setSurl(transactionParams.get(SURL));
        mPaymentParams.setFurl(transactionParams.get(FURL));
        //mPaymentParams.setNotifyURL(mPaymentParams.getSurl());  //for lazy pay
        /*
         * udf1 to udf5 are options params where you can pass additional information related to transaction.
         * If you don't want to use it, then send them as empty string like, udf1=""
         * */
        mPaymentParams.setUdf1(transactionParams.get(UDF1));
        mPaymentParams.setUdf2(transactionParams.get(UDF2));
        mPaymentParams.setUdf3(transactionParams.get(UDF3));
        mPaymentParams.setUdf4(transactionParams.get(UDF4));
        mPaymentParams.setUdf5(transactionParams.get(UDF5));
        /**
         * These are used for store card feature. If you are not using it then user_credentials = "default"
         * user_credentials takes of the form like user_credentials = "merchant_key : user_id"
         * here merchant_key = your merchant key,
         * user_id = unique id related to user like, email, phone number, etc.
         * */
        mPaymentParams.setUserCredentials(transactionParams.get(USER_CREDENTIALS));
        mPaymentParams.setHash(hashString);

        return mPaymentParams;
    }

    public static PayuConfig getPayUConfig() {
        PayuConfig mPayuConfig = new PayuConfig();
        mPayuConfig.setEnvironment(BuildConfig.PAYUBIZ_ENVIRONMENT);
        return mPayuConfig;
    }

}
