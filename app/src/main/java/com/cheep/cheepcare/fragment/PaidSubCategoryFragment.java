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
import com.cheep.cheepcare.activity.TaskCreationCCActivity;
import com.cheep.cheepcare.adapter.PaidServicesAdapter;
import com.cheep.cheepcare.dialogs.LimitExceededDialog;
import com.cheep.databinding.FragmentSelectSubCategoryBinding;
import com.cheep.dialogs.AcknowledgementInteractionListener;
import com.cheep.fragment.BaseFragment;
import com.cheep.model.SubServiceDetailModel;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.ErrorLoadingHelper;
import com.cheep.utils.LogUtils;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.cheep.network.NetworkUtility.TAGS.CAT_ID;

/**
 * Created by bhavesh on 28/4/17.
 */

public class PaidSubCategoryFragment extends BaseFragment {
    public static final String TAG = PaidSubCategoryFragment.class.getSimpleName();
    private FragmentSelectSubCategoryBinding mBinding;
    private PaidServicesAdapter mFreeServicesAdapter;
    ErrorLoadingHelper errorLoadingHelper;
    private TaskCreationCCActivity mTaskCreationCCActivity;
    private boolean isVerified = false;

    @SuppressWarnings("unused")
    public static PaidSubCategoryFragment newInstance() {
        PaidSubCategoryFragment fragment = new PaidSubCategoryFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_select_sub_category, container, false);
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
        if (!isVisibleToUser || mTaskCreationCCActivity == null) {
            return;
        }

        if (!mFreeServicesAdapter.getSelectedList().isEmpty()) {
            mTaskCreationCCActivity.setTaskState(TaskCreationCCActivity.STEP_ONE_VERIFIED);
        } else {
            mTaskCreationCCActivity.setTaskState(TaskCreationCCActivity.STEP_ONE_NORMAL);
        }

        // Hide the post task button
        mTaskCreationCCActivity.showPostTaskButton(true, true);
    }

    @Override
    public void initiateUI() {
        Log.d(TAG, "initiateUI() called");

        //Setting recycler view
        errorLoadingHelper = new ErrorLoadingHelper(mBinding.recyclerView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        mBinding.recyclerView.setLayoutManager(linearLayoutManager);
        mFreeServicesAdapter = new PaidServicesAdapter(mFreeItemInteractionListener);
        mBinding.recyclerView.setAdapter(mFreeServicesAdapter);
        errorLoadingHelper.showLoading();
        fetchListOfSubCategory(mTaskCreationCCActivity.mJobCategoryModel.catId);
    }

    private List<SubServiceDetailModel> getDummyList() {
        ArrayList<SubServiceDetailModel> subServiceDetailModels = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            SubServiceDetailModel subServiceDetailModel = new SubServiceDetailModel();
            subServiceDetailModel.name = "I need my leaking tap to be fixed";
            subServiceDetailModel.catId = i;
            subServiceDetailModel.monthlyPrice = String.valueOf(100);
            subServiceDetailModel.subServiceList = new ArrayList<>();
            subServiceDetailModels.add(subServiceDetailModel);
        }
        return subServiceDetailModels;
    }

    @Override
    public void setListener() {
        Log.d(TAG, "setListener() called");
    }

    /*public interface SubServiceListInteractionListener {
        void onSubCategoryRowItemClicked(SubServiceDetailModel subServiceDetailModel);
    }

    private SubServiceListInteractionListener mSubServiceListInteractionListener = new SubServiceListInteractionListener() {
        @Override
        public void onSubCategoryRowItemClicked(SubServiceDetailModel subServiceDetailModel) {
//            mTaskCreationCCActivity.setSelectedSubService(subServiceDetailModel);

            Log.d(TAG, "onSubCategoryRowItemClicked() called with: subServiceDetailModel = [" + subServiceDetailModel.name + "]");
            // Make the status Verified
            isVerified = true;

            //Alert The activity that step one is been varified.
            mTaskCreationCCActivity.setTaskState(TaskCreationCCActivity.STEP_ONE_VERIFIED);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mTaskCreationCCActivity.gotoStep(TaskCreationCCActivity.STAGE_2);
                }
            }, 500);

        }
    };*/

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        BaseAppCompatActivity activity = (BaseAppCompatActivity) context;
        if (activity instanceof TaskCreationCCActivity) {
            mTaskCreationCCActivity = (TaskCreationCCActivity) activity;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////// Fetch SubService Listing[START] ////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    private void fetchListOfSubCategory(String catId) {
        Log.d(TAG, "fetchListOfSubCategory() called with: catId = [" + catId + "]");
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mBinding.getRoot());
            return;
        }

        //Add Header parameters
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        if (PreferenceUtility.getInstance(mContext).getUserDetails() != null) {
            mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().userID);
        }

        //Add Params
        Map<String, String> mParams = new HashMap<>();
        mParams.put(CAT_ID, catId);

        VolleyNetworkRequest mVolleyNetworkRequestForCategoryList = new VolleyNetworkRequest(NetworkUtility.WS.FETCH_SUB_SERVICE_LIST
                , mCallFetchSubServiceListingWSErrorListener
                , mCallFetchSubServiceListingWSResponseListener
                , mHeaderParams
                , mParams
                , null);

        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForCategoryList, NetworkUtility.WS.FETCH_SUB_SERVICE_LIST);
    }

    Response.Listener mCallFetchSubServiceListingWSResponseListener = new Response.Listener() {
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
                        ArrayList<SubServiceDetailModel> list =
                                Utility.getObjectListFromJsonString(
                                        jsonObject.optString(NetworkUtility.TAGS.DATA)
                                        , SubServiceDetailModel[].class);

                        mFreeServicesAdapter.addAll(list/*, getString(R.string.label_other_sub_service)*/);
                        errorLoadingHelper.success();
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
                        errorLoadingHelper.success();
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);

                        // Show message
                        Utility.showSnackBar(error_message, mBinding.getRoot());
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
                mCallFetchSubServiceListingWSErrorListener.onErrorResponse(new VolleyError(e.getMessage()));
            }

        }
    };
    Response.ErrorListener mCallFetchSubServiceListingWSErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");
            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
        }
    };
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////// Fetch SubService Listing[END] ////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * This method would return the selected list by getting
     * from adapter
     *
     * @return
     */
    public List<SubServiceDetailModel> getSelectedSubServices() {
        return mFreeServicesAdapter.getSelectedList();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.FETCH_SUB_SERVICE_LIST);
    }

    private final PaidServicesAdapter.ItemInteractionListener mFreeItemInteractionListener =
            new PaidServicesAdapter.ItemInteractionListener() {
                @Override
                public void onLimitExceeded(SubServiceDetailModel subServiceDetailModel, int position) {
                    showLimitExceedDialog();
                }
            };

    private void showLimitExceedDialog() {
        LogUtils.LOGE(TAG, "showLimitExceedDialog: ");
        // TODO remove dummy name in dialog params
        LimitExceededDialog.newInstance(mContext, "Appliance Care",
                new AcknowledgementInteractionListener() {

                    @Override
                    public void onAcknowledgementAccepted() {
                    }
                });
    }

}