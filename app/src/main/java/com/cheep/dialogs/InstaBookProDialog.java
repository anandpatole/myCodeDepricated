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
import com.cheep.databinding.DialogInstabookProInfoBinding;
import com.cheep.model.InstaBookingProDetail;
import com.cheep.utils.GlideUtility;
import com.cheep.utils.Utility;

public class InstaBookProDialog extends DialogFragment {
    public static final String TAG = InstaBookProDialog.class.getSimpleName();

    private DialogInstabookProInfoBinding mDialog;
    private AcknowledgementInteractionListener mListener;
    private Context mContext;
    InstaBookingProDetail merchantDetail;
    String date = "";
    //    unicode for thumbs up U+1F44D
    int unicode = 0x1F44D;

    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    public static InstaBookProDialog newInstance(Context mContext, InstaBookingProDetail detail, String date, AcknowledgementInteractionListener listener) {
        InstaBookProDialog f = new InstaBookProDialog();
        f.setListener(listener);
        f.setContext(mContext);
        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putParcelable(Utility.MERCHANT, detail);
        args.putString(Utility.Extra.DATE, date);
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

        merchantDetail = getArguments().getParcelable(Utility.MERCHANT);
        date = getArguments().getString(Utility.Extra.DATE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Set Window Background as Transparent.
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().setGravity(Gravity.CENTER);
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        mDialog = DataBindingUtil.inflate(inflater, R.layout.dialog_instabook_pro_info, container, false);

        if (!TextUtils.isEmpty(merchantDetail.profileImg)) {
            GlideUtility.showCircularImageView(mContext, TAG, mDialog.imgProfilePic, merchantDetail.profileImg, R.drawable.ic_cheep_circular_icon, false, 0);
        } else {
            mDialog.imgProfilePic.setImageResource(Utility.DEFAULT_CHEEP_LOGO);
        }

        mDialog.tvTitle.setText(getString(R.string.all_done) + " " + new String(Character.toChars(unicode)));
        //Set Header Image

        // Set title
        mDialog.title.setText(merchantDetail.userName);

        if (TextUtils.isEmpty(merchantDetail.avgRatings) || Float.parseFloat(merchantDetail.avgRatings) == 0) {
            mDialog.tvSubtitle.setText(TextUtils.concat(getString(R.string.total_experience, merchantDetail.experience)));
            mDialog.tvSubtitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        } else {
            mDialog.tvSubtitle.setText(TextUtils.concat(getString(R.string.total_experience, merchantDetail.experience), " | ", merchantDetail.avgRatings));
        }
        if (!TextUtils.isEmpty(merchantDetail.verified) && merchantDetail.verified.equalsIgnoreCase(Utility.BOOLEAN.YES)) {
            mDialog.tvVerified.setVisibility(View.VISIBLE);
        } else {
            mDialog.tvVerified.setVisibility(View.GONE);
        }

        mDialog.ivBadge.setImageResource(Utility.getProLevelBadge(merchantDetail.proLevel));

        mDialog.tvBody.setText(getString(R.string.merchant_detail_body, merchantDetail.userName, date));
        mDialog.tvBook.setText(getString(R.string.label_pay_X, Utility.getQuotePriceFormatter(merchantDetail.rateGST)));


        // Click event of Okay button
        mDialog.tvBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Callback to activity
                mListener.onAcknowledgementAccepted();

                // Dissmiss the dialog.
                dismiss();
            }
        });
        return mDialog.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            getDialog().getWindow().setGravity(Gravity.CENTER);
        }
    }
}