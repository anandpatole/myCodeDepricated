package com.cheep.dialogs;


import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;

import com.cheep.cheepcarenew.model.PackageDetail;
import com.cheep.databinding.DialogPriceSummaryBinding;
import com.cheep.model.PriceModel;
import com.cheep.network.NetworkUtility;
import com.cheep.utils.GsonUtility;
import com.cheep.utils.Utility;

import java.text.DecimalFormat;

public class PriceSummaryDialog extends DialogFragment {
    public static final String TAG = "PriceSummaryDialog";

    public DialogPriceSummaryBinding mBinding;
    private PriceModel model;
    private String months;
    double discount;
    double monthlyPrice = 0;
    int howManyMonth = 0;
    double taxtAmount = 0;
    double totalPackageAmount = 0;
    double totalPackageGSTAmount = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public static PriceSummaryDialog newInstance(PriceModel model, String months, double discount) {
        PriceSummaryDialog f = new PriceSummaryDialog();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString(Utility.Extra.DATA_2, months);
        args.putString(Utility.Extra.DATA, GsonUtility.getJsonStringFromObject(model));
        args.putDouble(Utility.Extra.DATA_3, discount);
        f.setArguments(args);
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

        mBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_price_summary, container, false);
        model = (PriceModel) GsonUtility.getObjectFromJsonString(getArguments().getString(Utility.Extra.DATA), PriceModel.class);
        months = getArguments().getString(Utility.Extra.DATA_2);
        discount = getArguments().getDouble(Utility.Extra.DATA_3);
        setAllData();
        mBinding.cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });
        return mBinding.getRoot();
}

        private void setAllData () {
            switch (months) {
                case Utility.NUMBER.THREE:
                    monthlyPrice = Double.parseDouble(model.monthCostFor3);
                    howManyMonth = Integer.parseInt(months);
                    taxtAmount = Double.parseDouble(model.gstFor3);
                    break;
                case Utility.NUMBER.SIX:
                    monthlyPrice = Double.parseDouble(model.monthCostFor6);
                    howManyMonth = Integer.parseInt(months);
                    taxtAmount = Double.parseDouble(model.gstFor6);
                    break;
                case Utility.NUMBER.TWELVE:
                    monthlyPrice = Double.parseDouble(model.monthCostFor12);
                    howManyMonth = Integer.parseInt(months);
                    taxtAmount = Double.parseDouble(model.gstFor12);
                    break;

            }

            totalPackageAmount = monthlyPrice * howManyMonth;
            totalPackageGSTAmount = totalPackageAmount + taxtAmount;
//            if (discount == 0) {
//                mBinding.promocodell.setVisibility(View.GONE);
//            } else {
//                mBinding.promocodell.setVisibility(View.VISIBLE);
//                totalPackageGSTAmount = totalPackageGSTAmount - discount;
//            }
            DecimalFormat formatter = new DecimalFormat("#,###");
            mBinding.subscriptionUnitPrice.setText(getString(R.string.rupee_symbol_x, formatter.format(Double.valueOf(monthlyPrice))));
            mBinding.subscriptionTotalAmount.setText(getString(R.string.rupee_symbol_x, formatter.format(Double.valueOf(totalPackageAmount))));
            mBinding.subscriptionGst.setText(getString(R.string.rupee_symbol_x, formatter.format(Double.valueOf(taxtAmount))));
         //   mBinding.promoCodeDiscount.setText(getString(R.string.rupee_symbol_x, formatter.format(Double.valueOf(discount))));
            mBinding.subscriptionGrandTotalAmount.setText(getString(R.string.rupee_symbol_x, formatter.format(Double.valueOf(totalPackageGSTAmount))));
        }



    }

