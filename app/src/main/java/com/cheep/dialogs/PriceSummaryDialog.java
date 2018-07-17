package com.cheep.dialogs;


import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;

import com.cheep.databinding.DialogPriceSummaryBinding;
public class PriceSummaryDialog extends DialogFragment {
    public static final String TAG = "PriceSummaryDialog";

    public DialogPriceSummaryBinding mBinding;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    public static PriceSummaryDialog newInstance () {
        PriceSummaryDialog f = new PriceSummaryDialog();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        return f;
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Set Window Background as Transparent.
        if (getDialog() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            //getDialog().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }



     mBinding=   DataBindingUtil.inflate(inflater, R.layout.dialog_price_summary, container, false);

mBinding.cancel.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        getDialog().dismiss();
    }
});
        return mBinding.getRoot();
    }
}
