package com.cheep.cheepcarenew.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.R;
import com.cheep.cheepcare.fragment.ProfileTabFragment;
import com.cheep.cheepcarenew.adapters.ManageSubscriptionAddressAdapter;
import com.cheep.cheepcarenew.model.ManageSubscriptionModel;
import com.cheep.databinding.FragmentManageSubscriptionBinding;
import com.cheep.fragment.BaseFragment;
import com.cheep.model.AddressModel;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.GsonUtility;
import com.cheep.utils.LogUtils;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ManageSubscriptionFragment extends BaseFragment implements
        ManageSubscriptionAddressAdapter.AddressItemClickListener,
        View.OnClickListener{

    public static final String TAG = "ManageSubscriptionFragment";
    private ArrayList<ManageSubscriptionModel> addressList;
    private ManageSubscriptionAddressAdapter adapter;
    private FragmentManageSubscriptionBinding mBinding;
    private LinearLayoutManager linearLayoutManager;
    int listPosition = 0;
    int dateDifference = 0;

    public static ManageSubscriptionFragment newInstance(ArrayList<AddressModel> list) {
        Bundle args = new Bundle();
        args.putSerializable("list", list);
        ManageSubscriptionFragment fragment = new ManageSubscriptionFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_manage_subscription, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        Log.i(TAG, "onAttach: ");
        super.onAttach(context);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initiateUI();
        setListener();
        callManageSubscriptionWS();
    }

    @Override
    public void initiateUI() {

    }

    @Override
    public void setListener() {
        mBinding.back.setOnClickListener(this);
        mBinding.notification.setOnClickListener(this);
        mBinding.addressDropDowns.setOnClickListener(this);
        mBinding.addressDropUp.setOnClickListener(this);
        //mBinding.upgradeSubscriptionBtn.setOnClickListener(this);
       // mBinding.autoRenewalToggle.setOnClickListener(this);
        mBinding.addressDropDownsSingle.setOnClickListener(this);
        mBinding.addressDropDowns.setOnClickListener(this);

    }

    @Override
    public void onDetach() {
        Log.i(TAG, "onDetach: ");
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.MANAGE_SUBSCRIPTION);

        super.onDetach();
    }
    private void setAllAddressOnView(){
        mBinding.onlyOneAddressLayout.setVisibility(View.INVISIBLE);
        mBinding.allAddressLayout.setVisibility(View.VISIBLE);
        mBinding.addressDropDowns.setVisibility(View.VISIBLE);
        mBinding.addressDropUp.setVisibility(View.GONE);
        linearLayoutManager = new LinearLayoutManager(mContext);
        mBinding.subscriptionRecyclerView.setLayoutManager(linearLayoutManager);
        mBinding.subscriptionRecyclerView.setHasFixedSize(false);
        mBinding.subscriptionRecyclerView.setNestedScrollingEnabled(false);
        adapter = new ManageSubscriptionAddressAdapter(addressList, 0, ManageSubscriptionFragment.this);
        mBinding.subscriptionRecyclerView.setAdapter(adapter);
    }

    private void setOnlyOneAddressOnView() {
        mBinding.onlyOneAddressLayout.setVisibility(View.VISIBLE);
        mBinding.allAddressLayout.setVisibility(View.GONE);
        int count = 0;
        for (int i = 0; i < addressList.size(); i++) {
            if (listPosition == count) {
                mBinding.textAddressCategory.setText(Utility.getAddressCategoryString(addressList.get(i).category));
                mBinding.textAddressCategory.setCompoundDrawablesWithIntrinsicBounds(Utility.getAddressCategoryBlueIcon(addressList.get(i).category), 0, 0, 0);
            }
            count++;
        }
        setAllField();
    }

    private void setAllField() {
        int count = 0;
        for (int i = 0; i < addressList.size(); i++) {
            if (listPosition == count) {
               /* mBinding.textSubscriptionDuration.setText(addressList.get(i).packageDuration);
                mBinding.textAmountPaid.setText(addressList.get(i).paidAmount);
                mBinding.textPaymentMethod.setText(addressList.get(i).paymentType);
                mBinding.textSubscribedOn.setText(addressList.get(i).startDate);
                mBinding.textSubscriptionEndDate.setText(addressList.get(i).endDate);

                if (addressList.get(i).isRenew.equalsIgnoreCase(Utility.YES)) {
                    mBinding.relativeAutoRenewal.setVisibility(View.VISIBLE);
                    mBinding.renewLl.setVisibility(View.GONE);
                }else {
                    mBinding.relativeAutoRenewal.setVisibility(View.GONE);
                    mBinding.renewLl.setVisibility(View.VISIBLE);
                }
                dateDifference = Utility.getDifferenceBetweenTwoDate(addressList.get(i).startDate,addressList.get(i).endDate);
                if( dateDifference >= Utility.TEN){
                    mBinding.renewBtn.setTextColor(getResources().getColor(R.color.dark_blue));
                }else {
                    mBinding.renewBtn.setTextColor(getResources().getColor(R.color.black_translucent_1));
                }*/

            }
            count++;
        }

    }

    // ManageSubscriptionAddressAdapter.AddressItemClickListener
    @Override
    public void onClickItem(ArrayList<ManageSubscriptionModel> mList, int position)

    {
        this.addressList = mList;
        listPosition = position;
        setOnlyOneAddressOnView();


    }

    //View.OnClickListener
    @Override
    public void onClick(View view) {
         switch (view.getId()){
             case R.id.address_drop_downs_single:
                 setAllAddressOnView();
                 break;
             case R.id.address_drop_downs:
                 mBinding.onlyOneAddressLayout.setVisibility(View.VISIBLE);
                 mBinding.allAddressLayout.setVisibility(View.GONE);
                 break;
         }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// Category Manage Subscription [END]///////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void callManageSubscriptionWS() {
        if (!Utility.isConnected(getContext())) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mBinding.getRoot());
            return;
        }

        showProgressDialog();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(getContext()).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().userID);

        //noinspection unchecked
        VolleyNetworkRequest mVolleyNetworkRequestForCategoryList = new VolleyNetworkRequest(NetworkUtility.WS.MANAGE_SUBSCRIPTION
                , mCallGetCityCareDetailsWSErrorListener
                , mCallGetCityCareDetailsWSResponseListener
                , mHeaderParams
                , null
                , null);

        Volley.getInstance(getContext()).addToRequestQueue(mVolleyNetworkRequestForCategoryList, NetworkUtility.WS.MANAGE_SUBSCRIPTION);
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
                        JSONObject jsonData = jsonObject.optJSONObject(NetworkUtility.TAGS.DATA);
                        addressList = GsonUtility.getObjectListFromJsonString(jsonData.optString(NetworkUtility.TAGS.MANAGE_SUBSCRIPTION_PACKAGE), ManageSubscriptionModel[].class);
                        setOnlyOneAddressOnView();
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