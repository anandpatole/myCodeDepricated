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
import com.cheep.databinding.DialogPayByCashBinding;
import com.cheep.network.NetworkUtility;
import com.cheep.utils.Utility;

/**
 * Created by meet on 21/9/17.
 */

public class PayByCashDialog extends DialogFragment {
    public static final String TAG = "PayByCashDialog";
    private DialogPayByCashBinding mDialogPayByCashBinding;
    private String mPROName = Utility.EMPTY_STRING;
    private String mQuoteAmount = Utility.EMPTY_STRING;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    public static PayByCashDialog newInstance(String proName, String quoteAmount) {
        PayByCashDialog f = new PayByCashDialog();
        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString(NetworkUtility.TAGS.SP_USER_NAME, proName);
        args.putString(NetworkUtility.TAGS.QUOTE_AMOUNT, quoteAmount);
        f.setArguments(args);
        return f;
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

        mPROName = getArguments().getString(NetworkUtility.TAGS.SP_USER_NAME);
        mQuoteAmount = getArguments().getString(NetworkUtility.TAGS.QUOTE_AMOUNT);

        mDialogPayByCashBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_pay_by_cash, container, false);

        String text = getString(R.string.desc_pay_by_cash, mQuoteAmount, mPROName);
        StringBuilder description = new StringBuilder(text);
        // appending two space for two smiley at the end of description
        description.append(" ");
        Spannable span = new SpannableString(description);
        Drawable img2 = ContextCompat.getDrawable(getActivity(), R.drawable.ic_smiley_folded_hands);
        img2.setBounds(0, 0, img2.getIntrinsicWidth(), img2.getIntrinsicHeight());
        ImageSpan image2 = new ImageSpan(img2, ImageSpan.ALIGN_BASELINE);
        span.setSpan(image2, span.length() - 1, span.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

        mDialogPayByCashBinding.textDescription.setText(span);

        mDialogPayByCashBinding.textDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Dissmiss the dialog.
                dismiss();
            }
        });
        return mDialogPayByCashBinding.getRoot();
    }

}