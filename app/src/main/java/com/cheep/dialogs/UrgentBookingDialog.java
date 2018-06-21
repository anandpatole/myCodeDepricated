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

import com.cheep.databinding.DialogUrgentBookingBinding;
import com.cheep.network.NetworkUtility;
import com.cheep.utils.Utility;

public class UrgentBookingDialog extends DialogFragment {
    public static final String TAG = "UrgentBookingDialog";
    private DialogUrgentBookingBinding mBinding;
    private String urgent_booking_msg= Utility.EMPTY_STRING;
    public interface UrgentBookingListener {
        void onUrgentPayNow();
        void onUrgentCanWait();
    }

    UrgentBookingListener urgentBookingListener;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    public static UrgentBookingDialog newInstance(String msg, UrgentBookingListener urgentBookingListener) {
        UrgentBookingDialog f = new UrgentBookingDialog();
        f.urgentBookingListener = urgentBookingListener;
        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString(NetworkUtility.TAGS.URGENT_BOOKING_MSG,msg);
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

        urgent_booking_msg = getArguments().getString(NetworkUtility.TAGS.URGENT_BOOKING_MSG);


        mBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_urgent_booking, container, false);

        String text = getString(R.string.label_urgent_booking_info, urgent_booking_msg);


        mBinding.textUrgentBookingInfo.setText(text);

        mBinding.textUrgentBookingPayNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                urgentBookingListener.onUrgentPayNow();
                dismiss();
            }
        });

        mBinding.textUrgentBookingCanWait.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                urgentBookingListener.onUrgentCanWait();;
                dismiss();
            }
        });
        return mBinding.getRoot();
    }
}
