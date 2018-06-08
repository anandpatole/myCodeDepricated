package com.cheep.cheepcarenew.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.custom_view.tooltips.ToolTip;
import com.cheep.custom_view.tooltips.ToolTipView;
import com.cheep.databinding.ActivityAddressCategorySelectionBinding;
import com.cheep.databinding.TooltipAddressSelectionBinding;

public class AddressCategorySelectionActivity extends BaseAppCompatActivity {

    ActivityAddressCategorySelectionBinding mBinding;
    private ToolTipView toolTipView;
    private static final String TAG = "AddressCategorySelectionActivity";

    public static void newInstance(Context context) {
        context.startActivity(new Intent(context, AddressCategorySelectionActivity.class));
        ((Activity) context).overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_address_category_selection);
        initiateUI();
    }

    @Override
    protected void initiateUI() {
        setListeners();
        openTooltip(true);
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
        mBinding.cvOffice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBinding.cvOffice.setSelected(true);
                mBinding.cvHome.setSelected(false);
                openAddNewAddressDialog();

            }
        });
        mBinding.cvHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBinding.cvOffice.setSelected(false);
                mBinding.cvHome.setSelected(true);
                openAddNewAddressDialog();
            }
        });

        mBinding.cvAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTooltip(false);
            }
        });
    }


    private void openTooltip(boolean b) {

        Log.e(TAG, "onGlobalLayout: *******************");
        TooltipAddressSelectionBinding toolTipBinding = DataBindingUtil.inflate(
                LayoutInflater.from(AddressCategorySelectionActivity.this),
                R.layout.tooltip_address_selection,
                null,
                false);

        ToolTip toolTip = new ToolTip.Builder()
                .withTextColor(Color.WHITE)
                .withBackgroundColor(ContextCompat.getColor(AddressCategorySelectionActivity.this, R.color.splash_gradient_end))
                .withCornerRadius(getResources().getDimension(R.dimen.scale_3dp))
                .build();

        toolTipView = new ToolTipView.Builder(AddressCategorySelectionActivity.this)
                .withAnchor(mBinding.cvAddress)
                .withContentView(toolTipBinding.getRoot())
                .withToolTip(toolTip)
                .withGravity(Gravity.BOTTOM)
                .build();

        toolTipBinding.tvYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toolTipView.remove();
                Toast.makeText(AddressCategorySelectionActivity.this, "YES", Toast.LENGTH_SHORT).show();
            }
        });

        toolTipBinding.tvNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toolTipView.remove();
                Toast.makeText(AddressCategorySelectionActivity.this, "NO", Toast.LENGTH_SHORT).show();
            }
        });
        if (b)
            toolTipView.showDelayed(500);
        else toolTipView.show();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void openAddNewAddressDialog() {
        AddNewAddressActivity.newInstance(this);

    }
}
