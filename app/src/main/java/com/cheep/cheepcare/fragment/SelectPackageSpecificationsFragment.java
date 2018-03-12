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
import com.cheep.utils.GsonUtility;
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

public class SelectPackageSpecificationsFragment extends BaseFragment {

    public static final String TAG = "SelectPackageSpecificat";
    private PackageCustomizationActivity mPackageCustomizationActivity;
    private FragmentSelectPackageSpecificationBinding mBinding;
    private boolean isVerified = false;
    private AddressPackageCustomizationAdapter<AddressModel> mAddressAdapter;
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
        mBinding.cvCheepTip.setVisibility(View.GONE);

        initCheepTipsUI();
        initAddressUI();
        callPackageOptionListWS();
        callPackageCheepTipWS();
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
        LogUtils.LOGD(TAG, "callGetCityLandingCareDetailWS() called with: catId = [" + mPackageCustomizationActivity.mSelectedPackageModel + "]");
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
        mParams.put(CARE_PACKAGE_ID, mPackageCustomizationActivity.mSelectedPackageModel.id);

        //noinspection unchecked
        VolleyNetworkRequest mVolleyNetworkRequestForCategoryList = new VolleyNetworkRequest(NetworkUtility.WS.GET_CARE_PACKAGE_DETAILS
                , mCallGetCarePackageDetailsSingWSErrorListener
                , mCallGetCarePackageDetailsWSResponseListener
                , mHeaderParams
                , mParams
                , null);

        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForCategoryList, NetworkUtility.WS.GET_CARE_PACKAGE_DETAILS);

    }

    private void callPackageCheepTipWS() {
        LogUtils.LOGD(TAG, "callPackageCheepTipWS() called with: catId = [" + mPackageCustomizationActivity.mSelectedPackageModel.id + "]");
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mBinding.getRoot());
            return;
        }

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());

        //Add Params
        Map<String, String> mParams = new HashMap<>();
        mParams.put(CARE_PACKAGE_ID, mPackageCustomizationActivity.mSelectedPackageModel.id);

        //noinspection unchecked
        VolleyNetworkRequest mVolleyNetworkRequestForCategoryList = new VolleyNetworkRequest(NetworkUtility.WS.GET_CARE_PACKAGE_TIP
                , mCallGetCareTipWSErrorListener
                , mCallGetCheepTipResponseListener
                , mHeaderParams
                , mParams
                , null);

        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForCategoryList, NetworkUtility.WS.GET_CARE_PACKAGE_TIP);

    }

    private Response.Listener mCallGetCheepTipResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {
            LogUtils.LOGD(TAG, "onResponse() called with: response = [" + response + "]");
            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                LogUtils.LOGI(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
                        JSONObject data = jsonObject.getJSONObject(NetworkUtility.TAGS.DATA);
                        String title = data.optString(NetworkUtility.TAGS.TITLE);
                        String subTitle = data.optString(NetworkUtility.TAGS.SUBTITLE);
                        if (TextUtils.isEmpty(title) && TextUtils.isEmpty(subTitle)) {
                            mBinding.cvCheepTip.setVisibility(View.GONE);
                        } else {
                            mBinding.tvCheepTipsTitle.setText(title);
                            mBinding.tvCheepTipsDesc.setText(subTitle);
                            mBinding.cvCheepTip.setVisibility(View.VISIBLE);
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
                        list = GsonUtility.getObjectListFromJsonString(jsonData, PackageOption[].class);

                        addPackagesOptionListToPackages(mPackageCustomizationActivity.mSelectedPackageModel.id, list);

//                        mPackageCustomizationActivity.setContinueButtonText();

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

    /**
     * if user has saved details for given care package then set selected sub options and quantity of sub option
     *
     * @param id   care package id
     * @param list list of packageoption fetched from web service
     */
    private void addPackagesOptionListToPackages(String id, ArrayList<PackageOption> list) {
        int totalCount = 0;
        double monthlyPrice = 0;

        // main care package list
        for (PackageDetail detail : mPackageCustomizationActivity.getPackageList()) {
            if (detail.id.equalsIgnoreCase(id)) {
                ArrayList<PackageOption> tempList = detail.packageOptionList;
                if (tempList != null && !tempList.isEmpty())
                    for (PackageOption packageOption : list) {
                        for (PackageOption tempPackageOption : tempList) {
                            if (tempPackageOption.packageId.equalsIgnoreCase(packageOption.packageId)) {
                                LogUtils.LOGE(TAG, "packageId : " + tempPackageOption.packageId);
                                monthlyPrice = Double.parseDouble(packageOption.getChildList().get(0).monthlyPrice);
                                LogUtils.LOGE(TAG, "monthly price : " + monthlyPrice);
                                for (PackageSubOption packageSubOption : packageOption.packageOptionList) {
                                    for (PackageSubOption tempSubOption : tempPackageOption.packageOptionList) {
                                        if (packageSubOption.packageOptionId.equalsIgnoreCase(tempSubOption.packageOptionId))
                                            if (packageOption.selectionType.equalsIgnoreCase(PackageOption.SELECTION_TYPE.RADIO)) {
                                                packageSubOption.isSelected = tempSubOption.isSelected;
                                            } else {
                                                packageSubOption.qty = tempSubOption.qty;
                                                totalCount += packageSubOption.qty;
                                                monthlyPrice += Double.parseDouble(packageSubOption.unitPrice) * (packageSubOption.qty - 1);
                                                LogUtils.LOGE(TAG, "monthly price calculatd: " + monthlyPrice);
                                            }
                                    }
                                }
                                if (packageOption.selectionType.equalsIgnoreCase(PackageOption.SELECTION_TYPE.CHECK_BOX))
                                    mPackageCustomizationActivity.setContinueButtonText(totalCount, String.valueOf(monthlyPrice));
                            }
                        }
                    }
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
    private Response.ErrorListener mCallGetCareTipWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            LogUtils.LOGD(TAG, "onErrorResponse() called with: error = [" + error + "]");
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
        }
    };

    /**
     * set spinner data with guest user address list of logged in user address list
     */
    private void initAddressUI() {

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
            address_id = "";
        }});

        mAddressAdapter = new AddressPackageCustomizationAdapter<>(mContext
                , android.R.layout.simple_spinner_item
                , mList);
        mBinding.spinnerAddressSelection.setAdapter(mAddressAdapter);
        mBinding.spinnerAddressSelection.setSelected(true);
        mBinding.spinnerAddressSelection.setFocusableInTouchMode(true);

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
                verifyAddressForCity(model, false, 0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mBinding.tvSelectAddress.setText(getString(R.string.label_select_address));
            }
        });

        // if package option are being loaded from cart detail then set selected address
        for (PackageDetail detail : mPackageCustomizationActivity.getPackageList()) {
            if (detail.id.equalsIgnoreCase(mPackageCustomizationActivity.mSelectedPackageModel.id)) {
                if (detail.mSelectedAddressList != null && !detail.mSelectedAddressList.isEmpty()) {
                    AddressModel addressModel = detail.mSelectedAddressList.get(0);
                    if (addressModel != null) {
                        for (int i = 0; i < mList.size(); i++) {
                            AddressModel tempAdd = mList.get(i);
                            if (tempAdd.address_id.equalsIgnoreCase(addressModel.address_id))
                                mBinding.spinnerAddressSelection.setSelection(i);
                        }
                    }
                }
            }
        }

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
                        } else {
                            GuestUserDetails guestUserDetails = PreferenceUtility.getInstance(mContext).getGuestUserDetails();
                            if (guestUserDetails.addressList == null)
                                guestUserDetails.addressList = new ArrayList<>();
                            guestUserDetails.addressList.add(addressModel);
                            PreferenceUtility.getInstance(mContext).saveGuestUserDetails(guestUserDetails);
                        }
                        mList.add(addressModel);
                        verifyAddressForCity(addressModel, false, 0);
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

        mBinding.flAddressContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isClicked = true;
                mBinding.spinnerAddressSelection.performClick();
            }
        });
    }

    /**
     * fill UI of address row
     * if address is correct for selected city and user has no subcription of given address
     *
     * @param addressModel
     */
    private void fillAddressView(AddressModel addressModel) {

        mBinding.llAddressContainer.setVisibility(View.VISIBLE);
        mBinding.tvSelectAddress.setVisibility(View.GONE);


        mBinding.iconTaskWhere.setImageDrawable(ContextCompat.getDrawable(mContext
                , Utility.getAddressCategoryBlueIcon(addressModel.category)));

        // show address's nick name or nick name is null then show category
        mBinding.tvAddressNickname.setText(addressModel.getNicknameString(mContext));

        mBinding.tvAddress.setText(addressModel.getAddressWithInitials());
        mBinding.ivIsAddressSelected.setSelected(true);
        mSelectedAddress = addressModel;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Utility.PLACE_PICKER_REQUEST && addressDialog != null)
            addressDialog.onActivityResult(resultCode, data);

    }


    /**
     * @return true if address and service is selected
     * false if address or service is not selected
     */
    public boolean validateData() {
        boolean isAnyServiceSelected = false;
        for (PackageDetail detail : mPackageCustomizationActivity.getPackageList()) {
            if (detail.id.equalsIgnoreCase(mPackageCustomizationActivity.mSelectedPackageModel.id)) {
                PackageOption packageOption = null;
                if (detail.packageOptionList != null) {
                    packageOption = detail.packageOptionList.get(0);
                }
                if (packageOption == null)
                    return false;

                if (packageOption.selectionType.equalsIgnoreCase(PackageOption.SELECTION_TYPE.RADIO))
                    for (PackageSubOption option : packageOption.getChildList()) {
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

    /**
     * verfiy selected address is correct for selected city, and also if user has already purchased subscription
     *
     * @param model              address model
     * @param isCalledAfterLogin true if it is called from payment summary screen
     * @param step               after successful of login go next step of stage could be payment summary fragment or package bundling screen
     */
    public void verifyAddressForCity(final AddressModel model, final boolean isCalledAfterLogin, final int step) {

        //Add Header parameters

        PackageDetail detail = mPackageCustomizationActivity.mSelectedPackageModel;
        for (PackageDetail checkPackagedetail : mPackageCustomizationActivity.getPackageList()) {
            if (!detail.id.equalsIgnoreCase(checkPackagedetail.id) && checkPackagedetail.isSelected)
                if (detail.packageType.equalsIgnoreCase(checkPackagedetail.packageType)) {
                    if (checkPackagedetail.mSelectedAddressList != null && !checkPackagedetail.mSelectedAddressList.isEmpty())
                        if (model.address_id.equalsIgnoreCase(checkPackagedetail.mSelectedAddressList.get(0).address_id)) {
                            Utility.showToast(mPackageCustomizationActivity, getString(R.string.validation_message_same_address_for_same_group_of_care));
                            return;
                        }
                }
        }

        showProgressDialog();

        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).

                getXAPIKey());
        if (PreferenceUtility.getInstance(mContext).

                getUserDetails() != null)
            mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).

                    getUserDetails().userID);
        if (isCalledAfterLogin)

        {
            mList.clear();
            mList.addAll(PreferenceUtility.getInstance(mContext).getUserDetails().addressList);
        }


        //Add Params
        Map<String, Object> mParams = new HashMap<>();
        int addressId;
        try

        {
            addressId = Integer.parseInt(model.address_id);
        } catch (
                Exception e)

        {
            addressId = 0;
        }
        if (addressId <= 0)

        {
            // In case its nagative then provide other address information
            mParams.put(NetworkUtility.TAGS.ADDRESS_ID, Utility.ZERO_STRING);
            mParams.put(NetworkUtility.TAGS.CITY_NAME, model.cityName);
        } else

        {
            mParams.put(NetworkUtility.TAGS.ADDRESS_ID, model.address_id);
            mParams.put(NetworkUtility.TAGS.CARE_CITY_ID, mPackageCustomizationActivity.mCityDetail.id);
            mParams.put(NetworkUtility.TAGS.CITY_NAME, Utility.EMPTY_STRING);
            mParams.put(NetworkUtility.TAGS.CARE_PACKAGE_ID, mPackageCustomizationActivity.mSelectedPackageModel.id);
            mParams.put(NetworkUtility.TAGS.PACKAGE_TYPE, mPackageCustomizationActivity.mSelectedPackageModel.packageType);
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
                            String isPurchased = jsonData.optString(NetworkUtility.TAGS.IS_PURCHASED);
                            if (mPackageCustomizationActivity.mCityDetail.id.equalsIgnoreCase(city_id)) {
                                if (isPurchased.equalsIgnoreCase(Utility.BOOLEAN.YES)) {
                                    Utility.showToast(mPackageCustomizationActivity, getString(R.string.validation_message_already_purchased_package_for_this_address));
                                } else {
                                    String isSamePackageType = jsonData.optString(NetworkUtility.TAGS.IS_SAME_PACKAGE_TYPE);
                                    if (isSamePackageType.equalsIgnoreCase(Utility.BOOLEAN.YES)) {
                                        Utility.showToast(mPackageCustomizationActivity, getString(R.string.validation_message_same_address_for_same_group_of_care));
                                    } else {
                                        fillAddressView(model);
                                        if (isCalledAfterLogin) {
                                            mPackageCustomizationActivity.gotoStep(step);
                                        }
                                    }
                                }

                            } else {
                                Utility.showToast(mPackageCustomizationActivity, getString(R.string.validation_message_cheep_care_address, mPackageCustomizationActivity.mCityDetail.cityName));
                                if (isCalledAfterLogin) {
                                    mBinding.llAddressContainer.setVisibility(View.GONE);
                                    mBinding.tvSelectAddress.setVisibility(View.VISIBLE);
                                }
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
        Volley.getInstance(mContext).

                addToRequestQueue(mVolleyNetworkRequest, NetworkUtility.WS.VERIFY_ADDRESS_CHEEP_CARE);

    }

    /**
     * cheep tips UI manage code
     */
    private void initCheepTipsUI() {
        mBinding.rlChipTips.setVisibility(View.VISIBLE);
        ViewGroup.LayoutParams params = mBinding.rlChipTips.getLayoutParams();
        params.height = (int) getResources().getDimension(R.dimen.scale_30dp);
        mBinding.rlChipTips.setLayoutParams(params);
        mBinding.rlChipTips.setSelected(false);
        mBinding.ivBird.setImageResource(R.drawable.bird_cheep_tip);
        mBinding.ivCross.setImageResource(R.drawable.ic_drop_down_arrow);
        mBinding.ivCross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBinding.rlChipTips.isSelected()) {
//                    mBinding.rlChipTips.setVisibility(View.GONE);
                    mBinding.ivCross.setImageResource(R.drawable.ic_drop_down_arrow);
                    mBinding.ivBird.setImageResource(R.drawable.bird_cheep_tip);
                    mBinding.rlChipTips.setSelected(false);
                    ViewGroup.LayoutParams params = mBinding.rlChipTips.getLayoutParams();
                    params.height = (int) getResources().getDimension(R.dimen.scale_30dp);
                    mBinding.rlChipTips.setLayoutParams(params);

                } else {

                    mBinding.ivCross.setImageResource(R.drawable.icon_cross_blue);
                    mBinding.rlChipTips.setSelected(true);
                    mBinding.ivBird.setImageResource(R.drawable.bird_cheep_tip_big);

                    ViewGroup.LayoutParams params = mBinding.rlChipTips.getLayoutParams();
                    params.height = (int) getResources().getDimension(R.dimen.scale_50dp);
                    mBinding.rlChipTips.setLayoutParams(params);

                }
            }
        });
    }

    @Override
    public void onDestroy() {
        Volley.getInstance(mPackageCustomizationActivity).getRequestQueue().cancelAll(NetworkUtility.WS.GET_CARE_PACKAGE_DETAILS);
        Volley.getInstance(mPackageCustomizationActivity).getRequestQueue().cancelAll(NetworkUtility.WS.VERIFY_ADDRESS_CHEEP_CARE);
        Volley.getInstance(mPackageCustomizationActivity).getRequestQueue().cancelAll(NetworkUtility.WS.GET_CARE_PACKAGE_TIP);
        super.onDestroy();
    }
}
