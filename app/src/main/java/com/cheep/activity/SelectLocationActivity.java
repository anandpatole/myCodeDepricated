package com.cheep.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import com.cheep.R;
import com.cheep.adapter.PlacesAutoCompleteAdapter;
import com.cheep.databinding.ActivitySelectLocationBinding;
import com.cheep.model.GooglePlaceModel;
import com.cheep.model.LocationInfo;
import com.cheep.utils.FetchLocationInfoUtility;
import com.cheep.utils.GoogleMapUtils;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.SharedElementTransitionHelper;
import com.cheep.utils.Utility;
import com.google.android.gms.common.api.Status;


/**
 * Created by pankaj on 9/28/16.
 */
public class SelectLocationActivity extends BaseAppCompatActivity {

    private static final String TAG = SelectLocationActivity.class.getSimpleName();
    private ActivitySelectLocationBinding mActivitySelectLocationBinding;
    private PlacesAutoCompleteAdapter placesAutoCompleteAdapter;

    public static void newInstance(Context context) {
        Intent intent = new Intent(context, SelectLocationActivity.class);
        context.startActivity(intent);
    }

    public static void newInstance(Context context, Bundle bnd) {
        Intent intent = new Intent(context, SelectLocationActivity.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            context.startActivity(intent, bnd);
        } else {
            context.startActivity(intent);
        }
//        context.startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        ((AppCompatActivity) mContext).overridePendingTransition(0, 0);
        hideProgressDialog();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Sets intermediate background of activity when activity starts
        SharedElementTransitionHelper.enableTransition(this);
        mActivitySelectLocationBinding = DataBindingUtil.setContentView(this, R.layout.activity_select_location);
        initiateUI();
        setListeners();

    }

    @Override
    protected void initiateUI() {

        //Setting Toolbar
        setSupportActionBar(mActivitySelectLocationBinding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(Utility.EMPTY_STRING);
            mActivitySelectLocationBinding.toolbar.setNavigationIcon(R.drawable.icon_cross_white);

            mActivitySelectLocationBinding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Utility.hideKeyboard(mContext, mActivitySelectLocationBinding.editSearch);
                    onBackPressed();
                }
            });
        }

        mActivitySelectLocationBinding.editSearch.postDelayed(new Runnable() {
            @Override
            public void run() {
                Utility.showKeyboard(mContext, mActivitySelectLocationBinding.editSearch);
            }
        }, 50);

        //For Google API Call Adapter
        //Setting RecyclerView Adapter
        final PlacesAutoCompleteAdapter placesAutoCompleteAdapter = new PlacesAutoCompleteAdapter(listener);
        mActivitySelectLocationBinding.commonRecyclerViewNoSwipe.recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mActivitySelectLocationBinding.commonRecyclerViewNoSwipe.recyclerView.setAdapter(placesAutoCompleteAdapter);
        mActivitySelectLocationBinding.commonRecyclerViewNoSwipe.recyclerView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white));


        //Adding Searching textlistener to search edittext
        mActivitySelectLocationBinding.editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                placesAutoCompleteAdapter.getFilter().performFiltering(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    protected void setListeners() {
        mActivitySelectLocationBinding.textAutoLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mLocationTrackService != null && mLocationTrackService.mLocation != null && mLocationTrackService.mLocation.getLatitude() != 0 && mLocationTrackService.mLocation.getLongitude() != 0) {
                    showProgressDialog();
                    onLocationSelected(String.valueOf(mLocationTrackService.mLocation.getLatitude()), String.valueOf(mLocationTrackService.mLocation.getLongitude()));
                } else {
                    // Location not there fetch
                    if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        //Let the activity know that location permission not granted
                        if (ActivityCompat.shouldShowRequestPermissionRationale(SelectLocationActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                            ActivityCompat.requestPermissions(SelectLocationActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, Utility.REQUEST_CODE_PERMISSION_LOCATION);
                        } else {
                            ActivityCompat.requestPermissions(SelectLocationActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, Utility.REQUEST_CODE_PERMISSION_LOCATION);
                        }
                    } else {
                        initiateLocationRequestWithProgress();
                    }
                }
            }
        });
    }

    private void initiateLocationRequestWithProgress() {
        // Show the ProgressDialog till the time we received location update
        showProgressDialog();
        requestLocationUpdateFromService();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Utility.REQUEST_CODE_PERMISSION_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "onRequestPermissionsResult: Permission Granted");
//                //So, ask service to fetch the location now
                initiateLocationRequestWithProgress();
            } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Log.i(TAG, "onRequestPermissionsResult: Permission Denied");
                hideProgressDialog();
                Snackbar.make(mActivitySelectLocationBinding.getRoot(), getString(R.string.permission_denied_location), 3000).show();
            }
        }
    }

    PlacesAutoCompleteAdapter.OnPlaceClickListener listener = new PlacesAutoCompleteAdapter.OnPlaceClickListener() {
        @Override
        public void onPlaceClicked(final GooglePlaceModel googlePlaceModel) {
            showProgressDialog();
            GoogleMapUtils.getLatLongForPlace(mContext, googlePlaceModel.placeid, null, new GoogleMapUtils.OnGetLatLongCallback() {
                @Override
                public void onSuccess(String lat, String lng, Exception exception) {
                    if (exception == null) {
                        onLocationSelected(lat, lng);
                    }
                }
            });
        }
    };

    private void setLocation(LocationInfo mLocationInfo) {
        Log.d(TAG, "setLocation() called with: mLocationInfo = [" + mLocationInfo + "]");
        Intent data = new Intent();
        if (mLocationInfo != null)
            data.putExtra(Utility.Extra.LOCATION_INFO, Utility.getJsonStringFromObject(mLocationInfo));
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, 0);
    }

    @Override
    public void onLocationSettingsDialogNeedToBeShow(Status status) {
        super.onLocationSettingsDialogNeedToBeShow(status);
        // Location settings are not satisfied, but this can be fixed
        // by showing the user a dialog.
        try {
            // Show the dialog by calling startResolutionForResult(),
            // and check the result in onActivityResult().
            status.startResolutionForResult(this, Utility.REQUEST_CODE_CHECK_LOCATION_SETTINGS);
        } catch (IntentSender.SendIntentException e) {
            // Ignore the error.
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult() called with: requestCode = [" + requestCode + "], resultCode = [" + resultCode + "], data = [" + data + "]");
        if (requestCode == Utility.REQUEST_CODE_CHECK_LOCATION_SETTINGS) {
            if (resultCode == RESULT_OK) {
                mLocationTrackService.requestLocationUpdate();
            }
        }
    }

    @Override
    protected void onLocationFetched(final Location mLocation) {
        super.onLocationFetched(mLocation);
        Log.d(TAG, "onLocationFetched() called with: mLocation = [" + mLocation + "]");
        if (mLocation != null && mLocation.getLatitude() > 0 && mLocation.getLongitude() > 0) {
            onLocationSelected(String.valueOf(mLocation.getLatitude()), String.valueOf(mLocation.getLongitude()));
        }
    }

    public void onLocationSelected(String lat, String lon) {
        showProgressDialog();
        Log.d(TAG, "onLocationSelected() called with: lat = [" + lat + "], lng = [" + lon + "]");
        FetchLocationInfoUtility mFetchLocationInfoUtility = new FetchLocationInfoUtility(
                mContext,
                new FetchLocationInfoUtility.FetchLocationInfoCallBack() {
                    @Override
                    public void onLocationInfoAvailable(LocationInfo mLocationIno) {
                        Log.d(TAG, "onLocationInfoAvailable() called with: mLocationIno = [" + mLocationIno + "]");
                        if (!TextUtils.isEmpty(mLocationIno.City)) {
                            hideProgressDialog();
                            setLocation(mLocationIno);
                        } else {
                            setLocation(null);
                        }
                    }

                    @Override
                    public void internetConnectionNotFound() {
                        setResult(RESULT_CANCELED, new Intent());
                        finish();
                    }
                },
                PreferenceUtility.getInstance(mContext).getUserDetails() == null
        );
        mFetchLocationInfoUtility.getLocationInfo(lat, lon);
    }


}
