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

import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.databinding.FragmentTaskCreationPhase1Binding;
import com.cheep.fragment.BaseFragment;
import com.cheep.model.SubServiceDetailModel;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class TaskCreationPhase1Fragment extends BaseFragment {

    private static final String TAG = TaskCreationPhase1Fragment.class.getSimpleName();
    private FragmentTaskCreationPhase1Binding mBinding;
    private PagerAdapter mPagerAdapter;
    /*private TaskCreationCCActivity mTaskCreationCCActivity;*/

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
        /*if (activity instanceof TaskCreationCCActivity) {
            mTaskCreationCCActivity = (TaskCreationCCActivity) activity;
        }*/
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
}
