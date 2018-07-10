package com.cheep.cheepcarenew.dialogs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;

import android.app.ProgressDialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.cheepcarenew.fragments.ProfileTabFragment;
import com.cheep.cheepcarenew.fragments.ProfileDetailsFragmentnew;
import com.cheep.databinding.DialogEditAddressBinding;
import com.cheep.model.AddressModel;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.GsonUtility;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class EditAddressDialog extends DialogFragment implements View.OnClickListener {

    public static final String TAG = EditAddressDialog.class.getSimpleName();
    private DialogEditAddressBinding mBinding;
    private ProgressDialog mProgressDialog;
    private ArrayList<AddressModel> listOfAddress;
    private String isHomeOrIsOffice;
    private String addressId;
    private String lat;
    private String lng;
    private String country="";
    private String sate ="";
    private String city="";
    int addressPosition = 0;

    public EditAddressDialog() {
        // Required empty public constructor
    }


    public static EditAddressDialog newInstance(String addressType, ArrayList<AddressModel> addressList, int addressPosition) {
        EditAddressDialog fragment = new EditAddressDialog();
        Bundle args = new Bundle();
        args.putString(Utility.Extra.DATA, addressType);
        args.putString(Utility.Extra.DATA_2, GsonUtility.getJsonStringFromObject(addressList));
        args.putInt(Utility.Extra.POSITION, addressPosition);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isHomeOrIsOffice = getArguments().getString(Utility.Extra.DATA);
            listOfAddress = GsonUtility.getObjectListFromJsonString(getArguments().getString(Utility.Extra.DATA_2), AddressModel[].class);
            addressPosition = getArguments().getInt(Utility.Extra.POSITION);
            Log.e(TAG, "Editable Address Position = [" + addressPosition + "]");
        }
    }

    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimationZoomInOut;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        dialog.setCanceledOnTouchOutside(true);
        this.setCancelable(true);

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().getAttributes().windowAnimations = R.style.AlertAnimation;
        return dialog;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_edit_address, container, false);
        initiateUI();
        setListener();
        return mBinding.getRoot();
    }

    private void initiateUI() {
        // set address
        int position = 0;
        if (listOfAddress != null) {
            for (int i = 0; i < listOfAddress.size(); i++) {
                if (addressPosition == position) {
                    mBinding.tvAddress.setText(listOfAddress.get(i).address);
                    mBinding.editAddressInitials.setText(listOfAddress.get(i).address_initials);
                    mBinding.editAddressLandmark.setText(listOfAddress.get(i).landmark);
                    mBinding.editAddressPincode.setText(listOfAddress.get(i).pincode);
                    addressId = listOfAddress.get(i).address_id;
                    lat = listOfAddress.get(i).lat;
                    lng = listOfAddress.get(i).lng;

                    if(listOfAddress.get(i).countryName !=null){
                        country = listOfAddress.get(i).countryName;
                    }else {
                        country="";
                    }
                    if(listOfAddress.get(i).stateName !=null){
                        sate = listOfAddress.get(i).stateName;
                    }else {
                        sate="";
                    }
                    if(listOfAddress.get(i).cityName !=null){
                        city = listOfAddress.get(i).cityName;
                    }else {
                        city="";
                    }
                }
                position++;
            }
        }
        mBinding.editAddressInitials.addTextChangedListener(new EditAddressDialog.TextWatcherForeAddressInitials(mBinding.editAddressInitials));
        mBinding.editAddressLandmark.addTextChangedListener(new EditAddressDialog.TextWatcherForLandMarks(mBinding.editAddressLandmark));
        mBinding.editAddressPincode.addTextChangedListener(new EditAddressDialog.TextWatcherForPinCode(mBinding.editAddressPincode));
        continueTextEnableWhenFillAllField();
    }

    private void setListener() {
        mBinding.tvAddress.setOnClickListener(this);
        mBinding.imgBack.setOnClickListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    @SuppressLint("ResourceType")
    private void continueTextEnableWhenFillAllField() {
        if (mBinding.editAddressInitials.getText().toString().isEmpty() ||

                mBinding.editAddressPincode.getText().toString().isEmpty()) {

            mBinding.tvContinue.setOnClickListener(null);
            mBinding.tvContinue.setTextColor(Color.parseColor(getResources().getString(R.color.grey_dark_color)));
        } else {
            mBinding.tvContinue.setOnClickListener(this);
            mBinding.tvContinue.setTextColor(Color.parseColor(getResources().getString(R.color.splash_gradient_end)));
        }


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

    private class TextWatcherForeAddressInitials implements TextWatcher {
        final EditText editText;

        private TextWatcherForeAddressInitials(EditText editText) {
            this.editText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            Log.e("TextWatcherForPinCode", "onText Changed: " + s.toString());
            continueTextEnableWhenFillAllField();

        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    }

    private class TextWatcherForLandMarks implements TextWatcher {
        final EditText editText;

        private TextWatcherForLandMarks(EditText editText) {
            this.editText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            Log.e("TextWatcherForLandMarks", "onText Changed: " + s.toString());
            continueTextEnableWhenFillAllField();

        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    }

    private class TextWatcherForPinCode implements TextWatcher {
        final EditText editText;

        private TextWatcherForPinCode(EditText editText) {
            this.editText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            Log.e("TextWatcherForPinCode", "onText Changed: " + s.toString());
            continueTextEnableWhenFillAllField();

        }

        @Override
        public void afterTextChanged(Editable editable) {

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

            }
        }
    }


    //View.OnClickListener
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_continue:
                callEditProfileWS();
                break;
            case R.id.tv_address:
                showPlacePickerDialog();
                break;
            case R.id.img_back:
                dismiss();
                break;
        }
    }

    /************************************************************************************************
     **********************************Calling Webservice ********************************************
     ************************************************************************************************/

    protected void showProgressDialog(String message) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getContext());
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

    private void callEditProfileWS() {
        if (!Utility.isConnected(getContext())) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mBinding.getRoot());
            return;
        }

        showProgressDialog();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(getContext()).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(getContext()).getUserDetails().userID);
        //
        //
        //
        // Add Params
        Map<String, Object> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.ADDRESS_ID, addressId);
        mParams.put(NetworkUtility.TAGS.CATEGORY, isHomeOrIsOffice);
        mParams.put(NetworkUtility.TAGS.ADDRESS, mBinding.tvAddress.getText().toString().trim());
        mParams.put(NetworkUtility.TAGS.ADDRESS_INITIALS, mBinding.editAddressInitials.getText().toString().trim());
        mParams.put(NetworkUtility.TAGS.LANDMARK, mBinding.editAddressLandmark.getText().toString().trim());
        mParams.put(NetworkUtility.TAGS.PINCODE, mBinding.editAddressPincode.getText().toString().trim());
        mParams.put(NetworkUtility.TAGS.NAME, "");
        mParams.put(NetworkUtility.TAGS.NICKNAME, "");
        mParams.put(NetworkUtility.TAGS.LAT, lat);
        mParams.put(NetworkUtility.TAGS.LNG, lng);
        mParams.put(NetworkUtility.TAGS.COUNTRY, country);
        mParams.put(NetworkUtility.TAGS.STATE, sate);
        mParams.put(NetworkUtility.TAGS.CITY_NAME, city);

        //noinspection unchecked
        VolleyNetworkRequest mVolleyNetworkRequestForCategoryList = new VolleyNetworkRequest(NetworkUtility.WS.EDIT_ADDRESS
                , mCallGetCityCareDetailsWSErrorListener
                , mCallGetCityCareDetailsWSResponseListener
                , mHeaderParams
                , mParams
                , null);

        Volley.getInstance(getContext()).addToRequestQueue(mVolleyNetworkRequestForCategoryList, NetworkUtility.WS.EDIT_ADDRESS);
    }


    private Response.Listener mCallGetCityCareDetailsWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {
            hideProgressDialog();
            Log.e(TAG, "onResponse() called with: response = [" + response + "]");
            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.e(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
                        JSONObject jsonData = jsonObject.getJSONObject(NetworkUtility.TAGS.DATA);
                        AddressModel addressModel = (AddressModel) GsonUtility.getObjectFromJsonString(jsonData.toString(), AddressModel.class);
                        dismiss();
                        android.support.v4.app.Fragment mFragment = getFragmentManager().findFragmentByTag(ProfileTabFragment.TAG);

                        if (mFragment instanceof ProfileTabFragment) {
                            mFragment = ((ProfileTabFragment) mFragment).getObject();
                            if (mFragment instanceof ProfileDetailsFragmentnew)
                            {
                                ((ProfileDetailsFragmentnew) mFragment).getDataFromEditAddressDialog();
                            }

                            DialogFragment  mFragment1 = (DialogFragment) getFragmentManager().findFragmentByTag(AddressListProfileDialog.TAG);

                            if (mFragment1 instanceof AddressListProfileDialog) {

                                listOfAddress.set(addressPosition,addressModel);

                                ((AddressListProfileDialog) mFragment1).getDataFromEditAddressDialog(listOfAddress);

                            }

                        }
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        // Show message
                        Utility.showSnackBar(error_message, mBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.USER_DELETED:
                    case NetworkUtility.TAGS.STATUSCODETYPE.FORCE_LOGOUT_REQUIRED:
                        //Logout and finish the current activity
                        Utility.logout(getContext(), true, statusCode);

                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                mCallGetCityCareDetailsWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }

        }
    };
    private Response.ErrorListener mCallGetCityCareDetailsWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            hideProgressDialog();
            Log.e(TAG, "onErrorResponse() called with: error = [" + error + "]");
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
        }
    };

}
