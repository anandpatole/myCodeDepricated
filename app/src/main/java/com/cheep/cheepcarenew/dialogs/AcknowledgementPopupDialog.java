package com.cheep.cheepcarenew.dialogs;

import android.app.Dialog;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.cheep.R;
import com.cheep.cheepcarenew.activities.LandingScreenPickPackageActivity;
import com.cheep.databinding.AcknowledgementPopupDialogBinding;

public class AcknowledgementPopupDialog extends DialogFragment implements View.OnClickListener {

    public static final String TAG = AcknowledgementPopupDialog.class.getSimpleName();


    AcknowledgementListener acknowledgementListener;

    public void setAcknowledgementListener(AcknowledgementListener acknowledgementListener) {
        this.acknowledgementListener = acknowledgementListener;
    }

    private AcknowledgementPopupDialogBinding mBinding;
    Dialog dialog;


    public interface AcknowledgementListener{
        public void  onClickOfThanks();
    }


    public AcknowledgementPopupDialog() {
        // Required empty public constructor
    }


    public static AcknowledgementPopupDialog newInstance(AcknowledgementListener listener) {
        AcknowledgementPopupDialog fragment = new AcknowledgementPopupDialog();
        Bundle args = new Bundle();
        fragment.setAcknowledgementListener(listener);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimationZoomInOut;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.acknowledgement_popup_dialog, container, false);
        seListener();
        return mBinding.getRoot();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dialog = super.onCreateDialog(savedInstanceState);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        dialog.setCanceledOnTouchOutside(true);
        this.setCancelable(true);

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().getAttributes().windowAnimations = R.style.AlertAnimation;
        return dialog;

    }

    private void seListener(){
        mBinding.tvThanks.setOnClickListener(this);
    }

   //View.OnClickListener
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_thanks:
                acknowledgementListener.onClickOfThanks();
                dismissAllowingStateLoss();

                break;
        }
    }
}
