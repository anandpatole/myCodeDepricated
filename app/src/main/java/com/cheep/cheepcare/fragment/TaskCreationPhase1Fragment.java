package com.cheep.cheepcare.fragment;


import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.VolleyError;
import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.cheepcare.activity.TaskCreationCCActivity;
import com.cheep.databinding.FragmentTaskCreationPhase1Binding;
import com.cheep.fragment.BaseFragment;
import com.cheep.model.SubServiceDetailModel;
import com.cheep.network.NetworkUtility;
import com.cheep.network.Volley;
import com.cheep.utils.Utility;
import com.cheep.utils.WebCallClass;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class TaskCreationPhase1Fragment extends BaseFragment {

    private static final String TAG = TaskCreationPhase1Fragment.class.getSimpleName();
    private static final int FREE_WITH_CHEEP_CARE = 0;
    private static final int PAID_CHEEP_SERVICES = 1;
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
        if (!isVisibleToUser || mTaskCreationCCActivity == null) {
            return;
        }

        if (!(((FreeSubCategoryFragment) mPagerAdapter.getItem(0)).getSelectedSubServices().isEmpty()) ||
                !(((PaidSubCategoryFragment) mPagerAdapter.getItem(1)).getSelectedSubServices().isEmpty())) {
            mTaskCreationCCActivity.setTaskState(TaskCreationCCActivity.STEP_ONE_VERIFIED);
        } else {
            mTaskCreationCCActivity.setTaskState(TaskCreationCCActivity.STEP_ONE_NORMAL);
        }

        // Hide the post task button
        mTaskCreationCCActivity.showPostTaskButton(true, true);
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

        mBinding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == FREE_WITH_CHEEP_CARE) {
                    mBinding.flFreeCcContainer.setSelected(true);
                    mBinding.flPaidServicesContainer.setSelected(false);
                } else if (position == PAID_CHEEP_SERVICES) {
                    mBinding.flPaidServicesContainer.setSelected(true);
                    mBinding.flFreeCcContainer.setSelected(false);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
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

    public ArrayList<SubServiceDetailModel> getSelectedSubServices() {
        ArrayList<SubServiceDetailModel> list = getSelectedFreeServices();
        list.addAll(getSelectedPaidServices());
        return list;
    }

    public ArrayList<SubServiceDetailModel> getSelectedFreeServices() {
        return ((FreeSubCategoryFragment) mPagerAdapter.getItem(0)).getSelectedSubServices();
    }

    public ArrayList<SubServiceDetailModel> getSelectedPaidServices() {
        return ((PaidSubCategoryFragment) mPagerAdapter.getItem(1)).getSelectedSubServices();
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

        WebCallClass.fetchListOfSubCategory(mContext, mTaskCreationCCActivity.mJobCategoryModel
                , mTaskCreationCCActivity.mPackageType, mTaskCreationCCActivity.mAddressModel
                , mCommonResponseListener, mFetchListOfSubCategoryResponseListener);
    }

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

    private final WebCallClass.CommonResponseListener mCommonResponseListener =
            new WebCallClass.CommonResponseListener() {
                @Override
                public void volleyError(VolleyError error) {
                    Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");
                    showLoading(false);
                    showErrorMessage(mContext.getString(R.string.label_something_went_wrong));
//            Utility.showSnackBar(getString(R.string.label_something_went_wrong), mBinding.getRoot());
                }

                @Override
                public void showSpecificMessage(String message) {
                    showLoading(false);
                    showErrorMessage(message);
                    // Show message
//                        Utility.showSnackBar(error_message, mBinding.getRoot());
                }

                @Override
                public void forceLogout() {
                    showLoading(false);
                }
            };

    private final WebCallClass.FetchListOfSubCategoryResponseListener mFetchListOfSubCategoryResponseListener =
            new WebCallClass.FetchListOfSubCategoryResponseListener() {
                @Override
                public void fetchListOfSubCategorySuccessResponse(ArrayList<SubServiceDetailModel> freeCatList
                        , ArrayList<SubServiceDetailModel> paidCatList) {
                    showLoading(false);

                    ((FreeSubCategoryFragment) mPagerAdapter.getItem(0)).setSubCatList(freeCatList);
                    ((PaidSubCategoryFragment) mPagerAdapter.getItem(1)).setSubCatList(paidCatList);
                }
            };
}