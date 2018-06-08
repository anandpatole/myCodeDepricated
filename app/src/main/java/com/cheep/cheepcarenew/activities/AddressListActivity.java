package com.cheep.cheepcarenew.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.cheepcarenew.adapters.AddressListRecyclerViewAdapter;
import com.cheep.custom_view.tooltips.ToolTipView;
import com.cheep.databinding.ActivityAddressListBinding;
import com.cheep.model.AddressModel;
import com.cheep.model.UserDetails;
import com.cheep.utils.PreferenceUtility;

import java.util.ArrayList;

public class AddressListActivity extends BaseAppCompatActivity {

    ActivityAddressListBinding mBinding;
    private ToolTipView toolTipView;
    private static final String TAG = "AddressListActivity";
    AddressListRecyclerViewAdapter adapter;
    private ArrayList<AddressModel> list;

    public static void newInstance(Context context) {
        context.startActivity(new Intent(context, AddressListActivity.class));
        ((Activity) context).overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_address_list);
        initiateUI();
    }

    @Override
    protected void initiateUI() {
        setListeners();
        mBinding.rvAddress.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        UserDetails userDetails = PreferenceUtility.getInstance(this).getUserDetails();
        adapter = new AddressListRecyclerViewAdapter(userDetails != null ? userDetails.addressList : getlist());
        mBinding.rvAddress.setAdapter(adapter);
    }

    @Override
    protected void setListeners() {
        mBinding.imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (toolTipView != null)
                    toolTipView.remove();
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public ArrayList<AddressModel> getlist() {
        list = new ArrayList<>();
        AddressModel addressModel = new AddressModel();
        list.add(addressModel);
        list.add(addressModel);
        return list;
    }
}
