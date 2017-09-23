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
import com.cheep.databinding.DialogReferAndEarnInfoBinding;
import com.cheep.databinding.DialogReferAndEarnNoBalanceBinding;

/**
 * Created by meet on 21/9/17.
 */

public class ReferAndEarnDialogNoBalance extends DialogFragment {
    public static final String TAG = "ReferAndEarnDialogNoBalance";
    private DialogReferAndEarnNoBalanceBinding mDialogReferAndEarnNoBalanceBinding;




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
            //getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            //getDialog().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        mDialogReferAndEarnNoBalanceBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_refer_and_earn_no_balance, container, false);

        mDialogReferAndEarnNoBalanceBinding.textOkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Dissmiss the dialog.
                dismiss();
            }
        });
        return mDialogReferAndEarnNoBalanceBinding.getRoot();
    }

}