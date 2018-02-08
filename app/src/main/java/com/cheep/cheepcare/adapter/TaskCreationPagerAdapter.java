package com.cheep.cheepcare.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.cheep.cheepcare.fragment.TaskCreationPhase1Fragment;
import com.cheep.cheepcare.fragment.TaskCreationPhase2Fragment;
import com.cheep.cheepcare.fragment.FreeSubCategoryFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pankaj on 9/30/16.
 */

public class TaskCreationPagerAdapter extends FragmentPagerAdapter {
    private final List<String> mTitleList = new ArrayList<>();

    public final int TASK_CREATION_PHASE_1_FRAGMENT = 0;
    public final int TASK_CREATION_PHASE_2_FRAGMENT = 1;

    public TaskCreationPhase1Fragment mTaskCreationPhase1Fragment;
    public TaskCreationPhase2Fragment mTaskCreationPhase2Fragment;

    public TaskCreationPagerAdapter(FragmentManager fm) {
        super(fm);
        mTaskCreationPhase1Fragment = TaskCreationPhase1Fragment.newInstance();
        mTaskCreationPhase2Fragment = TaskCreationPhase2Fragment.newInstance();
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case TASK_CREATION_PHASE_1_FRAGMENT:
                return mTaskCreationPhase1Fragment;
            case TASK_CREATION_PHASE_2_FRAGMENT:
                return mTaskCreationPhase2Fragment;
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
