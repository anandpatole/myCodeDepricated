package com.cheep.cheepcarenew.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.R;


import com.cheep.adapter.RateRecyclerViewAdapter;
import com.cheep.cheepcarenew.model.CheepCareRateCardModel;
import com.cheep.databinding.FragmentRateCardPricingBinding;
import com.cheep.fragment.BaseFragment;
import com.cheep.model.RateCardModel;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.GsonUtility;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CheepCareRateCardPricingFragment extends BaseFragment {

    public static final String TAG = "CheepCareRateCardPricingFragment";
    private FragmentRateCardPricingBinding mBinding;
    static String subCategory = "";
    private  ArrayList<RateCardModel> list;

    private String cardID;
    private String name = "";

    public static CheepCareRateCardPricingFragment newInstance(String catId,String catName) {
        CheepCareRateCardPricingFragment fragment = new CheepCareRateCardPricingFragment();
        Bundle args = new Bundle();
        args.putString(Utility.Extra.DATA, catId);
        args.putString(Utility.Extra.DATA_2, catName);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_rate_card_pricing, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        Log.i(TAG, "onAttach: ");
        super.onAttach(context);
       /* if (context instanceof DrawerLayoutInteractionListener) {
            this.mListener = (DrawerLayoutInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initiateUI();
        setListener();
        getCategoryRateCardFromServer();
    }

    @Override
    public void initiateUI() {
        cardID = getArguments().getString(Utility.Extra.DATA);
        name = getArguments().getString(Utility.Extra.DATA_2);
        if (((AppCompatActivity) mContext).getSupportActionBar() != null) {
            //Setting up toolbar
            ((AppCompatActivity) mContext).setSupportActionBar(mBinding.toolbar);
            ((AppCompatActivity) mContext).getSupportActionBar().setTitle(name);

        }
        mBinding.textTitle.setText(name);

   /* RateRecyclerViewAdapter adapter = new RateRecyclerViewAdapter(temp);
        mBinding.rateCardPricingRecycler.setLayoutManager(new LinearLayoutManager(mContext));
        mBinding.rateCardPricingRecycler.setNestedScrollingEnabled(false);
        mBinding.rateCardPricingRecycler.setAdapter(adapter);*/
        //Provide callback to activity to link drawerlayout with toolbar
        // mListener.setUpDrawerLayoutWithToolBar(mBinding.toolbar);
    }

    @Override
    public void setListener() {
        mBinding.relationBack.setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.relation_back:
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.content, CheepCareRateCardFragment.newInstance(), CheepCareRateCardFragment.TAG).commitAllowingStateLoss();
                default:
                    //RateCardDialog.newInstance((AppCompatActivity) mContext);
                    break;


            }
        }
    };

    private void setAdapter(){
        RateRecyclerViewAdapter adapter = new RateRecyclerViewAdapter(list);
        mBinding.rateCardPricingRecycler.setLayoutManager(new LinearLayoutManager(mContext));
        mBinding.rateCardPricingRecycler.setNestedScrollingEnabled(false);
        mBinding.rateCardPricingRecycler.setAdapter(adapter);
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// Category AllCats [END]/////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    private void getCategoryRateCardFromServer() {

        if (!Utility.isConnected(getContext())) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mBinding.getRoot());
            return;
        }

        showProgressDialog();

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        if (PreferenceUtility.getInstance(mContext).getUserDetails() != null)
            mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().userID);


        // body
        Map<String, Object> mParams = new HashMap<>();
        mParams.put(NetworkUtility.TAGS.CAT_ID, cardID);
        //noinspection unchecked
        VolleyNetworkRequest mVolleyNetworkRequestForCategoryList = new VolleyNetworkRequest(NetworkUtility.WS.CATEGORY_RATE_CARD
                , mCallCategoryListWSErrorListener
                , mCallCategoryListWSResponseListener
                , mHeaderParams
                , mParams
                , null);
        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForCategoryList, NetworkUtility.WS.CATEGORY_RATE_CARD);
    }

    Response.Listener mCallCategoryListWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {
            Log.d(TAG, "onResponse() called with: response = [" + response + "]");

            String strResponse = (String) response;
            hideProgressDialog();
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
                        list = GsonUtility.getObjectListFromJsonString(jsonObject.optString(NetworkUtility.TAGS.DATA), RateCardModel[].class);
                        setAdapter();
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
                        if (getActivity() != null)
                            getActivity().finish();
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                mCallCategoryListWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }
        }
    };


    Response.ErrorListener mCallCategoryListWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");

            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());

            hideProgressDialog();
        }
    };


}
