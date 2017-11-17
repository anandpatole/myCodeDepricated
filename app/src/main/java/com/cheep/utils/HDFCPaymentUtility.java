package com.cheep.utils;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.cheep.BuildConfig;
import com.cheep.model.ProviderModel;
import com.cheep.model.TaskDetailModel;
import com.cheep.model.UserDetails;
import com.cheep.network.NetworkUtility;

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

    public static Map<String, String> getPaymentTransactionFieldsForNormalTask(String fcmToken,
                                                                               UserDetails userDetails,
                                                                               TaskDetailModel taskDetailModel,
                                                                               ProviderModel providerModel) {

        Map<String, String> mParams = new HashMap<>();

        String transaction_Id = Utility.getUniqueTransactionId();
        mParams.put(TXN_ID, transaction_Id);


        mParams.put(KEY, BuildConfig.PAYUBIZ_HDFC_KEY);
        mParams.put(EMAIL, userDetails.Email);
        mParams.put(FIRSTNAME, userDetails.UserName);

        if (taskDetailModel.taskPaidAmount != null && !taskDetailModel.taskPaidAmount.isEmpty())
            mParams.put(AMOUNT, taskDetailModel.taskPaidAmount);
        else
            mParams.put(AMOUNT, providerModel.quotePrice);

        mParams.put(PRODUCTINFO, taskDetailModel.taskId);

        if (taskDetailModel.taskId != null && !taskDetailModel.taskId.isEmpty())
            mParams.put(TASK_ID, taskDetailModel.taskId);
        else
            mParams.put(TASK_ID, Utility.EMPTY_STRING);

        mParams.put(UDF2, providerModel.providerId);
        mParams.put(UDF1, "Task Start Date : " + taskDetailModel.taskStartdate);
        mParams.put(UDF3, NetworkUtility.TAGS.PLATFORMTYPE.ANDROID);
        mParams.put(UDF4, Utility.EMPTY_STRING);
        mParams.put(UDF5, Utility.EMPTY_STRING);
        mParams.put(USER_CREDENTIALS, BuildConfig.PAYUBIZ_HDFC_KEY + ":" + userDetails.Email);
        mParams.put(DEVICE_TYPE, "1");
        mParams.put(ISMOBILEVIEW, "1");
        mParams.put(INSTRUMENT_TYPE, fcmToken);
        mParams.put(SURL, BuildConfig.PAYUBIZ_SUCCESS_URL);
        mParams.put(FURL, BuildConfig.PAYUBIZ_FAIL_URL);
        mParams.put(INSTRUMENT_ID, BuildConfig.PAYUBIZ_INSTRUMENT_ID);
        mParams.put(PHONE, userDetails.PhoneNumber);
        mParams.put(HASH, Utility.EMPTY_STRING);
        LogUtils.LOGE(TAG, "getPaymentTransactionFieldsForNormalTask: mparams " + mParams);
        return mParams;
    }


    public static Map<String, String> getPaymentTransactionFieldsForStrategicPartner(String fcmToken,
                                                                                     UserDetails userDetails,
                                                                                     String cheepCode,
                                                                                     String total,
                                                                                     String payableAmount,
                                                                                     String start_datetime) {

        Map<String, String> mTransactionFieldsParams;
        mTransactionFieldsParams = new HashMap<>();
        // Create Unique Transaction ID
        String transaction_Id = Utility.getUniqueTransactionId();

        mTransactionFieldsParams.put(TXN_ID, transaction_Id);
        mTransactionFieldsParams.put(DEVICE_TYPE, "1");
        mTransactionFieldsParams.put(ISMOBILEVIEW, "1");
        mTransactionFieldsParams.put(PRODUCTINFO, userDetails.UserID);
        mTransactionFieldsParams.put(KEY, BuildConfig.PAYUBIZ_HDFC_KEY);
        mTransactionFieldsParams.put(USER_CREDENTIALS, BuildConfig.PAYUBIZ_HDFC_KEY + ":" + userDetails.Email);
        mTransactionFieldsParams.put(INSTRUMENT_TYPE, fcmToken);
        mTransactionFieldsParams.put(SURL, BuildConfig.PAYUBIZ_SUCCESS_URL);
        mTransactionFieldsParams.put(FURL, BuildConfig.PAYUBIZ_FAIL_URL);
        mTransactionFieldsParams.put(INSTRUMENT_ID, BuildConfig.PAYUBIZ_INSTRUMENT_ID);

        // User Details
        mTransactionFieldsParams.put(FIRSTNAME, userDetails.UserName);
        mTransactionFieldsParams.put(EMAIL, userDetails.Email);
        mTransactionFieldsParams.put(PHONE, userDetails.PhoneNumber);
        // Total Amount
        mTransactionFieldsParams.put(AMOUNT, TextUtils.isEmpty(cheepCode) ? total : payableAmount);
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


    /**
     * Asynctask that will do encryption
     *
     * @Date : 6th Feb 2017
     * input: String that needs to be converted
     * output: String after Encryption completed
     */
    public static class AsyncFetchEncryptedString extends AsyncTask<String, Void, String> {

        EncryptTransactionParamsListener mEncryptTransactionParamsListener;

        public AsyncFetchEncryptedString(EncryptTransactionParamsListener encryptTransactionParamsListener) {
            mEncryptTransactionParamsListener = encryptTransactionParamsListener;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                return Utility.applyAESEncryption(new JSONObject(params[0]).toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String encryptedData) {
            super.onPostExecute(encryptedData);
            mEncryptTransactionParamsListener.onPostOfEncryption(encryptedData);
        }
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
                  "&productinfo=" + (isInstaBooking ? userDetails.UserID : taskDetailModel.taskId) +
                  "&user_credentials=" + userDetails.Email +
                  "&key=" + BuildConfig.PAYUBIZ_HDFC_KEY +
                  "&instrument_type=" + PreferenceUtility.getInstance(mContext).getFCMRegID() +
                  "&surl=" + BuildConfig.PAYUBIZ_SUCCESS_URL +
                  "&furl=" + BuildConfig.PAYUBIZ_FAIL_URL + "" +
                  "&instrument_id=7dd17561243c202" +
                  "&firstname=" + userDetails.UserName +
                  "&email=" + userDetails.Email +
                  "&phone=" + userDetails.PhoneNumber +
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
        void onPostOfEncryption(String encryptedData);
    }

}
