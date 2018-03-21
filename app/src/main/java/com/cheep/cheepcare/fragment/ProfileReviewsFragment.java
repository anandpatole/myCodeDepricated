package com.cheep.cheepcare.fragment;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.databinding.FragmentProfileReviewsBinding;
import com.cheep.fragment.BaseFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kruti on 19/3/18.
 */

public class ProfileReviewsFragment extends BaseFragment {

    private static final int REVIEWS_OF_ME_TAB = 0;
    private static final int REVIEWS_BY_ME_TAB = 1;
    private FragmentProfileReviewsBinding mBinding;
    private PagerAdapter mPagerAdapter;

    public static ProfileReviewsFragment newInstance() {
        Bundle args = new Bundle();
        ProfileReviewsFragment fragment = new ProfileReviewsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile_reviews, container, false);
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
        setUpViewPager();
    }

    private void setUpViewPager() {
        mPagerAdapter = new PagerAdapter(getChildFragmentManager());
        mPagerAdapter.addFragment(getString(R.string.label_details));
        mPagerAdapter.addFragment(getString(R.string.label_reviews));
        mBinding.viewPager.setAdapter(mPagerAdapter);

        mBinding.flReviewsOfMeContainer.setSelected(true);
        mBinding.flReviewsByMeContainer.setSelected(false);
    }

    @Override
    public void setListener() {
        mBinding.flReviewsOfMeContainer.setOnClickListener(mOnClickListener);
        mBinding.flReviewsByMeContainer.setOnClickListener(mOnClickListener);

        mBinding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == REVIEWS_OF_ME_TAB) {
                    mBinding.flReviewsOfMeContainer.setSelected(true);
                    mBinding.flReviewsByMeContainer.setSelected(false);
                } else if (position == REVIEWS_BY_ME_TAB) {
                    mBinding.flReviewsOfMeContainer.setSelected(false);
                    mBinding.flReviewsByMeContainer.setSelected(true);
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
        private final ReviewsOfMeFragment mReviewsOfMeFragment;
        private final ReviewsByMeFragment mReviewsByMeFragment;

        public PagerAdapter(FragmentManager fm) {
            super(fm);
            mReviewsOfMeFragment = ReviewsOfMeFragment.newInstance();
            mReviewsByMeFragment = ReviewsByMeFragment.newInstance();
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case REVIEWS_OF_ME_TAB:
                    return mReviewsOfMeFragment;
                case REVIEWS_BY_ME_TAB:
                    return mReviewsByMeFragment;
                default:
                    return mReviewsOfMeFragment;
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
                        case R.id.fl_reviews_of_me_container:
                            mBinding.flReviewsOfMeContainer.setSelected(true);
                            mBinding.flReviewsByMeContainer.setSelected(false);
                            mBinding.viewPager.setCurrentItem(0);
                            break;
                        case R.id.fl_reviews_by_me_container:
                            mBinding.flReviewsOfMeContainer.setSelected(false);
                            mBinding.flReviewsByMeContainer.setSelected(true);
                            mBinding.viewPager.setCurrentItem(1);
                            break;
                    }
                }
            };
}
