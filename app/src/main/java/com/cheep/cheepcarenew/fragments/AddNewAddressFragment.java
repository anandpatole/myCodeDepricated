package com.cheep.cheepcarenew.fragments;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.cheepcarenew.activities.AddressActivity;
import com.cheep.custom_view.tooltips.ToolTipView;
import com.cheep.databinding.ActivityAddNewAddressBinding;
import com.cheep.fragment.BaseFragment;
import com.cheep.utils.LogUtils;
import com.cheep.utils.Utility;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AddNewAddressFragment extends BaseFragment {


    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private boolean mLocationPermissionGranted = false;

    public AddNewAddressFragment() {

    }

    ActivityAddNewAddressBinding mBinding;
    private ToolTipView toolTipView;
    public static final String TAG = "AddressCategorySelectionFragment";


    public static AddNewAddressFragment newInstance() {
        Bundle args = new Bundle();
        AddNewAddressFragment fragment = new AddNewAddressFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.activity_add_new_address, null, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initiateUI();
    }
    /*
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_add_new_address);
        initiateUI();
    }
*/


    @Override
    public void initiateUI() {
        setListeners();
    }

    @Override
    public void setListener() {

    }

    protected void setListeners() {
        mBinding.imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (toolTipView != null)
                    toolTipView.remove();
                ((AddressActivity) mContext).onBackPressed();

            }
        });

        mBinding.cvCurrentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocationPermission();
            }
        });
        mBinding.tvContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AddressActivity) mContext).loadFragment(AddressListFragment.TAG, AddressListFragment.newInstance());
            }
        });
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            getDeviceLocation();
        } else {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    Utility.REQUEST_CODE_PERMISSION_LOCATION);
        }
    }


    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(mContext, null);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(mContext, null);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mContext);
        showProgressDialog();
        try {
            if (mLocationPermissionGranted) {
                Task locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener((Activity) mContext, new OnCompleteListener() {

                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            Location mLastKnownLocation = (Location) task.getResult();
                            if (mLastKnownLocation != null) {
                                getAddressFromLocation(mLastKnownLocation);
                            } else {
                                hideProgressDialog();
                                Utility.showSnackBar(getString(R.string.message_coundnot_find_current_location), mBinding.getRoot());
                            }
                        } else {
                            hideProgressDialog();
                            Utility.showSnackBar(getString(R.string.message_coundnot_find_current_location), mBinding.getRoot());
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
            Utility.showSnackBar(getString(R.string.message_coundnot_find_current_location), mBinding.getRoot());
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Utility.REQUEST_CODE_PERMISSION_LOCATION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getDeviceLocation();
        }
    }


    private void getAddressFromLocation(Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        Geocoder geocoder = new Geocoder(mContext);
        List<Address> addresses = new ArrayList<>();
        try {
            addresses = geocoder.getFromLocation(lat, lng, 2);
        } catch (IOException e) {
            Utility.showSnackBar(getString(R.string.message_coundnot_find_current_location), mBinding.getRoot());
            e.printStackTrace();
        }
        StringBuilder addressString = new StringBuilder(Utility.EMPTY_STRING);

        if (addresses.size() >= 1) {
            int i = 0;
            while (addresses.get(0).getAddressLine(i) != null) {
                addressString.append(addresses.get(0).getAddressLine(i));
                i++;
            }
            mBinding.editAddressInitials.setText(addressString);
            hideProgressDialog();
        } else {
            Utility.showSnackBar(getString(R.string.message_coundnot_find_current_location), mBinding.getRoot());
            LogUtils.LOGE(TAG, "error no address");
            hideProgressDialog();
        }

    }

}
