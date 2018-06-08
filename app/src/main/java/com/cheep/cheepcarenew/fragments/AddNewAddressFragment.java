package com.cheep.cheepcarenew.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.cheep.R;
import com.cheep.cheepcarenew.activities.AddressActivity;
import com.cheep.custom_view.tooltips.ViewTooltip;
import com.cheep.databinding.ActivityAddNewAddressBinding;
import com.cheep.databinding.TooltipAddressSelectionBinding;
import com.cheep.fragment.BaseFragment;
import com.cheep.network.NetworkUtility;
import com.cheep.utils.LogUtils;
import com.cheep.utils.Utility;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class AddNewAddressFragment extends BaseFragment {


    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private boolean mLocationPermissionGranted = false;
    private String category;
    private ViewTooltip.TooltipView tooltipView;
    private boolean isAddressNameVerified = false;
    private boolean isAddressPickYouLocationVerified = false;
    private boolean isAddressFlatNoVerified = false;
    private boolean isAddressPinCodeVerified = false;

    public AddNewAddressFragment() {

    }

    ActivityAddNewAddressBinding mBinding;
    //    private ToolTipView toolTipView;
    public static final String TAG = "AddressCategorySelectionFragment";


    public static AddNewAddressFragment newInstance(String category) {
        Bundle args = new Bundle();
        AddNewAddressFragment fragment = new AddNewAddressFragment();
        args.putString(Utility.Extra.DATA, category);
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
        setListener();
        if (getArguments() == null)
            return;
        category = getArguments().getString(Utility.Extra.DATA);
        mBinding.editAddressCity.setHint(getSpannableStringForHint(getString(R.string.hint_address_city)));
        mBinding.editAddressPincode.setHint(getSpannableStringForHint(getString(R.string.hint_pincode)));
        mBinding.editAddressInitials.setHint(getSpannableStringForHint(getString(R.string.hint_address_initials)));
        mBinding.editAddressLocality.setHint(getSpannableStringForHint(getString(R.string.hint_address_locality)));
        mBinding.llAddressFields.setVisibility(View.GONE);
    }

    @Override
    public void setListener() {
        mBinding.imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideToolTip(true);
                ((AddressActivity) mContext).onBackPressed();

            }
        });

        mBinding.cvCurrentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                getLocationPermission();
                showPlacePickerDialog();
            }
        });
        mBinding.tvContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AddressActivity) mContext).onBackPressed();
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
            mBinding.tvAddress.setText(addressString);
            mBinding.tvAddress.setVisibility(View.VISIBLE);
            mBinding.viewAddress.setVisibility(View.VISIBLE);
            mBinding.editAddressInitials.setVisibility(View.VISIBLE);
            mBinding.viewAddressInitials.setVisibility(View.VISIBLE);
            mBinding.editAddressCity.setVisibility(View.GONE);
            mBinding.viewCity.setVisibility(View.GONE);
            mBinding.editAddressLocality.setVisibility(View.GONE);
            mBinding.viewLocality.setVisibility(View.GONE);
            mBinding.cvCurrentLocation.setVisibility(View.GONE);
            hideProgressDialog();
            openTooltip(false);
        } else {
            Utility.showSnackBar(getString(R.string.message_coundnot_find_current_location), mBinding.getRoot());
            LogUtils.LOGE(TAG, "error no address");
            hideProgressDialog();
        }
    }


    private void openTooltip(boolean delay) {
        Log.e(TAG, "openTooltip: **********************");
        TooltipAddressSelectionBinding toolTipBinding = DataBindingUtil.inflate(
                LayoutInflater.from(mContext),
                R.layout.tooltip_address_selection,
                null,
                false);
        // set tooltip text
        if (category.equalsIgnoreCase(NetworkUtility.TAGS.ADDRESS_TYPE.HOME))
            toolTipBinding.tvTitle.setText(getString(R.string.label_hey_is_this_your_home_address, getString(R.string.label_home)));
        else
            toolTipBinding.tvTitle.setText(getString(R.string.label_hey_is_this_your_home_address, getString(R.string.label_office)));
        toolTipBinding.tvDescription.setText(getString(R.string.label_auto_detect_location_message));
        toolTipBinding.tvYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideToolTip(false);
                Toast.makeText(mContext, "YES", Toast.LENGTH_SHORT).show();
            }
        });
        toolTipBinding.tvNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideToolTip(false);

                mBinding.tvAddress.setText("");

                mBinding.editAddressInitials.setVisibility(View.GONE);
                mBinding.viewAddressInitials.setVisibility(View.GONE);

                mBinding.editAddressCity.setVisibility(View.VISIBLE);
                mBinding.viewCity.setVisibility(View.VISIBLE);
                mBinding.editAddressLocality.setVisibility(View.VISIBLE);
                mBinding.viewLocality.setVisibility(View.VISIBLE);
                mBinding.cvCurrentLocation.setVisibility(View.VISIBLE);
            }
        });

        ViewTooltip viewTooltip = ViewTooltip.on(this, mBinding.tvAddress).customView(toolTipBinding.getRoot(), delay)
                .position(ViewTooltip.Position.TOP)
                .clickToHide(true)
                .animation(new ViewTooltip.FadeTooltipAnimation(500))
                .autoHide(false, 0);
        tooltipView = viewTooltip.getTooltip_view();
        viewTooltip.show();


    }

    public SpannableStringBuilder getSpannableStringForHint(String fullString) {
        String newString = fullString + getString(R.string.label_star);
        SpannableStringBuilder text = new SpannableStringBuilder(newString);
        text.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.red)), fullString.length(), newString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return text;
    }

    private void showPlacePickerDialog() {

        try {
            Utility.hideKeyboard(mContext);
            PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
            Intent intent = intentBuilder.build((Activity) mContext);
            startActivityForResult(intent, Utility.PLACE_PICKER_REQUEST);

        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {

            //TODO: Adding dummy place when playservice is not there
            if (mBinding.editAddressInitials != null) {
//                edtAddress.setText("Dummy Address with " + Utility.STATIC_LAT + "," + Utility.STATIC_LNG);
                mBinding.editAddressInitials.setText(mContext.getString(R.string.label_dummy_address, Utility.STATIC_LAT, Utility.STATIC_LNG));
                mBinding.editAddressInitials.setFocusable(true);
                mBinding.editAddressInitials.setFocusableInTouchMode(true);
                try {
                    mBinding.editAddressInitials.setTag(new LatLng(Double.parseDouble(Utility.STATIC_LAT), Double.parseDouble(Utility.STATIC_LNG)));
                } catch (Exception exe) {
                    exe.printStackTrace();
                    mBinding.editAddressInitials.setTag(new LatLng(0, 0));
                }
            }

            e.printStackTrace();
            Utility.showToast(mContext, mContext.getString(R.string.label_playservice_not_available));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        hideProgressDialog();
        if (requestCode == Utility.PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                mBinding.llAddressFields.setVisibility(View.VISIBLE);

                isAddressPickYouLocationVerified = true;
                isAddressNameVerified = true;

                final Place place = PlacePicker.getPlace(mContext, data);
                final CharSequence address = place.getAddress();

                mBinding.tvAddress.setText(address);
                mBinding.tvAddress.setVisibility(View.VISIBLE);
                mBinding.viewAddress.setVisibility(View.VISIBLE);
                mBinding.editAddressInitials.setVisibility(View.VISIBLE);
                mBinding.viewAddressInitials.setVisibility(View.VISIBLE);
                mBinding.editAddressCity.setVisibility(View.GONE);
                mBinding.viewCity.setVisibility(View.GONE);
                mBinding.editAddressLocality.setVisibility(View.GONE);
                mBinding.viewLocality.setVisibility(View.GONE);
                mBinding.cvCurrentLocation.setVisibility(View.GONE);

                openTooltip(true);

            } else {
                if (TextUtils.isEmpty(mBinding.editAddressInitials.getText().toString().trim())) {
                    isAddressPickYouLocationVerified = false;
                } else {
                    isAddressPickYouLocationVerified = true;
                    isAddressNameVerified = true;
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        hideToolTip(true);
        super.onDestroy();
    }

    private void hideToolTip(boolean removenow) {
        if (tooltipView != null) {
            if (removenow)
                tooltipView.removeNow();
            else
                tooltipView.remove();
        }
    }


}
