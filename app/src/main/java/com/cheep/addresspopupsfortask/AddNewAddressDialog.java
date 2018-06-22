package com.cheep.addresspopupsfortask;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.cheepcarenew.activities.AddressActivity;
import com.cheep.cheepcarenew.dialogs.EditAddressDialog;
import com.cheep.databinding.DialogAddNewAddressBinding;
import com.cheep.model.AddressModel;
import com.cheep.model.GuestUserDetails;
import com.cheep.model.LocationInfo;
import com.cheep.model.MessageEvent;
import com.cheep.model.UserDetails;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.FetchLocationInfoUtility;
import com.cheep.utils.GsonUtility;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class AddNewAddressDialog extends DialogFragment  {


    private String category;
    private AddressModel mAddressModel;
    private AddressSelectionListener listener;
    private boolean isWhiteTheme;

    public void setListener(AddressSelectionListener listener) {
        this.listener = listener;
    }

    DialogAddNewAddressBinding mBinding;
    //    private ToolTipView toolTipView;
    public static final String TAG = "AddNewAddressDialog";

    public static AddNewAddressDialog newInstance(String category, boolean isWhiteTheme, AddressSelectionListener listener) {
        Bundle args = new Bundle();
        AddNewAddressDialog fragment = new AddNewAddressDialog();
        fragment.setListener(listener);
        args.putString(Utility.Extra.DATA, category);
        args.putBoolean(Utility.Extra.DATA_2, isWhiteTheme);
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_add_new_address, container, false);
        initiateUI();
        return mBinding.getRoot();
    }

    public void initiateUI() {
        setListener();
        if (getArguments() == null)
            return;
        category = getArguments().getString(Utility.Extra.DATA);
        isWhiteTheme= getArguments().getBoolean(Utility.Extra.DATA_2);

        if (isWhiteTheme) {
            mBinding.imgBack.setImageResource(R.drawable.icon_arrow_back_blue);
            mBinding.rlTop.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
            mBinding.tvTitle.setTextColor(ContextCompat.getColor(getContext(), R.color.splash_gradient_end));
        }


        mBinding.editAddressCity.setHint(getSpannableStringForHint(getString(R.string.hint_address_city)));
        mBinding.editAddressPincode.setHint(getSpannableStringForHint(getString(R.string.hint_pincode)));
        mBinding.editAddressInitials.setHint(getSpannableStringForHint(getString(R.string.hint_address_initials)));
        mBinding.editAddressLocality.setHint(getSpannableStringForHint(getString(R.string.hint_address_locality)));
        mBinding.llAddressFields.setVisibility(View.INVISIBLE);
        mBinding.cvCurrentLocation.setVisibility(View.VISIBLE);
    }

    public void setListener() {
        mBinding.imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        mBinding.cvCurrentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPlacePickerDialog();
            }
        });
        mBinding.tvContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utility.hideKeyboard(getContext());
                addAddress();
            }
        });

    }

    private void addAddress() {
        if (TextUtils.isEmpty(mBinding.tvAddress.getText().toString().trim())) {
            Utility.showToast(getContext(), getString(R.string.validate_address));
        } else if (TextUtils.isEmpty(mBinding.editAddressInitials.getText().toString().trim())) {
            Utility.showToast(getContext(), getString(R.string.validate_address_initials));
        } else if (TextUtils.isEmpty(mBinding.editAddressPincode.getText().toString().trim())
                || mBinding.editAddressPincode.getText().toString().trim().length() < 6) {
            Utility.showToast(getContext(), getString(R.string.validate_pincode));
        } else {

            AddressModel model = new AddressModel();
            model.category = category;
            model.address = mBinding.tvAddress.getText().toString().trim();
            model.address_initials = mBinding.editAddressInitials.getText().toString().trim();
            model.landmark = mBinding.editAddressLandmark.getText().toString().trim();
            model.pincode = mBinding.editAddressPincode.getText().toString().trim();

            callAddAddressWS(model, (LatLng) mBinding.tvAddress.getTag());
        }
    }


    public SpannableStringBuilder getSpannableStringForHint(String fullString) {
        String newString = fullString + getString(R.string.label_star);
        SpannableStringBuilder text = new SpannableStringBuilder(newString);
        text.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.red)), fullString.length(), newString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return text;
    }

    private void showPlacePickerDialog() {
        ((BaseAppCompatActivity) getContext()).showProgressDialog();
        try {
            Utility.hideKeyboard(getContext());
            PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
            Intent intent = intentBuilder.build((Activity) getContext());
            startActivityForResult(intent, Utility.PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            //TODO: Adding dummy place when playservice is not there
            if (mBinding.tvAddress != null) {
//                edtAddress.setText("Dummy Address with " + Utility.STATIC_LAT + "," + Utility.STATIC_LNG);
                mBinding.tvAddress.setText(getContext().getString(R.string.label_dummy_address, Utility.STATIC_LAT, Utility.STATIC_LNG));
                mBinding.tvAddress.setFocusable(true);
                mBinding.tvAddress.setFocusableInTouchMode(true);
                try {
                    mBinding.tvAddress.setTag(new LatLng(Double.parseDouble(Utility.STATIC_LAT), Double.parseDouble(Utility.STATIC_LNG)));
                } catch (Exception exe) {
                    exe.printStackTrace();
                    mBinding.tvAddress.setTag(new LatLng(0, 0));
                }
            }

            e.printStackTrace();
            Utility.showToast(getContext(), getContext().getString(R.string.label_playservice_not_available));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ((BaseAppCompatActivity) getContext()).hideProgressDialog();
        if (requestCode == Utility.PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                mBinding.llAddressFields.setVisibility(View.VISIBLE);
                final Place place = PlacePicker.getPlace(getContext(), data);
                final CharSequence address = place.getAddress();

                mBinding.tvAddress.setText(address);
                mBinding.tvAddress.setTag(place.getLatLng());

                mBinding.editAddressInitials.setVisibility(View.VISIBLE);
                mBinding.viewAddressInitials.setVisibility(View.VISIBLE);


                mBinding.editAddressCity.setVisibility(View.GONE);
                mBinding.viewCity.setVisibility(View.GONE);
                mBinding.editAddressLocality.setVisibility(View.GONE);
                mBinding.viewLocality.setVisibility(View.GONE);
                mBinding.cvCurrentLocation.setVisibility(View.GONE);


            }
        }
    }


    public void onEventMainThread(MessageEvent event) {
        Log.e("onEventMainThread", "" + event.BROADCAST_ACTION);
        switch (event.BROADCAST_ACTION) {
            case Utility.BROADCAST_TYPE.ADDRESS_SELECTED_POP_UP:
                ((AddressActivity) getContext()).onBackPressed();
                break;
        }
    }

    /**
     * Calling Add Address WS
     *
     * @param addressModel added new address
     * @param latLng       latlong of selected address
     */
    private void callAddAddressWS(final AddressModel addressModel, final LatLng latLng) {
        if (!Utility.isConnected(getContext())) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mBinding.getRoot());
            return;
        }

        if (PreferenceUtility.getInstance(getContext()).getUserDetails() == null) {
            ((BaseAppCompatActivity) getContext()).showProgressDialog();
            FetchLocationInfoUtility mFetchLocationInfoUtility = new FetchLocationInfoUtility(
                    getContext(),
                    new FetchLocationInfoUtility.FetchLocationInfoCallBack() {
                        @Override
                        public void onLocationInfoAvailable(LocationInfo mLocationIno) {
                            ((BaseAppCompatActivity) getContext()).hideProgressDialog();
                            GuestUserDetails guestUserDetails = PreferenceUtility.getInstance(getContext()).getGuestUserDetails();
                            addressModel.address_id = "-" + (guestUserDetails.addressList == null ? "1" : String.valueOf(guestUserDetails.addressList.size() + 1));
                            addressModel.cityName = mLocationIno.City;
                            addressModel.countryName = mLocationIno.Country;
                            addressModel.stateName = mLocationIno.State;
                            addressModel.lat = String.valueOf(latLng.latitude);
                            addressModel.lng = String.valueOf(latLng.longitude);
                            if (guestUserDetails.addressList == null)
                                guestUserDetails.addressList = new ArrayList<>();
                            guestUserDetails.addressList.add(addressModel);
                            PreferenceUtility.getInstance(getContext()).saveGuestUserDetails(guestUserDetails);
                            listener.onAddressSelection(addressModel);
                            dismiss();
                        }

                        @Override
                        public void internetConnectionNotFound() {
//                            hideProgressDialog();
                            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mBinding.getRoot());
                        }
                    },
                    false
            );
            mFetchLocationInfoUtility.getLocationInfo(String.valueOf(latLng.latitude), String.valueOf(latLng.longitude));
        } else {
            /*
             * Logged In User so first need to fetch other location info and then call add address
             * Webservice.
             */
            ((BaseAppCompatActivity) getContext()).showProgressDialog();
            FetchLocationInfoUtility mFetchLocationInfoUtility = new FetchLocationInfoUtility(
                    getContext(),
                    new FetchLocationInfoUtility.FetchLocationInfoCallBack() {
                        @Override
                        public void onLocationInfoAvailable(LocationInfo mLocationIno) {

                            //Add Header parameters
                            Map<String, String> mHeaderParams = new HashMap<>();
                            mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(getContext()).getXAPIKey());
                            mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(getContext()).getUserDetails().userID);

                            //Add Params
                            Map<String, Object> mParams = new HashMap<>();


                            addressModel.cityName = mLocationIno.City;
                            addressModel.countryName = mLocationIno.Country;
                            addressModel.stateName = mLocationIno.State;
                            addressModel.lat = String.valueOf(mLocationIno.lat);
                            addressModel.lng = String.valueOf(mLocationIno.lng);


                            mParams = NetworkUtility.addGuestAddressParams(mParams, addressModel);

                            Utility.hideKeyboard(getContext());
                            //Url is based on condition if address id is greater then 0 then it means we need to update the existing address
                            VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.ADD_ADDRESS
                                    , mCallAddAddressWSErrorListener
                                    , mCallAddAddressResponseListener
                                    , mHeaderParams
                                    , mParams
                                    , null);
                            Volley.getInstance(getContext()).addToRequestQueue(mVolleyNetworkRequest, NetworkUtility.WS.ADD_ADDRESS);
                        }

                        @Override
                        public void internetConnectionNotFound() {
                            ((BaseAppCompatActivity) getContext()).hideProgressDialog();
                            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mBinding.getRoot());
                        }
                    },
                    false
            );
            mFetchLocationInfoUtility.getLocationInfo(String.valueOf(latLng.latitude), String.valueOf(latLng.longitude));
        }
    }

    Response.Listener mCallAddAddressResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {

            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;

                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
                        UserDetails userDetails = PreferenceUtility.getInstance(getContext()).getUserDetails();
                        AddressModel addressModel = (AddressModel) GsonUtility.getObjectFromJsonString(jsonObject.getJSONObject(NetworkUtility.TAGS.DATA).toString(), AddressModel.class);
                        if (userDetails != null) {
                            if (userDetails.addressList == null)
                                userDetails.addressList = new ArrayList<>();
                            userDetails.addressList.add(addressModel);
                            PreferenceUtility.getInstance(getContext()).saveUserDetails(userDetails);
                        } else {
                            GuestUserDetails guestUserDetails = PreferenceUtility.getInstance(getContext()).getGuestUserDetails();
                            if (guestUserDetails.addressList == null)
                                guestUserDetails.addressList = new ArrayList<>();
                            guestUserDetails.addressList.add(addressModel);
                            PreferenceUtility.getInstance(getContext()).saveGuestUserDetails(guestUserDetails);
                        }

                        listener.onAddressSelection(addressModel);
                        dismiss();
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
//                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityHireNewJobBinding.getRoot());
                        Utility.showToast(getContext(), getString(R.string.label_something_went_wrong));

                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
//                        Utility.showSnackBar(error_message, mActivityHireNewJobBinding.getRoot());
                        Utility.showToast(getContext(), error_message);
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.USER_DELETED:
                    case NetworkUtility.TAGS.STATUSCODETYPE.FORCE_LOGOUT_REQUIRED:
                        //Logout and finish the current activity
                        Utility.logout(getContext(), true, statusCode);
                        ((Activity) getContext()).finish();
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mCallAddAddressWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
            ((BaseAppCompatActivity) getContext()).hideProgressDialog();
        }
    };

    Response.ErrorListener mCallAddAddressWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
            ((BaseAppCompatActivity) getContext()).hideProgressDialog();

            // Show Toast
//            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityHireNewJobBinding.getRoot());
            Utility.showToast(getContext(), getContext().getString(R.string.label_something_went_wrong));
        }
    };

}
