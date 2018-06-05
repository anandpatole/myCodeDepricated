package com.cheep.cheepcarenew.dialogs;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import com.cheep.R;
import com.cheep.custom_view.tooltips.ToolTip;
import com.cheep.custom_view.tooltips.ToolTipView;
import com.cheep.databinding.DialogAddressCategorySelectionBinding;
import com.cheep.databinding.TooltipAddressSelectionBinding;

/**
 * Created by meet on 20/9/17.
 */

public class AddressCategorySelectionDialog extends DialogFragment {
    public static final String TAG = "AddressCategorySelectio";
    private DialogAddressCategorySelectionBinding mBinding;
    private ToolTipView toolTipView;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Set Window Background as Transparent.
        if (getDialog() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().setCanceledOnTouchOutside(false);
            //getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            //getDialog().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        mBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_address_category_selection, container, false);
        init();
        return mBinding.getRoot();
    }

    private void init() {
        setListener();
        openTooltip();
    }

    private void openTooltip() {

        TooltipAddressSelectionBinding toolTipBinding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.tooltip_address_selection, null, false);
        ToolTip toolTip = new ToolTip.Builder()
                .withTextColor(Color.WHITE)
                .withParentView(mBinding.getRoot())
                .withContentView(toolTipBinding.getRoot())
                .withBackgroundColor(ContextCompat.getColor(getContext(), R.color.splash_gradient_end))
                .withCornerRadius(getResources().getDimension(R.dimen.scale_3dp))
                .build();

        toolTipView = new ToolTipView.Builder(getContext())
                .withAnchor(mBinding.cvAddress)
                .withToolTip(toolTip)
                .withGravity(Gravity.BOTTOM)
                .build();

        toolTipBinding.tvYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toolTipView.remove();
                Toast.makeText(getContext(), "YES", Toast.LENGTH_SHORT).show();
            }
        });

        toolTipBinding.tvNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toolTipView.remove();
                Toast.makeText(getContext(), "NO", Toast.LENGTH_SHORT).show();
            }
        });

        mBinding.cvAddress.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Log.e(TAG, "onGlobalLayout: *******************");
                toolTipView.showAtLocation();
            }
        });
    }

    private void setListener() {
        mBinding.imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (toolTipView != null)
                    toolTipView.remove();
                dismiss();
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
    }

    private void openAddNewAddressDialog() {
        AddNewAddressDialog dialog = new AddNewAddressDialog();
        dialog.show(getChildFragmentManager(), AddNewAddressDialog.TAG);
    }


}