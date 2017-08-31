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
import com.cheep.databinding.DialogInstabookMerchantInfoBinding;
import com.cheep.model.InstaBookingMerchantDetail;
import com.cheep.utils.Utility;

public class InstaBookMerchantDialog extends DialogFragment {
    public static final String TAG = "AcknowledgementDialogWi";

    private DialogInstabookMerchantInfoBinding mDialog;
    private AcknowledgementInteractionListener mListener;
    private Context mContext;
    InstaBookingMerchantDetail merchantDetail;
    String date = "";

    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    public static InstaBookMerchantDialog newInstance(Context mContext, InstaBookingMerchantDetail detail, String date, AcknowledgementInteractionListener listener) {
        InstaBookMerchantDialog f = new InstaBookMerchantDialog();
        f.setListener(listener);
        f.setContext(mContext);
        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putParcelable("merchant", detail);
        args.putString("date", date);


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

        merchantDetail = getArguments().getParcelable("merchant");
        date = getArguments().getString("date");
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

        mDialog = DataBindingUtil.inflate(inflater, R.layout.dialog_instabook_merchant_info, container, false);

        if (!TextUtils.isEmpty(merchantDetail.getProfileImg())) {
            Utility.showCircularImageView(mContext, TAG, mDialog.imgProfilePic, merchantDetail.getProfileImg(), R.drawable.ic_cheep_circular_icon, false, 0);
        } else {
            mDialog.imgProfilePic.setImageResource(Utility.DEFAULT_CHEEP_LOGO);
        }

        //Set Header Image

        // Set title
        mDialog.title.setText(merchantDetail.getUserName());


        if(TextUtils.isEmpty(merchantDetail.getAvgRatings()) || Float.parseFloat(merchantDetail.getAvgRatings()) == 0){
            mDialog.tvSubtitle.setText(TextUtils.concat(getString(R.string.total_experience, merchantDetail.getExperience())));
            mDialog.tvSubtitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        } else {

            mDialog.tvSubtitle.setText(TextUtils.concat(getString(R.string.total_experience, merchantDetail.getExperience()), " | ", String.format("%.1f", Float.parseFloat(merchantDetail.getAvgRatings()))));
        }
        if(!TextUtils.isEmpty(merchantDetail.getVerified()) && merchantDetail.getVerified().equalsIgnoreCase("yes")){
            mDialog.tvVerified.setVisibility(View.VISIBLE);
        } else{
            mDialog.tvVerified.setVisibility(View.GONE);
        }


        if(merchantDetail.getProLevel() != null) {
            if (merchantDetail.getProLevel().equals(Utility.PRO_LEVEL.PLATINUM))
                mDialog.ivBadge.setImageResource(R.drawable.ic_badge_platinum);
            else if (merchantDetail.getProLevel().equals(Utility.PRO_LEVEL.GOLD))
                mDialog.ivBadge.setImageResource(R.drawable.ic_badge_gold);
            else if (merchantDetail.getProLevel().equals(Utility.PRO_LEVEL.SILVER))
                mDialog.ivBadge.setImageResource(R.drawable.ic_badge_silver);
            else if (merchantDetail.getProLevel().equals(Utility.PRO_LEVEL.BRONZE))
                mDialog.ivBadge.setImageResource(R.drawable.ic_badge_bronze);
        }
        mDialog.tvBody.setText(getString(R.string.merchant_detail_body, merchantDetail.getUserName(), date));
        mDialog.tvBook.setText(getString(R.string.book_and_pay, "₹"+merchantDetail.getRate()));
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
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        getDialog().getWindow().setGravity(Gravity.CENTER);
    }
}