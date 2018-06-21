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
import com.cheep.databinding.DialogOutOfOfficeHoursBinding;
import com.cheep.network.NetworkUtility;
import com.cheep.utils.Utility;

public class OutOfOfficeHoursDialog extends DialogFragment {
    public static final String TAG = "OutOfOfficeHoursDialog";
    private DialogOutOfOfficeHoursBinding mBinding;
    private String out_of_office_hours_msg = Utility.EMPTY_STRING;

    public interface OutOfOfficeHoursListener {
        void onOutofOfficePayNow();
        // void onCanWait();
    }

    OutOfOfficeHoursListener outOfOfficeHoursListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public static OutOfOfficeHoursDialog newInstance(String msg, OutOfOfficeHoursListener outOfOfficeHoursListener) {
        OutOfOfficeHoursDialog f = new OutOfOfficeHoursDialog();
        f.outOfOfficeHoursListener = outOfOfficeHoursListener;
        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString(NetworkUtility.TAGS.OUT_OF_OFFICE_HOURS_MSG, msg);
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

        out_of_office_hours_msg = getArguments().getString(NetworkUtility.TAGS.OUT_OF_OFFICE_HOURS_MSG);


        mBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_out_of_office_hours, container, false);

        String text = getString(R.string.label_out_of_office_hours_info, out_of_office_hours_msg);


        mBinding.textOutOfOfficeHoursInfo.setText(text);

        mBinding.textOutOfOfficeHoursPayNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                outOfOfficeHoursListener.onOutofOfficePayNow();
                dismiss();
            }
        });

        mBinding.textOutOfOfficeHoursCanWait.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //outOfOfficeHoursListener.onCanWait();
                dismiss();
            }
        });
        return mBinding.getRoot();
    }
}
