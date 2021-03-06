package com.cheep.network;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.JsonSyntaxException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by pankaj on 9/15/17.
 */

public class PaytmNetworkRequest extends StringRequest {

    private static final String TAG = PaytmNetworkRequest.class.getSimpleName();
    private static final int DEFAULT_TIMEOUT_MS = 20000;
    private static final int DEFAULT_MAX_RETRIES = 0;
    private final Response.Listener<String> mListener;
    private final Map<String, String> mHeaderParams;
    private final String mBodyParams;
    private final boolean mIsWalletApi;
    private Map<String, File> mFileParams;
    private int mHttpResponseCode;

    public PaytmNetworkRequest(boolean isWalletApi,
                               int method,
                               String url,
                               Response.Listener<String> listener,
                               Response.ErrorListener errorListener,
                               Map<String, String> headerParams,
                               String bodyParams) {
        super(method, url, listener, errorListener);
        Log.d(TAG, "PaytmNetworkRequest() called with: method = [" + method + "], url = [" + url + "], listener = [" + listener + "], errorListener = [" + errorListener + "], headerParams = [" + headerParams + "], bodyParams = [" + bodyParams + "]");
        setRetryPolicy(new DefaultRetryPolicy(DEFAULT_TIMEOUT_MS,
                DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mIsWalletApi = isWalletApi;
        mListener = listener;
        mHeaderParams = headerParams;
        mBodyParams = bodyParams;
    }

    public PaytmNetworkRequest(boolean isWalletApi,
                               int method,
                               String url,
                               Response.Listener<String> listener,
                               Response.ErrorListener errorListener,
                               Map<String, String> headerParams,
                               String bodyParams,
                               Map<String, File> fileParams) {
        super(method, url, listener, errorListener);
        Log.d(TAG, "PaytmNetworkRequest() called with: method = [" + method + "], url = [" + url + "], listener = [" + listener + "], errorListener = [" + errorListener + "], headerParams = [" + headerParams + "], bodyParams = [" + bodyParams + "]");
        setRetryPolicy(new DefaultRetryPolicy(DEFAULT_TIMEOUT_MS,
                DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        this.mIsWalletApi = isWalletApi;
        mListener = listener;
        mHeaderParams = headerParams;
        mBodyParams = bodyParams;
        mFileParams = fileParams;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        if (mHeaderParams == null)
            return super.getHeaders();
        else
            return mHeaderParams;
    }

    @Override
    public String getBodyContentType() {
        if (mIsWalletApi)
            return "application/x-www-form-urlencoded; charset=utf-8";
        return "application/json; charset=utf-8";
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        if (!mIsWalletApi) {
            if (mBodyParams != null) {
//            final String request = new JSONObject(mBodyParams).toString();
                try {
                    return mBodyParams.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", mBodyParams, "utf-8");
                    return null;
                }
            } else {
                return null;
            }
        } else {
            return super.getBody();
        }
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        if (mIsWalletApi) {
            HashMap<String, String> map = new HashMap<>();
            JSONObject jObject = null;
            try {
                jObject = new JSONObject(mBodyParams);
                Iterator<?> keys = jObject.keys();

                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    String value = jObject.getString(key);
                    map.put(key, value);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return map;
        } else {
            return super.getParams();
        }
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
//        new HttpResponseCodeDeliverer(response.statusCode);
        try {
            String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            Log.e(TAG, "parseNetworkResponse: = " + json);
            return Response.success(json, HttpHeaderParser.parseCacheHeaders(response)); // it will return String
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JsonSyntaxException e) {
            return Response.error(new ParseError(e));
        } catch (Exception e) {
            return Response.error(new ParseError(e));
        }
    }

//    public static class HttpResponseCodeDeliverer{
//        public static int mHttpResponseCode;
//        HttpResponseCodeDeliverer(int httpResponseCode){
//            mHttpResponseCode = httpResponseCode;
//        }
//    }

    @Override
    protected void deliverResponse(String response) {
        mListener.onResponse(response);
    }
}
