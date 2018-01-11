package com.cheep.cheepcare.fragment;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
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
import com.cheep.cheepcare.adapter.AddressAdapter;
import com.cheep.cheepcare.adapter.ExpandablePackageServicesRecyclerAdapter;
import com.cheep.cheepcare.dialogs.BottomAddAddressDialog;
import com.cheep.cheepcare.model.CheepCarePackageServicesModel;
import com.cheep.cheepcare.model.PackageDetail;
import com.cheep.cheepcare.model.PackageOption;
import com.cheep.databinding.FragmentSelectPackageSpecificationBinding;
import com.cheep.fragment.BaseFragment;
import com.cheep.model.AddressModel;
import com.cheep.model.UserDetails;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.LogUtils;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.cheep.network.NetworkUtility.TAGS.CARE_PACKAGE_ID;
import static com.cheep.network.NetworkUtility.TAGS.DATA;

/**
 * Created by pankaj on 12/25/17.
 */

public class SelectPackageSpecificationsFragment extends BaseFragment {

    public static final String TAG = SelectPackageSpecificationsFragment.class.getSimpleName();
    private PackageCustomizationActivity mPackageCustomizationActivity;
    private FragmentSelectPackageSpecificationBinding mBinding;
    private boolean isVerified = false;
    private AddressAdapter<AddressModel> mAdapter;
    private List<AddressModel> mList;
    private boolean isClicked = false;
    BottomAddAddressDialog dialog;
    private CheepCarePackageServicesModel model;
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

        mBinding.ivIsAddressSelected.setSelected(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        mBinding.recyclerView.setLayoutManager(linearLayoutManager);
        mBinding.recyclerView.setNestedScrollingEnabled(false);

        callPackageOptionListWS();
        initAddressUI();
    }

    @Override
    public void setListener() {


    }

    private void callPackageOptionListWS() {
        LogUtils.LOGD(TAG, "callGetCityLandingCareDetailWS() called with: catId = [" + mPackageCustomizationActivity.mPackageId + "]");
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mBinding.getRoot());
            return;
        }

        showHindProgress(true);
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

    private void showHindProgress(boolean b) {
        mBinding.progressLoad.setVisibility(b ? View.VISIBLE : View.GONE);
    }

    private Response.Listener mCallGetCarePackageDetailsWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {
            showHindProgress(false);
            LogUtils.LOGD(TAG, "onResponse() called with: response = [" + response + "]");
            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                LogUtils.LOGI(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
                        ArrayList<CheepCarePackageServicesModel> list = new ArrayList<>();
                        model = (CheepCarePackageServicesModel) Utility.getObjectFromJsonString(jsonObject.optString(DATA), CheepCarePackageServicesModel.class);
                        list.add(model);
                        addPackagesOptionListToPackages(mPackageCustomizationActivity.mPackageId, list);
                        mBinding.recyclerView.setAdapter(new ExpandablePackageServicesRecyclerAdapter(list, true));
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

    private void addPackagesOptionListToPackages(String id, ArrayList<CheepCarePackageServicesModel> list) {
        for (PackageDetail detail : mPackageCustomizationActivity.getPackageList()) {
            if (detail.id.equalsIgnoreCase(id)) {
                detail.packageOptionList = list;
            }
        }
    }

    private Response.ErrorListener mCallGetCarePackageDetailsSingWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            showHindProgress(false);
            LogUtils.LOGD(TAG, "onErrorResponse() called with: error = [" + error + "]");
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
        }
    };

    private void initAddressUI() {
        // init ui for add address
        mBinding.lnAddAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog != null && dialog.isShowing())
                    dialog.dismiss();

                ArrayList<String> strings = new ArrayList<>();
                dialog = new BottomAddAddressDialog(SelectPackageSpecificationsFragment.this, new BottomAddAddressDialog.AddAddressListener() {
                    @Override
                    public void onAddAddress(AddressModel addressModel) {
                        mList.add(addressModel);
                        dialog.dismiss();
//                        mBinding.tvSpinnerAddress.setText(mList.get(mList.size() - 1).address);
//                        mBinding.ivIsAddressSelected.setSelected(true);
                    }
                }, strings);

                dialog.showDialog();
            }
        });


        // Spinner initialisation for select address view
        UserDetails userDetails = PreferenceUtility.getInstance(mPackageCustomizationActivity).getUserDetails();
        mList = new ArrayList<>();
        mList.clear();

        if (userDetails != null && !userDetails.addressList.isEmpty())
            mList = new ArrayList<>(userDetails.addressList);

        mList.add(0, new AddressModel() {{
            address = getString(R.string.label_select_address);
        }});

        mAdapter = new AddressAdapter<>(mContext
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
                    mBinding.tvSpinnerAddress.setText(getString(R.string.label_select_address));
                    mBinding.ivIsAddressSelected.setSelected(false);
                    return;
                }
                ImageSpan imageSpan = new ImageSpan(mContext, R.drawable.icon_address_home_active, ImageSpan.ALIGN_BASELINE);
                ForegroundColorSpan colorSpan = new ForegroundColorSpan(ContextCompat.getColor(mContext
                        , R.color.splash_gradient_end));

                SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
                spannableStringBuilder.append(Utility.ONE_CHARACTER_SPACE);
                spannableStringBuilder.append(Utility.ONE_CHARACTER_SPACE);
                spannableStringBuilder.setSpan(imageSpan, 0
                        , 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                spannableStringBuilder.append(Utility.ONE_CHARACTER_SPACE);

                String category;
                if (!TextUtils.isEmpty(mList.get(position).nickname))
                    category = mList.get(position).nickname;
                else
                    category = mList.get(position).category;

                if (!TextUtils.isEmpty(category)) {
                    spannableStringBuilder.append(category);
                    int startIndex = spannableStringBuilder.toString().indexOf(category);
                    int endIndex = startIndex + category.length();
                    spannableStringBuilder.setSpan(colorSpan, startIndex, endIndex,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannableStringBuilder.append(Utility.ONE_CHARACTER_SPACE);
                }

                spannableStringBuilder.append(mList.get(position).address);
                mBinding.tvSpinnerAddress.setText(spannableStringBuilder);
                mBinding.ivIsAddressSelected.setSelected(true);
                mSelectedAddress = mList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mBinding.tvSpinnerAddress.setText(getString(R.string.label_select_address));
            }
        });

        mBinding.tvSpinnerAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isClicked = true;
                mBinding.spinnerAddressSelection.performClick();
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Utility.PLACE_PICKER_REQUEST && dialog != null)
            dialog.onActivityResult(resultCode, data);

    }

    public boolean validateData() {


        boolean isAnyServiceSelected = false;
        for (PackageDetail detail : mPackageCustomizationActivity.getPackageList()) {
            if (detail.id.equalsIgnoreCase(mPackageCustomizationActivity.mPackageId)) {
                CheepCarePackageServicesModel model = detail.packageOptionList.get(0);
                if (model.selectionType.equalsIgnoreCase(CheepCarePackageServicesModel.SERVICE_TYPE.RADIO))
                    for (PackageOption option : model.getChildList()) {
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
}
