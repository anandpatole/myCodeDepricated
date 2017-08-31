package com.cheep.services;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.cheep.interfaces.LocationTrackServiceInteractionListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.lang.ref.WeakReference;
import java.util.ArrayList;


/**
 * Class which will keep track of location and notify the relavent activities when needed
 */
public class LocationTrackService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "LocationTrackService";

    //Location Request Parameters
    /**
     * This method sets the rate in milliseconds at which your app prefers to receive location updates.
     * Note that the location updates may be faster than this rate if another app is receiving updates at a faster rate,
     * or slower than this rate, or there may be no updates at all (if the device has no connectivity, for example).
     */
//    public static final int LOCATION_REQUEST_INTERVAL = 2 * 60 * 1000; // 2 minutes
    public static final int LOCATION_REQUEST_INTERVAL = 60000;

    /**
     * This method sets the fastest rate in milliseconds at which your app can handle location updates.
     * You need to set this rate because other apps also affect the rate at which updates are sent.
     * The Google Play services location APIs send out updates at the fastest rate that any app has requested with setInterval().
     * If this rate is faster than your app can handle, you may encounter problems with UI flicker or data overflow.
     * To prevent this, call setFastestInterval() to set an upper limit to the update rate.
     */
//    public static final int LOCATION_REQUEST_FASTEST_INTERVAL = 1 * 60 * 1000; // 1 minutes
    public static final int LOCATION_REQUEST_FASTEST_INTERVAL = 30000;
    public static final int LOCATION_REQUEST_SMALLEST_DISPLACEMENT = 500;

    private LocationRequest mLocationRequest;

    public static final int LOCATION_PERMISSION_NOT_GRANTED = 1;
    public static final int LOCATION_NOT_AVAILABLE = 2;
    public static final int LOCATION_FETCHED = 3;
    public static final int LOCATION_SETTINGS_DIALOG_NEEDS_TOBE_SHOWN = 4;
    public static final int GPS_ENABLED = 5;


    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;
    //Binder instance
    private final LocationTrackBinder mBinder = new LocationTrackBinder();
    private ArrayList<LocationTrackServiceInteractionListener> mCallBacks = new ArrayList<>();

    public Location mLocation;

    private MyHandler mHandler;


    //Empty Constructor
    public LocationTrackService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Build Google API Client
        buildGoogleApiClient();

        createLocationRequest();

        //Connect with Google API Client
        mGoogleApiClient.connect();

        //Initiate Handler
        mHandler = new MyHandler(this, mCallBacks);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        //Return the communication event of service, in our case its iBinder object
        return mBinder;
    }

    /**
     * Create Binder here
     */
    public class LocationTrackBinder extends Binder {

        /**
         * This is method so that we can get service object from LocationtrackBinder to call public methods of service
         *
         * @return instance of LocationTrackService
         */
        public LocationTrackService getService() {
            // Return this instance of LocalService so clients can call public methods
            return LocationTrackService.this;
        }
    }

    /**
     * Builds a GoogleApiClient. Uses the addApi() method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected() called with: bundle = [" + bundle + "]");
        requestLocationUpdate();
    }

    /**
     * requestLocationUpdate
     */
    public void requestLocationUpdate() {

        // Check if GoogleAPICilent is still NOTNULL
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
            return;
        }

        Log.d(TAG, "requestLocationUpdate() called");
        /*
          Check if LocationSettings can be managed if not available
         */
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates locationSettingsStates = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // Provides a simple way of getting a device's location and is well suited for
                        // applications that do not require a fine-grained location and that do not need location
                        // updates. Gets the best and most recent location currently available, which may be null
                        // in rare cases when a location is not available.
                        if (ActivityCompat.checkSelfPermission(LocationTrackService.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            Log.i(TAG, "onConnected: Permission not granted returning..");
                            return;
                        }
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, mLocationListener);

                        Message message1 = new Message();
                        message1.obj = status;
                        message1.what = GPS_ENABLED;
                        mHandler.sendMessage(message1);

                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Message message = new Message();
                        message.obj = status;
                        message.what = LOCATION_SETTINGS_DIALOG_NEEDS_TOBE_SHOWN;
                        mHandler.sendMessage(message);
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
//                        ...
                        break;
                }
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended() called with: i = [" + i + "]");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed() called with: connectionResult = [" + connectionResult + "]");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy() called");

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            //Remove location update
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mLocationListener);
            mGoogleApiClient.disconnect();
        }
        super.onDestroy();
    }

    public void addCallback(LocationTrackServiceInteractionListener mCallBack) {
        this.mCallBacks.add(mCallBack);
    }

    public void removeCallback(LocationTrackServiceInteractionListener mCallBack) {
        this.mCallBacks.remove(mCallBack);
    }

    /**
     * Create Handler to communicate with Activity
     */
    /**
     * Create Handler to communicate with Activity
     */
    private static class MyHandler extends Handler {
        final WeakReference<LocationTrackService> mLocationTrackServiceWeakReference;
        final ArrayList<LocationTrackServiceInteractionListener> mCallBacks;

        MyHandler(LocationTrackService service, ArrayList<LocationTrackServiceInteractionListener> mCallBacks) {
            mLocationTrackServiceWeakReference = new WeakReference<>(service);
            this.mCallBacks = mCallBacks;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case LOCATION_NOT_AVAILABLE:
                    for (LocationTrackServiceInteractionListener mCallBack : mCallBacks) {
                        mCallBack.onLocationNotAvailable();
                    }
                    break;
                case LOCATION_FETCHED:
                    for (LocationTrackServiceInteractionListener mCallBack : mCallBacks) {
                        mCallBack.onLocationFetched((Location) msg.obj);
                    }
                    break;
                case LOCATION_SETTINGS_DIALOG_NEEDS_TOBE_SHOWN:
                    for (LocationTrackServiceInteractionListener mCallBack : mCallBacks) {
                        mCallBack.onLocationSettingsDialogNeedToBeShow((Status) msg.obj);
                    }
                    break;
                case GPS_ENABLED:
                    for (LocationTrackServiceInteractionListener mCallBack : mCallBacks) {
                        mCallBack.gpsEnabled();
                    }
                    break;
            }
        }
    }
   /* private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case LOCATION_NOT_AVAILABLE:
                    for (LocationTrackServiceInteractionListener mCallBack : mCallBacks) {
                        mCallBack.onLocationNotAvailable();
                    }
                    break;
                case LOCATION_FETCHED:
                    for (LocationTrackServiceInteractionListener mCallBack : mCallBacks) {
                        mCallBack.onLocationFetched((Location) msg.obj);
                    }
                    break;
                case LOCATION_SETTINGS_DIALOG_NEEDS_TOBE_SHOWN:
                    for (LocationTrackServiceInteractionListener mCallBack : mCallBacks) {
                        mCallBack.onLocationSettingsDialogNeedToBeShow((Status) msg.obj);
                    }
                    break;
                case GPS_ENABLED:
                    for (LocationTrackServiceInteractionListener mCallBack : mCallBacks) {
                        mCallBack.gpsEnabled();
                    }
                    break;
            }
        }
    };*/

    protected LocationRequest createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(LOCATION_REQUEST_INTERVAL);
        mLocationRequest.setFastestInterval(LOCATION_REQUEST_FASTEST_INTERVAL);
        mLocationRequest.setSmallestDisplacement(LOCATION_REQUEST_SMALLEST_DISPLACEMENT);
//        mLocationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    /**
     * This will get triggered when location changes
     */
    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location mLastLocation) {
            mLocation = mLastLocation;
            Log.d(TAG, "onLocationChanged() called with: mLastLocation = [" + mLastLocation + "]");
            if (mLastLocation != null) {
                Message message = new Message();
                message.obj = mLastLocation;
                message.what = LOCATION_FETCHED;
                mHandler.sendMessage(message);
            } else {
                Message message = new Message();
                message.what = LOCATION_NOT_AVAILABLE;
                mHandler.sendMessage(message);
            }
        }
    };
}
