package com.cheep.cheepcarenew.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cheep.R;


import com.cheep.adapter.RateRecyclerViewAdapter;
import com.cheep.databinding.FragmentRateCardPricingBinding;
import com.cheep.fragment.BaseFragment;
import com.cheep.model.RateCardModel;

import java.util.ArrayList;

public class CheepCareRateCardPricingFragment extends BaseFragment  {

    public static final String TAG = "CheepCareRateCardPricingFragment";
     //FragmentCheepCareRateCardSelectionBinding mBinding;
    private FragmentRateCardPricingBinding mBinding;
   static String category;
   static String subCategory;
   // private DrawerLayoutInteractionListener mListener;

    public static CheepCareRateCardPricingFragment newInstance(String s,String s1 ) {
        Bundle args = new Bundle();
      category=s;
      subCategory=s1;
        CheepCareRateCardPricingFragment fragment = new CheepCareRateCardPricingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
   mBinding= DataBindingUtil.inflate(inflater, R.layout.fragment_rate_card_pricing, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onAttach(Context context)
    {
        Log.i(TAG, "onAttach: ");
        super.onAttach(context);
       /* if (context instanceof DrawerLayoutInteractionListener) {
            this.mListener = (DrawerLayoutInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }*/
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
            ((AppCompatActivity) mContext).getSupportActionBar().setTitle(category);

        }
        mBinding.textTitle.setText(category);
ArrayList<RateCardModel> temp=new ArrayList<>();
        for (int i=0;i<10;i++)
        {
            RateCardModel r=new RateCardModel();
            r.setProduct("I need a tap to be installed");
            r.setRate("200");
            r.setRate_unit("100");
            temp.add(r);
        }

        RateRecyclerViewAdapter adapter=new RateRecyclerViewAdapter(temp);
mBinding.rateCardPricingRecycler.setLayoutManager(new LinearLayoutManager(mContext));
        mBinding.rateCardPricingRecycler.setNestedScrollingEnabled(false);
mBinding.rateCardPricingRecycler.setAdapter(adapter);
        //Provide callback to activity to link drawerlayout with toolbar
       // mListener.setUpDrawerLayoutWithToolBar(mBinding.toolbar);
    }

    @Override
    public void setListener() {
mBinding.relationBack.setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.relation_back:
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.content, CheepCareRateCardFragment.newInstance(), CheepCareRateCardFragment.TAG).commitAllowingStateLoss();
                 default:
                     //RateCardDialog.newInstance((AppCompatActivity) mContext);
                     break;


            }
        }
    };


}
