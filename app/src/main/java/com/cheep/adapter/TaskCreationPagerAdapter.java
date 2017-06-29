package com.cheep.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.cheep.fragment.EnterTaskDetailFragment;
import com.cheep.fragment.SelectSubCategoryFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pankaj on 9/30/16.
 */

public class TaskCreationPagerAdapter extends FragmentPagerAdapter {
    private final List<String> mTitleList = new ArrayList<>();
    public SelectSubCategoryFragment mSelectSubCategoryFragment;
    public EnterTaskDetailFragment mEnterTaskDetailFragment;

    public TaskCreationPagerAdapter(FragmentManager fm) {
        super(fm);
        mSelectSubCategoryFragment = SelectSubCategoryFragment.newInstance();
        mEnterTaskDetailFragment = EnterTaskDetailFragment.newInstance();
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return mSelectSubCategoryFragment;
            case 1:
                return mEnterTaskDetailFragment;
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
