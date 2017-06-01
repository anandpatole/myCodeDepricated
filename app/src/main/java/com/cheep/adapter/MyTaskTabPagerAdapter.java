package com.cheep.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.cheep.fragment.TaskFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pankaj on 9/30/16.
 */

public class MyTaskTabPagerAdapter extends FragmentPagerAdapter {
    private final List<String> mTitleList = new ArrayList<>();

    public MyTaskTabPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return TaskFragment.newInstance(TaskFragment.TAB_PENDING_TASK);
            case 1:
                return TaskFragment.newInstance(TaskFragment.TAB_PAST_TASK);
            default:
                return TaskFragment.newInstance(TaskFragment.TAB_PENDING_TASK);
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
