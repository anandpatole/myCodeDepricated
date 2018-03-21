package com.cheep.cheepcare.activity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.cheepcare.fragment.AllNotificationsFragment;
import com.cheep.databinding.ActivityNotificationCcBinding;
import com.cheep.utils.Utility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pankaj on 10/5/16.
 */

public class NotificationCcActivity extends BaseAppCompatActivity {

    private static final String TAG = NotificationCcActivity.class.getSimpleName();
    private static final int ALL_TAB = 0;
    private static final int YOUR_TASKS_TAB = 1;
    private ActivityNotificationCcBinding mBinding;
    private PagerAdapter mPagerAdapter;

    public static void newInstance(Context context) {
        Intent intent = new Intent(context, NotificationCcActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_notification_cc);
        initiateUI();
        setListeners();
    }

    @Override
    protected void initiateUI() {

        setSupportActionBar(mBinding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(Utility.EMPTY_STRING);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mBinding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        mBinding.textTitle.setText(getString(R.string.label_notification));

        setUpViewPager();
    }

    private void setUpViewPager() {
        mPagerAdapter = new PagerAdapter(getSupportFragmentManager());
        mPagerAdapter.addFragment(getString(R.string.label_free_with_cc));
        mPagerAdapter.addFragment(getString(R.string.label_paid_cheep_services));
        mBinding.viewPager.setAdapter(mPagerAdapter);

        mBinding.flAllContainer.setSelected(true);
        mBinding.flYourTasksContainer.setSelected(false);
    }

    private static class PagerAdapter extends FragmentPagerAdapter {

        private static final int COUNT = 2;

        private final List<String> mTitleList = new ArrayList<>();
        private final AllNotificationsFragment mAllNotificationsFragment;
        private final AllNotificationsFragment mYourTasksFragment;

        public PagerAdapter(FragmentManager fm) {
            super(fm);
            mAllNotificationsFragment = AllNotificationsFragment.newInstance();
            mYourTasksFragment = AllNotificationsFragment.newInstance();
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case ALL_TAB:
                    return mAllNotificationsFragment;
                case YOUR_TASKS_TAB:
                    return mYourTasksFragment;
                default:
                    return mAllNotificationsFragment;
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

    @Override
    protected void setListeners() {
        mBinding.flAllContainer.setOnClickListener(mOnClickListener);
        mBinding.flYourTasksContainer.setOnClickListener(mOnClickListener);

        mBinding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == ALL_TAB) {
                    mBinding.flAllContainer.setSelected(true);
                    mBinding.flYourTasksContainer.setSelected(false);
                } else if (position == YOUR_TASKS_TAB) {
                    mBinding.flYourTasksContainer.setSelected(true);
                    mBinding.flAllContainer.setSelected(false);
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
                        case R.id.fl_all_container:
                            mBinding.flAllContainer.setSelected(true);
                            mBinding.flYourTasksContainer.setSelected(false);
                            mBinding.viewPager.setCurrentItem(0);
                            break;
                        case R.id.fl_your_tasks_container:
                            mBinding.flYourTasksContainer.setSelected(true);
                            mBinding.flAllContainer.setSelected(false);
                            mBinding.viewPager.setCurrentItem(1);
                            break;
                    }
                }
            };
}