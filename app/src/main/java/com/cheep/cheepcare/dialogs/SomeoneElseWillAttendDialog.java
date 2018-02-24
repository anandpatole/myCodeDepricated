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
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.databinding.DialogSomeoneElseWillAttendBinding;
import com.cheep.utils.LogUtils;
import com.cheep.utils.Utility;

public class SomeoneElseWillAttendDialog extends DialogFragment {
    public static final String TAG = SomeoneElseWillAttendDialog.class.getSimpleName();
    private DialogSomeoneElseWillAttendBinding mBinding;
    private DialogInteractionListener mListener;

    /*
    Empty Constructor
     */
    public SomeoneElseWillAttendDialog() {

    }

    public interface DialogInteractionListener {
        void okClicked(String personName);

        void onBackPressed();
    }

    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    public static SomeoneElseWillAttendDialog newInstance(Context context, DialogInteractionListener listener) {
        SomeoneElseWillAttendDialog dialog = new SomeoneElseWillAttendDialog();
        dialog.setListener(listener);
        // Supply num input as an argument.
        dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), NotSubscribedAddressDialog.TAG);
        return dialog;
    }

    /**
     * Set Lister that would provide callback to called activity/fragment
     *
     * @param listener
     */
    public void setListener(DialogInteractionListener listener) {
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
        mBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_someone_else_will_attend, container, false);
        
        mBinding.ivBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onBackPressed();
                dismiss();
            }
        });

        // Click event of Okay button
        mBinding.textOkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValid()) {
                    //Callback to activity
                    mListener.okClicked(mBinding.etPersonName.getText().toString().trim());

                    // Dismiss the dialog.
                    dismiss();
                }
            }
        });
        return mBinding.getRoot();
    }

    private boolean isValid() {
        if (TextUtils.isEmpty(mBinding.etPersonName.getText().toString().trim())) {
            Utility.showToast(getContext(), getString(R.string.validate_person_name));
            return false;
        }
        return true;
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