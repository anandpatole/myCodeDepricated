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
import com.cheep.databinding.DialogPaymentFailedBinding;
import com.cheep.dialogs.AcknowledgementInteractionListener;
import com.cheep.utils.LogUtils;

public class PaymentFailedDialog extends DialogFragment {
    public static final String TAG = PaymentFailedDialog.class.getSimpleName();
    private DialogPaymentFailedBinding mDialogFragmentAcknowledgementBinding;
    private AcknowledgementInteractionListener mListener;

    /*
    Empty Constructor
     */
    public PaymentFailedDialog() {

    }

    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    public static PaymentFailedDialog newInstance(AcknowledgementInteractionListener listener) {
        PaymentFailedDialog f = new PaymentFailedDialog();
        f.setListener(listener);
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

        mDialogFragmentAcknowledgementBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_payment_failed, container, false);


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