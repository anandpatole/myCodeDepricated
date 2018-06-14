package com.cheep.cheepcare.fragment;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.cheepcarenew.fragments.ProfileDetailsFragmentnew;
import com.cheep.databinding.FragmentProfileTabBinding;
import com.cheep.fragment.BaseFragment;
import com.cheep.interfaces.DrawerLayoutInteractionListener;
import com.cheep.utils.Utility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kruti on 19/3/18.
 */

public class ProfileTabFragment extends BaseFragment {

    public static final String TAG = "ProfileTabFragment";
    private static final int DETAILS_TAB = 0;
    private static final int REVIEWS_TAB = 1;
   // private static final int PAYMENT_HISTORY_TAB = 2;
    private FragmentProfileTabBinding mBinding;
    private PagerAdapter mPagerAdapter;
    private DrawerLayoutInteractionListener mListener;

    public static ProfileTabFragment newInstance() {
        Bundle args = new Bundle();
        ProfileTabFragment fragment = new ProfileTabFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile_tab, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initiateUI();
        setListener();
    }

    @Override
    public void initiateUI() {

        if (((AppCompatActivity) mContext).getSupportActionBar() != null) {
            //Setting up toolbar
            ((AppCompatActivity) mContext).setSupportActionBar(mBinding.toolbar);
            ((AppCompatActivity) mContext).getSupportActionBar().setTitle(Utility.EMPTY_STRING);
        }

        //Provide callback to activity to link drawerlayout with toolbar
        mListener.setUpDrawerLayoutWithToolBar(mBinding.toolbar);

        setUpViewPager();
    }

    @Override
    public void onAttach(Context context) {
        Log.i(TAG, "onAttach: ");
        super.onAttach(context);
        if (context instanceof DrawerLayoutInteractionListener) {
            this.mListener = (DrawerLayoutInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    private void setUpViewPager() {
        mPagerAdapter = new PagerAdapter(getChildFragmentManager());
        mPagerAdapter.addFragment(getString(R.string.label_details));
        mPagerAdapter.addFragment(getString(R.string.label_reviews));
       // mPagerAdapter.addFragment(getString(R.string.label_payment_history));
        mBinding.viewPager.setAdapter(mPagerAdapter);

        mBinding.flDetailsContainer.setSelected(true);
        mBinding.flReviewsContainer.setSelected(false);
      //  mBinding.flPaymentHistoryContainer.setSelected(false);
    }

    @Override
    public void setListener() {
        mBinding.flDetailsContainer.setOnClickListener(mOnClickListener);
        mBinding.flReviewsContainer.setOnClickListener(mOnClickListener);
      //  mBinding.flPaymentHistoryContainer.setOnClickListener(mOnClickListener);

        mBinding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == DETAILS_TAB) {
                    mBinding.flDetailsContainer.setSelected(true);
                    mBinding.flReviewsContainer.setSelected(false);
                   // mBinding.flPaymentHistoryContainer.setSelected(false);
                } else if (position == REVIEWS_TAB) {
                    mBinding.flDetailsContainer.setSelected(false);
                    mBinding.flReviewsContainer.setSelected(true);
                  //  mBinding.flPaymentHistoryContainer.setSelected(false);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private static class PagerAdapter extends FragmentPagerAdapter {

        private static final int COUNT = 2;
        private final List<String> mTitleList = new ArrayList<>();
        private final ProfileDetailsFragmentnew mProfileDetailsFragment;
        private final ProfileReviewsFragment mProfileReviewsFragment;
       // private final ProfilePaymentHistoryFragment mProfilePaymentHistoryFragment;

        public PagerAdapter(FragmentManager fm) {
            super(fm);
            mProfileDetailsFragment = ProfileDetailsFragmentnew.newInstance();
            mProfileReviewsFragment = ProfileReviewsFragment.newInstance();
            //mProfilePaymentHistoryFragment = ProfilePaymentHistoryFragment.newInstance();
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case DETAILS_TAB:
                    return mProfileDetailsFragment;
                case REVIEWS_TAB:
                    return mProfileReviewsFragment;
//                case PAYMENT_HISTORY_TAB:
//                    return mProfilePaymentHistoryFragment;
                default:
                    return mProfileDetailsFragment;
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

    private final View.OnClickListener mOnClickListener =
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (v.getId()) {
                        case R.id.fl_details_container:
                            mBinding.flDetailsContainer.setSelected(true);
                            mBinding.flReviewsContainer.setSelected(false);
                        //    mBinding.flPaymentHistoryContainer.setSelected(false);
                            mBinding.viewPager.setCurrentItem(0);
                            break;
                        case R.id.fl_reviews_container:
                            mBinding.flDetailsContainer.setSelected(false);
                            mBinding.flReviewsContainer.setSelected(true);
                           // mBinding.flPaymentHistoryContainer.setSelected(false);
                            mBinding.viewPager.setCurrentItem(1);
                            break;
                       /* case R.id.fl_payment_history_container:
                            mBinding.flDetailsContainer.setSelected(false);
                            mBinding.flReviewsContainer.setSelected(false);
                            mBinding.flPaymentHistoryContainer.setSelected(true);
                            mBinding.viewPager.setCurrentItem(2);*/
                            //break;
                    }
                }
            };
}
