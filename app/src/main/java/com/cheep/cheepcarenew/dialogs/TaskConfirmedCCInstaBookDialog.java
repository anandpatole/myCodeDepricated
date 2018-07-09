package com.cheep.cheepcarenew.dialogs;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
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
    private boolean isInstaBookingTask;
    private String dateTime;
    private String taskId;

    public interface TaskConfirmActionListener {

        void onAcknowledgementAccepted();

        void rescheduleTask(String taskId);
    }

    /*
    Empty Constructor
     */
    public TaskConfirmedCCInstaBookDialog() {

    }

    public static TaskConfirmedCCInstaBookDialog newInstance(TaskConfirmActionListener listener, boolean isInstaBookingTask, String dateTime,String taskId) {
        TaskConfirmedCCInstaBookDialog f = new TaskConfirmedCCInstaBookDialog();
        f.setListener(listener);
        Bundle args = new Bundle();
        args.putBoolean(Utility.Extra.IS_INSTA_BOOKING_TASK, isInstaBookingTask);
        args.putString(Utility.Extra.DATA, dateTime);
        args.putString(Utility.Extra.DATA_2, taskId);
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
        isInstaBookingTask = getArguments().getBoolean(Utility.Extra.IS_INSTA_BOOKING_TASK, true);
        dateTime = getArguments().getString(Utility.Extra.DATA);
        taskId = getArguments().getString(Utility.Extra.DATA_2);

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

//        if (isInstaBookingTask) {
//            String text1 = getString(R.string.msg_task_confirmed_instabook_1) + Utility.ONE_CHARACTER_SPACE;
        String text2 = getString(R.string.msg_task_confirmed_instabook_2, dateTime) + Utility.ONE_CHARACTER_SPACE + Utility.ONE_CHARACTER_SPACE;
        SpannableStringBuilder spanDesc1 = new SpannableStringBuilder(text2);
        Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.emoji_folded_hands);
        drawable.setBounds(0, 0, 30, 30);
        ImageSpan span1 = new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE);
//            spanDesc1.setSpan(span1, text1.length() - 1
//                    , text1.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        spanDesc1.setSpan(span1, text2.length() - 1
                , text2.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        mBinding.textTaskCreationAcknowledgment.setText(spanDesc1);
        mBinding.title.setText(R.string.label_your_pro_is_booked);
//        } else {
//
//            final String message;
//            if (!TextUtils.isEmpty(dateTime)) {
//                message = getString(R.string.msg_task_confirmed_cheep_care, dateTime, "3");
//            } else {
//                message = getString(R.string.msg_task_confirmed_cheep_care_no_time_specified);
//            }
//            mBinding.textTaskCreationAcknowledgment.setText(message);
//        }
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
                mListener.rescheduleTask(taskId);

                // Dissmiss the dialog.
//                dismiss();
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