package com.cheep.cheepcare.fragment;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.databinding.FragmentReviewsByMeBinding;
import com.cheep.fragment.BaseFragment;

/**
 * Created by kruti on 19/3/18.
 */

public class ReviewsByMeFragment extends BaseFragment {

    private FragmentReviewsByMeBinding mBinding;

    public static ReviewsByMeFragment newInstance() {
        Bundle args = new Bundle();
        ReviewsByMeFragment fragment = new ReviewsByMeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_reviews_by_me, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void initiateUI() {

    }

    @Override
    public void setListener() {

    }
}
