package com.cheep.cheepcarenew.fragments;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.cheepcarenew.activities.AddressActivity;
import com.cheep.cheepcarenew.adapters.AddressOptionsRecyclerViewAdapter;
import com.cheep.custom_view.GridSpacingItemDecoration;
import com.cheep.custom_view.tooltips.ToolTipView;
import com.cheep.databinding.ActivityAddressOptionForHomeOfficeBinding;
import com.cheep.fragment.BaseFragment;
import com.cheep.model.AddressModel;

import java.util.ArrayList;

public class AddressOptionForHomeOfficeFragment extends BaseFragment {

    ActivityAddressOptionForHomeOfficeBinding mBinding;
    private ToolTipView toolTipView;
    public static final String TAG = "AddressCategorySelectionFragment";
    private AddressOptionsRecyclerViewAdapter adapter;
    private ArrayList<AddressModel> list;

    public static AddressOptionForHomeOfficeFragment newInstance() {
        Bundle args = new Bundle();
        AddressOptionForHomeOfficeFragment fragment = new AddressOptionForHomeOfficeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.activity_address_option_for_home_office, null, false);
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
        mBinding.rvAddress.setLayoutManager(new GridLayoutManager(mContext, 2));
        adapter = new AddressOptionsRecyclerViewAdapter(getlist());
        mBinding.rvAddress.setAdapter(adapter);
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.scale_20dp);
        mBinding.rvAddress.addItemDecoration(new GridSpacingItemDecoration(2, spacingInPixels, true, 0));
    }

    @Override
    public void setListener() {

    }

    protected void setListeners() {
        mBinding.imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (toolTipView != null)
                    toolTipView.remove();
                ((AddressActivity) mContext).onBackPressed();

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
