package com.cheep.cheepcare.dialogs;


import android.app.DialogFragment;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.cheep.R;
import com.cheep.databinding.DialogCheepRateCardBinding;


public class RateCardDialog extends DialogFragment {

    public static final String TAG = RateCardDialog.class.getSimpleName();
    private DialogCheepRateCardBinding mDialogBusyNoWorriesBinding;
    private SelectSpecificTimeDialog.DialogInteractionListener mListener;



    public static void newInstance(Context context) {
        RateCardDialog dialog = new RateCardDialog();

        dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), SelectSpecificTimeDialog.TAG);
    }

    private void show(FragmentManager supportFragmentManager, String tag) {
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
            //getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            //getDialog().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        mDialogBusyNoWorriesBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_cheep_rate_card, container, false);


        return mDialogBusyNoWorriesBinding.getRoot();
    }
}