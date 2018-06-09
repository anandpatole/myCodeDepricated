package com.cheep.cheepcarenew.fragments;

import android.content.Context;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;
import com.cheep.cheepcare.dialogs.RateCardDialog;
import com.cheep.databinding.FragmentCheepcareRateCardBinding;
import com.cheep.fragment.BaseFragment;
import com.cheep.interfaces.DrawerLayoutInteractionListener;

import com.cheep.utils.Utility;

public class CheepCareRateCardFragment  extends BaseFragment {

    public static final String TAG = "CheepCareRateCardFragment";
    private FragmentCheepcareRateCardBinding mBinding;
    private DrawerLayoutInteractionListener mListener;

    public static CheepCareRateCardFragment newInstance() {
        Bundle args = new Bundle();
        CheepCareRateCardFragment fragment = new CheepCareRateCardFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_cheepcare_rate_card, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        Log.i(TAG, "onAttach: ");
        super.onAttach(context);
        if (context instanceof DrawerLayoutInteractionListener) {
            this.mListener = (DrawerLayoutInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initiateUI();
        setListener();
    }

    @Override
    public void initiateUI() {
        if (((AppCompatActivity) mContext).getSupportActionBar() != null) {
            //Setting up toolbar
            ((AppCompatActivity) mContext).setSupportActionBar(mBinding.toolbar);
            ((AppCompatActivity) mContext).getSupportActionBar().setTitle(Utility.EMPTY_STRING);
        }

        //Provide callback to activity to link drawerlayout with toolbar
        mListener.setUpDrawerLayoutWithToolBar(mBinding.toolbar);
    }

    @Override
    public void setListener() {
        mBinding.textElectrician.setOnClickListener(onClickListener);
        mBinding.textPainter.setOnClickListener(onClickListener);
        mBinding.textPlumber.setOnClickListener(onClickListener);
        mBinding.textRepairs.setOnClickListener(onClickListener);
mBinding.textApplicationRepair.setOnClickListener(onClickListener);
mBinding.textTechRepair.setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case  R.id.text_painter:
                    RateCardDialog.newInstance((AppCompatActivity) mContext);
                    break;
                 default:
                     RateCardDialog.newInstance((AppCompatActivity) mContext);
                     break;


            }
        }
    };
}
