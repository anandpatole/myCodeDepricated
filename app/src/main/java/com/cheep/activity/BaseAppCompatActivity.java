package com.cheep.activity;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.R;
import com.cheep.interfaces.LocationTrackServiceInteractionListener;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.services.LocationTrackService;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.Status;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by Bhavesh Patadiya on 25/5/15.
 */
public abstract class BaseAppCompatActivity extends AppCompatActivity {
    private static final String TAG = "BaseAppCompatActivity";
    protected Context mContext;
    public LocationTrackService mLocationTrackService;
    private boolean mBound = false;
    public static final int RESULT_CODE_GOOGLE_PLAY_SERVICE_RESOLVE = 101;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = this;

        //Check for Google PlayService
        new AsyncCheckGooglePlayService().execute();
    }

    protected abstract void initiateUI();

    protected abstract void setListeners();

    @Override
    protected void onDestroy() {

        //Unbind the service in onStope()
        if (mBound) {
            //Unbind the service
            unbindService(mServiceConnection);
            if (mLocationTrackService != null) {
                mLocationTrackService.removeCallback(mLocationTrackServiceInteractionListener);
            }
        }
        //make ProgressDialog as null
        mProgressDialog = null;
        super.onDestroy();
    }

    /**
     * Service Connection
     */
    ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected() called with: name = [" + name + "], service = [" + service + "]");
            LocationTrackService.LocationTrackBinder mBinder = (LocationTrackService.LocationTrackBinder) service;
            mLocationTrackService = mBinder.getService();
            mLocationTrackService.addCallback(mLocationTrackServiceInteractionListener);
            mBound = true;
            onBindLocationTrackService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected() called with: name = [" + name + "]");
            mBound = false;
        }

    };

    /**
     * This method will check whether currently LocationTrackService is bind to the activity or not
     *
     * @return whether service is bind or note
     */

    protected boolean checkLocationServiceBind() {
        return mBound;
    }

    LocationTrackServiceInteractionListener mLocationTrackServiceInteractionListener = new LocationTrackServiceInteractionListener() {
        @Override
        public void onLocationNotAvailable() {
            BaseAppCompatActivity.this.onLocationNotAvailable();
        }

        @Override
        public void onLocationFetched(Location mLocation) {
            BaseAppCompatActivity.this.onLocationFetched(mLocation);
        }

        @Override
        public void onLocationSettingsDialogNeedToBeShow(Status locationRequest) {
            BaseAppCompatActivity.this.onLocationSettingsDialogNeedToBeShow(locationRequest);
        }

        @Override
        public void gpsEnabled() {
            BaseAppCompatActivity.this.gpsEnabled();
        }
    };

    protected void onLocationNotAvailable() {
    }

    protected void onLocationFetched(Location mLocation) {
    }

    public void onBindLocationTrackService() {
    }

    public void onLocationSettingsDialogNeedToBeShow(Status locationRequest) {

    }

    public void gpsEnabled() {

    }

    public void bindLocationTrackService() {
        //Bind service
        Intent intent = new Intent(mContext, LocationTrackService.class);
//        startService(intent);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public void requestLocationUpdateFromService() {
        if (mLocationTrackService != null) {
            mLocationTrackService.requestLocationUpdate();
        }
    }

    /**
     * Show Progress Dialog
     */
    protected void showProgressDialog(String message) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setMessage(message);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }
        mProgressDialog.show();
    }

    /**
     * Show Progress Dialog
     */
    protected void showProgressDialog() {
        showProgressDialog(getString(R.string.label_please_wait));
    }

    /**
     * Close Progress Dialog
     */
    protected void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        mProgressDialog = null;
    }

    private class AsyncCheckGooglePlayService extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {
            return Utility.checkGooglePlayService(mContext);
        }

        @Override
        protected void onPostExecute(Integer error_code) {
            super.onPostExecute(error_code);
            if (error_code == ConnectionResult.SUCCESS || error_code == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED) {
                onGooglePlayServiceCheckedSuccessfully();
                bindLocationTrackService();
            } else {
                boolean isResolvable = GoogleApiAvailability.getInstance().isUserResolvableError(error_code);
                if (isResolvable) {
                    GoogleApiAvailability.getInstance().showErrorDialogFragment(BaseAppCompatActivity.this, error_code, RESULT_CODE_GOOGLE_PLAY_SERVICE_RESOLVE);
                }
            }
        }
    }

    /**
     * This will be getting used by below classes
     */
    protected void onGooglePlayServiceCheckedSuccessfully() {

    }

    public void callToCheepAdmin(View view) {
        if (view == null)
            return;
        emuCall(view, "", true);
    }

    public void callToOtherUser(View view, String sp_user_id) {
        if (view == null || TextUtils.isEmpty(sp_user_id))
            return;
        emuCall(view, sp_user_id, false);
    }

    public void emuCall(final View view, String sp_user_id, boolean isAdminCall) {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(getString(R.string.no_internet), view);
            return;
        }

        //Show Progress
        showProgressDialog();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);

        //Add Params
        //Add Params
        Map<String, String> mParams = new HashMap<>();
        if (!isAdminCall) {
            mParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);
            mParams.put(NetworkUtility.TAGS.SP_USER_ID, sp_user_id);
        }

        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(isAdminCall == true ? NetworkUtility.WS.CALL_TO_ADMIN : NetworkUtility.WS.CALL_TO_OTHER
                , new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");
                // Close Progressbar
                hideProgressDialog();

                Utility.showToast(mContext, getString(R.string.desc_shortly_receive_callback));
            }
        }
                , new Response.Listener() {
            @Override
            public void onResponse(Object response) {
                Log.d(TAG, "onResponse() called with: response = [" + response + "]");
                hideProgressDialog();

                Utility.showToast(mContext, getString(R.string.desc_shortly_receive_callback));
            }
        }
                , mHeaderParams
                , isAdminCall == true ? null : mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest, isAdminCall == true ? NetworkUtility.WS.CALL_TO_ADMIN : NetworkUtility.WS.CALL_TO_OTHER);
    }
}