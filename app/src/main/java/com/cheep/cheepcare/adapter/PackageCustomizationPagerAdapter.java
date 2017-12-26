package com.cheep.cheepcare.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.cheep.cheepcare.fragment.PackageBundlingFragment;
import com.cheep.cheepcare.fragment.SelectPackageSpecificationsFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pankaj on 9/30/16.
 */

public class PackageCustomizationPagerAdapter extends FragmentPagerAdapter {
    private final List<String> mTitleList = new ArrayList<>();
    public SelectPackageSpecificationsFragment mSelectPackageSpecificationsFragment;
    public PackageBundlingFragment mPackageBundlingFragment;

    public PackageCustomizationPagerAdapter(FragmentManager fm) {
        super(fm);
        mSelectPackageSpecificationsFragment = SelectPackageSpecificationsFragment.newInstance();
        mPackageBundlingFragment = PackageBundlingFragment.newInstance();
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return mSelectPackageSpecificationsFragment;
            case 1:
                return mPackageBundlingFragment;
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
