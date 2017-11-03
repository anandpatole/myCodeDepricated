package com.cheep.dialogs;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.cheep.R;
import com.cheep.databinding.DialogFragmentAcknowledgementWithProfilePicBinding;
import com.cheep.network.NetworkUtility;
import com.cheep.utils.Utility;

public class AcknowledgementDialogWithProfilePic extends DialogFragment {
    public static final String TAG = AcknowledgementDialogWithProfilePic.class.getSimpleName();
    private String mTitle = Utility.EMPTY_STRING;
    private String mMessage = Utility.EMPTY_STRING;
    private int imgResourceIdOfHeader = -1;
    private DialogFragmentAcknowledgementWithProfilePicBinding mDialogFragmentAcknowledgementBinding;
    private AcknowledgementInteractionListener mListener;
    private Context mContext;
    private String pictureURL;

    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    public static AcknowledgementDialogWithProfilePic newInstance(Context mContext, int imgResourceIdOfHeader, String title, String message, String pictureurl, AcknowledgementInteractionListener listener) {
        AcknowledgementDialogWithProfilePic f = new AcknowledgementDialogWithProfilePic();
        Log.d(TAG, "newInstance() called with: mContext = [" + mContext + "], imgResourceIdOfHeader = [" + imgResourceIdOfHeader + "], title = [" + title + "], message = [" + message + "], pictureurl = [" + pictureurl + "], listener = [" + listener + "]");
        f.setListener(listener);
        f.setContext(mContext);
        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString(NetworkUtility.TAGS.TITLE, title);
        args.putString(NetworkUtility.TAGS.MESSAGE, message);
        args.putString(NetworkUtility.TAGS.PICTURE_URL, pictureurl);
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
        pictureURL = getArguments().getString(NetworkUtility.TAGS.PICTURE_URL);
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

        if (pictureURL != null && (!TextUtils.isEmpty(pictureURL))) {
            Utility.showCircularImageView(mContext, TAG, mDialogFragmentAcknowledgementBinding.imgProfilePic, pictureURL, R.drawable.ic_cheep_circular_icon, false, 0);
        } else {
            mDialogFragmentAcknowledgementBinding.imgProfilePic.setImageResource(Utility.DEFAULT_CHEEP_LOGO);
        }

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
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        getDialog().getWindow().setGravity(Gravity.CENTER);
    }
}