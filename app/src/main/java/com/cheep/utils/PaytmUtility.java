package com.cheep.utils;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.BuildConfig;
import com.cheep.network.NetworkUtility;
import com.cheep.network.PaytmNetworkRequest;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by pankaj on 9/23/17.
 */

public class PaytmUtility {
    private static final String TAG = LogUtils.makeLogTag(PaytmUtility.class.getSimpleName());

    private PaytmUtility() {

    }

    ///////////////////////////////////////////////////////Paytm Send OTP API call starts///////////////////////////////////////////////////////

    public interface SendOtpResponseListener {
        void paytmSendOtpSuccessResponse(String state, boolean isRegenerated);

        void showGeneralizedErrorMessage();

        void paytmInvalidMobileNumber();

        void paytmAccountBlocked();

        void volleyError();
    }

    public static void sendOTP(final Context mContext, String mEtText, final SendOtpResponseListener listener, final boolean isRegenerated) {

        // Erro Listener
        Response.ErrorListener mGenerateOTPErrorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");
                listener.volleyError();
            }
        };

        // Response Listener
        Response.Listener mGenerateOTPResponseListener = new Response.Listener() {
            @Override
            public void onResponse(Object response) {
                Log.d(TAG, "onResponse() called with: response = [" + response + "]");

                try {
                    JSONObject jRoot = new JSONObject(response.toString());
                    JSONObject data = jRoot.getJSONObject(NetworkUtility.TAGS.DATA);
                    JSONObject paytmResponseData = data.getJSONObject(NetworkUtility.TAGS.PAYTM_RESPONSE_DATA);
                    String responseCode = paytmResponseData.getString(NetworkUtility.PAYTM.PARAMETERS.responseCode);
                    switch (responseCode) {
                        case NetworkUtility.PAYTM.RESPONSE_CODES.LOGIN:
                        case NetworkUtility.PAYTM.RESPONSE_CODES.REGISTER:
                            listener.paytmSendOtpSuccessResponse(paytmResponseData.getString(NetworkUtility.PAYTM.PARAMETERS.state), isRegenerated);
                            break;
                        case NetworkUtility.PAYTM.RESPONSE_CODES.INVALID_AUTHORIZATION:
                        case NetworkUtility.PAYTM.RESPONSE_CODES.BAD_REQUEST:
                        case NetworkUtility.PAYTM.RESPONSE_CODES.LOGIN_FAILED:
                            listener.showGeneralizedErrorMessage();
                            break;
                        //invalid email not handled as email is not mandatory and we are not sending email
                        case NetworkUtility.PAYTM.RESPONSE_CODES.INVALID_EMAIL:
                            break;
                        case NetworkUtility.PAYTM.RESPONSE_CODES.INVALID_MOBILE:
                            listener.paytmInvalidMobileNumber();
                            break;
                        case NetworkUtility.PAYTM.RESPONSE_CODES.ACCOUNT_BLOCKED:
                            //TODO: snackbar message to be changed in case required. now displaying generalized message of something went wrong
                            listener.paytmAccountBlocked();
                            break;
                        default:
                            listener.showGeneralizedErrorMessage();
                            break;

                    }

                } catch (JSONException e) {
                    listener.showGeneralizedErrorMessage();
                    e.printStackTrace();
                }
            }
        };

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().userID);


        // Generate Parameters
        Map<String, String> params = new HashMap<>();
        params.put(NetworkUtility.PAYTM.PARAMETERS.phone, mEtText);

        @SuppressWarnings("unchecked")
        VolleyNetworkRequest paytmNetworkRequest = new VolleyNetworkRequest(
                NetworkUtility.WS.PAYTM_GENERATE_OTP,
                mGenerateOTPErrorListener,
                mGenerateOTPResponseListener,
                mHeaderParams,
                params,
                null);
        Volley.getInstance(mContext).addToRequestQueue(paytmNetworkRequest, NetworkUtility.WS.PAYTM_GENERATE_OTP);


        /*Response.ErrorListener mSendOTPErrorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                LogUtils.LOGE(TAG, "onErrorResponse() called with: error = [" + error + "]");
                listener.volleyError();
            }
        };

        Response.Listener<String> mSendOTPResponseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                LogUtils.LOGD(TAG, "onResponse() called with: response = [" + response + "]");
                JSONObject jsonObject;
                try {
                    jsonObject = new JSONObject(response);
                    String responseCode = jsonObject.getString(NetworkUtility.PAYTM.PARAMETERS.responseCode);
                    switch (responseCode) {
                        case NetworkUtility.PAYTM.RESPONSE_CODES.LOGIN:
                        case NetworkUtility.PAYTM.RESPONSE_CODES.REGISTER:
                            listener.paytmSendOtpSuccessResponse(jsonObject.getString(NetworkUtility.PAYTM.PARAMETERS.state));
                            break;
                        case NetworkUtility.PAYTM.RESPONSE_CODES.INVALID_AUTHORIZATION:
                        case NetworkUtility.PAYTM.RESPONSE_CODES.BAD_REQUEST:
                        case NetworkUtility.PAYTM.RESPONSE_CODES.LOGIN_FAILED:
                            listener.showGeneralizedErrorMessage();
                            break;
                        //invalid email not handled as email is not mandatory and we are not sending email
                        case NetworkUtility.PAYTM.RESPONSE_CODES.INVALID_EMAIL:
                            break;
                        case NetworkUtility.PAYTM.RESPONSE_CODES.INVALID_MOBILE:
                            listener.paytmInvalidMobileNumber();
                            break;
                        case NetworkUtility.PAYTM.RESPONSE_CODES.ACCOUNT_BLOCKED:
                            //TODO: snackbar message to be changed in case required. now displaying generalized message of something went wrong
                            listener.paytmAccountBlocked();
                            break;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        Map<String, String> bodyParams = new HashMap<>();

//        mParams.put("email", "parekhkruti26@gmail.com");
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.phone, mEtText);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.clientId, BuildConfig.CLIENT_ID);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.scope, "wallet");
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.responseType, "token");

        final String requestString = new JSONObject(bodyParams).toString();

        PaytmNetworkRequest paytmNetworkRequest = new PaytmNetworkRequest(
                false,
                Request.Method.POST,
                NetworkUtility.PAYTM.OAUTH_APIS.SEND_OTP,
                mSendOTPResponseListener,
                mSendOTPErrorListener,
                null,
                requestString);
        Volley.getInstance(mContext).addToRequestQueue(paytmNetworkRequest, NetworkUtility.PAYTM.OAUTH_APIS.SEND_OTP);*/
    }
    ///////////////////////////////////////////////////////Paytm Send OTP API call ends///////////////////////////////////////////////////////


    ///////////////////////////////////////////////////////Paytm Verify OTP API call starts///////////////////////////////////////////////////////

    public interface VerifyOtpResponseListener {
        void paytmVerifyOtpSuccessResponse(String accessToken, long expires, String resourceOwnerCustomerId);

        void showGeneralizedErrorMessage();

        void paytmVerifyOtpInvalidOtp();

        void volleyError();
    }

    public static void verifyOtp(Context mContext, String mEtText, String mState, final VerifyOtpResponseListener listener) {
        // Erro Listener
        Response.ErrorListener mVerifyOTPErrorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");
                listener.volleyError();
            }
        };

        // Response Listener
        Response.Listener mVerifyOTPResponseListener = new Response.Listener() {
            @Override
            public void onResponse(Object response) {
                Log.d(TAG, "onResponse() called with: response = [" + response + "]");
                try {
                    JSONObject jRoot = new JSONObject(response.toString());
                    JSONObject data = jRoot.getJSONObject(NetworkUtility.TAGS.DATA);
                    JSONObject paytmResponseData = data.getJSONObject(NetworkUtility.TAGS.PAYTM_RESPONSE_DATA);
//                if (PaytmNetworkRequest.HttpResponseCodeDeliverer.mHttpResponseCode == 200) {
                    if (paytmResponseData.has(NetworkUtility.PAYTM.PARAMETERS.access_token)) {
                        String mAccessToken = paytmResponseData.getString(NetworkUtility.PAYTM.PARAMETERS.access_token);
                        long mExpires = paytmResponseData.getLong(NetworkUtility.PAYTM.PARAMETERS.expires);
                        String mResourceOwnerCustomerId = paytmResponseData.getString(NetworkUtility.PAYTM.PARAMETERS.resourceOwnerId);
                        listener.paytmVerifyOtpSuccessResponse(mAccessToken, mExpires, mResourceOwnerCustomerId);
                    } else {
                        String responseCode = paytmResponseData.getString(NetworkUtility.PAYTM.PARAMETERS.responseCode);
                        switch (responseCode) {
                            case NetworkUtility.PAYTM.RESPONSE_CODES.INVALID_CODE:
                            case NetworkUtility.PAYTM.RESPONSE_CODES.INVALID_AUTHORIZATION:
                            case NetworkUtility.PAYTM.RESPONSE_CODES.BAD_REQUEST:
                            case NetworkUtility.PAYTM.RESPONSE_CODES.LOGIN_FAILED:
                                listener.showGeneralizedErrorMessage();
                                break;
                            case NetworkUtility.PAYTM.RESPONSE_CODES.INVALID_OTP:
                                listener.paytmVerifyOtpInvalidOtp();
                                break;
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
//                listener.paytmVerifyTransactionMoneyResponse(response.toString());
            }
        };

        // Generate Parameters
        Map<String, String> params = new HashMap<>();
        params.put(NetworkUtility.PAYTM.PARAMETERS.otp, mEtText);
        params.put(NetworkUtility.PAYTM.PARAMETERS.state, mState);

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().userID);

        @SuppressWarnings("unchecked")
        VolleyNetworkRequest paytmNetworkRequest = new VolleyNetworkRequest(
                NetworkUtility.WS.PAYTM_VERIFY_OTP,
                mVerifyOTPErrorListener,
                mVerifyOTPResponseListener,
                mHeaderParams,
                params,
                null);
        Volley.getInstance(mContext).addToRequestQueue(paytmNetworkRequest, NetworkUtility.WS.PAYTM_VERIFY_OTP);


        /**
         * Below code was from client side, we need to call this now from server side.
         */
        /*Response.Listener<String> mVerifyOTPResponseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                LogUtils.LOGD(TAG, "onResponse() called with: response = [" + response + "]");
                JSONObject jsonObject;
                try {
                    jsonObject = new JSONObject(response);
//                if (PaytmNetworkRequest.HttpResponseCodeDeliverer.mHttpResponseCode == 200) {
                    if (jsonObject.has(NetworkUtility.PAYTM.PARAMETERS.access_token)) {
                        String mAccessToken = jsonObject.getString(NetworkUtility.PAYTM.PARAMETERS.access_token);
                        long mExpires = jsonObject.getInt(NetworkUtility.PAYTM.PARAMETERS.expires);
                        String mResourceOwnerCustomerId = jsonObject.getString(NetworkUtility.PAYTM.PARAMETERS.resourceOwnerId);
                        listener.paytmVerifyOtpSuccessResponse(mAccessToken, mExpires, mResourceOwnerCustomerId);
                    } else {
                        String responseCode = jsonObject.getString(NetworkUtility.PAYTM.PARAMETERS.responseCode);
                        switch (responseCode) {
                            case NetworkUtility.PAYTM.RESPONSE_CODES.INVALID_CODE:
                            case NetworkUtility.PAYTM.RESPONSE_CODES.INVALID_AUTHORIZATION:
                            case NetworkUtility.PAYTM.RESPONSE_CODES.BAD_REQUEST:
                            case NetworkUtility.PAYTM.RESPONSE_CODES.LOGIN_FAILED:
                                listener.showGeneralizedErrorMessage();
                                break;
                            case NetworkUtility.PAYTM.RESPONSE_CODES.INVALID_OTP:
                                listener.paytmVerifyOtpInvalidOtp();
                                break;
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        Map<String, String> headerParams = new HashMap<>();
        String authorization = "merchant-cheep-staging" + Utility.COLON + "e95253bf-2f0b-4727-aedf-686afdcbe07e";
        Log.d(TAG, "authorization " + authorization + "\n" + Base64.encodeBytes(authorization.getBytes()).equals("QmFzaWMgbWVyY2hhbnQtY2hlZXAtc3RhZ2luZzplOTUyNTNiZi0yZjBiLTQ3MjctYWVkZi02ODZhZmRjYmUwN2U="));
        headerParams.put(NetworkUtility.PAYTM.PARAMETERS.Authorization, Utility.BASIC + Utility.ONE_CHARACTER_SPACE + Base64.encodeBytes(authorization.getBytes()));
        Map<String, String> bodyParams = new HashMap<>();
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.otp, mEtText);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.state, mState);

        final String requestString = new JSONObject(bodyParams).toString();

        PaytmNetworkRequest paytmNetworkRequest = new PaytmNetworkRequest(
                false,
                Request.Method.POST,
                NetworkUtility.PAYTM.OAUTH_APIS.GET_ACCESS_TOKEN_SENDING_OTP,
                mVerifyOTPResponseListener,
                mVerifyOTPErrorListener,
                headerParams,
                requestString);
        Volley.getInstance(mContext).addToRequestQueue(paytmNetworkRequest, NetworkUtility.PAYTM.OAUTH_APIS.GET_ACCESS_TOKEN_SENDING_OTP);*/

    }
    ///////////////////////////////////////////////////////Paytm Verify OTP API call ends///////////////////////////////////////////////////////


    ///////////////////////////////////////////////////////Paytm Check Balance API call starts///////////////////////////////////////////////////////

    public interface CheckBalanceResponseListener {
        void paytmCheckBalanceSuccessResponse(JSONObject jsonObject);

        void paytmInvalidAuthorization();

        void showGeneralizedErrorMessage();

        void paytmInvalidMobileNumber();

        void paytmAccountBlocked();

        void volleyError();
    }

    public static void checkBalance(Context mContext, String mAccessToken, final CheckBalanceResponseListener listener) {
        // Erro Listener
        Response.ErrorListener mVerifyOTPErrorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");
                listener.volleyError();
            }
        };

        // Response Listener
        Response.Listener mVerifyOTPResponseListener = new Response.Listener() {
            @Override
            public void onResponse(Object response) {
                Log.d(TAG, "onResponse() called with: response = [" + response + "]");
                try {
                    JSONObject jRoot = new JSONObject(response.toString());
                    JSONObject data = jRoot.getJSONObject(NetworkUtility.TAGS.DATA);
                    JSONObject paytmResponseData = data.getJSONObject(NetworkUtility.TAGS.PAYTM_RESPONSE_DATA);

                    String responseCode = paytmResponseData.getString(NetworkUtility.PAYTM.PARAMETERS.statusCode);
                    switch (responseCode) {
                        case NetworkUtility.PAYTM.RESPONSE_CODES.SUCCESS:
                            listener.paytmCheckBalanceSuccessResponse(paytmResponseData);
                            break;
                        case NetworkUtility.PAYTM.RESPONSE_CODES.INVALID_AUTHORIZATION:
                        case NetworkUtility.PAYTM.RESPONSE_CODES.UNAUTHORIZED_ACCESS:
                            listener.paytmInvalidAuthorization();
                            break;
                        case NetworkUtility.PAYTM.RESPONSE_CODES.BAD_REQUEST:
                        case NetworkUtility.PAYTM.RESPONSE_CODES.LOGIN_FAILED:
                            listener.showGeneralizedErrorMessage();
                            break;
                        //invalid email not handled as email is not mandatory and we are not sending email
                        case NetworkUtility.PAYTM.RESPONSE_CODES.INVALID_EMAIL:
                            break;
                        case NetworkUtility.PAYTM.RESPONSE_CODES.INVALID_MOBILE:
                            // Show Toast
                            listener.paytmInvalidMobileNumber();
                            break;
                        case NetworkUtility.PAYTM.RESPONSE_CODES.ACCOUNT_BLOCKED:
                            //TODO: snackbar message to be changed in case required. now displaying generalized message of something went wrong
                            // Show Toast
                            listener.paytmAccountBlocked();
                            break;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        // Generate Parameters
        Map<String, String> params = new HashMap<>();
        params.put(NetworkUtility.PAYTM.PARAMETERS.token, mAccessToken);

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().userID);

        @SuppressWarnings("unchecked")
        VolleyNetworkRequest paytmNetworkRequest = new VolleyNetworkRequest(
                NetworkUtility.WS.PAYTM_CHECK_BALANCE,
                mVerifyOTPErrorListener,
                mVerifyOTPResponseListener,
                mHeaderParams,
                params,
                null);
        Volley.getInstance(mContext).addToRequestQueue(paytmNetworkRequest, NetworkUtility.WS.PAYTM_CHECK_BALANCE);


        /*
        Response.ErrorListener mCheckBalanceErrorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

                listener.volleyError();
            }
        };

        Response.Listener<String> mCheckBalanceResponseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "onResponse() called with: response = [" + response + "]");
                JSONObject jsonObject;
                try {
                    jsonObject = new JSONObject(response);

                    String responseCode = jsonObject.getString(NetworkUtility.PAYTM.PARAMETERS.statusCode);
                    switch (responseCode) {
                        case NetworkUtility.PAYTM.RESPONSE_CODES.SUCCESS:
                            listener.paytmCheckBalanceSuccessResponse(jsonObject);
                            break;
                        case NetworkUtility.PAYTM.RESPONSE_CODES.INVALID_AUTHORIZATION:
                            listener.paytmInvalidAuthorization();
                            break;
                        case NetworkUtility.PAYTM.RESPONSE_CODES.BAD_REQUEST:
                        case NetworkUtility.PAYTM.RESPONSE_CODES.LOGIN_FAILED:
                            listener.showGeneralizedErrorMessage();
                            break;
                        //invalid email not handled as email is not mandatory and we are not sending email
                        case NetworkUtility.PAYTM.RESPONSE_CODES.INVALID_EMAIL:
                            break;
                        case NetworkUtility.PAYTM.RESPONSE_CODES.INVALID_MOBILE:
                            // Show Toast
                            listener.paytmInvalidMobileNumber();
                            break;
                        case NetworkUtility.PAYTM.RESPONSE_CODES.ACCOUNT_BLOCKED:
                            //TODO: snackbar message to be changed in case required. now displaying generalized message of something went wrong
                            // Show Toast
                            listener.paytmAccountBlocked();
                            break;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        Map<String, String> headerParams = new HashMap<>();
        headerParams.put(NetworkUtility.PAYTM.PARAMETERS.ssotoken, mAccessToken);

        Map<String, String> bodyParamValue = new HashMap<>();
//        bodyParamValue.put(NetworkUtility.PAYTM.PARAMETERS.merchantGuid, BuildConfig.CLIENT_SECRET);
        bodyParamValue.put(NetworkUtility.PAYTM.PARAMETERS.mid, BuildConfig.SANDBOX_MERCHANT_ID);

        Map<String, JSONObject> bodyParams = new HashMap<>();
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.request, new JSONObject(bodyParamValue));

        final String requestString = new JSONObject(bodyParams).toString();

        PaytmNetworkRequest paytmNetworkRequest = new PaytmNetworkRequest(
                false,
                Request.Method.POST,
                NetworkUtility.PAYTM.CHECK_BALANCE_API,
                mCheckBalanceResponseListener,
                mCheckBalanceErrorListener,
                headerParams,
                requestString);
        Volley.getInstance(mContext).addToRequestQueue(paytmNetworkRequest, NetworkUtility.PAYTM.CHECK_BALANCE_API);*/
    }
    ///////////////////////////////////////////////////////Paytm Check Balance API call ends///////////////////////////////////////////////////////


    ///////////////////////////////////////////////////////Paytm Add Money API call starts///////////////////////////////////////////////////////
    public interface AddMoneyResponseListener {
        void paytmAddMoneySuccessResponse(String htmlResponse);

        void volleyError();
    }

    /////////////////////////////////////////////////////// Volley Checksum For Withdraw Money ///////////////////////////////////////////////////////
    public interface GetChecksumResponseListener {
        void volleyGetChecksumSuccessResponse(String checksumhash);

        void showSpecificErrorMessage(String errorMessage);

        void showGeneralizedErrorMessage();

        void volleyError();
    }


    public static String getChecksumForWithdrawMoney(Context mContext, String txnAmount, String mAccessToken, String mMobileNumber, String mResourceOwnerCustomerId, final GetChecksumResponseListener listener) {
        final Response.ErrorListener mGetChecksumErrorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");
                listener.volleyError();
            }
        };

        final Response.Listener mGetChecksumResponseListener = new Response.Listener() {
            @Override
            public void onResponse(Object response) {
                String strResponse = (String) response;
                try {
                    JSONObject jsonObject = new JSONObject(strResponse);
                    LogUtils.LOGD(TAG, "onResponse: " + jsonObject.toString());
                    int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                    switch (statusCode) {
                        case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
                            listener.volleyGetChecksumSuccessResponse(jsonObject.getJSONObject(NetworkUtility.TAGS.DATA).getString(NetworkUtility.TAGS.CHECKSUMHASH));
                            break;
                        case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                            listener.showGeneralizedErrorMessage();
                            break;
                        case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                            listener.showSpecificErrorMessage(jsonObject.getString(NetworkUtility.TAGS.MESSAGE));
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        String orderID = String.valueOf(System.currentTimeMillis());

        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().userID);

        Map<String, String> bodyParams = new HashMap<>();

        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.AppIP, Utility.getIPAddress(true));
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.AuthMode, Utility.USRPWD);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.Channel, BuildConfig.CHANNEL_ID);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.Currency, Utility.INR);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.CustId, mResourceOwnerCustomerId);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.DeviceId, mMobileNumber);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.IndustryType, BuildConfig.INDUSTRY_TYPE_ID);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.MID, BuildConfig.SANDBOX_MERCHANT_ID);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.OrderId, orderID);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.PaymentMode, Utility.PPI);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.ReqType, Utility.WITHDRAW);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.TxnAmount, txnAmount);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.SSOToken, mAccessToken);
        //PPI payment mode is for wallets
        //USRPWD authMode – for Wallet.

        @SuppressWarnings("unchecked")
        VolleyNetworkRequest volleyNetworkRequest = new VolleyNetworkRequest(
                NetworkUtility.WS.GET_CHECKSUM_HASH,
                mGetChecksumErrorListener,
                mGetChecksumResponseListener,
                mHeaderParams,
                bodyParams,
                null);
        Volley.getInstance(mContext).addToRequestQueue(volleyNetworkRequest, NetworkUtility.WS.GET_CHECKSUM_HASH);
        return orderID;
    }
    ///////////////////////////////////////////////////////Volley Checksum For Withdraw Money///////////////////////////////////////////////////////

    /////////////////////////////////////////////////////// Volley Checksum For Add Money ///////////////////////////////////////////////////////

    public static String getChecksumForAddMoney(Context mContext, String txnAmount, String mAccessToken, String mMobileNumber, String mResourceOwnerCustomerId, final GetChecksumResponseListener listener) {
        final Response.ErrorListener mGetChecksumErrorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");
                listener.volleyError();
            }
        };

        final Response.Listener mGetChecksumResponseListener = new Response.Listener() {
            @Override
            public void onResponse(Object response) {
                String strResponse = (String) response;
                try {
                    JSONObject jsonObject = new JSONObject(strResponse);
                    LogUtils.LOGD(TAG, "onResponse: " + jsonObject.toString());
                    int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                    switch (statusCode) {
                        case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
                            listener.volleyGetChecksumSuccessResponse(jsonObject.getJSONObject(NetworkUtility.TAGS.DATA).getString(NetworkUtility.TAGS.CHECKSUMHASH));
                            break;
                        case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                            listener.showGeneralizedErrorMessage();
                            break;
                        case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                            listener.showSpecificErrorMessage(jsonObject.getString(NetworkUtility.TAGS.MESSAGE));
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        String orderID = String.valueOf(System.currentTimeMillis());


        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().userID);


        Map<String, String> bodyParams = new HashMap<>();
        bodyParams.put(NetworkUtility.TAGS.ORDER_ID, orderID);
        bodyParams.put(NetworkUtility.TAGS.TXN_AMOUNT, txnAmount);
        bodyParams.put(NetworkUtility.TAGS.CUST_ID, mResourceOwnerCustomerId);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.CHANNEL_ID, BuildConfig.CHANNEL_ID);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.WEBSITE, BuildConfig.WEBSITE);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.INDUSTRY_TYPE_ID, BuildConfig.INDUSTRY_TYPE_ID);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.CALLBACK_URL, NetworkUtility.WS.VERIFY_CHECKSUM);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.SSO_TOKEN, mAccessToken);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.REQUEST_TYPE, Utility.ADD_MONEY);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.MID, BuildConfig.SANDBOX_MERCHANT_ID);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.MOBILE_NO, mMobileNumber);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.EMAIL, PreferenceUtility.getInstance(mContext).getUserDetails().email);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.THEME, Utility.MERCHANT);

        @SuppressWarnings("unchecked")
        VolleyNetworkRequest volleyNetworkRequest = new VolleyNetworkRequest(
                NetworkUtility.WS.GET_CHECKSUM_HASH,
                mGetChecksumErrorListener,
                mGetChecksumResponseListener,
                mHeaderParams,
                bodyParams,
                null);
        Volley.getInstance(mContext).addToRequestQueue(volleyNetworkRequest, NetworkUtility.WS.GET_CHECKSUM_HASH);
        return orderID;
    }
    /////////////////////////////////////////////////////// Volley Checksum For Add Money ///////////////////////////////////////////////////////


    ///////////////////////////////////////////////////////Volley save user details Web call starts///////////////////////////////////////////////////////
    public interface SavePaytmUserResponseListener {
        void volleySavePaytmUserSuccessResponse(String responseString);

        void showSpecificErrorMessage(String errorMessage);

        void showGeneralizedErrorMessage();

        void volleyError();
    }


    public static void savePaytmUserDetails(Context mContext, String mResourceOwnerCustomerId, String mAccessToken, String mobileNumber, final SavePaytmUserResponseListener listener, String methodType, String expiresTimeStamp) {

        final Response.ErrorListener mSavePaytmUserErrorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");
                listener.volleyError();
            }
        };

        final Response.Listener mSavePaytmUserResponseListener = new Response.Listener() {
            @Override
            public void onResponse(Object response) {
                String strResponse = (String) response;
                try {
                    JSONObject jsonObject = new JSONObject(strResponse);
                    LogUtils.LOGD(TAG, "onResponse: " + jsonObject.toString());
                    int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                    switch (statusCode) {
                        case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
                            listener.volleySavePaytmUserSuccessResponse(strResponse);
                            break;
                        case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                            listener.showGeneralizedErrorMessage();
                            break;
                        case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                            listener.showSpecificErrorMessage(jsonObject.getString(NetworkUtility.TAGS.MESSAGE));
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().userID);

        Map<String, String> bodyParams = new HashMap<>();
        bodyParams.put(NetworkUtility.TAGS.PAYTM_CUST_ID, mResourceOwnerCustomerId);
        bodyParams.put(NetworkUtility.TAGS.PAYTM_ACCESS_TOKEN, mAccessToken);
        bodyParams.put(NetworkUtility.TAGS.PAYTM_PHONE_NUMBER, mobileNumber);
        bodyParams.put(NetworkUtility.TAGS.PAYMENT_METHOD_TYPE_TAG, methodType);
        bodyParams.put(NetworkUtility.TAGS.ACCESS_TOKEN_EXPIRES_TIMESTAMP, expiresTimeStamp);

        @SuppressWarnings("unchecked")
        VolleyNetworkRequest volleyNetworkRequest = new VolleyNetworkRequest(
                NetworkUtility.WS.SAVE_PAYTM_USER_DETAILS,
                mSavePaytmUserErrorListener,
                mSavePaytmUserResponseListener,
                mHeaderParams,
                bodyParams,
                null);
        Volley.getInstance(mContext).addToRequestQueue(volleyNetworkRequest, NetworkUtility.WS.SAVE_PAYTM_USER_DETAILS);
    }
    ///////////////////////////////////////////////////////Volley save user details Web call ends///////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////Paytm Withdraw Money API call starts///////////////////////////////////////////////////////
    public interface WithdrawMoneyResponseListener {
        void paytmWithdrawMoneySuccessResponse(String htmlResponse);

        void volleyError();
    }

    public static void withdrawMoney(Context mContext,
                                     String generatedOrderId,
                                     String mAccessToken,
                                     String amount,
                                     String mChecksumHash,
                                     String mResourceOwnerCustomerId,
                                     String mMobileNumber,
                                     final WithdrawMoneyResponseListener listener) {

        final Response.ErrorListener mWithdrawMoneyErrorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");
                listener.volleyError();
            }
        };

        final Response.Listener mWithdrawMoneyResponseListener = new Response.Listener() {
            @Override
            public void onResponse(Object response) {
                try {
                    JSONObject jRoot = new JSONObject(response.toString());
                    JSONObject data = jRoot.getJSONObject(NetworkUtility.TAGS.DATA);
                    JSONObject paytmResponseData = data.getJSONObject(NetworkUtility.TAGS.PAYTM_RESPONSE_DATA);
                    listener.paytmWithdrawMoneySuccessResponse(paytmResponseData.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().userID);

        Map<String, String> bodyParamValue = new HashMap<>();
        bodyParamValue.put(NetworkUtility.PAYTM.PARAMETERS.MID, BuildConfig.SANDBOX_MERCHANT_ID);
        bodyParamValue.put(NetworkUtility.PAYTM.PARAMETERS.ReqType, Utility.WITHDRAW);
        bodyParamValue.put(NetworkUtility.PAYTM.PARAMETERS.TxnAmount, amount);
        bodyParamValue.put(NetworkUtility.PAYTM.PARAMETERS.AppIP, Utility.getIPAddress(true));
        bodyParamValue.put(NetworkUtility.PAYTM.PARAMETERS.OrderId, generatedOrderId);
        bodyParamValue.put(NetworkUtility.PAYTM.PARAMETERS.Currency, Utility.INR);
        bodyParamValue.put(NetworkUtility.PAYTM.PARAMETERS.DeviceId, mMobileNumber);
        bodyParamValue.put(NetworkUtility.PAYTM.PARAMETERS.SSOToken, mAccessToken);
        //PPI payment mode is for wallets
        bodyParamValue.put(NetworkUtility.PAYTM.PARAMETERS.PaymentMode, Utility.PPI);
        bodyParamValue.put(NetworkUtility.PAYTM.PARAMETERS.CustId, mResourceOwnerCustomerId);
        bodyParamValue.put(NetworkUtility.PAYTM.PARAMETERS.IndustryType, BuildConfig.INDUSTRY_TYPE_ID);
        bodyParamValue.put(NetworkUtility.PAYTM.PARAMETERS.Channel, BuildConfig.CHANNEL_ID);
        //USRPWD authMode – for Wallet.
        bodyParamValue.put(NetworkUtility.PAYTM.PARAMETERS.AuthMode, Utility.USRPWD);
        bodyParamValue.put(NetworkUtility.PAYTM.PARAMETERS.CheckSum, mChecksumHash);
//        bodyParamValue.put(NetworkUtility.PAYTM.PARAMETERS.CALLBACK_URL, NetworkUtility.WS.VERIFY_CHECKSUM);
//        bodyParamValue.put(NetworkUtility.PAYTM.PARAMETERS.EMAIL, PreferenceUtility.getInstance(mContext).getUserDetails().email);
        @SuppressWarnings("unchecked")
        VolleyNetworkRequest volleyNetworkRequest = new VolleyNetworkRequest(
                NetworkUtility.WS.PAYTM_WITHDRAW_MONEY,
                mWithdrawMoneyErrorListener,
                mWithdrawMoneyResponseListener,
                mHeaderParams,
                bodyParamValue,
                null);
        Volley.getInstance(mContext).addToRequestQueue(volleyNetworkRequest, NetworkUtility.WS.PAYTM_WITHDRAW_MONEY);

        /*Response.ErrorListener mWithdrawMoneyErrorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                LogUtils.LOGD(TAG, "onErrorResponse() called with: error = [" + error + "]");
                listener.volleyError();
            }
        };

        Response.Listener<String> mWithdrawMoneyResponseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                LogUtils.LOGD(TAG, "onResponse() of add money called with: response = [" + response + "]");
                listener.paytmWithdrawMoneySuccessResponse(response);
            }
        };

        Map<String, String> bodyParamValue = new HashMap<>();
        bodyParamValue.put(NetworkUtility.PAYTM.PARAMETERS.MID, BuildConfig.SANDBOX_MERCHANT_ID);
        bodyParamValue.put(NetworkUtility.PAYTM.PARAMETERS.ReqType, Utility.WITHDRAW);
        bodyParamValue.put(NetworkUtility.PAYTM.PARAMETERS.TxnAmount, amount);
        bodyParamValue.put(NetworkUtility.PAYTM.PARAMETERS.AppIP, Utility.getIPAddress(true));
        bodyParamValue.put(NetworkUtility.PAYTM.PARAMETERS.OrderId, generatedOrderId);
        bodyParamValue.put(NetworkUtility.PAYTM.PARAMETERS.Currency, Utility.INR);
        bodyParamValue.put(NetworkUtility.PAYTM.PARAMETERS.DeviceId, mMobileNumber);
        bodyParamValue.put(NetworkUtility.PAYTM.PARAMETERS.SSOToken, mAccessToken);
        //PPI payment mode is for wallets
        bodyParamValue.put(NetworkUtility.PAYTM.PARAMETERS.PaymentMode, Utility.PPI);
        bodyParamValue.put(NetworkUtility.PAYTM.PARAMETERS.CustId, mResourceOwnerCustomerId);
        bodyParamValue.put(NetworkUtility.PAYTM.PARAMETERS.IndustryType, BuildConfig.INDUSTRY_TYPE_ID);
        bodyParamValue.put(NetworkUtility.PAYTM.PARAMETERS.Channel, BuildConfig.CHANNEL_ID);
        //USRPWD authMode – for Wallet.
        bodyParamValue.put(NetworkUtility.PAYTM.PARAMETERS.AuthMode, Utility.USRPWD);
        bodyParamValue.put(NetworkUtility.PAYTM.PARAMETERS.CheckSum, mChecksumHash);
//        bodyParamValue.put(NetworkUtility.PAYTM.PARAMETERS.CALLBACK_URL, NetworkUtility.WS.VERIFY_CHECKSUM);
//        bodyParamValue.put(NetworkUtility.PAYTM.PARAMETERS.EMAIL, PreferenceUtility.getInstance(mContext).getUserDetails().email);

        Map<String, JSONObject> bodyParam = new HashMap<>();
        bodyParam.put(NetworkUtility.PAYTM.PARAMETERS.JsonData, new JSONObject(bodyParamValue));

        final String requestString = new JSONObject(bodyParam).toString();

        @SuppressWarnings("unchecked")
        PaytmNetworkRequest paytmNetworkRequest = new PaytmNetworkRequest(
                true,
                Request.Method.POST,
                NetworkUtility.PAYTM.WALLET_APIS.WITHDRAW_MONEY,
                mWithdrawMoneyResponseListener,
                mWithdrawMoneyErrorListener,
                null,
                requestString);
        Volley.getInstance(mContext).addToRequestQueue(paytmNetworkRequest, NetworkUtility.PAYTM.WALLET_APIS.WITHDRAW_MONEY);*/
    }
    ///////////////////////////////////////////////////////Paytm Withdraw Money API call ends///////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////Paytm Get Txn Status API call starts///////////////////////////////////////////////////////
    public interface GetTxnStatusResponseListener {
        void paytmGetTxnStatusSuccessResponse(String htmlResponse);

        void volleyError();
    }

    public static void getTxnStatus(Context mContext,
                                    String generatedOrderId,
                                    String mChecksumHash,
                                    final WithdrawMoneyResponseListener listener) {

        Response.ErrorListener mGetTxnStatusErrorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                LogUtils.LOGD(TAG, "onErrorResponse() called with: error = [" + error + "]");
                listener.volleyError();
            }
        };

        Response.Listener<String> mGetTxnStatusResponseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                LogUtils.LOGD(TAG, "onResponse() of add money called with: response = [" + response + "]");
                listener.paytmWithdrawMoneySuccessResponse(response);
            }
        };

        Map<String, String> bodyParamValue = new HashMap<>();
        bodyParamValue.put(NetworkUtility.PAYTM.PARAMETERS.MID, BuildConfig.SANDBOX_MERCHANT_ID);
//        bodyParamValue.put(NetworkUtility.PAYTM.PARAMETERS.ReqType, Utility.WITHDRAW);
//        bodyParamValue.put(NetworkUtility.PAYTM.PARAMETERS.TxnAmount, mEtText);
//        bodyParamValue.put(NetworkUtility.PAYTM.PARAMETERS.AppIP, Utility.getIPAddress(true));
        bodyParamValue.put(NetworkUtility.PAYTM.PARAMETERS.ORDERID, generatedOrderId);
//        bodyParamValue.put(NetworkUtility.PAYTM.PARAMETERS.Currency, Utility.INR);
//        bodyParamValue.put(NetworkUtility.PAYTM.PARAMETERS.DeviceId, mMobileNumber);
//        bodyParamValue.put(NetworkUtility.PAYTM.PARAMETERS.SSOToken, mAccessToken);
        //PPI payment mode is for wallets
//        bodyParamValue.put(NetworkUtility.PAYTM.PARAMETERS.PaymentMode, Utility.PPI);
//        bodyParamValue.put(NetworkUtility.PAYTM.PARAMETERS.CustId, mResourceOwnerCustomerId);
//        bodyParamValue.put(NetworkUtility.PAYTM.PARAMETERS.IndustryType, BuildConfig.INDUSTRY_TYPE_ID);
//        bodyParamValue.put(NetworkUtility.PAYTM.PARAMETERS.Channel, BuildConfig.CHANNEL_ID);
        //USRPWD authMode – for Wallet.
//        bodyParamValue.put(NetworkUtility.PAYTM.PARAMETERS.AuthMode, Utility.USRPWD);
        bodyParamValue.put(NetworkUtility.PAYTM.PARAMETERS.CHECKSUMHASH, mChecksumHash);
//        bodyParamValue.put(NetworkUtility.PAYTM.PARAMETERS.CALLBACK_URL, NetworkUtility.WS.VERIFY_CHECKSUM);
//        bodyParamValue.put(NetworkUtility.PAYTM.PARAMETERS.EMAIL, PreferenceUtility.getInstance(mContext).getUserDetails().email);

        Map<String, JSONObject> bodyParam = new HashMap<>();
        bodyParam.put(NetworkUtility.PAYTM.PARAMETERS.JsonData, new JSONObject(bodyParamValue));

        final String requestString = new JSONObject(bodyParam).toString();

        @SuppressWarnings("unchecked")
        PaytmNetworkRequest paytmNetworkRequest = new PaytmNetworkRequest(
                true,
                Request.Method.POST,
                NetworkUtility.PAYTM.WALLET_APIS.ADD_MONEY,
                mGetTxnStatusResponseListener,
                mGetTxnStatusErrorListener,
                null,
                requestString);
        Volley.getInstance(mContext).addToRequestQueue(paytmNetworkRequest, NetworkUtility.PAYTM.WALLET_APIS.ADD_MONEY);
    }

    ///////////////////////////////////////////////////////Paytm Get Txn Status API call ends///////////////////////////////////////////////////////


    /////////////////////////////////////////////////////// On Paytm Transaction response(Call to Our Server) ///////////////////////////////////////
    public interface VerifyTransactionMoneyResponseListener {
        void paytmVerifyTransactionMoneyResponse(String responseInJson);

        void volleyError();
    }

    public static void callVerifyOrderTransaction(Context mContext,
                                                  String generatedOrderId,
                                                  final VerifyTransactionMoneyResponseListener listener) {

        // Erro Listener
        Response.ErrorListener mVerifyTransactionMoneyErrorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");
                listener.volleyError();
            }
        };

        // Response Listener
        Response.Listener mVerifyTransactionMoneyResponseListener = new Response.Listener() {
            @Override
            public void onResponse(Object response) {
                Log.d(TAG, "onResponse() called with: response = [" + response + "]");
                listener.paytmVerifyTransactionMoneyResponse(response.toString());
            }
        };

        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().userID);


        // Generate Parameters
        Map<String, String> params = new HashMap<>();
        params.put(NetworkUtility.PAYTM.PARAMETERS.order_id, generatedOrderId);

        @SuppressWarnings("unchecked")
        VolleyNetworkRequest paytmNetworkRequest = new VolleyNetworkRequest(
                NetworkUtility.WS.FETCH_CALLBACK_RESPONSE_FROM_PAYTM,
                mVerifyTransactionMoneyErrorListener,
                mVerifyTransactionMoneyResponseListener,
                mHeaderParams,
                params,
                null);
        Volley.getInstance(mContext).addToRequestQueue(paytmNetworkRequest, NetworkUtility.WS.FETCH_CALLBACK_RESPONSE_FROM_PAYTM);
    }
/////////////////////////////////////////////////////// On Paytm Transaction response(Call to Our Server) ///////////////////////////////////////


    /////////////////////////////////////////////////////// Volley Checksum For SUBSCRIPTION ///////////////////////////////////////////////////////
    // TODO: PAYTM SUBSCRIPTION
    public static String getChecksumForSubscription(Context mContext, String txnAmount, String mAccessToken, String mMobileNumber, String mResourceOwnerCustomerId, final GetChecksumResponseListener listener) {
        final Response.ErrorListener mGetChecksumErrorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");
                listener.volleyError();
            }
        };

        final Response.Listener mGetChecksumResponseListener = new Response.Listener() {
            @Override
            public void onResponse(Object response) {
                String strResponse = (String) response;
                try {
                    JSONObject jsonObject = new JSONObject(strResponse);
                    LogUtils.LOGD(TAG, "onResponse: " + jsonObject.toString());
                    int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                    switch (statusCode) {
                        case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
                            listener.volleyGetChecksumSuccessResponse(jsonObject.getJSONObject(NetworkUtility.TAGS.DATA).getString(NetworkUtility.TAGS.CHECKSUMHASH));
                            break;
                        case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                            listener.showGeneralizedErrorMessage();
                            break;
                        case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                            listener.showSpecificErrorMessage(jsonObject.getString(NetworkUtility.TAGS.MESSAGE));
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        String orderID = String.valueOf(System.currentTimeMillis());


        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().userID);


        Map<String, Object> bodyParams = new HashMap<>();
        bodyParams.put(NetworkUtility.TAGS.ORDER_ID, orderID);
        bodyParams.put(NetworkUtility.TAGS.TXN_AMOUNT, txnAmount);
        bodyParams.put(NetworkUtility.TAGS.CUST_ID, mResourceOwnerCustomerId);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.CHANNEL_ID, BuildConfig.CHANNEL_ID);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.WEBSITE, BuildConfig.WEBSITE);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.INDUSTRY_TYPE_ID, BuildConfig.INDUSTRY_TYPE_ID);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.CALLBACK_URL, NetworkUtility.WS.VERIFY_CHECKSUM);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.SSO_TOKEN, mAccessToken);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.REQUEST_TYPE, Utility.SUBSCRIBE);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.MID, BuildConfig.SANDBOX_MERCHANT_ID);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.MOBILE_NO, mMobileNumber);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.EMAIL, PreferenceUtility.getInstance(mContext).getUserDetails().email);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.THEME, Utility.MERCHANT);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.SUBS_SERVICE_ID, "123");
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.SUBS_AMOUNT_TYPE, "FIX");
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.SUBS_FREQUENCY, 1);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.SUBS_FREQUENCY_UNIT, "YEAR");
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.SUBS_EXPIRY_DATE, "2028-03-29");
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.SUBS_ENABLE_RETRY, 1);

        @SuppressWarnings("unchecked")
        VolleyNetworkRequest volleyNetworkRequest = new VolleyNetworkRequest(
                NetworkUtility.WS.GET_CHECKSUM_HASH,
                mGetChecksumErrorListener,
                mGetChecksumResponseListener,
                mHeaderParams,
                bodyParams,
                null);
        Volley.getInstance(mContext).addToRequestQueue(volleyNetworkRequest, NetworkUtility.WS.GET_CHECKSUM_HASH);
        return orderID;
    }
    /////////////////////////////////////////////////////// Volley Checksum For SUBSCRIPTION ///////////////////////////////////////////////////////
    /////////////////////////////////////////////////////// Volley Checksum For Add Money ///////////////////////////////////////////////////////

    // TODO: PAYTM SUBSCRIPTION


    public static String autoRenewal(Context mContext, String txnAmount, String subsId, final GetChecksumResponseListener listener) {
        final Response.ErrorListener mGetChecksumErrorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");
                listener.volleyError();
            }
        };

        final Response.Listener mGetChecksumResponseListener = new Response.Listener() {
            @Override
            public void onResponse(Object response) {
                String strResponse = (String) response;
                try {
                    JSONObject jsonObject = new JSONObject(strResponse);
                    LogUtils.LOGD(TAG, "onResponse: " + jsonObject.toString());
                    int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                    switch (statusCode) {
                        case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
                            listener.volleyGetChecksumSuccessResponse(jsonObject.getJSONObject(NetworkUtility.TAGS.DATA).getString(NetworkUtility.TAGS.CHECKSUMHASH));
                            break;
                        case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                            listener.showGeneralizedErrorMessage();
                            break;
                        case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                            listener.showSpecificErrorMessage(jsonObject.getString(NetworkUtility.TAGS.MESSAGE));
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        String orderID = String.valueOf(System.currentTimeMillis());


        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().userID);


        Map<String, Object> bodyParams = new HashMap<>();
        bodyParams.put(NetworkUtility.TAGS.ORDER_ID, orderID);
        bodyParams.put(NetworkUtility.TAGS.TXN_AMOUNT, txnAmount);
        bodyParams.put(NetworkUtility.TAGS.SUBS_ID, subsId);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.MID, BuildConfig.SANDBOX_MERCHANT_ID);
        bodyParams.put(NetworkUtility.PAYTM.PARAMETERS.REQUEST_TYPE, Utility.RENEW_SUBSCRIPTION);

        @SuppressWarnings("unchecked")
        VolleyNetworkRequest volleyNetworkRequest = new VolleyNetworkRequest(
                NetworkUtility.WS.GET_CHECKSUM_HASH,
                mGetChecksumErrorListener,
                mGetChecksumResponseListener,
                mHeaderParams,
                bodyParams,
                null);
        Volley.getInstance(mContext).addToRequestQueue(volleyNetworkRequest, NetworkUtility.WS.GET_CHECKSUM_HASH);
        return orderID;
    }
    /////////////////////////////////////////////////////// Volley Checksum For Add Money ///////////////////////////////////////////////////////


}
