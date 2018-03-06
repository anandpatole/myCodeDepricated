package com.cheep.cheepcare.dialogs;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.databinding.DialogTaskConfirmedCheepCareInstabookBinding;
import com.cheep.utils.LogUtils;
import com.cheep.utils.Utility;

/**
 * This dialog is used for Cheep care (subscribed) task confirmation and Insta book task confirmtion.
 * Description text of dialog is different for both task
 */
public class TaskConfirmedCCInstaBookDialog extends DialogFragment {
    public static final String TAG = TaskConfirmedCCInstaBookDialog.class.getSimpleName();
    private TaskConfirmActionListener mListener;
    private String description;

    public interface TaskConfirmActionListener {

        void onAcknowledgementAccepted();

        void rescheduleTask();
    }

    /*
    Empty Constructor
     */
    public TaskConfirmedCCInstaBookDialog() {

    }

    public static TaskConfirmedCCInstaBookDialog newInstance(TaskConfirmActionListener listener, String description) {
        TaskConfirmedCCInstaBookDialog f = new TaskConfirmedCCInstaBookDialog();
        f.setListener(listener);
        Bundle args = new Bundle();
        args.putString(Utility.Extra.DATA, description);
        f.setArguments(args);
        return f;
    }

    /**
     * Set Lister that would provide callback to called activity/fragment
     *
     * @param listener interface listener for dialog actions
     */
    public void setListener(TaskConfirmActionListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        description = getArguments().getString(Utility.Extra.DATA);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Set Window Background as Transparent.
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().setCanceledOnTouchOutside(false);
            setCancelable(false);
        }


        DialogTaskConfirmedCheepCareInstabookBinding mBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_task_confirmed_cheep_care_instabook, container, false);

        mBinding.textTaskCreationAcknowledgment.setText(description);
        // Click event of Okay button
        mBinding.tvOkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Callback to activity
                mListener.onAcknowledgementAccepted();

                // Dissmiss the dialog.
                dismiss();
            }
        });

        // Click event of Reschedule button
        mBinding.tvReschedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Callback to activity
                mListener.rescheduleTask();

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