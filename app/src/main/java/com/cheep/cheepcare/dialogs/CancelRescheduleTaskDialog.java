package com.cheep.cheepcare.dialogs;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.databinding.DialogUserAvailableBinding;
import com.cheep.dialogs.AcknowledgementInteractionListener;
import com.cheep.utils.LogUtils;

public class CancelRescheduleTaskDialog extends DialogFragment {
    public static final String TAG = CancelRescheduleTaskDialog.class.getSimpleName();
    private DialogUserAvailableBinding mBinding;
    private AcknowledgementInteractionListener mListener;

    /*
    Empty Constructor
     */
    public CancelRescheduleTaskDialog() {

    }

    public interface DialogInteractionListener {
        void cancelTask();

        void rescheduleTaskClicked();
    }


    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    public static CancelRescheduleTaskDialog newInstance(Context context, AcknowledgementInteractionListener listener) {
        CancelRescheduleTaskDialog f = new CancelRescheduleTaskDialog();
        f.setListener(listener);
        f.setCancelable(true);
        f.show(((AppCompatActivity) context).getSupportFragmentManager(), CancelRescheduleTaskDialog.TAG);
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

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Set Window Background as Transparent.
        if (getDialog() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        mBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_user_available, container, false);

        // Click event of Okay button
        mBinding.textOkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Callback to activity
                mListener.onAcknowledgementAccepted();

                // Dissmiss the dialog.
                dismiss();
            }
        });
        return mBinding.getRoot();
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