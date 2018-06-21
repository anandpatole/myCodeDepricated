package com.cheep.dialogs;


import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.cheep.databinding.DialogPestControlHelpBinding;
import com.cheep.R;

public class PestControlHelpDialog extends DialogFragment {
    public static final String TAG = "PestControlHelpDialog";
   private DialogPestControlHelpBinding mBinding;

    public interface PestControlHelpListener {
        void onHelpClick();

    }

   PestControlHelpListener pestControlHelpListener;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    public static PestControlHelpDialog newInstance (PestControlHelpListener pestControlHelpListener) {
        PestControlHelpDialog f = new PestControlHelpDialog();
        f.pestControlHelpListener = pestControlHelpListener;
        // Supply num input as an argument.
        Bundle args = new Bundle();
        return f;
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




        mBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_pest_control_help, container, false);



mBinding.textPestControlBtn.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        pestControlHelpListener.onHelpClick();
       // dismiss();
    }
});


//        mBinding.textP.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                urgentBookingListener.onPayNow();
//                dismiss();
//            }
//        });


        return mBinding.getRoot();
    }
}
