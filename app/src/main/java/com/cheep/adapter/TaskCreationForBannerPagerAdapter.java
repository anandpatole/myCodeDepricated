package com.cheep.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.cheep.fragment.SelectBannerServicesFragment;
import com.cheep.fragment.ServiceDetailsFragment;
import com.cheep.fragment.ServiceQuestionsFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pankaj on 9/30/16.
 */

public class TaskCreationForBannerPagerAdapter extends FragmentPagerAdapter {
    private final List<String> mTitleList = new ArrayList<>();
    public SelectBannerServicesFragment mSelectSubCategoryFragment;
    public ServiceQuestionsFragment mServiceQuestionsFragment;
    public ServiceDetailsFragment mServiceDetailsFragment;

    public TaskCreationForBannerPagerAdapter(FragmentManager fm) {
        super(fm);
        mSelectSubCategoryFragment = SelectBannerServicesFragment.newInstance();
        mServiceQuestionsFragment = ServiceQuestionsFragment.newInstance();
        mServiceDetailsFragment = ServiceDetailsFragment.newInstance();
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return mSelectSubCategoryFragment;
            case 1:
                return mServiceQuestionsFragment;
            case 2:
                return mServiceDetailsFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mTitleList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitleList.get(position);
    }

    public void addFragment(String title) {
        mTitleList.add(title);
    }

}
