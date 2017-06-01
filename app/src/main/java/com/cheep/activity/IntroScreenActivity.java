package com.cheep.activity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import com.cheep.R;
import com.cheep.databinding.ActivityIntroScreenBinding;
import com.cheep.fragment.IntroImageFragment;

import java.util.ArrayList;

public class IntroScreenActivity extends BaseAppCompatActivity
{
    ActivityIntroScreenBinding activityIntroScreenBinding;
    public static final String TAG = IntroScreenActivity.class.getSimpleName();
    private IntroViewPagerAdapter introViewPagerAdapter;

    public static void newInstance(Context context) {
        Intent intent = new Intent(context, IntroScreenActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityIntroScreenBinding= DataBindingUtil.setContentView(this, R.layout.activity_intro_screen);
        initiateUI();
        setListeners();
    }

    @Override
    protected void initiateUI()
    {
        setupCoverViewPager(getIntroScreenList());
    }

    @Override
    protected void setListeners()
    {

    }

    /**
     * get list of intro screens
     * @return
     */
    private ArrayList<Integer> getIntroScreenList()
    {
        ArrayList<Integer> introScreens=new ArrayList<>();
        introScreens.add(R.drawable.img_intro_1);
        introScreens.add(R.drawable.img_intro_2);
        introScreens.add(R.drawable.img_intro_3);
        introScreens.add(R.drawable.img_intro_4);
        introScreens.add(R.drawable.img_intro_5);
        return introScreens;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////Intro Image Logic[Start]/////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void setupCoverViewPager(ArrayList<Integer> mIntroImageList)
    {
        Log.d(TAG, "setupCoverViewPager() called with: mIntroImageList = [" + mIntroImageList.size() + "]");
        introViewPagerAdapter = new IntroViewPagerAdapter(getSupportFragmentManager(), mIntroImageList);
        activityIntroScreenBinding.vpIntro.setAdapter(introViewPagerAdapter);

        //For Setting up view pager Indicator
        activityIntroScreenBinding.indicator.setViewPager(activityIntroScreenBinding.vpIntro);
        introViewPagerAdapter.registerDataSetObserver(activityIntroScreenBinding.indicator.getDataSetObserver());
    }

    public static class IntroViewPagerAdapter extends FragmentStatePagerAdapter {
        private ArrayList<Integer> introImageArrayList;

        IntroViewPagerAdapter(FragmentManager fragmentManager, ArrayList<Integer> introImageArrayList)
        {
            super(fragmentManager);
            Log.d(TAG, "IntroViewPagerAdapter() called with: fragmentManager = [" + fragmentManager + "], modelArrayList = [" + introImageArrayList + "]");
            this.introImageArrayList = introImageArrayList;
        }

        @Override
        public Fragment getItem(int position) {
            Log.d(TAG, "getItem() called with: position = [" + position + "]" + " Size: " + introImageArrayList.size());
            return IntroImageFragment.getInstance(introImageArrayList.get(position));
        }

        @Override
        public int getCount() {
            return introImageArrayList.size();
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////Intro Image Logic[End]///////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
}
