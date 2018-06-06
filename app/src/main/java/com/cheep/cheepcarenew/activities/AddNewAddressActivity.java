package com.cheep.cheepcarenew.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.View;

import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.custom_view.tooltips.ToolTipView;
import com.cheep.databinding.ActivityAddNewAddressBinding;

public class AddNewAddressActivity extends BaseAppCompatActivity {

    ActivityAddNewAddressBinding mBinding;
    private ToolTipView toolTipView;
    private static final String TAG = "AddressCategorySelectionActivity";

    public static void newInstance(Context context) {
        context.startActivity(new Intent(context, AddNewAddressActivity.class));
        ((Activity) context).overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_add_new_address);
        initiateUI();
    }

    @Override
    protected void initiateUI() {
        setListeners();
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

        mBinding.tvContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddressListActivity.newInstance(AddNewAddressActivity.this);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

}
