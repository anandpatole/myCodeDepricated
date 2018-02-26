package com.cheep.cheepcare.fragment;


import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.cheepcare.activity.TaskCreationCCActivity;
import com.cheep.databinding.FragmentTaskCreationPhase1Binding;
import com.cheep.fragment.BaseFragment;
import com.cheep.model.SubServiceDetailModel;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.network.VolleyNetworkRequest;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.cheep.network.NetworkUtility.TAGS.ADDRESS_ID;
import static com.cheep.network.NetworkUtility.TAGS.CAT_ID;
import static com.cheep.network.NetworkUtility.TAGS.FREE_SERVICE;
import static com.cheep.network.NetworkUtility.TAGS.PACKAGE_TYPE;
import static com.cheep.network.NetworkUtility.TAGS.PAID_SERVICE;

/**
 * A simple {@link Fragment} subclass.
 */
public class TaskCreationPhase1Fragment extends BaseFragment {

    private static final String TAG = TaskCreationPhase1Fragment.class.getSimpleName();
    private FragmentTaskCreationPhase1Binding mBinding;
    private PagerAdapter mPagerAdapter;
    private TaskCreationCCActivity mTaskCreationCCActivity;

    public static TaskCreationPhase1Fragment newInstance() {
        return new TaskCreationPhase1Fragment();
    }

    public TaskCreationPhase1Fragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_task_creation_phase1, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        BaseAppCompatActivity activity = (BaseAppCompatActivity) context;
        if (activity instanceof TaskCreationCCActivity) {
            mTaskCreationCCActivity = (TaskCreationCCActivity) activity;
        }
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
      /*  if (!isVisibleToUser || mTaskCreationCCActivity == null) {
            return;
        }

        if (!(((FreeSubCategoryFragment) mPagerAdapter.getItem(1)).getSelectedSubServices().isEmpty()) ||
                !(((PaidSubCategoryFragment) mPagerAdapter.getItem(1)).getSelectedSubServices().isEmpty())) {
            ma.setTaskState(TaskCreationCCActivity.STEP_ONE_VERIFIED);
        } else {
            mTaskCreationCCActivity.setTaskState(TaskCreationCCActivity.STEP_ONE_NORMAL);
        }

        // Hide the post task button
        mTaskCreationCCActivity.showPostTaskButton(true, true);*/
    }

    @Override
    public void initiateUI() {
        setUpViewPager();
        initCheepTipsUI();
        fetchListOfSubCategory();
    }

    private void setUpViewPager() {
        mPagerAdapter = new PagerAdapter(getChildFragmentManager());
        mPagerAdapter.addFragment(getString(R.string.label_free_with_cc));
        mPagerAdapter.addFragment(getString(R.string.label_paid_cheep_services));
        mBinding.viewPager.setAdapter(mPagerAdapter);

        mBinding.flFreeCcContainer.setSelected(true);
        mBinding.flPaidServicesContainer.setSelected(false);
    }

    private void initCheepTipsUI() {
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
    public void setListener() {
        mBinding.flFreeCcContainer.setOnClickListener(mOnClickListener);
        mBinding.flPaidServicesContainer.setOnClickListener(mOnClickListener);
    }

    private final View.OnClickListener mOnClickListener =
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (v.getId()) {
                        case R.id.fl_free_cc_container:
                            mBinding.flFreeCcContainer.setSelected(true);
                            mBinding.flPaidServicesContainer.setSelected(false);
                            mBinding.viewPager.setCurrentItem(0);
                            break;
                        case R.id.fl_paid_services_container:
                            mBinding.flPaidServicesContainer.setSelected(true);
                            mBinding.flFreeCcContainer.setSelected(false);
                            mBinding.viewPager.setCurrentItem(1);
                            break;
                    }
                }
            };

    public List<SubServiceDetailModel> getSelectedSubServices() {
        List<SubServiceDetailModel> list =
                ((FreeSubCategoryFragment) mPagerAdapter.getItem(0)).getSelectedSubServices();
        list.addAll(((PaidSubCategoryFragment) mPagerAdapter.getItem(1)).getSelectedSubServices());
        return list;
    }

    private static class PagerAdapter extends FragmentPagerAdapter {

        private static final int COUNT = 2;
        private static final int FREE_SERVICES = 0;
        private static final int PAID_SERVICES = 1;
        private final List<String> mTitleList = new ArrayList<>();
        private final FreeSubCategoryFragment mFreeSubCategoryFragment;
        private final PaidSubCategoryFragment mPaidSubCategoryFragment;

        public PagerAdapter(FragmentManager fm) {
            super(fm);
            mFreeSubCategoryFragment = FreeSubCategoryFragment.newInstance();
            mPaidSubCategoryFragment = PaidSubCategoryFragment.newInstance();
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case FREE_SERVICES:
                    return mFreeSubCategoryFragment;
                case PAID_SERVICES:
                    return mPaidSubCategoryFragment;
                default:
                    return mFreeSubCategoryFragment;
            }
        }

        @Override
        public int getCount() {
            return COUNT;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return mTitleList.get(position);
        }

        public void addFragment(String title) {
            mTitleList.add(title);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////// Fetch SubService Listing[START] ////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void fetchListOfSubCategory() {
        if (!Utility.isConnected(mContext)) {
            Utility.showSnackBar(Utility.NO_INTERNET_CONNECTION, mBinding.getRoot());
            return;
        }


        //Add Header parameters
        showLoading(true);
        Map<String, String> mHeaderParams = new HashMap<>();
        mHeaderParams.put(NetworkUtility.TAGS.X_API_KEY, PreferenceUtility.getInstance(mContext).getXAPIKey());
        if (PreferenceUtility.getInstance(mContext).getUserDetails() != null) {
            mHeaderParams.put(NetworkUtility.TAGS.USER_ID, PreferenceUtility.getInstance(mContext).getUserDetails().userID);
        }

        //Add Params
        Map<String, String> mParams = new HashMap<>();
        mParams.put(CAT_ID, mTaskCreationCCActivity.mJobCategoryModel.catId);
        mParams.put(PACKAGE_TYPE, mTaskCreationCCActivity.mPackageType);
        mParams.put(ADDRESS_ID, mTaskCreationCCActivity.mAddressModel.address_id);

        VolleyNetworkRequest mVolleyNetworkRequestForCategoryList = new VolleyNetworkRequest(NetworkUtility.WS.GET_CARE_FREE_PAID_SERVICES_FOR_CATEGORY
                , mCallFetchSubServiceListingWSErrorListener
                , mCallFetchSubServiceListingWSResponseListener
                , mHeaderParams
                , mParams
                , null);

        Volley.getInstance(mContext).addToRequestQueue(mVolleyNetworkRequestForCategoryList, NetworkUtility.WS.GET_CARE_FREE_PAID_SERVICES_FOR_CATEGORY);
    }

    Response.Listener mCallFetchSubServiceListingWSResponseListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {
            Log.d(TAG, "onResponse() called with: response = [" + response + "]");
            showLoading(false);
            String strResponse = (String) response;
            try {
                JSONObject jsonObject = new JSONObject(strResponse);
                Log.i(TAG, "onResponse: " + jsonObject.toString());
                int statusCode = jsonObject.getInt(NetworkUtility.TAGS.STATUS_CODE);
                String error_message;
                switch (statusCode) {
                    case NetworkUtility.TAGS.STATUSCODETYPE.SUCCESS:

                        JSONObject object = jsonObject.optJSONObject(NetworkUtility.TAGS.DATA);
                        ArrayList<SubServiceDetailModel> freeCatList = Utility.getObjectListFromJsonString(object.optString(FREE_SERVICE), SubServiceDetailModel[].class);
                        ArrayList<SubServiceDetailModel> paidCatList = Utility.getObjectListFromJsonString(object.optString(PAID_SERVICE), SubServiceDetailModel[].class);

                        ((FreeSubCategoryFragment) mPagerAdapter.getItem(0)).setSubCatList(freeCatList);
                        ((PaidSubCategoryFragment) mPagerAdapter.getItem(1)).setSubCatList(paidCatList);

//                        addList(list, getString(R.string.label_other_sub_service));
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_GENERALIZE_MESSAGE:
                        // Show Toast
                        showErrorMessage(getString(R.string.label_something_went_wrong));
//                        Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
                        break;
                    case NetworkUtility.TAGS.STATUSCODETYPE.DISPLAY_ERROR_MESSAGE:
                        error_message = jsonObject.getString(NetworkUtility.TAGS.MESSAGE);
                        showErrorMessage(error_message);

                        // Show message
//                        Utility.showSnackBar(error_message, mBinding.getRoot());
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
            showLoading(false);
            Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");
            showErrorMessage(getString(R.string.label_something_went_wrong));
//            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
        }
    };

    private void showLoading(boolean isShowing) {
        mBinding.progressLoad.setVisibility(isShowing ? View.VISIBLE : View.GONE);
    }

    private void showErrorMessage(String message) {
        mBinding.textError.setText(message);
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////// Fetch SubService Listing[END] ////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onDetach() {
        super.onDetach();

        Volley.getInstance(mContext).getRequestQueue().cancelAll(NetworkUtility.WS.GET_CARE_FREE_PAID_SERVICES_FOR_CATEGORY);
    }

}
