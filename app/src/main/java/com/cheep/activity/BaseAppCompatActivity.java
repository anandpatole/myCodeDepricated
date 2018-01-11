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

import com.cheep.R;
import com.cheep.interfaces.LocationTrackServiceInteractionListener;
import com.cheep.services.LocationTrackService;
import com.cheep.utils.Utility;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.Status;


/**
 * Created by Bhavesh Patadiya on 25/5/15.
 * A base activity that handles common functionality in the app. This includes
 * 1. Location tracking service and its callback to child class
 * 2. Progress Dialog showing/hiding.
 * 3. Abstract methods which child class can override and react accordingly.
 * 4. Ameyo API Integration.(Not using anymore- Commented)
 */
public abstract class BaseAppCompatActivity extends AppCompatActivity {

    //Contants
    private static final String TAG = BaseAppCompatActivity.class.getSimpleName();
    public static final int RESULT_CODE_GOOGLE_PLAY_SERVICE_RESOLVE = 101;

    // All child class can use the context initialize by @BaseAppCompatActivity class
    protected Context mContext;

    // Need to manage and track Location service which would be running in background.
    public LocationTrackService mLocationTrackService;

    // Whether Service is bound or not.
    private boolean mBound = false;

    // Instance of Progress dialog
    private ProgressDialog mProgressDialog;

    // Abstract methods
    protected abstract void initiateUI();

    protected abstract void setListeners();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = this;

        // App would do checking google play service installed properly in device and if yes will
        // start @LocationTrackService which would track users location. All this would be an
        // asynchroneous process
        new AsyncCheckGooglePlayService(mContext).execute();
    }


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
     * This method will check whether currently LocationTrackService is bind to the activity or not
     *
     * @return whether service is bind or note
     */
    protected boolean checkLocationServiceBind() {
        return mBound;
    }


    /**
     * Managing Interface[@{@link ServiceConnection} to get callback from @{@link LocationTrackService}
     */
    ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // Initialize instance of @LocationTrackService which child classes can use.
            LocationTrackService.LocationTrackBinder mBinder = (LocationTrackService.LocationTrackBinder) service;
            mLocationTrackService = mBinder.getService();
            mLocationTrackService.addCallback(mLocationTrackServiceInteractionListener);

            // @LocationTrackService is connected so setting @mBound boolean to true
            mBound = true;

            // Provide callback to child class in case of other extra processing or work it wanted to do.
            onBindLocationTrackService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // Service is disconnected so make the boolean to false.
            mBound = false;
        }

    };


    /**
     * Managing Interface @{@link LocationTrackServiceInteractionListener} for getting callback when
     * Location available/updated. Callback methods would pass this to child class upon which child classes can react.
     */
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


    /**
     * Some Concrete methods which eventually called by @mLocationTrackServiceInteractionListener
     * and child class can process accordingly.
     */
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

    /**
     * Start LocationTracking Service
     */
    public void bindLocationTrackService() {
        //Bind service
        Intent intent = new Intent(mContext, LocationTrackService.class);
//        startService(intent);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * Once @LocationTrackService starts this method would request for location update.
     */
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
    public void showProgressDialog() {
        showProgressDialog(getString(R.string.label_please_wait));
    }

    /**
     * Close Progress Dialog
     */
    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        mProgressDialog = null;
    }


    /**
     * This will be getting used by below classes
     */
    protected void onGooglePlayServiceCheckedSuccessfully() {

    }


    /**
     * Inner class which would do the processing for checking google play service & binding/starting
     * Location Service
     */
    private class AsyncCheckGooglePlayService extends AsyncTask<Void, Void, Integer> {
        private Context mContext;

        AsyncCheckGooglePlayService(Context context) {
            this.mContext = context;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            // Return whether google play service is
            return Utility.checkGooglePlayService(mContext);
        }

        @Override
        protected void onPostExecute(Integer error_code) {
            super.onPostExecute(error_code);
            // Checking for the success. We will also go ahead in case google playservice needs an update required.
            if (error_code == ConnectionResult.SUCCESS || error_code == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED) {
                // providing callback to child class
                onGooglePlayServiceCheckedSuccessfully();

                // Start/Bind the location service
                bindLocationTrackService();
            } else {
                // We found some error while checking google playservice,so checking whether its
                // resolvable or not.
                boolean isResolvable = GoogleApiAvailability.getInstance().isUserResolvableError(error_code);
                if (isResolvable) {
                    // Show error message to the user
                    GoogleApiAvailability
                            .getInstance()
                            .showErrorDialogFragment(BaseAppCompatActivity.this, error_code, RESULT_CODE_GOOGLE_PLAY_SERVICE_RESOLVE);
                }
            }
        }
    }


    /************************************************************************************************
     *********************** [Call to Users using Ameyo API] [Start] ********************************
     * According to Previous flow app uses Ameyo API in order to call to other users. However, after*
     * new flow discussed with @cheep team, app is not using anymore. In case in future, we need to *
     * use it we can uncomment the below code. ******************************************************
     * **********************************************************************************************
     ***********************************************************************************************/
    /*public void callToCheepAdmin(View view) {
        if (view == null)
            return;
        emuCall(view, Utility.EMPTY_STRING, true);
    }*/

    /*public void callToOtherUser(View view, String sp_user_id) {
        if (view == null || TextUtils.isEmpty(sp_user_id))
            return;
        emuCall(view, sp_user_id, false);
    }*/

    /*public void emuCall(final View view, String sp_user_id, boolean isAdminCall) {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, view);
            return;
        }

        //Show Progress
        showProgressDialog();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);

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
    }*/

    /***********************************************************************************************
     *********************** [Call to Users using Ameyo API] [End] ********************************
     ***********************************************************************************************/

}