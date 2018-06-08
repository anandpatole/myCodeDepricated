package com.cheep.cheepcarenew.fragments;

import android.content.Context;
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
import com.cheep.model.MessageEvent;
import com.cheep.utils.GsonUtility;
import com.cheep.utils.Utility;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

public class AddressListFragment extends BaseFragment {

    ActivityAddressListBinding mBinding;
    public static final String TAG = "AddressListFragment";
    AddressListRecyclerViewAdapter adapter;
    private ArrayList<AddressModel> list = new ArrayList<>();
    private String category;

    public static AddressListFragment newInstance(String category, String list) {

        Bundle args = new Bundle();

        AddressListFragment fragment = new AddressListFragment();
        args.putString(Utility.Extra.DATA, category);
        args.putString(Utility.Extra.DATA_2, list);
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
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initiateUI();
    }

    @Override
    public void initiateUI() {
        setListeners();
        list.clear();
        if (getArguments() == null)
            return;
        category = getArguments().getString(Utility.Extra.DATA);
        list.clear();
        list.addAll(GsonUtility.<AddressModel>getObjectListFromJsonString(getArguments().getString(Utility.Extra.DATA_2), AddressModel[].class));
        mBinding.rvAddress.setNestedScrollingEnabled(false);
        mBinding.rvAddress.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        adapter = new AddressListRecyclerViewAdapter(list, new AddressListRecyclerViewAdapter.AddressItemClickListener() {
            @Override
            public void onClickItem(AddressModel addressModel) {
                MessageEvent messageEvent = new MessageEvent();
                messageEvent.BROADCAST_ACTION = Utility.BROADCAST_TYPE.ADDRESS_SELECTED_POP_UP;
                messageEvent.addressModel = addressModel;
                EventBus.getDefault().postSticky(messageEvent);
                ((AddressActivity) mContext).onBackPressed();

            }
        });
        mBinding.rvAddress.setAdapter(adapter);

    }

    @Override
    public void setListener() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
                ((AddressActivity) mContext).loadFragment(AddNewAddressFragment.TAG, AddNewAddressFragment.newInstance(category));
            }
        });
    }
}
