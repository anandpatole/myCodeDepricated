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
import com.cheep.adapter.RateCardSelectionRecyclerViewAdapter;
import com.cheep.databinding.FragmentCheepRateCardSelectionBinding;
import com.cheep.fragment.BaseFragment;

import java.util.ArrayList;

public class CheepCareRateCardSelectionFragment extends BaseFragment implements RateCardSelectionRecyclerViewAdapter.InteractionListener {

    public static final String TAG = "CheepCareRateCardSelectionFragment";
    //FragmentCheepCareRateCardSelectionBinding mBinding;
    private FragmentCheepRateCardSelectionBinding mBinding;
    static String category;
    String subCategory;
    // private DrawerLayoutInteractionListener mListener;

    public static CheepCareRateCardSelectionFragment newInstance(String s) {
        Bundle args = new Bundle();
        category = s;
        CheepCareRateCardSelectionFragment fragment = new CheepCareRateCardSelectionFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_cheep_rate_card_selection, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onAttach(Context context) {
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
        ArrayList<String> temp = new ArrayList<>();
        temp.add("Laptop");
        temp.add("Desktop");
        temp.add("MotherBoard");
        temp.add("Printer");
        RateCardSelectionRecyclerViewAdapter adapter = new RateCardSelectionRecyclerViewAdapter(temp, CheepCareRateCardSelectionFragment.this);
        mBinding.recyclerRateCardSelectionList.setLayoutManager(new LinearLayoutManager(mContext));
        mBinding.recyclerRateCardSelectionList.setNestedScrollingEnabled(false);
        mBinding.recyclerRateCardSelectionList.setAdapter(adapter);
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

    @Override
    public void onClicked(String s) {

        subCategory = s;
        //getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.content, CheepCareRateCardPricingFragment.newInstance(category, subCategory), CheepCareRateCardFragment.TAG).commitAllowingStateLoss();
    }
}
