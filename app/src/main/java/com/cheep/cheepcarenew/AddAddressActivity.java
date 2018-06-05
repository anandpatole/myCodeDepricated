package com.cheep.cheepcarenew;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import com.cheep.R;
import com.cheep.activity.BaseAppCompatActivity;
import com.cheep.cheepcarenew.dialogs.AddNewAddressDialog;
import com.cheep.custom_view.tooltips.ToolTip;
import com.cheep.custom_view.tooltips.ToolTipView;
import com.cheep.databinding.ActivityAddAddressBinding;
import com.cheep.databinding.TooltipAddressSelectionBinding;

public class AddAddressActivity extends BaseAppCompatActivity {

    ActivityAddAddressBinding mBinding;
    private ToolTipView toolTipView;
    private static final String TAG = "AddAddressActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_add_address);
        ;
        initiateUI();
    }

    @Override
    protected void initiateUI() {
        openTooltip();
        setListeners();
    }

    @Override
    protected void setListeners() {
        mBinding.imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (toolTipView != null)
                    toolTipView.remove();
                finish();
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
                if (toolTipView != null && !toolTipView.isShowing()) {
                    toolTipView.show();
                }
            }
        });
    }


    private void openTooltip() {
        TooltipAddressSelectionBinding toolTipBinding = DataBindingUtil.inflate(
                LayoutInflater.from(this),
                R.layout.tooltip_address_selection,
                null,
                false);

        ToolTip toolTip = new ToolTip.Builder()
                .withTextColor(Color.WHITE)
                .withContentView(toolTipBinding.getRoot())
                .withBackgroundColor(ContextCompat.getColor(this, R.color.splash_gradient_end))
                .withCornerRadius(getResources().getDimension(R.dimen.scale_3dp))
                .withPadding(25, 25, 25, 25)
                .build();

        toolTipView = new ToolTipView.Builder(this)
                .withAnchor(mBinding.cvAddress)
                .withToolTip(toolTip)
                .withGravity(Gravity.BOTTOM)
                .build();

        toolTipBinding.tvYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toolTipView.remove();
                Toast.makeText(AddAddressActivity.this, "YES", Toast.LENGTH_SHORT).show();
            }
        });

        toolTipBinding.tvNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toolTipView.remove();
                Toast.makeText(AddAddressActivity.this, "NO", Toast.LENGTH_SHORT).show();
            }
        });


        mBinding.cvAddress.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Log.e(TAG, "onGlobalLayout: *******************");
                toolTipView.show();
            }
        });
    }

    private void openAddNewAddressDialog() {
        AddNewAddressDialog dialog = new AddNewAddressDialog();
        dialog.show(getSupportFragmentManager(), AddNewAddressDialog.TAG);
    }
}
