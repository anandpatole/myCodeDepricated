package com.cheep.dialogs;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.databinding.DialogAddMoneyPaytmBinding;

/**
 * Created by meet on 21/9/17.
 */

public class AddMoneyPaytmModelDialog extends DialogFragment {
    public static final String TAG = AddMoneyPaytmModelDialog.class.getSimpleName();
    private DialogAddMoneyPaytmBinding mDialogAddMoneyPaytmBinding;


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

        mDialogAddMoneyPaytmBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_add_money_paytm, container, false);

        StringBuilder description = new StringBuilder(getString(R.string.label_add_money_description));
        // appending two space for two smiley at the end of description
        description.append("    ");
        Spannable span = new SpannableString(description);
        Drawable img1 = ContextCompat.getDrawable(getActivity(), R.drawable.ic_smiley_bird);
        Drawable img2 = ContextCompat.getDrawable(getActivity(), R.drawable.ic_smiley_folded_hands);
        img1.setBounds(0, 0, img1.getIntrinsicWidth(), img1.getIntrinsicHeight());
        img2.setBounds(0, 0, img2.getIntrinsicWidth(), img2.getIntrinsicHeight());
        ImageSpan image1 = new ImageSpan(img1, ImageSpan.ALIGN_BASELINE);
        ImageSpan image2 = new ImageSpan(img2, ImageSpan.ALIGN_BASELINE);
        span.setSpan(image1, span.length() - 3, span.length() - 2, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        span.setSpan(image2, span.length() - 1, span.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

        mDialogAddMoneyPaytmBinding.textAddMoneyDescription.setText(span);

        mDialogAddMoneyPaytmBinding.textAddFunds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Dissmiss the dialog.
                dismiss();
            }
        });
        return mDialogAddMoneyPaytmBinding.getRoot();
    }

}