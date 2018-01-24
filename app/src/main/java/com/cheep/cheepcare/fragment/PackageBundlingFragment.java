package com.cheep.cheepcare.fragment;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.cheepcare.activity.PackageCustomizationActivity;
import com.cheep.cheepcare.adapter.PackageBundlingAdapter;
import com.cheep.cheepcare.model.PackageDetail;
import com.cheep.databinding.FragmentPackageBundlingBinding;
import com.cheep.fragment.BaseFragment;
import com.cheep.model.AddressModel;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.LogUtils;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PackageBundlingFragment extends BaseFragment {

    public static final String TAG = "PackageBundlingFragment";
    private PackageCustomizationActivity mPackageCustomizationActivity;
    private FragmentPackageBundlingBinding mBinding;
    private boolean isVerified = false;
    private PackageBundlingAdapter mPackageAdapter;

    public static PackageBundlingFragment newInstance() {
        return new PackageBundlingFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_package_bundling, container, false);
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
            mPackageCustomizationActivity.setTaskState(PackageCustomizationActivity.STEP_TWO_VERIFIED);
        } else {
            mPackageCustomizationActivity.setTaskState(PackageCustomizationActivity.STEP_TWO_UNVERIFIED);
        }
        mPackageAdapter = new PackageBundlingAdapter(new PackageBundlingAdapter.PackageItemClickListener() {
            @Override
            public void onPackageItemClick(int position, PackageDetail packageModel) {
                mPackageCustomizationActivity.mPackageId = packageModel.id;
                mPackageCustomizationActivity.gotoStep(PackageCustomizationActivity.STAGE_1);
                mPackageCustomizationActivity.loadAnotherPackage();
            }

            @Override
            public void onUpdateOfAddress(int position, AddressModel addressModel) {
                verifyAddressForCity(position, addressModel);
            }
        });
        mPackageAdapter.addPakcageList(getList());
        mBinding.rvBundlePackages.setLayoutManager(new LinearLayoutManager(
                mContext
                , LinearLayoutManager.VERTICAL
                , false
        ));
        mBinding.rvBundlePackages.setAdapter(mPackageAdapter);


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
        mBinding.rvBundlePackages.setNestedScrollingEnabled(false);
    }

    private List<PackageDetail> getList() {

        Collections.sort(mPackageCustomizationActivity.getPackageList(), new Comparator<PackageDetail>() {
            @Override
            public int compare(PackageDetail o1, PackageDetail o2) {
                boolean b1 = o1.isSelected;
                boolean b2 = o2.isSelected;

                return (b1 != b2) ? (b1) ? -1 : 1 : 0;
            }
        });
        ArrayList<PackageDetail> newList = new ArrayList<>();
        boolean isHeaderAdded = false;
        int count = 0;
        for (PackageDetail model : mPackageCustomizationActivity.getPackageList()) {
            if (model.isSelected) {
                model.rowType = PackageBundlingAdapter.ROW_PACKAGE_SELECTED;
                count++;
            } else {
                if (count > 0 && !isHeaderAdded) {
                    PackageDetail model1 = new PackageDetail();
                    model1.rowType = PackageBundlingAdapter.ROW_PACKAGE_HEADER;
                    newList.add(model1);
                    isHeaderAdded = true;
                }
                model.rowType = PackageBundlingAdapter.ROW_PACKAGE_NOT_SELECTED;
            }
            newList.add(model);
        }
        return newList;
    }

    @Override
    public void setListener() {

    }

    private void verifyAddressForCity(final int adapterPosition, final AddressModel model) {

        showProgressDialog();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        if (PreferenceUtility.getInstance(mContext).getUserDetails() != null)
            mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().userID);

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
                , errorListener
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
                                mPackageAdapter.getList().get(adapterPosition).mSelectedAddressList.add(model);
                                mPackageAdapter.notifyItemChanged(adapterPosition);
                            } else {
                                Utility.showToast(mPackageCustomizationActivity, getString(R.string.validation_message_cheep_care_address,mPackageCustomizationActivity.mCityDetail.cityName));
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
                    errorListener.onErrorResponse(new VolleyError(e.getMessage()));
                }
                hideProgressDialog();
            }
        }
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequest, NetworkUtility.WS.CHECK_PRO_AVAILABILITY_FOR_STRATEGIC_TASK);

    }

    Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            LogUtils.LOGD(TAG, "onErrorResponse() called with: error = [" + error + "]");
            // Close Progressbar
            hideProgressDialog();
            // Show Toast
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
        }

    };
}