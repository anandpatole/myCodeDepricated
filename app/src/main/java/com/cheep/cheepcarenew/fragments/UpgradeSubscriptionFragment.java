package com.cheep.cheepcarenew.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.databinding.FragmentUpgradeSubscriptionBinding;
import com.cheep.fragment.BaseFragment;
import com.cheep.model.AddressModel;

import java.util.ArrayList;

public class UpgradeSubscriptionFragment extends BaseFragment
{
    public static final String TAG = "UpgradeSubscriptionFragment";
    AddressModel addressModel;
    FragmentUpgradeSubscriptionBinding mBinding;
    public static UpgradeSubscriptionFragment newInstance(AddressModel address) {
        Bundle args = new Bundle();
        args.putSerializable("address", address);
        UpgradeSubscriptionFragment fragment = new UpgradeSubscriptionFragment();
        fragment.setArguments(args);
        return fragment;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_upgrade_subscription, container, false);
        return mBinding.getRoot();
    }
    @Override
    public void onAttach(Context context) {
        Log.i(TAG, "onAttach: ");
        super.onAttach(context);

    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initiateUI();
        setListener();
    }
    @Override
    public void onDetach() {
        Log.i(TAG, "onDetach: ");


        super.onDetach();
    }
    @Override
    public void initiateUI() {

        Bundle bundle = getArguments();
        addressModel= (AddressModel) bundle.getSerializable("address");

    }

    @Override
    public void setListener()
    {

    }
    View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            switch (v.getId())
            {

            }
        }
    };


}
