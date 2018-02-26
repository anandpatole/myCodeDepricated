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
import com.cheep.databinding.DialogUserAvailabilityBinding;

/**
 * Created by bhavesh on 21/9/17.
 */

public class UserAvailabilityDialog extends DialogFragment {
    public static final String TAG = UserAvailabilityDialog.class.getSimpleName();
    private DialogUserAvailabilityBinding mBinding;
    private DialogInteractionListener mListener;

    public interface DialogInteractionListener {
        void someoneElseWillAttendClicked();

        void rescheduleTaskClicked();

        void cancelTaskClicked();

        void userWillBeAvailableClicked();
    }

    public static void newInstance(Context context, @NonNull DialogInteractionListener listener) {
        UserAvailabilityDialog dialog = new UserAvailabilityDialog();
        dialog.setListener(listener);
        dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), UserAvailabilityDialog.TAG);
    }

    private void setListener(DialogInteractionListener listener) {
        mListener = listener;
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

        mBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_user_availability, container, false);

        mBinding.tvSomenoneElseWillAttend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Dissmiss the dialog.
                mListener.someoneElseWillAttendClicked();
                dismiss();
            }
        });
        mBinding.tvRescheduleTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Dissmiss the dialog.
                mListener.rescheduleTaskClicked();
                dismiss();
            }
        });
        mBinding.tvCancelTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Dissmiss the dialog.
                mListener.cancelTaskClicked();
                dismiss();
            }
        });
        mBinding.tvWaitILlBeAvailable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Dissmiss the dialog.
                mListener.userWillBeAvailableClicked();
                dismiss();
            }
        });
        return mBinding.getRoot();
    }
}