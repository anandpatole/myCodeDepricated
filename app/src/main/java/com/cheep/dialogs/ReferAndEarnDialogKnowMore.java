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
import com.cheep.databinding.DialogReferEarnKnowMoreBinding;

/**
 * Created by meet on 8/9/17.
 */

public class ReferAndEarnDialogKnowMore extends DialogFragment {
    public static final String TAG = "ReferAndEarnDialogKnowMore";
    private DialogReferEarnKnowMoreBinding mDialogReferEarnKnowMoreBinding;



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

        mDialogReferEarnKnowMoreBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_refer_earn_know_more, container, false);

        mDialogReferEarnKnowMoreBinding.tvOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Dissmiss the dialog.
                dismiss();
            }
        });
        return mDialogReferEarnKnowMoreBinding.getRoot();
    }

}