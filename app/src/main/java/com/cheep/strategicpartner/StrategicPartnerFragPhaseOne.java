package com.cheep.strategicpartner;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
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
import com.cheep.activity.TaskCreationActivity;
import com.cheep.databinding.FragmentStrategicPartnerPhaseOneBinding;
import com.cheep.fragment.BaseFragment;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.ErrorLoadingHelper;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.cheep.network.NetworkUtility.TAGS.CAT_ID;

/**
 * Created by Giteeka on 20/7/17.
 * This Fragment is first step of Strategic partner screen.
 * Expandable list view of services
 * Single or multiple sub services selection according to specific partners features
 */

public class StrategicPartnerFragPhaseOne extends BaseFragment {
    public static final String TAG = "StrategicPartnerFragPhaseOne";
    private FragmentStrategicPartnerPhaseOneBinding mFragmentStrategicPartnerPhaseOneBinding;
    private ErrorLoadingHelper errorLoadingHelper;
    private StrategicPartnerTaskCreationAct mStrategicPartnerTaskCreationAct;
    private boolean isVerified = false;
    private ArrayList<StrategicPartnerServiceModel> list;


    @SuppressWarnings("unused")
    public static StrategicPartnerFragPhaseOne newInstance() {
        return new StrategicPartnerFragPhaseOne();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mFragmentStrategicPartnerPhaseOneBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_strategic_partner_phase_one, container, false);
        return mFragmentStrategicPartnerPhaseOneBinding.getRoot();
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
        if (!isVisibleToUser || mStrategicPartnerTaskCreationAct == null) {
            return;
        }
        // set step number background in top banner
        if (isVerified) {
            mStrategicPartnerTaskCreationAct.setTaskState(TaskCreationActivity.STEP_ONE_VERIFIED);
        } else {
            mStrategicPartnerTaskCreationAct.setTaskState(TaskCreationActivity.STEP_ONE_NORMAL);
        }

    }


    @Override
    public void initiateUI() {
        Log.d(TAG, "initiateUI() called");

        //Setting recycler view
        errorLoadingHelper = new ErrorLoadingHelper(mFragmentStrategicPartnerPhaseOneBinding.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        mFragmentStrategicPartnerPhaseOneBinding.recyclerView.setLayoutManager(linearLayoutManager);
        errorLoadingHelper.showLoading();
        // web call
        fetchListOfSubCategory(mStrategicPartnerTaskCreationAct.mBannerImageModel.cat_id);

        // handle bottom button click
        mFragmentStrategicPartnerPhaseOneBinding.textContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (list != null && !list.isEmpty()) {
                    ArrayList<StrategicPartnerServiceModel> selectedServiceList = new ArrayList<>();
                    for (StrategicPartnerServiceModel model : list) {
                        // get all sub selected services
                        if (model.isSelected) {
                            StrategicPartnerServiceModel categoryModel = new StrategicPartnerServiceModel();
                            categoryModel.catId = model.catId;
                            categoryModel.name = model.name;
                            categoryModel.sub_cat_id = model.sub_cat_id;
                            ArrayList<StrategicPartnerServiceModel.AllSubSubCat> allSubSubCats = new ArrayList<>();
                            for (StrategicPartnerServiceModel.AllSubSubCat allSubSubCat : model.allSubSubCats) {
                                {
                                    if (allSubSubCat.isSelected) {
                                        allSubSubCats.add(allSubSubCat);
                                    }
                                }
                            }
                            categoryModel.allSubSubCats = allSubSubCats;
                            selectedServiceList.add(categoryModel);
                        }
                    }
                    if (!selectedServiceList.isEmpty()) {
                        mStrategicPartnerTaskCreationAct.setSelectedSubService(selectedServiceList);

                        Log.d(TAG, "onSubCategoryRowItemClicked() called with: subServiceDetailModel = [" + "]");
                        // Make the status Verified
                        isVerified = true;

                        //Alert The activity that step one is been verified.
                        mStrategicPartnerTaskCreationAct.setTaskState(StrategicPartnerTaskCreationAct.STEP_ONE_VERIFIED);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mStrategicPartnerTaskCreationAct.gotoStep(StrategicPartnerTaskCreationAct.STAGE_2);
                            }
                        }, 500);
                    } else {
                        Utility.showSnackBar(getString(R.string.step_1_desc_for_strategic_partner), mFragmentStrategicPartnerPhaseOneBinding.getRoot());
                    }
                }


            }
        });
    }

    @Override
    public void setListener() {
        Log.d(TAG, "setListener() called");
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////// Fetch Strategic partner Service Listing[START] ////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void fetchListOfSubCategory(String catId) {
        Log.d(TAG, "fetchListOfSubCategory() called with: catId = [" + catId + "]");
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(getString(R.string.no_internet), mFragmentStrategicPartnerPhaseOneBinding.getRoot());
            return;
        }

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().UserID);

        //Add Params
        Map<String, String> mParams = new HashMap<>();
        mParams.put(CAT_ID, catId);

        //noinspection unchecked
        VolleyNetworkRequest mVolleyNetworkRequestForCategoryList = new VolleyNetworkRequest(NetworkUtility.WS.FETCH_SUB_CATS_STRATEGIC_PARTNER_LIST
                , mCallFetchAllSubCateSPListingWSErrorListener
                , mCallFetchAllSubCateSPListingWSResponseListener
                , mHeaderParams
                , mParams
                , null);

        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForCategoryList, NetworkUtility.WS.FETCH_SUB_CATS_STRATEGIC_PARTNER_LIST);
    }

    private Response.Listener mCallFetchAllSubCateSPListingWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {
            Log.d(TAG, "onResponse() called with: response = [" + response + "]");
            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:
                        list = Utility.getObjectListFromJsonString(jsonObject.optString(NetworkUtility.TAGS.DATA), StrategicPartnerServiceModel[].class);
                        ExpandableServicesRecycleAdapter expandableRecyclerViewAdapter = new ExpandableServicesRecycleAdapter(list, mStrategicPartnerTaskCreationAct.isSingleSelection);
                        mFragmentStrategicPartnerPhaseOneBinding.recyclerView.setAdapter(expandableRecyclerViewAdapter);
                        errorLoadingHelper.success();
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mFragmentStrategicPartnerPhaseOneBinding.getRoot());
                        errorLoadingHelper.success();
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);

                        // Show message
                        Utility.showSnackBar(error_message, mFragmentStrategicPartnerPhaseOneBinding.getRoot());
                        errorLoadingHelper.success();
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.USER_DELETED:
                    case NetworkUtility.TAGS.STATUSCODETYPE.FORCE_LOGOUT_REQUIRED:
                        //Logout and finish the current activity
                        Utility.logout(mContext, true, statusCode);
                        if (getActivity() != null)
                            getActivity().finish();
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mCallFetchAllSubCateSPListingWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }

        }
    };
    private Response.ErrorListener mCallFetchAllSubCateSPListingWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mFragmentStrategicPartnerPhaseOneBinding.getRoot());
        }
    };
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////// Fetch SubService Listing[END] ////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        BaseAppCompatActivity activity = (BaseAppCompatActivity) context;
        if (activity instanceof StrategicPartnerTaskCreationAct) {
            mStrategicPartnerTaskCreationAct = (StrategicPartnerTaskCreationAct) activity;
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.FETCH_SUB_CATS_STRATEGIC_PARTNER_LIST);
    }


}
