package com.cheep.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.cheep.strategicpartner.StrategicPartnerFragPhaseOne;
import com.cheep.strategicpartner.StrategicPartnerFragPhaseThree;
import com.cheep.strategicpartner.StrategicPartnerFragPhaseTwo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pankaj on 9/30/16.
 */

public class TaskCreationForStrategicPartnerPagerAdapter extends FragmentPagerAdapter {
    private final List<String> mTitleList = new ArrayList<>();
    public StrategicPartnerFragPhaseOne mSelectSubCategoryFragment;
    public StrategicPartnerFragPhaseTwo mStrategicPartnerFragPhaseTwo;
    public StrategicPartnerFragPhaseThree mStrategicPartnerFragPhaseThree;

    public TaskCreationForStrategicPartnerPagerAdapter(FragmentManager fm) {
        super(fm);
        mSelectSubCategoryFragment = StrategicPartnerFragPhaseOne.newInstance();
        mStrategicPartnerFragPhaseTwo = StrategicPartnerFragPhaseTwo.newInstance();
        mStrategicPartnerFragPhaseThree = StrategicPartnerFragPhaseThree.newInstance();
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return mSelectSubCategoryFragment;
            case 1:
                return mStrategicPartnerFragPhaseTwo;
            case 2:
                return mStrategicPartnerFragPhaseThree;
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
