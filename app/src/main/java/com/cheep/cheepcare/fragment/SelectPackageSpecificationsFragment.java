package com.cheep.cheepcare.fragment;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.cheepcare.activity.PackageCustomizationActivity;
import com.cheep.cheepcare.adapter.AddressPackageCustomizationAdapter;
import com.cheep.cheepcare.adapter.ExpandablePackageServicesRecyclerAdapter;
import com.cheep.cheepcare.dialogs.BottomAddAddressDialog;
import com.cheep.cheepcare.model.PackageDetail;
import com.cheep.cheepcare.model.PackageOption;
import com.cheep.cheepcare.model.PackageSubOption;
import com.cheep.databinding.FragmentSelectPackageSpecificationBinding;
import com.cheep.fragment.BaseFragment;
import com.cheep.model.AddressModel;
import com.cheep.model.GuestUserDetails;
import com.cheep.model.UserDetails;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.LogUtils;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.cheep.network.NetworkUtility.TAGS.CARE_PACKAGE_ID;
import static com.cheep.network.NetworkUtility.TAGS.DATA;
import static com.cheep.network.NetworkUtility.TAGS.PACKAGE_OPTION_DETAILS;

/**
 * Created by pankaj on 12/25/17.
 */

public class SelectPackageSpecificationsFragment extends BaseFragment {

    public static final String TAG = SelectPackageSpecificationsFragment.class.getSimpleName();
    private PackageCustomizationActivity mPackageCustomizationActivity;
    private FragmentSelectPackageSpecificationBinding mBinding;
    private boolean isVerified = false;
    private AddressPackageCustomizationAdapter<AddressModel> mAdapter;
    private List<AddressModel> mList;
    private boolean isClicked = false;
    BottomAddAddressDialog addressDialog;
    public AddressModel mSelectedAddress;

    public static SelectPackageSpecificationsFragment newInstance() {
        return new SelectPackageSpecificationsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_select_package_specification, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initiateUI();
        setListener();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.d(TAG, "setUserVisibleHint() called with: isVisibleToUser = [" + isVisibleToUser + "]");
        if (!isVisibleToUser || mPackageCustomizationActivity == null) {
            return;
        }

        if (isVerified) {
            mPackageCustomizationActivity.setTaskState(PackageCustomizationActivity.STEP_ONE_VERIFIED);
        } else {
            mPackageCustomizationActivity.setTaskState(PackageCustomizationActivity.STEP_ONE_UNVERIFIED);
        }

        // Hide the post task button
//        mPackageCustomizationActivity.showPostTaskButton(false, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        BaseAppCompatActivity activity = (BaseAppCompatActivity) context;
        if (activity instanceof PackageCustomizationActivity) {
            mPackageCustomizationActivity = (PackageCustomizationActivity) activity;
        }
    }


    @Override
    public void initiateUI() {

        isClicked = false;
        mSelectedAddress = null;
        mBinding.ivIsAddressSelected.setSelected(false);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        mBinding.recyclerView.setLayoutManager(linearLayoutManager);
        mBinding.recyclerView.setNestedScrollingEnabled(false);


        callPackageOptionListWS();
        initAddressUI();
        initChipTipsUI();
    }

    @Override
    public void setListener() {
        mBinding.tvOtherBundlePackages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPackageCustomizationActivity.goToPackageBundling(PackageCustomizationActivity.STAGE_2);
            }
        });

    }

    private void callPackageOptionListWS() {
        LogUtils.LOGD(TAG, "callGetCityLandingCareDetailWS() called with: catId = [" + mPackageCustomizationActivity.mPackageId + "]");
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mBinding.getRoot());
            return;
        }

        showHideProgress(true);
        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());

        //Add Params
        Map<String, String> mParams = new HashMap<>();
        mParams.put(CARE_PACKAGE_ID, mPackageCustomizationActivity.mPackageId);

        //noinspection unchecked
        VolleyNetworkRequest mVolleyNetworkRequestForCategoryList = new VolleyNetworkRequest(NetworkUtility.WS.GET_CARE_PACKAGE_DETAILS
                , mCallGetCarePackageDetailsSingWSErrorListener
                , mCallGetCarePackageDetailsWSResponseListener
                , mHeaderParams
                , mParams
                , null);

        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForCategoryList, NetworkUtility.WS.GET_CARE_PACKAGE_DETAILS);

    }

    private void showHideProgress(boolean showProgress) {
        mBinding.progressLoad.setVisibility(showProgress ? View.VISIBLE : View.GONE);
    }

    private Response.Listener mCallGetCarePackageDetailsWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {
            showHideProgress(false);
            LogUtils.LOGD(TAG, "onResponse() called with: response = [" + response + "]");
            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                LogUtils.LOGI(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
                        String jsonData = jsonObject.optJSONObject(DATA).optString(PACKAGE_OPTION_DETAILS);
                        ArrayList<PackageOption> list;
                        list = Utility.getObjectListFromJsonString(jsonData, PackageOption[].class);
                        addPackagesOptionListToPackages(mPackageCustomizationActivity.mPackageId, list);
                        mPackageCustomizationActivity.setContinueButtonText();
                        mBinding.recyclerView.setAdapter(new ExpandablePackageServicesRecyclerAdapter(list, new ExpandablePackageServicesRecyclerAdapter.OnClickOfPackSubServiceListener() {
                            @Override
                            public void updateBottomButtonForSingleService(String selectedService, String price) {
                                mPackageCustomizationActivity.setContinueButtonText(selectedService, price);
                            }

                            @Override
                            public void updateBottomButtonForUnitService(int totalAppliance, String price) {
                                mPackageCustomizationActivity.setContinueButtonText(totalAppliance, price);
                            }
                        }));
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
                        Utility.logout(mContext, true, statusCode);
                        mPackageCustomizationActivity.finish();
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                mCallGetCarePackageDetailsSingWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }

        }
    };

    private void addPackagesOptionListToPackages(String id, ArrayList<PackageOption> list) {
        for (PackageDetail detail : mPackageCustomizationActivity.getPackageList()) {
            if (detail.id.equalsIgnoreCase(id)) {
                detail.packageOptionList = list;
            }
        }
    }

    private Response.ErrorListener mCallGetCarePackageDetailsSingWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            showHideProgress(false);
            LogUtils.LOGD(TAG, "onErrorResponse() called with: error = [" + error + "]");
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
        }
    };

    private void initAddressUI() {
        // init ui for add address
        mBinding.lnAddAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (addressDialog != null && addressDialog.isShowing())
                    addressDialog.dismiss();

                ArrayList<String> strings = new ArrayList<>();
                addressDialog = new BottomAddAddressDialog(SelectPackageSpecificationsFragment.this, new BottomAddAddressDialog.AddAddressListener() {
                    @Override
                    public void onAddAddress(AddressModel addressModel) {


                        //Saving information in shared preference

                        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
                        if (userDetails != null) {
                            userDetails.addressList.add(addressModel);
                            PreferenceUtility.getInstance(mContext).saveUserDetails(userDetails);
                        }
                        mList.add(addressModel);
                        verifyAddressForCity(addressModel);
                        addressDialog.dismiss();
                        if (!mList.isEmpty()) {
                            mBinding.llAddressView.setVisibility(View.VISIBLE);
                        }

                    }

                    @Override
                    public void onUpdateAddress(AddressModel addressModel) {

                    }
                }, strings, null);

                addressDialog.showDialog();
            }
        });


        // Spinner initialisation for select address view
        UserDetails userDetails = PreferenceUtility.getInstance(mPackageCustomizationActivity).getUserDetails();
        GuestUserDetails guestUserDetails = PreferenceUtility.getInstance(mPackageCustomizationActivity).getGuestUserDetails();
        mList = new ArrayList<>();
        mList.clear();

        if (userDetails != null && !userDetails.addressList.isEmpty())
            mList.addAll(userDetails.addressList);
        else {
            if (guestUserDetails.addressList != null)
                mList.addAll(guestUserDetails.addressList);
        }

        if (mList.isEmpty()) {
            mBinding.llAddressView.setVisibility(View.GONE);
        }

        mList.add(0, new AddressModel() {{
            address = getString(R.string.label_select_address);
        }});

        mAdapter = new AddressPackageCustomizationAdapter<>(mContext
                , android.R.layout.simple_spinner_item
                , mList);
        mBinding.spinnerAddressSelection.setAdapter(mAdapter);
        mBinding.spinnerAddressSelection.setFocusable(false);
        mBinding.spinnerAddressSelection.setPrompt("Prompt");
        mBinding.spinnerAddressSelection.setSelected(false);
        mBinding.spinnerAddressSelection.setFocusableInTouchMode(false);
        mBinding.spinnerAddressSelection.setSelection(-1);
        mBinding.spinnerAddressSelection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isClicked && position == 0) {
                    mBinding.tvSelectAddress.setText(getString(R.string.label_select_address));
                    mBinding.ivIsAddressSelected.setSelected(false);
                    mBinding.llAddressContainer.setVisibility(View.GONE);
                    mBinding.tvSelectAddress.setVisibility(View.VISIBLE);
                    return;
                }

                AddressModel model = mList.get(position);
                verifyAddressForCity(model);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mBinding.tvSelectAddress.setText(getString(R.string.label_select_address));
            }
        });

        mBinding.flAddressContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isClicked = true;
                mBinding.spinnerAddressSelection.performClick();
            }
        });
    }

    private void fillAddressView(AddressModel model) {

        mBinding.llAddressContainer.setVisibility(View.VISIBLE);
        mBinding.tvSelectAddress.setVisibility(View.GONE);


        mBinding.iconTaskWhere.setImageDrawable(ContextCompat.getDrawable(mContext
                , Utility.getAddressCategoryBlueIcon(model.category)));

        // show address's nick name or nick name is null then show category
        String category;
        if (!TextUtils.isEmpty(model.nickname))
            category = model.nickname;
        else
            category = model.category;

        mBinding.tvAddressNickname.setText(category);

        mBinding.tvAddress.setText(model.address);
        mBinding.ivIsAddressSelected.setSelected(true);
        mSelectedAddress = model;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Utility.PLACE_PICKER_REQUEST && addressDialog != null)
            addressDialog.onActivityResult(resultCode, data);

    }

    public boolean validateData() {
        boolean isAnyServiceSelected = false;
        for (PackageDetail detail : mPackageCustomizationActivity.getPackageList()) {
            if (detail.id.equalsIgnoreCase(mPackageCustomizationActivity.mPackageId)) {
                PackageOption model = null;
                if (detail.packageOptionList != null) {
                    model = detail.packageOptionList.get(0);
                }
                if (model == null)
                    return false;

                if (model.selectionType.equalsIgnoreCase(PackageOption.SELECTION_TYPE.RADIO))
                    for (PackageSubOption option : model.getChildList()) {
                        if (option.isSelected) {
                            isAnyServiceSelected = true;
                        }
                    }
                else {
                    isAnyServiceSelected = true;
                }
            }
        }

        if (mSelectedAddress == null) {
            Utility.showSnackBar("Please Selected Address", mBinding.getRoot());
            return false;
        } else if (!isAnyServiceSelected) {
            Utility.showSnackBar("Please select any service", mBinding.getRoot());
            return false;
        }
        return true;
    }

    private void verifyAddressForCity(final AddressModel model) {

        showProgressDialog();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        if (PreferenceUtility.getInstance(mContext).getUserDetails() != null)
            mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);

        //Add Params
        Map<String, Object> mParams = new HashMap<>();
        int addressId;
        try {
            addressId = Integer.parseInt(model.address_id);
        } catch (Exception e) {
            addressId = 0;
        }
        if (addressId <= 0) {
            // In case its nagative then provide other address information
            mParams.put(NetworkUtility.TAGS.ADDRESS_ID, Utility.ZERO_STRING);
            mParams.put(NetworkUtility.TAGS.CITY_NAME, model.cityName);
        } else {
            mParams.put(NetworkUtility.TAGS.ADDRESS_ID, model.address_id);
            mParams.put(NetworkUtility.TAGS.CITY_NAME, Utility.EMPTY_STRING);
        }

        Utility.hideKeyboard(mContext);
        @SuppressWarnings("unchecked")
        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.WS.VERIFY_ADDRESS_CHEEP_CARE
                , new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                LogUtils.LOGD(TAG, "onErrorResponse() called with: error = [" + error + "]");
                // Close Progressbar
                hideProgressDialog();
                // Show Toast
                Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
            }
        }
                , new Response.Listener() {
            @Override
            public void onResponse(Object response) {
                hideProgressDialog();
                Log.i(TAG, "onResponse: " + response);

                String strResponse = (String) response;
                try {
                    JSONObject jsonObject = new JSONObject(strResponse);
                    Log.i(TAG, "onResponse: " + jsonObject.toString());
                    int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                    String error_message;

                    switch (statusCode) {
                        case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
                            JSONObject jsonData = jsonObject.getJSONObject(NetworkUtility.TAGS.DATA);
                            // strategic partner pro id for given location
                            String city_id = jsonData.optString(NetworkUtility.TAGS.CITY_ID);
                            if (mPackageCustomizationActivity.mCityDetail.id.equalsIgnoreCase(city_id)) {
                                fillAddressView(model);
                            } else {
                                Utility.showToast(mPackageCustomizationActivity, getString(R.string.validation_message_cheep_care_address, mPackageCustomizationActivity.mCityDetail.cityName));
                            }

                            break;
                        case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                            // Show Toast
                            Utility.showToast(mPackageCustomizationActivity, getString(R.string.label_something_went_wrong));
                            break;
                        case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                            error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                            // Show message
                            Utility.showToast(mPackageCustomizationActivity, error_message);
                            break;
                        case NetworkUtility.TAGS.STATUSCODETYPE.USER_DELETED:
                        case NetworkUtility.TAGS.STATUSCODETYPE.FORCE_LOGOUT_REQUIRED:
                            //Logout and finish the current activity
                            Utility.logout(mContext, true, statusCode);
                            mPackageCustomizationActivity.finish();
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    mCallGetCarePackageDetailsSingWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
                }
                hideProgressDialog();
            }
        }
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest, NetworkUtility.WS.CHECK_PRO_AVAILABILITY_FOR_STRATEGIC_TASK);

    }

    private void initChipTipsUI() {
        ViewGroup.LayoutParams params = mBinding.rlChipTips.getLayoutParams();
        params.height = (int) getResources().getDimension(R.dimen.scale_30dp);
        mBinding.rlChipTips.setLayoutParams(params);
        mBinding.rlChipTips.setSelected(false);
        mBinding.ivBird.setImageResource(R.drawable.ic_cheep_bird_tip);
        mBinding.ivCross.setImageResource(R.drawable.ic_drop_down_arrow);
        mBinding.ivCross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBinding.rlChipTips.isSelected()) {

                    mBinding.ivCross.setImageResource(R.drawable.ic_drop_down_arrow);
                    mBinding.ivBird.setImageResource(R.drawable.ic_cheep_bird_tip);
                    mBinding.rlChipTips.setSelected(false);

                    ViewGroup.LayoutParams params = mBinding.rlChipTips.getLayoutParams();
                    params.height = (int) getResources().getDimension(R.dimen.scale_30dp);
                    mBinding.rlChipTips.setLayoutParams(params);

                } else {

                    mBinding.ivCross.setImageResource(R.drawable.icon_cross_blue);
                    mBinding.rlChipTips.setSelected(true);
                    mBinding.ivBird.setImageResource(R.drawable.ic_cheep_bird_tip_big);

                    ViewGroup.LayoutParams params = mBinding.rlChipTips.getLayoutParams();
                    params.height = (int) getResources().getDimension(R.dimen.scale_50dp);
                    mBinding.rlChipTips.setLayoutParams(params);

                }
            }
        });
    }

}