package com.cheep.cheepcare.dialogs;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.databinding.DialogBusyNoWorriesBinding;
import com.cheep.databinding.DialogNewAddressBinding;
import com.cheep.utils.Utility;

/**
 * Created by bhavesh on 21/9/17.
 */

public class NotSubscribedAddressDialog extends DialogFragment {
    public static final String TAG = NotSubscribedAddressDialog.class.getSimpleName();
    private DialogNewAddressBinding mDialogNewAddressBinding;
    private DialogInteractionListener mListener;

    public interface DialogInteractionListener {
        void onSubscribeClicked();

        void onNotNowClicked();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!(getTargetFragment() instanceof DialogInteractionListener)) {
            throw new RuntimeException("getTargetFragment() " + getTargetFragment() + " must be an instance of DialogInteractionListener!!");
        } else {
            mListener = (DialogInteractionListener) getTargetFragment();
        }
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

        mDialogNewAddressBinding =
                DataBindingUtil.inflate(inflater, R.layout.dialog_new_address, container, false);

        String string = getString(R.string.msg_not_subscribed_address);
        SpannableStringBuilder spannableStringBuilder =
                new SpannableStringBuilder(string);
        ForegroundColorSpan colorSpan =
                new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.splash_gradient_end));
        spannableStringBuilder.setSpan(colorSpan, 0, string.indexOf(Utility.COLON), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mDialogNewAddressBinding.textDescription.setText(spannableStringBuilder);

        mDialogNewAddressBinding.tvSubscribeNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Dissmiss the dialog.
                mListener.onSubscribeClicked();
                dismiss();
            }
        });
        mDialogNewAddressBinding.tvNotNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Dissmiss the dialog.
                mListener.onNotNowClicked();
                dismiss();
            }
        });
        return mDialogNewAddressBinding.getRoot();
    }
}