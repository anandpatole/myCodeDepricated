package com.cheep.cheepcarenew.fragments;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.cheepcarenew.activities.AddressActivity;
import com.cheep.cheepcarenew.adapters.AddressListRecyclerViewAdapter;
import com.cheep.databinding.ActivityAddressListBinding;
import com.cheep.fragment.BaseFragment;
import com.cheep.model.AddressModel;
import com.cheep.model.UserDetails;
import com.cheep.utils.PreferenceUtility;

import java.util.ArrayList;

public class AddressListFragment extends BaseFragment {

    ActivityAddressListBinding mBinding;
    public static final String TAG = "AddressListFragment";
    AddressListRecyclerViewAdapter adapter;
    private ArrayList<AddressModel> list;

    public static AddressListFragment newInstance() {

        Bundle args = new Bundle();

        AddressListFragment fragment = new AddressListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.activity_address_list, null, false);
        return mBinding.getRoot();
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initiateUI();
    }

    @Override
    public void initiateUI() {
        setListeners();
        mBinding.rvAddress.setNestedScrollingEnabled(false);
        mBinding.rvAddress.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
        adapter = new AddressListRecyclerViewAdapter(userDetails != null ? userDetails.addressList : getlist());
        mBinding.rvAddress.setAdapter(adapter);
    }

    @Override
    public void setListener() {

    }

    protected void setListeners() {
        mBinding.imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AddressActivity) mContext).onBackPressed();

            }
        });
        mBinding.rlBottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AddressActivity) mContext).loadFragment(AddressOptionForHomeOfficeFragment.TAG, AddressOptionForHomeOfficeFragment.newInstance());
            }
        });
    }


    public ArrayList<AddressModel> getlist() {
        list = new ArrayList<>();
        AddressModel addressModel = new AddressModel();
        list.add(addressModel);
        list.add(addressModel);
        list.add(addressModel);
        list.add(addressModel);
        list.add(addressModel);
        list.add(addressModel);
        list.add(addressModel);
        list.add(addressModel);
        list.add(addressModel);
        list.add(addressModel);
        list.add(addressModel);
        list.add(addressModel);
        list.add(addressModel);
        list.add(addressModel);
        list.add(addressModel);
        list.add(addressModel);
        list.add(addressModel);
        list.add(addressModel);
        list.add(addressModel);
        list.add(addressModel);
        list.add(addressModel);
        list.add(addressModel);
        return list;
    }
}
