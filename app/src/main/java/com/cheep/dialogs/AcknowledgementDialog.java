package com.cheep.dialogs;

import android.content.Intent;
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
import com.cheep.databinding.DialogFragmentAcknowledgementBinding;
import com.cheep.network.NetworkUtility;
import com.cheep.utils.Utility;

public class AcknowledgementDialog extends DialogFragment {
    public static final String TAG = "AcknowledgementDialog";
    private String mTitle = Utility.EMPTY_STRING;
    private String mMessage = Utility.EMPTY_STRING;
    private int imgResourceIdOfHeader = -1;
    private DialogFragmentAcknowledgementBinding mDialogFragmentAcknowledgementBinding;
    private AcknowledgementInteractionListener mListener;

    /*
    Empty Constructor
     */
    public AcknowledgementDialog() {

    }

    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    public static AcknowledgementDialog newInstance(int imgResourceIdOfHeader, String title, String message, AcknowledgementInteractionListener listener) {
        AcknowledgementDialog f = new AcknowledgementDialog();
        f.setListener(listener);
        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString(NetworkUtility.TAGS.TITLE, title);
        args.putString(NetworkUtility.TAGS.MESSAGE, message);
        args.putInt(NetworkUtility.TAGS.RESOURCE_ID, imgResourceIdOfHeader);
        f.setArguments(args);

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

        imgResourceIdOfHeader = getArguments().getInt(NetworkUtility.TAGS.RESOURCE_ID);
        mMessage = getArguments().getString(NetworkUtility.TAGS.MESSAGE);
        mTitle = getArguments().getString(NetworkUtility.TAGS.TITLE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Set Window Background as Transparent.
        if (getDialog() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        mDialogFragmentAcknowledgementBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_fragment_acknowledgement, container, false);

        //Set Image
        mDialogFragmentAcknowledgementBinding.imgHeader.setImageResource(imgResourceIdOfHeader);

        // Set title
        mDialogFragmentAcknowledgementBinding.title.setText(mTitle);

        //Set Message
        mDialogFragmentAcknowledgementBinding.textTaskCreationAcknowledgment.setText(mMessage);

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

    public interface AcknowledgementInteractionListener {
        void onAcknowledgementAccepted();
    }
}