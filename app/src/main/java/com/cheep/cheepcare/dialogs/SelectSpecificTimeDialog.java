package com.cheep.cheepcare.dialogs;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.databinding.DialogBusyNoWorriesBinding;

/**
 * Created by meet on 21/9/17.
 */

public class SelectSpecificTimeDialog extends DialogFragment {

    public static final String TAG = SelectSpecificTimeDialog.class.getSimpleName();
    private DialogBusyNoWorriesBinding mDialogBusyNoWorriesBinding;
    private DialogInteractionListener mListener;

    public interface DialogInteractionListener {
        void onSelectTimeClicked();

        void onNoThanksClicked();
    }

    public static void newInstance(Context context, @NonNull DialogInteractionListener listener) {
        SelectSpecificTimeDialog dialog = new SelectSpecificTimeDialog();
        dialog.setListener(listener);
        dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), SelectSpecificTimeDialog.TAG);
    }

    private void setListener(DialogInteractionListener listener) {
        mListener = listener;
    }

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

        mDialogBusyNoWorriesBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_busy_no_worries, container, false);

        mDialogBusyNoWorriesBinding.tvSelectTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Dissmiss the dialog.
                mListener.onSelectTimeClicked();
                dismiss();
            }
        });
        mDialogBusyNoWorriesBinding.tvNoThanks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Dissmiss the dialog.
                mListener.onNoThanksClicked();
                dismiss();
            }
        });
        return mDialogBusyNoWorriesBinding.getRoot();
    }
}