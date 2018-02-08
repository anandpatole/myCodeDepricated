package com.cheep.cheepcare.fragment;


import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
        TaskCreationPhase1Fragment fragment = new TaskCreationPhase1Fragment();
        return fragment;
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

    /*@Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.d(TAG, "setUserVisibleHint() called with: isVisibleToUser = [" + isVisibleToUser + "]");
        if (!isVisibleToUser || mTaskCreationCCActivity == null) {
            return;
        }

        *//*if (!mSubServiceUnitAdapter.getSelectedList().isEmpty()) {
            mTaskCreationCCActivity.setTaskState(TaskCreationCCActivity.STEP_ONE_VERIFIED);
        } else {
            mTaskCreationCCActivity.setTaskState(TaskCreationCCActivity.STEP_ONE_NORMAL);
        }*//*

        // Hide the post task button
        mTaskCreationCCActivity.showPostTaskButton(true, true);
    }*/

    @Override
    public void initiateUI() {
        setUpViewPager();
    }

    private void setUpViewPager() {
        mPagerAdapter = new PagerAdapter(getChildFragmentManager());
        mPagerAdapter.addFragment(getString(R.string.label_free_with_cc));
        mPagerAdapter.addFragment(getString(R.string.label_paid_cheep_services));
        mBinding.viewPager.setAdapter(mPagerAdapter);
        mBinding.tabLayout.setupWithViewPager(mBinding.viewPager);

        TextView textView =
                (TextView) LayoutInflater.from(mContext).inflate(R.layout.text_view, mBinding.tabLayout, false);
        textView.setText(getString(R.string.label_free_with_cc));
        textView.setSelected(true);
        mBinding.tabLayout.getTabAt(0).setCustomView(textView);
        textView =
                (TextView) LayoutInflater.from(mContext).inflate(R.layout.text_view, mBinding.tabLayout, false);
        textView.setText(getString(R.string.label_paid_cheep_services));
        textView.setSelected(false);
        mBinding.tabLayout.getTabAt(1).setCustomView(textView);
//        mBinding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
//            @Override
//            public void onTabSelected(TabLayout.Tab tab) {
//                tab.getCustomView().setSelected(true);
//            }
//
//            @Override
//            public void onTabUnselected(TabLayout.Tab tab) {
//                tab.getCustomView().setSelected(false);
//            }
//
//            @Override
//            public void onTabReselected(TabLayout.Tab tab) {
//
//            }
//        });
//        Utility.wrapTabIndicatorToTitle(mBinding.tabLayout, 0, 0);
    }

    @Override
    public void setListener() {

    }

    public List<SubServiceDetailModel> getSelectedSubServices() {
        List<SubServiceDetailModel> list =
                ((FreeSubCategoryFragment) mPagerAdapter.getItem(0)).getSelectedSubServices();
        list.addAll(((FreeSubCategoryFragment) mPagerAdapter.getItem(1)).getSelectedSubServices());
        return list;
    }

    private static class PagerAdapter extends FragmentPagerAdapter {

        private static final int COUNT = 2;
        private static final int FREE_SERVICES = 0;
        private static final int PAID_SERVICES = 1;
        private final List<String> mTitleList = new ArrayList<>();

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case FREE_SERVICES:
                    return FreeSubCategoryFragment.newInstance();
                case PAID_SERVICES:
                    return FreeSubCategoryFragment.newInstance();
                default:
                    return FreeSubCategoryFragment.newInstance();
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