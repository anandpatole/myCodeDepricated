package com.cheep.dialogs;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.cheep.R;

import com.cheep.databinding.DialogFragmentAcknowledgementWithProfilePicBinding;
import com.cheep.model.UserDetails;
import com.cheep.network.NetworkUtility;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.Utility;

public class AcknowledgementDialogWithProfilePic extends DialogFragment {
    public static final String TAG = "AcknowledgementDialogWithoutProfilePic";
    private String mTitle = Utility.EMPTY_STRING;
    private String mMessage = Utility.EMPTY_STRING;
    private int imgResourceIdOfHeader = -1;
    private DialogFragmentAcknowledgementWithProfilePicBinding mDialogFragmentAcknowledgementBinding;
    private AcknowledgementInteractionListener mListener;
    private Context mContext;

    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    public static AcknowledgementDialogWithProfilePic newInstance(Context mContext, int imgResourceIdOfHeader, String title, String message, AcknowledgementInteractionListener listener) {
        AcknowledgementDialogWithProfilePic f = new AcknowledgementDialogWithProfilePic();
        f.setListener(listener);
        f.setContext(mContext);
        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString(NetworkUtility.TAGS.TITLE, title);
        args.putString(NetworkUtility.TAGS.MESSAGE, message);
        args.putInt(NetworkUtility.TAGS.RESOURCE_ID, imgResourceIdOfHeader);
        f.setArguments(args);
        return f;
    }

    public void setContext(Context mContext) {
        this.mContext = mContext;
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
            getDialog().getWindow().setGravity(Gravity.CENTER);
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        mDialogFragmentAcknowledgementBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_fragment_acknowledgement_with_profile_pic, container, false);

        final UserDetails userDetails = PreferenceUtility.getInstance(mContext).getUserDetails();
        if (userDetails != null && userDetails.ProfileImg != null && (!TextUtils.isEmpty(userDetails.ProfileImg)))
            Utility.showCircularImageView(mContext, TAG, mDialogFragmentAcknowledgementBinding.imgProfilePic, userDetails.ProfileImg, R.drawable.ic_cheep_circular_icon, false, 0);


        //Set Header Image
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

    @Override
    public void onResume() {
        super.onResume();
//        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        getDialog().getWindow().setGravity(Gravity.CENTER);
    }
}