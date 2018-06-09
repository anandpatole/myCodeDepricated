package com.cheep.cheepcare.dialogs;



import android.content.Context;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.cheep.R;
import com.cheep.adapter.RateRecyclerViewAdapter;
import com.cheep.databinding.DialogCheepRateCardBinding;
import com.cheep.model.RateCardModel;

import java.util.ArrayList;


public class RateCardDialog extends DialogFragment {

    public static final String TAG = RateCardDialog.class.getSimpleName();
    private DialogCheepRateCardBinding mDialogBusyNoWorriesBinding;

static Context contexts;


    public static void newInstance(Context context) {
        RateCardDialog dialog = new RateCardDialog();
contexts=context;
        dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), RateCardDialog.TAG);

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
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            getDialog().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
getDialog().setCanceledOnTouchOutside(false);
        mDialogBusyNoWorriesBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_cheep_rate_card, container, false);

        //RecyclerView a=(RecyclerView) ((AppCompatActivity) contexts).findViewById(R.id.rate_card_recycler_view);
        ArrayList<RateCardModel> mlist=new ArrayList<>();
        RateCardModel model=new RateCardModel();
        model.setProduct("test");
        model.setRate("100");
        for (int i=0;i<15;i++)
        {
            mlist.add(model);
        }
        RateRecyclerViewAdapter adapter=new RateRecyclerViewAdapter(((AppCompatActivity) contexts),mlist);
        mDialogBusyNoWorriesBinding.rateCardRecyclerView.setLayoutManager(new LinearLayoutManager(contexts));
        mDialogBusyNoWorriesBinding.rateCardRecyclerView.setAdapter(adapter);
        mDialogBusyNoWorriesBinding.cancelRateCard.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        dismiss();
    }
});
        return mDialogBusyNoWorriesBinding.getRoot();
    }
}