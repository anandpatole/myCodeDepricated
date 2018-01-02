package com.cheep.cheepcare.dialogs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.R;
import com.cheep.databinding.DialogAddAddressCheepCareBinding;
import com.cheep.model.AddressModel;
import com.cheep.model.GuestUserDetails;
import com.cheep.model.LocationInfo;
import com.cheep.model.UserDetails;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.FetchLocationInfoUtility;
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
import static com.cheep.utils.Utility.getObjectFromJsonString;

/**
 * Created by Giteeka on 28/12/2017.
 */
public class BottomAddAddressDialog {
    private static final String TAG = "BottomAddAddressDialog";
    BottomAddAddressDialog bottomAlertDialog;
    public View view;
    BottomSheetDialog dialog;
    DialogAddAddressCheepCareBinding binding;
    private boolean isAddressNameVerified = false;
    private boolean isAddressPickYouLocationVerified = false;
    private boolean isAddressFlatNoVerified = false;
    private Context mContext;
    private Fragment fragment;
    private AddAddressListener addAddressListener;
    private ArrayList<String> addressCategory = new ArrayList<>();

    public interface AddAddressListener {
        void onAddAddress(AddressModel addressModel);
    }

    /**
     * Open dialog from activity
     *
     * @param context instance of activity
     */
    public BottomAddAddressDialog(Context context, AddAddressListener addAddressListener, ArrayList<String> strings) {
        mContext = context;
        binding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.dialog_add_address_cheep_care, null, false);
        view = binding.getRoot();
        dialog = new BottomSheetDialog(mContext);
        this.addAddressListener = addAddressListener;
        addressCategory =strings;
        init();
    }


    /**
     * Open dialog from fragment
     *
     * @param fragment instance of fragment
     * @param strings
     */
    public BottomAddAddressDialog(Fragment fragment, AddAddressListener addAddressListener, ArrayList<String> strings) {
        this.addAddressListener = addAddressListener;
        this.fragment = fragment;
        mContext = fragment.getActivity();
        binding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.dialog_add_address_cheep_care, null, false);
        view = binding.getRoot();
        dialog = new BottomSheetDialog(mContext);
        addressCategory =strings;
        init();

    }

    public boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }

    private void init() {


        if (!addressCategory.isEmpty()) {
            binding.radioHome.setVisibility(addressCategory.contains(NetworkUtility.TAGS.ADDRESS_TYPE.HOME) ?
                    View.VISIBLE : View.GONE);
            binding.radioOffice.setVisibility(addressCategory.contains(NetworkUtility.TAGS.ADDRESS_TYPE.OFFICE) ?
                    View.VISIBLE : View.GONE);
            binding.radioBiz.setVisibility(addressCategory.contains(NetworkUtility.TAGS.ADDRESS_TYPE.BIZ) ?
                    View.VISIBLE : View.GONE);
            binding.radioSoci.setVisibility(addressCategory.contains(NetworkUtility.TAGS.ADDRESS_TYPE.SOCI) ?
                    View.VISIBLE : View.GONE);
            binding.radioOther.setVisibility(addressCategory.contains(NetworkUtility.TAGS.ADDRESS_TYPE.OTHERS) ?
                    View.VISIBLE : View.GONE);
        }

        // click handler for address selection
        binding.editAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPlacePickerDialog();
            }
        });

        // address initials are mandatory
        // handle button enable/disable states
        binding.editAddressInitials.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!binding.editAddressInitials.getText().toString().trim().isEmpty()) {
                    isAddressFlatNoVerified = true;
                    checkAddAddressVerified();
                } else {
                    isAddressFlatNoVerified = false;
                    checkAddAddressVerified();
                }
            }
        });

        // click handler for add address button
        binding.btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               /* if (TextUtils.isEmpty(edtName.getText().toString().trim())) {
                    Utility.showToast(mContext, getString(R.string.validate_address_nickname));
                } else*/
                if (TextUtils.isEmpty(binding.editAddress.getText().toString().trim())) {
                    Utility.showToast(mContext, mContext.getString(R.string.validate_address));
                } else if (TextUtils.isEmpty(binding.editAddressInitials.getText().toString().trim())) {
                    Utility.showToast(mContext, mContext.getString(R.string.validate_address_initials));
                } else {
                    callAddAddressWS(
                            (binding.radioHome.isChecked()
                                    ? NetworkUtility.TAGS.ADDRESS_TYPE.HOME
                                    : binding.radioOffice.isChecked() ? NetworkUtility.TAGS.ADDRESS_TYPE.OFFICE : NetworkUtility.TAGS.ADDRESS_TYPE.OTHERS)
                                /*, edtName.getText().toString().trim()*/
                            , binding.editAddress.getText().toString().trim()
                            , binding.editAddressInitials.getText().toString().trim()
                            , (LatLng) binding.editAddress.getTag());
                }
            }
        });

    }

    private void showPlacePickerDialog() {

        try {
            Utility.hideKeyboard(mContext);
            PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
            Intent intent = intentBuilder.build((Activity) mContext);
            if (fragment != null)
                fragment.startActivityForResult(intent, Utility.PLACE_PICKER_REQUEST);
            else
                ((Activity) mContext).startActivityForResult(intent, Utility.PLACE_PICKER_REQUEST);

        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {

            //TODO: Adding dummy place when playservice is not there
            if (binding.editAddress != null) {
//                edtAddress.setText("Dummy Address with " + Utility.STATIC_LAT + "," + Utility.STATIC_LNG);
                binding.editAddress.setText(mContext.getString(R.string.label_dummy_address, Utility.STATIC_LAT, Utility.STATIC_LNG));
                binding.editAddress.setFocusable(true);
                binding.editAddress.setFocusableInTouchMode(true);
                try {
                    binding.editAddress.setTag(new LatLng(Double.parseDouble(Utility.STATIC_LAT), Double.parseDouble(Utility.STATIC_LNG)));
                } catch (Exception exe) {
                    exe.printStackTrace();
                    binding.editAddress.setTag(new LatLng(0, 0));
                }
            }

            e.printStackTrace();
            Utility.showToast(mContext, mContext.getString(R.string.label_playservice_not_available));
        }
    }

    public void onActivityResult(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            isAddressPickYouLocationVerified = true;
            isAddressNameVerified = true;
            final Place place = PlacePicker.getPlace(mContext, data);
            final CharSequence name = place.getName();
            final CharSequence address = place.getAddress();
            if (binding.editAddress != null) {
                binding.editAddress.setText(address);
                   /* edtAddress.setFocusable(true);
                    edtAddress.setFocusableInTouchMode(true);*/
                binding.editAddress.setTag(place.getLatLng());
            }
        } else {
            if (TextUtils.isEmpty(binding.editAddress.getText().toString().trim())) {
                isAddressPickYouLocationVerified = false;
            } else {
                isAddressPickYouLocationVerified = true;
                isAddressNameVerified = true;
            }
        }
        checkAddAddressVerified();
//        hideProgressDialog();
    }

    private void checkAddAddressVerified() {
        if (isAddressPickYouLocationVerified
                && isAddressNameVerified) {
            binding.btnAdd.setBackgroundColor(ContextCompat.getColor(mContext, R.color.splash_gradient_end));
        } else {
            binding.btnAdd.setBackgroundColor(ContextCompat.getColor(mContext, R.color.grey_varient_14));
        }
    }

    public void showDialog() {
        if (view.getParent() != null)
            ((ViewGroup) view.getParent()).removeAllViews();
        dialog.setContentView(view);
        dialog.show();
    }

    public void dismiss() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }


    /**
     * Calling Add Address WS
     *
     * @param addressType
     * @param address
     */
    private void callAddAddressWS(final String addressType, /*String addressName,*/ final String address, final String addressInitials, final LatLng latLng) {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, binding.getRoot());
            return;
        }

        if (PreferenceUtility.getInstance(mContext).getUserDetails() == null) {
//            showProgressDialog();
            FetchLocationInfoUtility mFetchLocationInfoUtility = new FetchLocationInfoUtility(
                    mContext,
                    new FetchLocationInfoUtility.FetchLocationInfoCallBack() {
                        @Override
                        public void onLocationInfoAvailable(LocationInfo mLocationIno) {
//                            hideProgressDialog();

                            GuestUserDetails guestUserDetails = PreferenceUtility.getInstance(mContext).getGuestUserDetails();

                            /**
                             * In case og Guest User we want to save it locally.
                             */
                            AddressModel addressModel = new AddressModel();
                            // Creating Dynamic AddressID but it would be nagative values always to differentiat with logged in users address.
                            addressModel.address_id = "-" + (guestUserDetails.addressList == null ? "1" : String.valueOf(guestUserDetails.addressList.size() + 1));
                            addressModel.address = address;
                            addressModel.cityName = mLocationIno.City;
                            addressModel.countryName = mLocationIno.Country;
                            addressModel.stateName = mLocationIno.State;
                            addressModel.address_initials = addressInitials;
                            addressModel.category = addressType;
                            addressModel.lat = String.valueOf(latLng.latitude);
                            addressModel.lng = String.valueOf(latLng.longitude);


                            //Saving information in sharedpreference
                            guestUserDetails.addressList.add(addressModel);
                            PreferenceUtility.getInstance(mContext).saveGuestUserDetails(guestUserDetails);
                            addAddressListener.onAddAddress(addressModel);

                        }

                        @Override
                        public void internetConnectionNotFound() {
//                            hideProgressDialog();
                            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, binding.getRoot());
                        }
                    },
                    false
            );
            mFetchLocationInfoUtility.getLocationInfo(String.valueOf(latLng.latitude), String.valueOf(latLng.longitude));
            return;
        } else {
            /**
             * Logged In User so first need to fetch other location info and then call add address
             * Webservice.
             */
//            showProgressDialog();
            FetchLocationInfoUtility mFetchLocationInfoUtility = new FetchLocationInfoUtility(
                    mContext,
                    new FetchLocationInfoUtility.FetchLocationInfoCallBack() {
                        @Override
                        public void onLocationInfoAvailable(LocationInfo mLocationIno) {

                            //Add Header parameters
                            Map<String, String> mHeaderParams = new HashMap<>();
                            mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
                            mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);

                            //Add Params
                            Map<String, Object> mParams = new HashMap<>();
                            mParams.put(NetworkUtility.TAGS.CATEGORY, addressType);
                            mParams.put(NetworkUtility.TAGS.ADDRESS, address);
                            mParams.put(NetworkUtility.TAGS.ADDRESS_INITIALS, addressInitials);
                            mParams.put(NetworkUtility.TAGS.LAT, mLocationIno.lat);
                            mParams.put(NetworkUtility.TAGS.LNG, mLocationIno.lng);
                            mParams.put(NetworkUtility.TAGS.COUNTRY, mLocationIno.Country);
                            mParams.put(NetworkUtility.TAGS.STATE, mLocationIno.State);
                            mParams.put(NetworkUtility.TAGS.CITY_NAME, mLocationIno.City);

                            Utility.hideKeyboard(mContext);
                            //Url is based on condition if address id is greater then 0 then it means we need to update the existing address
                            VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.ADD_ADDRESS
                                    , mCallAddAddressWSErrorListener
                                    , mCallAddAddressResponseListener
                                    , mHeaderParams
                                    , mParams
                                    , null);
                            Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest, NetworkUtility.WS.ADD_ADDRESS);
                        }

                        @Override
                        public void internetConnectionNotFound() {
//                            hideProgressDialog();
                            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, binding.getRoot());
                        }
                    },
                    false
            );
            mFetchLocationInfoUtility.getLocationInfo(String.valueOf(latLng.latitude), String.valueOf(latLng.longitude));
            return;
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

                        AddressModel addressModel = (AddressModel) getObjectFromJsonString(jsonObject.getJSONObject(NetworkUtility.TAGS.DATA).toString(), AddressModel.class);


                        //Saving information in sharedpreference
                        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
                        userDetails.addressList.add(addressModel);
                        PreferenceUtility.getInstance(mContext).saveUserDetails(userDetails);
                        addAddressListener.onAddAddress(addressModel);


                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
//                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityHireNewJobBinding.getRoot());
                        Utility.showToast(mContext, mContext.getString(R.string.label_something_went_wrong));
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
//                        Utility.showSnackBar(error_message, mActivityHireNewJobBinding.getRoot());
                        Utility.showToast(mContext, error_message);
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.USER_DELETED:
                    case NetworkUtility.TAGS.STATUSCODETYPE.FORCE_LOGOUT_REQUIRED:
                        //Logout and finish the current activity
                        Utility.logout(mContext, true, statusCode);
                        ((Activity) mContext).finish();
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mCallAddAddressWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
//            hideProgressDialog();
        }
    };

    Response.ErrorListener mCallAddAddressWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            // Close Progressbar
//            hideProgressDialog();

            // Show Toast
//            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mActivityHireNewJobBinding.getRoot());
            Utility.showToast(mContext, mContext.getString(R.string.label_something_went_wrong));
        }
    };
}
