package com.cheep.cheepcare.dialogs;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.databinding.DialogLimitExceedBinding;
import com.cheep.dialogs.AcknowledgementInteractionListener;
import com.cheep.utils.LogUtils;
import com.cheep.utils.Utility;

public class LimitExceededDialog extends DialogFragment {
    public static final String TAG = LimitExceededDialog.class.getSimpleName();
    private DialogLimitExceedBinding mDialogFragmentAcknowledgementBinding;
    private AcknowledgementInteractionListener mListener;
    private String mCarePackageName;

    /*
    Empty Constructor
     */
    public LimitExceededDialog() {

    }

    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    public static LimitExceededDialog newInstance(String carePackageName, AcknowledgementInteractionListener listener) {
        LimitExceededDialog f = new LimitExceededDialog();
        f.setListener(listener);
        Bundle args = new Bundle();
        args.putString(Utility.Extra.DATA, carePackageName);
        f.setArguments(args);
        // Supply num input as an argument.
        return f;
    }

    /**
     * Set Lister that would provide callback to called activity/fragment
     *
     * @param listener
     */
    public void setListener(AcknowledgementInteractionListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCarePackageName = getArguments().getString(Utility.Extra.DATA);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Set Window Background as Transparent.
        if (getDialog() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        mDialogFragmentAcknowledgementBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_limit_exceed, container, false);

        mDialogFragmentAcknowledgementBinding.tvMessage.setText(getString(R.string.label_limit_exceed_desc, mCarePackageName));

        // Click event of Okay button
        mDialogFragmentAcknowledgementBinding.textOkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Callback to activity
                mListener.onAcknowledgementAccepted();

                // Dissmiss the dialog.
                dismiss();
            }
        });
        return mDialogFragmentAcknowledgementBinding.getRoot();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //No call for super(). Bug on API Level > 11.
    }


    @Override
    public void show(FragmentManager manager, String tag) {
        LogUtils.LOGE(TAG, "show: ");

        try {
            FragmentTransaction ft = manager.beginTransaction();
            ft.add(this, tag);
            ft.commitAllowingStateLoss();
        } catch (IllegalStateException e) {
            Log.d(TAG, "Exception", e);
        }
        LogUtils.LOGE(TAG, "show: ------------------ ");

    }


}